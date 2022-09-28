/*
 * Copyright 2009-2022 the original author or authors.
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
import static org.eclipse.jdt.core.JavaCore.*
import static org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor

import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSuite
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.refactoring.IJavaRefactorings
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.junit.Test

final class RenameMethodTests extends RefactoringTestSuite {

    @Override
    protected String getRefactoringPath() {
        'RenameMethod/'
    }

    private RefactoringStatus runTest(String typeName, String methodName, String newMethodName, List<String> paramSignatures = [],
            boolean updateReferences = true, boolean createDelegate = false, boolean deprecateDelegate = true) {
        def unit = createCUfromTestFile(packageP, 'A')
        def type = getType(unit, typeName) ?: findType(typeName, unit)
        def method = type.getMethod(methodName, paramSignatures as String[])

        def descriptor = createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD)
        descriptor.deprecateDelegate = deprecateDelegate
        descriptor.javaElement = method
        descriptor.keepOriginal = createDelegate
        descriptor.newName = newMethodName
        descriptor.updateReferences = updateReferences

        def status = performRefactoring(createRefactoring(descriptor), true)
        assertEqualLines('invalid change', getFileContents(getOutputTestFileName('A')), unit.source)
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
    void test1() {
        def status = runTest('A', 'm', 'k')
        assert status.entries[0].message.startsWith('Found potential matches.')
    }

    @Test
    void test2() {
        def status = runTest('A', 'm', 'k')
        assert status.entries[0].message.startsWith('Found potential matches.')
    }

    @Test
    void test3() {
        def status = runTest('A', 'm', 'k', ['QD;'])
        assert status.entries[0].message.startsWith('Found potential matches.')
    }

    @Test
    void test4() {
        def status = runTest('A', 'm', 'k')
        assert status.isOK() : 'rename failed'
    }

    @Test
    void test5() {
        def status = runTest('A', 'm', 'k')
        assert status.isOK() : 'rename failed'
    }

    @Test
    void test6() {
        def status = runTest('A', 'm', 'k')
        assert status.isOK() : 'rename failed'
    }

    @Test
    void test7() {
        def status = runTest('A', 'm', 'k')
        assert status.isOK() : 'rename failed'
    }

    @Test
    void test8() {
        def status = runTest('B', 'm', 'k')
        assert status.isOK() : 'rename failed'
    }

    @Test
    void test9() {
        def status = runTest('A', 'm', 'k')
        assert status.isOK() : 'rename failed'
    }

    @Test
    void test10() {
        def status = runTest('A', 'setFoo', 'setFooBar', ['QString;'])
        assert status.isOK() : 'rename failed'
    }

    @Test
    void test11() {
        def status = runTest('A', 'setFoo', 'setFooBar', ['QInteger;'])
        assert status.entries[0].message.startsWith('Found potential matches.')
    }

    @Test
    void test12() {
        def status = runTest('A', 'setFoo', 'setFooBar', ['QInteger;'])
        assert status.entries[0].message.startsWith('Found potential matches.')
    }

    @Test
    void test13() {
        def status = runTest('A', 'getFoo', 'getFooBar')
        assert status.isOK() : 'rename failed' // property is not renamed, so no potential matches
    }

    @Test
    void test14() {
        def status = runTest('A', 'getFoo', 'getFooBar')
        assert status.isOK() : 'rename failed' // property is not renamed, so no potential matches
    }

    @Test
    void test15() {
        def status = runTest('A', 'getFoo', 'getBar')
        assert status.isOK()
    }

    @Test
    void testStaticImport() {
        createCU(root.createPackageFragment('o', true, null), 'Other.java', '''\
        |package o;
        |
        |public class Other {
        |  public static int FOO() {
        |    return 0;
        |  }
        |}
        |'''.stripMargin())

        def status = runTest('o.Other', 'FOO', 'BAR')
        assert status.entries[0].message.startsWith('This name is discouraged.')
    }

    @Test
    void testStaticImportAlias() {
        def status = runTest('A', 'm', 'k', ['[Ljava.lang.Object;'])
        assert status.entries[0].message.startsWith('Found potential matches.')
    }

    @Test
    void testAnonOverrides() {
        // rename I.run() to sam() and anonymous inners in A should change
        def status = runTest('I', 'run', 'sam')
        assert status.isOK() : 'rename failed'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/389
    void testEnumOverrides() {
        // rename A.getFoo() to foo() and enum constant overrides should change
        def status = runTest('A', 'getFoo', 'foo')
        assert status.isOK() : 'rename failed'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/390
    void testEnumOverrides2() {
        // rename A.getFoo() to foo() and enum constant overrides should change
        def status = runTest('A', 'getFoo', 'foo')
        assert status.isOK() : 'rename failed'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/389
    void testEnumOverrides3() {
        // rename A.ONE.getFoo() to foo() and enum constant overrides should change
        def status = runTest('A$1', 'getFoo', 'foo')
        assert status.isOK() : 'rename failed'
    }

    @Test
    void testInitializer1() {
        def status = runTest('A', 'm', 'k')
        assert status.isOK() : 'rename failed'
    }

    @Test
    void testInitializer2() {
        def status = runTest('A', 'm', 'k')
        assert status.isOK() : 'rename failed'
    }

    @Test
    void testInitializer3() {
        def status = runTest('A', 'm', 'k')
        assert status.isOK() : 'rename failed'
    }

    // org.codehaus.jdt.groovy.internal.compiler.ast.GroovyTypeDeclaration#parseMethods was set to no-op and so no bodies avail for refactor

    @Test
    void testDelegate1() {
        // rename static method 'm' to 'k' and add deprecated delegate
        def status = runTest('A', 'm', 'k', [], true, true)
        assert status.isOK() : 'rename failed'
    }

    @Test
    void testDelegate2() {
        // rename non-static method 'm' to 'k' and add deprecated delegate
        def status = runTest('A', 'm', 'k', [], true, true)
        assert status.isOK() : 'rename failed'
    }

    @Test
    void testOverload1() {
        // rename single-parameter method 'm' to 'k'
        def status = runTest('A', 'm', 'k', ['Ljava.lang.Object;'])
        assert status.isOK() : 'rename failed'
    }

    @Test
    void testOverload2() {
        // rename single-parameter method 'm' to 'k'
        def status = runTest('A', 'm', 'k', ['Ljava.lang.Object;'])
        assert status.isOK() : 'rename failed'
    }

    @Test
    void testOverload3() {
        // rename single-parameter method 'm' to 'k'
        def status = runTest('A', 'm', 'k', ['Ljava.lang.Object;'])
        assert status.isOK() : 'rename failed'
    }

    @Test
    void testOverload4() {
        project.options = project.getOptions(true).tap {
            put(COMPILER_CODEGEN_TARGET_PLATFORM, '1.8')
            put(COMPILER_COMPLIANCE, '1.8')
            put(COMPILER_SOURCE, '1.8')
        }

        def java = getInputTestFileName('B').replace('.groovy', '.java')
        def unit = createCU(packageP, 'B.java', getFileContents(java))

        // rename single-parameter method 'm' to 'x'
        def status = runTest('A', 'm', 'x', ['Ljava.lang.Object;'])
        assert status.entries[0].message.startsWith('Found potential matches.')

        java = getOutputTestFileName('B').replace('.groovy', '.java')
        assertEqualLines(getFileContents(java), unit.source)
    }
}
