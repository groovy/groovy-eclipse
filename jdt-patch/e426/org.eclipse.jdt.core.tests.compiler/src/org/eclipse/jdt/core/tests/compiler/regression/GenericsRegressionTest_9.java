/*******************************************************************************
 * Copyright (c) 2016, 2021 IBM Corporation.
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

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class GenericsRegressionTest_9 extends AbstractRegressionTest9 {

static {
//	TESTS_NAMES = new String[] { "testBug551913_001", "testBug551913_002" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public GenericsRegressionTest_9(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_9);
}

// vanilla test case
public void testBug488663_001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public Y<String> bar() {\n" +
			"		Y<String> y = new Y<>() {\n" +
			"			@Override\n" +
			"			public void foo(String s) {\n" +
			"				this.s = s;\n" +
			"	 		}\n" +
			"		};\n" +
			"		return y;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		Y<String> y = new X().bar();\n" +
			"		y.foo(\"Done\");\n" +
			"		y.print();\n" +
			"	}\n" +
			"}\n" +
			"abstract class Y<T> {\n" +
			"	String s;\n" +
			"	public abstract void foo(String s);\n" +
			"	public void print() {\n" +
			"		System.out.println(this.s);\n" +
			"	}\n" +
			"}\n",
		},
		"Done");
}

// negative test case for diamond operator instantiation of denotable anonymous type but with parameterized method
public void testBug488663_002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public Y<String> bar() {\n" +
			"		Y<String> y = new Y<>() {\n" +
			"			@Override\n" +
			"			public void foo(T t) {\n" +
			"				this.s = t;\n" +
			"			}\n" +
			"		};\n" +
			"		return y;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		Y<String> y = new X().bar();\n" +
			"		y.foo(\"Done\");\n" +
			"		y.print();\n" +
			"	}\n" +
			"}\n" +
			"abstract class Y<T> {\n" +
			"	T s;\n" +
			"	public abstract void foo(T t);\n" +
			"	public void print() {\n" +
			"		System.out.println(this.s);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	Y<String> y = new Y<>() {\n" +
		"	                  ^^^^^\n" +
		"The type new Y<String>(){} must implement the inherited abstract method Y<String>.foo(String)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	public void foo(T t) {\n" +
		"	                ^\n" +
		"T cannot be resolved to a type\n" +
		"----------\n");
}

// diamond operator instantiation of denotable anonymous types with different type params
public void testBug488663_003() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"@SuppressWarnings(\"unused\") \n" +
			"	public static void main(String[] args) {\n" +
			"		Y<?> y1 = new Y<>(){};\n" +
			"		Y<String> y2 = new Y<>(){};\n" +
			"		Y<? extends String> y3 = new Y<>() {};\n" +
			"		Y<? super String> y4 = new Y<>() {};\n" +
			"	}\n" +
			"}\n" +
			"class Y<T> {}\n",
		},
		"");
}

// inner classes with diamond operator and anonymous classes
public void testBug488663_004() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"@SuppressWarnings(\"unused\") \n" +
			"	public static void main(String[] args) {\n" +
			"		Y<?> y1 = new X().new Y<>(){};\n" +
			"		Y<String> y2 = new X().new Y<>(){};\n" +
			"		Y<? extends String> y3 = new X().new Y<>() {};\n" +
			"		Y<? super String> y4 = new X().new Y<>() {};\n" +
			"	}\n" +
			"\n" +
			"	class Y<T> {}\n" +
			"}\n",
		},
		"");
}

// compiler error for non-denotable anonymous type with diamond operator - negative test
public void testBug488663_005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"interface J{}\n" +
				"class Y<T extends I & J> {}\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Y<?> y = new Y<>() {};\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	Y<?> y = new Y<>() {};\n" +
			"	             ^\n" +
			"Type Y<I & J> inferred for Y<>, is not valid for an anonymous class with '<>'\n" +
			"----------\n");

}

//compiler error for non-denotable anonymous type with diamond operator - negative test
public void testBug488663_006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class Y<T> {\n" +
				"   Y(T x) {}\n" +
				"}\n" +
				"\n" +
				"class X {\n" +
				"  public static void main(String[] args) {\n" +
				"	  Y<? extends Integer> fi = null;\n" +
				"	  Y<?> f = new Y<>(fi){};\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	Y<?> f = new Y<>(fi){};\n" +
			"	             ^\n" +
			"Type Y<Y<capture#1-of ? extends Integer>> inferred for Y<>, is not valid for an anonymous class with '<>'\n" +
			"----------\n");

}
// instantiate an interface using the anonymous diamond
public void testBug488663_007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String name;\n" +
			"	public X(String name) {\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	String name() {\n" +
			"		return this.name;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X(\"Success\");\n" +
			"		I<X> i = new I<>() {\n" +
			"			public String toString(X x1) {\n" +
			"				return x1.name();\n" +
			"			}\n" +
			"		};\n" +
			"		System.out.println(i.toString(x));\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {\n" +
			"	String toString(T t);\n" +
			"}"
		},
		"Success");
}
// anonymous diamond instantiating interface as argument to an invocation
public void testBug488663_008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String name;\n" +
			"	public X(String name) {\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	<T> void print(T o, I<T> converter) {\n" +
			"		System.out.println(converter.toString(o));\n" +
			"	}\n" +
			"	String name() {\n" +
			"		return this.name;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X(\"Success\");\n" +
			"		x.print(x, new I<>() {\n" +
			"			public String toString(X x1) {\n" +
			"				return x1.name();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {\n" +
			"	String toString(T t);\n" +
			"}"
		},
		"Success");
}
// anonymous diamond instantiating an abstract class as argument to an invocation
public void testBug488663_009() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String name;\n" +
			"	public X(String name) {\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	<T> void print(T o, I<T> converter) {\n" +
			"		System.out.println(converter.toString(o));\n" +
			"	}\n" +
			"	String name() {\n" +
			"		return this.name;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X(\"Success\");\n" +
			"		x.print(x, new Z<>() {\n" +
			"			public String toString(X x1) {\n" +
			"				return x1.name();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {\n" +
			"	String toString(T t);\n" +
			"}\n" +
			"abstract class Z<T> implements I<T> {}\n"
		},
		"Success");
}
// anonymous diamond with polytype argument
public void testBug488663_010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String name;\n" +
			"	public X(String name) {\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		Y<String> y = new Y<>(() -> System.out.println(\"Done\")) {\n" +
			"		};\n" +
			"	}\n" +
			"}\n" +
			"interface J {\n" +
			"	void doSomething();\n" +
			"}\n" +
			"class Y<T> {\n" +
			"	public Y(J j) {\n" +
			"		j.doSomething();\n" +
			"	}\n" +
			"}",
		},
		"Done");
}
// anonymous diamond with polytype argument
public void testBug488663_011() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String name;\n" +
			"	public X(String name) {\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		Y<String> y = new Y<>(Y::foo) {\n" +
			"		};\n" +
			"	}\n" +
			"}\n" +
			"interface J {\n" +
			"	void doSomething();\n" +
			"}\n" +
			"class Y<T> {\n" +
			"	public Y(J j) {\n" +
			"		j.doSomething();\n" +
			"	}\n" +
			"	static void foo() {\n" +
			"		System.out.println(\"Done\");\n" +
			"	}\n" +
			"}",
		},
		"Done");
}
// Nested anonymous diamonds - TODO - confirm that this is indeed correct as per spec
public void testBug488663_012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String name;\n" +
			"	public X(String name) {\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	String name() {\n" +
			"		return this.name;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		Y<String> y = new Y<>(\"Done\", new I<>() {\n" +
			"				public void doSomething(String s) {\n" +
			"					System.out.println(s);\n" +
			"				}\n" +
			"			}){\n" +
			"		};\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {\n" +
			"	void doSomething(T t);\n" +
			"}\n" +
			"class Y<T> {\n" +
			"	public Y(T t, I<T> i) {\n" +
			"		i.doSomething(t);\n" +
			"	}\n" +
			"}",
		},
		"Done");
}
// Redundant type argument specification - TODO - confirm that this is correct
public void testBug488663_013() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String name;\n" +
			"	public X(String name) {\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	String name() {\n" +
			"		return this.name;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X(\"Success\");\n" +
			"		I<X> i = new I<X>() {\n" +
			"			public String toString(X x1) {\n" +
			"				return x1.name();\n" +
			"			}\n" +
			"		};\n" +
			"		System.out.println(i.toString(x));\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {\n" +
			"	String toString(T t);\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	I<X> i = new I<X>() {\n" +
		"	             ^\n" +
		"Redundant specification of type arguments <X>\n" +
		"----------\n",
		null, true, options);
}
// All non-private methods of an anonymous class instantiated with '<>' must be treated as being annotated with @override
public void testBug488663_014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String name;\n" +
			"	public X(String name) {\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	<T> void print(T o, I<T> converter) {\n" +
			"		System.out.println(converter.toString(o));\n" +
			"	}\n" +
			"	String name() {\n" +
			"		return this.name;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X(\"asdasfd\");\n" +
			"		x.print(x, new Z<>() {\n" +
			"			public String toString(String s) {\n" +
			"				return s;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {\n" +
			"	String toString(T t);\n" +
			"}\n" +
			"class Z<T> implements I<T> {\n" +
			"	public String toString(T t) {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	public String toString(String s) {\n" +
		"	              ^^^^^^^^^^^^^^^^^^\n" +
		"The method toString(String) of type new Z<X>(){} must override or implement a supertype method\n" +
		"----------\n");
}
// Inaccessible type inferred for anonymous diamond is an error
public void testBug488663_015() {
	this.runNegativeTest(
		new String[] {
			"Test.java",
			"public class Test<T> {\n" +
			"	private static class Inner {" +
			"		public Inner(){}\n" +
			"	}\n" +
			"	<R> void print(I<R> i) {}\n" +
			"	public Inner get() {\n" +
			"		return new Inner();\n" +
			"	}\n" +
			"}\n",
			"Z.java",
			"class Z<T> implements I<T> {\n" +
			"	public Z(T t1) {}\n" +
			"	public String toString (T t) {\n" +
			"		return t.toString();\n" +
			"	}\n" +
			"}",
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Test<String> t = new Test<>();\n" +
			"		t.print(new Z<>(t.get()) {\n" +
			"			\n" +
			"		});\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {\n" +
			"	String toString();\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	t.print(new Z<>(t.get()) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The type Test$Inner is not visible\n" +
		"----------\n");
}
// Inaccessible type inferred for anonymous diamond is an error - interface case
public void testBug488663_016() {
	this.runNegativeTest(
		new String[] {
			"Test.java",
			"public class Test<T> {\n" +
			"	private static class Inner {" +
			"		public Inner(){}\n" +
			"	}\n" +
			"	<R extends Inner> void print(I<R> i) {}\n" +
			"	public Inner get() {\n" +
			"		return new Inner();\n" +
			"	}\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Test<String> t = new Test<>();\n" +
			"		t.print(new I<>() {\n" +
			"			public String toString() {\n" +
			"				return \"\";\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {\n" +
			"	String toString();\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	t.print(new I<>() {\n" +
		"	            ^^^^^\n" +
		"The type Test$Inner is not visible\n" +
		"----------\n");
}
// All non-private methods of an anonymous class instantiated with '<>' must be treated as being annotated with @override
public void testBug517926() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String name;\n" +
			"	public X(String name) {\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	<T> void print(T o, I<T> converter) {\n" +
			"		System.out.println(converter.toString(o));\n" +
			"	}\n" +
			"	String name() {\n" +
			"		return this.name;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X(\"asdasfd\");\n" +
			"		x.print(x, new I<>() {\n" +
			"			public String name() {return null;}\n" +
			"			public String toString(X xx) {\n" +
			"				return xx.toString();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}\n" +
			"interface I<T> {\n" +
			"private String name() {return null;}" +
			"	String toString(T t);\n" +
			"default String getName() {return name();}" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	public String name() {return null;}\n" +
		"	              ^^^^^^\n" +
		"The method name() of type new I<X>(){} must override or implement a supertype method\n" +
		"----------\n");
}
public void testBug521815a() {
	runNegativeTest(
			new String[] {
					"a/b/X.java",
					"package a.b;\n" +
					"interface I{\n" +
					"    public static class Inner { }\n" +
					"}\n" +
					"class Cl {\n" +
					"    public static class Inner {}\n" +
					"}\n" +
					"public class X extends Cl implements I {}\n",
					"a/Y.java",
					"package p;\n" +
					"import static a.b.X.Inner;\n" +
					"public class Y {;\n" +
					"	Inner t;\n" +
					"}\n"
			},
			"----------\n" +
			"1. ERROR in a\\Y.java (at line 4)\n" +
			"	Inner t;\n" +
			"	^^^^^\n" +
			"The type Inner is ambiguous\n" +
			"----------\n");
}
public void testBug521815b() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_8) {
		return;
	}
	runNegativeTest(
			new String[] {
					"a/b/X.java",
					"package a.b;\n" +
					"interface I{\n" +
					"    public static class Inner { }\n" +
					"}\n" +
					"class Cl {\n" +
					"    public static class Inner {}\n" +
					"}\n" +
					"public class X extends Cl implements I {}\n",
					"a/Y.java",
					"package p;\n" +
					"import static a.b.X.Inner;\n" +
					"public class Y {;\n" +
					"}\n"
			},
			"----------\n" +
			"1. WARNING in a\\Y.java (at line 2)\n" +
			"	import static a.b.X.Inner;\n" +
			"	              ^^^^^^^^^^^\n" +
			"The import a.b.X.Inner is never used\n" +
			"----------\n");
}
public void testBug533644() {
	runConformTest(
		new String[] {
			"q/JobDetail.java",
			"package q;\n" +
			"import java.io.Serializable;\n" +
			"public interface JobDetail extends Serializable, Cloneable { }\n",
			"q/Scheduler.java",
			"package q;\n" +
			"import java.util.Map;\n" +
			"import java.util.Set;\n" +
			"public interface Scheduler {\n" +
			"    void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws SchedulerException;\n" +
			"}\n",
			"q/SchedulerException.java",
			"package q;\n" +
			"public class SchedulerException extends Exception {\n" +
			"    private static final long serialVersionUID = 174841398690789156L;\n" +
			"}\n",
			"q/Trigger.java",
			"package q;\n" +
			"import java.io.Serializable;\n" +
			"public interface Trigger extends Serializable, Cloneable, Comparable<Trigger> {\n" +
			"    public static final long serialVersionUID = -3904243490805975570L;\n" +
			"}\n"
		});
	Runner runner = new Runner();
	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"ForwardingScheduler.java",
			"import java.util.Map;\n" +
			"import java.util.Set;\n" +
			"\n" +
			"import q.JobDetail;\n" +
			"import q.Scheduler;\n" +
			"import q.SchedulerException;\n" +
			"import q.Trigger;\n" +
			"\n" +
			"public class ForwardingScheduler implements Scheduler {\n" +
			"  @Override\n" +
			"  public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace)\n" +
			"      throws SchedulerException {\n" +
			"  }\n" +
			"}\n"
	};
	runner.runConformTest();
}
//As All non-private methods of an anonymous class instantiated with '<>' must be treated as being annotated with @override,
//"Remove redundant type arguments" error should be reported if all the non-private methods defined in the anonymous class
//are also present in the parent class.
public void testBug551913_001() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	int foo() {\n" +
			"		java.util.HashSet<String> a = new java.util.HashSet<>();\n" +
			"		java.util.HashSet<String> b = new java.util.HashSet<String>(a) {\n" +
			"			private static final long serialVersionUID = 1L;\n" +
			"			public int x() {return 10;}\n" +
			"		};\n" +
			"		return 10;\n" +
			"	}\n\n" +
			"	public static void main(String[] args) {\n" +
			"		X abc= new X();\n" +
			"		System.out.println(abc.foo());" +
			"	}" +
			"}",
		},"10", options);
}
// As All non-private methods of an anonymous class instantiated with '<>' must be treated as being annotated with @override,
// "Remove redundant type arguments" error should be reported if all the non-private methods defined in the anonymous class
// are also present in the parent class.
public void testBug551913_002() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		java.util.HashSet<String> a = new java.util.HashSet<>();\n" +
			"		java.util.HashSet<String> b = new java.util.HashSet<String>(a) {\n" +
			"			private static final long serialVersionUID = 1L;\n" +
			"			public String toString() {return null;}\n" +
			"		};\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	java.util.HashSet<String> b = new java.util.HashSet<String>(a) {\n" +
		"	                                            ^^^^^^^\n" +
		"Redundant specification of type arguments <String>\n" +
		"----------\n",
		null, true, options);
}
public static Class<GenericsRegressionTest_9> testClass() {
	return GenericsRegressionTest_9.class;
}

}
