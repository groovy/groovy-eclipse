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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyLocalRefactoring;
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
public class ExtractLocalTests extends RefactoringTest {
	private static final Class<ExtractLocalTests> clazz = ExtractLocalTests.class;

	// not actually used
	private static final String REFACTORING_PATH = "ExtractLocal/";

	public ExtractLocalTests(String name) {
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
		helper(ExtractLocalTestsData.getTest1In(),
				ExtractLocalTestsData.getTest1Out(),
				ExtractLocalTestsData.findLocation("foo + bar", "test1"),
				"foo + bar".length(), true);
	}

	public void test2() throws Exception {
		helper(ExtractLocalTestsData.getTest2In(),
				ExtractLocalTestsData.getTest2Out(),
				ExtractLocalTestsData.findLocation("foo.bar", "test2"),
				"foo.bar".length(), true);
	}

	public void test3() throws Exception {
		helper(ExtractLocalTestsData.getTest3In(),
				ExtractLocalTestsData.getTest3Out(),
				ExtractLocalTestsData.findLocation("baz.foo.&bar", "test3"),
				"baz.foo.&bar".length(), true);
	}

	public void test4() throws Exception {
		helper(ExtractLocalTestsData.getTest4In(),
				ExtractLocalTestsData.getTest4Out(),
				ExtractLocalTestsData.findLocation("first + 1", "test4"),
				"first + 1".length(), false);
	}

	public void test5() throws Exception {
		helper(ExtractLocalTestsData.getTest5In(),
				ExtractLocalTestsData.getTest5Out(),
				ExtractLocalTestsData.findLocation("foo + bar", "test5"),
				"foo + bar".length(), true);
	}

	public void test6() throws Exception {
		helper(ExtractLocalTestsData.getTest6In(),
				ExtractLocalTestsData.getTest6Out(),
				ExtractLocalTestsData.findLocation("foo + bar", "test6"),
				"foo + bar".length(), true);
	}

	public void test7() throws Exception {
		helper(ExtractLocalTestsData.getTest7In(),
				ExtractLocalTestsData.getTest7Out(),
				ExtractLocalTestsData.findLocation("foo + bar", "test7"),
				"foo + bar".length(), true);
	}

	public void test8() throws Exception {
		helper(ExtractLocalTestsData.getTest8In(),
				ExtractLocalTestsData.getTest8Out(),
				ExtractLocalTestsData.findLocation("foo+  bar", "test8"),
				"foo+  bar".length(), true);
	}

	public void test9() throws Exception {
	    helper(ExtractLocalTestsData.getTest9In(),
	            ExtractLocalTestsData.getTest9Out(),
	            ExtractLocalTestsData.findLocation("map.one", "test9"),
	            "map.one".length(), true);
	}
	
	public void test10() throws Exception {
	    helper(ExtractLocalTestsData.getTest10In(),
	            ExtractLocalTestsData.getTest10Out(),
	            ExtractLocalTestsData.findLocation("model.farInstance()", "test10"),
	            "model.farInstance()".length(), true);
	}
	
	public void test10a() throws Exception {
	    helper(ExtractLocalTestsData.getTest10In(),
	            ExtractLocalTestsData.getTest10Out(),
	            ExtractLocalTestsData.findLocation("model.farInstance() ", "test10"),
	            "model.farInstance() ".length(), true);
	}
	
	public void test10b() throws Exception {
	    helper(ExtractLocalTestsData.getTest10In(),
	            ExtractLocalTestsData.getTest10Out(),
	            ExtractLocalTestsData.findLocation("model.farInstance()  ", "test10"),
	            "model.farInstance()  ".length(), true);
	}
	
	public void test11() throws Exception {
	    helper(ExtractLocalTestsData.getTest11In(),
	            ExtractLocalTestsData.getTest11Out(),
	            ExtractLocalTestsData.findLocation("println \"here\"", "test11"),
	            "println \"here\"".length(), true);
	}
	
	public void test12() throws Exception {
	    helper(ExtractLocalTestsData.getTest12In(),
	            ExtractLocalTestsData.getTest12Out(),
	            ExtractLocalTestsData.findLocation("println \"here\"", "test12"),
	            "println \"here\"".length(), true);
	}
	
	public void test13() throws Exception {
	    helper(ExtractLocalTestsData.getTest13In(),
	            ExtractLocalTestsData.getTest13Out(),
	            ExtractLocalTestsData.findLocation("a + b", "test13"),
	            "a + b".length(), true);
	}

	private void helper(String before, String expected, int offset, int length, boolean replaceAllOccurrences) throws Exception {
		GroovyCompilationUnit cu = (GroovyCompilationUnit) createCU(getPackageP(), "A.groovy", before);
		try {
			ExtractGroovyLocalRefactoring refactoring = new ExtractGroovyLocalRefactoring(cu, offset, length);
			refactoring.setReplaceAllOccurrences(replaceAllOccurrences);
			refactoring.setLocalName(refactoring.guessLocalNames()[0]);
			RefactoringStatus result = performRefactoring(refactoring, false);
			assertTrue("was supposed to pass", result == null || result.isOK());
			assertEqualLines("invalid extraction", expected, cu.getSource());

			assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
			assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());

			RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
			assertEqualLines("invalid undo", before, cu.getSource());

			assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
			assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());

			RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
			assertEqualLines("invalid redo", expected, cu.getSource());
		} finally {
			performDummySearch();
			cu.delete(true, null);
		}
	}
}