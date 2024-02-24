/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
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

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Jsr14Test extends AbstractRegressionTest {

public Jsr14Test(String name) {
	super(name);
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 15 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_4);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test1() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(X.class);\n" +
			"	}\n" +
			"}",
		},
		"class X");
	String expectedOutput =
		"  // Method descriptor #18 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
		"     3  getstatic X.class$0 : java.lang.Class [25]\n" +
		"     6  dup\n" +
		"     7  ifnonnull 35\n" +
		"    10  pop\n" +
		"    11  ldc <String \"X\"> [27]\n" +
		"    13  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [28]\n" +
		"    16  dup\n" +
		"    17  putstatic X.class$0 : java.lang.Class [25]\n" +
		"    20  goto 35\n" +
		"    23  new java.lang.NoClassDefFoundError [34]\n" +
		"    26  dup_x1\n" +
		"    27  swap\n" +
		"    28  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [36]\n" +
		"    31  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [42]\n" +
		"    34  athrow\n" +
		"    35  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [45]\n" +
		"    38  return\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
public static Class testClass() {
	return Jsr14Test.class;
}
}
