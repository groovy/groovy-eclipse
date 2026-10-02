/*******************************************************************************
 * Copyright (c) 2026 Andrey Loskutov, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.util;

import java.io.File;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.junit.Test;

/**
 * Tests for {@link Util#archiveFormat(String)} and
 * {@link Util#isPotentialZipArchive(String)}, in particular the exclusion of
 * native libraries (.so, .dll, .dylib), see
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5253
 */
public class UtilTest extends TestCase {

	private static final int NO_ARCHIVE = -1;

	public UtilTest(String name) {
		super(name);
	}

	private static void assertArchiveFormat(int expected, String name) {
		assertEquals("Unexpected archive format for: " + name, expected, Util.archiveFormat(name));
	}

	private static void assertPotentialZipArchive(boolean expected, String name) {
		assertEquals("Unexpected zip archive classification for: " + name, expected,
				Util.isPotentialZipArchive(name));
	}

	private static void assertNativeLibrary(boolean expected, String name) {
		assertEquals("Unexpected native library classification for: " + name, expected,
				Util.isNativeLibrary(name));
	}

	/** Asserts that the given name is neither an archive nor a potential zip archive. */
	private static void assertNotAnArchive(String name) {
		assertArchiveFormat(NO_ARCHIVE, name);
		assertPotentialZipArchive(false, name);
	}

	/** Asserts that the given name is a plain zip archive candidate. */
	private static void assertZipArchive(String name) {
		assertArchiveFormat(Util.ZIP_FILE, name);
		assertPotentialZipArchive(true, name);
	}

	@Test
	public void testArchives() {
		assertZipArchive("foo.jar");
		assertZipArchive("foo.zip");
		assertZipArchive("jrt-fs.jar");
		assertArchiveFormat(Util.JMOD_FILE, "java.base.jmod");
		assertPotentialZipArchive(true, "java.base.jmod");
	}

	@Test
	public void testSourceAndClassFiles() {
		assertNotAnArchive("Foo.java");
		assertNotAnArchive("Foo.JAVA");
		assertNotAnArchive("Foo.class");
		assertNotAnArchive("Foo.CLASS");
	}

	@Test
	public void testNoExtension() {
		assertNotAnArchive("foo");
		assertNotAnArchive("");
	}

	/**
	 * Names without any dot have no extension at all and must never be
	 * classified as native libraries, even if the name itself equals a native
	 * library extension. Such names reach
	 * {@link Util#isNativeLibrary(String)} via the bootclasspath handling in
	 * {@code Util#collectPlatformLibraries(java.io.File)}, where entries may be
	 * plain directories.
	 */
	@Test
	public void testNativeLibraryWithoutExtension() {
		assertNativeLibrary(false, "so");
		assertNativeLibrary(false, "dll");
		assertNativeLibrary(false, "dylib");
		assertNativeLibrary(false, "SO");
		assertNativeLibrary(false, "DYLIB");
		assertNativeLibrary(false, "libso");
		assertNativeLibrary(false, "foo");
		assertNativeLibrary(false, "");
		assertNativeLibrary(false, "usr" + File.separator + "lib" + File.separator + "so");
		assertNativeLibrary(false, File.separator + "so");
	}

	@Test
	public void testIsNativeLibrary() {
		assertNativeLibrary(true, "libfoo.so");
		assertNativeLibrary(true, "foo.dll");
		assertNativeLibrary(true, "libfoo.dylib");
		assertNativeLibrary(true, ".so");
		assertNativeLibrary(true, "libfoo.DyLiB");
		assertNativeLibrary(true, "usr" + File.separator + "lib" + File.separator + "libjvm.so");
		assertNativeLibrary(true, "some.dir" + File.separator + "libjvm.dylib");

		assertNativeLibrary(false, "foo.jar");
		assertNativeLibrary(false, "Foo.class");
		assertNativeLibrary(false, "Foo.java");
		assertNativeLibrary(false, "foo.sox");
		assertNativeLibrary(false, "foo.dlls");
		assertNativeLibrary(false, "foo.dylibx");
	}

	/**
	 * Only the part behind the last dot is the extension: if a path separator
	 * follows the last dot, that dot belongs to a directory segment and the
	 * name is not a native library.
	 */
	@Test
	public void testNativeLibraryWithSeparatorAfterLastDot() {
		assertNativeLibrary(false, "dir.so" + File.separator + "file");
		assertNativeLibrary(false, "dir.so/file");
		assertNativeLibrary(false, "dir.dll/file");
		assertNativeLibrary(false, "dir.dylib/file");
		assertNativeLibrary(false, "dir.so" + File.separator);
		assertNativeLibrary(false, "dir.so/");
		assertNativeLibrary(false, "libfoo.so/");
		assertNativeLibrary(false, "a.s/o");
		assertNativeLibrary(false, "a.d/ll");
	}

	@Test
	public void testNativeLibrariesAreNoArchives() {
		// see https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5253
		assertNotAnArchive("libfoo.so");
		assertNotAnArchive("foo.dll");
		assertNotAnArchive("libfoo.dylib");
		assertNotAnArchive("x.so");
		assertNotAnArchive(".so");
		assertNotAnArchive(".dll");
		assertNotAnArchive(".dylib");
	}

	@Test
	public void testNativeLibrariesAreDetectedCaseInsensitive() {
		assertNotAnArchive("libfoo.SO");
		assertNotAnArchive("FOO.DLL");
		assertNotAnArchive("libfoo.DYLIB");
		assertNotAnArchive("libfoo.DyLiB");
	}

	@Test
	public void testNativeLibrariesWithPath() {
		String nativeLibrary = "usr" + File.separator + "lib" + File.separator + "libjvm.so";
		assertNotAnArchive(nativeLibrary);
		assertNotAnArchive("some.dir" + File.separator + "libjvm.dylib");
		assertNotAnArchive("some.dir" + File.separator + "jvm.dll");
		// a dot in a directory segment must not turn a jar into a native library
		assertZipArchive("some.dir" + File.separator + "foo.jar");
	}

	/**
	 * Extensions of the same length as "class" (5) or "jmod" (4) must not be
	 * confused with native libraries and vice versa.
	 */
	@Test
	public void testExtensionsOfSameLength() {
		// 5 characters, like "class" and "dylib"
		assertZipArchive("foo.solar");
		assertZipArchive("foo.zzzzz");
		// 3 characters, like "dll"
		assertZipArchive("foo.dl");
		assertZipArchive("foo.dlx");
		// 2 characters, like "so"
		assertZipArchive("foo.sx");
	}

	/**
	 * A file whose extension only starts with a native library extension is a
	 * regular archive candidate.
	 */
	@Test
	public void testNamesSimilarToNativeLibraries() {
		assertZipArchive("foo.sox");
		assertZipArchive("foo.dlls");
		assertZipArchive("foo.dylibx");
		assertZipArchive("foo.libso");
		// no extension at all
		assertNotAnArchive("libso");
	}
}
