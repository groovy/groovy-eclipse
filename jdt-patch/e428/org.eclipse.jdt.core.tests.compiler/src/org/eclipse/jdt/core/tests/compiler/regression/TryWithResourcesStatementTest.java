/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation and others.
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
 *     Stephan Herrmann - Contributions for
 *     							bug 358827 - [1.7] exception analysis for t-w-r spoils null analysis
 *     							bug 349326 - [1.7] new warning for missing try-with-resources
 *     							bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TryWithResourcesStatementTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test380112e"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryWithResourcesStatementTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
// Test resource type related errors
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (int i = 0) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (int i = 0) {\n" +
		"	     ^^^\n" +
		"The resource type int does not implement java.lang.AutoCloseable\n" +
		"----------\n");
}
// Test resource type related errors
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (int[] tab = {}) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (int[] tab = {}) {\n" +
		"	     ^^^^^\n" +
		"The resource type int[] does not implement java.lang.AutoCloseable\n" +
		"----------\n");
}
// Test that resource type could be interface type.
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable{\n" +
			"	public void method1(){\n" +
			"		try (AutoCloseable a = new X()) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X implements AutoCloseable{\n" +
		"	             ^\n" +
		"The type X must implement the inherited abstract method AutoCloseable.close()\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	try (AutoCloseable a = new X()) {\n" +
		"	                   ^\n" +
		"Unhandled exception type Exception thrown by automatic close() invocation on a\n" +
		"----------\n");
}
// Type resource type related errors
public void test003a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y y = new Y()) { \n" +
			"			System.out.println();\n" +
			"		} catch (Exception e) {\n" +
			"		} finally {\n" +
			"           Zork z;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements Managed {\n" +
			"    public void close () throws Exception {\n" +
			"    }\n" +
			"}\n" +
			"interface Managed extends AutoCloseable {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
// Scope, visibility related tests.
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) throws IOException {\n" +
			"		int i = 0;\n" +
			"		try (LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" +
			"			String s;\n" +
			"			int i = 0;\n" +
			"			while ((s = reader.readLine()) != null) {\n" +
			"				System.out.println(s);\n" +
			"				i++;\n" +
			"			}\n" +
			"			System.out.println(\"\" + i + \" lines\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	int i = 0;\n" +
		"	    ^\n" +
		"Duplicate local variable i\n" +
		"----------\n");
}
//Scope, visibility related tests.
public void test004a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) throws IOException {\n" +
			"		try (LineNumberReader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" +
			"			String s;\n" +
			"			int r = 0;\n" +
			"			while ((s = r.readLine()) != null) {\n" +
			"				System.out.println(s);\n" +
			"				r++;\n" +
			"			}\n" +
			"			System.out.println(\"\" + r + \" lines\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	int r = 0;\n" +
		"	    ^\n" +
		"Duplicate local variable r\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	while ((s = r.readLine()) != null) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"Cannot invoke readLine() on the primitive type int\n" +
		"----------\n");
}
// check that resources are implicitly final
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) throws IOException {\n" +
			"		try (Reader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" +
			"			r = new FileReader(args[0]);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	r = new FileReader(args[0]);\n" +
		"	^\n" +
		"The resource r of a try-with-resources statement cannot be assigned\n" +
		"----------\n");
}
//check that try statement can be empty
public void test006() {
	this.runNegativeTest( // cannot be a conform test as this triggers an AIOOB.
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) throws IOException {\n" +
			"		try (Reader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" +
			"		} catch(Zork z) {" +
			"       }\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	} catch(Zork z) {       }\n" +
		"	        ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//check that resources are implicitly final but they can be explicitly final
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) throws IOException {\n" +
			"		try (final Reader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" +
			"			r = new FileReader(args[0]);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	r = new FileReader(args[0]);\n" +
		"	^\n" +
		"The resource r of a try-with-resources statement cannot be assigned\n" +
		"----------\n");
}
// resource type tests
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y [] i = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Y [] i = null) {\n" +
		"	     ^^^^\n" +
		"The resource type Y[] does not implement java.lang.AutoCloseable\n" +
		"----------\n");
}
// Resource Type tests
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i [] = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Y i [] = null) {\n" +
		"	     ^\n" +
		"The resource type Y[] does not implement java.lang.AutoCloseable\n" +
		"----------\n");
}
// Scope, visibility tests
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(int p){\n" +
			"       int k;\n" +
			"		try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" +
		"	                      ^\n" +
		"Duplicate local variable i\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" +
		"	                                     ^\n" +
		"Duplicate local variable p\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" +
		"	                                                    ^\n" +
		"Duplicate local variable k\n" +
		"----------\n");
}
// Scope, visibility tests
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"       catch (Exception e) {\n" +
			"           System.out.println(i);\n" +
			"       }\n" +
			"       finally {\n" +
			"           System.out.println(p);\n" +
			"       }\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	System.out.println(i);\n" +
		"	                   ^\n" +
		"i cannot be resolved to a variable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	System.out.println(p);\n" +
		"	                   ^\n" +
		"p cannot be resolved to a variable\n" +
		"---" +
		"-------\n");
}
// Scope, visibility related tests.
public void test012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" +
			"           try {\n" +
			"			    System.out.println();\n" +
			"           } catch (Exception i) {\n" +
			"           }\n" +
			"		}\n" +
			"       catch (Exception e) {\n" +
			"           System.out.println(i);\n" +
			"       }\n" +
			"       finally {\n" +
			"           System.out.println(p);\n" +
			"       }\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	} catch (Exception i) {\n" +
		"	                   ^\n" +
		"Duplicate parameter i\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	System.out.println(i);\n" +
		"	                   ^\n" +
		"i cannot be resolved to a variable\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 13)\n" +
		"	System.out.println(p);\n" +
		"	                   ^\n" +
		"p cannot be resolved to a variable\n" +
		"----------\n");
}
// Shadowing behavior tests
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"	try (Y y = new Y(); Y p = new Y()) {\n" +
			"	    X x = new X() {\n" +
			"		      public void foo(int p) {\n" +
			"                         try {\n" +
			"		             System.out.println();\n" +
			"		          } catch (Exception y) {\n" +
			"		          }\n" +
			"		       }\n" +
			"	           };\n" +
			"	} finally {\n" +
			"            System.out.println(y);\n" +
			"	}\n" +
			"   }\n" +
			"}\n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() {\n" +
			"		    System.out.println();\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	public void foo(int p) {\n" +
		"	                    ^\n" +
		"The parameter p is hiding another local variable defined in an enclosing scope\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	} catch (Exception y) {\n" +
		"	                   ^\n" +
		"The parameter y is hiding another local variable defined in an enclosing scope\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 13)\n" +
		"	System.out.println(y);\n" +
		"	                   ^\n" +
		"y cannot be resolved to a variable\n" +
		"----------\n");
}
// Test for unhandled exceptions
public void test014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try (Y y = new Y();) {\n" +
			"           if (y == null) {}\n" +
			"           Y why = new Y();\n" +
			"		    System.out.println(\"Try block\");\n" +
			"		} finally {\n" +
			"		    System.out.println(\"Finally block\");\n" +
			"		}\n" +
			"	}\n" +
			"} \n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws WeirdException {\n" +
			"		throw new WeirdException();\n" +
			"	}\n" +
			"	public void close() {\n" +
			"		    System.out.println(\"Closing resource\");\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class WeirdException extends Throwable {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Y y = new Y();) {\n" +
		"	           ^^^^^^^\n" +
		"Unhandled exception type WeirdException\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (y == null) {}\n" +
		"	               ^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	Y why = new Y();\n" +
		"	  ^^^\n" +
		"Resource leak: 'why' is never closed\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 5)\n" +
		"	Y why = new Y();\n" +
		"	        ^^^^^^^\n" +
		"Unhandled exception type WeirdException\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 22)\n" +
		"	class WeirdException extends Throwable {}\n" +
		"	      ^^^^^^^^^^^^^^\n" +
		"The serializable class WeirdException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n",
		null, true, options);
}
// Resource nullness tests
public void test015() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try (Y y = new Y();) {\n" +
			"           if (y == null)\n" +
			"				{}\n" +
			"		}\n" +
			"	}\n" +
			"} \n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() {\n" +
			"	}\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	{}\n" +
		"	^^\n" +
		"Dead code\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// Dead code tests, resource nullness, unhandled exception tests
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try (Y y = new Y();) {\n" +
			"           if (y == null) {}\n" +
			"           Y why = new Y();\n" +
			"		    System.out.println(\"Try block\");\n" +
			"		}\n" +
			"	}\n" +
			"} \n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws WeirdException {\n" +
			"		throw new WeirdException();\n" +
			"	}\n" +
			"	public void close() {\n" +
			"		    System.out.println(\"Closing resource\");\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class WeirdException extends Throwable {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Y y = new Y();) {\n" +
		"	           ^^^^^^^\n" +
		"Unhandled exception type WeirdException\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (y == null) {}\n" +
		"	               ^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	Y why = new Y();\n" +
		"	  ^^^\n" +
		"Resource leak: 'why' is never closed\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 5)\n" +
		"	Y why = new Y();\n" +
		"	        ^^^^^^^\n" +
		"Unhandled exception type WeirdException\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 20)\n" +
		"	class WeirdException extends Throwable {}\n" +
		"	      ^^^^^^^^^^^^^^\n" +
		"The serializable class WeirdException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n",
		null,
		true,
		options);
}
// Dead code tests
public void test017() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try (Y y = new Y();) {\n" +
			"           if (y == null)\n" +
			"				{}\n" +
			"		} finally {\n" +
			"       }\n" +
			"	}\n" +
			"} \n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() {\n" +
			"	}\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	{}\n" +
		"	^^\n" +
		"Dead code\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// Syntax error tests
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try () {\n" +
			"		} finally {\n" +
			"       }\n" +
			"	}\n" +
			"} \n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try () {\n" +
		"	    ^\n" +
		"Syntax error on token \"(\", Resources expected after this token\n" +
		"----------\n");
}
// Unhandled exception tests
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	public static void main(String [] args) {\n" +
			"            try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
			"            throw new XXException();\n" +
			"            } catch (XException x) {\n" +
			"	 		 } catch (YException y) {\n" +
			"            } catch (ZException z) {\n" +
			"	    	 } finally {\n" +
			"            }\n" +
			"	}\n" +
			"	public X() throws XException {\n" +
			"		throw new XException();\n" +
			"	}\n" +
			"	public void close() throws XXException {\n" +
			"		throw new XXException();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws YException {\n" +
			"		throw new YException();\n" +
			"	}\n" +
			"	public void close() throws YYException {\n" +
			"		throw new YYException();\n" +
			"	}\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"	public Z() throws ZException {\n" +
			"		throw new ZException();\n" +
			"	}\n" +
			"	public void close() throws ZZException {\n" +
			"		throw new ZZException();\n" +
			"	}\n" +
			"}\n" +
			"class XException extends Exception {}\n" +
			"class XXException extends Exception {}\n" +
			"class YException extends Exception {}\n" +
			"class YYException extends Exception {}\n" +
			"class ZException extends Exception {}\n" +
			"class ZZException extends Exception {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
		"	       ^\n" +
		"Unhandled exception type XXException thrown by automatic close() invocation on x\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
		"	                      ^\n" +
		"Unhandled exception type YYException thrown by automatic close() invocation on y\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
		"	                                     ^\n" +
		"Unhandled exception type ZZException thrown by automatic close() invocation on z\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	throw new XXException();\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unhandled exception type XXException\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 34)\n" +
		"	class XException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class XException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 35)\n" +
		"	class XXException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class XXException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 36)\n" +
		"	class YException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class YException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"8. WARNING in X.java (at line 37)\n" +
		"	class YYException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class YYException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"9. WARNING in X.java (at line 38)\n" +
		"	class ZException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class ZException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"10. WARNING in X.java (at line 39)\n" +
		"	class ZZException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class ZZException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// Resource type test
public void test021() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Y i = null) {\n" +
		"	     ^\n" +
		"The resource type Y does not implement java.lang.AutoCloseable\n" +
		"----------\n");
}
// Interface method return type compatibility test
public void test022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public int close () { return 0; }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	public int close () { return 0; }\n" +
		"	       ^^^\n" +
		"The return type is incompatible with AutoCloseable.close()\n" +
		"----------\n");
}
// Exception handling, compatibility tests
public void test023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () throws Blah {}\n" +
			"}\n" +
			"class Blah extends Throwable {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Y i = null) {\n" +
		"	       ^\n" +
		"Unhandled exception type Blah thrown by automatic close() invocation on i\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	public void close () throws Blah {}\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^\n" +
		"Exception Blah is not compatible with throws clause in AutoCloseable.close()\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 11)\n" +
		"	class Blah extends Throwable {}\n" +
		"	      ^^^^\n" +
		"The serializable class Blah does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// Exception handling tests
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	public static void main(String [] args) {\n" +
			"            try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
			"            throw new XXException();\n" +
			"            } catch (XException x) {\n" +
			"	 		 } catch (YException y) {\n" +
			"            } catch (ZException z) {\n" +
			"            } catch (XXException x) {\n" +
			"	 		 } catch (YYException y) {\n" +
			"            } catch (ZZException z) {\n" +
			"	    	 } finally {\n" +
			"            }\n" +
			"	}\n" +
			"	public X() throws XException {\n" +
			"		throw new XException();\n" +
			"	}\n" +
			"	public void close() throws XXException {\n" +
			"		throw new XXException();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws YException {\n" +
			"		throw new YException();\n" +
			"	}\n" +
			"	public void close() throws YYException {\n" +
			"		throw new YYException();\n" +
			"	}\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"	public Z() throws ZException {\n" +
			"		throw new ZException();\n" +
			"	}\n" +
			"	public void close() throws ZZException {\n" +
			"		throw new ZZException();\n" +
			"	}\n" +
			"}\n" +
			"class XException extends Exception {}\n" +
			"class XXException extends Exception {}\n" +
			"class YException extends Exception {}\n" +
			"class YYException extends Exception {}\n" +
			"class ZException extends Exception {}\n" +
			"class ZZException extends Exception {}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 37)\n" +
		"	class XException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class XException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 38)\n" +
		"	class XXException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class XXException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 39)\n" +
		"	class YException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class YException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 40)\n" +
		"	class YYException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class YYException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 41)\n" +
		"	class ZException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class ZException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 42)\n" +
		"	class ZZException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class ZZException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// Unhandled exception tests
public void test025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	public static void main(String [] args) {\n" +
			"            try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
			"            throw new XXException();\n" +
			"            } catch (XException x) {\n" +
			"	 		 } catch (YException y) {\n" +
			"            } catch (ZException z) {\n" +
			"            \n" +
			"            }\n" +
			"	}\n" +
			"	public X() throws XException {\n" +
			"		throw new XException();\n" +
			"	}\n" +
			"	public void close() throws XXException {\n" +
			"		throw new XXException();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws YException {\n" +
			"		throw new YException();\n" +
			"	}\n" +
			"	public void close() throws YYException {\n" +
			"		throw new YYException();\n" +
			"	}\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"	public Z() throws ZException {\n" +
			"		throw new ZException();\n" +
			"	}\n" +
			"	public void close() throws ZZException {\n" +
			"		throw new ZZException();\n" +
			"	}\n" +
			"}\n" +
			"class XException extends Exception {}\n" +
			"class XXException extends Exception {}\n" +
			"class YException extends Exception {}\n" +
			"class YYException extends Exception {}\n" +
			"class ZException extends Exception {}\n" +
			"class ZZException extends Exception {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
		"	       ^\n" +
		"Unhandled exception type XXException thrown by automatic close() invocation on x\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
		"	                      ^\n" +
		"Unhandled exception type YYException thrown by automatic close() invocation on y\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
		"	                                     ^\n" +
		"Unhandled exception type ZZException thrown by automatic close() invocation on z\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	throw new XXException();\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unhandled exception type XXException\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 34)\n" +
		"	class XException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class XException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 35)\n" +
		"	class XXException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class XXException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 36)\n" +
		"	class YException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class YException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"8. WARNING in X.java (at line 37)\n" +
		"	class YYException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class YYException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"9. WARNING in X.java (at line 38)\n" +
		"	class ZException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class ZException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"10. WARNING in X.java (at line 39)\n" +
		"	class ZZException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class ZZException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	public static void main(String [] args) {\n" +
			"            try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
			"            throw new XXException();\n" +
			"            } catch (XException x) {\n" +
			"	 		 } catch (YException y) {\n" +
			"            } catch (ZException z) {\n" +
			"            } catch (XXException x) {\n" +
			"	 		 } catch (YYException y) {\n" +
			"            } catch (ZZException z) {\n\n" +
			"            }\n" +
			"	}\n" +
			"	public X() throws XException {\n" +
			"		throw new XException();\n" +
			"	}\n" +
			"	public void close() throws XXException {\n" +
			"		throw new XXException();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws YException {\n" +
			"		throw new YException();\n" +
			"	}\n" +
			"	public void close() throws YYException {\n" +
			"		throw new YYException();\n" +
			"	}\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"	public Z() throws ZException {\n" +
			"		throw new ZException();\n" +
			"	}\n" +
			"	public void close() throws ZZException {\n" +
			"		throw new ZZException();\n" +
			"	}\n" +
			"}\n" +
			"class XException extends Exception {}\n" +
			"class XXException extends Exception {}\n" +
			"class YException extends Exception {}\n" +
			"class YYException extends Exception {}\n" +
			"class ZException extends Exception {}\n" +
			"class ZZException extends Exception {}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 37)\n" +
		"	class XException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class XException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 38)\n" +
		"	class XXException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class XXException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 39)\n" +
		"	class YException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class YException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 40)\n" +
		"	class YYException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class YYException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 41)\n" +
		"	class ZException extends Exception {}\n" +
		"	      ^^^^^^^^^^\n" +
		"The serializable class ZException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 42)\n" +
		"	class ZZException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class ZZException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"        try (X x = new X(); Y y = new Y()) {\n" +
			"            System.out.println(\"Body\");\n" +
			"            throw new Exception(\"Body\");\n" +
			"        } catch (Exception e) {\n" +
			"            System.out.println(e);\n" +
			"            Throwable [] suppressed = e.getSuppressed();\n" +
			"            for (int i = 0; i < suppressed.length; i++) {\n" +
			"                System.out.println(\"Suppressed:\" + suppressed[i]);\n" +
			"            }\n" +
			"        } finally {\n" +
			"            int finallyVar = 10;\n" +
			"            System.out.println(finallyVar);\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X CTOR\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X Close\");\n" +
			"        throw new Exception(\"X Close\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y CTOR\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y Close\");\n" +
			"        throw new Exception(\"Y Close\");\n" +
			"    }\n" +
			"}\n"
		},
		"X CTOR\n" +
		"Y CTOR\n" +
		"Body\n" +
		"Y Close\n" +
		"X Close\n" +
		"java.lang.Exception: Body\n" +
		"Suppressed:java.lang.Exception: Y Close\n" +
		"Suppressed:java.lang.Exception: X Close\n" +
		"10");
}
public void test028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"        try (X x = new X(); Y y = new Y()) {\n" +
			"            System.out.println(\"Body\");\n" +
			"        } catch (Exception e) {\n" +
			"            e.printStackTrace();\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X CTOR\");\n" +
			"    }\n" +
			"    public void close() {\n" +
			"        System.out.println(\"X DTOR\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y CTOR\");\n" +
			"    }\n" +
			"    public void close() {\n" +
			"        System.out.println(\"Y DTOR\");\n" +
			"    }\n" +
			"}\n"
		},
		"X CTOR\n" +
		"Y CTOR\n" +
		"Body\n" +
		"Y DTOR\n" +
		"X DTOR");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338881
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        try(FileReader fileReader = new FileReader(file);) {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"        } catch (IOException e) {\n" +
			"            System.out.println(\"Got IO exception\");\n" +
			"        } finally{\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"Got IO exception");
}
public void test030() {  // test return + resources
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	final boolean getOut = true;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = new X(); X x2 = new X()) {\n" +
			"            System.out.println(\"Outer Try\");\n" +
			"            while (true) {\n" +
			"            	try (Y y1 = new Y(); Y y2 = new Y()) {\n" +
			"            		System.out.println(\"Middle Try\");\n" +
			"            		try (Z z1 = new Z(); Z z2 = new Z()) {\n" +
			"            			System.out.println(\"Inner Try\");\n" +
			"            			if (getOut) \n" +
			"            				return;\n" +
			"            			else\n" +
			"            				break;\n" +
			"            		}\n" +
			"            	}\n" +
			"            }\n" +
			"            System.out.println(\"Out of while\");\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" +
		"X::X\n" +
		"X::X\n" +
		"Outer Try\n" +
		"Y::Y\n" +
		"Y::Y\n" +
		"Middle Try\n" +
		"Z::Z\n" +
		"Z::Z\n" +
		"Inner Try\n" +
		"Z::~Z\n" +
		"Z::~Z\n" +
		"Y::~Y\n" +
		"Y::~Y\n" +
		"X::~X\n" +
		"X::~X");
}
public void test030a() {  // test return + resources + with exceptions being thrown by close()
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	final boolean getOut = true;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = new X(); X x2 = new X()) {\n" +
			"            System.out.println(\"Outer Try\");\n" +
			"            while (true) {\n" +
			"            	try (Y y1 = new Y(); Y y2 = new Y()) {\n" +
			"            		System.out.println(\"Middle Try\");\n" +
			"            		try (Z z1 = new Z(); Z z2 = new Z()) {\n" +
			"            			System.out.println(\"Inner Try\");\n" +
			"            			if (getOut) \n" +
			"            				return;\n" +
			"            			else\n" +
			"            				break;\n" +
			"            		}\n" +
			"            	}\n" +
			"            }\n" +
			"            System.out.println(\"Out of while\");\n" +
			"        } catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"        throw new Exception(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"        throw new Exception(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"        throw new Exception(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" +
		"X::X\n" +
		"X::X\n" +
		"Outer Try\n" +
		"Y::Y\n" +
		"Y::Y\n" +
		"Middle Try\n" +
		"Z::Z\n" +
		"Z::Z\n" +
		"Inner Try\n" +
		"Z::~Z\n" +
		"Z::~Z\n" +
		"Y::~Y\n" +
		"Y::~Y\n" +
		"X::~X\n" +
		"X::~X\n" +
		"java.lang.Exception: Z::~Z\n" +
		"Suppressed: java.lang.Exception: Z::~Z\n" +
		"Suppressed: java.lang.Exception: Y::~Y\n" +
		"Suppressed: java.lang.Exception: Y::~Y\n" +
		"Suppressed: java.lang.Exception: X::~X\n" +
		"Suppressed: java.lang.Exception: X::~X");
}
public void test031() { // test break + resources
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	final boolean getOut = false;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = new X(); X x2 = new X()) {\n" +
			"            System.out.println(\"Outer Try\");\n" +
			"            while (true) {\n" +
			"            	try (Y y1 = new Y(); Y y2 = new Y()) {\n" +
			"            		System.out.println(\"Middle Try\");\n" +
			"            		try (Z z1 = new Z(); Z z2 = new Z()) {\n" +
			"            			System.out.println(\"Inner Try\");\n" +
			"            			if (getOut) \n" +
			"            				return;\n" +
			"            			else\n" +
			"            				break;\n" +
			"            		}\n" +
			"            	}\n" +
			"            }\n" +
			"            System.out.println(\"Out of while\");\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" +
		"X::X\n" +
		"X::X\n" +
		"Outer Try\n" +
		"Y::Y\n" +
		"Y::Y\n" +
		"Middle Try\n" +
		"Z::Z\n" +
		"Z::Z\n" +
		"Inner Try\n" +
		"Z::~Z\n" +
		"Z::~Z\n" +
		"Y::~Y\n" +
		"Y::~Y\n" +
		"Out of while\n" +
		"X::~X\n" +
		"X::~X");
}
public void test032() { // test continue + resources
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	final boolean getOut = false;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = new X(); X x2 = new X()) {\n" +
			"            System.out.println(\"Outer Try\");\n" +
			"            boolean more = true;\n" +
			"            while (more) {\n" +
			"            	try (Y y1 = new Y(); Y y2 = new Y()) {\n" +
			"            		System.out.println(\"Middle Try\");\n" +
			"            		try (Z z1 = new Z(); Z z2 = new Z()) {\n" +
			"            			System.out.println(\"Inner Try\");\n" +
			"                       more = false;\n" +
			"                       continue;\n" +
			"            		} finally { \n" +
			"                       System.out.println(\"Inner Finally\");\n" +
			"                   }\n" +
			"            	} finally {\n" +
			"                   System.out.println(\"Middle Finally\");\n" +
			"               }\n" +
			"            }\n" +
			"            System.out.println(\"Out of while\");\n" +
			"        } finally {\n" +
			"            System.out.println(\"Outer Finally\");\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" +
		"X::X\n" +
		"X::X\n" +
		"Outer Try\n" +
		"Y::Y\n" +
		"Y::Y\n" +
		"Middle Try\n" +
		"Z::Z\n" +
		"Z::Z\n" +
		"Inner Try\n" +
		"Z::~Z\n" +
		"Z::~Z\n" +
		"Inner Finally\n" +
		"Y::~Y\n" +
		"Y::~Y\n" +
		"Middle Finally\n" +
		"Out of while\n" +
		"X::~X\n" +
		"X::~X\n" +
		"Outer Finally");
}
public void test033() { // test null resources
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	final boolean getOut = false;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = null; Y y = new Y(); Z z = null) {\n" +
			"            System.out.println(\"Body\");\n" +
			"        } finally {\n" +
			"            System.out.println(\"Outer Finally\");\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" +
		"Y::Y\n" +
		"Body\n" +
		"Y::~Y\n" +
		"Outer Finally");
}
public void test034() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"		throw new Exception (\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"		throw new Exception (\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"		throw new Exception (\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"		throw new Exception (\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"java.lang.Exception: A::A\n" +
		"All done");
}
public void test035() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"		throw new Exception (\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"		throw new Exception (\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"		throw new Exception (\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"A::~A\n" +
		"java.lang.Exception: B::B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test036() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"		throw new Exception (\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"		throw new Exception (\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: C::C\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test037() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"		throw new Exception (\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: D::D\n" +
		"Suppressed: java.lang.Exception: C::~C\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: E::E\n" +
		"Suppressed: java.lang.Exception: D::~D\n" +
		"Suppressed: java.lang.Exception: C::~C\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"F::F\n" +
		"E::~E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: F::F\n" +
		"Suppressed: java.lang.Exception: E::~E\n" +
		"Suppressed: java.lang.Exception: D::~D\n" +
		"Suppressed: java.lang.Exception: C::~C\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test040() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"F::F\n" +
		"Inner try\n" +
		"F::~F\n" +
		"E::~E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: Body\n" +
		"Suppressed: java.lang.Exception: F::~F\n" +
		"Suppressed: java.lang.Exception: E::~E\n" +
		"Suppressed: java.lang.Exception: D::~D\n" +
		"Suppressed: java.lang.Exception: C::~C\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test041() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"F::F\n" +
		"Inner try\n" +
		"F::~F\n" +
		"E::~E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: F::~F\n" +
		"Suppressed: java.lang.Exception: E::~E\n" +
		"Suppressed: java.lang.Exception: D::~D\n" +
		"Suppressed: java.lang.Exception: C::~C\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test042() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"F::F\n" +
		"Inner try\n" +
		"F::~F\n" +
		"E::~E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: E::~E\n" +
		"Suppressed: java.lang.Exception: D::~D\n" +
		"Suppressed: java.lang.Exception: C::~C\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test043() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"F::F\n" +
		"Inner try\n" +
		"F::~F\n" +
		"E::~E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: D::~D\n" +
		"Suppressed: java.lang.Exception: C::~C\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test044() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"F::F\n" +
		"Inner try\n" +
		"F::~F\n" +
		"E::~E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: C::~C\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"F::F\n" +
		"Inner try\n" +
		"F::~F\n" +
		"E::~E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
public void test046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"F::F\n" +
		"Inner try\n" +
		"F::~F\n" +
		"E::~E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"java.lang.Exception: A::~A\n" +
		"All done");
}
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"B::B\n" +
		"Outer try\n" +
		"C::C\n" +
		"D::D\n" +
		"Middle try\n" +
		"E::E\n" +
		"F::F\n" +
		"Inner try\n" +
		"F::~F\n" +
		"E::~E\n" +
		"D::~D\n" +
		"C::~C\n" +
		"B::~B\n" +
		"A::~A\n" +
		"All done");
}
public void test048() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A()) {\n" +
			"			System.out.println(\"X::Try\");\n" +
			"			throw new Exception(\"X::Main\");\n" +
			"		} catch (Exception e) {\n" +
			"				System.out.println(e);\n" +
			"				Throwable suppressed [] = e.getSuppressed();\n" +
			"				for (int i = 0; i < suppressed.length; ++i) {\n" +
			"					System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"				}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		try (B b = new B()) {\n" +
			"			System.out.println(\"A::~A::Try\");\n" +
			"			throw new Exception(\"A::~A\");\n" +
			"		} catch (Exception e) {\n" +
			"				System.out.println(e);\n" +
			"				Throwable suppressed [] = e.getSuppressed();\n" +
			"				for (int i = 0; i < suppressed.length; ++i) {\n" +
			"					System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"				}\n" +
			"				throw e;\n" +
			"		} 	\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		try (C c = new C()) {\n" +
			"			System.out.println(\"B::~B::Try\");\n" +
			"			throw new Exception (\"B::~B\");\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"			throw e;\n" +
			"	} 	\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	} \n" +
			"}\n"
		},
		"Main\n" +
		"A::A\n" +
		"X::Try\n" +
		"A::~A\n" +
		"B::B\n" +
		"A::~A::Try\n" +
		"B::~B\n" +
		"C::C\n" +
		"B::~B::Try\n" +
		"C::~C\n" +
		"java.lang.Exception: B::~B\n" +
		"Suppressed: java.lang.Exception: C::~C\n" +
		"java.lang.Exception: A::~A\n" +
		"Suppressed: java.lang.Exception: B::~B\n" +
		"java.lang.Exception: X::Main\n" +
		"Suppressed: java.lang.Exception: A::~A\n" +
		"All done");
}
//ensure that it doesn't completely fail when using TWR and 1.5 mode
public void test049() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	runner.customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	runner.customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	runner.testFiles =
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        try(FileReader fileReader = new FileReader(file);) {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"        } catch (IOException e) {\n" +
			"            System.out.println(\"Got IO exception\");\n" +
			"        } finally{\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	try(FileReader fileReader = new FileReader(file);) {\n" +
		"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource specification not allowed here for source level below 1.7\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.forRelease("5");
	runner.runNegativeTest();
}
public void test050() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (E e = E.CONST) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
			"E.java",
			"public enum E implements AutoCloseable {\n" +
			"	CONST;\n" +
			"	private E () {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}"
		},
		"Main\n" +
		"E::E\n" +
		"Outer try\n" +
		"E::~E\n" +
		"java.lang.Exception: E::~E\n" +
		"All done");
}
public void test051() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
					"    public static void main(String[] args) throws Throwable {\n" +
					"        try (Test t = new Test()) {\n" +
					"            for (int i = 0; i < 10; i++) {\n" +
					"            }\n" +
					"\n" +
					"\n" +
					"        } \n" +
					"\n" +
					"        catch (Exception e) {\n" +
					"            StackTraceElement t = e.getStackTrace()[1];\n" +
					"            String file = t.getFileName();\n" +
					"            int line = t.getLineNumber();\n" +
					"            System.out.println(\"File = \" + file + \" \" + \"line = \" + line);\n" +
					"        }\n" +
					"    }\n" +
					"}\n" +
					"class Test implements AutoCloseable {\n" +
					"    public void close() throws Exception {\n" +
					"        throw new Exception();\n" +
					"    }\n" +
					"}\n"
		},
		"File = X.java line = 8");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348406
public void test052() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
					"    public static void main(String[] args) throws Throwable {\n" +
					"        try (Test t = new Test()) {\n" +
					"        } \n" +
					"    }\n" +
					"}\n" +
					"class Test {\n" +
					"    public void close() throws Exception {\n" +
					"        throw new Exception();\n" +
					"    }\n" +
					"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Test t = new Test()) {\n" +
		"	     ^^^^^^^^^^^^^^^^^^^\n" +
		"Resource specification not allowed here for source level below 1.7\n" +
		"----------\n",
		null,
		true,
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348705
// Unhandled exception due to autoclose should be reported separately
public void test053() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y y = new Y()) { \n" +
			"			y.close();\n" +
			"			System.out.println();\n" +
			"		} catch (RuntimeException e) {\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements Managed {\n" +
			"	 public Y() throws CloneNotSupportedException {}\n" +
			"    public void close () throws ClassNotFoundException, java.io.IOException {\n" +
			"    }\n" +
			"}\n" +
			"interface Managed extends AutoCloseable {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Y y = new Y()) { \n" +
		"	       ^\n" +
		"Unhandled exception type ClassNotFoundException thrown by automatic close() invocation on y\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	try (Y y = new Y()) { \n" +
		"	       ^\n" +
		"Unhandled exception type IOException thrown by automatic close() invocation on y\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	try (Y y = new Y()) { \n" +
		"	           ^^^^^^^\n" +
		"Unhandled exception type CloneNotSupportedException\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	y.close();\n" +
		"	^^^^^^^^^\n" +
		"Unhandled exception type ClassNotFoundException\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 4)\n" +
		"	y.close();\n" +
		"	^^^^^^^^^\n" +
		"Unhandled exception type IOException\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348705
// Variant of the above, witness for https://bugs.eclipse.org/358827#c6
public void test053a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y y = new Y()) { \n" +
			"			y.close();\n" +
			"			System.out.println();\n" +
			"		} catch (RuntimeException e) {\n" +
			"       } finally {\n" +
			"           System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements Managed {\n" +
			"	 public Y() throws CloneNotSupportedException {}\n" +
			"    public void close () throws ClassNotFoundException, java.io.IOException {\n" +
			"    }\n" +
			"}\n" +
			"interface Managed extends AutoCloseable {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Y y = new Y()) { \n" +
		"	       ^\n" +
		"Unhandled exception type ClassNotFoundException thrown by automatic close() invocation on y\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	try (Y y = new Y()) { \n" +
		"	       ^\n" +
		"Unhandled exception type IOException thrown by automatic close() invocation on y\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	try (Y y = new Y()) { \n" +
		"	           ^^^^^^^\n" +
		"Unhandled exception type CloneNotSupportedException\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	y.close();\n" +
		"	^^^^^^^^^\n" +
		"Unhandled exception type ClassNotFoundException\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 4)\n" +
		"	y.close();\n" +
		"	^^^^^^^^^\n" +
		"Unhandled exception type IOException\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349862 (NPE when union type is used in the resource section.)
public void test054() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        try (Object | Integer res = null) {\n" +
			"        } catch (Exception e) {\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Object | Integer res = null) {\n" +
		"	            ^\n" +
		"Syntax error on token \"|\", . expected\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349862 (NPE when union type is used in the resource section.)
public void test054a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        try (Object.Integer res = null) {\n" +
			"        } catch (Exception e) {\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (Object.Integer res = null) {\n" +
		"	     ^^^^^^^^^^^^^^\n" +
		"Object.Integer cannot be resolved to a type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353535 (verify error with try with resources)
public void test055() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.ByteArrayInputStream;\n" +
			"import java.io.InputStream;\n" +
			"public class X {\n" +
			"public static void main(String[] args) throws Exception {\n" +
			"  int b;\n" +
			"  try (final InputStream in = new ByteArrayInputStream(new byte[] { 42 })) {\n" +
			"    b = in.read();\n" +
			"  }\n" +
			"  System.out.println(\"Done\");\n" +
			"}\n" +
			"}\n",
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353535 (verify error with try with resources)
public void test055a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) throws Throwable {\n" +
			"        int tmp;\n" +
			"        try (A a = null) {\n" +
			"            try (A b = null) {\n" +
			"                tmp = 0;\n" +
			"            }\n" +
			"        }\n" +
			"        System.out.println(\"Done\");\n" +
			"    }\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"    @Override\n" +
			"    public void close() {\n" +
			"    }\n" +
			"}\n",
		},
		"Done");
}

// Note: test056* have been moved to ResourceLeakTests.java

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361053
public void test057() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	@Override\n" +
			"	public void close() throws Exception {\n" +
			"		throw new Exception();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		final boolean foo;\n" +
			"		try (X a = new X(); X b = new X()) {\n" +
			"			foo = true;\n" +
			"		} catch (final Exception exception) {\n" +
			"			return;\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},  "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=364008
public void test058() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.ByteArrayOutputStream;\n" +
			"import java.io.FileOutputStream;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"  public static void main(final String[] args) throws IOException {\n" +
			"    byte[] data;\n" +
			"    try (final ByteArrayOutputStream os = new ByteArrayOutputStream();\n" +
			"         final FileOutputStream out = new FileOutputStream(\"test.dat\")) {\n" +
			"      data = os.toByteArray();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		},  "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367566 - In try-with-resources statement close() method of resource is not called
public void test059() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"public class X implements java.lang.AutoCloseable {\n" +
			"  static boolean isOpen = true;\n" +
			"  public static void main(final String[] args) throws IOException {\n" +
			"    foo();\n" +
			"    System.out.println(isOpen);\n" +
			"  }\n" +
			"  static boolean foo() {\n" +
			"    try (final X x = new X()) {\n" +
			"      return x.num() >= 1;\n" +
			"    }\n" +
			"  }\n" +
			"  int num() { return 2; }\n" +
			"  public void close() {\n" +
			"    isOpen = false;\n" +
			"  }\n" +
			"}\n"
		},
		"false");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367566 - In try-with-resources statement close() method of resource is not called
public void test060() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	static int num = 10 ;\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	System.out.println(foo(1));\n" +
			"    	System.out.println(foo(2));\n" +
			"    	System.out.println(foo(3));\n" +
			"    }\n" +
			"	private static boolean foo(int where) throws Exception {\n" +
			"		final boolean getOut = true;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = new X(); X x2 = new X()) {\n" +
			"    		if (where == 1) {\n" +
			"    			return where == 1;\n" +
			"    		}\n" +
			"            System.out.println(\"Outer Try\");\n" +
			"            while (true) {\n" +
			"            	try (Y y1 = new Y(); Y y2 = new Y()) { \n" +
			"            		if (where == 2) {\n" +
			"            			return where == 2;\n" +
			"            		}		\n" +
			"            		System.out.println(\"Middle Try\");\n" +
			"            		try (Z z1 = new Z(); Z z2 = new Z()) {\n" +
			"            			System.out.println(\"Inner Try\");\n" +
			"            			if (getOut) \n" +
			"            				return num >= 10;\n" +
			"            			else\n" +
			"            				break; \n" +
			"            		}\n" +
			"            	}\n" +
			"            }\n" +
			"            System.out.println(\"Out of while\");\n" +
			"        }\n" +
			"		return false;\n" +
			"	}\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    @Override\n" +
			"	public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    @Override\n" +
			"	public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    @Override\n" +
			"	public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" +
		"X::X\n" +
		"X::X\n" +
		"X::~X\n" +
		"X::~X\n" +
		"true\n" +
		"Main\n" +
		"X::X\n" +
		"X::X\n" +
		"Outer Try\n" +
		"Y::Y\n" +
		"Y::Y\n" +
		"Y::~Y\n" +
		"Y::~Y\n" +
		"X::~X\n" +
		"X::~X\n" +
		"true\n" +
		"Main\n" +
		"X::X\n" +
		"X::X\n" +
		"Outer Try\n" +
		"Y::Y\n" +
		"Y::Y\n" +
		"Middle Try\n" +
		"Z::Z\n" +
		"Z::Z\n" +
		"Inner Try\n" +
		"Z::~Z\n" +
		"Z::~Z\n" +
		"Y::~Y\n" +
		"Y::~Y\n" +
		"X::~X\n" +
		"X::~X\n" +
		"true");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"import java.io.InputStream;\n" +
			"import java.net.MalformedURLException;\n" +
			"import java.net.URL;\n" +
			"\n" +
			"public class X {\n" +
			"    public static void main(String[] args) throws Exception {\n" +
			"      System.out.println(\"Done\");\n" +
			"    }\n" +
			"    public void foo() throws MalformedURLException {\n" +
			"        URL url = new URL(\"dummy\"); //$NON-NLS-1$\n" +
			"        try (InputStream is = url.openStream()) {\n" +
			"        } catch (IOException e) {\n" +
			"             return;\n" +
			"        } finally {\n" +
			"            try {\n" +
			"                java.nio.file.Files.delete(null);\n" +
			"            } catch (IOException e1) {\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.IOException;\n" +
			"import java.io.InputStream;\n" +
			"import java.net.MalformedURLException;\n" +
			"import java.net.URL;\n" +
			"import java.nio.file.Path;\n" +
			"import java.nio.file.StandardCopyOption;\n" +
			"\n" +
			"public class X {\n" +
			"    public static void main(String[] args) throws Exception {\n" +
			"      System.out.println(\"Done\");\n" +
			"    }\n" +
			"    public void executeImports() throws MalformedURLException {\n" +
			"        for (int i = 0; i < 3; i++) {\n" +
			"            URL url = new URL(\"dummy\"); //$NON-NLS-1$\n" +
			"            if (url != null) {\n" +
			"                Path target = new File(\"dummy\").toPath();\n" +
			"                try (InputStream is = url.openStream()) {\n" +
			"                    java.nio.file.Files.copy(is, target,\n" +
			"                            StandardCopyOption.REPLACE_EXISTING);\n" +
			"                } catch (IOException e) {\n" +
			"                     break;\n" +
			"                } finally {\n" +
			"                    try {\n" +
			"                        java.nio.file.Files.delete(target);\n" +
			"                    } catch (IOException e1) {\n" +
			"\n" +
			"                    }\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.IOException;\n" +
			"import java.io.InputStream;\n" +
			"import java.net.MalformedURLException;\n" +
			"import java.net.URL;\n" +
			"import java.nio.file.Path;\n" +
			"import java.nio.file.StandardCopyOption;\n" +
			"\n" +
			"public class X {\n" +
			"    public static void main(String[] args) throws Exception {\n" +
			"      System.out.println(\"Done\");\n" +
			"    }\n" +
			"    public void executeImports() throws MalformedURLException {\n" +
			"        for (int i = 0; i < 3; i++) {\n" +
			"            URL url = new URL(\"dummy\"); //$NON-NLS-1$\n" +
			"            if (url != null) {\n" +
			"                Path target = new File(\"dummy\").toPath();\n" +
			"                try (InputStream is = url.openStream()) {\n" +
			"                    java.nio.file.Files.copy(is, target,\n" +
			"                            StandardCopyOption.REPLACE_EXISTING);\n" +
			"                } catch (IOException e) {\n" +
			"                     continue;\n" +
			"                } finally {\n" +
			"                    try {\n" +
			"                        java.nio.file.Files.delete(target);\n" +
			"                    } catch (IOException e1) {\n" +
			"\n" +
			"                    }\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.IOException;\n" +
			"import java.io.InputStream;\n" +
			"import java.net.MalformedURLException;\n" +
			"import java.net.URL;\n" +
			"import java.nio.file.Path;\n" +
			"import java.nio.file.StandardCopyOption;\n" +
			"\n" +
			"public class X implements AutoCloseable {\n" +
			"	public void foo()  {\n" +
			"        try (X x = new X()) {\n" +
			"	     System.out.println(\"Try\");\n" +
			"	     throw new Exception();\n" +
			"        } catch (Exception e) {\n" +
			"	     System.out.println(\"Catch\");\n"+
			"             return;\n" +
			"        } finally {\n" +
			"        	System.out.println(\"Finally\");\n" +
			"        }\n" +
			"    }\n" +
			"	public void close() {\n" +
			"		System.out.println(\"Close\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"}\n"
		},
		"Try\n" +
		"Close\n" +
		"Catch\n" +
		"Finally");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375248 (AIOOB with try with resources)
public void test375248d() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.IOException;\n" +
			"import java.io.InputStream;\n" +
			"import java.net.MalformedURLException;\n" +
			"import java.net.URL;\n" +
			"import java.nio.file.Path;\n" +
			"import java.nio.file.StandardCopyOption;\n" +
			"\n" +
			"public class X implements AutoCloseable {\n" +
			"	public void foo()  {\n" +
			"        try (X x = new X()) {\n" +
			"	     System.out.println(\"Try\");\n" +
			"        } catch (Exception e) {\n" +
			"	     System.out.println(\"Catch\");\n"+
			"             return;\n" +
			"        } finally {\n" +
			"        	System.out.println(\"Finally\");\n" +
			"           return;\n" +
			"        }\n" +
			"    }\n" +
			"	public void close() {\n" +
			"		System.out.println(\"Close\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"}\n"
		},
		"Try\n" +
		"Close\n" +
		"Finally");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		HasAutoCloseable a;\n" +
			"		try(AutoCloseable b=(a=new HasAutoCloseable()).a) {\n" +
			"		}\n" +
			"		System.out.println(a);\n" +
			"	}\n" +
			"	public static class AutoCloseableA implements AutoCloseable {\n" +
			"		@Override\n" +
			"		public void close() {\n" +
			"			// TODO Auto-generated method stub\n" +
			"		}\n" +
			"	}\n" +
			"	public static class HasAutoCloseable {\n" +
			"		AutoCloseable a = new AutoCloseableA();\n" +
			"		public String toString() {\n" +
			"			return \"SUCCESS\";\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"        HasAutoCloseable aLocal;\n" +
			"        try(AutoCloseable b=(new HasAutoCloseable()).a){\n" +
			"        	aLocal = new HasAutoCloseable();\n" +
			"        }\n" +
			"        catch (Throwable e) {\n" +
			"        }\n" +
			"       System.out.println(aLocal.toString());       \n" +
			"    } \n" +
			"    public static class AutoCloseableA implements AutoCloseable{\n" +
			"        @Override\n" +
			"        public void close() {\n" +
			"        }\n" +
			"    }\n" +
			"    public static class HasAutoCloseable{\n" +
			"        AutoCloseable a=new AutoCloseableA(); \n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	System.out.println(aLocal.toString());       \n" +
		"	                   ^^^^^^\n" +
		"The local variable aLocal may not have been initialized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"        HasAutoCloseable aLocal;\n" +
			"        try(AutoCloseable b=(aLocal = new HasAutoCloseable()).a){\n" +
			"        	\n" +
			"        }\n" +
			"        catch (Throwable e) {\n" +
			"        }\n" +
			"       System.out.println(aLocal.toString());       \n" +
			"    } \n" +
			"    public static class AutoCloseableA implements AutoCloseable{\n" +
			"        @Override\n" +
			"        public void close() {\n" +
			"        }\n" +
			"    }\n" +
			"    public static class HasAutoCloseable{\n" +
			"        AutoCloseable a=new AutoCloseableA(); \n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	System.out.println(aLocal.toString());       \n" +
		"	                   ^^^^^^\n" +
		"The local variable aLocal may not have been initialized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		HasAutoCloseable a;\n" +
			"		try(AutoCloseable b=(a=new HasAutoCloseable()).a) {\n" +
			"       } finally {\n" +
			"            System.out.println(\"Finally\");\n" +
			"        }\n" +
			"		System.out.println(a);\n" +
			"	}\n" +
			"	public static class AutoCloseableA implements AutoCloseable {\n" +
			"		@Override\n" +
			"		public void close() {\n" +
			"			// TODO Auto-generated method stub\n" +
			"		}\n" +
			"	}\n" +
			"	public static class HasAutoCloseable {\n" +
			"		AutoCloseable a = new AutoCloseableA();\n" +
			"		public String toString() {\n" +
			"			return \"SUCCESS\";\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		"Finally\n" +
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326d() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"        HasAutoCloseable aLocal;\n" +
			"        try(AutoCloseable b=(new HasAutoCloseable()).a){\n" +
			"        	aLocal = new HasAutoCloseable();\n" +
			"        }\n" +
			"        catch (Throwable e) {\n" +
			"        } finally {\n" +
			"            System.out.println(\"Finally\");\n" +
			"        }\n" +
			"       System.out.println(aLocal.toString());       \n" +
			"    } \n" +
			"    public static class AutoCloseableA implements AutoCloseable{\n" +
			"        @Override\n" +
			"        public void close() {\n" +
			"        }\n" +
			"    }\n" +
			"    public static class HasAutoCloseable{\n" +
			"        AutoCloseable a=new AutoCloseableA(); \n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	System.out.println(aLocal.toString());       \n" +
		"	                   ^^^^^^\n" +
		"The local variable aLocal may not have been initialized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326e() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"        HasAutoCloseable aLocal;\n" +
			"        try(AutoCloseable b=(aLocal = new HasAutoCloseable()).a){\n" +
			"        	\n" +
			"        }\n" +
			"        catch (Throwable e) {\n" +
			"        } finally {\n" +
			"            System.out.println(\"Finally\");\n" +
			"        }\n" +
			"       System.out.println(aLocal.toString());       \n" +
			"    } \n" +
			"    public static class AutoCloseableA implements AutoCloseable{\n" +
			"        @Override\n" +
			"        public void close() {\n" +
			"        }\n" +
			"    }\n" +
			"    public static class HasAutoCloseable{\n" +
			"        AutoCloseable a=new AutoCloseableA(); \n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	System.out.println(aLocal.toString());       \n" +
		"	                   ^^^^^^\n" +
		"The local variable aLocal may not have been initialized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326f() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void testWithResourcesAssignment() throws Exception{\n" +
			"        HasAutoCloseable a;\n" +
			"        try(AutoCloseable b=(a=new HasAutoCloseable()).a){\n" +
			"        } finally {\n" +
			"        	System.out.println(a);\n" +
			"        }\n" +
			"    }\n" +
			"    public class AutoCloseableA implements AutoCloseable{\n" +
			"        @Override\n" +
			"        public void close() {\n" +
			"        }\n" +
			"    }\n" +
			"    public class HasAutoCloseable{\n" +
			"        AutoCloseable a=new AutoCloseableA();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	System.out.println(a);\n" +
		"	                   ^\n" +
		"The local variable a may not have been initialized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375326
public void test375326g() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class CheckedException extends Throwable {}\n" +
			"public class X {\n" +
			"    public void testWithResourcesAssignment() throws Exception{\n" +
			"        HasAutoCloseable a;\n" +
			"        try(AutoCloseable b=(a=new HasAutoCloseable()).a){\n" +
			"            throw new CheckedException();\n" +
			"        } catch (CheckedException e) {\n" +
			"            System.out.println(a);\n" +
			"        } finally {\n" +
			"        	System.out.println(a);\n" +
			"        }\n" +
			"    }\n" +
			"    public class AutoCloseableA implements AutoCloseable{\n" +
			"        @Override\n" +
			"        public void close() {\n" +
			"        }\n" +
			"    }\n" +
			"    public class HasAutoCloseable{\n" +
			"        AutoCloseable a=new AutoCloseableA();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	class CheckedException extends Throwable {}\n" +
		"	      ^^^^^^^^^^^^^^^^\n" +
		"The serializable class CheckedException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	System.out.println(a);\n" +
		"	                   ^\n" +
		"The local variable a may not have been initialized\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	System.out.println(a);\n" +
		"	                   ^\n" +
		"The local variable a may not have been initialized\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
public void test380112a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.*;\n" +
				"interface I extends Closeable, Serializable {}\n" +
				"public class X {\n"+
				"    public static void main(String [] args) {\n" +
				"        try (I i = getX()) {\n" +
				"        } catch (IOException x) {\n" +
				"        }\n"+
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    public static I getX() { return null;}\n"+
				"    public X(){}\n" +
				"}\n"
			},
			"Done");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
//variant with finally
public void test380112b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.*;\n" +
				"interface I extends Closeable, Serializable {}\n" +
				"public class X {\n"+
				"    public static void main(String [] args) {\n" +
				"        try (I i = getX()) {\n" +
				"        } catch (IOException x) {\n" +
				"        } finally {\n"+
				"          System.out.println(\"Done\");\n" +
				"        }\n" +
				"    }\n" +
				"    public static I getX() { return null;}\n"+
				"    public X(){}\n" +
				"}\n"
			},
			"Done");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
//variant with two methods throwing different Exceptions (one subtype of other)
//subtype should be the one to be caught
public void test380112c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.*;\n" +
				"interface I2 { public void close() throws FileNotFoundException; }\n"+
				"interface I extends Closeable, I2 {}\n" +
				"public class X {\n"+
				"    public static void main(String [] args) {\n" +
				"        try (I i = getX()) {\n" +
				"        } catch (FileNotFoundException x) {\n" +
				"        }\n"+
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    public static I getX() { return null;}\n"+
				"    public X(){}\n" +
				"}\n"
			},
			"Done");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
//test380112c's variant with finally
public void test380112d() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.*;\n" +
				"interface I2 { public void close() throws FileNotFoundException; }\n"+
				"interface I extends Closeable, I2 {}\n" +
				"public class X {\n"+
				"    public static void main(String [] args) {\n" +
				"        try (I i = getX()) {\n" +
				"        } catch (FileNotFoundException x) {\n" +
				"        } finally {\n"+
				"          System.out.println(\"Done\");\n" +
				"        }\n" +
				"    }\n" +
				"    public static I getX() { return null;}\n"+
				"    public X(){}\n" +
				"}\n"
			},
			"Done");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=380112
//test380112a variant moving the Interface into a binary
public void test380112e() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test380112.jar";
	String[] defaultLibs = getDefaultClassPaths();
	String[] libs = new String[defaultLibs.length + 1];
	System.arraycopy(defaultLibs, 0, libs, 0, defaultLibs.length);
	libs[defaultLibs.length] = path;
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.*;\n" +
				"import pkg380112.I;\n" +
				"public class X {\n"+
				"    public static void main(String [] args) {\n" +
				"        try (I i = getX()) {\n" +
				"        } catch (IOException x) {\n" +
				"        }\n"+
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    public static I getX() { return null;}\n"+
				"    public X(){}\n" +
				"}\n"
			}, "Done", libs, true, new String[] {"-cp", "."+File.pathSeparator+path});
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=394780
public void test394780() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X<R extends Resource> {\n" +
				"    public static void main(String[] args) {\n" +
				"        X<Resource> m = new X<>();\n" +
				"        m.tryWithResource(new ResourceImpl());\n" +
				"    }\n" +
				"    public void tryWithResource(R resource) {\n" +
				"        try (R r = resource) {\n" +
				"            r.compute();\n" +
				"        }\n" +
				"    }\n" +
				"}",
				"Resource.java",
				"public interface Resource extends AutoCloseable {\n" +
				"    void compute();\n" +
				"    @Override\n" +
				"    public void close();\n" +
				"}",
				"ResourceImpl.java",
				"public class ResourceImpl implements Resource {\n" +
				"    @Override\n" +
				"    public void close() {\n" +
				"        System.out.print(\"close\");\n" +
				"    }\n" +
				"    @Override\n" +
				"    public void compute() {\n" +
				"        System.out.print(\"compute\");\n" +
				"    }\n" +
				"}"
			},
			"computeclose");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=533187
public void testBug533187() {
	this.runConformTest(
			true,
			new String[] {
				"Stuck.java",
				"public class Stuck {\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(snippet1());\n" +
				"    }\n" +
				"    public static String snippet1() {\n" +
				"        try {\n" +
				"            synchronized (String.class) {\n" +
				"                try (AutoCloseable scope = null) { \n" +
				"                    return \"RETURN\";\n" +
				"                } catch (Throwable t) {\n" +
				"                    return t.toString();\n" +
				"                }\n" +
				"            }\n" +
				"        } finally {\n" +
				"            raise();\n" +
				"        }\n" +
				"    }\n" +
				"    public static void raise() {\n" +
				"        throw new RuntimeException();\n" +
				"    }\n" +
				"}"
			},
			null,
			null,
			null,
			null,
			"java.lang.RuntimeException\n" +
			"	at Stuck.raise(Stuck.java:19)\n" +
			"	at Stuck.snippet1(Stuck.java:15)\n" +
			"	at Stuck.main(Stuck.java:3)\n",
			null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=467230
public void testBug467230() {
	this.runConformTest(
			true,
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	static class C implements AutoCloseable {\n" +
				"		@Override\n" +
				"		public void close() {\n" +
				"			System.out.println(\"close\");\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		try (C c = new C()) {\n" +
				"			return;\n" +
				"		} catch (Exception e) {\n" +
				"			System.out.println(\"catch\");\n" +
				"		} finally {\n" +
				"			f();\n" +
				"		}\n" +
				"	}\n" +
				"	private static void f() {\n" +
				"		System.out.println(\"finally\");\n" +
				"		throw new RuntimeException();\n" +
				"	}\n" +
				"}"
			},
			null,
			null,
			null,
			"close\n" +
			"finally",
			"java.lang.RuntimeException\n" +
			"	at Test.f(Test.java:19)\n" +
			"	at Test.main(Test.java:14)\n",
			null);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/934
public void _testGHIssue934() {
	this.runConformTest(
			true,
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static class Y implements AutoCloseable {\n" +
				"		RuntimeException e;\n" +
				"\n" +
				"		public Y(RuntimeException e) {\n" +
				"			this.e = e;\n" +
				"		}\n" +
				"\n" +
				"		@Override\n" +
				"		public void close() {\n" +
				"			throw e;\n" +
				"		}\n" +
				"	}\n" +
				"    public static void main(String[] args) {\n" +
				"        RuntimeException e = new RuntimeException(\"My Exception\");\n" +
				"        try {\n" +
				"            try (Y A = new Y(e)) {\n" +
				"                throw e;\n" +
				"            }\n" +
				"        } catch (IllegalArgumentException iae) {\n" +
				"            if (iae.getCause() == e) \n" +
				"                System.out.println(\"OK!\");\n" +
				"        }\n" +
				"    }\n" +
				"}\n"

			},
			null,
			null,
			null,
			"OK!",
			"",
			null);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1063
// Regression in code generation for try with resources with the fix for Issue # 934
public void testGHIssue1063() {
	this.runConformTest(
			true,
			new String[] {
				"X.java",
				"import java.io.Closeable;\n" +
				"import java.io.IOException;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) throws IOException {\n" +
				"		try (DummyClosable closable = new DummyClosable()) {\n" +
				"			throw new IOException(\"OMG!!!\");\n" +
				"		} catch (IOException e) {\n" +
				"			throw e;\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	static class DummyClosable implements Closeable {\n" +
				"		@Override\n" +
				"		public void close() throws IOException {\n" +
				"			System.out.println(\"Closed!\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			null,
			null,
			null,
			"Closed!",
			"java.io.IOException: OMG!!!\n" +
			"	at X.main(X.java:7)\n",
			null);
}
public static Class testClass() {
	return TryWithResourcesStatementTest.class;
}
}
