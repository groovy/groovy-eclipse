/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package tests;

import inlineMethod.InlineMethodTestCase;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import rename.RenameTestCase;
import ASTWriter.ASTWriterTestCase;
import ExtractMethod.ExtractMethodTestCase;

/**
 * 
 * Archive of tests that require major changes in either the AST
 * or other core components
 *
 */

public class ArchivedFailingTestSuite extends BaseTestSuite {

	public static TestSuite suite() {
		
		TestSuite archivedTestssuite = new TestSuite("Archived Tests Suite");
		
		List<File> extractMethodTests = getFileList("/archivedFailingTestFiles","ExtractMethod_Test_");
		for (File file : extractMethodTests) {		
			archivedTestssuite.addTest(new ExtractMethodTestCase(file.getName(),file));
		}
		
		List<File> inLineMethodTests = getFileList("/archivedFailingTestFiles","InlineMethod_Test_");
		for (File file : inLineMethodTests) {		
			archivedTestssuite.addTest(new InlineMethodTestCase(file.getName(),file));
		}
		
		List<File> renameClassTests = getFileList("/archivedFailingTestFiles","RenameClass_Test_");
		for (File file : renameClassTests) {		
			archivedTestssuite.addTest(new RenameTestCase(file.getName(),file));
		}

		List<File> renameLocalTests = getFileList("/archivedFailingTestFiles","RenameLocal_Test_");
		for (File file : renameLocalTests) {		
			archivedTestssuite.addTest(new RenameTestCase(file.getName(),file));
		}
		
		List<File> astWriterTests = getFileList("/archivedFailingTestFiles","AST_Writer_Test_");
		for (File file : astWriterTests) {		
			archivedTestssuite.addTest(new ASTWriterTestCase(file.getName(),file));
		}
		
		return archivedTestssuite;
	}

}
