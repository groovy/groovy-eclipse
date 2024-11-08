/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *								bug 282152 - [1.5][compiler] Generics code rejected by Eclipse but accepted by javac
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								bug 401456 - Code compiles from javac/intellij, but fails from eclipse
 *								bug 405706 - Eclipse compiler fails to give compiler error when return type is a inferred generic
 *								Bug 408441 - Type mismatch using Arrays.asList with 3 or more implementations of an interface with the interface type as the last parameter
 *								Bug 413958 - Function override returning inherited Generic Type
 *								Bug 415734 - Eclipse gives compilation error calling method with an inferred generic return type
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 423496 - [1.8] Implement new incorporation rule once it becomes available
 *								Bug 426590 - [1.8][compiler] Compiler error with tenary operator
 *								Bug 427216 - [Java8] array to varargs regression
 *								Bug 425031 - [1.8] nondeterministic inference for GenericsRegressionTest.test283353
 *								Bug 430686 - [1.8][compiler] Generics: erroneously reports 'method not applicable for the arguments'
 *								Bug 430759 - [1.8][compiler] SourceTypeBinding cannot be cast to ParameterizedTypeBinding
 *								Bug 431408 - Java 8 (1.8) generics bug
 *								Bug 432603 - [compile][1.7] ecj reports an Error while javac doesn't
 *								Bug 399527 - Type inference problem
 *								Bug 434570 - Generic type mismatch for parametrized class annotation attribute with inner class
 *								Bug 434044 - Java 8 generics thinks single method is ambiguous
 *								Bug 434793 - [1.8][null][compiler] AIOOBE in ParameterizedGenericMethodBinding.substitute when inlining a method
 *								Bug 438337 - StackOverflow after update from Kepler to Luna
 *								Bug 452194 - Code no longer compiles in 4.4.1, but with confusing error
 *								Bug 456459 - Discrepancy between Eclipse compiler and javac - Enums, interfaces, and generics
 *								Bug 456924 - StackOverflowError during compilation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericsRegressionTest extends AbstractComparableTest {

	public GenericsRegressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testBug456459" };
//		TESTS_NUMBERS = new int[] { 1465 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return GenericsRegressionTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map compilerOptions = super.getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		compilerOptions.put(CompilerOptions.OPTION_ReportUnusedTypeParameter,CompilerOptions.IGNORE);
		return compilerOptions;
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=338350 (unchecked cast - only unavoidable on raw expression)
public void test338350() {
	String[] testFiles = new String[] {
			"Try.java",
			"import java.lang.reflect.Array;\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"public class Try<E> {\n" +
			"	void fooObj() {\n" +
			"		takeObj((E) Bar.getObject());\n" +
			"		takeObj((E) Bar.getArray());\n" +
			"		takeObj((E) Array.newInstance(Integer.class, 2));\n" +
			"	}\n" +
			"	void takeObj(E obj) { }\n" +
			"	void fooArray() {\n" +
			"		takeArray((E[]) Bar.getArray());\n" +
			"		takeArray((E[]) Array.newInstance(Integer.class, 2));\n" +
			"	}\n" +
			"	void takeArray(E[] array) { }\n" +
			"	<L> void foo(List<L> list) {\n" +
			"		list.toArray((L[]) Bar.getArray());\n" +
			"		list.toArray((L[]) Array.newInstance(Integer.class, 2));\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		List<String> l = (List<String>) Bar.getObject();\n" +
			"		List<String> l2 = Bar.getRawList();\n" +
			"		ArrayList<String> l3 = (ArrayList<String>) Bar.getRawList();\n" +
			"	}\n" +
			"}\n",
			"Bar.java",
			"import java.lang.reflect.Array;\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"public class Bar {\n" +
			"	public static Object getObject() {\n" +
			"		return new Object();\n" +
			"	}\n" +
			"	public static Object[] getArray() {\n" +
			"		return (Object[]) Array.newInstance(Integer.class, 2);\n" +
			"	}\n" +
			"	public static List getRawList() {\n" +
			"		return new ArrayList();\n" +
			"	}\n" +
			"}\n"
	};
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
			testFiles,
			"----------\n" +
			"1. WARNING in Try.java (at line 6)\n" +
			"	takeObj((E) Bar.getObject());\n" +
			"	        ^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to E\n" +
			"----------\n" +
			"2. WARNING in Try.java (at line 7)\n" +
			"	takeObj((E) Bar.getArray());\n" +
			"	        ^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object[] to E\n" +
			"----------\n" +
			"3. WARNING in Try.java (at line 8)\n" +
			"	takeObj((E) Array.newInstance(Integer.class, 2));\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to E\n" +
			"----------\n" +
			"4. WARNING in Try.java (at line 12)\n" +
			"	takeArray((E[]) Bar.getArray());\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object[] to E[]\n" +
			"----------\n" +
			"5. WARNING in Try.java (at line 13)\n" +
			"	takeArray((E[]) Array.newInstance(Integer.class, 2));\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to E[]\n" +
			"----------\n" +
			"6. WARNING in Try.java (at line 17)\n" +
			"	list.toArray((L[]) Bar.getArray());\n" +
			"	             ^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object[] to L[]\n" +
			"----------\n" +
			"7. WARNING in Try.java (at line 18)\n" +
			"	list.toArray((L[]) Array.newInstance(Integer.class, 2));\n" +
			"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to L[]\n" +
			"----------\n" +
			"8. WARNING in Try.java (at line 21)\n" +
			"	List<String> l = (List<String>) Bar.getObject();\n" +
			"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to List<String>\n" +
			"----------\n" +
			"9. WARNING in Try.java (at line 22)\n" +
			"	List<String> l2 = Bar.getRawList();\n" +
			"	                  ^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" +
			"----------\n" +
			"10. WARNING in Try.java (at line 23)\n" +
			"	ArrayList<String> l3 = (ArrayList<String>) Bar.getRawList();\n" +
			"	                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from List to ArrayList<String>\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in Bar.java (at line 11)\n" +
			"	public static List getRawList() {\n" +
			"	              ^^^^\n" +
			"List is a raw type. References to generic type List<E> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in Bar.java (at line 12)\n" +
			"	return new ArrayList();\n" +
			"	           ^^^^^^^^^\n" +
			"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
			"----------\n",
			null,
			true,
			customOptions);
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			testFiles,
			"----------\n" +
			"1. WARNING in Try.java (at line 6)\n" +
			"	takeObj((E) Bar.getObject());\n" +
			"	        ^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to E\n" +
			"----------\n" +
			"2. WARNING in Try.java (at line 7)\n" +
			"	takeObj((E) Bar.getArray());\n" +
			"	        ^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object[] to E\n" +
			"----------\n" +
			"3. WARNING in Try.java (at line 8)\n" +
			"	takeObj((E) Array.newInstance(Integer.class, 2));\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to E\n" +
			"----------\n" +
			"4. WARNING in Try.java (at line 12)\n" +
			"	takeArray((E[]) Bar.getArray());\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object[] to E[]\n" +
			"----------\n" +
			"5. WARNING in Try.java (at line 13)\n" +
			"	takeArray((E[]) Array.newInstance(Integer.class, 2));\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to E[]\n" +
			"----------\n" +
			"6. WARNING in Try.java (at line 17)\n" +
			"	list.toArray((L[]) Bar.getArray());\n" +
			"	             ^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object[] to L[]\n" +
			"----------\n" +
			"7. WARNING in Try.java (at line 18)\n" +
			"	list.toArray((L[]) Array.newInstance(Integer.class, 2));\n" +
			"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to L[]\n" +
			"----------\n" +
			"8. WARNING in Try.java (at line 21)\n" +
			"	List<String> l = (List<String>) Bar.getObject();\n" +
			"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from Object to List<String>\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in Bar.java (at line 11)\n" +
			"	public static List getRawList() {\n" +
			"	              ^^^^\n" +
			"List is a raw type. References to generic type List<E> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in Bar.java (at line 12)\n" +
			"	return new ArrayList();\n" +
			"	           ^^^^^^^^^\n" +
			"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
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
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
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
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
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
		"	                  ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The expression of type Map needs unchecked conversion to conform to Map<String,String>\n" +
		"----------\n" +
		"2. WARNING in Y.java (at line 6)\n" +
		"	options = project.getOptions(true);\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The expression of type Map needs unchecked conversion to conform to Map<String,String>\n" +
		"----------\n" +
		"3. WARNING in Y.java (at line 7)\n" +
		"	options = project == null ? null : project.getOptions(true);\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
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
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
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
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
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
		null,
		JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337962
public void test337962() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
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
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
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
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
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
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
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
		"Incorrect number of arguments for type X.Y<T>; it cannot be parameterized with arguments <>\n" +
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
	String log_18 =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new X<>(){\n" +
			"	    ^\n" +
			"\'<>\' cannot be used with anonymous classes\n" +
			"----------\n";
	String log_9 =
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	void newMethod(){\n" +
			"	     ^^^^^^^^^^^\n" +
			"The method newMethod() of type new X<Object>(){} must override or implement a supertype method\n" +
			"----------\n";
	String errorMsg = this.complianceLevel < ClassFileConstants.JDK9 ? log_18 : log_9;
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
			errorMsg);
}
public void test339478p() {
	String log_18 =
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
			"----------\n";
	String log_9 =
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	X Test = new X<>(){\n" +
			"	^\n" +
			"X is a raw type. References to generic type X<T> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	X Test = new X<>(){\n" +
			"			void newMethod(){\n" +
			"			}\n" +
			"		}.testFunction(\"SUCCESS\");\n" +
			"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from void to X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	void newMethod(){\n" +
			"	     ^^^^^^^^^^^\n" +
			"The method newMethod() of type new X<Object>(){} must override or implement a supertype method\n" +
			"----------\n";
	String errorMsg = this.complianceLevel < ClassFileConstants.JDK9 ? log_18 : log_9;
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
			errorMsg);
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
	String source =
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
			"}\n";

	runNegativeTest(
			new String[] { "X.java", source },
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	EntityKey entityKey = null;\n" +
			"	^^^^^^^^^\n" +
			"X.EntityKey is a raw type. References to generic type X.EntityKey<I> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	new EntityCondenser().condense(entityKey);  \n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked invocation condense(X.EntityKey) of the generic method condense(K) of type X.EntityCondenser\n" +
			"----------\n");
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
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type safety: The expression of type Class needs unchecked conversion to conform to Class<Number&Comparable<? super Number&Comparable<? super N>>>\n"
		:
		"Type safety: The expression of type Class needs unchecked conversion to conform to Class<Comparable<? super Comparable<? super N>&Number>&Number>\n"
		) +
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375394
public void test375394() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Collection;\n" +
			"public class X {\n" +
			"    static <C1,C2 extends Collection<Object>> boolean foo(C1 c, C2 c2) {\n" +
			"        return foo(c2,c); \n" +
			"    }\n" +
			"}\n",
		},
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return foo(c2,c); \n" +
		"	       ^^^\n" +
		"Bound mismatch: The generic method foo(C1, C2) of type X is not applicable for the arguments (C2, C1). The inferred type C1 is not a valid substitute for the bounded parameter <C2 extends Collection<Object>>\n" +
		"----------\n" :
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	return foo(c2,c); \n" +
			"	       ^^^\n" +
			"The method foo(C1, C2) in the type X is not applicable for the arguments (C2, C1)\n" +
			"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385780
public void test385780() {
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportUnusedTypeParameter,
			CompilerOptions.ERROR);
	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		new String[] {
			"X.java",
			"public class X<T> {\n"+
			"public <S> X() {\n"+
			"}\n"+
			"public void ph(int t) {\n"+
	        "}\n"+
			"}\n"+
			"interface doNothingInterface<T> {\n"+
			"}\n"+
			"class doNothing {\n"+
			"public <T> void doNothingMethod() {"+
			"}\n"+
			"}\n"+
			"class noerror {\n"+
			"public <T> void doNothing(T t) {"+
			"}"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X<T> {\n" +
		"	               ^\n" +
		"Unused type parameter T\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	public <S> X() {\n" +
		"	        ^\n" +
		"Unused type parameter S\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	interface doNothingInterface<T> {\n" +
		"	                             ^\n" +
		"Unused type parameter T\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 10)\n" +
		"	public <T> void doNothingMethod() {}\n" +
		"	        ^\n" +
		"Unused type parameter T\n" +
		"----------\n",
		null, true, customOptions);
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// version with intermediate assignment, always worked
public void testBug395002_1() {
	runConformTest(new String[] {
		"Client.java",
		"interface SelfBound<S extends SelfBound<S, T>, T> {\n" +
		"}\n" +
		"public class Client {\n" +
		"	<A extends SelfBound<?,A>> void foo3(A arg3) {\n" +
		"		SelfBound<?, A> var3 = arg3;\n" +
		"		SelfBound<? extends SelfBound<?, A>, ?> var4 = var3;\n" +
		"	}\n" +
		"}\n"
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// version with direct assignment to local
public void testBug395002_2() {
	runConformTest(new String[] {
		"Client.java",
		"interface SelfBound<S extends SelfBound<S, T>, T> {\n" +
		"}\n" +
		"public class Client {\n" +
		"	<A extends SelfBound<?,A>> void foo2(A arg2) {\n" +
		"		SelfBound<? extends SelfBound<?, A>, ?> var2 = arg2;\n" +
		"	}\n" +
		"}\n"
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// version with direct assignment to field
public void testBug395002_3() {
	runConformTest(new String[] {
		"Client.java",
		"interface SelfBound<S extends SelfBound<S, T>, T> {\n" +
		"}\n" +
		"public class Client<A extends SelfBound<?,A>>  {\n" +
		"	SelfBound<? extends SelfBound<?, A>, ?> field2;\n" +
		"	void foo2(A arg2) {\n" +
		"		field2 = arg2;\n" +
		"	}\n" +
		"}\n"
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// version with argument passing
public void testBug395002_4() {
	runConformTest(new String[] {
		"Client.java",
		"interface SelfBound<S extends SelfBound<S, T>, T> {\n" +
		"}\n" +
		"public class Client<A extends SelfBound<?,A>>  {\n" +
		"	void bar(SelfBound<? extends SelfBound<?, A>, ?> argBar) {};\n" +
		"	void foo2(A arg2) {\n" +
		"		bar(arg2);\n" +
		"	}\n" +
		"}\n"
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// original problem with invocation of generic type
public void testBug395002_full() {
	runConformTest(new String[] {
		"Bug.java",
		"interface SelfBound<S extends SelfBound<S, T>, T> {\n" +
		"}\n" +
		"class Test<X extends SelfBound<? extends Y, ?>, Y> {\n" +
		"}\n" +
		"public class Bug<A extends SelfBound<?, A>> {\n" +
		"	public Bug() {\n" +
		"		new Test<A, SelfBound<?, A>>();\n" +
		"	}\n" +
		"}\n"
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// combined version with direct assignment to local + original problem w/ invocation of generic type
public void testBug395002_combined() {
	runConformTest(new String[] {
		"Client.java",
		"interface SelfBound<S extends SelfBound<S, T>, T> {\n" +
		"}\n" +
		"class Test<X extends SelfBound<? extends Y, ?>, Y> {\n" +
		"}\n" +
		"public class Client {\n" +
		"	<A extends SelfBound<?,A>> void foo2(A arg2) {\n" +
		"		Object o = new Test<A, SelfBound<?, A>>();\n" +
		"		SelfBound<? extends SelfBound<?, A>, ?> var2 = arg2;\n" +
		"	}\n" +
		"}\n"
		});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=397888
public void test397888a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedTypeParameter, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference,
	          CompilerOptions.ENABLED);

	this.runNegativeTest(
		false /*skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		 new String[] {
 		"X.java",
         "/***\n" +
         " * @param <T>\n" +
         " */\n" +
         "public class X <T> {\n"+
         "/***\n" +
         " * @param <S>\n" +
         " */\n" +
         "	public <S> void ph(int i) {\n"+
         "	}\n"+
         "}\n"
         },
 		"----------\n" +
 		"1. ERROR in X.java (at line 8)\n" +
 		"	public <S> void ph(int i) {\n" +
 		"	                       ^\n" +
 		"The value of the parameter i is not used\n" +
 		"----------\n",
 		null, true, customOptions);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=397888
public void test397888b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedTypeParameter, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference,
        CompilerOptions.DISABLED);

	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
        new String[] {
     		   "X.java",
                "/***\n" +
                " * @param <T>\n" +
                " */\n" +
                "public class X <T> {\n"+
                "/***\n" +
                " * @param <S>\n" +
                " */\n" +
                "public <S> void ph() {\n"+
                "}\n"+
                "}\n"
        },
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public class X <T> {\n" +
		"	                ^\n" +
		"Unused type parameter T\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	public <S> void ph() {\n" +
		"	        ^\n" +
		"Unused type parameter S\n" +
		"----------\n",
		null, true, customOptions);
}
// Bug 401456 - Code compiles from javac/intellij, but fails from eclipse
public void test401456() {
	runConformTest(
		new String[] {
			"App.java",
			"import java.util.List;\n" +
			"\n" +
			"public class App {\n" +
			"\n" +
			"    public interface Command_1<T> {\n" +
			"        public void execute(T o);\n" +
			"    }\n" +
			"    public static class ObservableEventWithArg<T> {\n" +
			"        public class Monitor {\n" +
			"            public Object addListener(final Command_1<T> l) {\n" +
			"                return null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"    public static class Context<T> {\n" +
			"          public ObservableEventWithArg<String>.Monitor getSubmissionErrorEventMonitor() {\n" +
			"              return new ObservableEventWithArg<String>().new Monitor();\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"        compileError(new Context<List<String>>());\n" +
			"    }\n" +
			"\n" +
			"    private static void compileError(Context context) {\n" +
			"        context.getSubmissionErrorEventMonitor().addListener(\n" + // here the inner message send bogusly resolved to ObservableEventWithArg#RAW.Monitor
			"            new Command_1<String>() {\n" +
			"                public void execute(String o) {\n" +
			"                }\n" +
			"            });\n" +
			"    }\n" +
			"}\n"
		});
}
// https://bugs.eclipse.org/405706 - Eclipse compiler fails to give compiler error when return type is a inferred generic
// original test
public void testBug405706a() {
	runNegativeTest(
		new String[] {
			"TypeUnsafe.java",
			"import java.util.Collection;\n" +
			"\n" +
			"public class TypeUnsafe {\n" +
			"	public static <Type,\n" +
			"			CollectionType extends Collection<Type>>\n" +
			"			CollectionType\n" +
			"			nullAsCollection(Class<Type> clazz) {\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		Collection<Integer> integers = nullAsCollection(String.class);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in TypeUnsafe.java (at line 12)\n" +
		"	Collection<Integer> integers = nullAsCollection(String.class);\n" +
		"	                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Collection<String> to Collection<Integer>\n" +
		"----------\n");
}
// https://bugs.eclipse.org/405706 - Eclipse compiler fails to give compiler error when return type is a inferred generic
// include compatibility List <: Collection
public void testBug405706b() {
	runNegativeTest(
		new String[] {
			"TypeUnsafe.java",
			"import java.util.Collection;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class TypeUnsafe {\n" +
			"	public static <Type,\n" +
			"			CollectionType extends List<Type>>\n" +
			"			CollectionType\n" +
			"			nullAsList(Class<Type> clazz) {\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		Collection<Integer> integers = nullAsList(String.class);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in TypeUnsafe.java (at line 13)\n" +
		"	Collection<Integer> integers = nullAsList(String.class);\n" +
		"	                               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from List<String> to Collection<Integer>\n" +
		"----------\n");
}

// https://bugs.eclipse.org/408441 - Type mismatch using Arrays.asList with 3 or more implementations of an interface with the interface type as the last parameter
public void testBug408441() {
	runConformTest(
		new String[] {
			"TypeMistmatchIssue.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"\n" +
			"\n" +
			"public class TypeMistmatchIssue {\n" +
			"	static interface A {\n" +
			"	}\n" +
			"	static class B implements A {\n" +
			"	}\n" +
			"	static class C implements A {\n" +
			"	}\n" +
			"	static class D implements A {\n" +
			"	}\n" +
			"	\n" +
			"	void illustrate() {\n" +
			"		List<Class<? extends A>> no1= Arrays.asList(B.class, A.class);						// compiles\n" +
			"		List<Class<? extends A>> no2= Arrays.asList(C.class, B.class, A.class);				// compiles\n" +
			"		List<Class<? extends A>> no3= Arrays.asList(D.class, B.class, A.class);				// compiles\n" +
			"		\n" +
			"		List<Class<? extends A>> no4= Arrays.asList(D.class, C.class, B.class, A.class);	// cannot convert error !!!\n" +
			"\n" +
			"		List<Class<? extends A>> no5= Arrays.asList(A.class, B.class, C.class, D.class);	// compiles\n" +
			"		List<Class<? extends A>> no6= Arrays.asList(A.class, D.class, C.class, B.class);	// compiles\n" +
			"	}\n" +
			"}\n"
		});
}

// https://bugs.eclipse.org/413958 - Function override returning inherited Generic Type
public void testBug413958_1() {
	runConformTest(
		new String[] {
			"TestA.java",
			"public class TestA { }\n",
			"TestB.java",
			"public class TestB { }\n",
			"ReadOnlyWrapper.java",
			"@SuppressWarnings(\"unchecked\")\n" +
			"public class ReadOnlyWrapper<A extends TestA, B extends TestB> {\n" +
			"    protected A a;\n" +
			"    protected B b;\n" +
			"    public ReadOnlyWrapper(A ax,B bx){\n" +
			"        this.a = ax;\n" +
			"        this.b = bx;\n" +
			"    }\n" +
			"    public <X extends ReadOnlyWrapper<A,B>> X copy() {\n" +
			"        return (X) new ReadOnlyWrapper<A,B>(a,b);\n" +
			"    }\n" +
			"    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy() {\n" +
			"        return (X) new ReadOnlyWrapper<A,B>(a,b);\n" +
			"    }\n" +
			"    public A getA() {\n" +
			"        return this.a;\n" +
			"    }\n" +
			"    public B getB() {\n" +
			"        return this.b;\n" +
			"    }\n" +
			"}",
			"WritableWrapper.java",
			"@SuppressWarnings(\"unchecked\")\n" +
			"public class WritableWrapper<A extends TestA, B extends TestB> extends ReadOnlyWrapper<A, B> {\n" +
			"    public WritableWrapper(A ax,B bx){\n" +
			"        super(ax,bx);\n" +
			"    }\n" +
			"    @Override\n" +
			"    public <X extends ReadOnlyWrapper<A,B>> X copy() {\n" +
			"        return (X) new WritableWrapper<A, B>(a,b);\n" +
			"    }\n" +
			"    @Override\n" +
			"    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy() {\n" +
			"        // Works in Indigo, Fails in Kepler\n" +
			"        return (X) new WritableWrapper<A,B>(a,b);\n" +
			"    }\n" +
			"    public void setA(A ax) {\n" +
			"        this.a = ax;\n" +
			"    }\n" +
			"    public void setB(B bx) {\n" +
			"        this.b = bx;\n" +
			"    }\n" +
			"}\n",
			"TestGenerics.java",
			"public class TestGenerics {\n" +
			"    public static void main(String [] args) {\n" +
			"        final WritableWrapper<TestA, TestB> v1 = new WritableWrapper<TestA, TestB>(new TestA(), new TestB());\n" +
			"        final WritableWrapper<TestA,TestB> v2 = v1.copy();\n" +
			"        final WritableWrapper<TestA,TestB> v3 = v1.icopy();\n" +
			"    }\n" +
			"}\n"
		});
}
// https://bugs.eclipse.org/413958 - Function override returning inherited Generic Type
// Passing since https://bugs.eclipse.org/423496
public void testBug413958_2() {
	String[] sourceFiles =
		new String[] {
			"TestA.java",
			"public class TestA { }\n",
			"TestB.java",
			"public class TestB { }\n",
			"TestA2.java",
			"public class TestA2 extends TestA { }\n",
			"ReadOnlyWrapper.java",
			"@SuppressWarnings(\"unchecked\")\n" +
			"public class ReadOnlyWrapper<A extends TestA, B extends TestB> {\n" +
			"    protected A a;\n" +
			"    protected B b;\n" +
			"    public ReadOnlyWrapper(A ax,B bx){\n" +
			"        this.a = ax;\n" +
			"        this.b = bx;\n" +
			"    }\n" +
			"    public <X extends ReadOnlyWrapper<A,B>> X copy() {\n" +
			"        return (X) new ReadOnlyWrapper<A,B>(a,b);\n" +
			"    }\n" +
			"    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy() {\n" +
			"        return (X) new ReadOnlyWrapper<A,B>(a,b);\n" +
			"    }\n" +
			"    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy2(TA in) {\n" +
			"        return (X) new ReadOnlyWrapper<A,B>(a,b);\n" +
			"    }\n" +
			"    public A getA() {\n" +
			"        return this.a;\n" +
			"    }\n" +
			"    public B getB() {\n" +
			"        return this.b;\n" +
			"    }\n" +
			"}",
			"WritableWrapper.java",
			"@SuppressWarnings(\"unchecked\")\n" +
			"public class WritableWrapper<A extends TestA, B extends TestB> extends ReadOnlyWrapper<A, B> {\n" +
			"    public WritableWrapper(A ax,B bx){\n" +
			"        super(ax,bx);\n" +
			"    }\n" +
			"    @Override\n" +
			"    public <X extends ReadOnlyWrapper<A,B>> X copy() {\n" +
			"        return (X) new WritableWrapper<A, B>(a,b);\n" +
			"    }\n" +
			"    @Override\n" +
			"    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy() {\n" +
			"        return (X) new WritableWrapper<A,B>(a,b);\n" +
			"    }\n" +
			"    @Override\n" +
			"    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy2(TA in) {\n" +
			"        return (X) new WritableWrapper<A,B>(a,b);\n" +
			"    }\n" +
			"    public void setA(A ax) {\n" +
			"        this.a = ax;\n" +
			"    }\n" +
			"    public void setB(B bx) {\n" +
			"        this.b = bx;\n" +
			"    }\n" +
			"}\n",
			"TestGenerics.java",
			"public class TestGenerics {\n" +
			"    public static void main(String [] args) {\n" +
			"        final WritableWrapper<TestA, TestB> v1 = new WritableWrapper<TestA, TestB>(new TestA(), new TestB());\n" +
			"        final WritableWrapper<TestA,TestB> v2 = v1.copy();\n" +
			"        final WritableWrapper<TestA,TestB> v3 = v1.icopy();\n" +
			"        final WritableWrapper<TestA2,TestB> v4 = v1.icopy();\n" +
			"        final WritableWrapper<TestA2,TestB> v5 = v1.icopy2(new TestA2());\n" +
			"    }\n" +
			"}\n"
		};
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		runNegativeTest(
			sourceFiles,
			"----------\n" +
			"1. ERROR in TestGenerics.java (at line 6)\n" +
			"	final WritableWrapper<TestA2,TestB> v4 = v1.icopy();\n" +
			"	                                         ^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from ReadOnlyWrapper<TestA,TestB> to WritableWrapper<TestA2,TestB>\n" +
			"----------\n");
	else
		runConformTest(sourceFiles);
}
public void testBug415734() {
	String compileSrc =
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class Compile {\n" +
			"\n" +
			"    public <T, Exp extends List<T>> Exp typedNull() {\n" +
			"        return null;\n" +
			"    }\n" +
			"\n" +
			"    public void call() {\n" +
			"        ArrayList<String> list = typedNull();\n" +
			"    }\n" +
			"}\n";
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.EclipseHasABug.EclipseBug428061,
			new String[] {
				"Compile.java",
				compileSrc
			},
			"----------\n" +
			"1. ERROR in Compile.java (at line 11)\n" +
			"	ArrayList<String> list = typedNull();\n" +
			"	                         ^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from List<Object> to ArrayList<String>\n" +
			"----------\n");
	} else {
		runConformTest(
			new String[] {
				"Compile.java",
				compileSrc
			});
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426534, [1.8][compiler] Accessibility of vararg element type not checked for generic methods.
public void test426534() {
	runNegativeTest(
		new String[] {
			"p/B.java",
			"package p;\n" +
			"class A {\n" +
			"}\n" +
			"public class B extends A {\n" +
			"    public <T extends A> void foo(T ... o) { }\n" +
			"}\n",

			"X.java",
			"import p.*;\n" +
			"public class X  {\n" +
			"    public static void main(String argv[]) {\n" +
			"        new B().foo(null, null);\n" +
			"    }\n" +
			"}\n"
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	new B().foo(null, null);\n" +
				"	        ^^^\n" +
				"The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here\n" +
				"----------\n" :
					"----------\n" +
					"1. WARNING in p\\B.java (at line 5)\n" +
					"	public <T extends A> void foo(T ... o) { }\n" +
					"	                                    ^\n" +
					"Type safety: Potential heap pollution via varargs parameter o\n" +
					"----------\n" +
					"----------\n" +
					"1. ERROR in X.java (at line 4)\n" +
					"	new B().foo(null, null);\n" +
					"	        ^^^\n" +
					"The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here\n" +
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426589, [1.8][compiler] Compiler error with generic method/constructor invocation as vargs argument
public void test426589() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void take(String... strings) {\n" +
				"	}\n" +
				"	void test() {\n" +
				"		take(getString());\n" +
				"	}\n" +
				"	private <T> String getString() {\n" +
				"		return \"hi\";\n" +
				"	}\n" +
				"}\n"
			},
			"");
}
public void testBug426590() {
	runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"		\n" +
			"	}\n" +
			"	\n" +
			"	class B extends A {\n" +
			"		\n" +
			"	}\n" +
			"	\n" +
			"	class C extends B {\n" +
			"		\n" +
			"	}\n" +
			"	\n" +
			"	class D {\n" +
			"		D(A a) {\n" +
			"			\n" +
			"		}\n" +
			"		\n" +
			"		D(boolean b) {\n" +
			"			this(b ? new B() : new C());\n" +
			"		}\n" +
			"	}\n"
		});
}
public void testBug426590b() {
	runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"		\n" +
			"	}\n" +
			"	\n" +
			"	class B extends A {\n" +
			"		\n" +
			"	}\n" +
			"	\n" +
			"	class C extends B {\n" +
			"		\n" +
			"	}\n" +
			"	\n" +
			"	class D {\n" +
			"		void bla(boolean b) {\n" +
			"			test(b ? new B() : new C());\n" +
			"		}\n" +
			"		\n" +
			"		void test(A a) {\n" +
			"			\n" +
			"		}\n" +
			"	}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633() {
	runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	<T> void foo (T... p);\n" +
			"}\n" +
			"abstract class A implements I {\n" +
			"	public void foo(Object [] p) {}\n" +
			"}\n" +
			"public class X extends A {\n" +
			"	public static void main(String[] args) {\n" +
			"		A a = new X();\n" +
			"		a.foo(\"hello\", \"world\");\n" +
			"	}\n" +
			"}\n"

		},
		this.complianceLevel >= ClassFileConstants.JDK1_8 ?
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	<T> void foo (T... p);\n" +
			"	                   ^\n" +
			"Type safety: Potential heap pollution via varargs parameter p\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	public void foo(Object [] p) {}\n" +
			"	            ^^^^^^^^^^^^^^^^\n" +
			"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	a.foo(\"hello\", \"world\");\n" +
			"	  ^^^\n" +
			"The method foo(Object[]) in the type A is not applicable for the arguments (String, String)\n" +
			"----------\n" :
				this.complianceLevel >= ClassFileConstants.JDK1_7 ?
					"----------\n" +
					"1. WARNING in X.java (at line 2)\n" +
					"	<T> void foo (T... p);\n" +
					"	                   ^\n" +
					"Type safety: Potential heap pollution via varargs parameter p\n" +
					"----------\n" +
					"2. WARNING in X.java (at line 5)\n" +
					"	public void foo(Object [] p) {}\n" +
					"	            ^^^^^^^^^^^^^^^^\n" +
					"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" +
					"----------\n" :
						"----------\n" +
						"1. WARNING in X.java (at line 5)\n" +
						"	public void foo(Object [] p) {}\n" +
						"	            ^^^^^^^^^^^^^^^^\n" +
						"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" +
						"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
					"	 <T> void foo (T... p);\n" +
					"}\n" +
					"abstract class A  {\n" +
					"	public void foo(Object [] p) {\n" +
					"		System.out.println(\"A.foo\");\n" +
					"	}\n" +
					"}\n" +
					"abstract class B extends A implements I {\n" +
					"}\n" +
					"public class X extends B implements I {\n" +
					"	public static void main(String[] args) {\n" +
					"		B b = new X();\n" +
					"		b.foo(\"hello\", \"world\");\n" +
					"	}\n" +
					"}\n"
		},
		this.complianceLevel >= ClassFileConstants.JDK1_8 ?
				"----------\n" +
				"1. WARNING in X.java (at line 2)\n" +
				"	<T> void foo (T... p);\n" +
				"	                   ^\n" +
				"Type safety: Potential heap pollution via varargs parameter p\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 9)\n" +
				"	abstract class B extends A implements I {\n" +
				"	               ^\n" +
				"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" +
				"----------\n" +
				"3. WARNING in X.java (at line 11)\n" +
				"	public class X extends B implements I {\n" +
				"	             ^\n" +
				"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 14)\n" +
				"	b.foo(\"hello\", \"world\");\n" +
				"	  ^^^\n" +
				"The method foo(T...) of type I cannot be invoked as it is overridden by an inapplicable method\n" +
				"----------\n" :
				this.complianceLevel >= ClassFileConstants.JDK1_7 ?
						"----------\n" +
						"1. WARNING in X.java (at line 2)\n" +
						"	<T> void foo (T... p);\n" +
						"	                   ^\n" +
						"Type safety: Potential heap pollution via varargs parameter p\n" +
						"----------\n" +
						"2. WARNING in X.java (at line 9)\n" +
						"	abstract class B extends A implements I {\n" +
						"	               ^\n" +
						"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" +
						"----------\n" +
						"3. WARNING in X.java (at line 11)\n" +
						"	public class X extends B implements I {\n" +
						"	             ^\n" +
						"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" +
						"----------\n" :
							"----------\n" +
							"1. WARNING in X.java (at line 9)\n" +
							"	abstract class B extends A implements I {\n" +
							"	               ^\n" +
							"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" +
							"----------\n" +
							"2. WARNING in X.java (at line 11)\n" +
							"	public class X extends B implements I {\n" +
							"	             ^\n" +
							"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" +
							"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	 <T> void foo (T... p);\n" +
			"}\n" +
			"abstract class A  {\n" +
			"	public abstract void foo(Object [] p);\n" +
			"}\n" +
			"abstract class B extends A implements I {\n" +
			"}\n" +
			"public abstract class X extends B implements I {\n" +
			"	public static void main(B b) {\n" +
			"		b.foo(\"hello\", \"world\");\n" +
			"	}\n" +
			"}\n"
		},
		this.complianceLevel >= ClassFileConstants.JDK1_7 ?
				"----------\n" +
				"1. WARNING in X.java (at line 2)\n" +
				"	<T> void foo (T... p);\n" +
				"	                   ^\n" +
				"Type safety: Potential heap pollution via varargs parameter p\n" +
				"----------\n" : "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426678, [1.8][compiler] Another issue with vararg type element accessibility
public void test426678() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import p.*;\n" +
			"public class X  {\n" +
			"    public static void main(String argv[]) {\n" +
			"        new B().foo(null, null);\n" +
			"    }\n" +
			"}\n",

			"p/B.java",
			"package p;\n" +
			"class A {\n" +
			"}\n" +
			"public class B extends A {\n" +
			"    public <T extends A> void foo(T ... o) { System.out.println(\"PGMB\"); }\n" +
			"    public void foo(Object... o) { System.out.println(\"MB\"); }\n" +
			"}\n",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	new B().foo(null, null);\n" +
				"	        ^^^\n" +
				"The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here\n" +
				"----------\n" :
					"----------\n" +
					"1. ERROR in X.java (at line 4)\n" +
					"	new B().foo(null, null);\n" +
					"	        ^^^\n" +
					"The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here\n" +
					"----------\n" +
					"----------\n" +
					"1. WARNING in p\\B.java (at line 5)\n" +
					"	public <T extends A> void foo(T ... o) { System.out.println(\"PGMB\"); }\n" +
					"	                                    ^\n" +
					"Type safety: Potential heap pollution via varargs parameter o\n" +
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426678, [1.8][compiler] Another issue with vararg type element accessibility
public void test426678a() {
	runConformTest(
		new String[] {
			"X.java",
			"import p.*;\n" +
			"public class X  {\n" +
			"    public static void main(String argv[]) {\n" +
			"        new B().foo(null, null);\n" +
			"    }\n" +
			"}\n",
			"p/A.java",
			"package p;\n" +
			"public class A {\n" +
			"}\n",
			"p/B.java",
			"package p;\n" +
			"public class B extends A {\n" +
			"    public <T extends A> void foo(T ... o) { System.out.println(\"PGMB\"); }\n" +
			"    public void foo(Object... o) { System.out.println(\"MB\"); }\n" +
			"}\n",
		},
		"PGMB");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421922, [1.8][compiler] Varargs & Overload - Align to JLS8
public void test421922() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import p.*;\n" +
			"public class X  {\n" +
			"    public static void main(String argv[]) {\n" +
			"        new B().foo(null, null);\n" +
			"    }\n" +
			"}\n",

			"p/B.java",
			"package p;\n" +
			"interface A {\n" +
			"}\n" +
			"public class B implements A {\n" +
			"    public <T extends A> void foo(T ... o) { System.out.println(\"PGMB\"); }\n" +
			"    public void foo(Object... o) { System.out.println(\"MB\"); }\n" +
			"}\n",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	new B().foo(null, null);\n" +
		"	        ^^^\n" +
		"The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here\n" +
		"----------\n" :
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	new B().foo(null, null);\n" +
			"	        ^^^\n" +
			"The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in p\\B.java (at line 5)\n" +
			"	public <T extends A> void foo(T ... o) { System.out.println(\"PGMB\"); }\n" +
			"	                                    ^\n" +
			"Type safety: Potential heap pollution via varargs parameter o\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425719, [1.8][compiler] Bogus ambiguous call error from compiler.
public void test425719() {
	String interfaceMethod = this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"   <T> void foo(List<T> list);\n" :
				"   default <T> void foo(List<T> list) {\n" +
				"	   System.out.println(\"interface method\");\n" +
				"   }\n";

	runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"interface I {\n" +
			 interfaceMethod +
			"}\n" +
			"class Base {\n" +
			"    public <T> void foo(List<T> list) {\n" +
			"        System.out.println(\"class method\");\n" +
			"   }\n" +
			"}\n" +
			"public class X extends Base implements I {\n" +
			"	 public static void main(String argv[]) {\n" +
			"	    	new X().foo(new ArrayList<String>());\n" +
			"	    }\n" +
			"}\n",
		},
		"class method");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425719, [1.8][compiler] Bogus ambiguous call error from compiler.
public void test425719a() {
	String interfaceMethod = this.complianceLevel < ClassFileConstants.JDK1_8 ?
				"   <T> void foo(List<T> list);\n\n\n" :
				"   default <T> void foo(List<T> list) {\n" +
				"	   System.out.println(\"interface method\");\n" +
				"   }\n";

	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"interface I {\n" +
			 interfaceMethod +
			"}\n" +
			"abstract class Base {\n" +
			"    public abstract <T> void foo(List<T> list);\n" +
			"}\n" +
			"public abstract class X extends Base implements I {\n" +
			"	 public static void main(String argv[]) {\n" +
			"           X x = new Y();\n" +
			"	    	x.foo(new ArrayList<String>());\n" +
			"	    }\n" +
			"}\n" +
			"class Y extends X {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 17)\n" +
		"	class Y extends X {}\n" +
		"	      ^\n" +
		"The type Y must implement the inherited abstract method Base.foo(List<T>)\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425719, [1.8][compiler] Bogus ambiguous call error from compiler.
public void test425719b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	String interfaceMethod = this.complianceLevel < ClassFileConstants.JDK1_8 ?
				"   <T> void foo(List<T> list);\n\n\n" :
				"   default <T> void foo(List<T> list) {\n" +
				"	   System.out.println(\"interface method\");\n" +
				"   }\n";

	runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"interface I {\n" +
			 interfaceMethod +
			"}\n" +
			"abstract class Base {\n" +
			"    public abstract <T> void foo(List<T> list);\n" +
			"}\n" +
			"public abstract class X extends Base implements I {\n" +
			"	 public static void main(String argv[]) {\n" +
			"           X x = new Y();\n" +
			"	    	x.foo(new ArrayList<String>());\n" +
			"	    }\n" +
			"}\n" +
			"class Y extends X {\n" +
			"    public <T> void foo(List<T> list) {\n" +
			"        System.out.println(\"Y.foo\");\n" +
			"    }\n" +
			"}\n",
		},
		"Y.foo");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427282,  Internal compiler error: java.lang.ArrayIndexOutOfBoundsException: -1 at org.eclipse.jdt.internal.compiler.ClassFile.traverse
public void test427282() {
	runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		new String[] {
			"X.java",
			"import java.util.Collection;\n" +
			"public class X {\n" +
			"	public X(String... a) {\n" +
			"	}\n" +
			"	public static <T> T[] a(T[] a, T[] b) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void error() {\n" +
			"		final Collection<X> as = null;\n" +
			"       for (X a : as) {\n" +
			"           new X(X.a(new String[0], new String[0]));\n" +
			"       }\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 10)\n" +
		"	for (X a : as) {\n" +
		"	           ^^\n" +
		"Null pointer access: The variable as can only be null at this location\n" +
		"----------\n");
}
public void testBug427216() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test\n" +
			"{\n" +
			"   public static void main(String[] args)\n" +
			"   {\n" +
			"      foo(args); // ok in 1.7 and 1.8\n" +
			"      foo(java.util.Arrays.asList(\"1\").toArray(new String[0]));\n" +
			"		System.out.println(\"good\");\n" +
			"   }\n" +
			"\n" +
			"   private static void foo(String... args) { }\n" +
			"}\n"
		},
		"good");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427433, NPE at org.eclipse.jdt.internal.compiler.lookup.Scope.parameterCompatibilityLevel(Scope.java:4755)
public void testBug427433() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void testError() {\n" +
			"		assertEquals(A.e(null, null, null), null);\n" +
			"	}\n" +
			"	public static boolean assertEquals(String a, String b) {\n" +
			"		return false;\n" +
			"	}\n" +
			"	public static boolean assertEquals(Object a, Object b) {\n" +
			"		return false;\n" +
			"	}\n" +
			"}\n" +
			"class A {\n" +
			"	public static <T, V> V e(T[] t, V[] v, T object) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427433, NPE at org.eclipse.jdt.internal.compiler.lookup.Scope.parameterCompatibilityLevel(Scope.java:4755)
// variant to challenge a varargs invocation
public void testBug427433b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void testError() {\n" +
			"		assertEquals(A.e(null, null, null), null);\n" +
			"	}\n" +
			"	public static boolean assertEquals(String a, String b, X... xs) {\n" +
			"		return false;\n" +
			"	}\n" +
			"	public static boolean assertEquals(Object a, Object b, X... xs) {\n" +
			"		return false;\n" +
			"	}\n" +
			"}\n" +
			"class A {\n" +
			"	public static <T, V> V e(T[] t, V[] v, T object) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427438#c3, [1.8][compiler] NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode
public void testBug427438c3() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.Serializable;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	boolean b;\n" +
			"	public List<A> getLignes() {\n" +
			"		return (List<A>) data(b ? (Serializable) get() : null);\n" +
			"	}\n" +
			"	public List<A> get() {\n" +
			"		return null;\n" +
			"	}\n" +
			"	public <T extends Serializable> T data(T data) {\n" +
			"		return data;\n" +
			"	}\n" +
			"	public class A implements Serializable {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 6)\n" +
		"	return (List<A>) data(b ? (Serializable) get() : null);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked cast from Serializable to List<X.A>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 14)\n" +
		"	public class A implements Serializable {\n" +
		"	             ^\n" +
		"The serializable class A does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427411, [1.8][generics] JDT reports type mismatch when using method that returns generic type
public void test427411() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"public class X {\n" +
			"\n" +
			"    public static void main() {\n" +

			"        List<Object> list = null;\n" +
			"        Object o = null;\n" +

			"        genericMethod(list, genericClassTransformer(genericClassFactory(o)));\n" +

			"        genericMethod(list, genericClassFactory(o)); // works\n" +

			"        GenericClass<Iterable<? super Object>> tempVariable = genericClassTransformer(genericClassFactory(o));\n" +

			"        GenericClass<Iterable<? super Object>> tempVariable2 = genericClassFactory(o); // works\n" +

			"    }\n" +

			"    private static <T> void genericMethod(T param1, GenericClass<? super T> param2) {\n" +
			"        throw new UnsupportedOperationException();\n" +
			"    }\n" +

			"    public static <T> GenericClass<Iterable<? super T>> genericClassFactory(T item) {\n" +
			"        throw new UnsupportedOperationException();\n" +
			"    }\n" +

			"    public static <T> GenericClass<T> genericClassTransformer(GenericClass<T> matcher) {\n" +
			"        throw new UnsupportedOperationException();\n" +
			"    }\n" +

			"    private static class GenericClass<T> {\n" +
			"    }\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427728, [1.8] Type Inference rejects calls requiring boxing/unboxing
public void test427728() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static <T> int foo(T t) {\n" +
			"		return 1234;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"            goo(foo(10));\n" +
			"        }\n" +
			"	static void goo(Integer i) {\n" +
			"		System.out.println(i);\n" +
			"	}\n" +
			"}\n"
		},
		"1234");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427728, [1.8] Type Inference rejects calls requiring boxing/unboxing
public void test427728a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Collections;\n" +
			"public class X {\n" +
			"	public static void mai(String[] args) {\n" +
			"		Math.max(2345, java.util.Collections.max(Collections.<Integer>emptySet()));\n" +
			"		Math.max(0, java.util.Collections.<Integer>max(Collections.<Integer>emptySet()));\n" +
			"    }\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427736, [1.8][generics] Method not applicable error with identical parameter types
public void test427736() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	runNegativeTest(
		new String[] {
			"Test1.java",
			"class Test1<K, V> {\n" +
			" static class Node<K2, V2> {}\n" +
			" void keySet(V v) {}\n" +
			" /**\n" +
			"  * See {@link #keySet() keySet()},\n" +
			"  */\n" +
			" class KeySetView {}\n" +
			" static <K4, V4> void untree0(Node<K4, V4> hi) {}    \n" +
			" void untreesomething(Node<K, V> n) {\n" +
			"   untree0(n); \n" +
			" }\n" +
			"}\n"
		},
		"", null, true, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426836, [1.8] special handling for return type in references to method getClass() ?
public void test426836() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static <T> T id(T t) {\n" +
			"		return t;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		Class<? extends String> id = id(new X().getClass());\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	Class<? extends String> id = id(new X().getClass());\n" +
		"	                             ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Class<capture#1-of ? extends X> to Class<? extends String>\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428071, [1.8][compiler] Bogus error about incompatible return type during override
public void test428071() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
	runNegativeTest(
		new String[] {
			"K1.java",
			"import java.util.List;\n" +
			"import java.util.Map;\n" +
			"interface K1 {\n" +
			"	public Map<String,List> get();\n" +
			"}\n",
			"K.java",
			"import java.util.List;\n" +
			"import java.util.Map;\n" +
			"public class K implements K1 {\n" +
			"	public Map<String, List> get() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in K1.java (at line 4)\n" +
		"	public Map<String,List> get();\n" +
		"	                  ^^^^\n" +
		"List is a raw type. References to generic type List<E> should be parameterized\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in K.java (at line 4)\n" +
		"	public Map<String, List> get() {\n" +
		"	                   ^^^^\n" +
		"List is a raw type. References to generic type List<E> should be parameterized\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428019, [1.8][compiler] Type inference failures with nested generic invocation.
public void test428019() {
	runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"  static class Obj {}\n" +
			"  static class Dial<T> {}\n" +
			"  static void foo(Dial<? super Obj> dial, X context) {\n" +
			"    context.put(Dial.class, wrap(dial));\n" +
			"  }\n" +
			"  <T> void put(Class<T> clazz, T data) {\n" +
			"	System.out.println(\"put\");\n" +
			"  }\n" +
			"  static <T> Dial<T> wrap(Dial<T> dl) {\n" +
			"	  return null;\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"	X.foo(new Dial<Obj>(), new X());\n" +
			"  }\n" +
			"}\n"
		},
		"put");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428285,  [1.8][compiler] ECJ fails to recognize ? super Object == { Object }
public void test428285() {
	runConformTest(
		new String[] {
			"X.java",
			"class Reference<T> {\n" +
			"	ReferenceQueue<? super T>  queue;\n" +
			"}\n" +
			"class ReferenceQueue<T> {\n" +
			"}\n" +
			"public class X {\n" +
			"    public static void main(String args[]) {\n" +
			"            Reference<Object> r = new Reference<Object>();\n" +
			"            ReferenceQueue<Object> q = r.queue;\n" +
			"            System.out.println(\"OK\");\n" +
			"    }\n" +
			"}\n"
		},
		"OK");
}
public void testBug428366() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	<T> void m(String s, int i) {}\n" +
			"	<T> void m(String s1, String s2) {}\n" +
			"	void test() {\n" +
			"		m(\"1\", null);\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
public void test429733b() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		test(id(1.1d));\n" +
			"	}\n" +
			"	static <S> void test(S value) {\n" +
			"       System.out.println(value);\n" +
			"	}\n" +
			"	static <T> T id(T t) {\n" +
			"       return t;\n" +
			"   }\n" +
			"}\n"
		},
		"1.1");
}
public void test429733c() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new X();\n" +
			"	}\n" +
			"	<S> X(S value) {\n" +
			"       System.out.println(value);\n" +
			"	}\n" +
			"	static <T> T id(T t) {\n" +
			"       return t;\n" +
			"   }\n" +
			"   X() {\n" +
			"      this(id(1.1d));\n" +
			"   }\n" +
			"}\n"
		},
		"1.1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426537,  [1.8][inference] Eclipse compiler thinks I<? super J> is compatible with I<J<?>> - raw type J involved
public void testBug426537() { // non generic case
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(J[] list, I<J<?>> i) {\n" +
			"		sort(list, i);\n" +
			"	}\n" +
			"	J[] sort(J[] list, I<? super J> i) {\n" +
			"		return list;\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {}\n" +
			"interface J<T> {}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	void foo(J[] list, I<J<?>> i) {\n" +
		"	         ^\n" +
		"J is a raw type. References to generic type J<T> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	sort(list, i);\n" +
		"	^^^^\n" +
		"The method sort(J[], I<? super J>) in the type X is not applicable for the arguments (J[], I<J<?>>)\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	J[] sort(J[] list, I<? super J> i) {\n" +
		"	^\n" +
		"J is a raw type. References to generic type J<T> should be parameterized\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 5)\n" +
		"	J[] sort(J[] list, I<? super J> i) {\n" +
		"	         ^\n" +
		"J is a raw type. References to generic type J<T> should be parameterized\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 5)\n" +
		"	J[] sort(J[] list, I<? super J> i) {\n" +
		"	                             ^\n" +
		"J is a raw type. References to generic type J<T> should be parameterized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426537,  [1.8][inference] Eclipse compiler thinks I<? super J> is compatible with I<J<?>> - raw type J involved
public void testBug426537_generic() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(J[] list, I<J<?>> i) {\n" +
			"		sort(list, i);\n" +
			"	}\n" +
			"	<T> T[] sort(T[] list, I<? super T> i) {\n" +
			"		return list;\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {}\n" +
			"interface J<T> {}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	void foo(J[] list, I<J<?>> i) {\n" +
		"	         ^\n" +
		"J is a raw type. References to generic type J<T> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	sort(list, i);\n" +
		"	^^^^\n" +
		"The method sort(T[], I<? super T>) in the type X is not applicable for the arguments (J[], I<J<?>>)\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427957, [1.8] Type inference incorrect when a wildcard is missing
public void testBug427957() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    <T> void sort(T[] a, I<? super T> c) { }\n" +
			"    void foo(I[] e, I<I<?>> comp) {\n" +
			"        sort(e, comp);\n" +
			"    }\n" +
			"}\n" +
			"interface I<T> {}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	void foo(I[] e, I<I<?>> comp) {\n" +
		"	         ^\n" +
		"I is a raw type. References to generic type I<T> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	sort(e, comp);\n" +
		"	^^^^\n" +
		"The method sort(T[], I<? super T>) in the type X is not applicable for the arguments (I[], I<I<?>>)\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427992,  [1.8] compiler difference to javac involving a raw array
public void test427992() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return; // uses @Override
	runNegativeTest(
		new String[] {
			"X.java",
			"import static org.junit.Assert.assertArrayEquals;\n" +
			"import java.util.Arrays;\n" +
			"import org.junit.Test;\n" +
			"public class X {\n" +
			"  @Test(expected = IllegalArgumentException.class)\n" +
			"  public void shouldThrowExceptionWhenClassesAreNotInSameInheritanceTree() {\n" +
			"    Arrays.sort(new Class[] {Chimp.class, Cat.class}, ClassInheritanceDepthComparator.INSTANCE);\n" +
			"  }\n" +
			"  public static class Animal {\n" +
			"  }\n" +
			"  public static class Monkey extends Animal {\n" +
			"  }\n" +
			"  public static class Chimp extends Monkey {\n" +
			"  }\n" +
			"  public static class Cat extends Animal {\n" +
			"  }\n" +
			"public static class ClassInheritanceDepthComparator implements Comparator<Class<?>> {\n" +
			"  public static final ClassInheritanceDepthComparator INSTANCE = new ClassInheritanceDepthComparator();\n" +
			"  @Override\n" +
			"  public int compare(Class<?> c1, Class<?> c2) {\n" +
			"    if(c1.equals(c2)) {\n" +
			"      return 0;\n" +
			"    }\n" +
			"    if(c1.isAssignableFrom(c2)) {\n" +
			"      return -1;\n" +
			"    }\n" +
			"    if(c2.isAssignableFrom(c1)) {\n" +
			"      return 1;\n" +
			"    }\n" +
			"    throw new IllegalArgumentException(\"classes to compare must be in the same inheritance tree: \" + c1 + \"; \" + c2);\n" +
			"  }\n" +
			"}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static org.junit.Assert.assertArrayEquals;\n" +
		"	              ^^^^^^^^^\n" +
		"The import org.junit cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	import org.junit.Test;\n" +
		"	       ^^^^^^^^^\n" +
		"The import org.junit cannot be resolved\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	@Test(expected = IllegalArgumentException.class)\n" +
		"	 ^^^^\n" +
		"Test cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	Arrays.sort(new Class[] {Chimp.class, Cat.class}, ClassInheritanceDepthComparator.INSTANCE);\n" +
		"	       ^^^^\n" +
		"The method sort(T[], Comparator<? super T>) in the type Arrays is not applicable for the arguments (Class[], X.ClassInheritanceDepthComparator)\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 17)\n" +
		"	public static class ClassInheritanceDepthComparator implements Comparator<Class<?>> {\n" +
		"	                                                               ^^^^^^^^^^\n" +
		"Comparator cannot be resolved to a type\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 20)\n" +
		"	public int compare(Class<?> c1, Class<?> c2) {\n" +
		"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method compare(Class<?>, Class<?>) of type X.ClassInheritanceDepthComparator must override or implement a supertype method\n" +
		"----------\n");
}
public void testBug430987() {
	String source =
			"public class X {\n" +
			"\n" +
			"  public static interface Foo<T> {\n" +
			"    // no content\n" +
			"  }\n" +
			"\n" +
			"  public void compileError() {\n" +
			"    doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );\n" +
			"  }\n" +
			"\n" +
			"  public void noCompileError() {\n" +
			"    Foo foo = any( Foo.class );\n" +
			"    doSomethingWithFoo( foo, foo );\n" +
			"  }\n" +
			"\n" +
			"  public void fix() {\n" +
			"    this.<Object>doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );\n" +
			"  }\n" +
			"\n" +
			"  public <T> void  doSomethingWithFoo( Foo<T> foo, Foo<T> foo2 ) {\n" +
			"    // do something\n" +
			"  }\n" +
			"\n" +
			"  public static <T> T any(Class<T> clazz) {\n" +
			"    return null;\n" +
			"  }\n" +
			"\n" +
			"}\n";
		runNegativeTest(
			new String[] {
				"X.java",
				source
			},
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked invocation doSomethingWithFoo(X.Foo, X.Foo) of the generic method doSomethingWithFoo(X.Foo<T>, X.Foo<T>) of type X\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );\n" +
			"	                    ^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 8)\n" +
			"	doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );\n" +
			"	                                      ^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 12)\n" +
			"	Foo foo = any( Foo.class );\n" +
			"	^^^\n" +
			"X.Foo is a raw type. References to generic type X.Foo<T> should be parameterized\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 13)\n" +
			"	doSomethingWithFoo( foo, foo );\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked invocation doSomethingWithFoo(X.Foo, X.Foo) of the generic method doSomethingWithFoo(X.Foo<T>, X.Foo<T>) of type X\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 13)\n" +
			"	doSomethingWithFoo( foo, foo );\n" +
			"	                    ^^^\n" +
			"Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 13)\n" +
			"	doSomethingWithFoo( foo, foo );\n" +
			"	                         ^^^\n" +
			"Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 17)\n" +
			"	this.<Object>doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked invocation doSomethingWithFoo(X.Foo, X.Foo) of the generic method doSomethingWithFoo(X.Foo<T>, X.Foo<T>) of type X\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 17)\n" +
			"	this.<Object>doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );\n" +
			"	                                 ^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>\n" +
			"----------\n" +
			"10. WARNING in X.java (at line 17)\n" +
			"	this.<Object>doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );\n" +
			"	                                                   ^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>\n" +
			"----------\n");
}
public void testBug430686() {
	runConformTest(
		new String[] {
			"TestClass.java",
			"\n" +
			"public class TestClass\n" +
			"{\n" +
			"    private static class Alice<A extends Alice<A, B>, B extends Bob>\n" +
			"    {\n" +
			"    }\n" +
			"\n" +
			"    public static class Bob\n" +
			"    {\n" +
			"    }\n" +
			"\n" +
			"    public void callingMethod()\n" +
			"    {\n" +
			"        calledMethod(); // error: The method calledMethod() in the type TestClass is not applicable for the arguments ()\n" +
			"    }\n" +
			"\n" +
			"    private <A extends Alice<A, B>, B extends Bob> A calledMethod()\n" +
			"    {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n"
		});
}
public void testBug430759() {
	runConformTest(
		new String[] {
			"A.java",
			"class CriteriaBuilder {\n" +
			"\n" +
			"}\n" +
			"\n" +
			"class CriteriaQuery<T> {\n" +
			"\n" +
			"}\n" +
			"\n" +
			"class Root<T> {\n" +
			"\n" +
			"}\n" +
			"\n" +
			"public class A<E> {\n" +
			"\n" +
			"    protected abstract class CustomGenericQuery<T> {\n" +
			"    }\n" +
			"\n" +
			"    protected <T> T executeCustomSingleQuery(CustomGenericQuery<T> customQuery, Class<T> resultClass) {\n" +
			"	return null;\n" +
			"    }\n" +
			"\n" +
			"    public Long getCount() {\n" +
			"	return executeCustomSingleQuery(\n" +
			"\n" +
			"	new CustomGenericQuery<Long>() {\n" +
			"	    public void customizeQuery(final Root<E> root, final CriteriaBuilder cb,\n" +
			"		    CriteriaQuery<Long> cq) {\n" +
			"	    }\n" +
			"	}, Long.class);\n" +
			"    }\n" +
			"}\n"
		});
}
public void testBug431408() {
	runConformTest(
		new String[] {
			"EclipseJava8Generics.java",
			"public class EclipseJava8Generics {\n" +
			"\n" +
			"  public interface Foo<V> {\n" +
			"  }\n" +
			"\n" +
			"  public static class FooBar<V, T extends Foo<V>> {\n" +
			"  }\n" +
			"\n" +
			"  public static class BaseClass {\n" +
			"    protected <V> FooBar<V, ? extends Foo<V>> doSomething() {\n" +
			"      return null;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public static class DerivedClass extends BaseClass {\n" +
			"    @Override\n" +
			"    protected <V> FooBar<V, ? extends Foo<V>> doSomething() {\n" +
			"      //Eclipse 4.3.2 with Java 8 can't compile the next line \n" +
			"      FooBar<V, ? extends Foo<V>> prop = super.doSomething();\n" +
			"      return prop;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		});
}

public void testBug431581() {
	runNegativeTest(
		new String[] {
			"BugEclipse.java",
			"public class BugEclipse\n" +
			"{\n" +
			"  static Dog dog = new Dog();\n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    System.out.println(\"bug compile eclipse\");\n" +
			"    Cat cat = getDog(); // <- error here, eclipse compile this line but the execution print ClassCastException\n" +
			"  }\n" +
			"  public static <T extends Dog> T getDog()\n" +
			"  {\n" +
			"    return (T) dog;\n" +
			"  }\n" +
			"  static class Cat {\n" +
			"  }\n" +
			"  static class Dog {\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in BugEclipse.java (at line 7)\n" +
		"	Cat cat = getDog(); // <- error here, eclipse compile this line but the execution print ClassCastException\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8	?
		"	          ^^^^^^\n" +
		"Bound mismatch: The generic method getDog() of type BugEclipse is not applicable for the arguments (). The inferred type BugEclipse.Cat&BugEclipse.Dog is not a valid substitute for the bounded parameter <T extends BugEclipse.Dog>\n"
		:
		"	          ^^^^^^^^\n" +
		"Type mismatch: cannot convert from BugEclipse.Dog to BugEclipse.Cat\n"
		) +
		"----------\n" +
		"2. WARNING in BugEclipse.java (at line 11)\n" +
		"	return (T) dog;\n" +
		"	       ^^^^^^^\n" +
		"Type safety: Unchecked cast from BugEclipse.Dog to T\n" +
		"----------\n");
}
public void testBug432603() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"import java.util.Map;\n" +
			"import java.util.Map.Entry;\n" +
			"\n" +
			"abstract class Optional<T> {\n" +
			"	public static <T> Optional<T> fromNullable(T t) { return null; }\n" +
			"	abstract Optional<T> or(Optional<? extends T> secondChoice);\n" +
			"	abstract T or(Supplier<? extends T> supplier);\n" +
			"	abstract T or(T defaultValue);\n" +
			"}\n" +
			"\n" +
			"interface Supplier<T> { T get(); }\n" +
			"\n" +
			"public class Test {\n" +
			"\n" +
			"    private static final Object NO_VALUE = new Object();\n" +
			"\n" +
			"    public void method(Map<String, ?> map) {\n" +
			"        for (Entry<String, ?> entry : map.entrySet()) {\n" +
			"            Optional.fromNullable(entry.getValue()).or(NO_VALUE);\n" +
			"//                                                  ^^ error here\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 19)\n" +
		"	Optional.fromNullable(entry.getValue()).or(NO_VALUE);\n" +
		"	                                        ^^\n" +
		"The method or(Optional<? extends capture#2-of ?>) in the type Optional<capture#2-of ?> is not applicable for the arguments (Object)\n" +
		"----------\n",
		JavacTestOptions.Excuse.JavacCompilesIncorrectSource);
}
public void testBug432603a() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.Map;\n" +
			"import java.util.Map.Entry;\n" +
			"\n" +
			"abstract class Optional<T> {\n" +
			"	public static <T> Optional<T> fromNullable(T t) { return null; }\n" +
			"	abstract Optional<T> or(Optional<? extends T> secondChoice);\n" +
			"	abstract T or(Supplier<? extends T> supplier);\n" +
			"	abstract T or(T defaultValue);\n" +
			"}\n" +
			"\n" +
			"interface Supplier<T> { T get(); }\n" +
			"\n" +
			"public class Test {\n" +
			"\n" +
			"    private static final Object NO_VALUE = new Object();\n" +
			"\n" +
			"    public void method(Map<String, ?> map) {\n" +
			"        for (Entry<String, ?> entry : map.entrySet()) {\n" +
			"            Optional.<Object>fromNullable(entry.getValue()).or(NO_VALUE);\n" +
			"//                                                  ^^ error here\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		});
}
public void testBug399527() {
	runNegativeTest(
		false /*skipJavac */,
		JavacTestOptions.Excuse.JavacCompilesIncorrectSource,
		new String[] {
			"TypeInferenceProblem.java",
			"\n" +
			"public class TypeInferenceProblem {\n" +
			"  interface HeaderAccess<T> {\n" +
			"    T getHeader();\n" +
			"  }\n" +
			"\n" +
			"  interface IExpectationSetters<T> {\n" +
			"    IExpectationSetters<T> andReturn(T value);\n" +
			"  }\n" +
			"\n" +
			"  static class MocksControl implements IExpectationSetters<Object> {\n" +
			"    public IExpectationSetters<Object> andReturn(Object value) {\n" +
			"      return null;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  @SuppressWarnings(\"unchecked\")\n" +
			"  public static <T> IExpectationSetters<T> expect(final T value) {\n" +
			"    return (IExpectationSetters<T>) new MocksControl();\n" +
			"  }\n" +
			"\n" +
			"  private HeaderAccess<Object> mockHeaderAccess;\n" +
			"  private HeaderAccess<?> unboundedMockHeaderAccess;\n" +
			"\n" +
			"  public void test() {\n" +
			"    // No error\n" +
			"    expect(mockHeaderAccess.getHeader()).andReturn(new Object());\n" +
			"    /*\n" +
			"     * Error: The method andReturn(capture#1-of ?) in the type\n" +
			"     * TypeInferenceProblem.IExpectationSetters<capture#1-of ?> \n" +
			"     * is not applicable for the arguments (Object)\n" +
			"     */\n" +
			"    expect(unboundedMockHeaderAccess.getHeader()).andReturn(new Object());\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in TypeInferenceProblem.java (at line 33)\n" +
		"	expect(unboundedMockHeaderAccess.getHeader()).andReturn(new Object());\n" +
		"	                                              ^^^^^^^^^\n" +
		"The method andReturn(capture#1-of ?) in the type TypeInferenceProblem.IExpectationSetters<capture#1-of ?> is not applicable for the arguments (Object)\n" +
		"----------\n");
}
public void testBug399527_corrected() {
	runConformTest(
		new String[] {
			"TypeInferenceProblem.java",
			"\n" +
			"public class TypeInferenceProblem {\n" +
			"  interface HeaderAccess<T> {\n" +
			"    T getHeader();\n" +
			"  }\n" +
			"\n" +
			"  interface IExpectationSetters<T> {\n" +
			"    IExpectationSetters<T> andReturn(T value);\n" +
			"  }\n" +
			"\n" +
			"  static class MocksControl implements IExpectationSetters<Object> {\n" +
			"    public IExpectationSetters<Object> andReturn(Object value) {\n" +
			"      return null;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  @SuppressWarnings(\"unchecked\")\n" +
			"  public static <T> IExpectationSetters<T> expect(final T value) {\n" +
			"    return (IExpectationSetters<T>) new MocksControl();\n" +
			"  }\n" +
			"\n" +
			"  private HeaderAccess<Object> mockHeaderAccess;\n" +
			"  private HeaderAccess<?> unboundedMockHeaderAccess;\n" +
			"\n" +
			"  public void test() {\n" +
			"    // No error\n" +
			"    expect(mockHeaderAccess.getHeader()).andReturn(new Object());\n" +
			"    this.<Object>expect(unboundedMockHeaderAccess.getHeader()).andReturn(new Object());\n" +
			"  }\n" +
			"}\n"
		});
}
public void testBug399527_comment1() {
	String sourceString =
			"public class TypeInferenceProblemMin {\n" +
			"  interface HeaderAccess<T> {\n" +
			"    T getHeader();\n" +
			"  }\n" +
			"\n" +
			"  interface IExpectationSetters<T> {\n" +
			"  }\n" +
			"\n" +
			"  public static <T> IExpectationSetters<T> expect(final T value) {\n" +
			"	  return null;\n" +
			"  }\n" +
			"\n" +
			"  private HeaderAccess<?> unboundedMockHeaderAccess;\n" +
			"  \n" +
			"  public void test() {\n" +
			"    // no error:\n" +
			"    Object header = unboundedMockHeaderAccess.getHeader();\n" +
			"    IExpectationSetters<Object> exp1 = expect(header);\n" +
			"\n" +
			"    // Type mismatch: cannot convert from TypeInferenceProblemMin.IExpectationSetters<capture#2-of ?> to TypeInferenceProblemMin.IExpectationSetters<Object>\n" +
			"    IExpectationSetters<Object> exp2 = expect(unboundedMockHeaderAccess.getHeader());\n" +
			"  }\n" +
			"}\n";
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		runNegativeTest(
			new String[] {
				"TypeInferenceProblemMin.java",
				sourceString
			},
			"----------\n" +
			"1. ERROR in TypeInferenceProblemMin.java (at line 21)\n" +
			"	IExpectationSetters<Object> exp2 = expect(unboundedMockHeaderAccess.getHeader());\n" +
			"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from TypeInferenceProblemMin.IExpectationSetters<capture#2-of ?> to TypeInferenceProblemMin.IExpectationSetters<Object>\n" +
			"----------\n");
	else
		// conform due to target typing
		runConformTest(
			new String[] {
				"TypeInferenceProblemMin.java",
				sourceString
			});
}
public void testBug434570() {
	runConformTest(
		new String[] {
			"example/Example.java",
			"package example;\n" +
			"\n" +
			"import example.Example.Config;\n" +
			"import example.Example.CustomInitializer;\n" +
			"\n" +
			"@Config(initializers = CustomInitializer.class)\n" +
			"public class Example {\n" +
			"\n" +
			"	static interface Context {\n" +
			"	}\n" +
			"\n" +
			"	static interface ConfigurableContext extends Context {\n" +
			"	}\n" +
			"\n" +
			"	static abstract class AbstractContext implements ConfigurableContext {\n" +
			"	}\n" +
			"\n" +
			"	static class GenericContext extends AbstractContext {\n" +
			"	}\n" +
			"\n" +
			"	static interface Initializer<C extends ConfigurableContext> {\n" +
			"	}\n" +
			"\n" +
			"	static @interface Config {\n" +
			"		Class<? extends Initializer<? extends ConfigurableContext>>[] initializers() default {};\n" +
			"	}\n" +
			"\n" +
			"	static class CustomInitializer implements Initializer<GenericContext> {\n" +
			"	}\n" +
			"\n" +
			"	@Config(initializers = CustomInitializer.class)\n" +
			"	static class CompilationSuccess {\n" +
			"	}\n" +
			"\n" +
			"}\n"
		});
}
public void testBug434630() {
	runConformTest(
		new String[] {
			"Foo.java",
			"interface Provider<T> {}\n" +
			"@interface ProvidedBy {\n" +
			"	Class<? extends Provider<?>> value();" +
			"}\n" +
			"\n" +
			"@ProvidedBy(Foo.SomeProvider.class)\n" +
			"public interface Foo {\n" +
			"	\n" +
			"	public static class SomeProvider implements Provider<Foo> {\n" +
			"\n" +
			"		public Foo get() {\n" +
			"			return null;\n" +
			"		}\n" +
			"		\n" +
			"	}\n" +
			"}\n"
		});
}
public void testBug434570_comment3() {
	runConformTest(
		new String[] {
			"TestWontCompile.java",
			"import org.bug.AnnotationWithClassParameter;\n" +
			"import org.bug.CustomHandler;\n" +
			"import org.bug.Handler;\n" +
			"\n" +
			"\n" +
			"@AnnotationWithClassParameter(CustomHandler.class)\n" +
			"public class TestWontCompile extends ATest<Object> {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		Class<? extends Handler<?>> h = CustomHandler.class;\n" +
			"	}\n" +
			"\n" +
			"}\n",
			"ATest.java",
			"public abstract class ATest<T> {\n" +
			"\n" +
			"}\n",
			"org/bug/Item.java",
			"package org.bug;\n" +
			"\n" +
			"public interface Item {\n" +
			"\n" +
			"}\n",
			"org/bug/CustomItem.java",
			"package org.bug;\n" +
			"\n" +
			"public class CustomItem implements Item {\n" +
			"\n" +
			"}\n",
			"org/bug/Handler.java",
			"package org.bug;\n" +
			"\n" +
			"public abstract class Handler<T extends Item> {\n" +
			"\n" +
			"}\n",
			"org/bug/CustomHandler.java",
			"package org.bug;\n" +
			"\n" +
			"public class CustomHandler extends Handler<CustomItem> {\n" +
			"\n" +
			"}\n",
			"org/bug/AnnotationWithClassParameter.java",
			"package org.bug;\n" +
			"\n" +
			"import java.lang.annotation.Documented;\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE)\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@Documented\n" +
			"public @interface AnnotationWithClassParameter {\n" +
			"	\n" +
			"	Class<? extends Handler<?>> value();\n" +
			"\n" +
			"}\n"
		});
}
// same test but with null annotations analysis enabled
public void testBug434570_comment3b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	runConformTest(
		new String[] {
			"TestWontCompile.java",
			"import org.bug.AnnotationWithClassParameter;\n" +
			"import org.bug.CustomHandler;\n" +
			"import org.bug.Handler;\n" +
			"\n" +
			"\n" +
			"@AnnotationWithClassParameter(CustomHandler.class)\n" +
			"public class TestWontCompile extends ATest<Object> {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		Class<? extends Handler<?>> h = CustomHandler.class;\n" +
			"	}\n" +
			"\n" +
			"}\n",
			"ATest.java",
			"public abstract class ATest<T> {\n" +
			"\n" +
			"}\n",
			"org/bug/Item.java",
			"package org.bug;\n" +
			"\n" +
			"public interface Item {\n" +
			"\n" +
			"}\n",
			"org/bug/CustomItem.java",
			"package org.bug;\n" +
			"\n" +
			"public class CustomItem implements Item {\n" +
			"\n" +
			"}\n",
			"org/bug/Handler.java",
			"package org.bug;\n" +
			"\n" +
			"public abstract class Handler<T extends Item> {\n" +
			"\n" +
			"}\n",
			"org/bug/CustomHandler.java",
			"package org.bug;\n" +
			"\n" +
			"public class CustomHandler extends Handler<CustomItem> {\n" +
			"\n" +
			"}\n",
			"org/bug/AnnotationWithClassParameter.java",
			"package org.bug;\n" +
			"\n" +
			"import java.lang.annotation.Documented;\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE)\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@Documented\n" +
			"public @interface AnnotationWithClassParameter {\n" +
			"	\n" +
			"	Class<? extends Handler<?>> value();\n" +
			"\n" +
			"}\n"
		},
		options);
}
public void testBug434630_comment7() {
	runConformTest(
		new String[] {
			"test/FooImpl.java",
			"package test;\n" +
			"\n" +
			"public class FooImpl implements Foo {\n" +
			"\n" +
			"}\n",
			"test/Foo.java",
			"package test;\n" +
			"interface Provider<T> {}\n" +
			"@interface ProvidedBy {\n" +
			"	Class<? extends Provider<?>> value();" +
			"}\n" +
			"\n" +
			"@ProvidedBy(Foo.SomeProvider.class)\n" +
			"public interface Foo {\n" +
			"	\n" +
			"	public static class SomeProvider implements Provider<Foo> {\n" +
			"\n" +
			"		public Foo get() {\n" +
			"			return null;\n" +
			"		}\n" +
			"		\n" +
			"	}\n" +
			"}\n"
		});
}
public void testBug434044() {
	runConformTest(
		new String[] {
			"EclipseJava8Generics.java",
			"public class EclipseJava8Generics {\n" +
			"\n" +
			"  public interface Foo<V> {\n" +
			"    public V doFoo();\n" +
			"  }\n" +
			"\n" +
			"  public static class FooBar<V, T extends Foo<V>> {\n" +
			"    public T getBar() {\n" +
			"      return null;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public static class Factory {\n" +
			"\n" +
			"    public static <V, T extends Foo<V>> FooBar<V, T> createFooBar() {\n" +
			"      return null;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public static void test() {\n" +
			"    final FooBar<?, ? extends Foo<?>> typedProperty = Factory.createFooBar();\n" +
			"    //TODO Eclipse Bug 434044\n" +
			"    final Object propertyValue = typedProperty.getBar().doFoo();\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"}\n"
		});
}
public void testBug434044_comment20() {
	runConformTest(
		new String[] {
			"EclipseJava8Generics.java",
			"public class EclipseJava8Generics {\n" +
			"\n" +
			"  public interface Foo<V> {\n" +
			"    public V doFoo();\n" +
			"  }\n" +
			"\n" +
			"  public static class FooBar<V, T extends Foo<V>> {\n" +
			"    public T getBar() {\n" +
			"      return null;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public static abstract class AbstractFoo<V> implements Foo<V> {\n" +
			"  }\n" +
			"\n" +
			"  public static class Factory {\n" +
			"    public static <V, T extends AbstractFoo<V>> FooBar<V, T> createFooBar() {\n" +
			"      return null;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public static void test() {\n" +
			"    final FooBar<?, ? extends AbstractFoo<?>> typedProperty = Factory.createFooBar();\n" +
			"    //TODO Eclipse Bug 434044 still exists\n" +
			"    final Object propertyValue = typedProperty.getBar().doFoo();\n" +
			"  }\n" +
			"\n" +
			"}\n"
		});
}
public void testBug434044_comment36() {
	runNegativeTest(
		new String[] {
			"EclipseJava8Generics.java",
			"public class EclipseJava8Generics {\n" +
			"\n" +
			"  public interface Nasty {\n" +
			"    public Object doFoo(Integer a);\n" +
			"  }\n" +
			"  public interface Foo<V> {\n" +
			"    public V doFoo(String a);\n" +
			"  }\n" +
			"\n" +
			"  public static class FooBar<V, T extends Foo<V>> {\n" +
			"    public T getBar() {\n" +
			"      return null;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public static abstract class AbstractFoo<V> implements Foo<V>, Nasty {\n" +
			"  }\n" +
			"\n" +
			"  public static class Factory {\n" +
			"    public static <V, T extends AbstractFoo<V>> FooBar<V, T> createFooBar() {\n" +
			"      return null;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public static void test() {\n" +
			"    final FooBar<?, ? extends AbstractFoo<?>> typedProperty = Factory.createFooBar();\n" +
			"    //TODO Eclipse Bug 434044 still exists\n" +
			"    final Object propertyValue = typedProperty.getBar().doFoo(null);\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in EclipseJava8Generics.java (at line 28)\n" +
		"	final Object propertyValue = typedProperty.getBar().doFoo(null);\n" +
		"	                                                    ^^^^^\n" +
		"The method doFoo(String) is ambiguous for the type capture#3-of ? extends EclipseJava8Generics.AbstractFoo<?>\n" +
		"----------\n");
}
public void testBug434793() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	runConformTest(
		new String[] {
			"Outer.java",
			"import java.util.*;\n" +
			"\n" +
			"public class Outer {\n" +
			"	private static class SingletonList<E>\n" +
			"				 extends AbstractList<E>\n" +
			"				 implements java.util.RandomAccess, java.io.Serializable {\n" +
			"		public E get(int i) { throw new RuntimeException(); }\n" +
			"		public int size() { return 0; }\n" +
			"	}\n" +
			"}\n"
		},
		options);
}
public void testBug435643() {
	runConformTest(
		new String[] {
			"test/Main.java",
			"package test;\n" +
			"abstract class ASTNode<T extends ASTNode> implements Iterable<T> {\n" +
			"	public T getChild(int i) { return null; }\n" +
			"} \n" +
			"abstract class List<T extends ASTNode> extends ASTNode<T> { }\n" +
			"class Joiner {\n" +
			"	  public static Joiner on(String separator) {\n" +
			"	    return null;\n" +
			"	  }\n" +
			"	  String join(Iterable<?> parts) {\n" +
			"		  return \"\";\n" +
			"	  }\n" +
			"}\n" +
			"class AstFunctions {\n" +
			"	  public static <T extends ASTNode<?>> Function<T, String> prettyPrint() {\n" +
			"		  return null;\n" +
			"	  }\n" +
			"}\n" +
			"class Iterables {\n" +
			"	public static <F, T> Iterable<T> transform(final Iterable<F> fromIterable,\n" +
			"		      final Function<? super F, ? extends T> function) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"interface Function<F, T> {\n" +
			"	 T apply(F input);\n" +
			"}\n" +
			"public class Main {\n" +
			"\n" +
			"	  String test(ASTNode<?> node, ASTNode rawNode) {\n" +
			"		rawNode.getChild(0); \n" +
			"	    \n" +
			"        @SuppressWarnings(\"unchecked\")\n" +
			"        List<? extends ASTNode<?>> list = (List<? extends ASTNode<?>>) node;\n" +
			"        return Joiner.on(\", \").join(Iterables.transform(list, AstFunctions.prettyPrint()));\n" +
			"	  }\n" +
			"\n" +
			"}\n"
		});
}
public void testBug438337comment5() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	void test() {\n" +
			"		I18nResource<?> i18nResource = null;\n" +
			"	}\n" +
			"}\n",
			"I18nResource.java",
			"public interface I18nResource<E extends Internationalized<? extends I18nResource<E>>> extends BusinessObject {}\n",
			"Internationalized.java",
			"public interface Internationalized<E extends I18nResource<? extends Internationalized<E>>>\n" +
			"    extends BusinessObject {}\n",
			"BusinessObject.java",
			"public interface BusinessObject {}\n"
		});
}
public void testBug438337comment3() {
	runConformTest(
		new String[] {
			"PermissionDrivenPresenter.java",
			"public abstract class PermissionDrivenPresenter<V extends BaseView<? extends Presenter<V>>> extends BasePresenter<V> {\n" +
			"\n" +
			"    public void updatePermissions() {\n" +
			"        getView().setReadOnly(true);\n" +
			"    }\n" +
			"}\n",
			"View.java",
			"public interface View<P extends Presenter<? extends View<P>>> { }\n",
			"Presenter.java",
			"public interface Presenter<V extends View<? extends Presenter<V>>> { }\n",
			"BaseView.java",
			"public abstract class BaseView<P extends Presenter<? extends View<P>>> implements View<P> {\n" +
			"	void setReadOnly(boolean f) {}\n" +
			"}\n",
			"BasePresenter.java",
			"public abstract class BasePresenter<V extends View<? extends Presenter<V>>> implements Presenter<V> {\n" +
			"    public V getView() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422832, Class file triggers StackOverflowError when creating type hierarchy
public void testBug422832() {
	String path = getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator +
			"Bug422832ClassFile" + File.separator + "aspose.pdf.jar";
	String[] libs = getDefaultClassPaths();
	int len = libs.length;
	System.arraycopy(libs, 0, libs = new String[len+1], 0, len);
	libs[len] = path;
	runNegativeTest(
			new String[] {
					"ExampleClass.java",
					"public class ExampleClass extends aspose.b.a.a {}\n",
			},
			"----------\n" +
			"1. ERROR in ExampleClass.java (at line 1)\n" +
			"	public class ExampleClass extends aspose.b.a.a {}\n" +
			"	             ^^^^^^^^^^^^\n" +
			"The hierarchy of the type ExampleClass is inconsistent\n" +
			"----------\n",
			libs, false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=416480, Error in bytecode generated by ECJ compiler leads to IncompatibleClassChangeError
public void test416480() {
  this.runConformTest(
      new String[] {
          "X.java",
          "public class X<T extends Object & Runnable> {\n" +
		  "    private Runnable cast(T obj) {\n" +
		  "        return obj;\n" +
		  "    }\n" +
		  "    public static void main(String[] args) {\n" +
		  "        try {\n" +
		  "            Runnable runnable = new X().cast(new Object());\n" +
		  "        } catch (ClassCastException c) {\n" +
		  "            System.out.println(\"CCE\");\n" +
		  "        }\n" +
		  "    }\n" +
		  "}\n"
      },
      "CCE");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444024, Type mismatch error in annotation generics assignment which happens "sometimes"
public void test444024() {
		this.runConformTest(
		   new String[] {
			   "ViewpointOrganisationEntity.java",
			   "abstract public class ViewpointOrganisationEntity<T> {\n" +
			   "}\n",
			   "MetaCombo.java",
			   "public @interface MetaCombo {\n" +
			   "	Class< ? extends IComboDataSet< ? >> dataSet();\n" +
			   "}\n",
			   "IComboDataSet.java",
			   "public interface IComboDataSet<T> {\n" +
			   "}\n",
			   "ContractantTypeLister.java",
			   "public class ContractantTypeLister implements IComboDataSet<ContractantType> {\n" +
			   "}\n",
			   "ContractantType.java",
			   "@MetaCombo(dataSet = ContractantTypeLister.class)\n" +
			   "public class ContractantType extends ViewpointOrganisationEntity<Long>  {\n" +
			   "}\n",
		       "Contractant.java",
		       "public class Contractant extends ViewpointOrganisationEntity<Long> {\n" +
			   "	@MetaCombo(dataSet = ContractantTypeLister.class)\n" +
			   "	public ContractantType getContractantType() {\n" +
			   "		return null;\n" +
			   "	}\n" +
			   "}\n",
		   },
		   "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440019, [1.8][compiler] Type mismatch error with autoboxing/scalar types (works with 1.6)
public void test440019() {
		this.runConformTest(
		   new String[] {
			   "A.java",
			   "public class A {\n" +
			   "  <T> T get(String name, T defaultValue) { return defaultValue; }\n" +
			   "  void x() {\n" +
			   "    long a1 = get(\"key\", 0);\n" +
			   "    long a3 = get(\"key\", 0L);\n" +
			   "  }\n" +
			   "}\n",
		   },
		   "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443596, [1.8][compiler] Failure for overload resolution in case of Generics and Varags
public void test443596() {
	this.runNegativeTest(
		   new String[] {
			   "X.java",
			   "public final class X {\n" +
			   "    static interface Predicate<T> { boolean test(T object); }\n" +
			   "    public static <T> Predicate<T> in(Predicate<? extends T> arg) { return null; }\n" +
			   "    public static <T> Predicate<T> and(Predicate<? super T>... arg) { return null; }\n" +
			   "    public static <T> Predicate<T> and(Predicate<? super T> arg0, Predicate<? super T> arg1) { return null; }\n" +
			   "    static class FilteredCollection<E> {\n" +
			   "        Predicate<? super E> predicate;\n" +
			   "        public void error(Predicate<?> arg) { and(predicate, in(arg)); } // no compile\n" +
			   "    }\n" +
			   "}\n",
		   },
		   this.complianceLevel < ClassFileConstants.JDK1_7 ?
		   "" :
		   "----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	public static <T> Predicate<T> and(Predicate<? super T>... arg) { return null; }\n" +
			"	                                                           ^^^\n" +
			"Type safety: Potential heap pollution via varargs parameter arg\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446235, Java8 generics and boxing
public void test446235() {
		this.runConformTest(
		   new String[] {
			   "IntegerLongBug.java",
			   "public class IntegerLongBug {\n" +
			   "	public static void main(String ar[]) {\n" +
			   "		Integer number = 1000;\n" +
			   "		long numberLong = number; //compiles fine\n" +
			   "		long num = getNumber(5000); // compilation error\n" +
			   "	}\n" +
			   "	public static <T> T getNumber(T num) {\n" +
			   "		return num;\n" +
			   "	}\n" +
			   "}\n",
		   },
		   "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440019, [1.8][compiler] Type mismatch error with autoboxing/scalar types (works with 1.6)
public void test440019_c9() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   "public class X {\n" +
			   "    public static final int CORE_POOL_SIZE = 3;\n" +
			   "    public static final int KEEP_ALIVE_TIME = 60; // seconds\n" +
			   "    X(final int size, final long ttl){\n" +
			   "        System.out.println(\"size: \" + size + \" \" + \" ttl: \" + ttl);\n" +
			   "    }\n" +
			   "    public static void main(String[] args) {\n" +
			   "        new X(CORE_POOL_SIZE, get(KEEP_ALIVE_TIME)); // [1]\n" +
			   "    }\n" +
			   "    public static <T> T get(T value) {\n" +
			   "        return value;\n" +
			   "    }\n" +
			   "}\n",
		   },
		   "size: 3  ttl: 60");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446223, [1.8][compiler] Java8 generics eclipse doesn't compile
public void test446223() {
		this.runNegativeTest(
		   false /* skipJavac */,
		   JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		   new String[] {
			   "X.java",
			   "public class X {\n" +
			   "	public static void main(String ar[]) {\n" +
			   "		System.out.println(\"hi\");\n" +
			   "		DoNothing();\n" +
			   "	}\n" +
			   "	public interface Interface1 {\n" +
			   "		public void go();\n" +
			   "	}\n" +
			   "	public interface Interface2<X> {\n" +
			   "		public X go2();\n" +
			   "	}\n" +
			   "	private static <X, T extends Interface2<X> & Interface1> void DoNothing() {\n" +
			   "		return;\n" +
			   "	}\n" +
			   "}\n",
		   },
		   "----------\n" +
			"1. WARNING in X.java (at line 9)\n" +
			"	public interface Interface2<X> {\n" +
			"	                            ^\n" +
			"The type parameter X is hiding the type X\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 12)\n" +
			"	private static <X, T extends Interface2<X> & Interface1> void DoNothing() {\n" +
			"	                ^\n" +
			"The type parameter X is hiding the type X\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444334,  [1.8][compiler] Compiler generates error instead of warning on unchecked conversion
public void test444334() {
		this.runNegativeTest(
		   new String[] {
			   "X.java",
			   "import java.util.ArrayList;\n" +
			   "import java.util.List;\n" +
			   "public class X {\n" +
			   "    public static void Main(String[] args) {\n" +
			   "        doSomething(returnClassType(Class.class));\n" +
			   "        doSomething(returnListType(new ArrayList<List>()));\n" +
			   "    }\n" +
			   "    public static <T> void doSomething(Class<T> clazz) {\n" +
			   "        System.out.println(clazz.getSimpleName());\n" +
			   "    }\n" +
			   "    public static <T> T returnClassType(Class<T> clazz) {\n" +
			   "        return null;\n" +
			   "    }\n" +
			   "    public static <T> void doSomething(List<T> list) {\n" +
			   "        System.out.println(list.getClass().getSimpleName());\n" +
			   "    }\n" +
			   "    public static <T> T returnListType(List<T> list) {\n" +
			   "        return null;\n" +
			   "    }\n" +
			   "}\n",
		   },
		   "----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	doSomething(returnClassType(Class.class));\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked invocation doSomething(Class) of the generic method doSomething(Class<T>) of type X\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	doSomething(returnClassType(Class.class));\n" +
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type Class needs unchecked conversion to conform to Class<Object>\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 6)\n" +
			"	doSomething(returnListType(new ArrayList<List>()));\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked invocation doSomething(List) of the generic method doSomething(List<T>) of type X\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 6)\n" +
			"	doSomething(returnListType(new ArrayList<List>()));\n" +
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type List needs unchecked conversion to conform to List<Object>\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 6)\n" +
			"	doSomething(returnListType(new ArrayList<List>()));\n" +
			"	                                         ^^^^\n" +
			"List is a raw type. References to generic type List<E> should be parameterized\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438246, [1.8][compiler] Java 8 static methods compilation error
public void test438246() {
		this.runNegativeTest(
		   new String[] {
			   "Foo.java",
			   "import java.util.List;\n" +
			   "public abstract class Foo<C>\n" +
			   "{\n" +
			   "  @SuppressWarnings(\"unchecked\")\n" +
			   "  public static <C> void doit( List<Foo<C>> workers )\n" +
			   "  {\n" +
			   "    doit(  workers.toArray( new Foo[workers.size()] ) );\n" +
			   "  }\n" +
			   "  public static <C> void doit( Foo<C>... workers )\n" +
			   "  {\n" +
			   "  }\n" +
			   "}\n",
		   },
		   this.complianceLevel < ClassFileConstants.JDK1_7 ?
		   "" :
		   "----------\n" +
			"1. WARNING in Foo.java (at line 9)\n" +
			"	public static <C> void doit( Foo<C>... workers )\n" +
			"	                                       ^^^^^^^\n" +
			"Type safety: Potential heap pollution via varargs parameter workers\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448795, [1.8][compiler] Inference should discriminate between strict and loose modes
public void test448795() {
		this.runNegativeTest(
		   new String[] {
			   "X.java",
			   "public class X<T> {\n" +
			   "	static <T> T element(T [] ta) {\n" +
			   "		return ta[0];\n" +
			   "	}\n" +
			   "	public static void main(String[] args) {\n" +
			   "		int x = element(new int [] { 1234 });\n" +  // check that autoboxing does not kick in for arrays, i.e engine should not slip into loose mode.
			   "	}\n" +
			   "}\n",
		   },
		   "----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	int x = element(new int [] { 1234 });\n" +
			"	        ^^^^^^^\n" +
			"The method element(T[]) in the type X<T> is not applicable for the arguments (int[])\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448795, [1.8][compiler] Inference should discriminate between strict and loose modes
public void test448795a() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   "public class X<T> {\n" +
			   "	static <T> T element(int x, T t) {\n" +
			   "		System.out.println(\"Strict\");\n" +
			   "		return t;\n" +
			   "	}\n" +
			   "	static <T> T element(T t1, T t2) {\n" +
			   "		System.out.println(\"Loose\");\n" +
			   "		return t2;\n" +
			   "	}\n" +
			   "	public static void main(String[] args) {\n" +
			   "		int x = element(10, new Integer(20));\n" +
			   "	}\n" +
			   "}\n",
		   },
		   "Strict");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448795, [1.8][compiler] Inference should discriminate between strict and loose modes
public void test448795b() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   "public class X<T> {\n" +
			   "	static int element(int x, Integer t) {\n" +
			   "		System.out.println(\"non-generic\");\n" +
			   "		return t;\n" +
			   "	}\n" +
			   "	static <T> T element(int t1, T t2) {\n" +
			   "		System.out.println(\"generic\");\n" +
			   "		return t2;\n" +
			   "	}\n" +
			   "	public static void main(String[] args) {\n" +
			   "		int x = element(10, new Integer(20));\n" +
			   "	}\n" +
			   "}\n",
		   },
		   "non-generic");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448795, [1.8][compiler] Inference should discriminate between strict and loose modes
public void test448795c() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   "public class X<T> {\n" +
			   "	static int element(Integer x, Integer t) {\n" +
			   "		System.out.println(\"non-generic\");\n" +
			   "		return t;\n" +
			   "	}\n" +
			   "	static <T> T element(int t1, T t2) {\n" +
			   "		System.out.println(\"generic\");\n" +
			   "		return t2;\n" +
			   "	}\n" +
			   "	public static void main(String[] args) {\n" +
			   "		int x = element(10, new Integer(20));\n" +
			   "	}\n" +
			   "}\n",
		   },
		   "generic");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434118, [1.8][compiler] Compilation error on generic capture/type inference
public void test434118() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   "public class X {\n" +
			   "    public Object convertFails(Class<?> clazz, String value) {\n" +
			   "        return Enum.valueOf(clazz.asSubclass(Enum.class), value);\n" +
			   "    }\n" +
			   "    public Object convertCompiles(Class<?> clazz, String value) {\n" +
			   "        return Enum.valueOf(clazz.<Enum>asSubclass(Enum.class), value);\n" +
			   "    }\n" +
			   "    public Object convertCompiles2(Class<?> clazz, String value) {\n" +
			   "        Class<? extends Enum> enumType = clazz.asSubclass(Enum.class);\n" +
			   "        return Enum.valueOf(enumType, value);\n" +
			   "    }\n" +
			   "}\n",
		   },
		   "");
}
public void testBug452194() {
	runNegativeTest(
		new String[]{
			"test/Main.java",
			"package test;\n" +
			"\n" +
			"import java.util.Map.Entry;\n" +
			"\n" +
			"public class Main {\n" +
			"	public static void main(String[] args) {\n" +
			"		EcoreEMap map = new EcoreEMap();\n" +
			"		map.addUnique(new Object()); //Error here ONLY in 4.4\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"interface InternalEList<E> {\n" +
			"	public void addUnique(E object);\n" +
			"}\n" +
			"\n" +
			"class EcoreEMap<K, V> implements InternalEList<Entry<K, V>> {\n" +
			"	public void addUnique(Entry<K, V> object) {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in test\\Main.java (at line 7)\n" +
		"	EcoreEMap map = new EcoreEMap();\n" +
		"	^^^^^^^^^\n" +
		"EcoreEMap is a raw type. References to generic type EcoreEMap<K,V> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in test\\Main.java (at line 7)\n" +
		"	EcoreEMap map = new EcoreEMap();\n" +
		"	                    ^^^^^^^^^\n" +
		"EcoreEMap is a raw type. References to generic type EcoreEMap<K,V> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in test\\Main.java (at line 8)\n" +
		"	map.addUnique(new Object()); //Error here ONLY in 4.4\n" +
		"	    ^^^^^^^^^\n" +
		"The method addUnique(Map.Entry) in the type EcoreEMap is not applicable for the arguments (Object)\n" +
		"----------\n");
}
public void testBug454644() {
	Map<String,String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runNegativeTest(
		new String[] {
			"example/CollectionFactory.java",
			"/*\n" +
			" * Copyright 2002-2014 the original author or authors.\n" +
			" *\n" +
			" * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
			" * you may not use this file except in compliance with the License.\n" +
			" * You may obtain a copy of the License at\n" +
			" *\n" +
			" *      http://www.apache.org/licenses/LICENSE-2.0\n" +
			" *\n" +
			" * Unless required by applicable law or agreed to in writing, software\n" +
			" * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
			" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
			" * See the License for the specific language governing permissions and\n" +
			" * limitations under the License.\n" +
			" */\n" +
			"\n" +
			"package example;\n" +
			"\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.Collection;\n" +
			"import java.util.EnumSet;\n" +
			"import java.util.LinkedHashSet;\n" +
			"import java.util.LinkedList;\n" +
			"import java.util.List;\n" +
			"import java.util.NavigableSet;\n" +
			"import java.util.Set;\n" +
			"import java.util.SortedSet;\n" +
			"import java.util.TreeSet;\n" +
			"\n" +
			"/**\n" +
			" * The code in this class is taken directly from the Spring Framework for the purpose\n" +
			" * of reproducing a bug.\n" +
			" * <p>\n" +
			" * Specifically, the code comes from {@code org.springframework.core.CollectionFactory}.\n" +
			" *\n" +
			" * @author Juergen Hoeller\n" +
			" * @author Arjen Poutsma\n" +
			" * @author Sam Brannen\n" +
			" */\n" +
			"public abstract class CollectionFactory {\n" +
			"\n" +
			"	@SuppressWarnings({ \"unchecked\", \"cast\" })\n" +
			"	public static <E> Collection<E> createApproximateCollection(Object collection, int capacity) {\n" +
			"		if (collection instanceof LinkedList) {\n" +
			"			return new LinkedList<E>();\n" +
			"		}\n" +
			"		else if (collection instanceof List) {\n" +
			"			return new ArrayList<E>(capacity);\n" +
			"		}\n" +
			"		else if (collection instanceof EnumSet) {\n" +
			"			// superfluous cast necessary for bug in Eclipse 4.4.1.\n" +
			"			// return (Collection<E>) EnumSet.copyOf((EnumSet) collection);\n" +
			"\n" +
			"			// Original code which compiles using OpenJDK 1.8.0_40-ea-b11 and IntelliJ IDEA\n" +
			"			return EnumSet.copyOf((EnumSet) collection);\n" +
			"		}\n" +
			"		else if (collection instanceof SortedSet) {\n" +
			"			return new TreeSet<E>(((SortedSet<E>) collection).comparator());\n" +
			"		}\n" +
			"		else {\n" +
			"			return new LinkedHashSet<E>(capacity);\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static <E> Collection<E> createCollection(Class<?> collectionClass, Class<?> elementType, int capacity) {\n" +
			"		if (collectionClass.isInterface()) {\n" +
			"			if (Set.class.equals(collectionClass) || Collection.class.equals(collectionClass)) {\n" +
			"				return new LinkedHashSet<E>(capacity);\n" +
			"			}\n" +
			"			else if (List.class.equals(collectionClass)) {\n" +
			"				return new ArrayList<E>(capacity);\n" +
			"			}\n" +
			"			else if (SortedSet.class.equals(collectionClass) || NavigableSet.class.equals(collectionClass)) {\n" +
			"				return new TreeSet<E>();\n" +
			"			}\n" +
			"			else {\n" +
			"				throw new IllegalArgumentException(\"Unsupported Collection interface: \" + collectionClass.getName());\n" +
			"			}\n" +
			"		}\n" +
			"		else if (EnumSet.class.equals(collectionClass)) {\n" +
			"			// Assert.notNull(elementType, \"Cannot create EnumSet for unknown element type\");\n" +
			"\n" +
			"			// superfluous cast necessary for bug in Eclipse 4.4.1.\n" +
			"			// return (Collection<E>) EnumSet.noneOf((Class) elementType);\n" +
			"\n" +
			"			// Original code which compiles using OpenJDK 1.8.0_40-ea-b11 and IntelliJ IDEA\n" +
			"			return EnumSet.noneOf((Class) elementType);\n" +
			"		}\n" +
			"		else {\n" +
			"			if (!Collection.class.isAssignableFrom(collectionClass)) {\n" +
			"				throw new IllegalArgumentException(\"Unsupported Collection type: \" + collectionClass.getName());\n" +
			"			}\n" +
			"			try {\n" +
			"				return (Collection<E>) collectionClass.newInstance();\n" +
			"			}\n" +
			"			catch (Exception ex) {\n" +
			"				throw new IllegalArgumentException(\n" +
			"						\"Could not instantiate Collection type: \" + collectionClass.getName(), ex);\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in example\\CollectionFactory.java (at line 42)\n" +
		"	@SuppressWarnings({ \"unchecked\", \"cast\" })\n" +
		"	                                 ^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"cast\")\n" +
		"----------\n" +
		"2. WARNING in example\\CollectionFactory.java (at line 55)\n" +
		"	return EnumSet.copyOf((EnumSet) collection);\n" +
		"	                       ^^^^^^^\n" +
		"EnumSet is a raw type. References to generic type EnumSet<E> should be parameterized\n" +
		"----------\n" +
		"3. WARNING in example\\CollectionFactory.java (at line 87)\n" +
		"	return EnumSet.noneOf((Class) elementType);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked invocation noneOf(Class) of the generic method noneOf(Class<E>) of type EnumSet\n" +
		"----------\n" +
		"4. WARNING in example\\CollectionFactory.java (at line 87)\n" +
		"	return EnumSet.noneOf((Class) elementType);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The expression of type EnumSet needs unchecked conversion to conform to Collection<E>\n" +
		"----------\n" +
		"5. WARNING in example\\CollectionFactory.java (at line 87)\n" +
		"	return EnumSet.noneOf((Class) elementType);\n" +
		"	                      ^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		? "Type safety: The expression of type Class needs unchecked conversion to conform to Class<E>\n"
		: "Type safety: The expression of type Class needs unchecked conversion to conform to Class<Enum<Enum<E>>>\n") +
		"----------\n" +
		"6. WARNING in example\\CollectionFactory.java (at line 87)\n" +
		"	return EnumSet.noneOf((Class) elementType);\n" +
		"	                       ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n" +
		"7. WARNING in example\\CollectionFactory.java (at line 94)\n" +
		"	return (Collection<E>) collectionClass.newInstance();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked cast from capture#13-of ? to Collection<E>\n" +
		"----------\n",
		null, true, options);
}
// original test case, documenting existing compiler behavior
public void testBug456459a() {
	runNegativeTest(
		new String[] {
			"EnumTest.java",
			"import java.util.EnumSet;\n" +
			"public class EnumTest {\n" +
			"	\n" +
			"		static enum Cloneables implements Cloneable {\n" +
			"			One, Two, Three;\n" +
			"		}\n" +
			"	\n" +
			"		public <T extends Cloneable> T getOne(Class enumType) {\n" +
			"			EnumSet<? extends T> set = EnumSet.allOf(enumType);\n" +
			"			return set.iterator().next();\n" +
			"		}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in EnumTest.java (at line 8)\n" +
		"	public <T extends Cloneable> T getOne(Class enumType) {\n" +
		"	                                      ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in EnumTest.java (at line 9)\n" +
		"	EnumSet<? extends T> set = EnumSet.allOf(enumType);\n" +
		"	        ^^^^^^^^^^^\n" +
		"Bound mismatch: The type ? extends T is not a valid substitute for the bounded parameter <E extends Enum<E>> of the type EnumSet<E>\n" +
		"----------\n" +
		"3. WARNING in EnumTest.java (at line 9)\n" +
		"	EnumSet<? extends T> set = EnumSet.allOf(enumType);\n" +
		"	                           ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked invocation allOf(Class) of the generic method allOf(Class<E>) of type EnumSet\n" +
		"----------\n" +
		"4. WARNING in EnumTest.java (at line 9)\n" +
		"	EnumSet<? extends T> set = EnumSet.allOf(enumType);\n" +
		"	                           ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The expression of type EnumSet needs unchecked conversion to conform to EnumSet<? extends T>\n" +
		"----------\n" +
		"5. WARNING in EnumTest.java (at line 9)\n" +
		"	EnumSet<? extends T> set = EnumSet.allOf(enumType);\n" +
		"	                                         ^^^^^^^^\n" +
		"Type safety: The expression of type Class needs unchecked conversion to conform to Class<Enum<Enum<E>>>\n" +
		"----------\n");
}
// simple conflict introduced by additional wildcard bound
public void testBug456459b() {
	runNegativeTest(
		new String[] {
			"EnumTest.java",
			"import java.util.EnumSet;\n" +
			"public class EnumTest {\n" +
			"	\n" +
			"		static enum Cloneables implements Cloneable {\n" +
			"			One, Two, Three;\n" +
			"		}\n" +
			"	\n" +
			"		public void getOne(Class enumType) {\n" +
			"			EnumSet<? extends EnumTest> set = null;\n" +
			"		}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in EnumTest.java (at line 8)\n" +
		"	public void getOne(Class enumType) {\n" +
		"	                   ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in EnumTest.java (at line 9)\n" +
		"	EnumSet<? extends EnumTest> set = null;\n" +
		"	        ^^^^^^^^^^^^^^^^^^\n" +
		"Bound mismatch: The type ? extends EnumTest is not a valid substitute for the bounded parameter <E extends Enum<E>> of the type EnumSet<E>\n" +
		"----------\n");
}
// indirect conflict via wildcard's bound's bound.
public void testBug456459c() {
	runNegativeTest(
		new String[] {
			"X.java",
			"class A {};\n" +
			"class B {};\n" +
			"public class X<T extends A> {\n" +
			"	<U extends B> void m() {\n" +
			"		X<? extends U> l = null;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	X<? extends U> l = null;\n" +
		"	  ^^^^^^^^^^^\n" +
		"Bound mismatch: The type ? extends U is not a valid substitute for the bounded parameter <T extends A> of the type X<T>\n" +
		"----------\n");
}
public void testBug456924() {
	runConformTest(
		new String[] {
			"Test1.java",
			"class Test1<E> {\n" +
			"	<T extends Test1<T>> void method1(Class<T> t) {\n" +
			"		Test2<? super T> test2 = getTest2(t);\n" +
			"                // getTest2(t); // --> no error\n" +
			"	}\n" +
			"	<T extends Test1<T>> Test2<? super T> getTest2(Class<T> t){\n" +
			"		return null;\n" +
			"	}\n" +
			"	\n" +
			"}\n" +
			"\n",
			"Test2.java",
			"class Test2<E extends Test1<E>>{}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425203, [compiler][1.7][inference] ECJ rejects valid code using bounded wildcards
public void test425203() {
	String source =
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"interface MyFunction<Input, Output> {\n" +
			"	Output apply(Input input);\n" +
			"}\n" +
			"public class Test {\n" +
			"	public <Input, Output> List<Output> wrap(MyFunction<? super Input, ? extends Output> function, Input input) {\n" +
			"		return Arrays.asList(function.apply(input));\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Done\");\n" +
			"	}\n" +
			"}";
	if (this.complianceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				source,
			},
			"----------\n" +
			"1. WARNING in Test.java (at line 8)\n" +
			"	return Arrays.asList(function.apply(input));\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: A generic array of capture#2-of ? extends Output is created for a varargs parameter\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 8)\n" +
			"	return Arrays.asList(function.apply(input));\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from List<capture#2-of ? extends Output> to List<Output>\n" +
			"----------\n");
	} else {
		runConformTest(new String[]{ "Test.java", source }, "Done");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=489636, [1.8] "Bound mismatch" for a generic method parameter
public void test489636() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"interface I<T> {\n" +
			"	I<T> get();\n" +
			"}\n" +
			"public class Test {\n" +
			"\n" +
			"	@SuppressWarnings(\"unused\")\n" +
			"	private static <T, S extends I<T>> void test(S p) {\n" +
			"		I<T> i = p.get();\n" +
			"		test(i);      // Bound mismatch\n" +
			"	}\n" +
			"}"
	});
}

public void testBug498057() {
	runConformTest(
		new String[] {
			"scanner/AbstractScanner.java",
			"package scanner;\n" +
			"import scanner.AbstractScanner.ScannerModel;\n" +
			"public abstract class AbstractScanner<E, M extends ScannerModel<E>> implements Scanner<E> {\n" +
			"	public void scan(ScanListener<E> listener) {\n" +
			"	}\n" +
			"\n" +
			"	public static interface ScannerModel<E> extends ScanListener<E> {\n" +
			"	}\n" +
			"}\n" +
			"",
			"scanner/AbstractSubScanner.java",
			"package scanner;\n" +
			"import scanner.AbstractScanner.ScannerModel;\n" +
			"public abstract class AbstractSubScanner<E> extends AbstractScanner<E, ScannerModel<E>> {\n" +
			"	@Override\n" +
			"	public void scan(ScanListener<E> listener) {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"",
			"scanner/ScanListener.java",
			"package scanner;\n" +
			"public interface ScanListener<E> {\n" +
			"}\n" +
			"\n" +
			"",
			"scanner/Scanner.java",
			"package scanner;\n" +
			"\n" +
			"public interface Scanner<E> {\n" +
			"	void scan(ScanListener<E> listener);\n" +
			"}\n" +
			"",
			"scanner/StringScanner.java",
			"package scanner;\n" +
			"\n" +
			"public interface StringScanner extends Scanner<String> {\n" +
			"}\n" +
			"",
		}
	);
	runConformTest(
		false,
		new String[] {
			"scanner/ModifyMe.java",
			"package scanner;\n" +
			"\n" +
			"public class ModifyMe extends AbstractSubScanner<String> implements StringScanner {\n" +
			"}\n" +
			"",
		},
		"",
		"",
		"",
		null
	);
}
public void testBug460491_comment23() {
	runConformTest(
		new String[] {
			"PM.java",
			"public class PM<E extends Enum<E>> {\n" +
			"	public PM(Class<E> clazz) {\n" +
			"	}\n" +
			"\n" +
			"	enum MyEnum {\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		new PM<MyEnum>(MyEnum.class);\n" +
			"	}\n" +
			"}\n"
		});
}
public void testBug498486() {
	runConformTest(
			new String[] {
				"t/Ab.java",
				"package t;\n" +
				"public interface Ab<T, O extends Ob<? extends Ob.Id, ?>> {}\n" +
				"",
				"t/At.java",
				"package t;\n" +
				"public interface At<I extends Ob.Id & Comparable<I>, O extends Ob<I, O>> {\n" +
				"}\n" +
				"",
				"t/Ob.java",
				"package t;\n" +
				"public interface Ob<I extends Ob.Id & Comparable<I>, O extends Ob<I, O>> extends At<I, O> {\n" +
				"  interface Id {}\n" +
				"}\n" +
				"",
			}
		);
	runConformTest(
			false,
			new String[] {
				"i/Test.java",
				"package i;\n" +
				"\n" +
				"import t.Ab;\n" +
				"import t.Ob;\n" +
				"\n" +
				"\n" +
				"public class Test {\n" +
				"	<T, I extends Ob.Id & Comparable<I>, O extends Ob<I, O>, A extends Ab<T, O>> A // Erroneous compiler error here on the last O\n" +
				"			m() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			null,
			null,
			null,
			null
		);
}
public void testBug499048() {
	runConformTest(
		new String[] {
			"p/Outer.java",
			"package p;\n" +
			"public class Outer<S> {\n" +
			"	private static class Inner<T> {}\n" +
			"	Inner<S> test() {\n" +
			"		Outer.Inner<S> inner = new Outer.Inner<S>();\n" +
			"		return inner;\n" +
			"	}\n" +
			"}\n"
		});
}
public void testBug499126() {
	runConformTest(
		new String[] {
			"bug_ise_immutablelist/$Immutable.java",
			"package bug_ise_immutablelist;\n" +
			"\n" +
			"public class $Immutable<T> {\n" +
			"}\n" +
			"",
			"bug_ise_immutablelist/Test.java",
			"package bug_ise_immutablelist;\n" +
			"\n" +
			"public class Test {\n" +
			"	public static $Immutable<Object> f;\n" +
			"}\n" +
			"",
		}
	);
	runConformTest(
			false,
			new String[] {
				"Usage.java",
				"public class Usage {\n" +
				"	Object f() {return bug_ise_immutablelist.Test.f;}\n" +
				"}\n" +
				"",
			},
			null,
			null,
			null,
			null
	);
}
public void testBug441905() {
	runConformTest(
		new String[] {
			"EclipseJava8Generics.java",
			"import java.util.List;\n" +
			"\n" +
			"public class EclipseJava8Generics {\n" +
			"\n" +
			"  public interface Foo<V> {\n" +
			"  }\n" +
			"\n" +
			"  public static class FooBar<V, T extends Foo<V>> {\n" +
			"  }\n" +
			"\n" +
			"  protected void doFoos(List<FooBar<?, ? extends Foo<?>>> fooBars) {\n" +
			"    FooBar<?, ? extends Foo<?>> fooBar = fooBars.iterator().next();\n" +
			"    doFoo(fooBar);\n" +
			"  }\n" +
			"\n" +
			"  protected static <F> void doFoo(FooBar<F, ? extends Foo<F>> fooBar) {\n" +
			"  }\n" +
			"\n" +
			"}\n"
		});
}
public void testBug469297() {
	String source = "    import java.util.List;\n" +
			"    \n" +
			"    public class Test {\n" +
			"    \n" +
			"        static final void a(Class<? extends List<?>> type) {\n" +
			"            b(newList(type));\n" +
			"        }\n" +
			"    \n" +
			"        static final <T> List<T> b(List<T> list) {\n" +
			"            return list;\n" +
			"        }\n" +
			"    \n" +
			" @SuppressWarnings(\"deprecation\")\n" +
			"        static final <L extends List<?>> L newList(Class<L> type) {\n" +
			"            try {\n" +
			"                return type.newInstance();\n" +
			"            }\n" +
			"            catch (Exception e) {\n" +
			"                throw new RuntimeException(e);\n" +
			"            }\n" +
			"        }\n" +
			"    }\n";
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runConformTest(new String[] { "Test.java", source });
	} else {
		runNegativeTest(new String[] { "Test.java", source },
			"----------\n" +
			"1. ERROR in Test.java (at line 6)\n" +
			"	b(newList(type));\n" +
			"	^\n" +
			"The method b(List<T>) in the type Test is not applicable for the arguments (capture#1-of ? extends List<?>)\n" +
			"----------\n");
	}
}
public void testBug508799() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
	runConformTest(
		new String[] {
			"test/A.java",
			"package test;\n" +
			"\n" +
			"public interface A<T extends Iterable> {\n" +
			"    void a();\n" +
			"}\n" +
			"",
			"test/B1.java",
			"package test;\n" +
			"\n" +
			"public interface B1 extends A<Iterable> {\n" +
			"    void b1();\n" +
			"}\n" +
			"",
			"test/B2.java",
			"package test;\n" +
			"\n" +
			"public interface B2 extends A<Iterable> {\n" +
			"    void b2();\n" +
			"}\n" +
			"",
		},
		customOptions
	);
	runConformTest(false,
		new String[] {
			"test/C.java",
			"package test;\n" +
			"\n" +
			"public class C implements B1, B2 {\n" +
			"    public void a() {\n" +
			"    }\n" +
			"\n" +
			"    public void b1() {\n" +
			"    }\n" +
			"\n" +
			"    public void b2() {\n" +
			"    }\n" +
			"}\n" +
			"",
		}, null, customOptions, "", "", "", null
	);
}
public void testBug515614() {
	runConformTest(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"abstract class Generic<E> {\n" +
			"	interface NestedInterface {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"abstract class X<V> {\n" +
			"	public static <W> X<W> create(Class<W> vclass) {\n" +
			"		return vclass == null ? null : null;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"	X<Generic.NestedInterface[]> x = X.create(Generic.NestedInterface[].class);\n" +
			"}\n" +
			"",
		}
	);
}
public void testBug518157A() {
	runConformTest(
		new String[] {
			"RawClassParameterizationBug.java",
			"class RawClassParameterizationBug<Oops> {\n" +
			"\n" +
			"    public interface Example<K,V> {\n" +
			"    }\n" +
			"    \n" +
			"    public static class DefaultExample<K,V> implements Example<K,V> {\n" +
			"    }\n" +
			"    @SuppressWarnings(\"rawtypes\")\n" +
			"    static final Class<? extends Example> fails = DefaultExample.class;\n" +
			"}\n" +
			"",
		}
	);
}
public void testBug518157B() {
	runConformTest(
		new String[] {
			"AlternateRawClassParameterizationBug.java",
			"import java.util.Map;\n" +
			"\n" +
			"class AlternateRawClassParameterizationBug {\n" +
			"\n" +
			"    abstract static class MapEntry<K,V> implements Map.Entry<K, V> {\n" +
			"    }\n" +
			"\n" +
			"    @SuppressWarnings(\"rawtypes\")\n" +
			"    static final Class<? extends Map.Entry> mapFails = MapEntry.class;\n" +
			"\n" +
			"}\n" +
			"",
		}
	);
}
public void testBug521212() {
	runNegativeTest(
		new String[] {
			"X.java",
			"class Y<U extends Z> {}\n" +
			"class Z {}\n" +
			"public class X<T> {\n" +
			"    public static <V> Y<? extends V> one() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public static <V> Y<? extends V> one() {\n" +
		"	                    ^^^^^^^^^^^\n" +
		"Bound mismatch: The type ? extends V is not a valid substitute for the bounded parameter <U extends Z> of the type Y<U>\n" +
		"----------\n");
}
public void testBug526423() {
	runConformTest(
		new String[] {
			"test/compileBug/TestCompileBug.java",
			"package test.compileBug;\n" +
			"\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.LinkedHashMap;\n" +
			"import java.util.List;\n" +
			"import java.util.Map;\n" +
			"import java.util.Map.Entry;\n" +
			"\n" +
			"public class TestCompileBug {\n" +
			"    @SuppressWarnings({ \"rawtypes\" })\n" +
			"    private static void cannotCompile(Object token) {\n" +
			"    		// change the type to List<Entry> and ArrayList<Entry> and it compiles\n" +
			"    		@SuppressWarnings(\"unchecked\")\n" +
			"    		List<Map.Entry> identityServicesToTokensMap = new ArrayList<Map.Entry>(((LinkedHashMap) token).entrySet());\n" +
			"    }\n" +
			"}"
		}
	);
}
public void testBug526132() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	runNegativeTest(
		new String[] {
			"Test.java",
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"public class Test {\n" +
			"	private Map field = new HashMap();\n" +
			"	private void method() {\n" +
			"		field.put(\"key\", \"value\");\n" +
			"	}\n" +
			"	private void method() {\n" +
			"		field.put(\"key\", \"value\");\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in Test.java (at line 4)\n" +
		"	private Map field = new HashMap();\n" +
		"	        ^^^\n" +
		"Map is a raw type. References to generic type Map<K,V> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in Test.java (at line 4)\n" +
		"	private Map field = new HashMap();\n" +
		"	                        ^^^^^^^\n" +
		"HashMap is a raw type. References to generic type HashMap<K,V> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in Test.java (at line 5)\n" +
		"	private void method() {\n" +
		"	             ^^^^^^^^\n" +
		"Duplicate method method() in type Test\n" +
		"----------\n" +
		"4. WARNING in Test.java (at line 5)\n" +
		"	private void method() {\n" +
		"	             ^^^^^^^^\n" +
		"The method method() from the type Test is never used locally\n" +
		"----------\n" +
		"5. WARNING in Test.java (at line 6)\n" +
		"	field.put(\"key\", \"value\");\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The method put(Object, Object) belongs to the raw type Map. References to generic type Map<K,V> should be parameterized\n" +
		"----------\n" +
		"6. ERROR in Test.java (at line 8)\n" +
		"	private void method() {\n" +
		"	             ^^^^^^^^\n" +
		"Duplicate method method() in type Test\n" +
		"----------\n" +
		"7. WARNING in Test.java (at line 9)\n" +
		"	field.put(\"key\", \"value\");\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The method put(Object, Object) belongs to the raw type Map. References to generic type Map<K,V> should be parameterized\n" +
		"----------\n",
	null,
	true,
	customOptions);
}
public void testBug520482() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNAVOIDABLE_GENERIC_TYPE_PROBLEMS, JavaCore.DISABLED);
	runConformTest(
		new String[] {
			"test/A.java",
			"package test;\n" +
			"\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class A {\n" +
			"    static List f;\n" +
			"}\n",
		},
		customOptions
	);
	runNegativeTest(false,
		new String[] {
			"test/B.java",
			"package test;\n" +
			"\n" +
			"public class B extends A {\n" +
			"   public static test() {\n" +
			"		f.add(new B());\n" +
			"	}\n" +
			"}\n"
		},
		null,
		customOptions,
		"----------\n" +
		"1. ERROR in test\\B.java (at line 4)\n" +
		"	public static test() {\n" +
		"	              ^^^^^^\n" +
		"Return type for the method is missing\n" +
		"----------\n",
		"", "", null
	);
}
public void testBug532137() {
	runConformTest(
		new String[] {
			"subtypes/A.java",
			"package subtypes;\n" +
			"\n" +
			"\n" +
			"public abstract class A<R extends B> {\n" +
			"\n" +
			"	public A() {\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
			"subtypes/B.java",
			"package subtypes;\n" +
			"\n" +
			"public abstract class B  {\n" +
			"\n" +
			"	public B() {\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
			"subtypes/GenericType.java",
			"package subtypes;\n" +
			"public abstract class GenericType<REQ extends A<RES>, RES extends B> {\n" +
			"\n" +
			"}\n" +
			"",
			"subtypes/TestBase.java",
			"package subtypes;\n" +
			"\n" +
			"import java.util.Collections;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public abstract class TestBase {\n" +
			"\n" +
			"	@SuppressWarnings(\"rawtypes\")\n" +
			"	protected List<Class<? extends GenericType>> giveMeAListOfTypes() {\n" +
			"		return Collections.emptyList();\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		}
	);
	runNegativeTest(
		new String[] {
			"subtypes/TestImpl.java",
			"package subtypes;\n" +
			"\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class TestImpl extends TestBase{\n" +
			"\n" +
			"	@Override\n" +
			"    protected List<Class<? extends GenericType>> giveMeAListOfTypes() {\n" +
			"		return super.giveMeAListOfTypes();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"----------\n" +
		"1. WARNING in subtypes\\TestImpl.java (at line 8)\n" +
		"	protected List<Class<? extends GenericType>> giveMeAListOfTypes() {\n" +
		"	                               ^^^^^^^^^^^\n" +
		"GenericType is a raw type. References to generic type GenericType<REQ,RES> should be parameterized\n" +
		"----------\n",
		null,
		false
	);
}

public void testBug540313() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X/C120644mr.java",
			"package X;\n" +
			"\n" +
			"public class C120644mr<V, X extends java.lang.Exception> extends X.C16280iv<V> {\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X\\C120644mr.java (at line 3)\n" +
			"	public class C120644mr<V, X extends java.lang.Exception> extends X.C16280iv<V> {\n" +
			"	                                                                 ^^^^^^^^^^\n" +
			"X.C16280iv cannot be resolved to a type\n" +
			"----------\n";
	runner.runNegativeTest();
}

public void testBug540313a() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X/C120644mr.java",
			"package X;\n" +
			"\n" +
			"class Outer {\n" +
			"	class Inner<Z> {}\n" +
			"}\n" +
			"public class C120644mr<V, X extends Outer> extends X.Inner<V> {\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X\\C120644mr.java (at line 6)\n" +
			"	public class C120644mr<V, X extends Outer> extends X.Inner<V> {\n" +
			"	                                                   ^^^^^^^\n" +
			"X.Inner cannot be resolved to a type\n" +
			"----------\n";
	runner.runNegativeTest();
}

public void testBug540313b() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X/C120644mr.java",
			"package X;\n" +
			"\n" +
			"class Outer {\n" +
			"	class Inner<Z> {}\n" +
			"}\n" +
			"public class C120644mr<X extends Outer, V> {\n" +
			"	X.Inner<V> inner;\n" + // is this backed by JLS?
			"}\n"
		};
	runner.runConformTest();
}
public void testBug478708() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"bug/IInterface.java",
			"package bug;\n" +
			"\n" +
			"public class IInterface {\n" +
			"\n" +
			"}\n",
			"bug/AbstractA.java",
			"package bug;\n" +
			"\n" +
			"public abstract class AbstractA<T extends IInterface> {\n" +
			"\n" +
			"	public abstract class AbstractD<U> {\n" +
			"\n" +
			"	}\n" +
			"}\n",
			"bug/AbstractC.java",
			"package bug;\n" +
			"\n" +
			"\n" +
			"public abstract class AbstractC<T extends IInterface> extends T.AbstractD<E>  {\n" +
			"\n" +
			"	public AbstractC(AbstractA<T> a) {\n" +
			"		a.super();\n" +
			"	}\n" +
			"}\n",
			"bug/E.java",
			"package bug;\n" +
			"\n" +
			"public class E {\n" +
			"\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in bug\\AbstractC.java (at line 4)\n" +
			"	public abstract class AbstractC<T extends IInterface> extends T.AbstractD<E>  {\n" +
			"	                                                              ^^^^^^^^^^^\n" +
			"T.AbstractD cannot be resolved to a type\n" +
			"----------\n";
	runner.runNegativeTest();
}
public void testBug543526() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"C.java",
			"class C {\n" +
			"	<T extends CharSequence & java.util.List<T>> boolean m(T x) {\n" +
			"		return x instanceof String;\n" +
			"	}\n" +
			"}\n"
	};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in C.java (at line 3)\n" +
			"	return x instanceof String;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Incompatible conditional operand types T and String\n" +
			"----------\n";
	runner.runNegativeTest();
}
public void testBug543526b() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"C.java",
			"class C {\n" +
			"	<T extends CharSequence & java.util.List<T>> boolean m(T x) {\n" +
			"		return x instanceof Comparable<?>;\n" + // no problem casting to an interface
			"	}\n" +
			"}\n"
	};
	runner.runConformTest();
}
public void testBug552388() {
	runNegativeTest(
		new String[] {
			"A.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class A<T extends B> {\n" +
			"    protected List<C> list = new ArrayList<C>();\n" +
			"\n" +
			"    class C {}\n" +
			"\n" +
			"    public void createIO() {\n" +
			"        A<? extends B> x = null;\n" +
			"        List<A<? extends B>.C> y = x.list;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class B {\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 11)\n" +
		"	List<A<? extends B>.C> y = x.list;\n" +
		"	                           ^^^^^^\n" +
		"Type mismatch: cannot convert from List<A<capture#1-of ? extends B>.C> to List<A<? extends B>.C>\n" +
		"----------\n");
}
public void testBug552388b() {
	String output = this.complianceLevel > ClassFileConstants.JDK1_6 ?
			"----------\n" +
			"1. ERROR in A.java (at line 17)\n" +
			"	foo(l);\n" +
			"	^^^\n" +
			"The method foo(List<A<?>.C>) in the type A<T> is not applicable for the arguments (List<A<T>.C>)\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 33)\n" +
			"	foo2(l); \n" +
			"	^^^^\n" +
			"The method foo2(List<A<?>>) in the type A<T> is not applicable for the arguments (List<A<T>>)\n" +
			"----------\n"
			:
			"----------\n" +
			"1. ERROR in A.java (at line 16)\n" +
			"	List<C> l = new ArrayList<>();\n" +
			"	                ^^^^^^^^^\n" +
			"\'<>\' operator is not allowed for source level below 1.7\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 17)\n" +
			"	foo(l);\n" +
			"	^^^\n" +
			"The method foo(List<A<?>.C>) in the type A<T> is not applicable for the arguments (List<A<T>.C>)\n" +
			"----------\n" +
			"3. ERROR in A.java (at line 32)\n" +
			"	List<A<T>> l = new ArrayList<>();\n" +
			"	                   ^^^^^^^^^\n" +
			"\'<>\' operator is not allowed for source level below 1.7\n" +
			"----------\n" +
			"4. ERROR in A.java (at line 33)\n" +
			"	foo2(l); \n" +
			"	^^^^\n" +
			"The method foo2(List<A<?>>) in the type A<T> is not applicable for the arguments (List<A<T>>)\n" +
			"----------\n";
	runNegativeTest(
		new String[] {
			"A.java",
			"import java.util.*;\n" +
			"class A<T> {\n" +
			"    class C {\n" +
			"        T f;\n" +
			"    }\n" +
			"    C c = new C();\n" +
			"    A(T t) {\n" +
			"        c = new C();\n" +
			"        c.f = t;\n" +
			"    }\n" +
			"    void foo(List<A<?>.C> list) {\n" +
			"        list.add(new A<String>(\"\").c);\n" +
			"        list.add(new A<Number>(3).c);\n" +
			"    }\n" +
			"    T test() {\n" +
			"        List<C> l = new ArrayList<>();\n" +
			"        foo(l);\n" +
			"        return l.get(0).f;\n" +
			"    }\n" +
			"    public static void main(String... args) {\n" +
			"//         Number n = new A<Number>(1).test();\n" +
			"//         System.out.print(n.intValue());\n" +
			"        Number n = new A<Number>(1).test2();\n" +
			"        System.out.print(n.intValue());\n" +
			"    }\n" +
			"     \n" +
			"    void foo2(List<A<?>> list) {\n" +
			"        list.add(new A<String>(\"\"));\n" +
			"        list.add(new A<Number>(3));\n" +
			"    }\n" +
			"    T test2() {\n" +
			"        List<A<T>> l = new ArrayList<>();\n" +
			"        foo2(l); \n" +
			"        return l.get(0).c.f;\n" +
			"    }\n" +
			" \n" +
			"}\n"
		},
		output);
}
public void testBug561544() {
	if (this.complianceLevel < ClassFileConstants.JDK11)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNAVOIDABLE_GENERIC_TYPE_PROBLEMS, JavaCore.DISABLED);
	runNegativeTest(false,
		new String[] {
			"com/bsbportal/music/C2193c.java",
			"package com.bsbportal.music;\n"
			+ "\n"
			+ "public class C2193c {\n"
			+ "    java.util.List f6088b;\n"
			+ "    public void onResponse(p594x.C14183r<java.lang.String> rVar) {\n"
			+ "        mo12821b((java.util.List<java.lang.String>) this.f6088b);\n"
			+ "    }\n"
			+ "}\n"
		},
		null,
		customOptions,
		"----------\n" +
		"1. WARNING in com\\bsbportal\\music\\C2193c.java (at line 4)\n" +
		"	java.util.List f6088b;\n" +
		"	^^^^^^^^^^^^^^\n" +
		"List is a raw type. References to generic type List<E> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in com\\bsbportal\\music\\C2193c.java (at line 5)\n" +
		"	public void onResponse(p594x.C14183r<java.lang.String> rVar) {\n" +
		"	                       ^^^^^\n" +
		"p594x cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in com\\bsbportal\\music\\C2193c.java (at line 6)\n" +
		"	mo12821b((java.util.List<java.lang.String>) this.f6088b);\n" +
		"	^^^^^^^^\n" +
		"The method mo12821b(List<String>) is undefined for the type C2193c\n" +
		"----------\n" +
		"4. WARNING in com\\bsbportal\\music\\C2193c.java (at line 6)\n" +
		"	mo12821b((java.util.List<java.lang.String>) this.f6088b);\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked cast from List to List<String>\n" +
		"----------\n",
		"", "", null
	);
}

public void testBug576524() {
	if (this.complianceLevel >= ClassFileConstants.JDK16) {
		this.runConformTest(
			new String[] {
				"Singleton.java",
				"public class Singleton<T> {\n" +
				"    public static void main(String[] obj) {\n" +
				"        test(new Pair<Integer,String>());\n" +
				"    }\n" +
				"    public static <T> void test(Singleton<T> singleton) {\n" +
				"        if (singleton instanceof Pair<T,?> pair) {\n" +
				"            System.out.println(\"SUCCESS\");\n" +
				"        }\n" +
				"    }\n" +
				"}",
				"Pair.java",
				"public class Pair<T,U> extends Singleton<T> {\n" +
				"}"
			},
			"SUCCESS"
		);
	}
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/472
// If unchecked conversion was necessary for the arguments,
// substitute and erase the return type.
public void testBugGH472_a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(
			new String[] {
				"ReturnTypeTest.java",
				"public class ReturnTypeTest {\n"
				+ "    <T> T m(Class<T> arg1, Class<T> arg2) { return null; }\n"
				+ "\n"
				+ "    void test(Class c1, Class<Class<String>> c2) throws Exception {\n"
				+ "        m(c1, c2).newInstance();\n"
				+ "    }\n"
				+ "}"
			}
		);
	}
}

// A variation for the unchecked conversion test case.
// the type arguments contain wildcards like <? extends T>.
public void testBugGH472_b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(
			new String[] {
				"ReturnTypeTest.java",
				"public class ReturnTypeTest {\n"
				+ "    <T> T m(Class<T> arg1, Class<? extends T> arg2) { return null; }\n"
				+ "\n"
				+ "    void test(Class c1, Class<Class<String>> c2) throws Exception {\n"
				+ "        m(c1, c2).newInstance();\n"
				+ "    }\n"
				+ "}"
			}
		);
	}
}

// A variation for the unchecked conversion test case.
// the type arguments contain wildcards like <? super T>.
public void testBugGH472_c() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(
			new String[] {
				"ReturnTypeTest.java",
				"public class ReturnTypeTest {\n"
				+ "    <T> T m(Class<T> arg1, Class<? super T> arg2) { return null; }\n"
				+ "\n"
				+ "    void test(Class c1, Class<Class<String>> c2) throws Exception {\n"
				+ "        m(c1, c2).newInstance();\n"
				+ "    }\n"
				+ "}"
			}
		);
	}
}

// If unchecked conversion was necessary for the arguments,
// substitute and erase the thrown type.
public void testBugGH472_d() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(
			new String[] {
				"ThrowTest.java",
				"public class ThrowTest {\n"
				+ "\n"
				+ "    public static void test(MyDerivedException e, MyType t) {\n"
				+ "        try {\n"
				+ "            new Foo(e, t);\n"
				+ "        } catch (MyDerivedException e2) {}\n"
				+ "    }\n"
				+ "}\n"
				+ "\n"
				+ "class MyException extends Exception {}\n"
				+ "class MyDerivedException extends MyException {}\n"
				+ "\n"
				+ "class MyType<T> {}\n"
				+ "\n"
				+ "class Foo {\n"
				+ "    public <E1 extends MyException> Foo(E1 e, MyType<String> a) throws E1 {\n"
				+ "        throw e;\n"
				+ "    }\n"
				+ "}"
			}
		);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=540063
// VerifyError with nested static templated instance
public void test540063() {
	this.runConformTest(
		new String[] {
			"SomeClass.java",
			"public class SomeClass {\n" +
			"  public static void main(String[] args) {\n" +
			"    String s = Namespace.t.contained.s;\n" +
			"    System.out.println(s);\n" +
			"  }\n" +
			"\n" +
			"  static class Namespace {\n" +
			"    static Templated<StringHolder> t = new Templated<StringHolder>(new StringHolder());\n" +
			"  }\n" +
			"\n" +
			"  static class Templated<T> {\n" +
			"    T contained;\n" +
			"\n" +
			"    Templated(T contained) {\n" +
			"      this.contained = contained;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  static class StringHolder {\n" +
			"    String s = \"some string\";\n" +
			"  }\n" +
			"}\n"
			},
		"some string"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=540063
// VerifyError with nested static templated instance
public void test540063_2() {
	this.runConformTest(
		new String[] {
			"SomeClass.java",
			"public class SomeClass {\n" +
			"  public static void main(String[] args) {\n" +
			"    String s = t.contained.s;\n" +
			"    System.out.println(s);\n" +
			"  }\n" +
			"\n" +
			"  static Templated<StringHolder> t = new Templated<StringHolder>(new StringHolder());\n" +
			"\n" +
			"  static class Templated<T> {\n" +
			"    T contained;\n" +
			"\n" +
			"    Templated(T contained) {\n" +
			"      this.contained = contained;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  static class StringHolder {\n" +
			"    String s = \"some string\";\n" +
			"  }\n" +
			"}\n"
		},
		"some string"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=540063
// VerifyError with nested static templated instance
public void test540063_3() {
	this.runConformTest(
		new String[] {
			"SomeClass.java",
			"public class SomeClass {\n" +
			"  public static void main(String[] args) {\n" +
			"    String s = Namespace_O.Namespace_M.Namespace_I.t.contained.s;\n" +
			"    System.out.println(s);\n" +
			"  }\n" +
			"\n" +
			" static class Namespace_O {\n" +
			"	static class Namespace_M {\n" +
			"		static class Namespace_I {\n" +
			"			static Templated<StringHolder> t = new Templated<StringHolder>(new StringHolder());\n" +
			"		}\n" +
			"	}\n" +
			"  }\n" +
			"\n" +
			"  static class Templated<T> {\n" +
			"    T contained;\n" +
			"\n" +
			"    Templated(T contained) {\n" +
			"      this.contained = contained;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  static class StringHolder {\n" +
			"    String s = \"some string\";\n" +
			"  }\n" +
			"}\n"
		},
		"some string"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=570022
// QualifiedNameReference.setGenericCast(...) throws ArrayIndexOutOfBoundsException
public void test570022() {
	this.runNegativeTest(
			new String[] {
					"p000/aazh.java",
					"package p000;\n" +
					"\n" +
					"public class aazh {\n" +
					"    public static p000.abyz<p000.aazh.EnumC0166a> f2789a = p000.abyz.m1243c(p000.aazh.EnumC0166a.DISABLED);\n" +
					"\n" +
					"    public enum EnumC0166a {\n" +
					"        DISABLED;\n" +
					"        \n" +
					"        public boolean f2796f;\n" +
					"    }\n" +
					"}\n",
					"p000/abam.java",
					"package p000;\n" +
					"\n" +
					"public class abam {\n" +
					"    public Object mo369h() {\n" +
					"        return p000.aazh.f2789a.f4668a.f2796f;\n" +
					"    }\n" +
					"}\n",
					"p000/abyz.java",
					"package p000;\n" +
					"\n" +
					"public class abyz<T> {\n" +
					"    public volatile T f4668a;\n" +
					"}\n",
			},
			"----------\n" +
			"1. ERROR in p000\\aazh.java (at line 4)\n" +
			"	public static p000.abyz<p000.aazh.EnumC0166a> f2789a = p000.abyz.m1243c(p000.aazh.EnumC0166a.DISABLED);\n" +
			"	                                                                 ^^^^^^\n" +
			"The method m1243c(aazh.EnumC0166a) is undefined for the type abyz\n" +
			"----------\n");
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/656
//Compilation error with multiple bounds and package protected abstract method
public void testBugGH656() {
		this.runConformTest(
			new String[] {
				"package1/MyGenericClass.java",
				"package package1;\n" +
				"import package2.MyAbstract;\n" +
				"import package2.MyInterface;\n" +
				"public class MyGenericClass<T extends MyAbstract & MyInterface> { // removing MyInterface works\n" +
				"}\n",
				"package2/MyInterface.java",
				"package package2;\n" +
				"public interface MyInterface {\n" +
				"     void myMethod();\n" +
				"}\n",
				"package2/MyAbstract.java",
				"package package2;\n" +
				"public abstract class MyAbstract {\n" +
				"	/* package protected! */ abstract void someAbstractMethod();\n" +
				"}\n"
			}
		);
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/656
//Compilation error with multiple bounds and package protected abstract method
public void testBugGH656_2() {
		this.runNegativeTest(
			new String[] {
				"package1/MyGenericClass.java",
				"package package1;\n" +
				"import package2.MyAbstract;\n" +
				"import package2.MyInterface;\n" +
				"public class MyGenericClass<T extends MyAbstract & MyInterface> { // removing MyInterface works\n" +
				"}\n" +
				"class JavacWillAlsoError extends MyAbstract implements MyInterface {\n" +
				"\n" +
				"	public void myMethod() {\n" +
				"	}\n" +
				"\n" +
				"	public void someAbstractMethod() {\n" +
				"	}\n" +
				"}\n",
				"package2/MyInterface.java",
				"package package2;\n" +
				"public interface MyInterface {\n" +
				"     void myMethod();\n" +
				"}\n",
				"package2/MyAbstract.java",
				"package package2;\n" +
				"public abstract class MyAbstract {\n" +
				"	/* package protected! */ abstract void someAbstractMethod();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in package1\\MyGenericClass.java (at line 6)\n" +
			"	class JavacWillAlsoError extends MyAbstract implements MyInterface {\n" +
			"	      ^^^^^^^^^^^^^^^^^^\n" +
			"This class must implement the inherited abstract method MyAbstract.someAbstractMethod(), but cannot override it since it is not visible from JavacWillAlsoError. Either make the type abstract or make the inherited method visible\n" +
			"----------\n" +
			"2. WARNING in package1\\MyGenericClass.java (at line 11)\n" +
			"	public void someAbstractMethod() {\n" +
			"	            ^^^^^^^^^^^^^^^^^^^^\n" +
			"The method JavacWillAlsoError.someAbstractMethod() does not override the inherited method from MyAbstract since it is private to a different package\n" +
			"----------\n");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1802
// Unchecked casts go unreported with ECJ
public void testIssue1802() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
			class X {
					void foo(I<X> ix) {
						A<Y> ay = (A<Y>) ix;
						B<X> bx = (B<X>) ix;
					}
			}
			class Y extends X {}
			class Z extends X {}

			interface I<T>  {
			}

			final class B<T> implements I<X> {
			}


			final class A<T> implements I<X> {
			}
			"""
		};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	A<Y> ay = (A<Y>) ix;\n" +
			"	          ^^^^^^^^^\n" +
			"Type safety: Unchecked cast from I<X> to A<Y>\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	B<X> bx = (B<X>) ix;\n" +
			"	          ^^^^^^^^^\n" +
			"Type safety: Unchecked cast from I<X> to B<X>\n" +
			"----------\n";
	runner.runWarningTest();
}
}

