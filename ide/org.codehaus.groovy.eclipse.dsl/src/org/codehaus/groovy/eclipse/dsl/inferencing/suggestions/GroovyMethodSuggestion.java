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

    public static class MethodParameter {

        private String name;

        private String type;

        public MethodParameter(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

    }

}
