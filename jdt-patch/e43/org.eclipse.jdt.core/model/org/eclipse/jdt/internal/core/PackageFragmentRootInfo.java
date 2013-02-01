/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;
// GROOVY PATCHED

import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * The element info for <code>PackageFragmentRoot</code>s.
 */
class PackageFragmentRootInfo extends OpenableElementInfo {

	/**
	 * The SourceMapper for this JAR (or <code>null</code> if
	 * this JAR does not have source attached).
	 */
	protected SourceMapper sourceMapper = null;

	/**
	 * The kind of the root associated with this info.
	 * Valid kinds are: <ul>
	 * <li><code>IPackageFragmentRoot.K_SOURCE</code>
	 * <li><code>IPackageFragmentRoot.K_BINARY</code></ul>
	 */
	protected int rootKind= IPackageFragmentRoot.K_SOURCE;

	/**
	 * A array with all the non-java resources contained by this PackageFragment
	 */
	protected Object[] nonJavaResources;

	private boolean ignoreOptionalProblems;
	private boolean initialized;
/**
 * Create and initialize a new instance of the receiver
 */
public PackageFragmentRootInfo() {
	this.nonJavaResources = null;
	this.initialized = false;
}
/**
 * Starting at this folder, create non-java resources for this package fragment root
 * and add them to the non-java resources collection.
 *
 * @exception JavaModelException  The resource associated with this package fragment does not exist
 */
static Object[] computeFolderNonJavaResources(IPackageFragmentRoot root, IContainer folder, char[][] inclusionPatterns, char[][] exclusionPatterns) throws JavaModelException {
	IResource[] nonJavaResources = new IResource[5];
	int nonJavaResourcesCounter = 0;
	JavaProject project = (JavaProject) root.getJavaProject();
	try {
	    // GROOVY start
		// here, we only care about non-source package roots in Groovy projects
		boolean isInterestingPackageRoot = LanguageSupportFactory.isInterestingProject(project.getProject()) && root.getRawClasspathEntry().getEntryKind() != IClasspathEntry.CPE_SOURCE;
		// GROOVY end
		IClasspathEntry[] classpath = project.getResolvedClasspath();
		IResource[] members = folder.members();
		int length = members.length;
		if (length > 0) {
			String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
			String complianceLevel = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			nextResource: for (int i = 0; i < length; i++) {
				IResource member = members[i];
				switch (member.getType()) {
					case IResource.FILE :
						String fileName = member.getName();

						// ignore .java files that are not excluded
					    // GROOVY start
						/* old {
						 if (Util.isValidCompilationUnitName(fileName, sourceLevel, complianceLevel) && !Util.isExcluded(member, inclusionPatterns, exclusionPatterns))
						} new */
						if ((Util.isValidCompilationUnitName(fileName, sourceLevel, complianceLevel) && !Util.isExcluded(member, inclusionPatterns, exclusionPatterns)) &&
								// we want to show groovy scripts that are coming from class folders
								!(isInterestingPackageRoot && LanguageSupportFactory.isInterestingSourceFile(fileName)))
						// GROOVY end
							continue nextResource;
						// ignore .class files
						if (Util.isValidClassFileName(fileName, sourceLevel, complianceLevel))
							continue nextResource;
						// ignore .zip or .jar file on classpath
						if (isClasspathEntry(member.getFullPath(), classpath))
							continue nextResource;
						break;

					case IResource.FOLDER :
						// ignore valid packages or excluded folders that correspond to a nested pkg fragment root
						if (Util.isValidFolderNameForPackage(member.getName(), sourceLevel, complianceLevel)
								&& (!Util.isExcluded(member, inclusionPatterns, exclusionPatterns)
										|| isClasspathEntry(member.getFullPath(), classpath)))
							continue nextResource;
						break;
				}
				if (nonJavaResources.length == nonJavaResourcesCounter) {
					// resize
					System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter * 2]), 0, nonJavaResourcesCounter);
				}
				nonJavaResources[nonJavaResourcesCounter++] = member;
			}
		}
		if (ExternalFoldersManager.isInternalPathForExternalFolder(folder.getFullPath())) {
			IJarEntryResource[] jarEntryResources = new IJarEntryResource[nonJavaResourcesCounter];
			for (int i = 0; i < nonJavaResourcesCounter; i++) {
				jarEntryResources[i] = new NonJavaResource(root, nonJavaResources[i]);
			}
			return jarEntryResources;
		} else if (nonJavaResources.length != nonJavaResourcesCounter) {
			System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter]), 0, nonJavaResourcesCounter);
		}
		return nonJavaResources;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Compute the non-package resources of this package fragment root.
 */
private Object[] computeNonJavaResources(IResource underlyingResource, PackageFragmentRoot handle) {
	Object[] resources = NO_NON_JAVA_RESOURCES;
	try {
		// the underlying resource may be a folder or a project (in the case that the project folder
		// is actually the package fragment root)
		if (underlyingResource.getType() == IResource.FOLDER || underlyingResource.getType() == IResource.PROJECT) {
			resources =
				computeFolderNonJavaResources(
					handle,
					(IContainer) underlyingResource,
					handle.fullInclusionPatternChars(),
					handle.fullExclusionPatternChars());
		}
	} catch (JavaModelException e) {
		// ignore
	}
	return resources;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
synchronized Object[] getNonJavaResources(IJavaProject project, IResource underlyingResource, PackageFragmentRoot handle) {
	Object[] resources = this.nonJavaResources;
	if (resources == null) {
		resources = computeNonJavaResources(underlyingResource, handle);
		this.nonJavaResources = resources;
	}
	return resources;
}
/**
 * Returns the kind of this root.
 */
public int getRootKind() {
	return this.rootKind;
}
/**
 * Retuns the SourceMapper for this root, or <code>null</code>
 * if this root does not have attached source.
 */
protected SourceMapper getSourceMapper() {
	return this.sourceMapper;
}
boolean ignoreOptionalProblems(PackageFragmentRoot packageFragmentRoot) throws JavaModelException {
	if (this.initialized == false) {
		this.ignoreOptionalProblems = ((ClasspathEntry) packageFragmentRoot.getRawClasspathEntry()).ignoreOptionalProblems();
		this.initialized = true;
	}
	return this.ignoreOptionalProblems;
}
private static boolean isClasspathEntry(IPath path, IClasspathEntry[] resolvedClasspath) {
	for (int i = 0, length = resolvedClasspath.length; i < length; i++) {
		IClasspathEntry entry = resolvedClasspath[i];
		if (entry.getPath().equals(path)) {
			return true;
		}
	}
	return false;
}
/**
 * Set the fNonJavaResources to res value
 */
void setNonJavaResources(Object[] resources) {
	this.nonJavaResources = resources;
}
/**
 * Sets the kind of this root.
 */
protected void setRootKind(int newRootKind) {
	this.rootKind = newRootKind;
}
/**
 * Sets the SourceMapper for this root.
 */
protected void setSourceMapper(SourceMapper mapper) {
	this.sourceMapper= mapper;
}
}
