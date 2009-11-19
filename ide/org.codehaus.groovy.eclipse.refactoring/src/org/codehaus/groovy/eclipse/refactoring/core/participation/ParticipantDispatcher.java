/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.participation;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A <code>ParticipantDispatcher</code> checks all participants interested in a
 * specific Refactoring and is responsible to delegate the check methods to those.
 * After initial and final checks, all Changes get collected and returned.
 * 
 * @author Stefan Reinhard
 */
public class ParticipantDispatcher {
	
	private ParticipantManager manager;
	private RefactoringProvider provider;
	private List<GroovyRefactoringParticipant> activeParticipants;
	
	public ParticipantDispatcher(RefactoringProvider provider) {
		this.provider = provider;
		manager = ParticipantManager.getDefaultManager();
		activeParticipants = new LinkedList<GroovyRefactoringParticipant>();
	}
	
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
	throws CoreException {
		RefactoringStatus result = new RefactoringStatus();
		int flag = ParticipantManager.getType(provider);
		activeParticipants.clear();
		for (Class<? extends GroovyRefactoringParticipant> participant : manager.getActiveParticipants(flag)) {	
			try {
				GroovyRefactoringParticipant instance = participant.newInstance();
				if(instance.initialize(provider)) {
					RefactoringStatus status = instance.checkInitialConditions(pm);
					if (!status.hasError()) {
						result.merge(status);
						activeParticipants.add(instance);
					}
				}
			} catch (Exception e) { /*don't respect participants without default constructor*/ }
		}
		return result;
	}
	
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)  
	throws CoreException {
		RefactoringStatus status = new RefactoringStatus();
		for (GroovyRefactoringParticipant participant : activeParticipants) {
			try {
				status.merge(participant.checkFinalConditions(pm));
			} catch (Exception e) {
				status.addWarning(GroovyRefactoringMessages.ParticipantDispatcherError);
//				activeParticipants.remove(participant);
			}
		}
		return status;
	}
	
	public Change[] createChanges(IProgressMonitor pm) 
	throws OperationCanceledException, CoreException {
		LinkedList<Change> changes = new LinkedList<Change>();
		for (GroovyRefactoringParticipant participant : activeParticipants) {
			changes.add(participant.createChange(pm));
		}
		return changes.toArray(new Change[changes.size()]);
	}

}
