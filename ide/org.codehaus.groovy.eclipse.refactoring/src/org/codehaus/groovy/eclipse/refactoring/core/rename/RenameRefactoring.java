/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.ui.pages.rename.RenamePage;

public class RenameRefactoring extends GroovyRefactoring {
	
	public RenameRefactoring(RenameInfo info, String refactoringName) {
		super(info);
		setName(refactoringName);
		pages.add(new RenamePage(refactoringName, info));
	}
}
