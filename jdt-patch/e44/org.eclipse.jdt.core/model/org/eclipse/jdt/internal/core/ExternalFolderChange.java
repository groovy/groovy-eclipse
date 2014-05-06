/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - inconsistent initialization of classpath container backed by external class folder, see https://bugs.eclipse.org/320618
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;

@SuppressWarnings("rawtypes")
public class ExternalFolderChange {

	private JavaProject project;
	private IClasspathEntry[] oldResolvedClasspath;

	public ExternalFolderChange(JavaProject project, IClasspathEntry[] oldResolvedClasspath) {
		this.project = project;
		this.oldResolvedClasspath = oldResolvedClasspath;
	}

	/*
	 * Update external folders
	 */
	public void updateExternalFoldersIfNecessary(boolean refreshIfExistAlready, IProgressMonitor monitor) throws JavaModelException {
		HashSet oldFolders = ExternalFoldersManager.getExternalFolders(this.oldResolvedClasspath);
		IClasspathEntry[] newResolvedClasspath = this.project.getResolvedClasspath();
		HashSet newFolders = ExternalFoldersManager.getExternalFolders(newResolvedClasspath);
		if (newFolders == null)
			return;
		ExternalFoldersManager foldersManager = JavaModelManager.getExternalManager();
		Iterator iterator = newFolders.iterator();
		while (iterator.hasNext()) {
			Object folderPath = iterator.next();
			if (oldFolders == null || !oldFolders.remove(folderPath) || foldersManager.removePendingFolder(folderPath)) {
				try {
					foldersManager.createLinkFolder((IPath) folderPath, refreshIfExistAlready, monitor);
				} catch (CoreException e) {
					throw new JavaModelException(e);
				}
			}
		}
		// removal of linked folders is done during save
	}
	public String toString() {
		return "ExternalFolderChange: " + this.project.getElementName(); //$NON-NLS-1$
	}
}
