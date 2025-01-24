/*******************************************************************************
 * Copyright (c) 2016, 2023 IBM Corporation.
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
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.zip.ZipFile;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import org.eclipse.jdt.internal.compiler.env.IMultiModuleEntry;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.util.CtSym;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.JrtFileSystem;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ClasspathJrt extends ClasspathLocation implements IMultiModuleEntry {
	public final File file;
	protected final JrtFileSystem jrtFileSystem;
	protected ZipFile annotationZipFile;
	protected final boolean closeZipFileAtEnd;
	protected static final Map<String, Map<String,IModule>> ModulesCache = new ConcurrentHashMap<>();
	public final Set<String> moduleNamesCache;
	protected List<String> annotationPaths;

	public ClasspathJrt(File file, boolean closeZipFileAtEnd,
			AccessRuleSet accessRuleSet, String destinationPath) {
		super(accessRuleSet, destinationPath);
		this.file = file;
		try {
			this.jrtFileSystem= JRTUtil.getJrtSystem(file, null);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to init packages for " + file, e); //$NON-NLS-1$
		}
		this.closeZipFileAtEnd = closeZipFileAtEnd;
		this.moduleNamesCache = new HashSet<>();
	}

	@Override
	public List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
		return null;
	}
	@Override
	public char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		List<String> modules = JRTUtil.getModulesDeclaringPackage(this.jrtFileSystem, qualifiedPackageName, moduleName);
		return CharOperation.toCharArrays(modules);
	}
	@Override
	public boolean hasCompilationUnit(String qualifiedPackageName, String moduleName) {
		return JRTUtil.hasCompilationUnit(this.jrtFileSystem, qualifiedPackageName, moduleName);
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
			IBinaryType reader = JRTUtil.getClassfile(this.jrtFileSystem, qualifiedBinaryFileName, moduleName, this.moduleNamesCache::contains);

			if (reader != null) {
				reader = maybeDecorateForExternalAnnotations(qualifiedBinaryFileName, reader);
				char[] answerModuleName = reader.getModule();
				if (answerModuleName == null && moduleName != null)
					answerModuleName = moduleName.toCharArray();
				return new NameEnvironmentAnswer(reader, fetchAccessRestriction(qualifiedBinaryFileName), answerModuleName);
			}
		} catch (ClassFormatException | IOException e) {
			// treat as if class file is missing
		}
		return null;
	}

	protected IBinaryType maybeDecorateForExternalAnnotations(String qualifiedBinaryFileName, IBinaryType reader) {
		searchPaths:
		if (this.annotationPaths != null) {
			int extensionPos = qualifiedBinaryFileName.lastIndexOf('.'); // extension could be .class or .sig
			String qualifiedClassName = qualifiedBinaryFileName.substring(0, extensionPos);
			for (String annotationPath : this.annotationPaths) {
				try {
					if (this.annotationZipFile == null) {
						this.annotationZipFile = ExternalAnnotationDecorator.getAnnotationZipFile(annotationPath, null);
					}
					reader = ExternalAnnotationDecorator.create(reader, annotationPath, qualifiedClassName, this.annotationZipFile);

					if (reader.getExternalAnnotationStatus() == ExternalAnnotationStatus.TYPE_IS_ANNOTATED) {
						break searchPaths;
					}
				} catch (IOException e) {
					// don't let error on annotations fail class reading
				}
			}
			// location is configured for external annotations, but no .eea found, decorate in order to answer NO_EEA_FILE:
			reader = new ExternalAnnotationDecorator(reader, null);
		}
		return reader;
	}
	@Override
	public boolean hasAnnotationFileFor(String qualifiedTypeName) {
		return false; // TODO(SHMOD): implement
	}
	@Override
	public char[][][] findTypeNames(final String qualifiedPackageName, final String moduleName) {
		if (!isPackage(qualifiedPackageName, moduleName))
			return null; // most common case
		final char[] packageArray = qualifiedPackageName.toCharArray();
		final ArrayList answers = new ArrayList();

		try {
			JRTUtil.walkModuleImage(this.jrtFileSystem, new JRTUtil.JrtFileVisitor<java.nio.file.Path>() {

				@Override
				public FileVisitResult visitPackage(java.nio.file.Path dir, java.nio.file.Path modPath, BasicFileAttributes attrs) throws IOException {
					if (qualifiedPackageName.startsWith(dir.toString())) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(java.nio.file.Path dir, java.nio.file.Path modPath, BasicFileAttributes attrs) throws IOException {
					Path parent = dir.getParent();
					if (parent == null)
						return FileVisitResult.CONTINUE;
					if (!parent.toString().equals(qualifiedPackageName)) {
						return FileVisitResult.CONTINUE;
					}
					String fileName = dir.getName(dir.getNameCount() - 1).toString();
					// The path already excludes the folders and all the '/', hence the -1 for last index of '/'
					addTypeName(answers, fileName, -1, packageArray);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitModule(Path p, String name) throws IOException {
					if (moduleName == null)
						return FileVisitResult.CONTINUE;
					if (!moduleName.equals(name)) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					return FileVisitResult.CONTINUE;
				}

			}, JRTUtil.NOTIFY_ALL);
		} catch (IOException e) {
			String error = "Failed to find module " + moduleName + " defining package " + qualifiedPackageName //$NON-NLS-1$ //$NON-NLS-2$
					+ " in " + this; //$NON-NLS-1$
			if (JRTUtil.PROPAGATE_IO_ERRORS) {
				throw new IllegalStateException(error, e);
			} else {
				System.err.println(error);
				e.printStackTrace();
			}
		}

		int size = answers.size();
		if (size != 0) {
			char[][][] result = new char[size][][];
			answers.toArray(result);
			return result;
		}
		return null;
	}

	protected void addTypeName(final ArrayList answers, String fileName, int last, char[] packageName) {
		int indexOfDot = fileName.lastIndexOf('.');
		if (indexOfDot != -1) {
			String typeName = fileName.substring(last + 1, indexOfDot);
			answers.add(
				CharOperation.arrayConcat(
					CharOperation.splitOn('/', packageName),
					typeName.toCharArray()));
		}
	}
	@Override
	public void initialize() throws IOException {
		loadModules();
	}

	public void loadModules() {
		Map<String, IModule> cache = ModulesCache.computeIfAbsent(this.file.getPath(), key -> {
			HashMap<String,IModule> newCache = new HashMap<>();
			try {
				org.eclipse.jdt.internal.compiler.util.JRTUtil.walkModuleImage(this.jrtFileSystem,
						new org.eclipse.jdt.internal.compiler.util.JRTUtil.JrtFileVisitor<Path>() {

					@Override
					public FileVisitResult visitPackage(Path dir, Path mod, BasicFileAttributes attrs)
							throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path f, Path mod, BasicFileAttributes attrs)
							throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitModule(Path p, String name) throws IOException {
						try {
							ClasspathJrt.this.acceptModule(ClasspathJrt.this.jrtFileSystem.getClassfile(IModule.MODULE_INFO_CLASS, name), newCache);
						} catch (ClassFormatException e) {
							throw new IOException(e);
						}
						return FileVisitResult.SKIP_SUBTREE;
					}
				}, JRTUtil.NOTIFY_MODULES);

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

	void acceptModule(ClassFileReader reader, Map<String, IModule> cache) {
		if (reader != null) {
			IModule moduleDecl = reader.getModuleDeclaration();
			if (moduleDecl != null) {
				cache.put(String.valueOf(moduleDecl.name()), moduleDecl);
			}
		}
	}

	@Override
	public Collection<String> getModuleNames(Collection<String> limitModule, Function<String, IModule> getModule) {
		Map<String, IModule> cache = ModulesCache.get(this.file.getPath());
		return selectModules(cache.keySet(), limitModule, getModule);
	}
	@Override
	protected <T> List<String> allModules(Iterable<T> allSystemModules, Function<T,String> getModuleName, Function<T,IModule> getModule) {
		List<String> result = new ArrayList<>();
		boolean hasJavaDotSE = false;
		for (T mod : allSystemModules) {
			String moduleName = getModuleName.apply(mod);
			if ("java.se".equals(moduleName)) { //$NON-NLS-1$
				result.add(moduleName);
				hasJavaDotSE = true;
				break;
			}
		}
		for (T mod : allSystemModules) {
			String moduleName = getModuleName.apply(mod);
			boolean isJavaDotStart = moduleName.startsWith("java."); //$NON-NLS-1$
			boolean isPotentialRoot = !isJavaDotStart;	// always include non-java.*
			if (!hasJavaDotSE)
				isPotentialRoot |= isJavaDotStart;		// no java.se => add all java.*

			if (isPotentialRoot) {
				IModule m = getModule.apply(mod);
				if (m != null) {
					for (IPackageExport packageExport : m.exports()) {
						if (!packageExport.isQualified()) {
							result.add(moduleName);
							break;
						}
					}
				}
			}
		}
		return result;
	}
	@Override
	public void reset() {
		if (this.closeZipFileAtEnd) {
			if (this.annotationZipFile != null) {
				try {
					this.annotationZipFile.close();
				} catch(IOException e) {
					// ignore
				}
				this.annotationZipFile = null;
			}
		}
		if (this.annotationPaths != null) {
			//this.packageCache = null;
			this.annotationPaths = null;
		}
	}
	@Override
	public String toString() {
		return "Classpath for JRT System " + this.file.getPath(); //$NON-NLS-1$
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
	public boolean hasModule() {
		return true;
	}
	@Override
	public IModule getModule(char[] moduleName) {
		Map<String, IModule> modules = ModulesCache.get(this.file.getPath());
		if (modules != null) {
			return modules.get(String.valueOf(moduleName));
		}
		return null;
	}
	@Override
	public boolean servesModule(char[] moduleName) {
		return getModule(moduleName) != null;
	}

	public static void clearCache(String path, String releaseVersion) {
		if (releaseVersion != null) {
			path += '|'+ CtSym.getReleaseCode(releaseVersion);
		}
		ModulesCache.remove(path);
	}
}
