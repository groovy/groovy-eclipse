/* 
 * Copyright (C) 2007, 2008 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package core;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.GroovyRefactoringDispatcher;

/**
 * Dispatcher for the testcases, has a different fileprovider than the 
 * class that is used in eclipse
 * @author reto kleeb
 *
 */
public class GroovyRefactoringDispatcherMock extends GroovyRefactoringDispatcher {
	
	private final IGroovyFileProvider fileProvider;
	
	public GroovyRefactoringDispatcherMock(ASTNode node, IGroovyDocumentProvider docProvider, UserSelection selection, IGroovyFileProvider fileProvider) {
		super(node, selection, docProvider);
		this.fileProvider = fileProvider;
	}

    protected IGroovyFileProvider getWSFileProvider() {
		return fileProvider;
	}

}
