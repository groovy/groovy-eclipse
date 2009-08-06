/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.eclipse.jface.text.IDocument;

import core.TestFileProvider;

/**
 * 
 * @author martin
 *
 */
public abstract class MultiFileTestCase extends RefactoringTestCase {
	
	private IGroovyFileProvider fileProvider;
	private Map<String,String> expectedDocs = new HashMap<String, String>();
	
	public MultiFileTestCase(String name, File fileToTest) {
		super(name,fileToTest);
		String selection = properties.get("selectionInFile");
		if (selection == null) selection = "";
		fileProvider = new TestFileProvider(getOrigin().get(),selection);
		buildExpectedList(getExpected());
	}

	@Override
    public IGroovyDocumentProvider getDocumentProvider() {
		return fileProvider.getSelectionDocument();
	}
	
	@Override
    public void finalAssert() {
		List<IGroovyDocumentProvider> originDocuments = fileProvider.getAllSourceFiles();
		for (IGroovyDocumentProvider origin : originDocuments) {
			String originContent = origin.getDocumentContent();
			String expected = expectedDocs.get(origin.getName());
			assertEquals("Error in File: " + file + " ", expected,originContent);
		}
	}
	
	public IGroovyFileProvider getFileProvider() {
		return fileProvider;
	}
	
	private void buildExpectedList(IDocument expected) {
		String[] testFiles = expected.get().split("--->");
		//only one input file -> must be the selected document
		if (testFiles.length == 1) {
			expectedDocs.put("",testFiles[0]);
		} else {
			splitExpected(testFiles);
		}
	}

	private void splitExpected(String[] testFiles) {
		for (int i = 0; i < testFiles.length; i++ ) {
			String[] sourceParts = testFiles[i].split(":::");
			expectedDocs.put(sourceParts[0].trim(),sourceParts[1].trim());
		}
	}
}
