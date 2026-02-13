/*******************************************************************************
 * Copyright (c) 2016, 2025 IBM Corporation.
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
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Test class originally capturing issues specific to Java9, but meanwhile also just a continuation
 * of GenericsRegressionTest_1_8.
 */
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
//"Remove redundant type arguments" diagnostic should be reported ONLY if all the non-private methods defined in the anonymous class
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
// "Remove redundant type arguments" diagnostic should be reported ONLY if all the non-private methods defined in the anonymous class
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

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=551913
public void testBug551913_003() {
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
			"			public String toString() {return asString();}\n" +
			"           private String asString() { return null;}\n" +
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
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=551913
public void testBug551913_004() {
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
			"			public String toString() {return asString();}\n" +
			"           public String asString() { return null;}\n" +
			"		};\n" +
			"	}\n" +
			"}",
		},
		"",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// Recommendation from compiler to drop type arguments leads to compile error
public void testGH1506() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.Iterator;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"	public Iterable<File> getStackFramesClassesLocations(Object element) {\n" +
			"		return new Iterable<File>() {\n" +
			"			@Override\n" +
			"			public Iterator<File> iterator() {\n" +
			"				return Arrays.stream(new Object[0]) //\n" +
			"						.map(frame -> getClassesLocation(frame)) //\n" +
			"						.iterator();\n" +
			"			}\n" +
			"			\n" +
			"			File getClassesLocation(Object frame) {\n" +
			"				return null;\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}\n",
		},
		"",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// Recommendation from compiler to drop type arguments leads to compile error
public void testGH1506_2() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.Iterator;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"	public Iterable<File> getStackFramesClassesLocations(Object element) {\n" +
			"		return new Iterable<File>() {\n" +
			"			@Override\n" +
			"			public Iterator<File> iterator() {\n" +
			"				return Arrays.stream(new Object[0]) //\n" +
			"						.map(frame -> getClassesLocation(frame)) //\n" +
			"						.iterator();\n" +
			"			}\n" +
			"			\n" +
			"			private File getClassesLocation(Object frame) {\n" +
			"				return null;\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n"
		+ "1. ERROR in X.java (at line 8)\n"
		+ "	return new Iterable<File>() {\n"
		+ "	           ^^^^^^^^\n"
		+ "Redundant specification of type arguments <File>\n"
		+ "----------\n",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// Recommendation from compiler to drop type arguments leads to compile error
public void testGH1506_3() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<E> {\n" +
			"    static class AX<T>{}\n" +
			"    X(E e) {}\n" +
			"    X() {}\n" +
			"    public static void main(String[] args) {\n" +
			"    	X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { private void foo() {} });\n" +
			"	}\n" +
			"} \n",
		},
		"----------\n"
		+ "1. WARNING in X.java (at line 6)\n"
		+ "	X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { private void foo() {} });\n"
		+ "	            ^^\n"
		+ "X.AX is a raw type. References to generic type X.AX<T> should be parameterized\n"
		+ "----------\n"
		+ "2. WARNING in X.java (at line 6)\n"
		+ "	X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { private void foo() {} });\n"
		+ "	                                                                       ^^^^^\n"
		+ "The method foo() from the type new X.AX<String>(){} is never used locally\n"
		+ "----------\n",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// Recommendation from compiler to drop type arguments leads to compile error
public void testGH1506_4() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<E> {\n" +
			"    static class AX<T>{}\n" +
			"    X(E e) {}\n" +
			"    X() {}\n" +
			"    public static void main(String[] args) {\n" +
			"    	X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { public void foo() {} });\n" +
			"	}\n" +
			"} \n",
		},
		"----------\n"
		+ "1. WARNING in X.java (at line 6)\n"
		+ "	X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { public void foo() {} });\n"
		+ "	            ^^\n"
		+ "X.AX is a raw type. References to generic type X.AX<T> should be parameterized\n"
		+ "----------\n"
		+ "2. WARNING in X.java (at line 6)\n"
		+ "	X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { public void foo() {} });\n"
		+ "	                                                                      ^^^^^\n"
		+ "The method foo() from the type new X.AX<String>(){} is never used locally\n"
		+ "----------\n",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1560
// ECJ recommends diamond when using it would result in non-denotable types.
public void testGH1560() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
			import java.util.Collection;
			import java.util.List;

			public class X<S, D> {

				public interface MMenuElement {}
				public interface IObservable {}
				public static class ListDiffVisitor<E> {}
				public interface IObservablesListener {}
				public interface IObservableCollection<E> extends IObservable, Collection<E> {}
				public static class ObservableEvent {}
				public interface IDiff {}
				public static class ListDiff<E> implements IDiff {
					public void accept(ListDiffVisitor<? super E> visitor) {}
				}
				public static class ListChangeEvent<E> extends ObservableEvent {
					public ListDiff<E> diff;
				}
				public interface IListChangeListener<E> extends IObservablesListener {
					void handleListChange(ListChangeEvent<? extends E> event);
				}
				public interface IObservableList<E> extends List<E>, IObservableCollection<E> {
					void addListChangeListener(IListChangeListener<? super E> listener);
				}

				public void foo() {

					IObservableList<MMenuElement> l;

					l.addListChangeListener(event -> event.diff.accept(new ListDiffVisitor<MMenuElement>() {})); // <> should not be recommended here!!!

				}
			}
			""",
		},
		"----------\n"
		+ "1. ERROR in X.java (at line 30)\n"
		+ "	l.addListChangeListener(event -> event.diff.accept(new ListDiffVisitor<MMenuElement>() {})); // <> should not be recommended here!!!\n"
		+ "	^\n"
		+ "The local variable l may not have been initialized\n"
		+ "----------\n",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1560
// ECJ recommends diamond when using it would result in non-denotable types.
public void testGH1560_2() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
			import java.util.Collection;
			import java.util.List;

			public class X<S, D> {

				public interface MMenuElement {}
				public interface IObservable {}
				public static class ListDiffVisitor<E> {}
				public interface IObservablesListener {}
				public interface IObservableCollection<E> extends IObservable, Collection<E> {}
				public static class ObservableEvent {}
				public interface IDiff {}
				public static class ListDiff<E> implements IDiff {
					public void accept(ListDiffVisitor<? super E> visitor) {}
				}
				public static class ListChangeEvent<E> extends ObservableEvent {
					public ListDiff<E> diff;
				}
				public interface IListChangeListener<E> extends IObservablesListener {
					void handleListChange(ListChangeEvent<? extends E> event);
				}
				public interface IObservableList<E> extends List<E>, IObservableCollection<E> {
					void addListChangeListener(IListChangeListener<? super E> listener);
				}

				public void foo() {

					IObservableList<MMenuElement> l;

					l.addListChangeListener(event -> event.diff.accept(new ListDiffVisitor<>() {})); // non-denotable type error

				}
			}
			""",
		},
		"----------\n"
		+ "1. ERROR in X.java (at line 30)\n"
		+ "	l.addListChangeListener(event -> event.diff.accept(new ListDiffVisitor<>() {})); // non-denotable type error\n"
		+ "	                                                       ^^^^^^^^^^^^^^^\n"
		+ "Type X.ListDiffVisitor<capture#1-of ? extends X.MMenuElement> inferred for ListDiffVisitor<>, is not valid for an anonymous class with '<>'\n"
		+ "----------\n",
		null, true, options);
}
public void testGH2817() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"Test.java",
			"""
			class A<T> {}
			class B<T> {}

			public interface Test {
			    <T> void use(T t, B<? super T> b);
			    <T> B<A<? super T>> create();

			    default void test() {
			        use(new A<Object[]>(), create());
			    }
			}
			"""
		};
	runner.runConformTest();
}
public void testGH3501() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X.java",
			"""
			import java.util.Collections;
			public class X {
				void test() {
					Zork v = Collections.singleton("1");
				}
			}
			"""
		};
	runner.expectedCompilerLog =
			"""
			----------
			1. ERROR in X.java (at line 4)
				Zork v = Collections.singleton("1");
				^^^^
			Zork cannot be resolved to a type
			----------
			""";
	runner.runNegativeTest();
}
public void testGH3457() {
	runConformTest(new String[] {
		"Test.java",
		"""
		public class Test {
			public void test() {
				this.error(new TypeToken<A2<?>>() {});
			}

			public <T extends B1> void error(TypeToken<? extends A1<? extends T>> type) {}
			public static abstract class TypeToken<T> {}
			public static class A1<T extends B1> {}
			public static class A2<T extends B2> extends A1<T> {}

			public static class B1 {}
			public static class B2 extends B1 {}
		}
		"""
	});
}
public void testGH3457b() {
	runConformTest(new String[] {
		"QueryUtil.java",
		"""
		import java.util.ArrayList;
		import java.util.Collection;

		interface IQuery<T> { }

		public class QueryUtil {
			public static <T> IQuery<T> createCompoundQuery(IQuery<? extends T> query1, IQuery<T> query2, boolean and) {
				ArrayList<IQuery<? extends T>> queries = new ArrayList<>(2);
				queries.add(query1);
				queries.add(query2);
				return createCompoundQuery(queries, and);
			}
			public static <T> IQuery<T> createCompoundQuery(Collection<? extends IQuery<? extends T>> queries, boolean and) {
				return null;
			}
		}
		"""
	});
}
public void testGH3457c() {
	runConformTest(new String[] {
		"QueryUtil.java",
		"""
		import java.util.ArrayList;
		import java.util.Collection;
		import java.util.List;

		interface IQuery<T> extends List<T> { }

		public class QueryUtil {
			public static <T> IQuery<T> createCompoundQuery(IQuery<? extends T> query1, IQuery<T> query2, boolean and) {
				ArrayList<IQuery<? extends T>> queries = new ArrayList<>(2);
				queries.add(query1);
				queries.add(query2);
				return createCompoundQuery(queries, and);
			}
			public static <T> IQuery<T> createCompoundQuery(Collection<? extends List<? extends T>> queries, boolean and) {
				return null;
			}
		}
		"""
	});
}
public void testGH3948() {
	runConformTest(new String[] {
			"Foo.java",
			"""
			import java.util.Collections;
			import java.util.List;
			import java.util.function.BiConsumer;
			import java.util.function.Function;

			public class Foo {
			    private List<? extends Bar> value;

			    public void set(List<? extends Bar> value) {
			        this.value = value;
			    }

			    public List<Bar> get() {
			        return Collections.unmodifiableList(value);
			    }

			    public static void main(String[] args) {
			        test(Foo::get, Foo::set);
			    }

			    public static <U> void test(Function<Foo, List<U>> getter, BiConsumer<Foo, List<? extends U>> setter) {
			        // noop
			    }

			    public static interface Bar{}
			}
			"""
		});
}
public void testGH4022a() {
	runConformTest(new String[] {
			"Bug.java",
			"""
			import java.util.Collection;
			import java.util.Set;

			public abstract class Bug {
				// real code uses some guava method, so method signature  cannot be changed
				public abstract <T2> Collection<T2> toCollection(Iterable<? extends T2> elements);

				public abstract <T> Set<Class<? extends T>> getSubTypesAsSet(Class<T> type);

				public <T> Collection<Class<? extends T>> getSubTypesAsCollection(Class<T> superclassOrInterface)    {
					// compiles on older eclipse and javac 21
					// fails since d9d550f8ddeda45cf4d1b803a99afbd73abf57e4
					return toCollection(getSubTypesAsSet(superclassOrInterface));
				}

				public <T> Collection<Class<? extends T>> getSubTypesAsCollectionFixed(Class<T> superclassOrInterface) {
					// type as suggested by eclipse when using "extract local variable"
					Set<Class<? extends T>> subTypesAsSet = getSubTypesAsSet(superclassOrInterface);

					// compiles
					return toCollection(subTypesAsSet);
				}
			}
			"""
		});
}
public void testGH4022b() {
	runConformTest(new String[] {
			"OtherExample.java",
			"""
			import java.util.Set;
			import java.util.function.Function;

			interface TaskDefinition {
				public Set<String> getLabels();
			}

			abstract class FluentIterable<E> implements Iterable<E> {
				public abstract <T> FluentIterable<T> transformAndConcat(
						Function<? super E, ? extends Iterable<? extends T>> function);

				public abstract Set<E> toSet();
			}

			public abstract class OtherExample {
				public Set<String> getAllLabels(FluentIterable<TaskDefinition> from) {
			                // doesn't compile since d9d550f8ddeda45cf4d1b803a99afbd73abf57e4
					return from.transformAndConcat((TaskDefinition input) -> input.getLabels()).toSet();
				}
			}
			"""
		});
}
public void testGH4033() {
	runConformTest(new String[] {
			"Snippet.java",
			"""
			import java.util.Collection;
			import java.util.Iterator;

			public class Snippet {
				interface Apple {}
				interface Banana<T1, T2> {}
				interface Smoothie<T extends Apple, M extends Apple> extends Banana<T, String> {}

				public static void main(String[] args) {
					Collection<Smoothie<? extends Apple, ? extends Apple>> c = null;
					method(c);
				}

				static final <S extends Banana<? extends T, ?>, T> Iterator<T> method(
						Collection<S> c) {
					return null;
				}
			}
			"""
		});
}
public void testGH4039() {
	runConformTest(new String[] {
		"CollectionsSortReproducer.java",
		"""
		import java.util.Collection;
		import java.util.Collections;
		import java.util.Iterator;
		import java.util.List;

		public class CollectionsSortReproducer {
			class Cranberry<T_Value> implements Comparable<Cranberry<T_Value>> {
				@Override
				public int compareTo(Cranberry<T_Value> o) { return 0; }
			}

			public static void main(String[] args) {
				List<Cranberry<?>> l = Collections.emptyList();
			    Collections.sort(l);
			}
		}
		"""
	});
}

public void testGH4003() {
	if (this.complianceLevel < ClassFileConstants.JDK10)
		return; // uses 'var'
	runConformTest(new String[] {
		"EclipseCompilerBugReproducer.java",
		"""
		import java.util.List;
		import java.util.concurrent.CompletableFuture;
		import java.util.concurrent.CompletionStage;
		import java.util.function.Function;
		import java.util.function.Supplier;

		public class EclipseCompilerBugReproducer {

		    public static final class Mono<T> {

		        public static <T> Mono<T> fromFuture(CompletionStage<? extends T> stage) {
		            return new Mono<>();
		        }

		        public static <T> Mono<T> fromFuture(
		                Supplier<? extends CompletionStage<? extends T>> stageSupplier) {
		            return new Mono<>();
		        }

		        public <R> Mono<R> flatMap(Function<? super T, Mono<? extends R>> mapper) { return new Mono<>(); }
		        public <R> Mono<R> map(Function<? super T, ? extends R> mapper)          { return new Mono<>(); }
		        public List<T>      collectList()                                        { return List.of(); }
		        public T            block()                                              { return null; }
		    }

		    static final class Bucket {
		        CompletionStage<String> get(String id) {
		            return CompletableFuture.completedFuture("value-for-" + id);
		        }
		    }

		    /* ---------------------------------------------------------------------- */
		    /*  Reproducer                                                            */
		    /* ---------------------------------------------------------------------- */
		    public static void main(String[] args) {

		        var ids    = List.of("a", "b", "c");
		        var bucket = new Bucket();

		        ids.stream()
		           .flatMap(id ->
		               Mono.fromFuture(() -> bucket.get(id))   // <-- fails in Eclipse
		                   .map(String::toUpperCase)
		                   .collectList()
		                   .stream())
		           .forEach(System.out::println);
		    }
		}
		"""
	});
}

public void testGH4098() {
	runConformTest(new String[] {
		"ClassA.java",
		"""
		import java.util.Collections;
		import java.util.HashSet;
		import java.util.Set;

		public class ClassA {
		  public static void main(String[] args) {
		    Set<? super Integer> set = new HashSet<>(Set.of(1, 5));
		    System.out.println(Collections.unmodifiableSet(set));
		  }
		}
		"""
	});
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3907
// Compilation error on full build but not on incremental build due to @deprecated
public void testIssue3907() {
	runConformTest(new String[] {
		"LoadExtension.java",
		"""
		public class LoadExtension extends AbstractLoad<Integer> {
		}
		""",
		"TestIntegrationExtension.java",
		"""
		public final class TestIntegrationExtension implements Extension {

		}

		@ExtendWith(TestIntegrationExtension.class)
		@interface TestIntegration {
		}

		@interface ExtendWith {
			Class<? extends Extension>[] value();
		}

		interface Extension {
		}


		/**
		 * @deprecated
		 */
		@TestIntegration()
		@Deprecated
		abstract class AbstractLoad<T extends Number> {

		}
		"""
	});
}
public void testIssue3907_since() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"LoadExtension.java",
		"""
		public class LoadExtension extends AbstractLoad<Integer> {
		}
		""",
		"TestIntegrationExtension.java",
		"""
		public final class TestIntegrationExtension implements Extension {

		}

		@ExtendWith(TestIntegrationExtension.class)
		@interface TestIntegration {
		}

		@interface ExtendWith {
			Class<? extends Extension>[] value();
		}

		interface Extension {
		}


		/**
		 * @deprecated
		 */
		@TestIntegration()
		@Deprecated(since="13")
		abstract class AbstractLoad<T extends Number> {

		}
		"""
	};
	runner.expectedCompilerLog =
			"""
			----------
			1. WARNING in LoadExtension.java (at line 1)
				public class LoadExtension extends AbstractLoad<Integer> {
				                                   ^^^^^^^^^^^^
			The type AbstractLoad<Integer> is deprecated since version 13
			----------
			""";
	runner.runWarningTest();
}
public void testGH4308() {
	runConformTest(new String[] {
			"ClassA.java",
			"""
			import java.lang.reflect.InvocationTargetException;
			import java.util.HashSet;
			import java.util.Map;
			import java.util.Set;
			import java.util.function.Consumer;
			import java.util.stream.Collectors;

			public class ClassA {

				private static final Map<ClassB, Consumer<ClassC>> S;

				static {
					S = load(ClassB.class).stream().collect(Collectors.toMap(s -> s, // Compilation error
							s -> s.ping() ? s::sync : ClassD.getInstance()::replay));
				}

				private static <T> Set<T> load(Class<T> clazz) {
					Set<T> set = new HashSet<>();
					try {
						set.add(clazz.getConstructor().newInstance());
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
					return set;
				}

				private static class ClassB {
					public ClassB() {}
					boolean ping() {
						return false;
					}
					void sync(ClassC classC) { }
				}

				private static class ClassD {
					private static ClassD getInstance() {
						return new ClassD();
					}
					void replay(ClassC classC) {
						System.out.println("replay");
					}
				}

				private static class ClassC { }

				public static void main(String[] args) {
					S.values().forEach(consumer -> consumer.accept(new ClassC()));
				}
			}
			"""
		},
		"replay");
}
public void testGH4392() {
	runConformTest(new String[] {
			"AFactory.java",
			"""
			public abstract class AFactory<T> {

			    public <U extends T> U getProcess(Object object) {
			        return getProcess(object.getClass()); // Type mismatch: cannot convert from T to U
			    }

			    public abstract <U extends T> U getProcess(Class<?> classeObject);

			}
			"""});
}
public void testGH4402() {
	runConformTest(new String[] {
			"Main.java",
			"""
			import java.util.Collection;

			public class Main {

				void test() {
					ArrayNode results = new ArrayNode();

					// those first two compile fine
					assertThat(results, jsonArray(empty()));
					assertThat(results, jsonArray(contains(jsonObject().where("Id", jsonText("dataset-a")),
							jsonObject().where("Id", jsonText("dataset-b")))));
					/*
					 * this one fails compilation:
					 * The method jsonArray(Main.Matcher<? super Collection<? extends Main.JsonNode>>) in the type Main is not applicable for the arguments (Main.Matcher<Iterable<? extends capture#3-of ? extends Main.JsonNode>>)
					 *
					 * However it will compile fine if I comment out lines 12-13
					 */
					assertThat(results, jsonArray(contains(jsonObject().where("Id", jsonText("dataset-c")))));
					/*
					 * this one fails compilation:
					 * The method jsonArray(Main.Matcher<? super Collection<? extends Main.JsonNode>>) in the type Main is not applicable for the arguments (Main.Matcher<Iterable<? extends capture#3-of ? extends Main.JsonNode>>)
					 *
					 * However it will compile fine if I comment out lines 12-13 AND 20
					 */
					assertThat(results, jsonArray(contains(jsonObject().where("Id", jsonText("dataset-@1")),
							jsonObject().where("Id", jsonText("dataset-@2")))));
				}

				public static abstract class JsonNode { }
				public static class ArrayNode extends ContainerNode<ArrayNode> { }
				public static abstract class ContainerNode<T extends ContainerNode<T>> extends JsonNode { }
				public class ObjectNode extends ContainerNode<ObjectNode> { }
				public class TextNode extends JsonNode { }
				public interface Matcher<T> { }

				public static <E> Matcher<java.util.Collection<? extends E>> empty() { return null; }

				public static <T> void assertThat(T actual, Matcher<? super T> matcher) { }

				@SafeVarargs
				public static <E> Matcher<java.lang.Iterable<? extends E>> contains(Matcher<? super E>... itemMatchers) { return null; }

				public static Matcher<JsonNode> jsonArray(Matcher<? super Collection<? extends JsonNode>> elementsMatcher) { return null; }

				public static IsJsonObject jsonObject() { return null; }

				public static Matcher<JsonNode> jsonText(String text) { return null; }

				public static class IsJsonArray extends AbstractJsonNodeMatcher<ArrayNode> { }

				public static class IsJsonObject extends AbstractJsonNodeMatcher<ObjectNode> {
					public IsJsonObject where(String key, Matcher<? super JsonNode> valueMatcher) {
						return null;
					}
				}

				public static class IsJsonText extends AbstractJsonNodeMatcher<TextNode> { }
				public static abstract class AbstractJsonNodeMatcher<A extends JsonNode> implements Matcher<JsonNode> { }
			}
			"""
		},
		"");
}
public void testGH4346() {
	if (this.complianceLevel < ClassFileConstants.JDK16)
		return; // uses records
	runConformTest(new String[] {
			"X.java",
			"""
			import java.util.function.Function;
			public class X {
				public static <T> W1<W2<T>> main(String[] args) {
					return m(m2(), W2::t, W2::new);
				}

				public static <T1, T2> W1<T1> m(W1<T2> w1, Function<T1, T2> f1, Function<T2, T1> f2) {
					return null;
				}
				private static <T> W1<W1<T>> m2() {
					return null;
				}
				private record W1<T>(T t) {}
				private record W2<T>(W1<T> t) {}
			}
			"""
	});
}
public void testGH4498() {
	runConformTest(new String[] {
			"AFactory.java",
			"""
			public abstract class AFactory<T> {

			  public <U extends AFactory> U getProcess(Object object) {
			    return getProcess(object.getClass()); // Type mismatch: cannot convert from AFactory<capture#2-of ?> to U
			  }

			  public abstract <U extends AFactory<?>> U getProcess(Class<?> classeObject);

			}
			"""});
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4503
// [Mockito] Compilation error "Unhandled exception type Throwable"
public void testIssue4503_differs_from_javac() {
	runConformTest(new String[] {
			"X.java",
			"""
			public class X {

				interface RetryCallback<T, E extends Throwable> {}

				interface OngoingStubbing<T> {}

			    public void test() {
			        when(execute(any()));   // <------ Unhandled exception type Throwable
			    }

			    public static <T> T any() {
			        return null;
			    }

			    public static final <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E {
					return null;
				}

			    public static <T> OngoingStubbing<T> when(T methodCall) {
			        return null;
			    }
			}
			"""});
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4503
// [Mockito] Compilation error "Unhandled exception type Throwable"
public void testIssue4503_matches_with_javac() {
	runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

				interface RetryCallback<T, E extends Throwable> {}

				interface OngoingStubbing<T> {}

			    public void test() {
			        when(execute((RetryCallback<java.lang.Object,java.lang.Throwable>) null));   // <------ Unhandled exception type Throwable
			    }

			    public static <T> T any() {
			        return null;
			    }

			    public static final <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E {
					return null;
				}

			    public static <T> OngoingStubbing<T> when(T methodCall) {
			        return null;
			    }
			}
			"""},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\r\n" +
			"	when(execute((RetryCallback<java.lang.Object,java.lang.Throwable>) null));   // <------ Unhandled exception type Throwable\r\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Unhandled exception type Throwable\n" +
			"----------\n");
}
public void testGH4715() {
	runConformTest(new String[] {
		"TestMain.java",
		"""
		import java.util.Map;
		import java.util.Map.Entry;
		import java.util.Set;
		import java.util.stream.Collectors;

		public class TestMain {
			public static void main(String[] args) {
				final Map<String, Set<String>> pathToPIDMap = Map.ofEntries(
						Map.entry("i1", Set.of("PID1")),
						Map.entry("i1,i2", Set.of("PID2"))
						);

				final Map<Boolean, Map<String, Set<String>>> partitiionedPathToPIDSet = pathToPIDMap.entrySet().stream() // Error 3
					.collect(Collectors.partitioningBy(entry -> {
						final Set<String> pidSet = entry.getValue();
						final boolean isSinglePIDSet = pidSet.size() == 1;
						return isSinglePIDSet;
					}, Collectors.mapping(entry -> {
						final String path = entry.getKey();
						final Set<String> pidSet = entry.getValue();

						return Map.entry(path, pidSet);
					}, Collectors.toMap(Entry::getKey, Entry::getValue)))); //Errors 1 and 2
			}
		}

		"""
	});
}
public void testGH4533() {
	runConformTest(new String[] {
		"X.java",
		"""
		import java.util.function.Supplier;

		public class X implements Foo {

			private final Foo delegate;
			private final Helper helper;

			public X(Helper helper, Foo delegate) {
				this.helper = helper;
				this.delegate = delegate;
			}

			@Override
			public <T> void setProperty(T value) {
				helper.run(() -> delegate.setProperty(value));  // Problem detected during type inference: expression has no value
			}
		}

		interface Foo {
			public <T> void setProperty(T value);
		}

		interface Helper {
			public void run(Runnable runnable); // comment out this to see a different error
			public <R> R run(Supplier<R> supplier);
		}
		"""
	});
}
public void testGH4281() {
	runConformTest(new String[] {
		"InferenceTest.java",
		"""
		public class InferenceTest {

			public static class A<I extends C<I, ?>> {}

			public static class B<I extends C<I, ?>, J> {
				public B(A<I> a) {}
			}

			public static class C<I extends C<I, J>, J> {}

			public static void test(A<?> a) {
				new B<>(a);
			}
		}
		"""
	});
}
public void testGH1501() {
	runNegativeTest(new String[] {
			"Test.java",
			"""
			import java.util.Map;
			import java.util.function.Function;

			public class Test {
			  public Outer<Data> errorInLambda() {
			    Outer<Data> x = new Outer<>(new InnerImpl<>(Map.of()));
			    // the above one works but on mouse over InnerImpl produces:
			    // InnerImpl.InnerImpl<Object>(Map<String, Function<Object, Object>> map)
			    // should be:
			    // InnerImpl.InnerImpl<Data>(Map<String, Function<Data, Object>> map)

			    Outer<Data> y = new Outer<>(new InnerImpl<Object>(Map.of()));
			    // but this one correctly errors with:
			    // Cannot infer type arguments for Outer<>

			    return new Outer<>(new InnerImpl<>(Map.of("x",
			        data -> data.getValue() // Eclipse error:
			        // This lambda expression must return a result of type Object
			        // javac correctly infers Data to both diamonds here and no errors produced
			        )));
			  }
			}

			class Outer<T> {
			  final Inner<T> inner;

			  Outer(Inner<T> inner) {
			      this.inner = inner;
			  }
			}

			interface Inner<T> {}

			class InnerImpl<T> implements Inner<T> {
			  InnerImpl(Map<String, Function<T, Object>> map) {}
			}

			class Data {
			  String value;

			  Data(String value) {
			      this.value = value;
			  }

			  String getValue() {
			      return value;
			  }
			}
			"""
		},
		"""
		----------
		1. ERROR in Test.java (at line 12)
			Outer<Data> y = new Outer<>(new InnerImpl<Object>(Map.of()));
			                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Cannot infer type arguments for Outer<>
		----------
		""");
}
public void testGH4463() {
	runConformTest(new String[] {
		"A.java",
		"""
		interface A<T> {}
		interface B<T> extends A<T> {}
		interface C<T> extends A<T> {}

		class BC1 implements B<String>, C<String> {}
		class BC2 implements B<Integer>, C<Integer> {}

		// Eclipse Compile error "The interface A cannot be implemented more than once with different arguments: A<?> and A<?>"
		interface IHelperBC<T extends B<?> & C<?>> {}

		class HelperBC1 implements IHelperBC<BC1> {}
		class HelperBC2 implements IHelperBC<BC2> {}
		"""
	});
}
public void testGH4463b() {
	runConformTest(new String[] {
		"A.java",
		"""
		interface A<T> {}
		interface B<T> extends A<T> {}
		interface C<T> extends A<T> {}

		class BC1 implements B<String>, C<String> {}
		class BC2 implements B<Integer>, C<Integer> {}

		// eclipse compile error "The interface A cannot be implemented more than once with different arguments: A<? super T> and A<? super T>"
		interface IHelperBCSuper<T, BC extends B<? super T> & C<? super T>> {}
		// eclipse compile error "The interface A cannot be implemented more than once with different arguments: A<? extends T> and A<? extends T>"
		interface IHelperBCExtends<T, BC extends B<? extends T> & C<? extends T>> {}
		"""
	});
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4550
// Static interface methods excluded from type variable membership
public void testIssue4550() {
	runNegativeTest(new String[] {
			"Tester.java",
			"""
			public class Tester<T extends Thing> {
			    public void test() {
			        System.out.println("Testing: " + T.getStuff());  // Error is here
			    }

			    public static void main(String[] args) {
			        Tester<OtherThing> tester = new Tester<>();
			        tester.test();
			    }

			}

			interface Thing {
			    static String getStuff() {
			        return "Stuff";
			    }
			}

			class OtherThing implements Thing {
			}
			""",
	    },
		"----------\n" +
		"1. ERROR in Tester.java (at line 3)\n" +
		"	System.out.println(\"Testing: \" + T.getStuff());  // Error is here\n" +
		"	                                   ^^^^^^^^\n" +
		"The method getStuff() is undefined for the type T\n" +
		"----------\n"
		);
}

public void testGH4635() {
	runConformTest(new String[] {
		"Test.java",
		"""
		import java.util.stream.Stream;
		import java.util.Objects;
		public class Test {
		    static class Obj<T> {
		    }

		    public static void main(String[] args) {
		        String arg = "";

		        Stream<Obj<?>> stream11 = Stream.of(newObj());
		        Stream<Obj<?>> stream21 = Stream.of(newObj2(arg));

		        Stream<Obj<?>> stream12 = Stream.of(newObj(), newObj());
		        Stream<Obj<?>> stream22 = Stream.of(newObj2(arg), newObj2(arg));

		        //Stream<Obj<?>> stream11f = Stream.of(newObj()).filter(Objects::nonNull); // javac & eclipse KO
		        //Stream<Obj<?>> stream21f = Stream.of(newObj2(arg)).filter(Objects::nonNull); // javac & eclipse KO

		        Stream<Obj<?>> stream12f = Stream.of(newObj(), newObj()).filter(Objects::nonNull);
		        Stream<Obj<?>> stream22f = Stream.of(newObj2(arg), newObj2(arg)).filter(Objects::nonNull); // javac OK, eclipse KO
		    }

		    public static Obj<?> newObj() {
		        return new Obj<>();
		    }

		    public static <T> Obj<?> newObj2(T arg) {
		        return new Obj<>();
		    }
		}
		"""

	});
}
public void testGH4604() {
	runConformTest(new String[] {
			"X.java",
			"""
			import java.util.concurrent.*;
			import java.util.stream.*;
			public class X {
				public static void main(String[] args) {
					CompletableFuture.allOf(Stream.of(1)
						.map(value -> future(value))
						.toArray(CompletableFuture[]::new));
				}

				public static <T> CompletableFuture<?> future(T t) {
					return CompletableFuture.completedFuture(t);
				}
			}
			"""
		});
}
public void testGH4699_1() {
	if (this.complianceLevel < ClassFileConstants.JDK10) return; // uses 'var'
	runConformTest(new String[] {
			"EclipseBug.java",
			"""
			public class EclipseBug {
				void error1() {
					var someObject = getObject(); // <<--- Compiler complains here
				}
				private SomeObject<? extends SomeType<?, ? extends SpecialLocation>, ? extends SpecialLocation, ?> getObject() {
					return null;
				}
				static interface SomeLocation { }
				static interface SpecialLocation extends SomeLocation { }

				static interface SomeType<O extends SomeObject<? extends SomeType<?, L>, L, ? extends SomeObject<?, ?, ?>>, L extends SomeLocation> { }

				public interface SomeObject<T extends SomeType<?, L>, L extends SomeLocation, P extends SomeObject<?, ?, ?>> { }
			}
			"""
		});
}
public void testGH4699_full() {
	if (this.complianceLevel < ClassFileConstants.JDK10) return; // uses 'var'
	runConformTest(new String[] {
			"EclipseBug.java",
			"""
			public class EclipseBug {

				void error1() {
					var someObject = getObject();
				}

				void error2(SomeObject<? extends SomeType<?, ? extends SpecialLocation>, ? extends SpecialLocation, ?> theObject) {
					method(theObject);
				}

				void error3(SomeObject<? extends SomeType<?, ? extends SpecialLocation>, ? extends SpecialLocation, ?> theObject) {
					SomeObject<? extends SomeType<?, ? extends SpecialLocation>, ? extends SpecialLocation, ?> theObject2 = theObject;
				}

				void method(SomeObject<? extends SomeType<?, ? extends SpecialLocation>, ? extends SpecialLocation, ?> theObject) { }

				private SomeObject<? extends SomeType<?, ? extends SpecialLocation>, ? extends SpecialLocation, ?> getObject() {
					return null;
				}

				static interface SomeLocation { }
				static interface SpecialLocation extends SomeLocation { }
				static interface SomeType<O extends SomeObject<? extends SomeType<?, L>, L, ? extends SomeObject<?, ?, ?>>, L extends SomeLocation> { }
				public interface SomeObject<T extends SomeType<?, L>, L extends SomeLocation, P extends SomeObject<?, ?, ?>> { }
			}
			"""
		});
}

public void testGH4810() {
	runConformTest(new String[] {
			"Repro.java",
			"""
			import java.util.concurrent.Callable;
			public class Repro {
				Object test() {
					try {
						return myMethod(new MyCallable<>() {
							@Override
							public Object call() {
								return new Object();
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}
				<T, E extends Exception> T myMethod(MyCallable<T, E> callable) throws E {
					return callable.call();
				}
			}
			interface MyCallable<U, F extends Exception> extends Callable<U> {
				@Override
				U call() throws F;
			}
			"""
	});
}

public static Class<GenericsRegressionTest_9> testClass() {
	return GenericsRegressionTest_9.class;
}

}
