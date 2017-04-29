/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.alltests

import org.codehaus.groovy.eclipse.codeassist.tests.*
import org.codehaus.groovy.eclipse.codebrowsing.tests.*
import org.codehaus.groovy.eclipse.core.test.*
import org.codehaus.groovy.eclipse.dsl.tests.AllDSLTests
import org.codehaus.groovy.eclipse.junit.test.*
import org.codehaus.groovy.eclipse.quickfix.test.*
import org.codehaus.groovy.eclipse.quickfix.test.resolvers.*
import org.codehaus.groovy.eclipse.quickfix.test.templates.*
import org.codehaus.groovy.eclipse.refactoring.test.extract.*
import org.codehaus.groovy.eclipse.refactoring.test.formatter.*
import org.codehaus.groovy.eclipse.refactoring.test.rename.*
import org.codehaus.groovy.eclipse.test.ErrorLogTest
import org.codehaus.groovy.eclipse.test.actions.*
import org.codehaus.groovy.eclipse.test.adapters.*
import org.codehaus.groovy.eclipse.test.core.util.*
import org.codehaus.groovy.eclipse.test.debug.*
import org.codehaus.groovy.eclipse.test.search.*
import org.codehaus.groovy.eclipse.test.ui.*
import org.codehaus.groovy.eclipse.test.wizards.*

final class AllGroovyTests {

    static junit.framework.Test suite() {
        // ensure that the compiler chooser starts up
        def compiler = GroovyTestSuiteSupport.initializeCompilerChooser()

        // use syserr to see the messages in the build log; sysout seems to disapear without a trace on build server
        System.err.println '------------ AllGroovyTests ------------'
        System.err.println 'active Groovy version = ' + compiler.activeVersion
        System.err.println 'active Groovy version (specified) = ' + compiler.activeSpecifiedVersion
        System.err.println '----------------------------------------'

        def suite = new junit.framework.TestSuite(AllGroovyTests.class.name)
        suite.addTest(new junit.framework.JUnit4TestAdapter(ErrorLogTest))
        suite.addTest(new junit.framework.JUnit4TestAdapter(SanityTests))
        suite.addTest(adapt('org.codehaus.groovy.eclipse.codeassist.tests',
            AnnotationCompletionTests,
            CommandChainCompletionTests,
            ConstructorCompletionTests,
            ContentAssistLocationTests,
            ContextInformationTests,
            DefaultGroovyMethodCompletionTests,
            DefaultMethodContentAssistTests,
            ExtendedCompletionContextTests,
            FieldCompletionTests,
            FindImportsRegionTests,
            GenericCompletionTests,
            GroovyLikeCompletionTests,
            GuessingCompletionTests,
            InferencingCompletionTests,
            InnerTypeCompletionTests,
            LocalVariableCompletionTests,
            MethodCompletionTests,
            NewFieldCompletionTests,
            OtherCompletionTests,
            ProposalProviderAndFilterTests,
            RelevanceTests,
            StaticImportsCompletionTests,
            TypeCompletionTests,
            TypeCompletionTests2))
        suite.addTest(adapt('org.codehaus.groovy.eclipse.codebrowsing.tests',
            ASTFragmentTests,
            ASTPositionTests,
            CodeSelectAttributesTests,
            CodeSelectCategoriesTests,
            CodeSelectFieldsTests,
            CodeSelectGenericsTests,
            CodeSelectImportsTests,
            CodeSelectLocalTests,
            CodeSelectMethodsTests,
            CodeSelectPackageTests,
            CodeSelectPropertiesTests,
            CodeSelectStaticImportsTests,
            CodeSelectTypesTests,
            FindAllOccurrencesVisitorTests,
            FindSurroundingNodeTests,
            IsSameExpressionTests,
            PartialVisitTests))
        suite.addTest(adapt('org.codehaus.groovy.eclipse.core.tests',
            AstPositionTests,
            ClasspathContainerTests,
            ErrorRecoveryTests,
            SyntheticMemberSearchTests,
            UnrecoverableErrorTests))
        suite.addTest(AllDSLTests.suite())
        suite.addTest(adapt('org.codehaus.groovy.eclipse.junit.tests',
            JUnit3TestFinderTests,
            JUnit4TestFinderTests,
            MainMethodFinderTests))
        suite.addTest(adapt('org.codehaus.groovy.eclipse.quickfix.tests',
            GroovyProjectGroovyQuickFixTests,
            GroovyProjectJavaQuickFixTests,
            GroovyTemplatesCompletionTests,
            NonGroovyProjectQuickFixTests,
            QuickAssistTests))
        suite.addTest(adapt('org.codehaus.groovy.eclipse.refactoring.tests',
            // extract
            ConvertLocalToFieldTests,
            ExtractConstantTests,
            ExtractLocalTests,
            ExtractMethodTests,
            StaticExpressionCheckerTests,
            StaticFragmentCheckerTests,
            // formatter
            FindIndentsTests,
            FormatterPreferencesTests,
            GroovyFormatterTests,
            GroovyDocumentScannerTests,
            SemicolonRemoverTests,
            WhitespaceRemoverTests,
            // rename
            RenameFieldTests,
            RenameLocalTests,
            RenameMethodTests,
            RenameTypeTests,
            MoveCURefactoringTests,
            SyntheticAccessorRenamingTests))
        suite.addTest(adapt('org.codehaus.groovy.eclipse.ui.tests',
            // actions
            AddImportOnSelectionTests,
            AliasingOrganizeImportsTests,
            ConvertToJavaOrGroovyActionTests,
            ConvertToPropertyActionTests,
            GroovyNatureActionTests,
            OrganizeImportsTests,
            SaveParticipantRegistryTests,
            // adapters
            GroovyFileAdapterFactoryTests,
            GroovyIFileEditorInputAdapterFactoryTests,
            IsMainTesterTests,
            // core.util
            ArrayUtilsTests,
            ExpressionFinderTests,
            StringSourceBufferTests,
            TokenStreamTests,
            // debug
            BreakpointLocationTests,
            ConsoleLineTrackerTests,
            DebugBreakpointsTests,
            GroovyLauncherShortcutTests,
            // search
            FindOccurrencesTests,
            // ui
            BracketInserterTests,
            GroovyAutoIndenterTests,
            GroovyAutoIndenterTests2,
            GroovyPartitionScannerTests,
            GroovyTagScannerTests,
            HighlightingExtenderTests,
            OutlineExtenderTests,
            SemanticHighlightingTests,
            // wizards
            NewGroovyTestCaseWizardTests,
            NewGroovyTypeWizardTests))
        return suite
    }

    private static junit.framework.Test adapt(String name, Class... tests) {
        def suite = new junit.framework.TestSuite(name)
        for (test in tests) {
            suite.addTest(new junit.framework.JUnit4TestAdapter(test))
        }
        return suite
    }
}
