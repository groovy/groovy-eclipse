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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJarEntryResource;

public class JarEntryDirectory extends JarEntryResource {
	private IJarEntryResource[] children;

	public JarEntryDirectory(String simpleName) {
		super(simpleName);
	}

	public JarEntryResource clone(Object newParent) {
		JarEntryDirectory dir = new JarEntryDirectory(this.simpleName);
		dir.setParent(newParent);
		int length = this.children.length;
		if (length > 0) {
			IJarEntryResource[] newChildren = new IJarEntryResource[length];
			for (int i = 0; i < length; i++) {
				JarEntryResource child = (JarEntryResource) this.children[i];
				newChildren[i] = child.clone(dir);
			}
			dir.setChildren(newChildren);
		}
		return dir;
	}

	public IJarEntryResource[] getChildren() {
		return this.children;
	}

	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(new byte[0]);
	}

	public boolean isFile() {
		return false;
	}

	public void setChildren(IJarEntryResource[] children) {
		this.children = children;
	}

	public String toString() {
		return "JarEntryDirectory["+getEntryName()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
