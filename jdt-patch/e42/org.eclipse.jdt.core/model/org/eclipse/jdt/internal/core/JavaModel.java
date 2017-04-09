/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 * Implementation of {@link IJavaModel}. The Java Model maintains a cache of active
 * {@link IJavaProject}s in a workspace. A Java Model is specific to a workspace.
 * To retrieve a workspace's model, use the
 * {@link IJavaElement#getJavaModel() #getJavaModel()} method.
 *
 * @see IJavaModel
 */
public class JavaModel extends Openable implements IJavaModel {

	/**
	 * A set of java.io.Files used as a cache of external jars that
	 * are known to be existing.
	 * Note this cache is kept for the whole session.
	 */
	public static HashSet existingExternalFiles = new HashSet();

	/**
	 * A set of external files ({@link #existingExternalFiles}) which have
	 * been confirmed as file (i.e. which returns true to {@link java.io.File#isFile()}.
	 * Note this cache is kept for the whole session.
	 */
	public static HashSet existingExternalConfirmedFiles = new HashSet();

/**
 * Constructs a new Java Model on the given workspace.
 * Note that only one instance of JavaModel handle should ever be created.
 * One should only indirect through JavaModelManager#getJavaModel() to get
 * access to it.
 *
 * @exception Error if called more than once
 */
protected JavaModel() throws Error {
	super(null);
}
protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)	/*throws JavaModelException*/ {

	// determine my children
	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	int length = projects.length;
	IJavaElement[] children = new IJavaElement[length];
	int index = 0;
	for (int i = 0; i < length; i++) {
		IProject project = projects[i];
		if (JavaProject.hasJavaNature(project)) {
			children[index++] = getJavaProject(project);
		}
	}
	if (index < length)
		System.arraycopy(children, 0, children = new IJavaElement[index], 0, index);
	info.setChildren(children);

	newElements.put(this, info);

	return true;
}
/*
 * @see IJavaModel
 */
public boolean contains(IResource resource) {
	switch (resource.getType()) {
		case IResource.ROOT:
		case IResource.PROJECT:
			return true;
	}
	// file or folder
	IJavaProject[] projects;
	try {
		projects = getJavaProjects();
	} catch (JavaModelException e) {
		return false;
	}
	for (int i = 0, length = projects.length; i < length; i++) {
		JavaProject project = (JavaProject)projects[i];
		if (!project.contains(resource)) {
			return false;
		}
	}
	return true;
}
/**
 * @see IJavaModel
 */
public void copy(IJavaElement[] elements, IJavaElement[] containers, IJavaElement[] siblings, String[] renamings, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (elements != null && elements.length > 0 && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		runOperation(new CopyResourceElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	} else {
		runOperation(new CopyElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	}
}
/**
 * Returns a new element info for this element.
 */
protected Object createElementInfo() {
	return new JavaModelInfo();
}

/**
 * @see IJavaModel
 */
public void delete(IJavaElement[] elements, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (elements != null && elements.length > 0 && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		new DeleteResourceElementsOperation(elements, force).runOperation(monitor);
	} else {
		new DeleteElementsOperation(elements, force).runOperation(monitor);
	}
}
public boolean equals(Object o) {
	if (!(o instanceof JavaModel)) return false;
	return super.equals(o);
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return JAVA_MODEL;
}
/**
 * Flushes the cache of external files known to be existing.
 */
public static void flushExternalFileCache() {
	existingExternalFiles = new HashSet();
	existingExternalConfirmedFiles = new HashSet();
}

/*
 * @see JavaElement
 */
public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner owner) {
	switch (token.charAt(0)) {
		case JEM_JAVAPROJECT:
			if (!memento.hasMoreTokens()) return this;
			String projectName = memento.nextToken();
			JavaElement project = (JavaElement)getJavaProject(projectName);
			return project.getHandleFromMemento(memento, owner);
	}
	return null;
}
/**
 * @see JavaElement#getHandleMemento(StringBuffer)
 */
protected void getHandleMemento(StringBuffer buff) {
	buff.append(getElementName());
}
/**
 * Returns the <code>char</code> that marks the start of this handles
 * contribution to a memento.
 */
protected char getHandleMementoDelimiter(){
	Assert.isTrue(false, "Should not be called"); //$NON-NLS-1$
	return 0;
}
/**
 * @see IJavaModel
 */
public IJavaProject getJavaProject(String projectName) {
	return new JavaProject(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName), this);
}
/**
 * Returns the active Java project associated with the specified
 * resource, or <code>null</code> if no Java project yet exists
 * for the resource.
 *
 * @exception IllegalArgumentException if the given resource
 * is not one of an IProject, IFolder, or IFile.
 */
public IJavaProject getJavaProject(IResource resource) {
	switch(resource.getType()){
		case IResource.FOLDER:
			return new JavaProject(((IFolder)resource).getProject(), this);
		case IResource.FILE:
			return new JavaProject(((IFile)resource).getProject(), this);
		case IResource.PROJECT:
			return new JavaProject((IProject)resource, this);
		default:
			throw new IllegalArgumentException(Messages.element_invalidResourceForProject);
	}
}
/**
 * @see IJavaModel
 */
public IJavaProject[] getJavaProjects() throws JavaModelException {
	ArrayList list = getChildrenOfType(JAVA_PROJECT);
	IJavaProject[] array= new IJavaProject[list.size()];
	list.toArray(array);
	return array;

}
/**
 * @see IJavaModel
 */
public Object[] getNonJavaResources() throws JavaModelException {
		return ((JavaModelInfo) getElementInfo()).getNonJavaResources();
}

/*
 * @see IJavaElement
 */
public IPath getPath() {
	return Path.ROOT;
}
/*
 * @see IJavaElement
 */
public IResource resource(PackageFragmentRoot root) {
	return ResourcesPlugin.getWorkspace().getRoot();
}
/**
 * @see IOpenable
 */
public IResource getUnderlyingResource() {
	return null;
}
/**
 * Returns the workbench associated with this object.
 */
public IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
}

/**
 * @see IJavaModel
 */
public void move(IJavaElement[] elements, IJavaElement[] containers, IJavaElement[] siblings, String[] renamings, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (elements != null && elements.length > 0 && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		runOperation(new MoveResourceElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	} else {
		runOperation(new MoveElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	}
}

/**
 * @see IJavaModel#refreshExternalArchives(IJavaElement[], IProgressMonitor)
 */
public void refreshExternalArchives(IJavaElement[] elementsScope, IProgressMonitor monitor) throws JavaModelException {
	if (elementsScope == null){
		elementsScope = new IJavaElement[] { this };
	}
	JavaModelManager.getJavaModelManager().getDeltaProcessor().checkExternalArchiveChanges(elementsScope, monitor);
}

/**
 * @see IJavaModel
 */
public void rename(IJavaElement[] elements, IJavaElement[] destinations, String[] renamings, boolean force, IProgressMonitor monitor) throws JavaModelException {
	MultiOperation op;
	if (elements != null && elements.length > 0 && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		op = new RenameResourceElementsOperation(elements, destinations, renamings, force);
	} else {
		op = new RenameElementsOperation(elements, destinations, renamings, force);
	}

	op.runOperation(monitor);
}
/**
 * Configures and runs the <code>MultiOperation</code>.
 */
protected void runOperation(MultiOperation op, IJavaElement[] elements, IJavaElement[] siblings, String[] renamings, IProgressMonitor monitor) throws JavaModelException {
	op.setRenamings(renamings);
	if (siblings != null) {
		for (int i = 0; i < elements.length; i++) {
			op.setInsertBefore(elements[i], siblings[i]);
		}
	}
	op.runOperation(monitor);
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info, boolean showResolvedInfo) {
	buffer.append(tabString(tab));
	buffer.append("Java Model"); //$NON-NLS-1$
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	}
}

/**
 * Helper method - returns the targeted item (IResource if internal or java.io.File if external),
 * or null if unbound
 * Internal items must be referred to using container relative paths.
 */
public static Object getTarget(IPath path, boolean checkResourceExistence) {
	Object target = getWorkspaceTarget(path); // Implicitly checks resource existence
	if (target != null)
		return target;
	return getExternalTarget(path, checkResourceExistence);
}
public static IResource getWorkspaceTarget(IPath path) {
	if (path == null || path.getDevice() != null)
		return null;
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	if (workspace == null)
		return null;
	return workspace.getRoot().findMember(path);
}
public static Object getExternalTarget(IPath path, boolean checkResourceExistence) {
	if (path == null)
		return null;
	ExternalFoldersManager externalFoldersManager = JavaModelManager.getExternalManager();
	Object linkedFolder = externalFoldersManager.getFolder(path);
	if (linkedFolder != null) {
		if (checkResourceExistence) {
			// check if external folder is present
			File externalFile = new File(path.toOSString());
			if (!externalFile.isDirectory()) {
				return null;
			}
		}
		return linkedFolder;
	}
	File externalFile = new File(path.toOSString());
	if (!checkResourceExistence) {
		return externalFile;
	} else if (existingExternalFilesContains(externalFile)) {
		return externalFile;
	} else {
		if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
			System.out.println("(" + Thread.currentThread() + ") [JavaModel.getTarget(...)] Checking existence of " + path.toString()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (externalFile.isFile()) { // isFile() checks for existence (it returns false if a directory)
			// cache external file
			existingExternalFilesAdd(externalFile);
			return externalFile;
		}
	}
	return null;
}
private synchronized static void existingExternalFilesAdd(File externalFile) {
	existingExternalFiles.add(externalFile);
}
private synchronized static boolean existingExternalFilesContains(File externalFile) {
	return existingExternalFiles.contains(externalFile);
}

/**
 * Helper method - returns whether an object is afile (i.e. which returns true to {@link java.io.File#isFile()}.
 */
public static boolean isFile(Object target) {
	return getFile(target) != null;
}

/**
 * Helper method - returns the file item (i.e. which returns true to {@link java.io.File#isFile()},
 * or null if unbound
 */
public static synchronized File getFile(Object target) {
	if (existingExternalConfirmedFiles.contains(target))
		return (File) target;
	if (target instanceof File) {
		File f = (File) target;
		if (f.isFile()) {
			existingExternalConfirmedFiles.add(f);
			return f;
		}
	}

	return null;
}

protected IStatus validateExistence(IResource underlyingResource) {
	// Java model always exists
	return JavaModelStatus.VERIFIED_OK;
}
}
