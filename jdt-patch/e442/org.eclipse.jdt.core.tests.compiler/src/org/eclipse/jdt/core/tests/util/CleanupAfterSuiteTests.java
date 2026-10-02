/*******************************************************************************
 * Copyright (c) 2024 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CleanupAfterSuiteTests extends junit.framework.TestCase {

	private static int count = 1;

	public CleanupAfterSuiteTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite testSuite = new TestSuite("HouseKeeping_" + (count ++));
		testSuite.addTest(new TestSuite(CleanupAfterSuiteTests.class));
		return testSuite;
	}

	public void testCleanupWorkspace() throws Exception {
		System.out.println("Starting cleaning up workspace");
		Util.cleanupWorkspace(getClass().getName());
		System.out.println("Done cleaning up workspace");
	}

	public void testCleanupJavaModel() throws Exception {
		System.out.println("Starting cleaning up Java Model");
		Util.cleanupClassPathVariablesAndContainers();
		System.out.println("Done cleaning up Java Model");
	}
}