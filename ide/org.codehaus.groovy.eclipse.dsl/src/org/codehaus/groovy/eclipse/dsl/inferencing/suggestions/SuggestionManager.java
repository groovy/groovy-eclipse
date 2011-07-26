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

import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IProject;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-31
 */
public class SuggestionManager {

    private IProject project;

    public SuggestionManager(IProject project) {
        this.project = project;
    }

    public GroovySuggestionDeclaringType getDeclaringTypes(GroovyDSLDContext pattern) {
        String declaringTypeName = getDeclaringTypeName(pattern);
        if (declaringTypeName != null) {
            // TODO: MUST MATCH subtypes, not just exact declaring type.  Add support for it
            return InferencingSuggestionsManager.getInstance().getSuggestions(project).get(declaringTypeName);
        }
        return null;
    }

    public String getDeclaringTypeName(GroovyDSLDContext pattern) {
        return pattern.getCurrentType().getName();
    }

}
