/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.extract;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.core.extract.ConvertGroovyLocalToFieldRefactoring;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTest;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author Stephanie Van Dyk
 * @author Daniel Phan
 * @created Jan 22, 2012
 */
public class ConvertLocalToFieldTests extends RefactoringTest {

    public static Test suite() {
        return new RefactoringTestSetup(new TestSuite(ConvertLocalToFieldTests.class));
    }

    public ConvertLocalToFieldTests(String name) {
        super(name);
    }

    @Override
    protected String getRefactoringPath() {
        return "ConvertLocalToField/";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fIsPreDeltaTest = true;
    }

    public void testMethodToModule() throws Exception {
        runTest("testMethodToModule");
    }

    public void testClosureToModule() throws Exception {
        runTest("testClosureToModule");
    }

    public void testDeclarationWithDef() throws Exception {
        runTest("testDeclarationWithDef");
    }

    public void testDeclarationWithType() throws Exception {
        runTest("testDeclarationWithType");
    }

    public void testReference() throws Exception {
        runTest("testReference");
    }

    public void testTupleDeclaration() throws Exception {
        runTest("testTupleDeclaration");
    }

    public void testRename() throws Exception {
        runTest("testRename");
    }

    public void testInitialization() throws Exception {
        runTest("testInitialization");
    }

    public void testVariableConflict() throws Exception {
        runTest("testVariableConflict");
    }

    public void testFieldConflict() throws Exception {
        runTest("testFieldConflict");
    }

    public void testFieldReference() throws Exception {
        runTest("testFieldReference");
    }

    public void testException() throws Exception {
        runTest("testException");
    }

    public void testForLoop() throws Exception {
        runTest("testForLoop");
    }

    public void testPostfix() throws Exception {
        runTest("testPostfix");
    }

    public void testPrefix() throws Exception {
        runTest("testPrefix");
    }

    public void testMethodInvocation() throws Exception {
        runTest("testMethodInvocation");
    }

    public void testParameterList() throws Exception {
        runTest("testParameterList");
    }

    public void testArgumentList() throws Exception {
        runTest("testArgumentList");
    }

    public void testInnerClass() throws Exception {
        runTest("testInnerClass");
    }

    public void testInnerFieldConflict() throws Exception {
        runTest("testInnerFieldConflict");
    }

    public void testFakeField() throws Exception {
        runTest("testFakeField");
    }

    public void testClosure() throws Exception {
        runTest("testClosure");
    }

    public void testClosureVariableConflict() throws Exception {
        runTest("testClosureVariableConflict");
    }

    public void testClosureParameterList() throws Exception {
        runTest("testClosureParameterList");
    }

    public void testClosureImplicitIt() throws Exception {
        runTest("testClosureImplicitIt");
    }

    private void runTest(String testName) throws Exception {
        ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get(testName);
        GroovyCompilationUnit cu = (GroovyCompilationUnit) createCU(getPackageP(), "Test.groovy", testCase.getInput());
        try {
            ConvertGroovyLocalToFieldRefactoring refactoring = new ConvertGroovyLocalToFieldRefactoring(cu, testCase.getSelectionOffset(), testCase.getSelectionLength());
            refactoring.setFieldName(testCase.getFieldName());

            RefactoringStatus result = null;
            try {
                result = performRefactoring(refactoring, false);
            } catch (AssertionFailedError e) {
                // If expected is null, the TestCase expected the refactoring to fail.
                if (testCase.getExpected() == null) {
                    return;
                } else {
                    throw e;
                }
            }

            if (testCase.isExpectWarning()) {
                assertTrue("was supposed to pass", result.hasWarning());
            } else {
                assertTrue("was supposed to pass", result.isOK());
                assertEqualLines("invalid conversion", testCase.getExpected(), cu.getSource());
            }

            assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
            assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());

            RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
            assertEqualLines("invalid undo", testCase.getInput(), cu.getSource());

            assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
            assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());

            RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
            assertEqualLines("invalid redo", testCase.getExpected(), cu.getSource());
        } finally {
            performDummySearch();
            cu.delete(true, null);
        }
    }
}
