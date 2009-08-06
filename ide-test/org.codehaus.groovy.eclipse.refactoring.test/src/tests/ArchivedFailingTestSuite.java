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
