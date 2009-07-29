/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package core;

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

public class RefactoringTests extends TestCase{

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Run of all Refactoring Test");
		
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
		
	         
		//$JUnit-END$
		return suite;
	}
	
	public void testRefactoring() throws Exception {
		suite().run(super.createResult());
	}

}
