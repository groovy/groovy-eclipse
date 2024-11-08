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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTestForInterface extends JavadocTest {
	public JavadocTestForInterface(String name) {
		super(name);
	}
	public static Class javadocTestClass() {
		return JavadocTestForInterface.class;
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(javadocTestClass());
	}
	static { // Use this static to initialize testNames (String[]) , testRange (int[2]), testNumbers (int[])
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

	/*
	 * (non-Javadoc)
	 * Javadoc comment of Interface
	 */
	// Unexpected tag
	public void test001() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Valid class javadoc\n"
					+ "	 * @author ffr\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test002() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @param x Invalid tag\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 3)\n"
				+ "	* @param x Invalid tag\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test003() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @throws NullPointerException Invalid tag\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 3)\n"
				+ "	* @throws NullPointerException Invalid tag\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test004() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @exception NullPointerException Invalid tag\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 3)\n"
				+ "	* @exception NullPointerException Invalid tag\n"
				+ "	   ^^^^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test005() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @return Invalid tag\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 3)\n"
				+ "	* @return Invalid tag\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test006() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Invalid class javadoc\n"
					+ "	 * @exception NullPointerException Invalid tag\n"
					+ "	 * @throws NullPointerException Invalid tag\n"
					+ "	 * @return Invalid tag\n"
					+ "	 * @param x Invalid tag\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 3)\n"
				+ "	* @exception NullPointerException Invalid tag\n"
				+ "	   ^^^^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 4)\n"
				+ "	* @throws NullPointerException Invalid tag\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "3. ERROR in IX.java (at line 5)\n"
				+ "	* @return Invalid tag\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "4. ERROR in IX.java (at line 6)\n"
				+ "	* @param x Invalid tag\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test007() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Valid class javadoc\n"
					+ "	 * @author ffr\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "	/**\n"
					+ "	 * Invalid javadoc comment\n"
					+ "	 */\n"
					+ "}\n" }
			);
	}

	public void test008() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	public void foo();\n"
					+ "	/**\n"
					+ "	 * Invalid javadoc comment\n"
					+ "	 */\n"
					+ "}\n" }
			);
	}

	public void test009() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid javadoc comment\n"
					+ "	 */\n"
					+ "}\n" }
			);
	}


	// @see tag
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Invalid string references \n"
					+ "	 *\n"
					+ "	 * @see \"unterminated string\n"
					+ "	 * @see \"invalid\" no text allowed after the string\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 4)\n"
				+ "	* @see \"unterminated string\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 5)\n"
				+ "	* @see \"invalid\" no text allowed after the string\n"
				+ "	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test011() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Valid string references \n"
					+ "	 *\n"
					+ "	 * @see \"Valid normal string\"\n"
					+ "	 * @see \"Valid \\\"string containing\\\" \\\"double-quote\\\"\"\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test012() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Invalid URL link references \n"
					+ "	 *\n"
					+ "	 * @see <a href=\"invalid\">invalid</a\n"
					+ "	 * @see <a href=\"invalid\">invalid</a> no text allowed after the href\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 4)\n"
				+ "	* @see <a href=\"invalid\">invalid</a\n"
				+ "	                                ^^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 5)\n"
				+ "	* @see <a href=\"invalid\">invalid</a> no text allowed after the href\n"
				+ "	                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test013() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Valid URL link references \n"
					+ "	 *\n"
					+ "	 * @see <a hReF = \"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Valid URL link reference</A>\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	// @see Classes references
	public void test020() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
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
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test021() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
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
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 5)\n"
				+ "	* @see Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\IX.java (at line 9)\n"
				+ "	* @see Unknown Invalid ref: unknown class \n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test022() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				// Inner classes are not visible in generated documentation
				//"	 * @see VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"public interface IX {\n" +
				"	public void foo();\n" +
				"}\n"
			}
		);
	}

	public void test023() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "	/**\n"
					+ "	 * Invalid external classes references \n"
					+ "	 *\n"
					+ "	 * @see VisibilityPackage Invalid ref: non visible class \n"
					+ "	 * @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see VisibilityPackage Invalid ref: non visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n");
	}

	public void test024() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see test.copy.VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				// Inner classes are not visible in generated documentation
				//"	 * @see test.copy.VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"public interface IX {\n" +
				"	public void foo();\n" +
				"}\n"
			}
		);
	}

	// @see Field references
	public void test030() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Valid local class field references\n"
					+ "	 *\n"
					+ "	 * @see Visibility#vf_public Valid ref: visible field\n"
					+ "	 * @see Visibility.VcPublic#vf_public Valid ref: visible field in visible inner class\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test031() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
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
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 5)\n"
				+ "	* @see #x Invalid ref: non existent field\n"
				+ "	        ^\n"
				+ "Javadoc: x cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see Visibility#unknown Invalid ref: non existent field\n"
				+ "	                  ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see Visibility#vf_private Invalid ref: non visible field\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\IX.java (at line 9)\n"
				+ "	* @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class\n"
				+ "	                           ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "6. ERROR in test\\IX.java (at line 10)\n"
				+ "	* @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n");
	}

	public void test032() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"	/**\n" +
				"	 * Valid other package visible class fields references\n" +
				"	 *\n" +
				"	 * @see VisibilityPublic#vf_public Valid ref to not visible field of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class\n" +
				"	 */\n" +
				"public interface IX {\n" +
				"	public void foo();\n" +
				"}\n"
			}
		);
	}

	public void test033() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
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
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see VisibilityPackage#unknown Invalid ref to non existent field of other package non visible class\n"
				+ "	       ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class\n"
				+ "	                        ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n"
				+ "	                        ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 9)\n"
				+ "	* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\IX.java (at line 10)\n"
				+ "	* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n"
				+ "	                                 ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "6. ERROR in test\\IX.java (at line 11)\n"
				+ "	* @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n"
				+ "	                                 ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n");
	}

	// @see method references
	public void test040() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Valid local methods references with array\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) Valid local method reference\n"
					+ "	 * @see #smr_foo(char[], int[][], String[][][], Vector[][][][]) Valid local method reference\n"
					+ "	 * @see #smr_foo(char[],int[][],java.lang.String[][][],java.util.Vector[][][][]) Valid local method reference\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra);\n"
					+ "}\n" });
	}

	public void test041() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references with array (non applicable arrays)\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra);\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type IX is not applicable for the arguments (char[], int[][], String[][], Vector[][][][])\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test042() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo() Valid local method reference\n"
					+ "	 * @see #smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference\n"
					+ "	 * @see #smr_foo(java.lang.String, java.lang.String, int) Valid local method reference   \n"
					+ "	 * @see #smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo();\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i);\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);\n"
					+ "}\n" });
	}

	public void test043() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #unknown() Invalid ref: undefined local method reference\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 4)\n"
				+ "	* @see #unknown() Invalid ref: undefined local method reference\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type IX\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test044() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(int) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo();\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i);\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 4)\n"
				+ "	* @see #smr_foo(int) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo() in the type IX is not applicable for the arguments (int)\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 5)\n"
				+ "	* @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type IX is not applicable for the arguments (float, long, char, short, byte, int, boolean)\n"
				+ "----------\n"
				+ "3. ERROR in IX.java (at line 6)\n"
				+ "	* @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type IX is not applicable for the arguments (String, String, int, String)\n"
				+ "----------\n"
				+ "4. ERROR in IX.java (at line 7)\n"
				+ "	* @see #smr_foo(boolean) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type IX is not applicable for the arguments (boolean)\n"
				+ "----------\n"
				+ "5. ERROR in IX.java (at line 8)\n"
				+ "	* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
				+ "	                ^^^^^^^^^\n"
				+ "Javadoc: Hashtable cannot be resolved to a type\n"
				+ "----------\n"
				+ "6. ERROR in IX.java (at line 8)\n"
				+ "	* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
				+ "	                             ^^^^^^\n"
				+ "Javadoc: Vector cannot be resolved to a type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test045() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see IX#smr_foo() Valid local method reference\n"
					+ "	 * @see IX#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference\n"
					+ "	 * @see IX#smr_foo(String x, java.lang.String y, int z) Valid local method reference   \n"
					+ "	 * @see IX#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void smr_foo();\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i);\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);\n"
					+ "}\n" });
	}

	public void test046() {
		this.runConformTest(
			new String[] {
				"test/deep/qualified/name/p/IX.java",
				"package test.deep.qualified.name.p;\n"
					+ "import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see test.deep.qualified.name.p.IX#smr_foo() Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.IX#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.IX#smr_foo(String x, java.lang.String y, int z) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.IX#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void smr_foo();\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i);\n"
					+ "	public void smr_foo(java.util.Hashtable h, Vector v, boolean b);\n"
					+ "}\n" });
	}

	public void test047() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Valid package class methods references\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_public() Valid ref: visible method\n"
					+ "	 * @see Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class\n"
					+ "	 * @see test.Visibility#vm_public() Valid ref: visible method\n"
					+ "	 * @see test.Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test048() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-existence)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#unknown() Invalid ref: non-existent method\n"
					+ "	 * @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class\n"
					+ "	 * @see Unknown#vm_public() Invalid ref: non-existent class\n"
					+ "	 * @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 5)\n"
				+ "	* @see Visibility#unknown() Invalid ref: non-existent method\n"
				+ "	                  ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type Visibility\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class\n"
				+ "	                           ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type Visibility.VcPublic\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see Unknown#vm_public() Invalid ref: non-existent class\n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class\n"
				+ "	       ^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Visibility.Unknown cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test049() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-visible)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_private() Invalid ref: non-visible method\n"
					+ "	 * @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
					+ "	 * @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 5)\n"
				+ "	* @see Visibility#vm_private() Invalid ref: non-visible method\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type Visibility is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type Visibility.VcPublic is not visible\n"
				+ "----------\n");
	}

	public void test050() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-applicable)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_private(int) Invalid ref: non-applicable method\n"
					+ "	 * @see Visibility#vm_public(String) Invalid ref: non-applicable method\n"
					+ "	 * @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class\n"
					+ "	 * @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 5)\n"
				+ "	* @see Visibility#vm_private(int) Invalid ref: non-applicable method\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type Visibility is not applicable for the arguments (int)\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see Visibility#vm_public(String) Invalid ref: non-applicable method\n"
				+ "	                  ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type Visibility is not applicable for the arguments (String)\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type Visibility.VcPublic is not applicable for the arguments (Integer, byte)\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class\n"
				+ "	                           ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type Visibility.VcPublic is not applicable for the arguments (Double, Boolean)\n"
				+ "----------\n");
	}

	public void test051() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "	/**\n"
					+ "	 * Invalid other package non visible class methods references (non existent/visible arguments)\n"
					+ "	 * \n"
					+ "	 * @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n"
					+ "	 * @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n" +
			"1. WARNING in test\\IX.java (at line 2)\n"+
			"	import test.copy.*;\n"+
			"	       ^^^^^^^^^\n"+
			"The import test.copy is never used\n"+
			"----------\n"+
			"2. ERROR in test\\IX.java (at line 6)\n" +
			"	* @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"3. ERROR in test\\IX.java (at line 7)\n" +
			"	* @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type test.copy.VisibilityPackage is not visible\n" +
			"----------\n");
	}

	public void test052() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n" +
				"import test.copy.VisibilityPublic;\n" +
				"	/**\n" +
				"	 * Valid other package visible class methods references \n" +
				"	 * \n" +
				"	 * @see VisibilityPublic#vm_public() Valid ref to not visible method of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class\n" +
				"	 */\n" +
				"public interface IX {\n" +
				"	public void foo();\n" +
				"}\n"
			}
		);
	}

	public void test053() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "	/**\n"
					+ "	 * Valid other package visible class methods references \n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPublic#vm_public() Valid ref to not visible method of other package class\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class\n"
					+ "	 */\n"
					+ "public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	/*
	 * (non-Javadoc)
	 * Javadoc method comment in Interface
	 */
	// @deprecated tag
	public void test060() {
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	public void foo(IX x) {\n"
					+ "	 x.foo();\n"
					+ "	}\n"
					+ "}\n",
				"IX.java",
				"public interface IX {\n"
					+ "  /** \n"
					+ "   * \n"
					+ "   * **   ** ** ** @deprecated */\n"
					+ "	public void foo();\n"
					+ "}\n",
				},
			"----------\n"
				+ "1. WARNING in X.java (at line 3)\n"
				+ "	x.foo();\n"
				+ "	  ^^^^^\n"
				+ "The method foo() from the type IX is deprecated\n"
				+ "----------\n",
				null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}

	public void test061() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/** @deprecated */\n"
					+ "	int x=0;\n"
					+ "	/**\n"
					+ "	 * @see #x\n"
					+ "	 */\n"
					+ "	void foo();\n"
					+ "}\n",
				"IY.java",
				"/** @deprecated */\n"
					+ "public interface IY {\n"
					+ "	int y=0;\n"
					+ "	/**\n"
					+ "	 * @see IX#x\n"
					+ "	 * @see IY\n"
					+ "	 * @see IY#y\n"
					+ "	 */\n"
					+ "	void foo();\n"
					+ "}\n",
				"X.java",
				"public class X {\n"
					+ "	int x;\n"
					+ "	/**\n"
					+ "	 * @see IX#x\n"
					+ "	 * @see IY\n"
					+ "	 * @see IY#y\n"
					+ "	 */\n"
					+ "	void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	* @see IX#x\n"
				+ "	          ^\n"
				+ "Javadoc: The field IX.x is deprecated\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 5)\n"
				+ "	* @see IY\n"
				+ "	       ^^\n"
				+ "Javadoc: The type IY is deprecated\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 6)\n"
				+ "	* @see IY#y\n"
				+ "	       ^^\n"
				+ "Javadoc: The type IY is deprecated\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 6)\n"
				+ "	* @see IY#y\n"
				+ "	          ^\n"
				+ "Javadoc: The field IY.y is deprecated\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test062() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	void foo(IX x) {\n"
					+ "		x.foo(2);\n"
					+ "	}\n"
					+ "}\n",
				"IX.java",
				"public interface IX {\n"
					+ "  /** \n"
					+ "   * Valid tags with deprecation\n"
					+ "   *\n"
					+ "   * @param x Valid param tag\n"
					+ "   * @return Valid return tag\n"
					+ "   * @throws NullPointerException Valid throws tag\n"
					+ "   * @exception IllegalArgumentException Valid throws tag\n"
					+ "   * @see X Valid see tag\n"
					+ "   * @deprecated\n"
					+ "   */\n"
					+ "	public String foo(int x);\n"
					+ "}\n",
				},
			"----------\n"
				+ "1. WARNING in X.java (at line 4)\n"
				+ "	x.foo(2);\n"
				+ "	  ^^^^^^\n"
				+ "The method foo(int) from the type IX is deprecated\n"
				+ "----------\n");
	}

	public void test063() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
					+ "	\n"
					+ "	void foo(IX x) {\n"
					+ "		x.foo(2);\n"
					+ "	}\n"
					+ "}\n",
				"IX.java",
				"public interface IX {\n"
					+ "  /** \n"
					+ "   * Invalid javadoc tags with valid deprecation\n"
					+ "   *\n"
					+ "   * @param\n"
					+ "   * @return String\n"
					+ "   * @throws Unknown\n"
					+ "   * @see \"Invalid\n"
					+ "   * @see Unknown\n"
					+ "   * @param x\n"
					+ "   * @deprecated\n"
					+ "   */\n"
					+ "	public String foo(int x);\n"
					+ "}\n",
				},
			"----------\n"
				+ "1. WARNING in X.java (at line 4)\n"
				+ "	x.foo(2);\n"
				+ "	  ^^^^^^\n"
				+ "The method foo(int) from the type IX is deprecated\n"
				+ "----------\n"
				+ "----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	* @param\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Missing parameter name\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 7)\n"
				+ "	* @throws Unknown\n"
				+ "	          ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n"
				+ "3. ERROR in IX.java (at line 8)\n"
				+ "	* @see \"Invalid\n"
				+ "	       ^^^^^^^^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "4. ERROR in IX.java (at line 9)\n"
				+ "	* @see Unknown\n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n"
				+ "5. ERROR in IX.java (at line 10)\n"
				+ "	* @param x\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "6. ERROR in IX.java (at line 13)\n"
				+ "	public String foo(int x);\n"
				+ "	                      ^\n"
				+ "Javadoc: Missing tag for parameter x\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @param tag
	public void test064() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid @param: no tags, no args\n"
					+ "	 * Valid @throws/@exception: no tags, no thrown exception\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test065() {
		this.runConformTest(new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test066() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid @param declaration: no arguments, 2 declared tags\n"
					+ "	 * @param x\n"
					+ "	 * 			Invalid param: not an argument on 2 lines\n"
					+ "	 * @param x Invalid param: not an argument\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 4)\n"
				+ "	* @param x\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 6)\n"
				+ "	* @param x Invalid param: not an argument\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test067() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	\n"
					+ "	/**\n"
					+ "	 * Valid @param declaration: 3 arguments, 3 tags in right order\n"
					+ "	 * @param a Valid param\n"
					+ "	 * @param b Valid param \n"
					+ "	 * @param c Valid param\n"
					+ "	 */\n"
					+ "	public void foo(int a, int b, int c);\n"
					+ "}\n" });
	}

	public void test068() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid @param declaration: 3 arguments, 3 correct tags in right order + 2 additional\n"
					+ "	 * @param a Valid param\n"
					+ "	 * @param x Invalid param: not an argument\n"
					+ "	 * @param b Valid param \n"
					+ "	 * @param y Invalid param: not an argument\n"
					+ "	 * @param c Valid param\n"
					+ "	 */\n"
					+ "	public void foo(char a, char b, char c);\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	* @param x Invalid param: not an argument\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 7)\n"
				+ "	* @param y Invalid param: not an argument\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter y is not declared\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test069() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: all arguments are not documented\n"
					+ "	 */\n"
					+ "	public void foo(double a, double b, double c);\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	public void foo(double a, double b, double c);\n"
				+ "	                       ^\n"
				+ "Javadoc: Missing tag for parameter a\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 5)\n"
				+ "	public void foo(double a, double b, double c);\n"
				+ "	                                 ^\n"
				+ "Javadoc: Missing tag for parameter b\n"
				+ "----------\n"
				+ "3. ERROR in IX.java (at line 5)\n"
				+ "	public void foo(double a, double b, double c);\n"
				+ "	                                           ^\n"
				+ "Javadoc: Missing tag for parameter c\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test070() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid @param: mix of all possible errors (missing a, not argument tag and duplicated)\n"
					+ "	 * @param c Valid param\n"
					+ "	 * @param x Invalid param: not an argument\n"
					+ "	 * @param b Valid param\n"
					+ "	 * @param c Invalid param: duplicated\n"
					+ "	 * @param\n"
					+ "	 */\n"
					+ "	public void foo(double a, long b, int c);\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	* @param x Invalid param: not an argument\n"
				+ "	         ^\n"
				+ "Javadoc: Parameter x is not declared\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 7)\n"
				+ "	* @param c Invalid param: duplicated\n"
				+ "	         ^\n"
				+ "Javadoc: Duplicate tag for parameter\n"
				+ "----------\n"
				+ "3. ERROR in IX.java (at line 8)\n"
				+ "	* @param\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Missing parameter name\n"
				+ "----------\n"
				+ "4. ERROR in IX.java (at line 10)\n"
				+ "	public void foo(double a, long b, int c);\n"
				+ "	                       ^\n"
				+ "Javadoc: Missing tag for parameter a\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @throws/@exception tag
	public void test071() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid @throws tags: documented exception are unchecked\n"
					+ "	 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)\n"
					+ "	 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test072() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * @throws java.awt.AWTexception Invalid exception: unknown type\n"
					+ "	 * @throws IOException Invalid exception: unknown type\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 3)\n"
				+ "	* @throws java.awt.AWTexception Invalid exception: unknown type\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: java.awt.AWTexception cannot be resolved to a type\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 4)\n"
				+ "	* @throws IOException Invalid exception: unknown type\n"
				+ "	          ^^^^^^^^^^^\n"
				+ "Javadoc: IOException cannot be resolved to a type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test073() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"import java.io.FileNotFoundException;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 4)\n"
				+ "	* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Exception EOFException is not declared\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 5)\n"
				+ "	* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked\n"
				+ "	          ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Exception FileNotFoundException is not declared\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test074() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"import java.io.FileNotFoundException;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid @throws tags: documented exception are unchecked but some thrown exception are invalid\n"
					+ "	 * @throws IllegalAccessException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)\n"
					+ "	 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)\n"
					+ "	 */\n"
					+ "	public void foo() throws\n"
					+ "		IllegalAccessException, \n"
					+ "		InvalidException, \n"
					+ "		String, \n"
					+ "		java.io.EOFException, \n"
					+ "		FileNotFoundException, \n"
					+ "		IOException;\n"
					+ "}\n" },
					"----------\n" +
					"1. ERROR in IX.java (at line 13)\n" +
					"	InvalidException, \n" +
					"	^^^^^^^^^^^^^^^^\n" +
					"InvalidException cannot be resolved to a type\n" +
					"----------\n" +
					"2. ERROR in IX.java (at line 14)\n" +
					"	String, \n" +
					"	^^^^^^\n" +
					"No exception of type String can be thrown; an exception type must be a subclass of Throwable\n" +
					"----------\n" +
					"3. ERROR in IX.java (at line 15)\n" +
					"	java.io.EOFException, \n" +
					"	^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception EOFException\n" +
					"----------\n" +
					"4. ERROR in IX.java (at line 16)\n" +
					"	FileNotFoundException, \n" +
					"	^^^^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: Missing tag for declared exception FileNotFoundException\n" +
					"----------\n" +
					"5. ERROR in IX.java (at line 17)\n" +
					"	IOException;\n" +
					"	^^^^^^^^^^^\n" +
					"IOException cannot be resolved to a type\n" +
					"----------\n");
	}

	// @return tag
	public void test080() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid return declaration\n"
					+ "	 *\n"
					+ "	 * @return Return an int\n"
					+ "	 */\n"
					+ "	public int foo();\n"
					+ "}\n" });
	}

	public void test081() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid empty return declaration\n"
					+ "	 *\n"
					+ "	 * @return string\n"
					+ "	 */\n"
					+ "	public String foo();\n"
					+ "}\n" });
	}

	public void test082() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Missing return declaration\n"
					+ "	 */\n"
					+ "	public Object[] foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	public Object[] foo();\n"
				+ "	       ^^^^^^^^\n"
				+ "Javadoc: Missing tag for return type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test083() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid return declaration\n"
					+ "	 *\n"
					+ "	 * @return Dimension\n"
					+ "	 * @return Duplicated\n"
					+ "	 */\n"
					+ "	public double foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 6)\n"
				+ "	* @return Duplicated\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Duplicate tag for return type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test084() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid return declaration\n"
					+ "	 *\n"
					+ "	 * @return Invalid return on void method\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	* @return Invalid return on void method\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @see tag: string
	public void test090() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid string references \n"
					+ "	 *\n"
					+ "	 * @see \"unterminated string\n"
					+ "	 * @see \"invalid\" no text allowed after the string\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	* @see \"unterminated string\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 6)\n"
				+ "	* @see \"invalid\" no text allowed after the string\n"
				+ "	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test091() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid string references \n"
					+ "	 *\n"
					+ "	 * @see \"Valid normal string\"\n"
					+ "	 * @see \"Valid \\\"string containing\\\" \\\"double-quote\\\"\"\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	// @see tag: URL
	public void test092() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid URL link references \n"
					+ "	 *\n"
					+ "	 * @see <a\n"
					+ "	 * @see <a href=\"invalid\">invalid</a> no text allowed after the href\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	* @see <a\n"
				+ "	       ^^\n"
				+ "Javadoc: Malformed link reference\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 6)\n"
				+ "	* @see <a href=\"invalid\">invalid</a> no text allowed after the href\n"
				+ "	                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @see tag: class references
	public void test095() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "public interface IX {\n"
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
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test096() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid local classes references \n"
					+ "	 *\n"
					+ "	 * @see Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
					+ "	 * @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
					+ "	 * @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
					+ "	 * @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
					+ "	 * @see Unknown Invalid ref: unknown class \n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 9)\n"
				+ "	* @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type test.Visibility.AvcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\IX.java (at line 10)\n"
				+ "	* @see Unknown Invalid ref: unknown class \n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test097() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"public interface IX {\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				// Inner classes are not visible in generated documentation
				//"	 * @see VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"	public void foo();\n" +
				"}\n"
				}
			);
	}

	public void test098() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid external classes references \n"
					+ "	 *\n"
					+ "	 * @see VisibilityPackage Invalid ref: non visible class \n"
					+ "	 * @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see VisibilityPackage Invalid ref: non visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class \n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n");
	}

	public void test099() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n" +
				"public interface IX {\n" +
				"	/**\n" +
				"	 * Valid external classes references \n" +
				"	 *\n" +
				"	 * @see test.copy.VisibilityPublic Valid ref: visible class through import => no warning on import\n" +
				// Inner classes are not visible in generated documentation
				//"	 * @see test.copy.VisibilityPublic.VpPublic Valid ref: visible inner class in visible class \n" +
				"	 */\n" +
				"	public void foo();\n" +
				"}\n"
			}
		);
	}

	// @see tag: field references
	public void test105() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid local class field references\n"
					+ "	 *\n"
					+ "	 * @see Visibility#vf_public Valid ref: visible field\n"
					+ "	 * @see Visibility.VcPublic#vf_public Valid ref: visible field in visible inner class\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test106() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "public interface IX {\n"
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
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see #x Invalid ref: non existent field\n"
				+ "	        ^\n"
				+ "Javadoc: x cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see Visibility#unknown Invalid ref: non existent field\n"
				+ "	                  ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see Visibility#vf_private Invalid ref: non visible field\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 9)\n"
				+ "	* @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\IX.java (at line 10)\n"
				+ "	* @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class\n"
				+ "	                           ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "6. ERROR in test\\IX.java (at line 11)\n"
				+ "	* @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n");
	}

	public void test107() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n" +
				"import test.copy.*;\n" +
				"public interface IX {\n" +
				"	/**\n" +
				"	 * Invalid other package non visible class fields references\n" +
				"	 *\n" +
				"	 * @see VisibilityPublic#vf_public Valid ref to not visible field of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class\n" +
				"	 */\n" +
				"	public void foo();\n" +
				"}\n"
			}
		);
	}

	public void test108() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "public interface IX {\n"
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
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see VisibilityPackage#unknown Invalid ref to non existent field of other package non visible class\n"
				+ "	       ^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPackage is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class\n"
				+ "	                        ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 9)\n"
				+ "	* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class\n"
				+ "	                        ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 10)\n"
				+ "	* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type VisibilityPublic.VpPrivate is not visible\n"
				+ "----------\n"
				+ "5. ERROR in test\\IX.java (at line 11)\n"
				+ "	* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class\n"
				+ "	                                 ^^^^^^^\n"
				+ "Javadoc: unknown cannot be resolved or is not a field\n"
				+ "----------\n"
				+ "6. ERROR in test\\IX.java (at line 12)\n"
				+ "	* @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class\n"
				+ "	                                 ^^^^^^^^^^\n"
				+ "Javadoc: The field vf_private is not visible\n"
				+ "----------\n");
	}

	// @see method references
	public void test110() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"import java.util.Vector;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid local methods references with array\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) Valid local method reference\n"
					+ "	 * @see #smr_foo(char[], int[][], String[][][], Vector[][][][]) Valid local method reference\n"
					+ "	 * @see #smr_foo(char[],int[][],java.lang.String[][][],java.util.Vector[][][][]) Valid local method reference\n"
					+ "	 */  \n"
					+ "	public void foo();\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra);\n"
					+ "}\n" });
	}

	public void test111() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"import java.util.Vector;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references with array (non applicable arrays)\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
					+ "	 */  \n"
					+ "	public void foo();\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra);\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 6)\n"
				+ "	* @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type IX is not applicable for the arguments (char[], int[][], String[][], Vector[][][][])\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test112() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"import java.util.Vector;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo() Valid local method reference\n"
					+ "	 * @see #smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference\n"
					+ "	 * @see #smr_foo(java.lang.String, java.lang.String, int) Valid local method reference   \n"
					+ "	 * @see #smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "	public void foo();\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo();\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i);\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);\n"
					+ "}\n" });
	}

	public void test113() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #unknown() Invalid ref: undefined local method reference\n"
					+ "	 */  \n"
					+ "	public void foo();\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	* @see #unknown() Invalid ref: undefined local method reference\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type IX\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test114() {
		this.runNegativeTest(
			new String[] {
				"IX.java",
				"public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid local methods references\n"
					+ "	 * \n"
					+ "	 * @see #smr_foo(int) Invalid ref: invalid arguments declaration\n"
					+ "	 * @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(boolean) Invalid ref: local method not applicable\n"
					+ "	 * @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
					+ "	 */  \n"
					+ "	public void foo();\n"
					+ "\n"
					+ "	// Empty methods definition for reference\n"
					+ "	public void smr_foo();\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i);\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in IX.java (at line 5)\n"
				+ "	* @see #smr_foo(int) Invalid ref: invalid arguments declaration\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo() in the type IX is not applicable for the arguments (int)\n"
				+ "----------\n"
				+ "2. ERROR in IX.java (at line 6)\n"
				+ "	* @see #smr_foo(float, long, char, short, byte, int, boolean) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type IX is not applicable for the arguments (float, long, char, short, byte, int, boolean)\n"
				+ "----------\n"
				+ "3. ERROR in IX.java (at line 7)\n"
				+ "	* @see #smr_foo(String, String, int, String) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(String, String, int) in the type IX is not applicable for the arguments (String, String, int, String)\n"
				+ "----------\n"
				+ "4. ERROR in IX.java (at line 8)\n"
				+ "	* @see #smr_foo(boolean) Invalid ref: local method not applicable\n"
				+ "	        ^^^^^^^\n"
				+ "Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type IX is not applicable for the arguments (boolean)\n"
				+ "----------\n"
				+ "5. ERROR in IX.java (at line 9)\n"
				+ "	* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
				+ "	                ^^^^^^^^^\n"
				+ "Javadoc: Hashtable cannot be resolved to a type\n"
				+ "----------\n"
				+ "6. ERROR in IX.java (at line 9)\n"
				+ "	* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type\n"
				+ "	                             ^^^^^^\n"
				+ "Javadoc: Vector cannot be resolved to a type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test115() {
		this.runConformTest(
			new String[] {
				"IX.java",
				"import java.util.Vector;\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see IX#smr_foo() Valid local method reference\n"
					+ "	 * @see IX#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference\n"
					+ "	 * @see IX#smr_foo(String x, java.lang.String y, int z) Valid local method reference   \n"
					+ "	 * @see IX#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "public interface IX {\n"
					+ "	public void smr_foo();\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i);\n"
					+ "	public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b);\n"
					+ "}\n" });
	}

	public void test116() {
		this.runConformTest(
			new String[] {
				"test/deep/qualified/name/p/IX.java",
				"package test.deep.qualified.name.p;\n"
					+ "import java.util.Vector;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid local methods references\n"
					+ "	 * \n"
					+ "	 * @see test.deep.qualified.name.p.IX#smr_foo() Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.IX#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference\n"
					+ "	 * @see test.deep.qualified.name.p.IX#smr_foo(String x, java.lang.String y, int z) Valid local method reference   \n"
					+ "	 * @see test.deep.qualified.name.p.IX#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference\n"
					+ "	 */  \n"
					+ "	public void smr_foo();\n"
					+ "	public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d);\n"
					+ "	public void smr_foo(String str1, java.lang.String str2, int i);\n"
					+ "	public void smr_foo(java.util.Hashtable h, Vector v, boolean b);\n"
					+ "}\n" });
	}

	public void test117() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid package class methods references\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_public() Valid ref: visible method\n"
					+ "	 * @see Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class\n"
					+ "	 * @see test.Visibility#vm_public() Valid ref: visible method\n"
					+ "	 * @see test.Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class\n"
					+ "	 */  \n"
					+ "	public void foo();\n"
					+ "}\n" });
	}

	public void test118() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-existence)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#unknown() Invalid ref: non-existent method\n"
					+ "	 * @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class\n"
					+ "	 * @see Unknown#vm_public() Invalid ref: non-existent class\n"
					+ "	 * @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class\n"
					+ "	 */  \n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see Visibility#unknown() Invalid ref: non-existent method\n"
				+ "	                  ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type Visibility\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class\n"
				+ "	                           ^^^^^^^\n"
				+ "Javadoc: The method unknown() is undefined for the type Visibility.VcPublic\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see Unknown#vm_public() Invalid ref: non-existent class\n"
				+ "	       ^^^^^^^\n"
				+ "Javadoc: Unknown cannot be resolved to a type\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 9)\n"
				+ "	* @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class\n"
				+ "	       ^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Visibility.Unknown cannot be resolved to a type\n"
				+ "----------\n");
	}

	public void test119() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-visible)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_private() Invalid ref: non-visible method\n"
					+ "	 * @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
					+ "	 * @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
					+ "	 */  \n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see Visibility#vm_private() Invalid ref: non-visible method\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type Visibility is not visible\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: The type Visibility.VcPrivate is not visible\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() from the type Visibility.VcPublic is not visible\n"
				+ "----------\n");
	}

	public void test120() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid package class methods references (non-applicable)\n"
					+ "	 * \n"
					+ "	 * @see Visibility#vm_private(int) Invalid ref: non-applicable method\n"
					+ "	 * @see Visibility#vm_public(String) Invalid ref: non-applicable method\n"
					+ "	 * @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class\n"
					+ "	 * @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class\n"
					+ "	 */  \n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\IX.java (at line 6)\n"
				+ "	* @see Visibility#vm_private(int) Invalid ref: non-applicable method\n"
				+ "	                  ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type Visibility is not applicable for the arguments (int)\n"
				+ "----------\n"
				+ "2. ERROR in test\\IX.java (at line 7)\n"
				+ "	* @see Visibility#vm_public(String) Invalid ref: non-applicable method\n"
				+ "	                  ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type Visibility is not applicable for the arguments (String)\n"
				+ "----------\n"
				+ "3. ERROR in test\\IX.java (at line 8)\n"
				+ "	* @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class\n"
				+ "	                           ^^^^^^^^^^\n"
				+ "Javadoc: The method vm_private() in the type Visibility.VcPublic is not applicable for the arguments (Integer, byte)\n"
				+ "----------\n"
				+ "4. ERROR in test\\IX.java (at line 9)\n"
				+ "	* @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class\n"
				+ "	                           ^^^^^^^^^\n"
				+ "Javadoc: The method vm_public() in the type Visibility.VcPublic is not applicable for the arguments (Double, Boolean)\n"
				+ "----------\n");
	}

	public void test121() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "import test.copy.*;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Invalid other package non visible class methods references (non existent/visible arguments)\n"
					+ "	 * \n"
					+ "	 * @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n"
					+ "	 * @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" },
			"----------\n" +
			"1. WARNING in test\\IX.java (at line 2)\n"+
			"	import test.copy.*;\n"+
			"	       ^^^^^^^^^\n"+
			"The import test.copy is never used\n"+
			"----------\n"+
			"2. ERROR in test\\IX.java (at line 7)\n" +
			"	* @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type VisibilityPackage is not visible\n" +
			"----------\n" +
			"3. ERROR in test\\IX.java (at line 8)\n" +
			"	* @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type test.copy.VisibilityPackage is not visible\n" +
			"----------\n");
	}

	public void test122() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n" +
				"import test.copy.VisibilityPublic;\n" +
				"public interface IX {\n" +
				"	/**\n" +
				"	 * Valid other package visible class methods references \n" +
				"	 * \n" +
				"	 * @see VisibilityPublic#vm_public() Valid ref to not visible method of other package class\n" +
				"	 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class\n" +
				"	 */\n" +
				"	public void foo();\n" +
				"}\n"
			}
		);
	}

	public void test123() {
		runConformReferenceTest(
			new String[] {
				"test/IX.java",
				"package test;\n"
					+ "public interface IX {\n"
					+ "	/**\n"
					+ "	 * Valid other package visible class methods references \n"
					+ "	 * \n"
					+ "	 * @see test.copy.VisibilityPublic#vm_public() Valid ref to not visible method of other package class\n"
					+ "	 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class\n"
					+ "	 */\n"
					+ "	public void foo();\n"
					+ "}\n" });
	}
}
