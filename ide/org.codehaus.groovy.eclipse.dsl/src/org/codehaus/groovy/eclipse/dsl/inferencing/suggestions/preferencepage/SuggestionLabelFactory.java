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

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyPropertySuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IGroovySuggestion;

/**
 * 
 * @author Nieraj Singh
 * @created Apr 21, 2011
 */
public class SuggestionLabelFactory {

    public ISuggestionLabel getSuggestionLabel(IGroovySuggestion suggestion) {
        if (suggestion instanceof GroovyMethodSuggestion) {
            return new MethodSuggestionLabel((GroovyMethodSuggestion) suggestion);
        } else if (suggestion instanceof GroovyPropertySuggestion) {
            return new PropertySuggestionLabel((GroovyPropertySuggestion) suggestion);
        }
        return null;
    }

}
