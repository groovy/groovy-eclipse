/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation
import org.eclipse.ltk.core.refactoring.CreateChangeOperation
import org.eclipse.ltk.core.refactoring.PerformChangeOperation
import org.eclipse.ltk.core.refactoring.Refactoring
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.Assert

abstract class RenameRefactoringTestSuite extends GroovyEclipseTestSuite {

    protected static final class TestSource {
        String pack, name, contents, finalContents
    }

    protected final ICompilationUnit[] createUnits(TestSource... sources) {
        sources.collect {
            ICompilationUnit unit
            if (it.name.endsWith('.groovy')) {
                unit = addGroovySource(it.contents, it.name - ~/.groovy$/, it.pack)
            } else if (it.name.endsWith('.java')) {
                unit = addJavaSource(it.contents, it.name - ~/.java$/, it.pack)
            }
            unit.discardWorkingCopy()
            return unit
        }.toArray(new ICompilationUnit[0])
    }

    protected void assertContents(ICompilationUnit[] existingUnits, List<String> expectedContents) {
        def sb = new StringBuilder()
        existingUnits.eachWithIndex { existingUnit, i ->
            if (expectedContents[i] != null) {
                assertContents(existingUnit, expectedContents[i])
            } else if (existingUnits[i].exists()) { // unit should have been deleted
                sb.append('\nUnit ' + existingUnit.elementName + ' should have been deleted.\n')
                sb.append('Instead had the following contents:\n').append(existingUnit.contents)
                Assert.fail('Refactoring produced unexpected results:' + sb)
            }
        }
    }

    protected void assertContents(ICompilationUnit existingUnit, String expectedContents) {
        String actualContents = String.valueOf(existingUnit.contents)
        Assert.assertEquals('Refactoring produced unexpected results:', expectedContents, actualContents)
    }

    protected RefactoringStatus performRefactoring(Refactoring refactor, boolean providesUndo) {
        def create = new CreateChangeOperation(new CheckConditionsOperation(
            refactor, CheckConditionsOperation.ALL_CONDITIONS), RefactoringStatus.FATAL)
        def change = new PerformChangeOperation(create)
        def undoer = RefactoringCore.getUndoManager()
        change.setUndoManager(undoer, refactor.name)
undoer.flush()
        executePerformOperation(change, ResourcesPlugin.getWorkspace())
        RefactoringStatus status = create.conditionCheckingStatus
        assert change.changeExecuted() || !change.changeExecutionFailed() : 'Change was not executed'
        if (providesUndo) {
            assert change.undoChange != null : 'Undo does not exist'
            assert undoer.anythingToUndo() : 'Undo manager is empty'
        } else {
            assert change.undoChange == null : 'Undo manager contains undo but should not'
        }
        return status
    }

    /**
     * Can ignore all errors that don't have anything to do with us.
     */
    protected RefactoringStatus ignoreKnownErrors(RefactoringStatus result) {
        if (result.severity != RefactoringStatus.ERROR) {
            return result
        }
        for (entry in result.entries) {
            // if this entries is known or it isn't an error,
            // then it can be ignored; otherwise not OK
            if (!checkStringForKnownErrors(entry.message) && entry.isError()) {
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

    protected void executePerformOperation(PerformChangeOperation perform, IWorkspace workspace) {
        buildProject()
        waitForIndex()
        workspace.run(perform, new NullProgressMonitor())
    }
}
