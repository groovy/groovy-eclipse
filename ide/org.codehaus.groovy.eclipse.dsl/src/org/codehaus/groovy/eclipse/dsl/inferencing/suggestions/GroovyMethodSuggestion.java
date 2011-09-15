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

/**
 * 
 * @author Nieraj Singh
 * @created Apr 19, 2011
 */
public class GroovyMethodSuggestion extends GroovySuggestion {

    private List<MethodParameter> parameters;

    private boolean useNamedArgument;

    public GroovyMethodSuggestion(GroovySuggestionDeclaringType declaringType, List<MethodParameter> arguments,
            boolean useNameArguments, String name, String type, boolean isStatic, String javaDoc, boolean isActive) {
        super(declaringType, name, type, isStatic, javaDoc, isActive);
        this.useNamedArgument = useNameArguments;
        this.parameters = arguments;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public boolean useNamedArguments() {
        return useNamedArgument;
    }

    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + (useNamedArgument ? 1231 : 1237);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroovyMethodSuggestion other = (GroovyMethodSuggestion) obj;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (useNamedArgument != other.useNamedArgument)
            return false;
        return true;
    }
}
