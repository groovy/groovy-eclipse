/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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


import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;



@SuppressWarnings({ "unchecked", "rawtypes" })
public class AnnotationCompletionParserTest extends AbstractCompletionTest {
static {
	//TESTS_NAMES= new String[]{"test0087"};
}

public AnnotationCompletionParserTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(AnnotationCompletionParserTest.class);
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.getFirstSupportedJavaVersion());
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.getFirstSupportedJavaVersion());
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.getFirstSupportedJavaVersion());
	return options;
}

public void test0001(){
	String str =
		"public @MyAnn class X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"class X {\n" +
		"  X() {\n" +
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
		"public @MyAnn interface X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"interface X {\n" +
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
		"public @MyAnn enum X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"enum X {\n" +
		"  X() {\n" +
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
public void test0004(){
	String str =
		"public @MyAnn @interface X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"@interface X {\n" +
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
		"public @MyAnn class X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"class X {\n" +
		"  X() {\n" +
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
		"public @MyAnn interface X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"interface X {\n" +
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
		"public @MyAnn enum X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"enum X {\n" +
		"  X() {\n" +
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
public void test0008(){
	String str =
		"public @MyAnn @interface X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"@interface X {\n" +
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
		"public @MyAnn\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n";

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
		"  public @MyAnn class Y {\n" +
		"  }\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @<CompleteOnType:MyAnn>\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
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
public void test0011(){
	String str =
		"public class X {\n" +
		"  public @MyAnn class Y\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @<CompleteOnType:MyAnn>\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
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
public void test0012(){
	String str =
		"public class X {\n" +
		"  public @MyAnn\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @<CompleteOnType:MyAnn>\n" +
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
public void test0013_Diet(){
	String str =
		"public class X {\n" +
		"  public void foo() {\n" +
		"    @MyAnn class Y {\n" +
		"    }\n" +
		"  }\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
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
public void test0013_Method(){
	String str =
		"public class X {\n" +
		"  public void foo() {\n" +
		"    @MyAnn class Y {\n" +
		"    }\n" +
		"  }\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    @<CompleteOnType:MyAnn>\n" +
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
public void test0014(){
	String str =
		"public @MyAnn(ZORK) class X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"class X {\n" +
		"  X() {\n" +
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
		"public @MyAnn(ZORK) class X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"class X {\n" +
		"  X() {\n" +
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
		"public @MyAnn(ZORK)\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n";

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
		"public @MyAnn(v1=\"\", v2=\"\") class X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"class X {\n" +
		"  X() {\n" +
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
		"public @MyAnn(v1=\"\", v2=\"\")) class X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n" +
		"class X {\n" +
		"  X() {\n" +
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
		"public @MyAnn(v1=\"\", v2=\"\")\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n";

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
		"public @MyAnn(v1=\"\"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n";

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
		"  @MyAnn void foo() {}\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @<CompleteOnType:MyAnn>\n" +
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
public void test0022(){
	String str =
		"public class X {\n" +
		"  @MyAnn int var;\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @<CompleteOnType:MyAnn>\n" +
		"  int var;\n" +
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
public void test0023(){
	String str =
		"public class X {\n" +
		"  void foo(@MyAnn int i) {}\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @<CompleteOnType:MyAnn>\n" +
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
public void test0024_Diet(){
	String str =
		"public class X {\n" +
		"  void foo() {@MyAnn int i}\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
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
public void test0024_Method(){
	String str =
		"public class X {\n" +
		"  void foo() {@MyAnn int i}\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    @<CompleteOnType:MyAnn>\n" +
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
public void test0025(){
	String str =
		"@Annot(foo)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(<CompleteOnAttributeName:foo>)\n" +
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
public void test0026(){
	String str =
		"public class X {\n" +
		"  @Annot(foo)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
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
public void test0027(){
	String str =
		"public class X {\n" +
		"  @Annot(foo)\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  int var;\n" +
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
public void test0028_Diet(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0028_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(<CompleteOnAttributeName:foo>)\n" +
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
public void test0029_Diet(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(foo) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(int var1) {\n" +
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
public void test0029_Method(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(foo) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar(int var1) {\n" +
			"    @Annot(<CompleteOnAttributeName:foo>)\n" +
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
public void test0030(){
	String str =
		"public class X {\n" +
		"  @Annot(foo)\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  X() {\n" +
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
public void test0031(){
	String str =
		"@Annot(foo\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(<CompleteOnAttributeName:foo>)\n" +
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
public void test0032(){
	String str =
		"public class X {\n" +
		"  @Annot(foo\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
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
public void test0033(){
	String str =
		"public class X {\n" +
		"  @Annot(foo\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  int var;\n" +
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
public void test0034_Diet(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0034_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(<CompleteOnAttributeName:foo>)\n" +
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
		"  void bar(int var1, @Annot(foo int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(int var1) {\n" +
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
		"  void bar(int var1, @Annot(foo int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar(int var1) {\n" +
			"    @Annot(<CompleteOnAttributeName:foo>)\n" +
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
public void test0036(){
	String str =
		"public class X {\n" +
		"  @Annot(foo\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  X() {\n" +
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
public void test0037(){
	String str =
		"@Annot(foo=zzz)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(<CompleteOnAttributeName:foo>)\n" +
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
public void test0038(){
	String str =
		"public class X {\n" +
		"  @Annot(foo=zzz)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
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
public void test0039(){
	String str =
		"public class X {\n" +
		"  @Annot(foo=zzz)\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  int var;\n" +
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
public void test0040_Diet(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo=zzz)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0040_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo=zzz)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(<CompleteOnAttributeName:foo>)\n" +
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
		"  void bar(int var1, @Annot(foo=zzz) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(int var1) {\n" +
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
		"  void bar(int var1, @Annot(foo=zzz) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar(int var1) {\n" +
			"    @Annot(<CompleteOnAttributeName:foo>)\n" +
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
public void test0042(){
	String str =
		"public class X {\n" +
		"  @Annot(foo=zzz)\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  X() {\n" +
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
public void test0043(){
	String str =
		"@Annot(foo=zzz\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(<CompleteOnAttributeName:foo>)\n" +
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
public void test0044(){
	String str =
		"public class X {\n" +
		"  @Annot(foo=zzz\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
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
public void test0045(){
	String str =
		"public class X {\n" +
		"  @Annot(foo=zzz\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  int var;\n" +
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
public void test0046_Diet(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo=zzz\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0046_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo=zzz\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(<CompleteOnAttributeName:foo>)\n" +
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
		"  void bar(int var1, @Annot(foo=zzz int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(int var1) {\n" +
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
		"  void bar(int var1, @Annot(foo=zzz int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar(int var1) {\n" +
			"    @Annot(<CompleteOnAttributeName:foo>)\n" +
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
public void test0048(){
	String str =
		"public class X {\n" +
		"  @Annot(foo=zzz\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
		"  }\n" +
		"  zzz X() {\n" +
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
public void test0049(){
	String str =
		"@Annot(yyy=zzz,foo)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0050(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
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
public void test0051(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo)\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  int var;\n" +
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
public void test0052_Diet(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0052_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
		"  void bar(int var1, @Annot(yyy=zzz,foo) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(int var1) {\n" +
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
		"  void bar(int var1, @Annot(yyy=zzz,foo) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar(int var1) {\n" +
			"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0054(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo)\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  X() {\n" +
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
public void test0055(){
	String str =
		"@Annot(yyy=zzz,foo\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0056(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
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
public void test0057(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  int var;\n" +
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
public void test0058_Diet(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0058_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0059_Diet(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(int var1) {\n" +
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
public void test0059_Method(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar(int var1) {\n" +
			"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0060(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  X() {\n" +
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
		"@Annot(yyy=zzz,foo=zzz)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0062(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
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
public void test0063(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz)\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  int var;\n" +
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
public void test0064_Diet(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo=zzz)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0064_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo=zzz)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0065_Diet(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo=zzz) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(int var1) {\n" +
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
public void test0065_Method(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo=zzz) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar(int var1) {\n" +
			"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0066(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz)\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  X() {\n" +
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
		"@Annot(yyy=zzz,foo=zzz\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0068(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
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
public void test0069(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  int var;\n" +
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
public void test0070_Diet(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo=zzz\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0070_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo=zzz\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0071_Diet(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo=zzz int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(int var1) {\n" +
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
public void test0071_Method(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo=zzz int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar(int var1) {\n" +
			"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
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
public void test0072(){
	String str =
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" +
		"  public X() {\n" +
		"  }\n" +
		"  zzz X() {\n" +
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
		"@Annot(zzz=yyy,f)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "f";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:f>";
	String expectedParentNodeToString = "@Annot(zzz = yyy,<CompleteOnAttributeName:f>)";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	String expectedUnitDisplayString =
		"@Annot(zzz = yyy,<CompleteOnAttributeName:f>)\n" +
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
public void test0074(){
	String str =
		"@Annot(zzz=foo)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public @Annot(zzz = <CompleteOnName:foo>) class X {\n" +
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
public void test0075(){
	String str =
		"@Annot(zzz= a && foo)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public @Annot(zzz = (a && <CompleteOnName:foo>)) class X {\n" +
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
public void test0076(){
	String str =
		"@Annot(zzz= {foo})\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(zzz = {<CompleteOnName:foo>})\n" +
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
public void test0078(){
	String str =
		"@Annot(zzz= {yyy, foo})\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(zzz = {<CompleteOnName:foo>})\n" +
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
public void test0079(){
	String str =
		"@Annot(zzz=foo\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(zzz = <CompleteOnName:foo>)\n" +
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
public void test0080(){
	String str =
		"@Annot(zzz= a && foo\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(zzz = (a && <CompleteOnName:foo>))\n" +
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
public void test0081(){
	String str =
		"@Annot(zzz= {yyy, foo}\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(zzz = {<CompleteOnName:foo>})\n" +
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
public void test0082(){
	String str =
		"@Annot(zzz= {yyy, foo\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(zzz = {<CompleteOnName:foo>})\n" +
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
public void test0083(){
	String str =
		"@Annot(zzz= a && (b || (foo && c)))\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(<CompleteOnName:foo> && c)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public @Annot(zzz = (a && (b || (<CompleteOnName:foo> && c)))) class X {\n" +
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
public void test0084(){
	String str =
		"@Annot(zzz= a && (b || (foo\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(zzz = <CompleteOnName:foo>)\n" +
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
public void test0085(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz=foo)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  @Annot(zzz = <CompleteOnName:foo>) void bar() {\n" +
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
		"public class X {\n" +
		"  @Annot(zzz= a && foo)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  @Annot(zzz = (a && <CompleteOnName:foo>)) void bar() {\n" +
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
		"  @Annot(zzz= {foo})\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = {<CompleteOnName:foo>})\n" +
		"  public X() {\n" +
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
public void test0088(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= {yyy, foo})\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = {<CompleteOnName:foo>})\n"+
		"  public X() {\n" +
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
public void test0089(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz=foo\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = <CompleteOnName:foo>)\n" +
		"  public X() {\n" +
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
public void test0090(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= a && foo\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = (a && <CompleteOnName:foo>))\n" +
		"  public X() {\n" +
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
public void test0091(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= {yyy, foo}\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = {<CompleteOnName:foo>})\n" +
		"  public X() {\n" +
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
public void test0092(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= {yyy, foo\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = {<CompleteOnName:foo>})\n" +
		"  public X() {\n" +
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
public void test0093(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= a && (b || (foo && c)))\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(<CompleteOnName:foo> && c)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  @Annot(zzz = (a && (b || (<CompleteOnName:foo> && c)))) void bar() {\n" +
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
		"  @Annot(zzz= a && (b || (foo\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = <CompleteOnName:foo>)\n" +
		"  public X() {\n" +
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
public void test0095(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz=foo)\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = <CompleteOnName:foo>) int bar;\n" +
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
public void test0096(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= a && foo)\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = (a && <CompleteOnName:foo>)) int bar;\n" +
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
		"  @Annot(zzz= {foo})\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = {<CompleteOnName:foo>})\n" +
		"  int bar;\n" +
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
public void test0098(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= {yyy, foo})\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = {<CompleteOnName:foo>})\n" +
		"  int bar;\n" +
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
public void test0099(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz=foo\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = <CompleteOnName:foo>)\n" +
		"  int bar;\n" +
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
public void test0100(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= a && foo\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = (a && <CompleteOnName:foo>))\n" +
		"  int bar;\n" +
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
public void test0101(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= {yyy, foo}\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = {<CompleteOnName:foo>})\n" +
		"  int bar;\n" +
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
public void test0102(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= {yyy, foo\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = {<CompleteOnName:foo>})\n" +
		"  int bar;\n" +
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
public void test0103(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= a && (b || (foo && c)))\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(<CompleteOnName:foo> && c)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = (a && (b || (<CompleteOnName:foo> && c)))) int bar;\n" +
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
public void test0104(){
	String str =
		"public class X {\n" +
		"  @Annot(zzz= a && (b || (foo\n" +
		"  int bar;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  @Annot(zzz = <CompleteOnName:foo>)\n" +
		"  int bar;\n" +
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
public void test0105_Diet(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz=foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0105_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz=foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = <CompleteOnName:foo>)\n" +
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
		"  void bar() {\n" +
		"    @Annot(zzz= a && foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0106_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz= a && foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = (a && <CompleteOnName:foo>))\n" +
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
		"  void bar() {\n" +
		"    @Annot(zzz= {foo})\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0107_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz= {foo})\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = {<CompleteOnName:foo>})\n" +
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
		"  void bar() {\n" +
		"    @Annot(zzz= {yyy, foo})\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0108_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz= {yyy, foo})\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = {<CompleteOnName:foo>})\n" +
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
		"  void bar() {\n" +
		"    @Annot(zzz=foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0109_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz=foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = <CompleteOnName:foo>)\n" +
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
		"  void bar() {\n" +
		"    int var;\n" +
		"    @Annot(zzz= a && foo\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0110_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    int var;\n" +
		"    @Annot(zzz= a && foo\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    int var;\n" +
			"    @Annot(zzz = (a && <CompleteOnName:foo>))\n" +
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
		"  void bar() {\n" +
		"    @Annot(zzz= {yyy, foo}\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0111_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz= {yyy, foo}\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = {<CompleteOnName:foo>})\n" +
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
		"  void bar() {\n" +
		"    @Annot(zzz= {yyy, foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0112_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz= {yyy, foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = {<CompleteOnName:foo>})\n" +
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
		"  void bar() {\n" +
		"    @Annot(zzz= a && (b || (foo && c)))\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0113_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz= a && (b || (foo && c)))\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = <CompleteOnName:foo>)\n" +
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
		"  void bar() {\n" +
		"    @Annot(zzz= a && (b || (foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0114_Method(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(zzz= a && (b || (foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = <CompleteOnName:foo>)\n" +
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
public void test0115(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz=foo) int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(@Annot(zzz = <CompleteOnName:foo>) int var) {\n" +
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
public void test0116(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz= a && foo) int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(@Annot(zzz = (a && <CompleteOnName:foo>)) int var) {\n" +
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
public void test0117(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz= {foo}) int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0118(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz= {yyy, foo}) int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0119_Diet(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz=foo int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0119_Method(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz=foo int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = <CompleteOnName:foo>)\n" +
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
		"  void bar(@Annot(zzz= a && foo int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0120_Method(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz= a && foo int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    @Annot(zzz = (a && <CompleteOnName:foo>))\n" +
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
public void test0121(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz= {yyy, foo} int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0122(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz= {yyy, foo int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
public void test0123(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz= a && (b || (foo && c))) int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(<CompleteOnName:foo> && c)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar(@Annot(zzz = (a && (b || (<CompleteOnName:foo> && c)))) int var) {\n" +
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
public void test0124(){
	String str =
		"public class X {\n" +
		"  void bar(@Annot(zzz= a && (b || (foo int var) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=148742
public void test0125(){
	String str =
		"public interface X {\n" +
		"  public void test(@TestAnnotation int testParam);\n" +
		"}";


	String completeBehind = "@TestAnnotation";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:TestAnnotation>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "TestAnnotation";
	String expectedReplacedSource = "TestAnnotation";
	String expectedUnitDisplayString =
		"public interface X {\n" +
		"  @<CompleteOnType:TestAnnotation>\n" +
		"  public void test() {\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148742
public void test0126(){
	String str =
		"public abstract class X {\n" +
		"  public abstract void test(@TestAnnotation int testParam);\n" +
		"}";


	String completeBehind = "@TestAnnotation";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:TestAnnotation>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "TestAnnotation";
	String expectedReplacedSource = "TestAnnotation";
	String expectedUnitDisplayString =
		"public abstract class X {\n" +
		"  @<CompleteOnType:TestAnnotation>\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public abstract void test();\n" +
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
public void test0127(){
	String str =
		"public class Test {\n" +
		"  public static final int zzint = 0;\n" +
		"  @ZZAnnotation({ZZ})\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "{ZZ";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@ZZAnnotation(value)>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  @ZZAnnotation(value = {<CompleteOnName:ZZ>})\n" +
		"  public static final int zzint;\n" +
		"  public Test() {\n" +
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
public void test0128(){
	String str =
		"public class Test {\n" +
		"  public static final int zzint = 0;\n" +
		"  @ZZAnnotation(value={ZZ})\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "{ZZ";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@ZZAnnotation(value)>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  @ZZAnnotation(value = {<CompleteOnName:ZZ>})\n" +
		"  public static final int zzint;\n" +
		"  public Test() {\n" +
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
public void test0129(){
	String str =
		"public class Test {\n" +
		"  public static final int zzint = 0;\n" +
		"  @ZZAnnotation({ZZ\n" +
		"}";


	String completeBehind = "{ZZ";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@ZZAnnotation(value)>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"public class Test {\n" +
		"  @ZZAnnotation(value = {<CompleteOnName:ZZ>})\n" +
		"  public static final int zzint;\n" +
		"  public Test() {\n" +
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
}
