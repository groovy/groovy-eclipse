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

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class BinaryLiteralTest extends AbstractRegressionTest {
	public BinaryLiteralTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}

	public static Class testClass() {
		return BinaryLiteralTest.class;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0b001);\n" +
				"	}\n" +
				"}"
			},
			"1");
	}
	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0b);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	System.out.println(0b);\n" + 
			"	                   ^^\n" + 
			"Invalid binary literal number (only \'0\' and \'1\' are expected)\n" + 
			"----------\n");
	}
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0b2);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	System.out.println(0b2);\n" + 
			"	                   ^^\n" + 
			"Invalid binary literal number (only \'0\' and \'1\' are expected)\n" + 
			"----------\n");
	}
	public void test004() {
		Map customedOptions = getCompilerOptions();
		customedOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
		customedOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
		customedOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0b1110000);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	System.out.println(0b1110000);\n" + 
			"	                   ^^^^^^^^^\n" + 
			"Binary literals can only be used with source level 1.7 or greater\n" + 
			"----------\n",
			null,
			true,
			customedOptions);
	}
	public void test005() {
		Map customedOptions = getCompilerOptions();
		customedOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
		customedOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
		customedOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(-0b1110000);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	System.out.println(-0b1110000);\n" + 
			"	                    ^^^^^^^^^\n" + 
			"Binary literals can only be used with source level 1.7 or greater\n" + 
			"----------\n",
			null,
			true,
			customedOptions);
	}
	public void test006() {
		Map customedOptions = getCompilerOptions();
		customedOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
		customedOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
		customedOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0b1113000);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	System.out.println(0b1113000);\n" + 
			"	                   ^^^^^\n" + 
			"Binary literals can only be used with source level 1.7 or greater\n" + 
			"----------\n",
			null,
			true,
			customedOptions);
	}
}
