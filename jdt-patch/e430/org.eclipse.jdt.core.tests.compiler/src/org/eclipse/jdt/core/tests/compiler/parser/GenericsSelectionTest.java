/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

public class GenericsSelectionTest extends AbstractSelectionTest {
public GenericsSelectionTest(String testName) {
	super(testName);
}
/*
 * Selection at specific location
 */
public void test0001() {

	String str =
		"public class X {		\n" +
		"  Z<Object> z;								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Z<Object>> z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection at specific location
 */
public void test0002() {

	String str =
		"public class X {		\n" +
		"  void foo(){;								\n" +
		"    Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnType:Z<Object>> z;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection at specific location
 */
public void test0003() {

	String str =
		"public class X {		\n" +
		"  Y.Z<Object> z;								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Y.Z<Object>> z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection at specific location
 */
public void test0004() {

	String str =
		"public class X {		\n" +
		"  void foo(){;								\n" +
		"    Y.Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnType:Y.Z<Object>> z;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection at specific location
 */
public void test0005() {

	String str =
		"public class X {		\n" +
		"  Y<Object>.Z z;								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Y<Object>.Z> z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection at specific location
 */
public void test0006() {

	String str =
		"public class X {		\n" +
		"  void foo(){;								\n" +
		"    Y<Object>.Z z;								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnType:Y<Object>.Z> z;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection at specific location
 */
public void test0007() {

	String str =
		"public class X {		\n" +
		"  Y<Object>.Z<Object> z;								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Y<Object>.Z<Object>> z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection at specific location
 */
public void test0008() {

	String str =
		"public class X {		\n" +
		"  void foo(){;								\n" +
		"    Y<Object>.Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnType:Y<Object>.Z<Object>> z;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection of simple name
 */
public void test0009() {

	String str =
		"public class X {		\n" +
		"  Z<Object> z;								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Z<Object>> z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection of simple name
 */
public void test0010() {

	String str =
		"public class X {		\n" +
		"  void foo(){;								\n" +
		"    Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnType:Z<Object>> z;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection of qualified name
 */
public void test0011() {

	String str =
		"public class X {		\n" +
		"  Y.Z<Object> z;								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Y.Z<Object>> z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection of qualified name
 */
public void test0012() {

	String str =
		"public class X {		\n" +
		"  void foo(){;								\n" +
		"    Y.Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnType:Y.Z<Object>> z;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection of qualified name
 */
public void test0013() {

	String str =
		"public class X {		\n" +
		"  Y<Object>.Z z;								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Y<Object>.Z> z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection of qualified name
 */
public void test0014() {

	String str =
		"public class X {		\n" +
		"  void foo(){;								\n" +
		"    Y<Object>.Z z;								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnType:Y<Object>.Z> z;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection of qualified name
 */
public void test0015() {

	String str =
		"public class X {		\n" +
		"  Y<Object>.Z<Object> z;								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Y<Object>.Z<Object>> z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection of qualified name
 */
public void test0016() {

	String str =
		"public class X {		\n" +
		"  void foo(){;								\n" +
		"    Y<Object>.Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnType:Y<Object>.Z<Object>> z;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0017() {

	String str =
		"public class X {		\n" +
		"  public <T>X() {								\n" +
		"  }           								\n" +
		"  void foo(){;								\n" +
		"    new <Object>X();								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "X";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new <Object>X()>";

	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public <T>X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnAllocationExpression:new <Object>X()>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new <Object>X()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0018() {

	String str =
		"public class X <U>{		\n" +
		"  public <T>X() {								\n" +
		"  }           								\n" +
		"  void foo(){;								\n" +
		"    new <Object>X<String>();								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "X";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new <Object>X<String>()>";

	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"public class X<U> {\n" +
		"  public <T>X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnAllocationExpression:new <Object>X<String>()>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new <Object>X<String>()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0019() {

	String str =
		"public class X {		\n" +
		"  public class Inner {								\n" +
		"    public <U> Inner() {  								\n" +
		"    }           								\n" +
		"  }           								\n" +
		"  void foo(X x){;								\n" +
		"    x.new <Object>Inner();								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Inner";

	String expectedCompletionNodeToString = "<SelectOnQualifiedAllocationExpression:x.new <Object>Inner()>";

	String completionIdentifier = "Inner";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public class Inner {\n" +
		"    public <U>Inner() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(X x) {\n" +
		"    <SelectOnQualifiedAllocationExpression:x.new <Object>Inner()>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "x.new <Object>Inner()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0020() {

	String str =
		"public class X {		\n" +
		"  public class Inner<T> {								\n" +
		"    public <U> Inner() {  								\n" +
		"    }           								\n" +
		"  }           								\n" +
		"  void foo(X x){;								\n" +
		"    x.new <Object>Inner<String>();								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Inner";

	String expectedCompletionNodeToString = "<SelectOnQualifiedAllocationExpression:x.new <Object>Inner<String>()>";

	String completionIdentifier = "Inner";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public class Inner<T> {\n" +
		"    public <U>Inner() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(X x) {\n" +
		"    <SelectOnQualifiedAllocationExpression:x.new <Object>Inner<String>()>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "x.new <Object>Inner<String>()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0021() {

	String str =
		"public class X<V> {		\n" +
		"  public class Inner<T> {								\n" +
		"    public <U> Inner() {  								\n" +
		"    }           								\n" +
		"  }           								\n" +
		"  void foo(){;								\n" +
		"    new X<String>().new <Object>Inner<String>();								\n" +
		"  }           								\n" +
		"}											\n";

	String selection = "Inner";

	String expectedCompletionNodeToString = "<SelectOnQualifiedAllocationExpression:new X<String>().new <Object>Inner<String>()>";

	String completionIdentifier = "Inner";
	String expectedUnitDisplayString =
		"public class X<V> {\n" +
		"  public class Inner<T> {\n" +
		"    public <U>Inner() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnQualifiedAllocationExpression:new X<String>().new <Object>Inner<String>()>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new X<String>().new <Object>Inner<String>()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Selection of simple name
 */
public void test0022() {

	String str =
		"public class X {		\n" +
		"  Y.Z z;								\n" +
		"}											\n";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Y.Z> z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209639
public void test0023() {

	String str =
		"package test;\n" +
		"public class Test  {\n" +
		"	public List<String> foo() {\n" +
		"		return Collections.emptyList();\n" +
		"	}\n" +
		"}";

	String selection = "emptyList";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:Collections.emptyList()>";

	String completionIdentifier = "emptyList";
	String expectedUnitDisplayString =
		"package test;\n" +
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public List<String> foo() {\n" +
		"    return <SelectOnMessageSend:Collections.emptyList()>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Collections.emptyList()";
	String testName = "<select method>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255142
public void test0024() {

	String str =
		"import java.util.List;\n" +
		"public class X {\n" +
		"        <T> T bar(T t) { return t; }\n" +
		"        void foo(boolean b, Runnable r) {\n" +
		"                Zork z = null;\n" +
		"                String s = (String) bar(z); // 5\n" +
		"        }\n" +
		"}\n" +
		"\n";

	String selection = "bar";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:bar(z)>";

	String completionIdentifier = "bar";
	String expectedUnitDisplayString =
		"import java.util.List;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T>T bar(T t) {\n" +
		"  }\n" +
		"  void foo(boolean b, Runnable r) {\n" +
		"    Zork z;\n" +
		"    String s = (String) <SelectOnMessageSend:bar(z)>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "bar(z)";
	String testName = "<select method>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
}
