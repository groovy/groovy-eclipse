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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextStore;
import org.codehaus.groovy.eclipse.dsl.script.GDSLScriptExecutor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;

public class RefreshGDSLJob extends Job {
    
    public class GDSLResourceVisitor implements IResourceVisitor {
    
        private final IProject project;
        private final List<IFile> gdslFiles;
        public GDSLResourceVisitor(IProject project) {
            this.project = project;
            this.gdslFiles = new ArrayList<IFile>();
        }
    
        public boolean visit(IResource resource) throws CoreException {
            // don't visit the output folders
            if (resource.isDerived()) {
                return false;
            }
            if (resource.getType() == IResource.FILE) {
                IFile file = (IFile) resource;
                String extension = file.getFileExtension();
                if (extension != null && extension.equals("gdsl")) {
                    gdslFiles.add(file);
                }
            }
            return true;
        }
    
        public List<IFile> findFiles() {
            try {
                project.accept(this);
            } catch (CoreException e) {
                GroovyDSLActivator.logException(e);
            }
            return gdslFiles;
        }
    
    }

    private final IProject project;

    public RefreshGDSLJob(IProject project) {
        super("Refresh GDSL scripts for " + project.getName());
        this.project = project;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        GroovyLogManager.manager.log(TraceCategory.DSL, "Refreshing inferencing scripts for " + project.getName());
        String eventName = "Refreshing inferencing scripts: " + project.getName();
        GroovyLogManager.manager.logStart(eventName);
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        
        monitor.beginTask("Refreshing GDSL files", 9);
        
        GroovyLogManager.manager.log(TraceCategory.DSL, "Cancelling previous refresh jobs");
        monitor.subTask("Cancelling previous refresh jobs");
        // cancel all existing jobs
        Job[] jobs = getJobManager().find(project);
        if (jobs != null) {
            for (Job job : jobs) {
                if (job != this) {
                    job.cancel();
                }
            }
            // now wait for them to be finished
            for (Job job : jobs) {
                if (job != this) {
                    try {
                        job.join();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);

        
        // purge existing
        GroovyLogManager.manager.log(TraceCategory.DSL, "Purging old state");
        monitor.subTask("Purging old state");
        ContextStore store = GroovyDSLActivator.getDefault().getContextStoreManager().getContextStore(project);
        store.purgeAll();
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);

        // find gdsls
        GroovyLogManager.manager.log(TraceCategory.DSL, "Finding inferencing DSL scripts");
        monitor.subTask("Finding inferencing DSL scripts");
        List<IFile> findGDSLFiles = new GDSLResourceVisitor(project).findFiles();
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);
        
        // now add the rest
        GDSLScriptExecutor executor = new GDSLScriptExecutor(JavaCore.create(project));
        for (IFile file : findGDSLFiles) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Processing " + file.getName());
            monitor.subTask("Processing " + file.getName());
            executor.executeScript(file);
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            monitor.worked(1);
        }
        
        monitor.done();
        GroovyLogManager.manager.logEnd(eventName, TraceCategory.DSL);
        return Status.OK_STATUS;
    }
    
    @Override
    public boolean belongsTo(Object family) {
        return project.equals(family);
    }

}