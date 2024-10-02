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
 * Completion is expected to be an AllocationExpression.
 */
public class AllocationExpressionCompletionTest extends AbstractCompletionTest {
public AllocationExpressionCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(AllocationExpressionCompletionTest.class);
}
/*
 * Completion inside an if statement.
 */
public void testInIfStatement1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		if (true) {							\n" +
		"			new z.y.X(1, 2, i);				\n" +
		"		}									\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(",
		// expectedCompletionNodeToString:
		"<CompleteOnAllocationExpression:new z.y.X(<CompleteOnName:>, 2, i)>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    if (true)\n" +
		"        {\n" +
		"          <CompleteOnAllocationExpression:new z.y.X(<CompleteOnName:>, 2, i)>;\n" +
		"        }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"new z.y.X(1, 2, i)",
		// test name
		"<complete inside an if statement>"
	);
}
public void testInIfStatement2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		if (true) {							\n" +
		"			new z.y.X(1, 2, i);				\n" +
		"		}									\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    if (true)\n" +
		"        {\n" +
		"          new z.y.X(1, 2, <CompleteOnName:>, i);\n" +
		"        }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete inside an if statement>"
	);
}
/*
 * Completion on a constructor invocation with no qualification and using a qualified type name.
 *
 * ie. ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt
 *		where ClassType is a qualified type name
 */
public void testNoQualificationQualifiedTypeName1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		new z.y.X(1, 2, i);					\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(",
		// expectedCompletionNodeToString:
		"<CompleteOnAllocationExpression:new z.y.X()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnAllocationExpression:new z.y.X()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on non qualified instance creation with qualified type name>"
	);
}
public void testNoQualificationQualifiedTypeName2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		new z.y.X(1, 2, i);					\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new z.y.X(1, 2, <CompleteOnName:>, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on non qualified instance creation with qualified type name>"
	);
}
/*
 * Completion on a constructor invocation with no qualification and using a simple type name.
 *
 * ie. ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt
 *		where ClassType is a simple type name
 */
public void testNoQualificationSimpleTypeName1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		new X(1, 2, i);						\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(",
		// expectedCompletionNodeToString:
		"<CompleteOnAllocationExpression:new X()>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnAllocationExpression:new X()>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on non qualified instance creation with simple type name>"
	);
}
public void testNoQualificationSimpleTypeName2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		new X(1, 2, i);						\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new X(1, 2, <CompleteOnName:>, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on non qualified instance creation with simple type name>"
	);
}
/*
 * Completion on a constructor invocation qualified with a name.
 *
 * ie. ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testQualifiedWithName1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {\n" +
		"	void foo() {							\n" +
		"		Buz.x.new X(1, 2, i);				\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(",
		// expectedCompletionNodeToString:
		"<CompleteOnQualifiedAllocationExpression:Buz.x.new X(<CompleteOnName:>, 2, i)>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnQualifiedAllocationExpression:Buz.x.new X(<CompleteOnName:>, 2, i)>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"Buz.x.new X(1, 2, i)",
		// test name
		"<complete on name qualified instance creation>"
	);
}
public void testQualifiedWithName2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {\n" +
		"	void foo() {							\n" +
		"		Buz.x.new X(1, 2, i);				\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Buz.x.new X(1, 2, <CompleteOnName:>, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on name qualified instance creation>"
	);
}
/*
 * Completion on a constructor invocation qualified with a primary.
 *
 * ie. ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testQualifiedWithPrimary1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		primary().new X(1, 2, i);			\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(",
		// expectedCompletionNodeToString:
		"<CompleteOnQualifiedAllocationExpression:primary().new X(<CompleteOnName:>, 2, i)>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnQualifiedAllocationExpression:primary().new X(<CompleteOnName:>, 2, i)>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"primary().new X(1, 2, i)",
		// test name
		"<complete on primary qualified instance creation>"
	);
}
public void testQualifiedWithPrimary2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		primary().new X(1, 2, i);			\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    primary().new X(1, 2, <CompleteOnName:>, i);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on primary qualified instance creation>"
	);
}
}
