/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.refactoring.RenameSupport;

/**
 * @author Stefan Reinhard
 */
public class JavaRefactoringDispatcher {
	
	private IJavaElement element;
	
	public JavaRefactoringDispatcher(IJavaElement element) {
		this.element = element;
	}
	
	public RenameSupport dispatchJavaRenameRefactoring() throws CoreException {
		if (element instanceof IType) {
			return createTypeRefactoring((IType)element);
		} else if (element instanceof IField) {
			return createFieldRefactoring((IField)element);
		} else if (element instanceof IMethod) {
			return createMethodRefactoring((IMethod)element);
		}
		return null;
	}
	
	private RenameSupport createTypeRefactoring(IType type) throws CoreException {
		return RenameSupport.create(type, getNewName(), RenameSupport.UPDATE_REFERENCES);
	}

	private RenameSupport createFieldRefactoring(IField field) throws CoreException {
		return RenameSupport.create(field, getNewName(), 
				RenameSupport.UPDATE_REFERENCES | 
				RenameSupport.UPDATE_GETTER_METHOD |
				RenameSupport.UPDATE_SETTER_METHOD);
	}
	
	private RenameSupport createMethodRefactoring(IMethod method) throws CoreException {
		return RenameSupport.create(method, getNewName(), RenameSupport.UPDATE_REFERENCES);
	}
	
	private String newName;
	
	public String getNewName() {
		if (newName != null) {
			return newName;
		} else {
			return element.getElementName();
		}
	}
	
	public void setNewName(String name) {
		newName = name;
	}
}
