/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package rename;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import tests.BaseTestSuite;

public class RenameTestSuite extends BaseTestSuite {

	public static TestSuite suite() {
		
		TestSuite ts = new TestSuite("Rename Suite");
		
		List<File> files;
		
		files = getFileList("/renameClassFiles","RenameClass_Test_");
		
		for (File file : files) {		
			ts.addTest(new RenameTestCase(file.getName(),file));
		}
		
		files = getFileList("/RenameLocalFiles","RenameLocal_Test_");
		
		for (File file : files) {		
			ts.addTest(new RenameTestCase(file.getName(),file));
		}
		
		files = getFileList("/renameFieldFiles","RenameField_Test_");
		
		for (File file : files) {		
			ts.addTest(new RenameTestCase(file.getName(),file));
		}
		
		files = getFileList("/renameMethodFiles","RenameMethod_Test_");
		
		for (File file : files) {		
			ts.addTest(new RenameTestCase(file.getName(),file));
		}
		
		return ts;
	}
}
