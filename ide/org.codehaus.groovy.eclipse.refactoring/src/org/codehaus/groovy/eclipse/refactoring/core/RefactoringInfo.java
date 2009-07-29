/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core;

import java.util.Observable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


/**
 * @author Michael Klenk mklenk@hsr.ch
 */

public class RefactoringInfo extends Observable{
	
	protected RefactoringProvider provider;
	
	public RefactoringInfo(RefactoringProvider provider) {
		this.provider = provider;
	}

	public UserSelection getSelection() {
		return provider.getSelection();
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return provider.checkFinalConditions(pm);
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return provider.checkInitialConditions(pm);
	}

	public GroovyChange createGroovyChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return provider.createGroovyChange(pm);
	}

}
