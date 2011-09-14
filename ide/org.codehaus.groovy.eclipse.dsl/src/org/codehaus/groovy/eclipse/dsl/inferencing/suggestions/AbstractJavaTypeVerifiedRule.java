/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-09-14
 */
public abstract class AbstractJavaTypeVerifiedRule implements IValueCheckingRule {

    /**
     * 
     */
    public static final String THE_SPECIFIED_JAVA_TYPES_DO_NOT_EXIST = "The specified Java type(s) does not exist: ";

    public static final String INVALID_JAVA = "Invalid Java type.";

    private IJavaProject project;

    private NameLookup nameLookup;

    public AbstractJavaTypeVerifiedRule(IJavaProject project) {
        this.project = project;
    }

    protected IJavaProject getJavaProject() {
        return project;
    }

    protected NameLookup getNameLookup() throws JavaModelException {
        if (nameLookup == null) {
            if (project instanceof JavaProject) {
                nameLookup = ((JavaProject) project).newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
            }
        }
        return nameLookup;
    }

    protected IType getActualType(String name) throws JavaModelException {
        NameLookup nameLkUp = getNameLookup();
        if (nameLkUp != null) {
            return nameLkUp.findType(name, false, NameLookup.ACCEPT_ALL);
        }
        return null;
    }

    protected String composeErrorMessage(List<String> allNonExistantTypes) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(THE_SPECIFIED_JAVA_TYPES_DO_NOT_EXIST);
        int size = allNonExistantTypes.size();
        for (String name : allNonExistantTypes) {
            buffer.append(name);
            // If more are left, comma separate the entries
            if (--size > 0) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }

}
