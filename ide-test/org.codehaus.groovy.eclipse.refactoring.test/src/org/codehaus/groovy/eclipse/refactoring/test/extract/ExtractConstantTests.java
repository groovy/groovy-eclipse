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
package org.codehaus.groovy.eclipse.refactoring.test.extract;

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyConstantRefactoring;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestCase;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Note that all test files use tabs instead of spaces
 */
public final class ExtractConstantTests extends RefactoringTestCase {

    private static final String FOO_BAR = "Foo + Bar";
    private static final String FOO_BAR_FRAX = "Foo+Bar+A.frax()";

    public static junit.framework.Test suite() {
        return new RefactoringTestSetup(new junit.framework.TestSuite(ExtractConstantTests.class));
    }

    public ExtractConstantTests(String name) {
        super(name);
    }

    @Override
    protected String getRefactoringPath() {
        return "ExtractConstant/";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fIsPreDeltaTest = true;
    }

    public void test1() throws Exception {
        helper(ExtractConstantTestData.getTest1In(),
                ExtractConstantTestData.getTest1Out(),
                ExtractConstantTestData.findLocation(FOO_BAR, "test1"),
                FOO_BAR.length(), true, false, false);
    }

    public void test2() throws Exception {
        helper(ExtractConstantTestData.getTest2In(),
                ExtractConstantTestData.getTest2Out(),
                ExtractConstantTestData.findLocation(FOO_BAR, "test2"),
                FOO_BAR.length(), true, false, false);
    }

    public void test3() throws Exception {
        helper(ExtractConstantTestData.getTest3In(),
                ExtractConstantTestData.getTest3Out(),
                ExtractConstantTestData.findLocation(FOO_BAR_FRAX, "test3"),
                FOO_BAR_FRAX.length(), true, false, false);
    }

    public void test4() throws Exception {
        helper(ExtractConstantTestData.getTest4In(),
                ExtractConstantTestData.getTest4Out(),
                ExtractConstantTestData.findLocation(FOO_BAR_FRAX, "test4"),
                FOO_BAR_FRAX.length(), true, false, false);
    }

    public void test5a() throws Exception {
        helper(ExtractConstantTestData.getTest5aIn(),
                ExtractConstantTestData.getTest5aOut(),
                ExtractConstantTestData.findLocation(FOO_BAR_FRAX, "test5a"),
                FOO_BAR_FRAX.length(), true, false, false);
    }

    public void test6a() throws Exception {
        helper(ExtractConstantTestData.getTest6aIn(),
                ExtractConstantTestData.getTest6aOut(),
                ExtractConstantTestData.findLocation(FOO_BAR_FRAX, "test6a"),
                FOO_BAR_FRAX.length(), true, false, false);
    }

    public void test7() throws Exception {
        helper(ExtractConstantTestData.getTest7In(),
                ExtractConstantTestData.getTest7In(),
                ExtractConstantTestData.findLocation(FOO_BAR, "test7"),
                FOO_BAR.length(), false, false, true);
    }

    public void test8() throws Exception {
        helper(ExtractConstantTestData.getTest8In(),
                ExtractConstantTestData.getTest8Out(),
                ExtractConstantTestData.findLocation(FOO_BAR, "test8"),
                FOO_BAR.length(), false, false, false);
    }

    public void testNoReplaceOccurrences1() throws Exception {
        helper(ExtractConstantTestData.getTestNoReplaceOccurrences1In(),
                ExtractConstantTestData.getTestNoReplaceOccurrences1Out(),
                ExtractConstantTestData.findLocation(FOO_BAR_FRAX, "testNoReplaceOccurrences1"),
                FOO_BAR_FRAX.length(), false, false, false);
    }

    public void testQualifiedReplace1() throws Exception {
        helper(ExtractConstantTestData.getTestQualifiedReplace1In(),
                ExtractConstantTestData.getTestQualifiedReplace1Out(),
                ExtractConstantTestData.findLocation(FOO_BAR_FRAX, "testQualifiedReplace1"),
                FOO_BAR_FRAX.length(), true, true, false);
    }

    private void helper(String before, String expected, int offset, int length, boolean replaceAllOccurrences, boolean useQualifiedReplace, boolean makeFail) throws Exception {
        GroovyCompilationUnit cu = (GroovyCompilationUnit) createCU(getPackageP(), "A.groovy", before);
        try {
            ExtractGroovyConstantRefactoring refactoring = new ExtractGroovyConstantRefactoring(cu, offset, length);
            refactoring.setVisibility(JdtFlags.VISIBILITY_STRING_PACKAGE);
            refactoring.setReplaceAllOccurrences(replaceAllOccurrences);
            refactoring.setQualifyReferencesWithDeclaringClassName(useQualifiedReplace);
            refactoring.setConstantName(refactoring.guessConstantName());
            RefactoringStatus result = performRefactoring(refactoring, makeFail);
            if (makeFail) {
                assertTrue("Refactoring should NOT have been performed", result.hasError());
                return;
            }
            assertTrue("was supposed to pass", result == null || result.isOK());
            assertEqualLines("invalid extraction", expected, cu.getSource());

            assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
            assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());

            RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
            assertEqualLines("invalid undo", before, cu.getSource());

            assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
            assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());

            RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
            assertEqualLines("invalid redo", expected, cu.getSource());
        } finally {
            performDummySearch();
            cu.delete(true, null);
        }
    }
}
