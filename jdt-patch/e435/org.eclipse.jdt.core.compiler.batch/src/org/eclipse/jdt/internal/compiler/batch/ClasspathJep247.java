/*******************************************************************************
 * Copyright (c) 2018, 2023 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christoph Läubrich - use Filesystem helper method
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.CtSym;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathJep247 extends ClasspathJrt {

	protected java.nio.file.FileSystem fs;
	protected final String compliance;
	protected final long jdklevel;
	protected String releaseInHex;
	protected String[] subReleases;
	protected Path releasePath;
	protected Set<String> packageCache;
	protected final File jdkHome;
	protected String modulePath;

	public ClasspathJep247(File jdkHome, String release, AccessRuleSet accessRuleSet) {
		super(new File(new File(jdkHome, "lib"), JRTUtil.JRT_FS_JAR), false, accessRuleSet, null); //$NON-NLS-1$
		this.compliance = release;
		this.jdklevel = CompilerOptions.releaseToJDKLevel(this.compliance);
		this.jdkHome = jdkHome;
	}
	@Override
	public List<Classpath> fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
		 return null;
	}
	@Override
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName) {
		return findClass(typeName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, false);
	}
	@Override
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
		if (!isPackage(qualifiedPackageName, moduleName))
			return null; // most common case

		try {
			//TODO: Check if any conversion needed for path separator
			IBinaryType reader = null;
			byte[] content = null;
			qualifiedBinaryFileName = qualifiedBinaryFileName.replace(".class", ".sig"); //$NON-NLS-1$ //$NON-NLS-2$
			Path p = null;
			if (this.subReleases != null && this.subReleases.length > 0) {
				for (String rel : this.subReleases) {
					p = this.fs.getPath(rel, qualifiedBinaryFileName);
					if (Files.exists(p)) {
						content = JRTUtil.safeReadBytes(p);
						if (content != null)
							break;
					}
				}
			} else {
				p = this.fs.getPath(this.releaseInHex, qualifiedBinaryFileName);
				content = JRTUtil.safeReadBytes(p);
			}
			if (content != null) {
				reader = new ClassFileReader(p.toUri(), content, qualifiedBinaryFileName.toCharArray());
				reader = maybeDecorateForExternalAnnotations(qualifiedBinaryFileName, reader);
				char[] modName = moduleName != null ? moduleName.toCharArray() : null;
				return new NameEnvironmentAnswer(reader, fetchAccessRestriction(qualifiedBinaryFileName), modName);
			}
		} catch (ClassFormatException | IOException e) {
			// continue
		}
		return null;
	}

	@Override
	public void initialize() throws IOException {
		if (this.compliance == null) {
			return;
		}
		this.releaseInHex = CtSym.getReleaseCode(this.compliance);
		Path filePath = this.jdkHome.toPath().resolve("lib").resolve("ct.sym"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!Files.exists(filePath)) {
			return;
		}
		this.fs = JRTUtil.getJarFileSystem(filePath);
		this.releasePath = this.fs.getPath("/"); //$NON-NLS-1$
		if (!Files.exists(this.fs.getPath(this.releaseInHex))) {
			throw new IllegalArgumentException("release " + this.compliance + " is not found in the system");  //$NON-NLS-1$//$NON-NLS-2$
		}
		super.initialize();
	}
	@Override
	public void loadModules() {
		// Modules below level 9 are not dealt with here. Leave it to ClasspathJrt
		if (this.jdklevel <= ClassFileConstants.JDK1_8) {
			super.loadModules();
			return;
		}
		final Path modPath = this.fs.getPath(this.releaseInHex + "-modules"); //$NON-NLS-1$
		if (!Files.exists(modPath)) {
			throw new IllegalArgumentException("release " + this.compliance + " is not found in the system");  //$NON-NLS-1$//$NON-NLS-2$
		}
		this.modulePath = this.file.getPath() + "|" + modPath.toString(); //$NON-NLS-1$
		Map<String, IModule> cache = ModulesCache.computeIfAbsent(this.modulePath, key -> {
			Map<String,IModule> newCache = new HashMap<>();
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(modPath)) {
				for (final java.nio.file.Path subdir: stream) {
					Files.walkFileTree(subdir, new FileVisitor<java.nio.file.Path>() {

						@Override
						public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(java.nio.file.Path f, BasicFileAttributes attrs) throws IOException {
							byte[] content = null;
							if (Files.exists(f)) {
								content = JRTUtil.safeReadBytes(f);
								if (content == null) {
									return FileVisitResult.CONTINUE;
								}
								try {
									ClasspathJep247.this.acceptModule(new ClassFileReader(f.toUri(), content, f.getFileName().toString().toCharArray()), newCache);
								} catch (ClassFormatException e) {
									e.printStackTrace();
								}
							}
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(java.nio.file.Path f, IOException exc) throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc) throws IOException {
							return FileVisitResult.CONTINUE;
						}
					});
				}
			} catch (IOException e) {
				String error = "Failed to walk modules for " + key; //$NON-NLS-1$
				if (JRTUtil.PROPAGATE_IO_ERRORS) {
					throw new IllegalStateException(error, e);
				} else {
					System.err.println(error);
					e.printStackTrace();
					return null;
				}
			}
			return newCache.isEmpty() ? null : Collections.unmodifiableMap(newCache);
		});

		this.moduleNamesCache.addAll(cache.keySet());
	}

	@Override
	void acceptModule(ClassFileReader reader, Map<String, IModule> cache) {
		// Modules below level 9 are not dealt with here. Leave it to ClasspathJrt
		if (this.jdklevel <= ClassFileConstants.JDK1_8) {
			super.acceptModule(reader, cache);
			return;
		}
		if (reader != null) {
			IModule moduleDecl = reader.getModuleDeclaration();
			if (moduleDecl != null) {
				cache.put(String.valueOf(moduleDecl.name()), moduleDecl);
			}
		}
	}
	protected void addToPackageCache(String packageName, boolean endsWithSep) {
		if (this.packageCache.contains(packageName))
			return;
		this.packageCache.add(packageName);
	}
	@Override
	public synchronized char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		if (this.packageCache == null) {
			this.packageCache = new HashSet<>(41);
			this.packageCache.add(Util.EMPTY_STRING);
			List<String> sub = new ArrayList<>();
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(this.releasePath)) {
				for (final java.nio.file.Path subdir: stream) {
					String rel = JRTUtil.sanitizedFileName(subdir);
					if (rel.contains(this.releaseInHex)) {
						sub.add(rel);
					} else {
						continue;
					}
					Files.walkFileTree(subdir, new FileVisitor<java.nio.file.Path>() {
						@Override
						public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs) throws IOException {
							if (dir.getNameCount() <= 1)
								return FileVisitResult.CONTINUE;
							Path relative = dir.subpath(1, dir.getNameCount());
							addToPackageCache(relative.toString(), false);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(java.nio.file.Path f, BasicFileAttributes attrs) throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(java.nio.file.Path f, IOException exc) throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc) throws IOException {
							return FileVisitResult.CONTINUE;
						}
					});
				}
			} catch (IOException e) {
				String error = "Failed to find module " + moduleName + " defining package " + qualifiedPackageName //$NON-NLS-1$ //$NON-NLS-2$
						+ " in release " + this.releasePath + " in " + this; //$NON-NLS-1$ //$NON-NLS-2$
				if (JRTUtil.PROPAGATE_IO_ERRORS) {
					throw new IllegalStateException(error, e);
				} else {
					System.err.println(error);
					e.printStackTrace();
				}
			}
			this.subReleases = sub.toArray(new String[sub.size()]);
		}
		if (moduleName == null) {
			// Delegate to the boss, even if it means inaccurate error reporting at times
			List<String> mods = JRTUtil.getModulesDeclaringPackage(this.jrtFileSystem, qualifiedPackageName, moduleName);
			return CharOperation.toCharArrays(mods);
		}
		return singletonModuleNameIf(this.packageCache.contains(qualifiedPackageName));
	}

	@Override
	public String toString() {
		return "Classpath for JEP 247 for JDK " + this.file.getPath(); //$NON-NLS-1$
	}
	@Override
	public char[] normalizedPath() {
		if (this.normalizedPath == null) {
			String path2 = this.getPath();
			char[] rawName = path2.toCharArray();
			if (File.separatorChar == '\\') {
				CharOperation.replace(rawName, '\\', '/');
			}
			this.normalizedPath = CharOperation.subarray(rawName, 0, CharOperation.lastIndexOf('.', rawName));
		}
		return this.normalizedPath;
	}
	@Override
	public String getPath() {
		if (this.path == null) {
			this.path = this.file.toPath().normalize().toAbsolutePath().toString();
		}
		return this.path;
	}
	@Override
	public int getMode() {
		return BINARY;
	}
	@Override
	public boolean forbidsExportFrom(String modName) {
		return servesModule(modName.toCharArray());
	}

}
