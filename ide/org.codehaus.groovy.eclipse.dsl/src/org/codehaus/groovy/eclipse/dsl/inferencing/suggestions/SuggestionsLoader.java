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

import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer.SuggestionsReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-09-07
 */
public class SuggestionsLoader {

    private IFile file;

    public SuggestionsLoader(IFile file) {
        this.file = file;
    }

    /**
     * Loads all suggestions from XML, clearing any existing in-memory
     * suggestions, and adds corresponding contribution groups and point cutsF
     */
    public boolean loadExistingSuggestions() {
        if (file != null && file.isAccessible()) {
            IProject project = file.getProject();
            // Make sure the file is in a Groovy project, and the project is
            // accessible
            if (InferencingSuggestionsManager.getInstance().isValidProject(project)) {
                IPath path = file.getLocation();
                String absoluteFileName = path != null ? path.toString() : null;
                SuggestionsReader reader = new SuggestionsReader(file.getProject(), absoluteFileName);
                reader.read();
                addSuggestionsContributionGroup();
                return true;
            }
        }
        return false;
    }

    /**
     * Add contribution groups from existing in-memory suggestions. Does not
     * load suggestions from XML.
     */
    public void addSuggestionsContributionGroup() {

        DSLDStore store = GroovyDSLCoreActivator.getDefault().getContextStoreManager().getDSLDStore(file.getProject());

        // Purge the existing contribution group and point cut for the file
        // first
        store.purgeIdentifier(file);

        store.addContributionGroup(new SuggestionsPointCut(file), new SuggestionsContributionGroup(file));
    }

}
