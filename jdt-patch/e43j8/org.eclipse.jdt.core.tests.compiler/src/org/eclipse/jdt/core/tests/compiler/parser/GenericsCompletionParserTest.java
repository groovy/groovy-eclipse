/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericsCompletionParserTest extends AbstractCompletionTest {
public GenericsCompletionParserTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(GenericsCompletionParserTest.class);
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	return options;
}
public void test0001(){
	String str =
		"public class X  <T extends Z<Y>. {\n" +
		"}";


	String completeBehind = "Z<Y>.";
	int cursorLocation = str.indexOf("Z<Y>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z<Y>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Z<Y>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Z<Y>.";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:Z<Y>.>> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0002(){
	String str =
		"public class X  <T extends Z<Y>.W {\n" +
		"}";


	String completeBehind = "Z<Y>.W";
	int cursorLocation = str.indexOf("Z<Y>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z<Y>.W>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Z<Y>.W>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "Z<Y>.W";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:Z<Y>.W>> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0003(){
	String str =
		"public class Test<T extends test0001.X<Y>.Z> {\n" +
		"}";


	String completeBehind = "X<Y>.Z";
	int cursorLocation = str.indexOf("X<Y>.Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:test0001.X<Y>.Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:test0001.X<Y>.Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "test0001.X<Y>.Z";
	String expectedUnitDisplayString =
		"public class Test<T extends <CompleteOnType:test0001.X<Y>.Z>> {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0004(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:Y<Z>.>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0005(){
	String str =
		"public class X {\n" +
		"  public Y<Z>. foo\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:Y<Z>.>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0006(){
	String str =
		"public class X {\n" +
		"  public Y<Z>. foo;\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:Y<Z>.>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0007(){
	String str =
		"public class X {\n" +
		"  public Y<Z>. foo()\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public <CompleteOnType:Y<Z>.> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0008(){
	String str =
		"public class X {\n" +
		"  public Y<Z>. foo(){}\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public <CompleteOnType:Y<Z>.> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0009(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>.\n" +
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:Y<Z>.V<W>.>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0010(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>. foo\n" +
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:Y<Z>.V<W>.>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0011(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>. foo;\n" +
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:Y<Z>.V<W>.>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0012(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>. foo()\n" +
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public <CompleteOnType:Y<Z>.V<W>.> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0013(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>. foo(){}\n" +
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public <CompleteOnType:Y<Z>.V<W>.> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0014(){
	String str =
		"public class X extends  Y<Z>. {\n" +
		"  \n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClass:Y<Z>.>";
	String expectedParentNodeToString = 
		"public class X extends <CompleteOnClass:Y<Z>.> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X extends <CompleteOnClass:Y<Z>.> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0015(){
	String str =
		"public class X implements I1, Y<Z>. {\n" +
		"  \n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnInterface:Y<Z>.>";
	String expectedParentNodeToString = 
		"public class X implements I1, <CompleteOnInterface:Y<Z>.> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X implements I1, <CompleteOnInterface:Y<Z>.> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0016(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z>.){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:Y<Z>.>;\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0017(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z>. bar){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(<CompleteOnType:Y<Z>.> bar) {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0018(){
	String str =
		"public class X {\n" +
		"  Y<Z>. foo(){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <CompleteOnType:Y<Z>.> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0019(){
	String str =
		"public class X  {\n" +
		"  void foo() throws Y<Z>. {\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws <CompleteOnException:Y<Z>.> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0020(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.> void foo(){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0021(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.> void foo(\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0022(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.> int foo\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0023(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.> X\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0024(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.>\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0025(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>. void foo(){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0026(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>. void foo(\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0027(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>. int foo\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  int foo;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0028(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>. X\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0029(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:Y<Z>.>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0030_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0030_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0031_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z>. var\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0031_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z>. var\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0032_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z>.W\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0032_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z>.W\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.W>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "Y<Z>.W";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.W>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0033_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z>.W var\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0033_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z>.W var\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.W>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "Y<Z>.W";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.W>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0034_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.>bar();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0034_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.>bar();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0035_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.>bar(\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0035_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.>bar(\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0036_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.>bar\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0036_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.>bar\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0037_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0037_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0038_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0038_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0039_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new <Y, Y<Z>.>X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0039_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new <Y, Y<Z>.>X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0040_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    aaa.new <Y, Y<Z>.>X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0040_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    aaa.new <Y, Y<Z>.>X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0041_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new V().new <Y, Y<Z>.>X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0041_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new V().new <Y, Y<Z>.>X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0042_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z>. var;;){}\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0042_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z>. var;;){}\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0043_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0043_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0044_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z>. e) {\n" +
		"   }\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0044_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z>. e) {\n" +
		"   }\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	String expectedParentNodeToString =
			"try\n" +
			"  {\n" +
			"  }\n" +
			"catch (<CompleteOnException:Y<Z>.>  )\n" +
			"  {\n" +
			"  }";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    try\n" +
			"      {\n" +
			"      }\n" +
			"    catch (<CompleteOnException:Y<Z>.>  )\n" +
			"      {\n" +
			"      }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0045_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z>. e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0045_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z>. e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	String expectedParentNodeToString =
			"try\n" +
			"  {\n" +
			"  }\n" +
			"catch (<CompleteOnException:Y<Z>.>  )\n" +
			"  {\n" +
			"  }";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    try\n" +
			"      {\n" +
			"      }\n" +
			"    catch (<CompleteOnException:Y<Z>.>  )\n" +
			"      {\n" +
			"      }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0046_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    catch(Y<Z>. e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0046_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    catch(Y<Z>. e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnException:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0047_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (Y<Z>.) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0047_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (Y<Z>.) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object a = <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0048_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (Y<Z>.) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0048_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (Y<Z>.) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object a = <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0049_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    (Y<Z>.) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0049_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    (Y<Z>.) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0050_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z>.[0];\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0050_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z>.[0];\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0051_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0051_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0052_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0052_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new Y<Z>.\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0053_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    <Y<Z>.>super();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0053_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    <Y<Z>.>super();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0054_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    aaa.<Y<Z>.>super();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0054_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    aaa.<Y<Z>.>super();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0055_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    A.this.<Y<Z>.>super();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0055_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    A.this.<Y<Z>.>super();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    <CompleteOnType:Y<Z>.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}

public void test0056_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0056_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0057_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<V,Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0057_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Y<V,Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<V, <CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<V, <CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0058_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    W<U>.Y<V,Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0058_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    W<U>.Y<V,Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "W<U>.Y<V, <CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    W<U>.Y<V, <CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0059(){
	String str =
		"public class X  <T extends Z<Y {\n" +
		"}";


	String completeBehind = "Z<Y";
	int cursorLocation = str.indexOf("Z<Y") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y>";
	String expectedParentNodeToString = "Z<<CompleteOnType:Y>>";
	String completionIdentifier = "Y";
	String expectedReplacedSource = "Y";
	String expectedUnitDisplayString =
		"public class X<T> {\n" +
		"  Z<<CompleteOnType:Y>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0060(){
	String str =
		"public class X {\n" +
		"  public Y<Z\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0061(){
	String str =
		"public class X {\n" +
		"  public Y<Z>\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0062(){
	String str =
		"public class X {\n" +
		"  public Y<Z> var\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
	"  public Y<<CompleteOnType:Z>> var;\n" +
	"  public X() {\n" +
	"  }\n" +
	"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0063(){
	String str =
		"public class X {\n" +
		"  public Y<Z> var;\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public Y<<CompleteOnType:Z>> var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0064(){
	String str =
		"public class X {\n" +
		"  public Y<Z foo()\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0065(){
	String str =
		"public class X {\n" +
		"  public Y<Z> foo()\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public Y<<CompleteOnType:Z>> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0066(){
	String str =
		"public class X {\n" +
		"  public Y<Z foo(){}\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0067(){
	String str =
		"public class X {\n" +
		"  public Y<Z> foo(){}\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public Y<<CompleteOnType:Z>> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0068(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W\n" +
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<Z>.V<<CompleteOnType:W>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0069(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>\n" +
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<Z>.V<<CompleteOnType:W>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0070(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W> var\n" +
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public Y<Z>.V<<CompleteOnType:W>> var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0071(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W> var;\n" +
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public Y<Z>.V<<CompleteOnType:W>> var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0072(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W foo()\n" +
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<Z>.V<<CompleteOnType:W>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0073(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W> foo()\n" +
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public Y<Z>.V<<CompleteOnType:W>> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0074(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W foo(){}\n" +
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<Z>.V<<CompleteOnType:W>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0075(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W> foo(){}\n" +
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public Y<Z>.V<<CompleteOnType:W>> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0076(){
	String str =
		"public class X extends  Y<Z {\n" +
		"  \n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0077(){
	String str =
		"public class X extends  Y<Z> {\n" +
		"  \n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X extends Y<<CompleteOnType:Z>> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0078(){
	String str =
		"public class X implements I1, Y<Z {\n" +
		"  \n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X implements I1 {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0079(){
	String str =
		"public class X implements I1, Y<Z> {\n" +
		"  \n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X implements I1, Y<<CompleteOnType:Z>> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0080(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Y<<CompleteOnType:Z>>;\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0081(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z>){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Y<<CompleteOnType:Z>>;\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0082(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z> var){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(Y<<CompleteOnType:Z>> var) {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0083(){
	String str =
		"public class X {\n" +
		"  Y<Z foo(){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0084(){
	String str =
		"public class X {\n" +
		"  Y<Z> foo(){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  Y<<CompleteOnType:Z>> foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0085(){
	String str =
		"public class X  {\n" +
		"  void foo() throws Y<Z {\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Y<<CompleteOnType:Z>>;\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0086(){
	String str =
		"public class X  {\n" +
		"  void foo() throws Y<Z> {\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws Y<<CompleteOnType:Z>> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0087(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z void foo(){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0088(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z> void foo(){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends Y<<CompleteOnType:Z>>>\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0089(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>> void foo(){\n" +
		"  \n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends Y<<CompleteOnType:Z>>>void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0090(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z int foo\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  int foo;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0091(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z> int foo\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  int foo;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends Y<<CompleteOnType:Z>>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0092(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>> int foo\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends Y<<CompleteOnType:Z>>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0093(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z X\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0094(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z> X\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends Y<<CompleteOnType:Z>>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0095(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>> X\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends Y<<CompleteOnType:Z>>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0096(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0097(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends Y<<CompleteOnType:Z>>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0098(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>>\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends Y<<CompleteOnType:Z>>>\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0099_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z bar();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0099_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z bar();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0100_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z> bar();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0100_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z> bar();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0101_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>> bar();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0101_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>> bar();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0102_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z bar\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0102_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z bar\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0103_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z> bar\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0103_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z> bar\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0104_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>> bar\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0104_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>> bar\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0105_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0105_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0106_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0106_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0107_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0107_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    this.<Y, Y<Z>>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0108_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new <Y, Y<Z X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0108_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new <Y, Y<Z X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0109_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new <Y, Y<Z> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0109_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new <Y, Y<Z> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0110_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new <Y, Y<Z>> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0110_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new <Y, Y<Z>> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0111_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    aaa.new <Y, Y<Z X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0111_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    aaa.new <Y, Y<Z X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0112_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    aaa.new <Y, Y<Z> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0112_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    aaa.new <Y, Y<Z> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0113_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    aaa.new <Y, Y<Z>> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0113_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    aaa.new <Y, Y<Z>> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0114_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new V().new <Y, Y<Z X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0114_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new V().new <Y, Y<Z X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0115_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new V().new <Y, Y<Z> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0115_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new V().new <Y, Y<Z> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0116_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new V().new <Y, Y<Z>> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0116_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new V().new <Y, Y<Z>> X();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0117_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z var;;){}\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0117_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z var;;){}\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0118_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z> var;;){}\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0118_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z> var;;){}\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0119_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0119_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0120_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0120_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    for(Y<Z>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0121_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z e) {\n" +
		"   }\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0121_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z e) {\n" +
		"   }\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0122_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z> e) {\n" +
		"   }\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0122_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z> e) {\n" +
		"   }\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0123_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0123_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0124_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z> e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0124_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"    } catch(Y<Z> e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0125_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    catch(Y<Z e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0125_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    catch(Y<Z e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0126_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    catch(Y<Z> e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0126_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    catch(Y<Z> e\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0127_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (Y<Z ) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0127_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (Y<Z ) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object a = (Y < <CompleteOnName:Z>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0128_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (Y<Z> ) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0128_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (Y<Z> ) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object a = (Y < <CompleteOnName:Z>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0129_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    (Y<Z) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0129_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    (Y<Z) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    (Y < <CompleteOnName:Z>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0130_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    (Y<Z>) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0130_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    (Y<Z>) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    (Y < <CompleteOnName:Z>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0131_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z[0];\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0131_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z[0];\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = new Y<<CompleteOnType:Z>>();\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0132_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z>[0];\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0132_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z>[0];\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = new Y<<CompleteOnType:Z>>();\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0133_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0133_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = new Y<<CompleteOnType:Z>>();\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0134_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0134_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object[] o = new Y<Z>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object[] o = new Y<<CompleteOnType:Z>>();\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0135_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0135_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new Y<Z\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0136_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new Y<Z>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0136_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new Y<Z>\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0137_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    <Y<Z super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0137_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    <Y<Z super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0138_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    <Y<Z> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0138_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    <Y<Z> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0139_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    <Y<Z>> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0139_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    <Y<Z>> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0140_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    aaa.<Y<Z super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0140_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    aaa.<Y<Z super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0141_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    aaa.<Y<Z> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0141_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    aaa.<Y<Z> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0142_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    aaa.<Y<Z>> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0142_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    aaa.<Y<Z>> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0143_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    A.this.<Y<Z super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0143_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    A.this.<Y<Z super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0144_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    A.this.<Y<Z> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0144_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    A.this.<Y<Z> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0145_Diet(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    A.this.<Y<Z>> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0145_Method(){
	String str =
		"public class X {\n" +
		"  public X() {\n" +
		"    A.this.<Y<Z>> super(0);\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"    Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0146(){
	String str =
		"public class X {\n" +
		"  W<Y<Z\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0147(){
	String str =
		"public class X {\n" +
		"  W<Y<Z>\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0148(){
	String str =
		"public class X {\n" +
		"  W<Y<Z>>\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0149(){
	String str =
		"public class X {\n" +
		"  W<Y<Z>> var\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  W<Y<<CompleteOnType:Z>>> var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0150(){
	String str =
		"public class X {\n" +
		"  W<Y<Z>> var;\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  W<Y<<CompleteOnType:Z>>> var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0151(){
	String str =
		"public class X {\n" +
		"  W<A,B,C\n" +
		"}";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  W<A, <CompleteOnType:B>, C>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0152(){
	String str =
		"public class X {\n" +
		"  W<A,B,C>\n" +
		"}";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  W<A, <CompleteOnType:B>, C>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0153(){
	String str =
		"public class X {\n" +
		"  W<A,B,C> var\n" +
		"}";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  W<A, <CompleteOnType:B>, C> var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0154(){
	String str =
		"public class X {\n" +
		"  W<A,B,C> var;\n" +
		"}";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  W<A, <CompleteOnType:B>, C> var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0155(){
	String str =
		"public class X {\n" +
		"  Y<Z>.V<W> var;\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>.V<W> var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0156(){
	String str =
		"public class X {\n" +
		"  Y<Z>.V<W> var\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>.V<W> var;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0157(){
	String str =
		"public class X {\n" +
		"  Y<Z>.V<W>\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>.V<W>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0158(){
	String str =
		"public class X {\n" +
		"  Y<Z>.V<W\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Y<<CompleteOnType:Z>>.V<W>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0159_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (W<Y<Z> ) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0159_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (W<Y<Z> ) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object a = Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0160_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    ((Y<Z>) e).foo();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0160_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    ((Y<Z>) e).foo();\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    (Y < <CompleteOnName:Z>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0161(){
	String str =
		"public class X  <T extends Z<Y>> {\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:Z>> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0162(){
	String str =
		"public class X  <T extends X.Z<Y>> {\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X.Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:X.Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "X.Z";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:X.Z>> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0163(){
	String str =
		"public class X  <T extends X<W>.Z<Y>> {\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X<W>.Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:X<W>.Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "X<W>.Z";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:X<W>.Z>> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0164(){
	String str =
		"public class X {\n" +
		"  <T extends X<W>.Z> foo() {}\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X<W>.Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:X<W>.Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "X<W>.Z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <T extends <CompleteOnType:X<W>.Z>>foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0165_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (W.Y<Z>) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0165_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (W.Y<Z>) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(W.Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object a = (W.Y < <CompleteOnName:Z>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0166_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (W<U>.Y<Z>) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0166_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    Object a = (W<U>.Y<Z>) e;\n" +
		"  }\n" +
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object a = W<U>.Y<<CompleteOnType:Z>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0167_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    List<Integer> cont=new ArrayList<Integer>();\n" +
		"    for (Integer i:cont){\n" +
		"      i.\n" +
		"    }\n" +
		"  }\n" +
		"}";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0167_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    List<Integer> cont=new ArrayList<Integer>();\n" +
		"    for (Integer i:cont){\n" +
		"      i.\n" +
		"    }\n" +
		"  }\n" +
		"}";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:i.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "i.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    List<Integer> cont;\n" +
			"    Integer i;\n" +
			"    {\n" +
			"      <CompleteOnName:i.>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0168_Diet(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    List<Integer> cont=new ArrayList<Integer>();\n" +
		"    for (Integer i:cont){\n" +
		"    }\n" +
		"    i.\n" +
		"  }\n" +
		"}";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0168_Method(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    List<Integer> cont=new ArrayList<Integer>();\n" +
		"    for (Integer i:cont){\n" +
		"    }\n" +
		"    i.\n" +
		"  }\n" +
		"}";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:i.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "i.";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    List<Integer> cont;\n" +
			"    <CompleteOnName:i.>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=71705
*/
public void test0169(){
	String str =
		"public class X {\n"+
		"  Object o;\n"+
		"  void foo(int[] a, int[] b){\n"+
		"    if(a.lenth < b.length)\n"+
		"      System.out.println();\n"+
		"  }\n"+
		"}";


	String completeBehind = "Object";
	int cursorLocation = str.indexOf("Object") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Object>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Object";
	String expectedReplacedSource = "Object";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:Object>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(int[] a, int[] b) {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=71705
*/
public void test0170(){
	String str =
		"public class X {\n"+
		"  bar\n"+
		"  void foo(){\n"+
		"    A<B\n"+
		"  }\n"+
		"}";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:bar>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void _testXXX2(){
	String str =
		"public class X extends Y. {\n" +
		"}";


	String completeBehind = "Y";
	int cursorLocation = str.indexOf("Y") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClass:Y>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X extends <CompleteOnClass:Y> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0171_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0171_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0172_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0172_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0173_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>.C) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0173_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>.C) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0174_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>.C[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0174_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>.C[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0175_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>.C<D>) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0175_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>.C<D>) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0176_Diet(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>.C<D>[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0176_Method(){
	String str =
		"public class X{\n" +
		"  public void foo() {\n" +
		"    Object o =(A<B>.C<D>[]) tmp;\n" +
		"    bar\n" +
		"  }\n" +
		"}\n" +
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    Object o;\n" +
			"    <CompleteOnName:bar>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68594
 */
public void test0177(){
	String str =
		"public class X{\n" +
		"  Stack<List<Object>> o = null;\n" +
		"}\n" +
		"\n";


	String completeBehind = "Stack";
	int cursorLocation = str.indexOf("Stack") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Stack>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Stack";
	String expectedReplacedSource = "Stack";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:Stack>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n"
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72238
 */
public void test0178(){
	String str =
		"public class X <T>{\n" +
		"  X<ZZZ<\n" +
		"}\n" +
		"\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "X<<CompleteOnType:ZZZ>>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"public class X<T> {\n" +
		"  X<<CompleteOnType:ZZZ>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n"
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72238
 */
public void test0179(){
	String str =
		"public class X <T>{\n" +
		"  X<ZZZ.\n" +
		"}\n" +
		"\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "X<<CompleteOnType:ZZZ>>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"public class X<T> {\n" +
		"  X<<CompleteOnType:ZZZ>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n"
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72238
 */
public void test0180(){
	String str =
		"public class X <T>{\n" +
		"  X<ZZZ\n" +
		"}\n" +
		"\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "X<<CompleteOnType:ZZZ>>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"public class X<T> {\n" +
		"  X<<CompleteOnType:ZZZ>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n"
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72238
 */
public void test0181(){
	String str =
		"public class X <T>{\n" +
		"  X<ZZZ>\n" +
		"}\n" +
		"\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "X<<CompleteOnType:ZZZ>>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"public class X<T> {\n" +
		"  X<<CompleteOnType:ZZZ>>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n"
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=73573
 */
public void test0182(){
	String str =
		"public class X <T>{\n" +
		"  X<\n" +
		"}\n" +
		"\n";

	String completeBehind = "X";
	int cursorLocation = str.indexOf("X<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "X";
	String expectedReplacedSource = "X";
	String expectedUnitDisplayString =
		"public class X<T> {\n" +
		"  <CompleteOnType:X>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=73573
 */
public void test0183(){
	String str =
		"public class X <T>{\n" +
		"  X<Object\n" +
		"}\n" +
		"\n";

	String completeBehind = "X";
	int cursorLocation = str.indexOf("X<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "X";
	String expectedReplacedSource = "X";
	String expectedUnitDisplayString =
		"public class X<T> {\n" +
		"  <CompleteOnType:X>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=75649
 */
public void test0184_Diet(){
	String str =
		"public class X <T>{\n" +
		"  void foo() {\n" +
		"    X<? extends String> s;\n" +
		"  }\n" +
		"}\n" +
		"\n";

	String completeBehind = "Strin";
	int cursorLocation = str.indexOf("Strin") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X<T> {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=75649
 */
public void test0184_Method(){
	String str =
		"public class X <T>{\n" +
		"  void foo() {\n" +
		"    X<? extends String> s;\n" +
		"  }\n" +
		"}\n" +
		"\n";

	String completeBehind = "Strin";
	int cursorLocation = str.indexOf("Strin") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Strin>";
	String expectedParentNodeToString = "? extends <CompleteOnType:Strin>";
	String completionIdentifier = "Strin";
	String expectedReplacedSource = "String";
	String expectedUnitDisplayString =
			"public class X<T> {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    X<? extends <CompleteOnType:Strin>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83236
 */
public void test0185(){
	String str =
		"public class Test {\n" +
		"  Boolean\n" +
		"   * some text <b>bold<i>both</i></b>\n" +
		"   */\n" +
		"  public void foo(String s) {\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "Boolean";
	int cursorLocation = str.indexOf("Boolean") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Boolean>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Boolean";
	String expectedReplacedSource = "Boolean";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  <CompleteOnType:Boolean>;\n" +
		"  some text;\n" +
		"  bold<i> both;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public void foo(String s) {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0186(){
	String str =
		"public class Test {\n" +
		"  List<? ext\n" +
		"}\n";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  List<? extends <CompleteOnKeyword:ext>>;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0187_Diet(){
	String str =
		"public class Test {\n" +
		"  void foo() {\n" +
		"    List<? ext\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0187_Method(){
	String str =
		"public class Test {\n" +
		"  void foo() {\n" +
		"    List<? ext\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    List<? extends <CompleteOnKeyword:ext>>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=80432
 */
public void test0188_Diet(){
	String str =
		"public class Test {\n" +
		"  void foo() {\n" +
		"    for(;;) {\n" +
		"      bar(toto.\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "toto.";
	int cursorLocation = str.indexOf("toto.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=80432
 */
public void test0188_Method(){
	String str =
		"public class Test {\n" +
		"  void foo() {\n" +
		"    for(;;) {\n" +
		"      bar(toto.\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "toto.";
	int cursorLocation = str.indexOf("toto.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:toto.>";
	String expectedParentNodeToString = "bar(<CompleteOnName:toto.>)";
	String completionIdentifier = "";
	String expectedReplacedSource = "toto.";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    {\n" +
			"      bar(<CompleteOnName:toto.>);\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0189_Diet(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    zzz.<String>foo(new Runtime());\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "Runtime";
	int cursorLocation = str.indexOf("Runtime") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0189_Method(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    zzz.<String>foo(new Runtime());\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "Runtime";
	int cursorLocation = str.indexOf("Runtime") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Runtime>";
	String expectedParentNodeToString = "zzz.foo(new <CompleteOnType:Runtime>())";
	String completionIdentifier = "Runtime";
	String expectedReplacedSource = "Runtime";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    zzz.foo(new <CompleteOnType:Runtime>());\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0190_Diet(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    zzz.<String>foo(var);\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "var";
	int cursorLocation = str.indexOf("var") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0190_Method(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    zzz.<String>foo(var);\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "var";
	int cursorLocation = str.indexOf("var") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:var>";
	String expectedParentNodeToString = "zzz.foo(<CompleteOnName:var>)";
	String completionIdentifier = "var";
	String expectedReplacedSource = "var";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    zzz.foo(<CompleteOnName:var>);\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0191_Diet(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    zzz.<String>foo();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "foo(";
	int cursorLocation = str.indexOf("foo(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0191_Method(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    zzz.<String>foo();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "foo(";
	int cursorLocation = str.indexOf("foo(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:zzz.foo()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "foo(";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    <CompleteOnMessageSend:zzz.foo()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0192_Diet(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    zzz.<String>foo();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "fo";
	int cursorLocation = str.indexOf("fo") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0192_Method(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    zzz.<String>foo();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "fo";
	int cursorLocation = str.indexOf("fo") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:zzz.<String>fo()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    <CompleteOnMessageSendName:zzz.<String>fo()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0193_Diet(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    new Foo<X>();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0193_Method(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    new Foo<X>();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new Foo<X>()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    <CompleteOnAllocationExpression:new Foo<X>()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0194_Diet(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    new Foo<X<X>>();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0194_Method(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    new Foo<X<X>>();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new Foo<X<X>>()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    <CompleteOnAllocationExpression:new Foo<X<X>>()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0195_Diet(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    new Foo<X<X<X>>>();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0195_Method(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    new Foo<X<X<X>>>();\n" +
		"  }\n" +
		"}\n";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new Foo<X<X<X>>>()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    <CompleteOnAllocationExpression:new Foo<X<X<X>>>()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0196(){
	String str =
		"public class Test<T> ext{\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"public class Test<T> extends <CompleteOnKeyword:ext> {\n" +
		"  {\n" +
		"  }\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0197(){
	String str =
		"public class Test<T> imp{\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "imp";
	int cursorLocation = str.indexOf("imp") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:imp>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "imp";
	String expectedReplacedSource = "imp";
	String expectedUnitDisplayString =
		"public class Test<T> extends <CompleteOnKeyword:imp> {\n" +
		"  {\n" +
		"  }\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0198(){
	String str =
		"public class Test<T> extends X ext {\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "X ext";
	int cursorLocation = str.indexOf("X ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"public class Test<T> extends <CompleteOnKeyword:ext> {\n" +
		"  {\n" +
		"  }\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0199(){
	String str =
		"public class Test<T> extends X imp {\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "X imp";
	int cursorLocation = str.indexOf("X imp") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:imp>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "imp";
	String expectedReplacedSource = "imp";
	String expectedUnitDisplayString =
		"public class Test<T> extends <CompleteOnKeyword:imp> {\n" +
		"  {\n" +
		"  }\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0200(){
	String str =
		"public interface Test<T> ext{\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"public interface Test<T> extends <CompleteOnKeyword:ext> {\n" +
		"  {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0201(){
	String str =
		"public interface Test<T> imp{\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "imp";
	int cursorLocation = str.indexOf("imp") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:imp>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "imp";
	String expectedReplacedSource = "imp";
	String expectedUnitDisplayString =
		"public interface Test<T> extends <CompleteOnKeyword:imp> {\n" +
		"  {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100302
 */
public void test0202_Diet(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    for (Entry entry : (Set<Entry>) var) {\n" +
		"      entry.\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "entry.";
	int cursorLocation = str.indexOf("entry.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100302
 */
public void test0202_Method(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    for (Entry entry : (Set<Entry>) var) {\n" +
		"      entry.\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "entry.";
	int cursorLocation = str.indexOf("entry.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:entry.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "entry.";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    Entry entry;\n" +
			"    {\n" +
			"      <CompleteOnName:entry.>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100302
 */
public void test0203_Diet(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    for (Entry entry : (ZZZ<YYY>.Set<Entry>) var) {\n" +
		"      entry.\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "entry.";
	int cursorLocation = str.indexOf("entry.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100302
 */
public void test0203_Method(){
	String str =
		"public class Test {\n" +
		"  void bar() {\n" +
		"    for (Entry entry : (ZZZ<YYY>.Set<Entry>) var) {\n" +
		"      entry.\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "entry.";
	int cursorLocation = str.indexOf("entry.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:entry.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "entry.";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    Entry entry;\n" +
			"    {\n" +
			"      <CompleteOnName:entry.>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=103148
 */
public void test0204_Diet(){
	String str =
		"public class Test {\n"+
		"	public enum MyEnum { A };\n"+
		"	public static void foo() {\n"+
		"		EnumSet.<MyEnum>of(MyEnum.A);\n"+
		"		zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public enum MyEnum {\n" +
		"    A(),\n" +
		"    <clinit>() {\n" +
		"    }\n" +
		"    public MyEnum() {\n" +
		"    }\n" +
		"  }\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public static void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=103148
 */
public void test0204_Method(){
	String str =
		"public class Test {\n"+
		"	public enum MyEnum { A };\n"+
		"	public static void foo() {\n"+
		"		EnumSet.<MyEnum>of(MyEnum.A);\n"+
		"		zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public enum MyEnum {\n" +
			"    A(),\n" +
			"    <clinit>() {\n" +
			"    }\n" +
			"    public MyEnum() {\n" +
			"    }\n" +
			"  }\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  public static void foo() {\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123514
public void test0205(){
	String str =
		"public class X {\n" +
		"  <T> HashMap<K, V>\n" +
		"}";


	String completeBehind = "HashMap<";
	int cursorLocation = str.indexOf("HashMap<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "HashMap<<CompleteOnType:>, V>";
	String completionIdentifier = "";
	String expectedReplacedSource = "K";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  HashMap<<CompleteOnType:>, V>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0206_Diet(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  Collections.<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0206_Method(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  Collections.<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:Collections.<B>zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnMessageSendName:Collections.<B>zzz()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0207_Diet(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  bar().<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0207_Method(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  bar().<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:bar().<B>zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnMessageSendName:bar().<B>zzz()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0208_Diet(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  int.<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0208_Method(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  int.<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnName:zzz>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0209_Diet(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  this.<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0209_Method(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  this.<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:this.<B>zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnMessageSendName:this.<B>zzz()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0210_Diet(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  super.<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0210_Method(){
	String str =
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  super.<B>zzz\n"+
		"	}\n"+
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:super.<B>zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnMessageSendName:super.<B>zzz()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83685
public void test0211(){
	String str =
		"public class Test{\n" +
		"  Test.\n" +
		"}\n";

	String completeBehind = "Test";
	int cursorLocation = str.indexOf("Test.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Test>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Test";
	String expectedReplacedSource = "Test";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  <CompleteOnType:Test>;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0212(){
	String str =
		"public class Test {\n" +
		"  List<? extends Obj>\n" +
		"}\n";

	String completeBehind = "Obj";
	int cursorLocation = str.indexOf("Obj") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Obj>";
	String expectedParentNodeToString = "? extends <CompleteOnType:Obj>";
	String completionIdentifier = "Obj";
	String expectedReplacedSource = "Obj";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  List<? extends <CompleteOnType:Obj>>;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0213_Diet() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar();\n" +
		"      }\n" +
		"      catch (Top<Object>.IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public boolean foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0213_Method() {

	String str =
		"public class X {\n" +
		"	public boolean foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IllegalAccessException e) {\n" +
		"         bar();\n" +
		"      }\n" +
		"      catch (Top<Object>.IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Top<Object>.IZZ>";
	String expectedParentNodeToString =
			"try\n" +
			"  {\n" +
			"    throwing();\n" +
			"  }\n" +
			"catch (IllegalAccessException e)\n" +
			"  {\n" +
			"  }\n" +
			"catch (<CompleteOnException:Top<Object>.IZZ>  )\n" +
			"  {\n" +
			"  }";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "Top<Object>.IZZ";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public boolean foo() {\n" +
			"    try\n" +
			"      {\n" +
			"        throwing();\n" +
			"      }\n" +
			"    catch (IllegalAccessException e)\n" +
			"      {\n" +
			"      }\n" +
			"    catch (<CompleteOnException:Top<Object>.IZZ>  )\n" +
			"      {\n" +
			"      }\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=197400
public void test0214_Diet() {

	String str =
		"public class X {\n" +
		"	static {\n" +
		"      <>\n" +
		"   }" +
		"}\n";

	String completeBehind = "<";
	int cursorLocation = str.lastIndexOf("<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  static {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=197400
public void test0214_Method() {

	String str =
		"public class X {\n" +
		"	static {\n" +
		"      <>\n" +
		"   }" +
		"}\n";

	String completeBehind = "<";
	int cursorLocation = str.lastIndexOf("<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	// we are not in a constructor then the completion node isn't attached to the ast
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  static {\n" +
			"  }\n" +
			"  <clinit>() {\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210273
public void test0215_Diet() {

	String str =
		"public class X {\n" +
		"	void foo() {\n" +
		"      this.<X>bar();\n" +
		"   }" +
		"}\n";

	String completeBehind = "bar(";
	int cursorLocation = str.lastIndexOf("bar(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210273
public void test0215_Method() {

	String str =
		"public class X {\n" +
		"	void foo() {\n" +
		"      this.<X>bar();\n" +
		"   }" +
		"}\n";

	String completeBehind = "bar(";
	int cursorLocation = str.lastIndexOf("bar(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:this.bar()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "bar(";
	// we are not in a constructor then the completion node isn't attached to the ast
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    <CompleteOnMessageSend:this.bar()>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0216_Diet() {

	String str =
		"public class X {\n" +
		"	Object field = new Object(){\n" +
		"		void foo(List<String> ss) {\n" +
		"			for(String s: ss){\n" +
		"				s.z\n" +
		"			}\n" +
		"		}\n" +
		"	};\n" +
		"}\n";

	String completeBehind = "s.z";
	int cursorLocation = str.lastIndexOf("s.z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:s.z>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "z";
	String expectedReplacedSource = "s.z";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Object field = new Object() {\n" +
		"    void foo(List<String> ss) {\n" +
		"      String s;\n" +
		"      {\n" +
		"        <CompleteOnName:s.z>;\n" +
		"      }\n" +
		"    }\n" +
		"  };\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207631
public void test0217_Method() {

	String str =
		"public class X {\n" +
		"	void foo() {\n" +
		"      int y = (x >> (1));\n" +
		"      foo\n" +
		"   }" +
		"}\n";

	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf("foo") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	// we are not in a constructor then the completion node isn't attached to the ast
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    int y;\n" +
			"    <CompleteOnName:foo>;\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0218_Diet() {

	String str =
		"public enum X {\n" +
		"	SUCCESS { ZZZ }\n" +
		"}\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"public enum X {\n" +
		"  SUCCESS() {\n" +
		"    <CompleteOnType:ZZZ>;\n" +
		"  },\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=274557
public void test0219_Diet() {

	String str =
		"public class X {\n" +
		"	@Annot(value=\"\")\n" +
		"	int field;\n" +
		"}\n";

	String completeBehind = "value=\"";
	int cursorLocation = str.lastIndexOf("value=\"") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompletionOnString:\"\">";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "\"\"";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(value = <CompletionOnString:\"\">)\n" + 
		"  int field;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=274557
public void test0220_Diet() {

	String str =
		"public class X {\n" +
		"	@Annot(\"\")\n" +
		"	int field;\n" +
		"}\n";

	String completeBehind = "@Annot(\"";
	int cursorLocation = str.lastIndexOf("@Annot(\"") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompletionOnString:\"\">";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "\"\"";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(value = <CompletionOnString:\"\">)\n" + 
		"  int field;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void testBug351426(){
	String str =
		"public class X<T> {\n" +
		"  void foo() {\n" +
		"    X<String> x = new X<>();\n" +
		"  }\n" +
		"}";


	String completeBehind = "new X<";
	int cursorLocation = str.indexOf("new X<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "X<<CompleteOnType:>>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class X<T> {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    X<String> x = new X<<CompleteOnType:>>();\n" +
			"  }\n" +
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void testBug351426b(){
	String str =
		"public class X<T> {\n" +
		"	static class X1<E>{}\n" +
		"  void foo() {\n" +
		"    X1<String> x = new X.X1<>();\n" +
		"  }\n" +
		"}";


	String completeBehind = "new X.X1<";
	int cursorLocation = str.indexOf("new X.X1<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "X.X1<<CompleteOnType:>>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class X<T> {\n" + 
			"  static class X1<E> {\n" + 
			"    X1() {\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public X() {\n" + 
			"  }\n" + 
			"  void foo() {\n" + 
			"    X1<String> x = new X.X1<<CompleteOnType:>>();\n" + 
			"  }\n" + 
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void testBug351426c(){
	String str =
		"public class X<T> {\n" +
		"  public X<String> foo() {\n" +
		"   return new X<>();\n" +
		"  }\n" +
		"}";


	String completeBehind = "new X<";
	int cursorLocation = str.indexOf("new X<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "X<<CompleteOnType:>>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"public class X<T> {\n" + 
			"  public X() {\n" + 
			"  }\n" + 
			"  public X<String> foo() {\n" + 
			"    return new X<<CompleteOnType:>>();\n" + 
			"  }\n" + 
			"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
}
