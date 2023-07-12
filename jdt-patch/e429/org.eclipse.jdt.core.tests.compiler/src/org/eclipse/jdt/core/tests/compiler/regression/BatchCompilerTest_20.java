/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
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

import java.io.File;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class BatchCompilerTest_20 extends AbstractBatchCompilerTest {

	static {
//			TESTS_NAMES = new String[] { "testIssue558_1" };
	//		TESTS_NUMBERS = new int[] { 306 };
	//		TESTS_RANGE = new int[] { 298, -1 };
	}
	/**
	 * This test suite only needs to be run on one compliance.
	 *
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_20);
	}

	public static Class<BatchCompilerTest_20> testClass() {
		return BatchCompilerTest_20.class;
	}

	public BatchCompilerTest_20(String name) {
		super(name);
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
			+ " --enable-preview -source 20 -warn:none"
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
		JavaCore.VERSION_20);
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
			+ " --enable-preview -source 20 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
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
			JavaCore.VERSION_20);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "OKEND", this.verifier.getExecutionOutput());
	}
}
