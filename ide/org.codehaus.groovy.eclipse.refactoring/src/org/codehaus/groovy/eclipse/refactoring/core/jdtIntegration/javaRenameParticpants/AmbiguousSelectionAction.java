/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRenameParticpants;

import java.lang.reflect.Constructor;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IAmbiguousRenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.ProgrammaticalRenameRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyDummyRefactoringWizard;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Stefan Reinhard
 */
public class AmbiguousSelectionAction implements Runnable {
	
	private GroovyDummyRefactoringWizard wizard;
	private GroovyRefactoring refactoring;
	private RenameInfo info;
	private static Class<? extends GroovyRefactoring> refactoringClass = 
		ProgrammaticalRenameRefactoring.class;

	public AmbiguousSelectionAction(RefactoringProvider provider) {
		if (provider instanceof RenameFieldProvider) {
			info = new RenameFieldInfo(provider);
		} else if (provider instanceof RenameMethodProvider) {
			info = new RenameMethodInfo(provider);
		} else if (provider instanceof IRenameProvider){
			info = new RenameInfo(provider);
		} else {
			throw new IllegalArgumentException("Refactoring provider should be an IRenameProvider, " +
					"but was of type " + provider.getClass().getCanonicalName());
		}
		initRefactoringWizard();
	}

	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		Shell shell = window.getShell();
		
		RefactoringWizardOpenOperation op = 
			new RefactoringWizardOpenOperation(wizard);
		
		String titleForFailedChecks = "Refactoring"; //$NON-NLS-1$
		
		try {
			op.run(shell, titleForFailedChecks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private int getUIFlags() {
		return RefactoringWizard.DIALOG_BASED_USER_INTERFACE | 
			RefactoringWizard.NO_PREVIEW_PAGE;
	}
	
	public GroovyRefactoring getRefactoring() {
		return refactoring;
	}
	
	public boolean isAmbigous() {
		if (info instanceof IAmbiguousRenameInfo) {
			return ((IAmbiguousRenameInfo) info).refactoringIsAmbiguous();
		}
		return false;
	}
	
	private void initRefactoringWizard() {
		try {
			Constructor<? extends GroovyRefactoring> ctor = refactoringClass.getConstructor(RenameInfo.class, String.class);
			refactoring = ctor.newInstance(info, GroovyRefactoringMessages.RenameFieldRefactoring);
			wizard = new GroovyDummyRefactoringWizard(refactoring, getUIFlags());
		} catch (Exception e) {
			GroovyCore.logException("Exception initializing refactoring wizard", e);
		}
	}

	public static void setRefactoring(Class<? extends GroovyRefactoring> g) {
		refactoringClass = g;
	}
}
