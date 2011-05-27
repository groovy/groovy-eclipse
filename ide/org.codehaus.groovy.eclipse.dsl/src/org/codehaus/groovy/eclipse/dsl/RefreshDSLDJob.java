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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.script.DSLDScriptExecutor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ExternalPackageFragmentRoot;

public class RefreshDSLDJob extends Job {
    
    public class DSLDResourceVisitor implements IResourceVisitor {
    
        private final IProject project;
        private final Set<IStorage> dsldFiles;
        public DSLDResourceVisitor(IProject project) {
            this.project = project;
            this.dsldFiles = new HashSet<IStorage>();
        }
    
        public boolean visit(IResource resource) throws CoreException {
            // don't visit the output folders
            if (resource.isDerived()) {
                return false;
            }
            if (resource.getType() == IResource.FILE) {
                IFile file = (IFile) resource;
                if (isDSLD(file)) {
                    dsldFiles.add(file);
                }
            }
            return true;
        }
    
        public Set<IStorage> findFiles() {
            try {
                // first look for files in the project
                project.accept(this);

                // now look for files in class folders of the project
                findDSLDsInLibraries();
            } catch (CoreException e) {
                GroovyDSLCoreActivator.logException(e);
            }
            
            return dsldFiles;
        }

        /**
         * @throws JavaModelException
         */
        protected void findDSLDsInLibraries() throws JavaModelException {
            IJavaProject javaProject = JavaCore.create(project);
            for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
                if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
                    IPackageFragment frag = root.getPackageFragment("dsld");
                    if (frag.exists()) {
                        
                        // FIXADE start workaround for Bug 346928
                        // in 3.6 and earlier, it was not possible to refresh scripts in external folders 
                        // fixed in 3.7, consider removing when 3.6 is no longer supported.
                        IResource rootResource = root.getResource();
                        if (rootResource == null && root instanceof ExternalPackageFragmentRoot) {
                            // external source roots return null for getResource, but do have a resource 
                            rootResource = ((ExternalPackageFragmentRoot) root).resource();
                        }
                        if (rootResource != null) {
                            try {
                                // FIXADE pass the progress monitor in here
                                rootResource.refreshLocal(IResource.DEPTH_INFINITE, null);
                            } catch (CoreException e) {
                                GroovyDSLCoreActivator.logException(e);
                            }
                        }
                        // FIXADE end workaround
                        
                        Object[] resources = frag.getNonJavaResources();
                        for (Object resource : resources) {
                            if (resource instanceof IStorage) {
                                IStorage file = (IStorage) resource;
                                if (isDSLD(file)) {
                                    dsldFiles.add(file);
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * @param file
         */
        protected boolean isDSLD(IStorage file) {
            if (file instanceof IFile) {
                IFile iFile = (IFile) file;
                return !iFile.isDerived() && "dsld".equals(iFile.getFileExtension());
            } else {
                String name = file.getName();
                return name != null && name.endsWith(".dsld");
            }
        }
    
    }

    private final IProject project;

    public RefreshDSLDJob(IProject project) {
        super("Refresh DSLD scripts for " + project.getName());
        this.project = project;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
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
        
        monitor.beginTask("Refreshing DSLD files", 9);
        
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Cancelling previous refresh jobs");
        }
        monitor.subTask("Cancelling previous refresh jobs");
        // cancel all existing jobs
        Job[] jobs = getJobManager().find(getFamily(project));
        if (jobs != null) {
            for (Job job : jobs) {
                if (job != this) {
                    job.cancel();
                }
            }
//            FIXADE DANGER! DANGER! I think uncommenting this is causing the job to never end, but why???  Commenting out for now to see if this lets the tests pass and doesn't leave jobs running after shutting down.
//            // now wait for them to be finished
//            for (Job job : jobs) {
//                if (job != this) {
//                    try {
//                        job.join();
//                    } catch (InterruptedException e) {
//                    }
//                }
//            }
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
        Set<IStorage> findDSLDFiles = new DSLDResourceVisitor(project).findFiles();
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);
        
        // now add the rest
        DSLDScriptExecutor executor = new DSLDScriptExecutor(JavaCore.create(project));
        for (IStorage file : findDSLDFiles) {
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
        return getFamily(project).equals(family);
    }

    
    public static Object getFamily(IProject project) {
        Assert.isNotNull(project, "Null project passed to 'getFamily()'");
        return "DSLD: " + project.getName();
    }
}