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

import junit.framework.Test;

import org.eclipse.jdt.internal.codeassist.complete.InvalidCursorLocation;

public class DietCompletionTest extends AbstractCompletionTest {
public DietCompletionTest(String testName){
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(DietCompletionTest.class);
}
/*
 * Complete on superclass
 */
public void test01() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X extends IOException {		\n" +
		"}											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnClass:IOEx> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOException";
	String testName = "<complete on superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on superinterface
 */
public void test02() {

	String str =
		"import java.io.*;													\n" +
		"																	\n" +
		"public class X extends IOException implements Serializable {		\n" +
		" int foo(){} \n" +
		"}																	\n";

	String completeBehind = "Seria";
	String expectedCompletionNodeToString = "<CompleteOnInterface:Seria>";
	String completionIdentifier = "Seria";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException implements <CompleteOnInterface:Seria> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Serializable";
	String testName = "<complete on superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on qualified superclass
 */
public void test03() {

	String str =
		"import java.io.*;													\n" +
		"																	\n" +
		"public class X extends java.io.IOException  {						\n" +
		"}																	\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io.IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnClass:java.io.IOEx> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.IOException";
	String testName = "<complete on qualified superclass>";


	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);

}
/*
 * Complete on qualified superinterface
 */
public void test04() {

	String str =
		"import java.io.*;															\n" +
		"																			\n" +
		"public class X extends IOException implements java.io.Serializable {		\n" +
		"}																			\n";

	String completeBehind = "Seria";
	String expectedCompletionNodeToString = "<CompleteOnInterface:java.io.Seria>";
	String completionIdentifier = "Seria";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException implements <CompleteOnInterface:java.io.Seria> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.Serializable";
	String testName = "<complete on qualified superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on incomplete superclass
 */
public void test05() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X extends IOEx {				\n" +
		"}											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnClass:IOEx> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete on incomplete superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on incomplete superinterface
 */
public void test06() {

	String str =
		"import java.io.*;													\n" +
		"																	\n" +
		"public class X extends IOException implements Seria {				\n" +
		"}																	\n";

	String completeBehind = "Seria";
	String expectedCompletionNodeToString = "<CompleteOnInterface:Seria>";
	String completionIdentifier = "Seria";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException implements <CompleteOnInterface:Seria> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Seria";
	String testName = "<complete on incomplete superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on incomplete qualified superclass
 */
public void test07() {

	String str =
		"import java.io.*;													\n" +
		"																	\n" +
		"public class X extends java.io.IOEx  		{						\n" +
		"}																	\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io.IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString = "import java.io.*;\n" +
		"public class X extends <CompleteOnClass:java.io.IOEx> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
		String expectedReplacedSource = "java.io.IOEx";
	String testName = "<complete on incomplete qualified superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on incomplete qualified superinterface
 */
public void test08() {

	String str =
		"import java.io.*;															\n" +
		"																			\n" +
		"public class X extends IOException implements java.io.Seria {				\n" +
		"}																			\n";

	String completeBehind = "Seria";
	String expectedCompletionNodeToString = "<CompleteOnInterface:java.io.Seria>";
	String completionIdentifier = "Seria";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException implements <CompleteOnInterface:java.io.Seria> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.Seria";
	String testName = "<complete on incomplete qualified superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete inside qualified superclass
 */
public void test09() {

	String str =
		"																	\n" +
		"public class X extends java.io.IOException  		{				\n" +
		"}																	\n";

	String completeBehind = ".io";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io>";
	String completionIdentifier = "io";
	String expectedUnitDisplayString =
		"public class X extends <CompleteOnClass:java.io> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.IOException";
	String testName = "<complete inside qualified superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete inside qualified superinterface
 */
public void test10() {

	String str =
		"public class X extends IOException implements java.io.Serializable {		\n" +
		"}																			\n";

	String completeBehind = ".io";
	String expectedCompletionNodeToString = "<CompleteOnInterface:java.io>";
	String completionIdentifier = "io";
	String expectedUnitDisplayString =
		"public class X extends IOException implements <CompleteOnInterface:java.io> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.Serializable";
	String testName = "<complete inside qualified superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete inside qualified superclass ending with dot
 */
public void test11() {

	String str =
		"																	\n" +
		"public class X extends java.io.	{								\n" +
		"}																	\n";

	String completeBehind = ".io.";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"public class X extends <CompleteOnClass:java.io.> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.";
	String testName = "<complete inside qualified superclass ending with dot>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete inside qualified superinterface ending with dot
 */
public void test12() {

	String str =
		"public class X extends IOException implements java.io.				 {		\n" +
		"}																			\n";

	String completeBehind = ".io.";
	String expectedCompletionNodeToString = "<CompleteOnInterface:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"public class X extends IOException implements <CompleteOnInterface:java.io.> {\n" +
		"  public X() {\n" +

		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.";
	String testName = "<complete inside qualified superinterface ending with dot>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on empty superclass
 */
public void test13() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X extends  {					\n" +
		"}											\n";

	String completeBehind = "extends ";
	String expectedCompletionNodeToString = "<CompleteOnClass:>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnClass:> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "";
	String testName = "<complete on empty superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on empty superinterface
 */
public void test14() {

	String str =
		"import java.io.*;													\n" +
		"																	\n" +
		"public class X extends IOException implements  {					\n" +
		"}																	\n";

	String completeBehind = "implements ";
	String expectedCompletionNodeToString = "<CompleteOnInterface:>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException implements <CompleteOnInterface:> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "";
	String testName = "<complete on empty superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on empty superclass followed by identifier
 */
public void test15() {

	String str =
		"public class X extends java.io. IOException  {			\n" +
		"}														\n";

	String completeBehind = "java.io.";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"public class X extends <CompleteOnClass:java.io.> {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.";
	String testName = "<complete on empty superclass followed by identifier>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on keyword extends
 */
public void test16() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X extends  {					\n" +
		"}											\n";

	String completeBehind = "extends";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:extends>";
	String completionIdentifier = "extends";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnKeyword:extends> {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "extends";
	String testName = "<complete on keyword extends>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in keyword extends
 */
public void test17() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X ext  {						\n" +
		"}											\n";

	String completeBehind = "ext";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String completionIdentifier = "ext";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnKeyword:ext> {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "ext";
	String testName = "<complete in keyword extends>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in field type
 */
public void test18() {

	String str =
		"class X {									\n" +
		"											\n" +
		"	IOException x;							\n" +
		"}											\n";

	String completeBehind = "IO";
	String expectedCompletionNodeToString = "<CompleteOnType:IO>;";
	String completionIdentifier = "IO";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <CompleteOnType:IO>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOException";
	String testName = "<complete in field type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete at beginning of field type
 */
public void test19() {

	String str =
		"class X {									\n" +
		"											\n" +
		"	final IOException x;					\n" +
		"}											\n";

	String completeBehind = "final ";
	String expectedCompletionNodeToString = "<CompleteOnType:>;";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <CompleteOnType:>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOException";
	String testName = "<complete at beginning of field type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete at beginning of superclass
 */
public void test20() {

	String str =
		"class X extends IOException {				\n" +
		"											\n" +
		"}											\n";

	String completeBehind = "extends ";
	String expectedCompletionNodeToString = "<CompleteOnClass:>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"class X extends <CompleteOnClass:> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOException";
	String testName = "<complete at beginning of superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type
 */
public void test21() {

	String str =
		"class X {									\n" +
		"	IOEx									\n" +
		"}											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <CompleteOnType:IOEx>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in argument type
 */
public void test22() {

	String str =
		"class X {									\n" +
		"	int foo(IOEx							\n" +
		"}											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <CompleteOnType:IOEx>;\n" +
		"  X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in argument type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type
 */
public void test23() {

	String str =
		"class X {									\n" +
		"	IOEx									\n" +
		"											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <CompleteOnType:IOEx>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in argument type (no closing brace for type)
 */
public void test24() {

	String str =
		"class X {									\n" +
		"	int foo(IOEx							\n" +
		"											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <CompleteOnType:IOEx>;\n" +
		"  X() {\n" +

		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in argument type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type with modifiers
 */
public void test25() {

	String str =
		"class X {									\n" +
		"	public final IOEx						\n" +
		"											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <CompleteOnType:IOEx>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type with modifiers>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in field initialization
 */
public void test26() {

	String str =
		"class X {									\n" +
		"	public final int x = IOEx				\n" +
		"											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnName:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  public final int x = <CompleteOnName:IOEx>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in field initialization>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in nth argument type
 */
public void test27() {

	String str =
		"class X {									\n" +
		"	int foo(AA a, BB b, IOEx				\n" +
		"											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <CompleteOnType:IOEx>;\n" +
		"  X() {\n" +
		"  }\n" +
		"  int foo(AA a, BB b) {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in nth argument type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in nth argument qualified type
 */
public void test28() {

	String str =
		"class X {									\n" +
		"	int foo(AA a, BB b, java.io.IOEx		\n" +
		"											\n";

	String completeBehind = ".io.";
	String expectedCompletionNodeToString = "<CompleteOnType:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  <CompleteOnType:java.io.>;\n" +
		"  X() {\n" +
		"  }\n" +
		"  int foo(AA a, BB b) {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.IOEx";
	String testName = "<complete in nth argument qualified type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in nth thrown exception qualified type
 */
public void test29() {

	String str =
		"class X {												\n" +
		"	public int foo(AA a, BB b) throws AA, java.io.IOEx	\n" +
		"														\n";

	String completeBehind = ".io";
	String expectedCompletionNodeToString = "<CompleteOnException:java.io>";
	String completionIdentifier = "io";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  public int foo(AA a, BB b) throws AA, <CompleteOnException:java.io> {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.IOEx";
	String testName = "<complete in nth thrown exception qualified type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in completed argument
 */
public void test30() {

	String str =
		"class X {												\n" +
		"	public int foo(AA a, java.io.BB b) 					\n" +
		"														\n";

	String completeBehind = "io.";
	String expectedCompletionNodeToString = "<CompleteOnType:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  public int foo(AA a, <CompleteOnType:java.io.> b) {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.io.BB";
	String testName = "<complete in in completed argument>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Negative test: no diet completion in allocation expression
 */
public void test31() {

	String str =
		"class Bar {								\n"+
		"	void foo() {							\n"+
		"		new X().zzz();						\n"+
		"	}										\n"+
		"}\n";

	String completeBehind = "new X";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = NONE;
	String expectedUnitDisplayString =
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = null;
	String testName = "<no diet completion in allocation expression>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Completion in package statement
 */
public void test32() {

	String str =
		"package x.abc				\n"+
		"import x.util.*;				\n"+
		"import x.*;					\n"+
		"class X extends util{	\n"+
		"    X(){}				\n"+
		"    X(int a, int b){}	\n"+
		"}								\n";

	String completeBehind = "x.ab";
	String expectedCompletionNodeToString = "<CompleteOnPackage:x.ab>";
	String completionIdentifier = "ab";
	String expectedUnitDisplayString =
		"package <CompleteOnPackage:x.ab>;\n" +
		"import x.util.*;\n" +
		"import x.*;\n" +
		"class X extends util {\n" +
		"  X() {\n" +
		"  }\n" +
		"  X(int a, int b) {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "x.abc";
	String testName = "<complete in package statement>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Completion in import statement
 */
public void test33() {

	String str =
		"package x.abc;				\n"+
		"import x.util				\n"+
		"import x.*;					\n"+
		"class X extends util{	\n"+
		"    X(){}				\n"+
		"    X(int a, int b){}	\n"+
		"}								\n";

	String completeBehind = "x.util";
	String expectedCompletionNodeToString = "<CompleteOnImport:x.util>";
	String completionIdentifier = "util";
	String expectedUnitDisplayString =
		"package x.abc;\n" +
		"import <CompleteOnImport:x.util>;\n" +
		"import x.*;\n" +
		"class X extends util {\n" +
		"  X() {\n" +
		"  }\n" +
		"  X(int a, int b) {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "x.util";
	String testName = "<complete in import statement>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on superclass behind a unicode
 *
 * -- compute the unicode representation for a given string --
   [ String str = "IOEx";
	StringBuffer buffer = new StringBuffer("\"");
	for (int i = 0; i < str.length(); i++){
		String hex = Integer.toHexString(str.charAt(i));
		buffer.append("\\u0000".substring(0, 6-hex.length()));
		buffer.append(hex);
	}
	buffer.append("\"");
	buffer.toString()
	]
 */
public void test34() {

	String str =
		"import java.io.*;									\n" +
		"													\n" +
		"public class X extends IOE\\u0078ception {			\n" +
		"}													\n";

	String completeBehind = "IOE\\u0078";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnClass:IOEx> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOE\\u0078ception";
	String testName = "<complete on superclass behind a unicode>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test34a() {

	String str =
		"import java.io.*;									\n" +
		"													\n" +
		"public class X extends IOException {			\n" +
		"}													\n";

	String completeBehind = "IOE";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOE>";
	String completionIdentifier = "IOE";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnClass:IOE> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOException";
	String testName = "<complete on superclass before a unicode>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test34b() {

	String str =
		"import java.io.*;									\n" +
		"													\n" +
		"public class X extends IOE\\u0078c\\u0065ption {			\n" +
		"}													\n";

	String completeBehind = "IOE\\u0078c\\u0065p";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOExcep>";
	String completionIdentifier = "IOExcep";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnClass:IOExcep> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOE\\u0078c\\u0065ption";
	String testName = "<complete on superclass behind a unicode>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test34c() {

	String str =
		"import java.io.*;									\n" +
		"													\n" +
		"public class X \\u0065xt\\u0065nds IOE\\u0078c\\u0065ption {			\n" +
		"}													\n";

	String completeBehind = "IOE\\u0078c\\u0065p";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOExcep>";
	String completionIdentifier = "IOExcep";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends <CompleteOnClass:IOExcep> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOE\\u0078c\\u0065ption";
	String testName = "<complete on superclass behind a unicode>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Invalid completion inside a unicode
 *
 * -- compute the unicode representation for a given string --
   [ String str = "IOEx";
	StringBuffer buffer = new StringBuffer("\"");
	for (int i = 0; i < str.length(); i++){
		String hex = Integer.toHexString(str.charAt(i));
		buffer.append("\\u0000".substring(0, 6-hex.length()));
		buffer.append(hex);
	}
	buffer.append("\"");
	buffer.toString()
	]
 */
public void test35() {

	String str =
		"import java.io.*;									\n" +
		"													\n" +
		"public class X extends IOE\\u0078ception {			\n" +
		"}													\n";

	String completeBehind = "IOE\\u00";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = NONE;
	String expectedUnitDisplayString = "";
	String expectedReplacedSource = NONE;
	String testName = "<complete inside unicode>";

	try {
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
		this.checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_UNICODE);
	}
}
/*
 * Invalid completion inside a comment
 *
 */
public void test36() {

	String str =
		"import java.io.*;									\n" +
		"													\n" +
		"public class X extends /*IOException*/ {			\n" +
		"}													\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = NONE;
	String expectedUnitDisplayString = "";
	String expectedReplacedSource = NONE;
	String testName = "<complete inside comment>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	try {
		this.checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_COMMENT);
	}
}
/*
 * Invalid completion inside a string literal
 *
 */
public void test37() {

	String str =
		"import java.io.*;									\n" +
		"													\n" +
		"public class X {									\n" +
		"	String s = \"IOException\";						\n" +
		"}													\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompletionOnString:\"IOEx\">";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X {\n" +
		"  String s = <CompletionOnString:\"IOEx\">;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "\"IOException\"";
	String testName = "<complete inside a string literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Invalid completion inside a number literal
 *
 */
public void test38() {

	String str =
		"import java.io.*;									\n" +
		"													\n" +
		"public class X {									\n" +
		"	int s = 12345678;								\n" +
		"}													\n";

	String completeBehind = "1234";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = NONE;
	String expectedUnitDisplayString = "";
	String expectedReplacedSource = NONE;
	String testName = "<complete inside a number literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	try{
		this.checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_NUMBER);
	}
}
/*
 * Completion in import statement at the end of the unit
 */
public void test39() {

	String str =
		"package x.abc;				\n"+
		"import x.util";

	String completeBehind = "x.util";
	String expectedCompletionNodeToString = "<CompleteOnImport:x.util>";
	String completionIdentifier = "util";
	String expectedUnitDisplayString =
		"package x.abc;\n" +
		"import <CompleteOnImport:x.util>;\n";
	String expectedReplacedSource = "x.util";
	String testName = "<complete in import statement at the end of the unit>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Completion in import statement at the end of the unit (qualified empty name)
 */
public void test40() {

	String str =
		"package a.b;			\n"+
		"import java.";

	String completeBehind = "java.";
	String expectedCompletionNodeToString = "<CompleteOnImport:java.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"package a.b;\n" +
		"import <CompleteOnImport:java.>;\n";
	String expectedReplacedSource = "java.";
	String testName = "<complete in import statement at the end of the unit>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Should not find any diet completion
 */
public void test41() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X extends 					\n" +
		"	int foo(String str)						\n" +
		"		String variable = ;					\n" +
		"		{									\n" +
		"		 	String variableNotInScope;		\n" +
		"		}									\n" +
		"		foo(varia							\n";

	String completeBehind = "foo(var";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo(String str) {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "varia";
	String testName = "<should not find diet completion>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on array type with prefix dimensions
 */
public void test42() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X {		 					\n" +
		"	int[] foo(String str)					\n";

	String completeBehind = "int";
	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String completionIdentifier = "int";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <CompleteOnType:int>\n" +
		"}\n";
	String expectedReplacedSource = "int";
	String testName = "<completion on array type with prefix dimensions>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on array type with postfix dimensions
 */
public void test43() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X {		 					\n" +
		"	int foo(String str)	[]					\n";

	String completeBehind = "int";
	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String completionIdentifier = "int";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <CompleteOnType:int>\n" +
		"}\n";
	String expectedReplacedSource = "int";
	String testName = "<completion on array type with postfix dimensions>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type behind other member
 */
public void test44() {

	String str =
		"class X {									\n" +
		"	int i;									\n" +
		"	IOEx									\n" +
		"}											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  int i;\n" +
		"  <CompleteOnType:IOEx>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type behind other member
 */
public void test45() {

	String str =
		"class X {									\n" +
		"	int i;									\n" +
		"	public IOEx								\n" +
		"}											\n";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  int i;\n" +
		"  <CompleteOnType:IOEx>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on name in field initializer
 */
public void test46() {

	String str =
		"class X {									\n" +
		"	String s = \"hello\";					\n" +
		"	int f = s.								\n" +
		"}											\n";

	String completeBehind = "= s";
	String expectedCompletionNodeToString = "<CompleteOnName:s>";
	String completionIdentifier = "s";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  String s;\n" +
		"  int f = <CompleteOnName:s>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "s";
	String testName = "<complete on name in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in field initializer in innner class
 */
public void test47() {

	String str =
		"class X {									\n" +
		"	class Y {								\n" +
		"		Object[] f = { this.foo }			\n" +
		"		Object foo(){ return this; }		\n" +
		"}											\n";

	String completeBehind = "this.foo";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.foo>";
	String completionIdentifier = "foo";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  class Y {\n" +
		"    Object[] f = {<CompleteOnMemberAccess:this.foo>};\n" +
		"    Y() {\n" +
		"    }\n" +
		"    Object foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "this.foo";
	String testName = "<complete in field initializer in inner class>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Should not find fake field of type <CompleteOnType:f>
 */
public void test48() {

	String str =
		"package pack;								\n"+
		"class A  {									\n"+
		"											\n"+
		"	public static void main(String[] argv)	\n"+
		"			new Member().f					\n"+
		"			;								\n"+
		"	}										\n"+
		"	class Member {							\n"+
		"		int foo()							\n"+
		"		}									\n"+
		"	}										\n"+
		"};											\n";

	String completeBehind = "new Member().f";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "f";
	String expectedUnitDisplayString =
		"package pack;\n" +
		"class A {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"    }\n" +
		"    int foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  A() {\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "f";
	String testName = "<should not find fake field of type f>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Completion in middle of package import statement
 */
public void test49() {

	String str =
		"import java.lang.reflect.*;	\n"+
		"class X {						\n"+
		"}								\n";

	String completeBehind = "java.la";
	String expectedCompletionNodeToString = "<CompleteOnImport:java.la>";
	String completionIdentifier = "la";
	String expectedUnitDisplayString =
		"import <CompleteOnImport:java.la>;\n" +
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "java.lang.reflect";
	String testName = "<complete in middle of package import statement>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on instance creation in field initializer.
 */
public void test50() {

	String str =
		"class X {									\n" +
		"	String s = \"hello\";					\n" +
		"	Object o = new Xyz();					\n" +
		"}											\n";

	String completeBehind = "new X";
	String expectedCompletionNodeToString = "<CompleteOnType:X>";
	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  String s;\n" +
		"  Object o = new <CompleteOnType:X>();\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "Xyz";
	String testName = "<complete on instance creation in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on member access in field initializer.
 */
public void test51() {

	String str =
		"class X {									\n" +
		"	String s = \"hello\";					\n" +
		"	Object o = fred().xyz;					\n" +
		"}											\n";

	String completeBehind = "fred().x";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:fred().x>";
	String completionIdentifier = "x";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  String s;\n" +
		"  Object o = <CompleteOnMemberAccess:fred().x>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "xyz";
	String testName = "<complete on member access in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on class literal access in field initializer.
 */
public void test52() {

	String str =
		"class X {									\n" +
		"	String s = \"hello\";					\n" +
		"	Class c = int[].class;					\n" +
		"}											\n";

	String completeBehind = "int[].c";
	String expectedCompletionNodeToString = "<CompleteOnClassLiteralAccess:int[].c>";
	String completionIdentifier = "c";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  String s;\n" +
		"  Class c = <CompleteOnClassLiteralAccess:int[].c>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "int[].class";
	String testName = "<complete on class literal access in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on method invocation in field initializer.
 */
public void test53() {

	String str =
		"class X {									\n" +
		"	String s = \"hello\";					\n" +
		"	Object o = s.concat();					\n" +
		"}											\n";

	String completeBehind = "s.concat(";
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:s.concat()>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  String s;\n" +
		"  Object o = <CompleteOnMessageSend:s.concat()>;\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "concat(";
	String testName = "<complete on method invocation in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Should not find fake field of type <CompleteOnType:f>
 */
public void test54() {

	String str =
		"package pack;								\n"+
		"class A  {									\n"+
		"											\n"+
		"	public static void main(String[] argv	\n"+
		"			new Member().f					\n"+
		"			;								\n"+
		"	}										\n"+
		"	class Member {							\n"+
		"		int foo()							\n"+
		"		}									\n"+
		"	}										\n"+
		"};											\n";

	String completeBehind = "new Member().f";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "f";
	String expectedUnitDisplayString =
		"package pack;\n" +
		"class A {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"    }\n" +
		"    int foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  A() {\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "f";
	String testName = "<should not find fake field of type f>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on anonymous type in field initializer.
 */
public void test55() {

	String str =
		"class X {									\n" +
		"	Object o = new Object(){				\n" +
		"		void foo(){							\n" +
		"			String x = \"\";				\n" +
		"			x.index							\n" +
		"		}									\n" +
		"											\n" +
		"		void bar(){							\n" +
		"			String y = \"\";				\n" +
		"		}									\n" +
		"	};					 					\n" +
		"}											\n";

	String completeBehind = "x.index";
	String expectedCompletionNodeToString = "<CompleteOnName:x.index>";
	String completionIdentifier = "index";
	String expectedUnitDisplayString =
		"class X {\n" +
		"  Object o = new Object() {\n" +
		"    void foo() {\n" +
		"      String x;\n" +
		"      <CompleteOnName:x.index>;\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"  };\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "x.index";
	String testName = "<complete on anonymous type in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
}
