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

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import rename.RenameTestCase;

import tests.BaseTestSuite;

/**
 * little helper to run a single test (debuging!)
 * class to run a single (filebased test)
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
