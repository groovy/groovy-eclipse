/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRenameParticpants;

import java.lang.reflect.Field;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameRefactoring;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ui.PlatformUI;

/**
 * The default implementation for all Java <code>RenameParticipant</code>.
 * This class is extended for specific rename events like method renames.
 * Delegates the Refactoring methods to a <code>RefactoringProvider</code>.
 * 
 * @author Stefan Reinhard
 */
public abstract class JavaRenameParticipant extends RenameParticipant {

	protected RefactoringProvider provider;
	private AmbiguousSelectionAction selectionWizard;
	protected IJavaElement element;
	
	/**
	 * Return true if the Participant should contribute to the 
	 * refactoring of the currently renamed element.
	 * @param element The renamed Java element
	 */
	protected abstract boolean initialize(IJavaElement element);
	
	@Override
	protected boolean initialize(Object element) {
		if (getArguments().getUpdateReferences() && element instanceof IJavaElement) {	
			IJavaElement javaElement = (IJavaElement)element;
			return initialize(javaElement);
		} else {
			return false;
		}
	}
	
	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) 
	throws OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		
		selectionWizard = new AmbiguousSelectionAction(provider);
		
		if (selectionWizard.isAmbigous()) {
			PlatformUI.getWorkbench().getDisplay().syncExec(selectionWizard);
		}

		try { status = provider.checkFinalConditions(pm); } 
		catch (CoreException e) { GroovyCore.logException("Problem with refactoring", e); }
		return status;
	}

	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException, OperationCanceledException {
		
		try {
			ProcessorBasedRefactoring refactoring = getProcessor().getRefactoring();
			Field textChanges = ProcessorBasedRefactoring.class.getDeclaredField("fTextChangeMap");
			textChanges.setAccessible(true);
			Map<Object, TextChange> changes = (Map<Object,TextChange>) textChanges.get(refactoring);
			for (TextChange javaEdit : changes.values()) {
				if (javaEdit instanceof TextFileChange) {
					TextFileChange tfc = (TextFileChange) javaEdit;
					IFile remoteRile = tfc.getFile();
					if (remoteRile.getFileExtension().equals("groovy")) {
						tfc.setEnabled(false);
						tfc.getEdit().removeChildren();
					}
				}
			}
			
			provider.setJavaChanges(changes);
		} catch (Exception e) {
			
		}

		return provider.createGroovyChange(pm).createChange();
	}
	
	protected void setProvider(RefactoringProvider provider) {
		this.provider = provider;
	}

	@Override
	public String getName() {
		return "GroovyRenameParticipant";
	}

}
