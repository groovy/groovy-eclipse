/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AssignmentTest_1_7 extends AbstractRegressionTest {

public AssignmentTest_1_7(String name) {
	super(name);
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.ERROR);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 61 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
/*
 * no effect assignment bug
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=27235
 */
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Object o = new X();\n" +
			"		String s = \"ESS\";\n" +
			"		 o += s;\n" +
			"		System.out.println(o);\n" +
			"	}\n" +
			"	public String toString() {\n" +
			"		return \"SUCC\";\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
public static Class testClass() {
	return AssignmentTest_1_7.class;
}
}
