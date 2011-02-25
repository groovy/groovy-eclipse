/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.script.DSLDScriptExecutor;
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

public class RefreshDSLDJob extends Job {
    
    public class DSLDResourceVisitor implements IResourceVisitor {
    
        private final IProject project;
        private final List<IFile> dsldFiles;
        public DSLDResourceVisitor(IProject project) {
            this.project = project;
            this.dsldFiles = new ArrayList<IFile>();
        }
    
        public boolean visit(IResource resource) throws CoreException {
            // don't visit the output folders
            if (resource.isDerived()) {
                return false;
            }
            if (resource.getType() == IResource.FILE) {
                IFile file = (IFile) resource;
                String extension = file.getFileExtension();
                if (extension != null && extension.equals("dsld")) {
                    dsldFiles.add(file);
                }
            }
            return true;
        }
    
        public List<IFile> findFiles() {
            try {
                project.accept(this);
            } catch (CoreException e) {
                GroovyDSLCoreActivator.logException(e);
            }
            return dsldFiles;
        }
    
    }

    private final IProject project;

    public RefreshDSLDJob(IProject project) {
        super("Refresh GDSL scripts for " + project.getName());
        this.project = project;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        String event = null;
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Refreshing inferencing scripts for " + project.getName());
            event = "Refreshing inferencing scripts: " + project.getName();
            GroovyLogManager.manager.logStart(event);
        }
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        
        monitor.beginTask("Refreshing GDSL files", 9);
        
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Cancelling previous refresh jobs");
        }
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
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Purging old state");
        }
        monitor.subTask("Purging old state");
        DSLDStore store = GroovyDSLCoreActivator.getDefault().getContextStoreManager().getDSLDStore(project);
        store.purgeAll();
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);

        // find dslds
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Finding inferencing DSL scripts");
        }
        monitor.subTask("Finding inferencing DSL scripts");
        List<IFile> findGDSLFiles = new DSLDResourceVisitor(project).findFiles();
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);
        
        // now add the rest
        DSLDScriptExecutor executor = new DSLDScriptExecutor(JavaCore.create(project));
        for (IFile file : findGDSLFiles) {
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "Processing " + file.getName());
            }
            monitor.subTask("Processing " + file.getName());
            executor.executeScript(file);
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            monitor.worked(1);
        }
        
        monitor.done();
        if (event != null) {
            GroovyLogManager.manager.logEnd(event, TraceCategory.DSL);
        }
        return Status.OK_STATUS;
    }
    
    @Override
    public boolean belongsTo(Object family) {
        return project.equals(family);
    }

}