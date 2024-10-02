/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
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
 *     Jesper Steen Møller <jesper@selskabet.org> - contributions for:
 *         Bug 531046: [10] ICodeAssist#codeSelect support for 'var'
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class SelectionParserTest10 extends AbstractSelectionTest {
	static {
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_NAMES = new String[] { "test001" };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(SelectionParserTest10.class, F_10);
	}

	public SelectionParserTest10(String testName) {
		super(testName);
	}

	public void test001() throws JavaModelException {
		String string =   "public class X {\n"
						+ "  public static void main(String[] args) {\n"
						+ "    var s_s = args[0];\n"
						+ "  }\n"
						+ "}\n";

		String selection = "s_s";
		String expectedSelection = "<SelectionOnLocalName:var s_s = args[0]>;";

		String completionIdentifier = "s_s";
		String expectedUnitDisplayString = "public class X {\n" +
											"  public X() {\n" +
											"  }\n" +
											"  public static void main(String[] args) {\n" +
											"    <SelectionOnLocalName:var s_s = args[0]>;\n" +
											"  }\n" +
											"}\n";
		String expectedReplacedSource = "s_s";
		String testName = "X.java";

		int selectionStart = string.lastIndexOf(selection);
		int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

		checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
				completionIdentifier, expectedReplacedSource, testName);
	}

	public void test002() throws JavaModelException {
		String string =   "public class X {\n"
						+ "  public static void main(String[] args) {\n"
						+ "    var s_s = args[0];\n"
						+ "  }\n"
						+ "}\n";

		String selection = "var";
		String expectedSelection = "<SelectOnType:var>";

		String completionIdentifier = "var";
		String expectedUnitDisplayString = "public class X {\n" +
											"  public X() {\n" +
											"  }\n" +
											"  public static void main(String[] args) {\n" +
										    "    <SelectOnType:var> s_s = args[0];\n" +
											"  }\n" +
											"}\n";
		String expectedReplacedSource = "var";
		String testName = "X.java";

		int selectionStart = string.lastIndexOf(selection);
		int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

		checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
				completionIdentifier, expectedReplacedSource, testName);
	}
}
