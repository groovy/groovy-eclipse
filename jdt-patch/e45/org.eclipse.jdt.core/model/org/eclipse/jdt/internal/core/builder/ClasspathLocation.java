/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

public abstract class ClasspathLocation {

static ClasspathLocation forSourceFolder(IContainer sourceFolder, IContainer outputFolder, char[][] inclusionPatterns, char[][] exclusionPatterns, boolean ignoreOptionalProblems) {
	return new ClasspathMultiDirectory(sourceFolder, outputFolder, inclusionPatterns, exclusionPatterns, ignoreOptionalProblems);
}

public static ClasspathLocation forBinaryFolder(IContainer binaryFolder, boolean isOutputFolder, AccessRuleSet accessRuleSet) {
	return new ClasspathDirectory(binaryFolder, isOutputFolder, accessRuleSet);
}

static ClasspathLocation forLibrary(String libraryPathname, long lastModified, AccessRuleSet accessRuleSet, IPath annotationsPath) {
	return new ClasspathJar(libraryPathname, lastModified, accessRuleSet, annotationsPath);
}

static ClasspathLocation forLibrary(String libraryPathname, AccessRuleSet accessRuleSet, IPath annotationsPath) {
	return forLibrary(libraryPathname, 0, accessRuleSet, annotationsPath);
}

static ClasspathLocation forLibrary(IFile library, AccessRuleSet accessRuleSet, IPath annotationsPath) {
	return new ClasspathJar(library, accessRuleSet, annotationsPath);
}

public abstract NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName);

public abstract IPath getProjectRelativePath();

public boolean isOutputFolder() {
	return false;
}

public abstract boolean isPackage(String qualifiedPackageName);

public void cleanup() {
	// free anything which is not required when the state is saved
}
public void reset() {
	// reset any internal caches before another compile loop starts
}

public abstract String debugPathString();

}
