/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Base Class of all RefactoringWizards for the Groovy language
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class GroovyRefactoringWizard extends RefactoringWizard {
	
	protected GroovyRefactoring refactoring;
	
	public GroovyRefactoringWizard(GroovyRefactoring refactoring, int flags) {
		super(refactoring, flags);
		this.refactoring = refactoring;
		setWindowTitle(refactoring.getName());
		setDefaultPageTitle(refactoring.getName());
	}
	
	@Override
    protected void addUserInputPages() {
		for (IWizardPage page : refactoring.getPages()){
			addPage(page);
		}
	}

}
