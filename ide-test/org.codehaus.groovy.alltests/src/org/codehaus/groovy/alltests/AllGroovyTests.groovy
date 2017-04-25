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

import junit.framework.*
import org.codehaus.groovy.eclipse.codeassist.tests.*
import org.codehaus.groovy.eclipse.codebrowsing.tests.*
import org.codehaus.groovy.eclipse.core.AllCoreTests
import org.codehaus.groovy.eclipse.dsl.tests.AllDSLTests
import org.codehaus.groovy.eclipse.junit.test.*
import org.codehaus.groovy.eclipse.quickfix.test.*
import org.codehaus.groovy.eclipse.quickfix.test.resolvers.*
import org.codehaus.groovy.eclipse.quickfix.test.templates.*
import org.codehaus.groovy.eclipse.refactoring.test.extract.*
import org.codehaus.groovy.eclipse.refactoring.test.formatter.*
import org.codehaus.groovy.eclipse.refactoring.test.rename.*
import org.codehaus.groovy.eclipse.test.AllUITests
import org.codehaus.groovy.frameworkadapter.util.CompilerChooser

final class AllGroovyTests {

    static Test suite() {
        // ensure that the compiler chooser starts up
        CompilerChooser compiler = GroovyTestSuiteSupport.initializeCompilerChooser()

        // use syserr to see the messages in the build log; sysout seems to disapear without a trace on build server
        System.err.println '------------ AllGroovyTests ------------'
        System.err.println 'active Groovy version = ' + compiler.activeVersion
        System.err.println 'active Groovy version (specified) = ' + compiler.activeSpecifiedVersion
        System.err.println '----------------------------------------'

        TestSuite suite = new TestSuite(AllGroovyTests.class.name)
        suite.addTest(new JUnit4TestAdapter(SanityTests))
        suite.addTest(AllUITests.suite()) // first for 'ErrorLogTest'
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
        suite.addTest(AllCoreTests.suite())
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
        return suite
    }

    private static Test adapt(String name, Class... tests) {
        TestSuite suite = new TestSuite(name)
        for (test in tests) {
            suite.addTest(new JUnit4TestAdapter(test))
        }
        return suite
    }
}
