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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Nieraj Singh
 * @created Apr 19, 2011
 */
public class GroovySuggestionDeclaringType {
    private List<IGroovySuggestion> suggestions;

    private String name;

    public GroovySuggestionDeclaringType(String name) {
        this.suggestions = new ArrayList<IGroovySuggestion>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Creates a new instance of the suggestion in the specified declaring type.
     * 
     * @param type
     * @param isActive whether the suggestion should be active in the declaring
     *            type
     * @return
     */
    public IGroovySuggestion createSuggestion(SuggestionDescriptor descriptor) {

        IGroovySuggestion suggestion = new SuggestionFactory(descriptor).createSuggestion(this);

        suggestions.add(suggestion);
        return suggestion;
    }

    public boolean containsSuggestion(IGroovySuggestion suggestion) {
        return suggestions.contains(suggestion);
    }

    public IGroovySuggestion replaceSuggestion(SuggestionDescriptor descriptor, IGroovySuggestion suggestion) {

        if (suggestions.contains(suggestion)) {
            removeSuggestion(suggestion);

            IGroovySuggestion nwSuggestion = createSuggestion(descriptor);
            return nwSuggestion;
        }
        return null;

    }
    
    

    public boolean removeSuggestion(IGroovySuggestion suggestion) {
        return suggestions.remove(suggestion);
    }

    public List<IGroovySuggestion> getSuggestions() {
        return suggestions;
    }

    public boolean hasSuggestions() {
        return !suggestions.isEmpty();
    }

}
