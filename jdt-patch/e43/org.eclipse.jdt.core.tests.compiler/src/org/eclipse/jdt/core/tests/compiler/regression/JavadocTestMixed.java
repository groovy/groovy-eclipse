/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JavadocTestMixed extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;

	public JavadocTestMixed(String name) {
		super(name);
	}

	public static Class javadocTestClass() {
		return JavadocTestMixed.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug77602";
//		TESTS_NAMES = new String[] { "testBug80910" };
//		TESTS_NUMBERS = new int[] { 31, 32, 33 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(javadocTestClass());
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
		if (this.reportMissingJavadocTags != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.docCommentSupport = CompilerOptions.ENABLED;
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocComments = null;
	}

	/*
	 * Test missing javadoc
	 */
	public void test001() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** */\n"
					+ "public class X {\n"
					+ "  /** */\n"
					+ "  public int x;\n"
					+ "  /** */\n"
					+ "	 public X() {}\n"
					+ "  /** */\n"
					+ "	 public void foo() {}\n"
					+ "}\n" });
	}

	public void test002() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** */\n"
					+ "class X {\n"
					+ "  /** */\n"
					+ "  int x;\n"
					+ "  /** */\n"
					+ "	 X() {}\n"
					+ "  /** */\n"
					+ "  void foo() {}\n"
					+ "}\n" });
	}

	public void test003() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** */\n"
					+ "class X {\n"
					+ "  /** */\n"
					+ "  protected int x;\n"
					+ "  /** */\n"
					+ "  protected X() {}\n"
					+ "  /** */\n"
					+ "  protected void foo() {}\n"
					+ "}\n" });
	}

	public void test004() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** */\n"
					+ "class X {\n"
					+ "  /** */\n"
					+ "  private int x;\n"
					+ "  /** */\n"
					+ "  private X() {}\n"
					+ "  /** */\n"
					+ "  private void foo() {}\n"
					+ "}\n" });
	}

	public void test005() {
		this.reportInvalidJavadoc = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	public X() {}\n"
					+ "\n"
					+ "	public void foo() {}\n"
					+ "}\n" });
	}

	public void test006() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	String s1 = \"non-terminated;\n" +
				"	void foo() {}\n" +
				"	String s2 = \"terminated\";\n" +
				"}\n"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	String s1 = \"non-terminated;\n" +
				"	            ^^^^^^^^^^^^^^^^\n" +
				"String literal is not properly closed by a double-quote\n" +
				"----------\n"
		);
	}

	public void test010() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/** Field javadoc comment */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	/** Constructor javadoc comment */\n"
					+ "	public X() {\n"
					+ "	}\n"
					+ "	/** Method javadoc comment */\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 2)\n"
				+ "	public class X {\n"
				+ "	             ^\n"
				+ "Javadoc: Missing comment for public declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test011() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** Class javadoc comment */\n"
					+ "public class X {\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	/** Constructor javadoc comment */\n"
					+ "	public X() {\n"
					+ "	}\n"
					+ "	/** Method javadoc comment */\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 4)\n"
				+ "	public int x;\n"
				+ "	           ^\n"
				+ "Javadoc: Missing comment for public declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test012() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** Class javadoc comment */\n"
					+ "public class X {\n"
					+ "	/** Field javadoc comment */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	public X() {\n"
					+ "	}\n"
					+ "	/** Method javadoc comment */\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 7)\n"
				+ "	public X() {\n"
				+ "	       ^^^\n"
				+ "Javadoc: Missing comment for public declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test013() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** Class javadoc comment */\n"
					+ "public class X {\n"
					+ "	/** Field javadoc comment */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	/** Constructor javadoc comment */\n"
					+ "	public X() {\n"
					+ "	}\n"
					+ "	public void foo(int a) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 10)\n"
				+ "	public void foo(int a) {\n"
				+ "	            ^^^^^^^^^^\n"
				+ "Javadoc: Missing comment for public declaration\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/*
	 * Test mixing javadoc comments
	 */
	public void test021() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" });
	}

	public void test022() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Unexpected tag in class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " * @param x\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @param x\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test023() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Unexpected tag in field javadoc\n"
					+ " * @throws InvalidException\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 10)\n"
				+ "	* @throws InvalidException\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test024() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Wrong tags order in constructor javadoc\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @param str Valid param tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 19)\n"
				+ "	* @param str Valid param tag\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 22)\n"
				+ "	public X(String str) {\n"
				+ "	                ^^^\n"
				+ "Javadoc: Missing tag for parameter str\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test025() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Wrong param tag in method javadoc\n"
					+ " * @param vector Invalid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 26)\n"
				+ "	* @param vector Invalid param tag\n"
				+ "	         ^^^^^^\n"
				+ "Javadoc: Parameter vector is not declared\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 33)\n"
				+ "	public String foo(java.util.Vector list) {\n"
				+ "	                                   ^^^^\n"
				+ "Javadoc: Missing tag for parameter list\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test026() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Invalid see tag in class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Invalid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>unexpected text\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Missing throws tag in constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) throws java.io.IOException {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Missing return tag in method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 5)\n"
				+ "	* @see \"Test class X\n"
				+ "	       ^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see <a href=\"http://www.ibm.com\">Valid URL</a>unexpected text\n"
				+ "	                                              ^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Unexpected text\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 22)\n"
				+ "	public X(String str) throws java.io.IOException {\n"
				+ "	                            ^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Missing tag for declared exception IOException\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 32)\n"
				+ "	public String foo(java.util.Vector list) {\n"
				+ "	       ^^^^^^\n"
				+ "Javadoc: Missing tag for return type\n"
				+ "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/*
	 * Javadoc on invalid syntax
	 */
	public void test030() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc on invalid declaration\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "protected class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 7)\n"
				+ "	protected class X {\n"
				+ "	                ^\n"
				+ "Illegal modifier for the class X; only public, abstract & final are permitted\n"
				+ "----------\n");
	}

	public void test031() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc on invalid declaration\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 12)\n"
				+ "	public int x\n"
				+ "	           ^\n"
				+ "Syntax error, insert \";\" to complete ClassBodyDeclarations\n"
				+ "----------\n");
	}

	public void test032() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc on invalid declaration\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) \n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 22)\n"
				+ "	public X(String str) \n"
				+ "	                   ^\n"
				+ "Syntax error on token \")\", { expected after this token\n"
				+ "----------\n");
	}

	public void test033() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc on invalid declaration\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector ) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
					this.complianceLevel < ClassFileConstants.JDK1_5
					? "----------\n"
						+ "1. ERROR in test\\X.java (at line 23)\n"
						+ "	}\n"
						+ "	^\n"
						+ "Syntax error, insert \"}\" to complete ClassBody\n"
						+ "----------\n"
						+ "2. ERROR in test\\X.java (at line 26)\n"
						+ "	* @param list Valid param tag\n"
						+ "	         ^^^^\n"
						+ "Javadoc: Parameter list is not declared\n"
						+ "----------\n"
						+ "3. ERROR in test\\X.java (at line 33)\n"
						+ "	public String foo(java.util.Vector ) {\n"
						+ "	                            ^^^^^^\n"
						+ "Syntax error on token \"Vector\", VariableDeclaratorId expected after this token\n"
						+ "----------\n"
						+ "4. ERROR in test\\X.java (at line 36)\n"
						+ "	}\n"
						+ "	^\n"
						+ "Syntax error on token \"}\", delete this token\n"
						+ "----------\n"
					: "----------\n"
						+ "1. ERROR in test\\X.java (at line 23)\n"
						+ "	}\n"
						+ "	^\n"
						+ "Syntax error, insert \"}\" to complete ClassBody\n"
						+ "----------\n"
						+ "2. ERROR in test\\X.java (at line 26)\n"
						+ "	* @param list Valid param tag\n"
						+ "	         ^^^^\n"
						+ "Javadoc: Parameter list is not declared\n"
						+ "----------\n"
						+ "3. ERROR in test\\X.java (at line 33)\n"
						+ "	public String foo(java.util.Vector ) {\n"
						+ "	                           ^\n"
						+ "Syntax error on token \".\", ... expected\n"
						+ "----------\n"
						+ "4. ERROR in test\\X.java (at line 36)\n"
						+ "	}\n"
						+ "	^\n"
						+ "Syntax error on token \"}\", delete this token\n"
						+ "----------\n");
	}

	public void test040() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
					"	/**\n" +
					"	/**\n" +
					"	/**\n" +
					"	/** \n" +
					"	 * @param str\n" +
					"	 * @param x\n" +
					"	 */\n" +
					"	public void bar(String str, int x) {\n" +
					"	}\n" +
					"	public void foo() {\n" +
					"		bar(\"toto\", 0 /* block comment inline */);\n" +
					"	}\n" +
					"}\n" });
	}

	public void test041() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
					"	/**\n" +
					"	 * @see String\n" +
					"	 * @see #\n" +
					"	 * @return String\n" +
					"	 */\n" +
					"	String bar() {return \"\";}\n" +
					"}\n"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @see #\n" +
				"	       ^\n" +
				"Javadoc: Invalid reference\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
			);
	}
}
