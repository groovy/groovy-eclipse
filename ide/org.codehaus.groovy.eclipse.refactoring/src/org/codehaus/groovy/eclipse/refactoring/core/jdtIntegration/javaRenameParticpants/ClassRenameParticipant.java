/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRenameParticpants;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRefactorings.ClassRenameConverter;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassProvider;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

/**
 * Keeps Groovy updated after Java class rename refactorings
 * 
 * @author Stefan Reinhard
 */
public class ClassRenameParticipant extends JavaRenameParticipant {
	
	@Override
	protected boolean initialize(IJavaElement element) {
		if (element instanceof IType) {
			IType renamed = (IType) element;
			RenameClassProvider provider = ClassRenameConverter.createProvider(renamed);
			provider.setNewName(getArguments().getNewName());
			setProvider(provider);
			this.element = element;
			return provider.hasCandidates();
		}
		return false;
	}
	
	@Override
	public String getName() {
		return "Class Rename Participant";
	}
	
}
