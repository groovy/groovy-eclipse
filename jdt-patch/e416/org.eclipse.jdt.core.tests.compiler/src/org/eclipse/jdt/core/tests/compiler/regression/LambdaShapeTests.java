/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
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
public class LambdaShapeTests extends AbstractRegressionTest {
static {
//		TESTS_NAMES = new String[] { "test016"};
//		TESTS_NUMBERS = new int[] { 50 };
//		TESTS_RANGE = new int[] { 11, -1 };
}
public LambdaShapeTests(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface VoidI {\n" +
			"	void foo(String s);\n" +
			"}\n" +
			"class Test {\n" +
			"	public String gooVoid(VoidI i){return \"\";}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Test test = new Test();\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (false) {\n" +
			"				x += \"a\";\n" +
			"			}\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (true);\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (true) {\n" +
			"				x += \"a\";\n" +
			"			}\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			final boolean val = true;\n" +
			"			if (val) {\n" +
			"				x += \"a\";\n" +
			"			}\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			final boolean val = true;\n" +
			"			if (val);\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			final boolean val = false;\n" +
			"			if (val) {\n" +
			"				x += \"a\";\n" +
			"			}\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (x != null) {\n" +
			"				x += \"a\";\n" +
			"			}\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			final boolean val = true;\n" +
			"			if (x != null);\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (false) {\n" +
			"				x += \"a\";\n" +
			"			} else {\n" +
			"				x += \"b\";\n" +
			"			}\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (false) {\n" +
			"				x += \"a\";\n" +
			"			} else;\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			final boolean val = false;\n" +
			"			if (val) {\n" +
			"				x += \"a\";\n" +
			"			} else {\n" +
			"				x += \"b\";\n" +
			"			}\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			final boolean val = false;\n" +
			"			if (val) {\n" +
			"				x += \"a\";\n" +
			"			} else;\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (x != null) {\n" +
			"				x += \"a\";\n" +
			"			} else {\n" +
			"				x += \"b\";\n" +
			"			}\n" +
			"		});\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (x != null) {\n" +
			"				x += \"a\";\n" +
			"			} else;\n" +
			"		});\n" +
			"	}\n" +
			"}\n",
		});
}
public void test002() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x); \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		final boolean FALSE = false;\n" +
			"		goo((x) -> {\n" +
			"			if(true) return \"\";\n" +
			"			else return null;\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"			if(false) return \"\";\n" +
			"			else return null;\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"			if(x > 0) return \"\";\n" +
			"			else return null;\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"			if(FALSE) return \"\";\n" +
			"			else return null;\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"			if(!FALSE) return \"\";\n" +
			"			else return null;\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"			if(!FALSE) return \"\";\n" +
			"			else return null;\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		});
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface VoidI {\n" +
			"	void foo(String s);\n" +
			"}\n" +
			"class Test {\n" +
			"	public String gooVoid(VoidI i){return \"\";}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Test test = new Test();\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (true) {\n" +
			"				return 0;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	test.gooVoid((x) -> {\n" +
		"	     ^^^^^^^\n" +
		"The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	return 0;\n" +
		"	^^^^^^^^^\n" +
		"Void methods cannot return a value\n" +
		"----------\n");
}
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface VoidI {\n" +
			"	void foo(String s);\n" +
			"}\n" +
			"class Test {\n" +
			"	public String gooVoid(VoidI i){return \"\";}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Test test = new Test();\n" +
			"		test.gooVoid((x) -> {\n" +
			"			final boolean val = true;\n" +
			"			if (val) {\n" +
			"				return x;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	test.gooVoid((x) -> {\n" +
		"	     ^^^^^^^\n" +
		"The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	return x;\n" +
		"	^^^^^^^^^\n" +
		"Void methods cannot return a value\n" +
		"----------\n");
}
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface VoidI {\n" +
			"	void foo(String s);\n" +
			"}\n" +
			"class Test {\n" +
			"	public String gooVoid(VoidI i){return \"\";}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Test test = new Test();\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (x != null) {\n" +
			"				return 0;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	test.gooVoid((x) -> {\n" +
		"	     ^^^^^^^\n" +
		"The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	return 0;\n" +
		"	^^^^^^^^^\n" +
		"Void methods cannot return a value\n" +
		"----------\n");
}
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface VoidI {\n" +
			"	void foo(String s);\n" +
			"}\n" +
			"class Test {\n" +
			"	public String gooVoid(VoidI i){return \"\";}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Test test = new Test();\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (false) {\n" +
			"				x += \"a\";\n" +
			"			} else {\n" +
			"				return 0;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	test.gooVoid((x) -> {\n" +
		"	     ^^^^^^^\n" +
		"The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	return 0;\n" +
		"	^^^^^^^^^\n" +
		"Void methods cannot return a value\n" +
		"----------\n");
}
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface VoidI {\n" +
			"	void foo(String s);\n" +
			"}\n" +
			"class Test {\n" +
			"	public String gooVoid(VoidI i){return \"\";}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Test test = new Test();\n" +
			"		test.gooVoid((x) -> {\n" +
			"			final boolean val = false;\n" +
			"			if (val) {\n" +
			"				x += \"a\";\n" +
			"			} else {\n" +
			"				return 0;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	test.gooVoid((x) -> {\n" +
		"	     ^^^^^^^\n" +
		"The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 15)\n" +
		"	return 0;\n" +
		"	^^^^^^^^^\n" +
		"Void methods cannot return a value\n" +
		"----------\n");
}
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface VoidI {\n" +
			"	void foo(String s);\n" +
			"}\n" +
			"class Test {\n" +
			"	public String gooVoid(VoidI i){return \"\";}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Test test = new Test();\n" +
			"		test.gooVoid((x) -> {\n" +
			"			if (x != null) {\n" +
			"				x += \"a\";\n" +
			"			} else {\n" +
			"				return 0;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	test.gooVoid((x) -> {\n" +
		"	     ^^^^^^^\n" +
		"The method gooVoid(VoidI) in the type Test is not applicable for the arguments ((<no type> x) -> {})\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	return 0;\n" +
		"	^^^^^^^^^\n" +
		"Void methods cannot return a value\n" +
		"----------\n");
}
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x); \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		final boolean FALSE = false;\n" +
			"		goo((x) -> {\n" +
			"			if(FALSE) return \"\";\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x); \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			if(true);\n" +
			"			else return \"\";\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 9)\n" +
		"	else return \"\";\n" +
		"	     ^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n");
}
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x); \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			if(false) return null;\n" +
			"			else;\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	if(false) return null;\n" +
		"	          ^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	else;\n" +
		"	    ^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n");
}
public void test012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x); \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			if(x > 0) return \"\";\n" +
			"			else;\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 9)\n" +
		"	else;\n" +
		"	    ^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n");
}
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x); \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			if(x > 0);\n" +
			"			else return \"\";\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x); \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			if(x < 0) return null;\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x); \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		final boolean FALSE = false;\n" +
			"		goo((x) -> {\n" +
			"			if(!FALSE) return \"\";\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		final boolean FALSE = false;\n" +
			"		goo((x) -> {while (FALSE) throw new Exception();});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	goo((x) -> {while (FALSE) throw new Exception();});\n" +
		"	    ^^^^^^\n" +
		"This lambda expression must return a result of type String\n" +
		"----------\n");
}
public void test017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {while (false) return \"\";});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {while (false) return \"\";});\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {while (false) return \"\";});\n" +
		"	                          ^^^^^^^^^^\n" +
		"Unreachable code\n" +
		"----------\n");
}
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {while (x > 0) {\n" +
			"			if(x > 0) {return \"\";} else {break;}\n" +
			"			}});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {while (x > 0) {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	if(x > 0) {return \"\";} else {break;}\n" +
		"	                            ^^^^^^^^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n");
}
public void test019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {while (x > 0) {\n" +
			"			if(x > 0) {return \"\";}\n" +
			"		}});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {while (x > 0) {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		final boolean TRUE = true;\n" +
			"		goo((x) -> {while (TRUE) {\n" +
			"			if(x > 0) {System.out.println();}\n" +
			"			}});\n" +
			"		goo((x) -> {while (true) {\n" +
			"			if(x > 0) {System.out.println();}\n" +
			"			}});\n" +
			"	}\n" +
			"}\n"
		});
}
public void test021() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			int i = 100;\n" +
			"			outer: while(x > 0) {\n" +
			"				inner: while(i > 0) {\n" +
			"				if(--i > 50) {\n" +
			"					return \"\";\n" +
			"				}\n" +
			"				if(i > 90) {\n" +
			"					break outer;\n" +
			"				}\n" +
			"				return \"\";\n" +
			"				}\n" +
			"			}});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	inner: while(i > 0) {\n" +
		"	^^^^^\n" +
		"The label inner is never explicitly referenced\n" +
		"----------\n");
}
public void test022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	void foo(String s) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void zoo(I i) {}\n" +
			"	void test() {\n" +
			"		final boolean FALSE = false;\n" +
			"		final boolean TRUE = true;\n" +
			"		zoo((x) -> {while (TRUE) throw new Exception();});\n" +
			"		zoo((x) -> {while (!FALSE) return ;});\n" +
			"		zoo((x) -> {while (x.length() > 0) {\n" +
			"			if(x.length() > 0) {return ;} else {break;}\n" +
			"			}});\n" +
			"		zoo((x) -> {while (x.length() > 0) {\n" +
			"			if(x.length() > 0) {return ;}\n" +
			"			}});\n" +
			"		zoo((x) -> {while (true) {\n" +
			"			if(x.length() > 0) {System.out.println();}\n" +
			"			}});\n" +
			"		zoo((x) -> {while (TRUE) {\n" +
			"			if(x.length() > 0) {System.out.println();}\n" +
			"			}});\n" +
			"		zoo((x) -> {\n" +
			"			int i = 100;\n" +
			"			outer: while(x.length() > 0) {\n" +
			"				inner: while(i > 0) {\n" +
			"				if(--i > 50) {\n" +
			"					break inner ;\n" +
			"				}\n" +
			"				if(i > 90) {\n" +
			"					break outer;\n" +
			"				}\n" +
			"				return ;\n" +
			"				}\n" +
			"			}});\n" +
			"	}\n" +
			"}\n"
		});
}
public void test023() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		final boolean FALSE = false;\n" +
			"		final boolean TRUE = true;\n" +
			"		goo((x) -> {do {throw new Exception();}while (FALSE);});\n" +
			"		goo((x) -> {do { return \"\";}while (false);});\n" +
			"		goo((x) -> {do {\n" +
			"			if(x > 0) {System.out.println();}\n" +
			"			}while (true);});\n" +
			"		goo((x) -> {do {\n" +
			"			if(x > 0) {System.out.println();}\n" +
			"			}while (TRUE);});\n" +
			"	}\n" +
			"}\n"
		});
}
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {do {\n" +
			"			if(x > 0) {return \"\";} else {break;}\n" +
			"			}while (x > 0);});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {do {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	if(x > 0) {return \"\";} else {break;}\n" +
		"	                            ^^^^^^^^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n");
}
public void test025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {do {\n" +
			"			if(x > 0) {return \"\";}\n" +
			"			}while (x > 0);});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {do {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo(int x) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			int i = 100;\n" +
			"			outer: do {\n" +
			"				inner: do {\n" +
			"				if(--i > 50) {\n" +
			"					return \"\";\n" +
			"				}\n" +
			"				if(i > 90) {\n" +
			"					break outer;\n" +
			"				}\n" +
			"				return \"\";\n" +
			"				}while(i > 0);\n" +
			"			}while(x > 0);});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	inner: do {\n" +
		"	^^^^^\n" +
		"The label inner is never explicitly referenced\n" +
		"----------\n");
}
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	void foo(String s) throws Exception;\n" +
			"}\n" +
			"public class X {\n" +
			"	void zoo(I i) {}\n" +
			"	void test() {\n" +
			"		zoo((x) -> {do {\n" +
			"			if(x.length() > 0) {System.out.println();}\n" +
			"			}while (true);});\n" +
			"		zoo((x) -> {do {throw new Exception();}while (false);});\n" +
			"		zoo((x) -> {do { return ;}while (false);});\n" +
			"		zoo((x) -> {do { continue ;}while (true);});\n" +
			"		zoo((x) -> {do {\n" +
			"			if(x.length() > 0) {return ;} else {break;}\n" +
			"			}while (x.length() > 0);\n" +
			"		});\n" +
			"		zoo((x) -> {do {\n" +
			"			if(x.length() > 0) {return ;}\n" +
			"			}while (x.length() > 0);\n" +
			"		});\n" +
			"		zoo((x) -> {\n" +
			"		int i = 100;\n" +
			"		outer: do {\n" +
			"			inner: do {\n" +
			"			if(--i > 50) {\n" +
			"				break inner ;\n" +
			"			}\n" +
			"			if(i > 90) {\n" +
			"				break outer;\n" +
			"			}\n" +
			"			return ;\n" +
			"			}while(i > 0);\n" +
			"		}while(x.length() > 0);});\n" +
			"	}\n" +
			"}\n"
		});
}
public void test028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		final boolean FALSE = false; \n" +
			"		final boolean TRUE = true; \n" +
			"		goo((x) -> {\n" +
			"			for(;TRUE;){\n" +
			"			}});\n" +
			"		goo((x) -> {\n" +
			"			for(int i = 0;i < 100; i+= 10){\n" +
			"				switch(i) {\n" +
			"				case 90: {\n" +
			"					System.out.println();\n" +
			"					break;\n" +
			"				}\n" +
			"				case 80: {\n" +
			"					if(x > 10) return null;\n" +
			"					break;\n" +
			"				}\n" +
			"				default:\n" +
			"					return \"\";\n" +
			"				}\n" +
			"			}\n" +
			"			return \"\";\n" +
			"		});\n" +
			"		\n" +
			"		goo((x) -> {\n" +
			"			for(;TRUE;){\n" +
			"				if(x < 100) return \"\";\n" +
			"				else return null;\n" +
			"		}});\n" +
			"		goo((x) -> {\n" +
			"			for(;x > 0;){\n" +
			"				if(x < 100) return \"\";\n" +
			"				else return null;\n" +
			"			}\n" +
			"			return null;\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		});
}
public void test029() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		final boolean FALSE = false; \n" +
			"		goo((x) -> {\n" +
			"			for(;FALSE;){\n" +
			"			}});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This lambda expression must return a result of type String\n" +
		"----------\n");
}
public void test030() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			for(;x > 0;){\n" +
			"				if(x < 100) return \"\";\n" +
			"				else return null;\n" +
			"		}});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	else return null;\n" +
		"	     ^^^^^^^^^^^^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n");
}
public void test031() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			for(int i = 0;i < 100; i+= 10){\n" +
			"				switch(i) {\n" +
			"				case 90: {\n" +
			"					System.out.println();\n" +
			"					break;\n" +
			"				}\n" +
			"				case 80: {\n" +
			"					if(x > 10) return null;\n" +
			"					break;\n" +
			"				}\n" +
			"				default:\n" +
			"					return \"\";\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test() {\n" +
			"		goo((x) -> {\n" +
			"			outer: for(int i = 0;i < 100; i+= 10){\n" +
			"				inner : for(int j = x; j > 0; j--) {\n" +
			"					switch(i) {\n" +
			"					case 90: {\n" +
			"						System.out.println();\n" +
			"						break inner;\n" +
			"					}\n" +
			"					case 80: {\n" +
			"						if(x > 10) return null;\n" +
			"						break outer;\n" +
			"					}\n" +
			"					default:\n" +
			"						return \"\";\n" +
			"					}\n" +
			"				}\n" +
			"				\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 9)\n" +
		"	inner : for(int j = x; j > 0; j--) {\n" +
		"	                              ^^^\n" +
		"Dead code\n" +
		"----------\n");
}
public void test033() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			for(String str : strs){\n" +
			"				if(str.length() > 0) {\n" +
			"					return \"yes\";\n" +
			"				} else {\n" +
			"					return \"no\";\n" +
			"				}\n" +
			"			}\n" +
			"			return null;\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"			for(String str : strs){\n" +
			"				return \"no\";\n" +
			"			}\n" +
			"			return \"\";\n" +
			"		});\n" +
			"		\n" +
			"		goo((x) -> {\n" +
			"			for(String str : strs){\n" +
			"				if(str.length() > 0) break;\n" +
			"				System.out.println();\n" +
			"			}\n" +
			"			return \"\";\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		});
}
public void test034() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			for(String str : strs){\n" +
			"				if(str.length() > 0) {\n" +
			"					return \"yes\";\n" +
			"				} else {\n" +
			"					return \"no\";\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 11)\n" +
		"	} else {\n" +
		"					return \"no\";\n" +
		"				}\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n");
}
public void test035() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			for(String str : strs){\n" +
			"				return \"no\";\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			for(String str : strs){\n" +
			"				switch(str.length()) {\n" +
			"				case 9: {\n" +
			"					System.out.println();\n" +
			"					return \"nine\";\n" +
			"				}\n" +
			"				case 1: {\n" +
			"					if(x > 10) return null;\n" +
			"					return \"one\";\n" +
			"				}\n" +
			"				default:\n" +
			"					return \"\";\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			outer: for(String str : strs){\n" +
			"				inner : for(int j = x; j > 0; j--) {\n" +
			"					switch(str.length()) {\n" +
			"					case 9: {\n" +
			"						System.out.println();\n" +
			"						break inner;\n" +
			"					}\n" +
			"					case 8: {\n" +
			"						if(x > 10) return null;\n" +
			"						break outer;\n" +
			"					}\n" +
			"					default:\n" +
			"						return \"\";\n" +
			"					}\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 9)\n" +
		"	inner : for(int j = x; j > 0; j--) {\n" +
		"	                              ^^^\n" +
		"Dead code\n" +
		"----------\n");
}
public void test038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			switch(x) {\n" +
			"			case 0 : if(x > 10) return \">10\";\n" +
			"			case 1: return \"1\";\n" +
			"			default: return \"-1\";\n" +
			"			}\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"			String str = \"\";\n" +
			"			switch(x) {\n" +
			"			case 0 : if(x > 10) break; else {str = \"0\"; break;}\n" +
			"			case 1: str = \"1\";break;\n" +
			"			default: break;\n" +
			"			}\n" +
			"			return str;\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"			String str = \"\";\n" +
			"			switch(x){}\n" +
			"			return str;\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		});
}
public void test039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			switch(x) {\n" +
			"			case 0 : if(x > 10) return \">10\";\n" +
			"			case 1: return \"1\";\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			String str = \"\";\n" +
			"			switch(x) {\n" +
			"			case 0 : if(x > 10) break; else {str = \"0\"; break;}\n" +
			"			case 1: str = \"1\";break;\n" +
			"			default: break;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {})\n" +
		"----------\n");
}
public void test041() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			try {\n" +
			"				return \"\";\n" +
			"			} finally {\n" +
			"				\n" +
			"			}\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"				try {\n" +
			"					throw new Exception();\n" +
			"				} finally {\n" +
			"				}\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"				try {\n" +
			"					if(x > 0) \n" +
			"						throw new RuntimeException();\n" +
			"				} catch (NullPointerException e) {return null;} \n" +
			"				catch(ClassCastException c) {\n" +
			"				}\n" +
			"				finally {\n" +
			"					return \"\";\n" +
			"				}\n" +
			"		});\n" +
			"		\n" +
			"	}\n" +
			"}\n"
		});
}
public void test042() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			try {\n" +
			"				if(x > 0) {\n" +
			"					return \"\";\n" +
			"				}\n" +
			"			} finally {}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			try {\n" +
			"				return \"\";\n" +
			"			}catch (Exception e) {}\n" +
			"			finally {\n" +
			"				\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			try {\n" +
			"				//if(x > 0) \n" +
			"					throw new RuntimeException();\n" +
			"			} catch (NullPointerException e) {return null;} \n" +
			"			catch(ClassCastException c) {\n" +
			"			}\n" +
			"		});\n" +
			"		goo((x) -> {\n" +
			"			try {\n" +
			"				if(x > 0) \n" +
			"					throw new RuntimeException();\n" +
			"			} catch (NullPointerException e) {return null;} \n" +
			"			catch(ClassCastException c) {\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 15)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			try {\n" +
			"				if(x > 0) \n" +
			"					throw new RuntimeException();\n" +
			"			} catch (NullPointerException e) {return null;} \n" +
			"			catch(ClassCastException c) {\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n");
}
public void test046() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I { \n" +
			"	String foo(int x) throws Exception; \n" +
			"}\n" +
			"public class X {\n" +
			"	void goo(I i) {}\n" +
			"	void test(String[] strs) {\n" +
			"		goo((x) -> {\n" +
			"			if (true) {\n" +
			"				try {\n" +
			"					if(x > 0)\n" +
			"						throw new Exception();\n" +
			"				} finally {\n" +
			"					return \"\";\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo((x) -> {\n" +
		"	    ^^^^^^\n" +
		"This method must return a result of type String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 12)\n" +
		"	} finally {\n" +
		"					return \"\";\n" +
		"				}\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"finally block does not complete normally\n" +
		"----------\n");
}
public void testSwitch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			switch (args.length) {\n" +
			"			case 0:\n" +
			"				System.out.println(0);\n" +
			"				throw new RuntimeException();\n" +
			"			case 1:\n" +
			"				System.out.println(1);\n" +
			"				throw new RuntimeException();\n" +
			"			case 2:\n" +
			"				System.out.println(2);\n" +
			"				throw new RuntimeException();\n" +
			"			default: \n" +
			"				System.out.println(\"default\");\n" +
			"				throw new RuntimeException();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testSwitch2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			switch (args.length) {\n" +
			"			case 0:\n" +
			"				System.out.println(0);\n" +
			"				break;\n" +
			"			case 1:\n" +
			"				System.out.println(1);\n" +
			"				throw new RuntimeException();\n" +
			"			case 2:\n" +
			"				System.out.println(2);\n" +
			"				throw new RuntimeException();\n" +
			"			default: \n" +
			"				System.out.println(\"default\");\n" +
			"				throw new RuntimeException();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testSwitch3() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			switch (args.length) {\n" +
			"			case 0:\n" +
			"				System.out.println(0);\n" +
			"				throw new RuntimeException();\n" +
			"			case 1:\n" +
			"				System.out.println(1);\n" +
			"				throw new RuntimeException();\n" +
			"			case 2:\n" +
			"				System.out.println(2);\n" +
			"				throw new RuntimeException();\n" +
			"			default: \n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testSwitch4() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			switch (args.length) {\n" +
			"			case 0:\n" +
			"				System.out.println(0);\n" +
			"				throw new RuntimeException();\n" +
			"			case 1:\n" +
			"				System.out.println(1);\n" +
			"				throw new RuntimeException();\n" +
			"			case 2:\n" +
			"				System.out.println(2);\n" +
			"				throw new RuntimeException();\n" +
			"			default: \n" +
			"			    break;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testSwitch5() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			switch (args.length){\n" +
			"			case 1:\n" +
			"				if (args == null)\n" +
			"					break;\n" +
			"				else\n" +
			"					break;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testSwitch6() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			switch (args.length){\n" +
			"			case 1:\n" +
			"				if (args == null)\n" +
			"					break;\n" +
			"           throw new RuntimeException();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testWhileThis() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			boolean t = true;\n" +
			"			while (t) {\n" +
			"				System.out.println();\n" +
			"				throw new RuntimeException();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testWhile2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			while (t) {\n" +
			"				System.out.println();\n" +
			"				throw new RuntimeException();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testWhile3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			while (t && !!t) {\n" +
			"				System.out.println();\n" +
			"				throw new RuntimeException();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testWhile4() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			while (t && !!!t) {\n" +
			"				System.out.println();\n" +
			"				throw new RuntimeException();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			boolean t = true;\n" +
			"			do {\n" +
			"				System.out.println();\n" +
			"				throw new RuntimeException();\n" +
			"			} while (t);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testDo2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			do {\n" +
			"				System.out.println();\n" +
			"				throw new RuntimeException();\n" +
			"			} while (t);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testDo3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			do { \n" +
			"				System.out.println();\n" +
			"				throw new RuntimeException();\n" +
			"			} while (t && !!t);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testDo4() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			do {\n" +
			"				System.out.println();\n" +
			"			} while (t && !!!t);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo5() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				break;\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo6() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				if (args == null) break;\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo7() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				if (args == null) throw new RuntimeException();\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo8() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				throw new RuntimeException();\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testDo9() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				continue;\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testDo10() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"               if (true) \n" +
			"				    continue;\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo11() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"               if (true) \n" +
			"				    continue;\n" +
			"               else \n" +
			"                   continue;\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo12() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"               if (true) \n" +
			"				    continue;\n" +
			"               else \n" +
			"                   throw new RuntimeException();\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo13() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"               if (true) \n" +
			"                   throw new RuntimeException();\n" +
			"               else \n" +
			"                   throw new RuntimeException();\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"I");
}
public void testDo14() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"               if (true) { \n" +
			"                   System.out.println();\n" +
			"				    continue;\n" +
			"               }\n" +
			"               else {\n" +
			"                   continue;\n" +
			"               }\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo15() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			label:\n" +
			"			do {\n" +
			"				continue label;\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo16() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				blah:\n" +
			"				continue;\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo17() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				synchronized(args) {\n" +
			"				    continue;\n" +
			"               }\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testDo18() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				try {\n" +
			"					continue;\n" +
			"				} finally {\n" +
			"					throw new RuntimeException();\n" +
			"				}\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"I");
}
public void testDo19() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				try {\n" +
			"					continue;\n" +
			"				} finally {\n" +
			"				}\n" +
			"			} while (false);	\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testDo20() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				switch (args.length){\n" +
			"				default:\n" +
			"					continue;\n" +
			"				}\n" +
			"			} while (false);	\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testDo21() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				while (true) {\n" +
			"					continue;\n" +
			"				}\n" +
			"			} while (false);	\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"I");
}
public void testDo22() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			label:\n" +
			"			do {\n" +
			"				while (true) {\n" +
			"					continue label;\n" +
			"				}\n" +
			"			} while (false);	\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testDo23() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			label:\n" +
			"			while (true) {\n" +
			"				while (true) {\n" +
			"					continue label;\n" +
			"				}\n" +
			"			}	\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"I");
}
public void testDo24() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			label:\n" +
			"			do {\n" +
			"				for (;;) {\n" +
			"					continue label;\n" +
			"				}\n" +
			"			} while (false);	\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testDo25() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			label:\n" +
			"			do {\n" +
			"				do {\n" +
			"					continue label;\n" +
			"				} while (true);\n" +
			"			} while (false);	\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testDo26() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			do {\n" +
			"				label:\n" +
			"					while (true) {\n" +
			"						continue label;\n" +
			"					}\n" +
			"			} while (false);\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"I");
}
public void testForeach() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			for (String s: args) {\n" +
			"				System.out.println();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testForeach2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			for (String s: args) {\n" +
			"				System.out.println();\n" +
			"			do {\n" +
			"				System.out.println();\n" +
			"				switch (args.length) {\n" +
			"				case 0:\n" +
			"					System.out.println(0);\n" +
			"					break;\n" +
			"				case 1:\n" +
			"					System.out.println(1);\n" +
			"					throw new RuntimeException();\n" +
			"				case 2:\n" +
			"					System.out.println(2);\n" +
			"					throw new RuntimeException();\n" +
			"				default: \n" +
			"					System.out.println(\"default\");\n" +
			"					throw new RuntimeException();\n" +
			"				}\n" +
			"			} while (t);\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testForeach3() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			for (String s: args) {\n" +
			"				System.out.println();\n" +
			"			do {\n" +
			"				System.out.println();\n" +
			"				switch (args.length) {\n" +
			"				case 0:\n" +
			"					System.out.println(0);\n" +
			"					throw new RuntimeException();\n" +
			"				case 1:\n" +
			"					System.out.println(1);\n" +
			"					throw new RuntimeException();\n" +
			"				case 2:\n" +
			"					System.out.println(2);\n" +
			"					throw new RuntimeException();\n" +
			"				default: \n" +
			"					System.out.println(\"default\");\n" +
			"					throw new RuntimeException();\n" +
			"				}\n" +
			"			} while (t);\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testForeach4() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			for (String s: args) {\n" +
			"				System.out.println();\n" +
			"				throw new RuntimeException();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testIf() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			if (t) \n" +
			"               throw new RuntimeException();\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testIf2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			if (true) \n" +
			"               throw new RuntimeException();\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testIf3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			if (true) \n" +
			"               throw new RuntimeException();\n" +
			"           else \n" +
			"               throw new RuntimeException();\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testCFor() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			boolean t = true;\n" +
			"			for (; t ;) { \n" +
			"               throw new RuntimeException();\n" +
			"           }\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testCFor2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			final boolean t = true;\n" +
			"			for (; t ;) { \n" +
			"               throw new RuntimeException();\n" +
			"           }\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testTry() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"           try {\n" +
			"           } finally {\n" +
			"               throw new RuntimeException();\n" +
			"           }\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testTry2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"           try {\n" +
			"           } finally {\n" +
			"           }\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testTry3() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"           try {\n" +
			"           } catch (RuntimeException e) {\n" +
			"               throw new RuntimeException();\n" +
			"           }\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testTry4() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   static void goo(I i) {\n" +
			"		System.out.println(\"goo(I)\");\n" +
			"   }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"           try {\n" +
			"               throw new RuntimeException();\n" +
			"           } catch (RuntimeException e) {\n" +
			"               throw new RuntimeException();\n" +
			"           }\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testWhileTrue() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"            System.out.println(\"goo(I)\");\n" +
			"        }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			while (true) {\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testWhileTrue2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"            System.out.println(\"goo(I)\");\n" +
			"        }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			while (true) {\n" +
			"			    while (true) {\n" +
			"                   if (args == null) break;\n" +
			"			    }\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"goo(I)");
}
public void testWhileTrue3() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"            System.out.println(\"goo(I)\");\n" +
			"        }\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			while (true) {\n" +
			"                   if (args == null) break;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	goo(() -> {\n" +
		"	^^^\n" +
		"The method goo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n");
}
public void testLabeledStatement() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			label: \n" +
			"			while (true) {\n" +
			"				while (true) {\n" +
			"					break label;\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testLabeledStatement2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			outerlabel: \n" +
			"			label: \n" +
			"			while (true) {\n" +
			"				while (true) {\n" +
			"					break outerlabel;\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testLabeledStatement3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			outerlabel: \n" +
			"			label: \n" +
			"			while (true) {\n" +
			"				while (true) {\n" +
			"					break outerlabel;\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testLabeledStatement4() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			outerlabel: \n" +
			"			label: \n" +
			"			while (true) {\n" +
			"				while (true) {\n" +
			"					break label;\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"J");
}
public void testLabeledStatement5() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {\n" +
			"		System.out.println(\"I\");\n" +
			"	}\n" +
			"	static void goo(J i) {\n" +
			"		System.out.println(\"J\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo(() -> {\n" +
			"			outerlabel: \n" +
			"			label: \n" +
			"			while (true) {\n" +
			"				while (true) {\n" +
			"					break;\n" +
			"				}\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"I");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=470232 NPE at org.eclipse.jdt.internal.compiler.ast.WhileStatement.doesNotCompleteNormally
public void testBug470232_While() {
	this.runConformTest(
		new String[] {
			"While.java",
			"import java.util.function.Consumer;\n" +
			"class While {\n" +
			"    void m() {\n" +
			"        t(Long.class, value -> {\n" +
			"            int x = 1;\n" +
			"            while (--x >= 0)\n" +
			"                ;\n" +
			"        });\n" +
			"    }\n" +
			"    <T> void t(Class<T> clazz, Consumer<T> object) {\n" +
			"    }\n" +
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=470232 NPE at org.eclipse.jdt.internal.compiler.ast.WhileStatement.doesNotCompleteNormally
public void testBug470232_Do() {
	this.runConformTest(
		new String[] {
			"While.java",
			"import java.util.function.Consumer;\n" +
			"class While {\n" +
			"    void m() {\n" +
			"        t(Long.class, value -> {\n" +
			"            int x = 1;\n" +
			"            do {\n" +
			"            }while (--x >= 0);\n" +
			"        });\n" +
			"    }\n" +
			"    <T> void t(Class<T> clazz, Consumer<T> object) {\n" +
			"    }\n" +
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=470232 NPE at org.eclipse.jdt.internal.compiler.ast.WhileStatement.doesNotCompleteNormally
public void testBug470232_For() {
	this.runConformTest(
		new String[] {
			"While.java",
			"import java.util.function.Consumer;\n" +
			"class While {\n" +
			"    void m() {\n" +
			"        t(Long.class, value -> {\n" +
			"            int x = 1;\n" +
			"            for(;--x >= 0;)\n" +
			"            	;\n" +
			"        });\n" +
			"    }\n" +
			"    <T> void t(Class<T> clazz, Consumer<T> object) {\n" +
			"    }\n" +
			"}\n"
		});
}
public static Class testClass() {
	return LambdaShapeTests.class;
}
}
