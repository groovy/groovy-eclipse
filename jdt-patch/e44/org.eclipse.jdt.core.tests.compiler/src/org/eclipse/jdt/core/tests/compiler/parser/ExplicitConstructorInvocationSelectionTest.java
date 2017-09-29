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

/**
 * Selection is expected to be wrapped with an explicit constructor invocation.
 */
public class ExplicitConstructorInvocationSelectionTest extends AbstractSelectionTest {
public ExplicitConstructorInvocationSelectionTest(String testName) {
	super(testName);
}
/*
 * ExplicitConstructorInvocation ::= Name '.' 'super' '(' <ArgumentListopt> ')' ';'
 */
public void testNameSuper() {
	runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"		InnerBar(Bar x) {						\n" +
		"		}										\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar() {							\n" +
		"			Bar.super(fred());					\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// selectionStartBehind:
		"Bar.super(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    InnerBar(Bar x) {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar() {\n" +
		"      super(<SelectOnMessageSend:fred()>);\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar x;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation name super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Name '.' 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testNameThis() {
	runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"		InnerBar(Bar x) {						\n" +
		"		}										\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar() {							\n" +
		"			Bar.this(fred());					\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// selectionStartBehind:
		"Bar.this(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    InnerBar(Bar x) {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar() {\n" +
		"      this(<SelectOnMessageSend:fred()>);\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar x;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation name this>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Primary '.' 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testPrimarySuper() {
	runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"		InnerBar(Bar x) {						\n" +
		"		}										\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar(Bar x) {					\n" +
		"			primary().super(fred());			\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// selectionStartBehind:
		"super(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    InnerBar(Bar x) {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar(Bar x) {\n" +
		"      super(<SelectOnMessageSend:fred()>);\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar x;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation primary super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= 'super' '(' <ArgumentListopt> ')' ';'
 */
public void testSuper() {
	runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	Bar() {										\n" +
		"		super(fred());							\n" +
		"	}											\n" +
		"}												\n",
		// selectionStartBehind:
		"super(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"    super(<SelectOnMessageSend:fred()>);\n" +
		"  }\n" +
		"}\n",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testThis() {
	runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	Bar() {										\n" +
		"		this(fred());							\n" +
		"	}											\n" +
		"}												\n",
		// selectionStartBehind:
		"this(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"    this(<SelectOnMessageSend:fred()>);\n" +
		"  }\n" +
		"}\n",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation this>"
	);
}
}
