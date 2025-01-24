/*******************************************************************************
 * Copyright (c) 2016, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModule.IModuleReference;
import org.eclipse.jdt.internal.compiler.env.IMultiModuleEntry;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.JRTUtil.JrtFileVisitor;
import org.eclipse.jdt.internal.compiler.util.JrtFileSystem;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.util.Util;

public class ClasspathJrt extends ClasspathLocation implements IMultiModuleEntry {

protected final static Map<String, Map<String, IModule>> modulesCache = new ConcurrentHashMap<>();
protected final String zipFilename; // keep for equals
protected final JrtFileSystem jrtFileSystem;
static final Set<String> NO_LIMIT_MODULES = Collections.emptySet();

protected ClasspathJrt(String zipFilename) {
	this.zipFilename = Objects.requireNonNull(zipFilename);
	JrtFileSystem system = null;
	try {
		system = JRTUtil.getJrtSystem(new File(zipFilename), null);
	} catch (IOException e) {
		Util.log(e, "Failed to init packages for " + zipFilename); //$NON-NLS-1$
	}
	this.jrtFileSystem = system;
}

public ClasspathJrt(String zipFilename, AccessRuleSet accessRuleSet, IPath externalAnnotationPath) {
	this(zipFilename);
	this.accessRuleSet = accessRuleSet;
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
	loadModules(this);
}

static Set<String> getModuleNames(final ClasspathJrt jrt) {
	if (jrt.zipFilename == null) {
		return Set.of();
	}
	if (modulesCache.isEmpty()) {
		return null;
	}
	Map<String, IModule> modules = modulesCache.get(jrt.getKey());
	if (modules != null) {
		return modules.keySet();
	}
	return null;
}

public static void loadModules(final ClasspathJrt jrt) {
	String jrtKey = jrt.getKey();
	if (jrtKey == null) {
		return;
	}
	modulesCache.computeIfAbsent(jrtKey, key -> {
		Map<String, IModule> newCache = new HashMap<>();
		try {
			JRTUtil.walkModuleImage(jrt.jrtFileSystem, new JrtFileVisitor<Path>() {
				@Override
				public FileVisitResult visitModule(Path path, String name) throws IOException {
					jrt.acceptModule(JRTUtil.getClassfileContent(jrt.jrtFileSystem, IModule.MODULE_INFO_CLASS, name), name,	newCache);
					return FileVisitResult.SKIP_SUBTREE;
				}
			}, JRTUtil.NOTIFY_MODULES);
		} catch (IOException e) {
			Util.log(e, "Failed to init packages for " + jrt); //$NON-NLS-1$
		}
		return newCache.isEmpty() ? null : Map.copyOf(newCache);
	});
}

protected String getKey() {
	return this.zipFilename;
}

void acceptModule(byte[] content, String name, Map<String, IModule> cache) {
	if (content == null) {
		return;
	}
	try {
		ClassFileReader reader = new ClassFileReader(content, IModule.MODULE_INFO_CLASS.toCharArray());
		IModule moduleDecl = reader.getModuleDeclaration();
		if (moduleDecl != null) {
			cache.put(name, moduleDecl);
		}
	} catch (ClassFormatException e) {
		Util.log(e, "Failed to read module-info.class for " + name + " in " + this.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}
}

@Override
public void cleanup() {
	if (this.annotationZipFile != null) {
		try {
			this.annotationZipFile.close();
		} catch(IOException e) { // ignore it
		}
		this.annotationZipFile = null;
	}
}

@Override
public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathJrt)) return false;
	ClasspathJrt jar = (ClasspathJrt) o;
	if (this.accessRuleSet != jar.accessRuleSet)
		if (this.accessRuleSet == null || !this.accessRuleSet.equals(jar.accessRuleSet))
			return false;
	return this.zipFilename.endsWith(jar.zipFilename) && areAllModuleOptionsEqual(jar);
}

@Override
public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName,
										boolean asBinaryOnly, Predicate<String> moduleNameFilter) {
	if (!isPackage(qualifiedPackageName, moduleName)) {
		return null; // most common case
	}

	try {
		String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
		if (this.jrtFileSystem == null) {
			return null;
		}
		IBinaryType reader = JRTUtil.getClassfile(this.jrtFileSystem, qualifiedBinaryFileName, moduleName, moduleNameFilter);
		if (reader == null) {
			return null;
		}
		return createAnswer(fileNameWithoutExtension, reader, reader.getModule());
	} catch (ClassFormatException | IOException e) { // treat as if class file is missing
		return null;
	}
}
@Override
public IPath getProjectRelativePath() {
	return null;
}

@Override
public int hashCode() {
	return this.zipFilename == null ? super.hashCode() : this.zipFilename.hashCode();
}
@Override
public char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
	List<String> moduleNames = JRTUtil.getModulesDeclaringPackage(this.jrtFileSystem, qualifiedPackageName, moduleName);
	return CharOperation.toCharArrays(moduleNames);
}
@Override
public boolean hasCompilationUnit(String qualifiedPackageName, String moduleName) {
	return JRTUtil.hasCompilationUnit(this.jrtFileSystem, qualifiedPackageName, moduleName);
}
@Override
public boolean isPackage(String qualifiedPackageName, String moduleName) {
	return JRTUtil.getModulesDeclaringPackage(this.jrtFileSystem, qualifiedPackageName, moduleName) != null;
}

@Override
public String toString() {
	String start = "Classpath jrt file " + this.zipFilename; //$NON-NLS-1$
	return start;
}

@Override
public String debugPathString() {
	return this.zipFilename;
}
@Override
public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName,
		boolean asBinaryOnly, Predicate<String> moduleNameFilter) {
	String fileName = new String(typeName);
	return findClass(fileName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, asBinaryOnly, moduleNameFilter);
}
@Override
public boolean hasModule() {
	return true;
}
@Override
public IModule getModule(char[] moduleName) {
	return getModule(String.valueOf(moduleName));
}
public IModule getModule(String moduleName) {
	if (!hasModule()) {
		return null;
	}
	Map<String, IModule> modules = modulesCache.get(getKey());
	if (modules != null) {
		return modules.get(moduleName);
	}
	return null;
}
@Override
public Collection<String> getModuleNames(Collection<String> limitModules) {
	Set<String> cache = getModuleNames(this);
	if (cache != null)
		return selectModules(cache, limitModules);
	return Collections.emptyList();
}

protected Collection<String> selectModules(Set<String> keySet, Collection<String> limitModules) {
	Collection<String> rootModules;
	if (limitModules == NO_LIMIT_MODULES) {
		rootModules = new HashSet<>(keySet);
	} else if (limitModules != null) {
		Set<String> result = new HashSet<>(keySet);
		result.retainAll(limitModules);
		rootModules = result;
	} else {
		rootModules = JavaProject.internalDefaultRootModules(keySet, s -> s, this::getModule);
	}
	Set<String> allModules = new HashSet<>(rootModules);
	for (String mod : rootModules)
		addRequired(mod, allModules);
	return allModules;
}

protected void addRequired(String mod, Set<String> allModules) {
	IModule iMod = getModule(mod);
	if(iMod == null) {
		return;
	}
	for (IModuleReference requiredRef : iMod.requires()) {
		String moduleName = String.valueOf(requiredRef.name());
		IModule reqMod = getModule(moduleName);
		if (reqMod != null) {
			if (allModules.add(moduleName))
				addRequired(moduleName, allModules);
		}
	}
}
@Override
public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName) {
	//
	return findClass(typeName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, false, null);
}
/** TEST ONLY */
public static void resetCaches() {
	modulesCache.clear();
}
}
