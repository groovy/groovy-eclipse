/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.inlineMethod;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.codehaus.groovy.eclipse.refactoring.ui.pages.inlineMethod.InlineMethodPage;

/**
 * Extract the current selection in a separate method and call this one instead.
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class InlineMethodRefactoring extends GroovyRefactoring {

	public InlineMethodRefactoring(InlineMethodInfo info) {
		super(info);
		setName(GroovyRefactoringMessages.InlineMethodRefactoring);
		pages.add(new InlineMethodPage(GroovyRefactoringMessages.InlineMethodRefactoring,info));
	}
}
