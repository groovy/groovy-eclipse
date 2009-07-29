/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.documentProvider;

import java.util.List;

public interface IGroovyFileProvider {
	
	public List<IGroovyDocumentProvider> getAllSourceFiles();
	public IGroovyDocumentProvider getSelectionDocument();
	
}
