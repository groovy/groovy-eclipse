/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRenameParticipants;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.MethodUpdateRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;

/**
 * @author Stefan Reinhard
 */
public class MethodRenameParticipant extends GroovyRenameParticipant {

	@Override
	public boolean initialize(RefactoringProvider provider) {
		if (provider instanceof RenameMethodProvider) {
			RenameMethodProvider methodProvider = (RenameMethodProvider)provider;
			this.setRefactoring(new MethodUpdateRefactoring(methodProvider));
			return true;
		}
		return false;
	}

}
