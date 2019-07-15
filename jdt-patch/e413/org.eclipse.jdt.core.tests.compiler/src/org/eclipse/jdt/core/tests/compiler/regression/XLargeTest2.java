/*******************************************************************************
 * Copyright (c) 2018 Andrey Loskutov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

public class XLargeTest2 extends AbstractRegressionTest {

	public XLargeTest2(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}

	public static Class<?> testClass() {
		return XLargeTest2.class;
	}

	/**
	 * Check if we hit the 64Kb limit on generated table switch method code in
	 * class files. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=542084
	 */
	public void testBug542084_error() {

		int enumsCount = getEnumsCountForError();
		StringBuilder lotOfEnums = new StringBuilder(enumsCount * 7);
		for (int i = 0; i < enumsCount; i++) {
			lotOfEnums.append("A").append(i).append(", ");
		}

		String expectedCompilerLog;
		if (this.complianceLevel > ClassFileConstants.JDK1_8) {
			expectedCompilerLog =
					"1. ERROR in X.java (at line 2)\n" +
					"	enum Y {\n" +
					"	     ^\n" +
					"The code for the static initializer is exceeding the 65535 bytes limit\n";
		} else {
			expectedCompilerLog =
					"1. ERROR in X.java (at line 6)\n" +
					"	switch(y){\n" +
					"        case A0:\n" +
					"            System.out.println(\"a\");\n" +
					"            break;\n" +
					"        default:\n" +
					"            System.out.println(\"default\");\n" +
					"            break;\n" +
					"        }\n" +
					"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"The code for the switch table on enum X.Y is exceeding the 65535 bytes limit\n";
		}
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    enum Y {\n" +
						 lotOfEnums.toString() +
					"    }\n" +
					"    public static void main(String[] args) {\n" +
					"        X.Y y = X.Y.A0;\n" +
					"        switch(y){\n" + // Reported error should be here
					"        case A0:\n" +
					"            System.out.println(\"a\");\n" +
					"            break;\n" +
					"        default:\n" +
					"            System.out.println(\"default\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"    public void z2(Y y) {\n" +  // Should not report error on second switch
					"        switch(y){\n" +
					"        case A0:\n" +
					"            System.out.println(\"a\");\n" +
					"            break;\n" +
					"        default:\n" +
					"            System.out.println(\"default\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"}"
				},
				"----------\n" +
				expectedCompilerLog +
				"----------\n");
	}

	/**
	 * Check if we don't hit the 64Kb limit on generated table switch method code in
	 * class files. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=542084
	 */
	public void testBug542084_no_error() {
		int enumsCount = getEnumsCountForSuccess();
		StringBuilder lotOfEnums = new StringBuilder(enumsCount * 7);
		for (int i = 0; i < enumsCount; i++) {
			lotOfEnums.append("A").append(i).append(", ");
		}

		// Javac can't compile such big enums
		runConformTest(
				true,
				JavacTestOptions.SKIP,
				new String[] {
						"X.java",
						"public class X {\n" +
								"    enum Y {\n" +
								lotOfEnums.toString() +
								"    }\n" +
								"    public static void main(String[] args) {\n" +
								"        X.Y y = X.Y.A0;\n" +
								"        switch(y){\n" +
								"        case A0:\n" +
								"            System.out.println(\"SUCCESS\");\n" +
								"            break;\n" +
								"        default:\n" +
								"            System.out.println(\"default\");\n" +
								"            break;\n" +
								"        }\n" +
								"    }\n" +
								"}"
				},
				"SUCCESS");
	}

	/**
	 * @return Generated code for enums that exceeds the limit
	 */
	private int getEnumsCountForError() {
		if(this.complianceLevel > ClassFileConstants.JDK1_8) {
			return 2800;
		}
		return 4500;
	}

	/**
	 * @return Generated code for enums that does not exceeds the limit
	 */
	private int getEnumsCountForSuccess() {
		if(this.complianceLevel > ClassFileConstants.JDK1_8) {
			return 2300;
		}
		return 4300;
	}


}
