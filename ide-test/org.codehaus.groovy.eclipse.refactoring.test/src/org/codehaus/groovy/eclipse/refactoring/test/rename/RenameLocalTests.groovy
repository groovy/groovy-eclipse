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
package org.codehaus.groovy.eclipse.refactoring.test.rename

import org.codehaus.groovy.eclipse.refactoring.core.rename.JavaRefactoringDispatcher
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestCase
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.ILocalVariable
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring
import org.junit.Test

final class RenameLocalTests extends RefactoringTestCase {

    @Override
    protected String getRefactoringPath() {
        null
    }

    private RefactoringStatus helper(String unitNameNoExtension, String initial, String expected, String newVariableName, int refactorLocation, boolean expectingWarning) {
        ICompilationUnit cu = createCU(packageP, 'A.groovy', initial)
        ILocalVariable toRename = (ILocalVariable) cu.codeSelect(refactorLocation, 1)[0]
        JavaRefactoringDispatcher dispatcher = new JavaRefactoringDispatcher(toRename)
        dispatcher.setNewName(newVariableName)
        RenameJavaElementDescriptor descriptor = dispatcher.createDescriptorForLocalVariable()
        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor)

        RefactoringStatus result = performRefactoring(refactoring, !expectingWarning)
        assert result == null || result.isOK() || result.hasWarning() : 'was supposed to pass'
        assertEqualLines('invalid renaming', expected, cu.getSource())

        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '! anythingToRedo'

        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())
        assertEqualLines('invalid undo', initial, cu.getSource())

        assert !RefactoringCore.getUndoManager().anythingToUndo() : '! anythingToUndo'
        assert RefactoringCore.getUndoManager().anythingToRedo() : 'anythingToRedo'

        RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor())
        assertEqualLines('invalid redo', expected, cu.getSource())

        return result != null ? result : new RefactoringStatus()
    }

    private void helper(String initial, String expected) {
        RefactoringStatus result = helper('A', initial, expected, 'NEW', initial.indexOf('XXX'), false)
        assert result.isOK() : 'Result is not OK: ' + result
    }

    private void helperExpectWarning(String initial, String expected) {
        RefactoringStatus result = helper('A', initial, expected, 'NEW', initial.indexOf('XXX'), true)
        assert result.hasWarning() && !result.hasError() : 'Expected warning, but got ' + result
    }

    @Test
    void test0() {
        helper('def XXX = 9', 'def NEW = 9')
    }

    @Test
    void test1() {
        helper('def XXX = XXX', 'def NEW = NEW')
    }

    @Test
    void test2() {
        helper('def XXX = 9\nXXX = XXX', 'def NEW = 9\nNEW = NEW')
    }

    @Test
    void test3() {
        helper('def XXX = 9\ndef c = { XXX }', 'def NEW = 9\ndef c = { NEW }')
    }

    @Test
    void test4() {
        helper('def x(XXX) { XXX }', 'def x(NEW) { NEW }')
    }

    @Test
    void test5() {
        helper('def x(XXX) { XXX.something }', 'def x(NEW) { NEW.something }')
    }

    @Test
    void test6() {
        helper('def x(XXX) { "${XXX}" }', 'def x(NEW) { "${NEW}" }')
    }

    @Test
    void test6a() {
        helper('def x(XXX) { "${XXX.toString()}" }', 'def x(NEW) { "${NEW.toString()}" }')
    }

    @Test
    void test6c() {
        helper('def x(XXX) { "$XXX" }', 'def x(NEW) { "$NEW" }')
    }

    @Test
    void test7() {
        helper('def x(XXX) { XXX.XXX }', 'def x(NEW) { NEW.XXX }')
    }

    @Test
    void test8() {
        helper('def x(XXX) { this.XXX }', 'def x(NEW) { this.XXX }')
    }

    @Test
    void test9() {
        helper('class A { def x(XXX) { XXX\nthis.XXX } \nint XXX}', 'class A { def x(NEW) { NEW\nthis.XXX } \nint XXX}')
    }

    @Test
    void testWarning0() {
        helperExpectWarning('def XXX\nwhile(XXX) { XXX\n def NEW \n NEW }', 'def NEW\nwhile(NEW) { NEW\n def NEW \n NEW }')
    }

    @Test
    void testWarning1() {
        helperExpectWarning('class A { def NEW\ndef x(XXX) { XXX\nthis.XXX } }', 'class A { def NEW\ndef x(NEW) { NEW\nthis.XXX } }')
    }

    @Test
    void testWarning2() {
        helperExpectWarning('def XXX\ndef y = { NEW -> XXX }', 'def NEW\ndef y = { NEW -> NEW }')
    }

    @Test
    void testWarning3() {
        helperExpectWarning('def NEW\nwhile(NEW) { NEW\n def XXX \n XXX\nNEW }', 'def NEW\nwhile(NEW) { NEW\n def NEW \n NEW\nNEW }')
    }
}
