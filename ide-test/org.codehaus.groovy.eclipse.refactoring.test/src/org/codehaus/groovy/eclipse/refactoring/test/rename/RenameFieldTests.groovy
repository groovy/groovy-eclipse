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

import static org.codehaus.jdt.groovy.model.JavaCoreUtil.findType
import static org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.codehaus.groovy.eclipse.refactoring.test.internal.ParticipantTesting
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.refactoring.IJavaRefactorings
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor
import org.eclipse.jdt.internal.corext.util.JdtFlags
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.eclipse.ltk.core.refactoring.participants.RenameArguments
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring
import org.junit.Test

@CompileStatic
final class RenameFieldTests extends RefactoringTestSuite {

    @Override
    protected String getRefactoringPath() {
        'RenameField/'
    }

    private RefactoringStatus runTest(Map<String, Boolean> flags = Collections.EMPTY_MAP, String typeName, String fieldName, String newFieldName) {
        def unit = createCUfromTestFile(packageP, 'A')
        def type = getType(unit, typeName) ?: findType(typeName, unit)
        def field = type.getField(fieldName)
        boolean isEnum = JdtFlags.isEnum(field)
        boolean renameGetters = flags.getOrDefault('renameGetters', false)
        boolean renameSetters = flags.getOrDefault('renameSetters', false)
        boolean updateReferences = flags.getOrDefault('updateReferences', true)

        RenameJavaElementDescriptor descriptor = createRenameJavaElementDescriptor(
            isEnum ? IJavaRefactorings.RENAME_ENUM_CONSTANT : IJavaRefactorings.RENAME_FIELD)
        descriptor.javaElement = field
        descriptor.newName = newFieldName
        descriptor.updateReferences = updateReferences
        if (!isEnum) {
            descriptor.deprecateDelegate = flags.getOrDefault('deprecateDelegate', false)
            descriptor.keepOriginal = flags.getOrDefault('keepOriginal', false)
            descriptor.renameGetters = renameGetters
            descriptor.renameSetters = renameSetters
            descriptor.updateTextualOccurrences = flags.getOrDefault('updateTextualOccurrences', false)
        }

        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor)
        RenameFieldProcessor processor = (RenameFieldProcessor) refactoring.processor

        def elements = [field as IJavaElement]
        def arguments = [new RenameArguments(newFieldName, updateReferences)]
        if (renameGetters) {
            elements.add(processor.getter)
            arguments.add(new RenameArguments(processor.newGetterName, updateReferences))
        }
        if (renameSetters) {
            elements.add(processor.setter)
            arguments.add(new RenameArguments(processor.newSetterName, updateReferences))
        }
        String[] renameHandles = ParticipantTesting.createHandles(elements.toArray())

        RefactoringStatus status = performRefactoring(refactoring, flags.getOrDefault('performOnError', false))
        assertEqualLines('invalid change', getFileContents(getOutputTestFileName('A')), unit.source)
        ParticipantTesting.testRename(renameHandles, arguments as RenameArguments[])
        RefactoringCore.getUndoManager().with { undoManager ->
            assert undoManager.anythingToUndo() : 'anythingToUndo'
            assert !undoManager.anythingToRedo() : '! anythingToRedo'

            undoManager.performUndo(null, new NullProgressMonitor())
            assertEqualLines('invalid undo', getFileContents(getInputTestFileName('A')), unit.source)

            assert !undoManager.anythingToUndo() : '! anythingToUndo'
            assert undoManager.anythingToRedo() : 'anythingToRedo'

            undoManager.performRedo(null, new NullProgressMonitor())
            assertEqualLines('invalid redo', getFileContents(getOutputTestFileName('A')), unit.source)
        }

        return status
    }

    // NOTE: Test method names are matched to test case data stored externally

    @Test
    void testInitializer1() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void testInitializer2() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void testInitializer3() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test1() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test2() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test3() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test4() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test5() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test6() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test7() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test8() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test9() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test10() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test11() {
        createCU(root.createPackageFragment('o', true, null), 'Other.java', '''\
            |package o;
            |public class Other {
            |  public static int FOO;
            |}
            |'''.stripMargin())

        def status = runTest('o.Other', 'FOO', 'BAR')
        assert status.isOK()
    }

    @Test
    void testScript1() {
        def status = runTest('B', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void testScript2() {
        def status = runTest('B', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test12() {
        def status = runTest('A', 'f', 'g', updateTextualOccurrences: true)
        assert status.isOK()
    }

    @Test
    void test13() {
        def status = runTest('A', 'f', 'g')
        assert status.isOK()
    }

    @Test
    void test14() {
        def status = runTest('MyBean', 'foo', 'fooBar')
        assert status.isOK()
    }

    @Test
    void test15() {
        def status = runTest('MyBean', 'foo', 'fooBar')
        assert status.isOK() // TODO: partial matches should result in warning
    }
}
