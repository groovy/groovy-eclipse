/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for
 *	 							bug 185682 - Increment/decrement operators mark local variables as read
 *								bug 388800 - [1.8] adjust tests to 1.8 JRE
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

import junit.framework.Test;
/**
 * Name Lookup within Inner Classes
 * Creation date: (8/2/00 12:04:53 PM)
 * @author Dennis Conway
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class LookupTest extends AbstractRegressionTest {
public LookupTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

static {
//	TESTS_NAMES = new String [] { "test096" };
}
/**
 * Non-static member class
 */
public void test001() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private static int value = 23;					\n"+
			"	class B {										\n"+
			"		private int value;							\n"+
			"		B (int val) {								\n"+
			"			value = (A.value * 2) + val;			\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String args[]) {		\n"+
			"		int result = new A().new B(12).value; 		\n"+
			"		int expected = 58; 							\n"+
			"		System.out.println( 						\n"+
			"			result == expected 						\n"+
			"				? \"SUCCESS\"  						\n"+
			"				: \"FAILED : got \"+result+\" instead of \"+ expected); \n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 * Attempt to access non-static field from static inner class (illegal)
 */
public void test002() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private int value;								\n"+
			"	static class B {								\n"+
			"		B () {										\n"+
			"			value = 2;								\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		B result = new B();							\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" +
		"1. WARNING in p1\\A.java (at line 3)\n" +
		"	private int value;								\n" +
		"	            ^^^^^\n" +
		"The value of the field A.value is not used\n" +
		"----------\n" +
		"2. ERROR in p1\\A.java (at line 6)\n" +
		"	value = 2;								\n" +
		"	^^^^^\n" +
		"Cannot make a static reference to the non-static field value\n" +
		"----------\n");
}
/**
 * Access static field from static inner class
 */
public void test003() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private static int value;						\n"+
			"	static class B {								\n"+
			"		B () {										\n"+
			"			value = 2;								\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		B result = new B();							\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}",
		},
		"SUCCESS"
	);
}
/**
 *
 */
public void test004() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private String value;							\n"+
			"	private A (String strIn) {						\n"+
			"		value = new B(strIn, \"E\").str;			\n"+
			"	}												\n"+
			"	class B {										\n"+
			"		String str;									\n"+
			"			private B (String strFromA, String strIn)	{\n"+
			"				str = strFromA + strIn + new C(\"S\").str;\n"+
			"			}										\n"+
			"		class C {									\n"+
			"			String str;								\n"+
			"			private C (String strIn) {				\n"+
			"				str = strIn + new D(\"S\").str;		\n"+
			"			}										\n"+
			"			class D {								\n"+
			"				String str;							\n"+
			"				private D (String strIn) {			\n"+
			"					str = strIn;					\n"+
			"				}									\n"+
			"			}										\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		System.out.println(new A(\"SUCC\").value);	\n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 *
 */
public void test005() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private static void doSomething(String showThis) {\n"+
			"		System.out.print(showThis);					\n"+
			"		return;										\n"+
			"	}												\n"+
			"	class B {										\n"+
			"		void aMethod () {							\n"+
			"			p1.A.doSomething(\"SUCC\");				\n"+
			"			A.doSomething(\"ES\");					\n"+
			"			doSomething(\"S\");						\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		B foo = new A().new B();					\n"+
			"		foo.aMethod();								\n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 * jdk1.2.2 reports: No variable sucess defined in nested class p1.A. B.C.
 * jdk1.3 reports: success has private access in p1.A
 */
public void test006() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private static String success = \"SUCCESS\";	\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(this.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in p1\\A.java (at line 7)\n" +
		"	public void aTask() {System.out.println(this.success);}\n" +
		"	                                             ^^^^^^^\n" +
		"The field A.success is not visible\n" +
		"----------\n" +
		"2. WARNING in p1\\A.java (at line 7)\n" +
		"	public void aTask() {System.out.println(this.success);}\n" +
		"	                                             ^^^^^^^\n" +
		"The static field A.success should be accessed in a static way\n" +
		"----------\n");
}
/**
 * No errors in jdk1.2.2, jdk1.3
 */
public void test007() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private static String success = \"SUCCESS\";	\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(A.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 * jdk1.2.2 reports: Undefined variable: A.this
 * jdk1.3 reports: non-static variable this cannot be referenced from a static context
 */
public void test008() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private static String success = \"SUCCESS\";	\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(A.this.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in p1\\A.java (at line 7)\n" +
		"	public void aTask() {System.out.println(A.this.success);}\n" +
		"	                                        ^^^^^^\n" +
		"No enclosing instance of the type A is accessible in scope\n" +
		"----------\n" +
		"2. WARNING in p1\\A.java (at line 7)\n" +
		"	public void aTask() {System.out.println(A.this.success);}\n" +
		"	                                               ^^^^^^^\n" +
		"The static field A.success should be accessed in a static way\n" +
		"----------\n"
	);
}
/**
 * jdk1.2.2 reports: No variable success defined in nested class p1.A. B.C
 * jdk1.3 reports: success has private access in p1.A
 */
public void test009() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private String success = \"SUCCESS\";			\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(this.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in p1\\A.java (at line 7)\n" +
		"	public void aTask() {System.out.println(this.success);}\n" +
		"	                                             ^^^^^^^\n" +
		"The field A.success is not visible\n" +
		"----------\n");
}
/**
 * jdk1.2.2 reports: Can't make a static reference to nonstatic variable success in class p1.A
 * jdk1.3 reports: non-static variable success cannot be referenced from a static context
 */
public void test010() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private String success = \"SUCCESS\";			\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(A.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" +
		"1. WARNING in p1\\A.java (at line 3)\n" +
		"	private String success = \"SUCCESS\";			\n" +
		"	               ^^^^^^^\n" +
		"The value of the field A.success is not used\n" +
		"----------\n" +
		"2. ERROR in p1\\A.java (at line 7)\n" +
		"	public void aTask() {System.out.println(A.success);}\n" +
		"	                                        ^^^^^^^^^\n" +
		"Cannot make a static reference to the non-static field A.success\n" +
		"----------\n");
}
/**
 *
 */
public void test011() {
	this.runNegativeTest(
		new String[] {
			/* p2.Aa */
			"p2/Aa.java",
			"package p2;										\n"+
			"class Aa extends p1.A{								\n"+
			"	class B implements p1.A.C {						\n"+
			"	}												\n"+
			"	public static void main (String args[]) {		\n"+
			"	}												\n"+
			"}",
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"   public A() {									\n"+
			"	}												\n"+
			"	class B implements C {							\n"+
			"		public int sMethod() {						\n"+
			"			return 23;								\n"+
			"		}											\n"+
			"	}												\n"+
			"	public interface C {							\n"+
			"		public abstract int sMethod();				\n"+
			"	}												\n"+
			"}",

		},
		"----------\n" +
		"1. ERROR in p2\\Aa.java (at line 3)\n" +
		"	class B implements p1.A.C {						\n" +
		"	      ^\n" +
		"The type Aa.B must implement the inherited abstract method A.C.sMethod()\n" +
		"----------\n"
	);
}
/**
 *
 */
public void test012() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	public interface B {							\n"+
			"		public abstract void aMethod (int A);		\n"+
			"		public interface C {						\n"+
			"			public abstract void anotherMethod();	\n"+
			"		}											\n"+
			"	}												\n"+
			"	public class aClass implements B, B.C {			\n"+
			"		public void aMethod (int A) {				\n"+
			"		}											\n"+
			"		public void anotherMethod(){}				\n"+
			"	}												\n"+
			"   	public static void main (String argv[]) {	\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 *
 */
public void test013() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	public interface B {							\n"+
			"		public abstract void aMethod (int A);		\n"+
			"		public interface C {						\n"+
			"			public abstract void anotherMethod(int A);\n"+
			"		}											\n"+
			"	}												\n"+
			"	public class aClass implements B, B.C {			\n"+
			"		public void aMethod (int A) {				\n"+
			"			public void anotherMethod(int A) {};	\n"+
			"		}											\n"+
			"	}												\n"+
			"   	public static void main (String argv[]) {	\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in p1\\A.java (at line 9)\n" +
		"	public class aClass implements B, B.C {			\n" +
		"	             ^^^^^^\n" +
		"The type A.aClass must implement the inherited abstract method A.B.C.anotherMethod(int)\n" +
		"----------\n" +
		(this.complianceLevel < ClassFileConstants.JDK14
		?
		"2. ERROR in p1\\A.java (at line 11)\n" +
		"	public void anotherMethod(int A) {};	\n" +
		"	                         ^\n" +
		"Syntax error on token \"(\", ; expected\n" +
		"----------\n" +
		"3. ERROR in p1\\A.java (at line 11)\n" +
		"	public void anotherMethod(int A) {};	\n" +
		"	                               ^\n" +
		"Syntax error on token \")\", ; expected\n"
		:
		"1. ERROR in p1\\A.java (at line 9)\n" +
		"	public class aClass implements B, B.C {			\n" +
		"	             ^^^^^^\n" +
		"The type A.aClass must implement the inherited abstract method A.B.C.anotherMethod(int)\n" +
		"----------\n" +
		"2. ERROR in p1\\A.java (at line 11)\n" +
		"	public void anotherMethod(int A) {};	\n" +
		"	       ^^^^\n" +
		"Syntax error on token \"void\", record expected\n"
		) +
		"----------\n"
	);
}
/**
 *
 */
public void test014() {
	this.runNegativeTest(
		new String[] {
			/* pack1.First */
			"pack1/First.java",
			"package pack1;										\n"+
			"public class First {								\n"+
			"	public static void something() {}				\n"+
			"		class Inner {}								\n"+
			"	public static void main (String argv[]) {		\n"+
			"		First.Inner foo = new First().new Inner();	\n"+
			"		foo.something();							\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in pack1\\First.java (at line 7)\n" +
		"	foo.something();							\n" +
		"	    ^^^^^^^^^\n" +
		"The method something() is undefined for the type First.Inner\n" +
		"----------\n"
	);
}
/**
 *
 */
public void test015() {
	this.runConformTest(
		new String[] {
			/* pack1.First */
			"pack1/First.java",
			"package pack1;										\n"+
			"public class First {								\n"+
			"		class Inner {								\n"+
			"			public void something() {}				\n"+
			"		}											\n"+
			"	public static void main (String argv[]) {		\n"+
			"		First.Inner foo = new First().new Inner();	\n"+
			"		foo.something();							\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 *
 */
public void test016() {
	this.runConformTest(
		new String[] {
			/* pack1.Outer */
			"pack1/Outer.java",
			"package pack1;										\n"+
			"import pack2.*;									\n"+
			"public class Outer {								\n"+
			"	int time, distance;								\n"+
			"	public Outer() {								\n"+
			"	}												\n"+
			"	public Outer(int d) {							\n"+
			"		distance = d;								\n"+
			"	}												\n"+
			"	public void aMethod() {							\n"+
			"		this.distance *= 2;							\n"+
			"		return;										\n"+
			"	}												\n"+
			"}",
			/* pack2.OuterTwo */
			"pack2/OuterTwo.java",
			"package pack2;										\n"+
			"import pack1.*;									\n"+
			"public class OuterTwo extends Outer {				\n"+
			"	public OuterTwo(int bar) {						\n"+
			"		Outer A = new Outer(3) {					\n"+
			"			public void bMethod(){					\n"+
			"				final class X {						\n"+
			"					int price;						\n"+
			"					public X(int inp) {				\n"+
			"						price = inp + 32;			\n"+
			"					}								\n"+
			"				}									\n"+
			"			}										\n"+
			"		};											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		System.out.println(\"\");					\n"+
			"		OuterTwo foo = new OuterTwo(12);			\n"+
			"		Outer bar = new Outer(8);					\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test017() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	int value;										\n"+
			"	public A(B bVal) {								\n"+
			"		bVal.sval += \"V\";							\n"+
			"	}												\n"+
			"	static class B {								\n"+
			"		public static String sval;					\n"+
			"		public void aMethod() {						\n"+
			"			sval += \"S\";							\n"+
			"			A bar = new A(this);					\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		B foo = new B();							\n"+
			"		foo.sval = \"U\";							\n"+
			"		foo.aMethod();								\n"+
			"		System.out.println(foo.sval);				\n"+
			"	}												\n"+
			"}"
		},
		"USV"
	);
}
/**
 * member class
 */
public void test018() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	private String rating;							\n"+
			"	public class B {								\n"+
			"		String rating;								\n"+
			"		public B (A sth) {							\n"+
			"			sth.rating = \"m\";						\n"+
			"			rating = \"er\";						\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		A foo = new A();							\n"+
			"		foo.rating = \"o\";							\n"+
			"		B bar = foo.new B(foo);						\n"+
			"		System.out.println(foo.rating + bar.rating);\n"+
			"	}												\n"+
			"}"
		},
		"mer"
	);
}
/**
 * member class
 */
public void test019() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	private String rating;							\n"+
			"	public void setRating(A sth, String setTo) {	\n"+
			"		sth.rating = setTo;							\n"+
			"		return;										\n"+
			"	}												\n"+
			"	public class B {								\n"+
			"		public B (A sth) {							\n"+
			"			setRating(sth, \"m\");					\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		A foo = new A();							\n"+
			"		foo.rating = \"o\";							\n"+
			"		B bar = foo.new B(foo);						\n"+
			"		System.out.println(foo.rating + bar.other);	\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in p1\\A.java (at line 17)\n" +
		"	System.out.println(foo.rating + bar.other);	\n" +
		"	                                    ^^^^^\n" +
		"other cannot be resolved or is not a field\n" +
		"----------\n"
	);
}
/**
 * member class
 */
public void test020() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"----------\n" +
			"1. ERROR in p1\\A.java (at line 13)\n" +
			"	System.out.println(foo.rating + bar.other);	\n" +
			"	                                    ^^^^^\n" +
			"other cannot be resolved or is not a field\n" +
			"----------\n"
			:
			"----------\n" +
			"1. WARNING in p1\\A.java (at line 6)\n" +
			"	sth.rating = \"m\";						\n" +
			"	    ^^^^^^\n" +
			"Write access to enclosing field A.rating is emulated by a synthetic accessor method\n" +
			"----------\n" +
			"2. ERROR in p1\\A.java (at line 13)\n" +
			"	System.out.println(foo.rating + bar.other);	\n" +
			"	                                    ^^^^^\n" +
			"other cannot be resolved or is not a field\n" +
			"----------\n";
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	private String rating;							\n"+
			"	public class B {								\n"+
			"		public B (A sth) {							\n"+
			"			sth.rating = \"m\";						\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		A foo = new A();							\n"+
			"		foo.rating = \"o\";							\n"+
			"		B bar = foo.new B(foo);						\n"+
			"		System.out.println(foo.rating + bar.other);	\n"+
			"	}												\n"+
			"}"
		},
		errMessage);
}
/**
 * member class
 */
public void test021() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	private String rating;							\n"+
			"	public class B {								\n"+
			"		public B (A sth) {							\n"+
			"			sth.rating = \"m\";						\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		A foo = new A();							\n"+
			"		foo.rating = \"o\";							\n"+
			"		B bar = foo.new B(foo);						\n"+
			"		System.out.println(foo.rating);				\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test022() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"import p2.*;										\n"+
			"public class A {									\n"+
			"	public int aValue;								\n"+
			"	public A() {}									\n"+
			"	public static class C extends A {				\n"+
			"		public String aString;						\n"+
			"		public C() {								\n"+
			"		}											\n"+
			"	}												\n"+
			"}",
			/* p2.B */
			"p2/B.java",
			"package p2;										\n"+
			"import p1.*;										\n"+
			"public class B extends A.C {						\n"+
			"	public B() {}									\n"+
			"	public class D extends A {						\n"+
			"		public D() {								\n"+
			"			C val2 = new C();						\n"+
			"			val2.aString = \"s\";					\n"+
			"			A val = new A();						\n"+
			"			val.aValue = 23;						\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		D foo = new B().new D();					\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test023() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;\n"+
			"public class A implements B {						\n"+
			"}													\n"+
			"interface B {										\n"+
			"	public class A implements B {					\n"+
			"		public static void main (String argv[]) {	\n"+
			"			class Ba {								\n"+
			"				int time;							\n"+
			"			}										\n"+
			"			Ba foo = new Ba();						\n"+
			"			foo.time = 3;							\n"+
			"		}											\n"+
			"		interface C {								\n"+
			"		}											\n"+
			"		interface Bb extends C {					\n"+
			"		}											\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test024() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;									\n"+
			"public class A {								\n"+
			"	protected static String bleh;				\n"+
			"	interface B {								\n"+
			"		public String bleh();					\n"+
			"		class C{								\n"+
			"			public String bleh() {return \"B\";}\n"+
			"		}										\n"+
			"	}											\n"+
			"	class C implements B {						\n"+
			"		public String bleh() {return \"A\";}	\n"+
			"	}											\n"+
			"	public static void main(String argv[]) {	\n"+
			"		C foo = new A().new C();				\n"+
			"	}											\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test025() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;								\n"+
			"import p2.*;								\n"+
			"public class A {							\n"+
			"	public static class B {					\n"+
			"		public static int B;				\n"+
			"	}										\n"+
			"	public static void main(String argv[]) {\n"+
			"		B foo = new A.B();					\n"+
			"		B bar = new B();					\n"+
			"		foo.B = 2;							\n"+
			"		p2.B bbar = new p2.B();				\n"+
			"		if (bar.B == 35) {					\n"+
			"			System.out.println(\"SUCCESS\");\n"+
			"		}									\n"+
			"		else {								\n"+
			"			System.out.println(bar.B);		\n"+
			"		}									\n"+
			"	}										\n"+
			"}",
			"p2/B.java",
			"package p2;								\n"+
			"import p1.*;								\n"+
			"public class B extends A {					\n"+
			"	public B() {							\n"+
			"		A.B bleh = new A.B();				\n"+
			"		bleh.B = 35;						\n"+
			"	}										\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 *
 */
public void test026() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;								\n"+
			"public class A {							\n"+
			"	public static class B {					\n"+
			"		protected static int B;				\n"+
			"	}										\n"+
			"	public static void main(String argv[]) {\n"+
			"		B foo = new A.B();					\n"+
			"		B bar = new B();					\n"+
			"		B.B = 2;							\n"+
			"		p2.B bbar = new p2.B();				\n"+
			"		if (B.B == 35) {					\n"+
			"			System.out.println(\"SUCCESS\");\n"+
			"		}									\n"+
			"		else {								\n"+
			"			System.out.println(B.B);		\n"+
			"		}									\n"+
			"	}										\n"+
			"}",
			"p2/B.java",
			"package p2;								\n"+
			"import p1.*;								\n"+
			"public class B extends A {					\n"+
			"	public B() {							\n"+
			"		A.B bleh = new A.B();				\n"+
			"		bleh.B = 35;						\n"+
			"	}										\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in p2\\B.java (at line 6)\n" +
		"	bleh.B = 35;						\n" +
		"	     ^\n" +
		"The field A.B.B is not visible\n" +
		"----------\n");
}
/**
 *
 */
public void test027() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;								\n"+
			"public class A {							\n"+
			"	protected static class B {				\n"+
			"		public static int B;				\n"+
			"	}										\n"+
			"	public static void main(String argv[]) {\n"+
			"		B foo = new A.B();					\n"+
			"		B bar = new B();					\n"+
			"		B.B = 2;							\n"+
			"		p2.B bbar = new p2.B();				\n"+
			"		if (B.B == 35) {					\n"+
			"			System.out.println(\"SUCCESS\");\n"+
			"		}									\n"+
			"		else {								\n"+
			"			System.out.println(B.B);		\n"+
			"		}									\n"+
			"	}										\n"+
			"}",
			"p2/B.java",
			"package p2;								\n"+
			"import p1.*;								\n"+
			"public class B extends A {					\n"+
			"	public B() {							\n"+
			"		A.B bleh = new A.B();				\n"+
			"		A.B.B = 35;						\n"+
			"	}										\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in p2\\B.java (at line 5)\n" +
		"	A.B bleh = new A.B();				\n" +
		"	           ^^^^^^^^^\n" +
		"The constructor A.B() is not visible\n" +
		"----------\n"
	);
}
/**
 *
 */
public void test028() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;									\n"+
			"public class A {								\n"+
			"	static class B {							\n"+
			"		public static class C {					\n"+
			"			private static int a;				\n"+
			"			private int b;						\n"+
			"		}										\n"+
			"	}											\n"+
			"	class D extends B {							\n"+
			"		int j = p1.A.B.C.a;						\n"+
			"	}											\n"+
			"	public static void main (String argv[]) {	\n"+
			"		System.out.println(\"SUCCESS\");		\n"+
			"	}											\n"+
			"}"
		},
		"SUCCESS"
	);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=10634
 */
public void test029() {
	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"import p2.Top;	\n"+
			"public class X extends Top {	\n"+
			"	Member field;	\n"+
			"}	\n",
			"p2/Top.java",
			"package p2;	\n"+
			"public class Top {	\n"+
			"	class Member {	\n"+
			"		void foo(){}	\n"+
			"	}	\n"	+
			"}	\n"
		},
		"----------\n" +
		"1. ERROR in p1\\X.java (at line 4)\n" +
		"	Member field;	\n" +
		"	^^^^^^\n" +
		"The type Member is not visible\n" +
		"----------\n");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11435
 * 1.3 compiler must accept classfiles without abstract method (target >=1.2)
 */
public void test030() {

	Hashtable target1_2 = new Hashtable();
	target1_2.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_2);

	this.runConformTest(
		new String[] {
			"p1/A.java",
			"package p1; \n"+
			"public abstract class A implements I {	\n" +
			"  public static void main(String[] args) {	\n" +
			"    System.out.println(\"SUCCESS\");	\n" +
			"  }	\n" +
			"} \n" +
			"interface I {	\n" +
			"	void foo();	\n" +
			"}	\n",
		},
		"SUCCESS", // expected output
		null, // custom classpath
		true, // flush previous output dir content
		null, // special vm args
		target1_2,  // custom options
		null/*no custom requestor*/);

	this.runConformTest(
		new String[] {
			"p1/C.java",
			"package p1; \n"+
			"public class C {	\n" +
			"	void bar(A a){ \n" +
			"		a.foo();	\n" +
			"	}	\n" +
			"  public static void main(String[] args) {	\n" +
			"    System.out.println(\"SUCCESS\");	\n" +
			"  }	\n" +
			"} \n"
		},
		"SUCCESS", // expected output
		null, // custom classpath
		false, // flush previous output dir content
		null, // special vm args
		null,  // custom options
		null/*no custom requestor*/);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - must filter abstract methods when searching concrete methods
 */
public void test031() {

	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X extends AbstractY {	\n"+
			"	public void init() {	\n"+
			"		super.init();	\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().init();	\n"+
			"	}	\n"+
			"}	\n"+
			"abstract class AbstractY extends AbstractZ implements I {	\n"+
			"	public void init(int i) {	\n"+
			"	}	\n"+
			"}	\n"+
			"abstract class AbstractZ implements I {	\n"+
			"	public void init() {	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"}	\n"+
			"interface I {	\n"+
			"	void init();	\n"+
			"	void init(int i);	\n"+
			"}	\n"
		},
		"SUCCESS"); // expected output
}

/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=29211
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=29213
 */
public void test032() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------
			"public class X {\n" +
			"	public static void main(String[] arguments) {\n" +
			"		System.out.println(p.Bar.array[0].length);\n" +
			"		System.out.println(p.Bar.array.length);\n" +
			"		System.out.println(p.Bar.array[0].foo());\n" +
			"	}\n" +
			"}\n",
			"p/Bar.java", //----------------------------
			"package p;\n" +
			"public class Bar {\n" +
			"	public static Z[] array;\n" +
			"}\n" +
			"class Z {\n" +
			"	public String foo(){ \n" +
			"		return \"\";\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	System.out.println(p.Bar.array[0].length);\n" +
		"	                   ^^^^^^^^^^^^^^\n" +
		"The type Z is not visible\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	System.out.println(p.Bar.array.length);\n" +
		"	                   ^^^^^^^^^^^\n" +
		"The type Z is not visible\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	System.out.println(p.Bar.array[0].foo());\n" +
		"	                   ^^^^^^^^^^^^^^\n" +
		"The type Z is not visible\n" +
		"----------\n");
}

// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test033() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"package p;	\n" +
			"public abstract class X {	\n" +
			"	abstract void foo();	\n" +
			"}	\n",
			"q/Y.java", //==================================
			"package q;	\n" +
			"public class Y extends p.X {	\n" +
			"	void foo(){}	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in q\\Y.java (at line 2)\n" +
		"	public class Y extends p.X {	\n" +
		"	             ^\n" +
		"This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Y. Either make the type abstract or make the inherited method visible\n" +
		"----------\n" +
		"2. WARNING in q\\Y.java (at line 3)\n" +
		"	void foo(){}	\n" +
		"	     ^^^^^\n" +
		"The method Y.foo() does not override the inherited method from X since it is private to a different package\n" +
		"----------\n");
}

// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test034() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"package p;	\n" +
			"public abstract class X {	\n" +
			"	abstract void foo();	\n" +
			"}	\n",
			"q/Y.java", //==================================
			"package q;	\n" +
			"public abstract class Y extends p.X {	\n" +
			"	void foo(){}	\n" +
			"}	\n" +
			"class Z extends Y {	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. WARNING in q\\Y.java (at line 3)\n" +
		"	void foo(){}	\n" +
		"	     ^^^^^\n" +
		"The method Y.foo() does not override the inherited method from X since it is private to a different package\n" +
		"----------\n" +
		"2. ERROR in q\\Y.java (at line 5)\n" +
		"	class Z extends Y {	\n" +
		"	      ^\n" +
		"This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible\n" +
		"----------\n"
);
}

// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test035() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"package p;	\n" +
			"public abstract class X {	\n" +
			"	abstract void foo();	\n" +
			"	abstract void bar();	\n" +
			"}	\n",
			"p/Y.java", //==================================
			"package p;	\n" +
			"public abstract class Y extends X {	\n" +
			"	void foo(){};	\n" +
			"}	\n",
			"q/Z.java", //==================================
			"package q;	\n" +
			"class Z extends p.Y {	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in q\\Z.java (at line 2)\n" +
		"	class Z extends p.Y {	\n" +
		"	      ^\n" +
		"This class must implement the inherited abstract method X.bar(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible\n" +
		"----------\n");
}
// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test036() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"package p;	\n" +
			"public abstract class X {	\n" +
			"	abstract void foo();	\n" +
			"	public interface I {	\n" +
			"		void foo();	\n" +
			"	}	\n" +
			"}	\n",
			"q/Y.java", //==================================
			"package q;	\n" +
			"public abstract class Y extends p.X {	\n" +
			"	void foo(){}	\n" +
			"}	\n" +
			"class Z extends Y implements p.X.I {	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. WARNING in q\\Y.java (at line 3)\n" +
		"	void foo(){}	\n" +
		"	     ^^^^^\n" +
		"The method Y.foo() does not override the inherited method from X since it is private to a different package\n" +
		"----------\n" +
		"2. ERROR in q\\Y.java (at line 5)\n" +
		"	class Z extends Y implements p.X.I {	\n" +
		"	      ^\n" +
		"This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible\n" +
		"----------\n" + // TODO (philippe) should not have following error due to default abstract?
		"3. ERROR in q\\Y.java (at line 5)\n" +
		"	class Z extends Y implements p.X.I {	\n" +
		"	      ^\n" +
		"The inherited method Y.foo() cannot hide the public abstract method in X.I\n" +
		"----------\n");
}
// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test037() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"package p;	\n" +
			"public abstract class X {	\n" +
			"	abstract void foo();	\n" +
			"	void bar(){}	\n" +
			"}	\n",
			"q/Y.java", //==================================
			"package q;	\n" +
			"public abstract class Y extends p.X {	\n" +
			"	void foo(){}	//warn \n" +
			"	void bar(){}	//warn \n" +
			"}	\n" +
			"class Z extends Y {	\n" +
			"	void bar(){}	//nowarn \n" +
			"}	\n",
		},
		"----------\n" +
		"1. WARNING in q\\Y.java (at line 3)\n" +
		"	void foo(){}	//warn \n" +
		"	     ^^^^^\n" +
		"The method Y.foo() does not override the inherited method from X since it is private to a different package\n" +
		"----------\n" +
		"2. WARNING in q\\Y.java (at line 4)\n" +
		"	void bar(){}	//warn \n" +
		"	     ^^^^^\n" +
		"The method Y.bar() does not override the inherited method from X since it is private to a different package\n" +
		"----------\n" +
		"3. ERROR in q\\Y.java (at line 6)\n" +
		"	class Z extends Y {	\n" +
		"	      ^\n" +
		"This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible\n" +
		"----------\n");
}
// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test038() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"package p;	\n" +
			"public abstract class X {	\n" +
			"	abstract void foo();	\n" +
			"}	\n",
			"q/Y.java", //==================================
			"package q;	\n" +
			"public abstract class Y extends p.X {	\n" +
			"	void foo(){}	//warn \n" +
			"}	\n" +
			"class Z extends Y {	\n" +
			"	void foo(){}	//error \n" +
			"}	\n",
		},
		"----------\n" +
		"1. WARNING in q\\Y.java (at line 3)\n" +
		"	void foo(){}	//warn \n" +
		"	     ^^^^^\n" +
		"The method Y.foo() does not override the inherited method from X since it is private to a different package\n" +
		"----------\n" +
		"2. ERROR in q\\Y.java (at line 5)\n" +
		"	class Z extends Y {	\n" +
		"	      ^\n" +
		"This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible\n" +
		"----------\n");
}

// 31198 - regression after 30805 - Abstract non-visible method diagnosis fooled by intermediate declarations
public void test039() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"package p;	\n" +
			"public abstract class X {	\n" +
			"	abstract void foo();	\n" + // should not complain about this one in Z, since it has a visible implementation
			"	abstract void bar();	\n" +
			"}	\n",
			"p/Y.java", //==================================
			"package p;	\n" +
			"public abstract class Y extends X {	\n" +
			"	public void foo(){};	\n" +
			"}	\n",
			"q/Z.java", //==================================
			"package q;	\n" +
			"class Z extends p.Y {	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in q\\Z.java (at line 2)\n" +
		"	class Z extends p.Y {	\n" +
		"	      ^\n" +
		"This class must implement the inherited abstract method X.bar(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible\n" +
		"----------\n");
}

/*
 * 31398 - non-visible abstract method fooling method verification - should not complain about foo() or bar()
 */
public void test040() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //================================
			"package p;	\n" +
			"public class X extends q.Y.Member {	\n" +
			"		void baz(){}	\n" + // doesn't hide Y.baz()
			"}	\n",
			"q/Y.java", //================================
			"package q;	\n" +
			"public abstract class Y {	\n" +
			"	abstract void foo();	\n" +
			"	abstract void bar();	\n" +
			"	abstract void baz();	\n" +
			"	public static abstract class Member extends Y {	\n" +
			"		public void foo() {}	\n" +
			"		void bar(){}	\n" +
			"	}	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 2)\n" +
		"	public class X extends q.Y.Member {	\n" +
		"	             ^\n" +
		"This class must implement the inherited abstract method Y.baz(), but cannot override it since it is not visible from X. Either make the type abstract or make the inherited method visible\n" +
		"----------\n" +
		"2. WARNING in p\\X.java (at line 3)\n" +
		"	void baz(){}	\n" +
		"	     ^^^^^\n" +
		"The method X.baz() does not override the inherited method from Y since it is private to a different package\n" +
		"----------\n");
}

/*
 * 31450 - non-visible abstract method fooling method verification - should not complain about foo()
 */
public void test041() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //================================
			"package p;	\n" +
			"public class X extends q.Y.Member {	\n" +
			"	public void foo() {}	\n" +
			"	public static class M extends X {}	\n" +
			"}	\n",
			"q/Y.java", //================================
			"package q;	\n" +
			"public abstract class Y {	\n" +
			"	abstract void foo();	\n" +
			"	abstract void bar();	\n" +
			"	public static abstract class Member extends Y {	\n" +
			"		protected abstract void foo();	\n" + // takes precedence over inherited abstract Y.foo()
			"	}	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 2)\n" +
		"	public class X extends q.Y.Member {	\n" +
		"	             ^\n" +
		"This class must implement the inherited abstract method Y.bar(), but cannot override it since it is not visible from X. Either make the type abstract or make the inherited method visible\n" +
		"----------\n" +
		"2. ERROR in p\\X.java (at line 4)\n" +
		"	public static class M extends X {}	\n" +
		"	                    ^\n" +
		"This class must implement the inherited abstract method Y.bar(), but cannot override it since it is not visible from M. Either make the type abstract or make the inherited method visible\n" +
		"----------\n");
}

/*
 * 31450 - non-visible abstract method fooling method verification - should not complain about foo()
 */
public void test042() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //================================
			"package p;	\n" +
			"public class X extends q.Y.Member {	\n" +
			"	public void foo() {}	\n" +
			"	public static class M extends X {}	\n" +
			"}	\n",
			"q/Y.java", //================================
			"package q;	\n" +
			"public abstract class Y {	\n" +
			"	abstract void foo();	\n" +
			"	abstract void bar();	\n" +
			"	public static abstract class Member extends Y {	\n" +
			"		void foo(){}	\n" +
			"	}	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 2)\n" +
		"	public class X extends q.Y.Member {	\n" +
		"	             ^\n" +
		"This class must implement the inherited abstract method Y.bar(), but cannot override it since it is not visible from X. Either make the type abstract or make the inherited method visible\n" +
		"----------\n" +
		"2. WARNING in p\\X.java (at line 3)\n" +
		"	public void foo() {}	\n" +
		"	            ^^^^^\n" +
		"The method X.foo() does not override the inherited method from Y.Member since it is private to a different package\n" +
		"----------\n" +
		"3. ERROR in p\\X.java (at line 4)\n" +
		"	public static class M extends X {}	\n" +
		"	                    ^\n" +
		"This class must implement the inherited abstract method Y.bar(), but cannot override it since it is not visible from M. Either make the type abstract or make the inherited method visible\n" +
		"----------\n");
}

public void test043() {
	this.runConformTest(
		new String[] {
			"X.java", //================================
			"public class X {\n" +
			"	public interface Copyable extends Cloneable {\n" +
			"		public Object clone() throws CloneNotSupportedException;\n" +
			"	}\n" +
			"\n" +
			"	public interface TestIf extends Copyable {\n" +
			"	}\n" +
			"\n" +
			"	public static class ClassA implements Copyable {\n" +
			"		public Object clone() throws CloneNotSupportedException {\n" +
			"			return super.clone();\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static class ClassB implements TestIf {\n" +
			"		public Object clone() throws CloneNotSupportedException {\n" +
			"			return super.clone();\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		Copyable o1 = new ClassA();\n" +
			"		ClassB o2 = new ClassB();\n" +
			"		TestIf o3 = o2;\n" +
			"		Object clonedObject;\n" +
			"		clonedObject = o1.clone();\n" +
			"		clonedObject = o2.clone();\n" +
			"		clonedObject = o3.clone();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}"
		},
		"SUCCESS");
}
/*
 * 62639 - check that missing member type is not noticed if no direct connection with compiled type
 */
public void test044() {
	this.runConformTest(
		new String[] {
			"p/Dumbo.java",
			"package p;\n" +
			"public class Dumbo {\n" +
			"  public class Clyde { }\n" +
			"	public static void main(String[] args) {\n" +
			"		  System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");
	// delete binary file Dumbo$Clyde (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p" + File.separator + "Dumbo$Clyde.class"));

	this.runConformTest(
		new String[] {
			"q/Main.java",
			"package q;\n" +
			"public class Main extends p.Dumbo {\n" +
			"	public static void main(String[] args) {\n" +
			"		  p.Dumbo d;\n" +
			"		  System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS",
		null,
		false,
		null);
}
/*
 * ensure that can still found binary member types at depth >=2 (enclosing name Dumbo$Clyde $ Fred)
 */
public void test045() {
	this.runConformTest(
		new String[] {
			"p/Dumbo.java",
			"package p;\n" +
			"public class Dumbo {\n" +
			"  public class Clyde {\n" +
			"  	  public class Fred {\n" +
			"	  }\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		  System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");

	this.runConformTest(
		new String[] {
			"q/Main.java",
			"package q;\n" +
			"public class Main extends p.Dumbo {\n" +
			"	public static void main(String[] args) {\n" +
			"		  p.Dumbo.Clyde.Fred f;\n" +
			"		  System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS",
		null,
		false,
		null);
}
public void test046() {
	this.runNegativeTest(
		new String[] {
			"X.java", //================================
			"public class X {\n" +
			"     private XY foo(XY t) {\n" +
			"        System.out.println(t);\n" +
			"        return t;\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        new X() {\n" +
			"            void run() {\n" +
			"                foo(new XY());\n" +
			"            }\n" +
			"        }.run();\n" +
			"    }\n" +
			"}\n" +
			"class XY {\n" +
			"    public String toString() {\n" +
			"        return \"SUCCESS\";\n" +
			"    }\n" +
			"}\n"
		},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	foo(new XY());\n" +
			"	^^^\n" +
			"Cannot make a static reference to the non-static method foo(XY) from the type X\n" +
			"----------\n");
}
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java", //================================
			"public class X extends SuperTest\n" +
			"{\n" +
			"    public X()\n" +
			"    {\n" +
			"        super();\n" +
			"    }\n" +
			"  \n" +
			"    static void print(Object obj)\n" +
			"    {\n" +
			"        System.out.println(\"Object:\" + obj.toString());\n" +
			"    }\n" +
			"    \n" +
			"    public static void main(String[] args)\n" +
			"    {\n" +
			"        print(\"Hello world\");\n" +
			"    }\n" +
			"}\n" +
			"class SuperTest\n" +
			"{\n" +
			"    SuperTest(){};\n" +
			"    static void print(String s)\n" +
			"    {\n" +
			"        System.out.println(\"String: \" + s);\n" +
			"    }\n" +
			"}\n"	},
		"String: Hello world");
}
// 73740 - missing serialVersionUID diagnosis shouldn't trigger load of Serializable
public void test048() {
	this.runConformTest(
		new String[] {
			"X.java", //---------------------------
			"public class X {\n" +
			"   public static void main(String[] args) {\n"+
			"		System.out.println(\"SUCCESS\");\n"+
			"   }\n"+
			"}\n",
		},
		"SUCCESS",
		Util.concatWithClassLibs(OUTPUT_DIR, true/*output in front*/),
		false, // do not flush output
		null,  // vm args
		null, // options
		new ICompilerRequestor() {
			public void acceptResult(CompilationResult result) {
				assertNotNull("missing reference information",result.simpleNameReferences);
				char[] serializable = TypeConstants.JAVA_IO_SERIALIZABLE[2];
				for (int i = 0, length = result.simpleNameReferences.length; i < length; i++) {
					char[] name = result.simpleNameReferences[i];
					if (CharOperation.equals(name, serializable))
						assertTrue("should not contain reference to Serializable", false);
				}
			}
		});
}
// 76682 - ClassCastException in qualified name computeConversion
public void test049() {
	this.runNegativeTest(
		new String[] {
			"X.java", //---------------------------
			"public class X\n" +
			"{\n" +
			"    private String foo() {\n" +
			"        return \"Started \" + java.text.DateFormat.format(new java.util.Date());\n" +
			"    }\n" +
			"}\n" ,
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\r\n" +
		"	return \"Started \" + java.text.DateFormat.format(new java.util.Date());\r\n" +
		"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot make a static reference to the non-static method format(Date) from the type DateFormat\n" +
		"----------\n");
}
public void test050() {
	this.runConformTest(
		new String[] {
			"X.java", //---------------------------
			"public class X {\n" +
			"\n" +
			"    public static void main(String argv[]) {\n" +
			"    	X.Y.Z.foo();\n" +
			"    }\n" +
			"    static class Y {\n" +
			"    	static class Z {\n" +
			"    		static void foo() {\n" +
			"    			System.out.println(\"SUCCESS\");\n" +
			"    		}\n" +
			"    	}\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");
}

public void test051() {
	this.runNegativeTest(
		new String[] {
			"X.java", //---------------------------
			"public class X {\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"        args.finalize();\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	args.finalize();\n" +
		"	     ^^^^^^^^\n" +
		"The method finalize() from the type Object is not visible\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87463
public void test052() {
	this.runConformTest(
		new String[] {
			"X.java", //---------------------------
			"public class X {\n" +
			"	public void test() {\n" +
			"		class C {\n" +
			"			public C() {\n" +
			"			}\n" +
			"			public void foo() {\n" +
			"				System.out.println(\"hello\");\n" +
			"			}\n" +
			"		}\n" +
			"		int n = 0;\n" +
			"		switch (n) {\n" +
			"			case 0 :\n" +
			"				if (true) {\n" +
			"					C c2 = new C();\n" +
			"				}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87463 - variation
public void test053() {
	this.runConformTest(
		new String[] {
			"X.java", //---------------------------
			"public class X {\n" +
			"	public void test() {\n" +
			"		int l = 1;\n" +
			"		switch(l) {\n" +
			"			case 1: \n" +
			"				class C {\n" +
			"					public C() {\n" +
			"					}\n" +
			"					public void foo() {\n" +
			"						System.out.println(\"hello\");\n" +
			"					}\n" +
			"				}\n" +
			"				int n = 0;\n" +
			"				switch (n) {\n" +
			"					case 0 :\n" +
			"						if (true) {\n" +
			"							C c2 = new C();\n" +
			"						}\n" +
			"				}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93486
public void test054() {
    this.runConformTest(
        new String[] {
            "X.java", //---------------------------
            "import java.util.LinkedHashMap;\n" +
            "import java.util.Map.Entry;\n" +
            "\n" +
            "public class X {\n" +
            "    \n" +
            "    private LinkedHashMap fCache;\n" +
            "    \n" +
            "    public X(final int cacheSize) {\n" +
            "        // start with 100 elements but be able to grow until cacheSize\n" +
            "        fCache= new LinkedHashMap(100, 0.75f, true) {\n" +
            "            /** This class is not intended to be serialized. */\n" +
            "            private static final long serialVersionUID= 1L;\n" +
            "            protected boolean removeEldestEntry(Entry eldest) {\n" +
            "                return size() > cacheSize;\n" +
            "            }\n" +
            "        };\n" +
            "    }\n" +
            "}\n",
        },
        "");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106140
public void test055() {
    this.runNegativeTest(
        new String[] {
            "A.java",
            "import p.*;\n" +
            "public class A {\n" +
            "    public void errors() {\n" +
	            "    B b = new B();\n" +
            "        String s1 = b.str;\n" +
            "        String s2 = B.str;\n" +
            "    }\n" +
            "}\n",
            "p/B.java",
            "package p;\n" +
            "class B {\n" +
            "    public static String str;\n" +
            "}\n",
        },
		"----------\n" +
		"1. ERROR in A.java (at line 4)\n" +
		"	B b = new B();\n" +
		"	^\n" +
		"The type B is not visible\n" +
		"----------\n" +
		"2. ERROR in A.java (at line 4)\n" +
		"	B b = new B();\n" +
		"	          ^\n" +
		"The type B is not visible\n" +
		"----------\n" +
		"3. ERROR in A.java (at line 5)\n" +
		"	String s1 = b.str;\n" +
		"	            ^\n" +
		"The type B is not visible\n" +
		"----------\n" +
		"4. ERROR in A.java (at line 6)\n" +
		"	String s2 = B.str;\n" +
		"	            ^\n" +
		"The type B is not visible\n" +
		"----------\n");
}
// final method in static inner class still found in extending classes
public void test056() {
    this.runConformTest(
        new String[] {
            "X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    I x = new Z();\n" +
			"    x.foo();\n" +
			"  }\n" +
			"  static interface I {\n" +
			"    Y foo();\n" +
			"  }\n" +
			"  static class Y {\n" +
			"    public final Y foo() { \n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"        return null; \n" +
			"    }\n" +
			"  }\n" +
			"  static class Z extends Y implements I {\n" +
			"      // empty\n" +
			"  }\n" +
			"}",
        },
        "SUCCESS");
}
// unresolved type does not fool methods signature comparison
public void test057() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.awt.*;\n" +
			"public class X {\n" +
			"    public void foo(Window w) {\n" +
			"        // empty\n" +
			"    }\n" +
			"    public void foo(Applet a) {\n" +
			"        // empty\n" +
			"    }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	public void foo(Applet a) {\n" +
		"	                ^^^^^^\n" +
		"Applet cannot be resolved to a type\n" +
		"----------\n"
		);
}
public void test058() {
    this.runConformTest(
        new String[] {
        		"p/X.java", // =================
        		"package p;\n" +
        		"\n" +
        		"import p.q.Z;\n" +
        		"public class X { \n" +
        		"  public static void main(String argv[]) {\n" +
        		"     System.out.println(Z.z);\n" +
        		"  }\n" +
        		"}", // =================
        		"p/q/Z.java", // =================
        		"package p.q;\n" +
        		"\n" +
        		"public class Z extends Y implements I { \n" +
        		"}\n" +
        		"class Y {\n" +
        		"    protected static int z = 1;\n" +
        		"}\n" +
        		"interface I {\n" +
        		"    int z = 0;\n" +
        		"}", // =================
		},
		"0");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132813
public void test059() {
    this.runNegativeTest(
        new String[] {
        		"X.java", // =================
    			"public class X {\n" +
    			"	\n" +
    			"	void aa(int i) {\n" +
    			"	}\n" +
    			"	void aa(long l) {\n" +
    			"	}\n" +
    			"	Zork bb() {\n" +
    			"	}\n" +
    			"	void cc() {\n" +
    			"		this.bb();\n" +
    			"	}\n" +
    			"	public static void main(String[] args) {\n" +
    			"		System.out.println(\"SUCCESS\");\n" +
    			"	}\n" +
    			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	Zork bb() {\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	this.bb();\n" +
		"	     ^^\n" +
		"The method bb() from the type X refers to the missing type Zork\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132813 - variation
public void test060() {
    this.runNegativeTest(
        new String[] {
        		"X.java", // =================
    			"public class X {\n" +
    			"	\n" +
    			"	void aa(int i) {\n" +
    			"	}\n" +
    			"	Zork aa(long l) {\n" +
    			"	}\n" +
    			"	Zork bb() {\n" +
    			"	}\n" +
    			"	void cc() {\n" +
    			"		this.bb();\n" +
    			"	}\n" +
    			"	public static void main(String[] args) {\n" +
    			"		System.out.println(\"SUCCESS\");\n" +
    			"	}\n" +
    			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	Zork aa(long l) {\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	Zork bb() {\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	this.bb();\n" +
		"	     ^^\n" +
		"The method bb() from the type X refers to the missing type Zork\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=134839
public void test061() {
	Map options = getCompilerOptions();
	if (CompilerOptions.VERSION_1_3.equals(options.get(CompilerOptions.OPTION_Compliance))) {
		// ensure target is 1.1 for having default abstract methods involved
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
	}
    this.runConformTest(
        new String[] {
        		"X.java", // =================
    			"interface MyInterface {\n" +
    			"        public void writeToStream();\n" +
    			"        public void readFromStream();\n" +
    			"}\n" +
    			"\n" +
    			"public abstract class X implements MyInterface {\n" +
    			"        public void b() {\n" +
    			"        }\n" +
    			"        public void a() {\n" +
    			"                writeTypeToStream();\n" +
    			"        }\n" +
    			"        private void writeTypeToStream() {\n" +
    			"        }\n" +
    			"}\n", // =================
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=134839
public void test062() {
	Map options = getCompilerOptions();
	if (CompilerOptions.VERSION_1_3.equals(options.get(CompilerOptions.OPTION_Compliance))) {
		// ensure target is 1.1 for having default abstract methods involved
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
	}
    this.runConformTest(
        new String[] {
        		"X.java", // =================
    			"interface MyInterface {\n" +
    			"        public void writeToStream();\n" +
    			"        public void readFromStream();\n" +
    			"}\n" +
    			"\n" +
    			"public abstract class X implements MyInterface {\n" +
    			"        public void b() {\n" +
    			"        }\n" +
    			"        public void a() {\n" +
    			"                writeTypeToStream();\n" +
    			"        }\n" +
    			"        private void writeTypeToStream() {\n" +
    			"        }\n" +
    			"}\n", // =================
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135292
public void test063() {
    this.runNegativeTest(
        new String[] {
    		"X.java", // =================
			"class 56 {\n" +
			"\n" +
			"        private static class B {\n" +
			"                public static final String F = \"\";\n" +
			"        }\n" +
			"\n" +
			"        private static class C {\n" +
			"        }\n" +
			"\n" +
			"        public void foo() {\n" +
			"                System.out.println(B.F);\n" +
			"        }\n" +
			"}\n", // =================
	},
	"----------\n" +
	"1. ERROR in X.java (at line 1)\n" +
	"	class 56 {\n" +
	"	      ^^\n" +
	"Syntax error on token \"56\", Identifier expected\n" +
	"----------\n" +
	"2. ERROR in X.java (at line 3)\n" +
	"	private static class B {\n" +
	"	                     ^\n" +
	"Illegal modifier for the class B; only public, abstract & final are permitted\n" +
	"----------\n" +
	"3. ERROR in X.java (at line 7)\n" +
	"	private static class C {\n" +
	"	                     ^\n" +
	"Illegal modifier for the class C; only public, abstract & final are permitted\n" +
	"----------\n" +
	"4. ERROR in X.java (at line 8)\n" +
	"	}\n" +
	"	^\n" +
	"Syntax error on token \"}\", delete this token\n" +
	"----------\n" +
	"5. ERROR in X.java (at line 11)\n" +
	"	System.out.println(B.F);\n" +
	"	                   ^^^\n" +
	"The type B is not visible\n" +
	"----------\n" +
	"6. ERROR in X.java (at line 13)\n" +
	"	}\n" +
	"	^\n" +
	"Syntax error, insert \"}\" to complete ClassBody\n" +
	"----------\n");
}
//	https://bugs.eclipse.org/bugs/show_bug.cgi?id=137744
public void test064() {
	Map options = getCompilerOptions();
	if (CompilerOptions.VERSION_1_3.equals(options.get(CompilerOptions.OPTION_Compliance))) {
		// ensure target is 1.1 for having default abstract methods involved
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
	}
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"		B a = new C();\n" +
				"		\n" +
				"		a.hasKursAt(1);\n" +
				"	}\n" +
				"\n" +
				"}",
				"A.java",
				"abstract public class A implements IA0 {\n" +
				"	int t;\n" +
				"	public A() {\n" +
				"	}\n" +
				"}",
				"B.java",
				"abstract public class B extends A implements IA3, IA1 {\n" +
				"	int a;\n" +
				"	public B() {\n" +
				"	}\n" +
				"	public void test() {	\n" +
				"	}\n" +
				"}",
				"C.java",
				"public class C extends B implements IA4, IA2{\n" +
				"	int c;\n" +
				"	public C() {\n" +
				"	}\n" +
				"	public boolean hasKursAt(int zeitpunkt) {\n" +
				"		return false;\n" +
				"	}\n" +
				"}",
				"IA0.java",
				"public interface IA0 {\n" +
				"	public void test();\n" +
				"}",
				"IA1.java",
				"public interface IA1 extends IA0 {\n" +
				"	public boolean hasKursAt(int zeitpunkt);\n" +
				"}",
				"IA2.java",
				"public interface IA2 extends IA0 {\n" +
				"	public boolean hasKursAt(int zeitpunkt);\n" +
				"}",
				"IA3.java",
				"public interface IA3 extends IA2 {\n" +
				"}",
				"IA4.java",
				"public interface IA4 extends IA3 {\n" +
				"}"
			},
			"SUCCESS",
			null,
			true,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135323
public void test065() {
	this.runConformTest(
			new String[] {
				"com/internap/other/ScopeExample.java",//===================
				"package com.internap.other;\n" +
				"import com.internap.*;\n" +
				"public class ScopeExample {\n" +
				"	private static final String LOGGER = \"SUCCESS\";\n" +
				"	public static void main(String[] args) {\n" +
				"		PublicAccessSubclass sub = new PublicAccessSubclass() {\n" +
				"			public void implementMe() {\n" +
				"				System.out.println(LOGGER);\n" +
				"			}\n" +
				"		};\n" +
				"		sub.implementMe();\n" +
				"	}\n" +
				"}",
				"com/internap/PublicAccessSubclass.java",//===================
				"package com.internap;\n" +
				"public abstract class PublicAccessSubclass extends DefaultAccessSuperclass {\n" +
				"	public abstract void implementMe();				\n" +
				"}",
				"com/internap/DefaultAccessSuperclass.java",//===================
				"package com.internap;\n" +
				"class DefaultAccessSuperclass {\n" +
				"	private static final String LOGGER = \"FAILED\";\n" +
				"}",
			},
			"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135323 - variation
public void test066() {
	this.runConformTest(
			new String[] {
				"com/internap/other/ScopeExample.java",//===================
				"package com.internap.other;\n" +
				"import com.internap.*;\n" +
				"public class ScopeExample {\n" +
				"	private static final String LOGGER() { return \"SUCCESS\"; }\n" +
				"	public static void main(String[] args) {\n" +
				"		PublicAccessSubclass sub = new PublicAccessSubclass() {\n" +
				"			public void implementMe() {\n" +
				"				System.out.println(LOGGER());\n" +
				"			}\n" +
				"		};\n" +
				"		sub.implementMe();\n" +
				"	}\n" +
				"}",
				"com/internap/PublicAccessSubclass.java",//===================
				"package com.internap;\n" +
				"public abstract class PublicAccessSubclass extends DefaultAccessSuperclass {\n" +
				"	public abstract void implementMe();				\n" +
				"}",
				"com/internap/DefaultAccessSuperclass.java",//===================
				"package com.internap;\n" +
				"class DefaultAccessSuperclass {\n" +
				"	private static final String LOGGER() { return \"FAILED\"; }\n" +
				"}",
			},
			"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135323 - variation
public void test067() {
	Map options = getCompilerOptions();
	if (CompilerOptions.VERSION_1_3.equals(options.get(CompilerOptions.OPTION_Compliance))) {
		this.runNegativeTest(
				new String[] {
					"com/internap/other/ScopeExample.java",//===================
					"package com.internap.other;\n" +
					"import com.internap.*;\n" +
					"public class ScopeExample {\n" +
					"	private static final String LOGGER = \"FAILED\";\n" +
					"	public static void main(String[] args) {\n" +
					"		PublicAccessSubclass sub = new PublicAccessSubclass() {\n" +
					"			public void implementMe() {\n" +
					"				System.out.println(LOGGER);\n" +
					"			}\n" +
					"		};\n" +
					"		sub.implementMe();\n" +
					"	}\n" +
					"}",
					"com/internap/PublicAccessSubclass.java",//===================
					"package com.internap;\n" +
					"public abstract class PublicAccessSubclass extends DefaultAccessSuperclass {\n" +
					"	public abstract void implementMe();				\n" +
					"}",
					"com/internap/DefaultAccessSuperclass.java",//===================
					"package com.internap;\n" +
					"class DefaultAccessSuperclass {\n" +
					"	public static final String LOGGER = \"SUCCESS\";\n" +
					"}",
				},
				"----------\n" +
				"1. WARNING in com\\internap\\other\\ScopeExample.java (at line 4)\r\n" +
				"	private static final String LOGGER = \"FAILED\";\r\n" +
				"	                            ^^^^^^\n" +
				"The value of the field ScopeExample.LOGGER is not used\n" +
				"----------\n" +
				"2. ERROR in com\\internap\\other\\ScopeExample.java (at line 8)\r\n" +
				"	System.out.println(LOGGER);\r\n" +
				"	                   ^^^^^^\n" +
				"The field LOGGER is defined in an inherited type and an enclosing scope \n" +
				"----------\n");
		return;
	}
	this.runConformTest(
			new String[] {
				"com/internap/other/ScopeExample.java",//===================
				"package com.internap.other;\n" +
				"import com.internap.*;\n" +
				"public class ScopeExample {\n" +
				"	private static final String LOGGER = \"FAILED\";\n" +
				"	public static void main(String[] args) {\n" +
				"		PublicAccessSubclass sub = new PublicAccessSubclass() {\n" +
				"			public void implementMe() {\n" +
				"				System.out.println(LOGGER);\n" +
				"			}\n" +
				"		};\n" +
				"		sub.implementMe();\n" +
				"	}\n" +
				"}",
				"com/internap/PublicAccessSubclass.java",//===================
				"package com.internap;\n" +
				"public abstract class PublicAccessSubclass extends DefaultAccessSuperclass {\n" +
				"	public abstract void implementMe();				\n" +
				"}",
				"com/internap/DefaultAccessSuperclass.java",//===================
				"package com.internap;\n" +
				"class DefaultAccessSuperclass {\n" +
				"	public static final String LOGGER = \"SUCCESS\";\n" +
				"}",
			},
			"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=139099
public void test068() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_5) return;
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	this.runConformTest(
			new String[] {
				"X.java",//===================
				"public class X {\n" +
				"    public X() {\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"        X l = new X();\n" +
				"        StringBuffer sb = new StringBuffer();\n" +
				"        sb.append(l);\n" +
				"    }\n" +
				"}", // =================,
			},
			"",
			null,
			true,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=139099
public void test068a() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_5) return;

	this.runConformTest(
		new String[] {
			"X1.java",
			"public class X1 { X1 foo() { return null; } }\n" +
			"class X2 extends X1 { X2 foo() { return null; } }\n" +
			"class Y { public X2 foo() { return null; } }\n" +
			"interface I { X1 foo(); }\n" +
			"class Z extends Y implements I {}",
		},
		"");
	this.runConformTest(
		new String[] {
			"Test.java",//===================
			"public class Test {\n" +
			"    public static void main(String[] args) {\n" +
			"        X1 x = new X2().foo();\n" +
			"        X2 xx = new X2().foo();\n" +
			"        X1 z = new Z().foo();\n" +
			"        X2 zz = new Z().foo();\n" +
			"    }\n" +
			"}", // =================,
		},
		"",
		null,
		false,
		null);

	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	this.runConformTest(
		new String[] {
			"Test14.java",//===================
			"public class Test14 {\n" +
			"    public static void main(String[] args) {\n" +
			"        X1 x = new X2().foo();\n" +
			"        X2 xx = new X2().foo();\n" +
			"        X1 z = new Z().foo();\n" +
			"        X2 zz = new Z().foo();\n" +
			"    }\n" +
			"}", // =================,
		},
		"",
		null,
		false,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=139099 - variation
public void test069() {
	this.runConformTest(
			new String[] {
				"X.java",//===================
				"public class X {\n" +
				"    public X() {\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"        X l = new X();\n" +
				"        StringBuffer sb = new StringBuffer();\n" +
				"        sb.append(l);\n" +
				"    }\n" +
				"}", // =================,
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140643
public void test070() {
	this.runConformTest(
			new String[] {
				"X.java",//===================
				"public class X {\n" +
				"	interface I {\n" +
				"	}\n" +
				"\n" +
				"	void test() {\n" +
				"		new I() {\n" +
				"			void foo() {\n" +
				"			}\n" +
				"		}.foo(); // compiles OK.\n" +
				"		new I() {\n" +
				"			void $foo() {\n" +
				"			}\n" +
				"		}.$foo(); // The method $foo() is undefined for the type new T.I(){}\n" +
				"	}\n" +
				"}", // =================
			},
			"");
}
// using $ in the name of a class defined within another package
public void test071() {
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}",
			"p/X$X.java",
			"package p;\n" +
			"public class X$X {\n" +
			"}",
		},
		"");
	this.runConformTest(
		new String[] {
			"Y.java",
			"import p.*;\n" +
			"public class Y {\n" +
			"  X$X f = new X$X();\n" +
			"}",
		},
		"",
		null /* no extra class libraries */,
		false /* do not flush output directory */,
		null /* no vm arguments */,
		null /* no custom options*/,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}
public void test072() {
	this.runNegativeTest(
			new String[] {
				"X.java",//===================
				"public class X {\n" +
				"	void bar(AX ax) {\n" +
				"		ax.foo(null);\n" +
				"	}\n" +
				"	\n" +
				"}\n" +
				"interface IX {\n" +
				"	void foo(String s);\n" +
				"}\n" +
				"interface JX {\n" +
				"	void foo(Thread t);\n" +
				"}\n" +
				"abstract class AX implements IX, JX {\n" +
				"	public void foo(String s) {}\n" +
				"}\n", // =================
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	ax.foo(null);\n" +
			"	   ^^^\n" +
			"The method foo(String) is ambiguous for the type AX\n" +
			"----------\n");
}
public void test073() {
	this.runNegativeTest(
		new String[] {
			"E.java",//===================
			"public class E {\n" +
			"	void run(int i) {}\n" +
			"	static class Inner {\n" +
			"		void run() { run(1); }\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in E.java (at line 4)\n" +
		"	void run() { run(1); }\n" +
		"	             ^^^\n" +
		"The method run() in the type E.Inner is not applicable for the arguments (int)\n" +
		"----------\n");
}

// was Compliance_1_x#test008
public void test074() {
	String[] sources = new String[] {
		"p1/Test.java",
		"package p1; \n"+
		"import Test2;	\n" +
		"import Test2.Member;	\n" +
		"public class Test { \n"+
		"	public static void main(String[] arguments) { \n"+
		"		System.out.println(\"SUCCESS\");	\n"	+
		"	} \n"+
		"} \n",
		"Test2.java",
		"public class Test2 { \n"+
		"	public class Member {	\n" +
		"	} \n"+
		"} \n"
	};
	if (this.complianceLevel == ClassFileConstants.JDK1_3) {
		runConformTest(
			sources,
			"SUCCESS");
	} else {
		runNegativeTest(
			sources,
			"----------\n" +
			"1. ERROR in p1\\Test.java (at line 2)\n" +
			"	import Test2;	\n" +
			"	       ^^^^^\n" +
			"The import Test2 cannot be resolved\n" +
			"----------\n" +
			"2. ERROR in p1\\Test.java (at line 3)\n" +
			"	import Test2.Member;	\n" +
			"	       ^^^^^\n" +
			"The import Test2 cannot be resolved\n" +
			"----------\n");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150758
public void test075() {
	this.runConformTest(
			new String[] {
				"package1/Test.java",//===================
				"package package1;\n" +
				"import package2.MyList;\n" +
				"public class Test {\n" +
				"        public void reproduce(String sortKey, boolean isAscending) {\n" +
				"                MyList recList = new MyList();\n" +
				"                recList.add(null);\n" +
				"        }\n" +
				"}\n",//===================
				"package2/MyList.java",//===================
				"package package2;\n" +
				"import java.util.AbstractList;\n" +
				"import java.util.List;\n" +
				"public class MyList extends AbstractList implements List {\n" +
				"        void add(Integer i) {\n" +
				"        }\n" +
				"        public Object get(int index) {\n" +
				"                return null;\n" +
				"        }\n" +
				"        public int size() {\n" +
				"                return 0;\n" +
				"        }\n" +
				"}", // =================
			},
			"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159543
public void test076() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"p/Y.java",	//===================
				"package p;\n" +
				"public class Y {\n" +
				"  public static void foo(String s) {\n" +
				"  }\n" +
				"}\n",		//===================
				"q/X.java",	//===================
				"package q;\n" +
				"import static p.Y.foo;\n" +
				"public class X {\n" +
				"        void foo() {\n" +
				"        }\n" +
				"        void bar() {\n" +
				"          foo(\"\");\n" +
				"        }\n" +
				"}", 		// =================
			},
			"----------\n" +
			"1. ERROR in q\\X.java (at line 7)\n" +
			"	foo(\"\");\n" +
			"	^^^\n" +
			"The method foo() in the type X is not applicable for the arguments (String)\n" +
			"----------\n");
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159893
public void test077() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"abstract  class B {\n" +
			"  public String getValue(){\n" +
			"    return \"pippo\";\n" +
			"  }\n" +
			"}\n" +
			"class D {\n" +
			"  private String value;\n" +
			"  public D(String p_Value){\n" +
			"    value = p_Value;\n" +
			"  }\n" +
			"  private  String getValue(){\n" +
			"    return \"pippoD\";\n" +
			"  }\n" +
			"}\n" +
			"public class X extends B {\n" +
			"  class C extends D{\n" +
			"    public C() {\n" +
			"      super(getValue());\n" +
			"      String s = getValue();\n" +
			"    }\n" +
			"  }\n" +
			"}\n", 		// =================
		},
		"");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159893 - variation
public void test078() {
	this.runNegativeTest(
		new String[] {
			"X.java",	//===================
			"class D {\n" +
			"  private String value;\n" +
			"  public D(String p_Value){\n" +
			"    value = p_Value;\n" +
			"  }\n" +
			"  private  String getValue(){\n" +
			"    return \"pippoD\";\n" +
			"  }\n" +
			"}\n" +
			"public class X {\n" +
			"  class C extends D{\n" +
			"    public C() {\n" +
			"      super(getValue());\n" +
			"      String s = getValue();\n" +
			"    }\n" +
			"  }\n" +
			"}\n", 		// =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	private String value;\n" +
		"	               ^^^^^\n" +
		"The value of the field D.value is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	super(getValue());\n" +
		"	      ^^^^^^^^\n" +
		"The method getValue() from the type D is not visible\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	String s = getValue();\n" +
		"	           ^^^^^^^^\n" +
		"The method getValue() from the type D is not visible\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166354
// **
public void test079() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"abstract class Y {\n" +
			"  private void foo(boolean b) {\n" +
			"    System.out.println(\"Y\");\n" +
			"    return;\n" +
			"  }\n" +
			"}\n" +
			"public class X {\n" +
			"  private void foo(String s) {\n" +
			"    System.out.println(\"X\");\n" +
			"    return;\n" +
			"  }\n" +
			"  private class Z extends Y {\n" +
			"    public void bar(boolean b) {\n" +
			"      foo(\"Flag \" + b);\n" +
			"      X.this.foo(\"Flag \" + b);\n" +
			"    }\n" +
			"  }\n" +
			"  Z m = new Z();\n" +
			"  public static void main(String args[]) {\n" +
			"    new X().m.bar(true);\n" +
			"  }\n" +
			"}", 		// =================
		},
		"X\nX");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166354
// variant
public void test080() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"abstract class Y {\n" +
			"  private void foo(String s) {\n" +
			"    System.out.println(\"Y\");\n" +
			"    return;\n" +
			"  }\n" +
			"}\n" +
			"public class X {\n" +
			"  private void foo(String s) {\n" +
			"    System.out.println(\"X\");\n" +
			"    return;\n" +
			"  }\n" +
			"  private class Z extends Y {\n" +
			"    public void bar(boolean b) {\n" +
			"      foo(\"Flag \" + b);\n" +
			"      X.this.foo(\"Flag \" + b);\n" +
			"    }\n" +
			"  }\n" +
			"  Z m = new Z();\n" +
			"  public static void main(String args[]) {\n" +
			"    new X().m.bar(true);\n" +
			"  }\n" +
			"}", 		// =================
		},
		"X\nX");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174588
public void test081() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"public class X extends Y {\n" +
			"  public void set(int value) {\n" +
			"      System.out.println(\"set(\" + value + \")\");\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    X x = new X();\n" +
			"    x.set(1L);\n" +
			"  }\n" +
			"}\n" +
			"abstract class Y implements I {\n" +
			"  public void set(long value) {\n" +
			"    set((int)value);\n" +
			"  }\n" +
			"  public void set(double value) {\n" +
			"    set((int)value);\n" +
			"  }\n" +
			"}\n" +
			"interface I {\n" +
			"  void set(int value);\n" +
			"  void set(long value);\n" +
			"}\n", 		// =================
		},
		"set(1)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174588
// variant
public void test082() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"public class X extends Y {\n" +
			"  public void set(int value) {\n" +
			"      System.out.println(\"set(\" + value + \")\");\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    X x = new X();\n" +
			"    x.set(1L);\n" +
			"  }\n" +
			"}\n" +
			"abstract class Y implements I {\n" +
			"  public abstract void set(int value);\n" +
			"  public void set(long value) {\n" +
			"    set((int)value);\n" +
			"  }\n" +
			"  public void set(double value) {\n" +
			"    set((int)value);\n" +
			"  }\n" +
			"}\n" +
			"interface I {\n" +
			"  void set(int value);\n" +
			"  void set(long value);\n" +
			"}\n", 		// =================
		},
		"set(1)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174588
// variant
public void test083() {
	String src[] =
		new String[] {
			"X.java",
			"public class X extends Z {\n" +
			"  public void set(int value) {\n" +
			"      System.out.println(\"set(\" + value + \")\");\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    X x = new X();\n" +
			"    x.set(1L);\n" +
			"  }\n" +
			"}\n" +
			"abstract class Z extends Y {\n" +
			"  public void set(long value) {\n" +
			"    set((int)value);\n" +
			"  }\n" +
			"  public void set(double value) {\n" +
			"    set((int)value);\n" +
			"  }\n" +
			"}\n" +
			"abstract class Y implements I {\n" +
			"}\n" +
			"interface I {\n" +
			"  void set(int value);\n" +
			"  void set(long value);\n" +
			"}\n",
		};
	if (this.complianceLevel <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
			src,
			"----------\n" +
			"1. ERROR in X.java (at line 12)\r\n" +
			"	set((int)value);\r\n" +
			"	^^^\n" +
			"The method set(long) is ambiguous for the type Z\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 15)\r\n" +
			"	set((int)value);\r\n" +
			"	^^^\n" +
			"The method set(long) is ambiguous for the type Z\n" +
			"----------\n");
	} else {
		this.runConformTest(
			src,
			"set(1)");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174588
// variant
public void test084() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"public class X extends Y {\n" +
			"  public void set(int value, int i) {\n" +
			"      System.out.println(\"set(\" + value + \")\");\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    X x = new X();\n" +
			"    x.set(1L, 1);\n" +
			"  }\n" +
			"}\n" +
			"abstract class Y implements I {\n" +
			"  public void set(long value, int i) {\n" +
			"    set((int)value, i);\n" +
			"  }\n" +
			"  public void set(int i, double value) {\n" +
			"    set(i, (int)value);\n" +
			"  }\n" +
			"}\n" +
			"interface I {\n" +
			"  void set(int value, int i);\n" +
			"}\n", 		// =================
		},
		"set(1)");
}

public void test086() {
	this.runNegativeTest(
		new String[] {
			"X.java",	//===================
			"public class X {\n" +
			"	public static void main(String[] arguments) {\n" +
			"		Y y = new Y();\n" +
			"		System.out.println(y.array[0]);\n" +
			"		System.out.println(y.length);\n" +
			"	}\n" +
			"}\n" +
			"class Y {\n" +
			"	private class Invisible {}\n" +
			"	Invisible[] array;\n" +
			"}\n", 		// =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	System.out.println(y.length);\n" +
		"	                     ^^^^^^\n" +
		"length cannot be resolved or is not a field\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185422 - variation
public void _test087() {
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"import java.util.*;\n" +
				"/**\n" +
				" * @see Private - Private is not visible here\n" +
				" */\n" +
				"public abstract class X implements X.Private, Secondary.SecondaryPrivate {\n" +
				"	/**\n" +
				" * @see Private - Private is visible here\n" +
				"	 */\n" +
				"	private static interface Private {}\n" +
				"	Private field;\n" +
				"}\n" +
				"class Secondary {\n" +
				"	private static interface SecondaryPrivate {}\n" +
				"}\n", // =================
			},
			"done");
}
public void test088() {
	this.runNegativeTest(
		new String[] {
			"java/lang/Object.java",	//===================
			"package java.lang;\n" +
			"public class Object {\n" +
			"	public Object() {\n" +
			"		super();\n" +
			"	}\n" +
			"}\n", 		// =================
		},
		"----------\n" +
		"1. ERROR in java\\lang\\Object.java (at line 4)\n" +
		"	super();\n" +
		"	^^^^^^^^\n" +
		"super cannot be used in java.lang.Object\n" +
		"----------\n");
}

public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",	//===================
			"public class X {\n" +
			"	static class Member implements X {\n" +
			"		Member () {\n" +
			"			super();\n" +
			"		}\n" +
			"	}\n" +
			"}\n", 		// =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	static class Member implements X {\n" +
		"	                               ^\n" +
		"The type X cannot be a superinterface of Member; a superinterface must be an interface\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239833
public void test090() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public synchronized int f;\n" +
			"	public synchronized X() {}\n" +
			"	public volatile void foo() {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public synchronized int f;\n" +
		"	                        ^\n" +
		"Illegal modifier for the field f; only public, protected, private, static, final, transient & volatile are permitted\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	public synchronized X() {}\n" +
		"	                    ^^^\n" +
		"Illegal modifier for the constructor in type X; only public, protected & private are permitted\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	public volatile void foo() {}\n" +
		"	                     ^^^^^\n" +
		"Illegal modifier for the method foo; only public, protected, private, abstract, static, final, synchronized, native & strictfp are permitted\n" +
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211 - variation
public void test091() {
	this.runNegativeTest(
		new String[] {
			"foo/Test.java",//------------------------------
			"package foo;\n" +
			"public class Test {\n" +
			"        public class M1 {\n" +
			"              public class M2 {}\n" +
			"        }\n" +
			"}\n",
			"bar/Test2.java",//------------------------------
			"package bar;\n" +
			"import foo.Test;\n" +
			"import Test.M1.M2;\n" +
			"public class Test2 {\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in bar\\Test2.java (at line 3)\n" +
		"	import Test.M1.M2;\n" +
		"	       ^^^^\n" +
		"The import Test cannot be resolved\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211 - variation
public void test092() {
	this.runNegativeTest(
		new String[] {
			"foo/Test.java",//------------------------------
			"package foo;\n" +
			"public class Test {\n" +
			"        public class M1 {\n" +
			"              public class M2 {}\n" +
			"        }\n" +
			"}\n",
			"bar/Test2.java",//------------------------------
			"package bar;\n" +
			"import foo.*;\n" +
			"import Test.M1.M2;\n" +
			"public class Test2 {\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in bar\\Test2.java (at line 3)\n" +
		"	import Test.M1.M2;\n" +
		"	       ^^^^\n" +
		"The import Test cannot be resolved\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211 - variation
public void test093() {
	this.runNegativeTest(
		new String[] {
			"foo/Test.java",//------------------------------
			"package foo;\n" +
			"public class Test {\n" +
			"        public class M1 {\n" +
			"              public class foo {}\n" +
			"        }\n" +
			"}\n",
			"bar/Test2.java",//------------------------------
			"package bar;\n" +
			"import foo.Test;\n" +
			"import Test.M1.foo;\n" +
			"public class Test2 {\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in bar\\Test2.java (at line 3)\n" +
		"	import Test.M1.foo;\n" +
		"	       ^^^^\n" +
		"The import Test cannot be resolved\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211 - variation
public void test094() {
	this.runConformTest(
		new String[] {
			"foo/Test.java",//------------------------------
			"package foo;\n" +
			"public class Test {\n" +
			"        public class M1 {\n" +
			"              public class foo {}\n" +
			"        }\n" +
			"}\n",
			"bar/Test2.java",//------------------------------
			"package bar;\n" +
			"import foo.Test.M1.foo;\n" +
			"public class Test2 {\n" +
			"}\n",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277965
public void test095() {
	this.runNegativeTest(
		new String[] {
			"p1/B.java",
			"package p1;\n" +
			"protected class B1 {}",
			"X.java", // =================
			"public class X extends p1.B1 {}",
	},
	"----------\n" +
	"1. ERROR in p1\\B.java (at line 2)\n" +
	"	protected class B1 {}\n" +
	"	                ^^\n" +
	"Illegal modifier for the class B1; only public, abstract & final are permitted\n" +
	"----------\n" +
	"----------\n" +
	"1. ERROR in X.java (at line 1)\n" +
	"	public class X extends p1.B1 {}\n" +
	"	                       ^^^^^\n" +
	"The type p1.B1 is not visible\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id= 317212
public void test096() {
	this.runNegativeTest(
		new String[] {
			"p0/B.java",//------------------------------
			"package p0;\n" +
			"public class B {\n" +
			"    public static A m() {\n" +
			"        return new A();\n" +
			"    }\n" +
			"}\n" +
			"class A {\n" +
			"        public class M {\n" +
			"            public M() {}\n" +
			"        }\n" +
			"}\n",
			"p1/C.java",//------------------------------
			"package p1;\n" +
			"import p0.B;\n" +
			"public class C {\n" +
			"    public static void main(String[] args) {\n" +
			"        B.m().new M();\n" +
			"    }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in p1\\C.java (at line 5)\n" +
		"	B.m().new M();\n" +
		"	^^^^^\n" +
		"The type p0.A is not visible\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id= 317212
public void test097() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"----------\n" +
			"1. WARNING in B.java (at line 6)\n" +
			"	public class M {\n" +
			"	             ^\n" +
			"The type B.A.M is never used locally\n" +
			"----------\n" +
			"2. WARNING in B.java (at line 7)\n" +
			"	public M() {}\n" +
			"	       ^^^\n" +
			"The constructor B.A.M() is never used locally\n" +
			"----------\n" +
			"3. ERROR in B.java (at line 13)\n" +
			"	B.m().new M();\n" +
			"	^^^^^\n" +
			"The type B$A is not visible\n" +
			"----------\n"
			:
			"----------\n" +
			"1. WARNING in B.java (at line 3)\n" +
			"	return new B().new A();\n" +
			"	       ^^^^^^^^^^^^^^^\n" +
			"Access to enclosing constructor B.A() is emulated by a synthetic accessor method\n" +
			"----------\n" +
			"2. WARNING in B.java (at line 6)\n" +
			"	public class M {\n" +
			"	             ^\n" +
			"The type B.A.M is never used locally\n" +
			"----------\n" +
			"3. WARNING in B.java (at line 7)\n" +
			"	public M() {}\n" +
			"	       ^^^\n" +
			"The constructor B.A.M() is never used locally\n" +
			"----------\n" +
			"4. ERROR in B.java (at line 13)\n" +
			"	B.m().new M();\n" +
			"	^^^^^\n" +
			"The type B$A is not visible\n" +
			"----------\n";
	this.runNegativeTest(
		new String[] {
			"B.java",//------------------------------
			"public class B {\n" +
			"    public static A m() {\n" +
			"        return new B().new A();\n" +
			"    }\n" +
			"    private class A {\n" +
			"        public class M {\n" +
			"            public M() {}\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"class C {\n" +
			"    public static void main(String[] args) {\n" +
			"        B.m().new M();\n" +
			"    }\n" +
			"}\n",
		},
		errMessage);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test098() {
	this.runConformTest(
		new String[] {
			"B.java",//------------------------------
			"class A {\n" +
			"    public final static class B {\n" +
			"        public final static String length = \"very long\";\n" +
			"    }\n" +
			"    private  int [] B = new int[5];\n" +
			"}\n" +
			"public class B {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(A.B.length);\n" +
			"    }\n" +
			"}\n",
		},
		"very long");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test099() {
	this.runNegativeTest(
		new String[] {
			"B.java",//------------------------------
			"class A {\n" +
			"    public final static class B {\n" +
			"        public final static String length = \"very long\";\n" +
			"    }\n" +
			"    public int [] B = new int[5];\n" +
			"}\n" +
			"public class B {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(A.B.length);\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in B.java (at line 9)\n" +
		"	System.out.println(A.B.length);\n" +
		"	                   ^^^^^^^^^^\n" +
		"Cannot make a static reference to the non-static field A.B\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test100() {
	this.runConformTest(
		new String[] {
			"B.java",//------------------------------
			"class A {\n" +
			"    public final class B {\n" +
			"        public final String length = \"very long\";\n" +
			"    }\n" +
			"    public static int [] B = new int[5];\n" +
			"}\n" +
			"public class B {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(A.B.length);\n" +
			"    }\n" +
			"}\n",
		},
		"5");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test101() {
	this.runNegativeTest(
		new String[] {
			"B.java",//------------------------------
			"class A {\n" +
			"    private final class B {\n" +
			"        public final String length = \"very long\";\n" +
			"    }\n" +
			"    private int [] B = new int[5];\n" +
			"}\n" +
			"public class B {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(A.B.length);\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in B.java (at line 2)\n" +
		"	private final class B {\n" +
		"	                    ^\n" +
		"The type A.B is never used locally\n" +
		"----------\n" +
		"2. WARNING in B.java (at line 3)\n" +
		"	public final String length = \"very long\";\n" +
		"	                    ^^^^^^\n" +
		"The value of the field A.B.length is not used\n" +
		"----------\n" +
		"3. WARNING in B.java (at line 5)\n" +
		"	private int [] B = new int[5];\n" +
		"	               ^\n" +
		"The value of the field A.B is not used\n" +
		"----------\n" +
		"4. ERROR in B.java (at line 9)\n" +
		"	System.out.println(A.B.length);\n" +
		"	                     ^\n" +
		"The field A.B is not visible\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test102() {
	this.runNegativeTest(
		new String[] {
			"B.java",//------------------------------
			"class A {\n" +
			"    public final class B {\n" +
			"        private final String length = \"very long\";\n" +
			"    }\n" +
			"    private int [] B = new int[5];\n" +
			"}\n" +
			"public class B {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(A.B.length);\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in B.java (at line 3)\n" +
		"	private final String length = \"very long\";\n" +
		"	                     ^^^^^^\n" +
		"The value of the field A.B.length is not used\n" +
		"----------\n" +
		"2. WARNING in B.java (at line 5)\n" +
		"	private int [] B = new int[5];\n" +
		"	               ^\n" +
		"The value of the field A.B is not used\n" +
		"----------\n" +
		"3. ERROR in B.java (at line 9)\n" +
		"	System.out.println(A.B.length);\n" +
		"	                       ^^^^^^\n" +
		"The field A.B.length is not visible\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=316956
public void test103() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_4) return;
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"----------\n" +
			"1. WARNING in A.java (at line 2)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.x is not used\n" +
			"----------\n" +
			"2. WARNING in A.java (at line 4)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.B.x is not used\n" +
			"----------\n" +
			"3. WARNING in A.java (at line 5)\n" +
			"	private C c = new C() {\n" +
			"	          ^\n" +
			"The value of the field A.B.c is not used\n" +
			"----------\n" +
			"4. WARNING in A.java (at line 6)\n" +
			"	void foo() {\n" +
			"	     ^^^^^\n" +
			"The method foo() from the type new A.C(){} is never used locally\n" +
			"----------\n" +
			"5. WARNING in A.java (at line 12)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.C.x is not used\n" +
			"----------\n"
			:
			"----------\n" +
			"1. WARNING in A.java (at line 2)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.x is not used\n" +
			"----------\n" +
			"2. WARNING in A.java (at line 4)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.B.x is not used\n" +
			"----------\n" +
			"3. WARNING in A.java (at line 5)\n" +
			"	private C c = new C() {\n" +
			"	          ^\n" +
			"The value of the field A.B.c is not used\n" +
			"----------\n" +
			"4. WARNING in A.java (at line 6)\n" +
			"	void foo() {\n" +
			"	     ^^^^^\n" +
			"The method foo() from the type new A.C(){} is never used locally\n" +
			"----------\n" +
			"5. WARNING in A.java (at line 7)\n" +
			"	x = 3;\n" +
			"	^\n" +
			"Write access to enclosing field A.B.x is emulated by a synthetic accessor method\n" +
			"----------\n" +
			"6. WARNING in A.java (at line 12)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.C.x is not used\n" +
			"----------\n";
	this.runNegativeTest(
		new String[] {
			"A.java",//------------------------------
			"public class A {\n" +
			"	  private int x;\n" +
			"	  static class B {\n" +
			"	    private int x;\n" +
			"	    private C c = new C() {\n" +
			"	      void foo() {\n" +
			"	        x = 3;\n" +
			"	      }\n" +
			"	    };\n" +
			"	  }\n" +
			"	  static class C {\n" +
			"	    private int x;\n" +
			"	  }\n" +
			"	}\n",
		},
		errMessage);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=316956
public void test104() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_4) return;
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"A.java",//------------------------------
			"public class A {\n" +
			"	  private int x;\n" +
			"	  static class B {\n" +
			"	    private int x;\n" +
			"	    private C c = new C() {\n" +
			"	      void foo() {\n" +
			"	        x = 3;\n" +
			"	      }\n" +
			"	    };\n" +
			"	  }\n" +
			"	  static class C {\n" +
			"	    public int x;\n" +
			"	  }\n" +
			"	}\n",
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. WARNING in A.java (at line 2)\n" +
		"	private int x;\n" +
		"	            ^\n" +
		"The value of the field A.x is not used\n" +
		"----------\n" +
		"2. WARNING in A.java (at line 4)\n" +
		"	private int x;\n" +
		"	            ^\n" +
		"The value of the field A.B.x is not used\n" +
		"----------\n" +
		"3. WARNING in A.java (at line 5)\n" +
		"	private C c = new C() {\n" +
		"	          ^\n" +
		"The value of the field A.B.c is not used\n" +
		"----------\n" +
		"4. WARNING in A.java (at line 6)\n" +
		"	void foo() {\n" +
		"	     ^^^^^\n" +
		"The method foo() from the type new A.C(){} is never used locally\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=316956
public void test105() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_4) return;
	String errMessage =	isMinimumCompliant(ClassFileConstants.JDK11) ?
			"----------\n" +
			"1. WARNING in A.java (at line 2)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.x is not used\n" +
			"----------\n" +
			"2. WARNING in A.java (at line 3)\n" +
			"	private C c = new C() {\n" +
			"	          ^\n" +
			"The value of the field A.c is not used\n" +
			"----------\n" +
			"3. WARNING in A.java (at line 4)\n" +
			"	void foo() {\n" +
			"	     ^^^^^\n" +
			"The method foo() from the type new A.C(){} is never used locally\n" +
			"----------\n" +
			"4. WARNING in A.java (at line 9)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.C.x is not used\n" +
			"----------\n"
			:
			"----------\n" +
			"1. WARNING in A.java (at line 2)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.x is not used\n" +
			"----------\n" +
			"2. WARNING in A.java (at line 3)\n" +
			"	private C c = new C() {\n" +
			"	          ^\n" +
			"The value of the field A.c is not used\n" +
			"----------\n" +
			"3. WARNING in A.java (at line 4)\n" +
			"	void foo() {\n" +
			"	     ^^^^^\n" +
			"The method foo() from the type new A.C(){} is never used locally\n" +
			"----------\n" +
			"4. WARNING in A.java (at line 5)\n" +
			"	x = 3;\n" +
			"	^\n" +
			"Write access to enclosing field A.x is emulated by a synthetic accessor method\n" +
			"----------\n" +
			"5. WARNING in A.java (at line 9)\n" +
			"	private int x;\n" +
			"	            ^\n" +
			"The value of the field A.C.x is not used\n" +
			"----------\n";

	this.runNegativeTest(
		new String[] {
			"A.java",//------------------------------
			"public class A {\n" +
			"	  private int x;\n" +
			"	  private C c = new C() {\n" +
			"	    void foo() {\n" +
			"	      x = 3;\n" +
			"	    }\n" +
			"	  };\n" +
			"	  static class C {\n" +
			"	    private int x;\n" +
			"	  }\n" +
			"	 }\n",
		},
		errMessage);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350738
public void test106() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",//------------------------------
			"import java.util.List;\n" +
			"import java.util.Set;\n" +
			"public class X {\n" +
			"	private static List<Object> foo1(Set<Object> set) {\n" +
			"	    return foo1(set);\n" +
			"	}\n" +
			"	private static <T> List<T> foo3(Set<T> set) {\n" +
			"	    return foo3(set);\n" +
			"	}\n" +
			"}\n",
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	private static List<Object> foo1(Set<Object> set) {\n" +
		"	                            ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method foo1(Set<Object>) from the type X is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	private static <T> List<T> foo3(Set<T> set) {\n" +
		"	                           ^^^^^^^^^^^^^^^^\n" +
		"The method foo3(Set<T>) from the type X is never used locally\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}

public void testBug537828() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_4) return;
	this.runConformTest(
		new String[] {
			"FieldBug.java",//------------------------------
			"class A {\n" +
			"	Object obj = \"A.obj\";\n" +
			"}\n" +
			"\n" +
			"class B {\n" +
			"	private Object obj = \"B.obj\";\n" +
			"	public Object getObj() {return obj;}\n" +
			"}\n" +
			"\n" +
			"public class FieldBug {\n" +
			"	Object obj = \"FieldBug.obj\";\n" +
			"\n" +
			"	static class AA extends A {\n" +
			"		class BB extends B {\n" +
			"			Object n = obj;\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(new AA().new BB().n);\n" +
			"	}\n" +
			"}",
		},
		"A.obj");
}
public void testBug577350_001() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK10) return;
	this.runConformTest(
		new String[] {
			"X.java",//------------------------------
			"import java.lang.invoke.MethodHandles;\n"+
			"import java.lang.invoke.VarHandle;\n"+
			"\n"+
			"public class X {\n"+
			" private static final VarHandle VH;\n"+
			" static {\n"+
			"   var lookup = MethodHandles.lookup();\n"+
			"   try {\n"+
			"     VH = lookup.findVarHandle(X.class, \"value\", int.class);\n"+
			"   } catch (NoSuchFieldException | IllegalAccessException e) {\n"+
			"     throw new AssertionError(e);\n"+
			"   }\n"+
			" }\n"+
			"\n"+
			" private volatile int value;\n"+
			"\n"+
			" public void test() {\n"+
			"   VH.compareAndSet(this, 2, 3); // <--- HERE\n"+
			" }\n"+
			"\n"+
			" public static void main(String[] args) {\n"+
			"   new X().test();\n"+
			" }\n"+
			"}",
		},
		"");
}
public void testBug577350_002() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK9) return;
	this.runConformTest(
		new String[] {
			"X.java",//------------------------------
			"import java.lang.invoke.MethodHandles;\n"+
			"import java.lang.invoke.MethodHandles.Lookup;\n"+
			"import java.lang.invoke.VarHandle;\n" +
			"\n"+
			"public class X {\n"+
			" private static final VarHandle VH;\n"+
			" static {\n"+
			"   Lookup lookup = MethodHandles.lookup();\n"+
			"   try {\n"+
			"     VH = lookup.findVarHandle(X.class, \"value\", int.class);\n"+
			"   } catch (NoSuchFieldException | IllegalAccessException e) {\n"+
			"     throw new AssertionError(e);\n"+
			"   }\n"+
			" }\n"+
			"\n"+
			" private volatile int value;\n"+
			"\n"+
			" public void test() {\n"+
			"   VH.compareAndSet(this, 2, 3); // <--- HERE\n"+
			" }\n"+
			"\n"+
			" public static void main(String[] args) {\n"+
			"   new X().test();\n"+
			" }\n"+
			"}",
		},
		"");
}
public static Class testClass() {	return LookupTest.class;
}
}
