 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.editor.FoldingPositionProvider;
import org.codehaus.groovy.eclipse.editor.GroovyDocumentSetupParticipant;
import org.codehaus.groovy.eclipse.test.Activator;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * Test for {@link FoldingPositionProvider}.
 * 
 * @author km
 */
public class FoldingPositionProviderTest extends EclipseTestCase {
	/**
	 * Name of test file.
	 */
	private static final String FOLDING_TEST_CLASS_SCRIPT_NAME = "FoldingTestClass.groovy";
	/**
	 * Number of folding in test file.
	 */
	private static final int INITIAL_FOLDING_POSITIONS = 8;

	@SuppressWarnings("unchecked")
	private List moduleNodes;
	private FoldingPositionProvider foldingPositionProvider;
	private IDocument document;

	@SuppressWarnings("unchecked")
	public void testUpdatePositions() {
		foldingPositionProvider.updatePositions(moduleNodes);
		assertEquals("Expected that initially no positions have been removed!",
				0, foldingPositionProvider.getRemovedPositions().size());
		assertEquals(INITIAL_FOLDING_POSITIONS, foldingPositionProvider
				.getAddedPositions().size());
		assertEquals(INITIAL_FOLDING_POSITIONS, foldingPositionProvider
				.getPositions().size());

		// update folding without a change
		initFoldingPositionProvider(foldingPositionProvider.getPositions());
		foldingPositionProvider.updatePositions(moduleNodes);
		assertEquals("Expected that no positions have been removed!", 0,
				foldingPositionProvider.getRemovedPositions().size());
		assertEquals("Expected that no positions have been added!", 0,
				foldingPositionProvider.getAddedPositions().size());
		assertEquals("Expected same number of positions as before!",
				INITIAL_FOLDING_POSITIONS, foldingPositionProvider
						.getPositions().size());

		// remove one class node
		ModuleNode moduleNode = (ModuleNode) moduleNodes.get(0);
		List allClasses = moduleNode.getClasses();
		ClassNode removedClassNode = (ClassNode) allClasses.remove(allClasses
				.size() - 1);
		initFoldingPositionProvider(foldingPositionProvider.getPositions());
		foldingPositionProvider.updatePositions(moduleNodes);
		assertEquals("Expected that one positions has been removed!", 1,
				foldingPositionProvider.getRemovedPositions().size());
		assertEquals("Expected that no positions have been added!", 0,
				foldingPositionProvider.getAddedPositions().size());
		assertEquals("Expected that one positions has been removed!",
				INITIAL_FOLDING_POSITIONS - 1, foldingPositionProvider
						.getPositions().size());

		// re-add removed class node
		allClasses.add(removedClassNode);
		initFoldingPositionProvider(foldingPositionProvider.getPositions());
		foldingPositionProvider.updatePositions(moduleNodes);
		assertEquals("Expected that no positions have been removed!", 0,
				foldingPositionProvider.getRemovedPositions().size());
		assertEquals("Expected that one position has been added!", 1,
				foldingPositionProvider.getAddedPositions().size());
		assertEquals("Expected same number of positions as before!",
				INITIAL_FOLDING_POSITIONS, foldingPositionProvider
						.getPositions().size());
	}

	@Override
    protected void setUp() throws Exception {
		super.setUp();
		GroovyRuntime.addGroovyRuntime(testProject.getProject());
		final URL url = Activator.bundle().getEntry(
				"/testData/groovyfiles/" + FOLDING_TEST_CLASS_SCRIPT_NAME);
		InputStream input = null;

		try {
			input = url.openStream();
			IFile file = testProject.createGroovyTypeAndPackage("pack1",
					FOLDING_TEST_CLASS_SCRIPT_NAME, input);
			
			ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
			if (unit instanceof GroovyCompilationUnit) {
			    moduleNodes = 
			        Collections.singletonList(((GroovyCompilationUnit) unit).getModuleNode());
			} else {
			    fail("Not a groovy compilation unit");
			}
			document = createDocument(file);
			initFoldingPositionProvider(Collections.EMPTY_SET);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	/**
	 * Initializes the CUT with specified old positions
	 * @param oldPositions list of {@link org.eclipse.jface.text.Position}
	 */
	@SuppressWarnings("unchecked")
	private void initFoldingPositionProvider(Set oldPositions) {
		foldingPositionProvider = new FoldingPositionProvider(document,
				oldPositions);
	}

	/**
	 * Creates a document from the specified file.
	 * @param file file to create document from
	 * @return
	 * @throws IOException
	 */
	private IDocument createDocument(IFile file) throws IOException {
		String contents = getContents(file);
		IDocument document = new Document(contents);
		new GroovyDocumentSetupParticipant().setup(document);
		return document;
	}

	private String getContents(IFile fileHandle) throws IOException {
		URL url = fileHandle.getLocationURI().toURL();
		InputStream inputStream = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		try {
			StringBuilder stringBuffer = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuffer.append(line + "\n");
			}
			return stringBuffer.toString();
		} finally {
			reader.close();
		}
	}
}
