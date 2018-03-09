/*******************************************************************************
 * Copyright (c) 2016, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModule.IModuleReference;
import org.eclipse.jdt.internal.compiler.env.IMultiModuleEntry;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.util.Util;

public class ClasspathJrt extends ClasspathLocation implements IMultiModuleEntry {

//private HashMap<String, SimpleSet> packagesInModule = null;
private static HashMap<String, HashMap<String, SimpleSet>> PackageCache = new HashMap<>();
private static HashMap<String, Set<IModule>> ModulesCache = new HashMap<>();
String externalAnnotationPath;
private ZipFile annotationZipFile;
String zipFilename; // keep for equals
AccessRuleSet accessRuleSet;
String compliance = null;
private Path releasePath = null;
private String[] subReleases = null;
private java.nio.file.FileSystem fs = null;

static final Set<String> NO_LIMIT_MODULES = new HashSet<>();

public ClasspathJrt(String zipFilename, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, String compliance) {
	this.zipFilename = zipFilename;
	this.accessRuleSet = accessRuleSet;
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
	if (compliance != null && compliance.length() == 0) {
		this.compliance = null;
	} else {
		this.compliance = compliance;
	}
	initialize();
	loadModules(this);
}
/**
 * Calculate and cache the package list available in the zipFile.
 * @param jrt The ClasspathJar to use
 * @return A SimpleSet with the all the package names in the zipFile.
 */
static HashMap<String, SimpleSet> findPackagesInModules(final ClasspathJrt jrt) {
	String zipFileName = jrt.zipFilename;
	HashMap<String, SimpleSet> cache = PackageCache.get(zipFileName);
	if (cache != null) {
		return cache;
	}
	final HashMap<String, SimpleSet> packagesInModule = new HashMap<>();
	PackageCache.put(zipFileName, packagesInModule);
	try {
		final File imageFile = new File(zipFileName);
		org.eclipse.jdt.internal.compiler.util.JRTUtil.walkModuleImage(imageFile,
				new org.eclipse.jdt.internal.compiler.util.JRTUtil.JrtFileVisitor<Path>() {
			SimpleSet packageSet = null;
			@Override
			public FileVisitResult visitPackage(Path dir, Path mod, BasicFileAttributes attrs) throws IOException {
				ClasspathJar.addToPackageSet(this.packageSet, dir.toString(), true);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, Path mod, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitModule(Path mod) throws IOException {
				String name = mod.toString();
				try {
					jrt.acceptModule(JRTUtil.getClassfileContent(imageFile, IModule.MODULE_INFO_CLASS, name));
				} catch (ClassFormatException e) {
					e.printStackTrace();
				}
				this.packageSet = new SimpleSet(41);
				this.packageSet.add(""); //$NON-NLS-1$
				packagesInModule.put(name, this.packageSet);
				return FileVisitResult.CONTINUE;
			}
		}, JRTUtil.NOTIFY_PACKAGES | JRTUtil.NOTIFY_MODULES);
	} catch (IOException e) {
		// TODO: Java 9 Should report better
	}
	return packagesInModule;
}

public static void loadModules(final ClasspathJrt jrt) {
	String zipFileName = jrt.zipFilename;
	Set<IModule> cache = ModulesCache.get(zipFileName);

	if (cache == null) {
		try {
			final File imageFile = new File(zipFileName);
			org.eclipse.jdt.internal.compiler.util.JRTUtil.walkModuleImage(imageFile,
					new org.eclipse.jdt.internal.compiler.util.JRTUtil.JrtFileVisitor<Path>() {
				SimpleSet packageSet = null;

				@Override
				public FileVisitResult visitPackage(Path dir, Path mod, BasicFileAttributes attrs)
						throws IOException {
					ClasspathJar.addToPackageSet(this.packageSet, dir.toString(), true);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, Path mod, BasicFileAttributes attrs)
						throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitModule(Path mod) throws IOException {
					try {
						jrt.acceptModule(JRTUtil.getClassfileContent(imageFile, IModule.MODULE_INFO_CLASS, mod.toString()));
					} catch (ClassFormatException e) {
						e.printStackTrace();
					}
					return FileVisitResult.SKIP_SUBTREE;
				}
			}, JRTUtil.NOTIFY_MODULES);
		} catch (IOException e) {
			// TODO: Java 9 Should report better
		}
	} else {
//		for (IModuleDeclaration iModule : cache) {
//			jimage.env.acceptModule(iModule, jimage);
//		}
	}
}
public void initialize() {
	if (this.compliance == null) {
		return;
	}
	this.compliance = getReleaseOptionFromCompliance(this.compliance);
	Path lib = Paths.get(this.zipFilename).getParent();
	Path filePath = Paths.get(lib.toString(),  "ct.sym"); //$NON-NLS-1$
	URI t = filePath.toUri();
	if (!Files.exists(filePath)) {
		return;
	}
	URI uri = URI.create("jar:file:" + t.getRawPath()); //$NON-NLS-1$
	try {
		this.fs = FileSystems.getFileSystem(uri);
	} catch(FileSystemNotFoundException fne) {
		// Ignore and move on
	}
	if (this.fs == null) {
		HashMap<String, ?> env = new HashMap<>();
		try {
			this.fs = FileSystems.newFileSystem(uri, env);
		} catch (IOException e) {
			this.compliance = null;
			return;
		}
	}
	this.releasePath = this.fs.getPath("/"); //$NON-NLS-1$
	if (!Files.exists(this.fs.getPath(this.compliance))
			|| Files.exists(this.fs.getPath(this.compliance, "system-modules"))) { //$NON-NLS-1$
		this.compliance = null;
	}
	if (this.compliance != null) {
		List<String> sub = new ArrayList<>();
		try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(this.releasePath)) {
			for (final java.nio.file.Path subdir: stream) {
				String rel = subdir.getFileName().toString();
				if (rel.contains(this.compliance)) {
					sub.add(rel);
				} else {
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			// Rethrow
		}
		this.subReleases = sub.toArray(new String[sub.size()]);
	}
}
private String getReleaseOptionFromCompliance(String comp) {
	if (JavaCore.compareJavaVersions(comp, JavaCore.VERSION_1_5) <= 0) {
		// For a JDK 9 and above, the minimum release we support is "6"
		return "6"; //$NON-NLS-1$
	}
	int index = comp.indexOf("1."); //$NON-NLS-1$
	if (index != -1) {
		return comp.substring(index + 2, comp.length());
	} else {
		return comp;
	}
}
void acceptModule(byte[] content) {
	if (content == null)
		return;
	ClassFileReader reader = null;
	try {
		reader = new ClassFileReader(content, IModule.MODULE_INFO_CLASS.toCharArray());
	} catch (ClassFormatException e) {
		e.printStackTrace();
	}
	if (reader != null) {
		IModule moduleDecl = reader.getModuleDeclaration();
		if (moduleDecl != null) {
			Set<IModule> cache = ModulesCache.get(this.zipFilename);
			if (cache == null) {
				ModulesCache.put(this.zipFilename, cache = new HashSet<IModule>());
			}
			cache.add(moduleDecl);
		}
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
	if (!Util.equalOrNull(this.compliance, jar.compliance)) {
		return false;
	}
	if (this.accessRuleSet != jar.accessRuleSet)
		if (this.accessRuleSet == null || !this.accessRuleSet.equals(jar.accessRuleSet))
			return false;
	return this.zipFilename.endsWith(jar.zipFilename) && areAllModuleOptionsEqual(jar);
}

@Override
public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName,
										boolean asBinaryOnly, Predicate<String> moduleNameFilter) {
	if (!isPackage(qualifiedPackageName, moduleName)) return null; // most common case

	try {
		IBinaryType reader = null;
		byte[] content = null;
		String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
		if (this.subReleases != null && this.subReleases.length > 0) {
			qualifiedBinaryFileName = qualifiedBinaryFileName.replace(".class", ".sig"); //$NON-NLS-1$ //$NON-NLS-2$
			for (String rel : this.subReleases) {
				Path p = this.fs.getPath(rel, qualifiedBinaryFileName);
				if (Files.exists(p)) {
					content = Files.readAllBytes(p);
					if (content != null) {
						reader = new ClassFileReader(content, qualifiedBinaryFileName.toCharArray());
						break;
					}
				}
			}
		} else {
			reader = ClassFileReader.readFromModule(new File(this.zipFilename), moduleName, qualifiedBinaryFileName, moduleNameFilter);
		}
		if (reader != null) {
			if (this.externalAnnotationPath != null) {
				try {
					if (this.annotationZipFile == null) {
						this.annotationZipFile = ExternalAnnotationDecorator.getAnnotationZipFile(this.externalAnnotationPath, null);
					}
					reader = ExternalAnnotationDecorator.create(reader, this.externalAnnotationPath, fileNameWithoutExtension, this.annotationZipFile);
				} catch (IOException e) {
					// don't let error on annotations fail class reading
				}
			}
			if (this.accessRuleSet == null)
				return new NameEnvironmentAnswer(reader, null, reader.getModule());
			return new NameEnvironmentAnswer(reader,
					this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()),
					reader.getModule());
		}
	} catch (ClassFormatException e) { // treat as if class file is missing
	} catch (IOException e) { // treat as if class file is missing
	}
	return null;
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
	List<String> moduleNames = JRTUtil.getModulesDeclaringPackage(new File(this.zipFilename), qualifiedPackageName, moduleName);
	return CharOperation.toCharArrays(moduleNames);
}
@Override
public boolean hasCompilationUnit(String qualifiedPackageName, String moduleName) {
	return JRTUtil.hasCompilationUnit(new File(this.zipFilename), qualifiedPackageName, moduleName);
}
@Override
public boolean isPackage(String qualifiedPackageName, String moduleName) {
	return JRTUtil.getModulesDeclaringPackage(new File(this.zipFilename), qualifiedPackageName, moduleName) != null;
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
	Set<IModule> modules = ModulesCache.get(this.zipFilename);
	if (modules != null) {
		for (IModule mod : modules) {
			if (CharOperation.equals(mod.name(), moduleName))
					return mod;
		}
	}
	return null;
}
@Override
public Collection<String> getModuleNames(Collection<String> limitModules) {
	HashMap<String, SimpleSet> cache = findPackagesInModules(this);
	if (cache != null)
		return selectModules(cache.keySet(), limitModules);
	return Collections.emptyList();
}

private Collection<String> selectModules(Set<String> keySet, Collection<String> limitModules) {
	Collection<String> rootModules;
	if (limitModules == NO_LIMIT_MODULES) {
		rootModules = new HashSet<>(keySet);
	} else if (limitModules != null) {
		Set<String> result = new HashSet<>(keySet);
		result.retainAll(limitModules);
		rootModules = result;
	} else {
		rootModules = JavaProject.internalDefaultRootModules(keySet, s -> s, m -> getModule(m.toCharArray()));
	}
	Set<String> allModules = new HashSet<>(rootModules);
	for (String mod : rootModules)
		addRequired(mod, allModules);
	return allModules;
}

private void addRequired(String mod, Set<String> allModules) {
	IModule iMod = getModule(mod.toCharArray());
	for (IModuleReference requiredRef : iMod.requires()) {
		IModule reqMod = getModule(requiredRef.name());
		if (reqMod != null) {
			String reqModName = String.valueOf(reqMod.name());
			if (allModules.add(reqModName))
				addRequired(reqModName, allModules);
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
	PackageCache.clear();
	ModulesCache.clear();
}
}
