/*******************************************************************************
 * Copyright (c) 2018, 2020 IBM Corporation.
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
import java.io.IOException;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "rawtypes" })
public class BatchCompilerTest2 extends AbstractBatchCompilerTest {

	static {
//		TESTS_NAMES = new String[] { "testIssue147" };
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
					+ " --enable-preview -" + CompilerOptions.getLatestVersion() + " ",
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
					+ " --enable-preview -" + CompilerOptions.getLatestVersion() + " ",
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
					+ " --enable-preview -" + CompilerOptions.getLatestVersion() + " ",
					"",
					"",
					true);
	String expectedOutput = ".65535, super bit)";
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
					+ " --enable-preview -" + CompilerOptions.getLatestVersion() + " ",
					"",
					"",
					true);
	String expectedOutput = "65535, super bit)";
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
					+ " -source 1.8 -target 1.8",
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
					+ " -source 1.8",
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
					+ " -target 1.8",
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
					+ " -1.8",
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
					+ " -1.8",
					"",
					"",
					true);
	String expectedOutput = "invokevirtual SecurePrefsRoot.node(java.lang.String) : SecurePrefs [14]";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "SecurePrefsRoot.class", "SecurePrefsRoot", expectedOutput);
}
public void testBug562473() {
	this.runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"	}\n" +
					"}"
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\"" +
					" -source " + CompilerOptions.getLatestVersion() +
					" -target " + CompilerOptions.getLatestVersion() + " ",
					"",
					"",
					true);
}
public void testBug568802() {
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	String libPath = currentWorkingDirectoryPath + File.separator + "lib568802.jar";
	try {
	Util.createJar(
		new String[] {
			"hello/World.java;\n",
			"package hello;\n"
					+    "public class World {}\n",
			"module-info.java;\n",
			"module HelloModule {}\n"
		},
		libPath,
		JavaCore.VERSION_11,
		false);
	this.runConformTest(
			new String[] {
					"X.java",
					"import hello.World;\n"
					+ "public class X {\n"
					+ "	 World field = new World();\n"
					+ "}\n"
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\"" +
					" -cp " + libPath + // relative
					" -source " + CompilerOptions.getLatestVersion() +
					" -target " + CompilerOptions.getLatestVersion() + " ",
					"",
					"",
					true);
	} catch (IOException e) {
		System.err.println("BatchCompilerTest2#testBug568802 could not write to current working directory " + currentWorkingDirectoryPath);
	} finally {
		new File(libPath).delete();
	}
}
public void testIssue114() {
	this.runNegativeTest(
		new String[] {
				"Foo.java",
				"import com.sun.imageio.plugins.png.PNGImageReader;\n" +
				"import com.sun.imageio.plugins.png.PNGImageReaderSpi;\n" +
				"\n" +
				"public class Foo {\n" +
				"        PNGImageReader r;\n" +
				"}\n"
			},
			"\"" + OUTPUT_DIR +  File.separator + "Foo.java\"" +
					" --release 9" +
					" --add-exports java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED",
			"",
			"Exporting a package from system module 'java.desktop' is not allowed with --release\n",
			true
		);
}

public void testIssue147() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.invoke.MethodHandle;\n" +
			"import java.lang.invoke.MethodHandles;\n" +
			"import java.lang.reflect.Method;\n" +
			"\n" +
			"\n" +
			"public class X {\n" +
			"	public final Object invoke(Object self) throws Throwable {\n" +
			"\n" +
			"			Method method = null;\n" +
			"			try {\n" +
			"				MethodHandle methodHandle = MethodHandles.lookup().unreflect(method );\n" +
			"				return methodHandle.invoke(self);\n" +
			"			} catch (IllegalArgumentException e) {\n" +
			"				throw e;\n" +
			"			} \n" +
			"		}\n" +
			"}\n"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\"" +
				" --release " + CompilerOptions.VERSION_9 + " ",
				"",
				"",
				true);
	String expectedOutput = "java.lang.invoke.MethodHandle.invoke(java.lang.Object)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
}
