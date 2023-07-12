/*******************************************************************************
 * Copyright (c) 2015, 2022 IBM Corporation.
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
 *     Christoph Läubrich - adding helper for getting a JarFileSystem
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JRTUtil {

	public static final boolean DISABLE_CACHE = Boolean.getBoolean("org.eclipse.jdt.disable_JRT_cache"); //$NON-NLS-1$
	public static final boolean PROPAGATE_IO_ERRORS = Boolean.getBoolean("org.eclipse.jdt.propagate_io_errors"); //$NON-NLS-1$

	public static final String JAVA_BASE = "java.base".intern(); //$NON-NLS-1$
	public static final char[] JAVA_BASE_CHAR = JAVA_BASE.toCharArray();
	static final String MODULES_SUBDIR = "/modules"; //$NON-NLS-1$
	static final String[] DEFAULT_MODULE = new String[]{JAVA_BASE};
	static final String[] NO_MODULE = new String[0];
	static final String MULTIPLE = "MU"; //$NON-NLS-1$
	static final String DEFAULT_PACKAGE = ""; //$NON-NLS-1$
	static String MODULE_TO_LOAD;
	public static final String JRT_FS_JAR = "jrt-fs.jar"; //$NON-NLS-1$
	static URI JRT_URI = URI.create("jrt:/"); //$NON-NLS-1$
	public static final int NOTIFY_FILES = 0x0001;
	public static final int NOTIFY_PACKAGES = 0x0002;
	public static final int NOTIFY_MODULES = 0x0004;
	public static final int NOTIFY_ALL = NOTIFY_FILES | NOTIFY_PACKAGES | NOTIFY_MODULES;

	// TODO: Java 9 Think about clearing the cache too.
	private static Map<String, JrtFileSystem> images = new ConcurrentHashMap<>();

	/**
	 * Map from JDK home path to ct.sym file (located in /lib in the JDK)
	 */
	private static final Map<Path, CtSym> ctSymFiles = new ConcurrentHashMap<>();

	public interface JrtFileVisitor<T> {

		public default FileVisitResult visitPackage(T dir, T mod, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		public default FileVisitResult visitFile(T file, T mod, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		/**
		 * Invoked when a root directory of a module being visited. The element returned
		 * contains only the module name segment - e.g. "java.base". Clients can use this to control
		 * how the JRT needs to be processed, for e.g., clients can skip a particular module
		 * by returning FileVisitResult.SKIP_SUBTREE
		 */
		public default FileVisitResult visitModule(T path, String name) throws IOException  {
			return FileVisitResult.CONTINUE;
		}
	}

	public static abstract class AbstractFileVisitor<T> implements FileVisitor<T> {
		@Override
		public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}
	}

	/**
	 * @param image the path to the root of the JRE whose libraries we are interested in.
	 * @return may return {@code null}
	 * @deprecated use {@link JRTUtil#getJrtSystem(File, String)} instead
	 */
	public static JrtFileSystem getJrtSystem(File image) {
		try {
			return getJrtSystem(image, null);
		} catch (IOException e) {
			if(PROPAGATE_IO_ERRORS) {
				throw new IllegalStateException(e);
			}
			return null;
		}
	}

	/**
	 * @param image the path to the root of the JRE whose libraries we are interested in.
	 * @param release <code>--release</code> version
	 */
	public static JrtFileSystem getJrtSystem(File image, String release) throws IOException {
		String key = image.toString();
		Jdk jdk = new Jdk(image);

		if (release != null && !jdk.sameRelease(release)) {
			key = key + "|" + release; //$NON-NLS-1$
		}
		try {
			JrtFileSystem system = images.computeIfAbsent(key, x -> {
				try {
					return JrtFileSystem.getNewJrtFileSystem(jdk, release);
				} catch (IOException e) {
					// Needs better error handling downstream? But for now, make sure
					// a dummy JrtFileSystem is not created.
					String errorMessage = "Error: failed to create JrtFileSystem from " + image; //$NON-NLS-1$
					throw new RuntimeIOException(errorMessage, e);
				}
			});
			return system;
		} catch (RuntimeIOException e) {
				throw e.getCause();
		}
	}

	/**
	 * Convenient method to get access to the given archive as a {@link FileSystem}.
	 * <p>
	 * <b>Note:</b> if the file system for given archive was already created before, the method will reuse existing file
	 * system, otherwise a new {@link FileSystem} object will be created.
	 * <p>
	 * The caller should not close returned {@link FileSystem} as it might be shared with others.
	 *
	 * @param path
	 *            absolute file path to a jar archive
	 * @return never null
	 * @throws IOException
	 */
	public static FileSystem getJarFileSystem(Path path) throws IOException {
		URI uri = URI.create("jar:file:" + path.toUri().getRawPath()); //$NON-NLS-1$
		try {
			try {
				return FileSystems.getFileSystem(uri);
			} catch (FileSystemNotFoundException fne) {
				try {
					return FileSystems.newFileSystem(uri, Map.of(), ClassLoader.getSystemClassLoader());
				} catch (FileSystemAlreadyExistsException e) {
					return FileSystems.getFileSystem(uri);
				}
			}
		} catch (ProviderNotFoundException e) {
			throw new IOException("No provider for uri " + uri, e); //$NON-NLS-1$
		}
	}

	public static CtSym getCtSym(Path jdkHome) throws IOException {
		CtSym ctSym;
		try {
			ctSym = ctSymFiles.compute(jdkHome, (Path x, CtSym current) -> {
				if (current == null || !current.getFs().isOpen()) {
					try {
						return new CtSym(x);
					} catch (IOException e) {
						throw new RuntimeIOException(e);
					}
				}
				return current;
			});
		} catch (RuntimeIOException rio) {
			throw rio.getCause();
		}
		return ctSym;
	}

	/** TEST ONLY (use when changing the "modules.to.load" property). */
	public static void reset() {
		images.clear();
		MODULE_TO_LOAD = System.getProperty("modules.to.load"); //$NON-NLS-1$
	}

	/**
	 * Given the path of a modular image file, this method walks the archive content and
	 * notifies the supplied visitor about packages and files visited.
	 *
	 * The file system contains the following top level directories:
	 *  /modules/$MODULE/$PATH
	 *  /packages/$PACKAGE/$MODULE
	 *  The latter provides quick look up of the module that contains a particular package. However,
	 *  this method only notifies its clients of the entries within the modules (former) sub-directory.
	 *  Clients can decide which notifications they want to receive. See {@link JRTUtil#NOTIFY_ALL},
	 *  {@link JRTUtil#NOTIFY_FILES}, {@link JRTUtil#NOTIFY_PACKAGES} and {@link JRTUtil#NOTIFY_MODULES}.
	 *
	 * @param image a java.io.File handle to the JRT image.
	 * @param visitor an instance of JrtFileVisitor to be notified of the entries in the JRT image.
	 * @param notify flag indicating the notifications the client is interested in.
	 * @throws IOException
	 */
	public static void walkModuleImage(File image, final JRTUtil.JrtFileVisitor<java.nio.file.Path> visitor, int notify) throws IOException {
		JrtFileSystem system = getJrtSystem(image, null);
		if (system == null) {
			return;
		}
		system.walkModuleImage(visitor, notify);
	}

	public static void walkModuleImage(File image, String release, final JRTUtil.JrtFileVisitor<java.nio.file.Path> visitor, int notify) throws IOException {
		JrtFileSystem system = getJrtSystem(image, release);
		if (system == null) {
			return;
		}
		system.walkModuleImage(visitor, notify);
	}

	public static InputStream getContentFromJrt(File jrt, String fileName, String module) throws IOException {
		JrtFileSystem system = getJrtSystem(jrt, null);
		if (system == null) {
			throw new FileNotFoundException(String.valueOf(jrt));
		}
		return system.getContentFromJrt(fileName, module);
	}

	public static byte[] getClassfileContent(File jrt, String fileName, String module) throws IOException {
		JrtFileSystem system = getJrtSystem(jrt, null);
		if (system == null) {
			throw new FileNotFoundException(String.valueOf(jrt));
		}
		return system.getClassfileContent(fileName, module);
	}

	public static ClassFileReader getClassfile(File jrt, String fileName, String module) throws IOException, ClassFormatException {
		JrtFileSystem system = getJrtSystem(jrt, null);
		if (system == null) {
			throw new FileNotFoundException(String.valueOf(jrt));
		}
		return system.getClassfile(fileName, module);
	}

	public static ClassFileReader getClassfile(File jrt, String fileName, String module, Predicate<String> moduleNameFilter) throws IOException, ClassFormatException {
		JrtFileSystem system = getJrtSystem(jrt, null);
		if (system == null) {
			throw new FileNotFoundException(String.valueOf(jrt));
		}
		return system.getClassfile(fileName, module, moduleNameFilter);
	}

	public static List<String> getModulesDeclaringPackage(File jrt, String qName, String moduleName) {
		JrtFileSystem system = getJrtSystem(jrt);
		if (system == null) {
			return List.of();
		}
		return system.getModulesDeclaringPackage(qName, moduleName);
	}

	public static boolean hasCompilationUnit(File jrt, String qualifiedPackageName, String moduleName) {
		JrtFileSystem system = getJrtSystem(jrt);
		if (system == null) {
			return false;
		}
		return system.hasClassFile(qualifiedPackageName, moduleName);
	}

	/*
	 * Returns only the file name after removing trailing '/' if any for folders
	 */
	public static String sanitizedFileName(Path path) {
		String p = path.getFileName().toString();
		if (p.length() > 1 && p.charAt(p.length() - 1) == '/') {
			return p.substring(0, p.length() - 1);
		}
		return p;
	}

	/**
	 * Tries to read all bytes of the file denoted by path,
	 * returns null if the file could not be found or if the read was interrupted.
	 * @param path
	 * @return bytes or null
	 * @throws IOException any IO exception other than NoSuchFileException
	 */
	public static byte[] safeReadBytes(Path path) throws IOException {
		try {
			return Files.readAllBytes(path);
		} catch (ClosedByInterruptException e) {
			// retry once again
			try {
				return Files.readAllBytes(path);
			} catch (NoSuchFileException e2) {
				return null;
			} catch (ClosedByInterruptException e2) {
				if (PROPAGATE_IO_ERRORS) {
					throw e2;
				}
				return null;
			}
		} catch (NoSuchFileException e) {
			return null;
		}
	}

	/**
	 * @param image jrt file path
	 * @return JDK release corresponding to given jrt file, read from "release" file, if available. May return null.
	 */
	public static String getJdkRelease(File image) throws IOException {
		JrtFileSystem jrt = getJrtSystem(image, null);
		return jrt == null ? null : jrt.getJdkRelease();
	}
}

class JrtFileSystemWithOlderRelease extends JrtFileSystem {

	private List<Path> releaseRoots;
	private CtSym ctSym;

	/**
	 * The jrt file system is based on the location of the JRE home whose libraries
	 * need to be loaded.
	 *
	 * @param release the older release where classes and modules should be searched for.
	 * @throws IOException
	 */
	JrtFileSystemWithOlderRelease(Jdk jdkHome, String release) throws IOException {
		super(jdkHome, release);
		String releaseCode = CtSym.getReleaseCode(this.release);
		this.ctSym = JRTUtil.getCtSym(Paths.get(this.jdk.path));
		this.fs = this.ctSym.getFs();
		if (!Files.exists(this.fs.getPath(releaseCode))
				|| Files.exists(this.fs.getPath(releaseCode, "system-modules"))) { //$NON-NLS-1$
			this.fs = null;
		}
		this.releaseRoots = this.ctSym.releaseRoots(releaseCode);
	}

	@Override
	void walkModuleImage(final JRTUtil.JrtFileVisitor<java.nio.file.Path> visitor, final int notify) throws IOException {
		for (Path p : this.releaseRoots) {
			Files.walkFileTree(p, new JRTUtil.AbstractFileVisitor<java.nio.file.Path>() {
				@Override
				public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs)
						throws IOException {
					int count = dir.getNameCount();
					if (count == 1) {
						return FileVisitResult.CONTINUE;
					}
					if (count == 2) {
						// e.g. /9A/java.base
						java.nio.file.Path mod = dir.getName(1);
						if ((JRTUtil.MODULE_TO_LOAD != null && JRTUtil.MODULE_TO_LOAD.length() > 0
								&& JRTUtil.MODULE_TO_LOAD.indexOf(mod.toString()) == -1)) {
							return FileVisitResult.SKIP_SUBTREE;
						}
						return ((notify & JRTUtil.NOTIFY_MODULES) == 0) ? FileVisitResult.CONTINUE
								: visitor.visitModule(dir, JRTUtil.sanitizedFileName(mod));
					}
					if ((notify & JRTUtil.NOTIFY_PACKAGES) == 0) {
						// client is not interested in packages
						return FileVisitResult.CONTINUE;
					}
					return visitor.visitPackage(dir.subpath(2, count), dir.getName(1), attrs);
				}

				@Override
				public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
						throws IOException {
					if ((notify & JRTUtil.NOTIFY_FILES) == 0) {
						return FileVisitResult.CONTINUE;
					}
					// This happens when a file in a default package is present. E.g. /modules/some.module/file.name
					if (file.getNameCount() == 3) {
						cachePackage(JRTUtil.DEFAULT_PACKAGE, file.getName(1).toString());
					}
					return visitor.visitFile(file.subpath(2, file.getNameCount()), file.getName(1), attrs);
				}
			});
		}
	}

}

final class RuntimeIOException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RuntimeIOException(String message, IOException cause) {
		super(message, cause);
	}

	public RuntimeIOException(IOException cause) {
		super(cause);
	}

	@Override
	public synchronized IOException getCause() {
		return (IOException) super.getCause();
	}
}

class Jdk {
	final String path;
	final String release;
	static final Map<String, String> pathToRelease = new ConcurrentHashMap<>();

	public Jdk(File jrt) throws IOException {
		this.path = toJdkHome(jrt);
		try {
			String rel = pathToRelease.computeIfAbsent(this.path, key -> {
				try {
					return readJdkReleaseFile(this.path);
				} catch (IOException e) {
					throw new RuntimeIOException(e);
				}
			});
			this.release = rel;
		} catch (RuntimeIOException rio) {
			throw rio.getCause();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Jdk ["); //$NON-NLS-1$
		if (this.path != null) {
			builder.append("path="); //$NON-NLS-1$
			builder.append(this.path);
			builder.append(", "); //$NON-NLS-1$
		}
		if (this.release != null) {
			builder.append("release="); //$NON-NLS-1$
			builder.append(this.release);
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}

	boolean sameRelease(String other) {
		long jdkLevel = CompilerOptions.versionToJdkLevel(this.release);
		long otherJdkLevel = CompilerOptions.versionToJdkLevel(other);
		return Long.compare(jdkLevel, otherJdkLevel) == 0;
	}

	static String toJdkHome(File jrt) {
		String home;
		Path normalized = jrt.toPath().normalize();
		if (jrt.getName().equals(JRTUtil.JRT_FS_JAR)) {
			home = normalized.getParent().getParent().toString();
		} else {
			home = normalized.toString();
		}
		return home;
	}

	static String readJdkReleaseFile(String javaHome) throws IOException {
		Properties properties = new Properties();
		try(FileReader reader = new FileReader(new File(javaHome, "release"))){ //$NON-NLS-1$
			properties.load(reader);
		}
		// Something like JAVA_VERSION="1.8.0_05"
		String ver = properties.getProperty("JAVA_VERSION"); //$NON-NLS-1$
		if (ver != null) {
			ver = ver.replace("\"", "");  //$NON-NLS-1$//$NON-NLS-2$
		}
		return ver;
	}
}

class JrtFileSystem {

	private final Map<String, String> packageToModule = new HashMap<String, String>();

	private final Map<String, List<String>> packageToModules = new HashMap<String, List<String>>();


	private final Map<Path, Optional<byte[]>> classCache = new ConcurrentHashMap<>(10007);

	FileSystem fs;
	Path modRoot;
	Jdk jdk;
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
	 *
	 * @param jdkHome
	 * @throws IOException
	 */
	JrtFileSystem(Jdk jdkHome, String release) throws IOException {
		this.jdk = jdkHome;
		this.release = release;
		JRTUtil.MODULE_TO_LOAD = System.getProperty("modules.to.load"); //$NON-NLS-1$
		HashMap<String, String> env = new HashMap<>();
		env.put("java.home", this.jdk.path); //$NON-NLS-1$
		this.fs = FileSystems.newFileSystem(JRTUtil.JRT_URI, env);
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
			return Files.list(packagePath)
				.anyMatch(filePath -> filePath.toString().endsWith(SuffixConstants.SUFFIX_STRING_class)
										|| filePath.toString().endsWith(SuffixConstants.SUFFIX_STRING_CLASS));
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
			try {
				Optional<byte[]> bytes = this.classCache.computeIfAbsent(path, key -> {
					try {
						return Optional.ofNullable(JRTUtil.safeReadBytes(key));
					} catch (IOException e) {
						throw new RuntimeIOException(e);
					}
				});
				return bytes.orElse(null);
			} catch (RuntimeIOException rio) {
				throw rio.getCause();
			}
		}
	}

	ClassFileReader getClassfileFromModule(String fileName, String module) throws IOException, ClassFormatException {
		Path path = this.fs.getPath(JRTUtil.MODULES_SUBDIR, module, fileName);
		byte[] content = null;
		if(JRTUtil.DISABLE_CACHE) {
			content = JRTUtil.safeReadBytes(path);
		} else {
			try {
				Optional<byte[]> bytes = this.classCache.computeIfAbsent(path, key -> {
					try {
						return Optional.ofNullable(JRTUtil.safeReadBytes(key));
					} catch (IOException e) {
						throw new RuntimeIOException(e);
					}
				});
				content = bytes.orElse(null);
			} catch (RuntimeIOException rio) {
				throw rio.getCause();
			}
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
		Iterable<java.nio.file.Path> roots = this.fs.getRootDirectories();
		for (java.nio.file.Path path : roots) {
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(path)) {
				for (final java.nio.file.Path subdir: stream) {
					if (!subdir.toString().equals(JRTUtil.MODULES_SUBDIR)) {
						Files.walkFileTree(subdir, new JRTUtil.AbstractFileVisitor<java.nio.file.Path>() {
							@Override
							public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
								// e.g. /modules/java.base
								java.nio.file.Path relative = subdir.relativize(file);
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

	void walkModuleImage(final JRTUtil.JrtFileVisitor<java.nio.file.Path> visitor, final int notify) throws IOException {
		Files.walkFileTree(this.modRoot, new JRTUtil.AbstractFileVisitor<java.nio.file.Path>() {
			@Override
			public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs) throws IOException {
				int count = dir.getNameCount();
				if (count == 1) return FileVisitResult.CONTINUE;
				if (count == 2) {
					// e.g. /modules/java.base
					java.nio.file.Path mod = dir.getName(1);
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
			public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
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
			List<String> list = new ArrayList<String>();
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
	 * @return JDK release string (something like <code>1.8.0_05<code>) read from the "release" file from JDK home
	 *         directory
	 */
	public String getJdkRelease() {
		return this.jdk.release;
	}
}