/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathJep247Jdk12 extends ClasspathJep247 {

	Map<String, IModule> modules;

	public ClasspathJep247Jdk12(File jdkHome, String release, AccessRuleSet accessRuleSet) {
		super(jdkHome, release, accessRuleSet);
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
			ClassFileReader reader = null;
			byte[] content = null;
			qualifiedBinaryFileName = qualifiedBinaryFileName.replace(".class", ".sig"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.subReleases != null && this.subReleases.length > 0) {
				done: for (String rel : this.subReleases) {
					if (moduleName == null) {
						Path p = this.fs.getPath(rel);
						try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(p)) {
							for (final java.nio.file.Path subdir: stream) {
								Path f = this.fs.getPath(rel, subdir.getFileName().toString(), qualifiedBinaryFileName);
								if (Files.exists(f)) {
									content = JRTUtil.safeReadBytes(f);
									if (content != null)
										break done;
								}
							}
						}
					} else {
						Path p = this.fs.getPath(rel, moduleName, qualifiedBinaryFileName);
						if (Files.exists(p)) {
							content = JRTUtil.safeReadBytes(p);
							if (content != null)
								break;
						}
					}
				}
			} else {
				content = JRTUtil.safeReadBytes(this.fs.getPath(this.releaseInHex, qualifiedBinaryFileName));
			}
			if (content != null) {
				reader = new ClassFileReader(content, qualifiedBinaryFileName.toCharArray());
				return new NameEnvironmentAnswer(reader, fetchAccessRestriction(qualifiedBinaryFileName), null);
			}
		} catch(ClassFormatException e) {
			// Continue
		} catch (IOException e) {
			// continue
		}
		return null;
	}

	@Override
	public void initialize() throws IOException {
		if (this.compliance == null) {
			return;
		}
		if (this.fs != null) {
			super.initialize();
			return;
		}
		this.releaseInHex = Integer.toHexString(Integer.parseInt(this.compliance)).toUpperCase();
		Path filePath = this.jdkHome.toPath().resolve("lib").resolve("ct.sym"); //$NON-NLS-1$ //$NON-NLS-2$
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
			this.fs = FileSystems.newFileSystem(uri, env);
		}
		this.releasePath = this.fs.getPath("/"); //$NON-NLS-1$
		if (!Files.exists(this.fs.getPath(this.releaseInHex))) {
			throw new IllegalArgumentException("release " + this.compliance + " is not found in the system");  //$NON-NLS-1$//$NON-NLS-2$
		}
		List<String> sub = new ArrayList<>();
		try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(this.releasePath)) {
			for (final java.nio.file.Path subdir: stream) {
				String rel = subdir.getFileName().toString();
				if (rel.contains(this.releaseInHex))
					sub.add(rel);
			}
			this.subReleases = sub.toArray(new String[sub.size()]);
		} catch (IOException e) {
			//e.printStackTrace();
		}
		super.initialize();
	}
	@Override
	public void loadModules() {
		// Modules below level 8 are not dealt with here. Leave it to ClasspathJrt
		if (this.jdklevel <= ClassFileConstants.JDK1_8) {
			super.loadModules();
			return;
		}
		final Path modPath = this.fs.getPath(this.releaseInHex);
		this.modulePath = this.file.getPath() + "|" + modPath.toString(); //$NON-NLS-1$
		this.modules = ModulesCache.get(this.modulePath);
		if (this.modules == null) {
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(this.releasePath)) {
				for (final java.nio.file.Path subdir: stream) {
					String rel = subdir.getFileName().toString();
					if (!rel.contains(this.releaseInHex)) {
						continue;
					}
					Files.walkFileTree(subdir, Collections.EMPTY_SET, 2, new FileVisitor<java.nio.file.Path>() {

						@Override
						public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(java.nio.file.Path f, BasicFileAttributes attrs) throws IOException {
							if (attrs.isDirectory() || f.getNameCount() < 3) 
								return FileVisitResult.CONTINUE;
							byte[] content = null;
							if (Files.exists(f)) {
								content = JRTUtil.safeReadBytes(f);
								if (content == null)
									return FileVisitResult.CONTINUE;
								Path m = f.subpath(1, f.getNameCount() - 1);
								ClasspathJep247Jdk12.this.acceptModule(m.getFileName().toString(), content);
								ClasspathJep247Jdk12.this.moduleNamesCache.add(m.getFileName().toString());
							}
							return FileVisitResult.SKIP_SIBLINGS;
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
				e.printStackTrace();
			}
		} else {
			this.moduleNamesCache.addAll(this.modules.keySet());
		}
	}
	@Override
	public Collection<String> getModuleNames(Collection<String> limitModule, Function<String, IModule> getModule) {
		return selectModules(this.moduleNamesCache, limitModule, getModule);
	}
	@Override
	public IModule getModule(char[] moduleName) {
		if (this.modules != null) {
			return this.modules.get(String.valueOf(moduleName));
		}
		return null;
	}
	void acceptModule(String name, byte[] content) {
		if (content == null) 
			return;

		if (this.modules != null) {
			if (this.modules.containsKey(name))
				return;
		}

		ClassFileReader reader = null;
		try {
			reader = new ClassFileReader(content, IModule.MODULE_INFO_CLASS.toCharArray());
		} catch (ClassFormatException e) {
			e.printStackTrace();
		}
		if (reader != null) {
			acceptModule(reader);
		}
	}
	@Override
	void acceptModule(ClassFileReader reader) {
		// Modules below level 8 are not dealt with here. Leave it to ClasspathJrt
		if (this.jdklevel <= ClassFileConstants.JDK1_8) {
			super.acceptModule(reader);
			return;
		}
		if (reader != null) {
			IModule moduleDecl = reader.getModuleDeclaration();
			if (moduleDecl != null) {
				if (this.modules == null) {
					ModulesCache.put(this.modulePath, this.modules = new HashMap<String,IModule>());
				}
				this.modules.put(String.valueOf(moduleDecl.name()), moduleDecl);
			}
		}
	}
	@Override
	public synchronized char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		// Ignore moduleName as this has nothing to do with modules (as of now)
		if (this.packageCache != null)
			return singletonModuleNameIf(this.packageCache.contains(qualifiedPackageName));

		this.packageCache = new HashSet<>(41);
		this.packageCache.add(Util.EMPTY_STRING);
		try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(this.releasePath)) {
			for (final java.nio.file.Path subdir: stream) {
				String rel = subdir.getFileName().toString();
				if (!rel.contains(this.releaseInHex)) {
					continue;
				}
				try (DirectoryStream<java.nio.file.Path> stream2 = Files.newDirectoryStream(subdir)) {
					for (final java.nio.file.Path subdir2: stream2) {
						Files.walkFileTree(subdir2, new FileVisitor<java.nio.file.Path>() {
							@Override
							public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs) throws IOException {
								if (dir.getNameCount() <= 2)
									return FileVisitResult.CONTINUE;
								Path relative = dir.subpath(2, dir.getNameCount());
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
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			// Rethrow
		}
		return singletonModuleNameIf(this.packageCache.contains(qualifiedPackageName));
	}
}
