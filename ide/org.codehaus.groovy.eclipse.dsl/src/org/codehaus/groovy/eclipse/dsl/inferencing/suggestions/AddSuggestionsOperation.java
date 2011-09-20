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

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager.ProjectSuggestions;
import org.eclipse.core.resources.IProject;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-09-15
 */
public class AddSuggestionsOperation extends AbstractCreateOperation {

    public AddSuggestionsOperation(IProject project, IBaseGroovySuggestion suggestionContext) {
        super(project, suggestionContext);
    }

    protected ValueStatus run(SuggestionDescriptor descriptor) {

        ProjectSuggestions suggestions = InferencingSuggestionsManager.getInstance().getSuggestions(getProject());
        if (suggestions != null) {
            IGroovySuggestion suggestion = suggestions.addSuggestion(descriptor);
            return ValueStatus.getValidStatus(suggestion);
        }

        return ValueStatus.getErrorStatus(descriptor, "Project does not appear to be an accessible Groovy project");

    }

}
