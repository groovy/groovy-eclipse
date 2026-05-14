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
package org.eclipse.jdt.internal.core.search.matching;

import static java.util.stream.Collectors.joining;
import static org.eclipse.jdt.internal.core.JavaModelManager.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.JrtPackageFragmentRoot;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.Util;

/*
 * A name environment based on the classpath of a Java project.
 */
public class JavaSearchNameEnvironment implements IModuleAwareNameEnvironment, SuffixConstants {

	protected /* visible for testing only */ LinkedHashSet<ClasspathLocation> locationSet;
	Map<String, IModuleDescription> modules;
	private boolean modulesComputed = false;
	Map<String,ClasspathLocation> moduleLocations;
	Map<String,LinkedHashSet<ClasspathLocation>> moduleToClassPathLocations;
	/** an index of qualified package names (separated by / not .) to classpath locations */
	protected /* visible for testing only */ Map<String,LinkedHashSet<ClasspathLocation>> packageNameToClassPathLocations;

	/*
	 * A map from the fully qualified slash-separated name of the main type (String) to the working copy
	 */
	Map<String, org.eclipse.jdt.core.ICompilationUnit> workingCopies;

public JavaSearchNameEnvironment(IJavaProject javaProject, org.eclipse.jdt.core.ICompilationUnit[] copies) {
	if (isComplianceJava9OrHigher(javaProject)) {
		this.moduleLocations = new HashMap<>();
		this.moduleToClassPathLocations = new HashMap<>();
	}
	this.modules = new HashMap<>();
	this.packageNameToClassPathLocations = new HashMap<>();

	long start = 0;
	if (NameLookup.VERBOSE) {
		trace(" BUILDING JavaSearchNameEnvironment");  //$NON-NLS-1$
		trace(" -> project: " + javaProject);  //$NON-NLS-1$
		trace(" -> working copy size: " + (copies == null ? 0 : copies.length));  //$NON-NLS-1$
		start = System.currentTimeMillis();
	}

	this.locationSet = computeClasspathLocations((JavaProject) javaProject);
	this.workingCopies = getWorkingCopyMap(copies);

	// if there are working copies, we need to index their packages too
	if(this.workingCopies.size() > 0) {
		Optional<ClasspathLocation> firstSrcLocation = this.locationSet.stream().filter(ClasspathSourceDirectory.class::isInstance).findFirst();
		if(!firstSrcLocation.isPresent()) {
			/*
			 * Specifying working copies but not providing a project with a source folder is not supported by the current implementation.
			 * I'm not sure if this is valid use case, though.
			 *
			 * However, there is one test that (potentially) relies on this behavior. At lease it expects this constructor to NOT fail.
			 *
			 * org.eclipse.jdt.core.tests.model.ClassFileTests.testWorkingCopy11()
			 */
			//throw new IllegalArgumentException("Missing source folder for searching working copies: " + javaProject); //$NON-NLS-1$
		    if (NameLookup.VERBOSE) {
				trace(" -> ignoring working copies; no ClasspathSourceDirectory on project classpath ");  //$NON-NLS-1$
		    }
		} else {
			for (String qualifiedMainTypeName : this.workingCopies.keySet()) {
				int typeNameStart = qualifiedMainTypeName.lastIndexOf('/');
				if(typeNameStart > 0) {
					String pkgName = qualifiedMainTypeName.substring(0, typeNameStart);
					addPackageNameToIndex(firstSrcLocation.get(), pkgName);
				} else {
					addPackageNameToIndex(firstSrcLocation.get(), IPackageFragment.DEFAULT_PACKAGE_NAME);
				}
			}
		}
	}


    if (NameLookup.VERBOSE) {
		trace(" -> pkg roots size: " + (this.locationSet == null ? 0 : this.locationSet.size()));  //$NON-NLS-1$
		trace(" -> pkgs size: " + this.packageNameToClassPathLocations.size());  //$NON-NLS-1$
        trace(" -> spent: " + (System.currentTimeMillis() - start) + "ms");  //$NON-NLS-1$ //$NON-NLS-2$
    }
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

@Override
public void cleanup() {
	this.locationSet.clear();
	this.packageNameToClassPathLocations.clear();
}

protected /* visible for testing only */ void addProjectClassPath(JavaProject javaProject) {
	addProjectClassPath(javaProject, false);
}

void addProjectClassPath(JavaProject javaProject, boolean onlyExported) {
	long start = 0;
	if (NameLookup.VERBOSE) {
		trace(" EXTENDING JavaSearchNameEnvironment");  //$NON-NLS-1$
		trace(" -> project: " + javaProject);  //$NON-NLS-1$
		start = System.currentTimeMillis();
	}

	LinkedHashSet<ClasspathLocation> locations = computeClasspathLocations(javaProject, onlyExported);
	if (locations != null) this.locationSet.addAll(locations);

    if (NameLookup.VERBOSE) {
		trace(" -> pkg roots size: " + (this.locationSet == null ? 0 : this.locationSet.size()));  //$NON-NLS-1$
		trace(" -> pkgs size: " + this.packageNameToClassPathLocations.size());  //$NON-NLS-1$
        trace(" -> spent: " + (System.currentTimeMillis() - start) + "ms");  //$NON-NLS-1$ //$NON-NLS-2$
    }
}

private LinkedHashSet<ClasspathLocation> computeClasspathLocations(JavaProject javaProject) {
	return computeClasspathLocations(javaProject, false);
}

private LinkedHashSet<ClasspathLocation> computeClasspathLocations(JavaProject javaProject, boolean onlyExported) {

	IPackageFragmentRoot[] roots = null;
	try {
		roots = javaProject.getAllPackageFragmentRoots();
	} catch (JavaModelException e) {
		return null;// project doesn't exist
	}
	IModuleDescription projectModule = null;
	try {
		projectModule = javaProject.getModuleDescription();
	} catch (JavaModelException e) {
		if (JobManager.VERBOSE) {
			trace("", e); //$NON-NLS-1$
		}
	}

	LinkedHashSet<ClasspathLocation> locations = new LinkedHashSet<>();
	int length = roots.length;
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	for (int i = 0; i < length; i++) {
		PackageFragmentRoot root = (PackageFragmentRoot) roots[i];
		if (onlyExported && !isSourceOrExported(root)) {
			continue;
		}
		ClasspathLocation cp = mapToClassPathLocation(manager, root, projectModule);
		if (cp != null) {
			try {
				indexPackageNames(cp, roots[i]);
				locations.add(cp);
			} catch (JavaModelException e) {
				Util.log(e, "Error indexing package names!"); //$NON-NLS-1$
			}
		}
	}
	return locations;
}

private void indexPackageNames(ClasspathLocation cp, IPackageFragmentRoot root) throws JavaModelException {
	for (IJavaElement c : root.getChildren()) {
		String qualifiedPackageName = c.getElementName().replace('.', '/');
		addPackageNameToIndex(cp, qualifiedPackageName);
	}
	/* In theory IPackageFragmentRoot#getChildren should contain all. It always returns
	 * the default package (no matter what). However, for some reason only JarPackageFragmentRoot#getChildren
	 * really returns all children. PackageFragmentRoot#getChildren returns ONLY the default package for binary class folders.
	 *
	 * We therefore also go through listPackages as well
	 */
	char[][] packages = cp.listPackages();
	if(packages != null) {
		for (char[] packageName : packages) {
			String qualifiedPackageName = CharOperation.charToString(packageName).replace('.', '/');
			addPackageNameToIndex(cp, qualifiedPackageName);
		}
	}

}

private void addPackageNameToIndex(ClasspathLocation cp, String qualifiedPackageName) {
	LinkedHashSet<ClasspathLocation> cpl = this.packageNameToClassPathLocations.get(qualifiedPackageName);
	if(cpl == null) {
		this.packageNameToClassPathLocations.put(qualifiedPackageName, cpl = new LinkedHashSet<>());
	}
	cpl.add(cp);
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
			IJavaProject project = (IJavaProject) root.getParent();
			String compliance = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			cp = (root instanceof JrtPackageFragmentRoot) ?
					ClasspathLocation.forJrtSystem(path.toOSString(), rawClasspathEntry.getAccessRuleSet(), null, compliance) :
					ClasspathLocation.forLibrary(manager.getZipFile(path), rawClasspathEntry.getAccessRuleSet(), rawClasspathEntry.isModular(), compliance) ;
		} else {
			Object target = JavaModel.getTarget(root, true);
			if (target != null) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					cp = new ClasspathSourceDirectory((IContainer)target, root.fullExclusionPatternChars(), root.fullInclusionPatternChars());
				} else {
					ClasspathEntry rawClasspathEntry = (ClasspathEntry) root.getRawClasspathEntry();
					cp = ClasspathLocation.forBinaryFolder((IContainer) target, false, rawClasspathEntry.getAccessRuleSet(),
														null, rawClasspathEntry.isModular());
				}
			}
		}
	} catch (CoreException e1) {
		// problem opening zip file or getting root kind
		// consider root corrupt and ignore
	}
	JavaProject javaProject = root.getJavaProject();
	if (isComplianceJava9OrHigher(javaProject)) {
		IModuleDescription module = computeModuleFor(root, defaultModule);
		if (module != null) {
			addModuleClassPathInfo(module, cp);
		}
	}
	return cp;
}

private void addModuleClassPathInfo(IModuleDescription module, ClasspathLocation cp) {
	String moduleName = addModuleClassPathInfo(cp, module);
	if (moduleName != null) {
		this.modules.put(moduleName, module);
	}
	if (this.moduleLocations != null) {
		this.moduleLocations.put(moduleName, cp);
	}
}
private String addModuleClassPathInfo(ClasspathLocation cp, IModuleDescription imd) {
	IModule mod = NameLookup.getModuleDescriptionInfo(imd);
	String moduleName = null;
	if (mod != null && cp != null) {
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
		sourceFileName = null, qSourceFileName = null;

	final String qPackageName;
	final int typeNameStart;
	if (qualifiedTypeName.length() > typeName.length) {
		typeNameStart = qualifiedTypeName.length() - typeName.length;
		qPackageName =  qualifiedTypeName.substring(0, typeNameStart - 1);
	} else {
		typeNameStart = 0;
		qPackageName =  ""; //$NON-NLS-1$
	}

	NameEnvironmentAnswer suggestedAnswer = null;
	for (ClasspathLocation location : getLocationsFor(moduleName, qPackageName)) {
		if (!strategy.matches(location, ClasspathLocation::hasModule))
			continue;
		NameEnvironmentAnswer answer;
		if (location instanceof ClasspathSourceDirectory) {
			if (sourceFileName == null) {
				qSourceFileName = qualifiedTypeName; // doesn't include the file extension
				sourceFileName = qSourceFileName;
				if (typeNameStart > 0) {
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
					qSourceFileName,  // doesn't include the file extension
					false,
					null /*no module filtering on source dir*/);
			}
		} else {
			if (binaryFileName == null) {
				qBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
				binaryFileName = qBinaryFileName;
				if (typeNameStart > 0) {
					binaryFileName = qBinaryFileName.substring(typeNameStart);
				}
			}
			answer =
				location.findClass(
					binaryFileName,
					qPackageName,
					moduleName,
					qBinaryFileName,
					false,
					this.moduleLocations != null ? this.moduleLocations::containsKey : null);
		}
		if (answer != null) {
			if (!answer.ignoreIfBetter()) {
				if (answer.isBetter(suggestedAnswer)) {
					if(NameLookup.VERBOSE) {
						trace(" Result for JavaSearchNameEnvironment#findClass( " + qualifiedTypeName + ", " + CharOperation.charToString(typeName) + ", " + strategy + ", " + moduleName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						trace(" -> answer: " + answer); //$NON-NLS-1$
						trace(" -> location: " + location); //$NON-NLS-1$
					}
					return answer;
				}
			} else if (answer.isBetter(suggestedAnswer)) {
				// remember suggestion and keep looking
				suggestedAnswer = answer;
				if(NameLookup.VERBOSE) {
					trace(" Potential answer for JavaSearchNameEnvironment#findClass( " + qualifiedTypeName + ", " + CharOperation.charToString(typeName) + ", " + strategy + ", " + moduleName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					trace(" -> answer: " + answer); //$NON-NLS-1$
					trace(" -> location: " + location); //$NON-NLS-1$
				}
			}
		}
	}
	if (suggestedAnswer != null)
		// no better answer was found
		return suggestedAnswer;
	if(NameLookup.VERBOSE) {
		trace(" NO result for JavaSearchNameEnvironment#findClass( " + qualifiedTypeName + ", " + CharOperation.charToString(typeName) + ", " + strategy + ", " + moduleName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	return null;
}

protected /* visible for testing only */ Iterable<ClasspathLocation> getLocationsFor(/*@Nullable*/String moduleName, String qualifiedPackageName) {
	if (moduleName != null) {
		LinkedHashSet<ClasspathLocation> l = this.moduleToClassPathLocations.get(moduleName);
		if (l != null)
			return l;
		// FIXME: this seems bogus ... if we are searching with a module name and there is NONE, an empty set should be returned, shouldn't it?
	}
	if(qualifiedPackageName != null) {
		LinkedHashSet<ClasspathLocation> cpls = this.packageNameToClassPathLocations.get(qualifiedPackageName);
		if(cpls == null) {
			if(NameLookup.VERBOSE) {
				trace(" No result for JavaSearchNameEnvironment#getLocationsFor( " + moduleName + ", " + qualifiedPackageName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return Collections.emptySet();
		}
		if(NameLookup.VERBOSE) {
			trace(" Result for JavaSearchNameEnvironment#getLocationsFor( " + moduleName + ", " + qualifiedPackageName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			trace(" -> " + cpls.stream().map(Object::toString).collect(joining(" | "))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return cpls;
	}
	if(NameLookup.VERBOSE) {
		trace(" Potentially expensive search in JavaSearchNameEnvironment#getLocationsFor( " + moduleName + ", " + qualifiedPackageName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	return this.locationSet;
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
public char[][] getModulesDeclaringPackage(char[][] packageName, char[] moduleName) {
	String qualifiedPackageName = String.valueOf(CharOperation.concatWith(packageName, '/'));
	LookupStrategy strategy = LookupStrategy.get(moduleName);
	if (strategy == LookupStrategy.Named) {
		if (this.moduleToClassPathLocations != null) {
			String moduleNameString = String.valueOf(moduleName);
			LinkedHashSet<ClasspathLocation> cpl = this.moduleToClassPathLocations.get(moduleNameString);
			if (cpl != null) {
				for (ClasspathLocation cp : cpl) {
					if (cp.isPackage(qualifiedPackageName, moduleNameString))
						return new char[][] { moduleName };
				}
			}
		}
		return null;
	}
	char[][] moduleNames = CharOperation.NO_CHAR_CHAR;
	for (ClasspathLocation location : getLocationsFor(null /* ignore module */, qualifiedPackageName)) {
		if (strategy.matches(location, ClasspathLocation::hasModule) ) {
			if (location.isPackage(qualifiedPackageName, null)) {
				char[][] mNames = location.getModulesDeclaringPackage(qualifiedPackageName, null);
				if (mNames == null || mNames.length == 0) continue;
				moduleNames = CharOperation.arrayConcat(moduleNames, mNames);
			}
		}
	}
	if(NameLookup.VERBOSE) {
		trace(" Result for JavaSearchNameEnvironment#getModulesDeclaringPackage( " + qualifiedPackageName + ", " + CharOperation.charToString(moduleName) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		trace(" -> " + CharOperation.toString(moduleNames)); //$NON-NLS-1$
	}
	return moduleNames == CharOperation.NO_CHAR_CHAR ? null : moduleNames;
}

@Override
public char[][] listPackages(char[] moduleName) { LookupStrategy strategy = LookupStrategy.get(moduleName);
	switch (strategy) {
		case Named:
			if (this.moduleLocations != null) {
				ClasspathLocation location = this.moduleLocations.get(String.valueOf(moduleName));
				if (location == null)
					return CharOperation.NO_CHAR_CHAR;
				return location.listPackages();
			}
			return CharOperation.NO_CHAR_CHAR;
		default:
			throw new UnsupportedOperationException("can list packages only of a named module"); //$NON-NLS-1$
	}
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
		for (ClasspathLocation location : getLocationsFor(null /* ignore module */, qualifiedPackageNameString)) {
			if (strategy.matches(location, ClasspathLocation::hasModule) )
				if (location.hasCompilationUnit(qualifiedPackageNameString, moduleNameString)) {
					if(NameLookup.VERBOSE) {
						trace(" Result for JavaSearchNameEnvironment#hasCompilationUnit( " + qualifiedPackageNameString + ", " + moduleNameString + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						trace(" -> " + location); //$NON-NLS-1$
					}
					return true;
				}
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
			module =  (IModule)((JavaElement) moduleDesc).getElementInfo();
	} catch (JavaModelException e) {
		// do nothing
	}
	return module;
}

@Override
public char[][] getAllAutomaticModules() {
	if (this.moduleLocations == null || this.moduleLocations.size() == 0)
		return CharOperation.NO_CHAR_CHAR;
	Set<char[]> set = this.moduleLocations.values().stream().map(ClasspathLocation::getModule).filter(m -> m != null && m.isAutomatic())
			.map(IModule::name).collect(Collectors.toSet());
	return set.toArray(new char[set.size()][]);
}

public static INameEnvironment createWithReferencedProjects(IJavaProject javaProject, List<IJavaProject> referencedProjects, org.eclipse.jdt.core.ICompilationUnit[] copies) {
	JavaSearchNameEnvironment result = new JavaSearchNameEnvironment(javaProject, copies);

	for (IJavaProject referencedProject : referencedProjects) {
		result.addProjectClassPath((JavaProject)referencedProject, true);
	}
	return result;
}

private static boolean isComplianceJava9OrHigher(IJavaProject javaProject) {
	if (javaProject == null) {
		return false;
	}
	return CompilerOptions.versionToJdkLevel(javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)) >= ClassFileConstants.JDK9;
}

/**
 * Computes matching module for given {@link PackageFragmentRoot}
 * @param defaultModule project module or {@code null}
 * @return may return {@code null}
 */
private static IModuleDescription computeModuleFor(PackageFragmentRoot root, IModuleDescription defaultModule) {
	/*
	 * The JRE 9+ container is on the module path without the 'module' classpath attribute being set.
	 * We detected the JRE container by checking the container for system modules.
	 */
	IModuleDescription rootModule = root.getModuleDescription();
	if (rootModule != null && rootModule.isSystemModule()) {
		return rootModule;
	}

	try {
		IClasspathEntry classpathEntry = root.getRawClasspathEntry();
		if (ClasspathEntry.isModular(classpathEntry)) {
			return rootModule != null? rootModule : defaultModule;
		}
		/*
		 * Source/container classpath entries of a project don't always have the module attribute set,
		 * so we cannot rely on the attribute.
		 */
		int entryKind = classpathEntry.getEntryKind();
		if (entryKind == IClasspathEntry.CPE_SOURCE || entryKind == IClasspathEntry.CPE_CONTAINER) {
			return rootModule != null? rootModule : defaultModule;
		}
	} catch (JavaModelException e) {
		Util.log(e, "Error checking whether PackageFragmentRoot is on module path!"); //$NON-NLS-1$
	}
	return defaultModule;
}

private static boolean isSourceOrExported(PackageFragmentRoot root) {
	boolean isExported = true; // if we run into exceptions, assume exported
	try {
		IClasspathEntry entry = root.getRawClasspathEntry();
		isExported = entry.getEntryKind() == IClasspathEntry.CPE_SOURCE || entry.isExported();
	} catch (JavaModelException e) {
		Util.log(e, "Error checking whether package fragment root is exported!"); //$NON-NLS-1$
	}
	return isExported;
}

static boolean isOnModulePath(PackageFragmentRoot root) {
	boolean isOnModulePath;
	try {
		IJavaProject javaProject = root.getJavaProject();
		IModuleDescription defaultModule = javaProject.getModuleDescription();
		IModuleDescription moduleForRoot = computeModuleFor(root, defaultModule);
		isOnModulePath = moduleForRoot != null;
	} catch (JavaModelException e) {
		isOnModulePath = true; // if an exception occurs, assume yes
		Util.log(e, "Error checking whether PackageFragmentRoot is on module path!"); //$NON-NLS-1$
	}
	return isOnModulePath;
}
}
