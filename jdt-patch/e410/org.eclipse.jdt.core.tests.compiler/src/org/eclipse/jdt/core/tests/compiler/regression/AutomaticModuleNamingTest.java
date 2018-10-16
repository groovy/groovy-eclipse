/*******************************************************************************
 * Copyright (c) 2017 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import static org.eclipse.jdt.internal.compiler.env.AutomaticModuleNaming.determineAutomaticModuleName;
import static org.eclipse.jdt.internal.compiler.env.AutomaticModuleNaming.determineAutomaticModuleNameFromFileName;

import java.io.File;

import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class AutomaticModuleNamingTest extends AbstractRegressionTest {
	static {
		// TESTS_NAMES = new String[] { "testManifest" };
	}

	public AutomaticModuleNamingTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Class<?> testClass() {
		return AutomaticModuleNamingTest.class;
	}

	public void testManifest() throws Exception {
		String dirName = OUTPUT_DIR + File.separator + "automatic";
		try {
			String metaInfDir = dirName + File.separator + "META-INF";
			new File(metaInfDir).mkdirs();

			Util.createFile(metaInfDir + File.separator + "MANIFEST.MF", //
					"Manifest-Version: 1.0\n" //
							+ "Automatic-Module-Name: module.123\n");
			Util.zip(new File(dirName), dirName + File.separator + "foo.bar-1.2.3.jar");
			assertEquals("module.123", new String(
					determineAutomaticModuleName((dirName + File.separator + "foo.bar-1.2.3.jar").toString())));
		} finally {
			Util.delete(dirName);
		}
	}

	public void testSimple() throws Exception {
		assertEquals("junit", new String(determineAutomaticModuleNameFromFileName("junit.jar", false, true)));
	}

	public void testWithVersion() throws Exception {
		assertEquals("junit", new String(determineAutomaticModuleNameFromFileName("junit-4.8.2.jar", false, true)));
	}

	public void testMultiParts() throws Exception {
		assertEquals("foo.bar", new String(determineAutomaticModuleNameFromFileName("foo-bar.jar", false, true)));
	}

	public void testMultiPartWithVersion() throws Exception {
		assertEquals("foo.bar",
				new String(determineAutomaticModuleNameFromFileName("foo-bar-1.2.3-SNAPSHOT.jar", false, true)));
	}

	public void testMultiPartWithNumberWithoutDot() throws Exception {
		assertEquals("foo.bar.3d",
				new String(determineAutomaticModuleNameFromFileName("foo-bar-3d-1.2.3-SNAPSHOT.jar", false, true)));
	}

	public void testSpecialCharacters() throws Exception {
		assertEquals("foo.bar",
				new String(determineAutomaticModuleNameFromFileName("?foo?bar?-1.2.3-SNAPSHOT.jar", false, true)));
	}

	public void testMultipleSpecialCharacters() throws Exception {
		assertEquals("foo.bar", new String(
				determineAutomaticModuleNameFromFileName("?@#foo?@#bar?@#-1.2.3-SNAPSHOT.jar", false, true)));
	}

	public void testMultipleSpecialCharactersWithDirectory() throws Exception {
		assertEquals("foo.bar.bla",
				new String(determineAutomaticModuleNameFromFileName(
						File.separator + "somedir" + File.separator + "?@#foo?@#bar?@#bla?@#-1.2.3-SNAPSHOT.jar", true,
						true)));
	}

	public void testFileEndsWithDotJar() throws Exception {
		assertEquals("module.jar", new String(
				determineAutomaticModuleNameFromFileName("somedir" + File.separator + "module.jar.jar", true, true)));
	}

	public void testProjectNameEndsWithDotJar() throws Exception {
		// for hypothetical use case: project on module path treated as automatic module
		assertEquals("module.jar", new String(
				determineAutomaticModuleNameFromFileName("somedir" + File.separator + "module.jar", true, false)));
	}

	public void testUPPERCASE() throws Exception {
		// upper case .JAR isn't mentioned in the spec, but currently handled like .jar
		assertEquals("FOO.BAR", new String(determineAutomaticModuleNameFromFileName("FOO-BAR.JAR", true, true)));
	}

	public void testZip() throws Exception {
		// .ZIP isn't mentioned in the spec.
		assertEquals("CLASSES12.ZIP",
				new String(determineAutomaticModuleNameFromFileName("CLASSES12.ZIP", true, true)));
	}

	public void testBug529680() throws Exception {
		assertEquals("hibernate.jpa", new String(
				determineAutomaticModuleNameFromFileName("hibernate-jpa-2.1-api-1.0.0.Final.jar", true, true)));
	}

}
