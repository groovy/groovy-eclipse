/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdateKind;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.util.Util;

public abstract class ClasspathLocation {

	protected boolean isOnModulePath;
	protected IModule module;
	protected IUpdatableModule.UpdatesByKind updates;
	protected Set<String> limitModuleNames = null;
	protected String patchModuleName = null;
	// In the following signatures, passing a null moduleName signals "don't care":
	abstract public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName);
	abstract public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName,
													boolean asBinaryOnly, Predicate<String> moduleNameFilter);
	abstract public boolean isPackage(String qualifiedPackageName, String moduleName);
	public char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		return singletonModuleNameIf(isPackage(qualifiedPackageName, moduleName));
	}
	public boolean hasModule() { return getModule() != null; }
	abstract public boolean hasCompilationUnit(String pkgName, String moduleName);

	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName,
											boolean asBinaryOnly, Predicate<String> moduleNameFilter) {
		String fileName = new String(typeName);
		return findClass(fileName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, asBinaryOnly, moduleNameFilter);
	}
	public void setModule (IModule mod) {
		this.module = mod;
	}
	public IModule getModule() {
		return this.module;
	}
	protected boolean areAllModuleOptionsEqual(ClasspathLocation other) {
		if (this.patchModuleName != null) {
			if (other.patchModuleName == null)
				return false;
			if (!this.patchModuleName.equals(other.patchModuleName))
				return false;
		} else {
			if (other.patchModuleName != null)
				return false;
		}
		if (this.limitModuleNames != null) {
			if (other.limitModuleNames == null)
				return false;
			if (other.limitModuleNames.size() != this.limitModuleNames.size())
				return false;
			if (!this.limitModuleNames.containsAll(other.limitModuleNames))
				return false;
		} else {
			if (other.limitModuleNames != null)
				return false;
		}
		if (this.updates != null) {
			if (other.updates == null)
				return false;
			List<Consumer<IUpdatableModule>> packageUpdates = this.updates.getList(UpdateKind.PACKAGE, false);
			List<Consumer<IUpdatableModule>> otherPackageUpdates = other.updates.getList(UpdateKind.PACKAGE, false);
			if (packageUpdates != null) {
				if (otherPackageUpdates == null)
					return false;
				if (packageUpdates.size() != otherPackageUpdates.size())
					return false;
				if (!packageUpdates.containsAll(otherPackageUpdates))
					return false;
			} else {
				if (otherPackageUpdates != null)
					return false;
			}
			List<Consumer<IUpdatableModule>> moduleUpdates = this.updates.getList(UpdateKind.MODULE, false);
			List<Consumer<IUpdatableModule>> otherModuleUpdates = other.updates.getList(UpdateKind.MODULE, false);
			if (moduleUpdates != null) {
				if (otherModuleUpdates == null)
					return false;
				if (moduleUpdates.size() != otherModuleUpdates.size())
					return false;
				if (!moduleUpdates.containsAll(otherModuleUpdates))
					return false;
			} else {
				if (otherModuleUpdates != null)
					return false;
			}
		} else {
			if (other.updates != null)
				return false;
		}
		return true;
	}
	static ClasspathLocation forSourceFolder(IContainer sourceFolder, IContainer outputFolder,
			char[][] inclusionPatterns, char[][] exclusionPatterns, boolean ignoreOptionalProblems) {
		return new ClasspathMultiDirectory(sourceFolder, outputFolder, inclusionPatterns, exclusionPatterns,
				ignoreOptionalProblems);
	}
public static ClasspathLocation forBinaryFolder(IContainer binaryFolder, boolean isOutputFolder, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, boolean autoModule) {
	return new ClasspathDirectory(binaryFolder, isOutputFolder, accessRuleSet, externalAnnotationPath, autoModule);
}

static ClasspathLocation forLibrary(String libraryPathname, 
										long lastModified, 
										AccessRuleSet accessRuleSet, 
										IPath annotationsPath,
										boolean autoModule,
										String compliance) {
	return Util.archiveFormat(libraryPathname) == Util.JMOD_FILE ?
					new ClasspathJMod(libraryPathname, lastModified, accessRuleSet, annotationsPath) :
						(compliance == null || (CompilerOptions.versionToJdkLevel(compliance) < ClassFileConstants.JDK9) ?
			new ClasspathJar(libraryPathname, lastModified, accessRuleSet, annotationsPath, autoModule) :
				new ClasspathMultiReleaseJar(libraryPathname, lastModified, accessRuleSet, annotationsPath, autoModule, compliance));

}
public static ClasspathJrt forJrtSystem(String jrtPath, AccessRuleSet accessRuleSet, IPath annotationsPath, String release) throws CoreException {
	return (release == null || release.equals("")) ? new ClasspathJrt(jrtPath, accessRuleSet, annotationsPath) : //$NON-NLS-1$
						new ClasspathJrtWithReleaseOption(jrtPath, accessRuleSet, annotationsPath, release);
}

public static ClasspathLocation forLibrary(String libraryPathname, AccessRuleSet accessRuleSet, IPath annotationsPath,
											boolean autoModule, String compliance) {
	return forLibrary(libraryPathname, 0, accessRuleSet, annotationsPath, autoModule, compliance);
}

static ClasspathLocation forLibrary(IFile library, AccessRuleSet accessRuleSet, IPath annotationsPath,
										boolean autoModule, String compliance) {
	return (CompilerOptions.versionToJdkLevel(compliance) < ClassFileConstants.JDK9) ?
			new ClasspathJar(library, accessRuleSet, annotationsPath, autoModule) :
				new ClasspathMultiReleaseJar(library, accessRuleSet, annotationsPath, autoModule, compliance);
}
public static ClasspathLocation forLibrary(ZipFile zipFile, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, boolean isOnModulePath, String compliance) {
	return (CompilerOptions.versionToJdkLevel(compliance) < ClassFileConstants.JDK9) ?
			new ClasspathJar(zipFile, accessRuleSet, externalAnnotationPath, isOnModulePath) :
				new ClasspathMultiReleaseJar(zipFile, accessRuleSet, externalAnnotationPath, isOnModulePath, compliance);
}

public abstract IPath getProjectRelativePath();

public boolean isOutputFolder() {
	return false;
}

public void cleanup() {
	// free anything which is not required when the state is saved
}
public void reset() {
	// reset any internal caches before another compile loop starts
}

public abstract String debugPathString();

public char[][] singletonModuleNameIf(boolean condition) {
	if (!condition)
		return null;
	if (this.module != null)
		return new char[][] { this.module.name() };
	return new char[][] { ModuleBinding.UNNAMED };
}
}
