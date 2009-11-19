/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRenameParticipants;

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.FieldUpdateRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.MutatorUpdateRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;

/**
 * @author Stefan Reinhard
 */
public class FieldRenameParticipant extends GroovyRenameParticipant {

	@Override
	public boolean initialize(RefactoringProvider provider) {
		if (provider instanceof RenameFieldProvider) {
			RenameFieldProvider fieldProvider = (RenameFieldProvider)provider;
			FieldPattern pattern = fieldProvider.getFieldPattern();
			FieldNode fieldNode = pattern.getOriginalFieldNode();
			if (fieldNode != null && fieldNode.isSynthetic()) {
				this.setRefactoring(new MutatorUpdateRefactoring(fieldProvider));
			} else {
				this.setRefactoring(new FieldUpdateRefactoring(fieldProvider));
			}
			return true;
		}
		return false;
	}

}
