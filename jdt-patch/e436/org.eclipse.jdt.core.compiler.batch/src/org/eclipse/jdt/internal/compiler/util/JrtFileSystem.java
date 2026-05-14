/*******************************************************************************
 * Copyright (c) 2015, 2024 IBM Corporation.
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
package org.eclipse.jdt.internal.compiler.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;

public class JrtFileSystem {

	private final Map<String, String> packageToModule = new HashMap<>();

	private final Map<String, List<String>> packageToModules = new HashMap<>();

	FileSystem fs;
	final Path modRoot;
	final Jdk jdk;
	final String release;

	public static JrtFileSystem getNewJrtFileSystem(Jdk jdk, String release) throws IOException {
		if (release == null || jdk.sameRelease(release)) {
			return new JrtFileSystem(jdk, null);
		} else {
			return new JrtFileSystemWithOlderRelease(jdk, release);
		}
	}

	/**
	 * The jrt file system is based on the location of the JRE home whose libraries
	 * need to be loaded.
	 */
	JrtFileSystem(Jdk jdkHome, String release) throws IOException {
		this.jdk = jdkHome;
		this.release = release;
		JRTUtil.MODULE_TO_LOAD = System.getProperty("modules.to.load"); //$NON-NLS-1$
		this.fs = JRTUtil.getJrtFileSystem(this.jdk.path);
		this.modRoot = this.fs.getPath(JRTUtil.MODULES_SUBDIR);
		// Set up the root directory where modules are located
		walkJrtForModules();
	}

	public List<String> getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		qualifiedPackageName = qualifiedPackageName.replace('.', '/');
		String module = this.packageToModule.get(qualifiedPackageName);
		if (moduleName == null) {
			// wildcard search:
			if (module == null)
				return null;
			if (module == JRTUtil.MULTIPLE)
				return this.packageToModules.get(qualifiedPackageName);
			return Collections.singletonList(module);
		}
		if (module != null) {
			// specific search:
			if (module == JRTUtil.MULTIPLE) {
				List<String> list = this.packageToModules.get(qualifiedPackageName);
				if (list.contains(moduleName))
					return Collections.singletonList(moduleName);
			} else {
				if (module.equals(moduleName))
					return Collections.singletonList(moduleName);
			}
		}
		return null;
	}

	public String[] getModules(String fileName) {
		int idx = fileName.lastIndexOf('/');
		String pack = null;
		if (idx != -1) {
			pack = fileName.substring(0, idx);
		} else {
			pack = JRTUtil.DEFAULT_PACKAGE;
		}
		String module = this.packageToModule.get(pack);
		if (module != null) {
			if (module == JRTUtil.MULTIPLE) {
				List<String> list = this.packageToModules.get(pack);
				return list.toArray(new String[0]);
			} else {
				return new String[]{module};
			}
		}
		return JRTUtil.DEFAULT_MODULE;
	}

	public boolean hasClassFile(String qualifiedPackageName, String module) {
		if (module == null)
			return false;
		// easy checks first:
		String knownModule = this.packageToModule.get(qualifiedPackageName);
		if (knownModule == null || (knownModule != JRTUtil.MULTIPLE && !knownModule.equals(module)))
			return false;
		Path packagePath = this.fs.getPath(JRTUtil.MODULES_SUBDIR, module, qualifiedPackageName);
		if (!Files.exists(packagePath))
			return false;
		// iterate files:
		try {
			try (Stream<Path> list = Files.list(packagePath)) {
				return list.anyMatch(filePath -> filePath.toString().endsWith(SuffixConstants.SUFFIX_STRING_class)
						|| filePath.toString().endsWith(SuffixConstants.SUFFIX_STRING_CLASS));
			}
		} catch (IOException e) {
			return false;
		}
	}

	public InputStream getContentFromJrt(String fileName, String module) throws IOException {
		if (module != null) {
			byte[] fileBytes = getFileBytes(fileName, module);
			if(fileBytes == null) {
				return null;
			}
			return new ByteArrayInputStream(fileBytes);
		}
		String[] modules = getModules(fileName);
		for (String mod : modules) {
			byte[] fileBytes = getFileBytes(fileName, mod);
			if(fileBytes != null) {
				return new ByteArrayInputStream(fileBytes);
			}
		}
		return null;
	}

	private ClassFileReader getClassfile(String fileName, Predicate<String> moduleNameFilter) throws IOException, ClassFormatException {
		String[] modules = getModules(fileName);
		for (String mod : modules) {
			if (moduleNameFilter != null && !moduleNameFilter.test(mod)) {
				continue;
			}
			ClassFileReader reader = getClassfileFromModule(fileName, mod);
			if (reader != null) {
				return reader;
			}
		}
		return null;
	}

	byte[] getClassfileContent(String fileName, String module) throws IOException {
		byte[] content = null;
		if (module != null) {
			content = getFileBytes(fileName, module);
		} else {
			String[] modules = getModules(fileName);
			for (String mod : modules) {
				content = getFileBytes(fileName, mod);
				if (content != null) {
					break;
				}
			}
		}
		return content;
	}

	private byte[] getFileBytes(String fileName, String module) throws IOException {
		Path path = this.fs.getPath(JRTUtil.MODULES_SUBDIR, module, fileName);
		if(JRTUtil.DISABLE_CACHE) {
			return JRTUtil.safeReadBytes(path);
		} else {
			return JRTUtil.classCache.getClassBytes(this.jdk, path);
		}
	}

	ClassFileReader getClassfileFromModule(String fileName, String module) throws IOException, ClassFormatException {
		Path path = this.fs.getPath(JRTUtil.MODULES_SUBDIR, module, fileName);
		byte[] content = null;
		if(JRTUtil.DISABLE_CACHE) {
			content = JRTUtil.safeReadBytes(path);
		} else {
			content = JRTUtil.classCache.getClassBytes(this.jdk, path);
		}
		if (content != null) {
			ClassFileReader reader = new ClassFileReader(path.toUri(), content, fileName.toCharArray());
			reader.moduleName = module.toCharArray();
			return reader;
		} else {
			return null;
		}
	}
	public ClassFileReader getClassfile(String fileName, String module, Predicate<String> moduleNameFilter) throws IOException, ClassFormatException {
		ClassFileReader reader = null;
		if (module == null) {
			reader = getClassfile(fileName, moduleNameFilter);
		} else {
			reader = getClassfileFromModule(fileName, module);
		}
		return reader;
	}

	public ClassFileReader getClassfile(String fileName, String module) throws IOException, ClassFormatException {
		if (module == null) {
			return getClassfile(fileName, (Predicate<String>)null);
		} else {
			return getClassfileFromModule(fileName, module);
		}
	}

	void walkJrtForModules() throws IOException {
		Iterable<Path> roots = this.fs.getRootDirectories();
		for (Path path : roots) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
				for (final Path subdir: stream) {
					if (!subdir.toString().equals(JRTUtil.MODULES_SUBDIR)) {
						Files.walkFileTree(subdir, new SimpleFileVisitor<>() {
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
								// e.g. /modules/java.base
								Path relative = subdir.relativize(file);
								cachePackage(relative.getParent().toString(), relative.getFileName().toString());
								return FileVisitResult.CONTINUE;
							}
						});
					}
			    }
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}
		}
	}

	void walkModuleImage(final JRTUtil.JrtFileVisitor<Path> visitor, final int notify) throws IOException {
		Files.walkFileTree(this.modRoot, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				int count = dir.getNameCount();
				if (count == 1) return FileVisitResult.CONTINUE;
				if (count == 2) {
					// e.g. /modules/java.base
					Path mod = dir.getName(1);
					if ((JRTUtil.MODULE_TO_LOAD != null && JRTUtil.MODULE_TO_LOAD.length() > 0 &&
							JRTUtil.MODULE_TO_LOAD.indexOf(mod.toString()) == -1)) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					return ((notify & JRTUtil.NOTIFY_MODULES) == 0) ?
							FileVisitResult.CONTINUE : visitor.visitModule(dir, JRTUtil.sanitizedFileName(mod));
				}
				if ((notify & JRTUtil.NOTIFY_PACKAGES) == 0) {
					// We are dealing with a module or not client is not interested in packages
					return FileVisitResult.CONTINUE;
				}
				return visitor.visitPackage(dir.subpath(2, count), dir.getName(1), attrs);
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if ((notify & JRTUtil.NOTIFY_FILES) == 0)
					return FileVisitResult.CONTINUE;
				int count = file.getNameCount();
				// This happens when a file in a default package is present. E.g. /modules/some.module/file.name
				if (count == 3) {
					cachePackage(JRTUtil.DEFAULT_PACKAGE, file.getName(1).toString());
				}
				return visitor.visitFile(file.subpath(2, count), file.getName(1), attrs);
			}
		});
	}

	synchronized void cachePackage(String packageName, String module) {
		packageName = packageName.replace('.', '/');
		String currentModule = this.packageToModule.get(packageName);
		if (currentModule == null) {
			// Nothing found? Cache and return
			this.packageToModule.put(packageName.intern(), module.intern());
			return;
		}
		if(currentModule.equals(module)) {
			// Same module found? Just return
			return;
		}

		// We observe an additional module containing package
		if (currentModule == JRTUtil.MULTIPLE) {
			// We have already a list => update it
			List<String> list = this.packageToModules.get(packageName);
			if (!list.contains(module)) {
				if (JRTUtil.JAVA_BASE.equals(module)) {
					list.add(0, JRTUtil.JAVA_BASE);
				} else {
					list.add(module.intern());
				}
			}
		} else {
			// We found a second module => create a list
			List<String> list = new ArrayList<>();
			// Just do this as comparator might be overkill
			if (JRTUtil.JAVA_BASE == currentModule || JRTUtil.JAVA_BASE.equals(currentModule)) {
				list.add(currentModule.intern());
				list.add(module.intern());
			} else {
				list.add(module.intern());
				list.add(currentModule.intern());
			}
			packageName = packageName.intern();
			this.packageToModules.put(packageName, list);
			this.packageToModule.put(packageName, JRTUtil.MULTIPLE);
		}
	}

	/**
	 * @return JDK release string (something like <code>1.8.0_05</code>) read from the "release" file from JDK home
	 *         directory
	 */
	public String getJdkRelease() {
		return this.jdk.release;
	}
}