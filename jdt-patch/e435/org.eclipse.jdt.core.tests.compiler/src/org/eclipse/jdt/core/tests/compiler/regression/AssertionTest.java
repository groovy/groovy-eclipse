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
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

@SuppressWarnings({ "rawtypes" })
public class AssertionTest extends AbstractRegressionTest {
//	 Static initializer to specify tests subset using TESTS_* static variables
//	 All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 13, 14 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}
	public AssertionTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
	}

	public static Class testClass() {
		return AssertionTest.class;
	}

	public void test001() {
		this.runNegativeTest(
			new String[] {
				"assert.java",
				"public class assert {}\n",
			},
			"----------\n" +
			"1. ERROR in assert.java (at line 1)\n" +
			"	public class assert {}\n" +
			"	             ^^^^^^\n" +
			"Syntax error on token \"assert\", Identifier expected\n" +
			"----------\n");
	}

	public void test002() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "    int i = 4;\n"
			+ "    assert i != 4;\n"
			+ "	   System.out.println(i);\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(\"SUCCESS\");	\n"
			+ "	  } \n"
			+ "	} \n"
			+ "} \n" },
		"SUCCESS", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}

	public void test003() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "    int i = 4;\n"
			+ "    assert i != 4;\n"
			+ "	   System.out.println(i);\n"
			+ "	} \n"
			+ "} \n" },
		"4",
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-da"});
	}
	public void test004() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : \"SUC\";	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		assert false : new Object(){ public String toString(){ return \"CESS\";}};	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"SUCCESS", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test005() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		int i = 2;	\n"
			+ "		assert false : i;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"12", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test006() {
		this.runNegativeTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "	  try {	\n"
			+ "		assert false : unbound;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"----------\n" +
		"1. ERROR in A4.java (at line 4)\n" +
		"	assert false : unbound;	\n" +
		"	               ^^^^^^^\n" +
		"unbound cannot be resolved to a variable\n" +
		"----------\n");
	}
	public void test007() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1L;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "   try {	\n"
			+ "		assert false : 0L;	\n" // 0L isn't 0
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		long l = 2L;	\n"
			+ "		assert false : l;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"102", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test008() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1.0f;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		float f = 2.0f;	\n"
			+ "		assert false : f;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"1.02.0", //expected display
		null, // use default class-path
		true, // do not flush previous output dir content
		new String[] {"-ea"});
	}
	public void test009() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1.0;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		double d = 2.0;	\n"
			+ "		assert false : d;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"1.02.0", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	// http://dev.eclipse.org/bugs/show_bug.cgi?id=22334
	public void test010() {
		this.runConformTest(new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) { \n" +
			"		I.Inner inner = new I.Inner(); \n" +
			"		try { \n" +
			"			inner.test(); \n" +
			"			System.out.println(\"FAILED\"); \n" +
			"		} catch(AssertionError e){ \n" +
			"			System.out.println(\"SUCCESS\"); \n" +
			"		} \n" +
			"	} \n" +
			"} \n" +
			"interface I { \n" +
			"  public static class Inner { \n" +
			"    public void test() { \n" +
			"      assert false; \n" +
			"    } \n" +
			"  } \n" +
			"} \n" },
		"SUCCESS",
		null, // use default classpath
		true, // flush previous output dir content
		new String[] {"-ea"});
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28750
	 */
	public void test011() {
		this.runConformTest(
			new String[] {
				"AssertTest.java",
				"public class AssertTest {\n" +
				"   public AssertTest() {}\n" +
				"   public class InnerClass {\n" +
				"      InnerClass() {\n" +
				"        assert(false);\n" +
				"      }\n" +
				"   }\n" +
				"   \n" +
				"   public static void main(String[] args) {	\n" +
				"        System.out.print(\"SUCCESS\");	\n" +
				"	}	\n" +
				"}"
			},
			"SUCCESS"); // expected output
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=57743
	 */
	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main( String[] args ) {\n" +
				"        try {\n" +
				"            throw new Throwable( \"This is a test\");\n" +
				"        }\n" +
				"        catch( Throwable ioe ) {\n" +
				"            assert false : ioe;\n" +
				"        }\n" +
				"        System.out.print(\"SUCCESS\");	\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS"); // expected output
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=157389
	 */
	public void test013() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"        static class Y {\n" +
				"                public static void test() {\n" +
				"                        assert false;\n" +
				"                        System.out.println(\"SUCCESS\");\n" +
				"                }\n" +
				"        }\n" +
				"        public static void main(String[] args) {\n" +
				"                ClassLoader classLoader = new X().getClass().getClassLoader();\n" +
				"                // enable assertion for X.Y\n" +
				"                classLoader.setClassAssertionStatus(\"X$Y\", true);\n" +
				"                X.Y.test();\n" +
				"        }\n" +
				"}"
			},
			"SUCCESS"); // expected output
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=163600
	 */
	public void test014() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static class Foo {\n" +
				"		public void myMethod(boolean trash) {\n" +
				"			System.out.println(\"Expecting class Foo\");\n" +
				"			Class c = Foo.class;\n" +
				"			System.out.println(\"Got the class \" + c);\n" +
				"		}\n" +
				"	}\n" +
				"	public static class Bar {\n" +
				"		public void myMethod(boolean doAssert) {\n" +
				"			System.out.println(\"Expecting class Bar\");\n" +
				"			Class c = Bar.class;\n" +
				"			System.out.println(\"Got the class \" + c);\n" +
				"			assert c.getName().endsWith(\"Bar\");\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new Foo().myMethod(false);\n" +
				"		new Bar().myMethod(false);\n" +
				"	}\n" +
				"}"
			},
			"Expecting class Foo\n" +
			"Got the class class X$Foo\n" +
			"Expecting class Bar\n" +
			"Got the class class X$Bar"); // expected output
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=163600
	 */
	public void test015() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static class Foo {\n" +
				"		public void myMethod(boolean trash) {\n" +
				"			System.out.println(\"Expecting class Foo\");\n" +
				"			Class c = Foo.class;\n" +
				"			System.out.println(\"Got the class \" + c);\n" +
				"		}\n" +
				"	}\n" +
				"	public static class Bar {\n" +
				"		public void myMethod(boolean doAssert) {\n" +
				"			System.out.println(\"Expecting class Bar\");\n" +
				"			Class c = Bar.class;\n" +
				"			try {\n" +
				"				assert c.getName().endsWith(\"Bar2\");\n" +
				"			} catch(AssertionError e) {\n" +
				"				System.out.println(\"SUCCESS\");\n" +
				"			}\n" +
				"			System.out.println(\"Got the class \" + c);\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new Foo().myMethod(false);\n" +
				"		new Bar().myMethod(false);\n" +
				"	}\n" +
				"}"
			},
			"Expecting class Foo\n" +
			"Got the class class X$Foo\n" +
			"Expecting class Bar\n" +
			"SUCCESS\n" +
			"Got the class class X$Bar",
			null, // use default classpath
			true, // flush previous output dir content
			new String[] {"-ea"});
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=163600
	 */
	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static class Foo {\n" +
				"		public void myMethod(boolean trash) {\n" +
				"			System.out.println(\"Expecting class Foo\");\n" +
				"			Class c = Foo.class;\n" +
				"			System.out.println(\"Got the class \" + c);\n" +
				"		}\n" +
				"	}\n" +
				"	public static class Bar {\n" +
				"		public void myMethod(boolean doAssert) {\n" +
				"			System.out.println(\"Expecting class Bar\");\n" +
				"			Class c = Bar.class;\n" +
				"			try {\n" +
				"				assert c.getName().endsWith(\"Bar2\");\n" +
				"				System.out.println(\"SUCCESS\");\n" +
				"			} catch(AssertionError e) {\n" +
				"				System.out.println(\"FAILED\");\n" +
				"			}\n" +
				"			System.out.println(\"Got the class \" + c);\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new Foo().myMethod(false);\n" +
				"		new Bar().myMethod(false);\n" +
				"	}\n" +
				"}"
			},
			"Expecting class Foo\n" +
			"Got the class class X$Foo\n" +
			"Expecting class Bar\n" +
			"SUCCESS\n" +
			"Got the class class X$Bar",
			null, // use default classpath
			true, // flush previous output dir content
			new String[] {"-da"});
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255008
	public void test017() {
		runNegativeTest(
			new String[] { /* test files */
				"X.java",
				"public class X {\n" +
				"	protected void transform1(boolean srcPts) {\n" +
				"		final float error1;\n" +
				"		assert !(srcPts && (error1 = maxError()) > 0) : error1;\n" +
				"	}\n" +
				"	float foo1(boolean srcPts) {\n" +
				"		final float error2;\n" +
				"		if (!(srcPts && (error2 = maxError()) > 0)) {\n" +
				"		} else {\n" +
				"			return error2;\n" +
				"		}\n" +
				"		return 0;\n" +
				"	}\n" +
				"	float bar1(boolean srcPts) {\n" +
				"		final float error3;\n" +
				"		if ((srcPts && (error3 = maxError()) > 0)) {\n" +
				"			return error3;\n" +
				"		}\n" +
				"		return 0;\n" +
				"	}	\n" +
				"	protected void transform2(boolean srcPts) {\n" +
				"		final float error4;\n" +
				"		assert (srcPts && (error4 = maxError()) > 0) : error4;\n" +
				"	}\n" +
				"	float foo2(boolean srcPts) {\n" +
				"		final float error5;\n" +
				"		if (srcPts && (error5 = maxError()) > 0) {\n" +
				"		} else {\n" +
				"			return error5;\n" +
				"		}\n" +
				"		return 0;\n" +
				"	}\n" +
				"	float bar2(boolean srcPts) {\n" +
				"		final float error6;\n" +
				"		if (!(srcPts && (error6 = maxError()) > 0)) {\n" +
				"			return error6;\n" +
				"		}\n" +
				"		return 0;\n" +
				"	}\n" +
				"	private float maxError() {\n" +
				"		return 0;\n" +
				"	}\n" +
				"\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 23)\n" +
			"	assert (srcPts && (error4 = maxError()) > 0) : error4;\n" +
			"	                                               ^^^^^^\n" +
			"The local variable error4 may not have been initialized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 29)\n" +
			"	return error5;\n" +
			"	       ^^^^^^\n" +
			"The local variable error5 may not have been initialized\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 36)\n" +
			"	return error6;\n" +
			"	       ^^^^^^\n" +
			"The local variable error6 may not have been initialized\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test018() {
		this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"    static final int i;\n" +
			"    static {\n" +
			"        assert (i = 0) == 0;\n" +
			"        System.out.println(i);\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	static final int i;\n" +
		"	                 ^\n" +
		"The blank final field i may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	System.out.println(i);\n" +
		"	                   ^\n" +
		"The blank final field i may not have been initialized\n" +
		"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test019() {
		this.runConformTest(new String[] {
			"X.java",
			"public class X {\n" +
			"    static final int i;\n" +
			"    static {\n" +
			"        i = 0;\n" +
			"        assert i == 0;\n" +
			"        System.out.println(i);\n" +
			"    }\n" +
			"}"
		},
		"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test020() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n" +
						"    void method1() {\n" +
						"		 int i;" +
						"        assert (i = 0) == 0;\n" +
						"        System.out.println(i);\n" +
						"    }\n" +
						"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	System.out.println(i);\n" +
			"	                   ^\n" +
			"The local variable i may not have been initialized\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test021() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
					"	public int bar() {\n" +
					"		return 1;\n" +
					"	}\n" +
					"    void method1() {\n" +
						"		 int i;" +
						"        assert (i = this.bar()) == 0;\n" +
						"        System.out.println(i);\n" +
						"    }\n" +
						"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	System.out.println(i);\n" +
			"	                   ^\n" +
			"The local variable i may not have been initialized\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test022() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
					"	public int bar() {\n" +
					"		return 1;\n" +
					"	}\n" +
					"    void method1() {\n" +
						"		 int i;\n" +
						"        assert i++ == 0;\n" +
						"        System.out.println(i);\n" +
						"    }\n" +
						"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	assert i++ == 0;\n" +
			"	       ^\n" +
			"The local variable i may not have been initialized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	System.out.println(i);\n" +
			"	                   ^\n" +
			"The local variable i may not have been initialized\n" +
			"----------\n");
	}
	public void test023() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(new String[] {"X.java",
				"interface Foo {\n" +
				"  default Object test(Object a) {\n" +
				"    assert a != null; // triggers creation of bogus synthetic field\n" +
				"    return a;\n" +
				"  }\n" +
				"}\n" +
				"public class X implements Foo {\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().test(\"\");\n" +
				"		System.out.println(\"Hello\");\n" +
				"	}\n" +
				"}\n"}, "Hello");
	}
}
