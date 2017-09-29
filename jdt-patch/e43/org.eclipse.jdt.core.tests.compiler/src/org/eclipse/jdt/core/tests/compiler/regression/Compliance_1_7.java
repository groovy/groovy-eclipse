/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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

public class Compliance_1_7 extends AbstractComparableTest {

public Compliance_1_7(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
static {
// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "Bug58069" };
// Numbers of tests to run: "test<number>" will be run for each number of this array
//	TESTS_NUMBERS = new int[] { 104 };
// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 85, -1 };
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=283225
public void test1() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"package p1;\n" +
			"import java.util.List;\n" +
			"public class Z  {\n" +
			"	@SafeVarargs\n" +
			"	public static <T> List<T> asList(T... a) {\n" + 
			"		return null;\n" + 
			"	}\n" +
			"}"
		},
		""); // no special vm args

		String computedReferences = findReferences(OUTPUT_DIR + "/p1/Z.class");
		boolean check = computedReferences.indexOf("annotationRef/SafeVarargs") >= 0;
		if (!check){
			System.out.println(computedReferences);
		}
		assertTrue("did not indexed the reference to SafeVarargs", check);
}
public void test2() {
	this.runConformTest(
		new String[] {
			"p2/Z.java",
			"package p2;\n" +
			"import java.lang.annotation.Inherited;\n" +
			"@Inherited\n" +
			"public @interface Z  {\n" +
			"}"
		},
		""); // no special vm args

		String computedReferences = findReferences(OUTPUT_DIR + "/p2/Z.class");
		boolean check = computedReferences.indexOf("annotationRef/Inherited") >= 0;
		if (!check){
			System.out.println(computedReferences);
		}
		assertTrue("did not indexed the reference to Inherited", check);
}
public static Class testClass() {
	return Compliance_1_7.class;
}
}
