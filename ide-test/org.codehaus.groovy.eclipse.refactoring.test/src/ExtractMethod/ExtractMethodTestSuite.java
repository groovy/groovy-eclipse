/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package ExtractMethod;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import tests.BaseTestSuite;

public class ExtractMethodTestSuite extends BaseTestSuite {

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("Extract Method Suite");
		List<File> files = getFileList("/ExtractMethodFiles","ExtractMethod_Test_");
		for (File file : files) {		
			ts.addTest(new ExtractMethodTestCase(file.getName(),file));
		}
		return ts;
	}
}
