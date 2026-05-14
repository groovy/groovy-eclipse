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

import static org.codehaus.groovy.eclipse.refactoring.test.extract.ExtractConstantTestsData.*

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyConstantRefactoring
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.internal.corext.util.JdtFlags
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.Test

@CompileStatic
final class ExtractConstantTests extends RefactoringTestSuite {

    private static final String FOO_BAR = 'Foo + Bar'
    private static final String FOO_BAR_FRAX = 'Foo+Bar+A.frax()'

    final String refactoringPath = null

    private void helper(String before, String expected, int offset, int length, boolean replaceAllOccurrences, boolean useQualifiedReplace, boolean makeFail) {
        GroovyCompilationUnit cu = (GroovyCompilationUnit) createCU(packageP, 'A.groovy', before)

        ExtractGroovyConstantRefactoring refactoring = new ExtractGroovyConstantRefactoring(cu, offset, length)
        refactoring.constantName = refactoring.guessConstantName()
        refactoring.qualifyReferencesWithDeclaringClassName = useQualifiedReplace
        refactoring.replaceAllOccurrences = replaceAllOccurrences
        refactoring.visibility = JdtFlags.VISIBILITY_STRING_PACKAGE

        RefactoringStatus result = performRefactoring(refactoring, makeFail)
        if (makeFail) {
            assert result.hasError() : 'Refactoring should NOT have been performed'
            return
        }
        assert result == null || result.isOK() : 'was supposed to pass'
        assertEqualLines('invalid extraction', expected, cu.source)

        RefactoringCore.getUndoManager().with {
            assert anythingToUndo()
            assert !anythingToRedo()

            performUndo(null, new NullProgressMonitor())
            assertEqualLines('invalid undo', before, cu.source)

            assert !anythingToUndo()
            assert anythingToRedo()

            performRedo(null, new NullProgressMonitor())
            assertEqualLines('invalid redo', expected, cu.source)
        }
    }

    @Test
    void test1() {
        helper(getTest1In(), getTest1Out(), findLocation(FOO_BAR, 'test1'), FOO_BAR.length(), true, false, false)
    }

    @Test
    void test2() {
        helper(getTest2In(), getTest2Out(), findLocation(FOO_BAR, 'test2'), FOO_BAR.length(), true, false, false)
    }

    @Test
    void test3() {
        helper(getTest3In(), getTest3Out(), findLocation(FOO_BAR_FRAX, 'test3'), FOO_BAR_FRAX.length(), true, false, false)
    }

    @Test
    void test4() {
        helper(getTest4In(), getTest4Out(), findLocation(FOO_BAR_FRAX, 'test4'), FOO_BAR_FRAX.length(), true, false, false)
    }

    @Test
    void test5a() {
        helper(getTest5aIn(), getTest5aOut(), findLocation(FOO_BAR_FRAX, 'test5a'), FOO_BAR_FRAX.length(), true, false, false)
    }

    @Test
    void test6a() {
        helper(getTest6aIn(), getTest6aOut(), findLocation(FOO_BAR_FRAX, 'test6a'), FOO_BAR_FRAX.length(), true, false, false)
    }

    @Test
    void test7() {
        helper(getTest7In(), getTest7In(), findLocation(FOO_BAR, 'test7'), FOO_BAR.length(), false, false, true)
    }

    @Test
    void test8() {
        helper(getTest8In(), getTest8Out(), findLocation(FOO_BAR, 'test8'), FOO_BAR.length(), false, false, false)
    }

    @Test
    void testQualifiedReplace1() {
        helper(getTestQualifiedReplace1In(), getTestQualifiedReplace1Out(),
            findLocation(FOO_BAR_FRAX, 'testQualifiedReplace1'), FOO_BAR_FRAX.length(), true, true, false)
    }

    @Test
    void testNoReplaceOccurrences1() {
        helper(getTestNoReplaceOccurrences1In(), getTestNoReplaceOccurrences1Out(),
            findLocation(FOO_BAR_FRAX, 'testNoReplaceOccurrences1'), FOO_BAR_FRAX.length(), false, false, false)
    }
}
