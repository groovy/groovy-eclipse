/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
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

public class SelectionParserTest18 extends AbstractSelectionTest {
static {
//		TESTS_NUMBERS = new int[] { 53 };
		TESTS_NAMES = new String[] { "testBug486264_selectionOnLambda_expectLambdaMethod" };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(SelectionParserTest18.class, F_1_8);
}

public SelectionParserTest18(String testName) {
	super(testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424110, [1.8][hovering] Hover, F3 does not work for method reference in method invocation
public void test424110() throws JavaModelException {
	String string =
			"public class X {\n" +
			"	static F f = X::m; // [1] Works\n" +
			"	int i = fun(X::m); // [2] Does not work\n" +
			"	public static int m(int x) {\n" +
			"		return x;\n" +
			"	}\n" +
			"	private int fun(F f) {\n" +
			"		return f.foo(0);\n" +
			"	}\n" +
			"}\n" +
			"interface F {\n" +
			"	int foo(int x);\n" +
			"}\n";

	String selection = "m";

	String expectedCompletionNodeToString = "<SelectionOnReferenceExpressionName:X::m>";

	String completionIdentifier = "m";
	String expectedUnitDisplayString =
					"public class X {\n" +
					"  static F f = <SelectionOnReferenceExpressionName:X::m>;\n" +
					"  int i;\n" +
					"  <clinit>() {\n" +
					"  }\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static int m(int x) {\n" +
					"  }\n" +
					"  private int fun(F f) {\n" +
					"  }\n" +
					"}\n" +
					"interface F {\n" +
					"  int foo(int x);\n" +
					"}\n";
	String expectedReplacedSource = "X::m";
	String testName = "<select>";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	this.checkDietParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424110, [1.8][hovering] Hover, F3 does not work for method reference in method invocation
public void test424110a() throws JavaModelException {
	String string =
			"public class X {\n" +
			"	int i = fun(X::m); // [2] Does not work\n" +
			"	public static int m(int x) {\n" +
			"		return x;\n" +
			"	}\n" +
			"	private int fun(F f) {\n" +
			"		return f.foo(0);\n" +
			"	}\n" +
			"}\n" +
			"interface F {\n" +
			"	int foo(int x);\n" +
			"}\n";

	String selection = "m";

	String expectedCompletionNodeToString = "<SelectionOnReferenceExpressionName:X::m>";

	String completionIdentifier = "m";
	String expectedUnitDisplayString =
					"public class X {\n" +
					"  int i = fun(<SelectionOnReferenceExpressionName:X::m>);\n" +
					"  public X() {\n" +
					"  }\n" +
					"  public static int m(int x) {\n" +
					"  }\n" +
					"  private int fun(F f) {\n" +
					"  }\n" +
					"}\n" +
					"interface F {\n" +
					"  int foo(int x);\n" +
					"}\n";
	String expectedReplacedSource = "X::m";
	String testName = "<select>";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	this.checkDietParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430572, [1.8] CCE on hovering over 'super' in lambda expression
public void test430572() throws JavaModelException {
	String string =
			"@FunctionalInterface\n" +
			"interface FI {\n" +
			"	default int getID() {\n" +
			"		return 11;\n" +
			"	}\n" +
			"	void print();\n" +
			"}\n" +
			"class T {\n" +
			"	FI f2 = () -> System.out.println(super.toString());\n" +
			"}\n";

	String selection = "super";

	String expectedCompletionNodeToString = "<SelectOnSuper:super>";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
					"@FunctionalInterface interface FI {\n" +
					"  default int getID() {\n" +
					"  }\n" +
					"  void print();\n" +
					"}\n" +
					"class T {\n" +
					"  FI f2 = () -> System.out.println(<SelectOnSuper:super>.toString());\n" +
					"  T() {\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "super";
	String testName = "<select>";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	this.checkDietParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=476693
public void test476693() throws JavaModelException {
	String string =
			"import static java.util.stream.Collectors.toList;\n" +
			"import java.util.List;\n" +
			"import java.util.Spliterator;\n" +
			"import java.util.stream.Stream;\n" +
			"interface Seq<T> extends Stream<T>, Iterable<T> {\n" +
			"    @Override\n" +
			"    default Spliterator<T> spliterator() {\n" +
			"        return Iterable.super.spliterator();\n" +
			"    }\n" +
			"}\n" +
			"interface Tuple2<T1, T2> {}\n" +
			"interface Tuple3<T1, T2, T3> {}\n" +
			"\n" +
			"public class Test<T1, T2, T3> {\n" +
			"    <T1, T2> Seq<Tuple2<T1, T2>> m(Stream<T1> arg1, Stream<T2> arg2) {\n" +
			"        System.out.println(\"m1\"); return null;\n" +
			"    }\n" +
			"    <T1, T2> Seq<Tuple2<T1, T2>> m(Seq<T1> arg1, Seq<T2> arg2){\n" +
			"        System.out.println(\"m3\"); return null;\n" +
			"    }\n" +
			"    <T1, T2, T3> void m(Seq<T1> c1, Seq<T2> c2, Seq<T3> c3) {\n" +
			"            // Click F3 on the m() call. This will jump to m1, erroneously\n" +
			"            List<Tuple2<T1, T2>> l = m(c1, c2).collect(toList());\n" +
			"            System.out.println(\"Hello\"); // This shouldn't appear in the selection parse tree\n" +
			"    }\n" +
			"}";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:m(c1, c2)>";

	String completionIdentifier = "m";
	String expectedUnitDisplayString =
					"import static java.util.stream.Collectors.toList;\n" +
					"import java.util.List;\n" +
					"import java.util.Spliterator;\n" +
					"import java.util.stream.Stream;\n" +
					"interface Seq<T> extends Stream<T>, Iterable<T> {\n" +
					"  default @Override Spliterator<T> spliterator() {\n" +
					"  }\n" +
					"}\n" +
					"interface Tuple2<T1, T2> {\n" +
					"}\n" +
					"interface Tuple3<T1, T2, T3> {\n" +
					"}\n" +
					"public class Test<T1, T2, T3> {\n" +
					"  public Test() {\n" +
					"  }\n" +
					"  <T1, T2>Seq<Tuple2<T1, T2>> m(Stream<T1> arg1, Stream<T2> arg2) {\n" +
					"  }\n" +
					"  <T1, T2>Seq<Tuple2<T1, T2>> m(Seq<T1> arg1, Seq<T2> arg2) {\n" +
					"  }\n" +
					"  <T1, T2, T3>void m(Seq<T1> c1, Seq<T2> c2, Seq<T3> c3) {\n" +
					"    List<Tuple2<T1, T2>> l = <SelectOnMessageSend:m(c1, c2)>.collect(toList());\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "m(c1, c2)";
	String testName = "<select>";

	int selectionStart = string.indexOf("m(c1, c2)");
	int selectionEnd = selectionStart;

	this.checkMethodParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
}
public void test495912() {
	String string =
			"package xy;\n" +
			"public class Test {\n" +
			"	{\n" +
			"		Runnable r = () -> {\n" +
			"		      Integer i= 1;\n" +
			"		      byte b= i.byteValue();\n" +
			"		      if (true) {\n" +
			"		          if (false) {\n" +
			"		          }\n" +
			"		      }\n" +
			"		      String s= new String();\n" +
			"		};\n" +
			"	}\n" +
			"    public void foo(Runnable r) {\n" +
			"    }\n" +
			"}";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:i.byteValue()>";

	String completionIdentifier = "byteValue";
	String expectedUnitDisplayString =
					"package xy;\n" +
					"public class Test {\n" +
					"  {\n" +
					"    {\n" +
					"      Runnable r = () ->       {\n" +
					"        Integer i;\n" +
					"        byte b = <SelectOnMessageSend:i.byteValue()>;\n" +
					"        if (true)\n" +
					"            {\n" +
					"              if (false)\n" +
					"                  {\n" +
					"                  }\n" +
					"            }\n" +
					"        String s;\n" +
					"      };\n" +
					"    }\n" +
					"  }\n" +
					"  public Test() {\n" +
					"  }\n" +
					"  public void foo(Runnable r) {\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "i.byteValue()";
	String testName = "<select>";

	int selectionStart = string.indexOf("byteValue");
	int selectionEnd = selectionStart + completionIdentifier.length() - 1;

	this.checkMethodParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
}
public void test495912a() {
	String string =
			"package xy;\n" +
			"public class Test {\n" +
			"	{\n" +
			"		Runnable r = () -> {\n" +
			"		      Integer i= 1;\n" +
			"		      byte b= i.byteValue();\n" +
			"		      if (true) {\n" +
			"		          if (false) {\n" +
			"		          }\n" +
			"		      }\n" +
			"		      for (int i1 = 0; i1 < 42; i1++) {\n" +
			"		      }\n" +
			"		};\n" +
			"	}\n" +
			"    public void foo(Runnable r) {\n" +
			"    }\n" +
			"}";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:i.byteValue()>";

	String completionIdentifier = "byteValue";
	String expectedUnitDisplayString =
					"package xy;\n" +
					"public class Test {\n" +
					"  {\n" +
					"    {\n" +
					"      Runnable r = () ->       {\n" +
					"        Integer i;\n" +
					"        byte b = <SelectOnMessageSend:i.byteValue()>;\n" +
					"        if (true)\n" +
					"            {\n" +
					"              if (false)\n" +
					"                  {\n" +
					"                  }\n" +
					"            }\n" +
					"        for (int i1;; (i1 < 42); i1 ++) \n" +
					"          {\n" +
					"          }\n" +
					"      };\n" +
					"    }\n" +
					"  }\n" +
					"  public Test() {\n" +
					"  }\n" +
					"  public void foo(Runnable r) {\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "i.byteValue()";
	String testName = "<select>";

	int selectionStart = string.indexOf("byteValue");
	int selectionEnd = selectionStart + completionIdentifier.length() - 1;

	this.checkMethodParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
}
public void test495912b() {
	String string =
			"package xy;\n" +
			"import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;\n" +
			"import org.eclipse.e4.ui.model.application.ui.basic.MPart;\n" +
			"import org.eclipse.swt.widgets.Table;\n" +
			"import org.eclipse.ui.IWorkbenchWindow;\n" +
			"public abstract class CycleViewHandler extends CycleBaseHandler {\n" +
			"	@Override\n" +
			"	protected void addItems(Table table, WorkbenchPage page) {\n" +
			"		List<MPart> parts = null;\n" +
			"		parts.stream().sorted((firstPart, secondPart) -> {\n" +
			"			Long firstPartActivationTime = (Long) firstPart.getTransientData()\n" +
			"					.getOrDefault(PartServiceImpl.PART_ACTIVATION_TIME, Long.MIN_VALUE);\n" +
			"			Long secondPartActivationTime = (Long) secondPart.getTransientData()\n" +
			"					.getOrDefault(PartServiceImpl.PART_ACTIVATION_TIME, Long.MIN_VALUE);\n" +
			"			return 0;\n" +
			"		}).forEach(part -> {\n" +
			"			if (true) {\n" +
			"				if (true) {\n" +
			"				}\n" +
			"			} \n" +
			"			else {\n" +
			"				IWorkbenchWindow iwbw = page.getWorkbenchWindow();\n" +
			"				if (true){\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}";

	String expectedSelectionNodeToString = "<SelectOnMessageSend:firstPart.getTransientData()>";

	String selectionIdentifier = "getTransientData";
	String expectedUnitDisplayString =
			"package xy;\n" +
					"import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;\n" +
					"import org.eclipse.e4.ui.model.application.ui.basic.MPart;\n" +
					"import org.eclipse.swt.widgets.Table;\n" +
					"import org.eclipse.ui.IWorkbenchWindow;\n" +
					"public abstract class CycleViewHandler extends CycleBaseHandler {\n" +
					"  public CycleViewHandler() {\n" +
					"  }\n" +
					"  protected @Override void addItems(Table table, WorkbenchPage page) {\n" +
					"    List<MPart> parts;\n" + // this is the missing part without the fix in RecoveredMethod#add(Block....)
					"    parts.stream().sorted((<no type> firstPart, <no type> secondPart) -> {\n" +
					"  Long firstPartActivationTime = (Long) <SelectOnMessageSend:firstPart.getTransientData()>.getOrDefault(PartServiceImpl.PART_ACTIVATION_TIME, Long.MIN_VALUE);\n" +
					"  Long secondPartActivationTime;\n" +
					"  return 0;\n" +
					"}).forEach((<no type> part) -> {\n" +
					"  if (true)\n" +
					"      {\n" +
					"        if (true)\n" +
					"            {\n" +
					"            }\n" +
					"      }\n" +
					"  else\n" +
					"      {\n" +
					"        IWorkbenchWindow iwbw;\n" +
					"        if (true)\n" +
					"            {\n" +
					"            }\n" +
					"      }\n" +
					"});\n" +
					"  }\n" +
					"}\n";
	String expectedReplacedSource = "firstPart.getTransientData()";
	String testName = "<select>";

	int selectionStart = string.indexOf("getTransientData");
	int selectionEnd = selectionStart + selectionIdentifier.length() - 1;

	this.checkMethodParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedSelectionNodeToString,
			expectedUnitDisplayString,
			selectionIdentifier,
			expectedReplacedSource,
			testName);
}
public void testBug486264_selectionOnLambda_expectLambdaMethod() {
	String string =
			"package xy;\n" +
			"import java.util.stream.*;\n" +
			"public class Lambda {\n" +
			"	public static void foo() {\n" +
			"		Stream.of(\"1\").filter(t -> t.length() > 1).collect(Collectors.toList());\n" +
			"	}\n" +
			"	\n" +
			"}\n";

	String expectedSelectionNodeToString = "<SelectOnLambdaExpression:(<no type> t) -> (t.length() > 1))>";

	String selectionIdentifier = "t -> t.length() > 1";
	String expectedUnitDisplayString =
			"package xy;\n"
			+ "import java.util.stream.*;\n"
			+ "public class Lambda {\n"
			+ "  public Lambda() {\n"
			+ "  }\n"
			+ "  public static void foo() {\n"
			+ "    Stream.of(\"1\").filter(<SelectOnLambdaExpression:(<no type> t) -> (t.length() > 1))>).collect(Collectors.toList());\n"
			+ "  }\n"
			+ "}\n"
			+ "";
	String expectedReplacedSource = "t -> t.length() > 1";
	String testName = "<select>";

	int selectionStart = string.indexOf("t -> t.length() > 1");
	int selectionEnd = selectionStart + selectionIdentifier.length() - 1;

	this.checkMethodParse(
			string.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedSelectionNodeToString,
			expectedUnitDisplayString,
			"<NONE>",
			expectedReplacedSource,
			testName);
}
}
