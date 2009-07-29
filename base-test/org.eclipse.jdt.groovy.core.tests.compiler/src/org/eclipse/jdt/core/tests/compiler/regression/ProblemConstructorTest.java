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

import junit.framework.Test;

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
}
