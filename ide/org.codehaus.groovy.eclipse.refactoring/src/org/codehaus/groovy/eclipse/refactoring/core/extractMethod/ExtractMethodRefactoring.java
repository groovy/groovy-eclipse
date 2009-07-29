/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.extractMethod;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.codehaus.groovy.eclipse.refactoring.ui.pages.extractMethod.ExtractMethodPage;

/**
 * @author Michael Klenk mklenk@hsr.ch
 * Extract the current selection in a separate method and call this instead.
 *
 */
public class ExtractMethodRefactoring extends GroovyRefactoring {

	public ExtractMethodRefactoring(ExtractMethodInfo info) {
		super(info);
		setName(GroovyRefactoringMessages.ExtractMethodRefactoring);
		pages.add(new ExtractMethodPage(GroovyRefactoringMessages.ExtractMethodRefactoring,info));
	}
}
