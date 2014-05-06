/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - inconsistent initialization of classpath container backed by external class folder, see https://bugs.eclipse.org/320618
 *     Thirumala Reddy Mutchukota <thirumala@google.com> - Contribution to bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=411423
 *     Terry Parker <tparker@google.com> - [performance] Low hit rates in JavaModel caches - https://bugs.eclipse.org/421165
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ExternalFoldersManager {
	private static final String EXTERNAL_PROJECT_NAME = ".org.eclipse.jdt.core.external.folders"; //$NON-NLS-1$
	private static final String LINKED_FOLDER_NAME = ".link"; //$NON-NLS-1$
	private Map folders;
	private Set pendingFolders; // subset of keys of 'folders', for which linked folders haven't been created yet.
	private int counter = 0;
	/* Singleton instance */
	private static ExternalFoldersManager MANAGER;

	private ExternalFoldersManager() {
		// Prevent instantiation
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=377806
		if (Platform.isRunning()) {
			getFolders();
		}
	}
	
	public static synchronized ExternalFoldersManager getExternalFoldersManager() {
		if (MANAGER == null) {
			 MANAGER = new ExternalFoldersManager();
		}
		return MANAGER;
	}
	
	/**
	 * Returns a set of external paths to external folders referred to on the given classpath.
	 * Returns <code>null</code> if there are none.
	 */
	public static HashSet getExternalFolders(IClasspathEntry[] classpath) {
		if (classpath == null)
			return null;
		HashSet folders = null;
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				IPath entryPath = entry.getPath();
				if (isExternalFolderPath(entryPath)) {
					if (folders == null)
						folders = new HashSet();
					folders.add(entryPath);
				}
				IPath attachmentPath = entry.getSourceAttachmentPath();
				if (isExternalFolderPath(attachmentPath)) {
					if (folders == null)
						folders = new HashSet();
					folders.add(attachmentPath);
				}
			}
		}
		return folders;
	}

	/**
	 * Returns <code>true</code> if the provided path is a folder external to the project.
	 */
	public static boolean isExternalFolderPath(IPath externalPath) {
		if (externalPath == null)
			return false;
		String firstSegment = externalPath.segment(0);
		if (firstSegment != null && ResourcesPlugin.getWorkspace().getRoot().getProject(firstSegment).exists())
			return false;
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		if (manager.isExternalFile(externalPath) || manager.isAssumedExternalFile(externalPath))
			return false;
		File externalFolder = externalPath.toFile();
		if (externalFolder.isFile()) {
			manager.addExternalFile(externalPath);
			return false;
		}
		if (externalPath.getFileExtension() != null/*likely a .jar, .zip, .rar or other file*/ && !externalFolder.exists()) {
			manager.addAssumedExternalFile(externalPath);
			return false;
		}
		return true;
	}

	public static boolean isInternalPathForExternalFolder(IPath resourcePath) {
		return EXTERNAL_PROJECT_NAME.equals(resourcePath.segment(0));
	}

	public IFolder addFolder(IPath externalFolderPath, boolean scheduleForCreation) {
		return addFolder(externalFolderPath, getExternalFoldersProject(), scheduleForCreation);
	}

	private IFolder addFolder(IPath externalFolderPath, IProject externalFoldersProject, boolean scheduleForCreation) {
		Map knownFolders = getFolders();
		Object existing = knownFolders.get(externalFolderPath);
		if (existing != null) {
			return (IFolder) existing;
		}
		IFolder result;
		do {
			result = externalFoldersProject.getFolder(LINKED_FOLDER_NAME + this.counter++);
		} while (result.exists());
		if (scheduleForCreation) {
			synchronized(this) {
				if (this.pendingFolders == null)
					this.pendingFolders = Collections.synchronizedSet(new HashSet());
			}
			this.pendingFolders.add(externalFolderPath);
		}
		knownFolders.put(externalFolderPath, result);
		return result;
	}
	
	/** 
	 * Try to remove the argument from the list of folders pending for creation.
	 * @param externalPath to link to
	 * @return true if the argument was found in the list of pending folders and could be removed from it.
	 */
	public synchronized boolean removePendingFolder(Object externalPath) {
		if (this.pendingFolders == null)
			return false;
		return this.pendingFolders.remove(externalPath);
	}

	public IFolder createLinkFolder(IPath externalFolderPath, boolean refreshIfExistAlready, IProgressMonitor monitor) throws CoreException {
		IProject externalFoldersProject = createExternalFoldersProject(monitor); // run outside synchronized as this can create a resource
		return createLinkFolder(externalFolderPath, refreshIfExistAlready, externalFoldersProject, monitor);
	}

	private IFolder createLinkFolder(IPath externalFolderPath, boolean refreshIfExistAlready,
									IProject externalFoldersProject, IProgressMonitor monitor) throws CoreException {
		
		IFolder result = addFolder(externalFolderPath, externalFoldersProject, false);
		if (!result.exists())
			result.createLink(externalFolderPath, IResource.ALLOW_MISSING_LOCAL, monitor);
		else if (refreshIfExistAlready)
			result.refreshLocal(IResource.DEPTH_INFINITE,  monitor);
		return result;
	}

	public void createPendingFolders(IProgressMonitor monitor) throws JavaModelException{
		synchronized (this) {
			if (this.pendingFolders == null || this.pendingFolders.isEmpty()) return;
		}
		
		IProject externalFoldersProject = null;
		try {
			externalFoldersProject = createExternalFoldersProject(monitor);
		}
		catch(CoreException e) {
			throw new JavaModelException(e);
		}
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=368152
		// To avoid race condition (from addFolder and removeFolder, load the map elements into an array and clear the map immediately.
		// The createLinkFolder being in the synchronized block can cause a deadlock and hence keep it out of the synchronized block. 
		Object[] arrayOfFolders = null;
		synchronized (this.pendingFolders) {
			arrayOfFolders = this.pendingFolders.toArray();
			this.pendingFolders.clear();
		}

		for (int i=0; i < arrayOfFolders.length; i++) {
			try {
				createLinkFolder((IPath) arrayOfFolders[i], false, externalFoldersProject, monitor);
			} catch (CoreException e) {
				Util.log(e, "Error while creating a link for external folder :" + arrayOfFolders[i]); //$NON-NLS-1$
			}
		}
	}
	
	public void cleanUp(IProgressMonitor monitor) throws CoreException {
		ArrayList toDelete = getFoldersToCleanUp(monitor);
		if (toDelete == null)
			return;
		for (Iterator iterator = toDelete.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			IFolder folder = (IFolder) entry.getValue();
			folder.delete(true, monitor);
			IPath key = (IPath) entry.getKey();
			this.folders.remove(key);
		}
		IProject project = getExternalFoldersProject();
		if (project.isAccessible() && project.members().length == 1/*remaining member is .project*/)
			project.delete(true, monitor);
	}

	private ArrayList getFoldersToCleanUp(IProgressMonitor monitor) throws CoreException {
		DeltaProcessingState state = JavaModelManager.getDeltaState();
		HashMap roots = state.roots;
		HashMap sourceAttachments = state.sourceAttachments;
		if (roots == null && sourceAttachments == null)
			return null;
		Map knownFolders = getFolders();
		ArrayList result = null;
		synchronized (knownFolders) {
			Iterator iterator = knownFolders.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				IPath path = (IPath) entry.getKey();
				if ((roots != null && !roots.containsKey(path))
						&& (sourceAttachments != null && !sourceAttachments.containsKey(path))) {
					if (entry.getValue() != null) {
						if (result == null)
							result = new ArrayList();
						result.add(entry);
					}
				}
			}
		}
		return result;
	}

	public IProject getExternalFoldersProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(EXTERNAL_PROJECT_NAME);
	}
	public IProject createExternalFoldersProject(IProgressMonitor monitor) throws CoreException {
		IProject project = getExternalFoldersProject();
		if (!project.isAccessible()) {
			if (!project.exists()) {
				createExternalFoldersProject(project, monitor);
			}
			openExternalFoldersProject(project, monitor);
		}
		return project;
	}

	/*
	 * Attempt to open the given project (assuming it exists).
	 * If failing to open, make all attempts to recreate the missing pieces.
	 */
	private void openExternalFoldersProject(IProject project, IProgressMonitor monitor) throws CoreException {
		try {
			project.open(monitor);
		} catch (CoreException e1) {
			if (e1.getStatus().getCode() == IResourceStatus.FAILED_READ_METADATA) {
				// workspace was moved 
				// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=241400 and https://bugs.eclipse.org/bugs/show_bug.cgi?id=252571 )
				project.delete(false/*don't delete content*/, true/*force*/, monitor);
				createExternalFoldersProject(project, monitor);
			} else {
				// .project or folder on disk have been deleted, recreate them
				IPath stateLocation = JavaCore.getPlugin().getStateLocation();
				IPath projectPath = stateLocation.append(EXTERNAL_PROJECT_NAME);
				projectPath.toFile().mkdirs();
				try {
				    FileOutputStream output = new FileOutputStream(projectPath.append(".project").toOSString()); //$NON-NLS-1$
				    try {
				        output.write((
				        		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //$NON-NLS-1$
				        		"<projectDescription>\n" + //$NON-NLS-1$
				        		"	<name>" + EXTERNAL_PROJECT_NAME + "</name>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				        		"	<comment></comment>\n" + //$NON-NLS-1$
				        		"	<projects>\n" + //$NON-NLS-1$
				        		"	</projects>\n" + //$NON-NLS-1$
				        		"	<buildSpec>\n" + //$NON-NLS-1$
				        		"	</buildSpec>\n" + //$NON-NLS-1$
				        		"	<natures>\n" + //$NON-NLS-1$
				        		"	</natures>\n" + //$NON-NLS-1$
				        		"</projectDescription>").getBytes()); //$NON-NLS-1$
				    } finally {
				        output.close();
				    }
				} catch (IOException e) {
					// fallback to re-creating the project
					project.delete(false/*don't delete content*/, true/*force*/, monitor);
					createExternalFoldersProject(project, monitor);
				}
			}
			project.open(monitor);
		}
	}


	private void createExternalFoldersProject(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
		IPath stateLocation = JavaCore.getPlugin().getStateLocation();
		desc.setLocation(stateLocation.append(EXTERNAL_PROJECT_NAME));
		project.create(desc, IResource.HIDDEN, monitor);
	}

	public IFolder getFolder(IPath externalFolderPath) {
		return (IFolder) getFolders().get(externalFolderPath);
	}

	private Map getFolders() {
		if (this.folders == null) {
			Map tempFolders = new HashMap();
			IProject project = getExternalFoldersProject();
			try {
				if (!project.isAccessible()) {
					if (project.exists()) {
						// workspace was moved (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=252571 )
						openExternalFoldersProject(project, null/*no progress*/);
					} else {
						// if project doesn't exist, do not open and recreate it as it means that there are no external folders
						return this.folders = Collections.synchronizedMap(tempFolders);
					}
				}
				IResource[] members = project.members();
				for (int i = 0, length = members.length; i < length; i++) {
					IResource member = members[i];
					if (member.getType() == IResource.FOLDER && member.isLinked() && member.getName().startsWith(LINKED_FOLDER_NAME)) {
						IPath externalFolderPath = member.getLocation();
						tempFolders.put(externalFolderPath, member);
					}
				}
			} catch (CoreException e) {
				Util.log(e, "Exception while initializing external folders"); //$NON-NLS-1$
			}
			this.folders = Collections.synchronizedMap(tempFolders);
		}
		return this.folders;
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=313153
	// Use the same RefreshJob if the job is still available
	private void runRefreshJob(Collection paths) {
		Job[] jobs = Job.getJobManager().find(ResourcesPlugin.FAMILY_MANUAL_REFRESH);
		RefreshJob refreshJob = null;
		if (jobs != null) {
			for (int index = 0; index < jobs.length; index++) {
				// We are only concerned about ExternalFolderManager.RefreshJob
				if(jobs[index] instanceof RefreshJob) {
					refreshJob =  (RefreshJob) jobs[index];
					refreshJob.addFoldersToRefresh(paths);
					if (refreshJob.getState() == Job.NONE) {
						refreshJob.schedule();
					}
					break;
				}
			}
		}
		if (refreshJob == null) {
			refreshJob = new RefreshJob(new Vector(paths));
			refreshJob.schedule();
		}
	}
	/*
	 * Refreshes the external folders referenced on the classpath of the given source project
	 */
	public void refreshReferences(final IProject[] sourceProjects, IProgressMonitor monitor) {
		IProject externalProject = getExternalFoldersProject();
		try {
			HashSet externalFolders = null;
			for (int index = 0; index < sourceProjects.length; index++) {
				if (sourceProjects[index].equals(externalProject))
					continue;
				if (!JavaProject.hasJavaNature(sourceProjects[index]))
					continue;

				HashSet foldersInProject = getExternalFolders(((JavaProject) JavaCore.create(sourceProjects[index])).getResolvedClasspath());
				
				if (foldersInProject == null || foldersInProject.size() == 0)
					continue;
				if (externalFolders == null)
					externalFolders = new HashSet();
				
				externalFolders.addAll(foldersInProject);
			}
			if (externalFolders == null) 
				return;

			runRefreshJob(externalFolders);

		} catch (CoreException e) {
			Util.log(e, "Exception while refreshing external project"); //$NON-NLS-1$
		}
		return;
	}
	public void refreshReferences(IProject source, IProgressMonitor monitor) {
		IProject externalProject = getExternalFoldersProject();
		if (source.equals(externalProject))
			return;
		if (!JavaProject.hasJavaNature(source))
			return;
		try {
			HashSet externalFolders = getExternalFolders(((JavaProject) JavaCore.create(source)).getResolvedClasspath());
			if (externalFolders == null)
				return;
			
			runRefreshJob(externalFolders);
		} catch (CoreException e) {
			Util.log(e, "Exception while refreshing external project"); //$NON-NLS-1$
		}
		return;
	}

	public IFolder removeFolder(IPath externalFolderPath) {
		return (IFolder) getFolders().remove(externalFolderPath);
	}

	class RefreshJob extends Job {
		Vector externalFolders = null;
		RefreshJob(Vector externalFolders){
			super(Messages.refreshing_external_folders);
			this.externalFolders = externalFolders;
		}
		
		public boolean belongsTo(Object family) {
			return family == ResourcesPlugin.FAMILY_MANUAL_REFRESH;
		}
		
		/*
		 * Add the collection of paths to be refreshed to the already 
		 * existing list of paths.  
		 */
		public void addFoldersToRefresh(Collection paths) {
			if (!paths.isEmpty() && this.externalFolders == null) {
				this.externalFolders = new Vector(); 
			}
			Iterator it = paths.iterator();
			while(it.hasNext()) {
				Object path = it.next();
				if (!this.externalFolders.contains(path)) {
					this.externalFolders.add(path);
				}
			}
		}
		
		protected IStatus run(IProgressMonitor pm) {
			try {
				if (this.externalFolders == null) 
					return Status.OK_STATUS;
				IPath externalPath = null;
				for (int index = 0; index < this.externalFolders.size(); index++ ) {
					if ((externalPath = (IPath)this.externalFolders.get(index)) != null) {
						IFolder folder = getFolder(externalPath);
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321358
						if (folder != null)
							folder.refreshLocal(IResource.DEPTH_INFINITE, pm);
					}
					// Set the processed ones to null instead of removing the element altogether,
					// so that they will not be considered as duplicates.
					// This will also avoid elements being shifted to the left every time an element
					// is removed. However, there is a risk of Collection size to be increased more often.
					this.externalFolders.setElementAt(null, index);
				}
			} catch (CoreException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		}
	}
	
}
