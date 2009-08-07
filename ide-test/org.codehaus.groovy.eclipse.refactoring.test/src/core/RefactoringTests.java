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
package core;

import org.codehaus.groovy.eclipse.test.TestProject;
import org.eclipse.core.runtime.CoreException;

import formatter.FormatterTestSuite;
import inlineMethod.InlineMethodTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import rename.RenameTestSuite;
import tests.ASTBuilderTest;
import tests.ASTToolsTest;
import tests.FilePartReaderTest;
import tests.GroovyConventionBuilderTest;
import tests.HierarchyTreeTestCase;
import tests.SourceCodePointTest;
import ASTWriter.ASTWriterTestSuite;
import ExtractMethod.ExtractMethodTestSuite;

public class RefactoringTests extends TestCase {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Run of all Refactoring Test");
		TestProject testProject = null;
        try {
            testProject = new TestProject();
        } catch (CoreException e) {
            e.printStackTrace();
            fail("Fail, due to exception: " + e.getMessage());
        }

		//$JUnit-BEGIN$

			// Insert the files to test
	         suite.addTest(ASTWriterTestSuite.suite());
	         suite.addTest(ExtractMethodTestSuite.suite());
	         suite.addTest(InlineMethodTestSuite.suite());
	         suite.addTestSuite(FilePartReaderTest.class);
	         suite.addTestSuite(GroovyConventionBuilderTest.class);
	         suite.addTestSuite(SourceCodePointTest.class);
	         suite.addTestSuite(ASTToolsTest.class);
	         suite.addTestSuite(ASTBuilderTest.class);
	         suite.addTest(RenameTestSuite.suite());
	         suite.addTest(FormatterTestSuite.suite());
	         suite.addTestSuite(HierarchyTreeTestCase.class);
	         //Commented out on purpose
	         //
	         //suite.addTestSuite(ArchivedFailingTestSuite.class);
		
	     testProject.dispose();
	         
		//$JUnit-END$
		return suite;
	}
	
	public void testRefactoring() throws Exception {
		suite().run(super.createResult());
	}

}
