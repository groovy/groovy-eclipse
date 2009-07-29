/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.refactoring.core.IMultiEditProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.eclipse.text.edits.MultiTextEdit;

public abstract class RenameTextEditProvider implements IMultiEditProvider{
	
	protected String newName;
	protected String oldName;
	protected IGroovyDocumentProvider docProvider;
	
	public RenameTextEditProvider(String oldName, IGroovyDocumentProvider docProvider) {
		this.oldName = oldName;
		this.docProvider = docProvider;
	}
	
	public void setNewName(String newName) {
		this.newName = newName; 
	}
	
	public String getNewName() {
		return this.newName;
	}

	public IGroovyDocumentProvider getDocProvider() {
		return docProvider;
	}
	
	public abstract MultiTextEdit getMultiTextEdit();
	
	public List<String> getAlreadyUsedNames() {
		return Collections.emptyList();
	}
}
