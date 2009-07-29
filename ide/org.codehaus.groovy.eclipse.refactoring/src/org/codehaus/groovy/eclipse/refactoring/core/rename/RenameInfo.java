/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringInfo;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class RenameInfo extends RefactoringInfo {
	
	protected IRenameProvider provider;

	public RenameInfo(RefactoringProvider provider) {
		super(provider);
		this.provider = (IRenameProvider) provider;
	}

	public void setNewName(String newName) {
		provider.setNewName(newName);
	}
	
	public String getOldName(){
		return provider.getOldName();
	}
	
	public void checkUserInput(RefactoringStatus status, String text) {
		provider.checkUserInput(status, text);
	}
}
