/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Set;

import org.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;

/**
 * The element info for <code>JarPackageFragmentRoot</code>s.
 */
class JarPackageFragmentRootInfo extends PackageFragmentRootInfo {
	// a map from package name (String[]) to a size-2 array of Array<String>, the first element being the .class file names, and the second element being the non-Java resource names
	HashtableOfArrayToObject rawPackageInfo;
	Set<String> overriddenClasses;
	
}
