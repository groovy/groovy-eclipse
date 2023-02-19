/*******************************************************************************
 * Copyright (c) 2019, 2022 IBM Corporation.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.CtSym;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathJep247Jdk12 extends ClasspathJep247 {

	Map<String, IModule> modules;
	static String MODULE_INFO = "module-info.sig"; //$NON-NLS-1$

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
			IBinaryType reader = null;
			Path p = null;
			byte[] content = null;
			char[] foundModName = null;
			qualifiedBinaryFileName = qualifiedBinaryFileName.replace(".class", ".sig"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.subReleases != null && this.subReleases.length > 0) {
				done: for (String rel : this.subReleases) {
					if (moduleName == null) {
						p = this.fs.getPath(rel);
						try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(p)) {
							for (final java.nio.file.Path subdir: stream) {
								p = this.fs.getPath(rel, JRTUtil.sanitizedFileName(subdir), qualifiedBinaryFileName);
								if (Files.exists(p)) {
									content = JRTUtil.safeReadBytes(p);
									foundModName = JRTUtil.sanitizedFileName(subdir).toCharArray();
									if (content != null)
										break done;
								}
							}
						}
					} else {
						p = this.fs.getPath(rel, moduleName, qualifiedBinaryFileName);
						if (Files.exists(p)) {
							content = JRTUtil.safeReadBytes(p);
							if (content != null)
								break;
						}
					}
				}
			} else {
				p = this.fs.getPath(this.releaseInHex, qualifiedBinaryFileName);
				content = JRTUtil.safeReadBytes(p);
			}
			if (content != null) {
				reader = new ClassFileReader(p.toUri(), content, qualifiedBinaryFileName.toCharArray());
				reader = maybeDecorateForExternalAnnotations(qualifiedBinaryFileName, reader);
				char[] modName = moduleName != null ? moduleName.toCharArray() : foundModName;
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
		if (this.fs != null) {
			super.initialize();
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
		List<String> sub = new ArrayList<>();
		try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(this.releasePath)) {
			for (final java.nio.file.Path subdir: stream) {
				String rel = JRTUtil.sanitizedFileName(subdir);
				if (rel.contains(this.releaseInHex))
					sub.add(rel);
			}
			this.subReleases = sub.toArray(new String[sub.size()]);
		} catch (IOException e) {
			String error = "Failed to walk subreleases for release " + this.releasePath + " in " + filePath; //$NON-NLS-1$ //$NON-NLS-2$
			if (JRTUtil.PROPAGATE_IO_ERRORS) {
				throw new IllegalStateException(error, e);
			} else {
				System.err.println(error);
				e.printStackTrace();
			}
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
		final Path modPath = this.fs.getPath(this.releaseInHex);
		this.modulePath = this.file.getPath() + "|" + modPath.toString(); //$NON-NLS-1$
		Map<String, IModule> cache = ModulesCache.computeIfAbsent(this.modulePath, key -> {
			HashMap<String,IModule> newCache = new HashMap<>();
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(this.releasePath)) {
				for (final java.nio.file.Path subdir: stream) {
					String rel = JRTUtil.sanitizedFileName(subdir);
					if (!rel.contains(this.releaseInHex)) {
						continue;
					}
					Files.walkFileTree(subdir, Collections.emptySet(), 2, new FileVisitor<java.nio.file.Path>() {

						@Override
						public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(java.nio.file.Path f, BasicFileAttributes attrs) throws IOException {
							if (attrs.isDirectory() || f.getNameCount() < 3) {
								return FileVisitResult.CONTINUE;
							}
							if (f.getFileName().toString().equals(MODULE_INFO) && Files.exists(f)) {
								byte[] content = JRTUtil.safeReadBytes(f);
								if (content == null) {
									return FileVisitResult.CONTINUE;
								}
								Path m = f.subpath(1, f.getNameCount() - 1);
								String name = JRTUtil.sanitizedFileName(m);
								ClasspathJep247Jdk12.this.acceptModule(name, content, newCache);
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
		this.modules = cache;
		this.moduleNamesCache.addAll(cache.keySet());
	}
	@Override
	public Collection<String> getModuleNames(Collection<String> limitModule, Function<String, IModule> getModule) {
		return selectModules(this.moduleNamesCache, limitModule, getModule);
	}
	@Override
	public IModule getModule(char[] moduleName) {
		// Modules below level 9 are not dealt with here. Leave it to ClasspathJrt
		if (this.jdklevel <= ClassFileConstants.JDK1_8) {
			return super.getModule(moduleName);
		}
		if (this.modules != null) {
			return this.modules.get(String.valueOf(moduleName));
		}
		return null;
	}
	void acceptModule(String name, byte[] content, Map<String, IModule> cache) {
		if (content == null)
			return;

		if (cache.containsKey(name))
			return;

		ClassFileReader reader = null;
		try {
			reader = new ClassFileReader(content, IModule.MODULE_INFO_CLASS.toCharArray());
		} catch (ClassFormatException e) {
			e.printStackTrace();
		}
		if (reader != null) {
			acceptModule(reader, cache);
		}
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
	@Override
	public synchronized char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		if (this.jdklevel >= ClassFileConstants.JDK9) {
			// Delegate to the boss, even if it means inaccurate error reporting at times
			List<String> mods = JRTUtil.getModulesDeclaringPackage(this.file, qualifiedPackageName, moduleName);
			return CharOperation.toCharArrays(mods);
		}
		if (this.packageCache == null) {
			this.packageCache = new HashSet<>(41);
			this.packageCache.add(Util.EMPTY_STRING);
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(this.releasePath)) {
				for (final java.nio.file.Path subdir: stream) {
					String rel = JRTUtil.sanitizedFileName(subdir);
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
				String error = "Failed to find module " + moduleName + " defining package " + qualifiedPackageName //$NON-NLS-1$ //$NON-NLS-2$
						+ " in release " + this.releasePath + " in " + this; //$NON-NLS-1$ //$NON-NLS-2$
				if (JRTUtil.PROPAGATE_IO_ERRORS) {
					throw new IllegalStateException(error, e);
				} else {
					System.err.println(error);
					e.printStackTrace();
				}
			}
		}
		return singletonModuleNameIf(this.packageCache.contains(qualifiedPackageName));
	}
}
