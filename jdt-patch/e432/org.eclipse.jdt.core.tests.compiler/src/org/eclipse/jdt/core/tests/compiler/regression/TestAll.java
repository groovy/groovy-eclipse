/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
 *     Stephan Herrmann - Contributions for
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 358903 - Filter practically unimportant resource leak warnings
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *								Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *								bug 407191 - [1.8] Binary access support for type annotations
 *       Jesper Steen Moeller - Contributions for:
 *								Bug 406973 - [compiler] Parse MethodParameters attribute
 *								Bug 412153 - [1.8][compiler] Check validity of annotations which may be repeatable
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.ArrayList;

import org.eclipse.jdt.core.tests.compiler.parser.ImplicitlyDeclaredClassesTest;
import org.eclipse.jdt.core.tests.compiler.util.HashtableOfObjectTest;
import org.eclipse.jdt.core.tests.compiler.util.JrtUtilTest;
import org.eclipse.jdt.core.tests.dom.StandAloneASTParserTest;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all compiler regression tests
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
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
	standardTests.add(PublicScannerTest.class);
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
	standardTests.add(NullReferenceTestAsserts.class);
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
	standardTests.add(ResourceLeakTests.class);
	standardTests.add(PackageBindingTest.class);

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
	since_1_5.add(NullAnnotationBatchCompilerTest.class);
	since_1_5.add(ConcurrentBatchCompilerTest.class);
	since_1_5.add(ExternalizeStringLiteralsTest_1_5.class);
	since_1_5.add(Deprecated15Test.class);
	since_1_5.add(InnerEmulationTest_1_5.class);
	since_1_5.add(AssignmentTest_1_5.class);
	since_1_5.add(InnerClass15Test.class);
	since_1_5.add(NullAnnotationTest.class);
	since_1_5.add(XLargeTest2.class);

	// Tests to run when compliance is greater than 1.5
	ArrayList since_1_6 = new ArrayList();
	since_1_6.add(StackMapAttributeTest.class);
	since_1_6.add(Compliance_1_6.class);

	ArrayList since_1_7 = new ArrayList();
	since_1_7.add(AssignmentTest_1_7.class);
	since_1_7.add(BinaryLiteralTest.class);
	since_1_7.add(UnderscoresInLiteralsTest.class);
	since_1_7.add(TryStatement17Test.class);
	since_1_7.add(TryWithResourcesStatementTest.class);
	since_1_7.add(GenericsRegressionTest_1_7.class);
	since_1_7.add(PolymorphicSignatureTest.class);
	since_1_7.add(Compliance_1_7.class);
	since_1_7.add(MethodHandleTest.class);
	since_1_7.add(ResourceLeakAnnotatedTests.class);


	ArrayList since_1_8 = new ArrayList();
	since_1_8.add(NegativeTypeAnnotationTest.class);
	since_1_8.add(NullTypeAnnotationTest.class);
	since_1_8.add(NegativeLambdaExpressionsTest.class);
	since_1_8.add(LambdaExpressionsTest.class);
	since_1_8.add(LambdaRegressionTest.class);
	since_1_8.add(SerializableLambdaTest.class);
	since_1_8.add(OverloadResolutionTest8.class);
	since_1_8.add(JSR335ClassFileTest.class);
	since_1_8.add(ExpressionContextTests.class);
	since_1_8.add(InterfaceMethodsTest.class);
	since_1_8.add(GrammarCoverageTests308.class);
	since_1_8.add(FlowAnalysisTest8.class);
	since_1_8.add(TypeAnnotationTest.class);
	since_1_8.add(JSR308SpecSnippetTests.class);
	since_1_8.add(Deprecated18Test.class);
	since_1_8.add(MethodParametersAttributeTest.class);
	since_1_8.add(ClassFileReaderTest_1_8.class);
	since_1_8.add(RepeatableAnnotationTest.class);
	since_1_8.add(GenericsRegressionTest_1_8.class);
	since_1_8.add(Unicode18Test.class);
	since_1_8.add(LambdaShapeTests.class);
	since_1_8.add(StringConcatTest.class);
	since_1_8.add(UseOfUnderscoreTest.class);
	since_1_8.add(DubiousOutcomeTest.class);

	ArrayList since_9 = new ArrayList();
	since_9.add(Unicode9Test.class);
	since_9.add(ModuleCompilationTests.class);
	since_9.add(GenericsRegressionTest_9.class);
	since_9.add(InterfaceMethodsTest_9.class);
	since_9.add(Deprecated9Test.class);
	since_9.add(ModuleAttributeTests.class);
	since_9.add(AutomaticModuleNamingTest.class);
	since_9.add(UnnamedModuleTest.class);
	since_9.add(NullAnnotationTests9.class);
	since_9.add(AnnotationTest_9.class);
	since_9.add(JavadocTestForModule.class);
	since_9.add(TryStatement9Test.class);

	// add 10 specific test here (check duplicates)
	ArrayList since_10 = new ArrayList();
	since_10.add(JEP286Test.class);
	since_10.add(Unicode10Test.class);

	// add 11 specific test here (check duplicates)
	ArrayList since_11 = new ArrayList();
	 since_11.add(JEP323VarLambdaParamsTest.class);
	 since_11.add(JEP181NestTest.class);
	 since_11.add(BatchCompilerTest2.class);

	// add 12 specific test here (check duplicates)
	 ArrayList since_12 = new ArrayList();
	 since_12.add(Unicode11Test.class);

		// add 13 specific test here (check duplicates)
	 ArrayList since_13 = new ArrayList();
	 since_13.add(Unicode12_1Test.class);

	 // add 14 specific test here (check duplicates)
	 ArrayList since_14 = new ArrayList();
	 since_14.add(SwitchExpressionsYieldTest.class);
	 since_14.add(BatchCompilerTest_14.class);

	 // add 15 specific test here (check duplicates)
	 ArrayList since_15 = new ArrayList();
	 since_15.add(ClassFileReaderTest_17.class);
	 since_15.add(JavadocTest_15.class);
	 since_15.add(Unicode13Test.class);
	 since_15.add(BatchCompilerTest_15.class);
	 since_15.add(TextBlockTest.class);
	 since_15.add(ExternalizeStringLiteralsTest_15.class);

	 // add 16 specific test here (check duplicates)
	 ArrayList since_16 = new ArrayList();
	 since_16.add(LocalEnumTest.class);
	 since_16.add(LocalStaticsTest.class);
	 since_16.add(PreviewFeatureTest.class);
	 since_16.add(ValueBasedAnnotationTests.class);
	 since_16.add(BatchCompilerTest_16.class);
	 since_16.add(PatternMatching16Test.class);
	 since_16.add(RecordsRestrictedClassTest.class);
	 since_16.add(JavadocTestForRecord.class);
	 since_16.add(JavadocTest_16.class);

	 // add 17 specific test here (check duplicates)
	 ArrayList since_17 = new ArrayList();
	 since_17.add(SealedTypesTests.class);
	 since_17.add(InstanceofPrimaryPatternTest.class);
	 since_17.add(BatchCompilerTest_17.class);

	 // add 18 specific test here (check duplicates)
	 ArrayList since_18 = new ArrayList();
	 since_18.add(JavadocTest_18.class);

	 // add 21 specific test here (check duplicates)
	 ArrayList since_21 = new ArrayList();
	 since_21.add(SwitchPatternTest.class);
	 since_21.add(RecordPatternTest.class);
	 since_21.add(RecordPatternProjectTest.class);
	 since_21.add(NullAnnotationTests21.class);
	 since_21.add(BatchCompilerTest_21.class);

	 // add 21 specific test here (check duplicates)
	 ArrayList since_22 = new ArrayList();
//	 since_22.add(SuperAfterStatementsTest.class);
	 since_22.add(UnnamedPatternsAndVariablesTest.class);
	 since_22.add(UseOfUnderscoreWithPreviewTest.class);
	 since_22.add(SuperAfterStatementsTest.class);
	 since_22.add(StringTemplateTest.class);
	 since_22.add(SwitchPatternTest21.class);
	 since_22.add(ImplicitlyDeclaredClassesTest.class);

	 // Build final test suite
	TestSuite all = new TestSuite(TestAll.class.getName());
	all.addTest(new TestSuite(StandAloneASTParserTest.class));
	all.addTest(new TestSuite(HashtableOfObjectTest.class));
	all.addTest(new TestSuite(JrtUtilTest.class));
	int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_3) != 0) {
		ArrayList tests_1_3 = (ArrayList)standardTests.clone();
		tests_1_3.add(Compliance_1_3.class);
		tests_1_3.add(JavadocTest_1_3.class);
		tests_1_3.add(Compliance_CLDC.class);
		TestCase.resetForgottenFilters(tests_1_3);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_3, tests_1_3));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_4) != 0) {
		ArrayList tests_1_4 = (ArrayList)standardTests.clone();
		tests_1_4.addAll(since_1_4);
		tests_1_4.add(Compliance_1_4.class);
		tests_1_4.add(ClassFileReaderTest_1_4.class);
		tests_1_4.add(JavadocTest_1_4.class);
		TestCase.resetForgottenFilters(tests_1_4);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_4, tests_1_4));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_5) != 0) {
		ArrayList tests_1_5 = (ArrayList)standardTests.clone();
		tests_1_5.addAll(since_1_4);
		tests_1_5.addAll(since_1_5);
		TestCase.resetForgottenFilters(tests_1_5);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_5, tests_1_5));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_6) != 0) {
		ArrayList tests_1_6 = (ArrayList)standardTests.clone();
		tests_1_6.addAll(since_1_4);
		tests_1_6.addAll(since_1_5);
		tests_1_6.addAll(since_1_6);
		TestCase.resetForgottenFilters(tests_1_6);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_6, tests_1_6));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_7) != 0) {
		ArrayList tests_1_7 = (ArrayList)standardTests.clone();
		tests_1_7.addAll(since_1_4);
		tests_1_7.addAll(since_1_5);
		tests_1_7.addAll(since_1_6);
		tests_1_7.addAll(since_1_7);
		TestCase.resetForgottenFilters(tests_1_7);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_7, tests_1_7));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_8) != 0) {
		ArrayList tests_1_8 = (ArrayList)standardTests.clone();
		tests_1_8.addAll(since_1_4);
		tests_1_8.addAll(since_1_5);
		tests_1_8.addAll(since_1_6);
		tests_1_8.addAll(since_1_7);
		tests_1_8.addAll(since_1_8);
		TestCase.resetForgottenFilters(tests_1_8);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_8, tests_1_8));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_9) != 0) {
		ArrayList tests_9 = (ArrayList)standardTests.clone();
		tests_9.addAll(since_1_4);
		tests_9.addAll(since_1_5);
		tests_9.addAll(since_1_6);
		tests_9.addAll(since_1_7);
		tests_9.addAll(since_1_8);
		tests_9.addAll(since_9);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK9, tests_9));
	}

	if ((possibleComplianceLevels & AbstractCompilerTest.F_10) != 0) {
		ArrayList tests_10 = (ArrayList)standardTests.clone();
		tests_10.addAll(since_1_4);
		tests_10.addAll(since_1_5);
		tests_10.addAll(since_1_6);
		tests_10.addAll(since_1_7);
		tests_10.addAll(since_1_8);
		tests_10.addAll(since_9);
		tests_10.addAll(since_10);
		TestCase.resetForgottenFilters(tests_10);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK10, tests_10));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_11) != 0) {
		ArrayList tests_11 = (ArrayList)standardTests.clone();
		tests_11.addAll(since_1_4);
		tests_11.addAll(since_1_5);
		tests_11.addAll(since_1_6);
		tests_11.addAll(since_1_7);
		tests_11.addAll(since_1_8);
		tests_11.addAll(since_9);
		tests_11.addAll(since_10);
		tests_11.addAll(since_11);
		TestCase.resetForgottenFilters(tests_11);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_11), tests_11));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_12) != 0) {
		ArrayList tests_12 = (ArrayList)standardTests.clone();
		tests_12.addAll(since_1_4);
		tests_12.addAll(since_1_5);
		tests_12.addAll(since_1_6);
		tests_12.addAll(since_1_7);
		tests_12.addAll(since_1_8);
		tests_12.addAll(since_9);
		tests_12.addAll(since_10);
		tests_12.addAll(since_11);
		tests_12.addAll(since_12);
		TestCase.resetForgottenFilters(tests_12);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_12), tests_12));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_13) != 0) {
		ArrayList tests_13 = (ArrayList)standardTests.clone();
		tests_13.addAll(since_1_4);
		tests_13.addAll(since_1_5);
		tests_13.addAll(since_1_6);
		tests_13.addAll(since_1_7);
		tests_13.addAll(since_1_8);
		tests_13.addAll(since_9);
		tests_13.addAll(since_10);
		tests_13.addAll(since_11);
		tests_13.addAll(since_12);
		tests_13.addAll(since_13);
		TestCase.resetForgottenFilters(tests_13);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_13), tests_13));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_14) != 0) {
		ArrayList tests_14 = (ArrayList)standardTests.clone();
		tests_14.addAll(since_1_4);
		tests_14.addAll(since_1_5);
		tests_14.addAll(since_1_6);
		tests_14.addAll(since_1_7);
		tests_14.addAll(since_1_8);
		tests_14.addAll(since_9);
		tests_14.addAll(since_10);
		tests_14.addAll(since_11);
		tests_14.addAll(since_12);
		tests_14.addAll(since_13);
		tests_14.addAll(since_14);
		TestCase.resetForgottenFilters(tests_14);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_14), tests_14));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_15) != 0) {
		ArrayList tests_15 = (ArrayList)standardTests.clone();
		tests_15.addAll(since_1_4);
		tests_15.addAll(since_1_5);
		tests_15.addAll(since_1_6);
		tests_15.addAll(since_1_7);
		tests_15.addAll(since_1_8);
		tests_15.addAll(since_9);
		tests_15.addAll(since_10);
		tests_15.addAll(since_11);
		tests_15.addAll(since_12);
		tests_15.addAll(since_13);
		tests_15.addAll(since_14);
		tests_15.addAll(since_15);
		TestCase.resetForgottenFilters(tests_15);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_15), tests_15));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_16) != 0) {
		ArrayList tests_16 = (ArrayList)standardTests.clone();
		tests_16.addAll(since_1_4);
		tests_16.addAll(since_1_5);
		tests_16.addAll(since_1_6);
		tests_16.addAll(since_1_7);
		tests_16.addAll(since_1_8);
		tests_16.addAll(since_9);
		tests_16.addAll(since_10);
		tests_16.addAll(since_11);
		tests_16.addAll(since_12);
		tests_16.addAll(since_13);
		tests_16.addAll(since_14);
		tests_16.addAll(since_15);
		tests_16.addAll(since_16);
		TestCase.resetForgottenFilters(tests_16);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_16), tests_16));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_17) != 0) {
		ArrayList tests_17 = (ArrayList)standardTests.clone();
		tests_17.addAll(since_1_4);
		tests_17.addAll(since_1_5);
		tests_17.addAll(since_1_6);
		tests_17.addAll(since_1_7);
		tests_17.addAll(since_1_8);
		tests_17.addAll(since_9);
		tests_17.addAll(since_10);
		tests_17.addAll(since_11);
		tests_17.addAll(since_12);
		tests_17.addAll(since_13);
		tests_17.addAll(since_14);
		tests_17.addAll(since_15);
		tests_17.addAll(since_16);
		tests_17.addAll(since_17);
		TestCase.resetForgottenFilters(tests_17);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_17), tests_17));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_18) != 0) {
		ArrayList tests_18 = (ArrayList)standardTests.clone();
		tests_18.addAll(since_1_4);
		tests_18.addAll(since_1_5);
		tests_18.addAll(since_1_6);
		tests_18.addAll(since_1_7);
		tests_18.addAll(since_1_8);
		tests_18.addAll(since_9);
		tests_18.addAll(since_10);
		tests_18.addAll(since_11);
		tests_18.addAll(since_12);
		tests_18.addAll(since_13);
		tests_18.addAll(since_14);
		tests_18.addAll(since_15);
		tests_18.addAll(since_16);
		tests_18.addAll(since_17);
		tests_18.addAll(since_18);
		TestCase.resetForgottenFilters(tests_18);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_18), tests_18));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_19) != 0) {
		ArrayList tests_19 = (ArrayList)standardTests.clone();
		tests_19.addAll(since_1_4);
		tests_19.addAll(since_1_5);
		tests_19.addAll(since_1_6);
		tests_19.addAll(since_1_7);
		tests_19.addAll(since_1_8);
		tests_19.addAll(since_9);
		tests_19.addAll(since_10);
		tests_19.addAll(since_11);
		tests_19.addAll(since_12);
		tests_19.addAll(since_13);
		tests_19.addAll(since_14);
		tests_19.addAll(since_15);
		tests_19.addAll(since_16);
		tests_19.addAll(since_17);
		tests_19.addAll(since_18);
		TestCase.resetForgottenFilters(tests_19);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_19), tests_19));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_20) != 0) {
		ArrayList tests_20 = (ArrayList)standardTests.clone();
		tests_20.addAll(since_1_4);
		tests_20.addAll(since_1_5);
		tests_20.addAll(since_1_6);
		tests_20.addAll(since_1_7);
		tests_20.addAll(since_1_8);
		tests_20.addAll(since_9);
		tests_20.addAll(since_10);
		tests_20.addAll(since_11);
		tests_20.addAll(since_12);
		tests_20.addAll(since_13);
		tests_20.addAll(since_14);
		tests_20.addAll(since_15);
		tests_20.addAll(since_16);
		tests_20.addAll(since_17);
		tests_20.addAll(since_18);
		TestCase.resetForgottenFilters(tests_20);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_20), tests_20));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_21) != 0) {
		ArrayList tests_21 = (ArrayList)standardTests.clone();
		tests_21.addAll(since_1_4);
		tests_21.addAll(since_1_5);
		tests_21.addAll(since_1_6);
		tests_21.addAll(since_1_7);
		tests_21.addAll(since_1_8);
		tests_21.addAll(since_9);
		tests_21.addAll(since_10);
		tests_21.addAll(since_11);
		tests_21.addAll(since_12);
		tests_21.addAll(since_13);
		tests_21.addAll(since_14);
		tests_21.addAll(since_15);
		tests_21.addAll(since_16);
		tests_21.addAll(since_17);
		tests_21.addAll(since_18);
		tests_21.addAll(since_21);
		TestCase.resetForgottenFilters(tests_21);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(
				ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_21), tests_21));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_22) != 0) {
		ArrayList tests_22 = (ArrayList)standardTests.clone();
		tests_22.addAll(since_1_4);
		tests_22.addAll(since_1_5);
		tests_22.addAll(since_1_6);
		tests_22.addAll(since_1_7);
		tests_22.addAll(since_1_8);
		tests_22.addAll(since_9);
		tests_22.addAll(since_10);
		tests_22.addAll(since_11);
		tests_22.addAll(since_12);
		tests_22.addAll(since_13);
		tests_22.addAll(since_14);
		tests_22.addAll(since_15);
		tests_22.addAll(since_16);
		tests_22.addAll(since_17);
		tests_22.addAll(since_18);
		tests_22.addAll(since_21);
		tests_22.addAll(since_22);
		TestCase.resetForgottenFilters(tests_22);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(
				ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_22), tests_22));
	}
	all.addTest(new TestSuite(Jsr14Test.class));
	return all;
}
}
