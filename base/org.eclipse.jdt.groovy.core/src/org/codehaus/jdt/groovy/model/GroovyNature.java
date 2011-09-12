/*******************************************************************************
 * Copyright (c) 2009, 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *     Nieraj Singh - Additional work
 *******************************************************************************/
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

	public static final String GROOVY_NATURE = "org.eclipse.jdt.groovy.core.groovyNature"; //$NON-NLS-1$

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
		final WorkspaceJob job = new WorkspaceJob("Cleaning/Rebuilding Project: " + project.getName()) { //$NON-NLS-1$
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
	public void configure() throws CoreException {
		configure(getProject());
	}

	public void deconfigure() throws CoreException {
		deconfigure(getProject());
	}

	/**
	 * @see IProjectNature#getProject
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @see IProjectNature#setProject
	 */
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
	 * 
	 * @return
	 */
	public static List<IProject> getAllAccessibleGroovyProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> groovyProjects = new ArrayList<IProject>();
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
