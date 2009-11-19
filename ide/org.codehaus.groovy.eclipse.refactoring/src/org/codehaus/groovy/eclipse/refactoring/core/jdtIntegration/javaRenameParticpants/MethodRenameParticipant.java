/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRenameParticpants;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRefactorings.MethodRenameConverter;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

/**
 * Keeps Groovy updated after Java method rename refactorings
 * 
 * @author Stefan Reinhard
 */
public class MethodRenameParticipant extends JavaRenameParticipant {

	@Override
	protected boolean initialize(IJavaElement element) {
		if (element instanceof IMethod) {
			IMethod renamed = (IMethod) element;
			RenameMethodProvider provider = MethodRenameConverter.createProvider(renamed);
			provider.setNewName(getArguments().getNewName());
			setProvider(provider);
			return provider.hasCandidates();
		}
		return false;
	}
	
	@Override
	public String getName() {
		return "Method Rename Participant";
	}
	
	

}
