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
package org.eclipse.jdt.internal.core.search.matching;

import java.util.HashMap;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
//import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
//import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.builder.ClasspathJar;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.util.Util;

/*
 * A name environment based on the classpath of a Java project.
 */
public class JavaSearchNameEnvironment implements INameEnvironment, SuffixConstants {

	ClasspathLocation[] locations;

	/*
	 * A map from the fully qualified slash-separated name of the main type (String) to the working copy
	 */
	HashMap workingCopies;

public JavaSearchNameEnvironment(IJavaProject javaProject, org.eclipse.jdt.core.ICompilationUnit[] copies) {
	computeClasspathLocations(javaProject.getProject().getWorkspace().getRoot(), (JavaProject) javaProject);
	try {
		int length = copies == null ? 0 : copies.length;
		this.workingCopies = new HashMap(length);
		if (copies != null) {
			for (int i = 0; i < length; i++) {
				org.eclipse.jdt.core.ICompilationUnit workingCopy = copies[i];
				IPackageDeclaration[] pkgs = workingCopy.getPackageDeclarations();
				String pkg = pkgs.length > 0 ? pkgs[0].getElementName() : ""; //$NON-NLS-1$
				String cuName = workingCopy.getElementName();
				String mainTypeName = Util.getNameWithoutJavaLikeExtension(cuName);
				String qualifiedMainTypeName = pkg.length() == 0 ? mainTypeName : pkg.replace('.', '/') + '/' + mainTypeName;
				this.workingCopies.put(qualifiedMainTypeName, workingCopy);
			}
		}
	} catch (JavaModelException e) {
		// working copy doesn't exist: cannot happen
	}
}

public void cleanup() {
	for (int i = 0, length = this.locations.length; i < length; i++) {
		this.locations[i].cleanup();
	}
}

private void computeClasspathLocations(IWorkspaceRoot workspaceRoot, JavaProject javaProject) {

	IPackageFragmentRoot[] roots = null;
	try {
		roots = javaProject.getAllPackageFragmentRoots();
	} catch (JavaModelException e) {
		// project doesn't exist
		this.locations = new ClasspathLocation[0];
		return;
	}
	int length = roots.length;
	ClasspathLocation[] cpLocations = new ClasspathLocation[length];
	int index = 0;
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	for (int i = 0; i < length; i++) {
		PackageFragmentRoot root = (PackageFragmentRoot) roots[i];
		IPath path = root.getPath();
		try {
			if (root.isArchive()) {
				ZipFile zipFile = manager.getZipFile(path);
				cpLocations[index++] = new ClasspathJar(zipFile, ((ClasspathEntry) root.getRawClasspathEntry()).getAccessRuleSet());
			} else {
				Object target = JavaModel.getTarget(path, true);
				if (target == null) {
					// target doesn't exist any longer
					// just resize cpLocations
					System.arraycopy(cpLocations, 0, cpLocations = new ClasspathLocation[cpLocations.length-1], 0, index);
				} else if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					cpLocations[index++] = new ClasspathSourceDirectory((IContainer)target, root.fullExclusionPatternChars(), root.fullInclusionPatternChars());
				} else {
					cpLocations[index++] = ClasspathLocation.forBinaryFolder((IContainer) target, false, ((ClasspathEntry) root.getRawClasspathEntry()).getAccessRuleSet());
				}
			}
		} catch (CoreException e1) {
			// problem opening zip file or getting root kind
			// consider root corrupt and ignore
			// just resize cpLocations
			System.arraycopy(cpLocations, 0, cpLocations = new ClasspathLocation[cpLocations.length-1], 0, index);
		}
	}
	this.locations = cpLocations;
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName) {
	String
		binaryFileName = null, qBinaryFileName = null,
		sourceFileName = null, qSourceFileName = null,
		qPackageName = null;
	NameEnvironmentAnswer suggestedAnswer = null;
	for (int i = 0, length = this.locations.length; i < length; i++) {
		ClasspathLocation location = this.locations[i];
		NameEnvironmentAnswer answer;
		if (location instanceof ClasspathSourceDirectory) {
			if (sourceFileName == null) {
				qSourceFileName = qualifiedTypeName; // doesn't include the file extension
				sourceFileName = qSourceFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qSourceFileName.length() - typeName.length;
					qPackageName =  qSourceFileName.substring(0, typeNameStart - 1);
					sourceFileName = qSourceFileName.substring(typeNameStart);
				}
			}
			ICompilationUnit workingCopy = (ICompilationUnit) this.workingCopies.get(qualifiedTypeName);
			if (workingCopy != null) {
				answer = new NameEnvironmentAnswer(workingCopy, null /*no access restriction*/);
			} else {
				answer = location.findClass(
					sourceFileName, // doesn't include the file extension
					qPackageName,
					qSourceFileName);  // doesn't include the file extension
			}
		} else {
			if (binaryFileName == null) {
				qBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
				binaryFileName = qBinaryFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qBinaryFileName.length() - typeName.length - 6; // size of ".class"
					qPackageName =  qBinaryFileName.substring(0, typeNameStart - 1);
					binaryFileName = qBinaryFileName.substring(typeNameStart);
				}
			}
			answer =
				location.findClass(
					binaryFileName,
					qPackageName,
					qBinaryFileName);
		}
		if (answer != null) {
			if (!answer.ignoreIfBetter()) {
				if (answer.isBetter(suggestedAnswer))
					return answer;
			} else if (answer.isBetter(suggestedAnswer))
				// remember suggestion and keep looking
				suggestedAnswer = answer;
		}
	}
	if (suggestedAnswer != null)
		// no better answer was found
		return suggestedAnswer;
	return null;
}

public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName);
	return null;
}

public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1]);
	return null;
}

public boolean isPackage(char[][] compoundName, char[] packageName) {
	return isPackage(new String(CharOperation.concatWith(compoundName, packageName, '/')));
}

public boolean isPackage(String qualifiedPackageName) {
	for (int i = 0, length = this.locations.length; i < length; i++)
		if (this.locations[i].isPackage(qualifiedPackageName))
			return true;
	return false;
}

}
