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
import java.util.Arrays;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.compiler.unicode.*;
import org.eclipse.jdt.core.tests.compiler.util.HashtableOfObjectTest;
import org.eclipse.jdt.core.tests.compiler.util.JrtUtilTest;
import org.eclipse.jdt.core.tests.dom.StandAloneASTParserTest;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;

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
	standardTests.add(NameEnvironmentAnswerListenerTest.class);
	standardTests.add(XtextDependencies.class);

	// 1.5 - 1.7 (always enabled):
	standardTests.add(AssertionTest.class);
	standardTests.addAll(RunComparableTests.ALL_CLASSES);
	standardTests.add(ClassFileReaderTest_1_5.class);
	standardTests.add(GenericTypeSignatureTest.class);
	standardTests.add(InternalHexFloatTest.class);
	standardTests.add(JavadocTest_1_5.class);
	standardTests.add(BatchCompilerTest.class);
	standardTests.add(NullAnnotationBatchCompilerTest.class);
	standardTests.add(ConcurrentBatchCompilerTest.class);
	standardTests.add(ExternalizeStringLiteralsTest_1_5.class);
	standardTests.add(Deprecated15Test.class);
	standardTests.add(InnerEmulationTest_1_5.class);
	standardTests.add(AssignmentTest_1_5.class);
	standardTests.add(InnerClass15Test.class);
	standardTests.add(NullAnnotationTest.class);
	standardTests.add(XLargeTest2.class);
	standardTests.add(StackMapAttributeTest.class);
	standardTests.add(Compliance_1_6.class);
	standardTests.add(AssignmentTest_1_7.class);
	standardTests.add(BinaryLiteralTest.class);
	standardTests.add(UnderscoresInLiteralsTest.class);
	standardTests.add(TryStatement17Test.class);
	standardTests.add(TryWithResourcesStatementTest.class);
	standardTests.add(GenericsRegressionTest_1_7.class);
	standardTests.add(PolymorphicSignatureTest.class);
	standardTests.add(Compliance_1_7.class);
	standardTests.add(MethodHandleTest.class);
	standardTests.add(ResourceLeakAnnotatedTests.class);
	// 1.8 (always enabled):
	standardTests.add(NegativeTypeAnnotationTest.class);
	standardTests.add(NullTypeAnnotationTest.class);
	standardTests.add(NegativeLambdaExpressionsTest.class);
	standardTests.add(LambdaExpressionsTest.class);
	standardTests.add(LambdaRegressionTest.class);
	standardTests.add(SerializableLambdaTest.class);
	standardTests.add(OverloadResolutionTest8.class);
	standardTests.add(JSR335ClassFileTest.class);
	standardTests.add(ExpressionContextTests.class);
	standardTests.add(InterfaceMethodsTest.class);
	standardTests.add(GrammarCoverageTests308.class);
	standardTests.add(FlowAnalysisTest8.class);
	standardTests.add(TypeAnnotationTest.class);
	standardTests.add(JSR308SpecSnippetTests.class);
	standardTests.add(Deprecated18Test.class);
	standardTests.add(MethodParametersAttributeTest.class);
	standardTests.add(ClassFileReaderTest_1_8.class);
	standardTests.add(RepeatableAnnotationTest.class);
	standardTests.add(GenericsRegressionTest_1_8.class);
	standardTests.add(Unicode1_8Test.class);
	standardTests.add(LambdaShapeTests.class);
	standardTests.add(StringConcatTest.class);
	standardTests.add(UseOfUnderscoreTest.class);
	standardTests.add(DubiousOutcomeTest.class);

	// add all javadoc tests
	for (int i=0, l=JavadocTest.ALL_CLASSES.size(); i<l; i++) {
		standardTests.add(JavadocTest.ALL_CLASSES.get(i));
	}

	// specific to ancient versions that are no longer supported:
	ArrayList legacyTests = new ArrayList();
	legacyTests.add(Compliance_1_3.class);
	legacyTests.add(JavadocTest_1_3.class);
	legacyTests.add(Compliance_CLDC.class);
	legacyTests.add(Compliance_1_4.class);
	legacyTests.add(ClassFileReaderTest_1_4.class);
	legacyTests.add(JavadocTest_1_4.class);

	Class<?>[][] sinceTests = {
		{ // 9
			Unicode9Test.class,
			ModuleCompilationTests.class,
			GenericsRegressionTest_9.class,
			InterfaceMethodsTest_9.class,
			Deprecated9Test.class,
			ModuleAttributeTests.class,
			AutomaticModuleNamingTest.class,
			UnnamedModuleTest.class,
			NullAnnotationTests9.class,
			AnnotationTest_9.class,
			JavadocTestForModule.class,
			TryStatement9Test.class,
		},
		{ // 10
			JEP286Test.class,
			Unicode10Test.class
		},
		{ // 11
			JEP323VarLambdaParamsTest.class,
			JEP181NestTest.class,
			BatchCompilerTest2.class,
		},
		{ // 12
			Unicode11Test.class,
		},
		{ // 13
			Unicode12_1Test.class,
		},
		{ // 14
			SwitchExpressionsYieldTest.class,
			BatchCompilerTest_14.class,
		},
		{ // 15
			ClassFileReaderTest_17.class,
			JavadocTest_15.class,
			Unicode13Test.class,
			BatchCompilerTest_15.class,
			TextBlockTest.class,
			ExternalizeStringLiteralsTest_15.class,
		},
		{ // 16
			LocalEnumTest.class,
			LocalStaticsTest.class,
			PreviewFeatureTest.class,
			ValueBasedAnnotationTests.class,
			BatchCompilerTest_16.class,
			PatternMatching16Test.class,
			RecordsRestrictedClassTest.class,
			JavadocTestForRecord.class,
			JavadocTest_16.class,
		},
		{ // 17
			SealedTypesTests.class,
			InstanceofPrimaryPatternTest.class,
			BatchCompilerTest_17.class,
		},
		{ // 18
			JavadocTest_18.class,
		},
		{ // 19
			Unicode14Test.class,
		},
		{ // 20
			Unicode15Test.class,
		},
		{ // 21
			SwitchPatternTest.class,
			RecordPatternTest.class,
			RecordPatternProjectTest.class,
			NullAnnotationTests21.class,
			BatchCompilerTest_21.class,
			JEP441SnippetsTest.class,
		},
		{ // 22
			UnnamedPatternsAndVariablesTest.class,
			UseOfUnderscoreJava22Test.class,
			SwitchPatternTest22.class,
			Unicode15_1Test.class,
		},
		{ // 23
			MarkdownCommentsTest.class,
		},
		{ // 24
			Unicode16Test.class,
		},
		{ // 25
			ModuleImportTests.class,
			SuperAfterStatementsTest.class,
			ImplicitlyDeclaredClassesTest.class,
		},
		{ // 26
			PreviewFlagTest.class,
			PrimitiveInPatternsTest.class,
			PrimitiveInPatternsTestSH.class,
			Unicode17Test.class,
		}
	};
	assert sinceTests.length == AbstractCompilerTest.NUM_VERSIONS - 1 : "sinceTests should be aligned with NUM_VERSIONS";
	// Build final test suite
	TestSuite all = new TestSuite(TestAll.class.getName());
	all.addTest(new TestSuite(StandAloneASTParserTest.class));
	all.addTest(new TestSuite(HashtableOfObjectTest.class));
	all.addTest(new TestSuite(JrtUtilTest.class));

	int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();

	for (int v=0; v < AbstractCompilerTest.NUM_VERSIONS; v++) {
		int level = AbstractCompilerTest.F_1_8 << v;
		if ((possibleComplianceLevels & level) != 0) {
			ArrayList complianceTests = new ArrayList(standardTests);
			if (level == AbstractCompilerTest.F_1_8) {
				// invoke these only when 1.8 has been explicitly requested:
				TestCase.resetForgottenFilters(legacyTests);
				complianceTests.addAll(legacyTests);
			}
			for (int j=0; j<v; j++) {
				List<Class<?>> jList = Arrays.asList(sinceTests[j]);
				complianceTests.addAll(jList);
			}
			TestCase.resetForgottenFilters(complianceTests);
			long complianceLevel = ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_1_8+v);
			all.addTest(AbstractCompilerTest.buildComplianceTestSuite(complianceLevel, complianceTests));
		}
	}

	all.addTest(new TestSuite(Jsr14Test.class));
	return all;
}
}
