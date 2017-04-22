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
package org.codehaus.groovy.eclipse.refactoring.test.rename;

import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestCase;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.RefactoringCore;

public final class RenameMethodTests extends RefactoringTestCase {

    public static junit.framework.Test suite() {
        return new RefactoringTestSetup(new junit.framework.TestSuite(RenameMethodTests.class));
    }

    public static junit.framework.Test setUpTest(junit.framework.Test test) {
        return new RefactoringTestSetup(test);
    }

    public RenameMethodTests(String name) {
        super(name);
    }

    @Override
    protected String getRefactoringPath() {
        return "RenameMethod/";
    }

    private void runTest(String typeName, String methodName, String newMethodName, String[] signatures, boolean updateReferences, boolean createDelegate) throws Exception {
        ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
        try {
            IType classA = getType(cu, typeName);
            if (classA == null) {
                classA = cu.getJavaProject().findType(typeName);
            }
            IMethod method = classA.getMethod(methodName, signatures);
            RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory
                    .createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD);
            descriptor.setUpdateReferences(updateReferences);
            descriptor.setJavaElement(method);
            descriptor.setNewName(newMethodName);
            descriptor.setKeepOriginal(createDelegate);
            descriptor.setDeprecateDelegate(true);

            assertEquals("was supposed to pass", null, performRefactoring(descriptor));
            assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());

            assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
            assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());
            // assertEquals("1 to undo", 1,
            // Refactoring.getUndoManager().getRefactoringLog().size());

            RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
            assertEqualLines("invalid undo", getFileContents(getInputTestFileName("A")), cu.getSource());

            assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
            assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());
            // assertEquals("1 to redo", 1,
            // Refactoring.getUndoManager().getRedoStack().size());

            RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
            assertEqualLines("invalid redo", getFileContents(getOutputTestFileName("A")), cu.getSource());
        } finally {
            performDummySearch();
            cu.delete(true, null);
        }
    }

    //

    public void test1() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    public void test2() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    public void test3() throws Exception {
        runTest("A", "m", "k", new String[] {"QD;"}, true, false);
    }

    public void test4() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    public void test5() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    public void test6() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    public void test7() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    public void test8() throws Exception {
        runTest("B", "m", "k", new String[] {}, true, false);
    }

    public void test9() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    public void test10() throws Exception {
        createCU(((IPackageFragmentRoot) getPackageP().getParent()).createPackageFragment("o", true, null),
                "Other.java", "package o;\npublic class Other { public static int FOO() { return 0; }\n }");

        runTest("o.Other", "FOO", "BAR", new String[] {}, true, false);
    }

    // GRECLIPSE-1538
    public void test11() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21) {
            return;
        }
        runTest("A", "getFoo", "foo", new String[] {}, true, false);
    }

    public void testInitializer1() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    public void testInitializer2() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    public void testInitializer3() throws Exception {
        runTest("A", "m", "k", new String[] {}, true, false);
    }

    // org.codehaus.jdt.groovy.internal.compiler.ast.GroovyTypeDeclaration#parseMethods was set to no-op and so no bodies avail for refactor

    /** @see org.eclipse.jdt.internal.corext.refactoring.rename.RenameNonVirtualMethodProcessor#addDeclarationUpdate */
    public void _testDelegate1() throws Exception {
        // rename static method "m" to "k" and add deprecated delegate
        runTest("A", "m", "k", new String[] {}, true, true);
    }

    /** @see org.eclipse.jdt.internal.corext.refactoring.rename.RenameVirtualMethodProcessor */
    public void _testDelegate2() throws Exception {
        // rename non-static method "m" to "k" and add deprecated delegate
        runTest("A", "m", "k", new String[] {}, true, true);
    }

    public void testOverload1() throws Exception {
        runTest("A", "m", "k", new String[] {"Ljava.lang.Object;"}, true, false);
    }

    public void testOverload2() throws Exception {
        runTest("A", "m", "k", new String[] {"Ljava.lang.Object;"}, true, false);
    }

    public void testOverload3() throws Exception {
        runTest("A", "m", "k", new String[] {"Ljava.lang.Object;"}, true, false);
    }
}
