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

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-09-13
 */
public class JavaValidTypeRule extends AbstractJavaTypeVerifiedRule {

    public JavaValidTypeRule(IJavaProject project) {
        super(project);
    }

    public ValueStatus checkValidity(Object value) {
        if (value instanceof String) {
            String name = (String) value;
            IStatus status = JavaConventions.validateJavaTypeName(name, JavaCore.VERSION_1_3, JavaCore.VERSION_1_3);

            if (status.getSeverity() != IStatus.ERROR) {

                // Check if the type exists
                try {
                    IType type = getActualType(name);
                    if (type != null) {
                        return ValueStatus.getValidStatus(value);
                    } else {
                        return ValueStatus.getErrorStatus(value, THE_SPECIFIED_JAVA_TYPES_DO_NOT_EXIST + name);
                    }
                } catch (JavaModelException e) {
                    GroovyDSLCoreActivator.logException(e);
                }
            } else {
                return ValueStatus.getErrorStatus(value, INVALID_JAVA);
            }
        }
        return ValueStatus.getErrorStatus(value, INVALID_JAVA);
    }

}
