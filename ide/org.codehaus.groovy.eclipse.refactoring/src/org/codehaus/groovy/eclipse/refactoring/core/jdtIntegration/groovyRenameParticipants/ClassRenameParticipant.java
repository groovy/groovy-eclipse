/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRenameParticipants;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.ClassUpdateRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassProvider;

/**
 * @author Stefan Reinhard
 */
public class ClassRenameParticipant extends GroovyRenameParticipant {
	
	@Override
	public boolean initialize(RefactoringProvider provider) {
		if (provider instanceof RenameClassProvider) {
			RenameClassProvider classProvider = (RenameClassProvider)provider;
			this.setRefactoring(new ClassUpdateRefactoring(classProvider));
			return true;
		}
		return false;
	}

}