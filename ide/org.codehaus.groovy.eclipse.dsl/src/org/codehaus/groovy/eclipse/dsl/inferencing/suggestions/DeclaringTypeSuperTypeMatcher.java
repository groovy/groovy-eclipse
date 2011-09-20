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

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager.ProjectSuggestions;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IProject;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-09-08
 */
public class DeclaringTypeSuperTypeMatcher {

    private IProject project;

    public DeclaringTypeSuperTypeMatcher(IProject project) {
        this.project = project;
    }

    /**
     * Finds all the declaring types for the given current type and it's super
     * types that
     * are registered in the given project and contain at least one suggestion.
     * May be empty, but not null
     * 
     * @param context
     * @return
     */
    public List<GroovySuggestionDeclaringType> getAllSuperTypes(GroovyDSLDContext context) {
        ProjectSuggestions suggestions = InferencingSuggestionsManager.getInstance().getSuggestions(project);
        List<GroovySuggestionDeclaringType> superTypes = new ArrayList<GroovySuggestionDeclaringType>();
        if (suggestions != null) {
            for (GroovySuggestionDeclaringType declaringType : suggestions.getDeclaringTypes()) {
                if (context.matchesType(declaringType.getName())) {
                    superTypes.add(declaringType);
                }
            }
        }

        return superTypes;
    }
}
