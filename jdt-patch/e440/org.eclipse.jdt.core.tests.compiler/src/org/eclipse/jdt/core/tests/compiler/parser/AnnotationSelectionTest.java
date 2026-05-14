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

public class AnnotationSelectionTest extends AbstractSelectionTest {
public AnnotationSelectionTest(String testName) {
	super(testName);
}
/*
 * Selection at specific location
 */
public void test0001() {

	String str =
		"public @MyAnn class X {		\n" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public @<SelectOnType:MyAnn> class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
		"public @MyAnn.ZZ class X {		\n" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public @<SelectOnType:MyAnn> class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0003() {

	String str =
		"public @MyAnn.ZZ class X {		\n" +
		"}											\n";

	String selection = "ZZ";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn.ZZ>";

	String completionIdentifier = "ZZ";
	String expectedUnitDisplayString =
		"public @<SelectOnType:MyAnn.ZZ> class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn.ZZ";
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
		"public @MyAnn.ZZ class X {		\n" +
		"											\n";

	String selection = "ZZ";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn.ZZ>";

	String completionIdentifier = "ZZ";
	String expectedUnitDisplayString =
		"public @<SelectOnType:MyAnn.ZZ> class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn.ZZ";
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
//TODO enable this test when selection parser support this test case
public void _test0005() {

	String str =
		"public @MyAnn.ZZ" +
		"											\n";

	String selection = "ZZ";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn.ZZ>";

	String completionIdentifier = "ZZ";
	String expectedUnitDisplayString =
		"public @<SelectOnType:MyAnn.ZZ> class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn.ZZ";
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
		"public  class X {" +
		"  public @MyAnn void foo() {" +
		"  }" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public @<SelectOnType:MyAnn> void foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0007() {

	String str =
		"public  class X {" +
		"  public @MyAnn void foo(" +
		"  " +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public @<SelectOnType:MyAnn> void foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
		"public  class X {" +
		"  public @MyAnn Object var;" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public @<SelectOnType:MyAnn> Object var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0009() {

	String str =
		"public class X {" +
		"  public @MyAnn Object var" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public @<SelectOnType:MyAnn> Object var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0010() {

	String str =
		"public class X {" +
		"  public void foo(@MyAnn int i) {" +
		"  }" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo(@<SelectOnType:MyAnn> int i) {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0011() {

	String str =
		"public class X {" +
		"  public @MyAnn class Y {" +
		"  }" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public @<SelectOnType:MyAnn> class Y {\n" +
		"    public Y() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0012() {

	String str =
		"public class X {" +
		"  public void foo() {" +
		"    @MyAnn int i;" +
		"  }" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    @<SelectOnType:MyAnn> int i;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0013() {

	String str =
		"public @MyAnn() class X {" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public @<SelectOnType:MyAnn>() class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0014() {

	String str =
		"public @MyAnn(A.B) class X {" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public @<SelectOnType:MyAnn>(A.B) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0015() {

	String str =
		"public @MyAnn(value = \"\") class X {" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public @<SelectOnType:MyAnn>(value = \"\") class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0016() {

	String str =
		"public @MyAnn(value1 = \"\", value2 = \"\") class X {" +
		"}											\n";

	String selection = "MyAnn";

	String expectedCompletionNodeToString = "<SelectOnType:MyAnn>";

	String completionIdentifier = "MyAnn";
	String expectedUnitDisplayString =
		"public @<SelectOnType:MyAnn>(value1 = \"\",value2 = \"\") class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "MyAnn";
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
public void test0017() {

	String str =
		"public @MyAnn(value1 = \"\", value2 = \"\") class X {" +
		"}											\n";

	String selection = "value1";

	String expectedCompletionNodeToString = "<SelectOnName:value1>";

	String completionIdentifier = "value1";
	String expectedUnitDisplayString =
		"public @MyAnn(<SelectOnName:value1>,value2 = \"\") class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "value1";
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
}
