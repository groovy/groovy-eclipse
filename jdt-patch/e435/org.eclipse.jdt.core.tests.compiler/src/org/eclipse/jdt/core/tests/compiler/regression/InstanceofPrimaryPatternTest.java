/*******************************************************************************
 * Copyright (c) 2021, 2021 IBM Corporation and others.
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

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class InstanceofPrimaryPatternTest extends AbstractRegressionTest {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 17 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test001" };
	}

	public static Class<?> testClass() {
		return InstanceofPrimaryPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public InstanceofPrimaryPatternTest(String testName){
		super(testName);
	}

	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}
	public void test001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof String s) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			"Hello World!");
	}
	public void test002() {
		String expectedDiagnostics = this.complianceLevel < ClassFileConstants.JDK20 ?
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof (String s)) {\n" +
				"	                   ^\n" +
				"Syntax error on token \"(\", delete this token\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof (String s)) {\n" +
				"	                             ^\n" +
				"Syntax error on token \")\", delete this token\n" +
				"----------\n" :
							"----------\n" +
							"1. ERROR in X.java (at line 3)\n" +
							"	if (obj instanceof (String s)) {\n" +
							"	        ^^^^^^^^^^\n" +
							"Syntax error on token \"instanceof\", ReferenceType expected after this token\n" +
							"----------\n";
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof (String s)) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			expectedDiagnostics);
	}
	public void test003() {

		String expectedDiagnostics = this.complianceLevel < ClassFileConstants.JDK20 ?
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	        ^^^^^^^^^^\n" +
				"Syntax error, insert \"Type\" to complete InstanceofClassic\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	        ^^^^^^^^^^\n" +
				"Syntax error, insert \") Statement\" to complete BlockStatements\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	                     ^^^^^^\n" +
				"Syntax error on token \"String\", ( expected after this token\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	                               ^\n" +
				"Syntax error, insert \"AssignmentOperator Expression\" to complete Assignment\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	                               ^\n" +
				"Syntax error, insert \";\" to complete Statement\n" +
				"----------\n" :
									"----------\n" +
									"1. ERROR in X.java (at line 3)\n" +
									"	if (obj instanceof ((String s))) {\n" +
									"	                   ^\n" +
									"Syntax error on token \"(\", invalid ReferenceType\n" +
									"----------\n" +
									"2. ERROR in X.java (at line 3)\n" +
									"	if (obj instanceof ((String s))) {\n" +
									"	                               ^\n" +
									"Syntax error on token \")\", delete this token\n" +
									"----------\n";

		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof ((String s))) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			expectedDiagnostics);
	}
	public void test007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof var s) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"		Zork();\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	if (obj instanceof var s) {\n" +
			"	                   ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void test009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(String s) {\n" +
				"		if (s instanceof Object o) {\n" +
				"			System.out.println(s1);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	System.out.println(s1);\n" +
			"	                   ^^\n" +
			"s1 cannot be resolved to a variable\n" +
			"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1076
	// ECJ accepts invalid Java code instanceof final Type
	public void testGH1076() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class Test {\n" +
				"    void check() {\n" +
				"        Number n = Integer.valueOf(1);\n" +
				"        if (n instanceof final Integer) {}\n" +
				"        if (n instanceof final Integer x) {}\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (n instanceof final Integer) {}\n" +
			"	                 ^^^^^^^^^^^^^\n" +
			"Syntax error, modifiers are not allowed here\n" +
			"----------\n");
	}

	public void testGH1621() {
		runConformTest(
			new String[] {
				"ConsumerEndpointSpec.java",
				"""
				import java.util.function.Consumer;

				public class ConsumerEndpointSpec {
					public void setNotPropagatedHeaders(String... headers) {
						for (String s: headers) System.out.print(s);
					}
					void foo(Object h) {
						if (h instanceof ConsumerEndpointSpec producingMessageHandler) {
							acceptIfNotEmpty(new String[] {"OK"}, producingMessageHandler::setNotPropagatedHeaders);
						}
					}
					public <T> void acceptIfNotEmpty(T[] value, Consumer<T[]> consumer) {
						consumer.accept(value);
					}
					public static void main(String... args) {
						ConsumerEndpointSpec obj = new ConsumerEndpointSpec();
						obj.foo(obj);
					}
				}
				"""
			},
			"OK",
			getCompilerOptions());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						Object obj = new Object();
						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) {
						    System.out.println();
						} else {
						    throw new IllegalArgumentException("invalid type"); // works OK without this line
						}

						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
						    System.out.println();
						}
						zork();
					}
				}
				"""
			},
			"----------\n"
			+ "1. ERROR in X.java (at line 17)\n"
			+ "	zork();\n"
			+ "	^^^^\n"
			+ "The method zork() is undefined for the type X\n"
			+ "----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_1() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						Object obj = new Object();
						if (obj instanceof Double c) {
						    System.out.println();
						} else {
						    throw new IllegalArgumentException("invalid type"); // works OK without this line
						}

						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
						    System.out.println();
						}
					}
				}
				"""
			},
			"----------\n"
			+ "1. ERROR in X.java (at line 12)\n"
			+ "	} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c\n"
			+ "	                                 ^\n"
			+ "A pattern variable with the same name is already defined in the statement\n"
			+ "----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						if (args != null) {
							Object obj = new Object();
							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						}
						zork();
					}
				}
				"""
			},
			"----------\n"
			+ "1. ERROR in X.java (at line 19)\n"
			+ "	zork();\n"
			+ "	^^^^\n"
			+ "The method zork() is undefined for the type X\n"
			+ "----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_3() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						if (args != null) {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						}
					}
				}
				"""
			},
			"----------\n"
			+ "1. ERROR in X.java (at line 13)\n"
			+ "	} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c\n"
			+ "	                                 ^\n"
			+ "A pattern variable with the same name is already defined in the statement\n"
			+ "----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_4() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						try {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						} catch (Exception e) {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						} finally {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						}
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c\n" +
			"	                                 ^\n" +
			"A pattern variable with the same name is already defined in the statement\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 26)\n" +
			"	} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c\n" +
			"	                                 ^\n" +
			"A pattern variable with the same name is already defined in the statement\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 39)\n" +
			"	} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c\n" +
			"	                                 ^\n" +
			"A pattern variable with the same name is already defined in the statement\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_5() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						synchronized(args) {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						}
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c\n" +
			"	                                 ^\n" +
			"A pattern variable with the same name is already defined in the statement\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_6() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
					    try {
					        gain(args);
					    } catch (IllegalArgumentException iae) {
					        if (iae.getMessage().equals("invalid type"))
					            System.out.println("All well");
					    }
					}
					public static void gain(String[] args) {
						Object obj = new Object();
						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) {
						    System.out.println();
						} else {
						    throw new IllegalArgumentException("invalid type"); // works OK without this line
						}

						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
						    System.out.println();
						}
					}
				}
				"""
			},
			"All well",
			getCompilerOptions());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_7() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
					    try {
					        gain(new String[] {"Hello"});
					    } catch (IllegalArgumentException iae) {
					        if (iae.getMessage().equals("invalid type"))
					            System.out.println("All well");
					    }
					}
					public static void gain(String[] args) {
						switch(args[0]) {
							case "Hello" :
								Object obj = new Object();
								if (obj instanceof Integer r) {
								    System.out.println();
								} else if (obj instanceof Double c) {
								    System.out.println();
								} else {
								    throw new IllegalArgumentException("invalid type"); // works OK without this line
								}

								if (obj instanceof Integer r) {
								    System.out.println();
								} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
								    System.out.println();
								}
						}
					}
				}
				"""
			},
			"All well",
			getCompilerOptions());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_8() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
					    try {
					        gain(new String[] {"Hello"});
					    } catch (IllegalArgumentException iae) {
					        if (iae.getMessage().equals("invalid type"))
					            System.out.println("All well");
					    }
					}
					public static void gain(String[] args) {
						switch(args[0]) {
							case "Hello" :
								Object obj = new Object();
								if (obj instanceof Double c) {
								    System.out.println();
								} else {
								    throw new IllegalArgumentException("invalid type"); // works OK without this line
								}

								if (obj instanceof Integer r) {
								    System.out.println();
								} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
								    System.out.println();
								}
						}
					}
				}
				"""
			},
			"----------\n"
			+ "1. ERROR in X.java (at line 22)\n"
			+ "	} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c\n"
			+ "	                                 ^\n"
			+ "A pattern variable with the same name is already defined in the statement\n"
			+ "----------\n");
	}

	public void testGH3074() {
		runNegativeTest(
			new String[] {
				"Example.java",
				"""
				class Example<T> {
					private void foo(String x) {
						if (x instanceof Example<String> es) {

						}
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in Example.java (at line 3)
				if (x instanceof Example<String> es) {
				    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Incompatible conditional operand types String and Example<String>
			----------
			""");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3222
	// [Patterns][Ternary] Pattern binding variable not recognized in poly conditional operator expression
	public void testIssue3222() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {

					interface I  {
						int foo();
					}

					static void foo(I i) {
				        System.out.println(i.foo());
				    }

					public static void main(String[] args) {
						foo(args instanceof String [] argv ? () -> argv.length + 13  : () -> 42);
					}

				}
				"""
			},
			"13");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3222
	// [Patterns][Ternary] Pattern binding variable not recognized in poly conditional operator expression
	public void testIssue3222_2() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {

					interface I  {
						int foo();
					}

					static void foo(I i) {
				        System.out.println(i.foo());
				    }

					public static void main(String[] args) {
						foo(!(args instanceof String [] argv) ? () -> 42 : () -> argv.length + 13);
					}

				}
				"""
			},
			"13");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3222
	// [Patterns][Ternary] Pattern binding variable not recognized in poly conditional operator expression
	public void testIssue3222_3() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.util.function.Supplier;

				public class X {
				  interface I {
				    int foo(Supplier<Integer> arg);
				    default int foo(Object arg) {
				      return 13;
				    }
				  }
				  public X() {
				    super();
				  }
				  public static void main(String[] argv) {
				    int i = ((I) (x) -> {
				  return x.get();
				}).foo((argv instanceof String [] args ? () -> args.length + 42 : (Supplier<Integer>) null));
				    System.out.println(i);
				  }
				}
				"""
			},
			"42");
	}
}