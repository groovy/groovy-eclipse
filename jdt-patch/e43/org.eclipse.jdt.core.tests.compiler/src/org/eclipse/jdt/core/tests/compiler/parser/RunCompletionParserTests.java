/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RunCompletionParserTests extends junit.framework.TestCase {

	public final static List TEST_CLASSES = new ArrayList();
	public final static List TEST_CLASSES_1_5 = new ArrayList();
	static {
		TEST_CLASSES.add(AllocationExpressionCompletionTest.class);
		TEST_CLASSES.add(ClassLiteralAccessCompletionTest.class);
		TEST_CLASSES.add(CompletionParserTest.class);
		TEST_CLASSES.add(CompletionParserTest2.class);
		TEST_CLASSES.add(CompletionParserTestKeyword.class);
		TEST_CLASSES.add(CompletionRecoveryTest.class);
		TEST_CLASSES.add(DietCompletionTest.class);
		TEST_CLASSES.add(ExplicitConstructorInvocationCompletionTest.class);
		TEST_CLASSES.add(FieldAccessCompletionTest.class);
		TEST_CLASSES.add(InnerTypeCompletionTest.class);
		TEST_CLASSES.add(JavadocCompletionParserTest.class);
		TEST_CLASSES.add(LabelStatementCompletionTest.class);
		TEST_CLASSES.add(MethodInvocationCompletionTest.class);
		TEST_CLASSES.add(NameReferenceCompletionTest.class);
		TEST_CLASSES.add(ReferenceTypeCompletionTest.class);

		TEST_CLASSES_1_5.add(GenericsCompletionParserTest.class);
		TEST_CLASSES_1_5.add(EnumCompletionParserTest.class);
		TEST_CLASSES_1_5.add(AnnotationCompletionParserTest.class);
	}

	public RunCompletionParserTests(String name) {
		super(name);
	}

	public static Test suite() {
		ArrayList testClasses = new ArrayList();

		testClasses.addAll(RunCompletionParserTests.TEST_CLASSES);

		TestSuite all = new TestSuite(TestAll.class.getName());
		int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((possibleComplianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			ArrayList tests_1_3 = (ArrayList)testClasses.clone();
			// Reset forgotten subsets tests
			TestCase.TESTS_PREFIX = null;
			TestCase.TESTS_NAMES = null;
			TestCase.TESTS_NUMBERS= null;
			TestCase.TESTS_RANGE = null;
			TestCase.RUN_ONLY_ID = null;
			all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_3, tests_1_3));
		}
		if ((possibleComplianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			ArrayList tests_1_4 = (ArrayList)testClasses.clone();
			// Reset forgotten subsets tests
			TestCase.TESTS_PREFIX = null;
			TestCase.TESTS_NAMES = null;
			TestCase.TESTS_NUMBERS= null;
			TestCase.TESTS_RANGE = null;
			TestCase.RUN_ONLY_ID = null;
			all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_4, tests_1_4));
		}
		if ((possibleComplianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			ArrayList tests_1_5 = (ArrayList)testClasses.clone();
			tests_1_5.addAll(TEST_CLASSES_1_5);
			// Reset forgotten subsets tests
			TestCase.TESTS_PREFIX = null;
			TestCase.TESTS_NAMES = null;
			TestCase.TESTS_NUMBERS= null;
			TestCase.TESTS_RANGE = null;
			TestCase.RUN_ONLY_ID = null;
			all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_5, tests_1_5));
		}
		if ((possibleComplianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			ArrayList tests_1_6 = (ArrayList)testClasses.clone();
			tests_1_6.addAll(TEST_CLASSES_1_5);
			// Reset forgotten subsets tests
			TestCase.TESTS_PREFIX = null;
			TestCase.TESTS_NAMES = null;
			TestCase.TESTS_NUMBERS= null;
			TestCase.TESTS_RANGE = null;
			TestCase.RUN_ONLY_ID = null;
			all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_6, tests_1_6));
		}
		if ((possibleComplianceLevels & AbstractCompilerTest.F_1_7) != 0) {
			ArrayList tests_1_7 = (ArrayList)testClasses.clone();
			tests_1_7.addAll(TEST_CLASSES_1_5);
			// Reset forgotten subsets tests
			TestCase.TESTS_PREFIX = null;
			TestCase.TESTS_NAMES = null;
			TestCase.TESTS_NUMBERS= null;
			TestCase.TESTS_RANGE = null;
			TestCase.RUN_ONLY_ID = null;
			all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_7, tests_1_7));
		}

		return all;
	}
}
