/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.inlineMethod;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringInfo;

public class InlineMethodInfo extends RefactoringInfo {
	
	private InlineMethodProvider provider;

	public InlineMethodInfo(InlineMethodProvider provider) {
		super(provider);
		this.provider = provider;
	}
	
	public boolean isInlineAllInvocations() {
		return provider.isInlineAllInvocations();
	}

	public void setInlineAllInvocations(boolean inlineAllInvocations) {
		provider.setInlineAllInvocations(inlineAllInvocations);
	}

	public boolean isDeleteMethod() {
		return provider.isDeleteMethod();
	}

	public void setDeleteMethod(boolean deleteMethod) {
		provider.setDeleteMethod(deleteMethod);
	}

	public boolean isMethodDeclarationSelected() {
		return provider.isMethodDeclarationSelected();
	}

	public void setMethodDeclarationSelected(boolean methodDeclarationSelected) {
		provider.setMethodDeclarationSelected(methodDeclarationSelected);
	}
	
}
