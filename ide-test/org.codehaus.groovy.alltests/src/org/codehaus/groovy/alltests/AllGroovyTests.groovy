/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.alltests

import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite) @Suite.SuiteClasses([
    org.codehaus.groovy.alltests.SanityTests,
    org.codehaus.groovy.eclipse.test.ErrorLogTest,

    // org.codehaus.groovy.eclipse.codeassist.tests
    org.codehaus.groovy.eclipse.codeassist.tests.AnnotationCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.CommandChainCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.ConstructorCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.ContentAssistLocationTests,
    org.codehaus.groovy.eclipse.codeassist.tests.ContextInformationTests,
    org.codehaus.groovy.eclipse.codeassist.tests.DefaultGroovyMethodCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.DefaultMethodContentAssistTests,
    org.codehaus.groovy.eclipse.codeassist.tests.ExtendedCompletionContextTests,
    org.codehaus.groovy.eclipse.codeassist.tests.FieldCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.FindImportsRegionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.GenericCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.GroovyLikeCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.GuessingCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.InferencingCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.InnerTypeCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.ImportCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.LocalVariableCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.MethodCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.NewFieldCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.OtherCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.ProposalProviderAndFilterTests,
    org.codehaus.groovy.eclipse.codeassist.tests.RelevanceTests,
    org.codehaus.groovy.eclipse.codeassist.tests.StaticImportsCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.TriggerCharacterCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.TypeCompletionTests,
    org.codehaus.groovy.eclipse.codeassist.tests.TypeCompletionTests2,

    // org.codehaus.groovy.eclipse.codebrowsing.tests
    org.codehaus.groovy.eclipse.codebrowsing.tests.ASTFragmentTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.ASTPositionTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectAttributesTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectCategoriesTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectFieldsTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectGenericsTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectImportsTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectKeywordsTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectMethodsTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectMiscellaneousTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectPackageTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectPropertiesTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectStaticImportsTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectTypesTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.CodeSelectVariablesTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.FindAllOccurrencesVisitorTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.FindSurroundingNodeTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.IsSameExpressionTests,
    org.codehaus.groovy.eclipse.codebrowsing.tests.PartialVisitTests,

    // org.codehaus.groovy.eclipse.core.tests
    org.codehaus.groovy.eclipse.core.test.AstPositionTests,
    org.codehaus.groovy.eclipse.core.test.ClasspathContainerTests,
    org.codehaus.groovy.eclipse.core.test.ErrorRecoveryTests,
    org.codehaus.groovy.eclipse.core.test.SyntheticMemberSearchTests,
    org.codehaus.groovy.eclipse.core.test.UnrecoverableErrorTests,

    // org.codehaus.groovy.eclipse.dsl.tests
    org.codehaus.groovy.eclipse.dsl.tests.BuiltInDSLInferencingTests,
    org.codehaus.groovy.eclipse.dsl.tests.DSLContentAssistTests,
    org.codehaus.groovy.eclipse.dsl.tests.DSLInferencingTests,
    org.codehaus.groovy.eclipse.dsl.tests.DSLNamedArgContentAssistTests,
    org.codehaus.groovy.eclipse.dsl.tests.DSLStoreTests,
    org.codehaus.groovy.eclipse.dsl.tests.MetaDSLInferencingTests,
    org.codehaus.groovy.eclipse.dsl.tests.PointcutCreationTests,
    org.codehaus.groovy.eclipse.dsl.tests.PointcutEvaluationTests,
    org.codehaus.groovy.eclipse.dsl.tests.StringObjectVectorTests,

    // org.codehaus.groovy.eclipse.junit.tests
    org.codehaus.groovy.eclipse.junit.test.JUnit3TestFinderTests,
    org.codehaus.groovy.eclipse.junit.test.JUnit4TestFinderTests,
    org.codehaus.groovy.eclipse.junit.test.MainMethodFinderTests,

    // org.codehaus.groovy.eclipse.quickfix.tests
    org.codehaus.groovy.eclipse.quickfix.test.QuickAssistTests,
    org.codehaus.groovy.eclipse.quickfix.test.resolvers.GroovyProjectGroovyQuickFixTests,
    org.codehaus.groovy.eclipse.quickfix.test.resolvers.GroovyProjectJavaQuickFixTests,
    org.codehaus.groovy.eclipse.quickfix.test.resolvers.NonGroovyProjectQuickFixTests,
    org.codehaus.groovy.eclipse.quickfix.test.templates.GroovyTemplatesCompletionTests,

    // org.codehaus.groovy.eclipse.refactoring.tests
    org.codehaus.groovy.eclipse.refactoring.test.extract.ConvertLocalToFieldTests,
    org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractConstantTests,
    org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractLocalTests,
    org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractMethodTests,
    org.codehaus.groovy.eclipse.refactoring.test.extract.StaticExpressionCheckerTests,
    org.codehaus.groovy.eclipse.refactoring.test.extract.StaticFragmentCheckerTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.FindIndentsTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.FormatterPreferencesTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.GroovyFormatterTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.GroovyDocumentScannerTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.SemicolonRemoverTests,
    org.codehaus.groovy.eclipse.refactoring.test.formatter.WhitespaceRemoverTests,
    org.codehaus.groovy.eclipse.refactoring.test.rename.RenameFieldTests,
    org.codehaus.groovy.eclipse.refactoring.test.rename.RenameLocalTests,
    org.codehaus.groovy.eclipse.refactoring.test.rename.RenameMethodTests,
    org.codehaus.groovy.eclipse.refactoring.test.rename.RenamePackageTests,
    org.codehaus.groovy.eclipse.refactoring.test.rename.RenameTypeTests,
    org.codehaus.groovy.eclipse.refactoring.test.rename.MoveCURefactoringTests,
    org.codehaus.groovy.eclipse.refactoring.test.rename.SyntheticAccessorRenamingTests,

    // org.codehaus.groovy.eclipse.ui.tests
    org.codehaus.groovy.eclipse.test.actions.AddImportOnSelectionTests,
    org.codehaus.groovy.eclipse.test.actions.AliasingOrganizeImportsTests,
    org.codehaus.groovy.eclipse.test.actions.ConvertToJavaOrGroovyActionTests,
    org.codehaus.groovy.eclipse.test.actions.ConvertToPropertyActionTests,
    org.codehaus.groovy.eclipse.test.actions.GroovyNatureActionTests,
    org.codehaus.groovy.eclipse.test.actions.OrganizeImportsTests,
    org.codehaus.groovy.eclipse.test.actions.SaveParticipantRegistryTests,
    org.codehaus.groovy.eclipse.test.adapters.GroovyFileAdapterFactoryTests,
    org.codehaus.groovy.eclipse.test.adapters.GroovyIFileEditorInputAdapterFactoryTests,
    org.codehaus.groovy.eclipse.test.adapters.IsMainTesterTests,
    org.codehaus.groovy.eclipse.test.core.util.ArrayUtilsTests,
    org.codehaus.groovy.eclipse.test.core.util.ExpressionFinderTests,
    org.codehaus.groovy.eclipse.test.core.util.StringSourceBufferTests,
    org.codehaus.groovy.eclipse.test.core.util.TokenStreamTests,
    org.codehaus.groovy.eclipse.test.debug.BreakpointLocationTests,
    org.codehaus.groovy.eclipse.test.debug.DebugBreakpointsTests,
    org.codehaus.groovy.eclipse.test.launch.ConsoleLineTrackerTests,
    org.codehaus.groovy.eclipse.test.launch.GroovyScriptLaunchShortcutTests,
    org.codehaus.groovy.eclipse.test.search.FindOccurrencesTests,
    org.codehaus.groovy.eclipse.test.ui.BracketInserterTests,
    org.codehaus.groovy.eclipse.test.ui.GroovyAutoIndenterTests,
    org.codehaus.groovy.eclipse.test.ui.GroovyAutoIndenterTests2,
    org.codehaus.groovy.eclipse.test.ui.GroovyPartitionScannerTests,
    org.codehaus.groovy.eclipse.test.ui.GroovyTagScannerTests,
    org.codehaus.groovy.eclipse.test.ui.HighlightingExtenderTests,
    org.codehaus.groovy.eclipse.test.ui.OutlineExtenderTests,
    org.codehaus.groovy.eclipse.test.ui.SemanticHighlightingTests,
    org.codehaus.groovy.eclipse.test.wizards.NewGroovyTestWizardTests,
    org.codehaus.groovy.eclipse.test.wizards.NewGroovyTypeWizardTests
])
final class AllGroovyTests {
    @BeforeClass
    static void setUp() {
        // ensure that the compiler chooser is started
        def compiler = GroovyTestSuiteSupport.initializeCompilerChooser()

        println '------------ AllGroovyTests ------------'
        println 'active Groovy version = ' + compiler.activeVersion
        println 'active Groovy version (specified) = ' + compiler.activeSpecifiedVersion
        println '----------------------------------------'
    }
}
