/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The element info for <code>JarPackageFragmentRoot</code>s.
 */
class JarPackageFragmentRootInfo extends PackageFragmentRootInfo {
	/** contains .class file names, and non-Java resource names of a package */
	static record PackageContent(List<String> javaClasses, List<String> resources) {
		PackageContent() {
			this(new ArrayList<>(), new ArrayList<>());
		}
	}

	/**
	 * Cache for the the jar's entries names. A unmodifiable map from package name to PackageContent
	 */
	Map<List<String>, PackageContent> rawPackageInfo;
	Map<String, String> overriddenClasses;
}
