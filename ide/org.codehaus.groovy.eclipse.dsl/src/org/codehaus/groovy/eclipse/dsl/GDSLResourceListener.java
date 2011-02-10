/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextStore;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextStoreManager;
import org.codehaus.groovy.eclipse.dsl.script.GDSLScriptExecutor;
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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;

/**
 * Handles updates and changes of GDSL files
 * Things that need to get handled:
 * 
 * Project deletion or close: flush the context
 * Project creation, or groovy nature added: refresh GDSLs for project
 * GDSL script added: update that script from context
 * GDSL script deleted: remove that script from context
 * GDSL script changed: first remove and then re-add to context
 * 
 * @author andrew
 * @created Nov 25, 2010
 */
public class GDSLResourceListener implements IResourceChangeListener {

    private class GDSLChangeResourceDeltaVisitor implements IResourceDeltaVisitor {
        private final int eventType;
        
        private GDSLChangeResourceDeltaVisitor(int eventType) {
            this.eventType = eventType;
        }

        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource deltaResource = delta.getResource();
            
            if (!(deltaResource.isAccessible() || deltaResource.getType() == IResource.PROJECT) || deltaResource.isDerived()) {
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
                    GroovyLogManager.manager.log(TraceCategory.DSL, 
                            "Deleting DSL context for: " + project.getName());
                    contextStoreManager.clearContextStore(project);
                    return false;
                } else if (! contextStoreManager.hasContextStoreFor(project) &&
                        GroovyNature.hasGroovyNature(project)) {
                    // could be that this project has just been opened
                    Job refreshJob = new RefreshGDSLJob(project);
                    refreshJob.setPriority(Job.LONG);
                    refreshJob.schedule();
                    return false;
                }

                // not opening or closing the project,  just check to see if we still care about it 
                if (!GroovyNature.hasGroovyNature(project)) {
                    return false;
                }
            } else if (deltaResource.getType() == IResource.FILE) {
                // at this point, we know that we are in a groovy project
                
                IFile file = (IFile) deltaResource;
                if (isGDSLFile(file)) {
                    IProject project = file.getProject();
                    ContextStore store = contextStoreManager.getContextStore(project);
                    Assert.isNotNull(store, "Context store should not be null");
                    
                    GroovyLogManager.manager.log(TraceCategory.DSL, "Processing " + file.getName());
                    // this file has been changed or deleted.  Either way, must start by purging
                    // if this file diden't exist in the past, then this is a no-op
                    store.purgeFileFromStore(file);
                    
                    if (eventType == IResourceChangeEvent.POST_CHANGE) {
                        // also refresh the file
                        GDSLScriptExecutor executor = new GDSLScriptExecutor(JavaCore.create(project));
                        executor.executeScript(file);
                    }
                }
            }
            
            // we know that we are in a groovy project
            // so keep on trudging through the delta
            return true;
        }
    }
    
    private static final ContextStoreManager contextStoreManager = GroovyDSLActivator.getDefault().getContextStoreManager();
    
    public void resourceChanged(IResourceChangeEvent event) {
        switch (event.getType()) {
            case IResourceChangeEvent.PRE_DELETE:
            case IResourceChangeEvent.PRE_CLOSE:
            case IResourceChangeEvent.POST_CHANGE:
                try {
                    if (event.getDelta() != null) {
                        event.getDelta().accept(new GDSLChangeResourceDeltaVisitor(event.getType()));
                    }
                } catch (CoreException e) {
                    GroovyDSLActivator.logException(e);
                }
        }
    }

    /**
     * @param file
     * @return
     */
    public boolean isGDSLFile(IFile file) {
        String fileExtension = file.getFileExtension();
        return fileExtension != null && fileExtension.equals("gdsl");
    }
}
