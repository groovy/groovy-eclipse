/*******************************************************************************
 * Copyright (c) 2023 Christoph Läubrich.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.util;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implements a soft cache for reading class files from disk, as these caches can grow quite large but data can be
 * recovered afterwards we only hold a soft reference to the bytes itself.
 * <p>
 * Entries are keyed by file system, module and file name rather than by {@link Path}, so that a lookup answered from
 * the cache does not have to create a path in the file system first.
 */
class SoftClassCache {

	private final ConcurrentMap<FileSystem, FileSystemClasses> fileSystems = new ConcurrentHashMap<>();

	void clear() {
		this.fileSystems.clear();
	}

	/**
	 * Answers the cache entry for the given class file of the given module in the given file system.
	 * <p>
	 * Entries are grouped by file system and not by JDK home: the JRT file system of a JDK and the file system of its
	 * <code>ct.sym</code> (used with <code>--release</code>) must not share entries.
	 *
	 * @param fs
	 *            the file system the class file is read from, must not be <code>null</code>
	 * @param jdkPath
	 *            the home of the JDK the file system belongs to, only used to describe the cache
	 * @param module
	 *            the name of the module
	 * @param fileName
	 *            the name of the class file relative to the module root, like {@code java/lang/Object.class}
	 * @return the cache entry, never <code>null</code>
	 */
	ClassBytes getClassBytes(FileSystem fs, Path jdkPath, String module, String fileName) {
		// get() first: computeIfAbsent() may lock even if the key is present
		FileSystemClasses classes = this.fileSystems.get(fs);
		if (classes == null) {
			classes = this.fileSystems.computeIfAbsent(fs, f -> new FileSystemClasses(f, jdkPath));
		}
		return classes.get(module, fileName);
	}

	private static final class FileSystemClasses {
		private final ConcurrentMap<String, ConcurrentMap<String, ClassBytes>> modules = new ConcurrentHashMap<>();
		private final FileSystem fs;
		private final Path jdkPath;

		public FileSystemClasses(FileSystem fs, Path jdkPath) {
			this.fs = fs;
			this.jdkPath = jdkPath;
		}

		public ClassBytes get(String module, String fileName) {
			ConcurrentMap<String, ClassBytes> classes = this.modules.get(module);
			if (classes == null) {
				classes = this.modules.computeIfAbsent(module, m -> new ConcurrentHashMap<>());
			}
			ClassBytes classBytes = classes.get(fileName);
			if (classBytes == null) {
				// a new entry is read right away, so its path is needed anyway
				classBytes = classes.computeIfAbsent(fileName,
						f -> new ClassBytes(this.fs.getPath(JRTUtil.MODULES_SUBDIR, module, f)));
			}
			return classBytes;
		}

		@Override
		public String toString() {
			return "Class Cache for " + this.jdkPath + " (" + this.fs + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	static final class ClassBytes {
		private final Path path;
		private volatile URI uri;
		private volatile boolean empty;
		private volatile SoftReference<byte[]> bytes;

		ClassBytes(Path path) {
			this.path = path;
		}

		/**
		 * @return the URI of the class file, created on first use
		 */
		public URI getUri() {
			URI u = this.uri;
			if (u == null) {
				this.uri = u = this.path.toUri();
			}
			return u;
		}

		/**
		 * @return the content of the class file, or <code>null</code> if it does not exist
		 */
		public byte[] getBytes() throws IOException {
			if (this.empty) {
				return null;
			}
			SoftReference<byte[]> reference = this.bytes;
			if (reference != null) {
				byte[] bs = reference.get();
				if (bs != null) {
					return bs;
				}
			}
			byte[] readBytes = JRTUtil.safeReadBytes(this.path);
			if (readBytes == null) {
				this.empty = true;
				return null;
			}
			this.bytes = new SoftReference<>(readBytes);
			return readBytes;
		}
	}

}
