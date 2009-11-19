/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.participation;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.eclipse.ltk.core.refactoring.Refactoring;

/**
 * Base class for all Praticipants to Groovy Refactorings
 * 
 * @author Stefan Reinhard
 */
public abstract class GroovyRefactoringParticipant extends Refactoring {
	
	/**
	 * When a <code>GroovyRefactoring</code> gets initial checked, each
	 * participant registered for that Refactoring is asked via this
	 * method, if he wants to participate or not.
	 * @param provider
	 * @return participation?
	 */
	public abstract boolean initialize(RefactoringProvider provider);

}
