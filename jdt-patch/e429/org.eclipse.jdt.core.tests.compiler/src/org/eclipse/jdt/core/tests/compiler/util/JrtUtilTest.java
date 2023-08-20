/*******************************************************************************
 * Copyright (c) 2022 Andrey Loskutov, and others.
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
import java.nio.file.Paths;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.junit.Before;
import org.junit.Test;

public class JrtUtilTest extends TestCase {

	private String javaSpecVersion;
	private String javaHome;
	private File image;
	private String jdkRelease;

	public JrtUtilTest(String name) {
		super(name);
	}

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.javaSpecVersion = System.getProperty("java.specification.version", null);
		assertNotNull("java.specification.version is not defined", this.javaSpecVersion);
		this.javaHome = System.getProperty("java.home", null);
		assertNotNull("java.home is not defined", this.javaHome);
		this.image = Paths.get(this.javaHome).toFile();
		assertTrue("java.home points to invalid path", this.image.isDirectory());
		this.jdkRelease = JRTUtil.getJdkRelease(this.image);
	}

	@Test
	public void testGetReleaseVersion() {
		long expectedLevel = CompilerOptions.versionToJdkLevel(this.javaSpecVersion);
		long seenLevel = CompilerOptions.versionToJdkLevel(this.jdkRelease);
		assertEquals("Unexpected version: " + this.jdkRelease + ", not matching " + this.javaSpecVersion, expectedLevel, seenLevel);

		int sameRelease = JavaCore.compareJavaVersions(this.javaSpecVersion, this.jdkRelease);
		assertEquals("Unexpected version: " + this.jdkRelease + ", not matching " + this.javaSpecVersion, sameRelease, 0);
	}

	@Test
	public void testGetNewJrtFileSystem() throws Exception {
		int majorVersionSegment = getMajorVersionSegment(this.jdkRelease);
		Object jrtSystem = JRTUtil.getJrtSystem(this.image);
		Object jrtSystem2 = JRTUtil.getJrtSystem(this.image, String.valueOf(majorVersionSegment));
		assertSame(jrtSystem, jrtSystem2);

		jrtSystem2 = JRTUtil.getJrtSystem(this.image, String.valueOf(majorVersionSegment-2));
		assertNotSame(jrtSystem, jrtSystem2);

		Object jrtSystem3 = JRTUtil.getJrtSystem(this.image);
		assertSame(jrtSystem, jrtSystem3);
	}

	private static int getMajorVersionSegment(String releaseVersion) {
		int dot = releaseVersion.indexOf('.');
		if (dot > 0) {
			return Integer.parseInt(releaseVersion.substring(0, dot));
		}
		return Integer.parseInt(releaseVersion);
	}
}
