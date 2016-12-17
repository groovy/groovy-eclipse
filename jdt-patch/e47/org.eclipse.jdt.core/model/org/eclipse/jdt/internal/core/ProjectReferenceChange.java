/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ProjectReferenceChange {

	private JavaProject project;
	private IClasspathEntry[] oldResolvedClasspath;

	public ProjectReferenceChange(JavaProject project, IClasspathEntry[] oldResolvedClasspath) {
		this.project = project;
		this.oldResolvedClasspath = oldResolvedClasspath;
	}

	/*
	 * Update projects references so that the build order is consistent with the classpath
	 */
	public void updateProjectReferencesIfNecessary() throws JavaModelException {
		
		String[] oldRequired = this.oldResolvedClasspath == null ? CharOperation.NO_STRINGS : this.project.projectPrerequisites(this.oldResolvedClasspath);
		IClasspathEntry[] newResolvedClasspath = this.project.getResolvedClasspath();
		String[] newRequired = this.project.projectPrerequisites(newResolvedClasspath);
		final IProject projectResource = this.project.getProject();
		
		try {
			IProject[] projectReferences = projectResource.getDescription().getDynamicReferences();
			
			HashSet oldReferences = new HashSet(projectReferences.length);
			for (int i = 0; i < projectReferences.length; i++){
				String projectName = projectReferences[i].getName();
				oldReferences.add(projectName);
			}
			HashSet newReferences = (HashSet)oldReferences.clone();
	
			for (int i = 0; i < oldRequired.length; i++){
				String projectName = oldRequired[i];
				newReferences.remove(projectName);
			}
			for (int i = 0; i < newRequired.length; i++){
				String projectName = newRequired[i];
				newReferences.add(projectName);
			}
	
			Iterator iter;
			int newSize = newReferences.size();
			
			checkIdentity: {
				if (oldReferences.size() == newSize){
					iter = newReferences.iterator();
					while (iter.hasNext()){
						if (!oldReferences.contains(iter.next())){
							break checkIdentity;
						}
					}
					return;
				}
			}
			String[] requiredProjectNames = new String[newSize];
			int index = 0;
			iter = newReferences.iterator();
			while (iter.hasNext()){
				requiredProjectNames[index++] = (String)iter.next();
			}
			Util.sort(requiredProjectNames); // ensure that if changed, the order is consistent
			
			final IProject[] requiredProjectArray = new IProject[newSize];
			IWorkspaceRoot wksRoot = projectResource.getWorkspace().getRoot();
			for (int i = 0; i < newSize; i++){
				requiredProjectArray[i] = wksRoot.getProject(requiredProjectNames[i]);
			}
				
			// ensure that a scheduling rule is used so that the project description is not modified by another thread while we update it
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=214981
			// also ensure that if no change (checkIdentify block returned above) we don't reach here
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=241751
			IWorkspace workspace = projectResource.getWorkspace();
			ISchedulingRule rule = workspace.getRuleFactory().modifyRule(projectResource); // scheduling rule for modifying the project
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IProjectDescription description = projectResource.getDescription();
					description.setDynamicReferences(requiredProjectArray);
					projectResource.setDescription(description, IResource.AVOID_NATURE_CONFIG, null);
				}
			};
			workspace.run(runnable, rule, IWorkspace.AVOID_UPDATE, null);
		} catch(CoreException e){
			if (!ExternalJavaProject.EXTERNAL_PROJECT_NAME.equals(this.project.getElementName()))
				throw new JavaModelException(e);
		}
	}
	public String toString() {
		return "ProjectRefenceChange: " + this.project.getElementName(); //$NON-NLS-1$
	}
}
