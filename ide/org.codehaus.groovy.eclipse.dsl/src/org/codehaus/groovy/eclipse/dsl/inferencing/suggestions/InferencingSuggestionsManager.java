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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-09
 */
public class InferencingSuggestionsManager {

    private Map<IProject, ProjectSuggestions> perProjectSuggestions;

    private static InferencingSuggestionsManager manager;

    private InferencingSuggestionsManager() {
        // Singleton
    }

    public static InferencingSuggestionsManager getInstance() {
        if (manager == null) {
            manager = new InferencingSuggestionsManager();
        }
        return manager;
    }

    public void commitChanges() {
        // Serialise
    }

    public void restore() {
        // Discard any in memory changes and restore from current XML.
    }

    /**
     * Never null. may be empty. Original copy.
     * 
     * @param project
     * @return
     */

    public ProjectSuggestions getSuggestions(IProject project) {
        if (perProjectSuggestions == null) {
            perProjectSuggestions = new HashMap<IProject, ProjectSuggestions>();
        }

        ProjectSuggestions suggestionList = perProjectSuggestions.get(project);
        if (suggestionList == null) {
            suggestionList = new ProjectSuggestions();
            perProjectSuggestions.put(project, suggestionList);
        }
        return suggestionList;
    }

    public class ProjectSuggestions {
        private Map<String, GroovySuggestionDeclaringType> suggestions;

        protected ProjectSuggestions() {
            suggestions = new HashMap<String, GroovySuggestionDeclaringType>();
        }

        public GroovySuggestionDeclaringType getDeclaringType(String declaringTypeName) {
            GroovySuggestionDeclaringType declaringType = suggestions.get(declaringTypeName);
            if (declaringType == null) {
                declaringType = new GroovySuggestionDeclaringType(declaringTypeName);
                suggestions.put(declaringTypeName, declaringType);
            }
            return declaringType;
        }

        public void removeDeclaringType(GroovySuggestionDeclaringType declaringType) {
            suggestions.remove(declaringType.getName());
        }

        /**
         * 
         * @return
         */
        public Collection<GroovySuggestionDeclaringType> getDeclaringTypes() {
            return suggestions.values();
        }

    }

}
