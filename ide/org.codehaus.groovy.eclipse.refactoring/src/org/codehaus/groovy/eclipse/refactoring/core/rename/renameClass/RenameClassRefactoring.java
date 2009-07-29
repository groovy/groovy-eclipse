/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass;

import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameRefactoring;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.codehaus.groovy.eclipse.refactoring.ui.pages.rename.RenameClassPage;


public class RenameClassRefactoring extends RenameRefactoring {

	public RenameClassRefactoring(RenameInfo info) {
		super(info, GroovyRefactoringMessages.RenameClassRefactoring);
		pages.clear();
		pages.add(new RenameClassPage(GroovyRefactoringMessages.RenameClassRefactoring, info));
	}
}