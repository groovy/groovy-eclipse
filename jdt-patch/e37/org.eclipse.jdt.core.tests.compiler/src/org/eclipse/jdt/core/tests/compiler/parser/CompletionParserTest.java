/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import org.eclipse.jdt.internal.codeassist.complete.InvalidCursorLocation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class CompletionParserTest extends AbstractCompletionTest {
public CompletionParserTest(String testName) {
	super(testName);
}
static {
//	TESTS_NAMES = new String[] { "testXA_1FGGUQF_1FHSL8H_1" };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(CompletionParserTest.class);
}
public void testA() {
	String str =
		"package p; \n" +
		"public class A {\n" +
		"	public void foo(\n" +
		"		java.util.Locale, \n" +
		"		java.util.Vector) {\n" +
		"		int i;\n" +
		"		if (i instanceof O) {\n" +
		"		}\n" +
		"		String s = \"hello\";\n" +
		"		s.}\n" +
		"}\n";

	String testName = "<complete on methods/fields>";
	String completeBehind = "s.";
	String expectedCompletionNodeToString = "<CompleteOnName:s.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "s.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class A {\n" +
		"  public A() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    {\n" +
		"      int i;\n" +
		"      String s;\n" +
		"      <CompleteOnName:s.>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAA_1() {
	String str =
			"package p; \n" +
			"import something; \n" +
			"import p2.; \n" +
			"public class AA {\n" +
			"	void foo() {\n" +
			"		int maxUnits = 0;\n" +
			"		for (int i = 0; \n" +
			"			i < maxUnits; \n" +
			"			i++) {\n" +
			"			CompilationUnitResult unitResult = \n" +
			"				new CompilationUnitResult(\n" +
			"					null, \n" +
			"					i, \n" +
			"					maxUnits); \n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import something;\n" +
		"public class AA {\n" +
		"  public AA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int maxUnits;\n" +
		"    int i;\n" +
		"    {\n" +
		"      CompilationUnitResult unitResult = <CompleteOnName:n>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new";
	String testName = "<complete on initializer (new)>";

	int cursorLocation = str.indexOf("new CompilationUnitResult(") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAA_2() {
	String str =
			"package p; \n" +
			"import something; \n" +
			"import p2.; \n" +
			"public class AA {\n" +
			"	void foo() {\n" +
			"		int maxUnits = 0;\n" +
			"		for (int i = 0; \n" +
			"			i < maxUnits; \n" +
			"			i++) {\n" +
			"			CompilationUnitResult unitResult = \n" +
			"				new CompilationUnitResult(\n" +
			"					null, \n" +
			"					i, \n" +
			"					maxUnits); \n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method call argument>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "null";
	int cursorLocation = str.indexOf("null, ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import something;\n" +
		"public class AA {\n" +
		"  public AA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int maxUnits;\n" +
		"    int i;\n" +
		"    {\n" +
		"      CompilationUnitResult unitResult = new CompilationUnitResult(<CompleteOnName:n>);\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAA_3() {
	String str =
			"package p; \n" +
			"import something; \n" +
			"import p2.; \n" +
			"public class AA {\n" +
			"	void foo() {\n" +
			"		int maxUnits = 0;\n" +
			"		for (int i = 0; \n" +
			"			i < maxUnits; \n" +
			"			i++) {\n" +
			"			CompilationUnitResult unitResult = \n" +
			"				new CompilationUnitResult(\n" +
			"					null, \n" +
			"					i, \n" +
			"					maxUnits); \n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on call to constructor argument>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i, ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import something;\n" +
		"public class AA {\n" +
		"  public AA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int maxUnits;\n" +
		"    int i;\n" +
		"    {\n" +
		"      CompilationUnitResult unitResult = new CompilationUnitResult(null, <CompleteOnName:i>);\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAA_4() {
	String str =
			"package p; \n" +
			"import something; \n" +
			"import p2.; \n" +
			"public class AA {\n" +
			"	void foo() {\n" +
			"		int maxUnits = 0;\n" +
			"		for (int i = 0; \n" +
			"			i < maxUnits; \n" +
			"			i++) {\n" +
			"			CompilationUnitResult unitResult = \n" +
			"				new CompilationUnitResult(\n" +
			"					null, \n" +
			"					i, \n" +
			"					maxUnits); \n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on constructor call argument>";
	String completeBehind = "max";
	String expectedCompletionNodeToString = "<CompleteOnName:max>";
	String completionIdentifier = "max";
	String expectedReplacedSource = "maxUnits";
	int cursorLocation = str.indexOf("maxUnits); ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import something;\n" +
		"public class AA {\n" +
		"  public AA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int maxUnits;\n" +
		"    int i;\n" +
		"    {\n" +
		"      CompilationUnitResult unitResult = new CompilationUnitResult(null, i, <CompleteOnName:max>);\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAB_1FHU9LU() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHU9LU\n" +
			" */\n" +
			"class SuperClass {\n" +
			"	static void eFooStatic() {\n" +
			"	}\n" +
			"	void eFoo() {\n" +
			"	}\n" +
			"}\n" +
			"public class AB\n" +
			"	extends SuperClass {\n" +
			"	void eBar() {\n" +
			"		super.eFoo();\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on methods/fields from super class>";
	String completeBehind = "super.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:super.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "super.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class SuperClass {\n" +
		"  SuperClass() {\n" +
		"  }\n" +
		"  static void eFooStatic() {\n" +
		"  }\n" +
		"  void eFoo() {\n" +
		"  }\n" +
		"}\n" +
		"public class AB extends SuperClass {\n" +
		"  public AB() {\n" +
		"  }\n" +
		"  void eBar() {\n" +
		"    <CompleteOnMemberAccess:super.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAC_1FJ8D9Z_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ8D9Z\n" +
			" */\n" +
			"import java.io.*;\n" +
			"public class AC {\n" +
			"	AC() {\n" +
			"	}\n" +
			"	AC(int i) {\n" +
			"	}\n" +
			"	AC(int i, String s) {\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		new AC(new File(\n" +
			"			new java\n" +
			"			.util\n" +
			"			.Vector(}\n" +
			"}\n";

	String testName = "<complete on constructor argument>";
	String completeBehind = "new AC(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new AC()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.io.*;\n" +
		"public class AC {\n" +
		"  AC() {\n" +
		"  }\n" +
		"  AC(int i) {\n" +
		"  }\n" +
		"  AC(int i, String s) {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnAllocationExpression:new AC()>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAC_1FJ8D9Z_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ8D9Z\n" +
			" */\n" +
			"import java.io.*;\n" +
			"public class AC {\n" +
			"	AC() {\n" +
			"	}\n" +
			"	AC(int i) {\n" +
			"	}\n" +
			"	AC(int i, String s) {\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		new AC(new File(\n" +
			"			new java\n" +
			"			.util\n" +
			"			.Vector(}\n" +
			"}\n";

	String testName = "<complete on constructor argument>";
	String completeBehind = "new File(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new File()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.io.*;\n" +
		"public class AC {\n" +
		"  AC() {\n" +
		"  }\n" +
		"  AC(int i) {\n" +
		"  }\n" +
		"  AC(int i, String s) {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnAllocationExpression:new File()>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAC_1FJ8D9Z_3() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ8D9Z\n" +
			" */\n" +
			"import java.io.*;\n" +
			"public class AC {\n" +
			"	AC() {\n" +
			"	}\n" +
			"	AC(int i) {\n" +
			"	}\n" +
			"	AC(int i, String s) {\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		new AC(new File(\n" +
			"			new java.util.Vector(}\n" +
			"}\n";

	String testName = "<complete on constructor argument>";
	String completeBehind = "new java.util.Vector(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new java.util.Vector()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.io.*;\n" +
		"public class AC {\n" +
		"  AC() {\n" +
		"  }\n" +
		"  AC(int i) {\n" +
		"  }\n" +
		"  AC(int i, String s) {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnAllocationExpression:new java.util.Vector()>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testB() {
	String str =
		"package p; \n" +
		"public class B {\n" +
		"	Object o = new Object }\n";

	String testName = "<complete on type into type creation>";
	String completeBehind = "new Object";
	String expectedCompletionNodeToString = "<CompleteOnType:Object>";
	String completionIdentifier = "Object";
	String expectedReplacedSource = "Object";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class B {\n" +
		"  Object o = new <CompleteOnType:Object>();\n" +
		"  public B() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBA_1() {
	String str =
			"package p; \n" +
			"public class BA {\n" +
			"	void foo() {\n" +
			"		java.util.Vector v2;\n" +
			"		java.util.Vector v1;\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on package name>";
	String completeBehind = "java.";
	String expectedCompletionNodeToString = "<CompleteOnName:java.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "java.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class BA {\n" +
		"  public BA() {\n" +

		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:java.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBA_2() {
	String str =
			"package p; \n" +
			"public class BA {\n" +
			"	void foo() {\n" +
			"		java.util.Vector v2;\n" +
			"		java.util.Vector v1;\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on package contents>";
	String completeBehind = "java.util.";
	String expectedCompletionNodeToString = "<CompleteOnName:java.util.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "java.util.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class BA {\n" +
		"  public BA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:java.util.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBB_1FHJ8H9() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHJ8H9\n" +
			" */\n" +
			"public class BB {\n" +
			"	void bar() {\n" +
			"		f }\n" +
			"}\n";

	String testName = "<complete on method/field from implicit method call>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class BB {\n" +
		"  public BB() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"    <CompleteOnName:f>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBC_1FJ4GSG_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ4GSG\n" +
			" */\n" +
			"import java.util.Vector;\n" +
			"public class BC {\n" +
			"	int Value1 = 0;\n" +
			"	interface Constants {\n" +
			"		int OK = 1;\n" +
			"		int CANCEL = 2;\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		Vector v = \n" +
			"			new Vector(\n" +
			"				Value1, \n" +
			"				BC.Constants.OK\n" +
			"					| BC.Constants.CANCEL); \n" +
			"		Object ans = v.elementAt(1);\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on member type>";
	String completeBehind = "BC.";
	String expectedCompletionNodeToString = "<CompleteOnName:BC.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "BC.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.util.Vector;\n" +
		"public class BC {\n" +
		"  interface Constants {\n" +
		"    int OK;\n" +
		"    int CANCEL;\n" +
		"    <clinit>() {\n" +
		"    }\n" +
		"  }\n" +
		"  int Value1;\n" +
		"  public BC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Vector v = new Vector(Value1, <CompleteOnName:BC.>);\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBC_1FJ4GSG_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ4GSG\n" +
			" */\n" +
			"import java.util.Vector;\n" +
			"public class BC {\n" +
			"	int Value1 = 0;\n" +
			"	interface Constants {\n" +
			"		int OK = 1;\n" +
			"		int CANCEL = 2;\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		Vector v = \n" +
			"			new Vector(\n" +
			"				Value1, \n" +
			"				BC.Constants.OK\n" +
			"					| BC.Constants.CANCEL); \n" +
			"		Object ans = v.elementAt(1);\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on member type method/field>";
	String completeBehind = "| BC.Constants.";
	String expectedCompletionNodeToString = "<CompleteOnName:BC.Constants.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "BC.Constants.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.util.Vector;\n" +
		"public class BC {\n" +
		"  interface Constants {\n" +
		"    int OK;\n" +
		"    int CANCEL;\n" +
		"    <clinit>() {\n" +
		"    }\n" +
		"  }\n" +
		"  int Value1;\n" +
		"  public BC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Vector v = (BC.Constants.OK | <CompleteOnName:BC.Constants.>);\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBC_1FJ4GSG_3() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ4GSG\n" +
			" */\n" +
			"import java.util.Vector;\n" +
			"public class BC {\n" +
			"	int Value1 = 0;\n" +
			"	interface Constants {\n" +
			"		int OK = 1;\n" +
			"		int CANCEL = 2;\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		Vector v = \n" +
			"			new Vector(\n" +
			"				Value1, \n" +
			"				BC.Constants.OK\n" +
			"					| BC.Constants.CANCEL); \n" +
			"		Object ans = v.elementAt(1);\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "v.";
	String expectedCompletionNodeToString = "<CompleteOnName:v.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "v.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.util.Vector;\n" +
		"public class BC {\n" +
		"  interface Constants {\n" +
		"    int OK;\n" +
		"    int CANCEL;\n" +
		"    <clinit>() {\n" +
		"    }\n" +
		"  }\n" +
		"  int Value1;\n" +
		"  public BC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Vector v;\n" +
		"    Object ans = <CompleteOnName:v.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testC() {
	String str =
		"package p; \n" +
		"public class C {\n" +
		"	void foo() {\n" +
		"		String string = n;\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "= n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class C {\n" +
		"  public C() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    String string = <CompleteOnName:n>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "n";
	String testName = "<complete on local variable initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testCA_1FGPJQZ() {
	String str =
			"package p; \n" +
			"import p2.X; \n" +
			"/**\n" +
			" * 1FGPJQZ\n" +
			" */\n" +
			"public class CA {\n" +
			"	void moo() {\n" +
			"		unknownField.}\n" +
			"}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "unknownField.";
	String expectedCompletionNodeToString = "<CompleteOnName:unknownField.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "unknownField.";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import p2.X;\n" +
		"public class CA {\n" +
		"  public CA() {\n" +
		"  }\n" +
		"  void moo() {\n" +
		"    <CompleteOnName:unknownField.>;\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testCB_1FHSKQ9_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHSKQ9\n" +
			" */\n" +
			"public class CB {\n" +
			"	void foo() {\n" +
			"		int i = 0;\n" +
			"		int[] tab1 = new int[10];\n" +
			"		int j = tab1[i];\n" +
			"		System.out.println(\n" +
			"			\" \" + (i + 1)); \n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method call argument>";
	String completeBehind = "+ (i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class CB {\n" +
		"  public CB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    int[] tab1;\n" +
		"    int j;\n" +
		"    (\" \" + <CompleteOnName:i>);\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testCB_1FHSKQ9_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHSKQ9\n" +
			" */\n" +
			"public class CB {\n" +
			"	void foo() {\n" +
			"		int i = 0;\n" +
			"		int[] tab1 = new int[10];\n" +
			"		int j = tab1[i];\n" +
			"		System.out.println(\n" +
			"			\" \" + (i + 1)); \n" +
			"	}\n" +
			"}\n";

	String completeBehind = "i + 1";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedUnitDisplayString = null;
	String expectedReplacedSource = NONE;
	String testName = "<complete on digit into method call argument>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;

	try {
		checkMethodParse(
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
public void testCC_1FJ64I9() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ64I9\n" +
			" */\n" +
			"class CCHelper {\n" +
			"	class Member1 {\n" +
			"	}\n" +
			"	class Member2 {\n" +
			"	}\n" +
			"	void foo() {\n" +
			"	}\n" +
			"}\n" +
			"public class CC {\n" +
			"	void foo() {\n" +
			"		new CCHelper()\n" +
			"			.new CCHelper()\n" +
			"			.new M }\n" +
			"}\n";

	String testName = "<complete on qualified member type>";
	String completeBehind = ".new M";
	String expectedCompletionNodeToString = "<CompleteOnType:M>";
	String completionIdentifier = "M";
	String expectedReplacedSource = "M";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class CCHelper {\n" +
		"  class Member1 {\n" +
		"    Member1() {\n" +
		"    }\n" +
		"  }\n" +
		"  class Member2 {\n" +
		"    Member2() {\n" +
		"    }\n" +
		"  }\n" +
		"  CCHelper() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n" +
		"public class CC {\n" +
		"  public CC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new CCHelper().new CCHelper().new <CompleteOnType:M>();\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testD_1() {
	String str =
		"package p; \n" +
		"import java.util.*;\n" +
		"public class D {\n" +
		"	static int i;\n" +
		"	static {\n" +
		"		i = 5;\n" +
		"	}\n" +
		"	public int j;\n" +
		"	Vector a = new Vector();\n" +
		"	void foo(String s) {\n" +
		"		String string = null;\n" +
		"		int soso;\n" +
		"		float f;\n" +
		"		string.regionMatches(\n" +
		"			0, \n" +
		"			\"\", \n" +
		"			0, \n" +
		"			0); \n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on variable into type initializer>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i = 5;") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.util.*;\n" +
		"public class D {\n" +
		"  static int i;\n" +
		"  static {\n" +
		"    <CompleteOnName:i>;\n" +
		"  }\n" +
		"  public int j;\n" +
		"  Vector a;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public D() {\n" +
		"  }\n" +
		"  void foo(String s) {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testD_2() {
	String str =
		"package p; \n" +
		"import java.util.*;\n" +
		"public class D {\n" +
		"	static int i;\n" +
		"	static {\n" +
		"		i = 5;\n" +
		"	}\n" +
		"	public int j;\n" +
		"	Vector a = new Vector();\n" +
		"	void foo(String s) {\n" +
		"		String string = null;\n" +
		"		int soso;\n" +
		"		float f;\n" +
		"		string.regionMatches(\n" +
		"			0, \n" +
		"			\"\", \n" +
		"			0, \n" +
		"			0); \n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "string.";
	String expectedCompletionNodeToString = "<CompleteOnName:string.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "string.";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.util.*;\n" +
		"public class D {\n" +
		"  static int i;\n" +
		"  static {\n" +
		"  }\n" +
		"  public int j;\n" +
		"  Vector a;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public D() {\n" +
		"  }\n" +
		"  void foo(String s) {\n" +
		"    String string;\n" +
		"    int soso;\n" +
		"    float f;\n" +
		"    <CompleteOnName:string.>;\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_1() {
	String str =
			"package p; \n" +
			"public class DA {\n" +
			"	void foo() {\n" +
			"		new TestCase(\"error\") {\n" +
			"			protected void runTest() {\n" +
			"				Vector v11111 = new Vector();\n" +
			"				v }\n" +
			"		};\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field into anonymous declaration>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "v";
	int cursorLocation = str.indexOf("v }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class DA {\n" +
		"  public DA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new TestCase(\"error\") {\n" +
		"      protected void runTest() {\n" +
		"        Vector v11111;\n" +
		"        <CompleteOnName:v>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_2() {
	String str =
			"package p; \n" +
			"public class DA {\n" +
			"	void foo() {\n" +
			"		new TestCase(\"error\") {\n" +
			"			protected void runTest() {\n" +
			"				Vector v11111 = new Vector();\n" +
			"				v }\n" +
			"		};\n" +
			"	}\n" +
			"}\n";

	String completeBehind = "protected v";
	String expectedCompletionNodeToString = "<CompleteOnType:v>";
	String completionIdentifier = "v";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class DA {\n" +
		"  public DA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new TestCase(\"error\") {\n" +
		"      <CompleteOnType:v>;\n" +
		"      runTest() {\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "void";
	String testName = "<complete on return type into anonymous declaration>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_3() {
	String str =
			"package p; \n" +
			"public class DA {\n" +
			"	void foo() {\n" +
			"		new TestCase(\"error\") {\n" +
			"			protected void runTest() {\n" +
			"				Vector v11111 = new Vector();\n" +
			"				v }\n" +
			"		};\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method selector into anonymous declaration>";
	String completeBehind = "r";
	String expectedCompletionNodeToString = "<CompleteOnFieldName:void r>;";
	String completionIdentifier = "r";
	String expectedReplacedSource = "runTest";
	int cursorLocation = str.indexOf("runTest") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class DA {\n" +
		"  public DA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new TestCase(\"error\") {\n" +
		"      <CompleteOnFieldName:void r>;\n" +
		"      {\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_4() {
	String str =
			"package p; \n" +
			"public class DA {\n" +
			"	void foo() {\n" +
			"		new TestCase(\"error\") {\n" +
			"			protected void runTest() {\n" +
			"				Vector v11111 = new Vector();\n" +
			"				v }\n" +
			"		};\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on local variable type into anonymous declaration>";
	String completeBehind = "V";
	String expectedCompletionNodeToString = "<CompleteOnName:V>";
	String completionIdentifier = "V";
	String expectedReplacedSource = "Vector";
	int cursorLocation = str.indexOf("Vector v11111") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class DA {\n" +
		"  public DA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new TestCase(\"error\") {\n" +
		"      protected void runTest() {\n" +
		"        <CompleteOnName:V>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_5() {
	String str =
			"package p; \n" +
			"public class DA {\n" +
			"	void foo() {\n" +
			"		new TestCase(\"error\") {\n" +
			"			protected void runTest() {\n" +
			"				Vector v11111 = new Vector();\n" +
			"				v }\n" +
			"		};\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on local type into anonymous declaration>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "v";
	int cursorLocation = str.indexOf("v }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class DA {\n" +
		"  public DA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new TestCase(\"error\") {\n" +
		"      protected void runTest() {\n" +
		"        Vector v11111;\n" +
		"        <CompleteOnName:v>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDB_1FHSLDR() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHSLDR\n" +
			" */\n" +
			"public class DB {\n" +
			"	void foo() {\n" +
			"		try {\n" +
			"			System.out.println(\"\");\n" +
			"		}\n" +
			"		fi }\n" +
			"}\n";

	String testName = "<complete on finally keyword>";
	String completeBehind = "fi";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:fi>";
	String completionIdentifier = "fi";
	String expectedReplacedSource = "fi";
	int cursorLocation = str.indexOf("fi }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class DB {\n" +
		"  public DB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:fi>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDC_1FJJ0JR_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJJ0JR\n" +
			" */\n" +
			"public class DC\n" +
			"	extends ModelChangeOperation {\n" +
			"	ISec public SetSecondarySourceOperation(\n" +
			"		ISecondarySourceContainer element, \n" +
			"		VersionID id) {\n" +
			"	}\n" +
			"	protected abstract void doExecute(IProgressMonitor monitor)\n" +
			"		throws OperationFailedException {\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method return type>";
	String completeBehind = "ISec";
	String expectedCompletionNodeToString = "<CompleteOnType:ISec>";
	String completionIdentifier = "ISec";
	String expectedReplacedSource = "ISec";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class DC extends ModelChangeOperation {\n" +
		"  <CompleteOnType:ISec>;\n" +
		"  public DC() {\n" +
		"  }\n" +
		"  public SetSecondarySourceOperation(ISecondarySourceContainer element, VersionID id) {\n" +
		"  }\n" +
		"  protected abstract void doExecute(IProgressMonitor monitor) throws OperationFailedException;\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testE_1FG1YDS_1() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FG1YDS\n" +
		" */\n" +
		"public class E {\n" +
		"	{\n" +
		"		new Y()\n" +
		"	 }\n" +
		"	{\n" +
		"		new Y().}\n" +
		"	class Y\n" +
		"		extends java.util.Vector {\n" +
		"		void foo() {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on type into type creation>";
	String completeBehind = "Y";
	String expectedCompletionNodeToString = "<CompleteOnType:Y>";
	String completionIdentifier = "Y";
	String expectedReplacedSource = "Y";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class E {\n" +
		"  class Y extends java.util.Vector {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"    new <CompleteOnType:Y>();\n" +
		"  }\n" +
		"  {\n" +
		"  }\n" +
		"  public E() {\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf("Y()\n") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testE_1FG1YDS_2() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FG1YDS\n" +
		" */\n" +
		"public class E {\n" +
		"	{\n" +
		"		new Y()\n" +
		"	 }\n" +
		"	{\n" +
		"		new Y().}\n" +
		"	class Y\n" +
		"		extends java.util.Vector {\n" +
		"		void foo() {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on implicit method call into intializer>";
	String completeBehind = "new Y().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Y().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class E {\n" +
		"  class Y extends java.util.Vector {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"  }\n" +
		"  {\n" +
		"    <CompleteOnMemberAccess:new Y().>;\n" +
		"  }\n" +
		"  public E() {\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testE_1FG1YDS_3() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FG1YDS\n" +
		" */\n" +
		"public class E {\n" +
		"	{\n" +
		"		new Y()\n" +
		"	 }\n" +
		"	{\n" +
		"		new Y().}\n" +
		"	class Y\n" +
		"		extends java.util.Vector {\n" +
		"		void foo() {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on extend type>";
	String completeBehind = "java.util.";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.util.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "java.util.Vector";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class E {\n" +
		"  class Y extends <CompleteOnClass:java.util.> {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"  }\n" +
		"  {\n" +
		"  }\n" +
		"  public E() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testEA_1() {
	String str =
			"package p; \n" +
			"public class EA {\n" +
			"	void foo() {\n" +
			"		try {\n" +
			"			throw new Error();\n" +
			"		} catch (Exception eeee) {\n" +
			"			eeee.}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on catch block exception type declaration>";
	String completeBehind = "E";
	String expectedCompletionNodeToString = "<CompleteOnException:E>";
	String completionIdentifier = "E";
	String expectedReplacedSource = "Exception";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class EA {\n" +
		"  public EA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    try\n" +
		"      {\n" +
		"        throw new Error();\n" +
		"      }\n" +
		"    catch (<CompleteOnException:E>  )\n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf("Exception eeee") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testEA_2() {
	String str =
			"package p; \n" +
			"public class EA {\n" +
			"	void foo() {\n" +
			"		try {\n" +
			"			throw new Error();\n" +
			"		} catch (Exception eeee) {\n" +
			"			eeee.}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of thrown exception into catch block>";
	String completeBehind = "eeee.";
	String expectedCompletionNodeToString = "<CompleteOnName:eeee.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "eeee.";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class EA {\n" +
		"  public EA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      Exception eeee;\n" +
		"      <CompleteOnName:eeee.>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testEB_1FI74S3() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FI74S3\n" +
			" */\n" +
			"public class EB {\n" +
			"	int[] table;\n" +
			"	void foo() {\n" +
			"		int x = table.}\n" +
			"}\n";

	String testName = "<complete on method/field of array>";
	String completeBehind = "table.";
	String expectedCompletionNodeToString = "<CompleteOnName:table.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "table.";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class EB {\n" +
		"  int[] table;\n" +
		"  public EB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int x = <CompleteOnName:table.>;\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testEC_1FSBZ2Y() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FSBZ2Y\n" +
			" */\n" +
			"class EC {\n" +
			"	void foo() {\n" +
			"		EC\n" +
			"	}\n" +
			"}\n" +
			"class ECOtherTopLevel {\n" +
			"}\n";

	String testName = "<complete on local variable decaration type>";
	String completeBehind = "EC";
	String expectedCompletionNodeToString = "<CompleteOnName:EC>";
	String completionIdentifier = "EC";
	String expectedReplacedSource = "EC";
	String expectedUnitDisplayString =
		"package p;\n" +
		"class EC {\n" +
		"  EC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:EC>;\n" +
		"  }\n" +
		"}\n" +
		"class ECOtherTopLevel {\n" +
		"  ECOtherTopLevel() {\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf("EC\n") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testF() {
	String str =
		"package p; \n" +
		"public class F {\n" +
		"	void bar() {\n" +
		"	}\n" +
		"	class Y {\n" +
		"		void foo() {\n" +
		"			ba }\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on method/field explicit access>";
	String completeBehind = "ba";
	String expectedCompletionNodeToString = "<CompleteOnName:ba>";
	String completionIdentifier = "ba";
	String expectedReplacedSource = "ba";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class F {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:ba>;\n" +
		"    }\n" +
		"  }\n" +
		"  public F() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf("ba }") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testFA_1() {
	String str =
			"package p; \n" +
			"public class FA {\n" +
			"	byte value;\n" +
			"	public float foo() {\n" +
			"		return (float) value;\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on cast expression type>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "float";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class FA {\n" +
		"  byte value;\n" +
		"  public FA() {\n" +
		"  }\n" +
		"  public float foo() {\n" +
		"    <CompleteOnName:f>;\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf("float)") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testFA_2() {
	String str =
			"package p; \n" +
			"public class FA {\n" +
			"	byte value;\n" +
			"	public float foo() {\n" +
			"		return (float) value; \n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on returned value>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "value";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class FA {\n" +
		"  byte value;\n" +
		"  public FA() {\n" +
		"  }\n" +
		"  public float foo() {\n" +
		"    (float) <CompleteOnName:v>;\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf("value; \n") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testFB_1FI74S3() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FI74S3\n" +
			" */\n" +
			"public class FB {\n" +
			"	int[] table;\n" +
			"	void foo() {\n" +
			"		int x = table[1].}\n" +
			"}\n";

	String testName = "<complete on method/field of array element>";
	String completeBehind = "table[1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:table[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class FB {\n" +
		"  int[] table;\n" +
		"  public FB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int x = <CompleteOnMemberAccess:table[1].>;\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testFC_1FSBZ9B() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FSBZ9B\n" +
			" */\n" +
			"class FC {\n" +
			"	UNKOWNTYPE field;\n" +
			"	void foo() {\n" +
			"		f\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field implicit access>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class FC {\n" +
		"  UNKOWNTYPE field;\n" +
		"  FC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:f>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testG() {
	String str =
		"package p; \n" +
		"public class G {\n" +
		"	int bar() {\n" +
		"	}\n" +
		"	class Y {\n" +
		"		void foo(int b) {\n" +
		"			return b }\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on return value>";
	String completeBehind = "b";
	String expectedCompletionNodeToString = "<CompleteOnName:b>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "b";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class G {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo(int b) {\n" +
		"      return <CompleteOnName:b>;\n" +
		"    }\n" +
		"  }\n" +
		"  public G() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	int cursorLocation = str.indexOf("b }") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGA() {
	String str =
			"package p; \n" +
			"public class GA {\n" +
			"	void foo(String s) {\n" +
			"		String string = s;\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on local variable initializer>";
	String completeBehind = "s";
	String expectedCompletionNodeToString = "<CompleteOnName:s>";
	String completionIdentifier = "s";
	String expectedReplacedSource = "s";
	int cursorLocation = str.indexOf("s;") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class GA {\n" +
		"  public GA() {\n" +
		"  }\n" +
		"  void foo(String s) {\n" +
		"    String string = <CompleteOnName:s>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGB_1FI74S3() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FI74S3\n" +
			" */\n" +
			"public class GB {\n" +
			"	String[] table;\n" +
			"	void foo() {\n" +
			"		int x = table[1].}\n" +
			"}\n";

	String testName = "<complete on method/field of array element>";
	String completeBehind = "table[1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:table[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class GB {\n" +
		"  String[] table;\n" +
		"  public GB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int x = <CompleteOnMemberAccess:table[1].>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGC_1FSHLHV_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FSHLHV\n" +
			" */\n" +
			"public class GC {\n" +
			"public static void main(String[] args) {\n" +
			"	Object l = new Object() {\n" +
			"		public void handleEvent(String[] event) {\n" +
			"			String s = new String();\n" +
			"			s.\n" +
			"			try {\n" +
			"				event.;\n" +
			"			}\n" +
			"			catch (Exception e) {\n" +
			"				e.\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"}\n";

	String testName = "<complete on anonymous declaration type>";
	String completeBehind = "O";
	String expectedCompletionNodeToString = "<CompleteOnType:O>";
	String completionIdentifier = "O";
	String expectedReplacedSource = "Object";
	int cursorLocation = str.indexOf("Object()") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class GC {\n" +
		"  public GC() {\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    Object l = new <CompleteOnType:O>();\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGC_1FSHLHV_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FSHLHV\n" +
			" */\n" +
			"public class GC {\n" +
			"public static void main(String[] args) {\n" +
			"	Object l = new Object() {\n" +
			"		public void handleEvent(String[] event) {\n" +
			"			String s = new String();\n" +
			"			s.\n" +
			"			try {\n" +
			"				event.;\n" +
			"			}\n" +
			"			catch (Exception e) {\n" +
			"				e.\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"}\n";

	String testName = "<complete on method/field of local variable into anonymous declaration>";
	String completeBehind = "s.";
	String expectedCompletionNodeToString = "<CompleteOnName:s.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "s.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class GC {\n" +
		"  public GC() {\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    Object l;\n" +
		"    new Object() {\n" +
		"      public void handleEvent(String[] event) {\n" +
		"        String s;\n" +
		"        <CompleteOnName:s.>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGC_1FSHLHV_3() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FSHLHV\n" +
			" */\n" +
			"public class GC {\n" +
			"public static void main(String[] args) {\n" +
			"	Object l = new Object() {\n" +
			"		public void handleEvent(String[] event) {\n" +
			"			String s = new String();\n" +
			"			s.\n" +
			"			try {\n" +
			"				event.;\n" +
			"			}\n" +
			"			catch (Exception e) {\n" +
			"				e.\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"}\n";

	String testName = "<complete on method/field of array>";
	String completeBehind = "event.";
	String expectedCompletionNodeToString = "<CompleteOnName:event.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "event.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class GC {\n" +
		"  public GC() {\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    Object l;\n" +
		"    new Object() {\n" +
		"      public void handleEvent(String[] event) {\n" +
		"        String s;\n" +
		"        {\n" +
		"          <CompleteOnName:event.>;\n" +
		"        }\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGC_1FSHLHV_4() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FSHLHV\n" +
			" */\n" +
			"public class GC {\n" +
			"public static void main(String[] args) {\n" +
			"	Object l = new Object() {\n" +
			"		public void handleEvent(String[] event) {\n" +
			"			String s = new String();\n" +
			"			s.\n" +
			"			try {\n" +
			"				event.;\n" +
			"			}\n" +
			"			catch (Exception e) {\n" +
			"				e.\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"}\n";

	String testName = "<complete on method/field of thrown exception into catch block into anonymous declaration>";
	String completeBehind = "e.";
	String expectedCompletionNodeToString = "<CompleteOnName:e.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "e.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class GC {\n" +
		"  public GC() {\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    Object l;\n" +
		"    new Object() {\n" +
		"      public void handleEvent(String[] event) {\n" +
		"        String s;\n" +
		"        {\n" +
		"          Exception e;\n" +
		"          <CompleteOnName:e.>;\n" +
		"        }\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testH() {
	String str =
		"package p; \n" +
		"public class H {\n" +
		"	void foo(boolean bbbb) {\n" +
		"		while (Xbm }\n" +
		"	void bar() {\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on while keyword argument>";
	String completeBehind = "Xbm";
	String expectedCompletionNodeToString = "<CompleteOnName:Xbm>";
	String completionIdentifier = "Xbm";
	String expectedReplacedSource = "Xbm";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class H {\n" +
		"  public H() {\n" +
		"  }\n" +
		"  void foo(boolean bbbb) {\n" +
		"    while (<CompleteOnName:Xbm>)      ;\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testHA_1() {
	String str =
			"package p; \n" +
			"public class HA {\n" +
			"	void foo() {\n" +
			"		x.y.Z[] field1; \n" +
			"		field1[1].}\n" +
			"}\n";

	String testName = "<complete on package member type>";
	String completeBehind = "x.y.";
	String expectedCompletionNodeToString = "<CompleteOnName:x.y.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "x.y.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class HA {\n" +
		"  public HA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:x.y.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testHA_2() {
	String str =
			"package p; \n" +
			"public class HA {\n" +
			"	void foo() {\n" +
			"		x.y.Z[] field1; \n" +
			"		field1[1].}\n" +
			"}\n";

	String testName = "<complete on method/field of array element>";
	String completeBehind = "field1[1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:field1[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class HA {\n" +
		"  public HA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    x.y.Z[] field1;\n" +
		"    <CompleteOnMemberAccess:field1[1].>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testHB_1FHSLDR() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHSLDR\n" +
			" */\n" +
			"public class HB {\n" +
			"	void foo() {\n" +
			"		for (; i < totalUnits; i++) {\n" +
			"			unit = unitsToProcess[i];\n" +
			"			try {\n" +
			"				if (options.verbose) {\n" +
			"					System.out.println(\n" +
			"						\"process \"\n" +
			"							+ (i + 1)\n" +
			"							+ \"/\"\n" +
			"							+ totalUnits\n" +
			"							+ \" : \"\n" +
			"							+ unitsToProcess[i]\n" +
			"								.sourceFileName()); \n" +
			"				}\n" +
			"				process(unit, i);\n" +
			"			}\n" +
			"			fi }\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on finally keyword>";
	String completeBehind = "fi";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:fi>";
	String completionIdentifier = "fi";
	String expectedReplacedSource = "fi";
	int cursorLocation = str.indexOf("fi }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class HB {\n" +
		"  public HB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnKeyword:fi>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testHC_1FMPYO3_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FMPYO3\n" +
			" */\n" +
			"class HC {\n" +
			"	HC(Object o){}\n" +
			"	void foo(){\n" +
			"		HC a = new HC(new Object()).\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new HC(new Object()).";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new HC(new Object()).>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class HC {\n" +
		"  HC(Object o) {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    HC a = <CompleteOnMemberAccess:new HC(new Object()).>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testHC_1FMPYO3_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FMPYO3\n" +
			" */\n" +
			"class HC {\n" +
			"	HC(Object o){}\n" +
			"	void foo(){\n" +
			"		A a = new A(new Object()).\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on object of nested object creation declaration>";
	String completeBehind = "O";
	String expectedCompletionNodeToString = "<CompleteOnType:O>";
	String completionIdentifier = "O";
	String expectedReplacedSource = "Object";
	int cursorLocation = str.indexOf("Object()") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class HC {\n" +
		"  HC(Object o) {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    A a = new A(new <CompleteOnType:O>());\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testI() {
	String str =
		"package p; \n" +
		"public class I {\n" +
		"	Component }\n";

	String testName = "<complete on incomplete field declaration type>";
	String completeBehind = "C";
	String expectedCompletionNodeToString = "<CompleteOnType:C>";
	String completionIdentifier = "C";
	String expectedReplacedSource = "Component";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class I {\n" +
		"  <CompleteOnType:C>;\n" +
		"  public I() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testIA_1FGNBPR_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FGNBPR\n" +
			" */\n" +
			"public class IA {\n" +
			"	void foo1() {\n" +
			"		label1 : while (true) {\n" +
			"			class A {\n" +
			"				void foo2() {\n" +
			"					label2 : while (true) {\n" +
			"						break la }\n" +
			"				}\n" +
			"			}\n" +
			"			A a = new A();\n" +
			"			break la }\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on label name>";
	String completeBehind = "la";
	String expectedCompletionNodeToString = "break <CompleteOnLabel:la>;";
	String completionIdentifier = "la";
	String expectedReplacedSource = "la";
	int cursorLocation = str.indexOf("la }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class IA {\n" +
		"  public IA() {\n" +
		"  }\n" +
		"  void foo1() {\n" +
		"    {\n" +
		"      class A {\n" +
		"        A() {\n" +
		"        }\n" +
		"        void foo2() {\n" +
		"          break <CompleteOnLabel:la>;\n" +
		"        }\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testIA_1FGNBPR_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FGNBPR\n" +
			" */\n" +
			"public class IA {\n" +
			"	void foo1() {\n" +
			"		label1 : while (true) {\n" +
			"			class A {\n" +
			"				void foo2() {\n" +
			"					label2 : while (true) {\n" +
			"						break la }\n" +
			"				}\n" +
			"			}\n" +
			"			A a = new A();\n" +
			"			break la }\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on label name>";
	String completeBehind = "la";
	String expectedCompletionNodeToString = "break <CompleteOnLabel:la>;";
	String completionIdentifier = "la";
	String expectedReplacedSource = "la";
	int cursorLocation = str.indexOf("la }", str.indexOf("la }") + 1) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class IA {\n" +
		"  public IA() {\n" +
		"  }\n" +
		"  void foo1() {\n" +
		"    {\n" +
		"      class A {\n" +
		"        A() {\n" +
		"          super();\n" + // could be optimized out ?
		"        }\n" +
		"        void foo2() {\n" +
		"        }\n" +
		"      }\n" +
		"      A a;\n" +
		"      break <CompleteOnLabel:la>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testIB() {
	String str =
			"package p; \n" +
			"public class IB {\n" +
			"	UnknownFieldTYPE field;\n" +
			"	void foo() {\n" +
			"		field.}\n" +
			"}\n";

	String testName = "<complete on method/field of field of unkown type>";
	String completeBehind = "field.";
	String expectedCompletionNodeToString = "<CompleteOnName:field.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "field.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class IB {\n" +
		"  UnknownFieldTYPE field;\n" +
		"  public IB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:field.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testIC_1FMGUPR() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FMGUPR\n" +
			" */\n" +
			"public class IC {\n" +
			"	void foo(){\n" +
			"		new String().toString().\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on multiple method/field call>";
	String completeBehind = "new String().toString().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new String().toString().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class IC {\n" +
		"  public IC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:new String().toString().>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJ() {
	String str =
		"package p; \n" +
		"public class J {\n" +
		"	int foo1()[void foo2() int i;\n" +
		"	void foo3() {\n" +
		"		f }\n";

	String testName = "<complete on method/field access into corrupted method declaration>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class J {\n" +
		"  public J() {\n" +
		"  }\n" +
		"  int foo1() {\n" +
		"  }\n" +
		"  void foo2() {\n" +
		"  }\n" +
		"  void foo3() {\n" +
		"    <CompleteOnName:f>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJA_1FGQVW2_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FGQVW2\n" +
			" */\n" +
			"public class JA {\n" +
			"	void foo() {\n" +
			"		\"abc.txt\". 'a'.}\n" +
			"}\n";

	String testName = "<complete on string literal>";
	String completeBehind = "\"abc.txt\".";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:\"abc.txt\".>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class JA {\n" +
		"  public JA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:\"abc.txt\".>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJA_1FGQVW2_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FGQVW2\n" +
			" */\n" +
			"public class JA {\n" +
			"	void foo() {\n" +
			"		\"abc.txt\". 'a'.}\n" +
			"}\n";

	String testName = "<complete on char literal>";
	String completeBehind = "'a'.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:'a'.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class JA {\n" +
		"  public JA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:'a'.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJB() {
	String str =
			"package p; \n" +
			"public class JB\n" +
			"	extends UnknownSUPERCLASS\n" +
			"	implements UnknownSUPERINTERFACE {\n" +
			"	void foo() {\n" +
			"		f }\n" +
			"}\n";

	String testName = "<complete into method declared into corrupted class declaration>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class JB extends UnknownSUPERCLASS implements UnknownSUPERINTERFACE {\n" +
		"  public JB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:f>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJC_1FLG1ZC() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FLG1ZC\n" +
			" */\n" +
			"public class JC {\n" +
			"	void foo() {\n" +
			"		new String ().\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of object creation with dummy spaces>";
	String completeBehind = "new String ().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new String().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class JC {\n" +
		"  public JC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:new String().>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testK_1() {
	String str =
		"package p; \n" +
		"class Other {\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n" +
		"public class K {\n" +
		"	public static void main(\n" +
		"		java.lang.String[] args) {\n" +
		"		java.io.File bbbb = \n" +
		"			new File(\"c:\\abc.txt\"); \n" +
		"		O bb bbbb.}\n" +
		"}\n";

//	str =
//		"public class K {\n" +
//		"	void foo() {\n" +
//		"		new X(\"c:abc.txt\"); \n" +
//		"		O" +
//		"   }\n" +
//		"}\n";

	String testName = "<complete on corrupted local variable declaration>";
	String completeBehind = "		O";
	String expectedCompletionNodeToString = "<CompleteOnName:O>";
	String completionIdentifier = "O";
	String expectedReplacedSource = "O";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class Other {\n" +
		"  Other() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n" +
		"public class K {\n" +
		"  public K() {\n" +
		"  }\n" +
		"  public static void main(java.lang.String[] args) {\n" +
		"    java.io.File bbbb;\n" +
		"    <CompleteOnName:O>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testK_2() {
	String str =
		"package p; \n" +
		"class Other {\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n" +
		"public class K {\n" +
		"	public static void main(\n" +
		"		java.lang.String[] args) {\n" +
		"		java.io.File bbbb = \n" +
		"			new File(\"c:\\abc.txt\"); \n" +
		"		O bb bbbb.}\n" +
		"}\n";

	String testName = "<complete on corrupted local variable declaration name>";
	String completeBehind = "bb";
	String expectedCompletionNodeToString = "<CompleteOnLocalName:O bb>;";
	String completionIdentifier = "bb";
	String expectedReplacedSource = "bb";
	int cursorLocation = str.indexOf("bb bbbb.") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class Other {\n" +
		"  Other() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n" +
		"public class K {\n" +
		"  public K() {\n" +
		"  }\n" +
		"  public static void main(java.lang.String[] args) {\n" +
		"    java.io.File bbbb;\n" +
		"    <CompleteOnLocalName:O bb>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testK_3() {
	String str =
		"package p; \n" +
		"class Other {\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n" +
		"public class K {\n" +
		"	public static void main(\n" +
		"		java.lang.String[] args) {\n" +
		"		java.io.File bbbb = \n" +
		"			new File(\"c:\\abc.txt\"); \n" +
		"		O bb bbbb.}\n" +
		"}\n";

	String testName = "<complete on corrupted local variable declaration>";
	String completeBehind = "bbbb";
	String expectedCompletionNodeToString = "<CompleteOnName:bbbb>";
	String completionIdentifier = "bbbb";
	String expectedReplacedSource = "bbbb";
	int cursorLocation = str.indexOf("bbbb.}") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class Other {\n" +
		"  Other() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n" +
		"public class K {\n" +
		"  public K() {\n" +
		"  }\n" +
		"  public static void main(java.lang.String[] args) {\n" +
		"    java.io.File bbbb;\n" +
		"    O bb;\n" +
		"    <CompleteOnName:bbbb>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testK_4() {
	String str =
		"package p; \n" +
		"class Other {\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n" +
		"public class K {\n" +
		"	public static void main(\n" +
		"		java.lang.String[] args) {\n" +
		"		java.io.File bbbb = \n" +
		"			new File(\"c:\\abc.txt\"); \n" +
		"		O bb bbbb.}\n" +
		"}\n";

	String testName = "<complete on method/field of local variable with corrupted declaration>";
	String completeBehind = "bbbb.";
	String expectedCompletionNodeToString = "<CompleteOnName:bbbb.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "bbbb.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class Other {\n" +
		"  Other() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n" +
		"public class K {\n" +
		"  public K() {\n" +
		"  }\n" +
		"  public static void main(java.lang.String[] args) {\n" +
		"    java.io.File bbbb;\n" +
		"    O bb;\n" +
		"    <CompleteOnName:bbbb.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testKA_1FH5SU5() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FH5SU5\n" +
			" */\n" +
			"class KAHelper\n" +
			"	extends java.util.Vector {\n" +
			"}\n" +
			"public class KA {\n" +
			"	public int hashCode() {\n" +
			"		return 10;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		KA a = new KA;\n" +
			"		a.has }\n" +
			"}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "a.has";
	String expectedCompletionNodeToString = "<CompleteOnName:a.has>";
	String completionIdentifier = "has";
	String expectedReplacedSource = "a.has";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class KAHelper extends java.util.Vector {\n" +
		"  KAHelper() {\n" +
		"  }\n" +
		"}\n" +
		"public class KA {\n" +
		"  public KA() {\n" +
		"  }\n" +
		"  public int hashCode() {\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    KA a;\n" +
		"    <CompleteOnName:a.has>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testKB() {
	String str =
			"package p; \n" +
			"public class KB {\n" +
			"	void foo()[i }\n" +
			"}\n";

	String testName = "<complete on corrupted method header>";
	String completeBehind = "void foo()[i";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class KB {\n" +
		"  public KB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testKC_1FLG1ZC() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FLG1ZC\n" +
			" */\n" +
			"import java.io.*;\n" +
			"public class KC {\n" +
			"private static char[] read(String fileName){\n" +
			"	try {\n" +
			"		File file = new File(fileName);\n" +
			"		FileReader reader =\n" +
			"			new FileReader(file);\n" +
			"		int length;\n" +
			"		char[] contents =\n" +
			"			new char[\n" +
			"				length =\n" +
			"				(int) file.length()];\n" +
			"		int len = 0;\n" +
			"		int readSize = 0;\n" +
			"		while ((readSize != -1)\n" +
			"			&& (len != length)) {\n" +
			"			readSize = reader.read(\n" +
			"				contents,\n" +
			"				len,\n" +
			"				length - len);\n" +
			"			len += readSize;\n" +
			"		}\n" +
			"		reader. t\n";

	String testName = "<complete on method/field with dummy spaces>";
	String completeBehind = "reader. t";
	String expectedCompletionNodeToString = "<CompleteOnName:reader.t>";
	String completionIdentifier = "t";
	String expectedReplacedSource = "reader. t";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.io.*;\n" +
		"public class KC {\n" +
		"  public KC() {\n" +
		"  }\n" +
		"  private static char[] read(String fileName) {\n" +
		"    {\n" +
		"      File file;\n" +
		"      FileReader reader;\n" +
		"      int length;\n" +
		"      char[] contents;\n" +
		"      int len;\n" +
		"      int readSize;\n" +
		"      <CompleteOnName:reader.t>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testL_1() {
	String str =
		"package p; \n" +
		"public class L {\n" +
		"	void foo() {\n" +
		"		x.y.Z[] field1, \n" +
		"			field2; \n" +
		"		field1.if (int[].class }\n" +
		"}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "field1.";
	String expectedCompletionNodeToString = "<CompleteOnName:field1.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "field1.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class L {\n" +
		"  public L() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    x.y.Z[] field1;\n" +
		"    x.y.Z[] field2;\n" +
		"    <CompleteOnName:field1.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testL_2() {
	String str =
		"package p; \n" +
		"public class L {\n" +
		"	void foo() {\n" +
		"		x.y.Z[] field1, \n" +
		"			field2; \n" +
		"		field1.if (int[].class }\n" +
		"}\n";

	String testName = "<complete on method/field of array>";
	String completeBehind = "int[].";
	String expectedCompletionNodeToString = "<CompleteOnClassLiteralAccess:int[].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "int[].";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class L {\n" +
		"  public L() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    x.y.Z[] field1;\n" +
		"    x.y.Z[] field2;\n" +
		"    <CompleteOnClassLiteralAccess:int[].>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testL_3() {
	String str =
		"package p; \n" +
		"public class L {\n" +
		"	void foo() {\n" +
		"		x.y.Z[] field1, \n" +
		"			field2; \n" +
		"		field1.if (int[].class }\n" +
		"}\n";

	String testName = "<complete on argument of corrupted if statement>";
	String completeBehind = "int";
	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class L {\n" +
		"  public L() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    x.y.Z[] field1;\n" +
		"    x.y.Z[] field2;\n" +
		"    if (<CompleteOnName:int>)\n" +
		"        ;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testLA_1FGLMOF() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FGLMOF\n" +
			" */\n" +
			"public class LA {\n" +
			"	void[] foo() {\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		f }\n" +
			"}\n";

	String testName = "<complete on method/field with corrupted method header>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class LA {\n" +
		"  public LA() {\n" +
		"  }\n" +
		"  void[] foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"    <CompleteOnName:f>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testLB() {
	String str =
			"package p; \n" +
			"public class LB {\n" +
			"	void foo() {\n" +
			"	}\n" +
			"	void foo() {\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		i }\n" +
			"}\n";

	String testName = "<complete on method/field with duplicate method declaration>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class LB {\n" +
		"  public LB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"    <CompleteOnName:i>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testLC_1FLG1E2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FLG1E2\n" +
			" */\n" +
			"public class LC {\n" +
			"	void foo() {\n" +
			"		Object[] x = new Object[10];\n" +
			"		x [1].\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of array element with dummy spaces>";
	String completeBehind = "x [1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:x[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class LC {\n" +
		"  public LC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object[] x;\n" +
		"    <CompleteOnMemberAccess:x[1].>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testM_1FGGLMT() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGGLMT\n" +
		" */\n" +
		"public class M {\n" +
		"	class Member {\n" +
		"		void fooMember() {\n" +
		"		}\n" +
		"	}\n" +
		"	void foo() {\n" +
		"		new Member().}\n" +
		"}\n" +
		"class MemberOfCU {\n" +
		"}\n";

	String testName = "<complete on method/field of explicit object creation>";
	String completeBehind = "new Member().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Member().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class M {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"    }\n" +
		"    void fooMember() {\n" +
		"    }\n" +
		"  }\n" +
		"  public M() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:new Member().>;\n" +
		"  }\n" +
		"}\n" +
		"class MemberOfCU {\n" +
		"  MemberOfCU() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMA_1() {
	String str =
			"package p; \n" +
			"public class MA {\n" +
			"	class Member\n" +
			"		extends java.util.Vector {\n" +
			"		static void fooStaticMember() {\n" +
			"		}\n" +
			"		void fooMember() {\n" +
			"		}\n" +
			"		class MemberMember {\n" +
			"			void fooMemberMember() {\n" +
			"				MemberOfCUMA m = \n" +
			"					new MemberOfCUMA(); \n" +
			"			}\n" +
			"		}\n" +
			"		class MemberMember2 {\n" +
			"		}\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		Membe }\n" +
			"	void foobar() {\n" +
			"		new Member().}\n" +
			"	class Member2 {\n" +
			"	}\n" +
			"}\n" +
			"class MemberOfCUMA {\n" +
			"}\n";

	String testName = "<complete on local variable declaration type>";
	String completeBehind = "Membe";
	String expectedCompletionNodeToString = "<CompleteOnName:Membe>";
	String completionIdentifier = "Membe";
	String expectedReplacedSource = "Membe";
	int cursorLocation = str.indexOf("Membe }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class MA {\n" +
		"  class Member extends java.util.Vector {\n" +
		"    class MemberMember {\n" +
		"      MemberMember() {\n" +
		"      }\n" +
		"      void fooMemberMember() {\n" +
		"      }\n" +
		"    }\n" +
		"    class MemberMember2 {\n" +
		"      MemberMember2() {\n" +
		"      }\n" +
		"    }\n" +
		"    Member() {\n" +
		"    }\n" +
		"    static void fooStaticMember() {\n" +
		"    }\n" +
		"    void fooMember() {\n" +
		"    }\n" +
		"  }\n" +
		"  class Member2 {\n" +
		"    Member2() {\n" +
		"    }\n" +
		"  }\n" +
		"  public MA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:Membe>;\n" +
		"  }\n" +
		"  void foobar() {\n" +
		"  }\n" +
		"}\n" +
		"class MemberOfCUMA {\n" +
		"  MemberOfCUMA() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMA_2() {
	String str =
			"package p; \n" +
			"public class MA {\n" +
			"	class Member\n" +
			"		extends java.util.Vector {\n" +
			"		static void fooStaticMember() {\n" +
			"		}\n" +
			"		void fooMember() {\n" +
			"		}\n" +
			"		class MemberMember {\n" +
			"			void fooMemberMember() {\n" +
			"				MemberOfCUMA m = \n" +
			"					new MemberOfCUMA(); \n" +
			"			}\n" +
			"		}\n" +
			"		class MemberMember2 {\n" +
			"		}\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		Membe }\n" +
			"	void foobar() {\n" +
			"		new Member().}\n" +
			"	class Member2 {\n" +
			"	}\n" +
			"}\n" +
			"class MemberOfCUMA {\n" +
			"}\n";

	String testName = "<complete on object creation type>";
	String completeBehind = "MemberOfCU";
	String expectedCompletionNodeToString = "<CompleteOnType:MemberOfCU>";
	String completionIdentifier = "MemberOfCU";
	String expectedReplacedSource = "MemberOfCUMA";
	int cursorLocation = str.indexOf("MemberOfCUMA();") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class MA {\n" +
		"  class Member extends java.util.Vector {\n" +
		"    class MemberMember {\n" +
		"      MemberMember() {\n" +
		"      }\n" +
		"      void fooMemberMember() {\n" +
		"        MemberOfCUMA m = new <CompleteOnType:MemberOfCU>();\n" +
		"      }\n" +
		"    }\n" +
		"    class MemberMember2 {\n" +
		"      MemberMember2() {\n" +
		"      }\n" +
		"    }\n" +
		"    Member() {\n" +
		"    }\n" +
		"    static void fooStaticMember() {\n" +
		"    }\n" +
		"    void fooMember() {\n" +
		"    }\n" +
		"  }\n" +
		"  class Member2 {\n" +
		"    Member2() {\n" +
		"    }\n" +
		"  }\n" +
		"  public MA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void foobar() {\n" +
		"  }\n" +
		"}\n" +
		"class MemberOfCUMA {\n" +
		"  MemberOfCUMA() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMA_3() {
	String str =
			"package p; \n" +
			"public class MA {\n" +
			"	class Member\n" +
			"		extends java.util.Vector {\n" +
			"		static void fooStaticMember() {\n" +
			"		}\n" +
			"		void fooMember() {\n" +
			"		}\n" +
			"		class MemberMember {\n" +
			"			void fooMemberMember() {\n" +
			"				MemberOfCUMA m = \n" +
			"					new MemberOfCUMA(); \n" +
			"			}\n" +
			"		}\n" +
			"		class MemberMember2 {\n" +
			"		}\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		Membe }\n" +
			"	void foobar() {\n" +
			"		new Member().}\n" +
			"	class Member2 {\n" +
			"	}\n" +
			"}\n" +
			"class MemberOfCUMA {\n" +
			"}\n";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new Member().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Member().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class MA {\n" +
		"  class Member extends java.util.Vector {\n" +
		"    class MemberMember {\n" +
		"      MemberMember() {\n" +
		"      }\n" +
		"      void fooMemberMember() {\n" +
		"      }\n" +
		"    }\n" +
		"    class MemberMember2 {\n" +
		"      MemberMember2() {\n" +
		"      }\n" +
		"    }\n" +
		"    Member() {\n" +
		"    }\n" +
		"    static void fooStaticMember() {\n" +
		"    }\n" +
		"    void fooMember() {\n" +
		"    }\n" +
		"  }\n" +
		"  class Member2 {\n" +
		"    Member2() {\n" +
		"    }\n" +
		"  }\n" +
		"  public MA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void foobar() {\n" +
		"    <CompleteOnMemberAccess:new Member().>;\n" +
		"  }\n" +
		"}\n" +
		"class MemberOfCUMA {\n" +
		"  MemberOfCUMA() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMB_1FHSLMQ_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHSLMQ\n" +
			" */\n" +
			"public class MB {\n" +
			"	void foo() {\n" +
			"		try {\n" +
			"			System.out.println(\"\");\n" +
			"		} catch (Exception eFirst) {\n" +
			"			e } catch (Exception eSecond) {\n" +
			"			e }\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on local variable name into catch block>";
	String completeBehind = "e";
	String expectedCompletionNodeToString = "<CompleteOnName:e>";
	String completionIdentifier = "e";
	String expectedReplacedSource = "e";
	int cursorLocation = str.indexOf("e }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class MB {\n" +
		"  public MB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      Exception eFirst;\n" +
		"      <CompleteOnName:e>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMB_1FHSLMQ_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHSLMQ\n" +
			" */\n" +
			"public class MB {\n" +
			"	void foo() {\n" +
			"		try {\n" +
			"			System.out.println(\"\");\n" +
			"		} catch (Exeption eFirst) {\n" +
			"			e } catch (Exception eSecond) {\n" +
			"			e }\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on local variable name into catch block>";
	String completeBehind = "e";
	String expectedCompletionNodeToString = "<CompleteOnName:e>";
	String completionIdentifier = "e";
	String expectedReplacedSource = "e";
	int cursorLocation = str.indexOf("e }\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class MB {\n" +
		"  public MB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      Exception eSecond;\n" +
		"      <CompleteOnName:e>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMC_1FJ8D9Z() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ8D9Z\n" +
			" */\n" +
			"public class MC {\n" +
			"	p2.X someField;\n" +
			"	public void foo() {\n" +
			"		new p2.X(\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on object creation argument>";
	String completeBehind = "new p2.X(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new p2.X()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class MC {\n" +
		"  p2.X someField;\n" +
		"  public MC() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    <CompleteOnAllocationExpression:new p2.X()>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testN() {
	String str =
		"package p; \n" +
		"public class N extends p.M {\n" +
		"	void foo() {\n" +
		"		class MLocal\n" +
		"			extends Schmurz {\n" +
		"			void foo() {\n" +
		"			}\n" +
		"			int field1;\n" +
		"			class MLocalMember\n" +
		"				extends myInnerC {\n" +
		"				void foo() {\n" +
		"				}\n" +
		"				void bar() {\n" +
		"					new M }\n" +
		"			}\n" +
		"			class MLocalMember2 {\n" +
		"				void fooMyInnerC() {\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on object creation type>";
	String completeBehind = "new M";
	String expectedCompletionNodeToString = "<CompleteOnType:M>";
	String completionIdentifier = "M";
	String expectedReplacedSource = "M";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class N extends p.M {\n" +
		"  public N() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class MLocal extends Schmurz {\n" +
		"      class MLocalMember extends myInnerC {\n" +
		"        MLocalMember() {\n" +
		"        }\n" +
		"        void foo() {\n" +
		"        }\n" +
		"        void bar() {\n" +
		"          new <CompleteOnType:M>();\n" +
		"        }\n" +
		"      }\n" +
		"      class MLocalMember2 {\n" +
		"        MLocalMember2() {\n" +
		"        }\n" +
		"        void fooMyInnerC() {\n" +
		"        }\n" +
		"      }\n" +
		"      int field1;\n" +
		"      MLocal() {\n" +
		"      }\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNA_1() {
	String str =
			"package p; \n" +
			"class NException2\n" +
			"	extends NoClassDefFoundError {\n" +
			"}\n" +
			"interface NInterface {\n" +
			"	void foo();\n" +
			"}\n" +
			"class DAB {\n" +
			"	public DA foo() {\n" +
			"	}\n" +
			"	public int foufou;\n" +
			"}\n" +
			"class DANA {\n" +
			"	public int f;\n" +
			"	N fieldC;\n" +
			"}\n" +
			"public class NA\n" +
			"	extends NException2\n" +
			"	implements N {\n" +
			"	DA fieldB;\n" +
			"	class freak {\n" +
			"	}\n" +
			"	void dede() {\n" +
			"		DA local;\n" +
			"		local.fieldC.foo();\n" +
			"	}\n" +
			"}\n" +
			"interface NCool {\n" +
			"}\n";

	String testName = "<complete on local variable name>";
	String completeBehind = "l";
	String expectedCompletionNodeToString = "<CompleteOnName:l>";
	String completionIdentifier = "l";
	String expectedReplacedSource = "local";
	int cursorLocation = str.indexOf("local.") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class NException2 extends NoClassDefFoundError {\n" +
		"  NException2() {\n" +
		"  }\n" +
		"}\n" +
		"interface NInterface {\n" +
		"  void foo();\n" +
		"}\n" +
		"class DAB {\n" +
		"  public int foufou;\n" +
		"  DAB() {\n" +
		"  }\n" +
		"  public DA foo() {\n" +
		"  }\n" +
		"}\n" +
		"class DANA {\n" +
		"  public int f;\n" +
		"  N fieldC;\n" +
		"  DANA() {\n" +
		"  }\n" +
		"}\n" +
		"public class NA extends NException2 implements N {\n" +
		"  class freak {\n" +
		"    freak() {\n" +
		"    }\n" +
		"  }\n" +
		"  DA fieldB;\n" +
		"  public NA() {\n" +
		"  }\n" +
		"  void dede() {\n" +
		"    DA local;\n" +
		"    <CompleteOnName:l>;\n" +
		"  }\n" +
		"}\n" +
		"interface NCool {\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNA_2() {
	String str =
			"package p; \n" +
			"class NException2\n" +
			"	extends NoClassDefFoundError {\n" +
			"}\n" +
			"interface NInterface {\n" +
			"	void foo();\n" +
			"}\n" +
			"class DAB {\n" +
			"	public DA foo() {\n" +
			"	}\n" +
			"	public int foufou;\n" +
			"}\n" +
			"class DANA {\n" +
			"	public int f;\n" +
			"	N fieldC;\n" +
			"}\n" +
			"public class NA\n" +
			"	extends NException2\n" +
			"	implements N {\n" +
			"	DA fieldB;\n" +
			"	class freak {\n" +
			"	}\n" +
			"	void dede() {\n" +
			"		DA local;\n" +
			"		local.fieldC.foo();\n" +
			"	}\n" +
			"}\n" +
			"interface NCool {\n" +
			"}\n";

	String testName = "<complete on method/field of local variable>";
	String completeBehind = "local.f";
	String expectedCompletionNodeToString = "<CompleteOnName:local.f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "local.fieldC";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class NException2 extends NoClassDefFoundError {\n" +
		"  NException2() {\n" +
		"  }\n" +
		"}\n" +
		"interface NInterface {\n" +
		"  void foo();\n" +
		"}\n" +
		"class DAB {\n" +
		"  public int foufou;\n" +
		"  DAB() {\n" +
		"  }\n" +
		"  public DA foo() {\n" +
		"  }\n" +
		"}\n" +
		"class DANA {\n" +
		"  public int f;\n" +
		"  N fieldC;\n" +
		"  DANA() {\n" +
		"  }\n" +
		"}\n" +
		"public class NA extends NException2 implements N {\n" +
		"  class freak {\n" +
		"    freak() {\n" +
		"    }\n" +
		"  }\n" +
		"  DA fieldB;\n" +
		"  public NA() {\n" +
		"  }\n" +
		"  void dede() {\n" +
		"    DA local;\n" +
		"    <CompleteOnName:local.f>;\n" +
		"  }\n" +
		"}\n" +
		"interface NCool {\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNA_3() {
	String str =
			"package p; \n" +
			"class NException2\n" +
			"	extends NoClassDefFoundError {\n" +
			"}\n" +
			"interface NInterface {\n" +
			"	void foo();\n" +
			"}\n" +
			"class DAB {\n" +
			"	public DA foo() {\n" +
			"	}\n" +
			"	public int foufou;\n" +
			"}\n" +
			"class DANA {\n" +
			"	public int f;\n" +
			"	N fieldC;\n" +
			"}\n" +
			"public class NA\n" +
			"	extends NException2\n" +
			"	implements N {\n" +
			"	DA fieldB;\n" +
			"	class freak {\n" +
			"	}\n" +
			"	void dede() {\n" +
			"		DA local;\n" +
			"		local.fieldC.foo();\n" +
			"	}\n" +
			"}\n" +
			"interface NCool {\n" +
			"}\n";

	String testName = "<complete on method/field of local variable>";
	String completeBehind = "local.fieldC.";
	String expectedCompletionNodeToString = "<CompleteOnName:local.fieldC.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "local.fieldC.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class NException2 extends NoClassDefFoundError {\n" +
		"  NException2() {\n" +
		"  }\n" +
		"}\n" +
		"interface NInterface {\n" +
		"  void foo();\n" +
		"}\n" +
		"class DAB {\n" +
		"  public int foufou;\n" +
		"  DAB() {\n" +
		"  }\n" +
		"  public DA foo() {\n" +
		"  }\n" +
		"}\n" +
		"class DANA {\n" +
		"  public int f;\n" +
		"  N fieldC;\n" +
		"  DANA() {\n" +
		"  }\n" +
		"}\n" +
		"public class NA extends NException2 implements N {\n" +
		"  class freak {\n" +
		"    freak() {\n" +
		"    }\n" +
		"  }\n" +
		"  DA fieldB;\n" +
		"  public NA() {\n" +
		"  }\n" +
		"  void dede() {\n" +
		"    DA local;\n" +
		"    <CompleteOnName:local.fieldC.>;\n" +
		"  }\n" +
		"}\n" +
		"interface NCool {\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNB() {
	String str =
			"package p; \n" +
			"public class NB {\n" +
			"	void foo() {\n" +
			"		int iOutside;\n" +
			"		if (i != 0) {\n" +
			"			for (int i = 10; --i >= 0;)\n" +
			"				unit[i].parseMethod(\n" +
			"					parser, \n" +
			"					unit); \n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on variable name into for statement>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i >=") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class NB {\n" +
		"  public NB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int iOutside;\n" +
		"    {\n" +
		"      int i;\n" +
		"      -- <CompleteOnName:i>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNC_1FJ8D9Z() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ8D9Z\n" +
			" */\n" +
			"public class NC {\n" +
			"	String s = new String(\n";

	String testName = "<complete on field intializer into corrupted class declaration>";
	String completeBehind = "new String(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new String()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class NC {\n" +
		"  String s = <CompleteOnAllocationExpression:new String()>;\n" +
		"  public NC() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testO_1FG1YU0() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FG1YU0\n" +
		" */\n" +
		"public class O\n" +
		"	extends java.util.Vector {\n" +
		"	void bar(boolean bbbb) {\n" +
		"		this.}\n" +
		"}\n";

	String testName = "<complete on method/field of explicit this>";
	String completeBehind = "this.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "this.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class O extends java.util.Vector {\n" +
		"  public O() {\n" +
		"  }\n" +
		"  void bar(boolean bbbb) {\n" +
		"    <CompleteOnMemberAccess:this.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOA_1() {
	String str =
			"package p; \n" +
			"public class OA {\n" +
			"	void proc() {\n" +
			"		int[] a = new int[10];\n" +
			"		Object b = a;\n" +
			"		Class c = a.getClass();\n" +
			"		String s = a.toString();\n" +
			"		boolean l = a.equals(b);\n" +
			"		int h = a.hashCode();\n" +
			"		try {\n" +
			"			a.wait();\n" +
			"			a.wait(3);\n" +
			"			a.wait(4, 5);\n" +
			"		} catch (Exception e) {\n" +
			"		}\n" +
			"		a.notify();\n" +
			"		a.notifyAll();\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of array>";
	String completeBehind = "a.n";
	String expectedCompletionNodeToString = "<CompleteOnName:a.n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "a.notify";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class OA {\n" +
		"  public OA() {\n" +
		"  }\n" +
		"  void proc() {\n" +
		"    int[] a;\n" +
		"    Object b;\n" +
		"    Class c;\n" +
		"    String s;\n" +
		"    boolean l;\n" +
		"    int h;\n" +
		"    <CompleteOnName:a.n>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOA_2() {
	String str =
			"package p; \n" +
			"public class OA {\n" +
			"	void proc() {\n" +
			"		int[] a = new int[10];\n" +
			"		Object b = a;\n" +
			"		Class c = a.getClass();\n" +
			"		String s = a.toString();\n" +
			"		boolean l = a.equals(b);\n" +
			"		int h = a.hashCode();\n" +
			"		try {\n" +
			"			a.wait();\n" +
			"			a.wait(3);\n" +
			"			a.wait(4, 5);\n" +
			"		} catch (Exception e) {\n" +
			"		}\n" +
			"		a.notify();\n" +
			"		a.notifyAll();\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of array>";
	String completeBehind = "a.w";
	String expectedCompletionNodeToString = "<CompleteOnName:a.w>";
	String completionIdentifier = "w";
	String expectedReplacedSource = "a.wait";
	int cursorLocation = str.indexOf("a.wait(4, 5)") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class OA {\n" +
		"  public OA() {\n" +
		"  }\n" +
		"  void proc() {\n" +
		"    int[] a;\n" +
		"    Object b;\n" +
		"    Class c;\n" +
		"    String s;\n" +
		"    boolean l;\n" +
		"    int h;\n" +
		"    {\n" +
		"      <CompleteOnName:a.w>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOB_1() {
	String str =
			"package p; \n" +
			"public class OB {\n" +
			"	void foo() {\n" +
			"		label : while (true) {\n" +
			"			System.out.println(\"\");\n" +
			"			break label;\n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on keyword>";
	String completeBehind = "b";
	String expectedCompletionNodeToString = "<CompleteOnName:b>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "break";
	int cursorLocation = str.indexOf("break") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class OB {\n" +
		"  public OB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:b>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOB_2() {
	String str =
			"package p; \n" +
			"public class OB {\n" +
			"	void foo() {\n" +
			"		label : while (true) {\n" +
			"			System.out.println(\"\");\n" +
			"			break label;\n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on label name>";
	String completeBehind = "l";
	String expectedCompletionNodeToString = "<CompleteOnName:l>";
	String completionIdentifier = "l";
	String expectedReplacedSource = "label";
	int cursorLocation = str.indexOf("label") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class OB {\n" +
		"  public OB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:l>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOC_1FM7J7F() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FM7J7F\n" +
			" */\n" +
			"class OC {\n" +
			"	String s = new String(\n" +
			"}\n";

	String testName = "<complete on field initializer>";
	String completeBehind = "new String(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new String()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class OC {\n" +
		"  String s = <CompleteOnAllocationExpression:new String()>;\n" +
		"  OC() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testP_1FG1YU0() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FG1YU0\n" +
		" */\n" +
		"public class P {\n" +
		"	{\n" +
		"		void bar() {\n" +
		"			f }\n" +
		"		void foo() {\n" +
		"		}\n" +
		"	}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class P {\n" +
		"  {\n" +
		"  }\n" +
		"  public P() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"    <CompleteOnName:f>;\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testPA_1() {
	String str =
			"package p; \n" +
			"class PAHelper {\n" +
			"	public int fieldPublic;\n" +
			"	protected int fieldProtected;\n" +
			"	private int fieldPrivate;\n" +
			"	int fieldDefault;\n" +
			"	static void staticFoo() {\n" +
			"	}\n" +
			"	static int i = 1;\n" +
			"	int neuneu1() {\n" +
			"		return 0;\n" +
			"	}\n" +
			"	void neuneu2() {\n" +
			"	}\n" +
			"}\n" +
			"public class PA\n" +
			"	extends PAHelper {\n" +
			"	void foo() {\n" +
			"		B[] b = \n" +
			"			new java.lang.Number[]; \n" +
			"		java.lang.Short s;\n" +
			"		// b[1].;\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on comment>";
	String completeBehind = "b[1].";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
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
public void testPA_2() {
	String str =
			"package p; \n" +
			"class PAHelper {\n" +
			"	public int fieldPublic;\n" +
			"	protected int fieldProtected;\n" +
			"	private int fieldPrivate;\n" +
			"	int fieldDefault;\n" +
			"	static void staticFoo() {\n" +
			"	}\n" +
			"	static int i = 1;\n" +
			"	int neuneu1() {\n" +
			"		return 0;\n" +
			"	}\n" +
			"	void neuneu2() {\n" +
			"	}\n" +
			"}\n" +
			"public class PA\n" +
			"	extends PAHelper {\n" +
			"	void foo() {\n" +
			"		B[] b = \n" +
			"			new java.lang.Number[]; \n" +
			"		java.lang.Short s;\n" +
			"		// b[1].;\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on new keyword>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "new";
	int cursorLocation = str.indexOf("new ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class PAHelper {\n" +
		"  public int fieldPublic;\n" +
		"  protected int fieldProtected;\n" +
		"  private int fieldPrivate;\n" +
		"  int fieldDefault;\n" +
		"  static int i;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  PAHelper() {\n" +
		"  }\n" +
		"  static void staticFoo() {\n" +
		"  }\n" +
		"  int neuneu1() {\n" +
		"  }\n" +
		"  void neuneu2() {\n" +
		"  }\n" +
		"}\n" +
		"public class PA extends PAHelper {\n" +
		"  public PA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    B[] b = <CompleteOnName:n>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testPB() {
	String str =
			"package p; \n" +
			"public class PB {\n" +
			"	void foo() {\n" +
			"		class Local {\n" +
			"			void foo() {\n" +
			"			}\n" +
			"			class LocalMember1 {\n" +
			"				void foo() {\n" +
			"					class LocalMemberLocal {\n" +
			"						void foo() {\n" +
			"							f\n"+
			"						}\n" +
			"					}\n" +
			"				}\n" +
			"			}\n" +
			"			class LocalMember2 {\n" +
			"				void foo() {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field into nested local type>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class PB {\n" +
		"  public PB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class Local {\n" +
		"      class LocalMember1 {\n" +
		"        LocalMember1() {\n" +
		"        }\n" +
		"        void foo() {\n" +
		"          class LocalMemberLocal {\n" +
		"            LocalMemberLocal() {\n" +
		"            }\n" +
		"            void foo() {\n" +
		"              <CompleteOnName:f>;\n" +
		"            }\n" +
		"          }\n" +
		"        }\n" +
		"      }\n" +
		"      class LocalMember2 {\n" +
		"        LocalMember2() {\n" +
		"        }\n" +
		"        void foo() {\n" +
		"        }\n" +
		"      }\n" +
		"      Local() {\n" +
		"      }\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testPC_1FSU4EF() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FSU4EF\n" +
			" */\n" +
			"import java.util.Vector;\n" +
			"public class PC {\n" +
			"	void foo() {\n" +
			"		class Inner {\n" +
			"			Vector v = new Vector();\n" +
			"			void foo() {\n" +
			"				Vector v = new Vector();\n" +
			"				v.addElement();\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field into local type>";
	String completeBehind = "v.a";
	String expectedCompletionNodeToString = "<CompleteOnName:v.a>";
	String completionIdentifier = "a";
	String expectedReplacedSource = "v.addElement";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.util.Vector;\n" +
		"public class PC {\n" +
		"  public PC() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class Inner {\n" +
		"      Vector v;\n" +
		"      Inner() {\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        Vector v;\n" +
		"        <CompleteOnName:v.a>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQ_1FG1YU0() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FG1YU0\n" +
		" */\n" +
		"public class Q {\n" +
		"	void bar(boolean bbbb) {\n" +
		"		this.}\n" +
		"}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "this.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "this.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class Q {\n" +
		"  public Q() {\n" +
		"  }\n" +
		"  void bar(boolean bbbb) {\n" +
		"    <CompleteOnMemberAccess:this.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQA_1() {
	String str =
			"package p; \n" +
			"class QAHelper {\n" +
			"	int i = 10;\n" +
			"	void f() {\n" +
			"		Chk.chkIntVal(\n" +
			"			\"err_0\", \n" +
			"			\"i\", \n" +
			"			this.i, \n" +
			"			i); \n" +
			"	}\n" +
			"	static class Y\n" +
			"		extends QAHelper {\n" +
			"		public void f() {\n" +
			"			super.f();\n" +
			"			int j = super.i;\n" +
			"		}\n" +
			"		public static void main(String a[]) {\n" +
			"			Y oy = new Y();\n" +
			"			oy.f();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"public class QA {\n" +
			"	static String s[] = \n" +
			"		{\"Dolby\", \"Thx\",}; \n" +
			"	void check() {\n" +
			"		new QAHelper().new Y().main(\n" +
			"			s); \n" +
			"	}\n" +
			"	static public void main(String args[]) {\n" +
			"		new QA().check();\n" +
			"		Chk.endTest(\"ciner111\");\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "new QAHelper().new Y().m";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new QAHelper().new Y().m>";
	String completionIdentifier = "m";
	String expectedReplacedSource = "main";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class QAHelper {\n" +
		"  static class Y extends QAHelper {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    public void f() {\n" +
		"    }\n" +
		"    public static void main(String[] a) {\n" +
		"    }\n" +
		"  }\n" +
		"  int i;\n" +
		"  QAHelper() {\n" +
		"  }\n" +
		"  void f() {\n" +
		"  }\n" +
		"}\n" +
		"public class QA {\n" +
		"  static String[] s;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public QA() {\n" +
		"  }\n" +
		"  void check() {\n" +
		"    <CompleteOnMemberAccess:new QAHelper().new Y().m>;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQA_2() {
	String str =
			"package p; \n" +
			"class QAHelper {\n" +
			"	int i = 10;\n" +
			"	void f() {\n" +
			"		Chk.chkIntVal(\n" +
			"			\"err_0\", \n" +
			"			\"i\", \n" +
			"			this.i, \n" +
			"			i); \n" +
			"	}\n" +
			"	static class Y\n" +
			"		extends QAHelper {\n" +
			"		public void f() {\n" +
			"			super.f();\n" +
			"			int j = super.i;\n" +
			"		}\n" +
			"		public static void main(String a[]) {\n" +
			"			Y oy = new Y();\n" +
			"			oy.f();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"public class QA {\n" +
			"	static String s[] = \n" +
			"		{\"Dolby\", \"Thx\",}; \n" +
			"	void check() {\n" +
			"		new QAHelper().new Y().main(\n" +
			"			s); \n" +
			"	}\n" +
			"	static public void main(String args[]) {\n" +
			"		new QA().check();\n" +
			"		Chk.endTest(\"ciner111\");\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new QAHelper().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new QAHelper().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class QAHelper {\n" +
		"  static class Y extends QAHelper {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    public void f() {\n" +
		"    }\n" +
		"    public static void main(String[] a) {\n" +
		"    }\n" +
		"  }\n" +
		"  int i;\n" +
		"  QAHelper() {\n" +
		"  }\n" +
		"  void f() {\n" +
		"  }\n" +
		"}\n" +
		"public class QA {\n" +
		"  static String[] s;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public QA() {\n" +
		"  }\n" +
		"  void check() {\n" +
		"    <CompleteOnMemberAccess:new QAHelper().>;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQA_3() {
	String str =
			"package p; \n" +
			"class QAHelper {\n" +
			"	int i = 10;\n" +
			"	void f() {\n" +
			"		Chk.chkIntVal(\n" +
			"			\"err_0\", \n" +
			"			\"i\", \n" +
			"			this.i, \n" +
			"			i); \n" +
			"	}\n" +
			"	static class Y\n" +
			"		extends QAHelper {\n" +
			"		public void f() {\n" +
			"			super.f();\n" +
			"			int j = super.i;\n" +
			"		}\n" +
			"		public static void main(String a[]) {\n" +
			"			Y oy = new Y();\n" +
			"			oy.f();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"public class QA {\n" +
			"	static String s[] = \n" +
			"		{\"Dolby\", \"Thx\",}; \n" +
			"	void check() {\n" +
			"		new QAHelper().new Y().main(\n" +
			"			s); \n" +
			"	}\n" +
			"	static public void main(String args[]) {\n" +
			"		new QA().check();\n" +
			"		Chk.endTest(\"ciner111\");\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new QAHelper().new Y().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new QAHelper().new Y().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class QAHelper {\n" +
		"  static class Y extends QAHelper {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    public void f() {\n" +
		"    }\n" +
		"    public static void main(String[] a) {\n" +
		"    }\n" +
		"  }\n" +
		"  int i;\n" +
		"  QAHelper() {\n" +
		"  }\n" +
		"  void f() {\n" +
		"  }\n" +
		"}\n" +
		"public class QA {\n" +
		"  static String[] s;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public QA() {\n" +
		"  }\n" +
		"  void check() {\n" +
		"    <CompleteOnMemberAccess:new QAHelper().new Y().>;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQA_4() {
	String str =
			"package p; \n" +
			"class QAHelper {\n" +
			"	int i = 10;\n" +
			"	void f() {\n" +
			"		Chk.chkIntVal(\n" +
			"			\"err_0\", \n" +
			"			\"i\", \n" +
			"			this.i, \n" +
			"			i); \n" +
			"	}\n" +
			"	static class Y\n" +
			"		extends QAHelper {\n" +
			"		public void f() {\n" +
			"			super.f();\n" +
			"			int j = super.i;\n" +
			"		}\n" +
			"		public static void main(String a[]) {\n" +
			"			Y oy = new Y();\n" +
			"			oy.f();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"public class QA {\n" +
			"	static String s[] = \n" +
			"		{\"Dolby\", \"Thx\",}; \n" +
			"	void check() {\n" +
			"		new QAHelper().new Y().main(\n" +
			"			s); \n" +
			"	}\n" +
			"	static public void main(String args[]) {\n" +
			"		new QA().check();\n" +
			"		Chk.endTest(\"ciner111\");\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new QA().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new QA().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class QAHelper {\n" +
		"  static class Y extends QAHelper {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    public void f() {\n" +
		"    }\n" +
		"    public static void main(String[] a) {\n" +
		"    }\n" +
		"  }\n" +
		"  int i;\n" +
		"  QAHelper() {\n" +
		"  }\n" +
		"  void f() {\n" +
		"  }\n" +
		"}\n" +
		"public class QA {\n" +
		"  static String[] s;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public QA() {\n" +
		"  }\n" +
		"  void check() {\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    <CompleteOnMemberAccess:new QA().>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQB_1FIK820() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FIK820\n" +
			" */\n" +
			"public class QB {\n" +
			"	void foo() {\n" +
			"		{\n" +
			"		}\n" +
			"		.}\n" +
			"}\n";

	String testName = "<complete on block (no answers wanted)>";
	String completeBehind = ".";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(".}") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class QB {\n" +
		"  public QB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testR_1FGD31E() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGD31E\n" +
		" */\n" +
		"public class R {\n" +
		"	void moo() {\n" +
		"		b }\n" +
		"	void bar() {\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "b";
	String expectedCompletionNodeToString = "<CompleteOnName:b>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "b";
	int cursorLocation = str.indexOf("b }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class R {\n" +
		"  public R() {\n" +
		"  }\n" +
		"  void moo() {\n" +
		"    <CompleteOnName:b>;\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testRA_1() {
	String str =
			"package p; \n" +
			"public class RA extends A {\n" +
			"	private int f = 5;\n" +
			"	int i(int k) {\n" +
			"	}\n" +
			"	class B extends I {\n" +
			"		void foo();\n" +
			"		class C extends Z {\n" +
			"		}\n" +
			"		final int fo;\n" +
			"	}\n" +
			"	final void foo(k j) {\n" +
			"	}\n" +
			"	o o() throws Exc, Exc {\n" +
			"	}\n" +
			"	static {\n" +
			"		this.ff = 5;\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on incorrect this call>";
	String completeBehind = "t";
	String expectedCompletionNodeToString = "<CompleteOnName:t>";
	String completionIdentifier = "t";
	String expectedReplacedSource = "this";
	int cursorLocation = str.indexOf("this.ff") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class RA extends A {\n" +
		"  class B extends I {\n" +
		"    class C extends Z {\n" +
		"      C() {\n" +
		"      }\n" +
		"    }\n" +
		"    final int fo;\n" +
		"    B() {\n" +
		"    }\n" +
		"    void foo();\n" +
		"  }\n" +
		"  private int f;\n" +
		"  static {\n" +
		"    <CompleteOnName:t>;\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public RA() {\n" +
		"  }\n" +
		"  int i(int k) {\n" +
		"  }\n" +
		"  final void foo(k j) {\n" +
		"  }\n" +
		"  o o() throws Exc, Exc {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testRA_2() {
	String str =
			"package p; \n" +
			"public class RA extends A {\n" +
			"	private int f = 5;\n" +
			"	int i(int k) {\n" +
			"	}\n" +
			"	class B extends I {\n" +
			"		void foo();\n" +
			"		class C extends Z {\n" +
			"		}\n" +
			"		final int fo;\n" +
			"	}\n" +
			"	final void foo(k j) {\n" +
			"	}\n" +
			"	o o() throws Exc, Exc {\n" +
			"	}\n" +
			"	static {\n" +
			"		this.ff = 5;\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on t>";
	String completeBehind = "t";
	String expectedCompletionNodeToString = "<CompleteOnName:t>";
	String completionIdentifier = "t";
	String expectedReplacedSource = "this";
	int cursorLocation = str.indexOf("this") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class RA extends A {\n" +
		"  class B extends I {\n" +
		"    class C extends Z {\n" +
		"      C() {\n" +
		"      }\n" +
		"    }\n" +
		"    final int fo;\n" +
		"    B() {\n" +
		"    }\n" +
		"    void foo();\n" +
		"  }\n" +
		"  private int f;\n" +
		"  static {\n" +
		"    <CompleteOnName:t>;\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public RA() {\n" +
		"  }\n" +
		"  int i(int k) {\n" +
		"  }\n" +
		"  final void foo(k j) {\n" +
		"  }\n" +
		"  o o() throws Exc, Exc {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testRA_3() {
	String str =
			"package p; \n" +
			"public class RA extends A {\n" +
			"	private int f = 5;\n" +
			"	int i(int k) {\n" +
			"	}\n" +
			"	class B extends I {\n" +
			"		void foo();\n" +
			"		class C extends Z {\n" +
			"		}\n" +
			"		final int fo;\n" +
			"	}\n" +
			"	final void foo(k j) {\n" +
			"	}\n" +
			"	o o() throws Exc, Exc {\n" +
			"	}\n" +
			"	static {\n" +
			"		this.ff = 5;\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on exception type>";
	String completeBehind = "Exc";
	String expectedCompletionNodeToString = "<CompleteOnException:Exc>";
	String completionIdentifier = "Exc";
	String expectedReplacedSource = "Exc";
	int cursorLocation = str.indexOf("Exc {") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class RA extends A {\n" +
		"  class B extends I {\n" +
		"    class C extends Z {\n" +
		"      C() {\n" +
		"      }\n" +
		"    }\n" +
		"    final int fo;\n" +
		"    B() {\n" +
		"    }\n" +
		"    void foo();\n" +
		"  }\n" +
		"  private int f;\n" +
		"  static {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public RA() {\n" +
		"  }\n" +
		"  int i(int k) {\n" +
		"  }\n" +
		"  final void foo(k j) {\n" +
		"  }\n" +
		"  o o() throws Exc, <CompleteOnException:Exc> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testRB_1FI74S3() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FI74S3\n" +
			" */\n" +
			"public class RB {\n" +
			"	int[] table;\n" +
			"	void foo() {\n" +
			"		int x = table.}\n" +
			"}\n";

	String testName = "<complete on method/field of arry>";
	String completeBehind = "table.";
	String expectedCompletionNodeToString = "<CompleteOnName:table.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "table.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class RB {\n" +
		"  int[] table;\n" +
		"  public RB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int x = <CompleteOnName:table.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testS_1FGF64P_1() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGF64P\n" +
		" */\n" +
		"public class S {\n" +
		"	{\n" +
		"		new Y()..}\n" +
		"	class Y {\n" +
		"		void foo() {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on incorrect call>";
	String completeBehind = "new Y()..";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class S {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"    {\n" +
		"      <CompleteOnName:>;\n" +
		"    }\n" +
		"  }\n" +
		"  public S() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testS_1FGF64P_2() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGF64P\n" +
		" */\n" +
		"public class S {\n" +
		"	{\n" +
		"		new Y()..}\n" +
		"	class Y {\n" +
		"		void foo() {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new Y().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Y().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class S {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"    <CompleteOnMemberAccess:new Y().>;\n" +
		"  }\n" +
		"  public S() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testS_1FGF64P_3() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGF64P\n" +
		" */\n" +
		"public class S {\n" +
		"	{\n" +
		"		new Y()..}\n" +
		"	class Y {\n" +
		"		void foo() {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on incorrect call>";
	String completeBehind = "new Y()..";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class S {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"    {\n" +
		"      <CompleteOnName:>;\n" +
		"    }\n" +
		"  }\n" +
		"  public S() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testSA() {
	String str =
			"package p; \n" +
			"public class SA {\n" +
			"	public sy void foo() {\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method modifier>";
	String completeBehind = "sy";
	String expectedCompletionNodeToString = "<CompleteOnType:sy>";
	String completionIdentifier = "sy";
	String expectedReplacedSource = "sy";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class SA {\n" +
		"  <CompleteOnType:sy>;\n" +
		"  public SA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testSB_1FILFDG() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FILFDG\n" +
			" */\n" +
			"public class SB {\n" +
			"	public void foo() {\n" +
			"		String s = \"hello\n" +
			"		int}\n" +
			"}\n";

	String testName = "<complete on field declaration type>";
	String completeBehind = "int";
	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class SB {\n" +
		"  public SB() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    String s;\n" +
		"    <CompleteOnName:int>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testT_1FGF64P() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGF64P\n" +
		" */\n" +
		"public class T {\n" +
		"	{\n" +
		"		new Y().}\n" +
		"	class Y {\n" +
		"		void foo() {\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on object creation>";
	String completeBehind = "new Y().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Y().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class T {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"    <CompleteOnMemberAccess:new Y().>;\n" +
		"  }\n" +
		"  public T() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testTA_1FHISJJ_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHISJJ\n" +
			" */\n" +
			"public class TA {\n" +
			"	void foo() {\n" +
			"		Object[] items = \n" +
			"			{\n" +
			"				\"Mark unublishable\", \n" +
			"				null, \n" +
			"				\"Properties...\"}\n" +
			"		.;\n" +
			"		items.}\n" +
			"}\n";

	String testName = "<complete on array intializer value>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "null";
	int cursorLocation = str.indexOf("null, ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class TA {\n" +
		"  public TA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object[] items = {<CompleteOnName:n>};\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testTA_1FHISJJ_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHISJJ\n" +
			" */\n" +
			"public class TA {\n" +
			"	void foo() {\n" +
			"		Object[] items = \n" +
			"			{\n" +
			"				\"Mark unublishable\", \n" +
			"				null, \n" +
			"				\"Properties...\"}\n" +
			"		.;\n" +
			"		items.}\n" +
			"}\n";

	String testName = "<complete on method/field of array intializer>";
	String completeBehind =
			"			{\n" +
			"				\"Mark unublishable\", \n" +
			"				null, \n" +
			"				\"Properties...\"}\n" +
			"		.";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class TA {\n" +
		"  public TA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object[] items;\n" +
		"    <CompleteOnName:>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testTA_1FHISJJ_3() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHISJJ\n" +
			" */\n" +
			"public class TA {\n" +
			"	void foo() {\n" +
			"		Object[] items = \n" +
			"			{\n" +
			"				\"Mark unublishable\", \n" +
			"				null, \n" +
			"				\"Properties...\"}\n" +
			"		.;\n" +
			"		items.}\n" +
			"}\n";

	String testName = "<complete on method/field of array>";
	String completeBehind = "items.";
	String expectedCompletionNodeToString = "<CompleteOnName:items.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "items.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class TA {\n" +
		"  public TA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object[] items;\n" +
		"    <CompleteOnName:items.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testTB_1FHSLMQ() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHSLMQ\n" +
			" */\n" +
			"public class TB {\n" +
			"	void foo() {\n" +
			"		if (true)\n" +
			"			System.out.println(\"\");\n" +
			"		e }\n" +
			"}\n";

	String testName = "<complete on else keyword>";
	String completeBehind = "e";
	String expectedCompletionNodeToString = "<CompleteOnName:e>";
	String completionIdentifier = "e";
	String expectedReplacedSource = "e";
	int cursorLocation = str.indexOf("e }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class TB {\n" +
		"  public TB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:e>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testU_1FGGUME() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGGUME\n" +
		" */\n" +
		"public class U {\n" +
		"	public static final int Source = \n" +
		"		5; \n" +
		"}\n";

	String testName = "<complete on digit>";
	String completeBehind = "5";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
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
public void testUA_1FHISJJ_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHISJJ\n" +
			" */\n" +
			"public class UA {\n" +
			"	void foo() {\n" +
			"		Object[] items = \n" +
			"			new String[] {\n" +
			"				\"Mark unublishable\", \n" +
			"				null, \n" +
			"				\"Properties...\"}\n" +
			"		.;\n" +
			"		items.}\n" +
			"}\n";

	String testName = "<complete on array initializer>";
	String completeBehind =
			"new String[] {\n" +
			"				\"Mark unublishable\", \n" +
			"				null, \n" +
			"				\"Properties...\"}\n" +
			"		.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new String[]{\"Mark unublishable\", null, \"Properties...\"}.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class UA {\n" +
		"  public UA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object[] items = <CompleteOnMemberAccess:new String[]{\"Mark unublishable\", null, \"Properties...\"}.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testUA_1FHISJJ_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHISJJ\n" +
			" */\n" +
			"public class UA {\n" +
			"	void foo() {\n" +
			"		Object[] items = \n" +
			"			new String[] {\n" +
			"				\"Mark unublishable\", \n" +
			"				null, \n" +
			"				\"Properties...\"}\n" +
			"		.;\n" +
			"		items.}\n" +
			"}\n";

	String testName = "<complete on method/field of array>";
	String completeBehind = "items.";
	String expectedCompletionNodeToString = "<CompleteOnName:items.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "items.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class UA {\n" +
		"  public UA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object[] items;\n" +
		"    <CompleteOnName:items.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testUB_1FSBZ02() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FSBZ02\n" +
			" */\n" +
			"class UB {\n" +
			"	void bar() {\n" +
			"	}\n" +
			"	class UBMember {\n" +
			"		void bar2() {\n" +
			"		}\n" +
			"		void foo() {\n" +
			"			b\n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on keyword>";
	String completeBehind = "b";
	String expectedCompletionNodeToString = "<CompleteOnName:b>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "b";
	int cursorLocation = str.indexOf("b\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class UB {\n" +
		"  class UBMember {\n" +
		"    UBMember() {\n" +
		"    }\n" +
		"    void bar2() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:b>;\n" +
		"    }\n" +
		"  }\n" +
		"  UB() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testV_1FGGUOO_1() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGGUOO\n" +
		" */\n" +
		"public class V i java\n" +
		"	.io\n" +
		"	.Serializable {\n" +
		"}\n";

	String testName = "<complete on implements keyword>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i java") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class V extends <CompleteOnKeyword:i> {\n" +
		"  {\n" +
		"  }\n" +
		"  public V() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testV_1FGGUOO_2() {
	String str =
		"package x.y.z; \n" +
		"/**\n" +
		" * 1FGGUOO\n" +
		" */\n" +
		"public class V implements java.io.Serializable {\n" +
		"}\n";

	String testName = "<complete on package>";
	String completeBehind = "y";
	String expectedCompletionNodeToString = "<CompleteOnPackage:x.y>";
	String completionIdentifier = "y";
	String expectedReplacedSource =
		"x.y.z";
	int cursorLocation = str.indexOf("y") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package <CompleteOnPackage:x.y>;\n" +
		"public class V implements java.io.Serializable {\n" +
		"  public V() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testVA_1FHISJJ_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHISJJ\n" +
			" */\n" +
			"public class VA {\n" +
			"	void foo() {\n" +
			"		Object item = new String() {\n" +
			"			public boolean equals() {\n" +
			"				return false;\n" +
			"			}\n" +
			"		}\n" +
			"		.;\n" +
			"		item.}\n" +
			"}\n";

	String testName = "<complete on anonymous type declaration>";
	String completeBehind =
			"new String() {\n" +
			"			public boolean equals() {\n" +
			"				return false;\n" +
			"			}\n" +
			"		}\n" +
			"		.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new String() {\n" +
		"  public boolean equals() {\n" +
		"    return false;\n" +
		"  }\n" +
		"}.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class VA {\n" +
		"  public VA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object item = <CompleteOnMemberAccess:new String() {\n" +
		"  public boolean equals() {\n" +
		"    return false;\n" +
		"  }\n" +
		"}.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testVA_1FHISJJ_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FHISJJ\n" +
			" */\n" +
			"public class VA {\n" +
			"	void foo() {\n" +
			"		Object item = new String() {\n" +
			"			public boolean equals() {\n" +
			"				return false;\n" +
			"			}\n" +
			"		}\n" +
			"		.;\n" +
			"		item.}\n" +
			"}\n";

	String testName = "<complete on local variable>";
	String completeBehind = "item.";
	String expectedCompletionNodeToString = "<CompleteOnName:item.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "item.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class VA {\n" +
		"  public VA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object item;\n" +
		"    <CompleteOnName:item.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testVB_1() {
	String str =
		"package p; \n" +
		"public class VB {\n" +
		"	void foo() {\n" +
		"		new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}\n" +
		"		.;\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on local variable name into anonymous declaration>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "v";
	int cursorLocation = str.indexOf("v }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class VB {\n" +
		"  public VB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new java.io.File(\"error\") {\n" +
		"      protected void runTest() {\n" +
		"        Vector v11111;\n" +
		"        <CompleteOnName:v>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// TODO excluded test (completion on field access on anonymous inner class with syntax error)
public void _testVB_2() {
	String str =
		"package p; \n" +
		"public class VB {\n" +
		"	void foo() {\n" +
		"		new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}.\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on anonymous type declaration>";
	String completeBehind =
		"new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}.";
	String expectedCompletionNodeToString =
		"<CompleteOnMemberAccess:new java.io.File(\"error\") {\n" +
		"  protected void runTest() {\n" +
		"  }\n" +
		"}.>";
	String completionIdentifier = "";
	String expectedReplacedSource =
		"new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class VB {\n" +
		"  public VB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:new java.io.File(\"error\") {\n" +
		"  protected void runTest() {\n" +
		"  }\n" +
		"}.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testVB_3() {
	String str =
		"package p; \n" +
		"public class VB {\n" +
		"	void foo() {\n" +
		"		new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}\n" +
		"		.;\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on constructor>";
	String completeBehind = "new java.io.File(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new java.io.File()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class VB {\n" +
		"  public VB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnAllocationExpression:new java.io.File()>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// TODO excluded test (completion on field access on anonymous inner class with syntax error)
public void _testVB_4() {
	String str =
		"package p; \n" +
		"public class VB {\n" +
		"	void foo() {\n" +
		"		new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}\n" +
		"		.;\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on anonymous type declaration with dummy spaces>";
	String completeBehind =
		"new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}\n" +
		"		.";
	String expectedCompletionNodeToString =
		"<CompleteOnName:new java.io.File(\"error\") {\n" +
		"  protected void runTest() {\n" +
		"  }\n" +
		"}.>";
	String completionIdentifier = "";
	String expectedReplacedSource =
		"new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}\n" +
		"		.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class VB {\n" +
		"  public VB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:new java.io.File(\"error\") {\n" +
		"  protected void runTest() {\n" +
		"  }\n" +
		"}.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// TODO excluded test (completion on field access on anonymous inner class with syntax error)
public void _testVB_5() {
	String str =
		"package p; \n" +
		"public class VB {\n" +
		"	void foo() {\n" +
		"		new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}.;\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on anonymous type declaration with trailing semi-colon>";
	String completeBehind =
		"new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}.";
	String expectedCompletionNodeToString =
		"<CompleteOnMemberAccess:new java.io.File(\"error\") {\n" +
		"  protected void runTest() {\n" +
		"  }\n" +
		"}.>";
	String completionIdentifier = "";
	String expectedReplacedSource =
		"new java.io.File(\"error\") {\n" +
		"			protected void runTest() {\n" +
		"				Vector v11111 = new Vector();\n" +
		"				v }\n" +
		"		}.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class VB {\n" +
		"  public VB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:new java.io.File(\"error\") {\n" +
		"  protected void runTest() {\n" +
		"  }\n" +
		"}.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testW_1FGGUS4() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGGUS4\n" +
		" */\n" +
		"public class W {\n" +
		"	public static final int LA = \n" +
		"		1; \n" +
		"	public static final int LAB = \n" +
		"		2; \n" +
		"	public static final int LABO = \n" +
		"		4; \n" +
		"	public int produceDebugAttributes = \n" +
		"		LABO; \n" +
		"}\n";

	String testName = "<complete on field initializer>";
	String completeBehind = "L";
	String expectedCompletionNodeToString = "<CompleteOnName:L>";
	String completionIdentifier = "L";
	String expectedReplacedSource = "LABO";
	int cursorLocation = str.indexOf("LABO;") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class W {\n" +
		"  public static final int LA;\n" +
		"  public static final int LAB;\n" +
		"  public static final int LABO;\n" +
		"  public int produceDebugAttributes = <CompleteOnName:L>;\n" +
		"  public W() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testWA_1() {
	String str =
			"package p; \n" +
			"public class WA {\n" +
			"	void foo() {\n" +
			"		int value = 10;\n" +
			"		v int[] tab = new int[value];\n" +
			"	}\n";

	String testName = "<complete on array size value>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "value";
	int cursorLocation = str.indexOf("value];") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class WA {\n" +
		"  public WA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int value;\n" +
		"    int[] tab = new int[<CompleteOnName:v>];\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testWA_2() {
	String str =
			"package p; \n" +
			"public class WA {\n" +
			"	void foo() {\n" +
			"		int value = 10;\n" +
			"		v int[] tab = new int[value];\n" +
			"	}\n";

	String testName = "<complete on corrupter local variable declaration>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "v";
	int cursorLocation = str.indexOf("v int[]") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class WA {\n" +
		"  public WA() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int value;\n" +
		"    <CompleteOnName:v>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testWB_1FI313C() {
	String str =
			"package p; \n" +
			"/*\n" +
			" * 1FI313C\n" +
			" */\n" +
			"class WBHelper {\n" +
			"	public int fieldPublic;\n" +
			"	protected int fieldProtected;\n" +
			"	private int fieldPrivate;\n" +
			"	int fieldDefault;\n" +
			"	static void staticFoo() {\n" +
			"	}\n" +
			"	static int i = d;\n" +
			"	int neuneu1() {\n" +
			"	}\n" +
			"	void neuneu2() {\n" +
			"	}\n" +
			"}\n" +
			"public class WB\n" +
			"	extends WBHelper {\n" +
			"	void foo() {\n" +
			"		BIJOUR[] b = \n" +
			"			new java.lang.Number[]; \n" +
			"		java.lang.Short s;\n" +
			"		b[1].}\n" +
			"	B() {\n" +
			"	}\n" +
			"	B(int) {\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of array element>";
	String completeBehind = "b[1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:b[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"class WBHelper {\n" +
		"  public int fieldPublic;\n" +
		"  protected int fieldProtected;\n" +
		"  private int fieldPrivate;\n" +
		"  int fieldDefault;\n" +
		"  static int i;\n" +
		"  WBHelper() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  static void staticFoo() {\n" +
		"  }\n" +
		"  int neuneu1() {\n" +
		"  }\n" +
		"  void neuneu2() {\n" +
		"  }\n" +
		"}\n" +
		"public class WB extends WBHelper {\n" +
		"  public WB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    BIJOUR[] b;\n" +
		"    java.lang.Short s;\n" +
		"    <CompleteOnMemberAccess:b[1].>;\n" +
		"  }\n" +
		"  B() {\n" +
		"  }\n" +
		"  B() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGGV8C_1() {
	String str =
		"package p; \n" +
		"import p2.Y; \n" +
		"/**\n" +
		" * 1FGGV8C and 1FGPE8E\n" +
		" */\n" +
		"public class X {\n" +
		"	public static final float Vars; \n" +
		"	public static final float Lines; \n" +
		"	public static final float Source; \n" +
		"	public static final float UnreachableCode; \n" +
		"	public static final float produceDebugAttributes; \n" +
		"	void foo() {\n" +
		"		int locale, \n" +
		"			errorThreshold, \n" +
		"			preserveAllLocalVariables; \n" +
		"		return new Y[] {\n" +
		"			new Y(\n" +
		"				\"debug.vars\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Vars)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"debug.lines\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Lines)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"debug.source\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Source)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"debug.preserveAllLocals\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				preserveAllLocalVariables\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"optionalError.unReachableCode\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(errorThreshold\n" +
		"					& UnreachableCode)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1)\n" +
		"				 }\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on argument of anonymous type declaration>";
	String completeBehind = "t";
	String expectedCompletionNodeToString = "<CompleteOnName:t>";
	String completionIdentifier = "t";
	String expectedReplacedSource = "this";
	int cursorLocation = str.indexOf("this, ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import p2.Y;\n" +
		"public class X {\n" +
		"  public static final float Vars;\n" +
		"  public static final float Lines;\n" +
		"  public static final float Source;\n" +
		"  public static final float UnreachableCode;\n" +
		"  public static final float produceDebugAttributes;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int locale;\n" +
		"    int errorThreshold;\n" +
		"    int preserveAllLocalVariables;\n" +
		"    new Y(\"debug.vars\", <CompleteOnName:t>);\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGGV8C_2() {
	String str =
		"package p; \n" +
		"import p2.YZA; \n" +
		"/**\n" +
		" * 1FGGV8C and 1FGPE8E\n" +
		" */\n" +
		"public class X {\n" +
		"	public static final float Vars; \n" +
		"	public static final float Lines; \n" +
		"	public static final float Source; \n" +
		"	public static final float UnreachableCode; \n" +
		"	public static final float produceDebugAttributes; \n" +
		"	void foo() {\n" +
		"		int locale, \n" +
		"			errorThreshold, \n" +
		"			preserveAllLocalVariables; \n" +
		"		return new YZA[] {\n" +
		"			new YZA(\n" +
		"				\"debug.vars\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Vars)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new YZA(\n" +
		"				\"debug.lines\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Lines)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new YZA(\n" +
		"				\"debug.source\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Source)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new YZA(\n" +
		"				\"debug.preserveAllLocals\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				preserveAllLocalVariables\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new YZA(\n" +
		"				\"optionalError.unReachableCode\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(errorThreshold\n" +
		"					& UnreachableCode)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1)\n" +
		"				 }\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on anonymous type declaration into a return statement>";
	String completeBehind = "Y";
	String expectedCompletionNodeToString = "<CompleteOnType:Y>";
	String completionIdentifier = "Y";
	String expectedReplacedSource = "YZA";
	int cursorLocation = str.indexOf("YZA[]") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import p2.YZA;\n" +
		"public class X {\n" +
		"  public static final float Vars;\n" +
		"  public static final float Lines;\n" +
		"  public static final float Source;\n" +
		"  public static final float UnreachableCode;\n" +
		"  public static final float produceDebugAttributes;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int locale;\n" +
		"    int errorThreshold;\n" +
		"    int preserveAllLocalVariables;\n" +
		"    return new <CompleteOnType:Y>();\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGGV8C_3() {
	String str =
		"package p; \n" +
		"import p2.YZA; \n" +
		"/**\n" +
		" * 1FGGV8C and 1FGPE8E\n" +
		" */\n" +
		"public class X {\n" +
		"	public static final float Vars; \n" +
		"	public static final float Lines; \n" +
		"	public static final float Source; \n" +
		"	public static final float UnreachableCode; \n" +
		"	public static final float produceDebugAttributes; \n" +
		"	void foo() {\n" +
		"		int locale, \n" +
		"			errorThreshold, \n" +
		"			preserveAllLocalVariables; \n" +
		"		return new YZA[] {\n" +
		"			new YZA(\n" +
		"				\"debug.vars\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Vars)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new YZA(\n" +
		"				\"debug.lines\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Lines)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new YZA(\n" +
		"				\"debug.source\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Source)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new YZA(\n" +
		"				\"debug.preserveAllLocals\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				preserveAllLocalVariables\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new YZA(\n" +
		"				\"optionalError.unReachableCode\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(errorThreshold\n" +
		"					& UnreachableCode)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1)\n" +
		"				 }\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on anonymous type declaration nested into an array initializer>";
	String completeBehind = "Y";
	String expectedCompletionNodeToString = "<CompleteOnType:Y>";
	String completionIdentifier = "Y";
	String expectedReplacedSource = "YZA";
	int cursorLocation = str.indexOf("YZA(") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import p2.YZA;\n" +
		"public class X {\n" +
		"  public static final float Vars;\n" +
		"  public static final float Lines;\n" +
		"  public static final float Source;\n" +
		"  public static final float UnreachableCode;\n" +
		"  public static final float produceDebugAttributes;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int locale;\n" +
		"    int errorThreshold;\n" +
		"    int preserveAllLocalVariables;\n" +
		"    new YZA[]{new <CompleteOnType:Y>()};\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGGV8C_4() {
	String str =
		"package p; \n" +
		"import p2.Y; \n" +
		"/**\n" +
		" * 1FGGV8C and 1FGPE8E\n" +
		" */\n" +
		"public class X {\n" +
		"	public static final float Vars; \n" +
		"	public static final float Lines; \n" +
		"	public static final float Source; \n" +
		"	public static final float UnreachableCode; \n" +
		"	public static final float produceDebugAttributes; \n" +
		"	void foo() {\n" +
		"		int locale, \n" +
		"			errorThreshold, \n" +
		"			preserveAllLocalVariables; \n" +
		"		return new Y[] {\n" +
		"			new Y(\n" +
		"				\"debug.vars\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Vars)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"debug.lines\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Lines)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"debug.source\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Source)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"debug.preserveAllLocals\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				preserveAllLocalVariables\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"optionalError.unReachableCode\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(errorThreshold\n" +
		"					& UnreachableCode)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1)\n" +
		"				 }\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on method/field into array intializer>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "new";
	int cursorLocation = str.indexOf("new Y(") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import p2.Y;\n" +
		"public class X {\n" +
		"  public static final float Vars;\n" +
		"  public static final float Lines;\n" +
		"  public static final float Source;\n" +
		"  public static final float UnreachableCode;\n" +
		"  public static final float produceDebugAttributes;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int locale;\n" +
		"    int errorThreshold;\n" +
		"    int preserveAllLocalVariables;\n" +
		"    new Y[]{<CompleteOnName:n>};\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGPE8E() {
	String str =
		"package p; \n" +
		"import p2.Y; \n" +
		"/**\n" +
		" * 1FGGV8C and 1FGPE8E\n" +
		" */\n" +
		"public class X {\n" +
		"	public static final float Vars; \n" +
		"	public static final float Lines; \n" +
		"	public static final float Source; \n" +
		"	public static final float UnreachableCode; \n" +
		"	public static final float produceDebugAttributes; \n" +
		"	void foo() {\n" +
		"		int locale, \n" +
		"			errorThreshold, \n" +
		"			preserveAllLocalVariables; \n" +
		"		return new Y[] {\n" +
		"			new Y(\n" +
		"				\"debug.vars\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Vars)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"debug.lines\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Lines)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"debug.source\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(produceDebugAttributes\n" +
		"					& Source)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"debug.preserveAllLocals\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				preserveAllLocalVariables\n" +
		"					? 0\n" +
		"					: 1), \n" +
		"			new Y(\n" +
		"				\"optionalError.unReachableCode\", \n" +
		"				this, \n" +
		"				locale, \n" +
		"				(errorThreshold\n" +
		"					& UnreachableCode)\n" +
		"					!= 0\n" +
		"					? 0\n" +
		"					: 1)\n" +
		"				 }\n" +
		"	}\n" +
		"}\n";

	String testName = "<complete on method/field into return statement>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "new";
	int cursorLocation = str.indexOf("new Y[]") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import p2.Y;\n" +
		"public class X {\n" +
		"  public static final float Vars;\n" +
		"  public static final float Lines;\n" +
		"  public static final float Source;\n" +
		"  public static final float UnreachableCode;\n" +
		"  public static final float produceDebugAttributes;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int locale;\n" +
		"    int errorThreshold;\n" +
		"    int preserveAllLocalVariables;\n" +
		"    return <CompleteOnName:n>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// Disabled since javadoc completion has been implemented
public void _testXA_1FGGUQF_1FHSL8H_1() {
	String str =
			"// int\n" +
			"package p; \n" +
			"/**\n" +
			" * 1FGGUQF and 1FHSL8H\n" +
			" */\n" +
			"/**\n" +
			" * int\n" +
			" */\n" +
			"/*\n" +
			" * int\n" +
			" */\n" +
			"// int\n" +
			"/**\n" +
			"int.\n" +
			" * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			" */\n" +
			"/**\n" +
			" * int\n" +
			" */\n" +
			"public class XA {\n" +
			"	//  int\n" +
			"	/*  int */\n" +
			"	/** int */\n" +
			"	/**\n" +
			"	int.\n" +
			"	 * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			"	 */\n" +
			"	void /* int */\n" +
			"	foo() {\n" +
			"		//  int\n" +
			"		/*  int */\n" +
			"		/** int */\n" +
			"	}\n" +
			"	/**\n" +
			"	int.\n" +
			"	 * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			"	 */\n" +
			"	int field /* int */\n" +
			"	;\n" +
			"	/*\n" +
			"	    int\n" +
			"	*/\n" +
			"	static {\n" +
			"		// int\n" +
			"	}\n" +
			"}\n" +
			"//  int\n" +
			"/*  int */\n" +
			"/** int */\n";

	String testName = "<complete on comment>";
	String completeBehind = "int.";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf("int.\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
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
public void testXA_1FGGUQF_1FHSL8H_2() {
	String str =
			"// int\n" +
			"package p; \n" +
			"/**\n" +
			" * 1FGGUQF and 1FHSL8H\n" +
			" */\n" +
			"/**\n" +
			" * int\n" +
			" */\n" +
			"/*\n" +
			" * int\n" +
			" */\n" +
			"// int\n" +
			"/**\n" +
			"int.\n" +
			" * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			" */\n" +
			"/**\n" +
			" * int\n" +
			" */\n" +
			"public class XA {\n" +
			"	//  int\n" +
			"	/*  int */\n" +
			"	/** int */\n" +
			"	/**\n" +
			"	int.\n" +
			"	 * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			"	 */\n" +
			"	void /* int */\n" +
			"	foo() {\n" +
			"		//  int\n" +
			"		/*  int */\n" +
			"		/** int */\n" +
			"	}\n" +
			"	/**\n" +
			"	int.\n" +
			"	 * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			"	 */\n" +
			"	int field /* int */\n" +
			"	;\n" +
			"	/*\n" +
			"	    int\n" +
			"	*/\n" +
			"	static {\n" +
			"		// int\n" +
			"	}\n" +
			"}\n" +
			"//  int\n" +
			"/*  int */\n" +
			"/** int */\n";

	String testName = "<complete on comment>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf("int\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
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
public void testXA_1FGGUQF_1FHSL8H_3() {
	String str =
			"// int\n" +
			"package p; \n" +
			"/**\n" +
			" * 1FGGUQF and 1FHSL8H\n" +
			" */\n" +
			"/**\n" +
			" * int\n" +
			" */\n" +
			"/*\n" +
			" * int\n" +
			" */\n" +
			"// int\n" +
			"/**\n" +
			"int.\n" +
			" * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			" */\n" +
			"/**\n" +
			" * int\n" +
			" */\n" +
			"public class XA {\n" +
			"	//  int\n" +
			"	/*  int */\n" +
			"	/** int */\n" +
			"	/**\n" +
			"	int.\n" +
			"	 * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			"	 */\n" +
			"	void /* int */ foo() {\n" +
			"		//  int\n" +
			"		/*  int */\n" +
			"		/** int */\n" +
			"	}\n" +
			"	/**\n" +
			"	int.\n" +
			"	 * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			"	 */\n" +
			"	int field /* int */\n" +
			"	;\n" +
			"	/*\n" +
			"	    int\n" +
			"	*/\n" +
			"	static {\n" +
			"		// int\n" +
			"	}\n" +
			"}\n" +
			"//  int\n" +
			"/*  int */\n" +
			"/** int */\n";

	String testName = "<complete on comment>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf("int */") + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
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
public void testXA_1FGGUQF_1FHSL8H_4() {
	String str =
			"// int\n" +
			"package p; \n" +
			"/**\n" +
			" * 1FGGUQF and 1FHSL8H\n" +
			" */\n" +
			"/**\n" +
			" * int\n" +
			" */\n" +
			"/*\n" +
			" * int\n" +
			" */\n" +
			"// int\n" +
			"/**\n" +
			"int.\n" +
			" * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			" */\n" +
			"/**\n" +
			" * int\n" +
			" */\n" +
			"public class XA {\n" +
			"	//  int\n" +
			"	/*  int */\n" +
			"	/** int */\n" +
			"	/**\n" +
			"	int.\n" +
			"	 * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			"	 */\n" +
			"	void /* int */ foo() {\n" +
			"		//  int\n" +
			"		/*  int */\n" +
			"		/** int */\n" +
			"	}\n" +
			"	/**\n" +
			"	int.\n" +
			"	 * Internal API used to resolve a compilation unit minimally for code assist engine\n" +
			"	 */\n" +
			"	int field /* int */\n" +
			"	;\n" +
			"	/*\n" +
			"	    int\n" +
			"	*/\n" +
			"	static {\n" +
			"		// int\n" +
			"	}\n" +
			"}\n" +
			"//  int\n" +
			"/*  int  */\n" +
			"/** int   */\n";

	String testName = "<complete on comment>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf("int */ foo()") + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
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
public void testXB_1FIYM5I_1() {
	String str =
			"package p; \n" +
			"/*\n" +
			" * 1FIYM5I\n" +
			" */\n" +
			"public class XB\n" +
			"	extends java.io.File {\n" +
			"	void foo() {\n" +
			"		XB xb = new XB();\n" +
			"		this.separator.;\n" +
			"		this.bar().;\n" +
			"	}\n" +
			"	String bar() {\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of explicit this access>";
	String completeBehind = "this.s";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.s>";
	String completionIdentifier = "s";
	String expectedReplacedSource = "this.separator";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class XB extends java.io.File {\n" +
		"  public XB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    XB xb;\n" +
		"    <CompleteOnMemberAccess:this.s>;\n" +
		"  }\n" +
		"  String bar() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testXB_1FIYM5I_2() {
	String str =
			"package p; \n" +
			"/*\n" +
			" * 1FIYM5I\n" +
			" */\n" +
			"public class XB\n" +
			"	extends java.io.File {\n" +
			"	void foo() {\n" +
			"		XB xb = new XB();\n" +
			"		this.separator.;\n" +
			"		this.bar().;\n" +
			"	}\n" +
			"	String bar() {\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of explicitly accessed field>";
	String completeBehind = "this.separator.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.separator.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class XB extends java.io.File {\n" +
		"  public XB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    XB xb;\n" +
		"    <CompleteOnMemberAccess:this.separator.>;\n" +
		"  }\n" +
		"  String bar() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testXB_1FIYM5I_3() {
	String str =
			"package p; \n" +
			"/*\n" +
			" * 1FIYM5I\n" +
			" */\n" +
			"public class XB\n" +
			"	extends java.io.File {\n" +
			"	void foo() {\n" +
			"		XB xb = new XB();\n" +
			"		this.separator.;\n" +
			"		this.bar().;\n" +
			"	}\n" +
			"	String bar() {\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of explicit this access>";
	String completeBehind = "this.b";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.b>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "this.bar";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class XB extends java.io.File {\n" +
		"  public XB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    XB xb;\n" +
		"    <CompleteOnMemberAccess:this.b>;\n" +
		"  }\n" +
		"  String bar() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testXB_1FIYM5I_4() {
	String str =
			"package p; \n" +
			"/*\n" +
			" * 1FIYM5I\n" +
			" */\n" +
			"public class XB\n" +
			"	extends java.io.File {\n" +
			"	void foo() {\n" +
			"		XB xb = new XB();\n" +
			"		this.separator.;\n" +
			"		this.bar().;\n" +
			"	}\n" +
			"	String bar() {\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field of explicitly accessed method>";
	String completeBehind = "this.bar().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.bar().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class XB extends java.io.File {\n" +
		"  public XB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    XB xb;\n" +
		"    <CompleteOnMemberAccess:this.bar().>;\n" +
		"  }\n" +
		"  String bar() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testY_1FGPESI() {
	String str =
		"package p; \n" +
		"import p2.; \n" +
		"/**\n" +
		" * 1FGPESI\n" +
		" */\n" +
		"public class Y {\n" +
		"}\n";

	String testName = "<complete on imports>";
	String completeBehind = "p2.";
	String expectedCompletionNodeToString = "<CompleteOnImport:p2.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "p2.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnImport:p2.>;\n" +
		"public class Y {\n" +
		"  public Y() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testYA_1FGRIUH() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FGRIUH\n" +
			" */\n" +
			"public class YA\n" +
			"	extends YASecondTopLevel {\n" +
			"	void eFoo() {\n" +
			"	}\n" +
			"	class YAMember {\n" +
			"		void eFoo() {\n" +
			"		}\n" +
			"		void eBar() {\n" +
			"			e }\n" +
			"	}\n" +
			"}\n" +
			"class YASecondTopLevel {\n" +
			"	public boolean equals(YA yaya) {\n" +
			"		return true;\n" +
			"	}\n" +
			"	public eFoo() {\n" +
			"	}\n" +
			"	public void eFooBar() {\n" +
			"	}\n" +
			"}\n";

	String testName = "<complete on method/field>";
	String completeBehind = "e";
	String expectedCompletionNodeToString = "<CompleteOnName:e>";
	String completionIdentifier = "e";
	String expectedReplacedSource = "e";
	int cursorLocation = str.indexOf("e }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class YA extends YASecondTopLevel {\n" +
		"  class YAMember {\n" +
		"    YAMember() {\n" +
		"    }\n" +
		"    void eFoo() {\n" +
		"    }\n" +
		"    void eBar() {\n" +
		"      <CompleteOnName:e>;\n" +
		"    }\n" +
		"  }\n" +
		"  public YA() {\n" +
		"  }\n" +
		"  void eFoo() {\n" +
		"  }\n" +
		"}\n" +
		"class YASecondTopLevel {\n" +
		"  YASecondTopLevel() {\n" +
		"  }\n" +
		"  public boolean equals(YA yaya) {\n" +
		"  }\n" +
		"  public eFoo() {\n" +
		"  }\n" +
		"  public void eFooBar() {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testYB_1FJ4D46_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ4D46\n" +
			" */\n" +
			"public class YB {\n" +
			"	void foo() {\n" +
			"		new String(\"asdf\".getBytes()).}\n" +
			"}\n";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new String(\"asdf\".getBytes()).";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new String(\"asdf\".getBytes()).>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class YB {\n" +
		"  public YB() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:new String(\"asdf\".getBytes()).>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZ_1FGPF3D_1() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGPF3D\n" +
		" */\n" +
		"public class Z imp Pro.Sev, \n" +
		"	Bla.Blo {\n" +
		"}\n";

	String testName = "<complete on implements keyword>";
	String completeBehind = "imp";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:imp>";
	String completionIdentifier = "imp";
	String expectedReplacedSource = "imp";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class Z extends <CompleteOnKeyword:imp> {\n" +
		"  {\n" +
		"  }\n" +
		"  public Z() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZ_1FGPF3D_2() {
	String str =
		"package p; \n" +
		"/**\n" +
		" * 1FGPF3D\n" +
		" */\n" +
		"public class Z implements Pro.Sev, \n" +
		"	Bla.Blo {\n" +
		"}\n";

	String testName = "<complete on implented interface>";
	String completeBehind = "P";
	String expectedCompletionNodeToString = "<CompleteOnInterface:P>";
	String completionIdentifier = "P";
	String expectedReplacedSource = "Pro";
	int cursorLocation = str.indexOf("Pro.Sev") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class Z implements <CompleteOnInterface:P>, Bla.Blo {\n" +
		"  public Z() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZA_1() {
	String str =
			"package p; \n" +
			"import java.util.Vector;\n";

	String testName = "<complete on import keyword>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "import";
	int cursorLocation = str.indexOf("import") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:i>;\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZA_2() {
	String str =
			"package p; \n" +
			"import java.util.Vector;\n";

	String testName = "<complete on imported package>";
	String completeBehind = "jav";
	String expectedCompletionNodeToString = "<CompleteOnImport:jav>";
	String completionIdentifier = "jav";
	String expectedReplacedSource = "java.util.Vector";
	int cursorLocation = str.indexOf("java.util.Vector") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnImport:jav>;\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZA_3() {
	String str =
			"package p; \n" +
			"import java.util.Vector;\n";

	String testName = "<complete on imported type>";
	String completeBehind = "java.util.V";
	String expectedCompletionNodeToString = "<CompleteOnImport:java.util.V>";
	String completionIdentifier = "V";
	String expectedReplacedSource = "java.util.Vector";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnImport:java.util.V>;\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZB_1FJ4D46_1() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ4D46\n" +
			" */\n" +
			"import java.util.zip.CRC32;\n" +
			"import java.io.*;\n" +
			"public class ZB {\n" +
			"	public static void main(\n" +
			"		java.lang.String[] args) {\n" +
			"		File file = \n" +
			"			new File(\"d:\\\\314\"); \n" +
			"		CRC32 crc = new CRC32();\n" +
			"		file.}\n" +
			"}\n";

	String testName = "<complete on method/field of local variable>";
	String completeBehind = "file.";
	String expectedCompletionNodeToString = "<CompleteOnName:file.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "file.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.util.zip.CRC32;\n" +
		"import java.io.*;\n" +
		"public class ZB {\n" +
		"  public ZB() {\n" +
		"  }\n" +
		"  public static void main(java.lang.String[] args) {\n" +
		"    File file;\n" +
		"    CRC32 crc;\n" +
		"    <CompleteOnName:file.>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZB_1FJ4D46_2() {
	String str =
			"package p; \n" +
			"/**\n" +
			" * 1FJ4D46\n" +
			" */\n" +
			"import java.util.zip.CRC32;\n" +
			"import java.io.*;\n" +
			"public class ZB {\n" +
			"	public static void main(\n" +
			"		java.lang.String[] args) {\n" +
			"		File file = \n" +
			"			new File(\"d:\\\\314\"); \n" +
			"		CRC32 crc = new CRC32();\n" +
			"		file.}\n" +
			"}\n";

	String testName = "<complete on local variable type>";
	String completeBehind = "CRC";
	String expectedCompletionNodeToString = "<CompleteOnName:CRC>";
	String completionIdentifier = "CRC";
	String expectedReplacedSource = "CRC32";
	int cursorLocation = str.indexOf("CRC32 crc") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import java.util.zip.CRC32;\n" +
		"import java.io.*;\n" +
		"public class ZB {\n" +
		"  public ZB() {\n" +
		"  }\n" +
		"  public static void main(java.lang.String[] args) {\n" +
		"    File file;\n" +
		"    <CompleteOnName:CRC>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in initializer
 */
public void test001(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int v1;\n" +
		"    {\n" +
		"      Obj\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String testName = "<complete in initializer>";
	String completeBehind = "Obj";
	String expectedCompletionNodeToString = "<CompleteOnName:Obj>";
	String completionIdentifier = "Obj";
	String expectedReplacedSource = "Obj";
	int cursorLocation = str.indexOf("Obj") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int v1;\n" +
		"    {\n" +
		"      <CompleteOnName:Obj>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete after initializer
 */
public void test002(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int v1;\n" +
		"    {\n" +
		"      int v2\n" +
		"    }\n" +
		"    Obj" +
		"  }\n" +
		"}\n";

	String testName = "<complete after initializer>";
	String completeBehind = "Obj";
	String expectedCompletionNodeToString = "<CompleteOnName:Obj>";
	String completionIdentifier = "Obj";
	String expectedReplacedSource = "Obj";
	int cursorLocation = str.indexOf("Obj") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int v1;\n" +
		"    <CompleteOnName:Obj>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in initializer
 */
public void test003(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    int v1;\n" +
		"    {\n" +
		"      this.\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String testName = "<complete in initializer>";
	String completeBehind = "this.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "this.";
	int cursorLocation = str.indexOf("this.") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int v1;\n" +
		"    {\n" +
		"      <CompleteOnMemberAccess:this.>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

/**
 * Complete in switch
 */
public void test004(){
	String str =
		"public class X {\n" +
		"  final static int ZZZ = 1;\n"+
		"  void foo(){\n" +
		"    switch(2)\n" +
		"      case 0 + ZZZ :\n" +
		"      case 1 + ZZZ :\n" +
		"          bar(ZZZ)\n" +
		"  }\n" +
		"  void bar(int y) {}\n"+
		"}\n";

	String testName = "<complete in switch>";
	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompleteOnName:ZZZ>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  static final int ZZZ;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    bar(<CompleteOnName:ZZZ>);\n" +
		"  }\n" +
		"  void bar(int y) {\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in method type.
 */
public void test005(){
	String str =
		"public class X {\n" +
		"  clon foo(){\n" +
		"  }\n" +
		"}\n";

	String testName = "<complete in method type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <CompleteOnType:clon>\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in method type.
 */
public void test006(){
	String str =
		"public class X {\n" +
		"  clon\n" +
		"  foo();\n" +
		"}\n";

	String testName = "<complete in method type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:clon>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  foo();\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in field type.
 */
public void test007(){
	String str =
		"public class X {\n" +
		"  clon  x;\n" +
		"}\n";

	String testName = "<complete in field type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:clon>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in field type.
 */
public void test008(){
	String str =
		"public class X {\n" +
		"  clon\n" +
		"  x;\n" +
		"}\n";

	String testName = "<complete in field type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:clon>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in field type.
 */
public void test009(){
	String str =
		"public class X {\n" +
		"  clon\n" +
		"  x y;\n" +
		"}\n";

	String testName = "<complete in field tpye>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:clon>;\n" +
		"  x y;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in method type.
 */
public void test010(){
	String str =
		"public class X {\n" +
		"  clon\n" +
		"  x y(){}\n" +
		"}\n";

	String testName = "<complete in method type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:clon>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  x y() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=25233
 */
public void test011(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new Object[]{\n" +
		"      bar(zzz)\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	String testName = "<bug 25233>";
	String completeBehind = "zzz";
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	int cursorLocation = str.lastIndexOf("zzz") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    bar(<CompleteOnName:zzz>);\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=27370
 */
public void test012(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  Object o = new ZZZ\n" +
		"}\n";

	String testName = "<bug 27370>";
	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Object o = new <CompleteOnType:ZZZ>();\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=27735
 */
public void test013(){
	String str =
		"public class Bar {\n" +
		"  #\n" +
		"  Bar foo1 = new Bar(){};\n" +
		"  {int i;}\n" +
		"  synchronized void foo3() {}\n" +
		"  zzz\n" +
		"}\n";

	String testName = "<bug 27735>";
	String completeBehind = "zzz";
	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	int cursorLocation = str.lastIndexOf("zzz") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class Bar {\n" +
		"  Bar foo1;\n" +
		"  {\n" +
		"  }\n" +
		"  <CompleteOnType:zzz>;\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"  synchronized void foo3() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=27941
 */
public void test014(){
	String str =
		"public class Bar {\n" +
		"  void foo() {\n" +
		"    String s = \"a\" + \"b\";\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String testName = "<bug 27941>";
	String completeBehind = "zzz";
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	int cursorLocation = str.lastIndexOf("zzz") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class Bar {\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    String s;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=39502
 */
public void test015(){
	String str =
		"public class Bar {\n" +
		"  void foo() {\n" +
		"    Object o = new Object[]{};\n" +
		"    foo();\n" +
		"  }\n" +
		"}\n";

	String testName = "<bug 39502>";
	String completeBehind = "foo(";
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:foo()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "foo(";
	int cursorLocation = str.lastIndexOf("foo(") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class Bar {\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object o;\n" +
		"    <CompleteOnMessageSend:foo()>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=39502
 */
public void test016(){
	String str =
		"public class Bar {\n" +
		"  void foo() {\n" +
		"    Object o = new Object[0];\n" +
		"    foo();\n" +
		"  }\n" +
		"}\n";

	String testName = "<bug 39502>";
	String completeBehind = "foo(";
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:foo()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "foo(";
	int cursorLocation = str.lastIndexOf("foo(") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class Bar {\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object o;\n" +
		"    <CompleteOnMessageSend:foo()>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

public void test017(){
	String str =
		"public class Bar {\n" +
		"  String s;\n" +
		"  /**/\n" +
		"}\n";

	String testName = "";
	String completeBehind = "/**/";
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.lastIndexOf("/**/") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"public class Bar {\n" +
		"  String s;\n" +
		"  <CompleteOnType:>;\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310423
// To verify that assist node parent is set to the the type declaration
// when completion is requested after implements in a type declaration.
public void testBug310423(){
	String str =
		"import java.lang.annotation.Annotation;\n" +
		"interface In {}\n" +
		"interface Inn {\n" +
		"	interface Inn2 {}\n" +
		"	@interface InAnnot {}\n" +
		"}\n" +
		"@interface InnAnnot {}\n"+
		"public class Test implements In{\n" +
		"}\n";

	String testName = "";
	String completeBehind = "In";
	String expectedCompletionNodeToString = "<CompleteOnInterface:In>";
	String expectedParentNodeToString = 
		"public class Test implements <CompleteOnInterface:In> {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"}";
	String completionIdentifier = "In";
	String expectedReplacedSource = "In";
	int cursorLocation = str.lastIndexOf("In") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"import java.lang.annotation.Annotation;\n" + 
		"interface In {\n" + 
		"}\n" + 
		"interface Inn {\n" + 
		"  interface Inn2 {\n" + 
		"  }\n" + 
		"  @interface InAnnot {\n" + 
		"  }\n" + 
		"}\n" + 
		"@interface InnAnnot {\n" + 
		"}\n" + 
		"public class Test implements <CompleteOnInterface:In> {\n" + 
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
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338789
public void testBug338789(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"public class Test {\n" +
		"	public void throwing() throws IZZBException, IZZException {}\n" +
		"	public void foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IZZException | IZZ) {\n" +
		"         bar();\n" +
		"      }\n" +
		"   }" +
		"}\n" +
		"class IZZAException extends Exception {\n" +
		"}\n" +
		"class IZZBException extends Exception {\n" +
		"}\n" +
		"class IZZException extends Exception {\n" +
		"}\n";

	String testName = "<complete on multi-catch block exception type declaration>";
	String completeBehind = "IZZException | IZZ";
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  public void throwing() throws IZZBException, IZZException {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    try\n" + 
		"      {\n" + 
		"        throwing();\n" + 
		"      }\n" + 
		"    catch (IZZException | <CompleteOnException:IZZ>  )\n" + 
		"      {\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n" + 
		"class IZZAException extends Exception {\n" + 
		"  IZZAException() {\n" + 
		"  }\n" + 
		"}\n" + 
		"class IZZBException extends Exception {\n" + 
		"  IZZBException() {\n" + 
		"  }\n" + 
		"}\n" + 
		"class IZZException extends Exception {\n" + 
		"  IZZException() {\n" + 
		"  }\n" + 
		"}\n";

	int cursorLocation = str.indexOf("IZZException | IZZ") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338789
// Qualified assist type reference
public void testBug338789b(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"public class Test {\n" +
		"	public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException {}\n" +
		"	public void foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (java.lang.IllegalArgumentException | java.lang.I) {\n" +
		"         bar();\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String testName = "<complete on multi-catch block exception type declaration qualified>";
	String completeBehind = "java.lang.IllegalArgumentException | java.lang.I";
	String expectedCompletionNodeToString = "<CompleteOnException:java.lang.I>";
	String completionIdentifier = "I";
	String expectedReplacedSource = "java.lang.I";
	String expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    try\n" + 
		"      {\n" + 
		"        throwing();\n" + 
		"      }\n" + 
		"    catch (java.lang.IllegalArgumentException | <CompleteOnException:java.lang.I>  )\n" + 
		"      {\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	int cursorLocation = str.indexOf("java.lang.IllegalArgumentException | java.lang.I") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=343637
// Check that the whole union type ref is part of the completion node parent
public void testBug343637(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"public class Test {\n" +
		"	public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException, java.lang.IOException {}\n" +
		"	public void foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"	   catch (java.lang.IOException e){}\n" +
		"      catch (java.lang.IllegalArgumentException | java.lang.I) {\n" +
		"         bar();\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String testName = "<complete on multi-catch block exception type declaration qualified>";
	String completeBehind = "java.lang.IllegalArgumentException | java.lang.I";
	String expectedCompletionNodeToString = "<CompleteOnException:java.lang.I>";
	String completionIdentifier = "I";
	String expectedReplacedSource = "java.lang.I";
	String expectedUnitDisplayString =			
		"public class Test {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException, java.lang.IOException {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    try\n" + 
		"      {\n" + 
		"        throwing();\n" + 
		"      }\n" + 
		"    catch (java.lang.IOException e)\n" + 
		"      {\n" + 
		"      }\n" + 
		"    catch (java.lang.IllegalArgumentException | <CompleteOnException:java.lang.I>  )\n" + 
		"      {\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	int cursorLocation = str.indexOf("java.lang.IllegalArgumentException | java.lang.I") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346454
public void testBug346454(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"public class Test<T> {\n" +
		"	public void foo() {\n" +
		"      Test<String> t = new Test<>()\n" +
		"   }" +
		"}\n";

	String testName = "<complete after diamond type>";
	String completeBehind = "new Test<>(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new Test<>()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =			
		"public class Test<T> {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    Test<String> t = <CompleteOnAllocationExpression:new Test<>()>;\n" + 
		"  }\n" + 
		"}\n";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346454
public void testBug346454b(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"public class Test<T> {\n" +
		"	public class T2<Z>{}\n" +
		"	public void foo() {\n" +
		"      Test<String>.T2<String> t = new Test<>().new T2<>()\n" +
		"   }" +
		"}\n";

	String testName = "<complete after diamond type>";
	String completeBehind = "new Test<>().new T2<>(";
	String expectedCompletionNodeToString = "<CompleteOnQualifiedAllocationExpression:new Test<>().new T2<>()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =			
		"public class Test<T> {\n" + 
		"  public class T2<Z> {\n" + 
		"    public T2() {\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    Test<String>.T2<String> t = <CompleteOnQualifiedAllocationExpression:new Test<>().new T2<>()>;\n" + 
		"  }\n" + 
		"}\n";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346415
// To make sure that all catch blocks before the one in which we're invoking assist are avaiable in the ast.
public void testBug346415(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"public class Test {\n" +
		"	public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException, java.lang.IOException {}\n" +
		"	public void foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"	   catch (java.lang.IOException e){\n" +
		"      } catch (java.lang.IllegalArgumentException e){\n" +
		"	   } catch (/*propose*/) {\n" +
		"      }\n" +
		"   }\n" +
		"}\n";

	String testName = "<complete on third catch block>";
	String completeBehind = "catch (/*propose*/";
	String expectedCompletionNodeToString = "<CompleteOnException:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =			
			"public class Test {\n" + 
			"  public Test() {\n" + 
			"  }\n" + 
			"  public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException, java.lang.IOException {\n" + 
			"  }\n" + 
			"  public void foo() {\n" + 
			"    try\n" + 
			"      {\n" + 
			"        throwing();\n" + 
			"      }\n" + 
			"    catch (java.lang.IOException e)\n" + 
			"      {\n" + 
			"      }\n" + 
			"    catch (java.lang.IllegalArgumentException e)\n" + 
			"      {\n" + 
			"      }\n" + 
			"    catch (<CompleteOnException:>  )\n" + 
			"      {\n" + 
			"      }\n" + 
			"  }\n" + 
			"}\n";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
}
