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
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionsLoader;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer.SuggestionsFileProperties;
import org.codehaus.groovy.eclipse.dsl.script.DSLDScriptExecutor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ExternalPackageFragmentRoot;

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
                if (!alreadyAdded.contains(file) && (isDSLD(file) || isSuggestionFile(file))) {
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
                if (e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
                    // ignore.  Resource was deleted
                } else {
                    GroovyDSLCoreActivator.logException(e);
                }
            }
            
            return dsldFiles;
        }

        protected void findDSLDsInLibraries(IProgressMonitor monitor) throws JavaModelException {
            IJavaProject javaProject = JavaCore.create(project);
            IPackageFragmentRoot[] roots = getFragmentRoots(javaProject, monitor);
            for (IPackageFragmentRoot root : roots) {
                
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                
                if (root.getKind() == IPackageFragmentRoot.K_BINARY ||
                        // GRECLIPSE-1458 must check source folders, but avoid source folders from same project
                        isSourceFolderFromOtherProject(root)) {
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
                                if (monitor.isCanceled()) {
                                    throw new OperationCanceledException();
                                }
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
                            
                            try {
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
                            } catch (JavaModelException e) {
                                if (e.getStatus().getCode() == IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
                                    // can ignore, most likely classpath change during refresh operation
                                } else {
                                    throw e;
                                }
                            }
                        }
                    }
                }
            }
        }

        private boolean isSourceFolderFromOtherProject(IPackageFragmentRoot root) {
            if (root.isReadOnly()) {
                // not source folder
                return false;
            }
            IResource resource = root.getResource();
            if (resource == null) {
                return false;
            }
            
            if (resource.getProject().equals(project)) {
                return false;
            }
            return true;
        }

        /**
         * Get all package fragment roots in a safe way so that concurrent modifications aren't thrown
         * See http://jira.codehaus.org/browse/GRECLIPSE-1284
         * @param javaProject
         * @param monitor
         * @return
         * @throws JavaModelException
         */
        private IPackageFragmentRoot[] getFragmentRoots(final IJavaProject javaProject, IProgressMonitor monitor) throws JavaModelException {
            final IPackageFragmentRoot[][] roots = new IPackageFragmentRoot[1][];
            try {
                ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                    public void run(IProgressMonitor monitor) throws CoreException {
                        roots[0] = javaProject.getAllPackageFragmentRoots();
                    }
                }, getSchedulingRule(), IWorkspace.AVOID_UPDATE, monitor);
            } catch (CoreException e) {
                if (e.getStatus().getCode()  == IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
                    // ignore...project was deleted
                } else {
                    GroovyDSLCoreActivator.logException(e);
                }
            }
            return roots[0] != null ? roots[0] : new IPackageFragmentRoot[0];
        }  
        
        private ISchedulingRule getSchedulingRule() {
            IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
            
            // FIXADE Arrrrgh...we need to grab a build rule here.  Looks like a classpath container refresh
            // will grab the rule of projects that are contained in the container.
//            return new MultiRule(new ISchedulingRule[] {
//                // use project modification rule as this is needed to create the .classpath file if it doesn't exist yet, or to update project references
//                ruleFactory.modifyRule(this.project.getProject()),
//                
//                // and external project modification rule in case the external folders are modified
//                ruleFactory.modifyRule(JavaModelManager.getExternalManager().getExternalFoldersProject())
//            });
            return ruleFactory.buildRule();
        }
    }

    private final List<IProject> projects;
    private DSLDStoreManager contextStoreManager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();

    /**
     * Deprecated.  Use {@link DSLDStoreManager#initialize(IProject, boolean)}
     * instead.  This new method allows for the initialization of a store synchronously
     * or asynchronously.
     */
    @Deprecated
    public RefreshDSLDJob(IProject project) {
        this(Collections.singletonList(project));
    }

    /**
     * Deprecated.  Use {@link DSLDStoreManager#initialize(List, boolean)}
     * instead.  This new method allows for the initialization of a store synchronously
     * or asynchronously.
     */
    @Deprecated
    public RefreshDSLDJob(List<IProject> projects) {
        super("Refresh DSLD scripts");
        this.projects = contextStoreManager.addInProgress(projects);
    }
    
    protected boolean isDSLD(IStorage file) {
        return isFile(file, "dsld");
    }
    
    protected boolean isSuggestionFile(IStorage file) {
        return isFile(file, SuggestionsFileProperties.FILE_TYPE);
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

    @Override
    public IStatus run(IProgressMonitor monitor) {
        try {
            if (GroovyDSLCoreActivator.getDefault().isDSLDDisabled()) {
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.DSL, "DSLD support is currently disabled, so not refreshing DSLDs.");
                }
                return Status.OK_STATUS;
            }
    
            List<IStatus> errorStatuses = new ArrayList<IStatus>();
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
            monitor.beginTask("Refresh DSLD scripts", projects.size() * 9);
            for (IProject project : projects) {
                IStatus res = Status.OK_STATUS;
                try {
                    res = refreshProject(project, new SubProgressMonitor(monitor, 9));
                } finally {
                    contextStoreManager.removeInProgress(project);
                }
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
        } finally {
            // in case the job was exited early, ensure all projects 
            // have their initialization stage removed
            for (IProject project : projects) {
                contextStoreManager.removeInProgress(project);
            }
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
        for (IStorage file : findDSLDFiles) {
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "Processing " + file.getName() + " in project " + project.getName());
            }
            monitor.subTask("Processing " + file.getName() + " in project " + project.getName());
            
            if (isDSLD(file)) {
                DSLDScriptExecutor executor = new DSLDScriptExecutor(JavaCore.create(project));
                executor.executeScript(file);
            } else if (isSuggestionFile(file)) {
                new SuggestionsLoader((IFile)file).loadExistingSuggestions();
            }
             
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