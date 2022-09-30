/*******************************************************************************
 * Copyright (c) 2020 Thomas Wolf <thomas.wolf@paranor.ch> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class SuppressWarningsTest extends AbstractBatchCompilerTest {

	public SuppressWarningsTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}

	public static Class testClass() {
		return SuppressWarningsTest.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
		return options;
	}

	public void testSimpleSuppressWarnings() {
		this.runTest(true,
			new String[] {
				"p/SuppressTest.java",
				"package p;\n" +
				"public class SuppressTest {\n" +
				"@SuppressWarnings(\"boxing\")\n" +
				"public Long get(long l) {\n" +
				"  Long result = l * 2;\n" +
				"  return result;\n" +
				"}\n}\n"
			},
			"\"" + OUTPUT_DIR +  File.separator + "p/SuppressTest.java\""
			+ " -warn:+unused -warn:+boxing "
			+ " -1.5 -g -preserveAllLocals"
			+ " -d \"" + OUTPUT_DIR + "\" ",
			"", "", true, null);
	}

	public void testNestedSuppressWarnings() {
		this.runTest(true,
			new String[] {
				"p/SuppressTest.java",
				"package p;\n" +
				"@SuppressWarnings(\"unused\")\n" +
				"public class SuppressTest {\n" +
				"private String unused=\"testUnused\";\n" +
				"@SuppressWarnings(\"boxing\")\n" +
				"public Long get(long l) {\n" +
				"  Long result = l * 2;\n" +
				"  return result;\n" +
				"}\n}\n"
			},
			"\"" + OUTPUT_DIR +  File.separator + "p/SuppressTest.java\""
			+ " -warn:+unused -warn:+boxing "
			+ " -1.5 -g -preserveAllLocals"
			+ " -d \"" + OUTPUT_DIR + "\" ",
			"", "", true, null);
	}

	public void testUnrelatedSuppressWarnings() {
		this.runTest(true,
			new String[] {
				"p/SuppressTest.java",
				"package p;\n" +
				"@SuppressWarnings(\"unused\")\n" +
				"public class SuppressTest {\n" +
				"private String unused=\"testUnused\";\n" +
				"public Long get(long l) {\n" +
				"  Long result = l * 2;\n" +
				"  return result;\n" +
				"}\n}\n"
			},
			"\"" + OUTPUT_DIR +  File.separator + "p/SuppressTest.java\""
			+ " -warn:+unused -warn:+boxing "
			+ " -1.5 -g -preserveAllLocals"
			+ " -d \"" + OUTPUT_DIR + "\" ",
			"",
			"----------\n" +
			"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/SuppressTest.java (at line 6)\n" +
			"	Long result = l * 2;\n" +
			"	              ^^^^^\n" +
			"The expression of type long is boxed into Long\n" +
			"----------\n" +
			"1 problem (1 warning)\n",
			true, null);
	}

}
