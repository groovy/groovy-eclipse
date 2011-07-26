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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.preferencepage;

import java.util.List;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion.MethodParameter;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-04-20
 */
public class MethodSuggestionLabel extends AbstractSuggestionLabel {

    private GroovyMethodSuggestion suggestion;

    public MethodSuggestionLabel(GroovyMethodSuggestion suggestion) {
        this.suggestion = suggestion;
    }

    protected String constructName() {
        if (suggestion == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();

        String name = suggestion.getName();

        buffer.append(name);
        buffer.append(EMPTY_SPACE);
        buffer.append(COLON);
        buffer.append(EMPTY_SPACE);

        String typeName = suggestion.getType();
        buffer.append(typeName);

        List<MethodParameter> methodArguments = suggestion.getMethodArguments();
        if (methodArguments != null) {
            buffer.append(OPEN_PAR);
            int size = methodArguments.size();

            for (MethodParameter argument : methodArguments) {
                buffer.append(argument.getType());
                buffer.append(EMPTY_SPACE);
                buffer.append(argument.getName());

                // if there is more than one argument, comma separate them
                if (--size > 0) {
                    buffer.append(COMMA);
                    buffer.append(EMPTY_SPACE);
                }
            }

            buffer.append(CLOSE_PAR);
        }
        return buffer.toString();

    }

}
