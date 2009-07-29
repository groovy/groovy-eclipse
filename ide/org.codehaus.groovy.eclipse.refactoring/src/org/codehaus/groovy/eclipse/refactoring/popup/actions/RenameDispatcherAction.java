/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.popup.actions;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.rename.NoRefactoringForASTNodeException;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameDispatcher;
import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author martin
 * 
 */
public class RenameDispatcherAction extends GroovyRefactoringAction {

	public void run(IAction action) {
		if (initRefactoring()) {
			GroovyRefactoring refactoring = null;
			RenameDispatcher dispatcher = new RenameDispatcher(docProvider, selection);
			try {
				refactoring = dispatcher.dispatchRenameRefactoring();
				openRefactoringWizard(refactoring);
			} catch (NoRefactoringForASTNodeException e) {
				displayErrorDialog(e.getMessage());
			}
		}
	}

	@Override
    protected int getUIFlags() {
		return RefactoringWizard.DIALOG_BASED_USER_INTERFACE
				| RefactoringWizard.PREVIEW_EXPAND_FIRST_NODE;
	}

}
