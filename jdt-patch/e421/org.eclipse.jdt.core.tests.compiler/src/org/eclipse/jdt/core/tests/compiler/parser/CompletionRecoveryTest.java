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

public class CompletionRecoveryTest extends AbstractCompletionTest {
public CompletionRecoveryTest(String testName){
	super(testName);
}
static {
//	TESTS_NUMBERS = new int[] { 22 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(CompletionRecoveryTest.class);
}
/*
 * Complete on variable behind ill-formed declaration
 */
public void test01() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X extends IOException {		\n" +
		"	int foo(){								\n" +
		"		String str = ;						\n" +
		"		str.								\n";

	String completeBehind = "str.";
	String expectedCompletionNodeToString = "<CompleteOnName:str.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    String str;\n" +
		"    <CompleteOnName:str.>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "str.";
	String testName = "<complete on variable behind ill-formed declaration>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable behind ill-formed declaration and nested block
 */
public void test02() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X extends IOException {		\n" +
		"	int foo(){								\n" +
		"		String str = ;						\n" +
		"		{									\n" +
		"		 	int i;							\n" +
		"			str.							\n";

	String completeBehind = "str.";
	String expectedCompletionNodeToString = "<CompleteOnName:str.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    String str;\n" +
		"    {\n" +
		"      int i;\n" +
		"      <CompleteOnName:str.>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "str.";
	String testName = "<complete on variable behind ill-formed declaration and nested block>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable behind ill-formed declaration and inside local type field initialization
 */
public void test03() {

	String str =
		"import java.io.*;							\n" +
		"											\n" +
		"public class X extends IOException {		\n" +
		"	int foo(){								\n" +
		"		final String str = ;				\n" +
		"		class L {							\n" +
		"		 	int i = str						\n";

	String completeBehind = "i = str";
	String expectedCompletionNodeToString = "<CompleteOnName:str>";
	String completionIdentifier = "str";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    final String str;\n" +
		"    class L {\n" +
		"      int i = <CompleteOnName:str>;\n" +
		"      L() {\n" +
		"        super();\n" + // could be optimized out
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "str";
	String testName = "<complete on variable behind ill-formed declaration and inside local type field initialization>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable behind closed scope
 */
public void test04() {

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
	String expectedCompletionNodeToString = "<CompleteOnName:var>";
	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo(String str) {\n" +
		"    String variable;\n" +
		"    foo(<CompleteOnName:var>);\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "varia";
	String testName = "<complete on variable behind closed scope>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable str with sibling method stringAppend()
 */
public void test05() {

	String str =
		"import java.io.*;									\n"+
		"													\n"+
		"public class X extends 							\n"+
		"	int foo(String str)								\n"+
		"		String str = ;								\n"+
		"		{											\n"+
		"		 	String strNotInScope;					\n"+
		"		}											\n"+
		"		class L {									\n"+
		"			int bar(){								\n"+
		"				foo(str								\n"+
		"			void stringAppend(String s1, String s2)	\n";

	String completeBehind = "foo(str";
	String expectedCompletionNodeToString = "<CompleteOnName:str>";
	String completionIdentifier = "str";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo(String str) {\n" +
		"    String str;\n" +
		"    class L {\n" +
		"      L() {\n" +
		"      }\n" +
		"      int bar() {\n" +
		"        foo(<CompleteOnName:str>);\n" +
		"      }\n" +
		"      void stringAppend(String s1, String s2) {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "str";
	String testName = "<complete on variable str with sibling method stringAppend()>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable str with sibling method stringAppend(), eliminating
 * uninteresting method bodies
 */
public void test06() {

	String str =
		"import java.io.*;									\n"+
		"													\n"+
		"public class X extends 							\n"+
		"	int foo(String str)								\n"+
		"		String str = ;								\n"+
		"		{											\n"+
		"		 	String strNotInScope;					\n"+
		"		}											\n"+
		"		class L {									\n"+
		"			int notInterestingBody(){				\n"+
		"				System.out.println();				\n"+
		"			}										\n"+
		"			int bar(){								\n"+
		"				foo(str								\n"+
		"			void stringAppend(String s1, String s2)	\n";

	String completeBehind = "foo(str";
	String expectedCompletionNodeToString = "<CompleteOnName:str>";
	String completionIdentifier = "str";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo(String str) {\n" +
		"    String str;\n" +
		"    class L {\n" +
		"      L() {\n" +
		"      }\n" +
		"      int notInterestingBody() {\n" +
		"      }\n" +
		"      int bar() {\n" +
		"        foo(<CompleteOnName:str>);\n" +
		"      }\n" +
		"      void stringAppend(String s1, String s2) {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedReplacedSource = "str";
	String testName = "<complete on variable eliminating other uninteresting method bodies>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on new keyword
 */
public void test07(){

	String str =
		"import java.io.*							\n" +
		"											\n" +
		"public class X extends IOException {		\n" +
		"	int foo() {								\n" +
		"		X x = new X(						\n" +
		"}											\n";

	String completeBehind = "= n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedUnitDisplayString =
		"import java.io.*;\n" +
		"public class X extends IOException {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    X x = <CompleteOnName:n>;\n" +
		"  }\n" +
		"}\n";
	String expectedReplacedSource = "new";
	String testName = "<complete on new keyword>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on field type in anonymous type.
 */
public void test08() {
	runTestCheckDietParse(
		// compilationUnit:
		"package test;\n" +
		"import java.util.Vector;\n" +
		"public class VA {\n" +
		"	Object o1 = new Object() {\n" +
		"		V\n" +
		"		void foo2() {\n" +
		"			int i = 1;\n" +
		"		}\n" +
		"	};\n" +
		"	String s2;\n" +
		"	void bar() {\n" +
		"	}\n" +
		"	void foo() { \n" +
		"		new String[] {}..equals()\n" +
		"	}\n" +
		"}\n",
		// completeBehind:
		"		V",
		// expectedCompletionNodeToString:
		"<CompleteOnType:V>",
		// expectedUnitDisplayString:
		"package test;\n" +
		"import java.util.Vector;\n" +
		"public class VA {\n" +
		"  Object o1 = new Object() {\n" +
		"    <CompleteOnType:V>;\n" +
		"    void foo2() {\n" +
		"    }\n" +
		"  };\n" +
		"  String s2;\n" +
		"  public VA() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"V",
		// expectedReplacedSource:
		"V",
		// test name
		"<completion on field type in anonymous type>"
	);
}
/*
 * Complete on argument name
 */
public void test09() {
	runTestCheckDietParse(
		// compilationUnit:
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
		"};											\n",
		// completeBehind:
		"argv",
		// expectedCompletionNodeToString:
		"<CompleteOnArgumentName:String[] argv>",
		// expectedUnitDisplayString:
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
		"  public static void main(<CompleteOnArgumentName:String[] argv>) {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"argv",
		// expectedReplacedSource:
		"argv",
		// test name
		"<completion on argument name>"
	);
}
/*
 * Complete on argument name
 */
public void test10() {
	this.runTestCheckMethodParse(
		// compilationUnit:
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
		"};											\n",
		// completeBehind:
		"argv",
		// expectedCompletionNodeToString:
		"<CompleteOnArgumentName:String[] argv>",
		// expectedUnitDisplayString:
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
		"  public static void main(<CompleteOnArgumentName:String[] argv>) {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"argv",
		// expectedReplacedSource:
		"argv",
		// test name
		"<completion on argument name>"
	);
}
/*
 * Complete inside method with incomplete signature
 */
public void test11() {
	this.runTestCheckMethodParse(
		// compilationUnit:
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
		"};											\n",
		// completeBehind:
		"new Member().f",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:new Member().f>",
		// expectedUnitDisplayString:
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
		"    <CompleteOnMemberAccess:new Member().f>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"f",
		// expectedReplacedSource:
		"f",
		// test name
		"<complete inside method with incomplete signature>"
	);
}
/*
 * Complete on argument name with class decl later on
 */
public void test12() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class DD  {									\n"+
		"	public static void main(String[] argv		\n"+
		"												\n"+
		"class D {										\n"+
		"												\n"+
		"	int i;										\n"+
		"	class Mem1 {}								\n"+
		"	int dumb(String s)							\n"+
		"	int dumb(float fNum, double dNum) {			\n"+
		"		dumb(\"world\", i);						\n"+
		"												\n"+
		"		if (i == 0) {							\n"+
		"			class Local {						\n"+
		"												\n"+
		"				int hello() 					\n",
		// completeBehind:
		"argv",
		// expectedCompletionNodeToString:
		"<CompleteOnArgumentName:String[] argv>",
		// expectedUnitDisplayString:
		"class DD {\n" +
		"  DD() {\n" +
		"  }\n" +
		"  public static void main(<CompleteOnArgumentName:String[] argv>) {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"argv",
		// expectedReplacedSource:
		"argv",
		// test name
		"<complete on argument name with class decl later on>"
	);
}
/*
 * Complete behind array type
 */
public void test13() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class C {						\n"+
		"	void test() {				\n"+
		"		String[].				\n"+
		"	}							\n"+
		"}								\n",
		// completeBehind:
		"String[].",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:String[].>",
		// expectedUnitDisplayString:
		"class C {\n" +
		"  C() {\n" +
		"  }\n" +
		"  void test() {\n" +
		"    <CompleteOnClassLiteralAccess:String[].>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"String[].",
		// test name
		"<complete behind array type>"
	);
}
/*
 * Complete inside array type
 */
public void test14() {
	runTestCheckDietParse(
		// compilationUnit:
		"public class B {			\n"+
		"	class Member {}			\n"+
		"							\n"+
		"	int[] j;				\n",
		// completeBehind:
		"int[",
		// expectedCompletionNodeToString:
		NONE,
		// expectedUnitDisplayString:
		"public class B {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"    }\n" +
		"  }\n" +
		"  public B() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		NONE,
		// test name
		"<complete inside array type>"
	);
}
/*
 * Complete inside array type
 */
public void test15() {
	runTestCheckDietParse(
		// compilationUnit:
		"public class B {			\n"+
		"	class Member {}			\n"+
		"							\n"+
		"	int[					\n",
		// completeBehind:
		"int[",
		// expectedCompletionNodeToString:
		NONE,
		// expectedUnitDisplayString:
		"public class B {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"    }\n" +
		"  }\n" +
		"  public B() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		NONE,
		// test name
		"<complete inside array type>"
	);
}
/*
 * Complete behind invalid array type
 */
public void test16() {
	runTestCheckDietParse(
		// compilationUnit:
		"public class B {			\n"+
		"	class Member {}			\n"+
		"							\n"+
		"	int[					\n"+
		"	Obje					\n",
		// completeBehind:
		"Obje",
		// expectedCompletionNodeToString:
		"<CompleteOnType:Obje>",
		// expectedUnitDisplayString:
		"public class B {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"    }\n" +
		"  }\n" +
		"  <CompleteOnType:Obje>;\n" +
		"  public B() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"Obje",
		// expectedReplacedSource:
		"Obje",
		// test name
		"<complete behind invalid array type>"
	);
}
/*
 * Complete behind invalid base type
 */
public void test17() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class D {				\n" +
		"	class Member {}		\n" +
		"						\n" +
		"	void test() {		\n" +
		"		int.			\n" +
		"		test();			\n" +
		"	}					\n",
		// completeBehind:
		"int.",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int.>",
		// expectedUnitDisplayString:
		"class D {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"    }\n" +
		"  }\n" +
		"  D() {\n" +
		"  }\n" +
		"  void test() {\n" +
		"    <CompleteOnClassLiteralAccess:int.>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"int.",
		// test name
		"<complete behind invalid base type>"
	);
}
/*
 * Complete behind incomplete local method header
 */
public void test18() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class E {					\n"+
		"	int bar() {				\n"+
		"		class Local {		\n"+
		"			int hello() {	\n",
		// completeBehind:
		"hello()",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class E {\n" +
		"  E() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"    class Local {\n" +
		"      Local() {\n" +
		"      }\n" +
		"      int hello() {\n" +
		"      }\n" +
		"    }\n" +
		"    <CompleteOnName:>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete behind incomplete local method header>"
	);
}
/*
 * Complete behind catch variable
 */
public void test19() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Test {					\n" +
		"	void foo() {						\n" +
		"		try {							\n" +
		"		} catch (Exception e) {			\n" +
		"		}								\n" +
		"		e								\n" +
		"	}									\n" +
		"}										\n",
		// completeBehind:
		"\n\t\te",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e>",
		// expectedUnitDisplayString:
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:e>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"e",
		// expectedReplacedSource:
		"e",
		// test name
		"<complete behind catch variable>"
	);
}
/*
 * Complete on catch variable
 */
public void test20() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Test {					\n" +
		"	void foo() {						\n" +
		"		try {							\n" +
		"		} catch (Exception e) {			\n" +
		"			e							\n" +
		"		}								\n" +
		"	}									\n" +
		"}										\n",
		// completeBehind:
		"\n\t\t\te",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e>",
		// expectedUnitDisplayString:
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      Exception e;\n" +
		"      <CompleteOnName:e>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"e",
		// expectedReplacedSource:
		"e",
		// test name
		"<complete on catch variable>"
	);
}
/*
 * Complete on catch variable after syntax error
 */
public void test21() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Test {					\n" +
		"	void foo() {						\n" +
		"		try {							\n" +
		"			bar						\n" +
		"		} catch (Exception e) {			\n" +
		"			e							\n" +
		"		}								\n" +
		"	}									\n" +
		"}										\n",
		// completeBehind:
		"\n\t\t\te",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e>",
		// expectedUnitDisplayString:
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      Exception e;\n" +
		"      <CompleteOnName:e>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n"
		,
		// expectedCompletionIdentifier:
		"e",
		// expectedReplacedSource:
		"e",
		// test name
		"<complete on catch variable after syntax error>"
	);
}
/*
 * Complete on constructor type name
 * 1G1HF7P: ITPCOM:WIN98 - CodeAssist may not work in constructor signature
 */
public void test22() {
	runTestCheckDietParse(
		// compilationUnit:
		"public class SomeType {\n" +
		"	public SomeType(int i){}\n" +
		"}\n" +
		"\n" +
		"class SomeOtherType extends SomeType {\n" +
		"	SomeOtherType(int i){\n" +
		"		super(i);\n" +
		"	}\n" +
		"}\n",
		// completeBehind:
		"	SomeOther",
		// expectedCompletionNodeToString:
		"<CompleteOnType:SomeOther>",
		// expectedUnitDisplayString:
		"public class SomeType {\n" +
		"  public SomeType(int i) {\n" +
		"  }\n" +
		"}\n" +
		"class SomeOtherType extends SomeType {\n" +
		"  <CompleteOnType:SomeOther>;\n" +
		"  int i;\n" +
		"  {\n" +
		"  }\n" +
		"  SomeOtherType() {\n" +
		"  }\n" +
		"}\n"
		,
		// expectedCompletionIdentifier:
		"SomeOther",
		// expectedReplacedSource:
		"SomeOtherType",
		// test name
		"<complete on constructor type name>"
	);
}
/**
 * Complete in initializer in recovery mode
 */
public void test23() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){)\n" +
		"    {\n" +
		"      Obj\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// completeBehind:
		"Obj",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Obj>",
		// expectedUnitDisplayString:
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:Obj>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n"
		,
		// expectedCompletionIdentifier:
		"Obj",
		// expectedReplacedSource:
		"Obj",
		// test name
		"<complete in initializer>"
	);
}
/**
 * Complete after initializer in recovery mode
 */
public void test24() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){)\n" +
		"    int v1;\n" +
		"    {\n" +
		"      int v2\n" +
		"    }\n" +
		"    Obj" +
		"  }\n" +
		"}\n",
		// completeBehind:
		"Obj",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Obj>",
		// expectedUnitDisplayString:
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int v1;\n" +
		"    <CompleteOnName:Obj>;\n" +
		"  }\n" +
		"}\n"
		,
		// expectedCompletionIdentifier:
		"Obj",
		// expectedReplacedSource:
		"Obj",
		// test name
		"<complete after initializer>"
	);
}
/**
 * Complete after dot, before a number .<|>12
 */
public void test25() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"      this.12\n" +
		"  }\n" +
		"}\n",
		// completeBehind:
		"this.",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:this.>",
		// expectedUnitDisplayString:
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:this.>;\n" +
		"  }\n" +
		"}\n"
		,
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"this.",
		// test name
		"<complete after dot number>"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=201762
public void test26() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"\n" +
		"public class Try {\n" +
		"\n" +
		"    void main(Shell shell) {\n" +
		"\n" +
		"        final Label label= new Label(shell, SWT.WRAP);\n" +
		"        label.addPaintListener(new PaintListener() {\n" +
		"            public void paintControl(PaintEvent e) {\n" +
		"                e.gc.setLineCap(SWT.CAP_); // content assist after CAP_\n" +
		"            }\n" +
		"        });\n" +
		"\n" +
		"        shell.addControlListener(new ControlAdapter() { });\n" +
		"\n" +
		"        while (!shell.isDisposed()) { }\n" +
		"    }\n" +
		"}\n" +
		"\n",
		// completeBehind:
		"SWT.CAP_",
		// expectedCompletionNodeToString:
		"<CompleteOnName:SWT.CAP_>",
		// expectedUnitDisplayString:
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"    final Label label;\n" +
		"    label.addPaintListener(new PaintListener() {\n" +
		"  public void paintControl(PaintEvent e) {\n" +
		"    e.gc.setLineCap(<CompleteOnName:SWT.CAP_>);\n" +
		"  }\n" +
		"});\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"CAP_",
		// expectedReplacedSource:
		"SWT.CAP_",
		// test name
		"<complete after dot number>"
	);
}
}
