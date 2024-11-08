/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class UseOfUnderscoreJava22Test extends AbstractBatchCompilerTest {

	public static Test suite() {
		return buildMinimalComplianceTestSuite(UseOfUnderscoreJava22Test.class, F_22);
	}

	public UseOfUnderscoreJava22Test(String name) {
		super(name);
	}

	@Override
	protected Map<String, String> getCompilerOptions() {
		CompilerOptions compilerOptions = new CompilerOptions(super.getCompilerOptions());
		return compilerOptions.getMap();
	}

	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		if(!isJRE22Plus)
			return;
		runNegativeTest(false/*skipJavac*/, null, testFiles, expectedCompilerLog);
	}
	public void testReportsUnderscoreInstanceMemberAsError() {
		String message = "As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
		String errorLevel = "ERROR";

		runNegativeTest(new String[] { "A.java", """
				public class A {
					int _ = 1;
					public static void main(String[] args) {
						System.out.println("Hello, World!");
					}
				}
				""" },

				"----------\n" +
				"1. " + errorLevel + " in A.java (at line 2)\n" +
				"	int _ = 1;\n" +
				"	    ^\n" +
				message + "\n" +
				"----------\n");
	}

	public void testReportsUnicodeEscapeUnderscoreInstanceMemberAsError() {
		String message = "As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
		String errorLevel = "ERROR";

		runNegativeTest(new String[] { "A.java", """
				public class A {
					int \\u005F = 1;
					public static void main(String[] args) {
						System.out.println("Hello, World!");
					}
				}
				""" },
				"----------\n" +
				"1. " + errorLevel + " in A.java (at line 2)\n" +
				"	int \\u005F = 1;\n" +
				"	    ^^^^^^\n" +
				message + "\n" +
				"----------\n");
	}

	public void testReportsUnderscoreParameterAsError() {
		String message = "As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
		String errorLevel = "ERROR";

		runNegativeTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						foo(1);
					}
					public static void foo(int _) {
						System.out.println("Hello, World!");
					}
				}
				""" },
				"----------\n" +
				"1. " + errorLevel + " in A.java (at line 5)\n" +
				"	public static void foo(int _) {\n" +
				"	                           ^\n" +
				message + "\n" +
				"----------\n");
	}

	public void testReportsUnderscoreParameterAsErrorUnicodeEscape() {
		String message = "As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
		String errorLevel = "ERROR";

		runNegativeTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						foo(1);
					}
					public static void foo(int \\u005F) {
						System.out.println("Hello, World!");
					}
				}
				""" },
				"----------\n" +
				"1. " + errorLevel + " in A.java (at line 5)\n" +
				"	public static void foo(int \\u005F) {\n" +
				"	                           ^^^^^^\n" +
				message + "\n" +
				"----------\n");
	}

	public void testReportsUnderscoreLocalVariableAsErrorUnicodeEscape() {
		if(!isJRE22Plus)
			return;
		runConformTest(new String[] { "A.java",
				"""
				public class A {
					public static void main(String[] args) {
						int \\u005F = 12;
						System.out.println("hello, world");
					}
				}
				"""},
				"hello, world", null, new String[] { "--enable-preview" });
	}

}
