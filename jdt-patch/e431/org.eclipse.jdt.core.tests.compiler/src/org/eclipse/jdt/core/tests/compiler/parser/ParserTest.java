/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ParserTest extends AbstractRegressionTest {
static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 18 };
//		TESTS_RANGE = new int[] { 11, -1 };
}
public ParserTest(String name) {
	super(name);
}
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	throws\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"throws\", delete this token\n" +
		"----------\n"
	);
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws new\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	throws new\n" +
		"	^^^^^^^^^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n"
	);
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws new X\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	throws new X\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"throws\", throw expected\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	throws new X\n" +
		"	           ^\n" +
		"Syntax error, insert \"( )\" to complete Expression\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	throws new X\n" +
		"	           ^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n");
}
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	{\n" +
			"		throws\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	throws\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"throws\", delete this token\n" +
		"----------\n"
	);
}
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	{\n" +
			"		throws new\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	throws new\n" +
		"	^^^^^^^^^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n"
	);
}
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	{\n" +
			"		throws new X\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	throws new X\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"throws\", throw expected\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	throws new X\n" +
		"	           ^\n" +
		"Syntax error, insert \"( )\" to complete Expression\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	throws new X\n" +
		"	           ^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n");
}
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo()throw {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo()throw {\n" +
		"	          ^^^^^\n" +
		"Syntax error on token \"throw\", { expected\n" +
		"----------\n"
	);
}
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo()throw E {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo()throw E {\n" +
		"	          ^^^^^\n" +
		"Syntax error on token \"throw\", throws expected\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n"
	);
}
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws e\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	throws e\n" +
		"	^^^^^^^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n"
	);
}
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws e;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	throws e;\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"throws\", throw expected\n" +
		"----------\n"
	);
}
public void _test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(X, Object o, String s) {\n" +
			"	}\n" +
			"   public void bar(){}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo(X, Object o, String s) {\n" +
		"	                 ^\n" +
		"Syntax error on token \",\", . expected\n" +
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40681
 */
public void test012() {
	Hashtable nls = new Hashtable();
	nls.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo() {\n" +
			"		\"foo\".equals(\"bar\");\n" +
			"		;\n" +
			"	}\n" +
			"}\n"
		},
		null, nls,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	\"foo\".equals(\"bar\");\n" +
		"	^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	\"foo\".equals(\"bar\");\n" +
		"	             ^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40681
 */
public void test013() {
	Hashtable nls = new Hashtable();
	nls.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo() {\n" +
			"		\"foo\".equals(\"bar\");\n" +
			"		//;\n" +
			"	}\n" +
			"}\n"
		},
		null, nls,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	\"foo\".equals(\"bar\");\n" +
		"	^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	\"foo\".equals(\"bar\");\n" +
		"	             ^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47227
 */
public void test014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo() { \n" +
			"		import java.lang.*;\n" +
			"	} \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	import java.lang.*;\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"import\", delete this token\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	import java.lang.*;\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Syntax error on token(s), misplaced construct(s)\n" +
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"// some code\n" +
			"}\n" +
			"/*\n" +
			"// some comments\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	/*\n" +
		"// some comments\n" +
		"\n" +
		"	^^^^^^^^^^^^^^^^^^^^\n" +
		"Unexpected end of comment\n" +
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String s = \""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	String s = \"\n" +
		"	           ^\n" +
		"String literal is not properly closed by a double-quote\n" +
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	char c = '"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	char c = \'\n" +
		"	         ^\n" +
		"Invalid character constant\n" +
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	char c = '\\u0"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	char c = \'\\u0\n" +
		"	          ^^^\n" +
		"Invalid unicode\n" +
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=12287
 */
public void test019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void doit() {\n" +
			"		int[] foo = null;\n" +
			"		foo[0] = \n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	foo[0] = \n" +
		"	     ^\n" +
		"Syntax error, insert \"AssignmentOperator Expression\" to complete Assignment\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	foo[0] = \n" +
		"	     ^\n" +
		"Syntax error, insert \";\" to complete Statement\n" +
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=38895
 */
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"	}\n" +
			"	public static int newLibraryEntry() {\n" +

			"		if (sourceAttachmentPath != null) {\n" +
			"			if (sourceAttachmentPath.isEmpty()) { && !\n" +
			"sourceAttachmentPath.isAbsolute()) {\n" +
			"			foo();\n" +
			"		}\n" +
			"		return null;\n" +
			"	}\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"	}\n" +
			"	public void bar() {\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (sourceAttachmentPath.isEmpty()) { && !\n" +
		"	                                      ^^\n" +
		"Syntax error on token \"&&\", invalid (\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	sourceAttachmentPath.isAbsolute()) {\n" +
		"	                                   ^\n" +
		"Syntax error on token \"{\", invalid AssignmentOperator\n" +
		"----------\n"
	);
}
public void test021() {
	StringBuilder buffer = new StringBuilder();
	buffer.append("public class X {\n");
	for (int i = 0; i < 1000; i++) {
		buffer.append("\tint field_" + i + " = 0; \n");
	}
	for (int i = 0; i < 1000; i++) {
		if (i == 0)
			buffer.append("\tvoid method_" + i + "() { /* default */ } \n");
		else
			buffer.append("\tvoid method_" + i + "() { method_" + (i - 1) + "() \n");
	}
	buffer.append("}\n");

	Hashtable options = new Hashtable();
	options.put(CompilerOptions.OPTION_MaxProblemPerUnit, "10");
	this.runNegativeTest(
		new String[] {
			"X.java",
			buffer.toString()
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1003)\n" +
		"	void method_1() { method_0() \n" +
		"	                           ^\n" +
		"Syntax error, insert \"}\" to complete MethodBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1003)\n" +
		"	void method_1() { method_0() \n" +
		"	                           ^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1004)\n" +
		"	void method_2() { method_1() \n" +
		"	                           ^\n" +
		"Syntax error, insert \"}\" to complete MethodBody\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 1004)\n" +
		"	void method_2() { method_1() \n" +
		"	                           ^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 1005)\n" +
		"	void method_3() { method_2() \n" +
		"	                           ^\n" +
		"Syntax error, insert \"}\" to complete MethodBody\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 1005)\n" +
		"	void method_3() { method_2() \n" +
		"	                           ^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 1006)\n" +
		"	void method_4() { method_3() \n" +
		"	                           ^\n" +
		"Syntax error, insert \"}\" to complete MethodBody\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 1006)\n" +
		"	void method_4() { method_3() \n" +
		"	                           ^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 1007)\n" +
		"	void method_5() { method_4() \n" +
		"	                           ^\n" +
		"Syntax error, insert \"}\" to complete MethodBody\n" +
		"----------\n" +
		"10. ERROR in X.java (at line 2002)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n",
		null, // custom classpath
		true, // flush previous output dir content
		options // custom options
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156119
 */
public void test022() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"interface X {\n" +
			"    int f= 1;;\n" +
			"}"
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int f= 1;;\n" +
		"	         ^\n" +
		"Unnecessary semicolon\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156119
 */
public void test023() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"class X {\n" +
			"    int f= 1;;\n" +
			"}"
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int f= 1;;\n" +
		"	         ^\n" +
		"Unnecessary semicolon\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156119
 */
public void test024() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"interface X {\n" +
			"    int f= 1;\\u003B\n" +
			"}"
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int f= 1;\\u003B\n" +
		"	         ^^^^^^\n" +
		"Unnecessary semicolon\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=160337
 */
public void test025() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"        static class Y {\n" +
			"                public void foo(int i) {}\n" +
			"        }\n" +
			"        static Y FakeInvocationSite = new Y(){\n" +
			"                public void foo(int i) {}\n" +
			"        };\n" +
			"}"
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(int i) {}\n" +
		"	                       ^^\n" +
		"Empty block should be documented\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	public void foo(int i) {}\n" +
		"	                       ^^\n" +
		"Empty block should be documented\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=160337
 */
public void test026() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"        static class Y {\n" +
			"                public void foo(int i) {}\n" +
			"        }\n" +
			"        static Y FakeInvocationSite = new Y(){\n" +
			"                public void foo(int i) {\n" +
			"					class A {\n" +
			"						A() {}\n" +
			"						public void bar() {}\n" +
			"					}\n" +
			"					new A().bar();\n" +
			"				 }\n" +
			"        };\n" +
			"}"
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(int i) {}\n" +
		"	                       ^^\n" +
		"Empty block should be documented\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	public void bar() {}\n" +
		"	                  ^^\n" +
		"Empty block should be documented\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=173992
 */
public void test027() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.EOFException;\n" +
			"import java.io.FileNotFoundException;\n" +
			"import java.io.IOException;\n" +
			"import org.xml.sax.SAXException;\n" +
			"public class X {\n" +
        		"public void doSomething() throws FileNotFoundException, EOFException, SAXException{\n" +
        		"\n" +
        		"}\n" +
			"public void doSomethingElse() {\n" +
        		"try {\n" +
                	"	doSomething();\n" +
        		"}\n" +
       			" catch ( SAXException exception) {\n" +
			"\n" +
      			"}  \n" +
        		"catch ( FileNotFoundException exception ) {\n" +
			"\n" +
        		"}    \n" +
       			"catch (\n" +
                	"	// working before the slashes\n" +
        		") {\n" +
			"\n" +
        		"} \n" +
        		"} \n" +
        	"}\n"
        	},
		"----------\n" +
		"1. ERROR in X.java (at line 19)\n" +
		"	catch (\n" +
		"	      ^\n" +
		"Syntax error on token \"(\", FormalParameter expected after this token\n" +
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=239198
 */
public void _test028() {
	String error = (this.complianceLevel == ClassFileConstants.JDK14) ?
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Srtring bar = \"\"\"\n" +
			"    }\n" +
			"	              ^^^^\n" +
			"Text block is not properly closed with the delimiter\n" +
			"----------\n" :
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Srtring bar = \"\"\"\n" +
			"	              ^^\n" +
			"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Srtring bar = \"\"\"\n" +
			"	                ^\n" +
			"String literal is not properly closed by a double-quote\n" +
			"----------\n";
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"    public static void foo(String param) {\n" +
			"    	String foo= param;\n" +
			"    	Srtring bar = \"\"\"\n" +
			"    }\n" +
			"}"
		},
		error,
		null,
		true,
		options);
}
public void testBug485477() {
	runNegativeTest(
		new String[] {
			"T.java",
			"public class T {{\n" +
			"  Object o = T.super; // error: '.' expected\n" + // instance initializer
			"  System.out.println(o.toString());\n" +
			"}}\n" +
			"class U {\n" +
			"  Object o1;\n" +
			"  Object o2 = T.super;\n" + // field initializer
			"  U() {\n" +
			"    o1 = U.super;\n" +  // constructor
			"    System.out.println(o1.toString());\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in T.java (at line 2)\n" +
		"	Object o = T.super; // error: \'.\' expected\n" +
		"	             ^^^^^\n" +
		"Syntax error, insert \". Identifier\" to complete Expression\n" +
		"----------\n" +
		"2. ERROR in T.java (at line 7)\n" +
		"	Object o2 = T.super;\n" +
		"	              ^^^^^\n" +
		"Syntax error, insert \". Identifier\" to complete Expression\n" +
		"----------\n" +
		"3. ERROR in T.java (at line 9)\n" +
		"	o1 = U.super;\n" +
		"	       ^^^^^\n" +
		"Syntax error, insert \". Identifier\" to complete Expression\n" +
		"----------\n");
}
}
