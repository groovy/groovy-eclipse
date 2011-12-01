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

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer.SuggestionsFile;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer.SuggestionsTransform;
import org.codehaus.jdt.groovy.model.GroovyNature;
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
     * 
     * @return true if changes for given project are successfully committed.
     *         False otherwise
     */
    public boolean commitChanges(IProject project) {

        // don't commit if the project is not accessible
        if (!isValidProject(project)) {
            return false;
        }
        // Keep track of the last project that was modified
        lastModifiedProject = project;

        ProjectSuggestions suggestions = getSuggestions(project);
        SuggestionsTransform transform = new SuggestionsTransform(suggestions);
        String result = transform.transform();

        if (result != null) {
            SuggestionsFile suggestionsFile = new SuggestionsFile(project);

            IFile file = suggestionsFile.createFile();
            writeToFile(file, result);
            return true;
        }
        return false;
    }

    /**
     * Restores the suggestions for an accessible project back in the
     * suggestions model.
     * If the project is not accessible, it removes all the suggestions from the
     * model, as the model
     * should reflect whether the suggestions in the file can be read or not.
     * 
     * @param project
     * @return true if suggestions are restored or if project is not accessible,
     *         suggestions in memory are cleared. False if no restoration took
     *         place
     */
    public boolean restoreSuggestions(IProject project) {
        // For now restore from XML
        if (isValidProject(project)) {
            SuggestionsFile suggestionFile = new SuggestionsFile(project);
            IFile file = suggestionFile.getFile();
            if (file != null && file.exists()) {
                return new SuggestionsLoader(file).loadExistingSuggestions();
            }
        }
        // If restoring was not possible, remove suggestions from model
        ProjectSuggestions suggestions = getSuggestions(project);
        if (suggestions != null) {
            suggestions.removeAll();
            return true;
        }
        return false;
    }

    public boolean isValidProject(IProject project) {
        return project != null && project.isAccessible() && GroovyNature.hasGroovyNature(project);
    }

    /**
     * 
     * FIXNS: Hook to a resource change listener in the future.
     * 
     * @return gets the last project that had suggestion commits. It may be null
     *         including if the last modified project is no longer accessible.
     */
    public IProject getlastModifiedProject() {
        if (isValidProject(lastModifiedProject)) {
            return lastModifiedProject;
        }
        // Otherwise, clear the last modified project
        return lastModifiedProject = null;
    }

    protected void writeToFile(IFile file, String value) {
        if (file != null) {
            try {
                // For now replace all the contents
                file.setContents(new ByteArrayInputStream(value.getBytes()), true, true, new NullProgressMonitor());
            } catch (CoreException e) {
                GroovyDSLCoreActivator.logException(e);
            }
        }
    }

    /**
     * Not null, unless it is not an accessible Groovy project. may be empty.
     * Original copy.
     * 
     * @param project
     * @return Non-null suggestions for any accessible Groovy project. Null
     *         otherwise
     */

    public ProjectSuggestions getSuggestions(IProject project) {
        // Only Groovy Projects have suggestions
        if (!isValidProject(project)) {
            return null;
        }
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

            }

            IGroovySuggestion createdSuggestion = declaringType.createSuggestion(descriptor);
            // Don't add a new declaring type unless a suggestion was
            // successfully created
            if (createdSuggestion != null && !suggestions.containsKey(declaringType.getName())) {
                suggestions.put(declaringTypeName, declaringType);
            }
            return createdSuggestion;
        }

        public void removeDeclaringType(GroovySuggestionDeclaringType declaringType) {
            suggestions.remove(declaringType.getName());
        }

        /**
         * Removes all declaring types from the suggestions model for this
         * project
         */
        public void removeAll() {
            suggestions.clear();
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
