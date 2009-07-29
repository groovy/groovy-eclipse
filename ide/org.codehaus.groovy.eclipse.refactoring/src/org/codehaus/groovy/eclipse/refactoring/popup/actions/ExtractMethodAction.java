/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.popup.actions;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.extractMethod.ExtractMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.extractMethod.ExtractMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.core.extractMethod.ExtractMethodRefactoring;
import org.eclipse.jface.action.IAction;

public class ExtractMethodAction extends GroovyRefactoringAction {

	public void run(IAction action) {

		if (initRefactoring()) {
			ExtractMethodProvider extractMethodProvider = new ExtractMethodProvider(docProvider, selection, GroovyPlugin.getDefault().getPluginPreferences());
			ExtractMethodInfo info = new ExtractMethodInfo(extractMethodProvider);
			GroovyRefactoring groovyRefactoring = new ExtractMethodRefactoring(info);
			openRefactoringWizard(groovyRefactoring);
		}
	}

}
