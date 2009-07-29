/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package core;

import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameDispatcher;

/**
 * Dispatcher for the testcases, has a different fileprovider than the 
 * class that is used in eclipse
 * @author reto kleeb
 *
 */
public class RenameDispatcherMock extends RenameDispatcher {
	
	private final IGroovyFileProvider fileProvider;
	
	public RenameDispatcherMock(IGroovyDocumentProvider docProvider, UserSelection selection, IGroovyFileProvider fileProvider) {
		super(docProvider, selection);
		this.fileProvider = fileProvider;
	}

	@Override
    protected IGroovyFileProvider getWSFileProvider() {
		return fileProvider;
	}

}
