/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *      Jesper Steen Møller <jesper@selskabet.org> - Contributions for
 *			bug 527554 - [18.3] Compiler support for JEP 286 Local-Variable Type
 *
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.tests.compiler.regression.JEP286ReservedWordTest;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all parser regression tests
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
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

public static TestSuite getTestSuite(boolean addComplianceDiagnoseTest) {
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
	if (addComplianceDiagnoseTest)
		testClasses.add(ComplianceDiagnoseTest.class);

	TestSuite all = new TestSuite(TestAll.class.getName());
	int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_3) != 0) {
		ArrayList tests_1_3 = (ArrayList)testClasses.clone();
		TestCase.resetForgottenFilters(tests_1_3);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_3, tests_1_3));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_4) != 0) {
		ArrayList tests_1_4 = (ArrayList)testClasses.clone();
		TestCase.resetForgottenFilters(tests_1_4);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_4, tests_1_4));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_5) != 0) {
		ArrayList tests_1_5 = (ArrayList)testClasses.clone();
		tests_1_5.addAll(TEST_CLASSES_1_5);
		TestCase.resetForgottenFilters(tests_1_5);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_5, tests_1_5));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_6) != 0) {
		ArrayList tests_1_6 = (ArrayList)testClasses.clone();
		tests_1_6.addAll(TEST_CLASSES_1_5);
		TestCase.resetForgottenFilters(tests_1_6);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_6, tests_1_6));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_7) != 0) {
		ArrayList tests_1_7 = (ArrayList)testClasses.clone();
		tests_1_7.addAll(TEST_CLASSES_1_5);
		tests_1_7.add(ParserTest1_7.class);
		TestCase.resetForgottenFilters(tests_1_7);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_7, tests_1_7));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_8) != 0) {
		ArrayList tests_1_8 = (ArrayList)testClasses.clone();
		tests_1_8.addAll(TEST_CLASSES_1_5);
		tests_1_8.add(ParserTest1_7.class);
		tests_1_8.add(LambdaExpressionSyntaxTest.class);
		tests_1_8.add(ReferenceExpressionSyntaxTest.class);
		tests_1_8.add(TypeAnnotationSyntaxTest.class);
		tests_1_8.add(CompletionParserTest18.class);
		tests_1_8.add(SelectionParserTest18.class);
		TestCase.resetForgottenFilters(tests_1_8);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_8, tests_1_8));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_9) != 0) {
		ArrayList tests_9 = (ArrayList)testClasses.clone();
		tests_9.addAll(TEST_CLASSES_1_5);
		tests_9.add(ParserTest1_7.class);
		tests_9.add(LambdaExpressionSyntaxTest.class);
		tests_9.add(ReferenceExpressionSyntaxTest.class);
		tests_9.add(TypeAnnotationSyntaxTest.class);
		tests_9.add(CompletionParserTest18.class);
		tests_9.add(SelectionParserTest18.class);
		tests_9.add(SelectionParserTest9.class);
		tests_9.add(ModuleDeclarationSyntaxTest.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK9, tests_9));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_10) != 0) {
		ArrayList tests_10 = (ArrayList)testClasses.clone();
		tests_10.addAll(TEST_CLASSES_1_5);
		tests_10.add(ParserTest1_7.class);
		tests_10.add(LambdaExpressionSyntaxTest.class);
		tests_10.add(ReferenceExpressionSyntaxTest.class);
		tests_10.add(TypeAnnotationSyntaxTest.class);
		tests_10.add(CompletionParserTest18.class);
		tests_10.add(SelectionParserTest18.class);
		tests_10.add(SelectionParserTest9.class);
		tests_10.add(SelectionParserTest10.class);
		tests_10.add(ModuleDeclarationSyntaxTest.class);
		tests_10.add(JEP286ReservedWordTest.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK10, tests_10));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_11) != 0) {
		ArrayList tests_11 = (ArrayList)testClasses.clone();
		tests_11.addAll(TEST_CLASSES_1_5);
		tests_11.add(ParserTest1_7.class);
		tests_11.add(LambdaExpressionSyntaxTest.class);
		tests_11.add(ReferenceExpressionSyntaxTest.class);
		tests_11.add(TypeAnnotationSyntaxTest.class);
		tests_11.add(CompletionParserTest18.class);
		tests_11.add(SelectionParserTest18.class);
		tests_11.add(SelectionParserTest9.class);
		tests_11.add(SelectionParserTest10.class);
		tests_11.add(ModuleDeclarationSyntaxTest.class);
		tests_11.add(JEP286ReservedWordTest.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_11), tests_11));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_12) != 0) {
		ArrayList tests_12 = (ArrayList)testClasses.clone();
		tests_12.addAll(TEST_CLASSES_1_5);
		tests_12.add(ParserTest1_7.class);
		tests_12.add(LambdaExpressionSyntaxTest.class);
		tests_12.add(ReferenceExpressionSyntaxTest.class);
		tests_12.add(TypeAnnotationSyntaxTest.class);
		tests_12.add(CompletionParserTest18.class);
		tests_12.add(SelectionParserTest18.class);
		tests_12.add(SelectionParserTest9.class);
		tests_12.add(SelectionParserTest10.class);
		tests_12.add(SelectionParserTest12.class);
		tests_12.add(ModuleDeclarationSyntaxTest.class);
		tests_12.add(JEP286ReservedWordTest.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_12), tests_12));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_13) != 0) {
		ArrayList tests_13 = (ArrayList)testClasses.clone();
		tests_13.addAll(TEST_CLASSES_1_5);
		tests_13.add(ParserTest1_7.class);
		tests_13.add(LambdaExpressionSyntaxTest.class);
		tests_13.add(ReferenceExpressionSyntaxTest.class);
		tests_13.add(TypeAnnotationSyntaxTest.class);
		tests_13.add(CompletionParserTest18.class);
		tests_13.add(SelectionParserTest18.class);
		tests_13.add(SelectionParserTest9.class);
		tests_13.add(SelectionParserTest10.class);
		tests_13.add(SelectionParserTest12.class);
		tests_13.add(ModuleDeclarationSyntaxTest.class);
		tests_13.add(JEP286ReservedWordTest.class);
		//TODO:To add SwitchExpressionYieldTests here as well as master
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_13), tests_13));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_14) != 0) {
		ArrayList tests_14 = (ArrayList)testClasses.clone();
		tests_14.addAll(TEST_CLASSES_1_5);
		tests_14.add(ParserTest1_7.class);
		tests_14.add(LambdaExpressionSyntaxTest.class);
		tests_14.add(ReferenceExpressionSyntaxTest.class);
		tests_14.add(TypeAnnotationSyntaxTest.class);
		tests_14.add(CompletionParserTest18.class);
		tests_14.add(SelectionParserTest18.class);
		tests_14.add(SelectionParserTest9.class);
		tests_14.add(SelectionParserTest10.class);
		tests_14.add(SelectionParserTest12.class);
		tests_14.add(ModuleDeclarationSyntaxTest.class);
		tests_14.add(JEP286ReservedWordTest.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_14), tests_14));
	}
	return all;
}
public static Test suite() {
	return getTestSuite(true);
}
}
