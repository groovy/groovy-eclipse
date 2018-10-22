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

import junit.framework.Test;

public class UnnamedModuleTest extends AbstractRegressionTest9 {

static {
//	TESTS_NAMES = new String[] { "testBugXXX" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public UnnamedModuleTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_9);
}

public static Class<UnnamedModuleTest> testClass() {
	return UnnamedModuleTest.class;
}

public void testBug522327() {
	runConformTest(
		new String[] {
			"nonmodular/ProblemWithThrowable.java",
			"package nonmodular;\n" +
			"\n" +
			"import java.io.IOException;\n" +
			"import java.sql.SQLException;\n" +
			"\n" +
			"public class ProblemWithThrowable {\n" +
			"    public void saveProperties() throws IOException {\n" +
			"    }\n" +
			"}\n" +
			"",
		}
	);
}

public void testBug522326() {
	runConformTest(
		new String[] {
			"nonmodular/ProblemWithNested.java",
			"package nonmodular;\n" +
			"\n" +
			"import java.sql.Connection;\n" +
			"import java.util.Map.Entry;\n" +
			"\n" +
			"public class ProblemWithNested {\n" +
			"}\n" +
			"",
		}
	);
}
}
