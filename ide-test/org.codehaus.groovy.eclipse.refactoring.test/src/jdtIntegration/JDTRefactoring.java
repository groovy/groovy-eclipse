/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author Stefan Reinhard
 */
public class JDTRefactoring {

	public static void refactor(RefactoringContribution contrib,
			IJavaElement element, String newName) {

		RenameJavaElementDescriptor descriptor = 
			(RenameJavaElementDescriptor) contrib.createDescriptor();

		descriptor.setJavaElement(element);
		descriptor.setNewName(newName);
		descriptor.setUpdateReferences(true);
		RefactoringStatus status = descriptor.validateDescriptor();
		NullProgressMonitor pm = new NullProgressMonitor();

		try {
			Refactoring refactoring = descriptor.createRefactoring(status);
			status.merge(refactoring.checkInitialConditions(pm));
			status.merge(refactoring.checkFinalConditions(pm));
			Change change = refactoring.createChange(pm);
			change.perform(pm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
