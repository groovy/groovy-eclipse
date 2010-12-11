/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.GroovyRenameLocalVariableProcessor;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.RenameLocalGroovyVariableContribution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.refactoring.UserInterfaceManager;
import org.eclipse.jdt.internal.ui.refactoring.reorg.RenameLocalVariableWizard;
import org.eclipse.jdt.internal.ui.refactoring.reorg.RenameUserInterfaceManager;
import org.eclipse.jdt.internal.ui.refactoring.reorg.RenameUserInterfaceStarter;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

/**
 * @author Stefan Reinhard
 */
public class JavaRefactoringDispatcher {
    
    
    static {
        // register our groovy rename processor
        UserInterfaceManager uiManager = RenameUserInterfaceManager.getDefault();
        ReflectionUtils.executePrivateMethod(UserInterfaceManager.class, "put", new Class[] {Class.class, Class.class, Class.class}, 
                uiManager, new Object[] { GroovyRenameLocalVariableProcessor.class, RenameUserInterfaceStarter.class, RenameLocalVariableWizard.class });
    }
	
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
		} else if (element instanceof ILocalVariable) {
			return createLocalVariableRefactoring();
		}
		return null;
	}
	
    private RenameSupport createLocalVariableRefactoring() throws CoreException {
        RenameJavaElementDescriptor descriptor = createDescriptorForLocalVariable();
        return RenameSupport.create(descriptor);
    }

    public RenameJavaElementDescriptor createDescriptorForLocalVariable() {
        Map<String, String> args = new HashMap<String, String>();
        args.put("name", getNewName());
        args.put("input", element.getHandleIdentifier());
        RenameJavaElementDescriptor descriptor = 
            new RenameJavaElementDescriptor(IJavaRefactorings.RENAME_LOCAL_VARIABLE, 
                    element.getJavaProject().getElementName(), "Rename " + element.getElementName(), 
                    null, args, RenameSupport.UPDATE_REFERENCES);
        ReflectionUtils.setPrivateField(RefactoringDescriptor.class, "fRefactoringId", descriptor, RenameLocalGroovyVariableContribution.ID);
        return descriptor;
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
