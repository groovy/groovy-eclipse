/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.corext.refactoring.scripting.JavaUIRefactoringContribution;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RenameLocalGroovyVariableContribution extends JavaUIRefactoringContribution {

    public static final String ID = "org.codehaus.groovy.eclipse.refactoring.renameLocal";

    @SuppressWarnings({"rawtypes", "unchecked"})
    public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags) {
        return new RenameJavaElementDescriptor(id, project, description, comment, arguments, flags);
    }

    @Override
    public Refactoring createRefactoring(JavaRefactoringDescriptor descriptor, RefactoringStatus status) throws CoreException {
        if (descriptor instanceof RenameJavaElementDescriptor) {
            IJavaElement elt = (IJavaElement) ReflectionUtils.getPrivateField(RenameJavaElementDescriptor.class, "fJavaElement", descriptor);
            String newName = (String) ReflectionUtils.getPrivateField(RenameJavaElementDescriptor.class, "fName", descriptor);
            if (elt instanceof ILocalVariable && newName != null) {
                ILocalVariable var = (ILocalVariable) elt;
                return new RenameRefactoring(new GroovyRenameLocalVariableProcessor(var, newName, status));
            }
        }
        return null;
    }
}
