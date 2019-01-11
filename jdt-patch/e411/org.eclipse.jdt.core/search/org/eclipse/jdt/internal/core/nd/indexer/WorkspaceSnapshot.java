/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.indexer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;

/**
 * Represents a snapshot of all the indexable objects in the workspace at a given moment in time.
 */
public final class WorkspaceSnapshot {
	private Map<IPath, List<IJavaElement>> allIndexables;

	/**
	 * Enable this to index the content of output folders, in cases where that content exists and is up-to-date. This is
	 * much faster than indexing source files directly.
	 */
	public static boolean EXPERIMENTAL_INDEX_OUTPUT_FOLDERS;

	private WorkspaceSnapshot(Map<IPath, List<IJavaElement>> allIndexables) {
		this.allIndexables = allIndexables;
	}

	public Map<IPath, List<IJavaElement>> getAllIndexables() {
		return this.allIndexables;
	}

	public Set<IPath> allLocations() {
		return this.allIndexables.keySet();
	}

	public List<IJavaElement> get(IPath next) {
		List<IJavaElement> result = this.allIndexables.get(next);
		if (result == null) {
			return Collections.emptyList();
		}
		return result;
	}

	public static WorkspaceSnapshot create(IWorkspaceRoot root, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);

		List<IJavaElement> unfilteredIndexables = getAllIndexableObjectsInWorkspace(root, subMonitor.split(3));

		// Remove all duplicate indexables (jars which are referenced by more than one project)
		Map<IPath, List<IJavaElement>> allIndexables = removeDuplicatePaths(unfilteredIndexables);

		return new WorkspaceSnapshot(allIndexables);
	}

	private static IPath getWorkspacePathForRoot(IJavaElement next) {
		IResource resource = next.getResource();
		if (resource != null) {
			return resource.getFullPath();
		}
		return Path.EMPTY;
	}

	private static Map<IPath, List<IJavaElement>> removeDuplicatePaths(List<IJavaElement> allIndexables) {
		Map<IPath, List<IJavaElement>> paths = new HashMap<>();

		HashSet<IPath> workspacePaths = new HashSet<IPath>();
		for (IJavaElement next : allIndexables) {
			IPath nextPath = JavaIndex.getLocationForElement(next);
			IPath workspacePath = getWorkspacePathForRoot(next);

			List<IJavaElement> value = paths.get(nextPath);

			if (value == null) {
				value = new ArrayList<IJavaElement>();
				paths.put(nextPath, value);
			} else {
				if (workspacePath != null) {
					if (workspacePaths.contains(workspacePath)) {
						continue;
					}
					if (!workspacePath.isEmpty()) {
						Package.logInfo("Found duplicate workspace path for " + workspacePath.toString()); //$NON-NLS-1$
					}
					workspacePaths.add(workspacePath);
				}
			}

			value.add(next);
		}

		return paths;
	}

	private static List<IJavaElement> getAllIndexableObjectsInWorkspace(IWorkspaceRoot root, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		List<IJavaElement> allIndexables = new ArrayList<>();
		IProject[] projects = root.getProjects();

		List<IProject> projectsToScan = new ArrayList<>();

		for (IProject next : projects) {
			if (next.isOpen()) {
				projectsToScan.add(next);
			}
		}

		Set<IPath> scannedPaths = new HashSet<>();
		Set<IResource> resourcesToScan = new HashSet<>();
		SubMonitor projectLoopMonitor = subMonitor.split(1).setWorkRemaining(projectsToScan.size());
		for (IProject project : projectsToScan) {
			SubMonitor iterationMonitor = projectLoopMonitor.split(1);
			try {
				if (project.isOpen() && project.isNatureEnabled(JavaCore.NATURE_ID)) {
					IJavaProject javaProject = JavaCore.create(project);

					IClasspathEntry[] entries = javaProject.getRawClasspath();

					if (EXPERIMENTAL_INDEX_OUTPUT_FOLDERS) {
						IPath defaultOutputLocation = javaProject.getOutputLocation();
						for (IClasspathEntry next : entries) {
							IPath nextOutputLocation = next.getOutputLocation();
	
							if (nextOutputLocation == null) {
								nextOutputLocation = defaultOutputLocation;
							}
	
							IResource resource = root.findMember(nextOutputLocation);
							if (resource != null) {
								resourcesToScan.add(resource);
							}
						}
					}

					IPackageFragmentRoot[] projectRoots = javaProject.getAllPackageFragmentRoots();
					SubMonitor rootLoopMonitor = iterationMonitor.setWorkRemaining(projectRoots.length);
					for (IPackageFragmentRoot nextRoot : projectRoots) {
						rootLoopMonitor.split(1);
						if (!nextRoot.exists()) {
							continue;
						}
						IPath filesystemPath = JavaIndex.getLocationForElement(nextRoot);
						if (scannedPaths.contains(filesystemPath)) {
							continue;
						}
						scannedPaths.add(filesystemPath);
						if (nextRoot.getKind() == IPackageFragmentRoot.K_BINARY) {
							if (nextRoot.isArchive()) {
								allIndexables.add(nextRoot);
							} else {
								collectAllClassFiles(root, allIndexables, nextRoot);
							}
						} else {
							collectAllClassFiles(root, allIndexables, nextRoot);
						}
					}
				}
			} catch (CoreException e) {
				Package.log(e);
			}
		}

		collectAllClassFiles(root, allIndexables, resourcesToScan, subMonitor.split(1));
		return allIndexables;
	}

	private static void collectAllClassFiles(IWorkspaceRoot root, List<? super IClassFile> result, 
			Collection<? extends IResource> toScan, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor);

		ArrayDeque<IResource> resources = new ArrayDeque<>();
		resources.addAll(toScan);

		while (!resources.isEmpty()) {
			subMonitor.setWorkRemaining(Math.max(resources.size(), 3000)).split(1);
			IResource next = resources.removeFirst();

			if (next instanceof IContainer) {
				IContainer container = (IContainer)next;

				try {
					for (IResource nextChild : container.members()) {
						resources.addLast(nextChild);
					}
				} catch (CoreException e) {
					// If an error occurs in one resource, skip it and move on to the next
					Package.log(e);
				}
			} else if (next instanceof IFile) {
				IFile file = (IFile) next;

				String extension = file.getFileExtension();
				if (Objects.equals(extension, "class")) { //$NON-NLS-1$
					IJavaElement element = JavaCore.create(file);

					if (element instanceof IClassFile) {
						result.add((IClassFile)element);
					}
				}
			}
		}
	}

	private static void collectAllClassFiles(IWorkspaceRoot root, List<? super IClassFile> result,
			IParent nextRoot) throws CoreException {
		for (IJavaElement child : nextRoot.getChildren()) {
			try {
				int type = child.getElementType();
				if (type == IJavaElement.COMPILATION_UNIT) {
					continue;
				}
				if (!child.exists()) {
					continue;
				}

				if (type == IJavaElement.CLASS_FILE) {
					result.add((IClassFile)child);
				} else if (child instanceof IParent) {
					IParent parent = (IParent) child;

					collectAllClassFiles(root, result, parent);
				}
			} catch (CoreException e) {
				// Log exceptions, then continue with the next child
				Package.log(e);
			}
		}
	}
}
