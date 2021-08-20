/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

public class CompletionParserTest2 extends AbstractCompletionTest {
public CompletionParserTest2(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(CompletionParserTest2.class);
}
public void test0001(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = <CompleteOnName:zzz>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0002_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0002_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object o = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0003_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0003_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0004(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = <CompleteOnName:zzz>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0005_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0005_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o = <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0006_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0006_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0007(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new <CompleteOnType:zzz>();\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0008_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0008_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object o = new <CompleteOnType:zzz>();\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0009_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0009_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = new <CompleteOnType:zzz>();\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0010(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = new zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new <CompleteOnType:zzz>();\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0011_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0011_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o = new <CompleteOnType:zzz>();\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0012_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0012_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = new <CompleteOnType:zzz>();\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0013(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = yyy;\n" +
		"  zzz\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o;\n" +
		"  <CompleteOnType:zzz>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0014_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0014_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0015_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0015_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0016(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = yyy;\n" +
		"  zzz\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o;\n" +
		"  <CompleteOnType:zzz>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0017_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0017_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o;\n" +
			"      <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0018_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0018_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0019(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = bar(zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = bar(<CompleteOnName:zzz>);\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0020_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = bar(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0020_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = bar(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object o = bar(<CompleteOnName:zzz>);\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0021_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = bar(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0021_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = bar(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = bar(<CompleteOnName:zzz>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0022(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = bar(zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = bar(<CompleteOnName:zzz>);\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0023_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = bar(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0023_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = bar(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o = bar(<CompleteOnName:zzz>);\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0024_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = bar(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0024_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = bar(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = bar(<CompleteOnName:zzz>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0025(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X(zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X(<CompleteOnName:zzz>);\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0026_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = new X(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0026_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = new X(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object o = new X(<CompleteOnName:zzz>);\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0027_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = new X(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0027_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = new X(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = new X(<CompleteOnName:zzz>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0028(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = new X(zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X(<CompleteOnName:zzz>);\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0029_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = new X(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0029_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = new X(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o = new X(<CompleteOnName:zzz>);\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0030_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = new X(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0030_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = new X(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = new X(<CompleteOnName:zzz>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0031_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = {zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0031_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = {zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  Object o;\n" +
			"  {\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0032_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0032_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object o;\n" +
			"    {\n" +
			"      <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0033_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0033_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    {\n" +
			"      <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0034_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = {zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0034_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = {zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  Object o;\n" +
			"  {\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0035_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0035_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o;\n" +
			"      {\n" +
			"        <CompleteOnName:zzz>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0036_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0036_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    {\n" +
			"      <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0037(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = {zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = {<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0038_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object[] o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0038_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object[] o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object[] o = {<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0039_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object[] o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0039_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object[] o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = {<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0040(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object[] o = {zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = {<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0041_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object[] o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0041_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object[] o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object[] o = {<CompleteOnName:zzz>};\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0042_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object[] o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0042_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object[] o = {zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = {<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0043(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = new X[zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = new X[<CompleteOnName:zzz>];\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}



public void test0044_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object[] o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0044_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object[] o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object[] o = new X[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}



public void test0045_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object[] o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0045_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object[] o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = new X[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}



public void test0046(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object[] o = new X[zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = new X[<CompleteOnName:zzz>];\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}



public void test0047_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object[] o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0047_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object[] o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object[] o = new X[<CompleteOnName:zzz>];\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}



public void test0048_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object[] o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0048_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object[] o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = new X[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0049(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = new X[]{<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0050_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object[] o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0050_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object[] o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object[] o = new X[]{<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0051_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object[] o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>};\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0051_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object[] o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = new X[]{<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0052(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object[] o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = new X[]{<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0053_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object[] o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0053_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object[] o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object[] o = new X[]{<CompleteOnName:zzz>};\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0054_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object[] o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>};\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0054_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object[] o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = new X[]{<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0055(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = <CompleteOnName:zzz>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0056_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object[] o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0056_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object[] o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object[] o = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0057_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object[] o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0057_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object[] o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0058(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object[] o = zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object[] o = <CompleteOnName:zzz>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0059_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object[] o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0059_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object[] o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object[] o = <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0060_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object[] o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0060_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object[] o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0061(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X[zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X[<CompleteOnName:zzz>];\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0062_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0062_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object o = new X[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0063_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0063_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = new X[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0064(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = new X[zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X[<CompleteOnName:zzz>];\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0065_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0065_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o = new X[<CompleteOnName:zzz>];\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0066_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0066_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = new X[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = new X[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0067_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X[]{<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0067_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  Object o = new X[]{<CompleteOnName:zzz>};\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0068_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0068_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object o = new X[]{<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0069_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>};\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0069_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = new X[]{<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0070_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = new X[]{<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0070_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  Object o = new X[]{<CompleteOnName:zzz>};\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0071_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0071_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o;\n" +
			"      {\n" +
			"        <CompleteOnName:zzz>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0072_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>};\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0072_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    Object o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    {\n" +
			"      <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0073(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  int o = new int[zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  int o = new int[<CompleteOnName:zzz>];\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0074_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    int o = new int[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0074_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    int o = new int[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    int o = new int[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0075_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int o = new int[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0075_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int o = new int[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int o = new int[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0076(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  int o = new int[zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  int o = new int[<CompleteOnName:zzz>];\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0077_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    int o = new int[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0077_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    int o = new int[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      int o = new int[<CompleteOnName:zzz>];\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0078_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    int o = new int[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0078_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    int o = new int[zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int o = new int[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0079_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  int o = new int[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  int o = new int[]{<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0079_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  int o = new int[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  int o = new int[]{<CompleteOnName:zzz>};\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0080_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    int o = new int[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0080_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    int o = new int[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    int o = new int[]{<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0081_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int o = new int[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      new int[]{<CompleteOnName:zzz>};\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0081_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int o = new int[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int o = new int[]{<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0082_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  int o = new int[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  int o = new int[]{<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0082_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  int o = new int[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  int o = new int[]{<CompleteOnName:zzz>};\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0083_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    int o = new int[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0083_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    int o = new int[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      int o;\n" +
			"      {\n" +
			"        <CompleteOnName:zzz>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0084_Diet(){


	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    int o = new int[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      new int[]{<CompleteOnName:zzz>};\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0084_Method(){


	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    int o = new int[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int o;\n" +
			"    {\n" +
			"      <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0085_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  X o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  X o = new X[]{<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0085_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  X o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  X o = new X[]{<CompleteOnName:zzz>};\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0086_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    X o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0086_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    X o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    X o = new X[]{<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0087_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    X o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>};\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0087_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    X o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    X o = new X[]{<CompleteOnName:zzz>};\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0088_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  X o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  X o = new X[]{<CompleteOnName:zzz>};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0088_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  X o = new X[]{zzz;\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  X o = new X[]{<CompleteOnName:zzz>};\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0089_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    X o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0089_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    X o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      X o;\n" +
			"      {\n" +
			"        <CompleteOnName:zzz>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0090_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    X o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>};\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0090_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    X o = new X[]{zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    X o;\n" +
			"    {\n" +
			"      <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0091(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  Object o = \"yyy;\n" +
		"  zzz\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o;\n" +
		"  <CompleteOnType:zzz>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0092_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0092_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    Object o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0093_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0093_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0094(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  #\n" +
		"  Object o = \"yyy;\n" +
		"  zzz\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  Object o;\n" +
		"  <CompleteOnType:zzz>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0095_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0095_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o;\n" +
			"      <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0096_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0096_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"    #\n" +
		"    Object o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  {\n" +
			"    {\n" +
			"      Object o;\n" +
			"      <CompleteOnName:zzz>;\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0097_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0097_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "o = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    o = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0098_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0098_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "o = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    o = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0099_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0099_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "o = new <CompleteOnType:zzz>()";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    o = new <CompleteOnType:zzz>();\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0100_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0100_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = new zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "o = new <CompleteOnType:zzz>()";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    o = new <CompleteOnType:zzz>();\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0101_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0101_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0102_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0102_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0103_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0103_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0104_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0104_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object o;\n" +
		"    o = \"yyy;\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0105_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = 1 + zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0105_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = 1 + zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(1 + <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int x = (1 + <CompleteOnName:zzz>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0106_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = 1 + (zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0106_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = 1 + (zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(1 + <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int x = (1 + <CompleteOnName:zzz>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0107_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = 0;\n" +
		"    int y = 1 + x;\n" +
		"    zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0107_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = 0;\n" +
		"    int y = 1 + x;\n" +
		"    zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int x;\n" +
			"    int y;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0108_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = -zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0108_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = -zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(- <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int x = (- <CompleteOnName:zzz>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0109_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = -(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0109_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = -(zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(- <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int x = (- <CompleteOnName:zzz>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0110_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = 0;\n" +
		"    int y = -x;\n" +
		"    zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0110_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int x = 0;\n" +
		"    int y = -x;\n" +
		"    zzz;\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int x;\n" +
			"    int y;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0111_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    if(1 == zzz) {}\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0111_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    if(1 == zzz) {}\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(1 == <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    if ((1 == <CompleteOnName:zzz>))\n" +
			"        {\n" +
			"        }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0112_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    if(1 == (zzz)) {}\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0112_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    if(1 == (zzz)) {}\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(1 == <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    if ((1 == <CompleteOnName:zzz>))\n" +
			"        {\n" +
			"        }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0113_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(Object x){\n" +
		"    if(x instanceof ZZZ) {}\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(Object x) {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0113_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(Object x){\n" +
		"    if(x instanceof ZZZ) {}\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "(x instanceof <CompleteOnType:ZZZ>)";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo(Object x) {\n" +
			"    if ((x instanceof <CompleteOnType:ZZZ>))\n" +
			"        {\n" +
			"        }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0114_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a == b ? zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0114_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a == b ? zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "c = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    boolean a;\n" +
			"    boolean b;\n" +
			"    boolean c;\n" +
			"    c = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0115_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b;\n" +
		"    a == b ? zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0115_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b;\n" +
		"    a == b ? zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    boolean a;\n" +
			"    boolean b;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0116_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a == b ? a : zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0116_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a == b ? a : zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "c = ((a == b) ? a : <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    boolean a;\n" +
			"    boolean b;\n" +
			"    boolean c;\n" +
			"    c = ((a == b) ? a : <CompleteOnName:zzz>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0117_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a == b ? a : (zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0117_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a == b ? a : (zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "c = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    boolean a;\n" +
			"    boolean b;\n" +
			"    boolean c;\n" +
			"    c = <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0118_Diet(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a# == b ? a : zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0118_Method(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a# == b ? a : zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    boolean a;\n" +
			"    boolean b;\n" +
			"    boolean c;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0119_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    switch(1) {\n" +
		"      case zzz\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0119_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    switch(1) {\n" +
		"      case zzz\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString =
			"switch (1) {\n" +
			"case <CompleteOnName:zzz> :\n" +
			"}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    {\n" +
			"      switch (1) {\n" +
			"      case <CompleteOnName:zzz> :\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0120_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    switch(1) {\n" +
		"      case Something :\n" +
		"      case zzz\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0120_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    switch(1) {\n" +
		"      case Something :\n" +
		"      case zzz\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString =
			"switch (1) {\n" +
			"case Something :\n" +
			"case <CompleteOnName:zzz> :\n" +
			"}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    {\n" +
			"      switch (1) {\n" +
			"      case Something :\n" +
			"      case <CompleteOnName:zzz> :\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0121_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    tab[zzz]\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0121_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    tab[zzz]\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "tab[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    tab[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0122_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    tab[].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0122_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    tab[].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnClassLiteralAccess:tab[].zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "tab[].zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnClassLiteralAccess:tab[].zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0123_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    tab[0].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0123_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    tab[0].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:tab[0].zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnMemberAccess:tab[0].zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0124_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    foo()[zzz]\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0124_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    foo()[zzz]\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "foo()[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    foo()[<CompleteOnName:zzz>];\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0125_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    foo()[].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0125_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    foo()[].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0126_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    foo()[1].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0126_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    foo()[1].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:foo()[1].zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnMemberAccess:foo()[1].zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0127(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    if (zzz() == null) bar = null;\n" +
		"  }\n" +
		"  Object o = new O();\n" +
		"}\n";

	String completeBehind = "O";
	int cursorLocation = str.lastIndexOf("O") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:O>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:O>();";
	String completionIdentifier = "O";
	String expectedReplacedSource = "O";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Object o = new <CompleteOnType:O>();\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0128_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    new Object() {\n" +
		"      void bar() {\n" +
		"        a[zzz\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0128_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    new Object() {\n" +
		"      void bar() {\n" +
		"        a[zzz\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "a[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    new Object() {\n" +
			"      void bar() {\n" +
			"        a[<CompleteOnName:zzz>];\n" +
			"      }\n" +
			"    };\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0129_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object local;\n" +
		"    double bar;\n" +
		"    for(;;) {\n" +
		"      bar = (double)0;\n" +
		"    }\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0129_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object local;\n" +
		"    double bar;\n" +
		"    for(;;) {\n" +
		"      bar = (double)0;\n" +
		"    }\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object local;\n" +
			"    double bar;\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42856
 */
public void test0130_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    A.B c = null;\n" +
		"    zzz();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42856
 */
public void test0130_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    A.B c = null;\n" +
		"    zzz();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnMessageSend:zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "zzz(";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    A.B c;\n" +
			"    <CompleteOnMessageSend:zzz()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42856
 */
public void test0131_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    try {\n" +
		"    } catch(A.B e) {\n" +
		"      zzz();\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42856
 */
public void test0131_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    try {\n" +
		"    } catch(A.B e) {\n" +
		"      zzz();\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnMessageSend:zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "zzz(";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    {\n" +
			"      A.B e;\n" +
			"      <CompleteOnMessageSend:zzz()>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44647
 */
public void test0132_Diet(){
	String str =
		"public class A\n" +
		"{\n" +
		"   public A(final String str1, final String str2)\n" +
		"   {\n" +
		"      \n" +
		"   }\n" +
		"   \n" +
		"   private A[] methodA(final String str1, final String str2)\n" +
		"      {\n" +
		"         return new A[]\n" +
		"         {\n" +
		"            new A(str1, str2)\n" +
		"            {\n" +
		"               //initialiser!\n" +
		"               {\n" +
		"                  methodA(\"1\", \"2\");\n" +
		"               }\n" +
		"            },\n" +
		"            new A(\"hello\".c) //<--------code complete to \"hello\".concat()\n" +
		"         };\n" +
		"      \n" +
		"      }\n" +
		"}\n";


	String completeBehind = "\"2\");";
	int cursorLocation = str.indexOf("\"2\");") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class A {\n" +
		"  public A(final String str1, final String str2) {\n" +
		"  }\n" +
		"  private A[] methodA(final String str1, final String str2) {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44647
 */
public void test0132_Method(){
	String str =
		"public class A\n" +
		"{\n" +
		"   public A(final String str1, final String str2)\n" +
		"   {\n" +
		"      \n" +
		"   }\n" +
		"   \n" +
		"   private A[] methodA(final String str1, final String str2)\n" +
		"      {\n" +
		"         return new A[]\n" +
		"         {\n" +
		"            new A(str1, str2)\n" +
		"            {\n" +
		"               //initialiser!\n" +
		"               {\n" +
		"                  methodA(\"1\", \"2\");\n" +
		"               }\n" +
		"            },\n" +
		"            new A(\"hello\".c) //<--------code complete to \"hello\".concat()\n" +
		"         };\n" +
		"      \n" +
		"      }\n" +
		"}\n";


	String completeBehind = "\"2\");";
	int cursorLocation = str.indexOf("\"2\");") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String expectedParentNodeToString =
			"new A(str1, str2) {\n" +
			"  {\n" +
			"    <CompleteOnName:>;\n" +
			"  }\n" +
			"}";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class A {\n" +
			"  public A(final String str1, final String str2) {\n" +
			"  }\n" +
			"  private A[] methodA(final String str1, final String str2) {\n" +
			"    new A(str1, str2) {\n" +
			"      {\n" +
			"        <CompleteOnName:>;\n" +
			"      }\n" +
			"    };\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46470
 */
public void test0133_Diet(){
	String str =
	"public class X {\n" +
	"   int x;\n" +
	"   void foo() {\n" +
	"      switch(x){\n" +
	"         case 0:\n" +
	"            break;\n" +
	"      }\n" +
	"      bar\n" +
	"   }\n" +
	"}\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  int x;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46470
 */
public void test0133_Method(){
	String str =
	"public class X {\n" +
	"   int x;\n" +
	"   void foo() {\n" +
	"      switch(x){\n" +
	"         case 0:\n" +
	"            break;\n" +
	"      }\n" +
	"      bar\n" +
	"   }\n" +
	"}\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  int x;\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43212
 */
public void test0134(){
	String str =
	"public class X {\n" +
	"	Object o = new Object() {\n" +
	"		void foo() {\n" +
	"			try {\n" +
	"			} catch(Exception e) {\n" +
	"				e.\n" +
	"			}\n" +
	"		}\n" +
	"	};\n" +
	"}\n";


	String completeBehind = "e.";
	int cursorLocation = str.indexOf("e.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:e.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "e.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Object o = new Object() {\n" +
		"    void foo() {\n" +
		"      {\n" +
		"        Exception e;\n" +
		"        <CompleteOnName:e.>;\n" +
		"      }\n" +
		"    }\n" +
		"  };\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43212
 */
public void test0135_Diet(){
	String str =
	"public class X {\n" +
	"	void bar(){\n" +
	"		#\n" +
	"		class Inner {\n" +
	"			void foo() {\n" +
	"				try {\n" +
	"				} catch(Exception e) {\n" +
	"					e.\n" +
	"				}\n" +
	"			}\n" +
	"		}\n" +
	"	}\n" +
	"}\n";


	String completeBehind = "e.";
	int cursorLocation = str.indexOf("e.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43212
 */
public void test0135_Method(){
	String str =
	"public class X {\n" +
	"	void bar(){\n" +
	"		#\n" +
	"		class Inner {\n" +
	"			void foo() {\n" +
	"				try {\n" +
	"				} catch(Exception e) {\n" +
	"					e.\n" +
	"				}\n" +
	"			}\n" +
	"		}\n" +
	"	}\n" +
	"}\n";


	String completeBehind = "e.";
	int cursorLocation = str.indexOf("e.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:e.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "e.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    class Inner {\n" +
			"      Inner() {\n" +
			"      }\n" +
			"      void foo() {\n" +
			"        {\n" +
			"          Exception e;\n" +
			"          <CompleteOnName:e.>;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48070
 */
public void test0136(){
	String str =
		"public class X {\n" +
		"	void bar(){\n" +
		"	}\n" +
		"}\n";


	String completeBehind = "ba";
	int cursorLocation = str.indexOf("ba") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompletionOnMethodName:void ba()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ba";
	String expectedReplacedSource = "bar()";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <CompletionOnMethodName:void ba()>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=53624
 */
public void test0137_Diet(){
	String str =
		"public class X {\n" +
		"	void foo(){\n" +
		"		new Object(){\n" +
		"			void bar(){\n" +
		"				super.zzz();\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"}\n";


	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=53624
 */
public void test0137_Method(){
	String str =
		"public class X {\n" +
		"	void foo(){\n" +
		"		new Object(){\n" +
		"			void bar(){\n" +
		"				super.zzz();\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"}\n";


	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:super.zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "zzz()";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    new Object() {\n" +
			"      void bar() {\n" +
			"        <CompleteOnMessageSend:super.zzz()>;\n" +
			"      }\n" +
			"    };\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=41395
 */
public void test0138_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    new Y() {\n" +
		"      public void bar() {\n" +
		"        switch (zzz){\n" +
		"          case 1 :\n" +
    	"          };\n" +
		"        }\n" +
		"        new Z() {\n" +
		"          public void toto() {		\n" +
		"        }\n" +
		"      });\n" +
		"    });\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "to";
	int cursorLocation = str.indexOf("to") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=41395
 */
public void test0138_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    new Y() {\n" +
		"      public void bar() {\n" +
		"        switch (zzz){\n" +
		"          case 1 :\n" +
    	"          };\n" +
		"        }\n" +
		"        new Z() {\n" +
		"          public void toto() {		\n" +
		"        }\n" +
		"      });\n" +
		"    });\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "to";
	int cursorLocation = str.indexOf("to") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnFieldName:void to>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "to";
	String expectedReplacedSource = "toto";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    new Y() {\n" +
			"      public void bar() {\n" +
			"        new Z() {\n" +
			"          <CompleteOnFieldName:void to>;\n" +
			"          {\n" +
			"          }\n" +
			"        };\n" +
			"      }\n" +
			"    };\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0139(){
	String str =
		"public class X  extends Z. #  {\n" +
		"}";


	String completeBehind = "Z.";
	int cursorLocation = str.indexOf("Z.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClass:Z.>";
	String expectedParentNodeToString =
		"public class X extends <CompleteOnClass:Z.> {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}";
	String completionIdentifier = "";
	String expectedReplacedSource = "Z.";
	String expectedUnitDisplayString =
		"public class X extends <CompleteOnClass:Z.> {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n"
		;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=32061
 */
public void test0140(){
	String str =
		"public class X  {\n" +
		"    public void baz() {\n" +
		"    	new Object() {\n" +
		"            public void bar() {\n" +
		"            }\n" +
		"        };\n" +
		"    }\n" +
		"    private Object var = new Object() {\n" +
		"        public void foo(Object e) {\n" +
		"           e.\n" +
		"        }\n" +
		"    };\n" +
		"}";


	String completeBehind = "e.";
	int cursorLocation = str.indexOf("e.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:e.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "e.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  private Object var = new Object() {\n" +
		"    public void foo(Object e) {\n" +
		"      <CompleteOnName:e.>;\n" +
		"    }\n" +
		"  };\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void baz() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=32061
 */
public void test0141(){
	String str =
		"public class X  {\n" +
		"    Object var1 = new Object() {};\n" +
		"    void bar() {\n" +
		"        new Object() {};\n" +
		"        bar();\n" +
		"    }\n" +
		"    Object var2 = new \n" +
		"}";


	String completeBehind = "var2 = new ";
	int cursorLocation = str.indexOf("var2 = new ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "Object var2 = new <CompleteOnType:>();";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Object var1;\n" +
		"  Object var2 = new <CompleteOnType:>();\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=39499
 */
public void test0142_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    bar(new Object(){\n" +
		"      public void toto() {\n" +
		"        if(a instanceof Object) {}\n" +
		"      }\n" +
    	"    });\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "instanceof";
	int cursorLocation = str.indexOf("instanceof") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=39499
 */
public void test0142_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    bar(new Object(){\n" +
		"      public void toto() {\n" +
		"        if(a instanceof Object) {}\n" +
		"      }\n" +
    	"    });\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "instanceof";
	int cursorLocation = str.indexOf("instanceof") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:instanceof>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "instanceof";
	String expectedReplacedSource = "instanceof";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    new Object() {\n" +
			"      public void toto() {\n" +
			"        <CompleteOnKeyword:instanceof>;\n" +
			"      }\n" +
			"    };\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0143_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(int) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0143_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(int) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0144_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(int[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0144_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(int[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0145_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(X) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0145_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(X) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0146_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(X[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0146_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(X[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72352
 */
public void test0147(){
	String str =
		"public class Test {\n" +
		"  Object m;\n" +
		"  String[] values = (String[]) m;\n" +
		"  lo\n" +
		"  }";

	String completeBehind = "lo";
	int cursorLocation = str.indexOf("lo") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:lo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lo";
	String expectedReplacedSource = "lo";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  Object m;\n" +
		"  String[] values;\n" +
		"  <CompleteOnType:lo>;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83236
 */
public void test0148(){
	String str =
		"public class Test {\n" +
		"  Boolean\n" +
		"   * some text <b>bold<i>both</i></b>\n" +
		"   */\n" +
		"  public void foo(String s) {\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "Boolean";
	int cursorLocation = str.indexOf("Boolean") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Boolean>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Boolean";
	String expectedReplacedSource = "Boolean";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  <CompleteOnType:Boolean>;\n" +
		"  some text;\n" +
		"  bold<i> both;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public void foo(String s) {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91371
 */
public void test0149_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    new Object(){\n" +
		"      void bar(){\n" +
		"        if((titi & (ZZ\n" +
		"}\n" +
		"\n";


	String completeBehind = "ZZ";
	int cursorLocation = str.indexOf("ZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91371
 */
public void test0149_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    new Object(){\n" +
		"      void bar(){\n" +
		"        if((titi & (ZZ\n" +
		"}\n" +
		"\n";


	String completeBehind = "ZZ";
	int cursorLocation = str.indexOf("ZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "(titi & <CompleteOnName:ZZ>)";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    new Object() {\n" +
			"      void bar() {\n" +
			"        (titi & <CompleteOnName:ZZ>);\n" +
			"      }\n" +
			"    };\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91371
 */
public void test0150_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    if((titi & (ZZ\n" +
		"}\n" +
		"\n";


	String completeBehind = "ZZ";
	int cursorLocation = str.indexOf("ZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91371
 */
public void test0150_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    if((titi & (ZZ\n" +
		"}\n" +
		"\n";


	String completeBehind = "ZZ";
	int cursorLocation = str.indexOf("ZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "(titi & <CompleteOnName:ZZ>)";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    (titi & <CompleteOnName:ZZ>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=92451
 */
public void test0151_Diet(){
	String str =
		"public class X {\n" +
		"  public static void main(String[] args) {\n" +
 		"    java.util.List elements = null;\n" +
 		"    new Test(Test.toStrings((Test[])elements.toArray(new Test[0])));\n" +
		"     //code assist fails on this line\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "";
	int cursorLocation = str.indexOf(" //code assis") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=92451
 */
public void test0151_Method(){
	String str =
		"public class X {\n" +
		"  public static void main(String[] args) {\n" +
 		"    java.util.List elements = null;\n" +
 		"    new Test(Test.toStrings((Test[])elements.toArray(new Test[0])));\n" +
		"     //code assist fails on this line\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "";
	int cursorLocation = str.indexOf(" //code assis") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    java.util.List elements;\n" +
			"    <CompleteOnName:>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=98115
 */
public void test0152(){
	String str =
		"public class X {\n" +
		"  Object var = new Object() {\n" +
 		"    void bar() {\n" +
 		"      int i = 0;\n" +
		"    }\n" +
		"    void foo() {\n" +
 		"      zzz\n" +
		"    }\n" +
		"  };\n" +
		"}\n";


	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Object var = new Object() {\n" +
		"    void bar() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  };\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0153_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
 		"    label1 : for(;;) {\n" +
 		"      break lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0153_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
 		"    label1 : for(;;) {\n" +
 		"      break lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "break <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    break <CompleteOnLabel:lab>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0154_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
 		"    label1 : for(;;) {\n" +
 		"      break lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0154_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
 		"    label1 : for(;;) {\n" +
 		"      break lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "break <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    {\n" +
			"      break <CompleteOnLabel:lab>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0155_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
 		"    label1 : for(;;) {\n" +
 		"      continue lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0155_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
 		"    label1 : for(;;) {\n" +
 		"      continue lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "continue <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    continue <CompleteOnLabel:lab>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0156_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
 		"    label1 : for(;;) {\n" +
 		"      continue lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0156_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
 		"    label1 : for(;;) {\n" +
 		"      continue lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "continue <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    {\n" +
			"      continue <CompleteOnLabel:lab>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0157_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
 		"    label1 : for(;;) {\n" +
 		"      class X {\n" +
 		"        void foo() {\n" +
 		"          label2 : for(;;) foo();\n" +
 		"        }\n" +
 		"      }\n" +
 		"      continue lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0157_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
 		"    label1 : for(;;) {\n" +
 		"      class X {\n" +
 		"        void foo() {\n" +
 		"          label2 : for(;;) foo();\n" +
 		"        }\n" +
 		"      }\n" +
 		"      continue lab\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "continue <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    {\n" +
			"      class X {\n" +
			"        X() {\n" +
			"          super();\n" +
			"        }\n" +
			"        void foo() {\n" +
			"        }\n" +
			"      }\n" +
			"      continue <CompleteOnLabel:lab>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0158_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
 		"    label1 : for(;;) {\n" +
 		"      class X {\n" +
 		"        void foo() {\n" +
 		"          label2 : for(;;) {\n" +
 		"            continue lab\n" +
 		"          }\n" +
 		"        }\n" +
 		"      }\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0158_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
 		"    label1 : for(;;) {\n" +
 		"      class X {\n" +
 		"        void foo() {\n" +
 		"          label2 : for(;;) {\n" +
 		"            continue lab\n" +
 		"          }\n" +
 		"        }\n" +
 		"      }\n" +
 		"    }\n" +
		"  }\n" +
		"}\n";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "continue <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    {\n" +
			"      class X {\n" +
			"        X() {\n" +
			"        }\n" +
			"        void foo() {\n" +
			"          {\n" +
			"            continue <CompleteOnLabel:lab>;\n" +
			"          }\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0159() {

	String str =
		"public class X {\n" +
		"	String s = \"ZZZZZ\";\n" +
		"}\n";

	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  String s = <CompletionOnString:\"ZZZ\">;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "\"ZZZZZ\"";
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
public void test0160() {

	String str =
		"public class X {\n" +
		"	String s = \\u0022ZZ\\u005AZZ\\u0022;\n" +
		"}\n";

	String completeBehind = "ZZ\\u005A";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  String s = <CompletionOnString:\"ZZZ\">;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "\\u0022ZZ\\u005AZZ\\u0022";
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
public void test0161() {

	String str =
		"public class X {\n" +
		"	String s = \"AAAAA\" + \"ZZZZZ\";\n" +
		"}\n";

	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  String s = (\"AAAAA\" + <CompletionOnString:\"ZZZ\">);\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "\"ZZZZZ\"";
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
public void test0162() {

	String str =
		"public class X {\n" +
		"	String s = \"ZZZZZ\n" +
		"}\n";

	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  String s = <CompletionOnString:\"ZZZ\">;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "\"ZZZZZ";
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
public void test0163() {

	String str =
		"public class X {\n" +
		"	String s = \"ZZZZZ";

	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  String s = <CompletionOnString:\"ZZZ\">;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "\"ZZZZZ";
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
public void test0164() {

	String str =
		"public class X {\n" +
		"	String s = \"\\u005AZZZZ\\u000D\\u0022" +
		"}\n";

	String completeBehind = "\\u005AZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  String s = <CompletionOnString:\"ZZZ\">;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "\"\\u005AZZZZ";
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122755
public void test0165_Diet() {

	String str =
		"public class X {\n" +
		"	void foo() {" +
		"	/**" +
		"	 *" +
		"	 */." +
		"	}" +
		"}\n";

	String completeBehind = "/.";
	int cursorLocation = str.lastIndexOf("/.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122755
public void test0165_Method() {

	String str =
		"public class X {\n" +
		"	void foo() {" +
		"	/**" +
		"	 *" +
		"	 */." +
		"	}" +
		"}\n";

	String completeBehind = "/.";
	int cursorLocation = str.lastIndexOf("/.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnName:>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=137623
public void test0166_Diet() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      if(this.equals(null))\n" +
		"      {\n" +
		"         (zzz==int.\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "int.";
	int cursorLocation = str.lastIndexOf("int.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public boolean foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=137623
public void test0166_Method() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      if(this.equals(null))\n" +
		"      {\n" +
		"         (zzz==int.\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "int.";
	int cursorLocation = str.lastIndexOf("int.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClassLiteralAccess:int.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "int.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public boolean foo() {\n" +
			"    <CompleteOnClassLiteralAccess:int.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0167_Diet() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar();\n" +
		"      }\n" +
		"      catch (IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public boolean foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0167_Method() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar();\n" +
		"      }\n" +
		"      catch (IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString =
			"try\n" +
			"  {\n" +
			"    throwing();\n" +
			"  }\n" +
			"catch (IllegalAccessException e)\n" +
			"  {\n" +
			"  }\n" +
			"catch (<CompleteOnException:IZZ>  )\n" +
			"  {\n"+
			"  }";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public boolean foo() {\n" +
			"    try\n" +
			"      {\n" +
			"        throwing();\n" +
			"      }\n" +
			"    catch (IllegalAccessException e)\n" +
			"      {\n" +
			"      }\n" +
			"    catch (<CompleteOnException:IZZ>  )\n" +
			"      {\n" +
			"      }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0168_Diet() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar();\n" +
		"      }\n" +
		"      catch (IZZ\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public boolean foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0168_Method() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar();\n" +
		"      }\n" +
		"      catch (IZZ\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString =
			"try\n" +
			"  {\n" +
			"    throwing();\n" +
			"  }\n" +
			"catch (IllegalAccessException e)\n" +
			"  {\n" +
			"  }\n" +
			"catch (<CompleteOnException:IZZ>  )\n" +
			"  {\n"+
			"  }";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public boolean foo() {\n" +
			"    try\n" +
			"      {\n" +
			"        throwing();\n" +
			"      }\n" +
			"    catch (IllegalAccessException e)\n" +
			"      {\n" +
			"      }\n" +
			"    catch (<CompleteOnException:IZZ>  )\n" +
			"      {\n" +
			"      }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0169_Diet() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar()\n" +
		"      }\n" +
		"      catch (IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public boolean foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0169_Method() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar()\n" +
		"      }\n" +
		"      catch (IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public boolean foo() {\n" +
			"    <CompleteOnException:IZZ>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0170_Diet() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      #\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar();\n" +
		"      }\n" +
		"      catch (IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public boolean foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0170_Method() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      #\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar();\n" +
		"      }\n" +
		"      catch (IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public boolean foo() {\n" +
			"    <CompleteOnException:IZZ>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0171_Diet() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public boolean foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0171_Method() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString =
			"try\n" +
			"  {\n" +
			"    throwing();\n" +
			"  }\n" +
			"catch (<CompleteOnException:IZZ>  )\n" +
			"  {\n"+
			"  }"
	;
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public boolean foo() {\n" +
			"    try\n" +
			"      {\n" +
			"        throwing();\n" +
			"      }\n" +
			"    catch (<CompleteOnException:IZZ>  )\n" +
			"      {\n"+
			"      }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150632
public void test0172() {

	String str =
		"abstract class MatchFilter {\n"+
		"    private static final String SETTINGS_LAST_USED_FILTERS= \"filters_last_used\"; \n"+
		"\n"+
		"    // works if next line is commented out or moved to after PUBLIC_FILTER\n"+
		"    public abstract String getName();\n"+
		"\n"+
		"    // content assist at new ModifierFilter(|):\n"+
		"    private static final MatchFilter PUBLIC_FILTER= new ModifierFilter();\n"+
		"}\n"+
		"\n"+
		"class ModifierFilter extends MatchFilter {\n"+
		"   private final String fName;\n"+
		"    public ModifierFilter(String name) {\n"+
		"        fName= name;\n"+
		"    }\n"+
		"   public String getName() {\n"+
		"        return fName;\n"+
		"    }\n"+
		"}\n";

	String completeBehind = "new ModifierFilter(";
	int cursorLocation = str.lastIndexOf("new ModifierFilter(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new ModifierFilter()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"abstract class MatchFilter {\n" +
		"  private static final String SETTINGS_LAST_USED_FILTERS;\n" +
		"  private static final MatchFilter PUBLIC_FILTER = <CompleteOnAllocationExpression:new ModifierFilter()>;\n" +
		"  MatchFilter() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public abstract String getName();\n" +
		"}\n" +
		"class ModifierFilter extends MatchFilter {\n" +
		"  private final String fName;\n" +
		"  public ModifierFilter(String name) {\n" +
		"  }\n" +
		"  public String getName() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6004
public void test0173_Diet() {

	String str =
		"public class Y {\n" +
		"\n" +
		"	int foo(){\n" +
		"	\n" +
		"	\n" +
		"	int bar() {	\n" +
		"	\n" +
		"	public int x = new Object(;\n" +
		"	/*<CODE ASSIST>*/\n" +
		"	}\n" +
		"	}\n" +
		"}\n" +
		"}\n";

	String completeBehind = "";
	int cursorLocation = str.lastIndexOf("/*<CODE ASSIST>*/") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"public class Y {\n" +
		"  public int x;\n" +
		"  <CompleteOnType:>;\n" +
		"  public Y() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6004
public void test0173_Method() {

	String str =
		"public class Y {\n" +
		"\n" +
		"	int foo(){\n" +
		"	\n" +
		"	\n" +
		"	int bar() {	\n" +
		"	\n" +
		"	public int x = new Object(;\n" +
		"	/*<CODE ASSIST>*/\n" +
		"	}\n" +
		"	}\n" +
		"}\n" +
		"}\n";

	String completeBehind = "";
	int cursorLocation = str.lastIndexOf("/*<CODE ASSIST>*/") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class Y {\n" +
			"  public int x;\n" +
			"  <CompleteOnType:>;\n" +
			"  public Y() {\n" +
			"  }\n" +
			"  int foo() {\n" +
			"  }\n" +
			"  int bar() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0174_Diet() {

	String str =
		"public class X {\n" +
		"	# ; ZZZ\n" +
		"}\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0175_Diet() {

	String str =
		"public class X {\n" +
		"	int # ZZZ\n" +
		"}\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0176_Diet() {

	String str =
		"public class X {\n" +
		"	# int i; ZZZ\n" +
		"}\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  int i;\n" +
		"  <CompleteOnType:ZZZ>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0177_Diet() {

	String str =
		"public class X {\n" +
		"	# void foo() {} ZZZ\n" +
		"}\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:ZZZ>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=229927
public void test0178_Method() {

	String str =
		"package p;\n"+
		"\n"+
		"public class P {\n"+
		"        private void foo(String key){\n"+
		"                if (key != null) {\n"+
		"                        String[] keys= { k };\n"+
		"                }\n"+
		"        }\n"+
		"}\n";

	String completeBehind = "k";
	int cursorLocation = str.lastIndexOf("k") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:k>";
	String expectedParentNodeToString = "String[] keys = {<CompleteOnName:k>};";
	String completionIdentifier = "k";
	String expectedReplacedSource = "k";
	String expectedUnitDisplayString =
			"package p;\n" +
			"public class P {\n" +
			"  public P() {\n" +
			"  }\n" +
			"  private void foo(String key) {\n" +
			"    if ((key != null))\n" +
			"        {\n" +
			"          String[] keys = {<CompleteOnName:k>};\n" +
			"        }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
}
