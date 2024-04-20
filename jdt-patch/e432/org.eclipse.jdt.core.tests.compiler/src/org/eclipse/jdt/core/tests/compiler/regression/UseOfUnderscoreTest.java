/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class UseOfUnderscoreTest extends AbstractBatchCompilerTest {

	public static Test suite() {
		return buildMinimalComplianceTestSuite(UseOfUnderscoreTest.class, F_1_8);
	}

	public UseOfUnderscoreTest(String name) {
		super(name);
	}

	public void testReportsUnderscoreInstanceMemberAsError() {
		CompilerOptions options = new CompilerOptions(getCompilerOptions());

		String message;
		String errorLevel;
		if (options.sourceLevel < ClassFileConstants.JDK9) {
			message = "'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on";
			errorLevel = "WARNING";
		} else if (options.sourceLevel < ClassFileConstants.JDK22) {
			message = "'_' is a keyword from source level 9 onwards, cannot be used as identifier";
			errorLevel = "ERROR";
		} else if (options.sourceLevel == ClassFileConstants.JDK22) {
			message = "As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
			errorLevel = "ERROR";
		} else {
			message = "Unnamed Patterns and Variables is a preview feature and disabled by default. Use --enable-preview to enable";
			errorLevel = "ERROR";
		}

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
		CompilerOptions options = new CompilerOptions(getCompilerOptions());

		String message;
		String errorLevel;
		if (options.sourceLevel < ClassFileConstants.JDK9) {
			message = "'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on";
			errorLevel = "WARNING";
		} else if (options.sourceLevel < ClassFileConstants.JDK22) {
			message = "'_' is a keyword from source level 9 onwards, cannot be used as identifier";
			errorLevel = "ERROR";
		} else if (options.sourceLevel == ClassFileConstants.JDK22) {
			message = "As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
			errorLevel = "ERROR";
		} else {
			message =  "Unnamed Patterns and Variables is a preview feature and disabled by default. Use --enable-preview to enable";
			errorLevel = "ERROR";
		}

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
		CompilerOptions options = new CompilerOptions(getCompilerOptions());

		String message;
		String errorLevel;
		if (options.sourceLevel < ClassFileConstants.JDK9) {
			message = "'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on";
			errorLevel = "WARNING";
		} else if (options.sourceLevel < ClassFileConstants.JDK22) {
			message = "'_' is a keyword from source level 9 onwards, cannot be used as identifier";
			errorLevel = "ERROR";
		} else if (options.sourceLevel == ClassFileConstants.JDK22) {
			message = "As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
			errorLevel = "ERROR";
		} else {
			message =  "Unnamed Patterns and Variables is a preview feature and disabled by default. Use --enable-preview to enable";
			errorLevel = "ERROR";
		}

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
		CompilerOptions options = new CompilerOptions(getCompilerOptions());

		String message;
		String errorLevel;
		if (options.sourceLevel < ClassFileConstants.JDK9) {
			message = "'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on";
			errorLevel = "WARNING";
		} else if (options.sourceLevel < ClassFileConstants.JDK22) {
			message = "'_' is a keyword from source level 9 onwards, cannot be used as identifier";
			errorLevel = "ERROR";
		} else if (options.sourceLevel == ClassFileConstants.JDK22) {
			message = "As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
			errorLevel = "ERROR";
		} else {
			message =  "Unnamed Patterns and Variables is a preview feature and disabled by default. Use --enable-preview to enable";
			errorLevel = "ERROR";
		}

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
		CompilerOptions options = new CompilerOptions(getCompilerOptions());

		String SOURCE_CODE = """
				public class A {
				public static void main(String[] args) {
					int \\u005F = 12;
					System.out.println("hello, world");
				}
			}
			""";

		if (options.sourceLevel < ClassFileConstants.JDK9) {
			runNegativeTest(new String[] { "A.java", SOURCE_CODE },
					"""
					----------
					1. WARNING in A.java (at line 3)
						int \\u005F = 12;
						    ^^^^^^
					'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
					----------
					""");
		} else if (options.sourceLevel < ClassFileConstants.JDK22) {
			runNegativeTest(new String[] { "A.java", SOURCE_CODE },
					"""
					----------
					1. ERROR in A.java (at line 3)
						int \\u005F = 12;
						    ^^^^^^
					'_' is a keyword from source level 9 onwards, cannot be used as identifier
					----------
					""");
		} else {
			runConformTest(new String[] { "A.java", SOURCE_CODE },
					"hello, world");
		}
	}

}
