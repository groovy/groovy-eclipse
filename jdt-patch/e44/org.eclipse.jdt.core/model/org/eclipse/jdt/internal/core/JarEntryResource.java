/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.Util;

public abstract class JarEntryResource  extends PlatformObject implements IJarEntryResource {

	protected Object parent;
	protected String simpleName;

	public JarEntryResource(String simpleName) {
		this.simpleName = simpleName;
	}

	public abstract JarEntryResource clone(Object newParent);

	public boolean equals(Object obj) {
		if (! (obj instanceof JarEntryResource))
			return false;
		JarEntryResource other = (JarEntryResource) obj;
		return this.parent.equals(other.parent) && this.simpleName.equals(other.simpleName);
	}

	protected String getEntryName() {
		String parentEntryName;
		if (this.parent instanceof IPackageFragment) {
			String elementName = ((IPackageFragment) this.parent).getElementName();
			parentEntryName = elementName.length() == 0 ? "" : elementName .replace('.', '/') + '/'; //$NON-NLS-1$
		} else if (this.parent instanceof IPackageFragmentRoot) {
			parentEntryName = ""; //$NON-NLS-1$
		} else {
			parentEntryName = ((JarEntryDirectory) this.parent).getEntryName() + '/';
		}
		return parentEntryName + this.simpleName;
	}

	public IPath getFullPath() {
		return new Path(getEntryName()).makeAbsolute();
	}

	public String getName() {
		return this.simpleName;
	}

	public Object getParent() {
		return this.parent;
	}

	public IPackageFragmentRoot getPackageFragmentRoot() {
		if (this.parent instanceof IPackageFragment) {
			return (IPackageFragmentRoot) ((IPackageFragment) this.parent).getParent();
		} else if (this.parent instanceof IPackageFragmentRoot) {
			return (IPackageFragmentRoot) this.parent;
		} else {
			return ((JarEntryDirectory) this.parent).getPackageFragmentRoot();
		}
	}

	protected ZipFile getZipFile() throws CoreException {
		if (this.parent instanceof IPackageFragment) {
			JarPackageFragmentRoot root = (JarPackageFragmentRoot) ((IPackageFragment) this.parent).getParent();
			return root.getJar();
		} else if (this.parent instanceof JarPackageFragmentRoot) {
			return ((JarPackageFragmentRoot) this.parent).getJar();
		} else
			return ((JarEntryDirectory) this.parent).getZipFile();
	}

	public int hashCode() {
		return Util.combineHashCodes(this.simpleName.hashCode(), this.parent.hashCode());
	}

	public boolean isReadOnly() {
		return true;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}
}
