/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.documentProvider;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;


/**
 * Provides a Document in a Eclipse Workspace
 * 
 * @author Michael Klenk mklenk@hsr.ch
 * 
 */
public class WorkspaceDocumentProvider implements IGroovyDocumentProvider {

	private IFile file;
	private ModuleNode rootNode;

	public WorkspaceDocumentProvider(IFile file) {
		this.file = file;
	}

	public String getDocumentContent() {
		StringBuilder out = new StringBuilder();
		try {
			InputStream in = file.getContents();
			byte[] b = new byte[4096];
			for (int n; (n = in.read(b)) != -1;) {
				out.append(new String(b, 0, n));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public ModuleNode getRootNode() {
		if (rootNode == null) {
		    ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
		    if (unit instanceof GroovyCompilationUnit) {
		        rootNode = ((GroovyCompilationUnit) unit) .getModuleNode();
		    }
		}
		return rootNode;
	}

	public IDocument getDocument() {
		return new Document(getDocumentContent());
	}

	public IFile getFile() {
		return file;
	}

	public boolean fileExists() {
		return file.exists();
	}

	public String getName() {
		return file.getName();
	}

	public boolean isReadOnly() {
		return file.isReadOnly();
	}
}