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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-09-12
 */
public class JavaValidIdentifierRule implements IValueCheckingRule {

    protected static final String INVALID_JAVA_IDENTIFIER = "Invalid value";

    public ValueStatus checkValidity(Object value) {
        if (value instanceof String) {
            String text = (String) value;
            IStatus status = checkJavaType(text);

            if (status.getSeverity() == IStatus.ERROR) {
                return ValueStatus.getErrorStatus(value, status.getMessage());
            } else {
                return ValueStatus.getValidStatus(value);
            }
        }
        return ValueStatus.getErrorStatus(value);
    }

    protected IStatus checkJavaType(String value) {
        return JavaConventions.validateIdentifier(value, JavaCore.VERSION_1_3, JavaCore.VERSION_1_3);
    }
}
