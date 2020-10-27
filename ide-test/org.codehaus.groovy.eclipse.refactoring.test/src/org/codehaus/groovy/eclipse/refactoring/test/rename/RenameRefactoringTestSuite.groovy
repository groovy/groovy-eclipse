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

import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.groovy.tests.search.SearchTestSuite
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation
import org.eclipse.ltk.core.refactoring.CreateChangeOperation
import org.eclipse.ltk.core.refactoring.PerformChangeOperation
import org.eclipse.ltk.core.refactoring.Refactoring
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.Assert

@AutoFinal @CompileStatic
abstract class RenameRefactoringTestSuite extends GroovyEclipseTestSuite {

    protected static final class TestSource {
        String pack, name, contents, finalContents
    }

    protected final ICompilationUnit[] createUnits(TestSource... sources) {
        return sources.findResults {
            if (it.name.endsWith('.groovy')) {
                addGroovySource(it.contents, it.name - ~/.groovy$/, it.pack).tap {
                    discardWorkingCopy()
                }
            } else if (it.name.endsWith('.java')) {
                addJavaSource(it.contents, it.name - ~/.java$/, it.pack).tap {
                    discardWorkingCopy()
                }
            }
        }.toArray(new ICompilationUnit[0])
    }

    @CompileDynamic
    protected Refactoring createRefactoring(RefactoringDescriptor descriptor) {
        RefactoringStatus status = new RefactoringStatus()
        return descriptor.createRefactoring(status).tap {
            assert processor.isApplicable()
            assert status.isOK()
        }
    }

    protected RefactoringStatus performRefactoring(Refactoring refactor, boolean providesUndo = true) {
        def create = new CreateChangeOperation(new CheckConditionsOperation(refactor, CheckConditionsOperation.ALL_CONDITIONS), RefactoringStatus.FATAL)
        def change = new PerformChangeOperation(create)
        def undoer = RefactoringCore.getUndoManager()
        change.setUndoManager(undoer, refactor.name)

        RefactoringStatus status = executeOperation(change)

        assert change.changeExecuted() || !change.changeExecutionFailed() : 'Change was not executed'
        if (providesUndo) {
            assert change.undoChange != null : 'Undo does not exist'
            assert undoer.anythingToUndo() : 'Undo manager is empty'
        } else {
            assert change.undoChange == null : 'Undo manager contains undo but should not'
        }
        return status
    }

    protected RefactoringStatus executeOperation(PerformChangeOperation operation) {
        SearchTestSuite.waitUntilReady(packageFragmentRoot.javaProject)
        ResourcesPlugin.getWorkspace().run(operation, null)
        return operation.conditionCheckingStatus
    }

    protected RefactoringStatus ignoreKnownErrors(RefactoringStatus status) {
        if (status.severity != RefactoringStatus.ERROR) {
            return status
        }
        def knownErrors = [
            'Found potential matches',
            'Watchpoint participant',
            'Breakpoint participant',
            'Method breakpoint participant',
            'Launch configuration participant'
        ]
        for (entry in status.entries) {
            // if entry isn't an error or is a known error, then it can be ignored
            if (entry.isError() && !knownErrors.any { entry.message.contains(it) }) {
                return status
            }
        }
        return new RefactoringStatus()
    }

    protected void assertContents(ICompilationUnit existingUnit, String expectedContents) {
        Assert.assertEquals('Refactoring produced unexpected results:', expectedContents, existingUnit.source)
    }

    protected void assertContents(ICompilationUnit[] existingUnits, List<String> expectedContents) {
        for (int i = 0; i < existingUnits.length; i += 1) {
            if (expectedContents[i] != null) {
                assertContents(existingUnits[i], expectedContents[i])
            } else if (existingUnits[i].exists()) {
                Assert.fail("Unit ${existingUnits[i].elementName} should have been deleted.")
            }
        }
    }
}
