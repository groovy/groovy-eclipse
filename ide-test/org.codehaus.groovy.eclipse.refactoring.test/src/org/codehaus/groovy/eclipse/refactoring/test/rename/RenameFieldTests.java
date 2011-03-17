package org.codehaus.groovy.eclipse.refactoring.test.rename;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.test.ParticipantTesting;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTest;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RenameFieldTests extends RefactoringTest {
    private static final Class<RenameFieldTests> clazz = RenameFieldTests.class;

    private static final String REFACTORING_PATH = "RenameField/";

    public RenameFieldTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new RefactoringTestSetup(new TestSuite(clazz));
    }

    public static Test setUpTest(Test test) {
        return new RefactoringTestSetup(test);
    }

    protected String getRefactoringPath() {
        return REFACTORING_PATH;
    }

    protected void setUp() throws Exception {
        super.setUp();
        fIsPreDeltaTest= true;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private void helper2_0(String typeName, String fieldName,
            String newFieldName, boolean updateReferences,
            boolean createDelegates, boolean renameGetter, boolean renameSetter, 
            boolean performOnError)
            throws Exception {
        ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
        try {
            IType classA = getType(cu, typeName);
            IField field = classA.getField(fieldName);
            boolean isEnum = JdtFlags.isEnum(field);
            String id = isEnum ? IJavaRefactorings.RENAME_ENUM_CONSTANT
                    : IJavaRefactorings.RENAME_FIELD;
            RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory
                    .createRenameJavaElementDescriptor(id);
            descriptor.setUpdateReferences(updateReferences);
            descriptor.setJavaElement(field);
            descriptor.setNewName(newFieldName);
            if (!isEnum) {
                descriptor.setRenameGetters(renameGetter);
                descriptor.setRenameSetters(renameSetter);
                descriptor.setKeepOriginal(createDelegates);
                descriptor.setDeprecateDelegate(true);
            }
            RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor);
            RenameFieldProcessor processor = (RenameFieldProcessor) refactoring
                    .getProcessor();

            int numbers = 1;
            List<IAnnotatable> elements = new ArrayList<IAnnotatable>();
            elements.add(field);
            List<RenameArguments> args = new ArrayList<RenameArguments>();
            args.add(new RenameArguments(newFieldName, updateReferences));
            if (renameGetter) {
                elements.add(processor.getGetter());
                args.add(new RenameArguments(processor.getNewGetterName(),
                        updateReferences));
                numbers++;
            }
            if (renameSetter) {
                elements.add(processor.getSetter());
                args.add(new RenameArguments(processor.getNewSetterName(),
                        updateReferences));
                numbers++;
            }
            String[] renameHandles = ParticipantTesting.createHandles(elements
                    .toArray());

            RefactoringStatus result = performRefactoring(refactoring, performOnError);
            assertTrue("was supposed to pass", result==null || result.isOK());
            assertEqualLines("invalid renaming",
                    getFileContents(getOutputTestFileName("A")), cu.getSource());

            ParticipantTesting.testRename(renameHandles,
                    (RenameArguments[]) args.toArray(new RenameArguments[args
                            .size()]));

            assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                    .anythingToUndo());
            assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                    .anythingToRedo());

            RefactoringCore.getUndoManager().performUndo(null,
                    new NullProgressMonitor());
            assertEqualLines("invalid undo",
                    getFileContents(getInputTestFileName("A")), cu.getSource());

            assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
                    .anythingToUndo());
            assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
                    .anythingToRedo());

            RefactoringCore.getUndoManager().performRedo(null,
                    new NullProgressMonitor());
            assertEqualLines("invalid redo",
                    getFileContents(getOutputTestFileName("A")), cu.getSource());
        } finally {
            performDummySearch();
            cu.delete(true, null);
        }
    }

    private void helper2(boolean updateReferences) throws Exception {
        helper2_0("A", "f", "g", updateReferences, false, false, false, false);
    }
    private void helperPerformOnError(boolean updateReferences) throws Exception {
        helper2_0("A", "f", "g", updateReferences, false, false, false, true);
    }
    private void helperScript() throws Exception {
        helper2_0("B", "f", "g", true, false, false, false, false);
    }

    private void helper2() throws Exception {
        helper2(true);
    }

    public void testInitializer1() throws Exception {
        helper2();
    }

    public void testInitializer2() throws Exception {
        helper2();
    }

    public void testInitializer3() throws Exception {
        helper2();
    }

    public void test1() throws Exception {
        helper2();
    }
    public void test2() throws Exception {
        helper2();
    }
    public void test3() throws Exception {
        helper2();
    }
    public void test4() throws Exception {
        helper2();
    }
    public void test5() throws Exception {
        helperPerformOnError(true);
    }
    public void test6() throws Exception {
        helper2();
    }
    public void test7() throws Exception {
        helperPerformOnError(true);
    }
    public void test8() throws Exception {
        helper2();
    }
    public void test9() throws Exception {
        helper2();
    }
    public void testScript1() throws Exception {
        helperScript();
    }
    public void testScript2() throws Exception {
        helperScript();
    }
}
