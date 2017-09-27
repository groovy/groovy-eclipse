/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class Compliance_1_6 extends AbstractComparableTest {

public Compliance_1_6(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_6);
}
// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
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
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	this.runConformTest(
			new String[] {
					"Test.java",
					"public class Test {\n" + 
					"	public interface MyInterface {\n" + 
					"		public void hello();\n" + 
					"	}\n" + 
					"	private static class MyClass implements MyInterface {\n" + 
					"		@Override\n" + 
					"		public void hello() {\n" + 
					"			System.out.println(\"Hello\");\n" + 
					"		}\n" + 
					"	}\n" + 
					"	public static void main(String[] args) {\n" + 
					"		MyClass m = new MyClass();\n" + 
					"		m.hello();\n" + 
					"	}\n" + 
					"}"
			},
			"Hello",
			null /* no extra class libraries */,
			true /* flush output directory */,
			null,
			options,
			null/* do not perform statements recovery */);
}
public static Class testClass() {
	return Compliance_1_6.class;
}
}
