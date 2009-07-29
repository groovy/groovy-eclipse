/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

/**
 * class to run a single (filebased test)
 */
package core;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import rename.RenameTestCase;

import tests.BaseTestSuite;

/**
 * little helper to run a single test (debuging!)
 * @author reto kleeb
 *
 */
public class SingleFileTest extends BaseTestSuite {
	
	//Currently set to run a rename local test!
	//check line 32

	private final static String TESTCATEGORY = "/RenameClassFiles";
	private final static String TESTNAME = "RenameClass_Test_FieldExpression.txt";


	public static TestSuite suite() {
		TestSuite ts = new TestSuite("Single File: " + TESTNAME);
		List<File> files = getFileList(TESTCATEGORY, TESTNAME);
		for (File file : files) {
			ts.addTest(new RenameTestCase(file.getName(), file));
		}
		return ts;
	}
}
