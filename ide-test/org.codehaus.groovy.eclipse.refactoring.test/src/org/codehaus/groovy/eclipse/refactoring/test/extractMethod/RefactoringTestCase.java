/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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

package org.codehaus.groovy.eclipse.refactoring.test.extractMethod;
import java.io.File;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

import tests.BaseTestCase;

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
                Change change = createChange();
                change.perform(new NullProgressMonitor());
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

    public abstract Change createChange() throws OperationCanceledException, CoreException;
}
