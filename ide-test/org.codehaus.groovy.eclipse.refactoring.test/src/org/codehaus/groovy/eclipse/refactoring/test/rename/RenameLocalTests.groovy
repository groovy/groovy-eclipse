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
package org.codehaus.groovy.eclipse.refactoring.test.rename

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.refactoring.core.rename.JavaRefactoringDispatcher
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.ILocalVariable
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring
import org.junit.Test

@CompileStatic
final class RenameLocalTests extends RefactoringTestSuite {

    @Override
    protected String getRefactoringPath() {
        null
    }

    private RefactoringStatus helper(String unitNameNoExtension, String initial, String expected, String newVariableName, int refactorLocation, boolean expectingWarning) {
        ICompilationUnit cu = createCU(packageP, 'A.groovy', initial)
        ILocalVariable toRename = (ILocalVariable) cu.codeSelect(refactorLocation, 1)[0]
        JavaRefactoringDispatcher dispatcher = new JavaRefactoringDispatcher(toRename)
        dispatcher.newName = newVariableName
        RenameJavaElementDescriptor descriptor = dispatcher.createDescriptorForLocalVariable()
        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor)

        RefactoringStatus result = performRefactoring(refactoring, !expectingWarning)
        assert result == null || result.isOK() || result.hasWarning() : 'was supposed to pass'
        assertEqualLines('invalid renaming', expected, cu.source)

        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '! anythingToRedo'

        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())
        assertEqualLines('invalid undo', initial, cu.source)

        assert !RefactoringCore.getUndoManager().anythingToUndo() : '! anythingToUndo'
        assert RefactoringCore.getUndoManager().anythingToRedo() : 'anythingToRedo'

        RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor())
        assertEqualLines('invalid redo', expected, cu.source)

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
    void testRenameLocal1() {
        helper('def XXX = 9', 'def NEW = 9')
    }

    @Test
    void testRenameLocal2() {
        helper('def XXX = XXX', 'def NEW = XXX')
    }

    @Test
    void testRenameLocal3() {
        helper('def XXX = 9\nXXX = XXX', 'def NEW = 9\nNEW = NEW')
    }

    @Test
    void testRenameLocal4() {
        helper('def XXX = 9\ndef c = { XXX }', 'def NEW = 9\ndef c = { NEW }')
    }

    @Test
    void testRenameLocal5() {
        helper('def x(XXX) { XXX }', 'def x(NEW) { NEW }')
    }

    @Test
    void testRenameLocal6() {
        helper('def x(XXX) { XXX.something }', 'def x(NEW) { NEW.something }')
    }

    @Test
    void testRenameLocal7() {
        helper('def x(XXX) { "${XXX}" }', 'def x(NEW) { "${NEW}" }')
    }

    @Test
    void testRenameLocal8() {
        helper('def x(XXX) { "${XXX.toString()}" }', 'def x(NEW) { "${NEW.toString()}" }')
    }

    @Test
    void testRenameLocal9() {
        helper('def x(XXX) { "$XXX" }', 'def x(NEW) { "$NEW" }')
    }

    @Test
    void testRenameLocal10() {
        helperExpectWarning('def x(XXX) { XXX.XXX }', 'def x(NEW) { NEW.XXX }')
    }

    @Test
    void testRenameLocal11() {
        helperExpectWarning('def x(XXX) { this.XXX }', 'def x(NEW) { this.XXX }')
    }

    @Test
    void testRenameLocal12() {
        helperExpectWarning('class A { def x(XXX) { XXX\nthis.XXX } \nint XXX}', 'class A { def x(NEW) { NEW\nthis.XXX } \nint XXX}')
    }

    @Test // the two blocks should remain independent
    void testRenameLocal13() {
        String source = '''\
            |def method(List list) {
            |  for (XXX in list) {
            |    XXX
            |  }
            |  def out = list.collect { XXX ->
            |    XXX
            |  }
            |}
            |'''.stripMargin()
        String expect = '''\
            |def method(List list) {
            |  for (NEW in list) {
            |    NEW
            |  }
            |  def out = list.collect { XXX ->
            |    XXX
            |  }
            |}
            |'''.stripMargin()

        helper(source, expect)
    }

    @Test
    void testRenameLocalWithConflict1() {
        String source = '''\
            |def XXX
            |while(XXX) {
            |  XXX
            |  def NEW
            |  NEW
            |}
            |'''.stripMargin()
        String expect = '''\
            |def NEW
            |while(NEW) {
            |  NEW
            |  def NEW
            |  NEW
            |}
            |'''.stripMargin()

        helperExpectWarning(source, expect)
    }

    @Test
    void testRenameLocalWithConflict2() {
        String source = '''\
            |class A {
            |  def NEW
            |  def x(XXX) {
            |    XXX
            |    this.XXX
            |  }
            |}
            |'''.stripMargin()
        String expect = '''\
            |class A {
            |  def NEW
            |  def x(NEW) {
            |    NEW
            |    this.XXX
            |  }
            |}
            |'''.stripMargin()

        helperExpectWarning(source, expect)
    }

    @Test
    void testRenameLocalWithConflict3() {
        String source = '''\
            |def XXX
            |def y = { NEW -> XXX }
            |'''.stripMargin()
        String expect = '''\
            |def NEW
            |def y = { NEW -> NEW }
            |'''.stripMargin()

        helperExpectWarning(source, expect)
    }

    @Test
    void testRenameLocalWithConflict4() {
        String source = '''\
            |def NEW
            |while(NEW) {
            |  NEW
            |  def XXX
            |  XXX
            |  NEW
            |}
            |'''.stripMargin()
        String expect = '''\
            |def NEW
            |while(NEW) {
            |  NEW
            |  def NEW
            |  NEW
            |  NEW
            |}
            |'''.stripMargin()

        helperExpectWarning(source, expect)
    }
}
