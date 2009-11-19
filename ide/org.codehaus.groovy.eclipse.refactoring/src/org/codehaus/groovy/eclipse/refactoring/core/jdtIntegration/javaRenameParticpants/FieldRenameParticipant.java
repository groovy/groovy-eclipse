/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRenameParticpants;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRefactorings.FieldRenameConverter;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Keeps Groovy updated after Java field rename refactorings
 * 
 * @author Stefan Reinhard
 */
public class FieldRenameParticipant extends JavaRenameParticipant {

	@Override
	protected boolean initialize(IJavaElement element) {
		if (element instanceof IField) {
			IField renamed = (IField) element;
			RenameFieldProvider provider = FieldRenameConverter.createProvider(renamed);
			provider.checkUniqueFieldDefinitions(false);
			provider.setNewName(getArguments().getNewName());
			setProvider(provider);
			
			return provider.hasCandidates();
		}
		return false;
	}

	@Override
	public String getName() {
		return "Field Rename Participant";
	}

}
