/*******************************************************************************
 * Copyright (c) 2015, 2021 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 446691 - [1.8][null][compiler] NullPointerException in SingleNameReference.analyseCode
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
@SuppressWarnings({ "rawtypes" })
public class LambdaRegressionTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test572873a", "test572873b"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public LambdaRegressionTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test001() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"import java.util.Map;\n" +
				"import java.util.function.Function;\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().run();\n" +
				"  }\n" +
				"  public void run() {\n" +
				"    class Inner {\n" +
				"      public Inner() {\n" +
				"        System.out.println(\"miep\");\n" +
				"      }\n" +
				"    }\n" +
				"    Map<String, Inner> map = new HashMap<>();\n" +
				"    Function<String, Inner> function = (name) -> {\n" +
				"      Inner i = map.get(name);\n" +
				"      if (i == null) {\n" +
				"        i = new Inner();\n" +
				"        map.put(name, i);\n" +
				"      }\n" +
				"      return i;\n" +
				"\n" +
				"    };\n" +
				"    function.apply(\"test\");\n" +
				"  }\n" +
				"}\n",
			},
			"miep"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.function.Consumer;\n" +
				"@SuppressWarnings(\"all\")\n" +
				"public class X {\n" +
				"  private final String text = \"Bug?\";\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doIt();\n" +
				"  }\n" +
				"  private void doIt() {\n" +
				"    new Sub();\n" +
				"  }\n" +
				"  private class Super<T> {\n" +
				"    public Super(Consumer<T> consumer) {\n" +
				"    }\n" +
				"  }\n" +
				"  private class Sub extends Super<String> {\n" +
				"    public Sub() {\n" +
				"      super(s -> System.out.println(text));\n" +
				"      // super(s -> System.out.println(\"miep\"));\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.function.Consumer;\n" +
				"@SuppressWarnings(\"all\")\n" +
				"public class X {\n" +
				"  private final String text = \"Bug?\";\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doIt();\n" +
				"  }\n" +
				"  private void doIt() {\n" +
				"    new Sub();\n" +
				"  }\n" +
				"  private class Super<T> {\n" +
				"    public Super(Consumer<T> consumer) {\n" +
				"    }\n" +
				"  }\n" +
				"  private class Sub extends Super<String> {\n" +
				"    public Sub() {\n" +
				"       super(s -> System.out.println(\"miep\"));\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test004() {
	this.runConformTest(
		false,
		JavacHasABug.JavacThrowsAnException,
		new String[] {
			"Y.java",
			"import java.util.function.Supplier;\n" +
			"class E {\n" +
			"	E(Supplier<Object> factory) { }\n" +
			"}\n" +
			"public class Y extends E {\n" +
			"	Y() {\n" +
			"		super( () -> {\n" +
			"			class Z extends E {\n" +
			"				Z() {\n" +
			"					super(() -> new Object());\n" +
			"				}\n" +
			"			}\n" +
			"			return null;\n" +
			"			});\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new Y();\n" +
			"	}\n" +
			"}"
	},
	null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448724, [1.8] [compiler] Wrong resolution of overloaded method when irrelevant type parameter is present and lambda is used as parameter
public void test448724() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.concurrent.Callable;\n" +
			"public class X {\n" +
			"	public void mismatchRunnableCallable() throws Exception {\n" +
			"		//Resolves to case1(Runnable) method invocation; lambda with block\n" +
			"		case1(() -> {\"abc\".length();});\n" +
			"		//Resolves to case1(Callable) method invocation, resulting in type mismatch; block removed - lambda with expression\n" +
			"                case1(() -> \"abc\".length());\n" +
			"	}\n" +
			"	public void noSuchMismatch() throws Exception {\n" +
			"		//no difference to case1 \n" +
			"		case2(() -> {\"abc\".length();});\n" +
			"		//the only difference to case 1 is the missing irrelevant <T> type parameter. Properly resolves to case2(Runnable) here\n" +
			"		case2(() -> \"abc\".length());\n" +
			"	}\n" +
			"	public void case1(final Runnable r) {\n" +
			"		System.out.println(\"case1: Runnable\");\n" +
			"	}\n" +
			"	public <T> void case1(Callable<Boolean> c) {\n" +
			"		System.out.println(\"case1: Callable\");\n" +
			"	}\n" +
			"	public void case2(final Runnable supplier) {\n" +
			"		System.out.println(\"case2: Runnable\");\n" +
			"	}\n" +
			"	public void case2(Callable<Boolean> conditionEvaluator) {\n" +
			"		System.out.println(\"case2: Callable\");\n" +
			"	}\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		new X().mismatchRunnableCallable();\n" +
			"		new X().noSuchMismatch();\n" +
			"	}\n" +
			"}\n"
	},
	"case1: Runnable\n" +
	"case1: Runnable\n" +
	"case2: Runnable\n" +
	"case2: Runnable");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static <T, U, V> T foo(T t, U u, V v) {\n" +
			"       System.out.println(\"Wrong!\");\n" +
			"       return null;\n" +
			"   }\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"       return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", (u, v) -> v));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static <T, U, V> T foo(T t, U u, I<T, U, V> i) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", (u, v) -> v));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"----------\n" +
	"1. ERROR in X.java (at line 9)\n" +
	"	String s = goo(foo(\"String\", \"String\", (u, v) -> v));\n" +
	"	                                                 ^\n" +
	"Type mismatch: cannot convert from Object to String\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static String goo(String s, String s2) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", X::goo));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static String goo(String s, String s2) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> T foo(T t, U u, V v) {\n" +
			"       System.out.println(\"Wrong!\");\n" +
			"       return null;\n" +
			"   }\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", X::goo));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767d() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static String goo(String s, String s2) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> T foo(T t, U u, V v) {\n" +
			"        System.out.println(\"Wrong!\");\n" +
			"        return null;\n" +
			"   }\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", X::goo));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449410, [1.8][compiler] Eclipse java compiler does not detect a bad return type in lambda expression
public void test449410() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Collections;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    Collections.emptyMap()\n" +
			"        .entrySet()\n" +
			"        .forEach(entry -> test() ? bad() : returnType());\n" +
			"  }\n" +
			"  private static boolean test() {\n" +
			"    return (System.currentTimeMillis() & 0x1) == 0;\n" +
			"  }\n" +
			"  private static void returnType() {\n" +
			"  }\n" +
			"  private static void bad() {\n" +
			"  }\n" +
			"}\n"
	},
	"----------\n" +
	"1. ERROR in X.java (at line 6)\n" +
	"	.forEach(entry -> test() ? bad() : returnType());\n" +
	"	 ^^^^^^^\n" +
	"The method forEach(Consumer<? super Map.Entry<Object,Object>>) in the type Iterable<Map.Entry<Object,Object>> is not applicable for the arguments ((<no type> entry) -> {})\n" +
	"----------\n" +
	"2. ERROR in X.java (at line 6)\n" +
	"	.forEach(entry -> test() ? bad() : returnType());\n" +
	"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
	"Invalid expression as statement\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449824, [1.8] Difference in behaviour with method references and lambdas
// Captures present behavior - may not be correct.
public void test449824() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    Concrete<Target> fl = new Concrete<Target>();\n" +
			"    fl.call(each -> each.doSomething()); // fails\n" +
			"    fl.call((Target each) -> each.doSomething()); // fails\n" +
			"    fl.call(Target::doSomething); // succeeds in Eclipse 4.5M3 and 4.4.1\n" +
			"    // but fails in Java 8 1.8.0_11\n" +
			"  }\n" +
			"  public static class Target {\n" +
			"    public void doSomething() {\n" +
			"    }\n" +
			"  }\n" +
			"  public static class Concrete<T> implements Left<T>, Right<T> {\n" +
			"    public void call(RightHand<? super T> p) {\n" +
			"    }\n" +
			"  }\n" +
			"  public interface Left<T> {\n" +
			"    default void call(LeftHand<? super T> p) {\n" +
			"    }\n" +
			"  }\n" +
			"  public interface LeftHand<T> {\n" +
			"    public void left(T t);\n" +
			"  }\n" +
			"  public interface Right<T> {\n" +
			"    public void call(RightHand<? super T> p);\n" +
			"  }\n" +
			"  public interface RightHand<T> {\n" +
			"    public void right(T t);\n" +
			"  }\n" +
			"}\n"
	},
	"----------\n" +
	"1. ERROR in X.java (at line 4)\n" +
	"	fl.call(each -> each.doSomething()); // fails\n" +
	"	   ^^^^\n" +
	"The method call(X.RightHand<? super X.Target>) is ambiguous for the type X.Concrete<X.Target>\n" +
	"----------\n" +
	"2. ERROR in X.java (at line 6)\n" +
	"	fl.call(Target::doSomething); // succeeds in Eclipse 4.5M3 and 4.4.1\n" +
	"	   ^^^^\n" +
	"The method call(X.RightHand<? super X.Target>) is ambiguous for the type X.Concrete<X.Target>\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448954, [1.8][compiler] Suspect error: "The method foo(String, String, X::goo) is undefined for the type X"
public void test448954() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static String goo(String s, String s2) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> T foo(T t, U u, J j) {\n" +
			"		System.out.println(\"Wrong!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", X::goo));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"		return t;\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450380, [1.8][compiler] NPE in Scope.getExactConstructor(..) for bad constructor reference
public void test450380() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.function.IntFunction;\n" +
			"public class X {\n" +
			"    IntFunction<ArrayList<String>> noo() {\n" +
			"        return System::new;\n" +
			"    }\n" +
			"}\n"
	},
	"----------\n" +
	"1. ERROR in X.java (at line 5)\n" +
	"	return System::new;\n" +
	"	       ^^^^^^^^^^^\n" +
	"The type System does not define System(int) that is applicable here\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450604, [1.8] CCE at InferenceContext18.getParameter line 1377
public void test450604() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"import java.util.List;\n" +
			"import java.util.function.Function;\n" +
			"public class X<T, E extends Exception> {\n" +
			"	public static <T> List<T> of(T one) { return null; }\n" +
			"	public @SafeVarargs static <T> List<T> of(T... items) { return null; }\n" +
			"	public static void printDependencyLoops() throws IOException {\n" +
			"		Function<? super String, ? extends List<String>> mapping = X::of;\n" +
			"	}\n" +
			"}\n"
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450604, [1.8] CCE at InferenceContext18.getParameter line 1377
public void test450604a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static <T> List<T> of() { return null; }\n" +
			"	public static @SafeVarargs <T> List<T> of(T... values) { return null; }\n" +
			"	static void walkAll() {\n" +
			"		X.<String> of();\n" +
			"	}\n" +
			"}\n"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=451677, [1.8][compiler] missing type inference
public void _test451677() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.function.Function;\n" +
			"public class X {\n" +
			"	public static void test() {\n" +
			"		operationOnCreated(create(123, size -> new ArrayList<Integer>(size)), l -> l.size()); // works with: (ArrayList<Integer> l) -> l.size()\n" +
			"	}\n" +
			"	public static <R, A> R create(A arg, Function<A, R> factory) {\n" +
			"		return factory.apply(arg);\n" +
			"	}\n" +
			"	public static <R, A> R operationOnCreated(A created, Function<A, R> function) {\n" +
			"		return function.apply(created);\n" +
			"	}\n" +
			"}\n"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=451840
// [1.8] java.lang.BootstrapMethodError when running code with constructor reference
public void testBug451840() {
	runNegativeTest(new String [] {
		"X.java",
		"public class X {\n" +
		"    public static void main(String[] args) {\n" +
		"    	X test = new X();\n" +
		"    	MySupplier<X> s = test::new; // incorrect\n" +
		"    }\n" +
		"    public interface MySupplier<T> {\n" +
		"        T create();\n" +
		"    }\n" +
		"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	MySupplier<X> s = test::new; // incorrect\n" +
		"	                  ^^^^\n" +
		"test cannot be resolved to a type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448556
// [1.8][compiler] Invalid compiler error about effectively final variable outside the context of a lambda.
public void testBug4448556() {
	this.runConformTest(new String [] {
		"X.java",
		"import java.io.Serializable;\n" +
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"public class X {\n" +
		"    private static final List<Integer> INTEGERS = Arrays.asList(1, 2, 3, 4);\n" +
		"    public static void main(String[] args) {\n" +
		"        for (int i = 0; i < INTEGERS.size(); i++) {\n" +
		"            MyPredicate<Integer> predicate = INTEGERS.get(i)::equals;\n" +
		"        }\n" +
		"    }  \n" +
		"    public interface MyPredicate<T> extends Serializable {\n" +
		"        boolean accept(T each);\n" +
		"    }\n" +
		"}"
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448556
// [1.8][compiler] Invalid compiler error about effectively final variable outside the context of a lambda.
public void testBug4448556a() {
	this.runConformTest(new String [] {
		"X.java",
		"import java.io.Serializable;\n" +
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"public class X {\n" +
		"	int value = 0; \n" +
		"    private static final List<Integer> INTEGERS = Arrays.asList(1, 2, 3, 4);\n" +
		"    public Integer next() {\n" +
		"    	return new Integer(++value);\n" +
		"    }\n" +
		"    public static void main(String[] args) {\n" +
		"    	X t = new X();\n" +
		"        MyPredicate<Integer> predicate = t.next()::equals;\n" +
		"        System.out.println(\"Value \" + t.value + \" accept \" + predicate.accept(t.value));\n" +
		"    }\n" +
		"    public interface MyPredicate<T> extends Serializable {\n" +
		"        boolean accept(T each);\n" +
		"    }\n" +
		"}"
	},
	"Value 1 accept true");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=453687
// [1.8][compiler]Incorrect errors when compiling code with Method References
public void testBug453687() {
	this.runConformTest(new String [] {
		"X.java",
		"import static java.util.stream.Collectors.groupingBy;\n" +
		"import static java.util.stream.Collectors.mapping;\n" +
		"import static java.util.stream.Collectors.toSet;\n" +
		"import java.util.Locale;\n" +
		"import java.util.Map;\n" +
		"import java.util.Set;\n" +
		"import java.util.stream.Stream;\n" +
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		Map<String, Set<String>> countryLanguagesMap = Stream.of(Locale.getAvailableLocales()).collect(\n" +
		"				groupingBy(Locale::getDisplayCountry, mapping(Locale::getDisplayLanguage, toSet())));\n" +
		"	}\n" +
		"} "
	},
	"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=456481 - [1.8] VerifyError on constructor reference inside lambda
public void testBug456481() {
	this.runConformTest(new String [] {
		"Test.java",
		"public class Test  {\n" +
		"    interface Constructor {\n" +
		"        MyTest execute();\n" +
		"    }\n" +
		"    interface ArrayConstructor {\n" +
		"    	MyTest[] execute(int no);\n" +
		"    }\n" +
		"    interface ParameterizedConstructor {\n" +
		"    	MyParameterizedTest<String> execute();\n" +
		"    }\n" +
		"    class MyTest {\n" +
		"        MyTest() { System.out.println(\"Constructor executed\"); }\n" +
		"    }\n" +
		"    class MyParameterizedTest<T> {\n" +
		"    	MyParameterizedTest() {\n" +
		"    		System.out.println(\"Parameterized Constructor executed\");\n" +
		"    	}\n" +
		"    }\n" +
		"    public Constructor getConstructor() {\n" +
		"        return getConstructor(() -> { return MyTest::new; });\n" +
		"    }\n" +
		"    public MyTest[] getArray(int no) {\n" +
		"    	return new MyTest[no];\n" +
		"    }\n" +
		"    ArrayConstructor getArrayConstructor() {\n" +
		"    	return getArrayConstructor(() -> {return MyTest[]::new;});\n" +
		"    }\n" +
		"    ParameterizedConstructor getParameterizedConstructor() {\n" +
		"    	return getParameterizedConstructor(() -> {return MyParameterizedTest<String>::new;});\n" +
		"    }\n" +
		"    ArrayConstructor getArrayConstructor(ArrayWrapper w) {\n" +
		"    	return w.unwrap();\n" +
		"    }\n" +
		"    public static void main(String argv[]) {\n" +
		"        Test t = new Test();\n" +
		"        MyTest mytest = t.getConstructor().execute();\n" +
		"        MyTest[] array = t.getArrayConstructor().execute(2);\n" +
		"        MyParameterizedTest<String> pt = t.getParameterizedConstructor().execute();\n" +
		"    }\n" +
		"    ParameterizedConstructor getParameterizedConstructor(PTWrapper ptw) {\n" +
		"    	return ptw.unwrap();\n" +
		"    }\n" +
		"    Constructor getConstructor(Wrapper arg) {\n" +
		"        return arg.unwrap();\n" +
		"    }\n" +
		"    interface PTWrapper {\n" +
		"    	ParameterizedConstructor unwrap();\n" +
		"    }\n" +
		"    interface ArrayWrapper {\n" +
		"    	ArrayConstructor unwrap();\n" +
		"    }\n" +
		"    interface Wrapper {\n" +
		"        Constructor unwrap();\n" +
		"    }\n" +
		"}"
	},
	"Constructor executed\n" +
	"Parameterized Constructor executed");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=457007, VerifyError
public void testBug457007() {
	this.runConformTest(new String [] {
		"Test.java",
		"public class Test {\n" +
		"	void method() {\n" +
		"  		class Bar {}\n" +
		"  		java.util.function.Function<String, Bar> f = str -> new Bar();\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		System.out.println(\"done\");\n" +
		"	}\n" +
		"}"
	},
	"done");
}
public void testBug446691_comment5() {
	runConformTest(new String [] {
		"Test.java",
		"import java.util.*;\n" +
		"\n" +
		"public class Test {\n" +
		"  protected final Integer myInt;\n" +
		"\n" +
		"  public Test() {\n" +
		"    myInt = Integer.valueOf(0);\n" +
		"    try {\n" +
		"      Optional.empty().orElseThrow(() -> new IllegalArgumentException(myInt.toString()));\n" +
		"    } catch (IllegalArgumentException e) {\n" +
		"      throw new RuntimeException();\n" +
		"    }\n" +
		"    return;\n" +
		"  }\n" +
		"}\n"
	});
}
public void testBug446691_comment8() {
	runConformTest(new String [] {
		"Boom.java",
		"public class Boom {\n" +
		"  private final String field;\n" +
		"  public Boom(String arg) {\n" +
		"    this.field = arg;\n" +
		"    try {\n" +
		"      java.util.function.Supplier<String> supplier = () -> field;\n" +
		"    } catch (Exception e) {\n" +
		"      \n" +
		"    }\n" +
		"  }\n" +
		"}\n"
	});
}
public void testBug446691_comment14() {
	runNegativeTest(new String [] {
		"test/Main.java",
		"package test;\n" +
		"\n" +
		"import java.util.logging.Logger;\n" +
		"\n" +
		"public class Main {\n" +
		"\n" +
		"	private static final Logger LOG = Logger.getLogger(\"test\");\n" +
		"	private final static String value;\n" +
		"\n" +
		"	static {\n" +
		"		try {\n" +
		"			LOG.info(() -> String.format(\"Value is: %s\", value));\n" +
		"		} catch (final Exception ex) {\n" +
		"			throw new ExceptionInInitializerError(ex);\n" +
		"		}\n" +
		"	}\n" +
		"}"
	},
	"----------\n" +
	"1. ERROR in test\\Main.java (at line 8)\n" +
	"	private final static String value;\n" +
	"	                            ^^^^^\n" +
	"The blank final field value may not have been initialized\n" +
	"----------\n" +
	"2. ERROR in test\\Main.java (at line 12)\n" +
	"	LOG.info(() -> String.format(\"Value is: %s\", value));\n" +
	"	                                             ^^^^^\n" +
	"The blank final field value may not have been initialized\n" +
	"----------\n");
}
// error in lambda even if field is assigned later
public void testBug446691_comment14b() {
	runNegativeTest(new String [] {
		"test/Main.java",
		"package test;\n" +
		"\n" +
		"import java.util.logging.Logger;\n" +
		"\n" +
		"public class Main {\n" +
		"\n" +
		"	private static final Logger LOG = Logger.getLogger(\"test\");\n" +
		"	private final static String value;\n" +
		"\n" +
		"	static {\n" +
		"		try {\n" +
		"			LOG.info(() -> String.format(\"Value is: %s\", value));\n" +
		"		} catch (final Exception ex) {\n" +
		"			throw new ExceptionInInitializerError(ex);\n" +
		"		}\n" +
		"		value = \"\";" +
		"	}\n" +
		"}"
	},
	"----------\n" +
	"1. ERROR in test\\Main.java (at line 12)\n" +
	"	LOG.info(() -> String.format(\"Value is: %s\", value));\n" +
	"	                                             ^^^^^\n" +
	"The blank final field value may not have been initialized\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=463526
// Parenthesis are incorrectly allowed in lambda when LambdaBody is an expression statement
public void testBug463526() {
	runNegativeTest(new String [] {
		"Test.java",
		"public class Test {\n" +
		"    public static void main(String[] args) {\n" +
		"        Receiver r = new Receiver();\n" +
		"        r.accept((l) -> (doItOnTheClass(new Object())));\n" +
		"    }\n" +
		"    public static void doItOnTheClass(Object o) {\n" +
		"        System.out.println(\"done it\");\n" +
		"    }\n" +
		"    public static class Receiver {\n" +
		"        public void accept(Listener l) {\n" +
		"            l.doIt(new Object());\n" +
		"        }\n" +
		"    }\n" +
		"    public static interface Listener {\n" +
		"        public void doIt(Object o);\n" +
		"    }\n" +
		"}"
	},
	"----------\n" +
	"1. ERROR in Test.java (at line 4)\n" +
	"	r.accept((l) -> (doItOnTheClass(new Object())));\n" +
	"	  ^^^^^^\n" +
	"The method accept(Test.Listener) in the type Test.Receiver is not applicable for the arguments ((<no type> l) -> {})\n" +
	"----------\n" +
	"2. ERROR in Test.java (at line 4)\n" +
	"	r.accept((l) -> (doItOnTheClass(new Object())));\n" +
	"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
	"Invalid expression as statement\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=463526
// Parenthesis are incorrectly allowed in lambda when LambdaBody is an expression statement
public void testBug463526b() {
	runNegativeTest(new String [] {
		"Test.java",
		"import java.util.function.Consumer;\n" +
		"public class Test {\n" +
		"    public static void main(String[] args) {\n" +
		"        Receiver r = new Receiver();\n" +
		"        r.process((o) -> (new Object()));\n" +
		"    }\n" +
		"    public static class Receiver {\n" +
		"        public void process(Consumer<Object> p) {\n" +
		"        }\n" +
		"    }\n" +
		"}"
	},
	"----------\n" +
	"1. ERROR in Test.java (at line 5)\n" +
	"	r.process((o) -> (new Object()));\n" +
	"	  ^^^^^^^\n" +
	"The method process(Consumer<Object>) in the type Test.Receiver is not applicable for the arguments ((<no type> o) -> {})\n" +
	"----------\n" +
	"2. ERROR in Test.java (at line 5)\n" +
	"	r.process((o) -> (new Object()));\n" +
	"	                 ^^^^^^^^^^^^^^\n" +
	"Void methods cannot return a value\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=463526
// Parenthesis are incorrectly allowed in lambda when LambdaBody is an expression statement
public void testBug463526c() {
	runNegativeTest(new String [] {
		"Test.java",
		"import java.util.function.Consumer;\n" +
		"public class Test {\n" +
		"    public static void main(String[] args) {\n" +
		"        Receiver r = new Receiver();\n" +
		"        r.assign((o) -> (o = new Object()));\n" +
		"    }\n" +
		"    public static class Receiver {\n" +
		"        public void assign(Consumer<Object> a) {\n" +
		"        }\n" +
		"    }\n" +
		"}"
	},
	"----------\n" +
	"1. ERROR in Test.java (at line 5)\n" +
	"	r.assign((o) -> (o = new Object()));\n" +
	"	  ^^^^^^\n" +
	"The method assign(Consumer<Object>) in the type Test.Receiver is not applicable for the arguments ((<no type> o) -> {})\n" +
	"----------\n" +
	"2. ERROR in Test.java (at line 5)\n" +
	"	r.assign((o) -> (o = new Object()));\n" +
	"	                ^^^^^^^^^^^^^^^^^^\n" +
	"Void methods cannot return a value\n" +
	"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=464408
public void testBug464408() {
	runNegativeTest(new String[]{
		"test/X.java",
		"import java.util.ArrayList;\n" +
		"import java.util.List;\n" +
		"public class X {\n" +
		"   void x() {\n" +
		"       List<List<String>> list = new ArrayList<>();\n" +
		"       list.stream().toArray(List<String>[]::new);\n" +
		"   }" +
		"}"
	}, "----------\n" +
		"1. ERROR in test\\X.java (at line 6)\n" +
		"	list.stream().toArray(List<String>[]::new);\n" +
		"	                      ^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot create a generic array of List<String>\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=465900
// Internal compiler error: java.lang.IllegalArgumentException: info cannot be null at org.eclipse.jdt.internal.compiler.codegen.StackMapFrame.addStackItem(StackMapFrame.java:81)
public void testBug465900() {
	this.runConformTest(new String [] {
		"X.java",
		"import java.io.Serializable;\n" +
		"import java.util.ArrayList;\n" +
		"import java.util.List;\n" +
		"import java.util.function.Supplier;\n" +
		"public class X {\n" +
		"	private static final long serialVersionUID = 1L;\n" +
		"	protected void x() {\n" +
		"		String str = \"groep.koppeling.\" + (\"\".isEmpty() ? \"toevoegen\" : \"bewerken\");\n" +
		"		List<String> bean = new ArrayList<>();\n" +
		"		test(bean.get(0)::isEmpty);\n" +
		"	}\n" +
		"	private void test(SerializableSupplier<Boolean> test) {}\n" +
		"}\n" +
		"@FunctionalInterface\n" +
		"interface SerializableSupplier<T> extends Supplier<T>, Serializable {}\n"
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477888
// [1.8][compiler] Compiler silently produces garbage but editor shows no errors
public void testBug477888() {
	runNegativeTest(new String [] {
		"Test.java",
		"import java.io.IOException;\n" +
		"import java.nio.file.Files;\n" +
		"import java.nio.file.Paths;\n" +
		"import java.util.function.Consumer;\n" +
		"public class Test {\n" +
		"	public static void main(String[] args) throws IOException {\n" +
		"		Files.lines(Paths.get(args[0])).filter(x -> {return !x.startsWith(\".\");}).forEach(printMe());\n" +
		"	}\n" +
		"	private static Consumer<String> printMe() {\n" +
		"		return x -> x.isEmpty() ? System.out.println() : System.out.println(getIndex() + \" \" + x); // error must be reported here!\n" +
		"	}\n" +
		"	static int idx;\n" +
		"\n" +
		"	private static int getIndex() {\n" +
		"		return ++idx;\n" +
		"	}\n" +
		"}\n"
	},
	"----------\n" +
	"1. ERROR in Test.java (at line 10)\n" +
	"	return x -> x.isEmpty() ? System.out.println() : System.out.println(getIndex() + \" \" + x); // error must be reported here!\n" +
	"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
	"Invalid expression as statement\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=472648
// [compiler][1.8] Lambda expression referencing method with generic type has incorrect compile errors
public void testBug472648() {
	runNegativeTest(
		false,
		JavacHasABug.JavacBugFixed_901,
		new String [] {
		"Test.java",
		"import java.util.ArrayList;\n" +
		"import java.util.List;\n" +
		"import java.util.function.Consumer;\n" +
		"public class Test {\n" +
		"	public static void main(String argv[]) {\n" +
		"		new Test();\n" +
		"	}\n" +
		"	public Test() {\n" +
		"		List<Number> numList = new ArrayList<>();\n" +
		"		numList.add(1);\n" +
		"		numList.add(1.5);\n" +
		"		numList.add(2);\n" +
		"		numList.add(2.5);\n" +
		"		forEachValueOfType(numList, Integer.class, (Integer i) -> (System.out.println(Integer.toString(i))));\n" +
		"	}\n" +
		"	private <T> void forEachValueOfType(List<?> list, Class<T> type, Consumer<T> action) {\n" +
		"		\n" +
		"		for (Object o : list) {\n" +
		"			if (type.isAssignableFrom(o.getClass())) {\n" +
		"				@SuppressWarnings(\"unchecked\")\n" +
		"				T convertedObject = (T) o;\n" +
		"				action.accept(convertedObject);\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}"
	},
	"----------\n" +
	"1. ERROR in Test.java (at line 14)\n" +
	"	forEachValueOfType(numList, Integer.class, (Integer i) -> (System.out.println(Integer.toString(i))));\n" +
	"	^^^^^^^^^^^^^^^^^^\n" +
	"The method forEachValueOfType(List<?>, Class<T>, Consumer<T>) in the type Test is not applicable for the arguments (List<Number>, Class<Integer>, (Integer i) -> {})\n" +
	"----------\n" +
	"2. ERROR in Test.java (at line 14)\n" +
	"	forEachValueOfType(numList, Integer.class, (Integer i) -> (System.out.println(Integer.toString(i))));\n" +
	"	                                            ^^^^^^^\n" +
	"Incompatible type specified for lambda expression's parameter i\n" +
	"----------\n" +
	"3. ERROR in Test.java (at line 14)\n" +
	"	forEachValueOfType(numList, Integer.class, (Integer i) -> (System.out.println(Integer.toString(i))));\n" +
	"	                                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
	"Invalid expression as statement\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=473432
// Internal compiler error: java.lang.IllegalArgumentException: info cannot be null at org.eclipse.jdt.internal.compiler.codegen.StackMapFrame.addStackItem(StackMapFrame.java:81)
public void testBug473432() {
	this.runConformTest(new String [] {
		"Tester.java",
		"import java.util.function.Function;\n" +
		"public class Tester {\n" +
		"	private static class ValueWrapper<O> {\n" +
		"		private O val_;\n" +
		"		public ValueWrapper(O val) {\n" +
		"			val_ = val;\n" +
		"		}\n" +
		"		public <R> R mapOrElse(Function<O, R> func, R defaultValue) {\n" +
		"			if(val_ != null) {\n" +
		"				return func.apply(val_);\n" +
		"			}\n" +
		"			return defaultValue;\n" +
		"		}\n" +
		"	}\n" +
		"	private static void handleObject(Object object) {\n" +
		"		System.out.println(\"Handled: \" + object);\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		ValueWrapper<String> wrapper = new ValueWrapper<>(\"value\");\n" +
		"		boolean skipMethod = false;\n" +
		"		// works on both JDT 3.9.2 and 3.11.0\n" +
		"		Boolean result = skipMethod ? true : wrapper.mapOrElse(v -> false, null);\n" +
		"		System.out.println(result);\n" +
		"		wrapper = new ValueWrapper<>(null);\n" +
		"		// works on JDT 3.9.2\n" +
		"		handleObject(skipMethod ?\n" +
		"				true :\n" +
		"				wrapper.mapOrElse(v -> false, null));\n" +
		"		wrapper = new ValueWrapper<>(null);\n" +
		"		// works on neither version\n" +
		"		result = skipMethod ?\n" +
		"				true :\n" +
		"				wrapper.mapOrElse(v -> false, null);\n" +
		"		System.out.println(result);\n" +
		"	}\n" +
		"}\n"
	},
	"false\n" +
	"Handled: null\n" +
	"null");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=511676 [1.8] Lambda with inner class defs causes java.lang.VerifyError: Bad type on operand stack
public void testBug511676() {
	this.runConformTest(new String [] {
			"A.java",
			"import java.util.function.Function;\n" +
			"public class A {\n" +
			"    interface C<T> { }\n" +
			"    interface O<T> {\n" +
			"        Object r(C<T> s);\n" +
			"    }\n" +
			"    static <T, R> O<R> m(O<T> source, Function<T, O<R>> mapper) {\n" +
			"        return o -> {\n" +
			"            class D {\n" +
			"            	class E {\n" +
			"                }\n" +
			"                E e = new E();\n" +
			"            }\n" +
			"            D d = new D();\n" +
			"            return d.e;\n" +
			"        };\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        m(null, null);\n" +
			"        System.out.println(\" Done\");\n" +
			"    }\n" +
			"}\n"
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=511676 [1.8] Lambda with inner class defs causes java.lang.VerifyError: Bad type on operand stack
public void testBug511676a() {
	this.runConformTest(new String [] {
			"A.java",
			"public class A {\n" +
			"    interface C<T> { }\n" +
			"    interface O<T> {\n" +
			"        Object r(C<T> s);\n" +
			"    }\n" +
			"    static O<Object> def = o -> {\n" +
			"        class D {\n" +
			"        	class E {\n" +
			"            }\n" +
			"            E e = new E();\n" +
			"        }\n" +
			"        D d = new D();\n" +
			"        return d.e;\n" +
			"    };\n" +
			"    public static void main(String[] args) {\n" +
			"        O<Object> o = A.def;\n" +
			"        System.out.println(\" Done\");\n" +
			"    }\n" +
			"}\n"
		},
		"Done");
}
public void testBug543778() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Sandbox.java",
			"import java.util.function.Supplier;\n" +
			"\n" +
			"public class Sandbox {\n" +
			"\n" +
			"    <R extends Object> R get(Supplier<@NonNull R> impl) {\n" +
			"        return null;\n" +
			"    }\n" +
			"\n" +
			"    Object getter() {\n" +
			"        return get(() -> new Object() {\n" +
			"\n" +
			"            @Override\n" +
			"            public String toString() {\n" +
			"                return super.toString();\n" +
			"            }\n" +
			"\n" +
			"        });\n" +
			"    }\n" +
			"\n" +
			"}\n",
			"NonNull.java",
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({ ElementType.TYPE_USE })\n" +
			"public @interface NonNull {\n" +
			"    // marker annotation with no members\n" +
			"}\n",
		};
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED); // bug happens due to type annotation handling
	runner.runConformTest();
}
public void test572873a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"    static <T> Iterable<T> iterable() {\n" +
				"        return () -> new Iterator<T>() {\n" +
				"            @Override\n" +
				"            public boolean hasNext() {\n" +
				"                return false;\n" +
				"            }\n" +
				"\n" +
				"            @Override\n" +
				"            public T next() {\n" +
				"                return null;\n" +
				"            }\n" +
				"        };\n" +
				"    }\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"test T\");\n" +
				"	}\n" +
				"}",
			},
			"test T"
			);
}
public void test572873b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.function.Consumer;\n" +
				"\n" +
				"public class X {\n" +
				"	public static <T> void build(T element) {\n" +
				"        new Thread(() -> {\n" +
				"            new Consumer<T>() {\n" +
				"\n" +
				"                @Override\n" +
				"                public void accept(T t) {" +
				"\n" +
				"                }\n" +
				"            };\n" +
				"        });\n" +
				"	}" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"test T\");\n" +
				"	}\n" +
				"}",
			},
			"test T"
			);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1507
// Errors when referencing a var inside lambda
public void testIssue1507() {
	if (this.complianceLevel < ClassFileConstants.JDK10)
		return;
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {

				    private void makeBug() {
				        String nonBuggyLambda = getString(() -> {
				            assert "Goodbye World".equals(nonBuggyLambda);
				            System.out.println("No popup errors, but there is an error marker as expected");
				        });

				        var buggyLambda = getString(() -> {
				            assert "Goodbye World".equals(buggyLambda);
				            System.out.println("Now using var, this entire project can no longer build due to errors.");
				            System.out.println("There will be error popups and countless error log entries.");
				        });

				    }

				    private String getString(Runnable r) {
				        return "Goodbye World";
				    }
				}
				""",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	private void makeBug() {\n" +
			"	             ^^^^^^^^^\n" +
			"The method makeBug() from the type X is never used locally\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	assert \"Goodbye World\".equals(nonBuggyLambda);\n" +
			"	                              ^^^^^^^^^^^^^^\n" +
			"The local variable nonBuggyLambda may not have been initialized\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	assert \"Goodbye World\".equals(buggyLambda);\n" +
			"	                              ^^^^^^^^^^^\n" +
			"The local variable buggyLambda may not have been initialized\n" +
			"----------\n");
}
public static Class testClass() {
	return LambdaRegressionTest.class;
}
}
