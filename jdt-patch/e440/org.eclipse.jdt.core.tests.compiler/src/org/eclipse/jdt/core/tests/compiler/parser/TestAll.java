/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
import java.util.function.Consumer;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.compiler.regression.JEP286ReservedWordTest;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

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
	testClasses.add(StringLiteralTest.class);
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

	record TestsAddition(long complianceTestLevel, long jdkVersion, Consumer<ArrayList> action) {}
	List<TestsAddition> testAdditionsList = new ArrayList<>();

	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_1_8, ClassFileConstants.JDK1_8, (list) -> {addJava1_8Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_9, ClassFileConstants.JDK9, (list) -> {addJava9Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_10, ClassFileConstants.JDK10, (list) -> {addJava10Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_11, ClassFileConstants.JDK11, (list) -> {addJava10Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_12, ClassFileConstants.JDK12, (list) -> {addJava12Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_13, ClassFileConstants.JDK13, (list) -> {addJava12Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_14, ClassFileConstants.JDK14, (list) -> {addJava12Tests(list);addJava14Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_15, ClassFileConstants.JDK15, (list) -> {addJava12Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_16, ClassFileConstants.JDK16, (list) -> {addJava16Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_17, ClassFileConstants.JDK17, (list) -> {addJava16Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_18, ClassFileConstants.JDK18, (list) -> {addJava16Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_19, ClassFileConstants.JDK19, (list) -> {addJava16Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_21, ClassFileConstants.JDK21, (list) -> {addJava16Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_22, ClassFileConstants.JDK22, (list) -> {addJava16Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_23, ClassFileConstants.JDK23, (list) -> {addJava16Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_24, ClassFileConstants.JDK24, (list) -> {addJava16Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_25, ClassFileConstants.JDK25, (list) -> {addJava16Tests(list);}));
	testAdditionsList.add(new TestsAddition(AbstractCompilerTest.F_26, ClassFileConstants.JDK26, (list) -> {addJava16Tests(list);}));

	for (TestsAddition testVersionMap : testAdditionsList) {
		if ((possibleComplianceLevels & testVersionMap.complianceTestLevel) != 0) {
			ArrayList tests_1_8 = (ArrayList)testClasses.clone();
			tests_1_8.addAll(TEST_CLASSES_1_5);
			testVersionMap.action.accept(tests_1_8);
			resetForgottenFilters();
			all.addTest(AbstractCompilerTest.buildComplianceTestSuite(testVersionMap.jdkVersion, tests_1_8));
		}
	}
	return all;
}

private static void resetForgottenFilters() {
	// Reset forgotten subsets tests
	TestCase.TESTS_PREFIX = null;
	TestCase.TESTS_NAMES = null;
	TestCase.TESTS_NUMBERS= null;
	TestCase.TESTS_RANGE = null;
	TestCase.RUN_ONLY_ID = null;
}

private static void addJava16Tests(ArrayList tests_16) {
	addJava1_8Tests(tests_16);
	tests_16.add(PatternMatchingSelectionTest.class);
}
private static void addJava14Tests(ArrayList tests_14) {
	tests_14.add(SelectionParserTest14.class);
}
private static void addJava12Tests(ArrayList tests_12) {
	addJava10Tests(tests_12);
}

private static void addJava10Tests(ArrayList tests_10) {
	addJava9Tests(tests_10);
	tests_10.add(SelectionParserTest10.class);
	tests_10.add(JEP286ReservedWordTest.class);
}

private static void addJava9Tests(ArrayList tests_9) {
	addJava1_8Tests(tests_9);
	tests_9.add(SelectionParserTest9.class);
	tests_9.add(ModuleDeclarationSyntaxTest.class);
}

private static void addJava1_8Tests(ArrayList tests_1_8) {
	tests_1_8.add(ParserTest1_7.class);
	tests_1_8.add(LambdaExpressionSyntaxTest.class);
	tests_1_8.add(ReferenceExpressionSyntaxTest.class);
	tests_1_8.add(TypeAnnotationSyntaxTest.class);
	tests_1_8.add(CompletionParserTest18.class);
	tests_1_8.add(SelectionParserTest18.class);
}
public static Test suite() {
	return getTestSuite(true);
}
}
