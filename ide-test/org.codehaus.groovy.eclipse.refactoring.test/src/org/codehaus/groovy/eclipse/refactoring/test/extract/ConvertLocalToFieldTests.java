/*
 * Copyright 2003-2014 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.extract;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.core.extract.ConvertGroovyLocalToFieldRefactoring;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTest;
import org.codehaus.groovy.eclipse.refactoring.test.RefactoringTestSetup;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author Stephanie Van Dyk
 * @author Daniel Phan
 * @created Jan 22, 2012
 */
public class ConvertLocalToFieldTests extends RefactoringTest {
	private static final Class<ConvertLocalToFieldTests> clazz = ConvertLocalToFieldTests.class;

	// not actually used
	private static final String REFACTORING_PATH = "ConvertLocalToField/";
	
	public ConvertLocalToFieldTests(String name) {
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

	public void testMethodToModule() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testMethodToModule");
		helper(testCase);
	}
	
	public void testClosureToModule() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testClosureToModule");
		helper(testCase);
	}
	
	public void testDeclarationWithDef() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testDeclarationWithDef");
		helper(testCase);
	}
	
	public void testDeclarationWithType() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testDeclarationWithType");
		helper(testCase);
	}

	public void testReference() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testReference");
		helper(testCase);
	}
	
	public void testTupleDeclaration() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testTupleDeclaration");
		helper(testCase);
	}
	
	public void testRename() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testRename");
		helper(testCase);
	}
	
	public void testInitialization() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testInitialization");
		helper(testCase);
	}
	
	public void testVariableConflict() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testVariableConflict");
		helper(testCase);
	}
	
	public void testFieldConflict() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testFieldConflict");
		helper(testCase);
	}
	
	public void testFieldReference() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testFieldReference");
		helper(testCase);
	}
	
	public void testException() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testException");
		helper(testCase);
	}
	
	public void testForLoop() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testForLoop");
		helper(testCase);
	}
	
	public void testPostfix() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testPostfix");
		helper(testCase);
	}
	
	public void testPrefix() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testPrefix");
		helper(testCase);
	}
	
	public void testMethodInvocation() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testMethodInvocation");
		helper(testCase);
	}
	
	public void testParameterList() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testParameterList");
		helper(testCase);
	}
	
	public void testArgumentList() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testArgumentList");
		helper(testCase);
	}
	
	public void testInnerClass() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testInnerClass");
		helper(testCase);
	}
	
	public void testInnerFieldConflict() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testInnerFieldConflict");
		helper(testCase);
	}
	
	public void testFakeField() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testFakeField");
		helper(testCase);
	}
	
	public void testClosure() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testClosure");
		helper(testCase);
	}
	
	public void testClosureVariableConflict() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testClosureVariableConflict");
		helper(testCase);
	}
	
	public void testClosureParameterList() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testClosureParameterList");
		helper(testCase);
	}
	
	public void testClosureImplicitIt() throws Exception {
		ConvertLocalToFieldTestsData.TestCase testCase = ConvertLocalToFieldTestsData.getTestCases().get("testClosureImplicitIt");
		helper(testCase);
	}
	
	private void helper(ConvertLocalToFieldTestsData.TestCase testCase) throws Exception {
		GroovyCompilationUnit cu = (GroovyCompilationUnit) createCU(getPackageP(), "Test.groovy", testCase.getInput());
		try {
			ConvertGroovyLocalToFieldRefactoring refactoring = new ConvertGroovyLocalToFieldRefactoring(cu, testCase.getSelectionOffset(), testCase.getSelectionLength());
			refactoring.setFieldName(testCase.getFieldName());
			
			RefactoringStatus result = null;
			try {
				result = performRefactoring(refactoring, false);
			} catch (AssertionFailedError e) {
				// If expected is null, the TestCase expected the refactoring to
				// fail.
				if (testCase.getExpected() == null) {
					return;
				} else {
					throw e;
				}
			}
			
			if (testCase.isExpectWarning()) {
				assertTrue("was supposed to pass", result.hasWarning());
			} else {
				assertTrue("was supposed to pass", result.isOK());
				assertEqualLines("invalid conversion", testCase.getExpected(), cu.getSource());
			}
			
			assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
			assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());

			RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
			assertEqualLines("invalid undo", testCase.getInput(), cu.getSource());

			assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
			assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());

			RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
			assertEqualLines("invalid redo", testCase.getExpected(), cu.getSource());
		} finally {
			performDummySearch();
			cu.delete(true, null);
		}
	}
}