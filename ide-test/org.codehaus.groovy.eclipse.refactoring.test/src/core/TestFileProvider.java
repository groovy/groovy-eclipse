/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.eclipse.jface.text.Document;

/**
 * Fileprovider used for the file based tests
 * @author martin
 *
 */
public class TestFileProvider implements IGroovyFileProvider{
	
	private String completeSourceSection;
	private String selectionName;
	private Map<String,TestDocumentProvider> sourcefileList;
	private TestDocumentProvider selectionDocument;
	private String[] sources;
	private String[] fileNames;
	
	public TestFileProvider(String completeSourceSection, String selectionName) {
		this.completeSourceSection = completeSourceSection;
		this.selectionName = selectionName;
		sourcefileList =  new HashMap<String, TestDocumentProvider>();
	}

	public List<IGroovyDocumentProvider> getAllSourceFiles(){
		if (sourcefileList.isEmpty()) {
			String[] testFiles = completeSourceSection.split("--->");
			//only one input file -> must be the selected document
			if (testFiles.length == 1) {
				sourcefileList.put("",new TestDocumentProvider(new Document(testFiles[0]),""));
				selectionDocument = sourcefileList.get("");
			} else {
				splitSourceToDocumentProvider(testFiles);
			}
		}
		List<IGroovyDocumentProvider> returnList = new ArrayList<IGroovyDocumentProvider>();
		returnList.addAll(sourcefileList.values());
		return returnList;
	}

	private void splitSourceToDocumentProvider(String[] testFiles) {
		sources = new String[testFiles.length];
		fileNames = new String[testFiles.length];
		for (int i = 0; i < testFiles.length; i++ ) {
			String[] sourceParts = testFiles[i].split(":::");
			fileNames[i] = sourceParts[0].trim();
			sources[i] = sourceParts[1].trim();
			sourcefileList.put(fileNames[i],new TestDocumentProvider(new Document(sources[i]),fileNames[i]));
			if (fileNames[i].equals(selectionName)) {
				selectionDocument = sourcefileList.get(fileNames[i]);
			}
		}
		setRootNodes();
	}

	/**
	 * Need to compile all source files together to know in an other source file the declared class
	 * Set the RootNode manually to the FileDocumentProvider to avoid the recreation of a new one there
	 */
	private void setRootNodes() {
		Map<String,ModuleNode> rootNodes = ASTProvider.getRootNodes(sources, fileNames);
		for (String name : rootNodes.keySet()) {
			TestDocumentProvider provider = sourcefileList.get(name);
			provider.setRootNode(rootNodes.get(name));
		}
	}

	public IGroovyDocumentProvider getSelectionDocument() {
		if (selectionDocument == null) {
			getAllSourceFiles();
		}
		return selectionDocument;
	}
}
