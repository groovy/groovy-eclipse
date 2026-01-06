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
 * Completion is expected to be in a LabeledStatement.
 */
public class LabelStatementCompletionTest extends AbstractCompletionTest {
public LabelStatementCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(LabelStatementCompletionTest.class);
}
/*
 * Completion inside an inner class defined inside a labeled statement.
 */
public void test1FTEO9L() {
	String cu =
		"package p; 					\n" +
		"								\n" +
		"class CCHelper {				\n" +
		"	class Member1 {				\n" +
		"	}							\n" +
		"	class Member2 {				\n" +
		"	}							\n" +
		"	void foo() {				\n" +
		"	}							\n" +
		"}								\n" +
		"								\n" +
		"public class CC {				\n" +
		"	void foo() {				\n" +
		"		new CCHelper()			\n" +
		"			.new CCHelper()		\n" +
		"			.new M				\n" +
		"	}							\n" +
		"}								\n";
	// first case
	this.runTestCheckMethodParse(
		// compilationUnit:
		cu,
		// completeBehind:
		"			.n",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:new CCHelper().n>",
		// expectedUnitDisplayString:
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
		"    <CompleteOnMemberAccess:new CCHelper().n>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"n",
		// expectedReplacedSource:
		"new",
		// test name
		"<regression test 1FTEO9L (first case)>"
	);
	// second case
	this.runTestCheckMethodParse(
		// compilationUnit:
		cu,
		// completeBehind:
		"			.new CC",
		// expectedCompletionNodeToString:
		"<CompleteOnType:CC>",
		// expectedUnitDisplayString:
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
		"    new CCHelper().new <CompleteOnType:CC>();\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"CC",
		// expectedReplacedSource:
		"CCHelper",
		// test name
		"<regression test 1FTEO9L (second case)>"
	);
	// third case
	this.runTestCheckMethodParse(
		// compilationUnit:
		cu,
		// completeBehind:
		"			.new CCHelper()		\n" +
		"			.n",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:new CCHelper().new CCHelper().n>",
		// expectedUnitDisplayString:
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
		"    <CompleteOnMemberAccess:new CCHelper().new CCHelper().n>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"n",
		// expectedReplacedSource:
		"new",
		// test name
		"<regression test 1FTEO9L (third case)>"
	);
	// fourth case
	this.runTestCheckMethodParse(
		// compilationUnit:
		cu,
		// completeBehind:
		"			.new CCHelper()		\n" +
		"			.new M",
		// expectedCompletionNodeToString:
		"<CompleteOnType:M>",
		// expectedUnitDisplayString:
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
		"}\n",
		// expectedCompletionIdentifier:
		"M",
		// expectedReplacedSource:
		"M",
		// test name
		"<regression test 1FTEO9L (fourth case)>"
	);
}
/*
 * Completion inside a case that has an identifier as its constant expression.
 */
public void testInCaseWithIdentifier() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {										\n" +
		"	void foo() {									\n" +
		"		label1: {									\n" +
		"			switch (i) {							\n" +
		"				case a: label2: X o = new Object();	\n" +
		"			}										\n" +
		"		}											\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      {\n" +
		"        <CompleteOnName:X>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label1", "label2"},
		// test name
		"<complete in case with identifier>"
	);
}
/*
 * Completion inside a case that has a number as its constant expression.
 */
public void testInCaseWithNumberConstant() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {										\n" +
		"	void foo() {									\n" +
		"		label1: {									\n" +
		"			switch (i) {							\n" +
		"				case 1: label2: X o = new Object();	\n" +
		"			}										\n" +
		"		}											\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      {\n" +
		"        <CompleteOnName:X>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label1", "label2"},
		// test name
		"<complete in case with number>"
	);
}
/*
 * Completion inside an inner class defined inside a labeled statement.
 */
public void testInLabeledInnerClass() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		label1: {								\n" +
		"			Object o = new Object() {			\n" +
		"				void fred() {					\n" +
		"					label2: {					\n" +
		"						X o = new Object();		\n" +
		"					}							\n" +
		"				}								\n" +
		"			};									\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      Object o = new Object() {\n" +
		"        void fred() {\n" +
		"          label2: {\n" +
		"  <CompleteOnType:X> o;\n" +
		"}\n" +
		"        }\n" +
		"      };\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label2"},
		// test name
		"<complete in labeled inner class>"
	);
}
/*
 * Completion inside an inner class defined inside a labeled statement with a syntax error
 * just before the labeled statement.
 */
public void testInLabeledInnerClassWithErrorBefore() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		int i == 2; \n" +
		"		label1: {								\n" +
		"			Object o = new Object() {			\n" +
		"				void fred() {					\n" +
		"					label2: {					\n" +
		"						X o = new Object();		\n" +
		"					}							\n" +
		"				}								\n" +
		"			};									\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    {\n" +
		"      Object o;\n" +
		"      new Object() {\n" +
		"        void fred() {\n" +
		"          {\n" +
		"            <CompleteOnName:X>;\n" +
		"          }\n" +
		"        }\n" +
		"      };\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label2"},
		// test name
		"<complete in labeled inner class with syntax error before>"
	);
}
/*
 * Completion inside a labeled statement one level deep.
 */
public void testOneLevelDeep() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		label1: X o = new Object();				\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:X>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label1"},
		// test name
		"<complete in one level deep>"
	);
}
/*
 * Completion inside a labeled statement which is the second one in the method.
 */
public void testSecondLabel() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		label1: buzz();							\n" +
		"		label2: X o = new Object();				\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:X>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label2"},
		// test name
		"<complete in second labeled statement>"
	);
}
/*
 * Completion inside a labeled statement two level deep.
 */
public void testTwoLevelDeep() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		label1: {								\n" +
		"			label2: X o = new Object();			\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:X>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label1", "label2"},
		// test name
		"<complete in two level deep>"
	);
}
}
