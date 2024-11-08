/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTestForMethod extends JavadocTest {
	public JavadocTestForMethod(String name) {
		super(name);
	}
	public static Class javadocTestClass() {
		return JavadocTestForMethod.class;
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(javadocTestClass());
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "Bug51529a", "Bug51529b" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 117, 124, 132, 137 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 21, 50 };
//		TESTS_RANGE = new int[] { -1, 50 }; // run all tests with a number less or equals to 50
//		TESTS_RANGE = new int[] { 10, -1 }; // run all tests with a number greater or equals to 10
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		return options;
	}

	/* (non-Javadoc)
	 * Test @deprecated tag
	 */
	public void test001() {
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	{\n"
					+ "		new Z().foo();\n"
					+ "	}\n"
					+ "}\n",
				"Z.java",
				"public class Z {\n"
					+ "  /** \n"
					+ "   * \n"
					+ "   * **   ** ** ** @deprecated */\n"
					+ "	public void foo() { \n"
					+ "	}\n"
					+ "}\n",
				},
			"----------\n"
				+ "1. WARNING in X.java (at line 4)\n"
				+ "	new Z().foo();\n"
				+ "	        ^^^^^\n"
				+ "The method foo() from the type Z is deprecated\n"
				+ "----------\n",
				null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}

	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	/** @deprecated */\n" +
				"	int x;\n" +
				"	/**\n" +
				"	 * @see #x\n" +
				"	 */\n" +
				"	void foo() {\n" +
				"	}\n" +
				"}\n",
				"Y.java",
				"/** @deprecated */\n" +
				"public class Y {\n" +
				"	int y;\n" +
				"	/**\n" +
				"	 * @see X#x\n" +
				"	 * @see Y\n" +
				"	 * @see Y#y\n" +
				"	 */\n" +
				"	void foo() {\n" +
				"	}\n" +
				"}\n",
				"Z.java",
				"public class Z {\n" +
				"	int z;\n" +
				"	/**\n" +
				"	 * @see X#x\n" +
				"	 * @see Y\n" +
				"	 * @see Y#y\n" +
				"	 */\n" +
				"	void foo() {\n" +
				"	}\n" +
				"}\n" },
		"----------\n" +
		"1. ERROR in Z.java (at line 4)\n" +
		"	* @see X#x\n" +
		"	         ^\n" +
		"Javadoc: The field X.x is deprecated\n" +
		"----------\n" +
		"2. ERROR in Z.java (at line 5)\n" +
		"	* @see Y\n" +
		"	       ^\n" +
		"Javadoc: The type Y is deprecated\n" +
		"----------\n" +
		"3. ERROR in Z.java (at line 6)\n" +
		"	* @see Y#y\n" +
		"	       ^\n" +
		"Javadoc: The type Y is deprecated\n" +
		"----------\n" +
		"4. ERROR in Z.java (at line 6)\n" +
		"	* @see Y#y\n" +
		"	         ^\n" +
		"Javadoc: The field Y.y is deprecated\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
			);
	}

	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	{\n"
					+ "		new Z().foo(2);\n"
					+ "	}\n"
					+ "}\n",
				"Z.java",
				"public class Z {\n"
					+ "  /** \n"
					+ "   * Valid tags with deprecation at end\n"
					+ "   *\n"
					+ "   * @param x Valid param tag\n"
					+ "   * @return Valid return tag\n"
					+ "   * @throws NullPointerException Valid throws tag\n"
					+ "   * @exception IllegalArgumentException Valid throws tag\n"
					+ "   * @see X Valid see tag\n"
					+ "   * @deprecated\n"
					+ "   */\n"
					+ "	public String foo(int x) { \n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n",
				},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	new Z().foo(2);\n" +
		"	        ^^^^^^\n" +
		"The method foo(int) from the type Z is deprecated\n" +
		"----------\n"
				);
	}

	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	{\n"
					+ "		new Z().foo(2);\n"
					+ "	}\n"
					+ "}\n",
				"Z.java",
				"public class Z {\n"
					+ "  /** \n"
					+ "   * Invalid javadoc tags with valid deprecation at end\n"
					+ "   *\n"
					+ "   * @param\n"
					+ "   * @return String\n"
					+ "   * @throws Unknown\n"
					+ "   * @see \"Invalid\n"
					+ "   * @see Unknown\n"
					+ "   * @param x\n"
					+ "   * @deprecated\n"
					+ "   */\n"
					+ "	public String foo(int x) { \n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n",
				},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	new Z().foo(2);\n" +
		"	        ^^^^^^\n" +
		"The method foo(int) from the type Z is deprecated\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in Z.java (at line 5)\n" +
		"	* @param\n" +
		"	   ^^^^^\n" +
		"Javadoc: Missing parameter name\n" +
		"----------\n" +
		"2. ERROR in Z.java (at line 7)\n" +
		"	* @throws Unknown\n" +
		"	          ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in Z.java (at line 8)\n" +
		"	* @see \"Invalid\n" +
		"	       ^^^^^^^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n" +
		"4. ERROR in Z.java (at line 9)\n" +
		"	* @see Unknown\n" +
		"	       ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in Z.java (at line 10)\n" +
		"	* @param x\n" +
		"	   ^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"6. ERROR in Z.java (at line 13)\n" +
		"	public String foo(int x) { \n" +
		"	                      ^\n" +
		"Javadoc: Missing tag for parameter x\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
				);
	}

	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	{\n"
					+ "		new Z().foo(2);\n"
					+ "	}\n"
					+ "}\n",
				"Z.java",
				"public class Z {\n"
					+ "  /** \n"
					+ "   * Valid tags with deprecation at beginning\n"
					+ "   *\n"
					+ "   * @deprecated\n"
					+ "   * @param x Valid param tag\n"
					+ "   * @return Valid return tag\n"
					+ "   * @exception IllegalArgumentException Valid throws tag\n"
					+ "   * @throws NullPointerException Valid throws tag\n"
					+ "   * @see X Valid see tag\n"
					+ "   */\n"
					+ "	public String foo(int x) { \n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n",
				},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	new Z().foo(2);\n" +
		"	        ^^^^^^\n" +
		"The method foo(int) from the type Z is deprecated\n" +
		"----------\n"
				);
	}

	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	{\n"
					+ "		new Z().foo(2);\n"
					+ "	}\n"
					+ "}\n",
				"Z.java",
				"public class Z {\n"
					+ "  /** \n"
					+ "   * Invalid javadoc tags with valid deprecation at beginning\n"
					+ "   *\n"
					+ "   * @deprecated\n"
					+ "   * @param\n"
					+ "   * @return String\n"
					+ "   * @throws Unknown\n"
					+ "   * @exception IllegalArgumentException Valid throws tag\n"
					+ "   * @see \"Invalid\n"
					+ "   * @see Unknown\n"
					+ "   * @param x\n"
					+ "   */\n"
					+ "	public String foo(int x) { \n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n",
				},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	new Z().foo(2);\n" +
		"	        ^^^^^^\n" +
		"The method foo(int) from the type Z is deprecated\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in Z.java (at line 6)\n" +
		"	* @param\n" +
		"	   ^^^^^\n" +
		"Javadoc: Missing parameter name\n" +
		"----------\n" +
		"2. ERROR in Z.java (at line 8)\n" +
		"	* @throws Unknown\n" +
		"	          ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in Z.java (at line 10)\n" +
		"	* @see \"Invalid\n" +
		"	       ^^^^^^^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n" +
		"4. ERROR in Z.java (at line 11)\n" +
		"	* @see Unknown\n" +
		"	       ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in Z.java (at line 12)\n" +
		"	* @param x\n" +
		"	   ^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"6. ERROR in Z.java (at line 14)\n" +
		"	public String foo(int x) { \n" +
		"	                      ^\n" +
		"Javadoc: Missing tag for parameter x\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
				);
	}

	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	{\n"
					+ "		new Z().foo(2);\n"
					+ "	}\n"
					+ "}\n",
				"Z.java",
				"public class Z {\n"
					+ "  /** \n"
					+ "   * Valid tags with deprecation in the middle\n"
					+ "   *\n"
					+ "   * @param x Valid param tag\n"
					+ "   * @return Valid return tag\n"
					+ "   * @deprecated\n"
					+ "   * @exception IllegalArgumentException Valid throws tag\n"
					+ "   * @throws NullPointerException Valid throws tag\n"
					+ "   * @see X Valid see tag\n"
					+ "   */\n"
					+ "	public String foo(int x) { \n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n",
				},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	new Z().foo(2);\n" +
		"	        ^^^^^^\n" +
		"The method foo(int) from the type Z is deprecated\n" +
		"----------\n"
				);
	}

	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	{\n"
					+ "		new Z().foo(2);\n"
					+ "	}\n"
					+ "}\n",
				"Z.java",
				"public class Z {\n"
					+ "  /** \n"
					+ "   * Invalid javadoc tags with valid deprecation in the middle\n"
					+ "   *\n"
					+ "   * @param\n"
					+ "   * @return String\n"
					+ "   * @throws Unknown\n"
					+ "   * @exception IllegalArgumentException Valid throws tag\n"
					+ "   * @see \"Invalid\n"
					+ "   * @deprecated\n"
					+ "   * @see Unknown\n"
					+ "   */\n"
					+ "	public String foo(int x) { \n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n",
				},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	new Z().foo(2);\n" +
		"	        ^^^^^^\n" +
		"The method foo(int) from the type Z is deprecated\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in Z.java (at line 5)\n" +
		"	* @param\n" +
		"	   ^^^^^\n" +
		"Javadoc: Missing parameter name\n" +
		"----------\n" +
		"2. ERROR in Z.java (at line 7)\n" +
		"	* @throws Unknown\n" +
		"	          ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in Z.java (at line 9)\n" +
		"	* @see \"Invalid\n" +
		"	       ^^^^^^^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n" +
		"4. ERROR in Z.java (at line 11)\n" +
		"	* @see Unknown\n" +
		"	       ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in Z.java (at line 13)\n" +
		"	public String foo(int x) { \n" +
		"	                      ^\n" +
		"Javadoc: Missing tag for parameter x\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
				);
	}

	/* (non-Javadoc)
	 * Test @param tag
	 */
	public void test011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid @param: no tags, no args\n"
					+ "	 * Valid @throws/@exception: no tags, no thrown exception\n"
					+ "	 */\n"
					+ "	public void p_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	public void p_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param declaration: no arguments, 2 declared tags\n"
					+ "	 * @param x\n"
					+ "	 * 			Invalid param: not an argument on 2 lines\n"
					+ "	 * @param x Invalid param: not an argument\n"
					+ "	 */\n"
					+ "	public void p_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @param x\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @param x Invalid param: not an argument\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test014() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	/**\n"
					+ "	 * Valid @param declaration: 3 arguments, 3 tags in right order\n"
					+ "	 * @param a Valid param\n"
					+ "	 * @param b Valid param \n"
					+ "	 * @param c Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(int a, int b, int c) {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param declaration: 3 arguments, 3 correct tags in right order + 2 additional\n"
					+ "	 * @param a Valid param\n"
					+ "	 * @param x Invalid param: not an argument\n"
					+ "	 * @param b Valid param \n"
					+ "	 * @param x Invalid param: not an argument\n"
					+ "	 * @param c Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(char a, char b, char c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @param x Invalid param: not an argument\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	* @param x Invalid param: not an argument\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid @param declaration: 3 arguments, 3 tags in wrong order\n"
					+ "	 * @param c Valid param, not well placed\n"
					+ "	 * @param b Valid param, not well placed \n"
					+ "	 * @param a Valid param, not well placed\n"
					+ "	 */\n"
					+ "	public void p_foo(long a, long b, long c) {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param declaration: 3 arguments, 3 correct tags in wrong order + 1 duplicate tag + 1 additional\n"
					+ "	 * @param c Valid param, not well placed\n"
					+ "	 * @param a Valid param, not well placed\n"
					+ "	 * @param b Valid param, not well placed \n"
					+ "	 * @param a Invalid param: duplicated\n"
					+ "	 * @param x Invalid param: not an argument\n"
					+ "	 */\n"
					+ "	public void p_foo(float a, float b, float c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 7)\n"
				+ "	* @param a Invalid param: duplicated\n"
				+ "	         ^\n"
				+ "Javadoc: Duplicate tag for parameter\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 8)\n"
				+ "	* @param x Invalid param: not an argument\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: all arguments are not documented\n"
					+ "	 */\n"
					+ "	public void p_foo(double a, double b, double c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	public void p_foo(double a, double b, double c) {\n"
				+ "	                         ^\n"
				+ "Javadoc: Missing tag for parameter a\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 5)\n"
				+ "	public void p_foo(double a, double b, double c) {\n"
				+ "	                                   ^\n"
				+ "Javadoc: Missing tag for parameter b\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 5)\n"
				+ "	public void p_foo(double a, double b, double c) {\n"
				+ "	                                             ^\n"
				+ "Javadoc: Missing tag for parameter c\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: b and c arguments are not documented\n"
					+ "	 * @param a Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(int a, char b, long c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	public void p_foo(int a, char b, long c) {\n"
				+ "	                              ^\n"
				+ "Javadoc: Missing tag for parameter b\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	public void p_foo(int a, char b, long c) {\n"
				+ "	                                      ^\n"
				+ "Javadoc: Missing tag for parameter c\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: a and c arguments are not documented\n"
					+ "	 * @param b Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(int a, char b, long c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	public void p_foo(int a, char b, long c) {\n"
				+ "	                      ^\n"
				+ "Javadoc: Missing tag for parameter a\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	public void p_foo(int a, char b, long c) {\n"
				+ "	                                      ^\n"
				+ "Javadoc: Missing tag for parameter c\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test023() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: a and b arguments are not documented\n"
					+ "	 * @param c Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(int a, char b, long c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	public void p_foo(int a, char b, long c) {\n"
				+ "	                      ^\n"
				+ "Javadoc: Missing tag for parameter a\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	public void p_foo(int a, char b, long c) {\n"
				+ "	                              ^\n"
				+ "Javadoc: Missing tag for parameter b\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test024() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: c argument is not documented\n"
					+ "	 * @param a Valid param\n"
					+ "	 * @param b Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(int a, char b, long c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 7)\n"
				+ "	public void p_foo(int a, char b, long c) {\n"
				+ "	                                      ^\n"
				+ "Javadoc: Missing tag for parameter c\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test025() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: a argument is not documented + b and c are not well placed\n"
					+ "	 * @param c Valid param\n"
					+ "	 * @param b Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(int a, char b, long c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 7)\n"
				+ "	public void p_foo(int a, char b, long c) {\n"
				+ "	                      ^\n"
				+ "Javadoc: Missing tag for parameter a\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test026() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: b argument is not documented + a and c are not well placed\n"
					+ "	 * @param c Valid param\n"
					+ "	 * @param a Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(int a, char b, long c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 7)\n"
				+ "	public void p_foo(int a, char b, long c) {\n"
				+ "	                              ^\n"
				+ "Javadoc: Missing tag for parameter b\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test030() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: mix of all possible errors (missing a, not argument tag and duplicated)\n"
					+ "	 * @param c Valid param\n"
					+ "	 * @param x Invalid param: not an argument\n"
					+ "	 * @param b Valid param\n"
					+ "	 * @param c Invalid param: duplicated\n"
					+ "	 */\n"
					+ "	public void p_foo(double a, long b, int c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @param x Invalid param: not an argument\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	* @param c Invalid param: duplicated\n"
				+ "	         ^\n"
				+ "Javadoc: Duplicate tag for parameter\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 9)\n"
				+ "	public void p_foo(double a, long b, int c) {\n"
				+ "	                         ^\n"
				+ "Javadoc: Missing tag for parameter a\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test031() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: missing parameter name\n"
					+ "	 * @param\n"
					+ "	 */\n"
					+ "	public void p_foo(String a) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @param\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Missing parameter name\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	public void p_foo(String a) {\n"
				+ "	                         ^\n"
				+ "Javadoc: Missing tag for parameter a\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test032() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: missing parameter name + valid param \n"
					+ "	 * @param\n"
					+ "	 * @param x\n"
					+ "	 */\n"
					+ "	public void p_foo(int x) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @param\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Missing parameter name\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test033() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: missing parameter names + valid params \n"
					+ "	 * @param h\n"
					+ "	 * @param\n"
					+ "	 * @param h\n"
					+ "	 * @param\n"
					+ "	 */\n"
					+ "	public void p_foo(java.util.Hashtable h, float f) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @param\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Missing parameter name\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @param h\n"
				+ "	         ^\n"
				+ "Javadoc: Duplicate tag for parameter\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @param\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Missing parameter name\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 9)\n"
				+ "	public void p_foo(java.util.Hashtable h, float f) {\n"
				+ "	                                               ^\n"
				+ "Javadoc: Missing tag for parameter f\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test034() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: missing parameter name + valid param \n"
					+ "	 * @param *\n"
					+ "	 * @param ?\n"
					+ "	 */\n"
					+ "	public void p_foo(int x) {\n"
					+ "	}\n"
					+ "}\n"
				},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param *\n" +
				"	         ^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	* @param ?\n" +
				"	         ^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	public void p_foo(int x) {\n" +
				"	                      ^\n" +
				"Javadoc: Missing tag for parameter x\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void test035() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid @param but compiler errors\n"
					+ "	 * @param a Valid param\n"
					+ "	 * @param b Valid param\n"
					+ "	 * @param c Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(inr a, int b, int c) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 8)\n"
				+ "	public void p_foo(inr a, int b, int c) {\n"
				+ "	                  ^^^\n"
				+ "inr cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test036() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param + compiler errors\n"
					+ "	 * @param b Valid param\n"
					+ "	 * @param b Valid param\n"
					+ "	 * @param c Valid param\n"
					+ "	 */\n"
					+ "	public void p_foo(inr a, inx b, inq c) {\n"
					+ "	}\n"
					+ "}\n" },
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	* @param b Valid param\n" +
				"	         ^\n" +
				"Javadoc: Duplicate tag for parameter\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	public void p_foo(inr a, inx b, inq c) {\n" +
				"	                  ^^^\n" +
				"inr cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 8)\n" +
				"	public void p_foo(inr a, inx b, inq c) {\n" +
				"	                      ^\n" +
				"Javadoc: Missing tag for parameter a\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	public void p_foo(inr a, inx b, inq c) {\n" +
				"	                         ^^^\n" +
				"inx cannot be resolved to a type\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 8)\n" +
				"	public void p_foo(inr a, inx b, inq c) {\n" +
				"	                                ^^^\n" +
				"inq cannot be resolved to a type\n" +
				"----------\n");
	}

	public void test037() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: class reference instead of param name\n"
					+ "	 * @param java.lang.Hashtable\n"
					+ "	 */\n"
					+ "	public void p_foo(int x) {\n"
					+ "	}\n"
					+ "}\n"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param java.lang.Hashtable\n" +
				"	         ^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	public void p_foo(int x) {\n" +
				"	                      ^\n" +
				"Javadoc: Missing tag for parameter x\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Hashtable;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: class reference instead of param name + unused import\n"
					+ "	 * @param Hashtable\n"
					+ "	 */\n"
					+ "	public void p_foo(int x) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n"+
			"	import java.util.Hashtable;\n"+
			"	       ^^^^^^^^^^^^^^^^^^^\n"+
			"The import java.util.Hashtable is never used\n"+
			"----------\n"+
			"2. ERROR in X.java (at line 5)\n" +
			"	* @param Hashtable\n" +
			"	         ^^^^^^^^^\n" +
			"Javadoc: Parameter Hashtable is not declared\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	public void p_foo(int x) {\n" +
			"	                      ^\n" +
			"Javadoc: Missing tag for parameter x\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/* (non-Javadoc)
	 * Test @throws/@exception tag
	 */
	public void test050() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid @throws tags: documented exception are unchecked\n"
					+ "	 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)\n"
					+ "	 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 */\n"
					+ "	public void t_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test051() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * @throws IllegalArgumenException.. Invalid exception: invalid class name\n"
					+ "	 * @exception IllegalArgumen..Exception.. Invalid exception: invalid class name\n"
					+ "	 */\n"
					+ "	public void t_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @throws IllegalArgumenException.. Invalid exception: invalid class name\n"
				+ "	         ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid class name\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 4)\n"
				+ "	* @exception IllegalArgumen..Exception.. Invalid exception: invalid class name\n"
				+ "	            ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid class name\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test052() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * @throws java.awt.AWTexception Invalid exception: unknown type\n"
					+ "	 * @throws IOException Invalid exception: unknown type\n"
					+ "	 */\n"
					+ "	public void t_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @throws java.awt.AWTexception Invalid exception: unknown type\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: java.awt.AWTexception cannot be resolved to a type\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 4)\n"
				+ "	* @throws IOException Invalid exception: unknown type\n"
				+ "	          ^^^^^^^^^^^\n"
				+ "Javadoc: IOException cannot be resolved to a type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test053() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.FileNotFoundException;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 */\n"
					+ "	public void t_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Exception EOFException is not declared\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 5)\n"
				+ "	* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Exception FileNotFoundException is not declared\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test055() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid @throws tags: documented exception are unchecked but method throws an unknown exception\n"
					+ "	 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)\n"
					+ "	 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 */\n"
					+ "	public void t_foo() throws InvalidException {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 9)\n"
				+ "	public void t_foo() throws InvalidException {\n"
				+ "	                           ^^^^^^^^^^^^^^^^\n"
				+ "InvalidException cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test056() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * @throws IllegalArgumenException._ Invalid exception: invalid class name\n"
					+ "	 * @exception IllegalArgumen.*.Exception.. Invalid exception: invalid class name\n"
					+ "	 */\n"
					+ "	public void t_foo() throws InvalidException {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @throws IllegalArgumenException._ Invalid exception: invalid class name\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: IllegalArgumenException cannot be resolved to a type\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 4)\n"
				+ "	* @exception IllegalArgumen.*.Exception.. Invalid exception: invalid class name\n"
				+ "	            ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid class name\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 6)\n"
				+ "	public void t_foo() throws InvalidException {\n"
				+ "	                           ^^^^^^^^^^^^^^^^\n"
				+ "InvalidException cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test057() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * @throws java.awt.AWTexception Invalid exception: unknown type\n"
					+ "	 * @throws IOException Invalid exception: unknown type\n"
					+ "	 */\n"
					+ "	public void t_foo() throws InvalidException {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @throws java.awt.AWTexception Invalid exception: unknown type\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: java.awt.AWTexception cannot be resolved to a type\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 4)\n"
				+ "	* @throws IOException Invalid exception: unknown type\n"
				+ "	          ^^^^^^^^^^^\n"
				+ "Javadoc: IOException cannot be resolved to a type\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 6)\n"
				+ "	public void t_foo() throws InvalidException {\n"
				+ "	                           ^^^^^^^^^^^^^^^^\n"
				+ "InvalidException cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test058() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.FileNotFoundException;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 */\n"
					+ "	public void t_foo() throws InvalidException {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Exception EOFException is not declared\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 5)\n"
				+ "	* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Exception FileNotFoundException is not declared\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	public void t_foo() throws InvalidException {\n"
				+ "	                           ^^^^^^^^^^^^^^^^\n"
				+ "InvalidException cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test060() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @throws tags: documented exception are unchecked but thrown exception is not documented\n"
					+ "	 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)\n"
					+ "	 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 */\n"
					+ "	public void t_foo() throws IllegalAccessException {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 9)\n"
				+ "	public void t_foo() throws IllegalAccessException {\n"
				+ "	                           ^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Missing tag for declared exception IllegalAccessException\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test061() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * @throws /IllegalArgumenException.. Invalid exception: invalid class name\n"
					+ "	 * @exception .IllegalArgumen..Exception.. Invalid exception: invalid class name\n"
					+ "	 */\n"
					+ "	public void t_foo() throws IllegalAccessException {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @throws /IllegalArgumenException.. Invalid exception: invalid class name\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Missing class name\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 4)\n"
				+ "	* @exception .IllegalArgumen..Exception.. Invalid exception: invalid class name\n"
				+ "	            ^^\n"
				+ "Javadoc: Invalid class name\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 6)\n"
				+ "	public void t_foo() throws IllegalAccessException {\n"
				+ "	                           ^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Missing tag for declared exception IllegalAccessException\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test062() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * @throws java.awt.AWTexception Invalid exception: unknown type\n"
					+ "	 * @throws IOException Invalid exception: unknown type\n"
					+ "	 */\n"
					+ "	public void t_foo() throws IllegalAccessException {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @throws java.awt.AWTexception Invalid exception: unknown type\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: java.awt.AWTexception cannot be resolved to a type\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 4)\n"
				+ "	* @throws IOException Invalid exception: unknown type\n"
				+ "	          ^^^^^^^^^^^\n"
				+ "Javadoc: IOException cannot be resolved to a type\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 6)\n"
				+ "	public void t_foo() throws IllegalAccessException {\n"
				+ "	                           ^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Missing tag for declared exception IllegalAccessException\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test063() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.FileNotFoundException;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 * @throws IOException Invalid exception: unknown type\n"
					+ "	 */\n"
					+ "	public void t_foo() throws IllegalAccessException {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Exception EOFException is not declared\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 5)\n"
				+ "	* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Exception FileNotFoundException is not declared\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 6)\n"
				+ "	* @throws IOException Invalid exception: unknown type\n"
				+ "	          ^^^^^^^^^^^\n"
				+ "Javadoc: IOException cannot be resolved to a type\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 8)\n"
				+ "	public void t_foo() throws IllegalAccessException {\n"
				+ "	                           ^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Missing tag for declared exception IllegalAccessException\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test065() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid @throws tags: documented exception are unchecked but some thrown exception are invalid\n"
					+ "	 * @throws IllegalAccessException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)\n"
					+ "	 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 */\n"
					+ "	public void t_foo() throws\n"
					+ "		IllegalAccessException, \n"
					+ "		InvalidException, \n"
					+ "		String, \n"
					+ "		IllegalArgumentException\n"
					+ "	{}\n"
					+ "}\n" },
					"----------\n" +
					"1. ERROR in X.java (at line 12)\n" +
					"	InvalidException, \n" +
					"	^^^^^^^^^^^^^^^^\n" +
					"InvalidException cannot be resolved to a type\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 13)\n" +
					"	String, \n" +
					"	^^^^^^\n" +
					"No exception of type String can be thrown; an exception type must be a subclass of Throwable\n" +
					"----------\n");
	}

	public void test066() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * @throws %IllegalArgumenException Invalid exception: invalid class name\n"
					+ "	 * @exception (IllegalArgumen Invalid exception: invalid class name\n"
					+ "	 * @exception \"IllegalArgumen Invalid exception: invalid class name\n"
					+ "	 */\n"
					+ "	public void t_foo() throws\n"
					+ "		IllegalAccessException, \n"
					+ "		InvalidException, \n"
					+ "		String, \n"
					+ "		IllegalArgumentException\n"
					+ "	{}\n"
					+ "}\n" },
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	* @throws %IllegalArgumenException Invalid exception: invalid class name\n" +
					"	   ^^^^^^\n" +
					"Javadoc: Missing class name\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 4)\n" +
					"	* @exception (IllegalArgumen Invalid exception: invalid class name\n" +
					"	   ^^^^^^^^^\n" +
					"Javadoc: Missing class name\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 5)\n" +
					"	* @exception \"IllegalArgumen Invalid exception: invalid class name\n" +
					"	   ^^^^^^^^^\n" +
					"Javadoc: Missing class name\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 8)\n" +
					"	IllegalAccessException, \n" +
					"	^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception IllegalAccessException\n" +
					"----------\n" +
					"5. ERROR in X.java (at line 9)\n" +
					"	InvalidException, \n" +
					"	^^^^^^^^^^^^^^^^\n" +
					"InvalidException cannot be resolved to a type\n" +
					"----------\n" +
					"6. ERROR in X.java (at line 10)\n" +
					"	String, \n" +
					"	^^^^^^\n" +
					"No exception of type String can be thrown; an exception type must be a subclass of Throwable\n" +
					"----------\n" +
					"7. ERROR in X.java (at line 11)\n" +
					"	IllegalArgumentException\n" +
					"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception IllegalArgumentException\n" +
					"----------\n");
	}

	public void test067() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * @throws java.awt.AWTexception Invalid exception: unknown type\n"
					+ "	 * @throws IOException Invalid exception: unknown type\n"
					+ "	 */\n"
					+ "	public void t_foo() throws\n"
					+ "		IllegalAccessException, \n"
					+ "		InvalidException, \n"
					+ "		String, \n"
					+ "		IllegalArgumentException\n"
					+ "	{}\n"
					+ "}\n" },
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	* @throws java.awt.AWTexception Invalid exception: unknown type\n" +
					"	          ^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: java.awt.AWTexception cannot be resolved to a type\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 4)\n" +
					"	* @throws IOException Invalid exception: unknown type\n" +
					"	          ^^^^^^^^^^^\n" +
					"Javadoc: IOException cannot be resolved to a type\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 7)\n" +
					"	IllegalAccessException, \n" +
					"	^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception IllegalAccessException\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 8)\n" +
					"	InvalidException, \n" +
					"	^^^^^^^^^^^^^^^^\n" +
					"InvalidException cannot be resolved to a type\n" +
					"----------\n" +
					"5. ERROR in X.java (at line 9)\n" +
					"	String, \n" +
					"	^^^^^^\n" +
					"No exception of type String can be thrown; an exception type must be a subclass of Throwable\n" +
					"----------\n" +
					"6. ERROR in X.java (at line 10)\n" +
					"	IllegalArgumentException\n" +
					"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception IllegalArgumentException\n" +
					"----------\n");
	}

	public void test068() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.FileNotFoundException;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 */\n"
					+ "	public void t_foo() throws\n"
					+ "		IllegalAccessException, \n"
					+ "		InvalidException, \n"
					+ "		String, \n"
					+ "		IllegalArgumentException\n"
					+ "	{}\n"
					+ "}\n" },
					"----------\n" +
					"1. ERROR in X.java (at line 4)\n" +
					"	* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n" +
					"	          ^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Exception EOFException is not declared\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 5)\n" +
					"	* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n" +
					"	          ^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Exception FileNotFoundException is not declared\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 8)\n" +
					"	IllegalAccessException, \n" +
					"	^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception IllegalAccessException\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 9)\n" +
					"	InvalidException, \n" +
					"	^^^^^^^^^^^^^^^^\n" +
					"InvalidException cannot be resolved to a type\n" +
					"----------\n" +
					"5. ERROR in X.java (at line 10)\n" +
					"	String, \n" +
					"	^^^^^^\n" +
					"No exception of type String can be thrown; an exception type must be a subclass of Throwable\n" +
					"----------\n" +
					"6. ERROR in X.java (at line 11)\n" +
					"	IllegalArgumentException\n" +
					"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception IllegalArgumentException\n" +
					"----------\n");
	}

	public void test069() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.FileNotFoundException;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 */\n"
					+ "	public void t_foo() throws\n"
					+ "		IllegalAccessException, \n"
					+ "		InvalidException, \n"
					+ "		String, \n"
					+ "		java.io.EOFException, \n"
					+ "		FileNotFoundException, \n"
					+ "		IOException, \n"
					+ "		IllegalArgumentException\n"
					+ "	{}\n"
					+ "}\n" },
					"----------\n" +
					"1. ERROR in X.java (at line 6)\n" +
					"	IllegalAccessException, \n" +
					"	^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception IllegalAccessException\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 7)\n" +
					"	InvalidException, \n" +
					"	^^^^^^^^^^^^^^^^\n" +
					"InvalidException cannot be resolved to a type\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 8)\n" +
					"	String, \n" +
					"	^^^^^^\n" +
					"No exception of type String can be thrown; an exception type must be a subclass of Throwable\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 9)\n" +
					"	java.io.EOFException, \n" +
					"	^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception EOFException\n" +
					"----------\n" +
					"5. ERROR in X.java (at line 10)\n" +
					"	FileNotFoundException, \n" +
					"	^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception FileNotFoundException\n" +
					"----------\n" +
					"6. ERROR in X.java (at line 11)\n" +
					"	IOException, \n" +
					"	^^^^^^^^^^^\n" +
					"IOException cannot be resolved to a type\n" +
					"----------\n" +
					"7. ERROR in X.java (at line 12)\n" +
					"	IllegalArgumentException\n" +
					"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception IllegalArgumentException\n" +
					"----------\n");
	}

	/* (non-Javadoc)
	 * Test @return tag
	 */
	public void test070() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid return declaration\n"
					+ "	 *\n"
					+ "	 * @return Return an int\n"
					+ "	 */\n"
					+ "	public int s_foo() {\n"
					+ "	  return 0;\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test071() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid empty return declaration\n"
					+ "	 *\n"
					+ "	 * @return string\n"
					+ "	 */\n"
					+ "	public String s_foo() {\n"
					+ "	  return \"\";\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test072() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid return declaration\n"
					+ "	 *\n"
					+ "	 * @return Vector A list of things\n"
					+ "	 */\n"
					+ "	public java.util.Vector s_foo() {\n"
					+ "	  return new java.util.Vector();\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test073() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Missing return declaration\n"
					+ "	 */\n"
					+ "	public Object[] s_foo() {\n"
					+ "	  return new Object[0];\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	public Object[] s_foo() {\n"
				+ "	       ^^^^^^^^\n"
				+ "Javadoc: Missing tag for return type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test074() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid return declaration\n"
					+ "	 *\n"
					+ "	 * @return double\n"
					+ "	 * @return Dimension\n"
					+ "	 */\n"
					+ "	public double s_foo() {\n"
					+ "	  return 3.14;\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	* @return Dimension\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Duplicate tag for return type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test075() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid return declaration\n"
					+ "	 *\n"
					+ "	 * @return Invalid return on void method\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @return Invalid return on void method\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid return declaration\n"
					+ "	 *\n"
					+ "	 * @return Invalid return on void method\n"
					+ "	 * @return\n"
					+ "	 * @return Invalid return on void method\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @return Invalid return on void method\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @return\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Duplicate tag for return type\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @return Invalid return on void method\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Duplicate tag for return type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/* (non-Javadoc)
	 * Test @see tag
	 */
	// String references
	public void test080() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid string references \n"
					+ "	 *\n"
					+ "	 * @see \"\n"
					+ "	 * @see \"unterminated string\n"
					+ "	 * @see \"invalid string\"\"\n"
					+ "	 * @see \"invalid\" no text allowed after the string\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see \"\n"
				+ "	       ^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @see \"unterminated string\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @see \"invalid string\"\"\n"
				+ "	                       ^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 8)\n"
				+ "	* @see \"invalid\" no text allowed after the string\n"
				+ "	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test081() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid string references \n"
					+ "	 *\n"
					+ "	 * @see \"Valid normal string\"\n"
					+ "	 * @see \"Valid \\\"string containing\\\" \\\"double-quote\\\"\"\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	// URL Link references
	public void test085() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid URL link references \n"
					+ "	 *\n"
					+ "	 * @see <\n"
					+ "	 * @see <a\n"
					+ "	 * @see <a hre\n"
					+ "	 * @see <a href\n"
					+ "	 * @see <a href=\n"
					+ "	 * @see <a href=\"\n"
					+ "	 * @see <a href=\"invalid\n"
					+ "	 * @see <a href=\"invalid\"\n"
					+ "	 * @see <a href=\"invalid\">\n"
					+ "	 * @see <a href=\"invalid\">invalid\n"
					+ "	 * @see <a href=\"invalid\">invalid<\n"
					+ "	 * @see <a href=\"invalid\">invalid</\n"
					+ "	 * @see <a href=\"invalid\">invalid</a\n"
					+ "	 * @see <a href=\"invalid\">invalid</a> no text allowed after the href\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see <\n"
				+ "	       ^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @see <a\n"
				+ "	       ^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @see <a hre\n"
				+ "	       ^^^^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 8)\n"
				+ "	* @see <a href\n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 9)\n"
				+ "	* @see <a href=\n"
				+ "	       ^^^^^^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "6. ERROR in X.java (at line 10)\n"
				+ "	* @see <a href=\"\n"
				+ "	       ^^^^^^^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "7. ERROR in X.java (at line 11)\n"
				+ "	* @see <a href=\"invalid\n"
				+ "	       ^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "8. ERROR in X.java (at line 12)\n"
				+ "	* @see <a href=\"invalid\"\n"
				+ "	       ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "9. ERROR in X.java (at line 13)\n"
				+ "	* @see <a href=\"invalid\">\n"
				+ "	       ^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "10. ERROR in X.java (at line 14)\n"
				+ "	* @see <a href=\"invalid\">invalid\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "11. ERROR in X.java (at line 15)\n"
				+ "	* @see <a href=\"invalid\">invalid<\n"
				+ "	                                ^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "12. ERROR in X.java (at line 16)\n"
				+ "	* @see <a href=\"invalid\">invalid</\n"
				+ "	                                ^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "13. ERROR in X.java (at line 17)\n"
				+ "	* @see <a href=\"invalid\">invalid</a\n"
				+ "	                                ^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "14. ERROR in X.java (at line 18)\n"
				+ "	* @see <a href=\"invalid\">invalid</a> no text allowed after the href\n"
				+ "	                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test086() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Valid URL references \n"
					+ "	 *\n"
					+ "	 * @see <a href=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Valid URL link reference</a>\n"
					+ "	 * @see <A HREF = \"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Valid URL link reference</A>\n"
					+ "	 * @see <a hReF = \"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Valid URL link reference</A>\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test087() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
				+ "	/**\n"
				+ "	 * Invalid URL references \n"
				+ "	 *\n"
				+ "	 * @see <a xref=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Invalid URL link reference</a>\n"
				+ "	 * @see <b href=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Invalid URL link reference</a>\n"
				+ "	 * @see <a href=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Invalid URL link reference</b>\n"
				+ "	 */\n"
				+ "	public void s_foo() {\n"
				+ "	}\n"
				+ "}\n" },
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	* @see <a xref=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Invalid URL link reference</a>\n" +
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Malformed link reference\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	* @see <b href=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Invalid URL link reference</a>\n" +
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Malformed link reference\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	* @see <a href=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Invalid URL link reference</b>\n" +
				"	                                                                                                                         ^^^^\n" +
				"Javadoc: Malformed link reference\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @see Classes references
	public void test090() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Valid local classes references \n"
					+ "	 *\n"
					+ "	 * @see Visibility Valid ref: local class \n"
					+ "	 * @see Visibility.VcPublic Valid ref: visible inner class of local class \n"
					+ "	 * @see AbstractVisibility.AvcPublic Valid ref: visible inner class of local class \n"
					+ "	 * @see test.Visibility Valid ref: local class \n"
					+ "	 * @see test.Visibility.VcPublic Valid ref: visible inner class of local class \n"
					+ "	 * @see test.AbstractVisibility.AvcPublic Valid ref: visible inner class of local class \n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test091() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local classes references \n"
					+ "	 *\n"
					+ "	 * @see Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
					+ "	 * @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
					+ "	 * @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
					+ "	 * @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
					+ "	 * @see Unknown Invalid ref: unknown class \n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see Unknown Invalid ref: unknown class \n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test092() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	public void test093() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid external classes references \n"
					+ "	 *\n"
					+ "	 * @see VisibilityPackage Invalid ref: non visible class \n"
					+ "	 * @see VisibilityPackage.VpPrivate Invalid ref: non visible inner class in non visible class \n"
					+ "	 * @see VisibilityPackage.VpPublic Invalid ref: visible inner class in non visible class \n"
					+ "	 * @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see VisibilityPackage Invalid ref: non visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see VisibilityPackage.VpPrivate Invalid ref: non visible inner class in non visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see VisibilityPackage.VpPublic Invalid ref: visible inner class in non visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n");
	}

	public void test094() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see test.copy.VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	public void test095() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid external classes references \n"
					+ "	 *\n"
					+ "	 * @see test.copy.VisibilityPackage Invalid ref: non visible class \n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate Invalid ref: non visible inner class in non visible class \n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic Invalid ref: visible inner class in non visible class \n"
					+ "	 * @see test.copy.VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see test.copy.VisibilityPackage Invalid ref: non visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate Invalid ref: non visible inner class in non visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic Invalid ref: visible inner class in non visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n");
	}

	// @see Field references
	public void test100() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	int x;\n"
					+ "	/**\n"
					+ "	 * Valid local class field references\n"
					+ "	 *\n"
					+ "	 * @see #x Valid ref: visible field\n"
					+ "	 * @see Visibility#vf_public Valid ref: visible field\n"
					+ "	 * @see Visibility.VcPublic#vf_public Valid ref: visible field in visible inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test101() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local class field references\n"
					+ "	 *\n"
					+ "	 * @see Visibility#unknown Invalid ref: non existent field\n"
					+ "	 * @see Visibility#vf_private Invalid ref: non visible field\n"
					+ "	 * @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)\n"
					+ "	 * @see Visibility.VcPrivate#vf_private Invalid ref: non visible inner class (non visible field)\n"
					+ "	 * @see Visibility.VcPrivate#vf_public Invalid ref: non visible inner class (public field)\n"
					+ "	 * @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class\n"
					+ "	 * @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#unknown Invalid ref: non existent field\n"
				+ "	                  ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility#vf_private Invalid ref: non visible field\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.VcPrivate#vf_private Invalid ref: non visible inner class (non visible field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see Visibility.VcPrivate#vf_public Invalid ref: non visible inner class (public field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class\n"
				+ "	                           ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "7. ERROR in test\\X.java (at line 12)\n"
				+ "	* @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n");
	}

	public void test102() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Valid super class field references in the same package\n"
					+ "	 *\n"
					+ "	 * @see Visibility#avf_public Valid ref: visible inherited field\n"
					+ "	 * @see AbstractVisibility.AvcPublic#avf_public Valid ref: visible field of visible inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test103() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid super class field references in the same package\n"
					+ "	 *\n"
					+ "	 * @see Visibility#avf_private Invalid ref: non visible inherited field\n"
					+ "	 * @see Visibility.AvcPrivate#avf_private Invalid ref: inherited non visible inner class (non visible field)\n"
					+ "	 * @see Visibility.AvcPrivate#avf_public Invalid ref: inherited non visible inner class (visible field)\n"
					+ "	 * @see Visibility.AvcPublic#avf_private Invalid ref: non visible field of inherited visible inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#avf_private Invalid ref: non visible inherited field\n"
				+ "	                  ^^^^^^^^^^^\n"
				+ "Javadoc: The field avf_private is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility.AvcPrivate#avf_private Invalid ref: inherited non visible inner class (non visible field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.AvcPrivate#avf_public Invalid ref: inherited non visible inner class (visible field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.AvcPublic#avf_private Invalid ref: non visible field of inherited visible inner class\n"
				+ "	                            ^^^^^^^^^^^\n"
				+ "Javadoc: The field avf_private is not visible\n"
				+ "----------\n");
	}

	public void test104() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package non visible class fields references\n"
					+ "	 *\n"
					+ "	 * @see VisibilityPackage#unknown Invalid ref: non visible class (non existent field)\n"
					+ "	 * @see VisibilityPackage#vf_private Invalid ref: non visible class (non existent field)\n"
					+ "	 * @see VisibilityPackage#vf_public Invalid ref: non visible class (visible field)\n"
					+ "	 * @see VisibilityPackage.VpPrivate#unknown Invalid ref: non visible class and non visible inner class (non existent field)\n"
					+ "	 * @see VisibilityPackage.VpPrivate#vf_private Invalid ref: non visible class and non visible inner class (non visible field)\n"
					+ "	 * @see VisibilityPackage.VpPrivate#vf_public Invalid ref: non visible class and non visible inner class (visible field)\n"
					+ "	 * @see VisibilityPackage.VpPublic#unknown Invalid ref: non visible class and visible inner class (non existent field)\n"
					+ "	 * @see VisibilityPackage.VpPublic#vf_private Invalid ref: non visible class and visible inner class (non visible field)\n"
					+ "	 * @see VisibilityPackage.VpPublic#vf_public Invalid ref: non visible class and visible inner class (visible field)\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n" +
			"1. WARNING in test\\X.java (at line 2)\n"+
			"	import test.copy.*;\n"+
			"	       ^^^^^^^^^\n"+
			"The import test.copy is never used\n"+
			"----------\n"+
			"2. ERROR in test\\X.java (at line 7)\n" +
			"	* @see VisibilityPackage#unknown Invalid ref: non visible class (non existent field)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"3. ERROR in test\\X.java (at line 8)\n" +
			"	* @see VisibilityPackage#vf_private Invalid ref: non visible class (non existent field)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"4. ERROR in test\\X.java (at line 9)\n" +
			"	* @see VisibilityPackage#vf_public Invalid ref: non visible class (visible field)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"5. ERROR in test\\X.java (at line 10)\n" +
			"	* @see VisibilityPackage.VpPrivate#unknown Invalid ref: non visible class and non visible inner class (non existent field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"6. ERROR in test\\X.java (at line 11)\n" +
			"	* @see VisibilityPackage.VpPrivate#vf_private Invalid ref: non visible class and non visible inner class (non visible field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"7. ERROR in test\\X.java (at line 12)\n" +
			"	* @see VisibilityPackage.VpPrivate#vf_public Invalid ref: non visible class and non visible inner class (visible field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"8. ERROR in test\\X.java (at line 13)\n" +
			"	* @see VisibilityPackage.VpPublic#unknown Invalid ref: non visible class and visible inner class (non existent field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"9. ERROR in test\\X.java (at line 14)\n" +
			"	* @see VisibilityPackage.VpPublic#vf_private Invalid ref: non visible class and visible inner class (non visible field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"10. ERROR in test\\X.java (at line 15)\n" +
			"	* @see VisibilityPackage.VpPublic#vf_public Invalid ref: non visible class and visible inner class (visible field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n");
	}

	public void test105() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package non visible class fields references\n"
					+ "	 *\n"
					+ "	 * @see test.copy.VisibilityPackage#unknown Invalid ref: non visible class (non existent field)\n"
					+ "	 * @see test.copy.VisibilityPackage#vf_private Invalid ref: non visible class (non existent field)\n"
					+ "	 * @see test.copy.VisibilityPackage#vf_public Invalid ref: non visible class (visible field)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#unknown Invalid ref: non visible class and non visible inner class (non existent field)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#vf_private Invalid ref: non visible class and non visible inner class (non visible field)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#vf_public Invalid ref: non visible class and non visible inner class (visible field)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#unknown Invalid ref: non visible class and visible inner class (non existent field)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#vf_private Invalid ref: non visible class and visible inner class (non visible field)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#vf_public Invalid ref: non visible class and visible inner class (visible field)\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see test.copy.VisibilityPackage#unknown Invalid ref: non visible class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see test.copy.VisibilityPackage#vf_private Invalid ref: non visible class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see test.copy.VisibilityPackage#vf_public Invalid ref: non visible class (visible field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#unknown Invalid ref: non visible class and non visible inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#vf_private Invalid ref: non visible class and non visible inner class (non visible field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#vf_public Invalid ref: non visible class and non visible inner class (visible field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "7. ERROR in test\\X.java (at line 12)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#unknown Invalid ref: non visible class and visible inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "8. ERROR in test\\X.java (at line 13)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#vf_private Invalid ref: non visible class and visible inner class (non visible field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "9. ERROR in test\\X.java (at line 14)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#vf_public Invalid ref: non visible class and visible inner class (visible field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n");
	}

	public void test106() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package non visible class fields references\n"
					+ "	 *\n"
					+ "	 * @see VisibilityPublic#unknown Invalid ref to non existent field of other package class\n"
					+ "	 * @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n"
					+ "	 * @see VisibilityPublic#vf_public Valid ref to not visible field of other package class\n"
					+ "	 * @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n"
					+ "	 * @see VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)\n"
					+ "	 * @see VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)\n"
					+ "	 * @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n"
					+ "	 * @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n"
					+ "	 * @see VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class\n"
				+ "	       ^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: VisibilityPublic cannot be resolved to a type\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n"
				+ "	       ^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: VisibilityPublic cannot be resolved to a type\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see VisibilityPublic#vf_public Valid ref to not visible field of other package class\n"
				+ "	       ^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: VisibilityPublic cannot be resolved to a type\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: VisibilityPublic cannot be resolved to a type\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: VisibilityPublic cannot be resolved to a type\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: VisibilityPublic cannot be resolved to a type\n"
				+ "----------\n"
				+ "7. ERROR in test\\X.java (at line 12)\n"
				+ "	* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: VisibilityPublic cannot be resolved to a type\n"
				+ "----------\n"
				+ "8. ERROR in test\\X.java (at line 13)\n"
				+ "	* @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: VisibilityPublic cannot be resolved to a type\n"
				+ "----------\n"
				+ "9. ERROR in test\\X.java (at line 14)\n"
				+ "	* @see VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: VisibilityPublic cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test107() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Invalid other package non visible class fields references\n" +
				"	 *\n" +
				"	 * @see VisibilityPublic#vf_public Valid ref to visible field of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vf_public Fully Qualified valid ref to visible field of other package public inner class\n" +
				"	 */\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	public void test108() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Invalid other package non visible class fields references\n" +
				"	 *\n" +
				"	 * @see VisibilityPublic#unknown Invalid ref to non existent field of other package class\n" +
				"	 * @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n" +
				"	 * @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n" +
				"	 * @see VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)\n" +
				"	 * @see VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)\n" +
				"	 * @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n" +
				"	 * @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n" +
				"	 */\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in test\\X.java (at line 7)\n" +
			"	* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class\n" +
			"	                        ^^^^^^^\n" +
			"Javadoc: unknown cannot be resolved or is not a field\n" +
			"----------\n" +
			"2. ERROR in test\\X.java (at line 8)\n" +
			"	* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n" +
			"	                        ^^^^^^^^^^\n" +
			"Javadoc: The field vf_private is not visible\n" +
			"----------\n" +
			"3. ERROR in test\\X.java (at line 9)\n" +
			"	* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPublic.VpPrivate is not visible\n" +
			"----------\n" +
			"4. ERROR in test\\X.java (at line 10)\n" +
			"	* @see VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPublic.VpPrivate is not visible\n" +
			"----------\n" +
			"5. ERROR in test\\X.java (at line 11)\n" +
			"	* @see VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPublic.VpPrivate is not visible\n" +
			"----------\n" +
			"6. ERROR in test\\X.java (at line 12)\n" +
			"	* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n" +
			"	                                 ^^^^^^^\n" +
			"Javadoc: unknown cannot be resolved or is not a field\n" +
			"----------\n" +
			"7. ERROR in test\\X.java (at line 13)\n" +
			"	* @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n" +
			"	                                 ^^^^^^^^^^\n" +
			"Javadoc: The field vf_private is not visible\n" +
			"----------\n"
		);
	}

	public void test109() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Invalid other package non visible class fields references\n" +
				"	 *\n" +
				"	 * @see test.copy.VisibilityPublic#vf_public Valid ref to not visible field of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class\n" +
				"	 */\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	public void test110() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Invalid other package non visible class fields references\n" +
				"	 *\n" +
				"	 * @see test.copy.VisibilityPublic#unknown Invalid ref to non existent field of other package class\n" +
				"	 * @see test.copy.VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n" +
				"	 * @see test.copy.VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)\n" +
				"	 * @see test.copy.VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n" +
				"	 */\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in test\\X.java (at line 6)\n" +
			"	* @see test.copy.VisibilityPublic#unknown Invalid ref to non existent field of other package class\n" +
			"	                                  ^^^^^^^\n" +
			"Javadoc: unknown cannot be resolved or is not a field\n" +
			"----------\n" +
			"2. ERROR in test\\X.java (at line 7)\n" +
			"	* @see test.copy.VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n" +
			"	                                  ^^^^^^^^^^\n" +
			"Javadoc: The field vf_private is not visible\n" +
			"----------\n" +
			"3. ERROR in test\\X.java (at line 8)\n" +
			"	* @see test.copy.VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible\n" +
			"----------\n" +
			"4. ERROR in test\\X.java (at line 9)\n" +
			"	* @see test.copy.VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible\n" +
			"----------\n" +
			"5. ERROR in test\\X.java (at line 10)\n" +
			"	* @see test.copy.VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible\n" +
			"----------\n" +
			"6. ERROR in test\\X.java (at line 11)\n" +
			"	* @see test.copy.VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n" +
			"	                                           ^^^^^^^\n" +
			"Javadoc: unknown cannot be resolved or is not a field\n" +
			"----------\n" +
			"7. ERROR in test\\X.java (at line 12)\n" +
			"	* @see test.copy.VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n" +
			"	                                           ^^^^^^^^^^\n" +
			"Javadoc: The field vf_private is not visible\n" +
			"----------\n"
		);
	}

	// @see local method references
	public void test115() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Valid local methods references with array\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) Valid local method reference\n"
					+ "	 * @see #smr_foo(char[], int[][], String[][][], Vector[][][][]) Valid local method reference\n"
					+ "	 * @see #smr_foo(char[],int[][],java.lang.String[][][],java.util.Vector[][][][]) Valid local method reference\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test116() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references with array (wrong brackets peer)\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char[ , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char] , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][, String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[]], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[[], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][, Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][]], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][[], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[]][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[[][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][][) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][]]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][[]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][]][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][[][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[]][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[[][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(char[ , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(char] , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 8)\n"
				+ "	* @see #smr_foo(char[] , int[][, String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 9)\n"
				+ "	* @see #smr_foo(char[] , int[]], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 10)\n"
				+ "	* @see #smr_foo(char[] , int[[], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "6. ERROR in X.java (at line 11)\n"
				+ "	* @see #smr_foo(char[] , int][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "7. ERROR in X.java (at line 12)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][, Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "8. ERROR in X.java (at line 13)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][]], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "9. ERROR in X.java (at line 14)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][[], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "10. ERROR in X.java (at line 15)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[]][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "11. ERROR in X.java (at line 16)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[[][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "12. ERROR in X.java (at line 17)\n"
				+ "	* @see #smr_foo(char[] , int[][], String][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "13. ERROR in X.java (at line 18)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][][][) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "14. ERROR in X.java (at line 19)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][][]]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "15. ERROR in X.java (at line 20)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][][[]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "16. ERROR in X.java (at line 21)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][]][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "17. ERROR in X.java (at line 22)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][[][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "18. ERROR in X.java (at line 23)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[]][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "19. ERROR in X.java (at line 24)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[[][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "20. ERROR in X.java (at line 25)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test117() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references with array (non applicable arrays)\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int, String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String, Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector) Invalid ref: invalid arguments declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(char , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char, int[][], String[][][], Vector[][][][])\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(char[] , int[], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[], String[][][], Vector[][][][])\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 8)\n"
				+ "	* @see #smr_foo(char[] , int, String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int, String[][][], Vector[][][][])\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 9)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][], Vector[][][][])\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 10)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[], Vector[][][][])\n"
				+ "----------\n"
				+ "6. ERROR in X.java (at line 11)\n"
				+ "	* @see #smr_foo(char[] , int[][], String, Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String, Vector[][][][])\n"
				+ "----------\n"
				+ "7. ERROR in X.java (at line 12)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector[][][])\n"
				+ "----------\n"
				+ "8. ERROR in X.java (at line 13)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector[][])\n"
				+ "----------\n"
				+ "9. ERROR in X.java (at line 14)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector[])\n"
				+ "----------\n"
				+ "10. ERROR in X.java (at line 15)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector)\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test118() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references with array (non applicable arrays)\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char[1] , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[2][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][3], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[4][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][5][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][6], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[7][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][8][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][9][]) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][][10]) Invalid ref: invalid arguments declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(char[1] , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(char[] , int[2][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 8)\n"
				+ "	* @see #smr_foo(char[] , int[][3], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 9)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[4][][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 10)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][5][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "6. ERROR in X.java (at line 11)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][6], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "7. ERROR in X.java (at line 12)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[7][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "8. ERROR in X.java (at line 13)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][8][][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "9. ERROR in X.java (at line 14)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][][9][]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "10. ERROR in X.java (at line 15)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][][][10]) Invalid ref: invalid arguments declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test120() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo() Valid local method reference\n"
					+ "	 * @see #smr_foo(boolean, int, byte, short, char, long, float, double) Valid local method reference\n"
					+ "	 * @see #smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference\n"
					+ "	 * @see #smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) Valid local method reference\n"
					+ "	 * @see #smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference\n"
					+ "	 * @see #smr_foo(String, String, int) Valid local method reference\n"
					+ "	 * @see #smr_foo(java.lang.String, String, int) Valid local method reference   \n"
					+ "	 * @see #smr_foo(String, java.lang.String, int) Valid local method reference   \n"
					+ "	 * @see #smr_foo(java.lang.String, java.lang.String, int) Valid local method reference   \n"
					+ "	 * @see #smr_foo(String x,String y,int z) Valid local method reference   \n"
					+ "	 * @see #smr_foo(java.lang.String x,String y, int z) Valid local method reference   \n"
					+ "	 * @see #smr_foo(String x,java.lang.String y,int z) Valid local method reference   \n"
					+ "	 * @see #smr_foo(java.lang.String x,java.lang.String y,int z) Valid local method reference   \n"
					+ "	 * @see #smr_foo(java.util.Hashtable,java.util.Vector,boolean) Valid local method reference\n"
					+ "	 * @see #smr_foo(java.util.Hashtable,Vector,boolean) Valid local method reference\n"
					+ "	 * @see #smr_foo(java.util.Hashtable a, java.util.Vector b, boolean c) Valid local method reference\n"
					+ "	 * @see #smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo() {\n"
					+ "	}\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test121() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #unknown() Invalid ref: undefined local method reference\n"
					+ "	 * @see #smrfoo() Invalid ref: undefined local method reference\n"
					+ "	 * @see #smr_FOO() Invalid ref: undefined local method reference\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see #unknown() Invalid ref: undefined local method reference\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type X\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @see #smrfoo() Invalid ref: undefined local method reference\n"
				+ "	        ^^^^^^\n"
				+ "Javadoc: The method smrfoo() is undefined for the type X\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_FOO() Invalid ref: undefined local method reference\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_FOO() is undefined for the type X\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test122() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(boolean, int, byte, short, char, long, float) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean, int, byte, short, char, long) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean, int, byte, short, char) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean, int, byte, short) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean, int, byte) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean, int) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean) Invalid ref: local method not applicable\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see #smr_foo(boolean, int, byte, short, char, long, float) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte, short, char, long, float)\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(boolean, int, byte, short, char, long) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte, short, char, long)\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(boolean, int, byte, short, char) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte, short, char)\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 8)\n"
				+ "	* @see #smr_foo(boolean, int, byte, short) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte, short)\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 9)\n"
				+ "	* @see #smr_foo(boolean, int, byte) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte)\n"
				+ "----------\n"
				+ "6. ERROR in X.java (at line 10)\n"
				+ "	* @see #smr_foo(boolean, int) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int)\n"
				+ "----------\n"
				+ "7. ERROR in X.java (at line 11)\n"
				+ "	* @see #smr_foo(boolean) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean)\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test123() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(int, String, String) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(String, int, String) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(String, String) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(String) Invalid ref: local method not applicable\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see #smr_foo(int, String, String) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (int, String, String)\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(String, int, String) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, int, String)\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(String, String) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, String)\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 8)\n"
				+ "	* @see #smr_foo(String) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String)\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test124() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(java.util.Hashtable,java.util.Vector) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(java.util.Hashtable,boolean,java.util.Vector) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean,java.util.Hashtable,java.util.Vector) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(java.util.Hashtable) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(java.util.Vector) Invalid ref: local method not applicable\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see #smr_foo(java.util.Hashtable,java.util.Vector) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable, Vector)\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(java.util.Hashtable,boolean,java.util.Vector) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable, boolean, Vector)\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(boolean,java.util.Hashtable,java.util.Vector) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (boolean, Hashtable, Vector)\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 8)\n"
				+ "	* @see #smr_foo(java.util.Hashtable) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable)\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 9)\n"
				+ "	* @see #smr_foo(java.util.Vector) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Vector)\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test125() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(boolean,int i,byte y,short s,char c,long l,float f,double d) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(boolean b,int, byte y, short s, char c, long l, float f, double d) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(boolean b,int i,byte,short s,char c,long l,float f,double d) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(boolean b,int i,byte y,short,char c,long l,float f,double d) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(boolean b,int i,byte y,short s,char,long l,float f,double d) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(boolean b,int i,byte y,short s,char c,long,float f,double d) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(boolean b,int i,byte y,short s,char c,long l,float,double d) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(boolean b,int i,byte y,short s,char c,long l,float f,double) Invalid reference: mixed argument declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see #smr_foo(boolean,int i,byte y,short s,char c,long l,float f,double d) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(boolean b,int, byte y, short s, char c, long l, float f, double d) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(boolean b,int i,byte,short s,char c,long l,float f,double d) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 8)\n"
				+ "	* @see #smr_foo(boolean b,int i,byte y,short,char c,long l,float f,double d) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 9)\n"
				+ "	* @see #smr_foo(boolean b,int i,byte y,short s,char,long l,float f,double d) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "6. ERROR in X.java (at line 10)\n"
				+ "	* @see #smr_foo(boolean b,int i,byte y,short s,char c,long,float f,double d) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "7. ERROR in X.java (at line 11)\n"
				+ "	* @see #smr_foo(boolean b,int i,byte y,short s,char c,long l,float,double d) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "8. ERROR in X.java (at line 12)\n"
				+ "	* @see #smr_foo(boolean b,int i,byte y,short s,char c,long l,float f,double) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test126() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(String,String y,int z) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(java.lang.String x,String, int z) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(String x,java.lang.String y,int) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(java.lang.String,java.lang.String,int z) Invalid reference: mixed argument declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see #smr_foo(String,String y,int z) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(java.lang.String x,String, int z) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(String x,java.lang.String y,int) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 8)\n"
				+ "	* @see #smr_foo(java.lang.String,java.lang.String,int z) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test127() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(Hashtable,java.util.Vector,boolean) Invalid reference: unresolved argument type\n"
					+ "	 * @see #smr_foo(Hashtable,Vector,boolean) Invalid reference: unresolved argument type\n"
					+ "	 * @see #smr_foo(Hashtable a, java.util.Vector b, boolean c) Invalid reference: unresolved argument type\n"
					+ "	 * @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
					+ "	 * @see #smr_foo(java.util.Hashtable a, java.util.Vector b, boolean) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(java.util.Hashtable, Vector, boolean c) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(Hashtable a, java.util.Vector, boolean c) Invalid reference: mixed argument declaration\n"
					+ "	 * @see #smr_foo(Hashtable, Vector b, boolean c) Invalid reference: mixed argument declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(Hashtable,java.util.Vector,boolean) Invalid reference: unresolved argument type\n"
				+ "	                ^^^^^^^^^\n"
				+ "Javadoc: Hashtable cannot be resolved to a type\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(Hashtable,Vector,boolean) Invalid reference: unresolved argument type\n"
				+ "	                ^^^^^^^^^\n"
				+ "Javadoc: Hashtable cannot be resolved to a type\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 8)\n"
				+ "	* @see #smr_foo(Hashtable a, java.util.Vector b, boolean c) Invalid reference: unresolved argument type\n"
				+ "	                ^^^^^^^^^\n"
				+ "Javadoc: Hashtable cannot be resolved to a type\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 9)\n"
				+ "	* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
				+ "	                ^^^^^^^^^\n"
				+ "Javadoc: Hashtable cannot be resolved to a type\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 10)\n"
				+ "	* @see #smr_foo(java.util.Hashtable a, java.util.Vector b, boolean) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "6. ERROR in X.java (at line 11)\n"
				+ "	* @see #smr_foo(java.util.Hashtable, Vector, boolean c) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "7. ERROR in X.java (at line 12)\n"
				+ "	* @see #smr_foo(Hashtable a, java.util.Vector, boolean c) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "8. ERROR in X.java (at line 13)\n"
				+ "	* @see #smr_foo(Hashtable, Vector b, boolean c) Invalid reference: mixed argument declaration\n"
				+ "	               ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test130() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see X#smr_foo() Valid local method reference\n"
					+ "	 * @see X#smr_foo(boolean, int, byte, short, char, long, float, double) Valid local method reference\n"
					+ "	 * @see X#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference\n"
					+ "	 * @see X#smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) Valid local method reference\n"
					+ "	 * @see X#smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference\n"
					+ "	 * @see X#smr_foo(String, String, int) Valid local method reference\n"
					+ "	 * @see X#smr_foo(java.lang.String, String, int) Valid local method reference   \n"
					+ "	 * @see X#smr_foo(String, java.lang.String, int) Valid local method reference   \n"
					+ "	 * @see X#smr_foo(java.lang.String, java.lang.String, int) Valid local method reference   \n"
					+ "	 * @see X#smr_foo(String x,String y,int z) Valid local method reference   \n"
					+ "	 * @see X#smr_foo(java.lang.String x,String y, int z) Valid local method reference   \n"
					+ "	 * @see X#smr_foo(String x,java.lang.String y,int z) Valid local method reference   \n"
					+ "	 * @see X#smr_foo(java.lang.String x,java.lang.String y,int z) Valid local method reference   \n"
					+ "	 * @see X#smr_foo(java.util.Hashtable,java.util.Vector,boolean) Valid local method reference\n"
					+ "	 * @see X#smr_foo(java.util.Hashtable,Vector,boolean) Valid local method reference\n"
					+ "	 * @see X#smr_foo(java.util.Hashtable a, java.util.Vector b, boolean c) Valid local method reference\n"
					+ "	 * @see X#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo() {\n"
					+ "	}\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test131() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see X#unknown() Invalid ref: undefined local method reference\n"
					+ "	 * @see X#smrfoo() Invalid ref: undefined local method reference\n"
					+ "	 * @see X#smr_FOO() Invalid ref: undefined local method reference\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see X#unknown() Invalid ref: undefined local method reference\n"
				+ "	         ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type X\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	* @see X#smrfoo() Invalid ref: undefined local method reference\n"
				+ "	         ^^^^^^\n"
				+ "Javadoc: The method smrfoo() is undefined for the type X\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 7)\n"
				+ "	* @see X#smr_FOO() Invalid ref: undefined local method reference\n"
				+ "	         ^^^^^^^\n"
				+ "Javadoc: The method smr_FOO() is undefined for the type X\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test132() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see X#smr_foo(Object) Invalid ref: local method not applicable\n"
					+ "	 * @see X#smr_foo(int, byte, short, char, long, float, double) Invalid ref: local method not applicable\n"
					+ "	 * @see X#smr_foo(String, int) Invalid ref: local method not applicable\n"
					+ "	 * @see X#smr_foo(String) Invalid ref: local method not applicable\n"
					+ "	 * @see X#smr_foo(java.util.Hashtable,Vector) Invalid ref: local method not applicable\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo() {\n"
					+ "	}\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	* @see X#smr_foo(Object) Invalid ref: local method not applicable\n"
				+ "	         ^^^^^^^\n"
				+ "Javadoc: The method smr_foo() in the type X is not applicable for the arguments (Object)\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	* @see X#smr_foo(int, byte, short, char, long, float, double) Invalid ref: local method not applicable\n"
				+ "	         ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (int, byte, short, char, long, float, double)\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 8)\n"
				+ "	* @see X#smr_foo(String, int) Invalid ref: local method not applicable\n"
				+ "	         ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, int)\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 9)\n"
				+ "	* @see X#smr_foo(String) Invalid ref: local method not applicable\n"
				+ "	         ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String)\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 10)\n"
				+ "	* @see X#smr_foo(java.util.Hashtable,Vector) Invalid ref: local method not applicable\n"
				+ "	         ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable, Vector)\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test133() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see X#smr_foo(boolean,int i,byte,short s,char,long l,float,double d) Invalid reference: mixed argument declaration\n"
					+ "	 * @see X#smr_foo(String,String y,int) Invalid reference: mixed argument declaration\n"
					+ "	 * @see X#smr_foo(Hashtable,Vector,boolean) Invalid reference: unresolved argument type\n"
					+ "	 * @see X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	* @see X#smr_foo(boolean,int i,byte,short s,char,long l,float,double d) Invalid reference: mixed argument declaration\n"
				+ "	                ^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	* @see X#smr_foo(String,String y,int) Invalid reference: mixed argument declaration\n"
				+ "	                ^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 8)\n"
				+ "	* @see X#smr_foo(Hashtable,Vector,boolean) Invalid reference: unresolved argument type\n"
				+ "	                 ^^^^^^^^^\n"
				+ "Javadoc: Hashtable cannot be resolved to a type\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 9)\n"
				+ "	* @see X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration\n"
				+ "	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test135() {
		this.runConformTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"package test.deep.qualified.name.p;\n"
					+ "import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo() Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(boolean, int, byte, short, char, long, float, double) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(String, String, int) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.lang.String, String, int) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(String, java.lang.String, int) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.lang.String, java.lang.String, int) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(String x,String y,int z) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.lang.String x,String y, int z) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(String x,java.lang.String y,int z) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.lang.String x,java.lang.String y,int z) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable,java.util.Vector,boolean) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable,Vector,boolean) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable a, java.util.Vector b, boolean c) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo() {\n"
					+ "	}\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test136() {
		this.runNegativeTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"package test.deep.qualified.name.p;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see test.deep.qualified.name.p.X#unknown() Invalid ref: undefined local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smrfoo() Invalid ref: undefined local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_FOO() Invalid ref: undefined local method reference\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 6)\n"
				+ "	* @see test.deep.qualified.name.p.X#unknown() Invalid ref: undefined local method reference\n"
				+ "	                                    ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type X\n"
				+ "----------\n"
				+ "2. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 7)\n"
				+ "	* @see test.deep.qualified.name.p.X#smrfoo() Invalid ref: undefined local method reference\n"
				+ "	                                    ^^^^^^\n"
				+ "Javadoc: The method smrfoo() is undefined for the type X\n"
				+ "----------\n"
				+ "3. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 8)\n"
				+ "	* @see test.deep.qualified.name.p.X#smr_FOO() Invalid ref: undefined local method reference\n"
				+ "	                                    ^^^^^^^\n"
				+ "Javadoc: The method smr_FOO() is undefined for the type X\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test137() {
		this.runNegativeTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"package test.deep.qualified.name.p;\n"
					+ "import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(Object) Invalid ref: local method not applicable\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(int, byte, short, char, long, float, double) Invalid ref: local method not applicable\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(String, int) Invalid ref: local method not applicable\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(String) Invalid ref: local method not applicable\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable,Vector) Invalid ref: local method not applicable\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo() {\n"
					+ "	}\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 7)\n"
				+ "	* @see test.deep.qualified.name.p.X#smr_foo(Object) Invalid ref: local method not applicable\n"
				+ "	                                    ^^^^^^^\n"
				+ "Javadoc: The method smr_foo() in the type X is not applicable for the arguments (Object)\n"
				+ "----------\n"
				+ "2. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 8)\n"
				+ "	* @see test.deep.qualified.name.p.X#smr_foo(int, byte, short, char, long, float, double) Invalid ref: local method not applicable\n"
				+ "	                                    ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (int, byte, short, char, long, float, double)\n"
				+ "----------\n"
				+ "3. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 9)\n"
				+ "	* @see test.deep.qualified.name.p.X#smr_foo(String, int) Invalid ref: local method not applicable\n"
				+ "	                                    ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, int)\n"
				+ "----------\n"
				+ "4. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 10)\n"
				+ "	* @see test.deep.qualified.name.p.X#smr_foo(String) Invalid ref: local method not applicable\n"
				+ "	                                    ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String)\n"
				+ "----------\n"
				+ "5. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 11)\n"
				+ "	* @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable,Vector) Invalid ref: local method not applicable\n"
				+ "	                                    ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable, Vector)\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test138() {
		this.runNegativeTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"package test.deep.qualified.name.p;\n"
					+ "import java.util.Vector;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(boolean,int i,byte,short s,char,long l,float,double d) Invalid reference: mixed argument declaration\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(String,String y,int) Invalid reference: mixed argument declaration\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n" +
			"1. WARNING in test\\deep\\qualified\\name\\p\\X.java (at line 2)\n"+
			"	import java.util.Vector;\n"+
			"	       ^^^^^^^^^^^^^^^^\n"+
			"The import java.util.Vector is never used\n"+
			"----------\n"+
			"2. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 7)\n" +
			"	* @see test.deep.qualified.name.p.X#smr_foo(boolean,int i,byte,short s,char,long l,float,double d) Invalid reference: mixed argument declaration\n" +
			"	                                           ^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"3. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 8)\n" +
			"	* @see test.deep.qualified.name.p.X#smr_foo(String,String y,int) Invalid reference: mixed argument declaration\n" +
			"	                                           ^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"4. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 9)\n" +
			"	* @see test.deep.qualified.name.p.X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration\n" +
			"	                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"5. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 10)\n" +
			"	* @see test.deep.qualified.name.p.X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration\n" +
			"	                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test140() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Valid package class methods references\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_public() Valid ref: visible method\n"
					+ "	 * @see Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class\n"
					+ "	 * @see test.Visibility#vm_public() Valid ref: visible method\n"
					+ "	 * @see test.Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test141() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-existence)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#unknown() Invalid ref: non-existent method\n"
					+ "	 * @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class\n"
					+ "	 * @see Unknown#vm_public() Invalid ref: non-existent class\n"
					+ "	 * @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#unknown() Invalid ref: non-existent method\n"
				+ "	                  ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type Visibility\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class\n"
				+ "	                           ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type Visibility.VcPublic\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Unknown#vm_public() Invalid ref: non-existent class\n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class\n"
				+ "	       ^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Visibility.Unknown cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test142() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-visible)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_private() Invalid ref: non-visible method\n"
					+ "	 * @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
					+ "	 * @see Visibility.VcPrivate#vm_private() Invalid ref: non visible inner class (non visible method)\n"
					+ "	 * @see Visibility.VcPrivate#vm_private(boolean, String) Invalid ref: non visible inner class (non applicable method)\n"
					+ "	 * @see Visibility.VcPrivate#vm_public() Invalid ref: non visible inner class (visible method)\n"
					+ "	 * @see Visibility.VcPrivate#vm_public(Object, float) Invalid ref: non visible inner class (non applicable visible method)\n"
					+ "	 * @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#vm_private() Invalid ref: non-visible method\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type Visibility is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.VcPrivate#vm_private() Invalid ref: non visible inner class (non visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.VcPrivate#vm_private(boolean, String) Invalid ref: non visible inner class (non applicable method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see Visibility.VcPrivate#vm_public() Invalid ref: non visible inner class (visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see Visibility.VcPrivate#vm_public(Object, float) Invalid ref: non visible inner class (non applicable visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "7. ERROR in test\\X.java (at line 12)\n"
				+ "	* @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type Visibility.VcPublic is not visible\n"
				+ "----------\n");
	}

	public void test143() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-applicable)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_private(int) Invalid ref: non-applicable method\n"
					+ "	 * @see Visibility#vm_public(String) Invalid ref: non-applicable method\n"
					+ "	 * @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class\n"
					+ "	 * @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#vm_private(int) Invalid ref: non-applicable method\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type Visibility is not applicable for the arguments (int)\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility#vm_public(String) Invalid ref: non-applicable method\n"
				+ "	                  ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type Visibility is not applicable for the arguments (String)\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type Visibility.VcPublic is not applicable for the arguments (Integer, byte)\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class\n"
				+ "	                           ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type Visibility.VcPublic is not applicable for the arguments (Double, Boolean)\n"
				+ "----------\n");
	}

	public void test144() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (invalid arguments)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_private(,) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility#vm_public(,String) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility.VcPrivate#vm_private(char, double d) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility.VcPrivate#vm_public(#) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility.VcPublic#vm_private(a a a) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility.VcPublic#vm_public(####) Invalid ref: Invalid ref: invalid argument declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#vm_private(,) Invalid ref: invalid argument declaration\n"
				+ "	                            ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility#vm_public(,String) Invalid ref: invalid argument declaration\n"
				+ "	                           ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.VcPrivate#vm_private(char, double d) Invalid ref: invalid argument declaration\n"
				+ "	                                      ^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.VcPrivate#vm_public(#) Invalid ref: invalid argument declaration\n"
				+ "	                                     ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see Visibility.VcPublic#vm_private(a a a) Invalid ref: invalid argument declaration\n"
				+ "	                                     ^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see Visibility.VcPublic#vm_public(####) Invalid ref: Invalid ref: invalid argument declaration\n"
				+ "	                                    ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n");
	}

	public void test145() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Valid package super class methods references\n"
					+ "	 * \n"
					+ "	 * @see Visibility#avm_public() Valid ref: visible inherited method\n"
					+ "	 * @see AbstractVisibility.AvcPublic#avm_public() Valid ref: visible method in visible inner class\n"
					+ "	 * @see test.Visibility#avm_public() Valid ref: visible inherited method\n"
					+ "	 * @see test.AbstractVisibility.AvcPublic#avm_public() Valid ref: visible method in visible inner class\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test146() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid package super class methods references (non-visible)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#avm_private() Invalid ref: non-visible inherited method\n"
					+ "	 * @see Visibility.AvcPrivate#unknown() Invalid ref: non visible inherited inner class (non existent method)\n"
					+ "	 * @see Visibility.AvcPrivate#avm_private() Invalid ref: non visible inherited inner class (non visible method)\n"
					+ "	 * @see Visibility.AvcPrivate#avm_private(boolean, String) Invalid ref: non visible inherited inner class (non applicable method)\n"
					+ "	 * @see Visibility.AvcPrivate#avm_public() Invalid ref: non visible inherited inner class (visible method)\n"
					+ "	 * @see Visibility.AvcPrivate#avm_public(Object, float) Invalid ref: non visible inherited inner class (non applicable visible method)\n"
					+ "	 * @see Visibility.AvcPublic#avm_private() Invalid ref: non visible inherited method in visible inherited inner class\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#avm_private() Invalid ref: non-visible inherited method\n"
				+ "	                  ^^^^^^^^^^^\n"
				+ "Javadoc: The method avm_private() from the type AbstractVisibility is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility.AvcPrivate#unknown() Invalid ref: non visible inherited inner class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.AvcPrivate#avm_private() Invalid ref: non visible inherited inner class (non visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.AvcPrivate#avm_private(boolean, String) Invalid ref: non visible inherited inner class (non applicable method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see Visibility.AvcPrivate#avm_public() Invalid ref: non visible inherited inner class (visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see Visibility.AvcPrivate#avm_public(Object, float) Invalid ref: non visible inherited inner class (non applicable visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "7. ERROR in test\\X.java (at line 12)\n"
				+ "	* @see Visibility.AvcPublic#avm_private() Invalid ref: non visible inherited method in visible inherited inner class\n"
				+ "	                            ^^^^^^^^^^^\n"
				+ "Javadoc: The method avm_private() from the type AbstractVisibility.AvcPublic is not visible\n"
				+ "----------\n");
	}

	public void test147() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid package super class methods references (non-applicable)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#avm_private(int) Invalid ref: non-applicable inherited method\n"
					+ "	 * @see Visibility#avm_public(String) Invalid ref: non-applicable inherited method\n"
					+ "	 * @see Visibility.AvcPublic#avm_private(Integer, byte) Invalid ref: non applicable inherited method in visible inner class\n"
					+ "	 * @see Visibility.AvcPublic#avm_public(Double z, Boolean x) Invalid ref: non applicable inherited method in visible inner class\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#avm_private(int) Invalid ref: non-applicable inherited method\n"
				+ "	                  ^^^^^^^^^^^\n"
				+ "Javadoc: The method avm_private() in the type AbstractVisibility is not applicable for the arguments (int)\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility#avm_public(String) Invalid ref: non-applicable inherited method\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The method avm_public() in the type AbstractVisibility is not applicable for the arguments (String)\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.AvcPublic#avm_private(Integer, byte) Invalid ref: non applicable inherited method in visible inner class\n"
				+ "	                            ^^^^^^^^^^^\n"
				+ "Javadoc: The method avm_private() in the type AbstractVisibility.AvcPublic is not applicable for the arguments (Integer, byte)\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.AvcPublic#avm_public(Double z, Boolean x) Invalid ref: non applicable inherited method in visible inner class\n"
				+ "	                            ^^^^^^^^^^\n"
				+ "Javadoc: The method avm_public() in the type AbstractVisibility.AvcPublic is not applicable for the arguments (Double, Boolean)\n"
				+ "----------\n");
	}

	public void test148() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid package super class methods references (invalid arguments)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#avm_private(,,,,) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility#avm_public(String,,,) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility.AvcPrivate#avm_private(char c, double) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility.AvcPrivate#avm_public(*) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility.AvcPublic#avm_private(a a a) Invalid ref: invalid argument declaration\n"
					+ "	 * @see Visibility.AvcPublic#avm_public(*****) Invalid ref: invalid argument declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#avm_private(,,,,) Invalid ref: invalid argument declaration\n"
				+ "	                             ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility#avm_public(String,,,) Invalid ref: invalid argument declaration\n"
				+ "	                            ^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.AvcPrivate#avm_private(char c, double) Invalid ref: invalid argument declaration\n"
				+ "	                                        ^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.AvcPrivate#avm_public(*) Invalid ref: invalid argument declaration\n"
				+ "	                                       ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see Visibility.AvcPublic#avm_private(a a a) Invalid ref: invalid argument declaration\n"
				+ "	                                       ^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see Visibility.AvcPublic#avm_public(*****) Invalid ref: invalid argument declaration\n"
				+ "	                                      ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n");
	}

	public void test150() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package non visible class methods references (non existent/visible arguments)\n"
					+ "	 * \n"
					+ "	 * @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n"
					+ "	 * @see VisibilityPackage#vm_private() Invalid ref: non visible class (non visible method)\n"
					+ "	 * @see VisibilityPackage#vm_private(boolean) Invalid ref: non visible class (non existent method)\n"
					+ "	 * @see VisibilityPackage#vm_public() Invalid ref: non visible class (visible method)\n"
					+ "	 * @see VisibilityPackage#vm_public(long,long,long,int) Invalid ref: non visible class (visible method)\n"
					+ "	 * @see VisibilityPackage.VpPrivate#unknown() Invalid ref: non visible class and non visible inner class (non existent method)\n"
					+ "	 * @see VisibilityPackage.VpPrivate#vm_private() Invalid ref: non visible class and non visible inner class (non visible method)\n"
					+ "	 * @see VisibilityPackage.VpPrivate#vm_private(boolean, String) Invalid ref: non visible class and non visible inner class (non applicable method)\n"
					+ "	 * @see VisibilityPackage.VpPrivate#vm_public() Invalid ref: non visible class and non visible inner class (visible method)\n"
					+ "	 * @see VisibilityPackage.VpPrivate#vm_public(Object, float) Invalid ref: non visible class and non visible inner class (non applicable visible method)\n"
					+ "	 * @see VisibilityPackage.VpPublic#unknown() Invalid ref: non visible class and visible inner class (non existent method)\n"
					+ "	 * @see VisibilityPackage.VpPublic#vm_private() Invalid ref: non visible class and visible inner class (non visible method)\n"
					+ "	 * @see VisibilityPackage.VpPublic#vm_private(boolean, String) Invalid ref: non visible class and visible inner class (non applicable method)\n"
					+ "	 * @see VisibilityPackage.VpPublic#vm_public() Invalid ref: non visible class and visible inner class (visible method)\n"
					+ "	 * @see VisibilityPackage.VpPublic#vm_public(Object, float) Invalid ref: non visible class and visible inner class (non applicable visible method)\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n" +
			"1. WARNING in test\\X.java (at line 2)\n"+
			"	import test.copy.*;\n"+
			"	       ^^^^^^^^^\n"+
			"The import test.copy is never used\n"+
			"----------\n"+
			"2. ERROR in test\\X.java (at line 7)\n" +
			"	* @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"3. ERROR in test\\X.java (at line 8)\n" +
			"	* @see VisibilityPackage#vm_private() Invalid ref: non visible class (non visible method)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"4. ERROR in test\\X.java (at line 9)\n" +
			"	* @see VisibilityPackage#vm_private(boolean) Invalid ref: non visible class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"5. ERROR in test\\X.java (at line 10)\n" +
			"	* @see VisibilityPackage#vm_public() Invalid ref: non visible class (visible method)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"6. ERROR in test\\X.java (at line 11)\n" +
			"	* @see VisibilityPackage#vm_public(long,long,long,int) Invalid ref: non visible class (visible method)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"7. ERROR in test\\X.java (at line 12)\n" +
			"	* @see VisibilityPackage.VpPrivate#unknown() Invalid ref: non visible class and non visible inner class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"8. ERROR in test\\X.java (at line 13)\n" +
			"	* @see VisibilityPackage.VpPrivate#vm_private() Invalid ref: non visible class and non visible inner class (non visible method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"9. ERROR in test\\X.java (at line 14)\n" +
			"	* @see VisibilityPackage.VpPrivate#vm_private(boolean, String) Invalid ref: non visible class and non visible inner class (non applicable method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"10. ERROR in test\\X.java (at line 15)\n" +
			"	* @see VisibilityPackage.VpPrivate#vm_public() Invalid ref: non visible class and non visible inner class (visible method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"11. ERROR in test\\X.java (at line 16)\n" +
			"	* @see VisibilityPackage.VpPrivate#vm_public(Object, float) Invalid ref: non visible class and non visible inner class (non applicable visible method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"12. ERROR in test\\X.java (at line 17)\n" +
			"	* @see VisibilityPackage.VpPublic#unknown() Invalid ref: non visible class and visible inner class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"13. ERROR in test\\X.java (at line 18)\n" +
			"	* @see VisibilityPackage.VpPublic#vm_private() Invalid ref: non visible class and visible inner class (non visible method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"14. ERROR in test\\X.java (at line 19)\n" +
			"	* @see VisibilityPackage.VpPublic#vm_private(boolean, String) Invalid ref: non visible class and visible inner class (non applicable method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"15. ERROR in test\\X.java (at line 20)\n" +
			"	* @see VisibilityPackage.VpPublic#vm_public() Invalid ref: non visible class and visible inner class (visible method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"16. ERROR in test\\X.java (at line 21)\n" +
			"	* @see VisibilityPackage.VpPublic#vm_public(Object, float) Invalid ref: non visible class and visible inner class (non applicable visible method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n");
	}

	public void test151() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.VisibilityPackage;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package non visible class methods references (invalid arguments)\n"
					+ "	 * \n"
					+ "	 * @see VisibilityPackage#vm_private(boolean-) Invalid ref: invalid argument declaration\n"
					+ "	 * @see VisibilityPackage#vm_public(long, int() Invalid ref: invalid argument declaration\n"
					+ "	 * @see VisibilityPackage.VpPrivate#vm_private(char, a double d()) Invalid ref: invalid argument declaration\n"
					+ "	 * @see VisibilityPackage.VpPrivate#vm_public(()) Invalid ref: invalid argument declaration\n"
					+ "	 * @see VisibilityPackage.VpPublic#vm_private(char double) Invalid ref: invalid argument declaration\n"
					+ "	 * @see VisibilityPackage.VpPublic#vm_public((((() Invalid ref: invalid argument declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 2)\n"
				+ "	import test.copy.VisibilityPackage;\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see VisibilityPackage#vm_private(boolean-) Invalid ref: invalid argument declaration\n"
				+ "	                                   ^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see VisibilityPackage#vm_public(long, int() Invalid ref: invalid argument declaration\n"
				+ "	                                  ^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see VisibilityPackage.VpPrivate#vm_private(char, a double d()) Invalid ref: invalid argument declaration\n"
				+ "	                                             ^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see VisibilityPackage.VpPrivate#vm_public(()) Invalid ref: invalid argument declaration\n"
				+ "	                                            ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see VisibilityPackage.VpPublic#vm_private(char double) Invalid ref: invalid argument declaration\n"
				+ "	                                            ^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "7. ERROR in test\\X.java (at line 12)\n"
				+ "	* @see VisibilityPackage.VpPublic#vm_public((((() Invalid ref: invalid argument declaration\n"
				+ "	                                           ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n",
				JavacTestOptions.DEFAULT);
	}

	public void test152() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other fully qualified name package non visible class methods references (non existent/visible arguments)\n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n"
					+ "	 * @see test.copy.VisibilityPackage#vm_private() Invalid ref: non visible class (non visible method)\n"
					+ "	 * @see test.copy.VisibilityPackage#vm_private(boolean) Invalid ref: non visible class (non existent method)\n"
					+ "	 * @see test.copy.VisibilityPackage#vm_public() Invalid ref: non visible class (visible method)\n"
					+ "	 * @see test.copy.VisibilityPackage#vm_public(long,long,long,int) Invalid ref: non visible class (visible method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#unknown() Invalid ref: non visible class and non visible inner class (non existent method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#vm_private() Invalid ref: non visible class and non visible inner class (non visible method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#vm_private(boolean, String) Invalid ref: non visible class and non visible inner class (non applicable method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#vm_public() Invalid ref: non visible class and non visible inner class (visible method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#vm_public(Object, float) Invalid ref: non visible class and non visible inner class (non applicable visible method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#unknown() Invalid ref: non visible class and visible inner class (non existent method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#vm_private() Invalid ref: non visible class and visible inner class (non visible method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#vm_private(boolean, String) Invalid ref: non visible class and visible inner class (non applicable method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#vm_public() Invalid ref: non visible class and visible inner class (visible method)\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#vm_public(Object, float) Invalid ref: non visible class and visible inner class (non applicable visible method)\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see test.copy.VisibilityPackage#vm_private() Invalid ref: non visible class (non visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see test.copy.VisibilityPackage#vm_private(boolean) Invalid ref: non visible class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see test.copy.VisibilityPackage#vm_public() Invalid ref: non visible class (visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see test.copy.VisibilityPackage#vm_public(long,long,long,int) Invalid ref: non visible class (visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#unknown() Invalid ref: non visible class and non visible inner class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "7. ERROR in test\\X.java (at line 12)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#vm_private() Invalid ref: non visible class and non visible inner class (non visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "8. ERROR in test\\X.java (at line 13)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#vm_private(boolean, String) Invalid ref: non visible class and non visible inner class (non applicable method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "9. ERROR in test\\X.java (at line 14)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#vm_public() Invalid ref: non visible class and non visible inner class (visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "10. ERROR in test\\X.java (at line 15)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#vm_public(Object, float) Invalid ref: non visible class and non visible inner class (non applicable visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "11. ERROR in test\\X.java (at line 16)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#unknown() Invalid ref: non visible class and visible inner class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "12. ERROR in test\\X.java (at line 17)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#vm_private() Invalid ref: non visible class and visible inner class (non visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "13. ERROR in test\\X.java (at line 18)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#vm_private(boolean, String) Invalid ref: non visible class and visible inner class (non applicable method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "14. ERROR in test\\X.java (at line 19)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#vm_public() Invalid ref: non visible class and visible inner class (visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "15. ERROR in test\\X.java (at line 20)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#vm_public(Object, float) Invalid ref: non visible class and visible inner class (non applicable visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPackage is not visible\n"
				+ "----------\n");
	}

	public void test153() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other fully qualified name package non visible class methods references (invalid arguments)\n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPackage#vm_private(boolean-) Invalid ref: invalid argument declaration\n"
					+ "	 * @see test.copy.VisibilityPackage#vm_public(long, int() Invalid ref: invalid argument declaration\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#vm_private(char, a double d()) Invalid ref: invalid argument declaration\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPrivate#vm_public(()) Invalid ref: invalid argument declaration\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#vm_private(char double) Invalid ref: invalid argument declaration\n"
					+ "	 * @see test.copy.VisibilityPackage.VpPublic#vm_public((((() Invalid ref: invalid argument declaration\n"
					+ "	 */  \n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see test.copy.VisibilityPackage#vm_private(boolean-) Invalid ref: invalid argument declaration\n"
				+ "	                                             ^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see test.copy.VisibilityPackage#vm_public(long, int() Invalid ref: invalid argument declaration\n"
				+ "	                                            ^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#vm_private(char, a double d()) Invalid ref: invalid argument declaration\n"
				+ "	                                                       ^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPrivate#vm_public(()) Invalid ref: invalid argument declaration\n"
				+ "	                                                      ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#vm_private(char double) Invalid ref: invalid argument declaration\n"
				+ "	                                                      ^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see test.copy.VisibilityPackage.VpPublic#vm_public((((() Invalid ref: invalid argument declaration\n"
				+ "	                                                     ^^\n"
				+ "Javadoc: Invalid parameters declaration\n"
				+ "----------\n");
	}

	public void test154() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import test.copy.VisibilityPublic;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Valid other package visible class methods references \n" +
				"	 * \n" +
				"	 * @see VisibilityPublic#vm_public() Valid ref to not visible method of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class\n" +
				"	 */\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	public void test155() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.VisibilityPublic;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package visible class methods references (non-existent)\n"
					+ "	 * \n"
					+ "	 * @see VisibilityPublic#unknown() Invalid ref: non existent method\n"
					+ "	 * @see VisibilityPublic.VpPublic#unknown() Invalid ref: non existent method of visible inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see VisibilityPublic#unknown() Invalid ref: non existent method\n"
				+ "	                        ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type VisibilityPublic\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see VisibilityPublic.VpPublic#unknown() Invalid ref: non existent method of visible inner class\n"
				+ "	                                 ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type VisibilityPublic.VpPublic\n"
				+ "----------\n");
	}

	public void test156() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.VisibilityPublic;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package visible class methods references (non-visible)\n"
					+ "	 * \n"
					+ "	 * @see VisibilityPublic#vm_private() Invalid ref: non visible method in visible class\n"
					+ "	 * @see VisibilityPublic#vm_public() Valid ref: visible method in visible class\n"
					+ "	 * @see VisibilityPublic.VpPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
					+ "	 * @see VisibilityPublic.VpPrivate#vm_private() Invalid ref: non visible inner class in visible class and (non visible method)\n"
					+ "	 * @see VisibilityPublic.VpPrivate#vm_private(boolean, String) Invalid ref: non visible inner class in visible class (non applicable method)\n"
					+ "	 * @see VisibilityPublic.VpPrivate#vm_public() Invalid ref: non visible inner class in visible class (visible method)\n"
					+ "	 * @see VisibilityPublic.VpPrivate#vm_public(Object, float) Invalid ref: non visible inner class in visible class (non applicable visible method)\n"
					+ "	 * @see VisibilityPublic.VpPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see VisibilityPublic#vm_private() Invalid ref: non visible method in visible class\n"
				+ "	                        ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type VisibilityPublic is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see VisibilityPublic.VpPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see VisibilityPublic.VpPrivate#vm_private() Invalid ref: non visible inner class in visible class and (non visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see VisibilityPublic.VpPrivate#vm_private(boolean, String) Invalid ref: non visible inner class in visible class (non applicable method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 12)\n"
				+ "	* @see VisibilityPublic.VpPrivate#vm_public() Invalid ref: non visible inner class in visible class (visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 13)\n"
				+ "	* @see VisibilityPublic.VpPrivate#vm_public(Object, float) Invalid ref: non visible inner class in visible class (non applicable visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "7. ERROR in test\\X.java (at line 14)\n"
				+ "	* @see VisibilityPublic.VpPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
				+ "	                                 ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type VisibilityPublic.VpPublic is not visible\n"
				+ "----------\n");
	}

	public void test157() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.VisibilityPublic;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package visible class methods references (non-applicable)\n"
					+ "	 * \n"
					+ "	 * @see VisibilityPublic#vm_private(boolean) Invalid ref: non applicable method in visible class\n"
					+ "	 * @see VisibilityPublic#vm_public(long,long,long,int) Invalid ref: non applicable method in visible class\n"
					+ "	 * @see VisibilityPublic.VpPublic#vm_private(boolean, String) Invalid ref: non applicable method in visible inner class\n"
					+ "	 * @see VisibilityPublic.VpPublic#vm_public(Object, float) Invalid ref: visible inner class (non applicable visible method)\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see VisibilityPublic#vm_private(boolean) Invalid ref: non applicable method in visible class\n"
				+ "	                        ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type VisibilityPublic is not applicable for the arguments (boolean)\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see VisibilityPublic#vm_public(long,long,long,int) Invalid ref: non applicable method in visible class\n"
				+ "	                        ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type VisibilityPublic is not applicable for the arguments (long, long, long, int)\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see VisibilityPublic.VpPublic#vm_private(boolean, String) Invalid ref: non applicable method in visible inner class\n"
				+ "	                                 ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type VisibilityPublic.VpPublic is not applicable for the arguments (boolean, String)\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see VisibilityPublic.VpPublic#vm_public(Object, float) Invalid ref: visible inner class (non applicable visible method)\n"
				+ "	                                 ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type VisibilityPublic.VpPublic is not applicable for the arguments (Object, float)\n"
				+ "----------\n");
	}

	public void test158() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.VisibilityPublic;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package visible class methods references (non-existent)\n"
					+ "	 * \n"
					+ "	 * @see VisibilityPublic#vm_private(\"boolean\") Invalid ref: invalid argument declaration\n"
					+ "	 * @see VisibilityPublic#vm_public(long, \"int) Invalid ref: invalid argument definition\n"
					+ "	 * @see VisibilityPublic.VpPrivate#vm_private(double d()) Invalid ref: invalid argument declaration\n"
					+ "	 * @see VisibilityPublic.VpPrivate#vm_public(\") Invalid ref: invalid argument declaration\n"
					+ "	 * @see VisibilityPublic.VpPublic#vm_private(d()) Invalid ref: invalid argument declaration\n"
					+ "	 * @see VisibilityPublic.VpPublic#vm_public(205) Invalid ref: invalid argument declaration\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n" +
			"1. WARNING in test\\X.java (at line 2)\n"+
			"	import test.copy.VisibilityPublic;\n"+
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"+
			"The import test.copy.VisibilityPublic is never used\n"+
			"----------\n"+
			"2. ERROR in test\\X.java (at line 7)\n" +
			"	* @see VisibilityPublic#vm_private(\"boolean\") Invalid ref: invalid argument declaration\n" +
			"	                                  ^^^^^^^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"3. ERROR in test\\X.java (at line 8)\n" +
			"	* @see VisibilityPublic#vm_public(long, \"int) Invalid ref: invalid argument definition\n" +
			"	                                 ^^^^^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"4. ERROR in test\\X.java (at line 9)\n" +
			"	* @see VisibilityPublic.VpPrivate#vm_private(double d()) Invalid ref: invalid argument declaration\n" +
			"	                                            ^^^^^^^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"5. ERROR in test\\X.java (at line 10)\n" +
			"	* @see VisibilityPublic.VpPrivate#vm_public(\") Invalid ref: invalid argument declaration\n" +
			"	                                           ^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"6. ERROR in test\\X.java (at line 11)\n" +
			"	* @see VisibilityPublic.VpPublic#vm_private(d()) Invalid ref: invalid argument declaration\n" +
			"	                                           ^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"7. ERROR in test\\X.java (at line 12)\n" +
			"	* @see VisibilityPublic.VpPublic#vm_public(205) Invalid ref: invalid argument declaration\n" +
			"	                                          ^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n");
	}

	public void test159() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Valid other package visible class methods references \n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPublic#vm_public() Valid ref to not visible method of other package class\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test160() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package visible class methods references (non-existent)\n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPublic#unknown() Invalid ref: non existent method\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#unknown() Invalid ref: non existent method of visible inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see test.copy.VisibilityPublic#unknown() Invalid ref: non existent method\n"
				+ "	                                  ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type VisibilityPublic\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPublic#unknown() Invalid ref: non existent method of visible inner class\n"
				+ "	                                           ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type VisibilityPublic.VpPublic\n"
				+ "----------\n");
	}

	public void test161() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package visible class methods references (non-visible)\n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPublic#vm_private() Invalid ref: non visible method in visible class\n"
					+ "	 * @see test.copy.VisibilityPublic#vm_public() Valid ref: visible method in visible class\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPrivate#vm_private() Invalid ref: non visible inner class in visible class and (non visible method)\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPrivate#vm_private(boolean, String) Invalid ref: non visible inner class in visible class (non applicable method)\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPrivate#vm_public() Invalid ref: non visible inner class in visible class (visible method)\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPrivate#vm_public(Object, float) Invalid ref: non visible inner class in visible class (non applicable visible method)\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see test.copy.VisibilityPublic#vm_private() Invalid ref: non visible method in visible class\n"
				+ "	                                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type VisibilityPublic is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPrivate#vm_private() Invalid ref: non visible inner class in visible class and (non visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPrivate#vm_private(boolean, String) Invalid ref: non visible inner class in visible class (non applicable method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPrivate#vm_public() Invalid ref: non visible inner class in visible class (visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 12)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPrivate#vm_public(Object, float) Invalid ref: non visible inner class in visible class (non applicable visible method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "7. ERROR in test\\X.java (at line 13)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
				+ "	                                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type VisibilityPublic.VpPublic is not visible\n"
				+ "----------\n");
	}

	public void test162() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package visible class methods references (non-applicable)\n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPublic#vm_private(boolean) Invalid ref: non applicable method in visible class\n"
					+ "	 * @see test.copy.VisibilityPublic#vm_public(long,long,long,int) Invalid ref: non applicable method in visible class\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#vm_private(boolean, String) Invalid ref: non applicable method in visible inner class\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#vm_public(Object, float) Invalid ref: visible inner class (non applicable visible method)\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see test.copy.VisibilityPublic#vm_private(boolean) Invalid ref: non applicable method in visible class\n"
				+ "	                                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type VisibilityPublic is not applicable for the arguments (boolean)\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see test.copy.VisibilityPublic#vm_public(long,long,long,int) Invalid ref: non applicable method in visible class\n"
				+ "	                                  ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type VisibilityPublic is not applicable for the arguments (long, long, long, int)\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPublic#vm_private(boolean, String) Invalid ref: non applicable method in visible inner class\n"
				+ "	                                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type VisibilityPublic.VpPublic is not applicable for the arguments (boolean, String)\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see test.copy.VisibilityPublic.VpPublic#vm_public(Object, float) Invalid ref: visible inner class (non applicable visible method)\n"
				+ "	                                           ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type VisibilityPublic.VpPublic is not applicable for the arguments (Object, float)\n"
				+ "----------\n");
	}

	public void test163() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.VisibilityPublic;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid other package visible class methods references (non-existent)\n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPublic#vm_private(\"\") Invalid ref: invalid argument declaration\n"
					+ "	 * @see test.copy.VisibilityPublic#vm_public(\"\"\") Invalid ref: invalid argument definition\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPrivate#vm_private(String d()) Invalid ref: invalid argument declaration\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPrivate#vm_public([) Invalid ref: invalid argument declaration\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#vm_private([]) Invalid ref: invalid argument declaration\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#vm_public(char[], int[],]) Invalid ref: invalid argument declaration\n"
					+ "	 */\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n" +
			"1. WARNING in test\\X.java (at line 2)\n"+
			"	import test.copy.VisibilityPublic;\n"+
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"+
			"The import test.copy.VisibilityPublic is never used\n"+
			"----------\n"+
			"2. ERROR in test\\X.java (at line 7)\n" +
			"	* @see test.copy.VisibilityPublic#vm_private(\"\") Invalid ref: invalid argument declaration\n" +
			"	                                            ^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"3. ERROR in test\\X.java (at line 8)\n" +
			"	* @see test.copy.VisibilityPublic#vm_public(\"\"\") Invalid ref: invalid argument definition\n" +
			"	                                           ^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"4. ERROR in test\\X.java (at line 9)\n" +
			"	* @see test.copy.VisibilityPublic.VpPrivate#vm_private(String d()) Invalid ref: invalid argument declaration\n" +
			"	                                                      ^^^^^^^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"5. ERROR in test\\X.java (at line 10)\n" +
			"	* @see test.copy.VisibilityPublic.VpPrivate#vm_public([) Invalid ref: invalid argument declaration\n" +
			"	                                                     ^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"6. ERROR in test\\X.java (at line 11)\n" +
			"	* @see test.copy.VisibilityPublic.VpPublic#vm_private([]) Invalid ref: invalid argument declaration\n" +
			"	                                                     ^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n" +
			"7. ERROR in test\\X.java (at line 12)\n" +
			"	* @see test.copy.VisibilityPublic.VpPublic#vm_public(char[], int[],]) Invalid ref: invalid argument declaration\n" +
			"	                                                    ^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid parameters declaration\n" +
			"----------\n");
	}

	public void test164() {
		this.runNegativeReferenceTest(
			new String[] {
				"X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid param and throws tags\n"
					+ "	 * \n"
					+ "	 * @param\n"
					+ "	 * @throws\n"
					+ "	 */\n"
					+ "	public void s_foo(int a) throws Exception {\n"
					+ "	}\n"
					+ "}\n" },
					"----------\n"
					+ "1. ERROR in X.java (at line 6)\n"
					+ "	* @param\n"
					+ "	   ^^^^^\n"
					+ "Javadoc: Missing parameter name\n"
					+ "----------\n"
					+ "2. ERROR in X.java (at line 7)\n"
					+ "	* @throws\n"
					+ "	   ^^^^^^\n"
					+ "Javadoc: Missing class name\n"
					+ "----------\n"
					+ "3. ERROR in X.java (at line 9)\n"
					+ "	public void s_foo(int a) throws Exception {\n"
					+ "	                      ^\n"
					+ "Javadoc: Missing tag for parameter a\n"
					+ "----------\n"
					+ "4. ERROR in X.java (at line 9)\n"
					+ "	public void s_foo(int a) throws Exception {\n"
					+ "	                                ^^^^^^^^^\n"
					+ "Javadoc: Missing tag for declared exception Exception\n"
					+ "----------\n");
	}
}
