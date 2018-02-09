/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tal Lev-Ami - added package cache for zip files
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

@SuppressWarnings("rawtypes")
public class ClasspathJar extends ClasspathLocation {

static class PackageCacheEntry {
	long lastModified;
	long fileSize;
	SimpleSet packageSet;

	PackageCacheEntry(long lastModified, long fileSize, SimpleSet packageSet) {
		this.lastModified = lastModified;
		this.fileSize = fileSize;
		this.packageSet = packageSet;
	}
}

protected static SimpleLookupTable PackageCache = new SimpleLookupTable();
protected static SimpleLookupTable ModuleCache = new SimpleLookupTable();

protected static void addToPackageSet(SimpleSet packageSet, String fileName, boolean endsWithSep) {
	int last = endsWithSep ? fileName.length() : fileName.lastIndexOf('/');
	while (last > 0) {
		// extract the package name
		String packageName = fileName.substring(0, last);
		if (packageSet.addIfNotIncluded(packageName) == null)
			return; // already existed
		last = packageName.lastIndexOf('/');
	}
}

/**
 * Calculate and cache the package list available in the zipFile.
 * @return A SimpleSet with the all the package names in the zipFile.
 */
protected SimpleSet findPackageSet() {
	String zipFileName = this.zipFilename;
	PackageCacheEntry cacheEntry = (PackageCacheEntry) PackageCache.get(zipFileName);
	long timestamp = this.lastModified();
	long fileSize = new File(zipFileName).length();
	if (cacheEntry != null && cacheEntry.lastModified == timestamp && cacheEntry.fileSize == fileSize) {
		return cacheEntry.packageSet;
	}
	final SimpleSet packageSet = new SimpleSet(41);
	packageSet.add(""); //$NON-NLS-1$
	readJarContent(packageSet);
	PackageCache.put(zipFileName, new PackageCacheEntry(timestamp, fileSize, packageSet));
	return packageSet;
}
protected String readJarContent(final SimpleSet packageSet) {
	String modInfo = null;
	for (Enumeration e = this.zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = ((ZipEntry) e.nextElement()).getName();
		if (modInfo == null) {
			int folderEnd = fileName.lastIndexOf('/');
			folderEnd += 1;
			String className = fileName.substring(folderEnd, fileName.length());
			if (className.equalsIgnoreCase(IModule.MODULE_INFO_CLASS)) {
				modInfo = fileName;
			}
		}
		addToPackageSet(packageSet, fileName, false);
	}
	return modInfo;
}
IModule initializeModule() {
	IModule mod = null;
	ZipFile file = null;
	try {
		file = new ZipFile(this.zipFilename);
		ClassFileReader classfile = ClassFileReader.read(file, IModule.MODULE_INFO_CLASS); // FIXME: use jar cache
		if (classfile != null) {
			mod = classfile.getModuleDeclaration();
		}
	} catch (ClassFormatException | IOException e) {
		// do nothing
	} finally {
		try {
			if (file != null)
				file.close();
		} catch (IOException e) {
			// do nothing
		}
	}
	return mod;
}

String zipFilename; // keep for equals
IFile resource;
ZipFile zipFile;
ZipFile annotationZipFile;
long lastModified;
boolean closeZipFileAtEnd;
private SimpleSet knownPackageNames;
AccessRuleSet accessRuleSet;
String externalAnnotationPath;

ClasspathJar(IFile resource, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, boolean isOnModulePath) {
	this.resource = resource;
	try {
		java.net.URI location = resource.getLocationURI();
		if (location == null) {
			this.zipFilename = ""; //$NON-NLS-1$
		} else {
			File localFile = Util.toLocalFile(location, null);
			this.zipFilename = localFile.getPath();
		}
	} catch (CoreException e) {
		// ignore
	}
	this.zipFile = null;
	this.knownPackageNames = null;
	this.accessRuleSet = accessRuleSet;
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
	this.isOnModulePath = isOnModulePath;
}

ClasspathJar(String zipFilename, long lastModified, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, boolean isOnModulePath) {
	this.zipFilename = zipFilename;
	this.lastModified = lastModified;
	this.zipFile = null;
	this.knownPackageNames = null;
	this.accessRuleSet = accessRuleSet;
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
	this.isOnModulePath = isOnModulePath;
}

public ClasspathJar(ZipFile zipFile, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, boolean isOnModulePath) {
	this(zipFile.getName(), accessRuleSet, externalAnnotationPath, isOnModulePath);
	this.zipFile = zipFile;
	this.closeZipFileAtEnd = true;
}

public ClasspathJar(String fileName, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, boolean isOnModulePath) {
	this(fileName, 0, accessRuleSet, externalAnnotationPath, isOnModulePath);
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
}

public void cleanup() {
	if (this.closeZipFileAtEnd) {
		if (this.zipFile != null) {
			try {
				this.zipFile.close();
			} catch(IOException e) { // ignore it
			}
			this.zipFile = null;
		}
		if (this.annotationZipFile != null) {
			try {
				this.annotationZipFile.close();
			} catch(IOException e) { // ignore it
			}
			this.annotationZipFile = null;
		}
	}
	this.module = null; // TODO(SHMOD): is this safe?
	this.knownPackageNames = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathJar)) return false;
	ClasspathJar jar = (ClasspathJar) o;
	if (this.accessRuleSet != jar.accessRuleSet)
		if (this.accessRuleSet == null || !this.accessRuleSet.equals(jar.accessRuleSet))
			return false;
	return this.zipFilename.equals(jar.zipFilename) 
			&& lastModified() == jar.lastModified()
			&& this.isOnModulePath == jar.isOnModulePath
			&& areAllModuleOptionsEqual(jar);
}

public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
	if (!isPackage(qualifiedPackageName, moduleName)) return null; // most common case

	try {
		IBinaryType reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
		if (reader != null) {
			char[] modName = this.module == null ? null : this.module.name();
			if (reader instanceof ClassFileReader) {
				ClassFileReader classReader = (ClassFileReader) reader;
				if (classReader.moduleName == null)
					classReader.moduleName = modName;
				else
					modName = classReader.moduleName;
				}
			String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
			if (this.externalAnnotationPath != null) {
				try {
					if (this.annotationZipFile == null) {
						this.annotationZipFile = ExternalAnnotationDecorator
								.getAnnotationZipFile(this.externalAnnotationPath, null);
					}

					reader = ExternalAnnotationDecorator.create(reader, this.externalAnnotationPath,
							fileNameWithoutExtension, this.annotationZipFile);
				} catch (IOException e) {
					// don't let error on annotations fail class reading
				}
				if (reader.getExternalAnnotationStatus() == ExternalAnnotationStatus.NOT_EEA_CONFIGURED) {
					// ensure a reader that answers NO_EEA_FILE
					reader = new ExternalAnnotationDecorator(reader, null);
				}
			}
			if (this.accessRuleSet == null)
				return new NameEnvironmentAnswer(reader, null, modName);
			return new NameEnvironmentAnswer(reader, 
					this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()), 
					modName);
		}
	} catch (IOException e) { // treat as if class file is missing
	} catch (ClassFormatException e) { // treat as if class file is missing
	}
	return null;
}

public IPath getProjectRelativePath() {
	if (this.resource == null) return null;
	return	this.resource.getProjectRelativePath();
}

public int hashCode() {
	return this.zipFilename == null ? super.hashCode() : this.zipFilename.hashCode();
}

public boolean isPackage(String qualifiedPackageName, String moduleName) {
	if (moduleName != null) {
		if (this.module == null || !moduleName.equals(String.valueOf(this.module.name())))
			return false;
	}
	if (this.knownPackageNames == null)
		scanContent();
	return this.knownPackageNames.includes(qualifiedPackageName);
}
@Override
public boolean hasCompilationUnit(String pkgName, String moduleName) {
	for (Enumeration<? extends ZipEntry> e = this.zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = e.nextElement().getName();
		if (fileName.startsWith(pkgName) && fileName.toLowerCase().endsWith(SuffixConstants.SUFFIX_STRING_class))
			return true;
	}	
	return false;
}

/** Scan the contained packages and try to locate the module descriptor. */
private void scanContent() {
	try {
		if (this.zipFile == null) {
			if (org.eclipse.jdt.internal.core.JavaModelManager.ZIP_ACCESS_VERBOSE) {
				System.out.println("(" + Thread.currentThread() + ") [ClasspathJar.isPackage(String)] Creating ZipFile on " + this.zipFilename); //$NON-NLS-1$	//$NON-NLS-2$
			}
			this.zipFile = new ZipFile(this.zipFilename);
			this.closeZipFileAtEnd = true;
			this.knownPackageNames = findPackageSet();
		} else {
			this.knownPackageNames = findPackageSet();
		}
	} catch(Exception e) {
		this.knownPackageNames = new SimpleSet(); // assume for this build the zipFile is empty
	}
}

public long lastModified() {
	if (this.lastModified == 0)
		this.lastModified = new File(this.zipFilename).lastModified();
	return this.lastModified;
}

public String toString() {
	String start = "Classpath jar file " + this.zipFilename; //$NON-NLS-1$
	if (this.accessRuleSet == null)
		return start;
	return start + " with " + this.accessRuleSet; //$NON-NLS-1$
}

public String debugPathString() {
	long time = lastModified();
	if (time == 0)
		return this.zipFilename;
	return this.zipFilename + '(' + (new Date(time)) + " : " + time + ')'; //$NON-NLS-1$
}

@Override
public IModule getModule() {
	if (this.knownPackageNames == null)
		scanContent();
	return this.module;
}

@Override
public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName) {
	// 
	return findClass(typeName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, false);
}
public Manifest getManifest() {
	scanContent(); // ensure zipFile is initialized
	ZipEntry entry = this.zipFile.getEntry(TypeConstants.META_INF_MANIFEST_MF);
	try {
		if (entry != null)
			return new Manifest(this.zipFile.getInputStream(entry));
	} catch (IOException e) {
		// cannot use manifest
	}
	return null;
}
}
