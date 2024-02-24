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
public class EnumCompletionParserTest extends AbstractCompletionTest {
public EnumCompletionParserTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(EnumCompletionParserTest.class);
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	return options;
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83321
 */
public void test0001(){
	String str =
		"public class Completion {\n" +
		"	/*here*/\n" +
		"}\n" +
		"enum Natural {\n" +
		"	ONE;\n" +
		"}\n";

	String completeBehind = "/*here*/";
	int cursorLocation = str.indexOf("/*here*/") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"public class Completion {\n" +
		"  <CompleteOnType:>;\n" +
		"  public Completion() {\n" +
		"  }\n" +
		"}\n" +
		"enum Natural {\n" +
		"  ONE(),\n" +
		"  Natural() {\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0002(){
	String str =
		"public class Test {\n" +
		"	void foo() {\n" +
		"	  switch(c) {\n" +
		"	  	case FOO :\n" +
		"	  	  break;\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
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

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" +
		"case <CompleteOnName:FOO> :\n" +
		"    break;\n" +
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    switch (c) {\n" +
		"    case <CompleteOnName:FOO> :\n" +
		"        break;\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0003(){
	String str =
		"public class Test {\n" +
		"	void foo() {\n" +
		"	  switch(c) {\n" +
		"	  	case BAR :\n" +
		"	  	case FOO :\n" +
		"	  	  break;\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
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

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" +
		"case BAR :\n" +
		"case <CompleteOnName:FOO> :\n" +
		"    break;\n" +
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    switch (c) {\n" +
		"    case BAR :\n" +
		"    case <CompleteOnName:FOO> :\n" +
		"        break;\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0004(){
	String str =
		"public class Test {\n" +
		"	void foo() {\n" +
		"	  switch(c) {\n" +
		"	  	case BAR :\n" +
		"	  	  break;\n" +
		"	  	case FOO :\n" +
		"	  	  break;\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
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

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" +
		"case BAR :\n" +
		"    break;\n" +
		"case <CompleteOnName:FOO> :\n" +
		"    break;\n" +
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    switch (c) {\n" +
		"    case BAR :\n" +
		"        break;\n" +
		"    case <CompleteOnName:FOO> :\n" +
		"        break;\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0005(){
	String str =
		"public class Test {\n" +
		"	void foo() {\n" +
		"	  switch(c) {\n" +
		"	  	case BAR :\n" +
		"	  	  break;\n" +
		"	  	case FOO :\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
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

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" +
		"case BAR :\n" +
		"    break;\n" +
		"case <CompleteOnName:FOO> :\n" +
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    switch (c) {\n" +
		"    case BAR :\n" +
		"        break;\n" +
		"    case <CompleteOnName:FOO> :\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0006(){
	String str =
		"public class Test {\n" +
		"	void foo() {\n" +
		"	  switch(c) {\n" +
		"	  	case BAR :\n" +
		"	  	  break;\n" +
		"	  	case FOO\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
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

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" +
		"case BAR :\n" +
		"    break;\n" +
		"case <CompleteOnName:FOO> :\n" +
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      switch (c) {\n" +
		"      case BAR :\n" +
		"          break;\n" +
		"      case <CompleteOnName:FOO> :\n" +
		"      }\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0007(){
	String str =
		"public class Test {\n" +
		"	void foo() {\n" +
		"	  switch(c) {\n" +
		"	  	case BAR0 :\n" +
		"	      switch(c) {\n" +
		"	        case BAR :\n" +
		"	  	      break;\n" +
		"	  	    case FOO\n" +
		"	      }\n" +
		"	  	  break;\n" +
		"	  	case BAR2 :\n" +
		"	  	  break;\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
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

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" +
		"case BAR :\n" +
		"    break;\n" +
		"case <CompleteOnName:FOO> :\n" +
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      {\n" +
		"        switch (c) {\n" +
		"        case BAR :\n" +
		"            break;\n" +
		"        case <CompleteOnName:FOO> :\n" +
		"        }\n" +
		"      }\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0008(){
	String str =
		"public enum Test {\n" +
		"	A() {\n" +
		"	  void foo() {\n" +
		"	    zzz\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"public enum Test {\n" +
		"  A() {\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  },\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0009(){
	String str =
		"public enum Test {\n" +
		"	B,\n" +
		"	A() {\n" +
		"	  void foo() {\n" +
		"	    zzz\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"public enum Test {\n" +
		"  B(),\n" +
		"  A() {\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  },\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0010(){
	String str =
		"public enum Test {\n" +
		"	#\n" +
		"	B,\n" +
		"	A() {\n" +
		"	  void foo() {\n" +
		"	    zzz\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"public enum Test {\n" +
		"  B(),\n" +
		"  A() {\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  },\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0011(){
	String str =
		"public enum Test {\n" +
		"	B() {\n" +
		"	  void foo() {\n" +
		"	  }\n" +
		"	},\n" +
		"	A() {\n" +
		"	  void foo() {\n" +
		"	    zzz\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"public enum Test {\n" +
		"  B() {\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  },\n" +
		"  A() {\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  },\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0012(){
	String str =
		"public enum Test {\n" +
		"	#\n" +
		"	B() {\n" +
		"	  void foo() {\n" +
		"	  }\n" +
		"	},\n" +
		"	A() {\n" +
		"	  void foo() {\n" +
		"	    zzz\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"public enum Test {\n" +
		"  B() {\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  },\n" +
		"  A() {\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  },\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0013(){
	String str =
		"public enum Test {\n" +
		"	#\n" +
		"	B() {\n" +
		"	  void foo() {\n" +
		"	    #\n" +
		"	  }\n" +
		"	},\n" +
		"	A() {\n" +
		"	  void foo() {\n" +
		"	    zzz\n" +
		"	  }\n" +
		"	}\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"public enum Test {\n" +
		"  B() {\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  },\n" +
		"  A() {\n" +
		"    void foo() {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  },\n" +
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100868
public void test0014(){
	String str =
		"public enum Enum1 {\n"+
		"  A {\n"+
		"    tos\n"+
		"  };\n"+
		"}\n";

	String completeBehind = "tos";
	int cursorLocation = str.indexOf("tos") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:tos>";
	String expectedParentNodeToString =
			"() {\n" +
			"  <CompleteOnType:tos>;\n" +
			"}";
	String completionIdentifier = "tos";
	String expectedReplacedSource = "tos";
	String expectedUnitDisplayString =
		"public enum Enum1 {\n" +
		"  A() {\n" +
		"    <CompleteOnType:tos>;\n" +
		"  },\n" +
		"  public Enum1() {\n" +
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
