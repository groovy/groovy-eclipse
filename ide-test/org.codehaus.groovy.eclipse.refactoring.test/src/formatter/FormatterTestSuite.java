/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package formatter;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import tests.BaseTestSuite;

public class FormatterTestSuite extends BaseTestSuite {

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("Formatter Suite");
		List<File> files = getFileList("/FormatterFiles","Formatter_Test_");
		for (File file : files) {		
			ts.addTest(new FormatterTestCase(file.getName(),file));
		}
		return ts;
	}
}
