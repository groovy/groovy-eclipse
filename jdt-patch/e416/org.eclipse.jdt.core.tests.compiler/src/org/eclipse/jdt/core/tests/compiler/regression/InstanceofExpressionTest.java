/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class InstanceofExpressionTest extends AbstractRegressionTest {

	public InstanceofExpressionTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return InstanceofExpressionTest.class;
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=341828
	public void test001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.InputStream;\n" +
				"public class X {\n" +
				"    void foo(InputStream is) {\n" +
				"    if (is instanceof FileInputStream)\n" +
				"        System.out.println(\"Hello\");\n" +
				"    }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (is instanceof FileInputStream)\n" +
			"	                  ^^^^^^^^^^^^^^^\n" +
			"FileInputStream cannot be resolved to a type\n" +
			"----------\n"
		);
	}
}
