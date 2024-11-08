/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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

@SuppressWarnings({ "unchecked", "rawtypes" })
public class FieldAccessTest extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 22 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}

public FieldAccessTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.ERROR);
	return options;
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test001() {
	this.runConformTest(
		new String[] {
			"foo/BaseFoo.java",
			"package foo;\n" +
			"class BaseFoo {\n" +
			" public static final int VAL = 0;\n" +
			"}",
			"foo/NextFoo.java",
			"package foo;\n" +
			"public class NextFoo extends BaseFoo {\n" +
			"}",
			"bar/Bar.java",
			"package bar;\n" +
			"public class Bar {\n" +
			" int v = foo.NextFoo.VAL;\n" +
			"}"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test002() {
	this.runNegativeTest(
		new String[] {
			"foo/BaseFoo.java",
			"package foo;\n" +
			"public class BaseFoo {\n" +
			" public static final int VAL = 0;\n" +
			"}",
			"foo/NextFoo.java",
			"package foo;\n" +
			"public class NextFoo extends BaseFoo {\n" +
			"}",
			"bar/Bar.java",
			"package bar;\n" +
			"public class Bar {\n" +
			" int v = foo.NextFoo.VAL;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in bar\\Bar.java (at line 3)\n" +
		"	int v = foo.NextFoo.VAL;\n" +
		"	                    ^^^\n" +
		"The static field BaseFoo.VAL should be accessed directly\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"foo/BaseFoo.java",
			"package foo;\n" +
			"class BaseFoo {\n" +
			" public static final int VAL = 0;\n" +
			"}",
			"foo/NextFoo.java",
			"package foo;\n" +
			"public class NextFoo extends BaseFoo {\n" +
			"}",
			"bar/Bar.java",
			"package bar;\n" +
			"import foo.NextFoo;\n" +
			"public class Bar {\n" +
			"	NextFoo[] tab = new NextFoo[] { new NextFoo() };\n" +
			"	int v = tab[0].VAL;\n" +
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test004() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	this.runNegativeTest(
		true,
		new String[] {
			"foo/BaseFoo.java",
			"package foo;\n" +
			"public class BaseFoo {\n" +
			" public static final int VAL = 0;\n" +
			"}",
			"foo/NextFoo.java",
			"package foo;\n" +
			"public class NextFoo extends BaseFoo {\n" +
			"}",
			"bar/Bar.java",
			"package bar;\n" +
			"import foo.NextFoo;\n" +
			"public class Bar {\n" +
			"	NextFoo[] tab = new NextFoo[] { new NextFoo() };\n" +
			"	int v = tab[0].VAL;\n" +
			"}"
		},
		null,
		options,
		"----------\n" +
		"1. ERROR in bar\\Bar.java (at line 5)\n" +
		"	int v = tab[0].VAL;\n" +
		"	               ^^^\n" +
		"The static field BaseFoo.VAL should be accessed directly\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=142234
public void test005() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private String memberVariable;\n" +
			"	public String getMemberVariable() {\n" +
			"		return (memberVariable);\n" +
			"	}\n" +
			"}"
		},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return (memberVariable);\n" +
		"	        ^^^^^^^^^^^^^^\n" +
		"Unqualified access to the field X.memberVariable \n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=142234
public void test006() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private String memberVariable;\n" +
			"	public String getMemberVariable() {\n" +
			"		return \\u0028memberVariable\\u0029;\n" +
			"	}\n" +
			"}"
		},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return \\u0028memberVariable\\u0029;\n" +
		"	             ^^^^^^^^^^^^^^\n" +
		"Unqualified access to the field X.memberVariable \n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test007() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private void foo() {\n" +
			"		new A().a2.a.test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	private int test;\n" +
			"	A a2;\n" +
			"	A a = new A();\n" +
			"}\n" +
			"\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	new A().a2.a.test = 8;\n" +
		"	             ^^^^\n" +
		"The field A.test is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test008() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private void foo() {\n" +
			"		new A().a2.a.test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	int test;\n" +
			"	private A a2;\n" +
			"	A a = new A();\n" +
			"}\n" +
			"\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	new A().a2.a.test = 8;\n" +
		"	        ^^\n" +
		"The field A.a2 is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test009() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private void foo() {\n" +
			"		new A().a2.a.test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	int test;\n" +
			"	A a2;\n" +
			"	private A a = new A();\n" +
			"}\n" +
			"\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	new A().a2.a.test = 8;\n" +
		"	           ^\n" +
		"The field A.a is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test010() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private void foo() {\n" +
			"		A.a2.a.test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	static int test;\n" +
			"	static A a2;\n" +
			"	static private A a = new A();\n" +
			"}\n" +
			"\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	A.a2.a.test = 8;\n" +
		"	     ^\n" +
		"The field A.a is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test011() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private void foo() {\n" +
			"		A.a2.a.test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	static int test;\n" +
			"	static private A a2;\n" +
			"	static A a = new A();\n" +
			"}\n" +
			"\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	A.a2.a.test = 8;\n" +
		"	  ^^\n" +
		"The field A.a2 is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test012() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private void foo() {\n" +
			"		A.a2.a.test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	private static int test;\n" +
			"	static A a2;\n" +
			"	A a = new A();\n" +
			"}\n" +
			"\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	A.a2.a.test = 8;\n" +
		"	       ^^^^\n" +
		"The field A.test is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test013() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X extends A {\n" +
			"	private void foo() {\n" +
			"		test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	private int test;\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	test = 8;\n" +
		"	^^^^\n" +
		"The field A.test is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X extends A {\n" +
			"	private void foo() {\n" +
			"		this.test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	private int test;\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	this.test = 8;\n" +
		"	     ^^^^\n" +
		"The field A.test is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test015() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X extends A {\n" +
			"	private void foo() {\n" +
			"		MyA.test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	private static A MyA;\n" +
			"	static int test;\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	MyA.test = 8;\n" +
		"	^^^\n" +
		"The field A.MyA is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X extends A {\n" +
			"	private void foo() {\n" +
			"		MyA2.MyA.test = 8;\n" +
			"	}\n" +
			"}",
			"A.java",
			"class A {\n" +
			"	private static A MyA;\n" +
			"	static A MyA2;\n" +
			"	static int test;\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	MyA2.MyA.test = 8;\n" +
		"	     ^^^\n" +
		"The field A.MyA is not visible\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222534
public void test017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);

	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		Zork z;\n" +
			"       private static class Inner1 {\n" +
			"                private int field;\n" +
			"       }\n" +
			"       private static class Inner2 extends Inner1 {\n" +
			"                private int field;\n" +
			"                public void bar() {System.out.println(field);}\n" +
			"       }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222534 - variation
public void test018() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);

	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		Zork z;\n" +
			"		public static int field;\n" +
			"       private static class Inner1 {\n" +
			"                private int field;\n" +
			"       }\n" +
			"       private static class Inner2 extends Inner1 {\n" +
			"                private int field;\n" +
			"                public void bar() {System.out.println(field);}\n" +
			"       }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	private int field;\n" +
		"	            ^^^^^\n" +
		"The field X.Inner1.field is hiding a field from type X\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 8)\n" +
		"	private int field;\n" +
		"	            ^^^^^\n" +
		"The field X.Inner2.field is hiding a field from type X\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222534 - variation
public void test019() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);

	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		Zork z;\n" +
			"       private static class Inner1 {\n" +
			"                private int field;\n" +
			"       }\n" +
			"       private static class Inner2 extends Inner1 {\n" +
			"                public void bar(int field) {System.out.println(field);}\n" +
			"       }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222534 - variation
public void test020() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);

	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		Zork z;\n" +
			"		public static int field;\n" +
			"       private static class Inner1 {\n" +
			"                private int field;\n" +
			"       }\n" +
			"       private static class Inner2 extends Inner1 {\n" +
			"                public void bar(int field) {System.out.println(field);}\n" +
			"       }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	private int field;\n" +
		"	            ^^^^^\n" +
		"The field X.Inner1.field is hiding a field from type X\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 8)\n" +
		"	public void bar(int field) {System.out.println(field);}\n" +
		"	                    ^^^^^\n" +
		"The parameter field is hiding a field from type X\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=303830
public void test021() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"	public void bar() {\n" +
			"		ArrayList myList = new ArrayList();\n" +
			"		int len = myList.length;\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	int len = myList.length;\n" +
		"	                 ^^^^^^\n" +
		"length cannot be resolved or is not a field\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=303830
public void test022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static int NEW_FIELD;\n" +
			"}",
			"Y.java",
			"public class Y {\n" +
			"	void foo() {\n" +
			"		int i = X.OLD_FIELD;\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		int j = X.OLD_FIELD;\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 3)\n" +
		"	int i = X.OLD_FIELD;\n" +
		"	          ^^^^^^^^^\n" +
		"OLD_FIELD cannot be resolved or is not a field\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 6)\n" +
		"	int j = X.OLD_FIELD;\n" +
		"	          ^^^^^^^^^\n" +
		"OLD_FIELD cannot be resolved or is not a field\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318171
public void test023() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" +
			"public abstract class A {\n" +
			"    protected int field;\n" +
			"}\n",
			"p2/B.java",
			"package p2;\n" +
			"import p1.A;\n" +
			"public abstract class B extends A {\n" +
			"    protected int field;\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in p2\\B.java (at line 4)\n" +
		"	protected int field;\n" +
		"	              ^^^^^\n" +
		"The field B.field is hiding a field from type A\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318171
public void test024() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" +
			"public abstract class A extends Super {\n" +
			"}\n",
			"p1/Super.java",
			"package p1;\n" +
			"public abstract class Super extends SuperSuper {\n" +
			"}\n",
			"p1/SuperSuper.java",
			"package p1;\n" +
			"public abstract class SuperSuper {\n" +
			"    protected int field;\n" +
			"}\n",
			"p2/B.java",
			"package p2;\n" +
			"import p1.A;\n" +
			"public abstract class B extends A {\n" +
			"    protected int field;\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in p2\\B.java (at line 4)\n" +
		"	protected int field;\n" +
		"	              ^^^^^\n" +
		"The field B.field is hiding a field from type SuperSuper\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318171
public void test025() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" +
			"public abstract class A extends Super {\n" +
			"}\n",
			"p1/Super.java",
			"package p1;\n" +
			"public abstract class Super extends SuperSuper {\n" +
			"}\n",
			"p1/SuperSuper.java",
			"package p1;\n" +
			"public abstract class SuperSuper implements Interface{\n" +
			"}\n",
			"p1/Interface.java",
			"package p1;\n" +
			"public interface Interface{\n" +
			"    int field = 123;\n" +
			"}\n",
			"p2/B.java",
			"package p2;\n" +
			"import p1.A;\n" +
			"public abstract class B extends A {\n" +
			"    protected int field;\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in p2\\B.java (at line 4)\n" +
		"	protected int field;\n" +
		"	              ^^^^^\n" +
		"The field B.field is hiding a field from type Interface\n" +
		"----------\n");
}
public void testBug361039() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // to leverage autounboxing
	runNegativeTest(
		new String[] {
			"Bug361039.java",
			"public class Bug361039 {\n" +
			"	public Bug361039(boolean b) {\n" +
			"	}\n" +
			"	private Object foo() {\n" +
			"		return new Bug361039(!((Boolean)this.f));\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Bug361039.java (at line 5)\n" +
		"	return new Bug361039(!((Boolean)this.f));\n" +
		"	                                     ^\n" +
		"f cannot be resolved or is not a field\n" +
		"----------\n");
}
public void testBug568959_001() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // lambda
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			" public void foo(Object o) {\n"+
			"   I i = () -> {\n"+
			"     while (o.eq) {\n"+
			"       // nothing\n"+
			"     }\n"+
			"   };\n"+
			" }\n"+
			"}\n"+
			"interface I { \n"+
			" public abstract void run();\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	while (o.eq) {\n" +
		"	         ^^\n" +
		"eq cannot be resolved or is not a field\n" +
		"----------\n");
}
public static Class testClass() {
	return FieldAccessTest.class;
}
}

