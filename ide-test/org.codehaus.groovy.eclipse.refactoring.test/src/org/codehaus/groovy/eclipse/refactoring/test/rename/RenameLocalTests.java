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

import org.codehaus.groovy.eclipse.refactoring.core.rename.JavaRefactoringDispatcher;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestCase;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public final class RenameLocalTests extends RefactoringTestCase {

    public static junit.framework.Test suite() {
        return new RefactoringTestSetup(new junit.framework.TestSuite(RenameLocalTests.class));
    }

    public static junit.framework.Test setUpTest(junit.framework.Test test) {
        return new RefactoringTestSetup(test);
    }

    public RenameLocalTests(String name) {
        super(name);
    }

    protected String getRefactoringPath() {
        return "RenameLocal/";
    }

    protected void setUp() throws Exception {
        super.setUp();
        fIsPreDeltaTest= true;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void helper(String initial, String expected) throws Exception {
        RefactoringStatus result = helper("A", initial, expected, "NEW", initial.indexOf("XXX"), false);
        assertTrue("Result is not OK: " + result, result.isOK());
    }

    private void helperExpectWarning(String initial, String expected) throws Exception {
        RefactoringStatus result = helper("A", initial, expected, "NEW", initial.indexOf("XXX"), true);
        assertTrue("Expected warning, but got " + result, result.hasWarning() && !result.hasError());
    }


    private RefactoringStatus helper(String unitNameNoExtension, String initial,
            String expected, String newVariableName, int refactorLocation, boolean expectingWarning)
            throws Exception {
        ICompilationUnit cu = createCU(getPackageP(), "A.groovy", initial);
        try {
            ILocalVariable toRename = (ILocalVariable) cu.codeSelect(refactorLocation, 1)[0];
            JavaRefactoringDispatcher dispatcher = new JavaRefactoringDispatcher(toRename);
            dispatcher.setNewName(newVariableName);
            RenameJavaElementDescriptor descriptor = dispatcher.createDescriptorForLocalVariable();
            RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor);

            RefactoringStatus result = performRefactoring(refactoring, !expectingWarning);
            assertTrue("was supposed to pass", result==null || result.isOK() || result.hasWarning());
            assertEqualLines("invalid renaming",
                    expected, cu.getSource());

            assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                    .anythingToUndo());
            assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                    .anythingToRedo());

            RefactoringCore.getUndoManager().performUndo(null,
                    new NullProgressMonitor());
            assertEqualLines("invalid undo",
                    initial, cu.getSource());

            assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
                    .anythingToUndo());
            assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
                    .anythingToRedo());

            RefactoringCore.getUndoManager().performRedo(null,
                    new NullProgressMonitor());
            assertEqualLines("invalid redo",
                    expected, cu.getSource());

            return result != null ? result : new RefactoringStatus();
        } finally {
            performDummySearch();
            cu.delete(true, null);
        }
    }

    public void test0() throws Exception {
        helper("def XXX = 9", "def NEW = 9");
    }
    public void test1() throws Exception {
        helper("def XXX = XXX", "def NEW = NEW");
    }
    public void test2() throws Exception {
        helper("def XXX = 9\nXXX = XXX", "def NEW = 9\nNEW = NEW");
    }
    public void test3() throws Exception {
        helper("def XXX = 9\ndef c = { XXX }", "def NEW = 9\ndef c = { NEW }");
    }
    public void test4() throws Exception {
        helper("def x(XXX) { XXX }", "def x(NEW) { NEW }");
    }
    public void test5() throws Exception {
        helper("def x(XXX) { XXX.something }", "def x(NEW) { NEW.something }");
    }
    public void test6() throws Exception {
        helper("def x(XXX) { \"${XXX}\" }", "def x(NEW) { \"${NEW}\" }");
    }
    public void test6a() throws Exception {
        helper("def x(XXX) { \"${XXX.toString()}\" }", "def x(NEW) { \"${NEW.toString()}\" }");
    }
    public void test6c() throws Exception {
        helper("def x(XXX) { \"$XXX\" }", "def x(NEW) { \"$NEW\" }");
    }
    public void test7() throws Exception {
        helper("def x(XXX) { XXX.XXX }", "def x(NEW) { NEW.XXX }");
    }
    public void test8() throws Exception {
        helper("def x(XXX) { this.XXX }", "def x(NEW) { this.XXX }");
    }
    public void test9() throws Exception {
        helper("class A { def x(XXX) { XXX\nthis.XXX } \nint XXX}", "class A { def x(NEW) { NEW\nthis.XXX } \nint XXX}");
    }

    public void testWarning0() throws Exception {
        helperExpectWarning("def XXX\nwhile(XXX) { XXX\n def NEW \n NEW }", "def NEW\nwhile(NEW) { NEW\n def NEW \n NEW }");
    }
    public void testWarning1() throws Exception {
        helperExpectWarning("class A { def NEW\ndef x(XXX) { XXX\nthis.XXX } }", "class A { def NEW\ndef x(NEW) { NEW\nthis.XXX } }");
    }
    public void testWarning2() throws Exception {
        helperExpectWarning("def XXX\ndef y = { NEW -> XXX }", "def NEW\ndef y = { NEW -> NEW }");
    }
    public void testWarning3() throws Exception {
        helperExpectWarning("def NEW\nwhile(NEW) { NEW\n def XXX \n XXX\nNEW }", "def NEW\nwhile(NEW) { NEW\n def NEW \n NEW\nNEW }");
    }
}
