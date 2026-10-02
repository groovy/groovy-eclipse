/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
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
import java.io.IOException;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.Util;

public class BatchCompilerTest_22 extends AbstractBatchCompilerTest {

	/**
	 * This test suite only needs to be run on one compliance.
	 *
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_22);
	}

	public static Class<BatchCompilerTest_22> testClass() {
		return BatchCompilerTest_22.class;
	}

	public BatchCompilerTest_22(String name) {
		super(name);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/pull/3055
	// Add warnings for structurally required but otherwise unused local variables
	public void testIssue3055() throws IOException {
		createOutputTestDirectory("regression/.settings");
		Util.createFile(OUTPUT_DIR+"/.settings/org.eclipse.jdt.core.prefs",
				"eclipse.preferences.version=1\n" +
				"org.eclipse.jdt.core.compiler.problem.unusedLambdaParameter=error\n");
		this.runTest(
			false,
			new String[] {
				"bugs/warning/ShowBug.java",
				"""
				package bugs.warning;
				import java.util.Map;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;

				public class ShowBug {
					static void foo() {

						Stream<String> stream = null;

						@SuppressWarnings("unused")
						Map<String, String> m = stream.collect(Collectors.toMap(String::toUpperCase, xyz -> "NODATA")); //$NON-NLS-1$

					}
				}
				"""
			},
			"\"" + OUTPUT_DIR +  File.separator + "bugs" + File.separator + "warning" + File.separator + "ShowBug.java\""
			+ " -source 22"
			+ " -properties " + OUTPUT_DIR + File.separator +".settings" + File.separator + "org.eclipse.jdt.core.prefs "
			+ " -d \"" + OUTPUT_DIR + "\"",
			"",
			"----------\n" +
			"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/bugs/warning/ShowBug.java (at line 12)\n" +
			"	Map<String, String> m = stream.collect(Collectors.toMap(String::toUpperCase, xyz -> \"NODATA\")); //$NON-NLS-1$\n" +
			"	                        ^^^^^^\n" +
			"Null pointer access: The variable stream can only be null at this location\n" +
			"----------\n" +
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/bugs/warning/ShowBug.java (at line 12)\n" +
			"	Map<String, String> m = stream.collect(Collectors.toMap(String::toUpperCase, xyz -> \"NODATA\")); //$NON-NLS-1$\n" +
			"	                                                                             ^^^\n" +
			"The value of the lambda parameter xyz is not used\n" +
			"----------\n" +
			"2 problems (1 error, 1 warning)\n",
			false /*don't flush output dir*/,
			null /* progress */);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/pull/3055
	// Add warnings for structurally required but otherwise unused local variables
	public void testIssue3055_2() throws IOException {
		createOutputTestDirectory("regression/.settings");
		Util.createFile(OUTPUT_DIR+"/.settings/org.eclipse.jdt.core.prefs",
				"eclipse.preferences.version=1\n" +
				"org.eclipse.jdt.core.compiler.problem.unusedLambdaParameter=ignore\n");
		this.runTest(
			true,
			new String[] {
				"bugs/warning/ShowBug.java",
				"""
				package bugs.warning;
				import java.util.Map;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;

				public class ShowBug {
					static void foo() {

						Stream<String> stream = null;

						@SuppressWarnings("unused")
						Map<String, String> m = stream.collect(Collectors.toMap(String::toUpperCase, xyz -> "NODATA")); //$NON-NLS-1$

					}
				}
				"""
			},
			"\"" + OUTPUT_DIR +  File.separator + "bugs" + File.separator + "warning" + File.separator + "ShowBug.java\""
			+ " -source 22"
			+ " -properties " + OUTPUT_DIR + File.separator +".settings" + File.separator + "org.eclipse.jdt.core.prefs "
			+ " -d \"" + OUTPUT_DIR + "\"",
			"",
			"----------\n" +
			"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/bugs/warning/ShowBug.java (at line 12)\n" +
			"	Map<String, String> m = stream.collect(Collectors.toMap(String::toUpperCase, xyz -> \"NODATA\")); //$NON-NLS-1$\n" +
			"	                        ^^^^^^\n" +
			"Null pointer access: The variable stream can only be null at this location\n" +
			"----------\n" +
			"1 problem (1 warning)\n",
			false /*don't flush output dir*/,
			null /* progress */);
	}
}