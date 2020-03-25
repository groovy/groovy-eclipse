/*
 * Copyright 2009-2020 the original author or authors.
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

import static org.codehaus.groovy.eclipse.dsl.classpath.DSLDContainerInitializer.GLOBAL_DSLD_SUPPORT;
import static org.codehaus.groovy.eclipse.dsl.classpath.DSLDContainerInitializer.PLUGIN_DSLD_SUPPORT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionsLoader;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer.SuggestionsFileProperties;
import org.codehaus.groovy.eclipse.dsl.script.DSLDScriptExecutor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class RefreshDSLDJob extends Job {

    private final List<IProject> projects;

    /**
     * @deprecated Use {@link DSLDStoreManager#initialize(IProject, boolean)} instead.
     */
    @Deprecated
    public RefreshDSLDJob(final IProject project) {
        this(Collections.singletonList(project));
    }

    /**
     * @deprecated Use {@link DSLDStoreManager#initialize(List, boolean)} instead.
     */
    @Deprecated
    public RefreshDSLDJob(final List<IProject> projects) {
        super("Refresh DSLD scripts");
        this.projects = GroovyDSLCoreActivator.getDefault().getContextStoreManager().addInProgress(projects);
    }

    @Override
    public boolean belongsTo(final Object family) {
        return (family == RefreshDSLDJob.class);
    }

    private static boolean isDSLD(final IStorage file) {
        return isFile(file, "dsld");
    }

    private static boolean isSuggestionFile(final IStorage file) {
        return isFile(file, SuggestionsFileProperties.FILE_TYPE);
    }

    private static boolean isFile(final IStorage file, final String extension) {
        if (file instanceof IFile) {
            IFile iFile = (IFile) file;
            return !iFile.isDerived() && extension.equals(iFile.getFileExtension());
        } else {
            String name = file.getName();
            return name != null && name.endsWith(extension);
        }
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
        try {
            if (GroovyDSLCoreActivator.getDefault().isDSLDDisabled()) {
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.DSL, "DSLD support is currently disabled, so not refreshing DSLDs.");
                }
                return Status.OK_STATUS;
            }

            SubMonitor submon = SubMonitor.convert(monitor);
            submon.beginTask("Refresh DSLD scripts", projects.size() * 9);

            List<IStatus> errorStatuses = new ArrayList<>();
            for (IProject project : projects) {
                IStatus res = Status.OK_STATUS;
                try {
                    res = refreshProject(project, submon.split(9));
                } finally {
                    GroovyDSLCoreActivator.getDefault().getContextStoreManager().removeInProgress(project);
                }
                if (!res.isOK()) {
                    errorStatuses.add(res);
                } else if (res == Status.CANCEL_STATUS) {
                    return res;
                }
            }

            if (errorStatuses.isEmpty()) {
                return Status.OK_STATUS;
            } else {
                MultiStatus multi = new MultiStatus(GroovyDSLCoreActivator.PLUGIN_ID, 0, "Error refreshing DSLDs.", null);
                errorStatuses.forEach(multi::add);
                return multi;
            }
        } finally {
            // in case the job was exited early, ensure all projects have their initialization stage removed
            projects.forEach(GroovyDSLCoreActivator.getDefault().getContextStoreManager()::removeInProgress);
        }
    }

    private IStatus refreshProject(final IProject project, final IProgressMonitor monitor) {
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
        Collection<IStorage> findDSLDFiles = new DSLDResourceVisitor(project).findFiles(monitor);

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
                new SuggestionsLoader((IFile) file).loadExistingSuggestions();
            }

            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
        }
        monitor.worked(6);

        if (event != null) {
            GroovyLogManager.manager.logEnd(event, TraceCategory.DSL);
        }
        return Status.OK_STATUS;
    }

    //--------------------------------------------------------------------------

    private class DSLDResourceVisitor implements IResourceVisitor {

        private final Map<String, IStorage> dsldFiles = new HashMap<>();
        private final IProject project;

        DSLDResourceVisitor(final IProject project) {
            this.project = project;
        }

        @Override
        public boolean visit(final IResource resource) throws CoreException {
            // don't visit the output folders
            if (resource.isDerived()) {
                return false;
            }
            if (resource.getType() == IResource.FILE) {
                IFile file = (IFile) resource;
                if (isDSLD(file) || isSuggestionFile(file)) {
                    if (dsldFiles.putIfAbsent(file.getName(), file) != null) {
                        GroovyDSLCoreActivator.logWarning("DSLD File " + file.getFullPath() + " already added, so skipping.");
                    }
                }
            }
            return true;
        }

        public Collection<IStorage> findFiles(final IProgressMonitor monitor) {
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

            return dsldFiles.values();
        }

        protected void findDSLDsInLibraries(final IProgressMonitor monitor) throws JavaModelException {
            for (IPackageFragmentRoot root : getPackageFragmentRoots(monitor)) {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                try {
                    // GRECLIPSE-1458: must check source folders, but avoid source folders from same project
                    if ((root.getKind() == IPackageFragmentRoot.K_BINARY || isSourceFolderFromOtherProject(root))) {
                        if (root.getElementName().equals(GLOBAL_DSLD_SUPPORT) || root.getElementName().equals(PLUGIN_DSLD_SUPPORT)) {
                            List<IParent> containers = new LinkedList<>();
                            containers.add(root);
                            do {
                                IParent container = containers.remove(0);

                                for (IJavaElement child : container.getChildren()) {
                                    if (child instanceof IPackageFragment) {
                                        containers.add((IPackageFragment) child);
                                    }
                                }

                                Object[] resources;
                                if (container instanceof IPackageFragment) {
                                    resources = ((IPackageFragment) container).getNonJavaResources();
                                } else {
                                    resources = ((IPackageFragmentRoot) container).getNonJavaResources();
                                }

                                for (Object resource : resources) {
                                    if (resource instanceof IStorage && isDSLD((IStorage) resource)) {
                                        if (dsldFiles.putIfAbsent(((IStorage) resource).getName(), (IStorage) resource) != null) {
                                            GroovyLogManager.manager.log(TraceCategory.DSL,
                                                "DSLD file " + ((IStorage) resource).getFullPath() + " already added, so skipping.");
                                        }
                                    }
                                }
                            } while (!containers.isEmpty());
                            //
                        } else if (root.getPackageFragment("dsld").exists()) {
                            IFolder dsldFolder;
                            if (root.getResource() instanceof IFolder && (dsldFolder = ((IFolder) root.getResource()).getFolder("dsld")).exists()) {
                                for (IResource resource : dsldFolder.members()) {
                                    if (resource.getType() == IResource.FILE && isDSLD((IStorage) resource)) {
                                        if (dsldFiles.putIfAbsent(resource.getName(), (IStorage) resource) != null) {
                                            GroovyLogManager.manager.log(TraceCategory.DSL,
                                                "DSLD file " + resource.getFullPath() + " already added, so skipping.");
                                        }
                                    }
                                }
                            } else {
                                Object[] resources = root.getPackageFragment("dsld").getNonJavaResources();
                                for (Object resource : resources) {
                                    if (resource instanceof IStorage) {
                                        IStorage file = (IStorage) resource;
                                        if (isDSLD(file)) {
                                            if (dsldFiles.putIfAbsent(file.getName(), file) != null) {
                                                GroovyLogManager.manager.log(TraceCategory.DSL,
                                                    "DSLD file " + file.getFullPath() + " already added, so skipping.");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (CoreException e) {
                    switch (e.getStatus().getCode()) {
                    case IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST:
                    case IJavaModelStatusConstants.ELEMENT_NOT_ON_CLASSPATH:
                        break;
                    default:
                        GroovyDSLCoreActivator.logException(e);
                    }
                }
            }
        }

        private boolean isSourceFolderFromOtherProject(final IPackageFragmentRoot root) {
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
         * Get all package fragment roots in a safe way so that concurrent modifications aren't thrown.
         * <p>
         * GRECLIPSE-1284: When IDE runs its initialization jobs, I often get the following exception:
         * <pre>
         * java.util.ConcurrentModificationException
         *     at java.util.HashMap$HashIterator.nextEntry(Unknown Source)
         *     at java.util.HashMap$EntryIterator.next(Unknown Source)
         *     at java.util.HashMap$EntryIterator.next(Unknown Source)
         *     at org.eclipse.jdt.internal.core.JavaModelManager$9.run(JavaModelManager.java:2796)
         *     at org.eclipse.core.internal.resources.Workspace.run(Workspace.java:2344)
         *     at org.eclipse.jdt.internal.core.JavaModelManager.initializeAllContainers(JavaModelManager.java:2816)
         *     at org.eclipse.jdt.internal.core.JavaModelManager.getClasspathContainer(JavaModelManager.java:1873)
         *     at org.eclipse.jdt.core.JavaCore.getClasspathContainer(JavaCore.java:2798)
         *     at org.eclipse.jdt.internal.core.JavaProject.resolveClasspath(JavaProject.java:2679)
         *     at org.eclipse.jdt.internal.core.JavaProject.resolveClasspath(JavaProject.java:2843)
         *     at org.eclipse.jdt.internal.core.JavaProject.getResolvedClasspath(JavaProject.java:1948)
         *     at org.eclipse.jdt.internal.core.JavaProject.buildStructure(JavaProject.java:463)
         *     at org.eclipse.jdt.internal.core.Openable.generateInfos(Openable.java:258)
         *     at org.eclipse.jdt.internal.core.JavaElement.openWhenClosed(JavaElement.java:518)
         *     at org.eclipse.jdt.internal.core.JavaElement.getElementInfo(JavaElement.java:255)
         *     at org.eclipse.jdt.internal.core.JavaElement.getElementInfo(JavaElement.java:241)
         *     at org.eclipse.jdt.internal.core.JavaElement.getChildren(JavaElement.java:196)
         *     at org.eclipse.jdt.internal.core.JavaProject.getPackageFragmentRoots(JavaProject.java:1836)
         *     at org.codehaus.groovy.eclipse.dsl.RefreshDSLDJob$DSLDResourceVisitor.findDSLDsInLibraries(RefreshDSLDJob.java:99)
         *     at org.codehaus.groovy.eclipse.dsl.RefreshDSLDJob$DSLDResourceVisitor.findFiles(RefreshDSLDJob.java:89)
         *     at org.codehaus.groovy.eclipse.dsl.RefreshDSLDJob.refreshProject(RefreshDSLDJob.java:279)
         *     at org.codehaus.groovy.eclipse.dsl.RefreshDSLDJob.run(RefreshDSLDJob.java:227)
         *     at org.eclipse.core.internal.jobs.Worker.run(Worker.java:54)
         * </pre>
         */
        private IPackageFragmentRoot[] getPackageFragmentRoots(final IProgressMonitor monitor) throws JavaModelException {
            IPackageFragmentRoot[][] roots = new IPackageFragmentRoot[1][];
            try {
                JavaCore.run(pm -> {
                    roots[0] = JavaCore.create(project).getAllPackageFragmentRoots();
                }, project, monitor);
            } catch (CoreException e) {
                if (e.getStatus().getCode() != IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
                    GroovyDSLCoreActivator.logException(e);
                }
            }
            return roots[0] != null ? roots[0] : new IPackageFragmentRoot[0];
        }
    }
}
