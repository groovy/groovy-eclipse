/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.popup.actions;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodRefactoring;
import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author mike klenk
 * 
 */
public class InlineMethodAction extends GroovyRefactoringAction {

	public void run(IAction action) {
		if (initRefactoring()) {
			InlineMethodProvider provider = new InlineMethodProvider(docProvider, selection);
			InlineMethodInfo info = new InlineMethodInfo(provider);

			GroovyRefactoring groovyRefactoring = new InlineMethodRefactoring(info);
			openRefactoringWizard(groovyRefactoring);
		}
	}

	@Override
    protected int getUIFlags() {
		return RefactoringWizard.DIALOG_BASED_USER_INTERFACE
				| RefactoringWizard.PREVIEW_EXPAND_FIRST_NODE;
	}
}
