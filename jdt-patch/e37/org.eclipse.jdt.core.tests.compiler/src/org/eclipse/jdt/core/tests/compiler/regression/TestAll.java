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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.dom.StandAloneASTParserTest;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;

/**
 * Run all compiler regression tests
 */
public class TestAll extends junit.framework.TestCase {

public TestAll(String testName) {
	super(testName);
}
public static Test suite() {

	// Common test suites
	ArrayList standardTests = new ArrayList();
	standardTests.add(ArrayTest.class);
	standardTests.add(AssignmentTest.class);
	standardTests.add(BooleanTest.class);
	standardTests.add(CastTest.class);
	standardTests.add(ClassFileComparatorTest.class);
	standardTests.add(CollisionCase.class);
	standardTests.add(ConstantTest.class);
	standardTests.add(DeprecatedTest.class);
	standardTests.add(LocalVariableTest.class);
	standardTests.add(LookupTest.class);
	standardTests.add(NumericTest.class);
	standardTests.add(ProblemConstructorTest.class);
	standardTests.add(ProblemTypeAndMethodTest.class);
	standardTests.add(ScannerTest.class);
	standardTests.add(SwitchTest.class);
	standardTests.add(TryStatementTest.class);
	standardTests.add(UtilTest.class);
	standardTests.add(XLargeTest.class);
	standardTests.add(InternalScannerTest.class);
	standardTests.add(ConditionalExpressionTest.class);
	standardTests.add(ExternalizeStringLiteralsTest.class);
	standardTests.add(NonFatalErrorTest.class);
	standardTests.add(FlowAnalysisTest.class);
	standardTests.add(CharOperationTest.class);
	standardTests.add(RuntimeTests.class);
	standardTests.add(DebugAttributeTest.class);
	standardTests.add(NullReferenceTest.class);
	if (UnconditionalFlowInfo.COVERAGE_TEST_FLAG) {
		standardTests.add(NullReferenceImplTests.class);
	}
	standardTests.add(CompilerInvocationTests.class);
	standardTests.add(InnerEmulationTest.class);
	standardTests.add(SuperTypeTest.class);
	standardTests.add(ForStatementTest.class);
	standardTests.add(FieldAccessTest.class);
	standardTests.add(SerialVersionUIDTests.class);
	standardTests.add(LineNumberAttributeTest.class);
	standardTests.add(ProgrammingProblemsTest.class);
	standardTests.add(ManifestAnalyzerTest.class);
	standardTests.add(InitializationTests.class);

	// add all javadoc tests
	for (int i=0, l=JavadocTest.ALL_CLASSES.size(); i<l; i++) {
		standardTests.add(JavadocTest.ALL_CLASSES.get(i));
	}

	// Tests to run when compliance is greater than 1.3
	ArrayList since_1_4 = new ArrayList();
	since_1_4.add(AssertionTest.class);

	// Tests to run when compliance is greater than 1.4
	ArrayList since_1_5 = new ArrayList();
	since_1_5.addAll(RunComparableTests.ALL_CLASSES);
	since_1_5.add(ClassFileReaderTest_1_5.class);
	since_1_5.add(GenericTypeSignatureTest.class);
	since_1_5.add(InternalHexFloatTest.class);
	since_1_5.add(JavadocTest_1_5.class);
	since_1_5.add(BatchCompilerTest.class);
	since_1_5.add(ExternalizeStringLiterals15Test.class);
	since_1_5.add(Deprecated15Test.class);
	since_1_5.add(InnerEmulationTest_1_5.class);
	since_1_5.add(AssignmentTest_1_5.class);
	since_1_5.add(InnerClass15Test.class);

	// Tests to run when compliance is greater than 1.5
	ArrayList since_1_6 = new ArrayList();
	since_1_6.add(StackMapAttributeTest.class);
	since_1_6.add(Compliance_1_6.class);

	// Build final test suite
	TestSuite all = new TestSuite(TestAll.class.getName());
	all.addTest(new TestSuite(StandAloneASTParserTest.class));
	int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_3) != 0) {
		ArrayList tests_1_3 = (ArrayList)standardTests.clone();
		tests_1_3.add(Compliance_1_3.class);
		tests_1_3.add(JavadocTest_1_3.class);
		tests_1_3.add(Compliance_CLDC.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_3, tests_1_3));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_4) != 0) {
		ArrayList tests_1_4 = (ArrayList)standardTests.clone();
		tests_1_4.addAll(since_1_4);
		tests_1_4.add(Compliance_1_4.class);
		tests_1_4.add(ClassFileReaderTest_1_4.class);
		tests_1_4.add(JavadocTest_1_4.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_4, tests_1_4));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_5) != 0) {
		ArrayList tests_1_5 = (ArrayList)standardTests.clone();
		tests_1_5.addAll(since_1_4);
		tests_1_5.addAll(since_1_5);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_5, tests_1_5));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_6) != 0) {
		ArrayList tests_1_6 = (ArrayList)standardTests.clone();
		tests_1_6.addAll(since_1_4);
		tests_1_6.addAll(since_1_5);
		tests_1_6.addAll(since_1_6);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_6, tests_1_6));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_7) != 0) {
		ArrayList tests_1_7 = (ArrayList)standardTests.clone();
		tests_1_7.addAll(since_1_4);
		tests_1_7.addAll(since_1_5);
		tests_1_7.addAll(since_1_6);
		tests_1_7.add(AssignmentTest_1_7.class);
		tests_1_7.add(BinaryLiteralTest.class);
		tests_1_7.add(UnderscoresInLiteralsTest.class);
		tests_1_7.add(TryStatement17Test.class);
		tests_1_7.add(TryWithResourcesStatementTest.class);
		tests_1_7.add(GenericsRegressionTest_1_7.class);
		tests_1_7.add(PolymorphicSignatureTest.class);
		tests_1_7.add(Compliance_1_7.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_7, tests_1_7));
	}
	all.addTest(new TestSuite(Jsr14Test.class));
	return all;
}
}
