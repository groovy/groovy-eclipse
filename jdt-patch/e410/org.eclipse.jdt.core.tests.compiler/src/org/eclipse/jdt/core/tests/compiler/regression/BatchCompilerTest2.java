/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation.
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

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class BatchCompilerTest2 extends AbstractBatchCompilerTest {

	static {
//		TESTS_NAMES = new String[] { "test440477" };
//		TESTS_NUMBERS = new int[] { 306 };
//		TESTS_RANGE = new int[] { 298, -1 };
	}
	
	/**
	 * This test suite only needs to be run on one compliance.
	 * As it includes some specific 1.5 tests, it must be used with a least a 1.5 VM
	 * and not be duplicated in general test suite.
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_11);
	}
	public static Class testClass() {
		return BatchCompilerTest2.class;
	}
	public BatchCompilerTest2(String name) {
		super(name);
	}
	public void test001() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.List;\n" +
					"\n" +
					"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" +
					")\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		if (false) {\n" +
					"			;\n" +
					"		} else {\n" +
					"		}\n" +
					"		Zork z;\n" +
					"	}\n" +
					"}"
		        },
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -10 --enable-preview",
		        "",
		        "Preview of features is supported only at the latest source level\n",
		        true);
}
public void test002() throws Exception {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
							"\n" +
							"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" +
							")\n" +
							"public class X {\n" +
							"	public static void main(String[] args) {\n" +
							"		if (false) {\n" +
							"			;\n" +
							"		} else {\n" +
							"		}\n" +
							"		Zork z;\n" +
							"	}\n" +
							"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
					+ " -11 --enable-preview",
					"",
					"----------\n" +
							"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 11)\n" + 
							"	Zork z;\n" +
							"	^^^^\n" +
							"Zork cannot be resolved to a type\n" + 
							"----------\n" +
							"1 problem (1 error)\n",
							true);
}
public void test003() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"    public static void main(String [] args) {\n" +
					"        I lam = (Integer  x, var y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
					"        lam.apply(20, 200);\n" +
					"    }\n" +
					"}\n" +
					"interface I {\n" +
					"    public void apply(Integer k, Integer z);\n" +
					"}\n"
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
					+ " -11 --enable-preview",
					"",
					"----------\n" + 
					"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
					"	I lam = (Integer  x, var y) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
					"	                         ^\n" + 
					"\'var\' cannot be mixed with non-var parameters\n" + 
					"----------\n" + 
					"1 problem (1 error)\n",
					true);
}
public void test004() throws Exception {
	this.runConformTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"\n" +
					"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" +
					")\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		if (false) {\n" +
					"			;\n" +
					"		} else {\n" +
					"		}\n" +
					"	}\n" +
					"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
					+ " -11 --enable-preview",
					"",
					"",
					true);
	String expectedOutput = "// Compiled from X.java (version 11 : 55.65535, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
public void test005() throws Exception {
	this.runConformTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"\n" +
					"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" +
					")\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		if (false) {\n" +
					"			;\n" +
					"		} else {\n" +
					"		}\n" +
					"	}\n" +
					"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
					+ " --enable-preview -11 ",
					"",
					"",
					true);
	String expectedOutput = "// Compiled from X.java (version 11 : 55.65535, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
public void test006() throws Exception {
	this.runConformTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"\n" +
					"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" +
					")\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		if (false) {\n" +
					"			;\n" +
					"		} else {\n" +
					"		}\n" +
					"	}\n" +
					"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
					+ " -source 11",
					"",
					"",
					true);
	String expectedOutput = "// Compiled from X.java (version 11 : 55.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
public void testBug540123a() throws Exception {
	this.runConformTest(
			new String[] {
					"SecurePrefs.java",
					"public class SecurePrefs {\n" + 
					"  public SecurePrefs node (String s) {\n" + 
					"	  System.out.println(s);\n" + 
					"	  return null;\n" + 
					"  }\n" + 
					"}",
					"SecurePrefsRoot.java",
					"public class SecurePrefsRoot extends SecurePrefs {\n" + 
					"\n" + 
					"	public void foo() {\n" + 
					"		SecurePrefs node = node(\"Hello\");\n" + 
					"		if (node != null)\n" + 
					"			System.out.println(node.toString());\n" + 
					"	}\n" + 
					"	\n" + 
					"	public static void main(String[] args) {\n" + 
					"		new SecurePrefsRoot().foo();\n" + 
					"	}\n" + 
					"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "SecurePrefsRoot.java\""
			+" \"" + OUTPUT_DIR +  File.separator + "SecurePrefs.java\""
					+ " -source 1.3 -target 1.2",
					"",
					"",
					true);
	String expectedOutput = "invokevirtual SecurePrefsRoot.node(java.lang.String) : SecurePrefs [14]";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "SecurePrefsRoot.class", "SecurePrefsRoot", expectedOutput);
}
public void testBug540123b() throws Exception {
	this.runConformTest(
			new String[] {
					"SecurePrefs.java",
					"public class SecurePrefs {\n" + 
					"  public SecurePrefs node (String s) {\n" + 
					"	  System.out.println(s);\n" + 
					"	  return null;\n" + 
					"  }\n" + 
					"}",
					"SecurePrefsRoot.java",
					"public class SecurePrefsRoot extends SecurePrefs {\n" + 
					"\n" + 
					"	public void foo() {\n" + 
					"		SecurePrefs node = node(\"Hello\");\n" + 
					"		if (node != null)\n" + 
					"			System.out.println(node.toString());\n" + 
					"	}\n" + 
					"	\n" + 
					"	public static void main(String[] args) {\n" + 
					"		new SecurePrefsRoot().foo();\n" + 
					"	}\n" + 
					"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "SecurePrefsRoot.java\""
			+" \"" + OUTPUT_DIR +  File.separator + "SecurePrefs.java\""
					+ " -source 1.3",
					"",
					"",
					true);
	String expectedOutput = "invokevirtual SecurePrefsRoot.node(java.lang.String) : SecurePrefs [14]";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "SecurePrefsRoot.class", "SecurePrefsRoot", expectedOutput);
}
public void testBug540123c() throws Exception {
	this.runConformTest(
			new String[] {
					"SecurePrefs.java",
					"public class SecurePrefs {\n" + 
					"  public SecurePrefs node (String s) {\n" + 
					"	  System.out.println(s);\n" + 
					"	  return null;\n" + 
					"  }\n" + 
					"}",
					"SecurePrefsRoot.java",
					"public class SecurePrefsRoot extends SecurePrefs {\n" + 
					"\n" + 
					"	public void foo() {\n" + 
					"		SecurePrefs node = node(\"Hello\");\n" + 
					"		if (node != null)\n" + 
					"			System.out.println(node.toString());\n" + 
					"	}\n" + 
					"	\n" + 
					"	public static void main(String[] args) {\n" + 
					"		new SecurePrefsRoot().foo();\n" + 
					"	}\n" + 
					"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "SecurePrefsRoot.java\""
			+" \"" + OUTPUT_DIR +  File.separator + "SecurePrefs.java\""
					+ " -target 1.3",
					"",
					"",
					true);
	String expectedOutput = "invokevirtual SecurePrefsRoot.node(java.lang.String) : SecurePrefs [14]";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "SecurePrefsRoot.class", "SecurePrefsRoot", expectedOutput);
}
public void testBug540123d() throws Exception {
	this.runConformTest(
			new String[] {
					"SecurePrefs.java",
					"public class SecurePrefs {\n" + 
					"  public SecurePrefs node (String s) {\n" + 
					"	  System.out.println(s);\n" + 
					"	  return null;\n" + 
					"  }\n" + 
					"}",
					"SecurePrefsRoot.java",
					"public class SecurePrefsRoot extends SecurePrefs {\n" + 
					"\n" + 
					"	public void foo() {\n" + 
					"		SecurePrefs node = node(\"Hello\");\n" + 
					"		if (node != null)\n" + 
					"			System.out.println(node.toString());\n" + 
					"	}\n" + 
					"	\n" + 
					"	public static void main(String[] args) {\n" + 
					"		new SecurePrefsRoot().foo();\n" + 
					"	}\n" + 
					"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "SecurePrefsRoot.java\""
			+" \"" + OUTPUT_DIR +  File.separator + "SecurePrefs.java\""
					+ " -1.4",
					"",
					"",
					true);
	String expectedOutput = "invokevirtual SecurePrefsRoot.node(java.lang.String) : SecurePrefs [14]";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "SecurePrefsRoot.class", "SecurePrefsRoot", expectedOutput);
}
public void testBug540123e() throws Exception {
	this.runConformTest(
			new String[] {
					"SecurePrefs.java",
					"public class SecurePrefs {\n" + 
					"  public SecurePrefs node (String s) {\n" + 
					"	  System.out.println(s);\n" + 
					"	  return null;\n" + 
					"  }\n" + 
					"}",
					"SecurePrefsRoot.java",
					"public class SecurePrefsRoot extends SecurePrefs {\n" + 
					"\n" + 
					"	public void foo() {\n" + 
					"		SecurePrefs node = node(\"Hello\");\n" + 
					"		if (node != null)\n" + 
					"			System.out.println(node.toString());\n" + 
					"	}\n" + 
					"	\n" + 
					"	public static void main(String[] args) {\n" + 
					"		new SecurePrefsRoot().foo();\n" + 
					"	}\n" + 
					"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "SecurePrefsRoot.java\""
			+" \"" + OUTPUT_DIR +  File.separator + "SecurePrefs.java\""
					+ " -1.3",
					"",
					"",
					true);
	String expectedOutput = "invokevirtual SecurePrefs.node(java.lang.String) : SecurePrefs [14]";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "SecurePrefsRoot.class", "SecurePrefsRoot", expectedOutput);
}
}
