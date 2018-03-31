/*******************************************************************************
 * Copyright (c) 2015, 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IModule;

public class JRTUtil {

	public static final String JAVA_BASE = "java.base"; //$NON-NLS-1$
	public static final char[] JAVA_BASE_CHAR = JAVA_BASE.toCharArray();
	static final String MODULES_SUBDIR = "/modules"; //$NON-NLS-1$
	static final String[] DEFAULT_MODULE = new String[]{JAVA_BASE};
	static final String[] NO_MODULE = new String[0];
	static final String MULTIPLE = "MU"; //$NON-NLS-1$
	static final String DEFAULT_PACKAGE = ""; //$NON-NLS-1$
	static String MODULE_TO_LOAD = null;
	public static final String JRT_FS_JAR = "jrt-fs.jar"; //$NON-NLS-1$
	static URI JRT_URI = URI.create("jrt:/"); //$NON-NLS-1$
	public static int NOTIFY_FILES = 0x0001;
	public static int NOTIFY_PACKAGES = 0x0002;
	public static int NOTIFY_MODULES = 0x0004;
	public static int NOTIFY_ALL = NOTIFY_FILES | NOTIFY_PACKAGES | NOTIFY_MODULES;

	// TODO: Java 9 Think about clearing the cache too.
	private static Map<File, JrtFileSystem> images = null;

	private static final Object lock = new Object();

	public interface JrtFileVisitor<T> {

		public FileVisitResult visitPackage(T dir, T mod, BasicFileAttributes attrs) throws IOException;

		public FileVisitResult visitFile(T file, T mod, BasicFileAttributes attrs) throws IOException;
		/**
		 * Invoked when a root directory of a module being visited. The element returned 
		 * contains only the module name segment - e.g. "java.base". Clients can use this to control
		 * how the JRT needs to be processed, for e.g., clients can skip a particular module
		 * by returning FileVisitResult.SKIP_SUBTREE
		 */
		public FileVisitResult visitModule(T mod) throws IOException;
	}

	static abstract class AbstractFileVisitor<T> implements FileVisitor<T> {
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

	public static JrtFileSystem getJrtSystem(File image) {
		Map<File, JrtFileSystem> i = images;
		if (images == null) {
			synchronized (lock) {
	            i = images;
	            if (i == null) {
	            	images = i = new HashMap<>();
	            }
	        }
		}
		JrtFileSystem system = null;
		synchronized(i) {
			if ((system = images.get(image)) == null) {
				try {
					images.put(image, system = new JrtFileSystem(image));
				} catch (IOException e) {
					e.printStackTrace();
					// Needs better error handling downstream? But for now, make sure 
					// a dummy JrtFileSystem is not created.
				}
			}
		}
	    return system;
	}

	/** TEST ONLY (use when changing the "modules.to.load" property). */
	public static void reset() {
		images = null;
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
	 *  this method only notifies its clients of the entries within the modules sub-directory. The
	 *  clients can decide which notifications they want to receive. See {@link JRTUtil#NOTIFY_ALL},
	 *  {@link JRTUtil#NOTIFY_FILES}, {@link JRTUtil#NOTIFY_PACKAGES} and {@link JRTUtil#NOTIFY_MODULES}.
	 *
	 * @param image a java.io.File handle to the JRT image.
	 * @param visitor an instance of JrtFileVisitor to be notified of the entries in the JRT image.
	 * @param notify flag indicating the notifications the client is interested in.
	 * @throws IOException
	 */
	public static void walkModuleImage(File image, final JRTUtil.JrtFileVisitor<java.nio.file.Path> visitor, int notify) throws IOException {
		getJrtSystem(image).walkModuleImage(visitor, false, notify);
	}

	public static InputStream getContentFromJrt(File jrt, String fileName, String module) throws IOException {
		return getJrtSystem(jrt).getContentFromJrt(fileName, module);
	}
	public static byte[] getClassfileContent(File jrt, String fileName, String module) throws IOException, ClassFormatException {
		return getJrtSystem(jrt).getClassfileContent(fileName, module);
	}
	public static ClassFileReader getClassfile(File jrt, String fileName, IModule module) throws IOException, ClassFormatException {
		return getJrtSystem(jrt).getClassfile(fileName, module);
	}
	public static ClassFileReader getClassfile(File jrt, String fileName, String module) throws IOException, ClassFormatException {
		return getJrtSystem(jrt).getClassfile(fileName, module);
	}
	public static List<String> getModulesDeclaringPackage(File jrt, String qName, String moduleName) {
		return getJrtSystem(jrt).getModulesDeclaringPackage(qName, moduleName);
	}

	public static boolean hasCompilationUnit(File jrt, String qualifiedPackageName, String moduleName) {
		return getJrtSystem(jrt).hasClassFile(qualifiedPackageName, moduleName);
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
		} catch(ClosedByInterruptException e) {
			return null;
		} catch (NoSuchFileException e) {
			return null;
		}
	}
}
class JrtFileSystem {
	private final Map<String, String> packageToModule = new HashMap<String, String>();

	private final Map<String, List<String>> packageToModules = new HashMap<String, List<String>>();

	FileSystem jrtSystem = null;
	
	/**
	 * The jrt file system is based on the location of the JRE home whose libraries
	 * need to be loaded.
	 *
	 * @param jrt the path to the root of the JRE whose libraries we are interested in.
	 * @throws IOException 
	 */
	public JrtFileSystem(File jrt) throws IOException {
		initialize(jrt);
	}
	void initialize(File jrt) throws IOException {
		URL jrtPath = null;
		String jdkHome = null;
		if (jrt.toString().endsWith(JRTUtil.JRT_FS_JAR)) {
			jrtPath = jrt.toPath().toUri().toURL();
			jdkHome = jrt.getParentFile().getParent();
		} else {
			jdkHome = jrt.toPath().toString();
			jrtPath = Paths.get(jdkHome, "lib", JRTUtil.JRT_FS_JAR).toUri().toURL(); //$NON-NLS-1$

		}
		JRTUtil.MODULE_TO_LOAD = System.getProperty("modules.to.load"); //$NON-NLS-1$
		String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$
		if (javaVersion != null && javaVersion.startsWith("1.8")) { //$NON-NLS-1$
			JRTUtil.MODULE_TO_LOAD = System.getProperty("modules.to.load"); //$NON-NLS-1$
			URLClassLoader loader = new URLClassLoader(new URL[] { jrtPath });
			HashMap<String, ?> env = new HashMap<>();
			this.jrtSystem = FileSystems.newFileSystem(JRTUtil.JRT_URI, env, loader);
		} else {
			HashMap<String, String> env = new HashMap<>();
			env.put("java.home", jdkHome); //$NON-NLS-1$
			this.jrtSystem = FileSystems.newFileSystem(JRTUtil.JRT_URI, env);
		}
		walkModuleImage(null, true, 0 /* doesn't matter */);
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
				return list.toArray(new String[list.size()]);
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
		Path packagePath = this.jrtSystem.getPath(JRTUtil.MODULES_SUBDIR, module, qualifiedPackageName);
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
			return Files.newInputStream(this.jrtSystem.getPath(JRTUtil.MODULES_SUBDIR, module, fileName));
		}
		String[] modules = getModules(fileName);
		for (String mod : modules) {
			return Files.newInputStream(this.jrtSystem.getPath(JRTUtil.MODULES_SUBDIR, mod, fileName));
		}
		return null;
	}
	private ClassFileReader getClassfile(String fileName) throws IOException, ClassFormatException {
		String[] modules = getModules(fileName);
		byte[] content = null;
		String module = null;
		for (String mod : modules) {
			content = JRTUtil.safeReadBytes(this.jrtSystem.getPath(JRTUtil.MODULES_SUBDIR, mod, fileName));
			if (content != null) {
				module = mod;
				break;
			}
		}
		if (content != null) {
			ClassFileReader reader = new ClassFileReader(content, fileName.toCharArray());
			reader.moduleName = module.toCharArray();
			return reader;
		}
		return null;
	}

	byte[] getClassfileContent(String fileName, String module) throws IOException, ClassFormatException {
		byte[] content = null;
		if (module != null) {
			content = getClassfileBytes(fileName, new String(module.toCharArray()));
		} else {
			String[] modules = getModules(fileName);
			for (String mod : modules) {
				content = JRTUtil.safeReadBytes(this.jrtSystem.getPath(JRTUtil.MODULES_SUBDIR, mod, fileName));
				if (content != null) {
					break;
				}
			}
		}
		return content;
	}
	private byte[] getClassfileBytes(String fileName, String module) throws IOException, ClassFormatException {
		return JRTUtil.safeReadBytes(this.jrtSystem.getPath(JRTUtil.MODULES_SUBDIR, module, fileName));
	}
	public ClassFileReader getClassfile(String fileName, String module) throws IOException, ClassFormatException {
		ClassFileReader reader = null;
		if (module == null) {
			reader = getClassfile(fileName);
		} else {
			byte[] content = getClassfileBytes(fileName, module);
			if (content != null) {
				reader = new ClassFileReader(content, fileName.toCharArray());
				reader.moduleName = module.toCharArray();
			}
		}
		return reader;
	}
	public ClassFileReader getClassfile(String fileName, IModule module) throws IOException, ClassFormatException {
		ClassFileReader reader = null;
		if (module == null) {
			reader = getClassfile(fileName);
		} else {
			byte[] content = getClassfileBytes(fileName, new String(module.name()));
			if (content != null) {
				reader = new ClassFileReader(content, fileName.toCharArray());
			}
		}
		return reader;
	}

	void walkModuleImage(final JRTUtil.JrtFileVisitor<java.nio.file.Path> visitor, boolean visitPackageMapping, final int notify) throws IOException {
		Iterable<java.nio.file.Path> roots = this.jrtSystem.getRootDirectories();
		for (java.nio.file.Path path : roots) {
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(path)) {
				for (final java.nio.file.Path subdir: stream) {
					if (subdir.toString().equals(JRTUtil.MODULES_SUBDIR)) {
						if (visitPackageMapping) continue;
						Files.walkFileTree(subdir, new JRTUtil.AbstractFileVisitor<java.nio.file.Path>() {
							@Override
							public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs) throws IOException {
								int count = dir.getNameCount();
								if (count == 2) {
									// e.g. /modules/java.base
									java.nio.file.Path mod = dir.getName(1);
									if ((JRTUtil.MODULE_TO_LOAD != null && JRTUtil.MODULE_TO_LOAD.length() > 0 &&
											JRTUtil.MODULE_TO_LOAD.indexOf(mod.toString()) == -1)) {
										return FileVisitResult.SKIP_SUBTREE;
									}
									return ((notify & JRTUtil.NOTIFY_MODULES) == 0) ? 
											FileVisitResult.CONTINUE : visitor.visitModule(mod);
								}
								if (dir == subdir || count < 3 || (notify & JRTUtil.NOTIFY_PACKAGES) == 0) {
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
								return visitor.visitFile(file.subpath(2, file.getNameCount()), file.getName(1), attrs);
							}
						});
					} else if (visitPackageMapping) {
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
				throw new IOException(e.getMessage());
			}
		}
	}

	void cachePackage(String packageName, String module) {
		packageName = packageName.intern();
		module = module.intern();
		packageName = packageName.replace('.', '/');
		Object current = this.packageToModule.get(packageName);
		if (current == null) {
			this.packageToModule.put(packageName, module);
		} else if(current == module || current.equals(module)) {
			return;
		} else if (current == JRTUtil.MULTIPLE) {
			List<String> list = this.packageToModules.get(packageName);
			if (!list.contains(module)) {
				if (JRTUtil.JAVA_BASE == module || JRTUtil.JAVA_BASE.equals(module)) {
					list.add(0, JRTUtil.JAVA_BASE);
				} else {
					list.add(module);
				}
			}
		} else {
			String first = (String) current;
			this.packageToModule.put(packageName, JRTUtil.MULTIPLE);
			List<String> list = new ArrayList<String>();
			// Just do this as comparator might be overkill
			if (JRTUtil.JAVA_BASE == current || JRTUtil.JAVA_BASE.equals(current)) {
				list.add(first);
				list.add(module);
			} else {
				list.add(module);
				list.add(first);
			}
			this.packageToModules.put(packageName, list);
		}
	}
}