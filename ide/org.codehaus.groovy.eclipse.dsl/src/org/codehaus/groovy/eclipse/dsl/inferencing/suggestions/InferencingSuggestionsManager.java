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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        // TODO: no support yet for XML file being changed outside off the
        // manager. Assume for now that only one lookup is necessary per runtime
        // session.
        ProjectSuggestions suggestionList = perProjectSuggestions.get(project);
        if (suggestionList == null) {
            suggestionList = new ProjectSuggestions();
            perProjectSuggestions.put(project, suggestionList);
        }
        return suggestionList;
    }

    public boolean addSuggestion(IGroovySuggestion suggestion) {
        return false;

    }

    protected Set<GroovySuggestionDeclaringType> lookUpSuggestions(IProject project) {
        return null;
    }

    public boolean deleteDeclaringType(GroovySuggestionDeclaringType type) {
        return false;
    }

    public boolean deleteSuggestion(IGroovySuggestion suggestion) {
        return false;
    }

    /**
     * Declaring types are unique per project, even though they conceptually
     * model the same declaring type (for example, a declaring type
     * object for type java.lang.String will be different for projects P1 and
     * P2).
     * 
     * @param declaringTypeName
     * @param project
     * @return
     */
    public GroovySuggestionDeclaringType getDeclaringType(String declaringTypeName, IProject project) {
        if (declaringTypeName == null || project == null) {
            return null;
        }
        ProjectSuggestions suggestion = perProjectSuggestions.get(project);
        if (suggestion != null) {
            return suggestion.get(declaringTypeName);
        }
        return null;
    }

    public class ProjectSuggestions {
        private Map<String, GroovySuggestionDeclaringType> suggestions;

        protected ProjectSuggestions() {
            suggestions = new HashMap<String, GroovySuggestionDeclaringType>();
        }

        public GroovySuggestionDeclaringType get(String declaringTypeName) {
            GroovySuggestionDeclaringType declaringType = suggestions.get(declaringTypeName);
            if (declaringType == null) {
                declaringType = new GroovySuggestionDeclaringType(declaringTypeName);
                suggestions.put(declaringTypeName, declaringType);
            }
            return declaringType;
        }

        public void remove(String declaringTypeName) {
            suggestions.remove(declaringTypeName);
        }

        /**
         * Copy of the suggestions. Changes will not reflect in the actual
         * suggestions.
         * 
         * @return
         */
        public Map<String, GroovySuggestionDeclaringType> getSuggestions() {
            return suggestions;
        }

    }

}
