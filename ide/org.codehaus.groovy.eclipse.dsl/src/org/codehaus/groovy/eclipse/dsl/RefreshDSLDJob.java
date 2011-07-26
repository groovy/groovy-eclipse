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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.script.DSLDScriptExecutor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ExternalPackageFragmentRoot;
import org.eclipse.jface.preference.IPreferenceStore;

public class RefreshDSLDJob extends Job {
    
    public class DSLDResourceVisitor implements IResourceVisitor {
    
        private static final String PLUGIN_DSLD_SUPPORT = "plugin_dsld_support";
        private static final String GLOBAL_DSLD_SUPPORT = "global_dsld_support";
        private final IProject project;
        private final Set<IStorage> dsldFiles;
        private final Set<String> alreadyAdded; 
        public DSLDResourceVisitor(IProject project) {
            this.project = project;
            this.dsldFiles = new HashSet<IStorage>();
            alreadyAdded = new HashSet<String>();
        }
    
        public boolean visit(IResource resource) throws CoreException {
            // don't visit the output folders
            if (resource.isDerived()) {
                return false;
            }
            if (resource.getType() == IResource.FILE) {
                IFile file = (IFile) resource;
                if (!alreadyAdded.contains(file) && isDSLD(file)) {
                    alreadyAdded.add(file.getName());
                    dsldFiles.add(file);
                } else {
                    if (alreadyAdded.contains(file.getName())) {
                        GroovyDSLCoreActivator.logWarning("DSLD File " + file.getFullPath() + " already added, so skipping.");
                    }
                }
            }
            return true;
        }
    
        public Set<IStorage> findFiles(IProgressMonitor monitor) {
            try {
                // first look for files in the project
                project.accept(this);

                // now look for files in class folders of the project
                findDSLDsInLibraries(monitor);
            } catch (CoreException e) {
                GroovyDSLCoreActivator.logException(e);
            }
            
            return dsldFiles;
        }

        protected void findDSLDsInLibraries(IProgressMonitor monitor) throws JavaModelException {
            IJavaProject javaProject = JavaCore.create(project);
            for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
                if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
                    IPackageFragment frag = root.getPackageFragment("dsld");
                    if (frag.exists() || root.getElementName().equals(GLOBAL_DSLD_SUPPORT) || root.getElementName().equals(PLUGIN_DSLD_SUPPORT)) {
                        
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
                                rootResource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                                root.close();
                                root.open(monitor);
                                if (!root.exists() || !frag.exists()) {
                                    // must check a second time for existence because the close and re-opening of the root may 
                                    // have changed things
                                    continue;
                                }
                            } catch (CoreException e) {
                                GroovyDSLCoreActivator.logException(e);
                            }
                        }
                        // FIXADE end workaround
                        
                        if (rootResource instanceof IFolder && ((IFolder) rootResource).getFolder("dsld").exists()) {
                            IFolder dsldFolder = ((IFolder) rootResource).getFolder("dsld");
                            try {
                                for (IResource resource : dsldFolder.members()) {
                                    if (resource.getType() == IResource.FILE && !alreadyAdded.contains(resource.getName()) && isDSLD((IFile) resource)) {
                                        alreadyAdded.add(resource.getName());
                                        dsldFiles.add((IStorage) resource);
                                    } else {
                                        if (alreadyAdded.contains(resource.getName())) {
                                            GroovyLogManager.manager.log(TraceCategory.DSL, "DSLD File " + resource.getFullPath() + " already added, so skipping.");
                                        }
                                    }
                                }
                            } catch (CoreException e) {
                                GroovyDSLCoreActivator.logException(e);
                            }
                        } else {
                            
                            Object[] resources = frag.getNonJavaResources();
                            // make sure we don't add files with the same names.
                            // this ensures that a dsld file that is coming from 2 different places is 
                            // not added twice.
                            for (Object resource : resources) {
                                if (resource instanceof IStorage) {
                                    IStorage file = (IStorage) resource;
                                    if (!alreadyAdded.contains(file.getName()) && isDSLD(file)) {
                                        alreadyAdded.add(file.getName());
                                        dsldFiles.add(file);
                                    } else {
                                        if (alreadyAdded.contains(file.getName())) {
                                            GroovyLogManager.manager.log(TraceCategory.DSL, "DSLD File " + file.getFullPath() + " already added, so skipping.");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        protected boolean isDSLD(IStorage file) {
            return isFile(file, "dsld");
        }
        
        protected boolean isSuggestionFile(IStorage file) {
            return isFile(file, "sxml");
        }
        
        protected boolean isFile(IStorage file, String extension) {
            if (file instanceof IFile) {
                IFile iFile = (IFile) file;
                return !iFile.isDerived() && extension.equals(iFile.getFileExtension());
            } else {
                String name = file.getName();
                return name != null && name.endsWith(extension);
            }
        }
    
    }

    private final List<IProject> projects;

    public RefreshDSLDJob(IProject project) {
        this(Collections.singletonList(project));
    }
    public RefreshDSLDJob(List<IProject> projects) {
        super("Refresh DSLD scripts");
        this.projects = projects;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        IPreferenceStore prefStore = GroovyDSLCoreActivator.getDefault().getPreferenceStore();
        if (prefStore.getBoolean(DSLPreferencesInitializer.DSLD_DISABLED)) {
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "DSLD support is currently disabled, so not refreshing DSLDs.");
            }
            return Status.OK_STATUS;
        }

        // actually, don't cancel since refresh jobs for other
        // projects may be running
//        // cancel all existing jobs
//        Job[] jobs = getJobManager().find(RefreshDSLDJob.class);
//        if (jobs != null) {
//            for (Job job : jobs) {
//                if (job != this) {
//                    job.cancel();
//                }
//            }
//        }


        List<IStatus> errorStatuses = new ArrayList<IStatus>();
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask("Refresh DSLD scripts", projects.size() * 9);
        for (IProject project : projects) {
            IStatus res = refreshProject(project, new SubProgressMonitor(monitor, 9));
            if (!res.isOK()) {
                errorStatuses.add(res);
            } else if (res == Status.CANCEL_STATUS) {
                return res;
            }
        }
        monitor.done();
        
        if (errorStatuses.isEmpty()) {
            return Status.OK_STATUS;
        } else {
            MultiStatus multi = new MultiStatus(GroovyDSLCoreActivator.PLUGIN_ID, 0, "Error refreshing DSLDs.", null);
            for (IStatus error : errorStatuses) {
                multi.add(error);
            }
            return multi;
        }
    }
    
    private IStatus refreshProject(IProject project, IProgressMonitor monitor) {
        String event = null;
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Refreshing inferencing scripts for " + project.getName());
            event = "Refreshing inferencing scripts: " + project.getName();
            GroovyLogManager.manager.logStart(event);
        }
        
        monitor.beginTask("Refreshing DSLD files for project " + project.getName(), 9);
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);

        
        // purge existing
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Purging old state");
        }
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
        Set<IStorage> findDSLDFiles = new DSLDResourceVisitor(project).findFiles(monitor);
        
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);
        
        // now add the rest
        DSLDScriptExecutor executor = new DSLDScriptExecutor(JavaCore.create(project));
        for (IStorage file : findDSLDFiles) {
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "Processing " + file.getName() + " in project " + project.getName());
            }
            monitor.subTask("Processing " + file.getName() + " in project " + project.getName());
            executor.executeScript(file);
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
        }
        monitor.worked(6);
        
        monitor.done();
        if (event != null) {
            GroovyLogManager.manager.logEnd(event, TraceCategory.DSL);
        }
        return Status.OK_STATUS;
    }
    
    @Override
    public boolean belongsTo(Object family) {
        return family == RefreshDSLDJob.class;
    }
}