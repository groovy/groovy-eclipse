/*******************************************************************************
 * Copyright (c) 2020, 2023 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class PatternMatchingSelectionTest extends AbstractSelectionTest {
	static {
		//		TESTS_NUMBERS = new int[] { 1 };
//				TESTS_NAMES = new String[] { "test005" };
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
						"    if (<SelectOnName:x_>)\n" +
						"        ;\n" +
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
					    "    {\n" +
						"      if (((x_ instanceof String y_) && (y_.length() > 0)))\n" +
						"          {\n" +
						"            <SelectOnName:y_>;\n" +
						"          }\n" +
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
						"    ((x_ instanceof String y_) && <SelectOnName:y_>);\n" +
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
						"    b = ((x_ instanceof <SelectionOnLocalName:String y_>) && (y_.length() > 0));\n" +
						"  }\n" +
						"}\n";
		String expectedReplacedSource = "y_";
		String testName = "X.java";

		int selectionStart = string.indexOf(selection);
		int selectionEnd = string.indexOf(selection) + selection.length() - 1;

		checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
				selectionIdentifier, expectedReplacedSource, testName);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/769
	// Open Declaration(F3) broken in pattern instanceof #769
	public void testGH769() throws JavaModelException {
		String source =
				"import java.util.Random;\n" +
				"public class TestBug {\n" +
				"	private static final void bugDemonstration() {\n" +
				"		new Object() {					\n" +
				"			private void methodA(Object object) {\n" +
				"				if (!(object instanceof Random varX))\n" +
				"					return;\n" +
				"			}\n" +
				"		\n" +
				"			private void methodB(Object object) {\n" +
				"				if (object instanceof String var1) {\n" +
				"				}\n" +
				"			}\n" +
				"		};\n" +
				"	}\n" +
				"}\n";

		String selection = "String";
		String selectKey = "<SelectOnType:";
		String expectedCompletionNodeToString = selectKey + selection + ">";

		String completionIdentifier = "String";
		String expectedUnitDisplayString =
				"import java.util.Random;\n" +
				"public class TestBug {\n" +
				"  public TestBug() {\n" +
				"  }\n" +
				"  private static final void bugDemonstration() {\n" +
				"    new Object() {\n" +
				"      private void methodA(Object object) {\n" +
				"        if ((! (object instanceof Random varX)))\n" +
				"            return;\n" +
				"      }\n" +
				"      private void methodB(Object object) {\n" +
				"        if ((object instanceof <SelectOnType:String> var1))\n" +
				"            {\n" +
				"            }\n" +
				"      }\n" +
				"    };\n" +
				"  }\n" +
				"}\n";



		String expectedReplacedSource = "String";
		String testName = "TestBug.java";

		int selectionStart = source.lastIndexOf(selection);
		int selectionEnd = source.lastIndexOf(selection) + selection.length() - 1;

		checkMethodParse(
				source.toCharArray(),
				selectionStart,
				selectionEnd,
				expectedCompletionNodeToString,
				expectedUnitDisplayString,
				completionIdentifier,
				expectedReplacedSource,
				testName);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1568
	// Current text selection cannot be opened in an editor #1568
	public void testGH1568() {
		if (this.complianceLevel < ClassFileConstants.JDK17)
			return;
		String string =  "@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
						+ "    public static Object k_;\n"
						+ "    public int val_;\n"
						+ "    public static void foo(X[] ar_ray) {\n"
						+ "       if (k instanceof String z_) {\n"
						+ "           System.out.println(\"Some statement\");\n"
						+ "           for(X x_ : ar_ray) {\n"
						+ "    	          int per = x_.val_ * 2;\n"
						+ "           }\n"
						+ "       }\n"
						+ "    }\n"
						+ "}\n";

		String selection = "x_";
		String selectKey = "<SelectOnName:";
		String expectedSelection = selectKey + selection + ">";

		String selectionIdentifier = "x_";
		String expectedUnitDisplayString =
						"public @SuppressWarnings(\"preview\") class X {\n" +
						"  public static Object k_;\n" +
						"  public int val_;\n" +
						"  <clinit>() {\n" +
						"  }\n" +
						"  public X() {\n" +
						"  }\n" +
						"  public static void foo(X[] ar_ray) {\n" +
						"    {\n" +
						"      {\n" +
						"        if ((k instanceof String z_))\n" +
						"            {\n" +
			            "              System.out.println(\"Some statement\");\n" +
						"              for (X x_ : ar_ray) \n" +
						"                {\n" +
						"                  int;\n" +
						"                  int per;\n" +
						"                  <SelectOnName:x_>;\n" +
						"                }\n" +
						"            }\n" +
						"      }\n" +
						"    }\n" +
						"  }\n" +
						"}\n";

		String expectedReplacedSource = "x_";
		String testName = "X.java";

		int selectionStart = string.lastIndexOf(selection);
		int selectionEnd = selectionStart + selection.length() - 1;

		checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
				selectionIdentifier, expectedReplacedSource, testName);
	}
}
