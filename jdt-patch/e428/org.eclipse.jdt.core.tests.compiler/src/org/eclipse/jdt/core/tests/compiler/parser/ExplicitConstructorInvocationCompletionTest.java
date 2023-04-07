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
 * Completion is expected to be an ExplicitConstructorInvocation
 * or inside an ExplicitConstructorInvocation
 */
public class ExplicitConstructorInvocationCompletionTest extends AbstractCompletionTest {
public ExplicitConstructorInvocationCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(ExplicitConstructorInvocationCompletionTest.class);
}
/*
 * Completion on a qualified 'super' constructor invocation.
 *
 * ie. ExplicitConstructorInvocation ::= Primary '.' 'super' '(' ArgumentListopt ')' ';'
 */
public void testPrimarySuper() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"		InnerBar(Bar x) {						\n" +
		"		}										\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar(Bar x) {					\n" +
		"			primary().super(1, 2, i);			\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"super(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    InnerBar(Bar x) {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar(Bar x) {\n" +
		"      primary().super(1, 2, <CompleteOnName:>, i);\n" +
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
		"",
		// test name
		"<complete on explicit constructor invocation primary super>"
	);
}
/*
 * Completion on a qualified 'this' constructor invocation.
 *
 * ie. ExplicitConstructorInvocation ::= Primary '.' 'this' '(' ArgumentListopt ')' ';'
 */
public void testPrimaryThis() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"		InnerBar(Bar x) {						\n" +
		"		}										\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar(Bar x) {					\n" +
		"			primary().this(1, 2, i);			\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"this(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    InnerBar(Bar x) {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar(Bar x) {\n" +
		"      primary().this(1, 2, <CompleteOnName:>, i);\n" +
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
		"",
		// test name
		"<complete on explicit constructor invocation primary this>"
	);
}
/*
 * Completion on a 'super' constructor invocation.
 *
 * ie. ExplicitConstructorInvocation ::= 'super' '(' ArgumentListopt ')' ';'
 */
public void testSuper() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	Bar() {									\n" +
		"		super(1, 2, i);						\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"super(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"    super(1, 2, <CompleteOnName:>, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion on 'super' constructor invocation>"
	);
}
/*
 * Completion on a 'this' constructor invocation.
 *
 * ie. ExplicitConstructorInvocation ::= 'this' '(' ArgumentListopt ')' ';'
 */
public void testThis() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	Bar() {									\n" +
		"		this(1, 2, i);						\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"this(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"    this(1, 2, <CompleteOnName:>, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion on 'this' constructor invocation>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Name '.' 'super' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperNameSuper() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"		InnerBar(Bar x) {						\n" +
		"		}										\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar() {							\n" +
		"			Bar.super(fred().xyz);				\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"fred().x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    InnerBar(Bar x) {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar() {\n" +
		"      Bar.super(<CompleteOnMemberAccess:fred().x>);\n" +
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
		"xyz",
		// test name
		"<complete on wrapper name super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Name '.' 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperNameThis() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"		InnerBar(Bar x) {						\n" +
		"		}										\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar() {							\n" +
		"			Bar.this(fred().xyz);				\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"fred().x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    InnerBar(Bar x) {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar() {\n" +
		"      Bar.this(<CompleteOnMemberAccess:fred().x>);\n" +
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
		"xyz",
		// test name
		"<complete on wrapper name this>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Primary '.' 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperPrimarySuper() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	static Bar x;								\n" +
		"	public class InnerBar {						\n" +
		"		InnerBar(Bar x) {						\n" +
		"		}										\n" +
		"	}											\n" +
		"	public class SubInnerBar extends InnerBar {	\n" +
		"		SubInnerBar(Bar x) {					\n" +
		"			primary().super(fred().xyz);			\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"fred().x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  public class InnerBar {\n" +
		"    InnerBar(Bar x) {\n" +
		"    }\n" +
		"  }\n" +
		"  public class SubInnerBar extends InnerBar {\n" +
		"    SubInnerBar(Bar x) {\n" +
		"      primary().super(<CompleteOnMemberAccess:fred().x>);\n" +
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
		"xyz",
		// test name
		"<complete on wrapper primary super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= 'super' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperSuper() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	Bar() {										\n" +
		"		super(fred().xyz);						\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"    super(<CompleteOnMemberAccess:fred().x>);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on wrapper super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperThis() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	Bar() {										\n" +
		"		this(fred().xyz);							\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"    this(<CompleteOnMemberAccess:fred().x>);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on wrapper this>"
	);
}
}
