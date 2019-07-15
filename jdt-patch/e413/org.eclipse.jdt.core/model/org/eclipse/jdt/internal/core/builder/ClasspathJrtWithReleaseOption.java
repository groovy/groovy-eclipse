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
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

public class ClasspathJrtWithReleaseOption extends ClasspathJrt {

	final String release;
	String releaseInHex;
	private String[] subReleases;
	private java.nio.file.FileSystem fs;
	protected Path modulePath;
	private String modPathString;
	private boolean isJRE12Plus;

	public ClasspathJrtWithReleaseOption(String zipFilename, AccessRuleSet accessRuleSet, IPath externalAnnotationPath,
			String release) throws CoreException {
		super();
		if (release == null || release.equals("")) { //$NON-NLS-1$
			throw new IllegalArgumentException("--release argument can not be null"); //$NON-NLS-1$
		}
		this.zipFilename = zipFilename;
		this.accessRuleSet = accessRuleSet;
		if (externalAnnotationPath != null)
			this.externalAnnotationPath = externalAnnotationPath.toString();
		this.release = getReleaseOptionFromCompliance(release);
		initialize();
		loadModules(this);
	}
	/*
	 * JDK 11 doesn't contain release 5. Hence
	 * if the compliance is below 6, we simply return the lowest supported
	 * release, which is 6.
	 */
	private String getReleaseOptionFromCompliance(String comp) {
		if (JavaCore.compareJavaVersions(comp, JavaCore.VERSION_1_5) <= 0) {
			return "6"; //$NON-NLS-1$
		}
		int index = comp.indexOf("1."); //$NON-NLS-1$
		if (index != -1) {
			return comp.substring(index + 2, comp.length());
		} else {
			return comp;
		}
	}
	private boolean isJRE12Plus(Path path) {
		try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(path)) {
			for (final java.nio.file.Path subdir : stream) {
				String rel = JRTUtil.sanitizedFileName(subdir);
				if (Files.exists(this.fs.getPath(rel, "system-modules"))) { //$NON-NLS-1$
					int parseInt = Integer.parseInt(rel, 16);
					return (parseInt > 11);
				}
			}
		} catch (IOException e) {
			this.fs = null;
		}
		return false; 
	}
	/*
	 * Set up the paths where modules and regular classes need to be read. We need to deal with two different kind of
	 * formats of cy.sym: Post JDK 12: ct.sym -> 9 -> java/ -> lang/* 9-modules -> java.base -> module-info.sig
	 * 
	 * From JDK 12 onward: ct.sym -> 9 -> java.base -> module-info.sig java/ -> lang/* Notably, 1) in JDK 12 modules
	 * classes and ordinary classes are located in the same location 2) in JDK 12, ordinary classes are found inside
	 * their respective modules
	 * 
	 */
	protected void initialize() throws CoreException {
		this.releaseInHex = Integer.toHexString(Integer.parseInt(this.release)).toUpperCase();
		Path lib = Paths.get(this.zipFilename).getParent();
		Path filePath = Paths.get(lib.toString(), "ct.sym"); //$NON-NLS-1$
		URI t = filePath.toUri();
		if (!Files.exists(filePath)) {
			return;
		}
		URI uri = URI.create("jar:file:" + t.getRawPath()); //$NON-NLS-1$
		try {
			this.fs = FileSystems.getFileSystem(uri);
		} catch (FileSystemNotFoundException fne) {
			// Ignore and move on
		}
		if (this.fs == null) {
			HashMap<String, ?> env = new HashMap<>();
			try {
				this.fs = FileSystems.newFileSystem(uri, env);
			} catch (IOException e) {
				return;
			}
		}
		Path releasePath = this.fs.getPath("/"); //$NON-NLS-1$
		this.isJRE12Plus = isJRE12Plus(releasePath);
		Path modPath = this.fs.getPath(this.releaseInHex + (this.isJRE12Plus ? "" : "-modules")); //$NON-NLS-1$ //$NON-NLS-2$
		if (Files.exists(modPath)) {
			this.modulePath = modPath;
			this.modPathString = this.zipFilename + "|"+ modPath.toString(); //$NON-NLS-1$
		}
		
		if (!Files.exists(releasePath.resolve(this.releaseInHex))) {
			Exception e = new IllegalArgumentException("release " + this.release + " is not found in the system"); //$NON-NLS-1$//$NON-NLS-2$
			throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, e.getMessage(), e));
		}
		if (Files.exists(this.fs.getPath(this.releaseInHex, "system-modules"))) { //$NON-NLS-1$
			this.fs = null;  // Fallback to default version
			return;
		}
		if (this.release != null) {
			List<String> sub = new ArrayList<>();
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(releasePath)) {
				for (final java.nio.file.Path subdir : stream) {
					String rel = JRTUtil.sanitizedFileName(subdir);
					if (rel.contains(this.releaseInHex)) {
						sub.add(rel);
					} else {
						continue;
					}
				}
			} catch (IOException e) {
				this.fs = null; // Fallback to default version
			}
			this.subReleases = sub.toArray(new String[sub.size()]);
		}
	}

	static HashMap<String, SimpleSet> findPackagesInModules(final ClasspathJrtWithReleaseOption jrt) {
		// In JDK 11 and before, classes are not listed under their respective modules
		// Hence, we simply go to the default module system for package-module mapping
		if (jrt.fs == null || !jrt.isJRE12Plus) {
			return ClasspathJrt.findPackagesInModules(jrt);
		}
		String zipFileName = jrt.zipFilename;
		HashMap<String, SimpleSet> cache = PackageCache.get(jrt.modPathString);
		if (cache != null) {
			return cache;
		}
		final HashMap<String, SimpleSet> packagesInModule = new HashMap<>();
		PackageCache.put(jrt.modPathString, packagesInModule);
		try {
			final File imageFile = new File(zipFileName);
			org.eclipse.jdt.internal.compiler.util.JRTUtil.walkModuleImage(imageFile, jrt.release,
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
						public FileVisitResult visitModule(Path path, String name) throws IOException {
							this.packageSet = new SimpleSet(41);
							this.packageSet.add(""); //$NON-NLS-1$
							if (name.endsWith("/")) { //$NON-NLS-1$
								name = name.substring(0, name.length() - 1);
							}
							packagesInModule.put(name, this.packageSet);
							return FileVisitResult.CONTINUE;
						}
					}, JRTUtil.NOTIFY_PACKAGES | JRTUtil.NOTIFY_MODULES);
		} catch (IOException e) {
			// return empty handed
		}
		return packagesInModule;
	}

	public static void loadModules(final ClasspathJrtWithReleaseOption jrt) {
		if (jrt.fs == null || !jrt.isJRE12Plus) {
			ClasspathJrt.loadModules(jrt);
			return;
		}
		if (jrt.modPathString == null)
			return;
		Set<IModule> cache = ModulesCache.get(jrt.modPathString);
		if (cache == null) {
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(jrt.modulePath)) {
				for (final java.nio.file.Path subdir : stream) {

					Files.walkFileTree(subdir, Collections.EMPTY_SET, 1, new FileVisitor<java.nio.file.Path>() {
						@Override
						public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(java.nio.file.Path f, BasicFileAttributes attrs)
								throws IOException {
							byte[] content = null;
							if (Files.exists(f)) {
								content = JRTUtil.safeReadBytes(f);
								if (content == null)
									return FileVisitResult.CONTINUE;
								jrt.acceptModule(content);
							}
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(java.nio.file.Path f, IOException exc)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}
					});
				}
			} catch (IOException e) {
				// Nothing much to do
			}
		}
	}


	@Override
	public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String moduleName,
			String qualifiedBinaryFileName, boolean asBinaryOnly, Predicate<String> moduleNameFilter) {

		if (this.fs == null) {
			return super.findClass(binaryFileName, qualifiedPackageName, moduleName, qualifiedBinaryFileName,
					asBinaryOnly, moduleNameFilter);
		}
		if (!isPackage(qualifiedPackageName, moduleName))
			return null; // most common case

		try {
			IBinaryType reader = null;
			byte[] content = null;
			String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0,
												qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
			if (this.subReleases != null && this.subReleases.length > 0) {
				qualifiedBinaryFileName = qualifiedBinaryFileName.replace(".class", ".sig"); //$NON-NLS-1$ //$NON-NLS-2$
				outer: for (String rel : this.subReleases) {
					Path p = null;
					inner: if (this.isJRE12Plus) {
						if (moduleName != null) {
							p = this.fs.getPath(rel, moduleName, qualifiedBinaryFileName);
						} 
						else {
							try (DirectoryStream<java.nio.file.Path> stream = Files
									.newDirectoryStream(this.fs.getPath(rel))) {
								for (final java.nio.file.Path subdir : stream) {
									p = subdir.resolve(qualifiedBinaryFileName);
									if (Files.exists(p)) {
										if (subdir.getNameCount() == 2 ) {
											moduleName = subdir.getName(1).toString();
										}
										break inner;
									}
								}
							}
						}
					} else {
						p = this.fs.getPath(rel, qualifiedBinaryFileName);
					}
					if (Files.exists(p)) {
						content = JRTUtil.safeReadBytes(p);
						if (content != null) {
							reader = new ClassFileReader(content, qualifiedBinaryFileName.toCharArray());
							if (moduleName != null)
								((ClassFileReader) reader).moduleName = moduleName.toCharArray();
							break outer;
						}
					}
				}
			} else {
				reader = ClassFileReader.readFromModule(new File(this.zipFilename), moduleName, qualifiedBinaryFileName,
						moduleNameFilter);
			}
			return createAnswer(fileNameWithoutExtension, reader);
		} catch (ClassFormatException | IOException e) { 
			// treat as if class file is missing
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

	@Override
	public void cleanup() {
		try {
			super.reset();
		} finally {
			// The same file system is also used in JRTUtil, so don't close it here.
			this.fs = null;
		}
	}

	@Override
	public boolean hasModule() {
		return this.fs == null ? super.hasModule() : this.modPathString != null;
	}

	@Override
	protected String getKey() {
		return this.fs == null ? super.getKey() : this.modPathString;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ClasspathJrtWithReleaseOption))
			return false;
		ClasspathJrtWithReleaseOption jar = (ClasspathJrtWithReleaseOption) o;
		if (!Util.equalOrNull(this.release, jar.release)) {
			return false;
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		int hash = this.zipFilename == null ? super.hashCode() : this.zipFilename.hashCode();
		return Util.combineHashCodes(hash, this.release.hashCode());
	}

	@Override
	public String toString() {
		String start = "Classpath jrt file " + this.zipFilename + " with --release option " + this.release; //$NON-NLS-1$ //$NON-NLS-2$
		return start;
	}

}
