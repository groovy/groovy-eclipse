/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core;

import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.eclipse.text.edits.MultiTextEdit;

public interface IMultiEditProvider {
	
	public abstract MultiTextEdit getMultiTextEdit();

	public abstract IGroovyDocumentProvider getDocProvider();
	
}
