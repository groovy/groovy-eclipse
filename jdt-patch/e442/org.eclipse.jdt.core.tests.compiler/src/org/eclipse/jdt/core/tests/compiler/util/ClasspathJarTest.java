/*******************************************************************************
 * Copyright (c) 2026 Hélios GILLES and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Hélios GILLES - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJmod;
import org.eclipse.jdt.internal.compiler.batch.ClasspathMultiReleaseJar;

public class ClasspathJarTest extends TestCase {

	private Path directory;

	public ClasspathJarTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.directory = Files.createTempDirectory("ClasspathJarTest");
	}

	@Override
	protected void tearDown() throws Exception {
		try (var paths = Files.walk(this.directory)) {
			paths.sorted((a, b) -> b.compareTo(a)).forEach(p -> p.toFile().delete());
		}
		super.tearDown();
	}

	/**
	 * hasCompilationUnit answers whether a class file is directly inside the package, for every package and every
	 * kind of entry, as it did when it walked the entries on each call.
	 */
	public void testHasCompilationUnit() throws IOException {
		List<String> entries = List.of(
				"META-INF/MANIFEST.MF",
				"META-INF/versions/11/m/M.class",
				"p/A.class",
				"p/q/",
				"p/q/B.CLASS",
				"r/readme.txt",
				"Foo.class",
				"/Bar.class",
				"s/T.class/",
				"a//B.class",
				"u/.class",
				"v/W.Class",
				"w/x.classes",
				"x/y/z/",
				"t/Ü.class");
		File jar = writeJar("lib.jar", entries);
		Set<String> packages = new TreeSet<>(List.of("", "/", "P", "p/", "nope", "m", "a/b"));
		for (String entry : entries) {
			for (int i = entry.indexOf('/'); i != -1; i = entry.indexOf('/', i + 1)) {
				packages.add(entry.substring(0, i));
			}
		}
		ClasspathJar classpath = new ClasspathJar(jar, true, null, null);
		classpath.initialize();
		try {
			for (String pkg : packages) {
				assertEquals("package \"" + pkg + "\"", hasCompilationUnitByWalk(entries, pkg), classpath.hasCompilationUnit(pkg, null));
			}
			assertTrue(classpath.hasCompilationUnit("p", null));
			assertTrue(classpath.hasCompilationUnit("p/q", null));
			assertTrue(classpath.hasCompilationUnit("", null));
			assertFalse(classpath.hasCompilationUnit("x/y", null));
			assertFalse(classpath.hasCompilationUnit("m", null));
		} finally {
			classpath.reset();
		}
	}

	/**
	 * The packages are read again from the archive once the class path entry has been reset.
	 */
	public void testHasCompilationUnitAfterReset() throws IOException {
		File jar = writeJar("lib.jar", List.of("p/A.class"));
		ClasspathJar classpath = new ClasspathJar(jar, true, null, null);
		classpath.initialize();
		try {
			assertTrue(classpath.hasCompilationUnit("p", null));
			assertFalse(classpath.hasCompilationUnit("q", null));
		} finally {
			classpath.reset();
		}

		long lastModified = jar.lastModified();
		writeJar("lib.jar", List.of("q/B.class"));
		// ZipFile shares the content of an archive by file key and modification time
		assertTrue(jar.setLastModified(lastModified + 10_000));
		classpath.initialize();
		try {
			assertFalse(classpath.hasCompilationUnit("p", null));
			assertTrue(classpath.hasCompilationUnit("q", null));
		} finally {
			classpath.reset();
		}
	}

	/** The entries of a JMOD file, whose class files are in the classes/ folder. */
	private static final List<String> JMOD_ENTRIES = List.of(
			"classes/module-info.class",
			"classes/p/A.class",
			"classes/p/q/",
			"classes/p/q/B.CLASS",
			"classes/r/readme.txt",
			"classes/META-INF/MANIFEST.MF",
			"bin/tool",
			"conf/c/C.class",
			"lib/libfoo.so",
			"legal/LICENSE",
			"classesX/s/S.class",
			"Classes/t/T.class",
			"x/classes/u/U.class");

	/**
	 * hasCompilationUnit of a JMOD file only looks at the classes/ folder, and answers as the former implementation
	 * of ClasspathJmod which walked the entries on each call.
	 */
	public void testJmodHasCompilationUnit() throws IOException {
		File jmod = writeJar("m.jmod", JMOD_ENTRIES);
		List<String> classes = JMOD_ENTRIES.stream()
				.filter(entry -> entry.startsWith("classes/"))
				.map(entry -> entry.substring("classes/".length()))
				.toList();
		Set<String> packages = new TreeSet<>(List.of("", "/", "classes", "classes/p", "s", "t", "u", "x/classes/u", "nope"));
		for (String entry : JMOD_ENTRIES) {
			for (int i = entry.indexOf('/'); i != -1; i = entry.indexOf('/', i + 1)) {
				packages.add(entry.substring(0, i));
			}
		}
		ClasspathJmod classpath = new ClasspathJmod(jmod, true, null, null);
		classpath.initialize();
		try {
			for (String pkg : packages) {
				assertEquals("package \"" + pkg + "\"", hasCompilationUnitByWalk(classes, pkg), classpath.hasCompilationUnit(pkg, null));
			}
			assertTrue(classpath.hasCompilationUnit("p", null));
			assertTrue(classpath.hasCompilationUnit("p/q", null));
			assertFalse(classpath.hasCompilationUnit("", null));
			assertFalse(classpath.hasCompilationUnit("conf/c", null));
			assertFalse(classpath.hasCompilationUnit("c", null));
			assertFalse(classpath.hasCompilationUnit("classes/p", null));
		} finally {
			classpath.reset();
		}
	}

	/** The packages of a JMOD file are the folders below its classes/ folder. */
	public void testJmodIsPackage() throws IOException {
		ClasspathJmod classpath = new ClasspathJmod(writeJar("m.jmod", JMOD_ENTRIES), true, null, null);
		classpath.initialize();
		try {
			assertTrue(classpath.isPackage("", null));
			assertTrue(classpath.isPackage("p", null));
			assertTrue(classpath.isPackage("p/q", null));
			assertTrue(classpath.isPackage("r", null));
			assertTrue(classpath.isPackage("META-INF", null));
			assertFalse(classpath.isPackage("classes", null));
			assertFalse(classpath.isPackage("classes/p", null));
			assertFalse(classpath.isPackage("bin", null));
			assertFalse(classpath.isPackage("conf/c", null));
			assertFalse(classpath.isPackage("s", null));
			assertFalse(classpath.isPackage("t", null));
			assertFalse(classpath.isPackage("u", null));
		} finally {
			classpath.reset();
		}
	}

	/** listPackages of a JMOD file answers the packages below its classes/ folder, without the folder. */
	public void testJmodListPackages() throws IOException {
		ClasspathJmod classpath = new ClasspathJmod(writeJar("m.jmod", JMOD_ENTRIES), true, null, null);
		classpath.initialize();
		try {
			assertEquals(Set.of("p", "p.q"), toStrings(classpath.listPackages()));
		} finally {
			classpath.reset();
		}
	}

	/** findTypeNames of a JMOD file answers the types of a package below its classes/ folder. */
	public void testJmodFindTypeNames() throws IOException {
		ClasspathJmod classpath = new ClasspathJmod(writeJar("m.jmod", JMOD_ENTRIES), true, null, null);
		classpath.initialize();
		try {
			assertEquals(Set.of("p.A"), toStrings(classpath.findTypeNames("p", null)));
			assertEquals(Set.of("p.q.B"), toStrings(classpath.findTypeNames("p/q", null)));
			assertNull(classpath.findTypeNames("conf/c", null));
			assertNull(classpath.findTypeNames("s", null));
		} finally {
			classpath.reset();
		}
	}

	/** listPackages and findTypeNames of a JAR are not changed by the classes/ folder of JMOD files. */
	public void testJarListPackagesAndFindTypeNames() throws IOException {
		ClasspathJar classpath = new ClasspathJar(writeJar("lib.jar", JMOD_ENTRIES), true, null, null);
		classpath.initialize();
		try {
			assertEquals(Set.of("classes", "classes.p", "classes.p.q", "conf.c", "classesX.s", "Classes.t", "x.classes.u"),
					toStrings(classpath.listPackages()));
			assertEquals(Set.of("classes.p.A"), toStrings(classpath.findTypeNames("classes/p", null)));
			assertNull(classpath.findTypeNames("p", null));
		} finally {
			classpath.reset();
		}
	}

	/** The entries of a multi-release JAR, to be read for release 11. */
	private static final List<String> MULTI_RELEASE_ENTRIES = List.of(
			"p/A.class",
			"p/Both.class",
			"META-INF/versions/9/q/B.class",
			"META-INF/versions/11/r/C.class",
			"META-INF/versions/11/p/Both.class",
			"META-INF/versions/11/w/readme.txt",
			"META-INF/versions/17/s/D.class",
			"META-INF/versions/8/t/E.class",
			"META-INF/versions/011/u/F.class",
			"META-INF/versions/x/v/G.class");

	private static final String MULTI_RELEASE_MANIFEST = "Manifest-Version: 1.0\r\nMulti-Release: true\r\n\r\n";

	/**
	 * The classes of a package which is only in an applicable META-INF/versions/N/ folder are compilation units of
	 * that package, as it is a package for isPackage and findClass finds its classes.
	 */
	public void testMultiReleaseHasCompilationUnit() throws IOException {
		File jar = writeJar("mr.jar", MULTI_RELEASE_MANIFEST, MULTI_RELEASE_ENTRIES);
		ClasspathMultiReleaseJar classpath = new ClasspathMultiReleaseJar(jar, true, null, null, "11");
		classpath.initialize();
		try {
			for (String pkg : List.of("p", "q", "r")) {
				assertTrue("package \"" + pkg + "\"", classpath.isPackage(pkg, null));
				assertTrue("package \"" + pkg + "\"", classpath.hasCompilationUnit(pkg, null));
			}
			assertTrue(classpath.isPackage("w", null));
			assertFalse(classpath.hasCompilationUnit("w", null));
			for (String pkg : List.of("s", "t", "u", "v", "META-INF/versions/9/q", "META-INF/versions/17/s")) {
				assertFalse("package \"" + pkg + "\"", classpath.isPackage(pkg, null));
				assertFalse("package \"" + pkg + "\"", classpath.hasCompilationUnit(pkg, null));
			}
		} finally {
			classpath.reset();
		}
	}

	/** hasCompilationUnit does not depend on isPackage having read the archive before. */
	public void testMultiReleaseHasCompilationUnitFirst() throws IOException {
		File jar = writeJar("mr.jar", MULTI_RELEASE_MANIFEST, MULTI_RELEASE_ENTRIES);
		ClasspathMultiReleaseJar classpath = new ClasspathMultiReleaseJar(jar, true, null, null, "11");
		classpath.initialize();
		try {
			assertTrue(classpath.hasCompilationUnit("q", null));
			assertFalse(classpath.hasCompilationUnit("META-INF/versions/9/q", null));
		} finally {
			classpath.reset();
		}
	}

	/** listPackages and findTypeNames of a multi-release JAR answer the classes of the applicable version folders. */
	public void testMultiReleaseListPackagesAndFindTypeNames() throws IOException {
		File jar = writeJar("mr.jar", MULTI_RELEASE_MANIFEST, MULTI_RELEASE_ENTRIES);
		ClasspathMultiReleaseJar classpath = new ClasspathMultiReleaseJar(jar, true, null, null, "11");
		classpath.initialize();
		try {
			assertEquals(Set.of("p", "q", "r"), toStrings(classpath.listPackages()));
			assertEquals(Set.of("q.B"), toStrings(classpath.findTypeNames("q", null)));
			assertEquals(Set.of("r.C"), toStrings(classpath.findTypeNames("r", null)));
			char[][][] typeNames = classpath.findTypeNames("p", null);
			assertEquals(Set.of("p.A", "p.Both"), toStrings(typeNames));
			assertEquals("p.Both is answered once", 2, typeNames.length);
			assertNull(classpath.findTypeNames("s", null));
		} finally {
			classpath.reset();
		}
	}

	/** Without Multi-Release: true in the manifest, the version folders are plain folders. */
	public void testMultiReleaseWithoutManifestHeader() throws IOException {
		File jar = writeJar("mr.jar", "Manifest-Version: 1.0\r\n\r\n", MULTI_RELEASE_ENTRIES);
		ClasspathMultiReleaseJar classpath = new ClasspathMultiReleaseJar(jar, true, null, null, "11");
		classpath.initialize();
		try {
			assertTrue(classpath.hasCompilationUnit("p", null));
			assertFalse(classpath.isPackage("q", null));
			assertFalse(classpath.hasCompilationUnit("q", null));
			assertTrue(classpath.isPackage("META-INF/versions/9/q", null));
			assertTrue(classpath.hasCompilationUnit("META-INF/versions/9/q", null));
			assertEquals(Set.of("p.A", "p.Both"), toStrings(classpath.findTypeNames("p", null)));
		} finally {
			classpath.reset();
		}
	}

	private static Set<String> toStrings(char[][] names) {
		Set<String> result = new TreeSet<>();
		for (char[] name : names) {
			result.add(new String(name));
		}
		return result;
	}

	private static Set<String> toStrings(char[][][] names) {
		Set<String> result = new TreeSet<>();
		for (char[][] name : names) {
			result.add(new String(CharOperation.concatWith(name, '.')));
		}
		return result;
	}

	/** The former implementation of ClasspathJar.hasCompilationUnit, which walked the entries on each call. */
	private static boolean hasCompilationUnitByWalk(List<String> entries, String qualifiedPackageName) {
		qualifiedPackageName += '/';
		for (String fileName : entries) {
			if (fileName.startsWith(qualifiedPackageName) && fileName.length() > qualifiedPackageName.length()) {
				String tail = fileName.substring(qualifiedPackageName.length());
				if (tail.indexOf('/') != -1)
					continue;
				if (tail.toLowerCase().endsWith(".class"))
					return true;
			}
		}
		return false;
	}

	private File writeJar(String name, List<String> entries) throws IOException {
		return writeJar(name, null, entries);
	}

	private File writeJar(String name, String manifest, List<String> entries) throws IOException {
		File jar = this.directory.resolve(name).toFile();
		try (OutputStream out = Files.newOutputStream(jar.toPath()); ZipOutputStream zip = new ZipOutputStream(out)) {
			if (manifest != null) {
				zip.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
				zip.write(manifest.getBytes(StandardCharsets.UTF_8));
				zip.closeEntry();
			}
			for (String entry : entries) {
				zip.putNextEntry(new ZipEntry(entry));
				if (!entry.endsWith("/")) {
					zip.write(new byte[] { 1 });
				}
				zip.closeEntry();
			}
		}
		return jar;
	}
}
