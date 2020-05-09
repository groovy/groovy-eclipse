/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Compliance_1_4 extends AbstractRegressionTest {
boolean docSupport = false;

public Compliance_1_4(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.4
 */
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	if (this.docSupport) {
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
	}
	return options;
}
public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(), ClassFileConstants.JDK1_4);
}
public static Class testClass() {
	return Compliance_1_4.class;
}
// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "test079" };
//		TESTS_NUMBERS = new int[] { 104 };
//		TESTS_RANGE = new int[] { 85, -1 };
}
/* (non-Javadoc)
 * @see junit.framework.TestCase#setUp()
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	// Javadoc disabled by default
	this.docSupport = false;
}

// test001 - moved to SuperTypeTest#test002
// test002 - moved to SuperTypeTest#test003
// test003 - moved to SuperTypeTest#test004
// test004 - moved to SuperTypeTest#test005
// test005 - moved to SuperTypeTest#test006
// test006 - moved to SuperTypeTest#test007
// test007 - moved to TryStatementTest#test057
// test008 - moved to LookupTest#test074
// test009 - moved to RuntimeTests#test1004

// check actualReceiverType when array type
public void test010() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"package p1; \n"+
			"public class Z {	\n" +
			"	public static void main(String[] arguments) { \n"+
			"		String[] s = new String[]{\"SUCCESS\" };	\n" +
			"		System.out.print(s.length);	\n"	+
			"		System.out.print(((String[])s.clone())[0]);	\n"	+
			"	} \n"+
			"} \n"
		},
		"1SUCCESS");
}
// test unreachable code complaints
public void test011() {
	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1; \n"+
			"public class X { \n"+
			"	void foo() { \n"+
			"		while (false);	\n" +
			"		while (false) System.out.println(\"unreachable\");	\n" +
			"		do ; while (false);	\n" +
			"		do System.out.println(\"unreachable\"); while (false);	\n" +
			"		for (;false;);	\n" +
			"		for (;false;) System.out.println(\"unreachable\");	\n" +
			"		if (false);	\n" +
			"		if (false)System.out.println(\"unreachable\");		\n" +
			"	}	\n" +
			"} \n"
		},
		"----------\n" +
		"1. ERROR in p1\\X.java (at line 4)\n" +
		"	while (false);	\n" +
		"	             ^\n" +
		"Unreachable code\n" +
		"----------\n" +
		"2. ERROR in p1\\X.java (at line 5)\n" +
		"	while (false) System.out.println(\"unreachable\");	\n" +
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unreachable code\n" +
		"----------\n" +
		"3. ERROR in p1\\X.java (at line 8)\n" +
		"	for (;false;);	\n" +
		"	             ^\n" +
		"Unreachable code\n" +
		"----------\n" +
		"4. ERROR in p1\\X.java (at line 9)\n" +
		"	for (;false;) System.out.println(\"unreachable\");	\n" +
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unreachable code\n" +
		"----------\n" +
		"5. WARNING in p1\\X.java (at line 10)\n" +
		"	if (false);	\n" +
		"	          ^\n" +
		"Dead code\n" +
		"----------\n" +
		"6. WARNING in p1\\X.java (at line 11)\n" +
		"	if (false)System.out.println(\"unreachable\");		\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n"
	);
}
// binary compatibility
public void test012() {
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"package p1;	\n" +
			"class Store {	\n" +
			"	String value;	\n" +
			"	Store(String value){	\n" +
			"		this.value = value;	\n" +
			"	}	\n" +
			"}	\n" +
			"class Top {	\n" +
			"	static String bar = \"Top.bar\";	\n" +
			"	String foo = \"Top.foo\";	\n" +
			"	Store store = new Store(\"Top.store\");	\n" +
			"	static Store sstore = new Store(\"Top.sstore\");	\n" +
			"	static Top ss = new Top();	\n" +
			"}	\n" +
			"public class Y extends Updated {		\n" +
			"	public static void main(String[] arguments) {	\n" +
			"		new Y().test();	\n" +
			"	}	\n" +
			"	void test() {		\n" +
			"		System.out.print(\"*** FIELD ACCESS ***\");	\n" +
			"		System.out.print(\"*1* new Updated().bar: \" + new Updated().bar);	\n" +
			"		System.out.print(\"*2* new Updated().foo: \" + new Updated().foo);	\n" +
			"		System.out.print(\"*3* new Y().foo: \" + new Y().foo);	\n" +
			"		System.out.print(\"*4* new Y().bar: \" + new Y().bar);	\n" +
			"		System.out.print(\"*5* bar: \" + bar);	\n" +
			"		System.out.print(\"*6* foo: \" + foo);	\n" +
			"		System.out.print(\"*7* Y.bar: \" + Y.bar);	\n" +
			"		System.out.print(\"*8* this.bar: \" + this.bar);	\n" +
			"		System.out.print(\"*9* this.foo: \" + this.foo);	\n" +
			"		System.out.print(\"*10* store.value: \" + store.value);	\n" +
			"		System.out.print(\"*11* sstore.value: \" + sstore.value);	\n" +
			"		System.out.print(\"*12* ss.sstore.value: \" + ss.sstore.value);	\n" +
			"	}		\n" +
			"}		\n",
			"p1/Updated.java",
			"package p1;	\n" +
			"public class Updated extends Top {	\n" +
			"}	\n"
		},
		"*** FIELD ACCESS ***"
		+"*1* new Updated().bar: Top.bar"
		+"*2* new Updated().foo: Top.foo"
		+"*3* new Y().foo: Top.foo"
		+"*4* new Y().bar: Top.bar"
		+"*5* bar: Top.bar"
		+"*6* foo: Top.foo"
		+"*7* Y.bar: Top.bar"
		+"*8* this.bar: Top.bar"
		+"*9* this.foo: Top.foo"
		+"*10* store.value: Top.store"
		+"*11* sstore.value: Top.sstore"
		+"*12* ss.sstore.value: Top.sstore");

	this.runConformTest(
		new String[] {
			"p1/Updated.java",
			"package p1; \n"+
			"public class Updated extends Top { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		Y.main(arguments);	\n" +
			"	}	\n" +
			"	static String bar = \"Updated.bar\";	\n" +
			"	String foo = \"Updated.foo\";	\n" +
			"	Store store = new Store(\"Updated.store\");	\n" +
			"	static Store sstore = new Store(\"Updated.sstore\");	\n" +
			"	static Updated ss = new Updated();	\n" +
			"} \n"
		},
		"*** FIELD ACCESS ***"
		+"*1* new Updated().bar: Updated.bar"
		+"*2* new Updated().foo: Updated.foo"
		+"*3* new Y().foo: Updated.foo"
		+"*4* new Y().bar: Updated.bar"
		+"*5* bar: Updated.bar"
		+"*6* foo: Updated.foo"
		+"*7* Y.bar: Updated.bar"
		+"*8* this.bar: Updated.bar"
		+"*9* this.foo: Updated.foo"
		+"*10* store.value: Updated.store"
		+"*11* sstore.value: Updated.sstore"
		+"*12* ss.sstore.value: Top.sstore",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}
// binary compatibility
public void test013() {
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"package p1;	\n" +
			"class Store {	\n" +
			"	String value;	\n" +
			"	Store(String value){	\n" +
			"		this.value = value;	\n" +
			"	}	\n" +
			"}	\n" +
			"class Top {	\n" +
			"	static String bar() { return \"Top.bar()\"; }	\n" +
			"	String foo() { return \"Top.foo()\"; }	\n" +
			"}	\n" +
			"public class Y extends Updated {		\n" +
			"	public static void main(String[] arguments) {	\n" +
			"		new Y().test();	\n" +
			"	}	\n" +
			"	void test() {		\n" +
			"		System.out.print(\"*** METHOD ACCESS ***\");	\n" +
			"		System.out.print(\"*1* new Updated().bar(): \" + new Updated().bar());	\n" +
			"		System.out.print(\"*2* new Updated().foo(): \" + new Updated().foo());	\n" +
			"		System.out.print(\"*3* new Y().foo(): \" + new Y().foo());	\n" +
			"		System.out.print(\"*4* new Y().bar(): \" + new Y().bar());	\n" +
			"		System.out.print(\"*5* bar(): \" + bar());	\n" +
			"		System.out.print(\"*6* foo(): \" + foo());	\n" +
			"		System.out.print(\"*7* Y.bar(): \" + Y.bar());	\n" +
			"		System.out.print(\"*8* this.bar(): \" + this.bar());	\n" +
			"		System.out.print(\"*9* this.foo(): \" + this.foo());	\n" +
			"	}		\n" +
			"}		\n",
			"p1/Updated.java",
			"package p1;	\n" +
			"public class Updated extends Top {	\n" +
			"}	\n"
		},
		"*** METHOD ACCESS ***"
		+"*1* new Updated().bar(): Top.bar()"
		+"*2* new Updated().foo(): Top.foo()"
		+"*3* new Y().foo(): Top.foo()"
		+"*4* new Y().bar(): Top.bar()"
		+"*5* bar(): Top.bar()"
		+"*6* foo(): Top.foo()"
		+"*7* Y.bar(): Top.bar()"
		+"*8* this.bar(): Top.bar()"
		+"*9* this.foo(): Top.foo()");

	this.runConformTest(
		new String[] {
			"p1/Updated.java",
			"package p1; \n"+
			"public class Updated extends Top { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		Y.main(arguments);	\n" +
			"	}	\n" +
			"	static String bar() { return \"Updated.bar()\"; }	\n" +
			"	String foo() { return \"Updated.foo()\"; }	\n" +
			"} \n"
		},
		"*** METHOD ACCESS ***"
		+"*1* new Updated().bar(): Updated.bar()"
		+"*2* new Updated().foo(): Updated.foo()"
		+"*3* new Y().foo(): Updated.foo()"
		+"*4* new Y().bar(): Updated.bar()"
		+"*5* bar(): Updated.bar()"
		+"*6* foo(): Updated.foo()"
		+"*7* Y.bar(): Updated.bar()"
		+"*8* this.bar(): Updated.bar()"
		+"*9* this.foo(): Updated.foo()",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}

public void test014() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"class T {	\n" +
			"	void foo(boolean b) {	\n" +
			"		 System.out.print(\"T.foo(boolean)#\"); 	\n" +
			"	}	\n" +
			"	boolean bar = false;	\n" +
			"	class Member {	\n" +
			"		void display(){ System.out.print(\"T.Member#\"); }	\n" +
			"	}	\n" +
			"}	\n" +
			"public class X {	\n" +
			"	void foo(int i) {	\n" +
			"		 System.out.println(\"X.foo(int)#\"); 			\n" +
			"	}	\n" +
			"	int bar;	\n" +
			"	class Member {	\n" +
			"		void display(){ System.out.print(\"X.Member#\"); }	\n" +
			"	}	\n" +
			"	public static void main(String[] arguments) {	\n" +
			"		new X().bar();	\n" +
			"	}				\n" +
			"	void bar() { 	\n" +
			"		new T() {	\n" +
			"			{	\n" +
			"				foo(true);	\n" +
			"				System.out.print((boolean)bar + \"#\");	\n" +
			"				Member m = new Member();	\n" +
			"				m.display();	\n" +
			"			} 	\n" +
			"		};	\n" +
			"	}	\n" +
			"}	\n"
		},
		"T.foo(boolean)#false#T.Member#");
}

/*
 * check handling of default abstract methods
 */
public void test015() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		C c = new C() {	\n"+
			"			public void doSomething(){	\n"+
			"				System.out.println(\"SUCCESS\");	\n"+
			"			}	\n"+
			"		};	\n"+
			"		c.doSomething();	\n"+
			"	}	\n"+
			"}	\n"+
			"interface I {	\n"+
			"	void doSomething();	\n"+
			"}	\n"+
			"abstract class C implements I {	\n"+
			"}	\n"
		},
		"SUCCESS");
}

public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(boolean b) {}	\n"+
			"}	\n"+
			"public class X {	\n"+
			"      void foo(int i) {}	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(0); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"} 	\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	foo(0); 	\n" +
		"	^^^\n" +
		"The method foo(boolean) in the type T is not applicable for the arguments (int)\n" +
		"----------\n");
}

public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(boolean b) { System.out.println(\"SUCCESS\"); }	\n"+
			"}	\n"+
			"public class X {	\n"+
			"      void foo(int i) {}	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(false); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"      public static void main(String[] arguments) {	\n"+
			"			new X().bar();	\n" +
			"      }	\n"+
			"} 	\n"
		},
		"SUCCESS");
}

public void test018() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(int j) { System.out.println(\"SUCCESS\"); }	\n"+
			"}	\n"+
			"public class X {	\n"+
			"      void foo(int i) {}	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(0); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"      public static void main(String[] arguments) {	\n"+
			"			new X().bar();	\n" +
			"      }	\n"+
			"} 	\n"
		},
		"SUCCESS");
}
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(int j) { System.out.println(\"SUCCESS\"); }	\n"+
			"}	\n"+
			"class U {	\n"+
			"      void foo(int j) { System.out.println(\"FAILED\"); }	\n"+
			"}	\n"+
			"public class X extends U {	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(0); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"      public static void main(String[] arguments) {	\n"+
			"			new X().bar();	\n" +
			"      }	\n"+
			"} 	\n"
		},
		"SUCCESS");
}
public void test020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(int j) { System.out.println(\"SUCCESS\"); }	\n"+
			"}	\n"+
			"class U {	\n"+
			"      void foo(boolean j) { System.out.println(\"FAILED\"); }	\n"+
			"}	\n"+
			"public class X extends U {	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(0); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"      public static void main(String[] arguments) {	\n"+
			"			new X().bar();	\n" +
			"      }	\n"+
			"} 	\n"
		},
		"SUCCESS");
}
public void test020a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(U j) { System.out.println(\"SUCCESS\"); }	\n"+
			"}	\n"+
			"class U {	\n"+
			"}	\n"+
			"public class X extends U {	\n"+
			"      void foo(X j) { System.out.println(\"FAILED\"); }	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(new X()); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"      public static void main(String[] arguments) {	\n"+
			"			new X().bar();	\n" +
			"      }	\n"+
			"} 	\n"
		},
		"SUCCESS");
}
// binary check for 11511
public void test021() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"package p1;	\n" +
			"public class Z extends AbstractA {	\n" +
			"	public static void main(String[] arguments) {	\n" +
			"		new Z().init(); 	\n" +
			"	}	\n" +
			"}	\n" +
			"abstract class AbstractB implements K {	\n" +
			"	public void init() {	\n" +
			"		System.out.println(\"AbstractB.init()\");	\n" +
			"	}	\n" +
			"}	\n" +
			"interface K {	\n" +
			"	void init();	\n" +
			"	void init(int i);	\n" +
			"}	\n",
			"p1/AbstractA.java",
			"package p1;	\n" +
			"public abstract class AbstractA extends AbstractB implements K {	\n" +
			"	public void init(int i) {	\n" +
			"	}	\n" +
			"}	\n"
		},
		"AbstractB.init()"); // no special vm args

		// check that "new Z().init()" is bound to "Z.init()"
		String computedReferences = findReferences(OUTPUT_DIR + "/p1/Z.class");
		boolean check =
			computedReferences.indexOf("constructorRef/Z/0") >= 0
			&& computedReferences.indexOf("methodRef/init/0") >= 0;
		if (!check){
			System.out.println(computedReferences);
		}
		assertTrue("did not bind 'new Z().init()' to Z.init()'", check);
}
 /*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis
 */
public void test022() {

	this.runNegativeTest(
		new String[] {
			"p1/T.java",
			"package p1;	\n"+
			"interface II {}	\n"+
			"class TT {	\n"+
			"	void foo(boolean b) {}	\n"+
			"	void foo(int i, boolean b) {}	\n"+
			"	void foo(String s) {}	\n"+
			"}	\n"+
			"public abstract class T implements II {	\n"+
			"	void foo(int i) {}	\n"+
			"	void bar() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(0); // should say that foo(int, boolean) isn't applicable	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"	void boo() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(true); // should not complain about ambiguity	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"----------\n" +
		"1. ERROR in p1\\T.java (at line 13)\n" +
		"	foo(0); // should say that foo(int, boolean) isn\'t applicable	\n" +
		"	^^^\n" +
		"The method foo(int, boolean) in the type TT is not applicable for the arguments (int)\n" +
		"----------\n");
}

 /*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis
 */
public void test023() {

	this.runNegativeTest(
		new String[] {
			"p1/T.java",
			"package p1;	\n"+
			"interface II {}	\n"+
			"abstract class TT {	\n"+		// 259+ABSTRACT
			"	void foo(boolean b) {}	\n"+
			"	void foo(int i, boolean b) {}	\n"+
			"	void foo(String s) {}	\n"+
			"}	\n"+
			"public abstract class T implements II {	\n"+
			"	void foo(int i) {}	\n"+
			"	void bar() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(0); // should say that foo(int, boolean) isn't applicable	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"	void boo() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(true); // should complain ambiguity	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"----------\n" +
		"1. ERROR in p1\\T.java (at line 13)\n" +
		"	foo(0); // should say that foo(int, boolean) isn\'t applicable	\n" +
		"	^^^\n" +
		"The method foo(int, boolean) in the type TT is not applicable for the arguments (int)\n" +
		"----------\n");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis
 */
public void test024() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"interface II {}	\n"+
			"abstract class T implements II {	\n"+
			"	void foo(boolean b) {}	\n"+
			"	void foo(int i, boolean b) {}	\n"+
			"}	\n"+
			"abstract class TT implements II {	\n"+
			"	void foo(boolean b) {}	\n"+
			"}	\n"+
			"public class X {	\n"+
			"	void foo(int i) {}	\n"+
			"	void bar() {	\n"+
			"		new T() {	\n"+
			"			{	\n"+
			"				foo(0); // javac says foo cannot be resolved because of multiple matches	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"	void bar2() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(0); // should say that foo(boolean) isn't applicable	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"	void boo() {	\n"+
			"		new T() {	\n"+
			"			{	\n"+
			"				foo(true); // should complain ambiguity	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"}	\n"
		},
		"----------\n" +
		"1. ERROR in p1\\X.java (at line 15)\n" +
		"	foo(0); // javac says foo cannot be resolved because of multiple matches	\n" +
		"	^^^\n" +
		"The method foo(int, boolean) in the type T is not applicable for the arguments (int)\n" +
		"----------\n" +
		"2. ERROR in p1\\X.java (at line 22)\n" +
		"	foo(0); // should say that foo(boolean) isn\'t applicable	\n" +
		"	^^^\n" +
		"The method foo(boolean) in the type TT is not applicable for the arguments (int)\n" +
		"----------\n");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis (no matter if super is abstract or not)
 */
public void test025() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X extends AbstractY {	\n"+
			"	void bar(){	\n"+
			"		init(\"hello\");	\n"+
			"	}		\n"+
			"}	\n"+
			"abstract class AbstractY implements I {	\n"+
			"}	\n"+
			"interface I {	\n"+
			"	void init(String s, int i);	\n"+
			"}	\n"
		},
		"----------\n" +
		"1. ERROR in p1\\X.java (at line 2)\n" +
		"	public class X extends AbstractY {	\n" +
		"	             ^\n" +
		"The type X must implement the inherited abstract method I.init(String, int)\n" +
		"----------\n" +
		"2. ERROR in p1\\X.java (at line 4)\n" +
		"	init(\"hello\");	\n" +
		"	^^^^\n" +
		"The method init(String, int) in the type I is not applicable for the arguments (String)\n" +
		"----------\n");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis (no matter if super is abstract or not)
 */
public void test026() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X extends AbstractY {	\n"+
			"	void bar(){	\n"+
			"		init(\"hello\");	\n"+
			"	}		\n"+
			"}	\n"+
			"class AbstractY implements I {	\n"+
			"}	\n"+
			"interface I {	\n"+
			"	void init(String s, int i);	\n"+
			"}	\n"
		},
		"----------\n" +
		"1. ERROR in p1\\X.java (at line 4)\n" +
		"	init(\"hello\");	\n" +
		"	^^^^\n" +
		"The method init(String, int) in the type I is not applicable for the arguments (String)\n" +
		"----------\n" +
		"2. ERROR in p1\\X.java (at line 7)\n" +
		"	class AbstractY implements I {	\n" +
		"	      ^^^^^^^^^\n" +
		"The type AbstractY must implement the inherited abstract method I.init(String, int)\n" +
		"----------\n"
);
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11922
 * should report unreachable empty statement
 */
public void test027() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		for (;false;p());	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"	static void p(){	\n"+
			"		System.out.println(\"FAILED\");	\n"+
			"	}	\n"+
			"}	\n"
		},
		"----------\n" +
		"1. WARNING in p1\\X.java (at line 4)\n" +
		"	for (;false;p());	\n" +
		"	            ^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"2. ERROR in p1\\X.java (at line 4)\n" +
		"	for (;false;p());	\n" +
		"	                ^\n" +
		"Unreachable code\n" +
		"----------\n");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=12445
 * should report unreachable empty statement
 */
public void test028() {

	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"interface FooInterface {	\n" +
			"	public boolean foo(int a);	\n" +
			"	public boolean bar(int a);	\n" +
			"}	\n" +
			"public class X extends Z {	\n" +
			"	public boolean foo(int a){ return true; }	\n" +
			"	public boolean bar(int a){ return false; }	\n" +
			"	public static void main(String[] arguments) {	\n"+
			"		System.out.println(new X().test(0));	\n"+
			"	}	\n" +
			"}\n" +
			"abstract class Z implements FooInterface {	\n" +
			"	public boolean foo(int a, int b) {	\n" +
			"		return true;	\n" +
			"	}	\n" +
			"	public String test(int a) {	\n" +
			"		boolean result = foo(a); \n" +
			"		if (result)	\n" +
			"			return \"SUCCESS\";	\n" +
			"		else	\n" +
			"			return \"FAILED\";	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * verify error on qualified name ref in 1.4
 */
public void test029() {

	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X {	\n" +
			"	public static void main(String[] args) {	\n" +
			"		new X();	\n" +
			"		System.out.println(\"SUCCESS\");	\n" +
			"	}  	\n" +
			"	Woof woof_1;	\n" +
			"	public class Honk {	\n" +
			"		Integer honks;	\n" +
			"	}	\n" +
			"	public class Meow {	\n" +
			"		Honk honk_1;	\n" +
			"	}	\n" +
			"	public class Woof {	\n" +
			"		Meow meow_1;	\n" +
			"	}	\n" +
			"	public void setHonks(int num) {	\n" +
			"		// This is the line that causes the VerifyError	\n" +
			"		woof_1.meow_1.honk_1.honks = new Integer(num);	\n" +
			"		// Here is equivalent code that does not cause the error.	\n" +
			"		//  Honk h = woof_1.moo_1.meow_1.honk_1;	\n" +
			"		//  h.honks = new Integer(num);	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * 1.4 signals invocations of non-visible abstract protected method implementations.
 */
public void test030() {

	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X {	\n" +
			"	public static void main(String[] args){	\n" +
			"		new q.X2().foo(\"String\");	\n" +
			"		new q.X2().bar(\"String\");	\n" +
			"		new q.X2().barbar(\"String\");	\n" +
			"		new q.X2().baz(\"String\");	\n" +
			"	}	\n" +
			"}	\n",

			"p/X1.java",
			"package p;	\n" +
			"public abstract class X1 {	\n" +
			"	protected void foo(Object o){	System.out.println(\"X1.foo(Object)\"); }	\n" +
			"	protected void bar(Object o){	System.out.println(\"X1.bar(Object)\"); }	\n" +
			"	void barbar(Object o){	System.out.println(\"X1.barbar(Object)\"); }	\n" +
			"	protected void baz(Object o) { System.out.println(\"X1.baz(Object)\"); }	\n" +
			"}	\n",

			"q/X2.java",
			"package q;	\n" +
			"public class X2 extends p.X1 {	\n" +
			"	protected void foo(int i) { System.out.println(\"X2.foo(int)\"); }	\n" +
			"	protected void bar(Object o) { System.out.println(\"X2.bar(Object)\"); }	\n" +
			"	void barbar(Object o){	System.out.println(\"X2.barbar(Object)\"); }	\n" +
			"	protected void baz(String s) {	System.out.println(\"X2.baz(String)\"); }	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 5)\n" +
		"	new q.X2().bar(\"String\");	\n" +
		"	           ^^^\n" +
		"The method bar(Object) from the type X2 is not visible\n" +
		"----------\n" +
		"2. ERROR in p\\X.java (at line 6)\n" +
		"	new q.X2().barbar(\"String\");	\n" +
		"	           ^^^^^^\n" +
		"The method barbar(Object) from the type X2 is not visible\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in q\\X2.java (at line 5)\n" +
		"	void barbar(Object o){	System.out.println(\"X2.barbar(Object)\"); }	\n" +
		"	     ^^^^^^^^^^^^^^^^\n" +
		"The method X2.barbar(Object) does not override the inherited method from X1 since it is private to a different package\n" +
		"----------\n");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * 1.4 signals invocations of non-visible abstract protected method implementations.
 */
public void test031() {

	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X extends q.X2 {	\n" +
			"	public static void main(String[] args){	\n" +
			"			new X().doSomething();	\n" +
			"	}	\n" +
			"	void doSomething(){	\n" +
			"		foo(\"String\");	\n" +
			"		bar(\"String\");	\n" +
			"		barbar(\"String\");	\n" +
			"		baz(\"String\");	\n" +
			"	}	\n" +
			"}	\n",

			"p/X1.java",
			"package p;	\n" +
			"public abstract class X1 {	\n" +
			"	protected void foo(Object o){	System.out.println(\"X1.foo(Object)\"); }	\n" +
			"	protected void bar(Object o){	System.out.println(\"X1.bar(Object)\"); }	\n" +
			"	void barbar(Object o){	System.out.println(\"X1.barbar(Object)\"); }	\n" +
			"	protected void baz(Object o) { System.out.println(\"X1.baz(Object)\"); }	\n" +
			"}	\n",

			"q/X2.java",
			"package q;	\n" +
			"public class X2 extends p.X1 {	\n" +
			"	protected void foo(int i) { System.out.println(\"X2.foo(int)\"); }	\n" +
			"	protected void bar(Object o) { System.out.println(\"X2.bar(Object)\"); }	\n" +
			"	void barbar(Object o){	System.out.println(\"X2.barbar(Object)\"); }	\n" +
			"	protected void baz(String s) {	System.out.println(\"X2.baz(String)\"); }	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 9)\n" +
		"	barbar(\"String\");	\n" +
		"	^^^^^^\n" +
		"The method barbar(Object) from the type X2 is not visible\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in q\\X2.java (at line 5)\n" +
		"	void barbar(Object o){	System.out.println(\"X2.barbar(Object)\"); }	\n" +
		"	     ^^^^^^^^^^^^^^^^\n" +
		"The method X2.barbar(Object) does not override the inherited method from X1 since it is private to a different package\n" +
		"----------\n"
);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * 1.4 signals invocations of non-visible abstract protected field implementations.
 */
public void test032() {

	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X {	\n" +
			"	public static void main(String[] args){	\n" +
			"		System.out.println(new q.X2().foo);	\n" +
			"		System.out.println(new q.X2().bar);	\n" +
			"	}	\n" +
			"}	\n",

			"p/X1.java",
			"package p;	\n" +
			"public abstract class X1 {	\n" +
			"	protected String foo = \"X1.foo\"; 	\n" +
			"	String bar = \"X1.bar\";	\n" +
			"}	\n",

			"q/X2.java",
			"package q;	\n" +
			"public class X2 extends p.X1 {	\n" +
			"	protected String foo = \"X2.foo\";	\n" +
			"	String bar = \"X2.bar\";	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 4)\n" +
		"	System.out.println(new q.X2().foo);	\n" +
		"	                              ^^^\n" +
		"The field X2.foo is not visible\n" +
		"----------\n" +
		"2. ERROR in p\\X.java (at line 5)\n" +
		"	System.out.println(new q.X2().bar);	\n" +
		"	                              ^^^\n" +
		"The field X2.bar is not visible\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in q\\X2.java (at line 3)\n" +
		"	protected String foo = \"X2.foo\";	\n" +
		"	                 ^^^\n" +
		"The field X2.foo is hiding a field from type X1\n" +
		"----------\n");
}

/*
 * Initialization of synthetic fields prior to super constructor call
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23075
 */
public void test033() {

	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {	\n"+
			"  public int m;	\n"+
			"  public void pp() {	\n"+
			"     C c = new C(4);	\n"+
			"     System.out.println(c.get());	\n"+
			"  }	\n"+
			"  public static void main(String[] args) {	\n"+
			"     A a = new A();	\n"+
			"	  try {	\n"+
			"       a.pp(); 	\n"+
			"		System.out.println(\"SyntheticInit BEFORE SuperConstructorCall\");	\n"+
			"	  } catch(NullPointerException e) {	\n"+
			"		System.out.println(\"SyntheticInit AFTER SuperConstructorCall\"); // should no longer occur with target 1.4 \n"+
			"	  }	\n"+
			"  }	\n"+
			"  class C extends B {	\n"+
			"    public C(int x1) {	\n"+
			"      super(x1);    	\n"+
			"    }	\n"+
			"    protected void init(int x1) {	\n"+
			"       x = m * x1; // <- NULL POINTER EXCEPTION because of m	\n"+
			"    }  	\n"+
			"  }	\n"+
			"}	\n"+
			"class B {	\n"+
			"  int x;	\n"+
			"  public B(int x1) {	\n"+
			"    init(x1);	\n"+
			"  }	\n"+
			"  protected void init(int x1) {	\n"+
			"    x  = x1;	\n"+
			"  }	\n"+
			"  public int get() {	\n"+
			"    return x;	\n"+
			"  }	\n"+
			"}	\n"
		},
		"0\n" +
		"SyntheticInit BEFORE SuperConstructorCall");
}
/*
 * Initialization of synthetic fields prior to super constructor call - NPE check
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25174
 */
public void test034() {

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().new X2();	\n"+
			"	}	\n"+
			"	class X1 {	\n"+
			"		X1(){	\n"+
			"			this.baz();	\n"+
			"		}	\n"+
			"		void baz() {	\n"+
			"			System.out.println(\"-X1.baz()\");	\n"+
			"		}	\n"+
			"	}	\n"+
			"	class X2 extends X1 {	\n"+
			"		void baz() {	\n"+
			"			System.out.print(X.this==null ? \"X.this == null\" : \"X.this != null\");	\n"+
			"			X1 x1 = X.this.new X1(){	\n"+
			"				void baz(){	\n"+
			"					System.out.println(\"-X$1.baz()\");	\n"+
			"				}	\n"+
			"			};	\n"+
			"		}	\n"+
			"	}	\n"+
			"}\n",
		},
		"X.this != null-X$1.baz()");
}

public void test035() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	class Y { Y(int i){} }	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		int i = 1;	\n" +
			"		try {	\n" +
			"			X x =null;	\n" +
			"			x.new Y(++i);	\n" + // i won't get incremented before NPE
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(NullPointerException e){	\n" +
			"			System.out.println(\"SUCCESS:\"+i);	\n" +
			"		}	\n" +
			"	}	\n"+
			"}	\n",
		},
		"SUCCESS:1"
	);
}

public void test036() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	class Y {}	\n"+
			"	static class Z extends Y {	\n"+
			"		Z (X x){	\n"+
			"			x.super();	\n" +
			"		}		\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		try {	\n" +
			"			new Z(null);	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(NullPointerException e){	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n"+
			"}	\n",
		},
		"SUCCESS"
	);
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=24744
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23096
 *
 * NOTE: since JLS got revised to allow unterminated line comments (32476)
 */
public void test037() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_TaskTags, "TODO:");
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n"+
			"public class X {\n"+
			"}\n"+
			"// TODO: something"
		},
		"----------\n" +
		"1. WARNING in p\\X.java (at line 4)\n" +
		"	// TODO: something\n" +
		"	   ^^^^^^^^^^^^^^^\n" +
		"TODO: something\n" +
		"----------\n",
		null,
		true,
		customOptions);
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=24833
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23096
 *
 * NOTE: since JLS got revised to allow unterminated line comments (32476)
 */
public void test038() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_TaskTags, "TODO:");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"// TODO: something"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	// TODO: something\n" +
		"	   ^^^^^^^^^^^^^^^\n" +
		"TODO: something\n" +
		"----------\n",
		null,
		true,
		customOptions,
		"java.lang.ClassNotFoundException");
}

/*
 * unreachable empty statement/block are diagnosed in 1.3
 */
public void test039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"	public static void main(String[] args){	\n"+
			"		for (;null != null;);	\n"+
			"		for (;null != null;){}	\n"+
			"		for (;false;);	\n"+
			"		for (;false;){}	\n"+
			"		while (false);	\n"+
			"		while (false){}	\n"+
			"		if (false) {} else {}	\n"+
			"		if (false) ; else ;			\n"+
			"		System.out.println(\"FAILED\");	\n" +
			"	}	\n"+
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	for (;false;);	\n" +
		"	             ^\n" +
		"Unreachable code\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	for (;false;){}	\n" +
		"	             ^^\n" +
		"Unreachable code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	while (false);	\n" +
		"	             ^\n" +
		"Unreachable code\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 8)\n" +
		"	while (false){}	\n" +
		"	             ^^\n" +
		"Unreachable code\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 9)\n" +
		"	if (false) {} else {}	\n" +
		"	           ^^\n" +
		"Dead code\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 10)\n" +
		"	if (false) ; else ;			\n" +
		"	           ^\n" +
		"Dead code\n" +
		"----------\n"
);
}
// jls6.5.5.1 - simple type names favor member type over toplevel one.
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=30705
public void test040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	interface Homonym {}	\n"+ // takes precedence over others.
			"	void foo() {	\n"+
			"		class Homonym extends X {	\n"+
			"			{	\n"+
			"				class Y extends Homonym {};	\n"+ // X$Homonym
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"}	\n"+
			"class Homonym extends X {	\n"+
			"	{	\n"+
			"		class Y extends Homonym {};	\n"+ // X$Homonym
			"	}	\n"+
			"}	\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	class Homonym extends X {	\n" +
		"	      ^^^^^^^\n" +
		"The type Homonym is hiding the type X.Homonym\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	class Y extends Homonym {};	\n" +
		"	                ^^^^^^^\n" +
		"The type X.Homonym cannot be the superclass of Y; a superclass must be a class\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 13)\n" +
		"	class Y extends Homonym {};	\n" +
		"	                ^^^^^^^\n" +
		"The type X.Homonym cannot be the superclass of Y; a superclass must be a class\n" +
		"----------\n");
}
/*
 * 30856 - 1.4 compliant mode should consider abstract method matches
 */
public void test041() {
	this.runConformTest(
		new String[] {
			"p/X.java", //================================
			"package p;	\n" +
			"public class X {	\n" +
			"	void foo(int i, float f){}	\n" +
			"	public static void main(String[] args) {	\n" +
			"		q.Y y = new q.Y.Z();	\n" +
			"		y.bar();	\n" +
			"	}	\n" +
			"}	\n",
			"q/Y.java", //================================
			"package q;	\n" +
			"public abstract class Y extends p.X implements I {	\n" +
			"	public void bar(){   foo(1, 2); }	\n" +
			"	public static class Z extends Y {	\n" +
			"		public void foo(float f, int i) {	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n" +
			"interface I {	\n" +
			"	void foo(float f, int i);	\n" +
			"}	\n",
		},
		"SUCCESS");
}
/*
 * variation - 30856 - 1.4 compliant mode should consider abstract method matches
 */
public void test042() {
	this.runConformTest(
		new String[] {
			"p/X.java", //================================
			"package p;	\n" +
			"public class X extends X0 {	\n" +
			"	void foo(int i, float f){}	\n" +
			"	public static void main(String[] args) {	\n" +
			"		q.Y y = new q.Y.Z();	\n" +
			"		y.bar();	\n" +
			"	}	\n" +
			"}	\n" +
			"class X0 {	\n" +
			"	void foo(int i, double d){}	\n" + // extra match
			"}	\n",
			"q/Y.java", //================================
			"package q;	\n" +
			"public abstract class Y extends p.X implements I {	\n" +
			"	public void bar(){   foo(1, 2); }	\n" +
			"	public static class Z extends Y {	\n" +
			"		public void foo(float f, int i) {	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n" +
			"interface I {	\n" +
			"	void foo(float f, int i);	\n" +
			"}	\n",
		},
		"SUCCESS");
}

// binary compatibility
public void _test043() {
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"package p1;	\n" +
			"public class Y extends A implements I { \n" +
			"	public static void main(String[] args) {	\n" +
			"		Y.printValues();	\n" +
			"	}	\n" +
			"	public static void printValues() {	\n" +
			"		System.out.println(\"i=\"+i+\",j=\"+j+\",Y.i=\"+Y.i+\",Y.j=\"+Y.j);	\n" +
			"	}	\n" +
			"}	\n",
			"p1/A.java",
			"package p1;	\n" +
			"public class A {	\n" +
			"	static int i = 1;	\n" +
			"}	\n",
			"p1/I.java",
			"package p1;	\n" +
			"interface I {	\n" +
			"	int j = \"aa\".length();	\n" +
			"}	\n",
		},
		"i=1,j=2,Y.i=1,Y.j=2");

	this.runConformTest(
		new String[] {
			"p1/A.java",
			"package p1;	\n" +
			"public class A {	\n" +
			"	static int j = 3;	\n" +
			"	public static void main(String[] args) {	\n" +
			"		Y.printValues();	\n" +
			"	}	\n" +
			"}	\n",
			"p1/I.java",
			"package p1;	\n" +
			"interface I {	\n" +
			"	int j = \"aaaa\".length();	\n" +
			"}	\n",
		},
		"i=4,j=3,Y.i=4,Y.j=3",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}
/*
 * array.clone() should use array type in methodRef
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=36307
 */
public void test044() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"		args.clone();	\n"+
			"		System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel <= ClassFileConstants.JDK1_4
		?	"     1  invokevirtual java.lang.Object.clone() : java.lang.Object [16]\n"
		:	"     1  invokevirtual java.lang.String[].clone() : java.lang.Object [16]\n";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
// 39172
public void test045() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X { \n" +
			"	public static void main(String[] args) {	\n" +
			"		System.out.println(\"FAILED\");	\n" +
			"		return;;	\n" + // unreachable empty statement - must complain in 1.4 mode
			"	}	\n" +
			"}	\n"
		},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 5)\n" +
		"	return;;	\n" +
		"	       ^\n" +
		"Unreachable code\n" +
		"----------\n"
	);
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=39467
 * should diagnose missing abstract method implementation
 */
public void test046() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X extends Y {\n" +
			"}\n" +
			"abstract class Y extends Z {\n" +
			"  public abstract void foo();\n" +
			"}\n" +
			"abstract class Z extends T {\n" +
			"}\n" +
			"class T implements I {\n" +
			"  public void foo(){}\n" +
			"}\n" +
			"interface I {\n" +
			"    public void foo ();\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X extends Y {\n" +
		"	             ^\n" +
		"The type X must implement the inherited abstract method Y.foo()\n" +
		"----------\n"
	);
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=40442
 * Abstract class fails to invoke interface-defined method in 1.4 compliance mode.
 */
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends AbstractDoubleAlgorithm {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		((ObjectAlgorithm)(new X())).operate(new Double(0));\n" +
			"	}\n" +
			"    public void operate(Double pDouble)\n" +
			"    {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n" +
			"abstract class AbstractDoubleAlgorithm implements DoubleAlgorithm {\n" +
			"    public void operate(Object pObject)\n" +
			"    {\n" +
			"        operate((Double)pObject);\n" +
			"    }\n" +
			"}\n" +
			"interface DoubleAlgorithm extends ObjectAlgorithm {\n" +
			"    void operate(Double pDouble);\n" +
			"}\n" +
			"interface ObjectAlgorithm {\n" +
			"    void operate(Object pObject);\n" +
			"}"
		},
		"SUCCESS"
	);
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=40442
 * Abstract class fails to invoke interface-defined method in 1.4 compliance mode.
 * variation with 2 found methods
 */
public void test048() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends AbstractDoubleAlgorithm {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		((ObjectAlgorithm)(new X())).operate(new Double(0));\n" +
			"	}\n" +
			"    public void operate(Double pDouble)\n" +
			"    {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n" +
			"abstract class AbstractDoubleAlgorithm implements DoubleAlgorithm {\n" +
			"    public void operate(Object pObject)\n" +
			"    {\n" +
			"        operate((Double)pObject);\n" +
			"    }\n" +
			"    public void operate(X x) {}\n" +
			"}\n" +
			"interface DoubleAlgorithm extends ObjectAlgorithm {\n" +
			"    void operate(Double pDouble);\n" +
			"}\n" +
			"interface ObjectAlgorithm {\n" +
			"    void operate(Object pObject);\n" +
			"}"
		},
		"SUCCESS"
	);
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=41278
 */
public void test049() {
	this.runNegativeTest(
		new String[] {
			"pa/Caller.java",
			"package pa;\n" +
			"import pb.Concrete;\n" +
			"public class Caller {\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		Concrete aConcrete = new Concrete(); \n" +
			"		aConcrete.callme();\n" +
			"	}\n" +
			"}\n",
			"pa/Abstract.java",
			"package pa;\n" +
			"public abstract class Abstract {\n" +
			"\n" +
			"	protected void callme(){}\n" +
			"}\n",
			"pb/Concrete.java",
			"package pb;\n" +
			"public class Concrete extends pa.Abstract {\n" +
			"\n" +
			"	protected void callme(){	System.out.println(\"SUCCESS\"); }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in pa\\Caller.java (at line 7)\n" +
		"	aConcrete.callme();\n" +
		"	          ^^^^^^\n" +
		"The method callme() from the type Concrete is not visible\n" +
		"----------\n");
}

public void test050() {
	this.runNegativeTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  public static void main(String args[]) {\n" +
		"     foo();\n" +
		"  }\n" +
		"  public static void foo() {\n" +
		"     int a1 = 1;\n" +
		"     int a2 = 1;\n" +
		"     a1 = 2;\n" +
		"     while (false) {};\n" +
		"     a2 = 2;\n" +
		"  }\n" +
		"}\n",
	},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 10)\n" +
		"	while (false) {};\n" +
		"	              ^^\n" +
		"Unreachable code\n" +
		"----------\n");
}

public void test051() {
	this.runNegativeTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  public static void main(String args[]) {\n" +
		"     foo();\n" +
		"  }\n" +
		"  public static void foo() {\n" +
		"     int a1 = 1;\n" +
		"     int a2 = 1;\n" +
		"     a1 = 2;\n" +
		"     while (false);\n" +
		"     a2 = 2;\n" +
		"  }\n" +
		"}\n",
	},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 10)\n" +
		"	while (false);\n" +
		"	             ^\n" +
		"Unreachable code\n" +
		"----------\n");
}

public void test052() {
	this.runNegativeTest(
		new String[] {
			"p/A.java",
			"package p;\n" +
			"public class A {\n" +
			"  public static void main(String[] argv) {\n" +
			"    foo();\n" +
			"  }\n" +
			"  private int i;\n" +
			"  static class Y extends X {\n" +
			"    int x = i;\n" +
			"  }\n" +
			"  public static void foo() {\n" +
			"    return;\n" +
			"  }\n" +
			"}",

			"p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"  public static void main(String argv[]) {\n" +
			"     foo();\n" +
			"  }\n" +
			"  public static void foo() {\n" +
			"     int a1 = 1;\n" +
			"     int a2 = 1;\n" +
			"     a1 = 2;\n" +
			"     while (false);\n" +
			"     a2 = 2;\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in p\\A.java (at line 6)\n" +
		"	private int i;\n" +
		"	            ^\n" +
		"The value of the field A.i is not used\n" +
		"----------\n" +
		"2. ERROR in p\\A.java (at line 8)\n" +
		"	int x = i;\n" +
		"	        ^\n" +
		"Cannot make a static reference to the non-static field i\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in p\\X.java (at line 10)\n" +
		"	while (false);\n" +
		"	             ^\n" +
		"Unreachable code\n" +
		"----------\n");
}

public void test053() {
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"class X {\n" +
			"  static class A {\n" +
			"    interface I {\n" +
			"      int a = 3;\n" +
			"    }\n" +
			"  } \n" +
			"  interface I { \n" +
			"    int b = 4;\n" +
			"  }\n" +
			"  class Y extends A implements I {\n" +
			"    Object F() {\n" +
			"      return new I() {\n" +
			"        int c = a; // WE SHOULD NOT BE ABLE TO SEE BOTH a and b\n" +
			"        int d = b; // WE SHOULD NOT BE ABLE TO SEE BOTH a and b\n" +
			"      };\n" +
			"    }\n" +
			"  }\n" +
			"}",
		}
	);
}

public void test054() {
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"  static class A {\n" +
			"    interface I {\n" +
			"      int a = 3;\n" +
			"      void foo();\n" +
			"    }\n" +
			"  }\n" +
			"  interface I {\n" +
			"    int a = 4;\n" +
			"    void foo();\n" +
			"  }\n" +
			"  class Y extends A implements I {\n" +
			"    public void foo() {\n" +
			"      new I() {\n" +
			"        public void foo() {\n" +
			"          System.out.println(\"X$1::foo-\" + a);\n" +
			"        }\n" +
			"      }\n" +
			"      .foo();\n" +
			"    }\n" +
			"  }\n" +
			"public static void main(String argv[]) {\n" +
			"  new X().new Y().foo();\n" +
			"}\n" +
			"}",
		}
	);
}
public void test055() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"  static class A {\n" +
			"    interface I2 {\n" +
			"      int a = 3;\n" +
			"      void foo();\n" +
			"    }\n" +
			"  }\n" +
			"  interface I1 {\n" +
			"    int a = 4;\n" +
			"    void foo(int a);\n" +
			"  }\n" +
			"  class Y extends A implements I1 {\n" +
			"    public void foo(int a) {\n" +
			"      new I2() {\n" +
			"        public void foo() {\n" +
			"          System.out.println(\"X$1::foo-\" + a);\n" +
			"        }\n" +
			"      }\n" +
			"      .foo();\n" +
			"    }\n" +
			"  }\n" +
			"public static void main(String argv[]) {\n" +
			"  new X().new Y().foo(8);\n" +
			"}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in p\\X.java (at line 11)\n" +
		"	void foo(int a);\n" +
		"	             ^\n" +
		"The parameter a is hiding a field from type X.I1\n" +
		"----------\n" +
		"2. WARNING in p\\X.java (at line 14)\n" +
		"	public void foo(int a) {\n" +
		"	                    ^\n" +
		"The parameter a is hiding a field from type X.I1\n" +
		"----------\n"
	);
}

public void test056() {
	this.runConformTest(
		new String[] {
			"p/MethodQualification.java",
			"package p;\n" +
			"public class MethodQualification {\n" +
			"  void foo() {\n" +
			"  System.out.println(\"Inherited foo() for anonymous type\");\n" +
			"  class Local {\n" +
			"    void foo(){\n" +
			"    System.out.println(\"Enclosing foo() for anonymous type\");\n" +
			"    new MethodQualification () { {foo();} };\n" +
			"    }\n" +
			"  };\n" +
			"  }  \n" +
			"}",
		},
		""
	);
}

public void test057() {
	this.runConformTest(
		new String[] {
			"p/AG.java",
			"package p;\n" +
			"/**\n" +
			" * 1F9RITI\n" +
			" */\n" +
			"public class AG {\n" +
			"  public class X {\n" +
			"    class B {\n" +
			"      int intValueOfB = -9;\n" +
			"    }\n" +
			"    class SomeInner extends A {\n" +
			"      void someMethod() {\n" +
			"        int i = new B().intValueOfB; \n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  class A {\n" +
			"    class B {\n" +
			"      int intValueOfB = -9;\n" +
			"    }\n" +
			"  }\n" +
			"}",
		},
		""
	);
}

public void test058() {
	this.runConformTest(
		new String[] {
			"p/AE.java",
			"package p;\n" +
			"/**\n" +
			" * 1F9RITI\n" +
			" */\n" +
			"public class AE {\n" +
			"  public class X {\n" +
			"    int intValue = 153;\n" +
			"    class SomeInner extends A {\n" +
			"      void someMethod() {\n" +
			"        int i = intValue; \n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  class A {\n" +
			"    int intValue = 153;\n" +
			"  }\n" +
			"}",
		},
		""
	);
}

public void test059() {
	this.runNegativeTest(
		new String[] {
			"p/FieldQualification.java",
			"package p;\n" +
			"public class FieldQualification {\n" +
			"  String field = \"Inherited field for anonymous type\";\n" +
			"void foo() {\n" +
			"  class Local {\n" +
			"    String field = \"Enclosing field for anonymous type\";\n" +
			"    void foo() {\n" +
			"      System.out.println(\"Enclosing foo() for anonymous type\");\n" +
			"      new FieldQualification() {\n" +
			"        {\n" +
			"          System.out.println(field);\n" +
			"        }\n" +
			"      };\n" +
			"    }\n" +
			"  };\n" +
			"}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in p\\FieldQualification.java (at line 5)\n" +
		"	class Local {\n" +
		"	      ^^^^^\n" +
		"The type Local is never used locally\n" +
		"----------\n" +
		"2. WARNING in p\\FieldQualification.java (at line 6)\n" +
		"	String field = \"Enclosing field for anonymous type\";\n" +
		"	       ^^^^^\n" +
		"The field Local.field is hiding a field from type FieldQualification\n" +
		"----------\n" +
		"3. WARNING in p\\FieldQualification.java (at line 6)\n" +
		"	String field = \"Enclosing field for anonymous type\";\n" +
		"	       ^^^^^\n" +
		"The value of the field Local.field is not used\n" +
		"----------\n" +
		"4. WARNING in p\\FieldQualification.java (at line 7)\n" +
		"	void foo() {\n" +
		"	     ^^^^^\n" +
		"The method foo() from the type Local is never used locally\n" +
		"----------\n");
}

public void test060() {
	this.runConformTest(
		new String[] {
			"p/AF.java",
			"package p;\n" +
			"/**\n" +
			" * 1F9RITI\n" +
			" */\n" +
			"public class AF {\n" +
			"  public class X {\n" +
			"    int intMethod() {\n" +
			"      return 3333;\n" +
			"    }\n" +
			"    class SomeInner extends A {\n" +
			"      void someMethod() {\n" +
			"        int i = intMethod();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  class A {\n" +
			"    int intMethod() {\n" +
			"      return 3333;\n" +
			"    }\n" +
			"  }\n" +
			"}",
		},
		""
	);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=32342
 */
public void test061() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //======================
			"package p;	\n" +
			"public class X extends q.Y {	\n" +
			"	X someField;	\n" + // no ambiguity since inherited Y.X isn't visible
			"}	\n" +
			"class Z extends q.Y {	\n" +
			"	Z someField;	\n" + // ambiguous
			"}	\n",
			"q/Y.java", //======================
			"package q;	\n" +
			"public class Y {	\n" +
			"	private static class X {}	\n" +
			"	public static class Z {}	\n" +
			"}	\n"
		},
		"----------\n" +
		"1. WARNING in q\\Y.java (at line 3)\n" +
		"	private static class X {}	\n" +
		"	                     ^\n" +
		"The type Y.X is never used locally\n" +
		"----------\n");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11435
 * variant - must still complain when targeting super abstract method
 */
public void test062() {

	this.runNegativeTest(
		new String[] {
			"p1/Y.java",
			"package p1;	\n"+
			"public class Y extends AbstractT {	\n"+
			"	public void init(){	\n"+
			"		super.init();	\n"+
			"	}	\n"+
			"}	\n"+
			"abstract class AbstractT implements J {	\n"+
			"}	\n"+
			"interface J {	\n"+
			"	void init();	\n"+
			"}	\n"
		},
		"----------\n" +
		"1. ERROR in p1\\Y.java (at line 4)\n" +
		"	super.init();	\n" +
		"	^^^^^^^^^^^^\n" +
		"Cannot directly invoke the abstract method init() for the type J\n" +
		"----------\n"); // expected log
}

public void test063() {
	this.runNegativeTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	class Y extends X {}	\n"+
			"	class Z extends Y {	\n"+
			"		Z(){	\n"+
			"			System.out.println(\"SUCCESS\");	\n"+
			"		}	\n" +
			"	}	\n" +
			"	public static void main(String[] arguments) {	\n"+
			"		new X().new Z();	\n"+
			"	}	\n"+
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in p1\\X.java (at line 5)\n" +
		"	Z(){	\n" +
		"	^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n"
	);
}

/**
 * Refuse selection of own enclosing instance arg for super constructor call in 1.3 compliant mode
 */
public void test064() {
	this.runNegativeTest(
		new String[] {
			"Foo.java",
			"public class Foo {\n" +
			"	public static void main(String[] args) {\n"+
			"		System.out.println(\"SUCCESS\");\n"+
			"	}\n"+
			"	public class Bar extends Foo {\n" +
			"		public Bar() {\n" +
			"		}\n" +
			"	}\n" +
			"	public class Baz extends Bar {\n" +
			"		public Baz() {\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Foo.java (at line 10)\n" +
		"	public Baz() {\n" +
		"	       ^^^^^\n" +
		"No enclosing instance of type Foo is available due to some intermediate constructor invocation\n" +
		"----------\n");
}

public void test065() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().new Y().new Z().bar();	\n"+
			"	}	\n"+
			"	String foo() { return \"X-foo\"; }	\n"+
			"	class Y extends X {	\n"+
			"		String foo() { return \"Y-foo\"; }	\n"+
			"		class Z extends Y {	\n"+
			"			Z(){	\n"+
			"				//X.this.super();	\n"+
			"			}	\n"+
			"			String foo() { return \"Z-foo\"; }	\n"+
			"			void bar () {	\n"+
			"				System.out.println(X.this.foo());	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"}	\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	Z(){	\n" +
		"	^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n");
}

/*
 * Check that anonymous type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test066() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X(Object o) {}\n" +
			"  class M extends X {\n" +
			"    M(){\n" +
			"      super(null);\n" +
			"    }\n" +
			"    M(Object o) {\n" +
			"      super(new M(){});\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	super(new M(){});\n" +
		"	      ^^^^^^^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	super(new M(){});\n" +
		"	          ^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n");
}

/*
 * Check that indirect member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test067() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X(Object o) {\n" +
			"	}\n" +
			"	class N extends X {\n" +
			"		N(Object o) {\n" +
			"			super(o);\n" +
			"		}\n" +
			"	}\n" +
			"	class M extends N {\n" +
			"		M() {\n" +
			"			super(null); //1\n" +
			"		}\n" +
			"		M(Object o) {\n" +
			"			super(new M());//2\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	super(null); //1\n" +
		"	^^^^^^^^^^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	super(new M());//2\n" +
		"	^^^^^^^^^^^^^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	super(new M());//2\n" +
		"	      ^^^^^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n");
}

/*
 * Check that indirect member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test068() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	class MX1 extends X {\n" +
			"		MX1() {\n" +
			"		}\n" +
			"	}\n" +
			"	class MX2 extends MX1 {\n" +
			"		MX2() {\n" +
			"			super();	// ko\n" +
			"		}\n" +
			"		MX2(X x) {\n" +
			"			this();		// ok\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	super();	// ko\n" +
		"	^^^^^^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n");
}

/*
 * Check that indirect member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test069() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	class MX3 extends X {\n" +
			"		MX3(X x) {\n" +
			"		}\n" +
			"	}\n" +
			"	class MX4 extends MX3 {\n" +
			"		MX4() {\n" +
			"			super(new MX4());	// ko\n" +
			"		}\n" +
			"		MX4(X x) {\n" +
			"			this();		// ok\n" +
			"		}\n" +
			"		MX4(int i) {\n" +
			"			this(new MX4());		// ko\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	super(new MX4());	// ko\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	super(new MX4());	// ko\n" +
		"	      ^^^^^^^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	this(new MX4());		// ko\n" +
		"	     ^^^^^^^^^\n" +
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" +
		"----------\n");
}

// binary compatibility
public void test070() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends Middle {\n" +
			"	public static void main(String argv[]) {\n" +
			"		System.out.println(new X().field);\n" +
			"	}\n" +
			"}\n" +
			"class Middle extends Top {\n" +
			"}\n" +
			"class Top {\n" +
			"	String field = \"Top.field\";\n" +
			"}\n"
		},
		"Top.field");

	this.runConformTest(
		new String[] {
			"Middle.java",
			"public class Middle extends Top {\n" +
			"	public static void main(String[] arguments) { \n"+
			"		X.main(arguments);	\n" +
			"	}	\n" +
			"	String field = \"Middle.field\";\n" +
			"}\n"
		},
		"Middle.field",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}

/*
 * 43429 - AbstractMethodError calling clone() at runtime when using Eclipse compiler
 */
public void test071() {
	this.runConformTest(
		new String[] {
			"X.java", //================================
			"public class X {\n" +
			"	public interface Copyable extends Cloneable {\n" +
			"		public Object clone() throws CloneNotSupportedException;\n" +
			"	}\n" +
			"	public interface TestIf extends Copyable {\n" +
			"	}\n" +
			"	public static class ClassA implements Copyable {\n" +
			"		public Object clone() throws CloneNotSupportedException {\n" +
			"			return super.clone();\n" +
			"		}\n" +
			"	}\n" +
			"	public static class ClassB implements TestIf {\n" +
			"		public Object clone() throws CloneNotSupportedException {\n" +
			"			return super.clone();\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		Copyable o1 = new ClassA();\n" +
			"		ClassB o2 = new ClassB();\n" +
			"		TestIf o3 = o2;\n" +
			"		Object clonedObject;\n" +
			"		clonedObject = o1.clone();\n" +
			"		clonedObject = o2.clone();\n" +
			"		// The following line fails at runtime with AbstractMethodError when\n" +
			"		// compiled with Eclipse\n" +
			"		clonedObject = o3.clone();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
public void test072() {

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        try {\n" +
			"            f();\n" +
			"        } catch(NullPointerException e) {\n" +
			"            System.out.println(\"SUCCESS\");\n" +
			"        }\n" +
			"    }\n" +
			"    static void f() {\n" +
			"        Object x = new Object() {\n" +
			"            {\n" +
			"                    if (true) throw null;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}",
		},
	"SUCCESS");
}
// 52221
public void test073() {

	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        \n" +
			"        switch(args.length) {\n" +
			"            \n" +
			"            case 1:\n" +
			"                int i = 0;\n" +
			"                class Local {\n" +
			"	            }\n" +
			"                break;\n" +
			"                \n" +
			"			case 0 :\n" +
			"			    System.out.println(i); // local var can be referred to, only an initialization pb\n" +
			"			    System.out.println(new Local());\n" +
			"        		break;\n" +
			"\n" +
			"			case 2 :\n" +
			"                class Local { // not a duplicate\n" +
			"	            }\n" +
			"        		break;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 14)\n" +
		"	System.out.println(new Local());\n" +
		"	                       ^^^^^\n" +
		"Local cannot be resolved to a type\n" +
		"----------\n");
}

// checking for captured outer local initialization status
// NOTE: only complain against non-inlinable outer locals
// http://bugs.eclipse.org/bugs/show_bug.cgi?id=26134
public void test074() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"    	String nonInlinedString = \"[Local]\";	\n" +
			"    	int i = 2;	\n" +
			"		switch(i){	\n" +
			"			case 1:	\n" +
			"				final String displayString = nonInlinedString;\n" +
			"				final String inlinedString = \"a\";	\n" +
			"				class Local {	\n" +
			"					public String toString() {	\n" +
			"						return inlinedString + displayString;	\n" +
			"					}	\n" +
			"				}	\n" +
			"			case 2:	\n" +
			"				System.out.print(new Local());	\n" +
			"				System.out.print(\"-\");	\n" +
			"				System.out.println(new Local(){	\n" +
			"					public String toString() {	\n" +
			"						return super.toString()+\": anonymous\";	\n" +
			"					}	\n" +
			"				});	\n" +
			"		}	\n" +
			"    }	\n" +
			"}	\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	System.out.print(new Local());	\n" +
		"	                     ^^^^^\n" +
		"Local cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 17)\n" +
		"	System.out.println(new Local(){	\n" +
		"	                       ^^^^^\n" +
		"Local cannot be resolved to a type\n" +
		"----------\n");
}
public void test075() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"    public void foo(int p1) {} \n" +
			"    public void foo(short p1) {} \n" +
			"}	\n",
			"Y.java",
			"public class Y extends X {	\n" +
			"    public void foo(long p1) {} \n" +
			"    public void testEc() { foo((short)1); } \n" +
			"}	\n",
		},
		"SUCCESS");
}

/**
 * Test fix for bug 58069.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=58069">58069</a>
 */
public void test076() {
	this.docSupport = true;
	runNegativeTest(
		new String[] {
			"IX.java",
			"interface IX {\n" +
				"	public static class Problem extends Exception {}\n" +
				"}\n",
			"X.java",
			"public abstract class X {\n" +
				"	public static class Problem extends Exception {}\n" +
				"	public abstract static class InnerClass implements IX {\n" +
				"		/**\n" +
				"		 * @throws Problem \n" +
				"		 */\n" +
				"		public void foo() throws IllegalArgumentException {\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"\n"
		},
		"----------\n" +
		"1. WARNING in IX.java (at line 2)\n" +
		"	public static class Problem extends Exception {}\n" +
		"	                    ^^^^^^^\n" +
		"The serializable class Problem does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public static class Problem extends Exception {}\n" +
		"	                    ^^^^^^^\n" +
		"The serializable class Problem does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	* @throws Problem \n" +
		"	          ^^^^^^^\n" +
		"Javadoc: Exception Problem is not declared\n" +
		"----------\n"	);
}
/**
 * Test fix bug 58069 for method.
 * Note that problem is not flagged in doc comments as it is only raised while verifying
 * implicit method and javadoc resolution does not use it.
 */
public void test077() {
	this.docSupport = true;
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		new Test().foo(); \n"+
			"	} \n"+
			"	String bar() { \n"+
			"		return \"FAILED\";	\n" +
			"	} \n"+
			"	void foo(){ \n"+
			"		/** @see #bar() */\n" +
			"		class Y extends Secondary { \n"+
			"			/** @see #bar() */\n" +
			"			String z = bar();	\n" +
			"		}; \n"+
			"		System.out.println(new Y().z);	\n" +
			"	} \n"+
			"} \n" +
			"class Secondary { \n" +
			"	String bar(){ return \"FAILED\"; } \n" +
			"} \n"
		}
	);
}
/**
 * Test fix bug 58069 for field.
 * Note that problem is not flagged in doc comments as it is only raised while verifying
 * Name or Qualified name references and javadoc reference is a field reference.
 */
public void test078() {
	this.docSupport = true;
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		new Test().foo(); \n"+
			"	} \n"+
			"	String bar = \"FAILED\";"+
			"	void foo(){ \n"+
			"		/** @see #bar */\n" +
			"		class Y extends Secondary { \n"+
			"			/** @see #bar */\n" +
			"			String z = bar; \n"+
			"		}; \n"+
			"		System.out.println(new Y().z);	\n" +
			"	} \n"+
			"} \n" +
			"class Secondary { \n" +
			"	String bar = \"FAILED\"; \n" +
			"} \n"
		}
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47227
 */
public void test079() {
	this.runNegativeTest(
		new String[] {
			"Hello.java",
			"void ___eval() {\n" +
			"	new Runnable() {\n" +
			"		int ___run() throws Throwable {\n" +
			"			return blah;\n" +
			"		}\n" +
			"		private String blarg;\n" +
			"		public void run() {\n" +
			"		}\n" +
			"	};\n" +
			"}\n" +
			"public class Hello {\n" +
			"	private static int x;\n" +
			"	private String blah;\n" +
			"	public static void main(String[] args) {\n" +
			"	}\n" +
			"	public void hello() {\n" +
			"	}\n" +
			"	public boolean blah() {\n" +
			"		return false;\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Hello.java (at line 1)\n" +
		"	void ___eval() {\n" +
		"	^^^^^^^^^^^^^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n" +
		"2. ERROR in Hello.java (at line 2)\n" +
		"	new Runnable() {\n" +
		"		int ___run() throws Throwable {\n" +
		"			return blah;\n" +
		"		}\n" +
		"		private String blarg;\n" +
		"		public void run() {\n" +
		"		}\n" +
		"	};\n" +
		"}\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=67643
 * from 1.5 source level on most specific common super type is allowed
 */
public void test080() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"public class X {\n" +
			"    private static class C1 extends ArrayList {\n" +
			"    }\n" +
			"    private static class C2 extends ArrayList {\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"		ArrayList list = args == null ? new C1(): new C2();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	private static class C1 extends ArrayList {\n" +
		"	                     ^^\n" +
		"The serializable class C1 does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	private static class C2 extends ArrayList {\n" +
		"	                     ^^\n" +
		"The serializable class C2 does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	ArrayList list = args == null ? new C1(): new C2();\n" +
		"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Incompatible conditional operand types X.C1 and X.C2\n" +
		"----------\n");
}
public void test081() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public X foo() { return this; } \n" +
			"}\n" +
			"class Y extends X {\n" +
			"    public Y foo() { return this; } \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	public Y foo() { return this; } \n" +
		"	       ^\n" +
		"The return type is incompatible with X.foo()\n" +
		"----------\n");
}
// covariance
public void test082() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X1();\n" +
			"		System.out.println(x.foo());\n" +
			"	}\n" +
			"	Object foo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class X1 extends X {\n" +
			"	String foo() {\n" +
			"		return \"SUCCESS\";\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	String foo() {\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with X.foo()\n" +
		"----------\n");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=66533
 */
public void test084() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		Object enum = null;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	Object enum = null;\n" +
		"	       ^^^^\n" +
		"\'enum\' should not be used as an identifier, since it is a reserved keyword from source level 1.5 on\n" +
		"----------\n");
}
/**
 * Test unused import with static
 */
public void test085() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"import static j.l.S.*;\n" +
				"import static j.l.S.in;\n" +
				"\n" +
				"public class A {\n" +
				"\n" +
				"}\n",
			"j/l/S.java",
			"package j.l;\n" +
				"public class S {\n" +
				"	public static int in;\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in A.java (at line 1)\n" +
			"	import static j.l.S.*;\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, static imports are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 2)\n" +
			"	import static j.l.S.in;\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, static imports are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"3. ERROR in A.java (at line 2)\n" +
			"	import static j.l.S.in;\n" +
			"	              ^^^^^^^^\n" +
			"The import j.l.S.in cannot be resolved\n" +
			"----------\n"
		);
}
/**
 * Test invalid static import syntax
 */
public void test086() {
	this.runNegativeTest(
		new String[] {
			"p/S.java",
			"package p;\n" +
				"public class S {\n" +
				"    public final static String full = \"FULL\";\n" +
				"    public final static String success = \"SUCCESS\";\n" +
				"}\n",
			"X.java",
			"import static p.S;\n" +
				"public class X {\n" +
				"	public static void main ( String[] args) {\n" +
				"		\n" +
				"      System.out.print(full+\" \"+p.S.success);\n" +
				"   }\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	import static p.S;\n" +
			"	^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, static imports are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	System.out.print(full+\" \"+p.S.success);\n" +
			"	                 ^^^^\n" +
			"full cannot be resolved to a variable\n" +
			"----------\n"
		);
}
public void test087() {
	this.runNegativeTest(
		new String[] {
			"p/S.java",
			"public class S {\n" +
				"    public final static String full = \"FULL\";\n" +
				"    public final static String success = \"SUCCESS\";\n" +
				"}\n",
			"X.java",
			"import static S;\n" +
				"public class X {\n" +
				"	public static void main ( String[] args) {\n" +
				"		\n" +
				"      System.out.print(full+\" \"+S.success);\n" +
				"   }\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	import static S;\n" +
			"	^^^^^^^^^^^^^^^^\n" +
			"Syntax error, static imports are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	import static S;\n" +
			"	              ^\n" +
			"The import S cannot be resolved\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	System.out.print(full+\" \"+S.success);\n" +
			"	                 ^^^^\n" +
			"full cannot be resolved to a variable\n" +
			"----------\n"
		);
}
public void test088() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"import java.util.Date;\n" +
			"import java.lang.reflect.*;\n" +
			"public class X extends Date implements Runnable{\n" +
			" \n" +
			" Integer w = Integer.valueOf(90);\n" +
			" protected double x = 91.1;\n" +
			" public long y = 92;\n" +
			" static public Boolean z = Boolean.valueOf(true); \n" +
			" public class X_inner {\n" +
			"  public X_inner() {\n" +
			"   this.super();\n" +
			"   System.out.println(\"....\");\n" +
			"  }\n" +
			" }\n" +
			" X_inner a = new X_inner();\n" +
			" public interface X_interface {\n" +
			"   public void f(); \n" +
			" }\n" +
			" static {\n" +
			"  System.out.println(\"Static initializer\");\n" +
			" }\n" +
			" public X() { } \n" +
			" public X(int a1,int b1) { } \n" +
			" private void a() { System.out.println(\"A\");} \n" +
			" protected void b() { System.out.println(\"B\");} \n" +
			" public void c() { System.out.println(\"C\");} \n" +
			" static public int d() {System.out.println(\"Static D\");return -1;} \n" +
			" public static void main(String args[]) {\n" +
			"  X  b = new X();\n" +
			"  Class c = b.getClass();\n" +
			"  Class _getClasses [] = X.class.getClasses(); \n" +
			"//  System.out.println(_getClasses[0].toString());\n" +
			"//  System.out.println(_getClasses[1].toString());\n" +
			"  if (_getClasses.length == 0) {System.out.println(\"FAILED\");};\n" +
			"  Constructor _getConstructors[] = c.getConstructors(); \n" +
			"  try {\n" +
			"   Field _getField = c.getField(\"y\");\n" +
			"   Method _getMethod = c.getMethod(\"d\",null);\n" +
			" \n" +
			"   Boolean b_z = X.z; \n" +
			"  }\n" +
			"  catch (NoSuchFieldException e) { System.out.println(\"NoSuchFieldException\");}\n" +
			"  catch (NoSuchMethodException e) { System.out.println(\"NoSuchMethodException\");};\n" +
			" } \n" +
			" public void run() {System.out.println(\"RUN\");} \n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in p\\X.java (at line 4)\n" +
		"	public class X extends Date implements Runnable{\n" +
		"	             ^\n" +
		"The serializable class X does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. ERROR in p\\X.java (at line 12)\n" +
		"	this.super();\n" +
		"	^^^^\n" +
		"Illegal enclosing instance specification for type Object\n" +
		"----------\n" +
		"3. WARNING in p\\X.java (at line 25)\n" +
		"	private void a() { System.out.println(\"A\");} \n" +
		"	             ^^^\n" +
		"The method a() from the type X is never used locally\n" +
		"----------\n");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78089
 */
public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"    @interface I1 {}\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"    public static void main(String argv[])   {\n" +
			"    	System.out.print(\"SUCCESS\");\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\r\n" +
		"	@interface I1 {}\r\n" +
		"	           ^^\n" +
		"Syntax error, annotation declarations are only available if source level is 1.5 or greater\n" +
		"----------\n");
}
//78104
public void test090() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	void foo(int[] ints, Object o) {\n" +
			"		ints = ints.clone();\n" +
			"		ints = (int[])ints.clone();\n" +
			"		X x = this.clone();\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	ints = ints.clone();\n" +
		"	       ^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Object to int[]\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	X x = this.clone();\n" +
		"	      ^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Object to X\n" +
		"----------\n"
	);
}
//78104 - variation
public void test091() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		args = args.clone();\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\r\n" +
		"	args = args.clone();\r\n" +
		"	       ^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Object to String[]\n" +
		"----------\n"
	);
}
// check autoboxing only enabled in 5.0 source mode
public void test092() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(Boolean b) {\n" +
			"		if (b) { \n" +
			"			int i = 0;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	if (b) { \n" +
		"	    ^\n" +
		"Type mismatch: cannot convert from Boolean to boolean\n" +
		"----------\n"
	);
}
public void test093() {
	this.runNegativeTest(
		new String[] {
			"p/X_1.java",
			"package p;\n" +
			"/*   dena JTest Suite, Version 2.2, September 1997\n" +
			" *   Copyright (c) 1995-1997 Modena Software (I) Pvt. Ltd., All Rights Reserved\n" +
			" */\n" +
			"/*  Section    :  Inner classes \n" +
			" *  FileName   :  ciner026.java\n" +
			" *  Purpose    :  Positive test for Inner classes\n" +
			" *  \n" +
			" *  An anonymous class can have initializers but cannot have a constructor.\n" +
			" *  The argument list of the associated new expression is implicitely \n" +
			" *  passed to the constructor of the super class. \n" +
			" *\n" +
			" */\n" +
			" \n" +
			" class X_1 {\n" +
			"  static int xx = 100;\n" +
			"  //inner class Y  \n" +
			"  static class Y {  \n" +
			"   public int j = 0;\n" +
			"   Y(int x){ j = x; }\n" +
			"   }  \n" +
			" public void call_inner()\n" +
			" {\n" +
			"   int i = test_anonymous().j;\n" +
			" }     \n" +
			" public static void main(String argv[])\n" +
			" {\n" +
			"   X_1 ox = new X_1();\n" +
			"   ox.call_inner(); \n" +
			" }  \n" +
			"public void newMethod ( ) {\n" +
			"  Float f1 = null;\n" +
			"  f1=(f1==0.0)?1.0:f1;\n" +
			"}\n" +
			"   static Y test_anonymous()\n" +
			"   { \n" +
			"    //anonymous implementation of class Y\n" +
			"    return new Y(xx) //xx should be implicitely passed to Y()\n" +
			"    {\n" +
			"    };    \n" +
			"   \n" +
			"   } //end test_anonymous      \n" +
			"} ",
		},
		"----------\n" +
		"1. ERROR in p\\X_1.java (at line 33)\n" +
		"	f1=(f1==0.0)?1.0:f1;\n" +
		"	   ^^^^^^^^^\n" +
		"Incompatible operand types Float and double\n" +
		"----------\n"
	);
}
/*
 * Test unused import warning in presence of syntax errors
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21022
 */
public void test094(){

	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;	\n" +
			"public class X {	\n" +
			"	void foo(){\n" +
			"		()\n" +
			"		IOException e;\n" +
			"	} \n" +
			"}		\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	()\n" +
		"	^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84743
public void test095(){

	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"   int foo();\n" +
			"}\n" +
			"interface J {\n" +
			"   String foo();\n" +
			"}\n" +
			" \n" +
			"public class X implements I {\n" +
			"   public int foo() {\n" +
			" 	return 0;\n" +
			"   }\n" +
			"   public static void main(String[] args) {\n" +
			"         I i = new X();\n" +
			"         try {\n" +
			"	        J j = (J) i;\n" +
			"         } catch(ClassCastException e) {\n" +
			"	        System.out.println(\"SUCCESS\");\n" +
			"         }\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	J j = (J) i;\n" +
		"	      ^^^^^\n" +
		"Cannot cast from I to J\n" +
		"----------\n");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47074
 */
public void test096() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"    interface A {\n" +
			"       void doSomething();\n" +
			"    }\n" +
			"\n" +
			"    interface B {\n" +
			"       int doSomething();\n" +
			"    }\n" +
			"\n" +
			"    interface C extends B {\n" +
			"    }\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"        \n" +
			"        A a = null;\n" +
			"        C c = (C)a; //COMPILER ERROR\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 17)\n" +
		"	C c = (C)a; //COMPILER ERROR\n" +
		"	      ^^^^\n" +
		"Cannot cast from X.A to X.C\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79396
public void test097() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"    public static void main(String argv[]) {\n" +
			"    	int cst = X1.CST;\n" +
			"        X2.Root.foo();\n" +
			"    }\n" +
			"    static void foo() {}\n" +
			"}\n" +
			"\n" +
			"class X1 {\n" +
			"    static {\n" +
			"		System.out.print(\"[X1]\");\n" +
			"    }\n" +
			"    public static final int CST = 12;\n" +
			"    static X Root = null;\n" +
			"}\n" +
			"class X2 {\n" +
			"    static {\n" +
			"		System.out.print(\"[X2]\");\n" +
			"    }\n" +
			"    public final int CST = 12;\n" +
			"    static X Root = null;\n" +
			"}\n"
		},
		"[X2]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=78906
public void test098() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		System.out.print(\"foo\");\n" +
			"	}\n" +
			"	class Y {\n" +
			"		String this$0;\n" +
			"		String this$0$;\n" +
			"		void print() { \n" +
			"			foo();\n" +
			"			System.out.println(this$0+this$0$);\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X.Y y = new X().new Y();\n" +
			"		y.this$0 = \"hello\";\n" +
			"		y.this$0$ = \"world\";\n" +
			"		y.print();\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	String this$0;\n" +
		"	       ^^^^^^\n" +
		"Duplicate field X.Y.this$0\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=77349
public void test099() {
	this.runNegativeTest(
		new String[] {
			"I.java",
			"public interface I extends Cloneable {\n" +
			"	class Inner {\n" +
			"		Object bar(I i) throws CloneNotSupportedException { return i.clone(); }\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in I.java (at line 3)\n" +
		"	Object bar(I i) throws CloneNotSupportedException { return i.clone(); }\n" +
		"	                                                             ^^^^^\n" +
		"The method clone() is undefined for the type I\n" +
		"----------\n"
	);
}

public void test100() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    int \\ud800\\udc05\\ud800\\udc04\\ud800\\udc03\\ud800\\udc02\\ud800\\udc01\\ud800\\udc00;\n" +
			"    void foo() {\n" +
			"        int \\ud800\\udc05\\ud800\\udc04\\ud800\\udc03\\ud800\\udc02\\ud800\\udc01\\ud800\\udc00;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int \\ud800\\udc05\\ud800\\udc04\\ud800\\udc03\\ud800\\udc02\\ud800\\udc01\\ud800\\udc00;\n" +
		"	    ^^^^^^\n" +
		"Invalid unicode\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	int \\ud800\\udc05\\ud800\\udc04\\ud800\\udc03\\ud800\\udc02\\ud800\\udc01\\ud800\\udc00;\n" +
		"	    ^^^^^^\n" +
		"Invalid unicode\n" +
		"----------\n"
	);
}

public void test101() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	Character c0 = \'a\';\n" +
			"	public static void main(String argv[]) {\n" +
			"		Character c1;\n" +
			"		c1 = \'b\';\n" +
			"\n" +
			"		Character c2 = \'c\';\n" +
			"		Character[] c3 = { \'d\' };\n" +
			"	\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Character c0 = \'a\';\n" +
		"	               ^^^\n" +
		"Type mismatch: cannot convert from char to Character\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	c1 = \'b\';\n" +
		"	     ^^^\n" +
		"Type mismatch: cannot convert from char to Character\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	Character c2 = \'c\';\n" +
		"	               ^^^\n" +
		"Type mismatch: cannot convert from char to Character\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 8)\n" +
		"	Character[] c3 = { \'d\' };\n" +
		"	                   ^^^\n" +
		"Type mismatch: cannot convert from char to Character\n" +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108856
public void test102() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] s) {\n" +
			"		new Object() {\n" +
			"			{\n" +
			"				new Object() {\n" +
			"					{\n" +
			"						System.out.println(this.getClass().getName());\n" +
			"					}\n" +
			"				};\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}\n"
		},
		"X$2");
}
public void test103() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"		System.out.print(X.class);\n" +
			"    }\n" +
			"}\n",
		},
		"class X");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"// Compiled from X.java (version 1.4 : 48.0, super bit)\n" +
		"public class X {\n" +
		"  \n" +
		"  // Field descriptor #6 Ljava/lang/Class;\n" +
		"  static synthetic java.lang.Class class$0;\n" +
		"  \n" +
		"  // Method descriptor #9 ()V\n" +
		"  // Stack: 1, Locals: 1\n" +
		"  public X();\n" +
		"    0  aload_0 [this]\n" +
		"    1  invokespecial java.lang.Object() [11]\n" +
		"    4  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #18 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
		"     3  getstatic X.class$0 : java.lang.Class [25]\n" +
		"     6  dup\n" +
		"     7  ifnonnull 35\n" +
		"    10  pop\n" +
		"    11  ldc <String \"X\"> [27]\n" +
		"    13  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [28]\n" +
		"    16  dup\n" +
		"    17  putstatic X.class$0 : java.lang.Class [25]\n" +
		"    20  goto 35\n" +
		"    23  new java.lang.NoClassDefFoundError [34]\n" +
		"    26  dup_x1\n" +
		"    27  swap\n" +
		"    28  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [36]\n" +
		"    31  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [42]\n" +
		"    34  athrow\n" +
		"    35  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [45]\n" +
		"    38  return\n" +
		"      Exception Table:\n" +
		"        [pc: 11, pc: 16] -> 23 when : java.lang.ClassNotFoundException\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 38, line: 4]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 39] local: args index: 0 type: java.lang.String[]\n" +
		"}";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=125570
public void test104() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] s) {\n" +
			"		new Object() {\n" +
			"			{\n" +
			"				new Object() {\n" +
			"					{\n" +
			"						class Y {\n" +
			"							{\n" +
			"								System.out.print(this.getClass());\n" +
			"								System.out.print(\' \');\n" +
			"								System.out.print(this.getClass().getName());\n" +
			"							}\n" +
			"						}\n" +
			"						;\n" +
			"						new Y();\n" +
			"					}\n" +
			"				};\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}"
		},
		"class X$1$Y X$1$Y");
}

// enclosing instance - note that the behavior is different in 1.5
public void test105() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    static class Y { }\n" +
			"    static class Z1 {\n" +
			"        Runnable m;\n" +
			"        Z1(Runnable p) {\n" +
			"            this.m = p;\n" +
			"        }\n" +
			"    }\n" +
			"    class Z2 extends Z1 {\n" +
			"        Z2(final Y p) {\n" +
			"            super(new Runnable() {\n" +
			"                public void run() {\n" +
			"                    foo(p);\n" +
			"                }\n" +
			"            });\n" +
			"        }\n" +
			"    }\n" +
			"    void foo(Y p) { }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	foo(p);\n" +
		"	^^^^^^\n" +
		"No enclosing instance of the type X is accessible in scope\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
public void test106() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"import java.util.zip.*;\n" +
			"public class X {\n" +
			"	void x() throws ZipException {\n" +
			"		IandJ ij= new K();\n" +
			"		ij.m();\n" +
			"	}\n" +
			"	void y() throws ZipException {\n" +
			"		K k= new K();\n" +
			"		k.m();\n" +
			"	}\n" +
			"}\n" +
			"interface I { void m() throws IOException; }\n" +
			"interface J { void m() throws ZipException; }\n" +
			"interface IandJ extends I, J {}\n" +
			"class K implements IandJ { public void m() throws ZipException { } }"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
public void test107() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		C c = new D();\n" +
			"		c.xyz();\n" +
			"	}\n" +
			"}\n" +
			"class AException extends Exception { }\n" +
			"class BException extends Exception { }\n" +
			"interface A { void xyz() throws AException; }\n" +
			"interface B { void xyz() throws BException; }\n" +
			"interface C extends A, B { }\n" +
			"class D implements C {\n" +
			"	public void xyz() { System.out.println(1); }\n" +
			"}"
		},
		"1");
}
}
