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

import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.refactoring.IJavaRefactorings
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.junit.Ignore
import org.junit.Test

final class RenameMethodTests extends RefactoringTestSuite {

    @Override
    protected String getRefactoringPath() {
        'RenameMethod/'
    }

    private void runTest(String typeName, String methodName, String newMethodName, List<String> signatures, boolean updateReferences, boolean createDelegate) {
        ICompilationUnit cu = createCUfromTestFile(packageP, 'A')
        IType classA = getType(cu, typeName)
        if (classA == null) {
            classA = cu.getJavaProject().findType(typeName)
        }
        IMethod method = classA.getMethod(methodName, signatures as String[])
        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD)
        descriptor.setUpdateReferences(updateReferences)
        descriptor.setJavaElement(method)
        descriptor.setNewName(newMethodName)
        descriptor.setKeepOriginal(createDelegate)
        descriptor.setDeprecateDelegate(true)

        assert performRefactoring(descriptor) == null : 'was supposed to pass'
        assertEqualLines('invalid renaming', getFileContents(getOutputTestFileName('A')), cu.getSource())

        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '! anythingToRedo'
        //assert Refactoring.getUndoManager().getRefactoringLog().size() == 1 : '1 to undo'

        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())
        assertEqualLines('invalid undo', getFileContents(getInputTestFileName('A')), cu.getSource())

        assert !RefactoringCore.getUndoManager().anythingToUndo() : '! anythingToUndo'
        assert RefactoringCore.getUndoManager().anythingToRedo() : 'anythingToRedo'
        //assert Refactoring.getUndoManager().getRedoStack().size() == 1 : '1 to redo'

        RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor())
        assertEqualLines('invalid redo', getFileContents(getOutputTestFileName('A')), cu.getSource())
    }

    // NOTE: Test method names are matched to test case data stored externally

    @Test
    void test1() {
        runTest('A', 'm', 'k', [], true, false)
    }

    @Test
    void test2() {
        runTest('A', 'm', 'k', [], true, false)
    }

    @Test
    void test3() {
        runTest('A', 'm', 'k', ['QD;'], true, false)
    }

    @Test
    void test4() {
        runTest('A', 'm', 'k', [], true, false)
    }

    @Test
    void test5() {
        runTest('A', 'm', 'k', [], true, false)
    }

    @Test
    void test6() {
        runTest('A', 'm', 'k', [], true, false)
    }

    @Test
    void test7() {
        runTest('A', 'm', 'k', [], true, false)
    }

    @Test
    void test8() {
        runTest('B', 'm', 'k', [], true, false)
    }

    @Test
    void test9() {
        runTest('A', 'm', 'k', [], true, false)
    }

    @Test
    void test10() {
        createCU(packageP.parent.createPackageFragment('o', true, null), 'Other.java', 'package o;\npublic class Other { public static int FOO() { return 0; }\n }')

        runTest('o.Other', 'FOO', 'BAR', [], true, false)
    }

    @Test
    void testAnonOverrides() {
        // rename I.run() to I.sam() and anon. inners in A should change
        runTest('I', 'run', 'sam', [], true, false);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/389
    void testEnumOverrides() {
        // rename A.getFoo() to A.foo() and enum constant overrides should change
        runTest('A', 'getFoo', 'foo', [], true, false)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/390
    void testEnumOverrides2() {
        // rename A.getFoo() to A.foo() and enum constant overrides should change
        runTest('A', 'getFoo', 'foo', [], true, false)
    }

    @Test
    void testInitializer1() {
        runTest('A', 'm', 'k', [], true, false)
    }

    @Test
    void testInitializer2() {
        runTest('A', 'm', 'k', [], true, false)
    }

    @Test
    void testInitializer3() {
        runTest('A', 'm', 'k', [], true, false)
    }

    // org.codehaus.jdt.groovy.internal.compiler.ast.GroovyTypeDeclaration#parseMethods was set to no-op and so no bodies avail for refactor

    @Test @Ignore('@see org.eclipse.jdt.internal.corext.refactoring.rename.RenameNonVirtualMethodProcessor#addDeclarationUpdate')
    void testDelegate1() {
        // rename static method 'm' to 'k' and add deprecated delegate
        runTest('A', 'm', 'k', [], true, true)
    }

    @Test @Ignore('@see org.eclipse.jdt.internal.corext.refactoring.rename.RenameVirtualMethodProcessor')
    void testDelegate2() {
        // rename non-static method 'm' to 'k' and add deprecated delegate
        runTest('A', 'm', 'k', [], true, true)
    }

    @Test
    void testOverload1() {
        runTest('A', 'm', 'k', ['Ljava.lang.Object;'], true, false)
    }

    @Test
    void testOverload2() {
        runTest('A', 'm', 'k', ['Ljava.lang.Object;'], true, false)
    }

    @Test
    void testOverload3() {
        runTest('A', 'm', 'k', ['Ljava.lang.Object;'], true, false)
    }
}
