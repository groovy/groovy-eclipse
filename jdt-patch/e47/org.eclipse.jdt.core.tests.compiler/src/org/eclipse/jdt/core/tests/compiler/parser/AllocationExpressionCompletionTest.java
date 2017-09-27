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
public void testInIfStatement() {
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
		"<CompleteOnAllocationExpression:new z.y.X(1, 2)>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnAllocationExpression:new z.y.X(1, 2)>;\n" +
		"    }\n" +
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
public void testNoQualificationQualifiedTypeName() {
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
		"<CompleteOnAllocationExpression:new z.y.X(1, 2)>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnAllocationExpression:new z.y.X(1, 2)>;\n" +
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
public void testNoQualificationSimpleTypeName() {
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
		"<CompleteOnAllocationExpression:new X(1, 2)>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnAllocationExpression:new X(1, 2)>;\n" +
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
public void testQualifiedWithName() {
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
		"<CompleteOnQualifiedAllocationExpression:Buz.x.new X(1, 2)>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnQualifiedAllocationExpression:Buz.x.new X(1, 2)>;\n" +
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
public void testQualifiedWithPrimary() {
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
		"<CompleteOnQualifiedAllocationExpression:primary().new X(1, 2)>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnQualifiedAllocationExpression:primary().new X(1, 2)>;\n" +
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
