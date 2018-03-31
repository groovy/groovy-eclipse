/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.search.matching;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.builder.ClasspathJar;
import org.eclipse.jdt.internal.core.builder.ClasspathJrt;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.util.Util;

/*
 * A name environment based on the classpath of a Java project.
 */
public class JavaSearchNameEnvironment implements IModuleAwareNameEnvironment, SuffixConstants {

	LinkedHashSet<ClasspathLocation> locationSet;
	Map<String, IModuleDescription> modules;
	private boolean modulesComputed = false;
	Map<String,ClasspathLocation> moduleLocations;
	Map<String,LinkedHashSet<ClasspathLocation>> moduleToClassPathLocations;

	/*
	 * A map from the fully qualified slash-separated name of the main type (String) to the working copy
	 */
	Map<String, org.eclipse.jdt.core.ICompilationUnit> workingCopies;

public JavaSearchNameEnvironment(IJavaProject javaProject, org.eclipse.jdt.core.ICompilationUnit[] copies) {
	if (CompilerOptions.versionToJdkLevel(javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)) >= ClassFileConstants.JDK9) {
		this.moduleLocations = new HashMap<>();
		this.moduleToClassPathLocations = new HashMap<>();
	}
	this.modules = new HashMap<>();
	this.locationSet = computeClasspathLocations((JavaProject) javaProject);
	this.workingCopies = getWorkingCopyMap(copies);
}

public static Map<String, org.eclipse.jdt.core.ICompilationUnit> getWorkingCopyMap(
		org.eclipse.jdt.core.ICompilationUnit[] copies) {
	int length = copies == null ? 0 : copies.length;
	HashMap<String, org.eclipse.jdt.core.ICompilationUnit> result = new HashMap<>(length);
	try {
		if (copies != null) {
			for (int i = 0; i < length; i++) {
				org.eclipse.jdt.core.ICompilationUnit workingCopy = copies[i];
				IPackageDeclaration[] pkgs = workingCopy.getPackageDeclarations();
				String pkg = pkgs.length > 0 ? pkgs[0].getElementName() : ""; //$NON-NLS-1$
				String cuName = workingCopy.getElementName();
				String mainTypeName = Util.getNameWithoutJavaLikeExtension(cuName);
				String qualifiedMainTypeName = pkg.length() == 0 ? mainTypeName : pkg.replace('.', '/') + '/' + mainTypeName;
				result.put(qualifiedMainTypeName, workingCopy);
				// TODO : JAVA 9 - module-info.java has the same name across modules - Any issues here?
			}
		}
	} catch (JavaModelException e) {
		// working copy doesn't exist: cannot happen
	}
	return result;
}

public void cleanup() {
	this.locationSet.clear();
}

void addProjectClassPath(JavaProject javaProject) {
	LinkedHashSet<ClasspathLocation> locations = computeClasspathLocations(javaProject);
	if (locations != null) this.locationSet.addAll(locations);
}

private LinkedHashSet<ClasspathLocation> computeClasspathLocations(JavaProject javaProject) {

	IPackageFragmentRoot[] roots = null;
	try {
		roots = javaProject.getAllPackageFragmentRoots();
	} catch (JavaModelException e) {
		return null;// project doesn't exist
	}
	IModuleDescription imd = null;
	try {
		imd = javaProject.getModuleDescription();
	} catch (JavaModelException e) {
		// e.printStackTrace(); // ignore
	}

	LinkedHashSet<ClasspathLocation> locations = new LinkedHashSet<ClasspathLocation>();
	int length = roots.length;
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	for (int i = 0; i < length; i++) {
		ClasspathLocation cp = mapToClassPathLocation(manager, (PackageFragmentRoot) roots[i], imd);
		if (cp != null) locations.add(cp);
	}
	return locations;
}

private void computeModules() {
	if (!this.modulesComputed) {
		this.modulesComputed = true;
		JavaElementRequestor requestor = new JavaElementRequestor();
		try {
			JavaModelManager.getModulePathManager().seekModule(CharOperation.ALL_PREFIX, true, requestor);
			IModuleDescription[] mods = requestor.getModules();
			for (IModuleDescription mod : mods) {
				this.modules.putIfAbsent(mod.getElementName(), mod);
			}
		} catch (JavaModelException e) {
			// do nothing
		}
	}
}

private ClasspathLocation mapToClassPathLocation(JavaModelManager manager, PackageFragmentRoot root, IModuleDescription defaultModule) {
	ClasspathLocation cp = null;
	IPath path = root.getPath();
	try {
		if (root.isArchive()) {
			ClasspathEntry rawClasspathEntry = (ClasspathEntry) root.getRawClasspathEntry();
			cp = JavaModelManager.isJrt(path) ? 
					new ClasspathJrt(path.toOSString(), rawClasspathEntry.getAccessRuleSet(), 
							ClasspathEntry.getExternalAnnotationPath(rawClasspathEntry, ((IJavaProject)root.getParent()).getProject(), true)) :
						new ClasspathJar(manager.getZipFile(path), rawClasspathEntry.getAccessRuleSet(),
								ClasspathEntry.getExternalAnnotationPath(rawClasspathEntry,
										((IJavaProject) root.getParent()).getProject(), true),
								rawClasspathEntry.isModular());
		} else {
			Object target = JavaModel.getTarget(path, true);
			if (target != null) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					cp = new ClasspathSourceDirectory((IContainer)target, root.fullExclusionPatternChars(), root.fullInclusionPatternChars());
				} else {
					ClasspathEntry rawClasspathEntry = (ClasspathEntry) root.getRawClasspathEntry();
					cp = ClasspathLocation.forBinaryFolder((IContainer) target, false, rawClasspathEntry.getAccessRuleSet(),
														ClasspathEntry.getExternalAnnotationPath(rawClasspathEntry, ((IJavaProject)root.getParent()).getProject(), true),
														rawClasspathEntry.isModular());
				}
			}
		}
	} catch (CoreException e1) {
		// problem opening zip file or getting root kind
		// consider root corrupt and ignore
	}
	IModuleDescription imd = root.getModuleDescription();
	if (imd != null) {
		String moduleName = addModuleClassPathInfo(cp, imd);
		if (moduleName != null)
			this.modules.put(moduleName, imd);
		if (this.moduleLocations != null)
			this.moduleLocations.put(moduleName, cp);
	} else if (defaultModule != null) {
		addModuleClassPathInfo(cp, defaultModule);
	}
	return cp;
}
private String addModuleClassPathInfo(ClasspathLocation cp, IModuleDescription imd) {
	IModule mod = NameLookup.getModuleDescriptionInfo(imd);
	String moduleName = null;
	if (mod != null) {
		char[] name = mod.name();
		if (name != null) {
			moduleName = new String(name);
			cp.setModule(mod);
			addClassPathToModule(moduleName, cp);
		}
	}
	return moduleName;
}
private void addClassPathToModule(String moduleName, ClasspathLocation cp) {
	if (this.moduleToClassPathLocations != null) {
		LinkedHashSet<ClasspathLocation> l = this.moduleToClassPathLocations.get(moduleName);
		if (l == null) {
			l = new LinkedHashSet<>();
			this.moduleToClassPathLocations.put(moduleName, l);
		}
		l.add(cp);
	}
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, LookupStrategy strategy, /*@Nullable*/String moduleName) {
	String
		binaryFileName = null, qBinaryFileName = null,
		sourceFileName = null, qSourceFileName = null,
		qPackageName = null;
	NameEnvironmentAnswer suggestedAnswer = null;
	Iterator<ClasspathLocation> iter = getLocationsFor(moduleName);
	while (iter.hasNext()) {
		ClasspathLocation location = iter.next();
		if (!strategy.matches(location, ClasspathLocation::hasModule))
			continue;
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
					moduleName,
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
					moduleName,
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

private Iterator<ClasspathLocation> getLocationsFor(/*@Nullable*/String moduleName) {
	if (moduleName != null) {
		LinkedHashSet<ClasspathLocation> l = this.moduleToClassPathLocations.get(moduleName);
		if (l != null && l.size() > 0)
			return l.iterator();
	}
	return this.locationSet.iterator();
}

@Override
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName,
			LookupStrategy.get(moduleName),
			LookupStrategy.getStringName(moduleName));
	return null;
}

@Override
public NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1],
			LookupStrategy.get(moduleName),
			LookupStrategy.getStringName(moduleName));
	return null;
}

@Override
public char[][] getModulesDeclaringPackage(char[][] parentPackageName, char[] packageName, char[] moduleName) {
	String qualifiedPackageName = String.valueOf(CharOperation.concatWith(parentPackageName, packageName, '/'));
	LookupStrategy strategy = LookupStrategy.get(moduleName);
	if (strategy == LookupStrategy.Named) {
		if (this.moduleToClassPathLocations != null) {
			String moduleNameString = String.valueOf(moduleName);
			LinkedHashSet<ClasspathLocation> cpl = this.moduleToClassPathLocations.get(moduleNameString);
			List<ClasspathLocation> l = cpl != null ? cpl.stream().collect(Collectors.toList()): null;
			if (l != null) {
				for (ClasspathLocation cp : l) {
					if (cp.isPackage(qualifiedPackageName, moduleNameString))
						return new char[][] { moduleName };
				}
			}
		}
		return null;
	}
	char[][] moduleNames = CharOperation.NO_CHAR_CHAR;
	for (ClasspathLocation location : this.locationSet) {
		if (strategy.matches(location, ClasspathLocation::hasModule) ) {
			if (location.isPackage(qualifiedPackageName, null)) {
				char[][] mNames = location.getModulesDeclaringPackage(qualifiedPackageName, null);
				if (mNames == null || mNames.length == 0) continue;
				moduleNames = CharOperation.arrayConcat(moduleNames, mNames);
			}
		}
	}
	return moduleNames == CharOperation.NO_CHAR_CHAR ? null : moduleNames;
}

@Override
public boolean hasCompilationUnit(char[][] qualifiedPackageName, char[] moduleName, boolean checkCUs) {
	String qualifiedPackageNameString = String.valueOf(CharOperation.concatWith(qualifiedPackageName, '/'));
	LookupStrategy strategy = LookupStrategy.get(moduleName);
	String moduleNameString = LookupStrategy.getStringName(moduleName);
	if (strategy == LookupStrategy.Named) {
		if (this.moduleLocations != null) {
			ClasspathLocation location = this.moduleLocations.get(moduleNameString);
			if (location != null)
				return location.hasCompilationUnit(qualifiedPackageNameString, moduleNameString);
		}
	} else {
		for (ClasspathLocation location : this.locationSet) {
			if (strategy.matches(location, ClasspathLocation::hasModule) )
				if (location.hasCompilationUnit(qualifiedPackageNameString, moduleNameString))
					return true;
		}
	}
	return false;
}

@Override
public IModule getModule(char[] moduleName) {
	computeModules();
	IModuleDescription moduleDesc = this.modules.get(new String(moduleName));
	IModule module = null;
	try {
		if (moduleDesc != null)
			module =  ((ModuleDescriptionInfo)((JavaElement) moduleDesc).getElementInfo());
	} catch (JavaModelException e) {
		// do nothing
	}
	return module;
}

@Override
public char[][] getAllAutomaticModules() {
	if (this.moduleLocations == null || this.moduleLocations.size() == 0)
		return CharOperation.NO_CHAR_CHAR;
	Set<char[]> set = this.moduleLocations.values().stream().map(e -> e.getModule()).filter(m -> m != null && m.isAutomatic())
			.map(m -> m.name()).collect(Collectors.toSet());
	return set.toArray(new char[set.size()][]);
}
}
