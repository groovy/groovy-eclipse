/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration;

import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.RenameLocalProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class GroovyRenameParticipant extends RenameParticipant {
	
	private RenameLocalProvider renameLocalProvider;
	//private WorkspaceDocumentProvider docProvider;

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		try {
			status = renameLocalProvider.checkInitialConditions(pm);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return status;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
        return renameLocalProvider.createGroovyChange(pm).createChange();
	}

	@Override
	public String getName() {
		return "GroovyRenameParticipant";
	}

	@Override
	protected boolean initialize(Object element) {
//		if (element instanceof IMethod) {
//			UserSelection selection = new UserSelection(698,0);
//			IFile renameFile = GroovyModel.getModel().getIFileForSrcFile("d.groovy");
//			docProvider = new WorkspaceDocumentProvider(renameFile);
//			renameLocalProvider = new RenameLocalProvider(docProvider,selection);
//			renameLocalProvider.setNewName(getArguments().getNewName());
//			return true;
//		}
		return false;
	}
}
