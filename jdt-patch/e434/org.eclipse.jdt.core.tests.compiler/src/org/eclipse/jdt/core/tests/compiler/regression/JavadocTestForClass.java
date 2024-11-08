/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTestForClass extends JavadocTest {

	public static Test suite() {
		return buildAllCompliancesTestSuite(javadocTestClass());
	}
	public JavadocTestForClass(String name) {
		super(name);
	}
	public static Class javadocTestClass() {
		return JavadocTestForClass.class;
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
	 * Test unexpected tags
	 */
	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Valid class javadoc\n"
					+ "	 * @author ffr\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @param x Invalid tag\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @param x Invalid tag\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @throws NullPointerException Invalid tag\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @throws NullPointerException Invalid tag\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @exception NullPointerException Invalid tag\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @exception NullPointerException Invalid tag\n"
				+ "	   ^^^^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @return Invalid tag\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @return Invalid tag\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @exception NullPointerException Invalid tag\n"
					+ "	 * @throws NullPointerException Invalid tag\n"
					+ "	 * @return Invalid tag\n"
					+ "	 * @param x Invalid tag\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	* @exception NullPointerException Invalid tag\n"
				+ "	   ^^^^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 4)\n"
				+ "	* @throws NullPointerException Invalid tag\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 5)\n"
				+ "	* @return Invalid tag\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 6)\n"
				+ "	* @param x Invalid tag\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test007() {
		this.runConformTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Valid class javadoc\n"
					+ "	 * @author ffr\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void foo() {}\n"
					+ "	/**\n"
					+ "	 * Invalid javadoc comment\n"
					+ "	 */\n"
					+ "}\n" }
			);
	}

	public void test008() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	public void foo() {}\n"
					+ "	/**\n"
					+ "	 * Invalid javadoc comment\n"
					+ "	 */\n"
					+ "}\n" }
			);
	}

	public void test009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	/**\n"
					+ "	 * Invalid javadoc comment\n"
					+ "	 */\n"
					+ "}\n" }
			);
	}

	/*
	 * (non-Javadoc) Test @see tag
	 */
	// String references
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Invalid string references \n"
					+ "	 *\n"
					+ "	 * @see \"\n"
					+ "	 * @see \"unterminated string\n"
					+ "	 * @see \"invalid string\"\"\n"
					+ "	 * @see \"invalid\" no text allowed after the string\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @see \"\n"
				+ "	       ^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 5)\n"
				+ "	* @see \"unterminated string\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 6)\n"
				+ "	* @see \"invalid string\"\"\n"
				+"	                       ^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 7)\n"
				+ "	* @see \"invalid\" no text allowed after the string\n"
				+ "	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Valid string references \n"
					+ "	 *\n"
					+ "	 * @see \"Valid normal string\"\n"
					+ "	 * @see \"Valid \\\"string containing\\\" \\\"double-quote\\\"\"\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	// URL Link references
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Invalid URL link references \n"
					+ "	 *\n"
					+ "	 * @see <a href=\n"
					+ "	 * @see <a href=\"invalid\">invalid</a> no text allowed after the href\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @see <a href=\n"
				+ "	       ^^^^^^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 5)\n"
				+ "	* @see <a href=\"invalid\">invalid</a> no text allowed after the href\n"
				+ "	                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test013() {
		this.runConformTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Valid URL link references \n"
					+ "	 *\n"
					+ "	 * @see <a href=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Valid URL link reference</a>\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	// @see Classes references
	public void test020() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
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
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test021() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Invalid local classes references \n"
					+ "	 *\n"
					+ "	 * @see Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
					+ "	 * @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
					+ "	 * @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
					+ "	 * @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
					+ "	 * @see Unknown Invalid ref: unknown class \n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 5)\n"
				+ "	* @see Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Unknown Invalid ref: unknown class \n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test022() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				// Inner classes are not visible in generated documentation
				//"	 * @see VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"public class X {\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	public void test023() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "	/**\n"
					+ "	 * Invalid external classes references \n"
					+ "	 *\n"
					+ "	 * @see VisibilityPackage Invalid ref: non visible class \n"
					+ "	 * @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see VisibilityPackage Invalid ref: non visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n");
	}

	public void test024() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see test.copy.VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				// Inner classes are not visible in generated documentation
				//"	 * @see test.copy.VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"public class X {\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
				}
		);
	}

	// @see Field references
	public void test030() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Valid local class field references\n"
					+ "	 *\n"
					+ "	 * @see #x Valid ref: accessible field\n"
					+ "	 * @see Visibility#vf_public Valid ref: visible field\n"
					+ "	 * @see Visibility.VcPublic#vf_public Valid ref: visible field in visible inner class\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	int x;\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test031() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Invalid local class field references\n"
					+ "	 *\n"
					+ "	 * @see #x Invalid ref: non existent field\n"
					+ "	 * @see Visibility#unknown Invalid ref: non existent field\n"
					+ "	 * @see Visibility#vf_private Invalid ref: non visible field\n"
					+ "	 * @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)\n"
					+ "	 * @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class\n"
					+ "	 * @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 5)\n"
				+ "	* @see #x Invalid ref: non existent field\n"
				+ "	        ^\n"
				+ "Javadoc: x cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#unknown Invalid ref: non existent field\n"
				+ "	                  ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility#vf_private Invalid ref: non visible field\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class\n"
				+ "	                           ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n");
	}

	public void test032() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"	/**\n" +
				"	 * Valid other package visible class fields references\n" +
				"	 *\n" +
				"	 * @see VisibilityPublic#vf_public Valid ref to visible field of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vf_public Valid ref to visible field of other package public inner class\n" +
				"	 */\n" +
				"public class X {\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	public void test033() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "	/**\n"
					+ "	 * Invalid other package non visible class fields references\n"
					+ "	 *\n"
					+ "	 * @see VisibilityPackage#unknown Invalid ref to non existent field of other package non visible class\n"
					+ "	 * @see VisibilityPublic#unknown Invalid ref to non existent field of other package class\n"
					+ "	 * @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n"
					+ "	 * @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n"
					+ "	 * @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n"
					+ "	 * @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see VisibilityPackage#unknown Invalid ref to non existent field of other package non visible class\n"
				+ "	       ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class\n"
				+ "	                        ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n"
				+ "	                        ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 9)\n"
				+ "	* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n"
				+ "	                                 ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "6. ERROR in test\\X.java (at line 11)\n"
				+ "	* @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n"
				+ "	                                 ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n");
	}

	// @see method references
	public void test040() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Valid local methods references with array\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) Valid local method reference\n"
					+ "	 * @see #smr_foo(char[], int[][], String[][][], Vector[][][][]) Valid local method reference\n"
					+ "	 * @see #smr_foo(char[],int[][],java.lang.String[][][],java.util.Vector[][][][]) Valid local method reference\n"
					+ "	 */  \n"
					+ "public class X {\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test041() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references with array (non applicable arrays)\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 */  \n"
					+ "public class X {\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][][], Vector[][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector[][][])\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test042() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo() Valid local method reference\n"
					+ "	 * @see #smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference\n"
					+ "	 * @see #smr_foo(java.lang.String, java.lang.String, int) Valid local method reference   \n"
					+ "	 * @see #smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "public class X {\n"
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

	public void test043() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #unknown() Invalid ref: undefined local method reference\n"
					+ "	 */  \n"
					+ "public class X {\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @see #unknown() Invalid ref: undefined local method reference\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type X\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test044() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
					+ "	 */  \n"
					+ "public class X {\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {\n"
					+ "	}\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i) {\n"
					+ "	}\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (float, long, char, short, byte, int, boolean)\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 5)\n"
				+ "	* @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, String, int, String)\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 6)\n"
				+ "	* @see #smr_foo(boolean) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean)\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
				+ "	                ^^^^^^^^^\n"
				+ "Javadoc: Hashtable cannot be resolved to a type\n"
				+ "----------\n"
				+ "5. ERROR in X.java (at line 7)\n"
				+ "	* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
				+ "	                             ^^^^^^\n"
				+ "Javadoc: Vector cannot be resolved to a type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test045() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see X#smr_foo() Valid local method reference\n"
					+ "	 * @see X#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference\n"
					+ "	 * @see X#smr_foo(String x, java.lang.String y, int z) Valid local method reference   \n"
					+ "	 * @see X#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "public class X {\n"
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

	public void test046() {
		this.runConformTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"package test.deep.qualified.name.p;\n"
					+ "import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo() Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(String x, java.lang.String y, int z) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "public class X {\n"
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

	public void test047() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Valid package class methods references\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_public() Valid ref: visible method\n"
					+ "	 * @see Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class\n"
					+ "	 * @see test.Visibility#vm_public() Valid ref: visible method\n"
					+ "	 * @see test.Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class\n"
					+ "	 */  \n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test048() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-existence)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#unknown() Invalid ref: non-existent method\n"
					+ "	 * @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class\n"
					+ "	 * @see Unknown#vm_public() Invalid ref: non-existent class\n"
					+ "	 * @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class\n"
					+ "	 */  \n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 5)\n"
				+ "	* @see Visibility#unknown() Invalid ref: non-existent method\n"
				+ "	                  ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type Visibility\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class\n"
				+ "	                           ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type Visibility.VcPublic\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Unknown#vm_public() Invalid ref: non-existent class\n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class\n"
				+ "	       ^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Visibility.Unknown cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test049() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-visible)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_private() Invalid ref: non-visible method\n"
					+ "	 * @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
					+ "	 * @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
					+ "	 */  \n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 5)\n"
				+ "	* @see Visibility#vm_private() Invalid ref: non-visible method\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type Visibility is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type Visibility.VcPublic is not visible\n"
				+ "----------\n");
	}

	public void test050() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-applicable)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_private(int) Invalid ref: non-applicable method\n"
					+ "	 * @see Visibility#vm_public(String) Invalid ref: non-applicable method\n"
					+ "	 * @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class\n"
					+ "	 * @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class\n"
					+ "	 */  \n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 5)\n"
				+ "	* @see Visibility#vm_private(int) Invalid ref: non-applicable method\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type Visibility is not applicable for the arguments (int)\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 6)\n"
				+ "	* @see Visibility#vm_public(String) Invalid ref: non-applicable method\n"
				+ "	                  ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type Visibility is not applicable for the arguments (String)\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 7)\n"
				+ "	* @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type Visibility.VcPublic is not applicable for the arguments (Integer, byte)\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 8)\n"
				+ "	* @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class\n"
				+ "	                           ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type Visibility.VcPublic is not applicable for the arguments (Double, Boolean)\n"
				+ "----------\n");
	}

	public void test051() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "	/**\n"
					+ "	 * Invalid other package non visible class methods references (non existent/visible arguments)\n"
					+ "	 * \n"
					+ "	 * @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n"
					+ "	 * @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n"
					+ "	 */  \n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n" +
			"1. WARNING in test\\X.java (at line 2)\n"+
			"	import test.copy.*;\n"+
			"	       ^^^^^^^^^\n"+
			"The import test.copy is never used\n"+
			"----------\n"+
			"2. ERROR in test\\X.java (at line 6)\n" +
			"	* @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"3. ERROR in test\\X.java (at line 7)\n" +
			"	* @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type test.copy.VisibilityPackage is not visible\n" +
			"----------\n");
	}

	public void test052() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import test.copy.VisibilityPublic;\n" +
				"	/**\n" +
				"	 * Valid other package visible class methods references \n" +
				"	 * \n" +
				"	 * @see VisibilityPublic#vm_public() Valid ref to not visible method of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class\n" +
				"	 */\n" +
				"public class X {\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	public void test053() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Valid other package visible class methods references \n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPublic#vm_public() Valid ref to not visible method of other package class\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class\n"
					+ "	 */\n"
					+ "public class X {\n"
					+ "	public void s_foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253750
	public void test054() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) {
			runNegativeTest(
					new String[] {
						"X.java",
						"import java.util.Map;\n" +
						"import java.util.Map.Entry;\n" +
						"\n" +
						"/**\n" +
						" * <ul>\n" +
						" * <li> {@link Entry} </li>\n" +
						" * </ul>\n" +
						" */\n" +
						"public interface X extends Map {\n" +
						"	int i = 0;\n" +
						"}\n",
						},
						"----------\n" +
						"1. WARNING in X.java (at line 2)\n"+
						"	import java.util.Map.Entry;\n"+
						"	       ^^^^^^^^^^^^^^^^^^^\n"+
						"The import java.util.Map.Entry is never used\n"+
						"----------\n"+
						"2. ERROR in X.java (at line 6)\n" +
						"	* <li> {@link Entry} </li>\n" +
						"	              ^^^^^\n" +
						"Javadoc: Invalid member type qualification\n" +
						"----------\n");
			return;
		}
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.Map.Entry;\n" +
				"\n" +
				"/**\n" +
				" * <ul>\n" +
				" * <li> {@link Entry} </li>\n" +
				" * </ul>\n" +
				" */\n" +
				"public interface X extends Map {\n" +
				"	int i;\n" +
				"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	int i;\n" +
				"	    ^\n" +
				"The blank final field i may not have been initialized\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253750 - variation
	public void test055() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"\n" +
				"/**\n" +
				" * <ul>\n" +
				" * <li> {@link Entry} </li>\n" +
				" * </ul>\n" +
				" */\n" +
				"public interface X extends Map {\n" +
				"	Entry e = null;\n" +
				"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	* <li> {@link Entry} </li>\n" +
				"	              ^^^^^\n" +
				"Javadoc: Invalid member type qualification\n" +
				"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253750
	public void test056() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) {
			runNegativeTest(
					new String[] {
						"X.java",
						"import java.util.Map;\n" +
						"import java.util.Map.Entry;\n" +
						"\n" +
						"/**\n" +
						" * <ul>\n" +
						" * <li> {@link Entry} </li>\n" +
						" * </ul>\n" +
						" */\n" +
						"public interface X extends Map {\n" +
						"	Entry e = null;\n" +
						"}\n",
						},
						"----------\n" +
						"1. WARNING in X.java (at line 2)\n"+
						"	import java.util.Map.Entry;\n"+
						"	       ^^^^^^^^^^^^^^^^^^^\n"+
						"The import java.util.Map.Entry is never used\n"+
						"----------\n"+
						"2. ERROR in X.java (at line 6)\n" +
						"	* <li> {@link Entry} </li>\n" +
						"	              ^^^^^\n" +
						"Javadoc: Invalid member type qualification\n" +
						"----------\n");
			return;
		}
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.Map.Entry;\n" +
				"\n" +
				"/**\n" +
				" * <ul>\n" +
				" * <li> {@link Entry} </li>\n" +
				" * </ul>\n" +
				" */\n" +
				"public interface X extends Map {\n" +
				"	Entry e;\n" +
				"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	Entry e;\n" +
				"	      ^\n" +
				"The blank final field e may not have been initialized\n" +
				"----------\n");
	}
}
