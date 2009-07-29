/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

abstract public class SingleFileRefactoringProvider extends RefactoringProvider {
	
	protected IGroovyDocumentProvider documentProvider;
	
	public SingleFileRefactoringProvider(IGroovyDocumentProvider docProvider, UserSelection selection) {
		super(selection);
		documentProvider = docProvider;
	}
	
	public ModuleNode getRootNode() {
		return documentProvider.getRootNode();
	}
	
	public IDocument getDocument() {
		return documentProvider.getDocument();
	}

	@Override
    public IGroovyDocumentProvider getDocumentProvider() {
		return documentProvider;
	}
	
	@Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		if (!documentProvider.fileExists()) {
			result.addFatalError("Sourcefile " + documentProvider.getName() + " not found");
		} else if (documentProvider.isReadOnly()) {
			result.addFatalError("Soucefile " + documentProvider.getName() + " is read only");
		}
		addInitialConditionsCheckStatus(result);
		return result;
	}
}
