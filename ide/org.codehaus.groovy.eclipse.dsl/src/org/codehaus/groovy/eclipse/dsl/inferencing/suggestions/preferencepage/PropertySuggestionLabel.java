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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.preferencepage;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyPropertySuggestion;

public class PropertySuggestionLabel extends AbstractSuggestionLabel {

    private GroovyPropertySuggestion property;

    public PropertySuggestionLabel(GroovyPropertySuggestion property) {
        this.property = property;
    }

    @Override
    protected String constructName() {
        if (property == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(property.getName());
        String type = property.getType();
        if (type != null && !type.isEmpty()) {
            sb.append(EMPTY_SPACE);
            sb.append(COLON);
            sb.append(EMPTY_SPACE);
            sb.append(type);
        }
        return sb.toString();
    }
}
