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
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Jsr14Test extends AbstractRegressionTest {

public Jsr14Test(String name) {
	super(name);
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.getFirstSupportedJavaVersion());
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.getFirstSupportedJavaVersion());
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.getFirstSupportedJavaVersion());
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
	return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
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
		"""
		// Compiled from X.java (version 1.8 : 52.0, super bit)
		public class X {
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 1
		  public X();
		    0  aload_0 [this]
		    1  invokespecial java.lang.Object() [8]
		    4  return
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X
		 \s
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		    0  getstatic java.lang.System.out : java.io.PrintStream [16]
		    3  ldc <Class X> [1]
		    5  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [22]
		    8  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 8, line: 4]
		      Local variable table:
		        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]
		}""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
public static Class testClass() {
	return Jsr14Test.class;
}
}
