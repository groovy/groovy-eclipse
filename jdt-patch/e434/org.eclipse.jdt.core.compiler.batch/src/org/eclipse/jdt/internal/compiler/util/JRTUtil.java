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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JRTUtil {

	public static final boolean DISABLE_CACHE = Boolean.getBoolean("org.eclipse.jdt.disable_JRT_cache"); //$NON-NLS-1$
	public static final boolean PROPAGATE_IO_ERRORS = Boolean.getBoolean("org.eclipse.jdt.propagate_io_errors"); //$NON-NLS-1$

	public static final String JAVA_BASE = "java.base"; //$NON-NLS-1$
	public static final char[] JAVA_BASE_CHAR = JAVA_BASE.toCharArray();
	public static final String MODULES_SUBDIR = "/modules"; //$NON-NLS-1$
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
	private static final Map<Path, FileSystem> JRT_FILE_SYSTEMS = new ConcurrentHashMap<>();

	static final SoftClassCache classCache = new SoftClassCache();

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
		Jdk jdk = new Jdk(image);
		String key = jdk.path.toString();
		if (release != null && !jdk.sameRelease(release)) {
			key += "|" + release; //$NON-NLS-1$
		}
		try {
			JrtFileSystem system = images.computeIfAbsent(key, x -> {
				try {
					return JrtFileSystem.getNewJrtFileSystem(jdk, release);
				} catch (IOException e) {
					// Needs better error handling downstream? But for now, make sure
					// a dummy JrtFileSystem is not created.
					String errorMessage = "Error: failed to create JrtFileSystem from " + image; //$NON-NLS-1$
					throw new UncheckedIOException(errorMessage, e);
				}
			});
			return system;
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	/**
	 * @param path
	 *            absolute path to java.home
	 * @return new {@link FileSystem} based on {@link #JRT_FS_JAR} for given Java home path.
	 * @throws IOException
	 *             on any error
	 */
	public static FileSystem getJrtFileSystem(Path path) throws IOException {
		try {
			FileSystem fs = JRT_FILE_SYSTEMS.computeIfAbsent(path.toAbsolutePath().normalize(), p -> {
				try {
					return FileSystems.newFileSystem(JRTUtil.JRT_URI, Map.of("java.home", p.toString())); //$NON-NLS-1$
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			return fs;
		} catch (UncheckedIOException e) {
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

	@SuppressWarnings("resource") // getFs() does not transfer ownership
	public static CtSym getCtSym(Path jdkHome) throws IOException {
		CtSym ctSym;
		try {
			ctSym = ctSymFiles.compute(jdkHome.toAbsolutePath().normalize(), (Path x, CtSym current) -> {
				if (current == null || !current.getFs().isOpen()) {
					try {
						return new CtSym(x);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
				return current;
			});
		} catch (UncheckedIOException rio) {
			throw rio.getCause();
		}
		return ctSym;
	}

	/** TEST ONLY (use when changing the "modules.to.load" property). */
	public static void reset() {
		images.clear();
		classCache.clear();
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
	 */
	public static void walkModuleImage(File image, JRTUtil.JrtFileVisitor<Path> visitor, int notify) throws IOException {
		walkModuleImage(getJrtSystem(image, null), visitor, notify);
	}

	public static void walkModuleImage(JrtFileSystem system, JRTUtil.JrtFileVisitor<Path> visitor, int notify) throws IOException {
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
			throw new FileNotFoundException(String.valueOf(system));
		}
		return getClassfileContent(system,fileName, module);
	}

	public static byte[] getClassfileContent(JrtFileSystem system, String fileName, String module)
			throws FileNotFoundException, IOException {
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
		JrtFileSystem jrtSystem = getJrtSystem(jrt, null);
		if (jrtSystem == null) {
			throw new FileNotFoundException(String.valueOf(jrt));
		}
		return getClassfile(jrtSystem, fileName, module, moduleNameFilter);
	}

	public static ClassFileReader getClassfile(JrtFileSystem system, String fileName, String module,
			Predicate<String> moduleNameFilter)
			throws FileNotFoundException, IOException, ClassFormatException {
		return system.getClassfile(fileName, module, moduleNameFilter);
	}

	public static List<String> getModulesDeclaringPackage(File jrt, String qName, String moduleName) {
		return getModulesDeclaringPackage(getJrtSystem(jrt), qName, moduleName);
	}

	public static List<String> getModulesDeclaringPackage(JrtFileSystem system, String qName, String moduleName) {
		if (system == null) {
			return List.of();
		}
		return system.getModulesDeclaringPackage(qName, moduleName);
	}

	public static boolean hasCompilationUnit(File jrt, String qualifiedPackageName, String moduleName) {
		JrtFileSystem system = getJrtSystem(jrt);
		return hasCompilationUnit(system, qualifiedPackageName, moduleName);
	}

	public static boolean hasCompilationUnit(JrtFileSystem system, String qualifiedPackageName, String moduleName) {
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

	private final List<Path> releaseRoots;
	private final CtSym ctSym;

	/**
	 * The jrt file system is based on the location of the JRE home whose libraries
	 * need to be loaded.
	 *
	 * @param release the older release where classes and modules should be searched for.
	 */
	JrtFileSystemWithOlderRelease(Jdk jdkHome, String release) throws IOException {
		super(jdkHome, release);
		String releaseCode = CtSym.getReleaseCode(this.release);
		this.ctSym = JRTUtil.getCtSym(this.jdk.path);
		this.fs = this.ctSym.getFs();
		if (!Files.exists(this.fs.getPath(releaseCode))
				|| Files.exists(this.fs.getPath(releaseCode, "system-modules"))) { //$NON-NLS-1$
			this.fs = null;
		}
		this.releaseRoots = this.ctSym.releaseRoots(releaseCode);
	}

	@Override
	void walkModuleImage(final JRTUtil.JrtFileVisitor<Path> visitor, final int notify) throws IOException {
		for (Path p : this.releaseRoots) {
			Files.walkFileTree(p, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
						throws IOException {
					int count = dir.getNameCount();
					if (count == 1) {
						return FileVisitResult.CONTINUE;
					}
					if (count == 2) {
						// e.g. /9A/java.base
						Path mod = dir.getName(1);
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
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
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

class Jdk {
	final Path path;
	final String release;
	private static final Map<Path, String> pathToRelease = new ConcurrentHashMap<>();

	public Jdk(File jrt) throws IOException {
		this.path = toJdkHome(jrt);
		try {
			this.release = pathToRelease.computeIfAbsent(this.path, p -> {
				try {
					return readJdkReleaseFile(p);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} catch (UncheckedIOException rio) {
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

	static Path toJdkHome(File jrt) {
		Path home;
		Path normalized = jrt.toPath().toAbsolutePath().normalize();
		if (jrt.getName().equals(JRTUtil.JRT_FS_JAR)) {
			home = normalized.getParent().getParent();
		} else {
			home = normalized;
		}
		return home;
	}

	static String readJdkReleaseFile(Path javaHome) throws IOException {
		Properties properties = new Properties();
		try (InputStream in = Files.newInputStream(javaHome.resolve("release"))) { //$NON-NLS-1$
			properties.load(in);
		}
		// Something like JAVA_VERSION="1.8.0_05"
		String ver = properties.getProperty("JAVA_VERSION"); //$NON-NLS-1$
		if (ver != null) {
			ver = ver.replace("\"", "");  //$NON-NLS-1$//$NON-NLS-2$
		}
		return ver;
	}
}
