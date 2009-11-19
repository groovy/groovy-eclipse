/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRenameParticipants;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.participation.GroovyRefactoringParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Abstract Refactoring Participant who delegates all actions 
 * to a <code>RefactoringProvider</code>. 
 * 
 * @author Stefan Reinhard
 */
public abstract class GroovyRenameParticipant extends GroovyRefactoringParticipant {

	private Refactoring refactoring;
	
	@Override
	public abstract boolean initialize(RefactoringProvider provider);

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) 
	throws CoreException, OperationCanceledException {
		return refactoring.checkInitialConditions(pm);
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) 
	throws CoreException, OperationCanceledException {
		return refactoring.checkFinalConditions(pm);
	}

	@Override
	public Change createChange(IProgressMonitor pm)
	throws CoreException, OperationCanceledException {
		return refactoring.createChange(pm);
	}

	@Override
	public String getName() {
		return refactoring.getName();
	}

	public Refactoring getRefactoring() {
		return refactoring;
	}

	public void setRefactoring(Refactoring refactoring) {
		this.refactoring = refactoring;
	}
	
	

}
