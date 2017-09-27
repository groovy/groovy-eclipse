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
 * Completion is expected to be a ClassLiteralAccess.
 */
public class ClassLiteralAccessCompletionTest extends AbstractCompletionTest {
public ClassLiteralAccessCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(ClassLiteralAccessCompletionTest.class);
}
/*
 * Completion on the keyword 'class' on an array type
 */
public void testArrayType() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {										\n" +
		"	void foo() {									\n" +
		"		String[].;									\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"String[].",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:String[].>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnClassLiteralAccess:String[].>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"String[].",
		// test name
		"<complete on array type member>"
	);
}
/*
 * Test access to the keyword 'class' on an array type
 * where the keyword is non empty.
 */
public void testArrayTypeWithNonEmptyIdentifier() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {										\n" +
		"	void foo() {									\n" +
		"		String[].class;								\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"String[].cl",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:String[].cl>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnClassLiteralAccess:String[].cl>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"cl",
		// expectedReplacedSource:
		"String[].class",
		// test name
		"<complete on array type member with non empty identifier>"
	);
}
/*
 * Completion on the keyword 'class' on a primitive array type
 */
public void testPrimitiveArrayType() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {										\n" +
		"	void foo() {									\n" +
		"		int[].;										\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"int[].",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int[].>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnClassLiteralAccess:int[].>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"int[].",
		// test name
		"<complete on primitive array type member>"
	);
}
/*
 * Completion on the keyword 'class' on a primitive array type where the
 * keyword is non empty
 */
public void testPrimitiveArrayTypeWithNonEmptyIdentifier() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {										\n" +
		"	void foo() {									\n" +
		"		int[].class;								\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"int[].cl",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int[].cl>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnClassLiteralAccess:int[].cl>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"cl",
		// expectedReplacedSource:
		"int[].class",
		// test name
		"<complete on primitive array type member with non empty identifier>"
	);
}
/*
 * Completion on the keyword 'class' on a primitive type
 */
public void testPrimitiveType() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {										\n" +
		"	void foo() {									\n" +
		"		int.;										\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"int.",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int.>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnClassLiteralAccess:int.>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"int.",
		// test name
		"<complete on primitive type member>"
	);
}
/*
 * Completion on the keyword 'class' on a primitive type where the
 * keyword is non empty
 */
public void testPrimitiveTypeWithNonEmptyIdentifier() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {										\n" +
		"	void foo() {									\n" +
		"		int.class;									\n" +
		"	}												\n" +
		"}													\n",
		// completeBehind:
		"int.cl",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int.cl>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnClassLiteralAccess:int.cl>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"cl",
		// expectedReplacedSource:
		"int.class",
		// test name
		"<complete on primitive type member with non empty identifier>"
	);
}
}
