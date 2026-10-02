/*******************************************************************************
 * Copyright (c) 2012, 2014 IBM Corporation GK Software AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.ast.FakedTrackingVariable;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ConcurrentBatchCompilerTest extends BatchCompilerTest {

	public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(), CompilerOptions.getFirstSupportedJdkLevel());
	}
	public static Class testClass() {
		return ConcurrentBatchCompilerTest.class;
	}
	public ConcurrentBatchCompilerTest(String name) {
		super(name);
	}

	Thread runner1;
	Thread runner2;

	static int COUNT = 100;

	/* Invoke the compiler COUNT times to increase bug probabililty. */
	@Override
	protected boolean invokeCompiler(PrintWriter out, PrintWriter err, Object extraArguments, TestCompilationProgress compilationProgress) {
		boolean success = true;
		for (int j=0; j<COUNT; j++) {
			success &= super.invokeCompiler(out, err, extraArguments, compilationProgress);
		}
		return success;
	}

	/* Disambiguate file names for concurrent tests in the same directory. */
	@Override
	protected String testName() {
		Thread current = Thread.currentThread();
		String baseName = super.testName();
		if (current == this.runner1)
			return baseName+"-Thread1";
		if (current == this.runner2)
			return baseName+"-Thread2";
		return baseName;
	}

	public void testBug372319() throws Throwable {
		try {
			FakedTrackingVariable.TEST_372319 = true;

			// expected error output for runner2 times COUNT:
			final StringBuilder errorOutput = new StringBuilder();
			for (int j=0; j<COUNT; j++)
				errorOutput.append("----------\n" +
						"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/test01/X.java (at line 12)\n" +
						"	FileReader reader = getReader(\"somefile\");\n" +
						"	           ^^^^^^\n" +
						"Potential resource leak: \'reader\' may not be closed\n" +
						"----------\n" +
						"1 problem (1 error)\n");

			// collect exceptions indicating a failure:
			final Throwable[] thrown = new Throwable[2];

			final String firstSupportedVersion = CompilerOptions.getFirstSupportedJavaVersion();
			this.runner1 = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							runConformTest(new String[] {
								"org/eclipse/jdt/internal/launching/CompositeId.java",
								"/*******************************************************************************\n" +
								" * Copyright (c) 2000, 2014 IBM Corporation and others.\n" +
								" * All rights reserved. This program and the accompanying materials\n" +
								" * are made available under the terms of the Eclipse Public License v1.0\n" +
								" * which accompanies this distribution, and is available at\n" +
								" * http://www.eclipse.org/legal/epl-v10.html\n" +
								" * \n" +
								" * Contributors:\n" +
								" *     IBM Corporation - initial API and implementation\n" +
								" *******************************************************************************/\n" +
								"package org.eclipse.jdt.internal.launching;\n" +
								"\n" +
								"import java.util.ArrayList;\n" +
								"\n" +
								"/**\n" +
								" * Utility class for id's made of multiple Strings\n" +
								" */\n" +
								"public class CompositeId {\n" +
								"	private String[] fParts;\n" +
								"	\n" +
								"	public CompositeId(String[] parts) {\n" +
								"		fParts= parts;\n" +
								"	}\n" +
								"	\n" +
								"	public static CompositeId fromString(String idString) {\n" +
								"		ArrayList<String> parts= new ArrayList<String>();\n" +
								"		int commaIndex= idString.indexOf(',');\n" +
								"		while (commaIndex > 0) {\n" +
								"			int length= Integer.valueOf(idString.substring(0, commaIndex)).intValue();\n" +
								"			String part= idString.substring(commaIndex+1, commaIndex+1+length);\n" +
								"			parts.add(part);\n" +
								"			idString= idString.substring(commaIndex+1+length);\n" +
								"			commaIndex= idString.indexOf(',');\n" +
								"		}\n" +
								"		String[] result= parts.toArray(new String[parts.size()]);\n" +
								"		return new CompositeId(result);\n" +
								"	}\n" +
								"	\n" +
								"	@Override\n" +
								"	public String toString() {\n" +
								"		StringBuffer buf= new StringBuffer();\n" +
								"		for (int i= 0; i < fParts.length; i++) {\n" +
								"			buf.append(fParts[i].length());\n" +
								"			buf.append(',');\n" +
								"			buf.append(fParts[i]);\n" +
								"		}\n" +
								"		return buf.toString();\n" +
								"	}\n" +
								"	\n" +
								"	public String get(int index) {\n" +
								"		return fParts[index];\n" +
								"	}\n" +
								"	\n" +
								"	public int getPartCount() {\n" +
								"		return fParts.length;\n" +
								"	}\n" +
								"}\n" +
								""
							},
					        "\"" + OUTPUT_DIR +  File.separator + "org/eclipse/jdt/internal/launching/CompositeId.java\""
				            + " -" + firstSupportedVersion + " -g -preserveAllLocals"
				            + " -proceedOnError -d \"" + OUTPUT_DIR + "\"",
							"",
							"",
							false);
						} catch (Throwable t) {
							thrown[0] = t;
						}
					}
			});
			this.runner2 = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// from ResourceLeakTests.test056e():
						Map options = getCompilerOptions();
						options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
						options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
						runNegativeTest(
							new String[] {
								"test01/X.java",
								"package test01;\n" +
								"import java.io.File;\n" +
								"import java.io.FileReader;\n" +
								"import java.io.IOException;\n" +
								"public class X {\n" +
								"    FileReader getReader(String filename) throws IOException {\n" +
								"        File file = new File(\"somefile\");\n" +
								"        FileReader fileReader = new FileReader(file);\n" +
								"        return fileReader;\n" + 		// don't complain here, pass responsibility to caller
								"    }\n" +
								"    void foo() throws IOException {\n" +
								"        FileReader reader = getReader(\"somefile\");\n" +
								"        char[] in = new char[50];\n" +
								"        reader.read(in);\n" +
								"    }\n" +
								"    public static void main(String[] args) throws IOException {\n" +
								"        new X().foo();\n" +
								"    }\n" +
								"}\n"
							},
					        "\"" + OUTPUT_DIR +  File.separator + "test01/X.java\""
				            + " -" + firstSupportedVersion + " -g -preserveAllLocals -err:+resource"
				            + " -proceedOnError -d \"" + OUTPUT_DIR + "\"",
				            "",
							errorOutput.toString(),
							false);
					} catch (Throwable t) {
						thrown[1] = t;
					}
				}
			});

			this.runner2.start();
			this.runner1.start();
			this.runner1.join();
			this.runner2.join();
			if (thrown[0] != null) throw thrown[0];
			if (thrown[1] != null) throw thrown[1];
		} finally {
			FakedTrackingVariable.TEST_372319 = false;
		}
	}
}
