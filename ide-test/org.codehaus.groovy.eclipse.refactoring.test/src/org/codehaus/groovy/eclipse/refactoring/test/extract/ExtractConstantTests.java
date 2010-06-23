package org.codehaus.groovy.eclipse.refactoring.test.extract;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyConstantRefactoring;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTest;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Note that all test files use tabs instead of spaces
 * 
 * @author andrew
 * @created May 13, 2010
 */
public class ExtractConstantTests extends RefactoringTest {
    private static final String FOO_BAR = "Foo + Bar";

    private static final String FOO_BAR_FRAX = "Foo+Bar+A.frax()";

    private static final Class<ExtractConstantTests> clazz = ExtractConstantTests.class;

    private static final String REFACTORING_PATH = "ExtractConstant/";

    public ExtractConstantTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new RefactoringTestSetup(new TestSuite(clazz));
    }

    public static Test setUpTest(Test test) {
        return new RefactoringTestSetup(test);
    }

    @Override
    protected String getRefactoringPath() {
        return REFACTORING_PATH;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fIsPreDeltaTest = true;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test1() throws Exception {
        helper(indexOf(FOO_BAR), FOO_BAR.length(), true, false);
    }

    public void test2() throws Exception {
        helper(indexOf(FOO_BAR), FOO_BAR.length(), true, false);
    }

    public void test3() throws Exception {
        helper(indexOf(FOO_BAR_FRAX), FOO_BAR_FRAX.length(), true, false);
    }

    public void test4() throws Exception {
        helper(indexOf(FOO_BAR_FRAX), FOO_BAR_FRAX.length(), true, false);
    }

    public void test5a() throws Exception {
        helper(indexOf(FOO_BAR_FRAX), FOO_BAR_FRAX.length(), true, false);
    }

    public void test6a() throws Exception {
        helper(indexOf(FOO_BAR_FRAX), FOO_BAR_FRAX.length(), true, false);
    }

    public void testNoReplaceOccurrences1() throws Exception {
        helper(indexOf(FOO_BAR_FRAX), FOO_BAR_FRAX.length(), false, false);
    }

    public void testQualifiedReplace1() throws Exception {
        helper(indexOf(FOO_BAR_FRAX), FOO_BAR_FRAX.length(), true, true);
    }

    private int indexOf(String str) throws IOException {
        return getFileContents(getInputTestFileName("A")).indexOf(str);
    }

    private void helper(int offset, int length, boolean replaceAllOccurrences, boolean useQualifiedReplace) throws Exception {
        GroovyCompilationUnit cu = (GroovyCompilationUnit) createCUfromTestFile(getPackageP(), "A");
        try {
            ExtractGroovyConstantRefactoring refactoring = new ExtractGroovyConstantRefactoring(cu, offset, length);
            refactoring.setReplaceAllOccurrences(replaceAllOccurrences);
            refactoring.setQualifyReferencesWithDeclaringClassName(useQualifiedReplace);
            refactoring.setConstantName(refactoring.guessConstantName());
            RefactoringStatus result = performRefactoring(refactoring, false);
            assertTrue("was supposed to pass", result == null || result.isOK());
            assertEqualLines("invalid extraction", getFileContents(getOutputTestFileName("A")), cu.getSource());

            assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
            assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());

            RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
            assertEqualLines("invalid undo", getFileContents(getInputTestFileName("A")), cu.getSource());

            assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
            assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());

            RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
            assertEqualLines("invalid redo", getFileContents(getOutputTestFileName("A")), cu.getSource());
        } finally {
            performDummySearch();
            cu.delete(true, null);
        }
    }
}