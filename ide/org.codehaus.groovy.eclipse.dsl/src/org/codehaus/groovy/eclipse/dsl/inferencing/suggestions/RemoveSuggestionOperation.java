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

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager.ProjectSuggestions;
import org.eclipse.core.resources.IProject;

/**
 * Removes given selections from the specified project.
 * 
 * @author Nieraj Singh
 * @created 2011-09-15
 */
public class RemoveSuggestionOperation extends AbstractSuggestionOperation {

    private List<IBaseGroovySuggestion> selections;

    /**
     * Project and Selections must not be null
     */
    public RemoveSuggestionOperation(IProject project, List<IBaseGroovySuggestion> selections) {
        super(project, null);
        this.selections = selections;
    }

    public ValueStatus run() {
        ProjectSuggestions suggestions = InferencingSuggestionsManager.getInstance().getSuggestions(getProject());
        if (suggestions != null) {
            for (Object obj : selections) {
                if (obj instanceof GroovySuggestionDeclaringType) {

                    suggestions.removeDeclaringType((GroovySuggestionDeclaringType) obj);
                } else if (obj instanceof IGroovySuggestion) {
                    IGroovySuggestion suggestion = (IGroovySuggestion) obj;
                    GroovySuggestionDeclaringType declaringType = suggestion.getDeclaringType();
                    declaringType.removeSuggestion(suggestion);
                    // Also remove the declaring type if no suggestions are left
                    if (declaringType.getSuggestions().isEmpty()) {
                        suggestions.removeDeclaringType(declaringType);
                    }
                }
            }
        }
        return ValueStatus.getValidStatus(null);
    }
}
