/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class ConditionalExpressionTest extends AbstractRegressionTest {

	public ConditionalExpressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 65 };
//		TESTS_RANGE = new int[] { 11, -1 };
//	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return ConditionalExpressionTest.class;
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100162
	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    final boolean isA = true;\n" +
				"    public static void main(String[] args) {\n" +
				"        X x = new X();\n" +
				"        System.out.print(x.isA ? \"SUCCESS\" : \"FAILURE\");\n" +
				"    }\n" +
				"}",
			},
			"SUCCESS"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=107193
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class RecipeElement {\n" +
				"    public static final RecipeElement[] NO_CHILDREN= new RecipeElement[0]; \n" +
				"}\n" +
				"class Ingredient extends RecipeElement { }\n" +
				"class X extends RecipeElement {\n" +
				"    private Ingredient[] fIngredients;\n" +
				"    public RecipeElement[] getChildren() {\n" +
				"        return fIngredients == null ? NO_CHILDREN : fIngredients;\n" +
				"    }\n" +
				"}",
			},
			""
		);
	}
}
