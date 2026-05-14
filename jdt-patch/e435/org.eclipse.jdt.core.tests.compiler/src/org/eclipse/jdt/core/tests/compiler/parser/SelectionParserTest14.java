/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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

public class SelectionParserTest14 extends AbstractSelectionTest {
static {
//		TESTS_NUMBERS = new int[] { 1 };
//		TESTS_NAMES = new String[] { "test005" };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(SelectionParserTest14.class, F_14);
}

public SelectionParserTest14(String testName) {
	super(testName);
}
/*
 * Multi constant case statement with ':', selection node is the string constant
 */
public void test001() throws JavaModelException {
	String string =  "public class X {\n" +
	"static final String ONE=\"One\", TWO = \"Two\", THREE=\"Three\";\n" +
	"  public static void foo(String num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE:\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"}";

	String selection = "ONE";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "ONE";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  static final String ONE;\n" +
					"  static final String TWO;\n" +
					"  static final String THREE;\n" +
					"  <clinit>() {\n" +
					"  }\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static void foo(String num) {\n" +
					"    {\n" +
					"      switch (num) {\n" +
					"      case <SelectOnName:ONE> :\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "ONE";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with ':', selection node is the first enum constant
 */
public void test002() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(Num num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE:\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}";

	String selection = "ONE";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "ONE";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  enum Num {\n" +
					"    ONE(),\n" +
					"    TWO(),\n" +
					"    THREE(),\n" +
					"    <clinit>() {\n" +
					"    }\n" +
					"    Num() {\n" +
					"    }\n" +
					"  }\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static void foo(Num num) {\n" +
					"    {\n" +
					"      switch (num) {\n" +
					"      case <SelectOnName:ONE> :\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "ONE";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with ':', selection node is the second string constant
 */
public void test003() throws JavaModelException {
	String string =  "public class X {\n" +
	"static final String ONE=\"One\", TWO = \"Two\", THREE=\"Three\";\n" +
	"  public static void foo(String num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE:\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"}";

	String selection = "TWO";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "TWO";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  static final String ONE;\n" +
					"  static final String TWO;\n" +
					"  static final String THREE;\n" +
					"  <clinit>() {\n" +
					"  }\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static void foo(String num) {\n" +
					"    {\n" +
					"      switch (num) {\n" +
					"      case <SelectOnName:TWO> :\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "TWO";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with ':', selection node is the second enum constant
 */
public void test004() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(Num num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE:\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}";

	String selection = "TWO";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "TWO";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  enum Num {\n" +
					"    ONE(),\n" +
					"    TWO(),\n" +
					"    THREE(),\n" +
					"    <clinit>() {\n" +
					"    }\n" +
					"    Num() {\n" +
					"    }\n" +
					"  }\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static void foo(Num num) {\n" +
					"    {\n" +
					"      switch (num) {\n" +
					"      case <SelectOnName:TWO> :\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "TWO";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection node is the string constant
 */
public void test005() throws JavaModelException {
	String string =  "public class X {\n" +
	"static final String ONE=\"One\", TWO = \"Two\", THREE=\"Three\";\n" +
	"  public static void foo(String num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num);\n" +
	"    }" +
	"  }\n" +
	"}";
	/*
	 * Note: The completion parser ignores the -> that follows and we end up creating
	 * the CaseStatement without maring it as an Expression, hence the ':' instead of the '->'
	 */
	String selection = "ONE";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";
	String selectionIdentifier = "ONE";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  static final String ONE;\n" +
					"  static final String TWO;\n" +
					"  static final String THREE;\n" +
					"  <clinit>() {\n" +
					"  }\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static void foo(String num) {\n" +
					"    {\n" +
					"      switch (num) {\n" +
					"      case <SelectOnName:ONE> :\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "ONE";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection node is the first enum constant
 */
public void test006() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(Num num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num);\n" +
	"		 break; // illegal, but should be ignored and shouldn't matter\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}";

	String selection = "ONE";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "ONE";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  enum Num {\n" +
					"    ONE(),\n" +
					"    TWO(),\n" +
					"    THREE(),\n" +
					"    <clinit>() {\n" +
					"    }\n" +
					"    Num() {\n" +
					"    }\n" +
					"  }\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static void foo(Num num) {\n" +
					"    {\n" +
					"      switch (num) {\n" +
					"      case <SelectOnName:ONE> :\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "ONE";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection node is the second string constant
 */
public void test007() throws JavaModelException {
	String string =  "public class X {\n" +
	"static final String ONE=\"One\", TWO = \"Two\", THREE=\"Three\";\n" +
	"  public static void foo(String num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"}";

	String selection = "TWO";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "TWO";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  static final String ONE;\n" +
					"  static final String TWO;\n" +
					"  static final String THREE;\n" +
					"  <clinit>() {\n" +
					"  }\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static void foo(String num) {\n" +
					"    {\n" +
					"      switch (num) {\n" +
					"      case <SelectOnName:TWO> :\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "TWO";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection node is the second enum constant
 */
public void test008() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(Num num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}";

	String selection = "TWO";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "TWO";
	String expectedUnitDisplayString =
			"public class X {\n" +
					"  enum Num {\n" +
					"    ONE(),\n" +
					"    TWO(),\n" +
					"    THREE(),\n" +
					"    <clinit>() {\n" +
					"    }\n" +
					"    Num() {\n" +
					"    }\n" +
					"  }\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static void foo(Num num) {\n" +
					"    {\n" +
					"      switch (num) {\n" +
					"      case <SelectOnName:TWO> :\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "TWO";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a reference in the case block
 * which same as the switch's expression
 */
public void test009() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(Num num_) {\n" +
	" 	 switch (num_) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num_);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  enum Num {\n" +
			"    ONE(),\n" +
			"    TWO(),\n" +
			"    THREE(),\n" +
			"    <clinit>() {\n" +
			"    }\n" +
			"    Num() {\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public static void foo(Num num_) {\n" +
			"    {\n" +
			"      switch (num_) {\n" +
			"      case THREE ->\n" +
			"          <SelectOnName:num_>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a reference in the case block
 * which is referencing a local variable defined in the case block
 */
public void test010() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(Num num_) {\n" +
	" 	 switch (num_) {\n" +
	"	   case ONE, TWO, THREE -> {\n" +
	"		 int i_j = 0;" +
	"		 System.out.println(i_j);\n" +
	"		 break;" +
	"		 }\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}";

	String selection = "i_j";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "i_j";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  enum Num {\n" +
			"    ONE(),\n" +
			"    TWO(),\n" +
			"    THREE(),\n" +
			"    <clinit>() {\n" +
			"    }\n" +
			"    Num() {\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public static void foo(Num num_) {\n" +
			"    {\n" +
			"      {\n" +
			"        switch (num_) {\n" +
			"        case THREE ->\n" +
			"            int i_j;\n" +
			"            <SelectOnName:i_j>;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "i_j";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type enum in switch expression
 */
public void test011() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(Num num_) {\n" +
	" 	 switch (num_) {\n" +
	"	   case ONE, TWO, THREE -> {\n" +
	"		 break;" +
	"		 }\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  enum Num {\n" +
			"    ONE(),\n" +
			"    TWO(),\n" +
			"    THREE(),\n" +
			"    <clinit>() {\n" +
			"    }\n" +
			"    Num() {\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public static void foo(Num num_) {\n" +
			"    <SelectOnName:num_>;\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test012() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(int num_) {\n" +
	" 	 switch (num_ + 1) {\n" +
	"	   case 1, 2, 3 -> {\n" +
	"		 break;" +
	"		 }\n" +
	"    }" +
	"  }\n" +
	"}";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public static void foo(int num_) {\n" +
			"    <SelectOnName:num_>;\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test013() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(int num_) {\n" +
	" 	 int i = switch (num_) {\n" +
	"	   case 1, 2, 3 -> (num_ + 1);\n" +
	"      default -> 0;\n" +
	"    }" +
	"  }\n" +
	"}";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public static void foo(int num_) {\n" +
			"    int i;\n" +
			"    {\n" +
			"      switch (num_) {\n" +
			"      case 3 ->\n" +
			"          <SelectOnName:num_>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test014() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(int num_) {\n" +
	" 	 int i = switch (num_) {\n" +
	"	   case 1, 2, 3 -> 0;\n" +
	"      default -> (num_ + 1);\n" +
	"    }" +
	"  }\n" +
	"}";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public static void foo(int num_) {\n" +
			"    int i;\n" +
			"    {\n" +
			"      switch (num_) {\n" +
			"      case 3 ->\n" +
			"          0;\n" +
			"      default ->\n" +
			"          <SelectOnName:num_>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test015() throws JavaModelException {
	String string =  "public class X {\n" +
	"  public static void foo(int num_) {\n" +
	" 	 int i = switch (num_) {\n" +
	"	   case 1, 2, 3 -> 0;\n" +
	"      default -> (num_ + 1);\n" +
	"    }" +
	"  }\n" +
	"}";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public static void foo(int num_) {\n" +
			"    int i;\n" +
			"    {\n" +
			"      switch (num_) {\n" +
			"      case 3 ->\n" +
			"          0;\n" +
			"      default ->\n" +
			"          <SelectOnName:num_>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test016() throws JavaModelException {
	String string =  "public class X {\n" +
			"	public void bar(int s) {\n" +
			"		int i_j = switch (s) {\n" +
			"			case 1, 2, 3 -> (s+1);\n" +
			"			default -> i_j;\n" +
			"		};\n" +
			"	}\n" +
			"}\n";

	String selection = "i_j";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "i_j";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void bar(int s) {\n" +
			"    int i_j;\n" +
			"    {\n" +
			"      switch (s) {\n" +
			"      case 3 ->\n" +
			"          (s + 1);\n" +
			"      default ->\n" +
			"          <SelectOnName:i_j>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "i_j";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test017() throws JavaModelException {
	String string =  "public class X {\n" +
			"	public void bar(int s) {\n" +
			"		int i_j = switch (s) {\n" +
			"			case 1, 2, 3 -> (s+1);\n" +
			"			default -> (1+i_j);\n" +
			"		};\n" +
			"	}\n" +
			"}\n";

	String selection = "i_j";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "i_j";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void bar(int s) {\n" +
			"    int i_j;\n" +
			"    {\n" +
			"      switch (s) {\n" +
			"      case 3 ->\n" +
			"          (s + 1);\n" +
			"      default ->\n" +
			"          <SelectOnName:i_j>;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "i_j";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test018() throws JavaModelException {
	String string =  "import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1() { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int i) { \n" +
			"		m(switch(i) { \n" +
			"			case 1 -> this::n_1; \n" +
			"			default -> this::n_2; }); \n" +
			"	}\n" +
			"}";

	String selection = "n_1";
	String selectKey = "<SelectionOnReferenceExpressionName:this::";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "n_1";
	String expectedUnitDisplayString =
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {\n" +
			"}\n" +
			"interface IN1 extends IN0 {\n" +
			"}\n" +
			"interface IN2 extends IN0 {\n" +
			"}\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  IN1 n_1() {\n" +
			"  }\n" +
			"  IN2 n_2() {\n" +
			"  }\n" +
			"  <M>void m(Supplier<M> m2) {\n" +
			"  }\n" +
			"  void testSw(int i) {\n" +
			"    m(switch (i) {\n" +
			"case 1 ->\n" +
			"    <SelectionOnReferenceExpressionName:this::n_1>;\n" +
			"default ->\n" +
			"    this::n_2;\n" +
			"});\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "this::n_1";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test019() throws JavaModelException {
	String string =  "import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1() { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int i) { \n" +
			"		m(switch(i) { \n" +
			"			case 2 -> () -> n_1(); \n" +
			"			default -> this::n_2; }); \n" +
			"	}\n" +
			"}";

	String selection = "n_1";
	String selectKey = "<SelectOnMessageSend:";
	String expectedSelection = selectKey + selection + "()>";

	String selectionIdentifier = "n_1";
	String expectedUnitDisplayString =
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {\n" +
			"}\n" +
			"interface IN1 extends IN0 {\n" +
			"}\n" +
			"interface IN2 extends IN0 {\n" +
			"}\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  IN1 n_1() {\n" +
			"  }\n" +
			"  IN2 n_2() {\n" +
			"  }\n" +
			"  <M>void m(Supplier<M> m2) {\n" +
			"  }\n" +
			"  void testSw(int i) {\n" +
			"    m(switch (i) {\n" +
			"case 2 ->\n" +
			"    () -> <SelectOnMessageSend:n_1()>;\n" +
			"default ->\n" +
			"    this::n_2;\n" +
			"});\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "n_1()";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test020() throws JavaModelException {
	String string =  "import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1() { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int i) { \n" +
			"		m(switch(i) { \n" +
			"			default -> this::n_2; }); \n" +
			"	}\n" +
			"}";

	String selection = "n_2";
	String selectKey = "<SelectionOnReferenceExpressionName:this::";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "n_2";
	String expectedUnitDisplayString =
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {\n" +
			"}\n" +
			"interface IN1 extends IN0 {\n" +
			"}\n" +
			"interface IN2 extends IN0 {\n" +
			"}\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  IN1 n_1() {\n" +
			"  }\n" +
			"  IN2 n_2() {\n" +
			"  }\n" +
			"  <M>void m(Supplier<M> m2) {\n" +
			"  }\n" +
			"  void testSw(int i) {\n" +
			"    m(switch (i) {\n" +
			"default ->\n" +
			"    <SelectionOnReferenceExpressionName:this::n_2>;\n" +
			"});\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "this::n_2";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test021() throws JavaModelException {
	String string =  "import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1(int ijk) { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int ijk) { \n" +
			"		m(switch(ijk) { \n" +
			"			default -> () -> n_1(ijk); }); \n" +
			"	}\n" +
			"}";

	String selection = "n_1";
	String selectKey = "<SelectOnMessageSend:";
	String expectedSelection = selectKey + selection + "(ijk)>";

	String selectionIdentifier = "n_1";
	String expectedUnitDisplayString =
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {\n" +
			"}\n" +
			"interface IN1 extends IN0 {\n" +
			"}\n" +
			"interface IN2 extends IN0 {\n" +
			"}\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  IN1 n_1(int ijk) {\n" +
			"  }\n" +
			"  IN2 n_2() {\n" +
			"  }\n" +
			"  <M>void m(Supplier<M> m2) {\n" +
			"  }\n" +
			"  void testSw(int ijk) {\n" +
			"    m(switch (ijk) {\n" +
			"default ->\n" +
			"    () -> <SelectOnMessageSend:n_1(ijk)>;\n" +
			"});\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "n_1(ijk)";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test022() throws JavaModelException {
	String string =  "import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1(int ijk) { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int ijk) { \n" +
			"		m(switch(ijk) { \n" +
			"			default -> () -> n_1(ijk); }); \n" +
			"	}\n" +
			"}";

	String selection = "ijk";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "ijk";
	String expectedUnitDisplayString =
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.function.*;\n" +
			"interface IN0 {\n" +
			"}\n" +
			"interface IN1 extends IN0 {\n" +
			"}\n" +
			"interface IN2 extends IN0 {\n" +
			"}\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  IN1 n_1(int ijk) {\n" +
			"  }\n" +
			"  IN2 n_2() {\n" +
			"  }\n" +
			"  <M>void m(Supplier<M> m2) {\n" +
			"  }\n" +
			"  void testSw(int ijk) {\n" +
			"    m(switch (ijk) {\n" +
			"default ->\n" +
			"    () -> n_1(<SelectOnName:ijk>);\n" +
			"});\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "ijk";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void testIssue708_1() throws JavaModelException {
	String string =  "public class X {\n"
			+ "	public void test(Type type, String string) {\n"
			+ "		switch (type) {\n"
			+ "		case openDeclarationFails -> {\n"
			+ "			switch (string) {\n"
			+ "				case \"Test\" -> method(Type.openDeclarationFails);\n"
			+ "			}\n"
			+ "		}\n"
			+ "		}\n"
			+ "	}\n"
			+ "	private void method(Type relay) {}\n"
			+ "	static public enum Type {\n"
			+ "		openDeclarationFails, anotherValue;\n"
			+ "	}\n"
			+ "}";

	String selection = "openDeclarationFails";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "openDeclarationFails";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public static enum Type {\n" +
			"    openDeclarationFails(),\n" +
			"    anotherValue(),\n" +
			"    <clinit>() {\n" +
			"    }\n" +
			"    public Type() {\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void test(Type type, String string) {\n" +
			"    {\n" +
			"      switch (type) {\n" +
			"      case <SelectOnName:openDeclarationFails> :\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  private void method(Type relay) {\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "openDeclarationFails";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void testIssue708_2() throws JavaModelException {
	String string =  "public class X {\n"
			+ "	static public enum Type {\n"
			+ "		openDeclarationFails, anotherValue;\n"
			+ "	}\n"
			+ "	public void test(Type type, String string) {\n"
			+ "		switch (type) {\n"
			+ "		case openDeclarationFails -> {\n"
			+ "			switch (string) {\n"
			+ "			case \"Test\" -> method(Type.openDeclarationFails);\n"
			+ "			}\n"
			+ "		}\n"
			+ "		case anotherValue -> {\n"
			+ "			switch (string) {\n"
			+ "			case \"Test\" -> method(Type.anotherValue);\n"
			+ "			}\n"
			+ "		}\n"
			+ "		}\n"
			+ "	}\n"
			+ "	private void method(Type relay) {}\n"
			+ "}";

	String selection = "anotherValue";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + "Type." + selection + ">";

	String selectionIdentifier = "anotherValue";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public static enum Type {\n" +
			"    openDeclarationFails(),\n" +
			"    anotherValue(),\n" +
			"    <clinit>() {\n" +
			"    }\n" +
			"    public Type() {\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void test(Type type, String string) {\n" +
			"    {\n" +
			"      {\n" +
			"        {\n" +
			"          switch (type) {\n" +
			"          case openDeclarationFails ->\n" +
			"              {\n" +
			"                switch (string) {\n" +
			"                case \"Test\" ->\n" +
			"                    method(Type.openDeclarationFails);\n" +
			"                }\n" +
			"              }\n" +
			"          case anotherValue ->\n" +
			"              switch (string) {\n" +
			"              case \"Test\" ->\n" +
			"                  <SelectOnName:Type.anotherValue>;\n" +
			"              }\n" +
			"          }\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  private void method(Type relay) {\n" +
			"  }\n" +
			"}\n";
	String expectedReplacedSource = "Type.anotherValue";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
}
