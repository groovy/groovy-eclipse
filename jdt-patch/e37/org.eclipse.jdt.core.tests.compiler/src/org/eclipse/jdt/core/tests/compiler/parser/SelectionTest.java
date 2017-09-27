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
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

public class SelectionTest extends AbstractSelectionTest {
static {
//		TESTS_NUMBERS = new int[] { 53 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(SelectionTest.class);
}

public SelectionTest(String testName) {
	super(testName);
}
/*
 * Select superclass
 */
public void test01() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X extends IOException {		\n" +
		"}											\n";

	String selectionStartBehind = "extends ";
	String selectionEndBehind = "IOException";

	String expectedCompletionNodeToString = "<SelectOnType:IOException>";
	String completionIdentifier = "IOException";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <SelectOnType:IOException> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOException";
	String testName = "<select superclass>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select superinterface
 */
public void test02() {

	String str =
		"import java.io.*;													\n" +
		"																	\n" +
		"public class X extends IOException implements Serializable {		\n" +
		" int foo(){} 														\n" +
		"}																	\n";

	String selectionStartBehind = "implements ";
	String selectionEndBehind = "Serializable";

	String expectedCompletionNodeToString = "<SelectOnType:Serializable>";
	String completionIdentifier = "Serializable";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException implements <SelectOnType:Serializable> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Serializable";
	String testName = "<select superinterface>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select qualified superclass
 */
public void test03() {

	String str =
		"public class X extends java.io.IOException {	\n" +
		"}												\n";

	String selectionStartBehind = "java.io.";
	String selectionEndBehind = "IOException";

	String expectedCompletionNodeToString = "<SelectOnType:java.io.IOException>";
	String completionIdentifier = "IOException";
	String expectedUnitDisplayString =
		"public class X extends <SelectOnType:java.io.IOException> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.IOException";
	String testName = "<select qualified superclass>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select package from qualified superclass
 */
public void test04() {

	String str =
		"public class X extends java.io.IOException {	\n" +
		"}												\n";

	String selectionStartBehind = "java.";
	String selectionEndBehind = "java.io";

	String expectedCompletionNodeToString = "<SelectOnType:java.io>";
	String completionIdentifier = "io";
	String expectedUnitDisplayString =
		"public class X extends <SelectOnType:java.io> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.IOException";
	String testName = "<select package from qualified superclass>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select message send
 */
public void test05() {

	String str =
		"public class X extends java.io.IOException {	\n" +
		"	int foo(){									\n" +
		"		System.out.println(\"hello\");			\n";

	String selectionStartBehind = "System.out.";
	String selectionEndBehind = "println";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:System.out.println(\"hello\")>";
	String completionIdentifier = "println";
	String expectedUnitDisplayString =
		"public class X extends java.io.IOException {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    <SelectOnMessageSend:System.out.println(\"hello\")>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "System.out.println(\"hello\")";
	String testName = "<select message send>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select message send with recovery before
 */
public void test06() {

	String str =
		"public class X extends 						\n" +
		"	int foo(){									\n" +
		"		System.out.println(\"hello\");			\n";

	String selectionStartBehind = "System.out.";
	String selectionEndBehind = "println";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:System.out.println(\"hello\")>";
	String completionIdentifier = "println";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    <SelectOnMessageSend:System.out.println(\"hello\")>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "System.out.println(\"hello\")";
	String testName = "<select message send with recovery before>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select message send with sibling method
 */
public void test07() {

	String str =
		"public class X extends 						\n" +
		"	int foo(){									\n" +
		"		this.bar(\"hello\");					\n" +
		"	int bar(String s){							\n" +
		"		return s.length();						\n"	+
		"	}											\n" +
		"}												\n";

	String selectionStartBehind = "this.";
	String selectionEndBehind = "this.bar";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:this.bar(\"hello\")>";
	String completionIdentifier = "bar";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    <SelectOnMessageSend:this.bar(\"hello\")>;\n" +
		"  }\n" +
		"  int bar(String s) {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "this.bar(\"hello\")";
	String testName = "<select message send with sibling method>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select field reference
 */
public void test08() {

	String str =
		"public class X {		 						\n" +
		"	int num = 0;								\n" +
		"	int foo(){									\n" +
		"		int j = this.num;						\n" +
		"}												\n";

	String selectionStartBehind = "this.";
	String selectionEndBehind = "this.num";

	String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.num>";
	String completionIdentifier = "num";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  int num;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    int j = <SelectionOnFieldReference:this.num>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "this.num";
	String testName = "<select field reference>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select field reference with syntax errors
 */
public void test09() {

	String str =
		"public class X 		 						\n" +
		"	int num 									\n" +
		"	int foo(){									\n" +
		"		int j = this.num;						\n" +
		"}												\n";

	String selectionStartBehind = "this.";
	String selectionEndBehind = "this.num";

	String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.num>";
	String completionIdentifier = "num";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  int num;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    int j = <SelectionOnFieldReference:this.num>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "this.num";
	String testName = "<select field reference with syntax errors>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select field reference inside message receiver
 */
public void test10() {

	String str =
		"public class X {		 					\n" +
		"	X x; 									\n" +
		"	int foo(){								\n" +
		"		int j = this.x.foo();				\n" +
		"}											\n";

	String selectionStartBehind = "this.";
	String selectionEndBehind = "this.x";

	String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.x>";
	String completionIdentifier = "x";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  X x;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    int j = <SelectionOnFieldReference:this.x>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "this.x";
	String testName = "<select field reference inside message receiver>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select allocation
 */
public void test11() {

	String str =
		"public class X {		 					\n" +
		"	X(int i){}								\n" +
		"	int foo(){								\n" +
		"		int j = 0;							\n" +
		"		X x = new X(j);						\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "new ";
	String selectionEndBehind = "new X";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new X(j)>";
	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  X(int i) {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    int j;\n" +
		"    X x = <SelectOnAllocationExpression:new X(j)>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new X(j)";
	String testName = "<select allocation>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select qualified allocation
 */
public void test12() {

	String str =
		"public class X {		 					\n" +
		" 	class Y {								\n" +
		"		Y(int i){}							\n" +
		"	}										\n" +
		"	X(int i){}								\n" +
		"	int foo(){								\n" +
		"		int j = 0;							\n" +
		"		X x = new X(j);						\n" +
		"		x.new Y(1);							\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "x.new ";
	String selectionEndBehind = "x.new Y";

	String expectedCompletionNodeToString = "<SelectOnQualifiedAllocationExpression:x.new Y(1)>";
	String completionIdentifier = "Y";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y(int i) {\n" +
		"    }\n" +
		"  }\n" +
		"  X(int i) {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    int j;\n" +
		"    X x;\n" +
		"    <SelectOnQualifiedAllocationExpression:x.new Y(1)>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "x.new Y(1)";
	String testName = "<select qualified allocation>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select qualified name reference receiver
 */
public void test13() {

	String str =
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		java.lang.System.out.println();		\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "java.lang.";
	String selectionEndBehind = "java.lang.System";

	String expectedCompletionNodeToString = "<SelectOnName:java.lang.System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    <SelectOnName:java.lang.System>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.lang.System.out";
	String testName = "<select qualified name receiver>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select qualified name reference
 */
public void test14() {

	String str =
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		System sys = java.lang.System;		\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "java.lang.";
	String selectionEndBehind = "java.lang.System";

	String expectedCompletionNodeToString = "<SelectOnName:java.lang.System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    System sys = <SelectOnName:java.lang.System>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.lang.System";
	String testName = "<select qualified name>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select variable type with modifier
 */
public void test15() {

	String str =
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		final System sys = null;			\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "final ";
	String selectionEndBehind = "final System";

	String expectedCompletionNodeToString = "<SelectOnType:System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    final <SelectOnType:System> sys;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "System";
	String testName = "<select variable type with modifier>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select variable type
 */
public void test16() {

	String str =
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		System sys = null;					\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "\n		";
	String selectionEndBehind = "\n		System";

	String expectedCompletionNodeToString = "<SelectOnType:System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    <SelectOnType:System> sys;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "System";
	String testName = "<select variable type>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select name
 */
public void test17() {

	String str =
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		System 								\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "\n		";
	String selectionEndBehind = "\n		System";

	String expectedCompletionNodeToString = "<SelectOnName:System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    <SelectOnName:System>;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "System";
	String testName = "<select name>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select anonymous type
 */
public void test18() {

	String str =
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		new Object(){						\n" +
		"			int bar(){}						\n" +
		"		}									\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "new ";
	String selectionEndBehind = "new Object";

	String expectedCompletionNodeToString =
		"<SelectOnAllocationExpression:new Object() {\n" +
		"}>";
	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    <SelectOnAllocationExpression:new Object() {\n" +
		"    }>;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "new Object()";
	String testName = "<select anonymous type>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select cast type
 */
public void test19() {

	String str =
		"public class X {		 					\n" +
		"	Object foo(){							\n" +
		"		return (Object) this;				\n" +
		"		}									\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "return (";
	String selectionEndBehind = "return (Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";
	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  Object foo() {\n" +
		"    return (<SelectOnType:Object>) this;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "Object";
	String testName = "<select cast type>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select package
 */
public void test20() {

	String str =
		"package x.y.other;					\n" +
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		}									\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "x.";
	String selectionEndBehind = "x.y";

	String expectedCompletionNodeToString = "<SelectOnPackage:x.y>";
	String completionIdentifier = "y";
	String expectedUnitDisplayString =
		"package <SelectOnPackage:x.y>;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "x.y.other";
	String testName = "<select package>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select import
 */
public void test21() {

	String str =
		"import x.y.Other;					\n" +
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		}									\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "y.";
	String selectionEndBehind = "y.Other";

	String expectedCompletionNodeToString = "<SelectOnImport:x.y.Other>";
	String completionIdentifier = "Other";
	String expectedUnitDisplayString =
		"import <SelectOnImport:x.y.Other>;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "x.y.Other";
	String testName = "<select import>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select import on demand
 */
public void test22() {

	String str =
		"import x.y.other.*;					\n" +
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		}									\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "y.";
	String selectionEndBehind = "y.other";

	String expectedCompletionNodeToString = "<SelectOnImport:x.y.other>";
	String completionIdentifier = "other";
	String expectedUnitDisplayString =
		"import <SelectOnImport:x.y.other>;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "x.y.other";
	String testName = "<select import on demand>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select array initializer type
 */
public void test23() {

	String str =
		"public class X {		 					\n" +
		"	int foo(){								\n" +
		"		String[] p = new String[]{\"Left\"};\n" +
//		"		}									\n" +
		"	}										\n" +
		"}											\n";

	String selectionStartBehind = "new ";
	String selectionEndBehind = "new String";
	String expectedCompletionNodeToString = "<SelectOnType:String>";
	String completionIdentifier = "String";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    String[] p = <SelectOnType:String>;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "String";
	String testName = "<select array initializer type>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select nested type superclass with syntax error behind
 */
public void test24() {

	String str =
		"public class G {					\n" +
		"	void foo() {					\n" +
		"		class X {					\n" +
		"			class Y extends G {		\n" +
		"				int foo()			\n" +
		"			}						\n" +
		"		}							\n" +
		"	}								\n" +
		"}									\n";

	String selectionStartBehind = "extends ";
	String selectionEndBehind = "extends G";

	String expectedCompletionNodeToString = "<SelectOnType:G>";

	String completionIdentifier = "G";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  public G() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class X {\n" +
		"      class Y extends <SelectOnType:G> {\n" +
		"        Y() {\n" +
		"        }\n" +
		"        int foo() {\n" +
		"        }\n" +
		"      }\n" +
		"      X() {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "G";
	String testName = "<select nested type superclass>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select super
 */
public void test25() {

	String str =
		"public class G {					\n" +
		"	Object foo() {					\n" +
		"		return super.foo();			\n" +
		"	}								\n" +
		"}									\n";

	String selectionStartBehind = "return ";
	String selectionEndBehind = "return super";

	String expectedCompletionNodeToString = "<SelectOnSuper:super>";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  public G() {\n" +
		"  }\n" +
		"  Object foo() {\n" +
		"    return <SelectOnSuper:super>;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "super";
	String testName = "<select super>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select qualified super
 */
public void test26() {

	String str =
		"public class G {						\n" +
		"	Object foo() {						\n" +
		"		new X(){						\n" +
		"			Object bar(){				\n" +
		"				return G.super.foo();	\n" +
		"			}							\n" +
		"		}								\n" +
		"	}									\n" +
		"}										\n";

	String selectionStartBehind = "G.";
	String selectionEndBehind = "G.super";

	String expectedCompletionNodeToString = "<SelectOnQualifiedSuper:G.super>";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  public G() {\n" +
		"  }\n" +
		"  Object foo() {\n" +
		"    new X() {\n" +
		"      Object bar() {\n" +
		"        return <SelectOnQualifiedSuper:G.super>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "G.super";
	String testName = "<select qualified super>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select super constructor call
 */
public void test27() {

	String str =
		"public class G {					\n" +
		"	G() {							\n" +
		"		super();					\n" +
		"	}								\n" +
		"}									\n";

	String selectionStartBehind = "\n\t\t";
	String selectionEndBehind = "super";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:super()>;";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  G() {\n" +
		"    <SelectOnExplicitConstructorCall:super()>;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "super()";
	String testName = "<select super constructor call>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select qualified super constructor call
 */
public void test28() {

	String str =
		"public class G {						\n" +
		"	class M {}							\n" +
		"	static Object foo() {				\n" +
		"		class X extends M {				\n" +
		"			X (){						\n" +
		"				new G().super();		\n" +
		"			}							\n" +
		"		}								\n" +
		"	}									\n" +
		"}										\n";

	String selectionStartBehind = "new G().";
	String selectionEndBehind = "new G().super";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().super()>;";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  class M {\n" +
		"    M() {\n" +
		"    }\n" +
		"  }\n" +
		"  public G() {\n" +
		"  }\n" +
		"  static Object foo() {\n" +
		"    class X extends M {\n" +
		"      X() {\n" +
		"        <SelectOnExplicitConstructorCall:new G().super()>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new G().super()";
	String testName = "<select qualified super constructor call>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select qualified super constructor call with arguments
 */
public void test29() {

	String str =
		"public class G {								\n" +
		"	class M {}									\n" +
		"	static Object foo() {						\n" +
		"		class X extends M {						\n" +
		"			X (){								\n" +
		"				new G().super(23 + \"hello\");	\n" +
		"			}									\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n";

	String selectionStartBehind = "new G().";
	String selectionEndBehind = "new G().super";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().super((23 + \"hello\"))>;";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  class M {\n" +
		"    M() {\n" +
		"    }\n" +
		"  }\n" +
		"  public G() {\n" +
		"  }\n" +
		"  static Object foo() {\n" +
		"    class X extends M {\n" +
		"      X() {\n" +
		"        <SelectOnExplicitConstructorCall:new G().super((23 + \"hello\"))>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new G().super(23 + \"hello\")";
	String testName = "<select qualified super constructor call with arguments>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select super constructor call with arguments
 */
public void test30() {

	String str =
		"public class G {					\n" +
		"	G() {							\n" +
		"		super(new G());				\n" +
		"	}								\n" +
		"}									\n";

	String selectionStartBehind = "\n\t\t";
	String selectionEndBehind = "super";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:super(new G())>;";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  G() {\n" +
		"    <SelectOnExplicitConstructorCall:super(new G())>;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "super(new G())";
	String testName = "<select super constructor call with arguments>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Regression test for 1FVQ0LK
 */
public void test31() {

	String str =
		"class X {							\n" +
		"	Y f;							\n" +
		"	void foo() {					\n" +
		"		new Bar(fred());			\n" +
		"		Z z= new Z();				\n" +
		"	}								\n" +
		"}									\n";

	String selectionStartBehind = "\n\t";
	String selectionEndBehind = "Y";

	String expectedCompletionNodeToString = "<SelectOnType:Y>";

	String completionIdentifier = "Y";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <SelectOnType:Y> f;\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "Y";
	String testName = "<regression test for 1FVQ0LK>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Regression test for 1FWT4AJ: ITPCOM:WIN98 - SelectionParser produces duplicate type declaration
 */
public void test32() {

	String str =
		"package search;												\n"+
		"import java.io.*;												\n"+
		"public class PhraseQuery {										\n"+
		"	public boolean containsPhrase(){							\n"+
		"		try {													\n"+
		"				char currentChar = \"hello\".toLowerCase()		\n"+
		"	}															\n"+
		"}																\n";

	String selectionStartBehind = "\"hello\".";
	String selectionEndBehind = "\"hello\".toLowerCase";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:\"hello\".toLowerCase()>";

	String completionIdentifier = "toLowerCase";
	String expectedUnitDisplayString =
		"package search;\n" +
		"import java.io.*;\n" +
		"public class PhraseQuery {\n" +
		"  public PhraseQuery() {\n" +
		"  }\n" +
		"  public boolean containsPhrase() {\n" +
		"    {\n" +
		"      char currentChar = <SelectOnMessageSend:\"hello\".toLowerCase()>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "\"hello\".toLowerCase()";
	String testName = "<1FWT4AJ: ITPCOM:WIN98 - SelectionParser produces duplicate type declaration>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Regression test for 1G4CLZM: ITPJUI:WINNT - 'Import Selection' - Set not found
 */
public void test33() {

	String str =
		"	import java.util.AbstractMap;				\n"+
		"	public class c4 extends AbstractMap {		\n"+
		"		/**										\n"+
		"		 * @see AbstractMap#entrySet			\n"+
		"		 */										\n"+
		"		public Set entrySet() {					\n"+
		"			return null;						\n"+
		"		}										\n"+
		"	}											\n";

	String selectionStartBehind = "\n\t\tpublic ";
	String selectionEndBehind = "public Set";

	String expectedCompletionNodeToString = "<SelectOnType:Set>";

	String completionIdentifier = "Set";
	String expectedUnitDisplayString =
		"import java.util.AbstractMap;\n" +
		"public class c4 extends AbstractMap {\n" +
		"  public c4() {\n" +
		"  }\n" +
		"  public <SelectOnType:Set> entrySet() {\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "Set";
	String testName = "<1G4CLZM: ITPJUI:WINNT - 'Import Selection' - Set not found>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Regression test for 1GB99S3: ITPJUI:WINNT - SH: NPE in editor while getting hover help
 */
public void test34() {

	String str =
		"public class X {							\n"+
		"	public int foo() {						\n"+
		"		Object[] array = new Object[0];		\n"+
		"		return array.length;				\n"+
		"	}										\n"+
		"}											\n";

	String selectionStartBehind = "\n\t\treturn ";
	String selectionEndBehind = "array.length";

	String expectedCompletionNodeToString = NONE;

	String completionIdentifier = NONE;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public int foo() {\n" +
		"    Object[] array;\n" +
		"    return array.length;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = NONE;
	String testName = "<1GB99S3: ITPJUI:WINNT - SH: NPE in editor while getting hover help>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select this constructor call
 */
public void test35() {

	String str =
		"public class G {					\n" +
		"	G() {							\n" +
		"	}								\n" +
		"	G(int x) {						\n" +
		"		this();						\n" +
		"	}								\n" +
		"}									\n";

	String selectionStartBehind = "\n\t\t";
	String selectionEndBehind = "this";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:this()>;";

	String completionIdentifier = "this";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  G() {\n" +
		"  }\n" +
		"  G(int x) {\n" +
		"    <SelectOnExplicitConstructorCall:this()>;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "this()";
	String testName = "<select this constructor call>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select qualified this constructor call
 */
public void test36() {

	String str =
		"public class G {						\n" +
		"	static Object foo() {				\n" +
		"		class X {						\n" +
		"			X (){						\n" +
		"			}							\n" +
		"			X (int x){					\n" +
		"				new G().this();			\n" +
		"			}							\n" +
		"		}								\n" +
		"	}									\n" +
		"}										\n";

	String selectionStartBehind = "new G().";
	String selectionEndBehind = "new G().this";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().this()>;";

	String completionIdentifier = "this";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  public G() {\n" +
		"  }\n" +
		"  static Object foo() {\n" +
		"    class X {\n" +
		"      X() {\n" +
		"        super();\n"+
		"      }\n" +
		"      X(int x) {\n" +
		"        <SelectOnExplicitConstructorCall:new G().this()>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new G().this()";
	String testName = "<select qualified this constructor call>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select qualified this constructor call with arguments
 */
public void test37() {

	String str =
		"public class G {								\n" +
		"	static Object foo() {						\n" +
		"		class X {								\n" +
		"			X (){								\n" +
		"			}									\n" +
		"			X (int x){							\n" +
		"				new G().this(23 + \"hello\");	\n" +
		"			}									\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n";

	String selectionStartBehind = "new G().";
	String selectionEndBehind = "new G().this";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().this((23 + \"hello\"))>;";

	String completionIdentifier = "this";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  public G() {\n" +
		"  }\n" +
		"  static Object foo() {\n" +
		"    class X {\n" +
		"      X() {\n" +
		"        super();\n"+
		"      }\n" +
		"      X(int x) {\n" +
		"        <SelectOnExplicitConstructorCall:new G().this((23 + \"hello\"))>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new G().this(23 + \"hello\")";
	String testName = "<select qualified this constructor call with arguments>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * Select this constructor call with arguments
 */
public void test38() {

	String str =
		"public class G {					\n" +
		"	G() {							\n" +
		"		this(new G());				\n" +
		"	}								\n" +
		"}									\n";

	String selectionStartBehind = "\n\t\t";
	String selectionEndBehind = "this";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:this(new G())>;";

	String completionIdentifier = "this";
	String expectedUnitDisplayString =
		"public class G {\n" +
		"  G() {\n" +
		"    <SelectOnExplicitConstructorCall:this(new G())>;\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "this(new G())";
	String testName = "<select this constructor call with arguments>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * bugs 3293 search does not work in inner class (1GEUQHJ)
 */
public void test39() {

	String str =
		"public class X {                \n" +
		"  Object hello = new Object(){  \n" +
		"    public void foo(String s){  \n" +
		"      s.length();               \n" +
		"    }                           \n" +
		"  };                            \n" +
		"}								 \n";

	String selectionStartBehind = "s.";
	String selectionEndBehind = "length";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:s.length()>";

	String completionIdentifier = "length";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Object hello = new Object() {\n" +
		"    public void foo(String s) {\n" +
		"      <SelectOnMessageSend:s.length()>;\n" +
		"    }\n" +
		"  };\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "s.length()";
	String testName = "<select message send in anonymous class>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

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
 * bugs 3229 OpenOnSelection - strange behaviour of code resolve (1GAVL08)
 */
public void test40() {

	String str =
		"public class X {                \n" +
		"  Object                        \n" +
		"}								 \n";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <SelectOnType:Object>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Object";
	String testName = "<select fake field>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs 11475 selection on local name.
 */
public void test41() {

	String str =
		"public class X {                \n" +
		"  public void foo(){                   \n" +
		"    Object var;              \n" +
		"  }                             \n" +
		"}								 \n";

	String selection = "var";

	String expectedCompletionNodeToString = "<SelectionOnLocalName:Object var>;";

	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    <SelectionOnLocalName:Object var>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "var";
	String testName = "<select local name>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs 11475 selection on argument name.
 */
public void test42() {

	String str =
		"public class X {                \n" +
		"  public void foo(Object var){          \n" +
		"  }                             \n" +
		"}								 \n";

	String selection = "var";

	String expectedCompletionNodeToString = "<SelectionOnArgumentName:Object var>";

	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo(<SelectionOnArgumentName:Object var>) {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "var";
	String testName = "<select argument name>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs 11475 selection on argument name inside catch statement.
 */
public void test43() {

	String str =
		"public class X {                \n" +
		"  public void foo(){                   \n" +
		"    try{              \n" +
		"    }catch(Object var){}\n" +
		"  }                             \n" +
		"}								 \n";

	String selection = "var";

	String expectedCompletionNodeToString = "<SelectionOnArgumentName:Object var>";

	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    <SelectionOnArgumentName:Object var>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "var";
	String testName = "<select argument name inside catch statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs 15430
 */
public void test44() {

	String str =
		"public class X {                \n" +
		"  String x = super.foo()  \n" +
		"}								 \n";

	String selection = "super";

	String expectedCompletionNodeToString = "<SelectOnSuper:super>";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  String x = <SelectOnSuper:super>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "super";
	String testName = "<select super in field initializer>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs 14468
 */
public void test45() {

	String str =
		"public class X {                \n" +
		"  void foo() {\n" +
		"    if(x instanceof Object){\n" +
		"    }\n" +
		"  }  \n" +
		"}								 \n";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"public class X {\n"+
		"  public X() {\n"+
		"  }\n"+
		"  void foo() {\n"+
		"    <SelectOnType:Object>;\n"+
		"  }\n"+
		"}\n";
	String expectedReplacedSource = "Object";
	String testName = "<select inside instanceof statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs 14468
 */
public void test46() {

	String str =
		"public class X {                \n" +
		"  void foo() {\n" +
		"    y = x instanceof Object;\n" +
		"  }  \n" +
		"}								 \n";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"public class X {\n"+
		"  public X() {\n"+
		"  }\n"+
		"  void foo() {\n"+
		"    <SelectOnType:Object>;\n"+
		"  }\n"+
		"}\n";
	String expectedReplacedSource = "Object";
	String testName = "<select inside instanceof statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs 14468
 */
public void test47() {

	String str =
		"public class X {                \n" +
		"  void foo() {\n" +
		"   boolean y = x instanceof Object;\n" +
		"  }  \n" +
		"}								 \n";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"public class X {\n"+
		"  public X() {\n"+
		"  }\n"+
		"  void foo() {\n"+
		"    boolean y = <SelectOnType:Object>;\n"+
		"  }\n"+
		"}\n";
	String expectedReplacedSource = "Object";
	String testName = "<select inside instanceof statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs 14468
 */
public void test48() {

	String str =
		"public class X {                \n" +
		"  boolean y = x instanceof Object;\n" +
		"}								 \n";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"public class X {\n"+
		"  boolean y = <SelectOnType:Object>;\n"+
		"  public X() {\n"+
		"  }\n"+
		"}\n";
	String expectedReplacedSource = "Object";
	String testName = "<select inside instanceof statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs 28064
 */
public void test49() {

	String str =
		"public class X {                \n" +
		"  X x = new X(){}\n" +
		"}								 \n";

	String selection = "X";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new X() {\n" +
											"}>";

	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"public class X {\n"+
		"  X x = <SelectOnAllocationExpression:new X() {\n" +
		"  }>;\n"+
		"  public X() {\n"+
		"  }\n"+
		"}\n";
	String expectedReplacedSource = "new X()";
	String testName = "<select anonymous type>";

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
 * bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=52422
 */
public void test50() {

	String str =
		"public class X {                \n" +
		"  void foo() {\n" +
		"    new Object(){\n" +
		"      void bar(){\n" +
		"        bar2();\n" +
		"      }\n" +
		"      void bar2() {\n" +
		"      }\n" +
		"    }\n" +
		"  }  \n" +
		"}								 \n";

	String selection = "bar2";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:bar2()>";

	String completionIdentifier = "bar2";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new Object() {\n" +
		"      void bar() {\n" +
		"        <SelectOnMessageSend:bar2()>;\n" +
		"      }\n" +
		"      void bar2() {\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "bar2()";
	String testName = "<select inside anonymous type>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=52422
 */
public void test51() {

	String str =
		"public class X {                \n" +
		"  void foo() {\n" +
		"    new Object(){\n" +
		"      void foo0(){\n" +
		"        new Object(){\n" +
		"          void bar(){\n" +
		"            bar2();\n" +
		"          }\n" +
		"          void bar2() {\n" +
		"          }\n" +
		"        }\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}								 \n";

	String selection = "bar2";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:bar2()>";

	String completionIdentifier = "bar2";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new Object() {\n" +
		"      void foo0() {\n" +
		"        new Object() {\n" +
		"          void bar() {\n" +
		"            <SelectOnMessageSend:bar2()>;\n" +
		"          }\n" +
		"          void bar2() {\n" +
		"          }\n" +
		"        };\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "bar2()";
	String testName = "<select inside anonymous type>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
 * bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=52422
 */
public void test52() {

	String str =
		"public class X {                \n" +
		"  void foo() {\n" +
		"    new Object(){\n" +
		"      void foo0(){\n" +
		"        new Object(){\n" +
		"          void bar(){\n" +
		"            bar2();\n" +
		"          }\n" +

		"        }\n" +
		"      }\n" +
		"      void bar2() {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}								 \n";

	String selection = "bar2";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:bar2()>";

	String completionIdentifier = "bar2";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new Object() {\n" +
		"      void foo0() {\n" +
		"        new Object() {\n" +
		"          void bar() {\n" +
		"            <SelectOnMessageSend:bar2()>;\n" +
		"          }\n" +
		"        };\n" +
		"      }\n" +
		"      void bar2() {\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "bar2()";
	String testName = "<select inside anonymous type>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
public void test53() {

	String str =
		"public class X {                \n" +
		"  void foo(String[] stringArray) {\n" +
		"    for(String string2 : stringArray);\n" +
		"  }\n" +
		"}								 \n";

	String selection = "string2";

	String expectedCompletionNodeToString = "<SelectionOnLocalName:String string2>;";

	String completionIdentifier = "string2";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(String[] stringArray) {\n" +
		"    for (<SelectionOnLocalName:String string2> : stringArray) \n" +
		"      ;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "string2";
	String testName = "<select>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84001
public void test54() {

	String str =
		"public class X {                \n" +
		"  void foo() {\n" +
		"    new Test.Sub();\n" +
		"  }\n" +
		"}								 \n";

	String selection = "Test";

	String expectedCompletionNodeToString = "<SelectOnType:Test>";

	String completionIdentifier = "Test";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new <SelectOnType:Test>();\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Test";
	String testName = "<select>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84001
public void test55() {

	String str =
		"public class X {                \n" +
		"  void foo() {\n" +
		"    new Test.Sub();\n" +
		"  }\n" +
		"}								 \n";

	String selection = "Sub";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new Test.Sub()>";

	String completionIdentifier = "Sub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <SelectOnAllocationExpression:new Test.Sub()>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new Test.Sub()";
	String testName = "<select>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

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
