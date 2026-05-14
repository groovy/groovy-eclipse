/*******************************************************************************
 * Copyright (c) 2016, 2022 IBM corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
@SuppressWarnings({ "rawtypes" })
public class TryStatement9Test extends AbstractRegressionTest {

static {
///	TESTS_NAMES = new String[] { "testBug488569_019" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryStatement9Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_9);
}

public void testBug488569_001() { // vanilla test case
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Closeable;\n" +
				"import java.io.IOException;\n" +
				"\n" +
				"class Y implements Closeable {\n" +
				"        @Override\n" +
				"        public void close() throws IOException {\n" +
				"                // nothing\n" +
				"        }\n" +
				"}\n" +
				"public class X {\n" +
				"\n" +
				"        public void foo() throws IOException {\n" +
				"             final Y y1 = new Y();\n" +
				"             try (y1) { \n" +
				"            	 //\n" +
				"             }\n" +
				"        } \n" +
				"        public static void main(String[] args) {\n" +
				"			System.out.println(\"Done\");\n" +
				"		}\n" +
				"} \n"
			},
			"Done");
}

// vanilla with a delimiter
public void testBug488569_002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Closeable;\n" +
				"import java.io.IOException;\n" +
				"\n" +
				"class Y implements Closeable {\n" +
				"        @Override\n" +
				"        public void close() throws IOException {\n" +
				"                // nothing\n" +
				"        }\n" +
				"}\n" +
				"public class X {\n" +
				"\n" +
				"        public void foo() throws IOException {\n" +
				"             final Y y1 = new Y();\n" +
				"             try (y1;) { \n" +
				"            	 //\n" +
				"             }\n" +
				"        } \n" +
				"        public static void main(String[] args) {\n" +
				"			System.out.println(\"Done\");\n" +
				"		}\n" +
				"} \n"
			},
			"Done");
}

public void testBug488569_003() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Closeable;\n" +
				"import java.io.IOException;\n" +
				"\n" +
				"class Y implements Closeable {\n" +
				"        @Override\n" +
				"        public void close() throws IOException {\n" +
				"                // nothing\n" +
				"        }\n" +
				"}\n" +
				"public class X {\n" +
				"\n" +
				"        public void foo() throws IOException {\n" +
				"             final Y y1 = new Y();\n" +
				"             final Y y2 = new Y();\n" +
				"             try (y1; y2) { \n" +
				"            	 //\n" +
				"             }\n" +
				"        } \n" +
				"        public static void main(String[] args) {\n" +
				"			System.out.println(\"Done\");\n" +
				"		}\n" +
				"} \n"
			},
			"Done");
}
public void testBug488569_004() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Closeable;\n" +
				"import java.io.IOException;\n" +
				"\n" +
				"class Y implements Closeable {\n" +
				"        @Override\n" +
				"        public void close() throws IOException {\n" +
				"                // nothing\n" +
				"        }\n" +
				"}\n" +
				"public class X {\n" +
				"\n" +
				"        public void foo() throws IOException {\n" +
				"             final Y y1 = new Y();\n" +
				"             try (y1; final Y y2 = new Y()) { \n" +
				"            	 //\n" +
				"             }\n" +
				"        } \n" +
				"        public static void main(String[] args) {\n" +
				"			System.out.println(\"Done\");\n" +
				"		}\n" +
				"} \n"
			},
		"Done");
}

public void testBug488569_005() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Closeable;\n" +
				"import java.io.IOException;\n" +
				"\n" +
				"class Y implements Closeable {\n" +
				"        @Override\n" +
				"        public void close() throws IOException {\n" +
				"                // nothing\n" +
				"        }\n" +
				"}\n" +
				"public class X {\n" +
				"\n" +
				"        public void foo() throws IOException {\n" +
				"             final Y y1 = new Y();\n" +
				"             try (final Y y = new Y(); y1; final Y y2 = new Y()) { \n" +
				"            	 //\n" +
				"             }\n" +
				"        } \n" +
				"        public static void main(String[] args) {\n" +
				"			System.out.println(\"Done\");\n" +
				"		}\n" +
				"} \n"
			},
		"Done");
}
public void testBug488569_006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"public class X { \n" +
			"    public void foo() throws IOException {\n" +
			"         Y y1 = new Y();\n" +
			"         try(y1) { \n" +
			"             return;\n" +
			"         }\n" +
			"    } \n" +
			"}  \n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"		final int x = 10;\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}",
		},
		"");
}

// check for the error for non-effectively final variable.
public void testBug488569_007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"        public void foo() throws IOException {\n" +
			"             Y y1 = new Y();\n" +
			"             y1 = new Y();\n" +
			"             try (y1) { \n" +
			"            	 //\n" +
			"             }\n" +
			"        } \n" +
			"        public static void main(String[] args) {\n" +
			"			System.out.println(\"Done\");\n" +
			"		}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 14)\n" +
		"	y1 = new Y();\n" +
		"	^^^^^^^^^^^^\n" +
		"Resource leak: \'y1\' is not closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 15)\n" +
		"	try (y1) { \n" +
		"	     ^^\n" +
		"Local variable y1 defined in an enclosing scope must be final or effectively final\n" +
		"----------\n");
}
//check for the error for combination of NameRef and LocalVarDecl.
public void testBug488569_008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"        public void foo() throws IOException {\n" +
			"             try (y1; Y y1 = new Y()) { \n" +
			"            	 //\n" +
			"             }\n" +
			"        } \n" +
			"        public static void main(String[] args) {\n" +
			"			System.out.println(\"Done\");\n" +
			"		}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	try (y1; Y y1 = new Y()) { \n" +
		"	     ^^\n" +
		"y1 cannot be resolved\n" +
		"----------\n");
}

//check for the warning for combination of LocalVarDecl and NameRef.
public void testBug488569_009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"        public void foo() throws IOException {\n" +
			"             try (Y y1 = new Y(); y1) { \n" +
			"            	 //\n" +
			"             }\n" +
			"        } \n" +
			"        public static void main(String[] args) {\n" +
			"			System.out.println(\"Done\");\n" +
			"		}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 13)\n" +
		"	try (Y y1 = new Y(); y1) { \n" +
		"	                     ^^\n" +
		"Duplicate resource reference y1\n" +
		"----------\n");
}
//check for the warning for combination of NameRef and NameRef.
public void testBug488569_010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"        public void foo() throws IOException {\n" +
			"             Y y1 = new Y();\n" +
			"             try (y1; y1) { \n" +
			"            	 //\n" +
			"             }\n" +
			"        } \n" +
			"        public static void main(String[] args) {\n" +
			"			System.out.println(\"Done\");\n" +
			"		}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 14)\n" +
		"	try (y1; y1) { \n" +
		"	         ^^\n" +
		"Duplicate resource reference y1\n" +
		"----------\n");
}
public void testBug488569_011() {
	this.runConformTest(
			new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"        public void foo() throws IOException {\n" +
			"             try (Y y1 = new Y();y1) { \n" +
			"            	 //\n" +
			"             }\n" +
			"        } \n" +
			"        public static void main(String[] args) {\n" +
			"			System.out.println(\"Done\");\n" +
			"		}\n" +
			"} \n"
			},
			"Done");
}

public void testBug488569_012() {
	this.runConformTest(
			new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"        public void foo() throws IOException {\n" +
			"             Y y = new Y();\n" +
			"             try (Y y1 = new Y();y;y1) { \n" +
			"            	 //\n" +
			"             }\n" +
			"        } \n" +
			"        public static void main(String[] args) {\n" +
			"			System.out.println(\"Done\");\n" +
			"		}\n" +
			"} \n"
			},
			"Done");
}

// Confirm the behavior as described in https://bugs.eclipse.org/bugs/show_bug.cgi?id=338402#c16 even with the
// presence of a duplicate variable in-line with javac9.
public void testBug488569_013() {
	this.runConformTest(
			new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"    	Z z1 = new Z();\n" +
			"        try (Y y = new Y();z1;y) {\n" +
			"        }\n" +
			"    }  \n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"Y CLOSE\");\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class Z implements AutoCloseable {\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"Z CLOSE\");\n" +
			"	}\n" +
			"}\n"
			},
			"Y CLOSE\n" +
			"Z CLOSE\n" +
			"Y CLOSE"
			);
}

// check for unhandled-exception error
public void testBug488569_014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Y y1 = new Y();		 		 \n" +
			"		try (y1)  {\n" +
			"			System.out.println(\"In Try\");\n" +
			"		} finally {\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"   \n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() throws IOException {\n" +
			"		System.out.println(\"Closed\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	try (y1)  {\n" +
		"	     ^^\n" +
		"Unhandled exception type IOException thrown by automatic close() invocation on y1\n" +
		"----------\n");
}

// field to be legal
public void testBug488569_015(){
	this.runConformTest(
			new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"public class X {\n" +
			"	final Y y = new Y();\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"		try (y)  {\n" +
			"			System.out.println(\"In Try\");\n" +
			"		} catch (IOException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"		finally {  \n" +
			"		}  \n" +
			"		//y1 = new Y();	 \n" +
			"	} \n" +
			"} \n" +
			"   \n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() throws IOException {\n" +
			"		System.out.println(\"Closed\");\n" +
			"	}\n" +
			"}\n"
			},
			"In Try\n" +
			"Closed"
			);
}
//field to be legal - but null field not to be called for close
public void testBug488569_016(){
	this.runConformTest(
			new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"public class X {\n" +
			"	final Y y = null;\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"		try (y)  {\n" +
			"			System.out.println(\"In Try\");\n" +
			"		} catch (IOException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"		finally {  \n" +
			"		}  \n" +
			"	} \n" +
			"} \n" +
			"   \n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() throws IOException {\n" +
			"		System.out.println(\"Closed\");\n" +
			"	}\n" +
			"}\n"
			},
			"In Try"
			);
}

// field in various avatars
public void testBug488569_017(){
	this.runConformTest(
			new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"class Z {\n" +
			"	final Y yz = new Y();\n" +
			"}\n" +
			"public class X extends Z {\n" +
			"	final Y y2 = new Y();\n" +
			"	\n" +
			"	public void foo() {\n" +
			"		try (super.yz; y2)  {\n" +
			"			System.out.println(\"In Try\");\n" +
			"		} catch (IOException e) {\n" +
			"			\n" +
			"		}finally { \n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	@Override\n" +
			"	public void close() throws IOException {\n" +
			"		System.out.println(\"Closed\");\n" +
			"	} \n" +
			"}  \n"
			},
			"In Try\n" +
			"Closed\n" +
			"Closed"
			);
}

// negative tests: non-final fields
public void testBug488569_018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"class Z {\n" +
			"	 Y yz = new Y();\n" +
			"}\n" +
			"public class X extends Z {\n" +
			"	 Y y2 = new Y();\n" +
			"	\n" +
			"	public void foo() {\n" +
			"		try (this.y2; super.yz;y2)  {  \n" +
			"			System.out.println(\"In Try\");\n" +
			"		} catch (IOException e) {			  \n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	@Override\n" +
			"	public void close() throws IOException {\n" +
			"		System.out.println(\"Closed\");\n" +
			"	} \n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	try (this.y2; super.yz;y2)  {  \n" +
		"	          ^^\n" +
		"Field y2 must be final\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	try (this.y2; super.yz;y2)  {  \n" +
		"	                    ^^\n" +
		"Field yz must be final\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	try (this.y2; super.yz;y2)  {  \n" +
		"	                       ^^\n" +
		"Local variable y2 defined in an enclosing scope must be final or effectively final\n" +
		"----------\n");
}
//negative tests: duplicate fields
public void testBug488569_019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"class Z {\n" +
			"	 final Y yz = new Y();\n" +
			"}\n" +
			"public class X extends Z {\n" +
			"	final  Y y2 = new Y();\n" +
			"	\n" +
			"	 Y bar() {\n" +
			"		 return new Y();\n" +
			"	 }\n" +
			"	public void foo() {\n" +
			"		Y y3 = new Y();\n" +
			"		try (y3; y3;super.yz;super.yz;this.y2;)  {  \n" +
			"			System.out.println(\"In Try\");\n" +
			"		} catch (IOException e) {			  \n" +
			"		} \n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	@Override\n" +
			"	public void close() throws IOException {\n" +
			"		System.out.println(\"Closed\");\n" +
			"	}  \n" +
			"}  \n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 14)\n" +
		"	try (y3; y3;super.yz;super.yz;this.y2;)  {  \n" +
		"	         ^^\n" +
		"Duplicate resource reference y3\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 14)\n" +
		"	try (y3; y3;super.yz;super.yz;this.y2;)  {  \n" +
		"	                     ^^^^^^^^\n" +
		"Duplicate resource reference super.yz\n" +
		"----------\n");
}

public void testBug488569_020() { // vanilla test case
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"\n" +
				"public class X {\n" +
				"     final Z y2 = new Z();\n" +
				"     public static void main(String[] args) throws Exception {\n" +
				"          X t = new X();\n" +
				"          try (t.y2) {     \n" +
				"          }          \n" +
				"     }  \n" +
				"}\n" +
				"\n" +
				"class Z implements AutoCloseable {\n" +
				"     @Override\n" +
				"     public void close() throws IOException {\n" +
				"          System.out.println(\"Done\");\n" +
				"     }\n" +
				"} \n"
			},
			"Done");
}

//negative tests: duplicate fields
public void testBug488569_021() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"public class X {\n" +
			"     final Z z = new Z();\n" +
			"     public X() {\n" +
			"          try(this.z) {\n" +
			"               \n" +
			"          }\n" +
			"     }\n" +
			"}\n" +
			"\n" +
			"class Z implements AutoCloseable {\n" +
			"     @Override\n" +
			"     public void close() throws IOException {\n" +
			"          System.out.println(\"Closed\");\n" +
			"     } \n" +
			"}  \n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	try(this.z) {\n" +
		"	    ^^^^^^\n" +
		"Unhandled exception type IOException thrown by automatic close() invocation on z\n" +
		"----------\n");
}
public void testBug577128_1() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X implements AutoCloseable {\n" +
			"	private void release() {\n"
			+ "		X cl = new X();\n"
			+ "		try (this;cl) {} \n"
			+ "        	catch (IOException e) {\n"
			+ "        }\n"
			+ "	}\n"
			+ "	public static void main(String[] args) {\n"
			+ "		X cl = new X();\n"
			+ "		cl.release();\n"
			+ "	}\n"
			+ "	@Override\n"
			+ "	public void close() throws IOException {\n"
			+ "		System.out.println(\"close() call\");\n"
			+ "	}\n"
			+ "}\n",
		},
		"close() call\n" +
		"close() call");
}
public void testGH1825() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
			import java.io.*;
			class X {
				protected X read() throws IOException {
					InputStream is = null;

					try (InputStream ){
					}
					return null;
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 6)
			try (InputStream ){
			     ^^^^^^^^^^^
		InputStream cannot be resolved to a variable
		----------
		""");
}

public static Class testClass() {
	return TryStatement9Test.class;
}
}
