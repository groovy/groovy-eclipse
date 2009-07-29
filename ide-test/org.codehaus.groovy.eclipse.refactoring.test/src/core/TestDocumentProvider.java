/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package core;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.eclipse.jface.text.IDocument;

/**
 * Doc Provider that works with Files (used for file based tests)
 */
public class TestDocumentProvider implements IGroovyDocumentProvider {
	
	private IDocument document;
	private ModuleNode rootNode;
	private String name;
	private boolean fileExists = true;
	private boolean isReadOnly = false;
	
	public TestDocumentProvider(IDocument doc,String name) {
		document = doc;
		this.name = name;
	}

	public IDocument getDocument() {
		return document;
	}

	public String getDocumentContent() {
		return document.get();
	}

	public ModuleNode getRootNode() {
		if(rootNode == null)
			rootNode = ASTProvider.getAST(document.get(), name);
		return rootNode;
	}
	
	public void setRootNode(ModuleNode rootNode) {
		this.rootNode = rootNode;
	}

	public boolean fileExists() {
		return fileExists;
	}

	public String getName() {
		return name;
	}

	public boolean isReadOnly() {
		return isReadOnly;
	}

}
