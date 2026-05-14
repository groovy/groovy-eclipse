/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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

import junit.framework.Test;
import org.eclipse.jdt.core.JavaModelException;

public class SelectionParserTest9 extends AbstractSelectionTest {
static {
//		TESTS_NUMBERS = new int[] { 1 };
//		TESTS_NAMES = new String[] { "test510339_007" };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(SelectionParserTest9.class, F_9);
}

public SelectionParserTest9(String testName) {
	super(testName);
}

public void test510339_001_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  exports pack1;\n"
			+ "}\n";


	String selection = "pack1";
	String selectKey = "<SelectOnPackageVisibility:";
	String expectedCompletionNodeToString = selectKey + selection + ">";

	String completionIdentifier = "pack1";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  exports " + expectedCompletionNodeToString +";\n"
			+ "}\n";
	String expectedReplacedSource = "pack1";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_002_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  exports pack1 to second;\n"
			+ "}\n";


	String selection = "second";

	String expectedCompletionNodeToString = "<SelectOnModuleReference:" + selection + ">";

	String completionIdentifier = "second";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  exports pack1 to "+ expectedCompletionNodeToString + ";\n"
			+ "}\n";
	String expectedReplacedSource = "second";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_003_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  opens pack1;\n"
			+ "}\n";


	String selection = "pack1";
	String selectKey = "<SelectOnPackageVisibility:";
	String expectedCompletionNodeToString = selectKey + selection + ">";

	String completionIdentifier = "pack1";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  opens " + expectedCompletionNodeToString +";\n"
			+ "}\n";
	String expectedReplacedSource = "pack1";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_004_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  opens pack1 to second;\n"
			+ "}\n";


	String selection = "second";

	String expectedCompletionNodeToString = "<SelectOnModuleReference:" + selection + ">";

	String completionIdentifier = "second";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  opens pack1 to "+ expectedCompletionNodeToString + ";\n"
			+ "}\n";
	String expectedReplacedSource = "second";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_005_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  requires second;\n"
			+ "}\n";


	String selection = "second";

	String expectedCompletionNodeToString = "<SelectOnModuleReference:" + selection + ">";

	String completionIdentifier = "second";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  requires "+ expectedCompletionNodeToString + ";\n"
			+ "}\n";
	String expectedReplacedSource = "second";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_006_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  uses Z;\n"
			+ "}\n";


	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:" + selection + ">";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  uses "+ expectedCompletionNodeToString + ";\n"
			+ "}\n";
	String expectedReplacedSource = "Z";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_007_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  uses pack1.Z;\n"
			+ "}\n";


	String selection = "Z";
	String expectedCompletionNodeToString = "<SelectOnType:pack1.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  uses <SelectOnType:pack1.Z>" + ";\n"
			+ "}\n";
	String expectedReplacedSource = "pack1.Z";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_008_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  provides Y with Z;\n"
			+ "}\n";


	String selection = "Y";

	String expectedCompletionNodeToString = "<SelectOnType:" + selection + ">";

	String completionIdentifier = "Y";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  provides "+ expectedCompletionNodeToString + " with Z;\n"
			+ "}\n";
	String expectedReplacedSource = "Y";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_009_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  provides pack1.Y with Z;\n"
			+ "}\n";


	String selection = "Y";

	String expectedCompletionNodeToString = "<SelectOnType:pack1.Y>";

	String completionIdentifier = "Y";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  provides <SelectOnType:pack1.Y> with Z;\n"
			+ "}\n";
	String expectedReplacedSource = "pack1.Y";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_010_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  provides Y with Z;\n"
			+ "}\n";


	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:" + selection + ">";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  provides Y with "+ expectedCompletionNodeToString + ";\n"
			+ "}\n";
	String expectedReplacedSource = "Z";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test510339_011_since_9() throws JavaModelException {
	String string =  "module my.mod {\n"
			+ "  provides Y with pack1.Z;\n"
			+ "}\n";


	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:pack1.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
			"module my.mod {\n"
			+ "  provides Y with "+ expectedCompletionNodeToString + ";\n"
			+ "}\n";
	String expectedReplacedSource = "pack1.Z";
	String testName = "module-info.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		string.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
}
