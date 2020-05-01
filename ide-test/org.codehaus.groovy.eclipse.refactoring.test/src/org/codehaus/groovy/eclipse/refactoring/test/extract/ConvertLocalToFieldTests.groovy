/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.extract

import groovy.transform.NotYetImplemented

import org.codehaus.groovy.eclipse.refactoring.core.extract.ConvertGroovyLocalToFieldRefactoring
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.Test

final class ConvertLocalToFieldTests extends RefactoringTestSuite {

    @Override
    protected String getRefactoringPath() {
        null
    }

    private void runTest(String testName = test.methodName) {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get(testName)
    }

    private void runTest(ConvertLocalToFieldTestsData.TestCase testCase) {
        GroovyCompilationUnit cu = (GroovyCompilationUnit) createCU(packageP, 'Test.groovy', testCase.input)

        def refactoring = new ConvertGroovyLocalToFieldRefactoring(cu, testCase.selectionOffset, testCase.selectionLength)
        refactoring.fieldName = testCase.fieldName

        RefactoringStatus result = null
        try {
            result = performRefactoring(refactoring, false)
        } catch (AssertionError e) {
            // If expected is null, the TestCase expected the refactoring to fail.
            if (testCase.expected == null) {
                return
            } else {
                throw e
            }
        }

        if (testCase.isExpectWarning()) {
            assert result.hasWarning() : 'was supposed to pass'
        } else {
            assert result.isOK() : 'was supposed to pass'
            assertEqualLines('invalid conversion', testCase.expected, cu.source)
        }

        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '! anythingToRedo'

        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())
        assertEqualLines('invalid undo', testCase.input, cu.source)

        assert !RefactoringCore.getUndoManager().anythingToUndo() : '! anythingToUndo'
        assert RefactoringCore.getUndoManager().anythingToRedo() : 'anythingToRedo'

        RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor())
        assertEqualLines('invalid redo', testCase.expected, cu.source)
    }

    //--------------------------------------------------------------------------

    @Test
    void testMethodToModule() {
        runTest('testMethodToModule')
    }

    @Test
    void testClosureToModule() {
        runTest('testClosureToModule')
    }

    @Test
    void testDeclarationWithDef() {
        runTest('testDeclarationWithDef')
    }

    @Test
    void testDeclarationWithType() {
        runTest('testDeclarationWithType')
    }

    @Test
    void testReference() {
        runTest('testReference')
    }

    @Test
    void testTupleDeclaration() {
        runTest('testTupleDeclaration')
    }

    @Test
    void testRename() {
        runTest('testRename')
    }

    @Test
    void testInitialization() {
        runTest('testInitialization')
    }

    @Test
    void testVariableConflict() {
        runTest('testVariableConflict')
    }

    @Test
    void testFieldConflict() {
        runTest('testFieldConflict')
    }

    @Test
    void testFieldReference() {
        runTest('testFieldReference')
    }

    @Test
    void testException() {
        runTest('testException')
    }

    @Test
    void testForLoop() {
        runTest('testForLoop')
    }

    @Test
    void testPostfix() {
        runTest('testPostfix')
    }

    @Test
    void testPrefix() {
        runTest('testPrefix')
    }

    @Test
    void testMethodInvocation() {
        runTest('testMethodInvocation')
    }

    @Test
    void testParameterList() {
        runTest('testParameterList')
    }

    @Test
    void testArgumentList() {
        runTest('testArgumentList')
    }

    @Test
    void testInnerClass() {
        runTest('testInnerClass')
    }

    @Test
    void testInnerFieldConflict() {
        runTest('testInnerFieldConflict')
    }

    @Test
    void testFakeField() {
        runTest('testFakeField')
    }

    @Test
    void testClosure() {
        runTest()
    }

    @Test
    void testClosureVariableConflict() {
        runTest()
    }

    @Test
    void testClosureParameterList() {
        runTest()
    }

    @Test
    void testClosureImplicitIt() {
        runTest()
    }

    @Test @NotYetImplemented
    void testWithinFieldInitializer1() {
        def testCase = new ConvertLocalToFieldTestsData.TestCase('''\
            |class Pogo {
            |  private Object field = { ->
            |    def target/**/ = null
            |  }
            |}
            |'''.stripMargin(), '''\
            |class Pogo {
            |\tprivate def target
            |  private Object field = { ->
            |    target/**/ = null
            |  }
            |}
            |'''.stripMargin())
        runTest(testCase)
    }

    @Test
    void testWithinFieldInitializer2() {
        def testCase = new ConvertLocalToFieldTestsData.TestCase('''\
            |@groovy.transform.Field
            |Object field = { ->
            |  def target/**/ = null
            |}
            |'''.stripMargin(), '''\
            |@groovy.transform.Field def target
            |@groovy.transform.Field
            |Object field = { ->
            |  target/**/ = null
            |}
            |'''.stripMargin())
        runTest(testCase)
    }

    @Test
    void testWithinObjectInitializer() {
        def testCase = new ConvertLocalToFieldTestsData.TestCase('''\
            |class Pogo {
            |  {
            |    def target/**/ = null
            |  }
            |}
            |'''.stripMargin(), '''\
            |class Pogo {
            |\tprivate def target
            |  {
            |    target/**/ = null
            |  }
            |}
            |'''.stripMargin())
        runTest(testCase)
    }

    @Test @NotYetImplemented
    void testWithinStaticInitializer() {
        def testCase = new ConvertLocalToFieldTestsData.TestCase('''\
            |class Pogo {
            |  static {
            |    def target/**/ = null
            |  }
            |}
            |'''.stripMargin(), '''\
            |class Pogo {
            |\tprivate static def target
            |  static {
            |    target/**/ = null
            |  }
            |}
            |'''.stripMargin())
        runTest(testCase)
    }

    @Test @NotYetImplemented // not sure if this is legal, but wanted case to test enclosing checks
    void testWithinAnnotationClosure() {
        createCU(packageP, 'Tag.groovy', '@interface Tag { Class value() }')
        def testCase = new ConvertLocalToFieldTestsData.TestCase('''\
            |@Tag(value = { ->
            |  def target/**/ = null
            |})
            |class Pogo {
            |}
            |'''.stripMargin(), '''\
            |@Tag(value = { ->
            |  Pogo.target/**/ = null
            |})
            |class Pogo {
            |\tprivate static def target
            |}
            |'''.stripMargin())
        runTest(testCase)
    }
}
