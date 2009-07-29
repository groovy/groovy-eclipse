/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.refactoring.ui.pages.rename.RenameFileSelectionPage;
import org.codehaus.groovy.eclipse.refactoring.ui.pages.rename.AmbiguousRenameFirstPage;

/**
 * main class for the refactoring rename method,
 * changes pages of the wizard in case the refactoring is ambiguous
 * @author reto
 *
 */
public class AmbiguousRenameRefactoring extends RenameRefactoring {

	public AmbiguousRenameRefactoring(IAmbiguousRenameInfo info, String refactoringName) {
		super((RenameInfo)info, refactoringName);
		if(info.refactoringIsAmbiguous()){
			pages.clear();
			pages.add(new AmbiguousRenameFirstPage(refactoringName, info));
			pages.add(new RenameFileSelectionPage(refactoringName,info));
		}
	}

}
