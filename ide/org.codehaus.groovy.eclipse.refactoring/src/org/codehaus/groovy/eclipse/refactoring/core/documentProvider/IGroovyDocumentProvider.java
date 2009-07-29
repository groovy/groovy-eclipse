/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.documentProvider;

import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.jface.text.IDocument;

/**
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public interface IGroovyDocumentProvider {
		
	public abstract ModuleNode getRootNode();
	public abstract String getDocumentContent();
	public abstract IDocument getDocument();
	public abstract boolean fileExists();
	public abstract String getName();
	public abstract boolean isReadOnly();

}
