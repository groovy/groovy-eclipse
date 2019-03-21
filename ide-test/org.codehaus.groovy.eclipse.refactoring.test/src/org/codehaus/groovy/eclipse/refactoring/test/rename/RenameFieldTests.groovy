/*
 * Copyright 2009-2017 the original author or authors.
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

import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.codehaus.groovy.eclipse.refactoring.test.internal.ParticipantTesting
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IAnnotatable
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IField
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.refactoring.IJavaRefactorings
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor
import org.eclipse.jdt.internal.corext.util.JdtFlags
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.eclipse.ltk.core.refactoring.participants.RenameArguments
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring
import org.junit.Test

final class RenameFieldTests extends RefactoringTestSuite {

    @Override
    protected String getRefactoringPath() {
        'RenameField/'
    }

    private void helper2_0(String typeName, String fieldName, String newFieldName, boolean updateReferences, boolean createDelegates, boolean renameGetter, boolean renameSetter, boolean performOnError, boolean updateTextual) {
        ICompilationUnit cu = createCUfromTestFile(packageP, 'A')
        IType classA = getType(cu, typeName)
        if (classA == null) {
            classA = cu.getJavaProject().findType(typeName)
        }
        IField field = classA.getField(fieldName)
        boolean isEnum = JdtFlags.isEnum(field)
        String id = isEnum ? IJavaRefactorings.RENAME_ENUM_CONSTANT : IJavaRefactorings.RENAME_FIELD
        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(id)
        descriptor.setUpdateReferences(updateReferences)
        descriptor.setJavaElement(field)
        descriptor.setNewName(newFieldName)
        if (!isEnum) {
            descriptor.setRenameGetters(renameGetter)
            descriptor.setRenameSetters(renameSetter)
            descriptor.setKeepOriginal(createDelegates)
            descriptor.setUpdateTextualOccurrences(updateTextual)
            descriptor.setDeprecateDelegate(true)
        }
        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor)
        RenameFieldProcessor processor = (RenameFieldProcessor) refactoring.getProcessor()

        List<IAnnotatable> elements = [field]
        List<RenameArguments> args = [new RenameArguments(newFieldName, updateReferences)]
        if (renameGetter) {
            elements.add(processor.getGetter())
            args.add(new RenameArguments(processor.getNewGetterName(), updateReferences))
        }
        if (renameSetter) {
            elements.add(processor.getSetter())
            args.add(new RenameArguments(processor.getNewSetterName(), updateReferences))
        }
        String[] renameHandles = ParticipantTesting.createHandles(elements.toArray())

        RefactoringStatus result = performRefactoring(refactoring, performOnError)
        assert result==null || result.isOK() : 'was supposed to pass'
        assertEqualLines('invalid renaming', getFileContents(getOutputTestFileName('A')), cu.getSource())

        ParticipantTesting.testRename(renameHandles, args.toArray(new RenameArguments[args.size()]))

        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '! anythingToRedo'

        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())
        assertEqualLines('invalid undo', getFileContents(getInputTestFileName('A')), cu.getSource())

        assert !RefactoringCore.getUndoManager().anythingToUndo() : '! anythingToUndo'
        assert RefactoringCore.getUndoManager().anythingToRedo() : 'anythingToRedo'

        RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor())
        assertEqualLines('invalid redo', getFileContents(getOutputTestFileName('A')), cu.getSource())
    }

    private void helper2(boolean updateReferences) {
        helper2_0('A', 'f', 'g', updateReferences, false, false, false, false, false)
    }

    private void helperPerformOnError(boolean updateReferences) {
        helper2_0('A', 'f', 'g', updateReferences, false, false, false, true, false)
    }

    private void helperScript() {
        helper2_0('B', 'f', 'g', true, false, false, false, false, false)
    }

    private void helper2() {
        helper2(true)
    }

    // NOTE: Test method names are matched to test case data stored externally

    @Test
    void testInitializer1() {
        helper2()
    }

    @Test
    void testInitializer2() {
        helper2()
    }

    @Test
    void testInitializer3() {
        helper2()
    }

    @Test
    void test1() {
        helper2()
    }

    @Test
    void test2() {
        helper2()
    }

    @Test
    void test3() {
        helper2()
    }

    @Test
    void test4() {
        helper2()
    }

    @Test
    void test5() {
        helperPerformOnError(true)
    }

    @Test
    void test6() {
        helper2()
    }

    @Test
    void test7() {
        helperPerformOnError(true)
    }

    @Test
    void test8() {
        helper2()
    }

    @Test
    void test9() {
        helper2()
    }

    @Test
    void test10() {
        helper2()
    }

    @Test
    void test11() {
        createCU(packageP.parent.createPackageFragment('o', true, null), 'Other.java', 'package o;\npublic class Other { public static int FOO;\n }')
        helper2_0('o.Other', 'FOO', 'BAR', true, false, false, false, false, false)
    }

    @Test
    void testScript1() {
        helperScript()
    }

    @Test
    void testScript2() {
        helperScript()
    }

    @Test
    void test12() {
        helper2_0('A', 'f', 'g', true, false, false, false, false, true)
    }

    @Test
    void test13() {
        helper2_0('A', 'f', 'g', true, false, false, false, false, true)
    }

    @Test
    void test14() {
        helper2_0('MyBean', 'foo', 'baz', true, false, false, false, false, true)
    }
}
