/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.GroovyRenameLocalVariableProcessor;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.RenameLocalGroovyVariableContribution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
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

    //--------------------------------------------------------------------------

    public RenameJavaElementDescriptor createDescriptorForLocalVariable() {
        Map<String, String> args = new HashMap<String, String>();
        args.put("name", getNewName());
        args.put("input", element.getHandleIdentifier());
        RenameJavaElementDescriptor descriptor = new RenameJavaElementDescriptor(
            IJavaRefactorings.RENAME_LOCAL_VARIABLE, element.getJavaProject().getElementName(),
            "Rename " + element.getElementName(), null, args, RenameSupport.UPDATE_REFERENCES);
        ReflectionUtils.setPrivateField(RefactoringDescriptor.class, "fRefactoringId", descriptor,
            RenameLocalGroovyVariableContribution.ID);
        return descriptor;
    }

    public RenameSupport dispatchJavaRenameRefactoring() throws CoreException {
        switch (element.getElementType()) {
        case IJavaElement.TYPE:
            return RenameSupport.create((IType) element, getNewName(), DEFAULT_FLAGS);

        case IJavaElement.FIELD:
            return RenameSupport.create((IField) element, getNewName(), DEFAULT_FLAGS |
                RenameSupport.UPDATE_GETTER_METHOD | RenameSupport.UPDATE_SETTER_METHOD);

        case IJavaElement.METHOD:
            return RenameSupport.create((IMethod) element, getNewName(), DEFAULT_FLAGS);

        case IJavaElement.LOCAL_VARIABLE:
            return RenameSupport.create(createDescriptorForLocalVariable());

        /*case IJavaElement.PACKAGE_FRAGMENT:
            return RenameSupport.create((IPackageFragment) element, getNewName(), DEFAULT_FLAGS);*/
        }
        return null;
    }

    private static final int DEFAULT_FLAGS = RenameSupport.UPDATE_REFERENCES | RenameSupport.UPDATE_TEXTUAL_MATCHES;
}
