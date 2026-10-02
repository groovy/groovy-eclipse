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
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implements a soft cache for reading class files from disk, as these caches can grow quite large but data can be
 * recovered afterwards we only hold a soft reference to the bytes itself.
 */
class SoftClassCache {

	private final ConcurrentMap<Path, JdkClasses> jdks = new ConcurrentHashMap<>();

	void clear() {
		this.jdks.clear();
	}

	public byte[] getClassBytes(Jdk jdk, Path path) throws IOException {
		return this.jdks.computeIfAbsent(jdk.path, JdkClasses::new).get(path);
	}

	private static final class JdkClasses {
		private final ConcurrentMap<Path, ClassBytes> classes = new ConcurrentHashMap<>(10007);
		private final Path jdkPath;

		public JdkClasses(Path jdkPath) {
			this.jdkPath = jdkPath;
		}

		public byte[] get(Path path) throws IOException {
			return this.classes.computeIfAbsent(path, ClassBytes::new).getBytes();
		}

		@Override
		public String toString() {
			return "Class Cache for " + this.jdkPath; //$NON-NLS-1$
		}
	}

	private static final class ClassBytes {
		private final Path path;
		private volatile boolean empty;
		private volatile SoftReference<byte[]> bytes;

		public ClassBytes(Path path) {
			this.path = path;
		}

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
