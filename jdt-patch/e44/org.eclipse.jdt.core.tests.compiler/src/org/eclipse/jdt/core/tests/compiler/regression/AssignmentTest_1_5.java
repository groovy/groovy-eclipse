/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class AssignmentTest_1_5 extends AbstractRegressionTest {

public AssignmentTest_1_5(String name) {
	super(name);
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.ERROR);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 15 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test1() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Integer value = 4711;\n" + 
			"		double test = 47d;\n" + 
			"		value += test;\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	value += test;\n" + 
		"	^^^^^^^^^^^^^\n" + 
		"The operator += is undefined for the argument type(s) Integer, double\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Integer value = 4711;\n" + 
			"		float test = 47f;\n" + 
			"		value += test;\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	value += test;\n" + 
		"	^^^^^^^^^^^^^\n" + 
		"The operator += is undefined for the argument type(s) Integer, float\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Integer value = 4711;\n" + 
			"		byte test = 47;\n" + 
			"		value += test;\n" + 
			"		System.out.println(value);\n" +
			"	}\n" + 
			"}",
		},
		"4758");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test4() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Integer value = 4711;\n" + 
			"		char test = 'a';\n" + 
			"		value += test;\n" + 
			"		System.out.println(value);\n" +
			"	}\n" + 
			"}",
		},
		"4808");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test5() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Integer value = 4711;\n" + 
			"		long test = 100L;\n" + 
			"		value += test;\n" + 
			"		System.out.println(value);\n" +
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	value += test;\n" + 
		"	^^^^^^^^^^^^^\n" + 
		"The operator += is undefined for the argument type(s) Integer, long\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test6() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Integer value = 4711;\n" + 
			"		boolean test = true;\n" + 
			"		value += test;\n" + 
			"		System.out.println(value);\n" +
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	value += test;\n" + 
		"	^^^^^^^^^^^^^\n" + 
		"The operator += is undefined for the argument type(s) int, boolean\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test7() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Integer value = 4711;\n" + 
			"		short test = 32767;\n" + 
			"		value += test;\n" + 
			"		System.out.println(value);\n" +
			"	}\n" + 
			"}",
		},
		"37478");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test8() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		int x = -8;\n" + 
			"		x += 7.8f;\n" + 
			"		System.out.println(x == 0 ? \"SUCCESS\" : \"FAILED\");\n" + 
			"	}\n" + 
			"}",
		},
		"SUCCESS");
}
public void test9() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class XSuper<T> {\n" +
			"	T value;\n" +
			"}\n" +
			"public class X extends XSuper<String>{\n" +
			"	public void a() {\n" +
			"		this.value += 1;\n" +
			"		this.value = this.value + 1;\n" +
			"		System.out.println(this.value);\n" +
			"	}\n" +
			"\n" +
			"	public static void main(final String[] args) {\n" +
			"		X x = new X();\n" +
			"		x.value = \"[\";\n" +
			"		x.a();\n" +
			"	}\n" +
			"}\n",
		},
		"[11");
}
public void test10() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Byte b = new Byte((byte)1);\n" +
			"		int i = b++;\n" +
			"		System.out.print(i);\n" +
			"		System.out.print(b);\n" +
			"	}\n" +
			"}\n",
		},
		"12"
	);
}
public void test11() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String args[]) { \n" + 
			"		Long _long = new Long(44);\n" + 
			"		byte b = (byte) 1;\n" + 
			"		char c = (char) 2;\n" + 
			"		short s = (short) 32767;\n" + 
			"		int i = 10;\n" + 
			"		long l = 80L;\n" + 
			"		_long >>>= b;\n" + 
			"		_long <<= c;\n" + 
			"		_long >>= s;\n" + 
			"		_long >>>= i;\n" + 
			"		_long = 77l;\n" + 
			"		_long <<= l;\n" + 
			"		System.out.println(_long);\n" + 
			"	}\n" + 
			"}",
		},
		"5046272"
	);
}
public void test12() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Byte b = new Byte((byte)1);\n" +
			"		int i = ++b;\n" +
			"		System.out.print(i);\n" +
			"		System.out.print(b);\n" +
			"	}\n" +
			"}\n",
		},
		"22"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test13() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Integer value = 4711;\n" + 
			"		long test = 47L;\n" + 
			"		value &= test;\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	value &= test;\n" + 
		"	^^^^^^^^^^^^^\n" + 
		"The operator &= is undefined for the argument type(s) Integer, long\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test14() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Integer value = 4711;\n" + 
			"		long test = 47L;\n" + 
			"		value |= test;\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	value |= test;\n" + 
		"	^^^^^^^^^^^^^\n" + 
		"The operator |= is undefined for the argument type(s) Integer, long\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test15() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Byte value = (byte) 1;\n" + 
			"		value++;\n" + 
			"		System.out.println(value);\n" + 
			"	}\n" + 
			"}",
		},
		"2");
}
public static Class testClass() {
	return AssignmentTest_1_5.class;
}
}
