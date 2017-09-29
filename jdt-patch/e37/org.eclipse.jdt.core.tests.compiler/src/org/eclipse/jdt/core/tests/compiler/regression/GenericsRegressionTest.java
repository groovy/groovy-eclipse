/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 282152 - [1.5][compiler] Generics code rejected by Eclipse but accepted by javac
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericsRegressionTest extends AbstractComparableTest {

	public GenericsRegressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test347426" };
//		TESTS_NAMES = new String[] { "test1464" };
//		TESTS_NUMBERS = new int[] { 1465 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return GenericsRegressionTest.class;
	}

	protected Map getCompilerOptions() {
		Map compilerOptions = super.getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		return compilerOptions;
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void _test322531a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz) {\n" +
				"        boolean b = \n" +
				"            clazz == clazz || \n" +
				"            X.class == X.class || \n" +
				"            I.class == I.class || \n" +
				"            clazz == X.class || \n" +
				"            X.class == clazz || \n" +
				"            clazz == I.class || \n" +
				"            I.class == clazz || \n" +
				"            I.class == X.class ||\n" +
				"            X.class == I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	clazz == clazz || \n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	clazz == X.class || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<X>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	X.class == clazz || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<T>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 12)\n" + 
			"	I.class == X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	X.class == I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X implements I {\n" +
				"    <T extends I> void main(Class<T> clazz) {\n" +
				"        boolean b = \n" +
				"            clazz == clazz || \n" +
				"            X.class == X.class || \n" +
				"            I.class == I.class || \n" +
				"            clazz == X.class || \n" +
				"            X.class == clazz || \n" +
				"            clazz == I.class || \n" +
				"            I.class == clazz || \n" +
				"            I.class == X.class ||\n" +
				"            X.class == I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	clazz == clazz || \n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 12)\n" + 
			"	I.class == X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 13)\n" + 
			"	X.class == I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public final class X {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	x.getClass() == clazz || \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<capture#1-of ? extends X> and Class<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	clazz == x.getClass(); \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<capture#2-of ? extends X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531e() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public final class X implements I {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	x.getClass() == clazz || \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<capture#1-of ? extends X> and Class<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	clazz == x.getClass(); \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<capture#2-of ? extends X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void _test322531g() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface List<E> {}\n" +
				"interface I {}\n" +
				"public class X implements I {\n" +
				"    void main(List<I> li, X t) {\n" +
				"        boolean b = I.class == t.getClass();\n" +
				"	         b = li == t.getList();\n" +
				"    }\n" +
				"    \n" +
				"    List<? extends Object> getList() {\n" +
				"    	return null;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	boolean b = I.class == t.getClass();\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<capture#1-of ? extends X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void _test322531h() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X implements I {\n" +
				"    <T extends I> void main(Class<T> clazz, X t) {\n" +
				"        boolean b = \n" +
				"            clazz == t.getClass() || \n" +
				"            t.getClass() == clazz || \n" +
				"            I.class == t.getClass() ||\n" +
				"            t.getClass() == I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	I.class == t.getClass() ||\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<capture#3-of ? extends X>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	t.getClass() == I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<capture#4-of ? extends X> and Class<I>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531i() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {};\n" +
				"public class X {\n" +
				"    public X() {\n" +
				"    }\n" +
				"    public <T extends I> void test(Class<T> clazz) {\n" +
				"        Class<I> ci = I.class;\n" +
				"        Class<X> ti = X.class;\n" +
				"        boolean b = ci == X.class ||\n" +
				"        	        X.class == ci ||\n" +
				"        			I.class == X.class ||\n" +
				"        			X.class == I.class ||\n" +
				"        			ti == I.class ||\n" +
				"        			I.class == ti ||\n" +
				"        			ti == ci ||\n" +
				"        			ci == ti;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	boolean b = ci == X.class ||\n" + 
			"	            ^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	X.class == ci ||\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 10)\n" + 
			"	I.class == X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 11)\n" + 
			"	X.class == I.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 12)\n" + 
			"	ti == I.class ||\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 13)\n" + 
			"	I.class == ti ||\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 14)\n" + 
			"	ti == ci ||\n" + 
			"	^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 15)\n" + 
			"	ci == ti;\n" + 
			"	^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void _test322531j() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz) {\n" +
				"        boolean b = \n" +
				"            clazz != clazz || \n" +
				"            X.class != X.class || \n" +
				"            I.class != I.class || \n" +
				"            clazz != X.class || \n" +
				"            X.class != clazz || \n" +
				"            clazz != I.class || \n" +
				"            I.class != clazz || \n" +
				"            I.class != X.class ||\n" +
				"            X.class != I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	clazz != clazz || \n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	clazz != X.class || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<X>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	X.class != clazz || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<T>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 12)\n" + 
			"	I.class != X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	X.class != I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n");
	}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
public void test282152() {
    this.runConformTest(
        new String[] {
            "Test.java",
            "public interface Test<T extends Number> {\n" +
            "    public <U> void test(Test<? super U> t, U value);\n" +
            "    public void setValue(T v);" +
            "}",
            "Impl.java",
            "public class Impl<T extends Number> implements Test<T>{\n" +
            "    T val;" +
            "    public <U> void test(Test<? super U> t, U value) {\n" +
            "        t.setValue(value);\n" +
            "    }\n" +
            "    public void setValue(T v) {\n" +
            "        this.val = v;\n" +
            "    }\n" +
            "}",
            "Client.java",
            "public class Client {\n" +
            "    void test() {\n" +
            "        Impl<Integer> t1 = new Impl<Integer>();\n" +
            "        Double n = Double.valueOf(3.14);\n" +
            "        t1.test(new Impl<Number>(), n);\n" +
            "    }\n" +
            "}\n"
        },
        ""); // no specific success output string
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// violating lower bound
public void test282152b() {
    this.runNegativeTest(
        new String[] {
            "Test.java",
            "public interface Test<T extends Number> {\n" +
            "    public <U> void test(Test<? super U> t, U value);\n" +
            "    public void setValue(T v);" +
            "}",
            "Impl.java",
            "public class Impl<T extends Number> implements Test<T>{\n" +
            "    T val;" +
            "    public <U> void test(Test<? super U> t, U value) {\n" +
            "        t.setValue(value);\n" +
            "    }\n" +
            "    public void setValue(T v) {\n" +
            "        this.val = v;\n" +
            "    }\n" +
            "}",
            "Client.java",
            "public class Client {\n" +
            "    void test() {\n" +
            "        Impl<Integer> t1 = new Impl<Integer>();\n" +
            "        Number n = Double.valueOf(3.14);\n" +
            "        t1.test(new Impl<Double>(), n);\n" +
            "    }\n" +
            "}\n"
        },
        "----------\n" + 
		"1. ERROR in Client.java (at line 5)\n" + 
		"	t1.test(new Impl<Double>(), n);\n" + 
		"	   ^^^^\n" + 
		"The method test(Test<? super U>, U) in the type Impl<Integer> is not applicable for the arguments (Impl<Double>, Number)\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// contradictory bounds
public void test282152c() {
    this.runNegativeTest(
        new String[] {
            "Test.java",
            "public interface Test<T extends Number> {\n" +
            "    public <U extends Exception> void test(Test<? super U> t, U value);\n" +
            "    public void setValue(T v);" +
            "}"
        },
        "----------\n" + 
		"1. ERROR in Test.java (at line 2)\n" + 
		"	public <U extends Exception> void test(Test<? super U> t, U value);\n" + 
		"	                                            ^^^^^^^^^\n" + 
		"Bound mismatch: The type ? super U is not a valid substitute for the bounded parameter <T extends Number> of the type Test<T>\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// compatible constraints
public void test282152d() {
    this.runConformTest(
        new String[] {
            "Test.java",
            "public interface Test<T extends Number> {\n" +
            "    public <U extends Integer> void test(Test<? super U> t, U value);\n" +
            "    public void setValue(T v);" +
            "}",
            "Impl.java",
            "public class Impl<T extends Number> implements Test<T>{\n" +
            "    T val;" +
            "    public <U extends Integer> void test(Test<? super U> t, U value) {\n" +
            "        t.setValue(value);\n" +
            "    }\n" +
            "    public void setValue(T v) {\n" +
            "        this.val = v;\n" +
            "    }\n" +
            "}",
            "Client.java",
            "public class Client {\n" +
            "    void test() {\n" +
            "        Impl<Integer> t1 = new Impl<Integer>();\n" +
            "        Integer i = Integer.valueOf(3);\n" +
            "        t1.test(new Impl<Integer>(), i);\n" +
            "    }\n" +
            "}\n"
        },
        ""); // no specific success output string
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// direct use of type variable does not involve capture, thus no merging of constraints happens
public void test282152e() {
	this.runNegativeTest(
	    new String[] {
	        "Test.java",
	        "public interface Test<T extends Number> {\n" +
	        "    public <U> void test(Test<U> t, U value);\n" +
	        "    public void setValue(T v);" +
	        "}"
	    },
	    "----------\n" + 
		"1. ERROR in Test.java (at line 2)\n" + 
		"	public <U> void test(Test<U> t, U value);\n" + 
		"	                          ^\n" + 
		"Bound mismatch: The type U is not a valid substitute for the bounded parameter <T extends Number> of the type Test<T>\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330869
public void test330869() {
    this.runConformTest(
            new String[] {
                    "X.java",
                    "public class X {\n" +
                    "    public <T> T getAdapter(Class<? extends T> adapterType) {\n" +
                    "        T result = null;\n" +
                    "        if (adapterType == Foo.class) {\n" +
                    "        }\n" +
                    "        else if (adapterType == Bar.class) {\n" +
                    "        }\n" +
                    "        return  result;\n" +
                    "     }\n" +
                    "     public class Foo {\n" +
                    "     }\n" +
                    "     public interface Bar {\n" +
                    "     }\n" +
                    "}\n"
            },
            ""); // no specific success output string
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface Adaptable {\n" +
					"    public Object getAdapter(Class clazz);    \n" +
					"}\n" +
					"public class X implements Adaptable {\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	public Object getAdapter(Class clazz);    \n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface Adaptable {\n" +
					"    public Object getAdapter(Class clazz);    \n" +
					"}\n" +
					"public class X implements Adaptable {\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	public Object getAdapter(Class clazz);    \n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	public Object getAdapter(Class clazz) {\n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface Adaptable {\n" +
					"    public Object getAdapter(Class<String> clazz);    \n" +
					"}\n" +
					"public class X implements Adaptable {\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	public Object getAdapter(Class clazz) {\n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface Adaptable {\n" +
					"    public Object getAdapter(Class<String> clazz);    \n" +
					"}\n" +
					"public class X implements Adaptable {\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n" +
					"class Y extends X {\n" +
					"    @Override\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n"

			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	public Object getAdapter(Class clazz) {\n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817e() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"class Top {\n" +
					"    public void set(List arg) { } // OK to warn in 1.5 code\n" +
					"    public List get() { return null; } // OK to warn in 1.5 code\n" +
					"}\n" +
					"class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) { // should not warn (overrides)\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() { // should not warn (overrides)\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n" +
					"public class X {\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public void set(List arg) { } // OK to warn in 1.5 code\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public List get() { return null; } // OK to warn in 1.5 code\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817f() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"class Top {\n" +
					"    public void set(List arg) { } // OK to warn in 1.5 code\n" +
					"    public List<String> get() { return null; }\n" +
					"}\n" +
					"class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) { // should not warn (overrides)\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() { // should warn (super's return type is not raw)\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n" +
					"public class X {\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public void set(List arg) { } // OK to warn in 1.5 code\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 11)\n" + 
			"	public List get() { // should warn (super\'s return type is not raw)\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 11)\n" + 
			"	public List get() { // should warn (super\'s return type is not raw)\n" + 
			"	       ^^^^\n" + 
			"Type safety: The return type List for get() from the type Sub needs unchecked conversion to conform to List<String> from the type Top\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (Disable reporting of unavoidable problems)
public void test322817g() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"Top.java",
					"import java.util.List;\n" +
					"public class Top {\n" +
					"    public void set(List arg) { } // OK to warn in 1.5 code\n" +
					"    public List get() { return null; } // OK to warn in 1.5 code\n" +
					"    List list; // OK to warn in 1.5 code\n" +
					"}\n",
					"Sub.java",
					"import java.util.List;\n" +
					"public class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) { // should not warn (overrides)\n" +
					"        super.set(arg);\n" +
					"        arg.set(0, \"A\"); // should not warn ('arg' is forced raw)\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() { // should not warn (overrides)\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n",
					"X.java",
					"import java.util.List;\n" +
					"public class X {\n" +
					"    void run() {\n" +
					"        new Top().list.add(\"arg\"); // should not warn (uses raw field declared elsewhere)\n" +
					"        new Top().get().add(\"arg\"); // should not warn (uses raw API)\n" +
					"        List raw= new Top().get(); // OK to warn ('raw' declared here)\n" +
					"        raw.add(\"arg\"); // OK to warn ('raw' declared here)\n" +
					"        // When Top#get() is generified, both of the following will fail\n" +
					"        // with a compile error if type arguments don't match:\n" +
					"        List<String> unchecked= new Top().get(); // should not warn (forced)\n" +
					"        unchecked.add(\"x\");\n" +
					"        // Should not warn about unchecked cast, but should warn about\n" +
					"        // unnecessary cast:\n" +
					"        List<String> cast= (List<String>) new Top().get();\n" +
					"        cast.add(\"x\");\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in Top.java (at line 3)\n" + 
			"	public void set(List arg) { } // OK to warn in 1.5 code\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Top.java (at line 4)\n" + 
			"	public List get() { return null; } // OK to warn in 1.5 code\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Top.java (at line 5)\n" + 
			"	List list; // OK to warn in 1.5 code\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	List raw= new Top().get(); // OK to warn (\'raw\' declared here)\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 7)\n" + 
			"	raw.add(\"arg\"); // OK to warn (\'raw\' declared here)\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 14)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from List to List<String>\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (Enable reporting of unavoidable problems)
public void test322817h() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
			new String[] {
					"Top.java",
					"import java.util.List;\n" +
					"public class Top {\n" +
					"    public void set(List arg) { }\n" +
					"    public List get() { return null; }\n" +
					"    List list;\n" +
					"}\n",
					"Sub.java",
					"import java.util.List;\n" +
					"public class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) {\n" +
					"        super.set(arg);\n" +
					"        arg.set(0, \"A\");\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() {\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n",
					"X.java",
					"import java.util.List;\n" +
					"public class X {\n" +
					"    void run() {\n" +
					"        new Top().list.add(\"arg\");\n" +
					"        new Top().get().add(\"arg\");\n" +
					"        List raw= new Top().get();\n" +
					"        raw.add(\"arg\");\n" +
					"        List<String> unchecked= new Top().get();\n" +
					"        unchecked.add(\"x\");\n" +
					"        List<String> cast= (List<String>) new Top().get();\n" +
					"        cast.add(\"x\");\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in Top.java (at line 3)\n" + 
			"	public void set(List arg) { }\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Top.java (at line 4)\n" + 
			"	public List get() { return null; }\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Top.java (at line 5)\n" + 
			"	List list;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in Sub.java (at line 4)\n" + 
			"	public void set(List arg) {\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Sub.java (at line 6)\n" + 
			"	arg.set(0, \"A\");\n" + 
			"	^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method set(int, Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Sub.java (at line 9)\n" + 
			"	public List get() {\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	new Top().list.add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	new Top().get().add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 6)\n" + 
			"	List raw= new Top().get();\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 7)\n" + 
			"	raw.add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 8)\n" + 
			"	List<String> unchecked= new Top().get();\n" + 
			"	                        ^^^^^^^^^^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 10)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: Unchecked cast from List to List<String>\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 10)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from List to List<String>\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (Default options)
public void test322817i() {
	Map customOptions = getCompilerOptions();
	this.runNegativeTest(
			new String[] {
					"Top.java",
					"import java.util.List;\n" +
					"public class Top {\n" +
					"    public void set(List arg) { }\n" +
					"    public List get() { return null; }\n" +
					"    List list;\n" +
					"}\n",
					"Sub.java",
					"import java.util.List;\n" +
					"public class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) {\n" +
					"        super.set(arg);\n" +
					"        arg.set(0, \"A\");\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() {\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n",
					"X.java",
					"import java.util.List;\n" +
					"public class X {\n" +
					"    void run() {\n" +
					"        new Top().list.add(\"arg\");\n" +
					"        new Top().get().add(\"arg\");\n" +
					"        List raw= new Top().get();\n" +
					"        raw.add(\"arg\");\n" +
					"        List<String> unchecked= new Top().get();\n" +
					"        unchecked.add(\"x\");\n" +
					"        List<String> cast= (List<String>) new Top().get();\n" +
					"        cast.add(\"x\");\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in Top.java (at line 3)\n" + 
			"	public void set(List arg) { }\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Top.java (at line 4)\n" + 
			"	public List get() { return null; }\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Top.java (at line 5)\n" + 
			"	List list;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in Sub.java (at line 4)\n" + 
			"	public void set(List arg) {\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Sub.java (at line 6)\n" + 
			"	arg.set(0, \"A\");\n" + 
			"	^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method set(int, Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Sub.java (at line 9)\n" + 
			"	public List get() {\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	new Top().list.add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	new Top().get().add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 6)\n" + 
			"	List raw= new Top().get();\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 7)\n" + 
			"	raw.add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 8)\n" + 
			"	List<String> unchecked= new Top().get();\n" + 
			"	                        ^^^^^^^^^^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 10)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: Unchecked cast from List to List<String>\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 10)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from List to List<String>\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (all in same file)
public void test322817j() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"class Top {\n" +
					"    public void set(List arg) { } // OK to warn in 1.5 code\n" +
					"    public List get() { return null; } // OK to warn in 1.5 code\n" +
					"}\n" +
					"class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) { // should not warn (overrides)\n" +
					"        super.set(arg);\n" +
					"        arg.set(0, \"A\"); // should not warn ('arg' is forced raw)\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() { // should not warn (overrides)\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n" +
					"public class X {\n" +
					"    void run() {\n" +
					"        new Top().get().add(\"arg\");\n" +
					"        List raw= new Top().get(); // OK to warn ('raw' declared here)\n" +
					"        raw.add(\"arg\"); // OK to warn ('raw' declared here)\n" +
					"        List<String> unchecked= new Top().get();\n" +
					"        unchecked.add(\"x\");\n" +
					"        List<String> cast= (List<String>) new Top().get();\n" +
					"        cast.add(\"x\");\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public void set(List arg) { } // OK to warn in 1.5 code\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public List get() { return null; } // OK to warn in 1.5 code\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 19)\n" + 
			"	new Top().get().add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 20)\n" + 
			"	List raw= new Top().get(); // OK to warn (\'raw\' declared here)\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 21)\n" + 
			"	raw.add(\"arg\"); // OK to warn (\'raw\' declared here)\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 22)\n" + 
			"	List<String> unchecked= new Top().get();\n" + 
			"	                        ^^^^^^^^^^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 24)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: Unchecked cast from List to List<String>\n" + 
			"----------\n" + 
			"8. WARNING in X.java (at line 24)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from List to List<String>\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (make sure there is no NPE when receiver is null)
public void test322817k() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.Arrays;\n" +
					"import java.util.Set;\n" +
					"import java.util.HashSet;\n" +
					"public class X {\n" +
					"    public void foo(String[] elements) {\n" +
					"	     Set set= new HashSet(Arrays.asList(elements));\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	Set set= new HashSet(Arrays.asList(elements));\n" + 
			"	^^^\n" + 
			"Set is a raw type. References to generic type Set<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 6)\n" + 
			"	Set set= new HashSet(Arrays.asList(elements));\n" + 
			"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The constructor HashSet(Collection) belongs to the raw type HashSet. References to generic type HashSet<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 6)\n" + 
			"	Set set= new HashSet(Arrays.asList(elements));\n" + 
			"	             ^^^^^^^\n" + 
			"HashSet is a raw type. References to generic type HashSet<E> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622 (private access - different packages)
public void test334622a() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"    private Object foo;\n" +
					"}\n",
					"q/Y.java",
					"package q;\n" +
					"import p.X;\n" +
					"public class Y {\n" +
					"    public <T extends X> void test(T t) {\n" +
					"        System.out.println(t.foo);\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 3)\n" + 
			"	private Object foo;\n" + 
			"	               ^^^\n" + 
			"The value of the field X.foo is not used\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in q\\Y.java (at line 5)\n" + 
			"	System.out.println(t.foo);\n" + 
			"	                     ^^^\n" + 
			"The field X.foo is not visible\n" + 
			"----------\n" + 
			"2. ERROR in q\\Y.java (at line 7)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622 (private access - same package)
public void test334622b() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"    private Object foo;\n" +
					"}\n",
					"p/Y.java",
					"package p;\n" +
					"public class Y {\n" +
					"    public <T extends X> void test(T t) {\n" +
					"        System.out.println(t.foo);\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 3)\n" + 
			"	private Object foo;\n" + 
			"	               ^^^\n" + 
			"The value of the field X.foo is not used\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in p\\Y.java (at line 4)\n" + 
			"	System.out.println(t.foo);\n" + 
			"	                     ^^^\n" + 
			"The field X.foo is not visible\n" + 
			"----------\n" + 
			"2. ERROR in p\\Y.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622 (member of type variable shouldn't contain private members of class constituting intersection type)
public void test334622c() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"    private Object foo;\n" +
					"    public <T extends X> void test(T t) {\n" +
					"        System.out.println(t.foo);\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n"
			},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" : 
				
			// 1.7+ output.	
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	private Object foo;\n" + 
			"	               ^^^\n" + 
			"The value of the field X.foo is not used\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	System.out.println(t.foo);\n" + 
			"	                     ^^^\n" + 
			"The field X.foo is not visible\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622 (member of type variable shouldn't contain private members of class constituting intersection type)
public void test334622d() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"    private Object foo() { return null; }\n" +
					"    public <T extends X> void test(T t) {\n" +
					"        t.foo();\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n"
			},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" : 
				
			// 1.7+ output.	
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	t.foo();\n" + 
			"	  ^^^\n" + 
			"The method foo() from the type X is not visible\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335751 ([1.7][compiler] Cycle inheritance in type arguments is not detected)
public void test335751() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X<A extends B, B extends A> {}\n"
			},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X<A extends B, B extends A> {}\n" + 
			"	               ^\n" + 
			"Illegal forward reference to type parameter B\n" + 
			"----------\n" : 

			// 1.7+ output.	
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X<A extends B, B extends A> {}\n" + 
			"	                                      ^\n" + 
			"Cycle detected: a cycle exists in the type hierarchy between B and A\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=334121 ([1.7][compiler] Stackoverflow error if compiled in 1.7 compliance mode)
public void test334121() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X<A extends A> {}\n"
			},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X<A extends A> {}\n" + 
			"	               ^\n" + 
			"Illegal forward reference to type parameter A\n" + 
			"----------\n" : 

			// 1.7+ output.	
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X<A extends A> {}\n" + 
			"	                         ^\n" + 
			"Cycle detected: the type A cannot extend/implement itself or one of its own member types\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337751 
public void test337751() {
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"Project.java",
			"import java.util.Map;\n" +
			"public class Project {\n" +
			"    public Map getOptions(boolean b) {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions14,
		null);
	
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"import java.util.Map;\n" +
			"public class Y {\n" +
			"    void foo(Project project) {\n" +
			"        Map<String, String> options=\n" +
			"                        project != null ? project.getOptions(true) : null;\n" +
			"        options = project.getOptions(true);\n" +
			"        options = project == null ? null : project.getOptions(true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in Y.java (at line 5)\n" + 
		"	project != null ? project.getOptions(true) : null;\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The expression of type Map needs unchecked conversion to conform to Map<String,String>\n" + 
		"----------\n" + 
		"2. WARNING in Y.java (at line 6)\n" + 
		"	options = project.getOptions(true);\n" + 
		"	          ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The expression of type Map needs unchecked conversion to conform to Map<String,String>\n" + 
		"----------\n" + 
		"3. WARNING in Y.java (at line 7)\n" + 
		"	options = project == null ? null : project.getOptions(true);\n" + 
		"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The expression of type Map needs unchecked conversion to conform to Map<String,String>\n" + 
		"----------\n",
		null,
		false,
		compilerOptions15,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=337751 
public void test337751a() {
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"Project.java",
			"import java.util.Map;\n" +
			"public class Project {\n" +
			"    public Map getOptions(boolean b) {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions14,
		null);
	
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"import java.util.Map;\n" +
			"public class Y {\n" +
			"    void foo(Project project) {\n" +
			"        Map<String, String> options=\n" +
			"                        project != null ? project.getOptions(true) : null;\n" +
			"        options = project.getOptions(true);\n" +
			"        options = project == null ? null : project.getOptions(true);\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		false,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337962 
public void test337962() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"class Super {\n" +
			"    protected List fList;\n" +
			"}\n" +
			"public class X extends Super {\n" +
			"    protected List fSubList; // raw type warning (good)\n" +
			"    {\n" +
			"        fSubList = new ArrayList();\n " +
			"        fList.add(null); // type safety warning (TODO: bad, should be hidden)\n" +
			"        super.fList.add(null); // type safety warning (TODO: bad, should be hidden)\n" +
			"        fSubList.add(null); // type safety warning (good, should not be hidden)\n" +
			"    }\n" +
			"    void foo(String s) {\n" +
			"        fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" +
			"        super.fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" +
			"        fSubList.add(s); // type safety warning (good, should not be hidden)\n" +
			"    }\n" +
			"    X(String s) {\n" +
			"        fSubList = new ArrayList();\n " +
			"        fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" +
			"        super.fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" +
			"        fSubList.add(s); // type safety warning (good, should not be hidden)\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	protected List fList;\n" + 
		"	          ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 7)\n" + 
		"	protected List fSubList; // raw type warning (good)\n" + 
		"	          ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 9)\n" + 
		"	fSubList = new ArrayList();\n" + 
		"	               ^^^^^^^^^\n" + 
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 10)\n" + 
		"	fList.add(null); // type safety warning (TODO: bad, should be hidden)\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 11)\n" + 
		"	super.fList.add(null); // type safety warning (TODO: bad, should be hidden)\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 12)\n" + 
		"	fSubList.add(null); // type safety warning (good, should not be hidden)\n" + 
		"	^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"7. WARNING in X.java (at line 15)\n" + 
		"	fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" + 
		"	^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"8. WARNING in X.java (at line 16)\n" + 
		"	super.fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" + 
		"	^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"9. WARNING in X.java (at line 17)\n" + 
		"	fSubList.add(s); // type safety warning (good, should not be hidden)\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"10. WARNING in X.java (at line 20)\n" + 
		"	fSubList = new ArrayList();\n" + 
		"	               ^^^^^^^^^\n" + 
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" + 
		"----------\n" + 
		"11. WARNING in X.java (at line 21)\n" + 
		"	fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" + 
		"	^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"12. WARNING in X.java (at line 22)\n" + 
		"	super.fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" + 
		"	^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"13. WARNING in X.java (at line 23)\n" + 
		"	fSubList.add(s); // type safety warning (good, should not be hidden)\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n",
		null,
		false,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337962 
public void test337962b() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"class Super {\n" +
			"    protected List fList;\n" +
			"}\n" +
			"public class X extends Super {\n" +
			"    protected List fSubList; // raw type warning (good)\n" +
			"    {\n" +
			"        fSubList = new ArrayList();\n " +
			"        fList.add(null); // type safety warning (TODO: bad, should be hidden)\n" +
			"        super.fList.add(null); // type safety warning (TODO: bad, should be hidden)\n" +
			"        fSubList.add(null); // type safety warning (good, should not be hidden)\n" +
			"    }\n" +
			"    void foo(String s) {\n" +
			"        fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" +
			"        super.fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" +
			"        fSubList.add(s); // type safety warning (good, should not be hidden)\n" +
			"    }\n" +
			"    X(String s) {\n" +
			"        fSubList = new ArrayList();\n " +
			"        fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" +
			"        super.fList.add(s); // type safety warning (TODO: bad, should be hidden)\n" +
			"        fSubList.add(s); // type safety warning (good, should not be hidden)\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	protected List fList;\n" + 
		"	          ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 7)\n" + 
		"	protected List fSubList; // raw type warning (good)\n" + 
		"	          ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 9)\n" + 
		"	fSubList = new ArrayList();\n" + 
		"	               ^^^^^^^^^\n" + 
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 12)\n" + 
		"	fSubList.add(null); // type safety warning (good, should not be hidden)\n" + 
		"	^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 17)\n" + 
		"	fSubList.add(s); // type safety warning (good, should not be hidden)\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 20)\n" + 
		"	fSubList = new ArrayList();\n" + 
		"	               ^^^^^^^^^\n" + 
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" + 
		"----------\n" + 
		"7. WARNING in X.java (at line 23)\n" + 
		"	fSubList.add(s); // type safety warning (good, should not be hidden)\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
		"----------\n",
		null,
		false,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338011 
public void test338011() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"public class X extends A {\n" +
			"    public X(Map m) { // should warn about raw type m\n" +
			"        super(m);\n" +
			"        m.put(\"one\", 1); // warns about raw method invocation (good)\n" +
			"    }\n" +
			"    public X(Map<String, Integer> m, boolean b) {\n" +
			"        super(m); // shows that parametrizing the parameter type is no problem \n" +
			"        new A(m);\n" +
			"        m.put(\"one\", 1);\n" +
			"    }\n" +
			"}\n" +
			"class A {\n" +
			"    public A (Map m) {\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	public X(Map m) { // should warn about raw type m\n" + 
		"	         ^^^\n" + 
		"Map is a raw type. References to generic type Map<K,V> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 5)\n" + 
		"	m.put(\"one\", 1); // warns about raw method invocation (good)\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method put(Object, Object) belongs to the raw type Map. References to generic type Map<K,V> should be parameterized\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 14)\n" + 
		"	public A (Map m) {\n" + 
		"	          ^^^\n" + 
		"Map is a raw type. References to generic type Map<K,V> should be parameterized\n" + 
		"----------\n",
		null,
		false,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338011 
public void test338011b() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"public class X extends A {\n" +
			"    public X(Map m) { // should warn about raw type m\n" +
			"        super(m);\n" +
			"        m.put(\"one\", 1); // warns about raw method invocation (good)\n" +
			"    }\n" +
			"    public X(Map<String, Integer> m, boolean b) {\n" +
			"        super(m); // shows that parametrizing the parameter type is no problem \n" +
			"        new A(m);\n" +
			"        m.put(\"one\", 1);\n" +
			"    }\n" +
			"}\n" +
			"class A {\n" +
			"    public A (Map m) {\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	public X(Map m) { // should warn about raw type m\n" + 
		"	         ^^^\n" + 
		"Map is a raw type. References to generic type Map<K,V> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 5)\n" + 
		"	m.put(\"one\", 1); // warns about raw method invocation (good)\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method put(Object, Object) belongs to the raw type Map. References to generic type Map<K,V> should be parameterized\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 14)\n" + 
		"	public A (Map m) {\n" + 
		"	          ^^^\n" + 
		"Map is a raw type. References to generic type Map<K,V> should be parameterized\n" + 
		"----------\n",
		null,
		false,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339478
// To verify that diamond construct is not allowed in source level 1.6 or below
public void test339478a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		X<String> x = new X<>();\n" + 
			"		x.testFunction(\"SUCCESS\");\n" + 
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	X<String> x = new X<>();\n" + 
		"	                  ^\n" + 
		"\'<>\' operator is not allowed for source level below 1.7\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339478
// To verify that diamond construct is not allowed in source level 1.6 or below
public void test339478b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" + 
			"		X<> x1 = null;\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	X<> x1 = null;\n" + 
		"	^\n" + 
		"Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Map;\n" +
			"public class X implements Map<> {\n" +
			"    static Map<> foo (Map<> x) { \n" +
			"        return null;\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public class X implements Map<> {\n" + 
		"	                          ^^^\n" + 
		"Incorrect number of arguments for type Map<K,V>; it cannot be parameterized with arguments <>\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	static Map<> foo (Map<> x) { \n" + 
		"	       ^^^\n" + 
		"Incorrect number of arguments for type Map<K,V>; it cannot be parameterized with arguments <>\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	static Map<> foo (Map<> x) { \n" + 
		"	                  ^^^\n" + 
		"Incorrect number of arguments for type Map<K,V>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478d() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Map;\n" +
			"public class X  {\n" +
			"    static Map<> foo () { \n" +
			"        return null;\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	static Map<> foo () { \n" + 
		"	       ^^^\n" + 
		"Incorrect number of arguments for type Map<K,V>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478e() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    class Y<K> {\n" +
			"    }\n" +
			"    public static void main(String [] args) {\n" +
			"        X<String>.Y<> [] y = null; \n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	X<String>.Y<> [] y = null; \n" + 
		"	^^^^^^^^^^^\n" + 
		"Incorrect number of arguments for type X<String>.Y; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478f() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    class Y<K> {\n" +
			"    }\n" +
			"    public static void main(String [] args) {\n" +
			"        X<String>.Y<>  y = null; \n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	X<String>.Y<>  y = null; \n" + 
		"	^^^^^^^^^^^\n" + 
		"Incorrect number of arguments for type X<String>.Y; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478g() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    public void foo(Object x) {\n" +
			"        if (x instanceof X<>) {    \n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	if (x instanceof X<>) {    \n" + 
		"	                 ^\n" + 
		"Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478h() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    public void foo(Object x) throws X.Y<>.LException {\n" +
			"    }\n" +
			"    static class Y<T> {\n" +
			"    static class LException extends Throwable {}\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public void foo(Object x) throws X.Y<>.LException {\n" + 
		"	                                 ^^^\n" + 
		"Incorrect number of arguments for type X.Y; it cannot be parameterized with arguments <>\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 5)\n" + 
		"	static class LException extends Throwable {}\n" + 
		"	             ^^^^^^^^^^\n" + 
		"The serializable class LException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n");
}
public void test339478i() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    public void foo () {\n" +
			"        Object o = new X<> [10];\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	Object o = new X<> [10];\n" + 
		"	               ^\n" + 
		"Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478j() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" + 
			"		X<>[] x1 = null;\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	X<>[] x1 = null;\n" + 
		"	^\n" + 
		"Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478k() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	X<>[] x1 = null;\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	X<>[] x1 = null;\n" + 
		"	^\n" + 
		"Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478l() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" + 
			"		X<> x1 = null;\n" + 
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	X<> x1 = null;\n" + 
		"	^\n" + 
		"Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478m() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	X<> f1 = null;\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	X<> f1 = null;\n" + 
		"	^\n" + 
		"Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478n() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public void foo(X<> args) {\n" + 
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public void foo(X<> args) {\n" + 
		"	                ^\n" + 
		"Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
public void test339478o() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X<>(){\n" +
			"			void newMethod(){\n" +
			"			}\n" +
			"		}.testFunction(\"SUCCESS\");\n" + 
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" + 
			"}",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	new X<>(){\n" + 
		"	    ^\n" + 
		"\'<>\' operator is not allowed for source level below 1.7\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	new X<>(){\n" + 
		"	    ^\n" + 
		"\'<>\' cannot be used with anonymous classes\n" + 
		"----------\n":
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	new X<>(){\n" + 
			"	    ^\n" + 
			"\'<>\' cannot be used with anonymous classes\n" + 
			"----------\n");
}
public void test339478p() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		X Test = new X<>(){\n" +
			"			void newMethod(){\n" +
			"			}\n" +
			"		}.testFunction(\"SUCCESS\");\n" + 
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" + 
			"}",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	X Test = new X<>(){\n" + 
		"	^\n" + 
		"X is a raw type. References to generic type X<T> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	X Test = new X<>(){\n" + 
		"	             ^\n" + 
		"\'<>\' operator is not allowed for source level below 1.7\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	X Test = new X<>(){\n" + 
		"	             ^\n" + 
		"\'<>\' cannot be used with anonymous classes\n" + 
		"----------\n" : 
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	X Test = new X<>(){\n" + 
			"	^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	X Test = new X<>(){\n" + 
			"	             ^\n" + 
			"\'<>\' cannot be used with anonymous classes\n" + 
			"----------\n");
}
public void test339478q() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		X Test = new X<>();\n" +
			"	}\n" +
			"}",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	X Test = new X<>();\n" + 
		"	             ^\n" + 
		"\'<>\' operator is not allowed for source level below 1.7\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	X Test = new X<>();\n" + 
		"	             ^\n" + 
		"The type X is not generic; it cannot be parameterized with arguments <>\n" + 
		"----------\n":
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	X Test = new X<>();\n" + 
		"	             ^\n" + 
		"The type X is not generic; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334493 
public void test334493() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface Super<P> {}\n" +
			"class Y<C> implements Super<Integer>{}\n" +
			"interface II extends Super<Double>{}\n" +
			"class S<A> extends Y<Byte> {}\n" +
			"interface T<B> extends II{}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"        S<Integer> s = null;\n" +
			"        T<Integer> t = null;\n" +
			"        t = (T) s;          //casting to raw type, no error\n" +
			"        System.out.println(t);\n" +
			"    }\n" +
			"}\n"
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	t = (T) s;          //casting to raw type, no error\n" + 
		"	    ^^^^^\n" + 
		"Cannot cast from S<Integer> to T\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 10)\n" + 
		"	t = (T) s;          //casting to raw type, no error\n" + 
		"	    ^^^^^\n" + 
		"Type safety: The expression of type T needs unchecked conversion to conform to T<Integer>\n" + 
		"----------\n" : 
			"----------\n" + 
				"1. WARNING in X.java (at line 10)\n" + 
				"	t = (T) s;          //casting to raw type, no error\n" + 
				"	    ^^^^^\n" + 
				"Type safety: The expression of type T needs unchecked conversion to conform to T<Integer>\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 10)\n" + 
				"	t = (T) s;          //casting to raw type, no error\n" + 
				"	     ^\n" + 
				"T is a raw type. References to generic type T<B> should be parameterized\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class C<T>  {\n" +
				"	public abstract Object foo(T x);\n" +
				"   public Integer foo(String x){ return 1; }\n" +
				"}\n" +
				"public class X extends C<String> {\n" +
				"    zork z;\n" +
				"}\n"			
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	zork z;\n" + 
				"	^^^^\n" + 
				"zork cannot be resolved to a type\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class C<T>  {\n" +
				"	public abstract Integer foo(T x);\n" +
				"   public Object foo(String x){ return 1; }\n" +
				"}\n" +
				"public class X extends C<String> {\n" +
				"}\n"			
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	public class X extends C<String> {\n" + 
				"	             ^\n" + 
				"The type X must implement the inherited abstract method C<String>.foo(String) to override C<String>.foo(String)\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class B<T> {\n" +
				"	public abstract Object foo(T x);\n" +
				"}\n" +
				"abstract class C<T> extends B<T> {\n" +
				"    public Integer foo(String x){ return 1; }\n" +
				"}\n" +
				"public class X extends C<String> {\n" +
				"    zork z;\n" +
				"}\n"			
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	zork z;\n" + 
				"	^^^^\n" + 
				"zork cannot be resolved to a type\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class B<T> {\n" +
				"	public abstract Integer foo(T x);\n" +
				"}\n" +
				"abstract class C<T> extends B<T> {\n" +
				"    public Object foo(String x){ return 1; }\n" +
				"}\n" +
				"public class X extends C<String> {\n" +
				"}\n"			
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	public class X extends C<String> {\n" + 
				"	             ^\n" + 
				"The type X must implement the inherited abstract method B<String>.foo(String) to override C<String>.foo(String)\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313e() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class C<T>  {\n" +
				"	public abstract Object foo(T x);\n" +
				"   public static Integer foo(String x){ return 1; }\n" +
				"}\n" +
				"public class X extends C<String> {\n" +
				"}\n"			
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	public class X extends C<String> {\n" + 
				"	             ^\n" + 
				"The static method foo(String) conflicts with the abstract method in C<String>\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347145
public void test347145() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class A {}\n" +
				"class B<V> extends A {} \n" +
				"class F<T extends A, Y extends B<T>> {\n" +
				"	static <U extends A , V extends B<U>> F<U,V> g() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X  {\n" +
				"    F<? extends B, ? extends B<? extends B>> f011 = F.g();\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 9)\n" + 
			"	F<? extends B, ? extends B<? extends B>> f011 = F.g();\n" + 
			"	            ^\n" + 
			"B is a raw type. References to generic type B<V> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	F<? extends B, ? extends B<? extends B>> f011 = F.g();\n" + 
			"	                                     ^\n" + 
			"B is a raw type. References to generic type B<V> should be parameterized\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347426
public void test347426() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    class A<T extends B<?>> {  }\n" +
				"    class B<T extends A<?>> {\n" +
				"        D<? extends B<T>> x;\n" +
				"    }\n" +
				"    class D<T extends B<?>> {}\n" +
				"    <E extends B<?>> X(E x, D<B<A<?>>> d) {\n" +
				"        if (x.x == d) {\n" +
				"            return;\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347426
public void test347426b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"    class A<T extends X<?>> {\n" +
				"        B<? extends A<T>> x;\n" +
				"    }\n" +
				"    class B<T extends A<?>> {}\n" +
				"    boolean b = ((A<?>)null).x == ((B<A<X<?>>>)null);   \n" +
				"}\n"
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347426
public void test347426c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"    class A<T extends X<? extends String>> {\n" +
				"        B<? extends A<T>> x;\n" +
				"    }\n" +
				"    class B<T extends A<?>> {}\n" +
				"    boolean b = ((A<? extends X<?>>)null).x == ((B<A<X<? extends String>>>)null);       \n" +
				"}\n"
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=283353
public void test283353() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    EntityKey entityKey = null;\n" +
				"    new EntityCondenser().condense(entityKey);  \n" +
				"  }\n" +
				"  public static class EntityCondenser {\n" +
				"    <I, E extends EntityType<I, E, K>, K extends EntityKey<I>> void condense(K entityKey) {\n" +
				"    }\n" +
				"  }\n" +
				"  public class EntityKey<I> {}\n" +
				"  public interface EntityType<\n" +
				"    I,\n" +
				"    E extends EntityType<I, E, K>,\n" +
				"    K extends EntityKey<I>> {\n" +
				"  }\n" +
				"}\n"
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347600
public void test347600() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class A {}\n" +
				"class B<V> extends A {} \n" +
				"class D extends B<E> {}\n" +
				"class E extends B<D> {}\n" +
				"public class X<T, Y extends B<U>, U extends B<Y>> {    \n" +
				"    public static <T1, Y1 extends B<U1>, U1 extends B<Y1>> X<T1, Y1, U1> getX() {\n" +
				"        return null;\n" +
				"    }\n" +
				"    X<B, ? extends D, ? extends E> f = getX();   \n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 9)\n" + 
			"	X<B, ? extends D, ? extends E> f = getX();   \n" + 
			"	  ^\n" + 
			"B is a raw type. References to generic type B<V> should be parameterized\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347746
public void test347746() {
	 this.runNegativeTest(
	     new String[] {
	         "X.java",
	         "public class X {\n" +
	   		 "    class A<T extends B<?>> {}\n" +
	   		 "    class B<T extends A<?>> extends D {}\n" +
	   		 "    class C<T extends D> {}\n" +
	   		 "    class D {}\n" +
	   		 "    class E<T extends C<? extends B<?>>> {}\n" +
	   		 "    <U extends C<V>, V extends B<W>, W extends A<V>> W foo(E<U> e) {\n" +
	   		 "        return goo(e);\n" +
	   		 "    }\n" +
	   		 "    <P extends C<Q>, Q extends B<R>, R extends A<Q>> R goo(E<P> e) {\n" +
	   		 "        return null;\n" +
	   		 "    }\n" +
	   		 "}\n"
	     },
	     "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348493
// To verify that diamond construct is not allowed in source level 1.6 or below
public void test348493() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<Z> {}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		X<String>.X2<> x = new X<String>().new X2<>();\n" + 
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	X<String>.X2<> x = new X<String>().new X2<>();\n" + 
		"	^^^^^^^^^^^^\n" + 
		"Incorrect number of arguments for type X<String>.X2; it cannot be parameterized with arguments <>\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	X<String>.X2<> x = new X<String>().new X2<>();\n" + 
		"	                                       ^^\n" + 
		"\'<>\' operator is not allowed for source level below 1.7\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348493
// To verify that diamond construct is not allowed in source level 1.6 or below
public void test348493a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" + 
			"		X<> x = new X<>();\n" + 
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	X<> x = new X<>();\n" + 
		"	^\n" + 
		"Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	X<> x = new X<>();\n" + 
		"	            ^\n" + 
		"\'<>\' operator is not allowed for source level below 1.7\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=366131
public void test366131() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n" +
			"class Range<T extends Comparable<? super T>> {\n" +
			"    public boolean containsNC(T value) {\n" +
			"        return false;\n" +
			"    }\n" +
			"}\n" +
			"class NumberRange<T extends Number & Comparable<? super T>> extends Range<T> {\n" +
			"    public boolean contains(Comparable<?> value) {\n" +
			"        return castTo((Class) null).containsNC((Comparable) null);\n" +
			"    }\n" +
			"    public <N extends Number & Comparable<? super N>> NumberRange<N>\n" +
			"castTo(Class<N> type) {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=366131
public void test366131b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"        Zork z;\n" +
			"    }\n" +
			"}\n" +
			"class Range<T extends Comparable<? super T>> {\n" +
			"    public boolean containsNC(T value) {\n" +
			"        return false;\n" +
			"    }\n" +
			"}\n" +
			"class NumberRange<T extends Number & Comparable<? super T>> extends Range<T> {\n" +
			"    public boolean contains(Comparable<?> value) {\n" +
			"        return castTo((Class) null).containsNC((Comparable) null);\n" +
			"    }\n" +
			"    public <N extends Number & Comparable<? super N>> NumberRange<N>\n" +
			"castTo(Class<N> type) {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 13)\n" + 
		"	return castTo((Class) null).containsNC((Comparable) null);\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: Unchecked invocation castTo(Class) of the generic method castTo(Class<N>) of type NumberRange<T>\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 13)\n" + 
		"	return castTo((Class) null).containsNC((Comparable) null);\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method containsNC(Comparable) belongs to the raw type Range. References to generic type Range<T> should be parameterized\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 13)\n" + 
		"	return castTo((Class) null).containsNC((Comparable) null);\n" + 
		"	              ^^^^^^^^^^^^\n" + 
		"Type safety: The expression of type Class needs unchecked conversion to conform to Class<Number&Comparable<? super Number&Comparable<? super N>>>\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 13)\n" + 
		"	return castTo((Class) null).containsNC((Comparable) null);\n" + 
		"	               ^^^^^\n" + 
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 13)\n" + 
		"	return castTo((Class) null).containsNC((Comparable) null);\n" + 
		"	                                        ^^^^^^^^^^\n" + 
		"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" + 
		"----------\n");
}
}