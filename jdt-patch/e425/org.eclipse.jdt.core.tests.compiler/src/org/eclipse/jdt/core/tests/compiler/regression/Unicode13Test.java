/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class Unicode13Test extends AbstractRegressionTest {
public Unicode13Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_15);
}
public void test1() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		public int a\\u08BE; // new unicode character in unicode 13 \n" +
			"}",
		},
		"",
		options);
}
public void test2() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		public int a\\ud880\\udc00; // new unicode character in unicode 13 using high and low surrogate\n" +
			"}",
		},
		"",
		options);
}
public static Class<Unicode13Test> testClass() {
	return Unicode13Test.class;
}
}
