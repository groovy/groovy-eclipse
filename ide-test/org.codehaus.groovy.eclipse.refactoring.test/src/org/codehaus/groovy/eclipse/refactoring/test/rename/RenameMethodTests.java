package org.codehaus.groovy.eclipse.refactoring.test.rename;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTest;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.RefactoringCore;

public class RenameMethodTests extends RefactoringTest {
	private static final Class<RenameMethodTests> clazz= RenameMethodTests.class;
	private static final String REFACTORING_PATH= "RenameMethod/";

	public RenameMethodTests(String name) {
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


	private void helper2_0(String typeName, String methodName, String newMethodName, String[] signatures, boolean updateReferences, boolean createDelegate) throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		try{
			IType classA= getType(cu, typeName);
			IMethod method= classA.getMethod(methodName, signatures);
			RenameJavaElementDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD);
			descriptor.setUpdateReferences(updateReferences);
			descriptor.setJavaElement(method);
			descriptor.setNewName(newMethodName);
			descriptor.setKeepOriginal(createDelegate);
			descriptor.setDeprecateDelegate(true);

			assertEquals("was supposed to pass", null, performRefactoring(descriptor));
			assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());

			assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
			assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());
			//assertEquals("1 to undo", 1, Refactoring.getUndoManager().getRefactoringLog().size());

			RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
			assertEqualLines("invalid undo", getFileContents(getInputTestFileName("A")), cu.getSource());

			assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
			assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());
			//assertEquals("1 to redo", 1, Refactoring.getUndoManager().getRedoStack().size());

			RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
			assertEqualLines("invalid redo", getFileContents(getOutputTestFileName("A")), cu.getSource());
		} finally{
			performDummySearch();
			cu.delete(true, null);
		}
	}
	
   private void helperDelegate() throws Exception{
        helper2_0("A", "m", "k", new String[0], true, true);
    }


	private void helper2(boolean updateReferences) throws Exception{
		helper2_0("A", "m", "k", new String[0], updateReferences, false);
	}

	private void helper2() throws Exception{
		helper2(true);
	}

	public void test1() throws Exception{
		helper2();
	}
	public void test2() throws Exception{
	    helper2();
	}
	public void test3() throws Exception{
	    helper2_0("A", "m", "k", new String[] { "QD;" }, true, false);
	}
	public void test4() throws Exception{
	    helper2();
	}
	public void test5() throws Exception{
	    helper2();
	}
	public void test6() throws Exception{
	    helper2();
	}
	public void test7() throws Exception{
	    helper2();
	}
	public void test8() throws Exception{
        helper2_0("B", "m", "k", new String[] { }, true, false);
	}
    public void test9() throws Exception{
        helper2();
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
	
	public void testDelegate1() throws Exception {
	    helperDelegate();
    }
	public void testDelegate2() throws Exception {
	    helperDelegate();
	}
	
}
