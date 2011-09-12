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

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.codeassist.Activator;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer.SuggestionsFile;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer.SuggestionsTransform;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-09
 */
public class InferencingSuggestionsManager {

    private Map<IProject, ProjectSuggestions> perProjectSuggestions;

    private static InferencingSuggestionsManager manager;

    private IProject lastModifiedProject;

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
     * For now support per project commits
     */
    public void commitChanges(IProject project) {

        // Keep track of the last project that was modified
        lastModifiedProject = project;

        ProjectSuggestions suggestions = getSuggestions(project);
        SuggestionsTransform transform = new SuggestionsTransform(suggestions);
        String result = transform.transform();

        if (result != null) {
            SuggestionsFile suggestionsFile = new SuggestionsFile(project);

            IFile file = suggestionsFile.getFile();
            writeToFile(file, result);
        }

    }

    /**
     * 
     * @return gets the last project that had suggestion commits. It may be null
     */
    public IProject getlastModifiedProject() {
        return lastModifiedProject;
    }

    protected void writeToFile(IFile file, String value) {
        if (file != null) {
            try {
                // For now replace all the contents
                file.setContents(new ByteArrayInputStream(value.getBytes()), true, true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.logError(e);
            }
        }
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

        ProjectSuggestions projectSuggestions = perProjectSuggestions.get(project);
        if (projectSuggestions == null) {
            projectSuggestions = new ProjectSuggestions(project);
            perProjectSuggestions.put(project, projectSuggestions);
        }
        return projectSuggestions;
    }

    public class ProjectSuggestions {
        private Map<String, GroovySuggestionDeclaringType> suggestions;

        private IProject project;

        protected ProjectSuggestions(IProject project) {
            suggestions = new HashMap<String, GroovySuggestionDeclaringType>();
            this.project = project;
        }

        /**
         * 
         * @return a new, clean project suggestion, registered in the
         *         suggestions manager, for the project associated with the
         *         current project selection. All
         *         current suggestions will be deleted.
         */
        public ProjectSuggestions registerNewProjectSuggestion() {
            suggestions.clear();
            ProjectSuggestions cleanProjectSuggestions = new ProjectSuggestions(project);
            perProjectSuggestions.put(project, cleanProjectSuggestions);
            return cleanProjectSuggestions;
        }

        /**
         * May be null if no suggestions have ever been added to the specified
         * declaring
         * type before. Declaring types cannot exist
         * by them selves without at least one suggestion
         * 
         * @param declaringTypeName
         * @return
         */
        public GroovySuggestionDeclaringType getExactDeclaringType(String declaringTypeName) {
            return suggestions.get(declaringTypeName);
        }
        
  

        /**
         * Creates a declaring type or returns an existing one.
         * 
         * @param declaringTypeName
         * @return
         */
        public IGroovySuggestion addSuggestion(SuggestionDescriptor descriptor) {
            String declaringTypeName = descriptor.getDeclaringTypeName();
            GroovySuggestionDeclaringType declaringType = suggestions.get(declaringTypeName);
            if (declaringType == null) {
                declaringType = new GroovySuggestionDeclaringType(declaringTypeName);
                suggestions.put(declaringTypeName, declaringType);
            }
            return declaringType.createSuggestion(descriptor);
        }

        public void removeDeclaringType(GroovySuggestionDeclaringType declaringType) {
            suggestions.remove(declaringType.getName());
        }

        /**
         * 
         * @return all the declaring types. Never null, but may be empty
         */
        public Collection<GroovySuggestionDeclaringType> getDeclaringTypes() {
            return suggestions.values();
        }

    }

}
