/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.dsl;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionsLoader;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer.SuggestionsFileProperties;
import org.codehaus.groovy.eclipse.dsl.script.DSLDScriptExecutor;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

/**
 * Handles updates and changes of DSLD files
 * Things that need to get handled:
 *
 * Project deletion or close: flush the context
 * Project creation, or groovy nature added: refresh DSLDs for project
 * DSLD script added: update that script from context
 * DSLD script deleted: remove that script from context
 * DSLD script changed: first remove and then re-add to context
 */
public class DSLDResourceListener implements IResourceChangeListener {

    private static final DSLDStoreManager contextStoreManager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        switch (event.getType()) {
        case IResourceChangeEvent.PRE_DELETE:
        case IResourceChangeEvent.PRE_CLOSE:
        case IResourceChangeEvent.POST_CHANGE:
            try {
                if (event.getDelta() != null) {
                    event.getDelta().accept(new DSLDChangeResourceDeltaVisitor(event.getType()));
                }
            } catch (CoreException e) {
                GroovyDSLCoreActivator.logException(e);
            }
        }
    }

    public boolean isDSLDFile(IFile file) {
        return "dsld".equals(file.getFileExtension());
    }

    public boolean isXDSL(IFile file) {
        String fileExtension = file.getFileExtension();
        return fileExtension != null && fileExtension.equals(SuggestionsFileProperties.FILE_TYPE);
    }

    private class DSLDChangeResourceDeltaVisitor implements IResourceDeltaVisitor {
        private final int eventType;

        private DSLDChangeResourceDeltaVisitor(int eventType) {
            this.eventType = eventType;
        }

        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource deltaResource = delta.getResource();

            if (deltaResource.isDerived()) {
                // fail fast
                return false;
            }

            if (deltaResource.getType() == IResource.PROJECT) {
                IProject project = (IProject) deltaResource;

                // two possibilities handled here:
                // project being closed/deleted/groovy nature removed
                // project being opened.
                if ((eventType == IResourceChangeEvent.PRE_DELETE && delta.getKind() == IResourceDelta.REMOVED) ||
                        eventType == IResourceChangeEvent.PRE_CLOSE ||
                        // just in case nature has been removed for this project
                        !GroovyNature.hasGroovyNature(project)) {
                    // no longer managing state for this project
                    if (GroovyLogManager.manager.hasLoggers()) {
                        GroovyLogManager.manager.log(TraceCategory.DSL, "Deleting DSL context for: " + project.getName());
                    }
                    contextStoreManager.removeDSLDStore(project);
                    return false;
                } else if (GroovyNature.hasGroovyNature(project) && !contextStoreManager.hasDSLDStoreFor(project)) {
                    // could be that this project has just been opened
                    GroovyDSLCoreActivator.getDefault().getContextStoreManager().initialize(project, false);
                    return false;
                }

                // not opening or closing the project, just check to see if we
                // still care about it
                if (!GroovyNature.hasGroovyNature(project)) {
                    return false;
                }
            } else if (deltaResource.getType() == IResource.FILE) {
                // at this point, we know that we are in a groovy project

                IFile file = (IFile) deltaResource;
                if (isDSLDFile(file) || isXDSL(file)) {
                    IProject project = file.getProject();
                    DSLDStore store = contextStoreManager.getDSLDStore(project);
                    Assert.isNotNull(store, "Context store should not be null");

                    if (GroovyLogManager.manager.hasLoggers()) {
                        GroovyLogManager.manager.log(TraceCategory.DSL, "Processing " + file.getName());
                    }
                    // this file has been changed or deleted. Either way, must
                    // start by purging
                    // if this file diden't exist in the past, then this is a
                    // no-op
                    store.purgeIdentifier(file);

                    if (file.isAccessible() && eventType == IResourceChangeEvent.POST_CHANGE) {
                        // also refresh the file
                        if (isDSLDFile(file)) {
                            DSLDScriptExecutor executor = new DSLDScriptExecutor(JavaCore.create(project));
                            executor.executeScript(file);
                        } else if (isXDSL(file)) {
                            // At this point the suggestions should already be in the manager. only contribution groups
                            // and point cuts need to be created
                            new SuggestionsLoader(file).addSuggestionsContributionGroup();
                        }
                    }
                }
            }

            // we know that we are in a groovy project
            // so keep on trudging through the delta
            return true;
        }
    }
}
