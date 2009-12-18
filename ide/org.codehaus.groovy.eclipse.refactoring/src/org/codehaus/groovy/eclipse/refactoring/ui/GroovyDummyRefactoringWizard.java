/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;

/**
 * @author Stefan Reinhard
 */
public class GroovyDummyRefactoringWizard extends GroovyRefactoringWizard {
	
	/**
	 * @param refactoring
	 * @param flags
	 */
	public GroovyDummyRefactoringWizard(GroovyRefactoring refactoring, int flags) {
		super(refactoring, flags);
		this.setForcePreviewReview(false);
	}
	
	@Override
    public boolean performFinish() {
		return true;
	}

}
