/*******************************************************************************
* Copyright (c) 2023 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;

public class BatchCompilerTest_21 extends AbstractBatchCompilerTest {

	/**
	 * This test suite only needs to be run on one compliance.
	 *
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}

	public static Class<BatchCompilerTest_21> testClass() {
		return BatchCompilerTest_21.class;
	}

	public BatchCompilerTest_21(String name) {
		super(name);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1774
	// [switch] Code generated for statement switch doesn't handle MatchException
	public void testGHI1774_Expression() throws Exception {

		String path = LIB_DIR;
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		Util.createJar(new String[] {
			"p/Color.java",
			"package p;\n" +
			"public enum Color {\n" +
			"	R, Y;\n" +
			"	public static Color getColor() {\n" +
			"		return R;\n" +
			"	}\n" +
			"}",
		},
		libPath,
		JavaCore.VERSION_20);
		this.runConformTest(
			new String[] {
				"src/p/X.java",
				"package p;\n"
				+ "import p.Color;\n"
				+ "public class X {\n"
				+ "	public static void main(String argv[]) {\n"
				+ "		Color c = Color.getColor();\n"
				+ "		try {\n"
				+ "			int a = switch (c) {\n"
				+ "             case null -> 0;\n"
				+ "				case R -> 1;\n"
				+ "				case Y -> 2;\n"
				+ "			};\n"
				+ "		} catch (MatchException e) {\n"
				+ "			System.out.print(\"OK\");\n"
				+ "		} catch (Exception e) {\n"
				+ "			System.out.print(\"NOT OK: \" + e);\n"
				+ "		}\n"
				+ "			System.out.print(\"END\");\n"
				+ "	}\n"
				+ "}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " -source 21 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "END", this.verifier.getExecutionOutput());
		Util.createJar(new String[] {
				"p/Color.java",
				"package p;\n" +
				"public enum Color {\n" +
				"	R, Y, B;\n" +
				"	public static Color getColor() {\n" +
				"		return B;\n" +
				"	}\n" +
				"}",
			},
			libPath,
			JavaCore.VERSION_20);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "OKEND", this.verifier.getExecutionOutput());
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1774
	// [switch] Code generated for statement switch doesn't handle MatchException
	public void testGHI1774_Statement() throws Exception {

		String path = LIB_DIR;
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		Util.createJar(new String[] {
			"p/Color.java",
			"package p;\n" +
			"public enum Color {\n" +
			"	R, Y;\n" +
			"	public static Color getColor() {\n" +
			"		return R;\n" +
			"	}\n" +
			"}",
		},
		libPath,
		JavaCore.VERSION_20);
		this.runConformTest(
			new String[] {
				"src/p/X.java",
				"package p;\n"
				+ "import p.Color;\n"
				+ "public class X {\n"
				+ "	public static void main(String argv[]) {\n"
				+ "		Color c = Color.getColor();\n"
				+ "		try {\n"
				+ "			switch (c) {\n"
				+ "             case null -> System.out.println(\"Null\");\n"
				+ "				case R -> System.out.print(\"R\");\n"
				+ "				case Y -> System.out.println(\"Y\");\n"
				+ "			};\n"
				+ "		} catch (MatchException e) {\n"
				+ "			System.out.print(\"OK!\");\n"
				+ "		} catch (Exception e) {\n"
				+ "			System.out.print(\"NOT OK: \" + e);\n"
				+ "		}\n"
				+ "			System.out.print(\"END\");\n"
				+ "	}\n"
				+ "}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " -source 21 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[0]);
		assertEquals("Incorrect output", "REND", this.verifier.getExecutionOutput());
		Util.createJar(new String[] {
				"p/Color.java",
				"package p;\n" +
				"public enum Color {\n" +
				"	R, Y, B;\n" +
				"	public static Color getColor() {\n" +
				"		return B;\n" +
				"	}\n" +
				"}",
			},
			libPath,
			JavaCore.VERSION_20);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[0]);
		assertEquals("Incorrect output", "OK!END", this.verifier.getExecutionOutput());
	}

	public void testIssue558_1() throws Exception {
		String path = LIB_DIR;
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		Util.createJar(new String[] {
			"p/Color.java",
			"package p;\n" +
			"public enum Color {\n" +
			"	R, Y;\n" +
			"	public static Color getColor() {\n" +
			"		return R;\n" +
			"	}\n" +
			"}",
		},
		libPath,
		JavaCore.VERSION_21);
		this.runConformTest(
			new String[] {
				"src/p/X.java",
				"package p;\n"
				+ "import p.Color;\n"
				+ "public class X {\n"
				+ "	public static void main(String argv[]) {\n"
				+ "		Color c = Color.getColor();\n"
				+ "		try {\n"
				+ "			int a = switch (c) {\n"
				+ "				case R -> 1;\n"
				+ "				case Y -> 2;\n"
				+ "			};\n"
				+ "		} catch (MatchException e) {\n"
				+ "			System.out.print(\"OK\");\n"
				+ "		} catch (Exception e) {\n"
				+ "			System.out.print(\"NOT OK: \" + e);\n"
				+ "		}\n"
				+ "			System.out.print(\"END\");\n"
				+ "	}\n"
				+ "}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " -source 21 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], null);
		assertEquals("Incorrect output", "END", this.verifier.getExecutionOutput());
		Util.createJar(new String[] {
				"p/Color.java",
				"package p;\n" +
				"public enum Color {\n" +
				"	R, Y, B;\n" +
				"	public static Color getColor() {\n" +
				"		return B;\n" +
				"	}\n" +
				"}",
			},
			libPath,
			JavaCore.VERSION_21);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], null);
		assertEquals("Incorrect output", "OKEND", this.verifier.getExecutionOutput());
	}

	public void testIssue558_2() throws Exception {
		String path = LIB_DIR;
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		Util.createJar(new String[] {
			"p/I.java",
			"package p;\n" +
			"public sealed interface I {\n" +
			"	public static I getImpl() {\n" +
			"		return new A();\n" +
			"	}\n" +
			"}\n" +
			"final class A implements I {}\n" +
			"final class B implements I {}",
		},
		libPath,
		JavaCore.VERSION_21);
		this.runConformTest(
			new String[] {
				"src/p/X.java",
				"package p;\n"
				+ "import p.I;\n"
				+ "public class X {\n"
				+ "	public static void main(String argv[]) {\n"
				+ "		I i = I.getImpl();\n"
				+ "		try {\n"
				+ "			int r = switch (i) {\n"
				+ "				case A a -> 1;\n"
				+ "				case B b -> 2;\n"
				+ "			};\n"
				+ "		} catch (MatchException e) {\n"
				+ "			System.out.print(\"OK\");\n"
				+ "		} catch (Exception e) {\n"
				+ "			System.out.print(\"NOT OK: \" + e);\n"
				+ "		}\n"
				+ "			System.out.print(\"END\");\n"
				+ "	}\n"
				+ "}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " -source 21 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], null);
		assertEquals("Incorrect output", "END", this.verifier.getExecutionOutput());
		Util.createJar(new String[] {
				"p/I.java",
				"package p;\n" +
				"public sealed interface I {\n" +
				"	public static I getImpl() {\n" +
				"		return new C();\n" +
				"	}\n" +
				"}\n" +
				"final class A implements I {}\n" +
				"final class B implements I {}\n" +
				"final class C implements I {}",
			},
			libPath,
			JavaCore.VERSION_21);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], null);
		assertEquals("Incorrect output", "OKEND", this.verifier.getExecutionOutput());
	}

}