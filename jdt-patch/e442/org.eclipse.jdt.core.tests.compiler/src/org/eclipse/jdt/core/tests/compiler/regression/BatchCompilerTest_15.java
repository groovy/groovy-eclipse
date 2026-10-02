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
public class BatchCompilerTest_15 extends AbstractBatchCompilerTest {

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
		return buildMinimalComplianceTestSuite(testClass(), F_15);
	}
	public static Class testClass() {
		return BatchCompilerTest_15.class;
	}
	public BatchCompilerTest_15(String name) {
		super(name);
	}
	public void testBug564047_001(){
		if (!AbstractBatchCompilerTest.isJREVersionEqualTo(CompilerOptions.VERSION_15))
			return; // preview test - relevant only at level 15

		String currentWorkingDirectoryPath = System.getProperty("user.dir");
		if (currentWorkingDirectoryPath == null) {
			System.err.println("BatchCompilerTest#testBug564047_001 could not access the current working directory " + currentWorkingDirectoryPath);
		} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
			System.err.println("BatchCompilerTest#testBug564047_001 current working directory is not a directory " + currentWorkingDirectoryPath);
		} else {
			String lib1Path = currentWorkingDirectoryPath + File.separator + "lib1.jar";
			try {
			Util.createJar(
					new String[] {
						"p/Y.java",
						"package p;\n" +
						"public sealed class Y permits Z{}",
						"p/Z.java",
						"package p;\n" +
						"public final class Z extends Y{}",
					},
					lib1Path,
					JavaCore.VERSION_15,
					true);
			this.runNegativeTest(
					new String[] {
						"src/p/X.java",
						"package p;\n" +
						"public class X extends Y {\n" +
						"  public static void main(String[] args){\n" +
						"     System.out.println(0);\n" +
						"  }\n" +
						"}",
					},
			        "\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
					+ " -cp " + lib1Path  // relative
					+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			        + " --release 15 --enable-preview -g -preserveAllLocals"
			        + " -proceedOnError -referenceInfo"
			        + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			        "",
			        "----------\n" +
			        "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 2)\n" +
			        "	public class X extends Y {\n" +
			        "	                       ^\n" +
			        "The class X with a sealed direct supertype Y should be declared either final, sealed, or non-sealed\n" +
			        "----------\n" +
			        "2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 2)\n" +
			        "	public class X extends Y {\n" +
			        "	                       ^\n" +
			        "The class X cannot extend the class Y as it is not a permitted subtype of Y\n" +
			        "----------\n" +
			        "2 problems (2 errors)\n",
			        true);
			} catch (IOException e) {
				System.err.println("BatchCompilerTest#testBug563430_001 could not write to current working directory " + currentWorkingDirectoryPath);
			} finally {
				new File(lib1Path).delete();
			}
		}
	}
}
