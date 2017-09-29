/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class SelectionParserTest18 extends AbstractSelectionTest {
static {
//		TESTS_NUMBERS = new int[] { 53 };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(SelectionParserTest18.class, F_1_8);
}

public SelectionParserTest18(String testName) {
	super(testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424110, [1.8][hovering] Hover, F3 does not work for method reference in method invocation
public void test424110() throws JavaModelException {
	String string = 
			"public class X {\n" +
			"	static F f = X::m; // [1] Works\n" +
			"	int i = fun(X::m); // [2] Does not work\n" +
			"	public static int m(int x) {\n" +
			"		return x;\n" +
			"	}\n" +
			"	private int fun(F f) {\n" +
			"		return f.foo(0);\n" +
			"	}\n" +
			"}\n" +
			"interface F {\n" +
			"	int foo(int x);\n" +
			"}\n";

	String selection = "m";

	String expectedCompletionNodeToString = "<SelectionOnReferenceExpressionName:X::m>";

	String completionIdentifier = "m";
	String expectedUnitDisplayString =
					"public class X {\n" + 
					"  static F f = <SelectionOnReferenceExpressionName:X::m>;\n" + 
					"  int i;\n" + 
					"  <clinit>() {\n" + 
					"  }\n" + 
					"  public X() {\n" + 
					"  }\n" + 
					"  public static int m(int x) {\n" + 
					"  }\n" + 
					"  private int fun(F f) {\n" + 
					"  }\n" + 
					"}\n" + 
					"interface F {\n" + 
					"  int foo(int x);\n" + 
					"}\n";
	String expectedReplacedSource = "X::m";
	String testName = "<select>";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	this.checkDietParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424110, [1.8][hovering] Hover, F3 does not work for method reference in method invocation
public void test424110a() throws JavaModelException {
	String string = 
			"public class X {\n" +
			"	int i = fun(X::m); // [2] Does not work\n" +
			"	public static int m(int x) {\n" +
			"		return x;\n" +
			"	}\n" +
			"	private int fun(F f) {\n" +
			"		return f.foo(0);\n" +
			"	}\n" +
			"}\n" +
			"interface F {\n" +
			"	int foo(int x);\n" +
			"}\n";

	String selection = "m";

	String expectedCompletionNodeToString = "<SelectionOnReferenceExpressionName:X::m>";

	String completionIdentifier = "m";
	String expectedUnitDisplayString =
					"public class X {\n" + 
					"  int i = fun(<SelectionOnReferenceExpressionName:X::m>);\n" + 
					"  public X() {\n" + 
					"  }\n" + 
					"  public static int m(int x) {\n" + 
					"  }\n" + 
					"  private int fun(F f) {\n" + 
					"  }\n" + 
					"}\n" + 
					"interface F {\n" + 
					"  int foo(int x);\n" + 
					"}\n";
	String expectedReplacedSource = "X::m";
	String testName = "<select>";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	this.checkDietParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430572, [1.8] CCE on hovering over 'super' in lambda expression
public void test430572() throws JavaModelException {
	String string = 
			"@FunctionalInterface\n" +
			"interface FI {\n" +
			"	default int getID() {\n" +
			"		return 11;\n" +
			"	}\n" +
			"	void print();\n" +
			"}\n" +
			"class T {\n" +
			"	FI f2 = () -> System.out.println(super.toString());\n" +
			"}\n";

	String selection = "super";

	String expectedCompletionNodeToString = "<SelectOnSuper:super>";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
					"@FunctionalInterface interface FI {\n" + 
					"  default int getID() {\n" + 
					"  }\n" + 
					"  void print();\n" + 
					"}\n" + 
					"class T {\n" + 
					"  FI f2 = () -> System.out.println(<SelectOnSuper:super>.toString());\n" + 
					"  T() {\n" + 
					"  }\n" + 
					"}\n";
	String expectedReplacedSource = "super";
	String testName = "<select>";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	this.checkDietParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
}
}