/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Element info for PackageFragments.
 */
class PackageFragmentInfo extends OpenableElementInfo {

	/**
	 * A array with all the non-java resources contained by this PackageFragment
	 */
	protected Object[] nonJavaResources;

/**
 * Create and initialize a new instance of the receiver
 */
public PackageFragmentInfo() {
	this.nonJavaResources = null;
}
/**
 */
boolean containsJavaResources() {
	return this.children.length != 0;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
Object[] getNonJavaResources(IResource underlyingResource, PackageFragmentRoot rootHandle) {
	if (this.nonJavaResources == null) {
		try {
			this.nonJavaResources =
				PackageFragmentRootInfo.computeFolderNonJavaResources(
					rootHandle,
					(IContainer)underlyingResource,
					rootHandle.fullInclusionPatternChars(),
					rootHandle.fullExclusionPatternChars());
		} catch (JavaModelException e) {
			// root doesn't exist: consider package has no nonJavaResources
			this.nonJavaResources = NO_NON_JAVA_RESOURCES;
		}
	}
	return this.nonJavaResources;
}
/**
 * Set the nonJavaResources to res value
 */
void setNonJavaResources(Object[] resources) {
	this.nonJavaResources = resources;
}
}
