/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

/**
 * This is almost a copy of the one in org.eclipse.jdt.tests.latestBREE.
 * The other one also includes tests that are coded using the latest language
 * features and API. However, the bundle is not yet setup to run with the build
 * hence this is a temporary arrangement to keep the tests being run. The recommended
 * strategy is to keep this one updated and when the time comes, move this over
 * to the other bundle after synch-up of tests from both.
 * @author jay
 *
 */
public class TextBlockTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_NAMES = new String[] { "testCompliances_14" };
	}

	public static Class<?> testClass() {
		return TextBlockTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_14);
	}
	public TextBlockTest(String testName){
		super(testName);
	}
	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean previewFlag) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, previewFlag ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		return defaultOptions;
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions, String[] vmArguments) {
		runConformTest(testFiles, expectedOutput, customOptions, vmArguments, new JavacTestOptions("-source 14 --enable-preview"));
	}
	public void test001() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public static String textb = \"\"\"\"\"\";\n" +
				"	                               ^^\n" +
				"Syntax error on token \"\"\"\", invalid AssignmentOperator\n" +
				"----------\n");
	}
	public void test002() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\" \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public static String textb = \"\"\" \"\"\";\n" +
				"	                               ^^^\n" +
				"Syntax error on token \"\" \"\", invalid AssignmentOperator\n" +
				"----------\n");
	}
	public void test003() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public static String textb = \"\"\"\n" +
				"\";\n" +
				"	                             ^^^^^\n" +
				"Text block is not properly closed with the delimiter\n" +
				"----------\n");
	}
	public void test003a() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public static String textb = \"\"\"\n" +
				"\n" +
				"	                             ^^^^\n" +
				"Text block is not properly closed with the delimiter\n" +
				"----------\n");
	}
	/*
	 * negative - unescaped '\' in a text block
	 */
	public void test004() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"abc\\def" +
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public static String textb = \"\"\"\n" +
				"abc\\def\"\"\";\n" +
				"	                             ^^^^^^^^^\n" +
				"Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\\'  \\\\ )\n" +
				"----------\n");
	}
	/* empty text block */
	public void test005() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - escaped '\'
	 */
	public void test006() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"abc\\\\def" +
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"abc\\def",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * Positive - Multi line text block with varying indentation
	 * and \n
	 */
	public void test007() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"    line 1\n" +
						"    line 2\n" +
						"  \n" +
						"  line 3\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"line 1\n" + // test framework trims the leading whitespace
				"  line 2\n" +
				"\n" +
				"line 3",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * Positive - Multi line text block with varying indentation
	 * and \n and \r
	 */
	public void test008() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"    line 1\n" +
						"    line 2\r" +
						"  \r" +
						"  line 3\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"line 1\n" +
				"  line 2\n" +
				"\n" +
				"line 3", // the trailing whitespace is trimmed by the test framework
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * Positive - Multi line text block with varying indentation
	 * and \n and \r
	 */
	public void test008a() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"    line 1\n" +
						"    line 2\r" +
						"  \r" +
						"  line 3\n\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(\"<\");\n" +
						"		System.out.print(textb);\n" +
						"		System.out.print(\">\");\n" +
						"	}\n" +
						"}\n"
				},
				"<    line 1\n" +
				"    line 2\n" +
				"\n" +
				"  line 3\n" +
				">", // the trailing whitespace is trimmed by the test framework
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using unescaped '"' in text block
	 */
	public void test009() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"\"abc-def" +
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"\"abc-def",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using escaped '"' in text block
	 */
	public void test010() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"\"abc-def\\\"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"\"abc-def\"",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using escaped \ and escaped " in text block
	 */
	public void test011() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"\"abc\\\"\"\"def\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"\"abc\"\"\"def",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using Unicode in text block
	 * and compare with an equal String literal
	 */
	public void test012() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"\\u0ba4\\u0bae\\u0bbf\\u0bb4\"\"\";\n" +
						"	public static String str = \"\\u0ba4\\u0bae\\u0bbf\\u0bb4\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(str.equals(textb));\n" +
						"	}\n" +
						"}\n"
				},
				"true",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - bigger piece of code as text block
	 */
	public void test013() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"static String code = \"\"\"\n" +
						"              public void print(Object o) {\n" +
						"                  System.out.println(Objects.toString(o));\n" +
						"              }\n" +
						"              \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(code);\n" +
						"	}\n" +
						"}\n"
				},
				"public void print(Object o) {\n" +
				"    System.out.println(Objects.toString(o));\n" +
				"}",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - concatenation of string with text block
	 */
	public void test014() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String code = \"public void print(Object o) {\\n\" +\n" +
						"              \"\"\"\n" +
						"                  System.out.println(Objects.toString(o));\n" +
						"              }\n" +
						"              \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(code);\n" +
						"	}\n" +
						"}\n"
				},
				"public void print(Object o) {\n" +
				"    System.out.println(Objects.toString(o));\n" +
				"}",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - freely using quotes
	 */
	public void test015() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String story = \"\"\"\n" +
						"    \"When I use a word,\" Humpty Dumpty said,\n" +
						"    in rather a scornful tone, \"it means just what I\n" +
						"    choose it to mean - neither more nor less.\"\n" +
						"    \"The question is,\" said Alice, \"whether you\n" +
						"    can make words mean so many different things.\"\n" +
						"    \"The question is,\" said Humpty Dumpty,\n" +
						"    \"which is to be master - that's all.\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(story);\n" +
						"	}\n" +
						"}\n"
				},
				"\"When I use a word,\" Humpty Dumpty said,\n" +
				"in rather a scornful tone, \"it means just what I\n" +
				"choose it to mean - neither more nor less.\"\n" +
				"\"The question is,\" said Alice, \"whether you\n" +
				"can make words mean so many different things.\"\n" +
				"\"The question is,\" said Humpty Dumpty,\n" +
				"\"which is to be master - that's all.",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - html code with indentation
	 */
	public void test016() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" +
						"              <html>\n" +
						"                  <body>\n" +
						"                      <p>Hello, world</p>\n" +
						"                  </body>\n" +
						"              </html>\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n" +
				"    <body>\n" +
				"        <p>Hello, world</p>\n" +
				"    </body>\n" +
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - html code with indentation with empty lines
	 */
	public void test016a() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" +
						"              <html>\\r\\n" +
						"                  <body>\\r\\n" +
						"                      <p>Hello, world</p>\\r\\n" +
						"                  </body>\\r\\n" +
						"              </html>\\r\\n" +
						"              \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n" +
				"                  <body>\n" +
				"                      <p>Hello, world</p>\n" +
				"                  </body>\n" +
				"              </html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - html code with indentation with \r as terminator
	 */
	public void test016c() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" +
						"              <html>\n" +
						"                  <body>\n" +
						"                      <p>Hello, world</p>\n" +
						"                  </body>\n" +
						"              </html>\n" +
						"              \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n" +
				"    <body>\n" +
				"        <p>Hello, world</p>\n" +
				"    </body>\n" +
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - html code with indentation and trailing whitespace
	 */
	public void test017() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" +
						"              <html>  \n" +
						"                  <body>    \n" +
						"                      <p>Hello, world</p>      \n" +
						"                  </body>    \n" +
						"              </html>  \n" +
						"                   \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n" +
				"    <body>\n" +
				"        <p>Hello, world</p>\n" +
				"    </body>\n" +
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using octal escape char for trailing whitespace
	 */
	public void test018() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" +
						"              <html>\\040\\040\n" +
						"                  <body>\\040\\040\n" +
						"                      <p>Hello, world</p>\\040\\040\\040\n" +
						"                  </body>\\040\\040\n" +
						"              </html>\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>  \n" +
				"    <body>  \n" +
				"        <p>Hello, world</p>   \n" +
				"    </body>  \n" +
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using text block as a method argument
	 */
	public void test019() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(\"\"\"\n" +
						"              <html>\\n" +
						"                  <body>\\n" +
						"                      <p>Hello, world</p>\\n" +
						"                  </body>\\n" +
						"              </html>\\n" +
						"              \"\"\");\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n" +
				"                  <body>\n" +
				"                      <p>Hello, world</p>\n" +
				"                  </body>\n" +
				"              </html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using variable assigned with text block as a method argument
	 */
	public void test020() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		String html = \"\"\"\n" +
						"              <html>\n" +
						"                  <body>\n" +
						"                      <p>Hello, world</p>\n" +
						"                  </body>\n" +
						"              </html>\n" +
						"                  \"\"\";\n" +
						"		System.out.println(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n" +
				"    <body>\n" +
				"        <p>Hello, world</p>\n" +
				"    </body>\n" +
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - assigning strings and text blocks interchangeably.
	 */
	public void test021() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		String html = \"\"\"\n" +
						"              <html>\n" +
						"                  <body>\n" +
						"                      <p>Hello, world</p>\n" +
						"                  </body>\n" +
						"              </html>\n" +
						"                  \"\"\";\n" +
						"       String s = html;\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n" +
				"    <body>\n" +
				"        <p>Hello, world</p>\n" +
				"    </body>\n" +
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	public void test024() {
		runConformTest(
				new String[] {
						"Main.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Main {\n" +
						"    public static void main(String[] args) {\n" +
						"		runConformTest(\n" +
						"				new String[] {\n" +
						"						\"XYZ.java\",\n" +
						"						\"\"\"\n" +
						"								public class XYZ {\n" +
						"									public static String textb = \\\"\"\"\n" +
						"											abc\\\\\\\"\"\"def\"  \n" +
						"												\\\"\"\";\n" +
						"									public static void main(String[] args) {\n" +
						"										System.out.println(textb);\n" +
						"									}\n" +
						"								}\"\"\"" +
						"				}, \n" +
						"				\"\",\n" +
						"				null,\n" +
						"				new String[] {\"--enable-preview\"});\n" +
						"    }\n" +
						"	private static void runConformTest(String[] strings, String text, Object object, String[] strings2) {\n" +
						"		System.out.println(strings[1]);\n" +
						"	}\n" +
						"}"
				},
				"public class XYZ {\n" +
				"	public static String textb = \"\"\"\n" +
				"			abc\\\"\"\"def\"\n" +
				"				\"\"\";\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(textb);\n" +
				"	}\n" +
				"}",
				null,
				new String[] {"--enable-preview"});
	}
	public void test025() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"		public static String textb = \"\"\"\n" +
						"			abc\\def\"\"\";\n" +
						"		public static void main(String[] args) {\n" +
						"			System.out.println(textb);\n" +
						"		}\n" +
						"	}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public static String textb = \"\"\"\n" +
				"			abc\\def\"\"\";\n" +
				"	                             ^^^^^^^^^^^^\n" +
				"Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\\'  \\\\ )\n" +
				"----------\n",
				null,
				true,
				getCompilerOptions());
	}

	public void test027() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"  public static void main (String[] args) {\n" +
						"     String xyz = \n" +
						"       \"\"\"\n" +
						"         public class Switch {\n" +
						"           public static void bar(int arg0) {\n" +
						"             int arg1 = 0;\n" +
						"             pointer: foo(\n" +
						"               switch (0 + arg0) {\n" +
						"                 case 1 -> 1;\n" +
						"                 default -> {break p;}\\n\"\n" +
						"               }\n" +
						"             });\n" +
						"             public static void foo(int arg0) {\n" +
						"               bar(MyDay.SUNDAY);\n" +
						"               }\n" +
						"             }\\n\"\"\";  \n" +
						"    System.out.println(xyz);\n" +
						"  }\n" +
						"}"
				},
				"public class Switch {\n" +
				"  public static void bar(int arg0) {\n" +
				"    int arg1 = 0;\n" +
				"    pointer: foo(\n" +
				"      switch (0 + arg0) {\n" +
				"        case 1 -> 1;\n" +
				"        default -> {break p;}\n" +
				"\"\n" +
				"      }\n" +
				"    });\n" +
				"    public static void foo(int arg0) {\n" +
				"      bar(MyDay.SUNDAY);\n" +
				"      }\n" +
				"    }",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	// An empty text block
	public void test028() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"  public static void main (String[] args) {\n" +
						"     String xyz = \n" +
						"       \"\"\"\n" +
						"         \\n\"\"\";  \n" +
						"    System.out.println(xyz);\n" +
						"  }\n" +
						"}"
				},
				"",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	// An empty text block
	public void test029() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Cls2 {\n" +
						"	public static String str = \"\"\"\n" +
						"			   Hello Guru	\n" +
						"				\n" +
						"			\"\"\";\n" +
						"  public static void main (String[] args) {\n" +
						"    System.out.println(str);\n" +
						"  }\n" +
						"}"
				},
				"Hello Guru", // output comparison tool strips off all trailing whitespace
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug550356() {
		Map<String, String> options = getCompilerOptions(false);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public static String textb = \"\"\"\n" +
				"\"\"\";\n" +
				"	                             ^^^^^^^\n" +
				"Text Blocks is a preview feature and disabled by default. Use --enable-preview to enable\n" +
				"----------\n",
				null,
				true,
				options);
	}
	public void testBug551948_1() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Cls2 {\n" +
						"static String text = \"\"\"\n" +
						"            Lorem ipsum dolor sit amet, consectetur adipiscing \\\n" +
						"            elit, sed do eiusmod tempor incididunt ut labore \\\n" +
						"            et dolore magna aliqua.\\\n" +
						"            \"\"\";\n" +
						"  public static void main (String[] args) {\n" +
						"    System.out.print(text);\n" +
						"  }\n" +
						"}"
				},
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", // output comparison tool strips off all trailing whitespace
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_2() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Cls2 {\n" +
						"static String noLastLF = \"\"\"\n" +
						"    abc\n" +
						"        def\\\n" +
						"    ghi\"\"\";\n" +
						"  public static void main (String[] args) {\n" +
						"    System.out.print(noLastLF);\n" +
						"  }\n" +
						"}"
				},
				"abc\n    defghi",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_3() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Cls2 {\n" +
						"static String python = \"\"\"\n" +
						"    if x == True and \\\\\n" +
						"        y == False\n" +
						"    \"\"\";\n" +
						"  public static void main (String[] args) {\n" +
						"    System.out.print(python);\n" +
						"  }\n" +
						"}"
				},
				"if x == True and \\\n" +
				"    y == False",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_4() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Cls2 {\n" +
						"static String colors = \"\"\"\n" +
						"    red   \\\n" +
						"    green \\\n" +
						"    blue  \\\n" +
						"    orange\"\"\"; \n" +
						"  public static void main (String[] args) {\n" +
						"    System.out.print(colors);\n" +
						"  }\n" +
						"}"
				},
				"red   green blue  orange",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_5() {
		runNegativeTest(
				new String[] {
						"Cls2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Cls2 {\n" +
						"static String colors = \"\"\"\n" +
						"    \\red   \n" +
						"    \\green \n" +
						"    \\blue  \n" +
						"    \\orange\"\"\"; \n" +
						"  public static void main (String[] args) {\n" +
						"    System.out.print(colors);\n" +
						"  }\n" +
						"}"
				},
				"----------\n" +
				"1. ERROR in Cls2.java (at line 3)\n" +
				"	static String colors = \"\"\"\n" +
				"    \\red   \n" +
				"    \\green \n" +
				"	                       ^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\\'  \\\\ )\n" +
				"----------\n",
				null,
				true,
				getCompilerOptions(true));
	}
	public void testBug551948_6() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Cls2 {\n" +
						"static String str = \"A\\sline\\swith\\sspaces\";\n" +
						"  public static void main (String[] args) {\n" +
						"    System.out.print(str);\n" +
						"  }\n" +
						"}"
				},
				"A line with spaces",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_7() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Cls2 {\n" +
						"static String colors = \"\"\"\n" +
						"    red  \\s\n" +
						"    green\\s\n" +
						"    blue \\s\n" +
						"    \"\"\";\n" +
						"  public static void main (String[] args) {\n" +
						"    System.out.print(colors);\n" +
						"  }\n" +
						"}"
				},
				"red   \ngreen \nblue", // trailing whitespaces are trimmed
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_8() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class Cls2 {\n" +
						"static String s = \"\"\"\n" +
						"aaa\n" +
						"\n" +
						"bbb\n" +
						"\n" +
						"\n" +
						"ccc" +
						"\"\"\";\n" +
						"  public static void main (String[] args) {\n" +
						"    System.out.print(s);\n" +
						"  }\n" +
						"}"
				},
				"aaa\n\n" +
				"bbb\n\n\n" +
				"ccc",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_1() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	String textBlock = \"\"\"\n" +
						"\n" +
						"    			aa\"\"\";\n" +
						"    	 System.out.print(compare(textBlock));\n" +
						"    }\n" +
						"    private static boolean compare(String textBlock) {\n" +
						"    	char LF  = (char) 0x000A;\n" +
						"        String str = \"\" + LF + \"aa\";\n" +
						"        return textBlock.equals(str);\n" +
						"    }\n" +
						"}"
				},
				"true",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_2() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	String textBlock = \"\"\"\n" +
						"\\n" +
						"\\n" +
						"\"\"\";\n" +
						"    	 System.out.print(compare(textBlock));\n" +
						"    }\n" +
						"    private static boolean compare(String textBlock) {\n" +
						"    	 char LF  = (char) 0x000A;\n" +
						"        String str = \"\" + LF + LF + \"\";\n" +
						"        return textBlock.equals(str);\n" +
						"    }\n" +
						"}"
				},
				"true",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_3() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	 String textBlock = \"\"\"\n" +
						"\n" +
						"\"\"\";\n" +
						"    	 System.out.print(textBlock);\n" +
						"    	 System.out.print(compare(textBlock));\n" +
						"    }\n" +
						"    private static boolean compare(String textBlock) {\n" +
						"    	 char LF  = (char) 0x000A;\n" +
						"        String str = \"\" + '\\u0015' + LF + \"\";\n" +
						"        return textBlock.equals(str.stripIndent());\n" +
						"    }\n" +
						"}"
				},
				"true",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_4() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	 String textBlock = \"\"\"\n" +
						"v\r" +
						"\r" +
						"vaa\"\"\";\n" +
						"	char[] cs = textBlock.toCharArray();\n" +
						"    for (char c : cs) {\n" +
						"		//System.out.print((int)c);\n" +
						"		//System.out.print(',');\n" +
						"	}\n" +
						"    //System.out.println();\n" +
						"    	 System.out.print(compare(textBlock));\n" +
						"    }\n" +
						"    private static boolean compare(String textBlock) {\n" +
						"    	 char LF  = (char) 0x000A;\n" +
						"        String str = \"v\" + LF + LF + '\\u0076' + \"aa\";\n" +
						"        return textBlock.equals(str.stripIndent());\n" +
						"    }\n" +
						"}"
				},
				"true",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_5() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	 String textBlock = \"\"\"\n" +
						"aa\f" +
						"\f" +
						"\"\"\";\n" +
						"    	 System.out.print(compare(textBlock));\n" +
						"    }\n" +
						"    private static boolean compare(String textBlock) {\n" +
						"    	 char LF  = (char) 0x000A;\n" +
						"        String str = \"aa\" + LF + LF + \"\";\n" +
						"        return textBlock.equals(str);\n" +
						"    }\n" +
						"}"
				},
				"false",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_6() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	 String textBlock = \"\"\"\n" +
						"\n" +
						"\"\"\";\n" +
						"    	 System.out.print(compare(textBlock));\n" +
						"    }\n" +
						"    private static boolean compare(String textBlock) {\n" +
						"    	 char LF  = (char) 0x000A;\n" +
						"        String str = \"\" + '\\u0015' + LF + \"\";\n" +
						"        return textBlock.equals(str);\n" +
						"    }\n" +
						"}"
				},
				"true",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_7() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	 String textBlock = \"\"\"\n" +
						"aav\n" +
						"\"\"\";\n" +
						"    	 System.out.print(compare(textBlock));\n" +
						"    }\n" +
						"    private static boolean compare(String textBlock) {\n" +
						"    	 char LF  = (char) 0x000A;\n" +
						"        String str = \"aa\" + '\\u0076' + LF + \"\";\n" +
						"        return textBlock.equals(str.stripIndent());\n" +
						"    }\n" +
						"}"
				},
				"true",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_8() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	 String textBlock = \"\"\"\n" +
						"\\\"some\\\"\\n \\\"string\\\" \\n \\\"here\\\"\\n\"\"\";\n" +
						"    	 System.out.print(textBlock.length());\n" +
						"    }\n" +
						"}"
				},
				"26",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	// Escaped """ with escaping at the first '"'
	public void testCompliances_9() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	 String textBlock = \"\"\"\n" +
						"some string ends with \\\"\"\"\\n\"\"\";\n" +
						"    	 System.out.print(textBlock.length());\n" +
						"    }\n" +
						"}"
				},
				"26",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	// Escaped """ with escaping at the second '"'
	public void testCompliances_10() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	 String textBlock = \"\"\"\n" +
						"some string ends with \"\\\"\"\\n\"\"\";\n" +
						"    	 System.out.print(textBlock.length());\n" +
						"    }\n" +
						"}"
				},
				"26",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	// Escaped """ with escaping at the third '"'
	public void testCompliances_11() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	 String textBlock = \"\"\"\n" +
						"some string ends with \"\"\\\"\\n\"\"\";\n" +
						"    	 System.out.print(textBlock.length());\n" +
						"    }\n" +
						"}"
				},
				"26",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_12() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	String textBlock = \"\"\"\n" +
						"\r\n" +
						"    			aa\"\"\";\n" +
						"    	 System.out.print(compare(textBlock));\n" +
						"    }\n" +
						"    private static boolean compare(String textBlock) {\n" +
						"    	char LF  = (char) 0x000A;\n" +
						"        String str = \"\" + LF + \"aa\";\n" +
						"        return textBlock.equals(str);\n" +
						"    }\n" +
						"}"
				},
				"true",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_13() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	String textb = \"\"\"\n" +
						"\\0\\1\\2\\3\\4\\5\\6\\7\\10\\11\\12\\13\\14\\15\\16\\17\\20\\21\\22\\23\\24\\25\\26\\27\\30\\31\\32\\33\\34\\35\\36\\37\\40\\41\\42\\43\\44\\45\\46\\47\\50\\51\\52\\53\\54\\55\\56\\57\\60\\61\\62\\63\\64\\65\\66\\67\\70\\71\\72\\73\\74\\75\\76\\77\\100\\101\\102\\103\\104\\105\\106\\107\\110\\111\\112\\113\\114\\115\\116\\117\\120\\121\\122\\123\\124\\125\\126\\127\\130\\131\\132\\133\\134\\135\\136\\137\\140\\141\\142\\143\\144\\145\\146\\147\\150\\151\\152\\153\\154\\155\\156\\157\\160\\161\\162\\163\\164\\165\\166\\167\\170\\171\\172\\173\\174\\175\\176\\177\\200\\201\\202\\203\\204\\205\\206\\207\\210\\211\\212\\213\\214\\215\\216\\217\\220\\221\\222\\223\\224\\225\\226\\227\\230\\231\\232\\233\\234\\235\\236\\237\\240\\241\\242\\243\\244\\245\\246\\247\\250\\251\\252\\253\\254\\255\\256\\257\\260\\261\\262\\263\\264\\265\\266\\267\\270\\271\\272\\273\\274\\275\\276\\277\\300\\301\\302\\303\\304\\305\\306\\307\\310\\311\\312\\313\\314\\315\\316\\317\\320\\321\\322\\323\\324\\325\\326\\327\\330\\331\\332\\333\\334\\335\\336\\337\\340\\341\\342\\343\\344\\345\\346\\347\\350\\351\\352\\353\\354\\355\\356\\357\\360\\361\\362\\363\\364\\365\\366\\367\\370\\371\\372\\373\\374\\375\\376\\377\"\"\";\n" +
						"		System.out.println(textb.length());\n" +
						"		for (int i=0; i<=0xFF; i++) {\n" +
						"            if (i != (int)textb.charAt(i)) {\n" +
						"                System.out.println(\"Error in octal escape :\" + i);\n" +
						"            }\n" +
						"        }\n" +
						"    }\n" +
						"}"
				},
				"256",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testCompliances_14() {
		runConformTest(
				new String[] {
						"C.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class C {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	String textBlock = \"\"\"\r\n" +
						"          This is a multi-line\n" +
						"          message that is super-\n" +
						"          exciting!\"\"\";\n" +
						"    	 System.out.print(compare(textBlock));\n" +
						"    }\n" +
						"    private static boolean compare(String textBlock) {\n" +
						"        String str = \"This is a multi-line\\n\" + \n" +
						"				\"message that is super-\\n\" + \n" +
						"				\"exciting!\";\n" +
						"        return textBlock.equals(str);\n" +
						"    }\n" +
						"}"
				},
				"true",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug553252() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		Map<String, String> copy = new HashMap<String, String>(defaultOptions);
		copy.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_13);
		copy.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
		copy.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_13);
		copy.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		copy.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 0)\n" +
				"	public class X {\n" +
				"	^\n" +
				"Preview features enabled at an invalid source release level 13, preview can be enabled only at source level 14\n" +
				"----------\n",
				null,
				true,
				copy);
	}
}
