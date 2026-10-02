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

/**
 * Completion is expected to be a name reference.
 */
public class NameReferenceCompletionTest extends AbstractCompletionTest {
public NameReferenceCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(NameReferenceCompletionTest.class);
}
/*
 * Regression test for 1FTZ849.
 * The instance creation before the completion is not properly closed, and thus
 * the completion parser used to think the completion was on a type.
 */
public void test1FTZ849() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		X o = new X;						\n" +
		"		fred.xyz;							\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred.x",
		// expectedCompletionNodeToString:
		"<CompleteOnName:fred.x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    X o;\n" +
		"    <CompleteOnName:fred.x>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"fred.xyz",
		// test name
		"<1FTZ849>"
	);
}
/*
 * Completion in a field initializer with no syntax error.
 */
public void test1FUUP73() {
	runTestCheckDietParse(
		// compilationUnit:
		"public class A {					\n" +
		"	String s = \"hello\";			\n" +
		"	Object o = s.concat(\"boo\");	\n",
		// completeBehind:
		"Object o = s",
		// expectedCompletionNodeToString:
		"<CompleteOnName:s>",
		// expectedUnitDisplayString:
		"public class A {\n" +
		"  String s;\n" +
		"  Object o = <CompleteOnName:s>;\n" +
		"  public A() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"s",
		// expectedReplacedSource:
		"s",
		// test name
		"<1FUUP73>"
	);
	runTestCheckDietParse(
		// compilationUnit:
		"public class A {					\n" +
		"	String s = \"hello\";			\n" +
		"	Object o = s.concat(\"boo\");	\n",
		// completeBehind:
		"Object o = s.c",
		// expectedCompletionNodeToString:
		"<CompleteOnName:s.c>",
		// expectedUnitDisplayString:
		"public class A {\n" +
		"  String s;\n" +
		"  Object o = <CompleteOnName:s.c>;\n" +
		"  public A() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"c",
		// expectedReplacedSource:
		"s.concat",
		// test name
		"<1FUUP73>"
	);
}
/*
 * Regression test for 1FVRQQA.
 */
public void test1FVRQQA_1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		Enumeration e = null; 				\n" +
		"		e.to								\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"e.to",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e.to>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Enumeration e;\n" +
		"    <CompleteOnName:e.to>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"to",
		// expectedReplacedSource:
		"e.to",
		// test name
		"<1FVRQQA_1>"
	);
}
/*
 * Regression test for 1FVRQQA.
 */
public void test1FVRQQA_2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {													\n" +
		"	void foo() {											\n" +
		"		for (Enumeration e = getSomeEnumeration(); e.has	\n" +
		"	}														\n" +
		"}															\n",
		// completeBehind:
		"e.has",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e.has>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Enumeration e;\n" +
	    "    for (; <CompleteOnName:e.has>; ) \n" +
	    "      ;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"has",
		// expectedReplacedSource:
		"e.has",
		// test name
		"<1FVRQQA_2>"
	);
}
/*
 * Regression test for 1FVT66Q.
 */
public void test1FVT66Q_1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"package test;							\n" +
		"										\n" +
		"public class Test {					\n" +
		"	public void foo() {					\n" +
		"		final int codeAssistTarget= 3;	\n" +
		"										\n" +
		"		Thread t= new Thread() {		\n" +
		"			public void run() {			\n" +
		"				codeAss					\n" +
		"			}							\n" +
		"		};								\n" +
		"		codeA							\n" +
		"	}									\n" +
		"}										\n",
		// completeBehind:
		"	codeAss",
		// expectedCompletionNodeToString:
		"<CompleteOnName:codeAss>",
		// expectedUnitDisplayString:
		"package test;\n" +
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    final int codeAssistTarget;\n" +
		"    Thread t;\n" +
		"    new Thread() {\n" +
		"      public void run() {\n" +
		"        <CompleteOnName:codeAss>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"codeAss",
		// expectedReplacedSource:
		"codeAss",
		// test name
		"<1FVT66Q_1>"
	);
}
/*
 * Regression test for 1FVT66Q.
 */
public void test1FVT66Q_2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"package test;							\n" +
		"										\n" +
		"public class Test {					\n" +
		"	public void foo() {					\n" +
		"		final int codeAssistTarget= 3;	\n" +
		"										\n" +
		"		Thread t= new Thread() {		\n" +
		"			public void run() {			\n" +
		"				codeAss					\n" +
		"			}							\n" +
		"		};								\n" +
		"		codeA							\n" +
		"	}									\n" +
		"}										\n",
		// completeBehind:
		"\n		codeA",
		// expectedCompletionNodeToString:
		"<CompleteOnName:codeA>",
		// expectedUnitDisplayString:
		"package test;\n" +
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    final int codeAssistTarget;\n" +
		"    Thread t;\n" +
		"    <CompleteOnName:codeA>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"codeA",
		// expectedReplacedSource:
		"codeA",
		// test name
		"<1FVT66Q_2>"
	);
}
/*
 * Regression test for 1G8DE30.
 */
public void test1G8DE30() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		new Runnable() {					\n" +
		"			public void run() {				\n" +
		"				Bar							\n" +
		"			}								\n" +
		"		};									\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"public void run() {				\n				",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new Runnable() {\n" +
		"      public void run() {\n" +
		"        <CompleteOnName:>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"Bar",
		// test name
		"<1G8DE30>"
	);
}
/*
 * Completion on an empty name reference.
 */
public void testEmptyNameReference() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"											\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"int i = 0;							\n		",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    <CompleteOnName:>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference>"
	);
}
/*
 * Completion on an empty name reference after a cast.
 */
public void testEmptyNameReferenceAfterCast() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		X x = (X)							\n" +
		"											\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"(X)",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    X x = (X) <CompleteOnName:>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference after cast>"
	);
}
/*
 * Completion on an empty name reference after + operator.
 */
public void testEmptyNameReferenceAfterPlus() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		1 + 								\n" +
		"											\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"1 +",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference after + operator>"
	);
}
/*
 * Completion on an empty name reference in an array dimension.
 */
public void testEmptyNameReferenceInArrayDim() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		int[]								\n" +
		"											\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"int[",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference in array dim>"
	);
}
/*
 * Completion on an empty name reference in inner class.
 */
public void testEmptyNameReferenceInInnerClass() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		class Y {							\n" +
		"			void bar() {					\n" +
		"											\n" +
		"			}								\n" +
		"		}									\n" +
		"											\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"\n				",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class Y {\n" +
		"      Y() {\n" +
		"      }\n" +
		"      void bar() {\n" +
		"        <CompleteOnName:>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference in inner class>"
	);
}
/*
 * Completion in the statement following an if expression.
 */
public void testInIfThenStatement() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		if (bar()) 							\n" +
		"											\n" +
		"											\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"\n			",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete in if then statement>"
	);
}
/*
 * Completion in the statement following an if expression.
 */
public void testInIfThenWithInstanceOfStatement() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		if (this instanceof Bar) 			\n" +
		"											\n" +
		"											\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"\n			",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    if ((this instanceof Bar))\n" +
		"        <CompleteOnName:>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete in if then statement>"
	);
}
/*
 * Completion on a name reference inside an inner class in a field initializer.
 */
public void testInnerClassFieldInitializer() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	Object o = new Object() {				\n" +
		"		void foo() {						\n" +
		"			xyz								\n" +
		"		}									\n" +
		"	};										\n" +
		"}											\n",
		// completeBehind:
		"xyz",
		// expectedCompletionNodeToString:
		"<CompleteOnName:xyz>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Object o = new Object() {\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:xyz>;\n" +
		"    }\n" +
		"  };\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"xyz",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on name reference in inner class in field initializer>"
	);
}
/*
 * Completion on an empty name reference inside an invocation in a field initializer.
 */
public void testInvocationFieldInitializer() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	String s = fred(1 + );					\n" +
		"	void foo() {							\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"(1 + ",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  String s = (1 + <CompleteOnName:>);\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference in invocation in field initializer>"
	);
}
/*
 * Completion inside an anonymous inner class which is
 * inside a method invocation with receiver.
 */
public void testMethodInvocationAnonymousInnerClass() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		primary().bizz(							\n" +
		"			new X() {							\n" +
		"				void fuzz() {					\n" +
		"					x.y.z						\n" +
		"				}								\n" +
		"			}									\n"	+
		"		);										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:x.>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new X() {\n" +
		"      void fuzz() {\n" +
		"        <CompleteOnName:x.>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"x.y.z",
		// test name
		"<complete inside anonymous inner class inside method invocation 1>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is in the
 * first type reference.
 */
public void testQualifiedNameReferenceShrinkAll() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"		a.b.c.Xxx o = new Y(i);		\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"		a",
		// expectedCompletionNodeToString:
		"<CompleteOnName:a>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    <CompleteOnName:a>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"a",
		// expectedReplacedSource:
		"a",
		// test name
		"<complete on qualified name reference (shrink all)>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the first dot.
 */
public void testQualifiedNameReferenceShrinkAllButOne() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar() {							\n" +
		"			Bar.x.x.super();					\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"Bar.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Bar.>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    public InnerBar() {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar() {\n" +
		"      super();\n" +
		"      <CompleteOnName:Bar.>;\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar x;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"Bar.",
		// test name
		"<complete on qualified name reference (shrink all but one)>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the first dot.
 */
public void testQualifiedNameReferenceShrinkAllButOne2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"		a.b.c.X o = new Y(i);		\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"		a.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:a.>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    <CompleteOnName:a.>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"a.",
		// test name
		"<complete on qualified name reference (shrink all but one) 2>"
	);
}
/*
 * Completion on a qualified name reference,where the cursor is right after the end
 * of the last name reference.
 */
public void testQualifiedNameReferenceShrinkNone() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar() {							\n" +
		"			Bar.x.x.super();					\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"Bar.x.x",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Bar.x.x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    public InnerBar() {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar() {\n" +
		"      super();\n" +
		"      <CompleteOnName:Bar.x.x>;\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar x;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"Bar.x.x",
		// test name
		"<complete on qualified name reference (shrink none)>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the end
 * of the last type reference.
 */
public void testQualifiedNameReferenceShrinkNone2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"		a.b.c.Xxx o = new Y(i);		\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:a.b.c.X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    <CompleteOnName:a.b.c.X>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"a.b.c.Xxx",
		// test name
		"<complete on qualified name reference (shrink none) 2>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the
 * last dot.
 */
public void testQualifiedNameReferenceShrinkOne() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar() {							\n" +
		"			Bar.x.x.super();					\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"Bar.x.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Bar.x.>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    public InnerBar() {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar() {\n" +
		"      super();\n" +
		"      <CompleteOnName:Bar.x.>;\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar x;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"Bar.x.",
		// test name
		"<complete on qualified name reference (shrink one)>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the
 * last dot.
 */
public void testQualifiedNameReferenceShrinkOne2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"		a.b.c.X o = new Y(i);		\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"a.b.c.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:a.b.c.>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    <CompleteOnName:a.b.c.>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"a.b.c.",
		// test name
		"<complete on qualified name reference (shrink one) 2>"
	);
}
/*
 * Completion on a qualified name reference that contains a unicode.
 */
public void testUnicode() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {					\n" +
		"	void foo() {			\n" +
		"		bar.\\u005ax 		\n" +
		"	}						\n" +
		"}							\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnName:bar.Zx>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:bar.Zx>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"Zx",
		// expectedReplacedSource:
		"bar.\\u005ax",
		// test name
		"<complete on unicode>"
	);
}
}
