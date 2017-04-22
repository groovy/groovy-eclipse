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

import org.codehaus.groovy.eclipse.test.TestProject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.ltk.core.refactoring.Change
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation
import org.eclipse.ltk.core.refactoring.CreateChangeOperation
import org.eclipse.ltk.core.refactoring.IUndoManager
import org.eclipse.ltk.core.refactoring.PerformChangeOperation
import org.eclipse.ltk.core.refactoring.Refactoring
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName

abstract class RenameRefactoringTestCase {

    @Rule
    public TestName test = new TestName()

    protected TestProject testProject

    @Before
    final void setUpTestCase() {
        println '----------------------------------------'
        println 'Starting: ' + test.getMethodName()

        testProject = new TestProject()
        TestProject.setAutoBuilding(false)
    }

    @After
    final void tearDownTestCase() {
        testProject.dispose()
        testProject = null
    }

    protected void assertContents(ICompilationUnit[] existingUnits, List<String> expectedContents) {
        def sb = new StringBuilder()
        existingUnits.eachWithIndex { unit, i ->
            if (expectedContents[i] != null) {
                String actualContents = String.valueOf(unit.getContents())
                if (!actualContents.equals(expectedContents[i])) {
                    sb.append('\n-----EXPECTING-----\n')
                    sb.append(expectedContents[i])
                    sb.append('\n--------WAS--------\n')
                    sb.append(actualContents)
                }
            } else if (existingUnits[i].exists()) {
                // unit should have been deleted
                sb.append('\nUnit ' + unit.getElementName() + ' should have been deleted.\n')
                sb.append('Instead had the following contents:\n')
                sb.append(unit.getContents())
            }
        }
        if (sb.length() > 0) Assert.fail('Refactoring produced unexpected results:' + sb)
    }

    protected void assertContents(ICompilationUnit existingUnits, String expectedContents) {
        def sb = new StringBuilder()
        String actualContents = String.valueOf(existingUnits.getContents())
        if (!actualContents.equals(expectedContents)) {
            sb.append('\n-----EXPECTING-----\n')
            sb.append(expectedContents)
            sb.append('\n--------WAS--------\n')
            sb.append(actualContents)
        }
        if (sb.length() > 0) Assert.fail('Refactoring produced unexpected results:' + sb)
    }

    protected RefactoringStatus performRefactoring(Refactoring ref, boolean providesUndo, boolean performOnFail) {
        testProject.fullBuild()
        testProject.waitForIndexer()
        IUndoManager undoManager = getUndoManager()
        final CreateChangeOperation create = new CreateChangeOperation(
            new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS), RefactoringStatus.FATAL)
        final PerformChangeOperation perform = new PerformChangeOperation(create)
        perform.setUndoManager(undoManager, ref.getName())
        IWorkspace workspace = ResourcesPlugin.getWorkspace()
        executePerformOperation(perform, workspace)
        RefactoringStatus status = create.getConditionCheckingStatus()
        assert perform.changeExecuted() || !perform.changeExecutionFailed() : 'Change was not executed'
        Change undo = perform.getUndoChange()
        if (providesUndo) {
            assert undo != null : 'Undo does not exist'
            assert undoManager.anythingToUndo() : 'Undo manager is empty'
        } else {
            assert undo == null : 'Undo manager contains undo but should not'
        }
        return status
    }

    /**
     * Can ignore all errors that don't have anything to do with us.
     */
    protected RefactoringStatus ignoreKnownErrors(RefactoringStatus result) {
        if (result.getSeverity() != RefactoringStatus.ERROR) {
            return result
        }
        for (entry in result.getEntries()) {
            // if this entries is known or it isn't an error,
            // then it can be ignored; otherwise not OK
            if (!checkStringForKnownErrors(entry.getMessage()) && entry.isError()) {
                return result
            }
        }
        return new RefactoringStatus()
    }

    private boolean checkStringForKnownErrors(String resultString) {
        return resultString.contains('Found potential matches') ||
            resultString.contains('Method breakpoint participant') ||
            resultString.contains('Watchpoint participant') ||
            resultString.contains('Breakpoint participant') ||
            resultString.contains('Launch configuration participant')
    }

    protected Refactoring createRefactoring(RefactoringDescriptor descriptor) {
        RefactoringStatus status = new RefactoringStatus()
        Refactoring refactoring = descriptor.createRefactoring(status)
        assert refactoring != null : 'refactoring should not be null'
        assert status.isOK() : 'status should be ok, but was: ' + status
        return refactoring
    }

    protected IUndoManager getUndoManager() {
        IUndoManager undoManager = RefactoringCore.getUndoManager()
        undoManager.flush()
        return undoManager
    }

    protected void executePerformOperation(PerformChangeOperation perform, IWorkspace workspace) {
        workspace.run(perform, new NullProgressMonitor())
    }
}
