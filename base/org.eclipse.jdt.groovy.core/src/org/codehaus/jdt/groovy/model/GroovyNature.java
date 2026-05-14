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
package org.codehaus.jdt.groovy.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @see IProjectNature
 */
public class GroovyNature implements IProjectNature {

    private IProject project;

    public static final String GROOVY_NATURE = "org.eclipse.jdt.groovy.core.groovyNature";

    public static void configure(final IProject project) throws CoreException {
        cleanAndRebuildProject(project);
    }

    public static void deconfigure(final IProject project) throws CoreException {
        cleanAndRebuildProject(project);
    }

    private static void cleanAndRebuildProject(final IProject project) {
        if (project == null || !project.isAccessible())
            return;
        final Job[] existingJob = Job.getJobManager().find(project);
        // We only want to have one these guys active at a time.
        if (existingJob != null && existingJob.length > 0)
            return;
        final WorkspaceJob job = new WorkspaceJob("Cleaning/Rebuilding Project: " + project.getName()) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                project.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
                if (!ResourcesPlugin.getWorkspace().isAutoBuilding())
                    project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                return Status.OK_STATUS;
            }

            @Override
            public boolean belongsTo(final Object family) {
                if (family instanceof IProject)
                    return project.getName().equals(((IProject) family).getName());
                return super.belongsTo(family);
            }
        };
        job.setRule(ResourcesPlugin.getWorkspace().getRoot());
        job.schedule();
    }

    /**
     * @see IProjectNature#configure
     */
    @Override
    public void configure() throws CoreException {
        configure(getProject());
    }

    @Override
    public void deconfigure() throws CoreException {
        deconfigure(getProject());
    }

    /**
     * @see IProjectNature#getProject
     */
    @Override
    public IProject getProject() {
        return project;
    }

    /**
     * @see IProjectNature#setProject
     */
    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

    public static boolean hasGroovyNature(IProject project) {
        try {
            return project.hasNature(GROOVY_NATURE);
        } catch (CoreException e) {
            // project does not exist or is not open
        }
        return false;
    }

    /**
     * Returns a new copy of all available and accessible Groovy projects in the workspace
     */
    public static List<IProject> getAllAccessibleGroovyProjects() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> groovyProjects = new ArrayList<>();
        if (projects != null) {
            for (IProject project : projects) {
                if (project.isAccessible() && hasGroovyNature(project)) {
                    groovyProjects.add(project);
                }
            }
        }
        return groovyProjects;
    }
}
