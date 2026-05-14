/*******************************************************************************
 * Copyright (c) 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Run tests from the super class with (legacy) declaration annotations. */
public class NullDeclarationAnnotationTest extends NullAnnotationTest {

	public NullDeclarationAnnotationTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testBug545715" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(testClass().getName());
		buildMinimalComplianceTestSuite(FIRST_SUPPORTED_JAVA_VERSION, 1, suite, testClass());
		return suite;
	}

	public static Class testClass() {
		return NullDeclarationAnnotationTest.class;
	}

	public boolean useDeclarationAnnotations() {
		return true;
	}

	/**
	 * @deprecated
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.TEST_JAR_SUFFIX = ".jar";
	}

	@Override
	protected String getAnnotationLibPath() throws IOException {
		return getAnnotationV1LibPath();
	}

}
