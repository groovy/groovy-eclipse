/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.unicode;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class Unicode17Test extends AbstractRegressionTest {
public Unicode17Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_26);
}
public void test1() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_26);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		public int a\\uD888\\uDFB0; // new unicode character in unicode 17 using high and low surrogate\n" +
			"}",
		},
		"",
		options);
}
public void test2() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_25);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		public int a\\uD888\\uDFB0; // new unicode character in unicode 17 using high and low surrogate\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public int a\\uD888\\uDFB0; // new unicode character in unicode 17 using high and low surrogate\n" +
		"	            ^^^^^^^^^^^^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n",
		null,
		false,
		options);
}
public static Class<Unicode17Test> testClass() {
	return Unicode17Test.class;
}
}
