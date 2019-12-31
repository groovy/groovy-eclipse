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
package org.codehaus.groovy.eclipse.refactoring.test.rename

import static org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor

import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.codehaus.jdt.groovy.model.JavaCoreUtil
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.refactoring.IJavaRefactorings
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.junit.Ignore
import org.junit.Test

final class RenameMethodTests extends RefactoringTestSuite {

    @Override
    protected String getRefactoringPath() {
        'RenameMethod/'
    }

    private void runTest(String typeName, String methodName, String newMethodName, List<String> paramSignatures = Collections.EMPTY_LIST,
            boolean updateReferences = true, boolean createDelegate = false, boolean deprecateDelegate = true) {
        ICompilationUnit unit = createCUfromTestFile(packageP, 'A')
        IType type = getType(unit, typeName)
        if (type == null) {
            type = JavaCoreUtil.findType(typeName, unit)
        }
        IMethod method = type.getMethod(methodName, paramSignatures as String[])

        RenameJavaElementDescriptor descriptor = createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD)
        descriptor.deprecateDelegate = deprecateDelegate
        descriptor.javaElement = method
        descriptor.keepOriginal = createDelegate
        descriptor.newName = newMethodName
        descriptor.updateReferences = updateReferences

        assert performRefactoring(descriptor) == null : 'was supposed to pass'
        assertEqualLines('invalid renaming', getFileContents(getOutputTestFileName('A')), unit.source)

        assert RefactoringCore.getUndoManager().anythingToUndo() : 'anythingToUndo'
        assert !RefactoringCore.getUndoManager().anythingToRedo() : '! anythingToRedo'

        RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor())
        assertEqualLines('invalid undo', getFileContents(getInputTestFileName('A')), unit.source)

        assert !RefactoringCore.getUndoManager().anythingToUndo() : '! anythingToUndo'
        assert RefactoringCore.getUndoManager().anythingToRedo() : 'anythingToRedo'

        RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor())
        assertEqualLines('invalid redo', getFileContents(getOutputTestFileName('A')), unit.source)
    }

    // NOTE: Test method names are matched to test case data stored externally

    @Test
    void test1() {
        runTest('A', 'm', 'k')
    }

    @Test
    void test2() {
        runTest('A', 'm', 'k')
    }

    @Test
    void test3() {
        runTest('A', 'm', 'k', ['QD;'])
    }

    @Test
    void test4() {
        runTest('A', 'm', 'k')
    }

    @Test
    void test5() {
        runTest('A', 'm', 'k')
    }

    @Test
    void test6() {
        runTest('A', 'm', 'k')
    }

    @Test
    void test7() {
        runTest('A', 'm', 'k')
    }

    @Test
    void test8() {
        runTest('B', 'm', 'k')
    }

    @Test
    void test9() {
        runTest('A', 'm', 'k')
    }

    @Test
    void testStaticImport() {
        createCU(packageP.parent.createPackageFragment('o', true, null), 'Other.java', '''\
        |package o;
        |
        |public class Other {
        |  public static int FOO() {
        |    return 0;
        |  }
        |}
        |'''.stripMargin())

        runTest('o.Other', 'FOO', 'BAR')
    }

    @Test
    void testAnonOverrides() {
        // rename I.run() to sam() and anonymous inners in A should change
        runTest('I', 'run', 'sam')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/389
    void testEnumOverrides() {
        // rename A.getFoo() to foo() and enum constant overrides should change
        runTest('A', 'getFoo', 'foo')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/390
    void testEnumOverrides2() {
        // rename A.getFoo() to foo() and enum constant overrides should change
        runTest('A', 'getFoo', 'foo')
    }

    @Test @Ignore('Need to get ref to method in enum const') // https://github.com/groovy/groovy-eclipse/issues/389
    void testEnumOverrides3() {
        // rename A.ONE.getFoo() to foo() and enum constant overrides should change
        runTest('A$1', 'getFoo', 'foo')
    }

    @Test
    void testInitializer1() {
        runTest('A', 'm', 'k')
    }

    @Test
    void testInitializer2() {
        runTest('A', 'm', 'k')
    }

    @Test
    void testInitializer3() {
        runTest('A', 'm', 'k')
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
