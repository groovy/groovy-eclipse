/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package tests;
import java.io.File;
import junit.framework.AssertionFailedError;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;


public abstract class RefactoringTestCase extends BaseTestCase {
	
	public RefactoringTestCase(String name, File file) {
		super(name, file);
		// Set Method to call for JUnit
		setName("testRefactoring");
	}

	/**
	 * Simulates the framework flow of a Refactoring
	 */
	public void testRefactoring() throws Throwable {
		try {
			preAction();
			RefactoringStatus rs = checkInitialCondition();
			simulateUserInput();
			rs.merge(checkFinalCondition());
			if (analyseRefactoringStatus(rs)) {
				GroovyChange change = createChange();
				change.performChanges();
			}
			finalAssert();
		} catch (Exception e) {
			//Hack because groovy wraps exception from java sometimes
			if (e.getCause() instanceof AssertionFailedError) {
				throw e.getCause();
			}
            //Unexpected Refactoring Error
            throw e;
		}
	}
	
	private boolean analyseRefactoringStatus(RefactoringStatus state) {
		RefactoringStatusEntry[] entries = state.getEntries();
		if (shouldFail && (entries.length == 0)) {
			fail("Should fail: " + properties.get("failMessage"));
		} 
		for(int i = 0; i < entries.length; i++) {
			RefactoringStatusEntry entry = entries[i];
			if((entry.isError() || entry.isFatalError()) && shouldFail == false) {
				//error was not expected
				fail("condition check failed: " + entry.getMessage());
			} else {
				//Test the errorMessage
				if(shouldFail && properties.get("failMessage")!= null) {
					assertEquals(properties.get("failMessage"),entry.getMessage());
				}
			}
			if (entry.isFatalError()) {
				return false;
			}
		}
		return true;
	}
	
	public abstract void preAction();
	
	public abstract RefactoringStatus checkInitialCondition() throws OperationCanceledException, CoreException;
	
	public abstract void simulateUserInput();
	
	public abstract RefactoringStatus checkFinalCondition() throws OperationCanceledException, CoreException;
	
	public abstract GroovyChange createChange() throws OperationCanceledException, CoreException;
}
