/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

/**
 * Run all parser regression tests
 */
public class TestAll extends junit.framework.TestCase {

	public final static List TEST_CLASSES_1_5 = new ArrayList();
	static {
		/* completion tests */
		TEST_CLASSES_1_5.addAll(RunCompletionParserTests.TEST_CLASSES_1_5);
		/* selection tests */
		TEST_CLASSES_1_5.add(GenericsSelectionTest.class);
		TEST_CLASSES_1_5.add(AnnotationSelectionTest.class);
		TEST_CLASSES_1_5.add(EnumSelectionTest.class);
		/* recovery tests */
		TEST_CLASSES_1_5.add(GenericDietRecoveryTest.class);
		TEST_CLASSES_1_5.add(EnumDietRecoveryTest.class);
		TEST_CLASSES_1_5.add(AnnotationDietRecoveryTest.class);
		TEST_CLASSES_1_5.add(StatementRecoveryTest_1_5.class);
	}

public TestAll(String testName) {
	super(testName);
}

public static Test suite() {
	ArrayList testClasses = new ArrayList();

	/* completion tests */
	testClasses.addAll(RunCompletionParserTests.TEST_CLASSES);

	/* selection tests */
	testClasses.add(ExplicitConstructorInvocationSelectionTest.class);
	testClasses.add(SelectionTest.class);
	testClasses.add(SelectionTest2.class);
	testClasses.add(SelectionJavadocTest.class);

	/* recovery tests */
	testClasses.add(DietRecoveryTest.class);
	testClasses.add(StatementRecoveryTest.class);

	/* source element parser tests */
	testClasses.add(SourceElementParserTest.class);

	/* document element parser tests */
	testClasses.add(DocumentElementParserTest.class);

	/* syntax error diagnosis tests */
	testClasses.add(SyntaxErrorTest.class);
	testClasses.add(DualParseSyntaxErrorTest.class);
	testClasses.add(ParserTest.class);
	testClasses.add(ComplianceDiagnoseTest.class);

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
		tests_1_7.add(ParserTest1_7.class);
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
