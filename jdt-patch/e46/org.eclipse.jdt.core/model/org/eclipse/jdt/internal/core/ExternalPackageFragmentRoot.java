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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A package fragment root that corresponds to an external class folder.
 *
 * <p>NOTE: An external package fragment root never has an associated resource.
 *
 * @see org.eclipse.jdt.core.IPackageFragmentRoot
 * @see org.eclipse.jdt.internal.core.PackageFragmentRootInfo
 */
public class ExternalPackageFragmentRoot extends PackageFragmentRoot {

	/**
	 * The path to the external folder
	 * (an OS path)
	 */
	protected final IPath externalPath;

	/**
	 * Constructs a package fragment root which is the root of the Java package directory hierarchy
	 * based on an external folder that is not contained in a <code>IJavaProject</code> and
	 * does not have an associated <code>IResource</code>.
	 */
	protected ExternalPackageFragmentRoot(IPath externalPath, JavaProject project) {
		super(null, project);
		this.externalPath = externalPath;
	}

	protected ExternalPackageFragmentRoot(IResource linkedFolder, IPath externalPath, JavaProject project) {
		super(linkedFolder, project);
		this.externalPath = externalPath == null ? linkedFolder.getLocation() : externalPath;
	}

	/**
	 * An external class folder is always K_BINARY.
	 */
	protected int determineKind(IResource underlyingResource) {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * Returns true if this handle represents the same external folder
	 * as the given handle.
	 *
	 * @see Object#equals
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof ExternalPackageFragmentRoot) {
			ExternalPackageFragmentRoot other= (ExternalPackageFragmentRoot) o;
			return this.externalPath.equals(other.externalPath);
		}
		return false;
	}
	public String getElementName() {
		return this.externalPath.lastSegment();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public int getKind() {
		return IPackageFragmentRoot.K_BINARY;
	}
	int internalKind() throws JavaModelException {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPath getPath() {
		return this.externalPath;
	}

	/**
	 * @see IJavaElement
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		return null;
	}
	public int hashCode() {
		return this.externalPath.hashCode();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public boolean isExternal() {
		return true;
	}

	public IResource resource(PackageFragmentRoot root) {
		if (this.resource == null)
			return this.resource = JavaModelManager.getExternalManager().getFolder(this.externalPath);
		return super.resource(root);
	}

	protected boolean resourceExists(IResource underlyingResource) {
		if (underlyingResource == null)
			return false;
		IPath location = underlyingResource.getLocation();
		if (location == null)
			return false;
		File file = location.toFile();
		if (file == null)
			return false;
		return file.exists();
	}

	protected void toStringAncestors(StringBuffer buffer) {
		// don't show project as it is irrelevant for external folders.
	}
}
