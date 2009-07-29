/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public interface IRenameProvider{
	
	public abstract void setNewName(String newName);
	
	public abstract String getOldName();
	
	public abstract void checkUserInput(RefactoringStatus status, String text);
}
