/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class PatternMatchingSelectionTest extends AbstractSelectionTest {
static {
//		TESTS_NUMBERS = new int[] { 1 };
//		TESTS_NAMES = new String[] { "test005" };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(PatternMatchingSelectionTest.class, F_16);
}

public PatternMatchingSelectionTest(String testName) {
	super(testName);
}
public void test001() throws JavaModelException {
	String string =  "public class X {\n"
			+ "    protected Object x_ = \"FIELD X\";\n"
			+ "    @SuppressWarnings(\"preview\")\n"
			+ "	   public void f(Object obj, boolean b) {\n"
			+ "        if ((x_ instanceof String y) && y.length() > 0) {\n"
			+ "            System.out.println(y.toLowerCase());\n"
			+ "        }\n"
			+ "    }\n"
			+ "}";

	String selection = "x_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "x_";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  protected Object x_;\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public @SuppressWarnings(\"preview\") void f(Object obj, boolean b) {\n" +
					"    <SelectOnName:x_>;\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "x_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test002() throws JavaModelException {
	String string =  "public class X {\n"
			+ "    protected Object x_ = \"FIELD X\";\n"
			+ "    @SuppressWarnings(\"preview\")\n"
			+ "	   public void f(Object obj, boolean b) {\n"
			+ "        if ((x_ instanceof String y_) && y_.length() > 0) {\n"
			+ "            System.out.println(y_.toLowerCase());\n"
			+ "        }\n"
			+ "    }\n"
			+ "}";

	String selection = "y_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "y_";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  protected Object x_;\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public @SuppressWarnings(\"preview\") void f(Object obj, boolean b) {\n" +
					"    String y_;\n" +
					"    {\n" +
					"      <SelectOnName:y_>;\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "y_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test003() throws JavaModelException {
	String string =  "public class X {\n"
			+ "    protected Object x_ = \"FIELD X\";\n"
			+ "    @SuppressWarnings(\"preview\")\n"
			+ "	   public void f(Object obj, boolean b) {\n"
			+ "        b = (x_ instanceof String y_) && (y_.length() > 0);\n"
			+ "    }\n"
			+ "}";

	String selection = "y_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "y_";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  protected Object x_;\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public @SuppressWarnings(\"preview\") void f(Object obj, boolean b) {\n" +
					"    String y_;\n" +
					"    <SelectOnName:y_>;\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "y_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test004() throws JavaModelException {
	String string =  "public class X {\n"
			+ "    @SuppressWarnings(\"preview\")\n"
			+ "	   public void f(Object obj, boolean b) {\n"
			+ "        b = (x_ instanceof String y_) && (y_.length() > 0);\n"
			+ "    }\n"
			+ "}";

	String selection = "y_";
	String selectKey = "<SelectionOnLocalName:String ";
	String expectedSelection = selectKey + selection + ">;";

	String selectionIdentifier = "y_";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public @SuppressWarnings(\"preview\") void f(Object obj, boolean b) {\n" +
					"    <SelectionOnLocalName:String y_>;\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "y_";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = string.indexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
}
