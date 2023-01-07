/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

import java.util.Locale;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class SyntaxErrorTest extends AbstractCompilerTest {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

public SyntaxErrorTest(String testName){
	super(testName);
}
public void checkParse(
	char[] source,
	String expectedSyntaxErrorDiagnosis,
	String testName) {

	/* using regular parser in DIET mode */
	Parser parser =
		new Parser(
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				new CompilerOptions(getCompilerOptions()),
				new DefaultProblemFactory(Locale.getDefault())),
			optimizeStringLiterals);
	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

	parser.parse(sourceUnit, compilationResult);

	StringBuilder buffer = new StringBuilder(100);
	if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
		CategorizedProblem[] problems = compilationResult.getAllProblems();
		int count = problems.length;
		int problemCount = 0;
		char[] unitSource = compilationResult.compilationUnit.getContents();
		for (int i = 0; i < count; i++) {
			if (problems[i] != null) {
				if (problemCount == 0)
					buffer.append("----------\n");
				problemCount++;
				buffer.append(problemCount + (problems[i].isError() ? ". ERROR" : ". WARNING"));
				buffer.append(" in " + new String(problems[i].getOriginatingFileName()).replace('/', '\\'));
				try {
					buffer.append(((DefaultProblem)problems[i]).errorReportSource(unitSource));
					buffer.append("\n");
					buffer.append(problems[i].getMessage());
					buffer.append("\n");
				} catch (Exception e) {
				}
				buffer.append("----------\n");
			}
		}
	}
	String computedSyntaxErrorDiagnosis = buffer.toString();
 	//System.out.println(Util.displayString(computedSyntaxErrorDiagnosis));
	assertEquals(
		"Invalid syntax error diagnosis" + testName,
		Util.convertToIndependantLineDelimiter(expectedSyntaxErrorDiagnosis),
		Util.convertToIndependantLineDelimiter(computedSyntaxErrorDiagnosis));
}
/*
 * Should diagnose parenthesis mismatch
 */
public void test01() {

	String s =
		"public class X {								\n"+
		" public void solve(){							\n"+
		"												\n"+
		"  X[] results = new X[10];						\n"+
		"  for(int i = 0; i < 10; i++){					\n"+
		"   X result = results[i];						\n"+
		"   boolean found = false;						\n"+
		"   for(int j = 0; j < 10; j++){				\n"+
		"    if (this.equals(result.documentName){		\n"+
		"     found = true;								\n"+
		"     break;									\n"+
		"    }											\n"+
		"   }											\n"+
		"  }											\n"+
		"  return andResult;							\n"+
		" }												\n"+
		"}												\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <parenthesis mismatch> (at line 9)\n" +
		"	if (this.equals(result.documentName){		\n" +
		"	                                   ^\n" +
		"Syntax error, insert \") Statement\" to complete BlockStatements\n" +
		"----------\n";

	String testName = "<parenthesis mismatch>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
/*
 * Should diagnose brace mismatch
 */
public void test02() {

	String s =
		"class Bar {			\n"+
		"	Bar() {				\n"+
		"		this(fred().x{);\n"+
		"	}					\n"+
		"}						\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <brace mismatch> (at line 3)\n" +
		"	this(fred().x{);\n" +
		"	             ^\n" +
		"Syntax error on token \"{\", delete this token\n" +
		"----------\n";

	String testName = "<brace mismatch>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
/*
 * Should diagnose parenthesis mismatch
 */
public void test03() {

	String s =
		"public class X { // should complain	\n"+
		"	int foo(							\n"+
		"		[ arg1, 						\n"+
		"		{ arg2, ]						\n"+
		"		  arg3, 						\n"+
		"	){									\n"+
		"	}									\n"+
		"}										\n";

	String expectedSyntaxErrorDiagnosis =
			"----------\n" +
			"1. ERROR in <parenthesis mismatch> (at line 3)\n" +
			"	[ arg1, 						\n" +
			"	^\n" +
			"Syntax error on token \"[\", byte expected\n" +
			"----------\n" +
			"2. ERROR in <parenthesis mismatch> (at line 4)\n" +
			"	{ arg2, ]						\n" +
			"	^\n" +
			"Syntax error on token \"{\", byte expected\n" +
			"----------\n" +
			"3. ERROR in <parenthesis mismatch> (at line 4)\n" +
			"	{ arg2, ]						\n" +
			"	        ^\n" +
			"Syntax error on token \"]\", byte expected\n" +
			"----------\n" +
			"4. ERROR in <parenthesis mismatch> (at line 5)\n" +
			"	arg3, 						\n" +
			"	    ^\n" +
			"Syntax error on token \",\", FormalParameter expected after this token\n" +
			"----------\n";

	String testName = "<parenthesis mismatch>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
/*
 * Should not diagnose parenthesis mismatch
 */
public void test04() {

	String s =
		"public class X { // should not complain	\n"+
		"	int foo(								\n"+
		"		{ arg1, 							\n"+
		"		{ arg2, }							\n"+
		"		  arg3, }							\n"+
		"	){										\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <no parenthesis mismatch> (at line 2)\n" +
		"	int foo(								\n" +
		"	       ^\n" +
		"Syntax error on token \"(\", = expected\n" +
		"----------\n" +
		"2. ERROR in <no parenthesis mismatch> (at line 5)\n" +
		"	arg3, }							\n" +
		"	^^^^\n" +
		"Syntax error on token \"arg3\", delete this token\n" +
		"----------\n" +
		"3. ERROR in <no parenthesis mismatch> (at line 6)\n" +
		"	){										\n" +
		"	^\n" +
		"Syntax error on token \")\", ; expected\n" +
		"----------\n";

	String testName = "<no parenthesis mismatch>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=61189
public void test05() {

	String s =
		"public class X {							\n"+
		"	public void foo() {						\n"+
		"		(X) foo(); 							\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n"+
		"1. ERROR in <test> (at line 3)\n"+
		"	(X) foo(); 							\n"+
		"	  ^\n"+
		"Syntax error, insert \"AssignmentOperator Expression\" to complete Assignment\n"+
		"----------\n"+
		"2. ERROR in <test> (at line 3)\n"+
		"	(X) foo(); 							\n"+
		"	  ^\n"+
		"Syntax error, insert \";\" to complete BlockStatements\n"+
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=61189
public void test06() {

	String s =
		"public class X { 							\n"+
		"	public void foo(int i) {				\n"+
		"		i; 									\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
			"----------\n" +
			"1. ERROR in <test> (at line 3)\n" +
			"	i; 									\n" +
			"	^\n" +
			"Syntax error, insert \"VariableDeclarators\" to complete LocalVariableDeclaration\n" +
			"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133292
public void test07() {

	String s =
		"public class X { 											\n"+
		"	java.lang.Object o[] = { new String(\"SUCCESS\") ; };	\n"+
		"}															\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n"+
		"1. ERROR in <test> (at line 2)\n"+
		"	java.lang.Object o[] = { new String(\"SUCCESS\") ; };	\n"+
		"	                                               ^\n"+
		"Syntax error on token \";\", , expected\n"+
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133292
public void test08() {

	String s =
		"public class X { 											\n"+
		"	Object o[] = { new String(\"SUCCESS\") ; };				\n"+
		"}															\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n"+
		"1. ERROR in <test> (at line 2)\n"+
		"	Object o[] = { new String(\"SUCCESS\") ; };				\n"+
		"	                                     ^\n"+
		"Syntax error on token \";\", , expected\n"+
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133292
public void test09() {

	String s =
		"public class X { 												\n"+
		"	void foo() {												\n"+
		"		java.lang.Object o[] = { new String(\"SUCCESS\") ; };	\n"+
		"	}															\n"+
		"}																\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n"+
		"1. ERROR in <test> (at line 3)\n"+
		"	java.lang.Object o[] = { new String(\"SUCCESS\") ; };	\n"+
		"	                                               ^\n"+
		"Syntax error on token \";\", , expected\n"+
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133292
public void test10() {

	String s =
		"public class X { 												\n"+
		"	void foo() {												\n"+
		"		Object o[] = { new String(\"SUCCESS\") ; };				\n"+
		"	}															\n"+
		"}																\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n"+
		"1. ERROR in <test> (at line 3)\n"+
		"	Object o[] = { new String(\"SUCCESS\") ; };				\n"+
		"	                                     ^\n"+
		"Syntax error on token \";\", , expected\n"+
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test11() {

	String s =
		"package a;										\n"+
		"public interface Test {						\n"+
		"  public void myMethod()						\n"+
		"}												\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n"+
		"1. ERROR in <test> (at line 3)\n"+
		"	public void myMethod()						\n"+
		"	                     ^\n"+
		"Syntax error, insert \";\" to complete MethodDeclaration\n"+
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test12() {

	String s =
		"package a;										\n"+
		"public interface Test {						\n"+
		"  public void myMethod()						\n"+
		"    System.out.println();						\n"+
		"}												\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n"+
		"1. ERROR in <test> (at line 3)\n"+
		"	public void myMethod()						\n"+
		"	                     ^\n"+
		"Syntax error on token \")\", { expected after this token\n"+
		"----------\n"+
		"2. ERROR in <test> (at line 5)\n"+
		"	}												\n"+
		"	^\n"+
		"Syntax error, insert \"}\" to complete InterfaceBody\n"+
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=221266
public void test13() {

	String s =
		"package a;										\n"+
		"public class Test {							\n"+
		"  public void foo() {							\n"+
		"    foo(a  \"\\\"\");							\n"+
		"  }											\n"+
		"}												\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n"+
		"1. ERROR in <test> (at line 4)\n"+
		"	foo(a  \"\\\"\");							\n"+
		"	       ^^^^\n"+
		"Syntax error on token \"\"\\\"\"\", delete this token\n"+
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=212713
public void test14() {

	String s =
		"public interface Test {\n"+
		"  static {  }\n"+
		"  {         }\n"+
		"}\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test> (at line 2)\n" +
		"	static {  }\n" +
		"	       ^^^^\n" +
		"The interface Test cannot define an initializer\n" +
		"----------\n" +
		"2. ERROR in <test> (at line 3)\n" +
		"	{         }\n" +
		"	^^^^^^^^^^^\n" +
		"The interface Test cannot define an initializer\n" +
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210419
public void test15() {

	String s =
		"package bug;\n" +
		"public class Test {\n" +
		"  static int X;\n" +
		"  String field = { String str;\n" +
		"      switch (X) {\n" +
		"        case 0:\n" +
		"          str = \"zero\";\n" +
		"          break;\n" +
		"        default:\n" +
		"          str = \"other\";\n" +
		"          break;\n" +
		"      }\n" +
		"      this.field = str;\n" +
		"  };\n" +
		"  public static void main(String[] args) {\n" +
		"    System.out.println(new Test().field);\n" +
		"  }\n" +
		"}\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test> (at line 4)\n" +
		"	String field = { String str;\n" +
		"	               ^^^^^^^^\n" +
		"Syntax error on token(s), misplaced construct(s)\n" +
		"----------\n" +
		"2. ERROR in <test> (at line 4)\n" +
		"	String field = { String str;\n" +
		"	                           ^\n" +
		"Syntax error on token \";\", { expected after this token\n" +
		"----------\n";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
}
