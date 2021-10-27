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
 * Completion is expected to be a MethodInvocation.
 */
public class MethodInvocationCompletionTest extends AbstractCompletionTest {
public MethodInvocationCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(MethodInvocationCompletionTest.class);
}
/*
 * Completion with no receiver inside a for statement.
 */
public void test1FVVWS8_1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		for (int i = 10; i > 0; --i)		\n" +
		"			fred(							\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    <CompleteOnMessageSend:fred()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FVVWS8_1>"
	);
}
/*
 * Completion with no receiver inside an if statement.
 */
public void test1FVVWS8_2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		if (true)							\n" +
		"			fred(							\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:fred()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FVVWS8_2>"
	);
}
/*
 * Completion with no receiver inside a for statement
 * and after a field access.
 */
public void test1FW2ZTB_1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {											\n" +
		"	int[] array;									\n" +
		"	void foo() {									\n" +
		"		for (int i = this.array.length; i > 0; --i)	\n" +
		"			fred(									\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  int[] array;\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    <CompleteOnMessageSend:fred()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FW2ZTB_1"
	);
}
/*
 * Completion with no receiver inside another message send
 * and after a field access in a previous argument.
 */
public void test1FW2ZTB_2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {											\n" +
		"	int[] array;									\n" +
		"	void foo() {									\n" +
		"		bar(this.array.length, 10, fred(			\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  int[] array;\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:fred()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FW2ZTB_2"
	);
}
/*
 * Complete on method invocation with expression receiver
 * inside another invocation with no receiver.
 */
public void test1FW35YZ_1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		bar(primary().fred(					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:primary().fred()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:primary().fred()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FW35YZ_1>"
	);
}
/*
 * Complete on qualified allocation expression
 * inside an invocation with no receiver.
 */
public void test1FW35YZ_2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		bar(primary().new X(				\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X(",
		// expectedCompletionNodeToString:
		"<CompleteOnQualifiedAllocationExpression:primary().new X()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnQualifiedAllocationExpression:primary().new X()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<1FW35YZ_2>"
	);
}
/*
 * Completion with primary receiver.
 */
public void test1FWYBKF() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"			this.x.bar(						\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"bar(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:this.x.bar()>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:this.x.bar()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"bar(",
		// test name
		"<1FWYBKF>"
	);
}
/*
 * Completion just after a parameter which is a message send.
 */
public void test1GAJBUQ() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		x.y.Z.fred(buzz());					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(buzz()",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:x.y.Z.fred(buzz())>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:x.y.Z.fred(buzz())>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(buzz()",
		// test name
		"<1GAJBUQ>"
	);
}
/*
 * Completion just before the second parameter, the first parameter being an empty
 * anonymous class.
 */
public void testAfterEmptyAnonymous() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		this.fred(new Runnable() {}, 2, i);	\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(new Runnable() {}, ",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    this.fred(new Runnable() {\n" +
		"}, <CompleteOnName:>, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion just before second parameter, the first parameter being an empty anonymous class>"
	);
}
/*
 * Completion just after the first parameter.
 */
public void testAfterFirstParameter() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		this.fred(\"abc\" , 2, i);		\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(\"abc\" ",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:this.fred(\"abc\")>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:this.fred(\"abc\")>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(\"abc\" ",
		// test name
		"<completion just after first parameter>"
	);
}
/*
 * Completion just before the first parameter.
 */
public void testBeforeFirstParameter() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		this.fred(1, 2, i);					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:this.fred(<CompleteOnName:>, 2, i)>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:this.fred(<CompleteOnName:>, 2, i)>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(1, 2, i)",
		// test name
		"<completion just before first parameter>"
	);
}
/*
 * Completion just before the last parameter.
 */
public void testBeforeLastParameter() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		this.fred(1, 2, i);					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    this.fred(1, 2, <CompleteOnName:>, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion just before last parameter>"
	);
}
/*
 * Completion just before the second parameter.
 */
public void testBeforeSecondParameter() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		this.fred(1, 2, i);					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(1, ",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    this.fred(1, <CompleteOnName:>, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion just before second parameter>"
	);
}
/*
 * Completion on empty name inside the expression of the first parameter.
 */
public void testEmptyInFirstParameter() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		this.fred(\"abc\" + , 2, i);		\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(\"abc\" +",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    this.fred((\"abc\" + <CompleteOnName:>), 2, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion empty in first parameter>"
	);
}
/*
 * Completion inside the expression of the first parameter.
 */
public void testInFirstParameter() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		this.fred(\"abc\" + bizz, 2, i);	\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(\"abc\" + bi",
		// expectedCompletionNodeToString:
		"<CompleteOnName:bi>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    this.fred((\"abc\" + <CompleteOnName:bi>), 2, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"bi",
		// expectedReplacedSource:
		"bizz",
		// test name
		"<completion inside first parameter>"
	);
}
/*
 * Completion inside an if statement.
 */
public void testInIfStatement() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		if (true) {							\n" +
		"			bar.fred();						\n" +
		"		}									\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:bar.fred()>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    if (true)\n" +
		"        {\n" +
		"          <CompleteOnMessageSend:bar.fred()>;\n" +
		"        }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred()",
		// test name
		"<completion inside a if statement>"
	);
}
/*
 * Completion in labeled method invocation with expression receiver.
 */
public void testLabeledWithExpressionReceiver() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		label1: bar().fred(1, 2, o);		\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    label1: bar().fred(1, 2, <CompleteOnName:>, o);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// expectedLabels:
		new String[] {"label1"},
		// test name
		"<completion in labeled method invocation with expression receiver>"
	);
}
/*
 * Completion in labeled method invocation without receiver.
 */
public void testLabeledWithoutReceiver() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		label1: fred(1, 2, o);				\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    label1: fred(1, 2, <CompleteOnName:>, o);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// expectedLabels:
		new String[] {"label1"},
		// test name
		"<completion in labeled method invocation without receiver>"
	);
}
/*
 * MethodInvocation ::= Name '(' ArgumentListopt ')'
 */
public void testNoReceiver() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		fred();								\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:fred()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<completion on method invocation with no receiver>"
	);
}
/*
 * Completion just before the first parameter with a space after the open parenthesis.
 */
public void testSpaceThenFirstParameter() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		this.fred( 1, 2, i);				\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred( ",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    this.fred(<CompleteOnName:>, 2, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion just before first parameter with a space after open parenthesis>"
	);
}
/*
 * MethodInvocation ::= 'super' '.' 'Identifier' '(' ArgumentListopt ')'
 */
public void testSuper() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		super.fred(1, 2, i);				\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:super.fred()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:super.fred()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<completion on super method invocation>"
	);
}
/*
 * Complete on method invocation with expression receiver.
 */
public void testWithExpressionReceiver() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		bar().fred();						\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:bar().fred()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:bar().fred()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred()",
		// test name
		"<completion on method invocation with expression receiver>"
	);
}
/*
 * Completion with a name receiver.
 */
public void testWithNameReceiver() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		Vector v = new Vector();			\n" +
		"		v.addElement(\"1\");				\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"addElement(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:v.addElement()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Vector v;\n" +
		"    <CompleteOnMessageSend:v.addElement()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"addElement(",
		// test name
		"<completion with name receiver>"
	);
}
/*
 * Completion with a name receiver after conditional expression.
 */
public void testWithNameReceiverAfterConditionalExpression() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class X {									\n" +
		"	void foo() {							\n" +
		"		buzz.test(cond ? max : min);		\n" +
		"		bar.fred();							\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:bar.fred()>",
		// expectedUnitDisplayString:
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMessageSend:bar.fred()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// expectedLabels:
		new String[] {},
		// test name
		"<completion with name receiver after conditional expression>"
	);
}
/*
 * Completion with a name receiver and 2 arguments.
 */
public void testWithNameReceiverAndTwoArgs() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		X x = new X();						\n" +
		"		x.fred(1, 2, o);					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"x.fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    X x;\n" +
		"    x.fred(1, 2, <CompleteOnName:>, o);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion with name receiver and 2 arguments>"
	);
}
/*
 * Completion with a qualified name receiver.
 */
public void testWithQualifiedNameReceiver() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		X x = new X();						\n" +
		"		y.x.fred(1, 2, o);					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"x.fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    X x;\n" +
		"    y.x.fred(1, 2, <CompleteOnName:>, o);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion with qualified name receiver>"
	);
}
}
