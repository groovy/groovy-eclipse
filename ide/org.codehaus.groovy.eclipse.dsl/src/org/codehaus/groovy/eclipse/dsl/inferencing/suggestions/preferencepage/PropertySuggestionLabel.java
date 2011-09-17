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

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyPropertySuggestion;

/**
 * 
 * @author Nieraj Singh
 * @created Apr 21, 2011
 */
public class PropertySuggestionLabel extends AbstractSuggestionLabel {
    private GroovyPropertySuggestion property;

    public PropertySuggestionLabel(GroovyPropertySuggestion property) {
        this.property = property;
    }

    protected String constructName() {
        if (property == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();

        String name = property.getName();

        buffer.append(name);
 

        String type = property.getType();

        if (type != null && type.length() > 0) {
            buffer.append(EMPTY_SPACE);
            buffer.append(COLON);
            buffer.append(EMPTY_SPACE);
            buffer.append(type);
        }

        return buffer.toString();
    }

}
