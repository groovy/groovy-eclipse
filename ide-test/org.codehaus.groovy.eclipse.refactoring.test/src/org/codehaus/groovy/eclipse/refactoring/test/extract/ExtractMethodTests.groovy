/*
 * Copyright 2009-2019 the original author or authors.
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

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyMethodRefactoring
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSpec
import org.codehaus.groovy.eclipse.refactoring.test.internal.TestPrefInitializer
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Platform
import org.eclipse.jdt.core.Flags
import org.eclipse.ltk.core.refactoring.Change
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized)
final class ExtractMethodTests extends GroovyEclipseTestSuite {

    @Parameters
    static params() {
        URL url = Platform.getBundle('org.codehaus.groovy.eclipse.refactoring.test').getEntry('/resources/ExtractMethod')
        new File(FileLocator.toFileURL(url).file).listFiles({ File dir, String item ->
            item ==~ /ExtractMethod_Test_.*/
        } as FilenameFilter)
    }

    ExtractMethodTests(File file) {
        spec = new RefactoringTestSpec(file)
    }

    private RefactoringTestSpec spec
    private GroovyCompilationUnit unit
    private ExtractGroovyMethodRefactoring refactoring

    @Test
    void test() {
        preAction()
        def rs = checkInitialCondition()
        simulateUserInput()
        rs.merge(checkFinalCondition())
        if (analyseRefactoringStatus(rs)) {
            Change change = createChange()
            change.perform(new NullProgressMonitor())
        }

        String actual = String.valueOf(unit.contents)
        String expect = spec.expected.get()
        Assert.assertEquals(expect, actual)
    }

    private void preAction() {
        unit = addGroovySource(spec.document.get(), nextUnitName())

        int offset = spec.userSelection.offset
        int length = spec.userSelection.length
        RefactoringStatus status = new RefactoringStatus()
        printf 'Attempting to extract new method from [%d,%d):%n %s%n',
            offset, offset + length, String.valueOf(unit.contents).substring(offset, offset + length)

        refactoring = new ExtractGroovyMethodRefactoring(unit, offset, length, status)
        refactoring.setPreferences(TestPrefInitializer.initializePreferences(spec.properties as HashMap, unit.javaProject))

        assert status.severity == RefactoringStatus.OK : "Bad refactoring status on init: $status"
    }

    private RefactoringStatus checkInitialCondition() {
        refactoring.checkInitialConditions(new NullProgressMonitor())
    }

    private void simulateUserInput() {
        switch (spec.properties['modifier']) {
        case 'def':
        case 'public':
            refactoring.modifier = Flags.AccPublic
            break
        case 'private':
            refactoring.modifier = Flags.AccPrivate
            break
        case 'protected':
            refactoring.modifier = Flags.AccProtected
            break
        default:
            refactoring.modifier = Flags.AccDefault
        }

        refactoring.newMethodname = spec.properties['newMethodName']

        String moveSettings = spec.properties['moveVariable']
        if (moveSettings != null && moveSettings.trim().length() > 0) {
            boolean upEvent = false
            int sortOfMoveCharPosition = moveSettings.indexOf('+')
            if (sortOfMoveCharPosition == -1) {
                upEvent = true
                sortOfMoveCharPosition = moveSettings.indexOf('-')
            }
            String varName = moveSettings.substring(0, sortOfMoveCharPosition)
            int numberOfMoves = Integer.valueOf(moveSettings.substring(sortOfMoveCharPosition + 1, moveSettings.length()))
            refactoring.setMoveParameter(varName, upEvent, numberOfMoves)
        }

        String variableToRename = spec.properties['variableToRename']
        if (variableToRename != null && variableToRename.trim().length() > 0) {
            Map<String, String> variablesToRename = [:]
            for (renameMapping in variableToRename.split(';')) {
                String[] singleRenames = renameMapping.split(':')
                if (singleRenames.length == 2) {
                    variablesToRename.put(singleRenames[0], singleRenames[1])
                }
            }
            refactoring.parameterRename = variablesToRename
        }
    }

    private RefactoringStatus checkFinalCondition() {
        refactoring.checkFinalConditions(new NullProgressMonitor())
    }

    private boolean analyseRefactoringStatus(RefactoringStatus state) {
        RefactoringStatusEntry[] entries = state.entries
        if (spec.shouldFail) {
            assert entries.length > 0 : "Should fail: ${spec.properties['failMessage']}"
        }
        for (int i = 0; i < entries.length; i += 1) {
            RefactoringStatusEntry entry = entries[i]
            if ((entry.isError() || entry.isFatalError()) && !spec.shouldFail) {
                // error was not expected
                assert false : 'condition check failed: ' + entry.message
            } else {
                // check the error message
                if (spec.shouldFail && spec.properties['failMessage'] != null ) {
                    assert entry.message == spec.properties['failMessage']
                }
            }
            if (entry.isFatalError()) {
                return false
            }
        }
        return true
    }

    private Change createChange() {
        refactoring.createChange(new NullProgressMonitor())
    }
}
