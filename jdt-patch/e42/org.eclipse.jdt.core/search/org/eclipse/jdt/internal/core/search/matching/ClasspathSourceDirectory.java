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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.util.ResourceCompilationUnit;
import org.eclipse.jdt.internal.core.util.Util;

public class ClasspathSourceDirectory extends ClasspathLocation {

	IContainer sourceFolder;
	SimpleLookupTable directoryCache;
	SimpleLookupTable missingPackageHolder = new SimpleLookupTable();
	char[][] fullExclusionPatternChars;
	char[][] fulInclusionPatternChars;

ClasspathSourceDirectory(IContainer sourceFolder, char[][] fullExclusionPatternChars, char[][] fulInclusionPatternChars) {
	this.sourceFolder = sourceFolder;
	this.directoryCache = new SimpleLookupTable(5);
	this.fullExclusionPatternChars = fullExclusionPatternChars;
	this.fulInclusionPatternChars = fulInclusionPatternChars;
}

public void cleanup() {
	this.directoryCache = null;
}

SimpleLookupTable directoryTable(String qualifiedPackageName) {
	SimpleLookupTable dirTable = (SimpleLookupTable) this.directoryCache.get(qualifiedPackageName);
	if (dirTable == this.missingPackageHolder) return null; // package exists in another classpath directory or jar
	if (dirTable != null) return dirTable;

	try {
		IResource container = this.sourceFolder.findMember(qualifiedPackageName); // this is a case-sensitive check
		if (container instanceof IContainer) {
			IResource[] members = ((IContainer) container).members();
			dirTable = new SimpleLookupTable();
			for (int i = 0, l = members.length; i < l; i++) {
				IResource m = members[i];
				String name;
				if (m.getType() == IResource.FILE) {
					int index = Util.indexOfJavaLikeExtension(name = m.getName());
					if (index >= 0) {
						String fullPath = m.getFullPath().toString();
						if (!org.eclipse.jdt.internal.compiler.util.Util.isExcluded(fullPath.toCharArray(), this.fulInclusionPatternChars, this.fullExclusionPatternChars, false/*not a folder path*/)) {
							dirTable.put(name.substring(0, index), m);
						}
					}
				}
			}
			this.directoryCache.put(qualifiedPackageName, dirTable);
			return dirTable;
		}
	} catch(CoreException ignored) {
		// treat as if missing
	}
	this.directoryCache.put(qualifiedPackageName, this.missingPackageHolder);
	return null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathSourceDirectory)) return false;

	return this.sourceFolder.equals(((ClasspathSourceDirectory) o).sourceFolder);
}

public NameEnvironmentAnswer findClass(String sourceFileWithoutExtension, String qualifiedPackageName, String qualifiedSourceFileWithoutExtension) {
	SimpleLookupTable dirTable = directoryTable(qualifiedPackageName);
	if (dirTable != null && dirTable.elementSize > 0) {
		IFile file = (IFile) dirTable.get(sourceFileWithoutExtension);
		if (file != null) {
			return new NameEnvironmentAnswer(new ResourceCompilationUnit(file, file.getLocationURI()), null /* no access restriction */);
		}
	}
	return null;
}

public IPath getProjectRelativePath() {
	return this.sourceFolder.getProjectRelativePath();
}

public int hashCode() {
	return this.sourceFolder == null ? super.hashCode() : this.sourceFolder.hashCode();
}

public boolean isPackage(String qualifiedPackageName) {
	return directoryTable(qualifiedPackageName) != null;
}

public void reset() {
	this.directoryCache = new SimpleLookupTable(5);
}

public String toString() {
	return "Source classpath directory " + this.sourceFolder.getFullPath().toString(); //$NON-NLS-1$
}

public String debugPathString() {
	return this.sourceFolder.getFullPath().toString();
}

}
