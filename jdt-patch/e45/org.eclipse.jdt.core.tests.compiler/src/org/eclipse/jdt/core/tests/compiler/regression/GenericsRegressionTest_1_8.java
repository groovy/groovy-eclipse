/*******************************************************************************
 * Copyright (c) 2013, 2015 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     IBM Corporation - additional tests     
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericsRegressionTest_1_8 extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testBug434483" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public GenericsRegressionTest_1_8(String name) {
	super(name);
}
public static Class testClass() {
	return GenericsRegressionTest_1_8.class;
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

public void testBug423070() {
	this.runConformTest(
		new String[] {
			"junk/Junk3.java",
			"package junk;\n" + 
			"\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collections;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"class ZZObject extends Object {\n" + 
			"}\n" + 
			"\n" + 
			"public class Junk3 {\n" + 
			"\n" + 
			"    public static final List EMPTY_LIST = new ArrayList<>();\n" + 
			"    public static final <T> List<T> emptyList() {\n" + 
			"        return (List<T>) EMPTY_LIST;\n" + 
			"    }\n" + 
			"    \n" + 
			"    public Junk3(List<ZZObject> list) {\n" + 
			"    }\n" + 
			"    \n" + 
			"    //FAILS - if passed as argument\n" + 
			"    public Junk3() {\n" + 
			"        this(emptyList());\n" + 
			"    }\n" + 
			"    \n" + 
			"\n" + 
			"    //WORKS - if you assign it (and lose type info?)\n" + 
			"    static List works = emptyList();\n" + 
			"    public Junk3(boolean bogus) {\n" + 
			"        this(works);\n" + 
			"    }\n" + 
			"}",
		});
}

public void testConditionalExpression1() {
	runConformTest(
		new String[] {
			"X.java",
			"class A {}\n" +
			"class B extends A {}\n" +
			"public class X {\n" +
			"	<T> T combine(T x, T y) { return x; }\n" +
			"	A test(A a, B b, boolean flag) {\n" +
			"		return combine(flag ? a : b, a);\n" +
			"	}\n" +
			"}\n"
		});
}

public void testConditionalExpression2() {
	runConformTest(
		new String[] {
			"X.java",
			"class A{/**/}\n" + 
			"class B extends A {/**/}\n" + 
			"class C extends B {/**/}\n" + 
			"class G<T> {/**/}\n" + 
			"\n" + 
			"public class X {\n" + 
			"G<A> ga=null;\n" + 
			"G<B> gb=null;\n" + 
			"G<C> gc=null;\n" + 
			"G<? super A> gsa=null;\n" + 
			"G<? super B> gsb=null;\n" + 
			"G<? super C> gsc=null;\n" + 
			"\n" + 
			"@SuppressWarnings(\"unused\")\n" + 
			"    public void test(boolean f) {\n" + 
			"		G<? super B> l1 = (f) ? gsa : gb;\n" +
			"		G<? super B> l2 = (f) ? gsb : gb;\n" +
			"       G<? super C> l3 = (f) ? gsc : gb;\n" +
			"       G<? super B> l4 = (f) ? gsb : gsb;\n" +
			"	}\n" +
			"}"
		});
}
public void testBug423839() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test<T> {\n" + 
			"\n" + 
			"    public <T> T randomElement(Collection<T> list) {\n" + 
			"        return randomElement(list instanceof List ? list : new ArrayList<>(list));\n" + 
			"    }\n" + 
			"\n" + 
			"}\n"
		});
}
public void testBug418807() {
	runConformTest(
		new String[] {
			"Word.java",
			"import java.util.Arrays;\n" + 
			"import java.util.List;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"import java.util.stream.Stream;\n" + 
			" \n" + 
			"public class Word {\n" + 
			"	private final String str;\n" + 
			"\n" + 
			"	public Word(String s) {\n" + 
			"		str = s;\n" + 
			"	}\n" + 
			"\n" + 
			"	@Override\n" + 
			"	public String toString() {\n" + 
			"		return str;\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		List<String> names = Arrays.asList(\"Aaron\", \"Jack\", \"Ben\");\n" + 
			"		Stream<Word> ws = names.stream().map(Word::new);\n" + 
			"		List<Word> words = ws.collect(Collectors.toList());\n" + 
			"		words.forEach(System.out::println);\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug414631() {
	runConformTest(
		new String[] {
			"test/Y.java",
			"package test;\n" + 
			"import java.util.function.Supplier;\n" + 
			"public abstract class Y<E>  {\n" + 
			"  public static <E> Y<E> empty() { return null;}\n" + 
			"  public static <E> Y<E> cons(E head, Supplier<Y<E>> tailFun) {return null;}\n" + 
			"}",
			"test/X.java",
			"package test;\n" + 
			"import static test.Y.*;\n" + 
			"public class X  {\n" + 
			"  public void foo() {\n" + 
			"    Y<String> generated = cons(\"a\", () -> cons(\"b\", Y::<String>empty));\n" + 
			"  }\n" + 
			"}\n"
		});
}
public void testBug424038() {
	runNegativeTest(
		new String[] {
			"Foo.java",
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"public class Foo<E> {\n" + 
			"\n" + 
			"    public void gather() {\n" + 
			"        StreamLike<E> stream = null;\n" + 
			"        List<Stuff<E>> list1 = stream.gather(() -> new Stuff<>()).toList();\n" + 
			"        List<Consumer<E>> list2 = stream.gather(() -> new Stuff<>()).toList(); // ERROR\n" + 
			"    }\n" + 
			"\n" + 
			"    interface StreamLike<E> {\n" + 
			"        <T extends Consumer<E>> StreamLike<T> gather(Supplier<T> gatherer);\n" + 
			"\n" + 
			"        List<E> toList();\n" + 
			"    }\n" + 
			"\n" + 
			"    static class Stuff<T> implements Consumer<T> {\n" + 
			"        public void accept(T t) {}\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Foo.java (at line 8)\n" + 
		"	List<Consumer<E>> list2 = stream.gather(() -> new Stuff<>()).toList(); // ERROR\n" + 
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from List<Foo.Stuff<E>> to List<Consumer<E>>\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/423504 - [1.8] Implement "18.5.3 Functional Interface Parameterization Inference" 
public void testBug423504() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"public class X  {\n" + 
			"  public static void main(String argv[]) {\n" + 
			"    I<? extends Collection<String>> sorter = (List<String> m) -> { /* sort */ };\n" + 
			"  }\n" + 
			"} \n" + 
			"\n" + 
			"interface I<T> { \n" + 
			"  public void sort(T col);\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/420525 - [1.8] [compiler] Incorrect error "The type Integer does not define sum(Object, Object) that is applicable here"
public void testBug420525() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.List;\n" + 
			"import java.util.concurrent.CompletableFuture;\n" + 
			"import java.util.concurrent.ExecutionException;\n" +
			"public class X {\n" +
			"	void test(List<CompletableFuture<Integer>> futures) {\n" + 
			"		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[]{})).thenApplyAsync( (Void v) -> {\n" + 
			"			Integer finalResult = futures.stream().map( (CompletableFuture<Integer> f) -> {\n" + 
			"				try {\n" + 
			"					return f.get();\n" + 
			"				} catch (InterruptedException | ExecutionException e) {\n" + 
			"					return 0;\n" + 
			"				}\n" + 
			"			}).reduce(0, Integer::sum);\n" + 
			"			\n" + 
			"			log(\"final result is \" + finalResult);\n" + 
			"			if (finalResult != 50){\n" + 
			"				throw new RuntimeException(\"FAILED\");\n" + 
			"			} else{\n" + 
			"				log(\"SUCCESS\");\n" + 
			"			}\n" + 
			"			\n" + 
			"			return null;\n" + 
			"		});\n" + 
			"\n" + 
			"	}\n" +
			"	void log(String msg) {}\n" +
			"}\n"
		});
}
//https://bugs.eclipse.org/420525 - [1.8] [compiler] Incorrect error "The type Integer does not define sum(Object, Object) that is applicable here"
public void testBug420525_mini() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.List;\n" + 
			"import java.util.concurrent.CompletableFuture;\n" + 
			"import java.util.concurrent.ExecutionException;\n" +
			"public class X {\n" +
			"	void test(List<CompletableFuture<Integer>> futures, boolean b) {\n" + 
			"		Integer finalResult = futures.stream().map( (CompletableFuture<Integer> f) -> {\n" +
			"					if (b) \n" +
			"						return 1;\n" +
			"					else\n" +
			"						return Integer.valueOf(13);" +
			"				}).reduce(0, Integer::sum);\n" + 
			"	}\n" +
			"}\n"
		});
}

// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=420525#c7
public void testBug420525a() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"interface I<T> {\n" + 
			"    T bold(T t);\n" + 
			"}\n" + 
			"\n" + 
			"class Main {  \n" + 
			"    public String foo(String x) { return \"<b>\" + x + \"</b>\"; }\n" + 
			"    String bar() {\n" + 
			"        I<? extends String> i = this::foo;\n" + 
			"        return i.bold(\"1\");\n" + 
			"    }  \n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Main.java (at line 9)\n" + 
		"	return i.bold(\"1\");\n" + 
		"	         ^^^^\n" + 
		"The method bold(capture#1-of ? extends String) in the type I<capture#1-of ? extends String> is not applicable for the arguments (String)\n" + 
		"----------\n");
}

public void testBug424415() {
	runConformTest(
		new String[] {
			"X.java",
			"\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"\n" + 
			"interface Functional<T> {\n" + 
			"   T apply();\n" + 
			"}\n" + 
			"\n" + 
			"class X {\n" + 
			"    void foo(Object o) { }\n" + 
			"\n" + 
			"	<Q extends Collection<?>> Q goo(Functional<Q> s) {\n" + 
			"		return null;\n" + 
			"	} \n" + 
			"\n" + 
			"    void test() {\n" + 
			"        foo(goo(ArrayList<String>::new));\n" + 
			"    }\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424415#c6
public void testBug424415b() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"\n" + 
			"interface Functional<T> {\n" + 
			"   T apply();\n" + 
			"}\n" + 
			"\n" + 
			"class X {\n" + 
			"    void foo(Object o) { }\n" + 
			"    void foo(String str) {} \n" + 
			"\n" + 
			"    <Q extends Collection<?>> Q goo(Functional<Q> s) {\n" + 
			"        return null;\n" + 
			"    } \n" + 
			"\n" + 
			"    void test() {\n" + 
			"        foo(goo(ArrayList<String>::new));\n" + 
			"    }\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424415#c8
public void testBug424415c() {
	runConformTest(
		new String[] {
			"com/example/MyEmployee.java",
			"package com.example;\n" + 
			"class MyEmployee {\n" + 
			"	\n" + 
			"	public enum Gender { MALE, FEMALE, OTHERS }\n" + 
			"\n" + 
			"	private int age = 0;\n" + 
			"	private Gender gender = Gender.MALE;\n" + 
			"	\n" + 
			"	public MyEmployee(int age, Gender gender) {\n" + 
			"		this.age = age;\n" + 
			"		this.gender = gender;\n" + 
			"	}	\n" + 
			"	\n" + 
			"	public int getAge() {\n" + 
			"		return age;\n" + 
			"	}\n" + 
			"	\n" + 
			"	public Gender getGender() {\n" + 
			"		return gender;\n" + 
			"	}\n" + 
			"}",
			"com/example/Test.java",
			"package com.example;\n" + 
			"\n" + 
			"import java.util.List;\n" + 
			"import java.util.concurrent.ConcurrentMap;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"\n" + 
			"	ConcurrentMap<MyEmployee.Gender, List<MyEmployee>> test(List<MyEmployee> el) {\n" + 
			"		return el.parallelStream()\n" + 
			"					.collect(\n" + 
			"						Collectors.groupingByConcurrent(MyEmployee::getGender)\n" + 
			"						);\n" + 
			"	}\n" + 
			"	\n" + 
			"}"
		});
}
public void testBug424631() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"\n" + 
			"interface Functional<T> {\n" + 
			"   T apply();\n" + 
			"}\n" + 
			"\n" + 
			"class X {\n" + 
			"    void foo(Collection<String> o) { }\n" + 
			"\n" + 
			"	<Q extends Collection<?>> Q goo(Functional<Q> s) {\n" + 
			"		return null;\n" + 
			"	} \n" + 
			"\n" + 
			"    void test() { \n" + 
			"        foo(goo(ArrayList<String>::new));\n" + 
			"    }\n" + 
			"}\n"
		});
}

public void testBug424403() {
	runConformTest(
		new String[] {
			"X.java",
			"interface Functional { int foo(); }\n" + 
			"\n" + 
			"public class X {\n" + 
			"    static int bar() {\n" + 
			"        return -1;\n" + 
			"    }\n" + 
			"    static <T> T consume(T t) { return null; }\n" + 
			"\n" + 
			"    public static void main(String[] args) {\n" + 
			"    	Functional f = consume(X::bar);\n" + 
			"    }  \n" + 
			"}\n"
		});
}
public void testBug401850a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"import java.util.ArrayList;\n" + 
			"public class X<T> {\n" + 
			"   X(T t) {}\n" + 
			"   X(String s) {}\n" + 
			"   int m(X<String> xs) { return 0; }\n" + 
			"   int i = m(new X<>(\"\"));\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 1)\n" + 
		"	import java.util.List;\n" + 
		"	       ^^^^^^^^^^^^^^\n" + 
		"The import java.util.List is never used\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 2)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n");
}
public void testBug401850b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"import java.util.ArrayList;\n" + 
			"public class X<T> {\n" + 
			"   X(T t) {}\n" + 
			"   X(String s) {}\n" + 
			"   int m(X<String> xs) { return 0; }\n" + 
			"   int i = m(new X<String>(\"\"));\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	int i = m(new X<String>(\"\"));\n" + 
		"	          ^^^^^^^^^^^^^^^^^\n" + 
		"The constructor X<String>(String) is ambiguous\n" + 
		"----------\n");
}

public void testBug424710() {
	runConformTest(
		new String[] {
			"MapperTest.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Arrays;\n" + 
			"import java.util.List;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.regex.Matcher;\n" + 
			"import java.util.regex.Pattern;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"public class MapperTest {\n" + 
			"\n" + 
			"    public static void main( String... argv ){\n" + 
			"        List<String> data = Arrays.asList(\"abc\", \"123\", \"1a\", \"?!?\");\n" + 
			"        List<Pattern> patterns = Arrays.asList(Pattern.compile(\"[a-z]+\"), Pattern.compile(\"[0-9]+\"));\n" + 
			"		patterns.stream()\n" + 
			"				.flatMap(\n" + 
			"						p -> {\n" + 
			"							Stream<Matcher> map = data.stream().map(p::matcher);\n" + 
			"							Stream<Matcher> filter = map.filter(Matcher::find);\n" + 
			"							Function<? super Matcher, ? extends Object> mapper = Matcher::group;\n" + 
			"							mapper = matcher -> matcher.group();\n" + 
			"							return filter.map(mapper);\n" + 
			"						})\n" + 
			"				.forEach(System.out::println);\n" + 
			"    }\n" + 
			"}\n"
		});
}

public void testBug424075() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" + 
			"import java.util.function.*;\n" + 
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"        Consumer<Object> c = null;\n" + 
			"        Arrays.asList(pred(), c);\n" + 
			"    }\n" + 
			"\n" + 
			"    static <T> Predicate<T> pred() {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug424205a() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"	void bar(String t);\n" + 
			"}\n" + 
			"class X<T> implements I {\n" + 
			"	public void bar(String t) {}\n" + 
			"	X(String x) {}\n" + 
			"	X(T x) {}\n" + 
			"	public void one(X<I> c){}\n" + 
			"	public void two() {\n" + 
			"		X<I> i = new X<>((String s) -> { });\n" + 
			"		one (i);\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug424205b() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"	void bar(String t);\n" + 
			"}\n" + 
			"public class X<T> implements I {\n" + 
			"	public void bar(String t) {}\n" + 
			"	X(String x) {}\n" + 
			"	X(T x) {}\n" + 
			"	public void one(X<I> c){}\n" + 
			"	public void two() {\n" + 
			"		one(new X<>((String s) -> { })); // 1. Three errors\n" + 
			"		X<I> i = new X<>((String s) -> { }); // 2. Error - Comment out the previous line to see this error go away.\n" + 
			"		one (i);\n" + 
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"main\");\n" +
			"		new X<Integer>(\"one\").two();\n" +
			"	}\n" + 
			"}\n"
		},
		"main");
}
public void testBug424712a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Collection;\n" + 
			"import java.util.function.Supplier;\n" + 
			"import java.util.Set;\n" + 
			"\n" + 
			"public class X {\n" + 
			"    public static <T, SOURCE extends Collection<T>, DEST extends Collection<T>>\n" + 
			"        DEST foo(SOURCE sourceCollection, DEST collectionFactory) {\n" + 
			"            return null;\n" + 
			"    }  \n" + 
			"    \n" + 
			"    public static void main(String... args) {\n" + 
			"        Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\n" + 
		"	Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);\n" + 
		"	    ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 12)\n" + 
		"	Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);\n" + 
		"	                        ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 12)\n" + 
		"	Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);\n" + 
		"	                                      ^^^\n" + 
		"Cannot instantiate the type Set\n" + 
		"----------\n");
}
public void testBug424712b() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Comparator;\n" + 
			"public class X {\n" +
			"	<T> void test() {\n" + 
			"		Comparator<? super T> comparator = (Comparator<? super T>) Comparator.naturalOrder();\n" +
			"		System.out.println(\"OK\");\n" + 
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().test();\n" +
			"	}\n" +
			"}\n"
		},
		"OK");
}
public void testBug425142_minimal() {
	runNegativeTest(
		new String[] {
			"SomethingBreaks.java",
			"import java.io.IOException;\n" + 
			"import java.nio.file.Files;\n" + 
			"import java.nio.file.Paths;\n" + 
			"import java.util.function.Consumer;\n" + 
			"\n" + 
			"@FunctionalInterface interface Use<T, E extends Throwable> {   void accept(T t) throws E; }\n" + 
			"\n" + 
			"@SuppressWarnings(\"unused\") public class SomethingBreaks<T, E extends Throwable> {\n" + 
			"  protected static SomethingBreaks<String, IOException> stream() {     return null;  }\n" + 
			"\n" + 
			"  public void forEach(Consumer<T> use) throws E {}\n" + 
			"\n" + 
			"  public <E2 extends E> void forEach(Use<T, E2> use) throws E, E2 {}\n" + 
			"\n" + 
			"  private static void methodReference(String s) throws IOException {\n" + 
			"    System.out.println(Files.size(Paths.get(s)));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase9() throws IOException {\n" + 
			"    stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in SomethingBreaks.java (at line 20)\n" + 
		"	stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
		"	         ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n");
}
public void testBug425142_full() {
	runNegativeTest(
		new String[] {
			"SomethingBreaks.java",
			"import java.io.IOException;\n" + 
			"import java.nio.file.Files;\n" + 
			"import java.nio.file.Paths;\n" + 
			"import java.util.function.Consumer;\n" + 
			"\n" + 
			"@FunctionalInterface interface Use<T, E extends Throwable> {   void accept(T t) throws E; }\n" + 
			"\n" + 
			"@SuppressWarnings(\"unused\") public class SomethingBreaks<T, E extends Throwable> {\n" + 
			"  protected static SomethingBreaks<String, IOException> stream() {     return null;  }\n" + 
			"\n" + 
			"  public void forEach(Consumer<T> use) throws E {}\n" + 
			"\n" + 
			"  public <E2 extends E> void forEach(Use<T, E2> use) throws E, E2 {}\n" + 
			"\n" + 
			"  private static void methodReference(String s) throws IOException {\n" + 
			"    System.out.println(Files.size(Paths.get(s)));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase1() throws IOException {\n" + 
			"    Use<String, IOException> c =\n" + 
			"      (String s) -> System.out.println(Files.size(Paths.get(s)));\n" + 
			"    stream().forEach(c);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase2() throws IOException {\n" + 
			"    Use<String, IOException> c = SomethingBreaks::methodReference;\n" + 
			"    stream().forEach(c);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase3() throws IOException {\n" + 
			"    stream().forEach((Use<String, IOException>) (String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase4() throws IOException {\n" + 
			"    stream().forEach((Use<String, IOException>) SomethingBreaks::methodReference);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase5() throws IOException {\n" + 
			"    stream().<IOException> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase6() throws IOException {\n" + 
			"    stream().<IOException> forEach(SomethingBreaks::methodReference);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase7() throws IOException {\n" + 
			"    stream().<Use<String, IOException>> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase8() throws IOException {\n" + 
			"    stream().<Use<String, IOException>> forEach(SomethingBreaks::methodReference);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase9() throws IOException {\n" + 
			"    stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase10() throws IOException {\n" + 
			"    stream().forEach(SomethingBreaks::methodReference);\n" + 
			"  }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in SomethingBreaks.java (at line 39)\n" + 
		"	stream().<IOException> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
		"	                       ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n" + 
		"2. ERROR in SomethingBreaks.java (at line 43)\n" + 
		"	stream().<IOException> forEach(SomethingBreaks::methodReference);\n" + 
		"	                       ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n" + 
		"3. ERROR in SomethingBreaks.java (at line 43)\n" + 
		"	stream().<IOException> forEach(SomethingBreaks::methodReference);\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n" + 
		"4. ERROR in SomethingBreaks.java (at line 47)\n" + 
		"	stream().<Use<String, IOException>> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
		"	                                                                             ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n" + 
		"5. ERROR in SomethingBreaks.java (at line 51)\n" + 
		"	stream().<Use<String, IOException>> forEach(SomethingBreaks::methodReference);\n" + 
		"	                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n" + 
		"6. ERROR in SomethingBreaks.java (at line 55)\n" + 
		"	stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
		"	         ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n" + 
		"7. ERROR in SomethingBreaks.java (at line 59)\n" + 
		"	stream().forEach(SomethingBreaks::methodReference);\n" + 
		"	         ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n" + 
		"8. ERROR in SomethingBreaks.java (at line 59)\n" + 
		"	stream().forEach(SomethingBreaks::methodReference);\n" + 
		"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n");
}
public void testBug424195a() {
	runNegativeTestMultiResult(
		new String[] {
			"NPEOnCollector.java",
			"import java.io.IOException;\n" + 
			"import java.nio.file.Path;\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.function.Predicate;\n" + 
			"import java.util.jar.JarEntry;\n" + 
			"import java.util.jar.JarFile;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"\n" + 
			"public class NPEOnCollector {\n" + 
			"  static void processJar(Path plugin) throws IOException {\n" + 
			"    \n" + 
			"    try(JarFile jar = new JarFile(plugin.toFile())) {\n" + 
			"      try(Stream<JarEntry> entries = jar.stream()) {\n" + 
			"        Stream<JarEntry> stream = entries\n" + 
			"          .distinct().collect(Collectors.toCollection(ArrayList::new));\n" + 
			"        \n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n"
		},
		null,
		new String[] {
			"----------\n" + 
			"1. ERROR in NPEOnCollector.java (at line 17)\n" + 
			"	Stream<JarEntry> stream = entries\n" + 
			"          .distinct().collect(Collectors.toCollection(ArrayList::new));\n" + 
			"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Collection<JarEntry> to Stream<JarEntry>\n" + 
			"----------\n",
			"----------\n" + 
			"1. ERROR in NPEOnCollector.java (at line 18)\n" + 
			"	.distinct().collect(Collectors.toCollection(ArrayList::new));\n" + 
			"	                                            ^^^^^^^^^^^^^^\n" + 
			"The constructed object of type ArrayList is incompatible with the descriptor\'s return type: Stream<JarEntry>&Collection<T#2>&Collection<JarEntry>\n" + 
			"----------\n"
		});
}
public void testBug424195b() {
	runConformTest(
		new String[] {
			"NPEOnCollector.java",
			"import java.io.IOException;\n" + 
			"import java.nio.file.Path;\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.function.Predicate;\n" + 
			"import java.util.jar.JarEntry;\n" + 
			"import java.util.jar.JarFile;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"\n" + 
			"public class NPEOnCollector {\n" + 
			"  static void processJar(Path plugin) throws IOException {\n" + 
			"    \n" + 
			"    try(JarFile jar = new JarFile(plugin.toFile())) {\n" + 
			"      try(Stream<JarEntry> entries = jar.stream()) {\n" + 
			"        Collection<JarEntry> collection = entries\n" + 
			"          .distinct().collect(Collectors.toCollection(ArrayList::new));\n" + 
			"        \n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n"
		});
}
public void testBug424195_comment2() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.PrintStream;\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"import java.util.stream.*;\n" + 
			"public class X  {\n" + 
			"\n" + 
			"    public static void main(String argv[]) {\n" + 
			"        ArrayList<Integer> al = IntStream\n" + 
			"        	     .range(0, 10_000)\n" + 
			"        	     .boxed()\n" + 
			"        	     .collect(Collectors.toCollection(ArrayList::new));\n" + 
			"\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug425153() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"class C1 {}\n" + 
			"class C2 {}\n" + 
			"\n" + 
			"interface I<P1 extends C1, P2 extends P1> {\n" + 
			"    P2 foo(P1 p1);\n" + 
			"}\n" + 
			"\n" + 
			"public class Main  {\n" + 
			"	    public static void main(String argv[]) {\n" + 
			"	    	I<?, ?> i = (C1 c1) -> { return new C2(); };\n" + 
			"	        Object c2 = i.foo(null);\n" + 
			"	    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Main.java (at line 10)\n" + 
		"	I<?, ?> i = (C1 c1) -> { return new C2(); };\n" + 
		"	            ^^^^^^^^^^\n" + 
		"The target type of this expression is not a well formed parameterized type due to bound(s) mismatch\n" + 
		"----------\n");
}
public void testBug424845() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Collections;\n" + 
			"import java.util.Comparator;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"    \n" + 
			"\n" + 
			"    interface Function<K, V>{\n" + 
			"        public V apply(K orig);\n" + 
			"    }\n" + 
			"    \n" + 
			"    \n" + 
			"    static class Ordering<O> {\n" + 
			"\n" + 
			"        public <K> Comparator<K> onResultOf(Function<K, ? extends O> function) {\n" + 
			"            return null;\n" + 
			"        }\n" + 
			"\n" + 
			"        \n" + 
			"    }\n" + 
			"    \n" + 
			"    public static void main(String[] args) {\n" + 
			"        List<Object> list = new ArrayList<>();\n" + 
			"        Function<Object, String> function = new Function<Object, String>() {\n" + 
			"            public String apply(Object arg0) {\n" + 
			"                return arg0.toString();\n" + 
			"            }\n" + 
			"        };\n" + 
			"        Ordering<Comparable<String>> natural = new Ordering<>();\n" + 
			"        Collections.sort(list, natural.onResultOf(function));\n" + 
			"    }\n" + 
			"    \n" + 
			"}\n"
		});
}
public void testBug425278() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I<T, S extends X<T>> { \n" + 
			"    T foo(S p);\n" + 
			"}\n" + 
			"\n" + 
			"public class X<T>  {\n" + 
			"    public void bar() {\n" + 
			"    I<Object, X<Object>> f = (p) -> p; // Error\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug425783() {
	runConformTest(
		new String[] {
			"Test.java",
			"class MyType<S extends MyType<S>> {\n" +
			"	S myself() { return (S)this; }\n" +
			"}\n" + 
			"public class Test {\n" +
			"	MyType test() {\n" +
			"		return newInstance().myself();\n" +
			"	}\n" +
			"	MyType test2() {\n" +
			"		return newInstance().myself();\n" +
			"	}\n" +
			"	public <T extends MyType> T newInstance() {\n" + 
			"		return (T) new MyType();\n" + 
			"	}" +
			"}\n"
		});
}
public void testBug425798() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.*;\n" +
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"import java.util.stream.*;\n" +
			"interface MyCollector<T, A, R> extends Collector<T, A, R> {\n" + 
			"}\n" +
			"public abstract class X {\n" +
			"	abstract <T, K, U, M extends Map<K, U>>\n" + 
			"    MyCollector<T, ?, M> toMap(Function<? super T, ? extends K> km,\n" + 
			"                                BinaryOperator<U> mf);" +
			"	void test(Stream<Annotation> annotations) {\n" +
			"		annotations\n" +
			"			.collect(toMap(Annotation::annotationType,\n" +
			"				 (first, second) -> first));\n" +
			"	}\n" +
			"}\n"
		},
		"");
}
public void testBug425798a() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.*;\n" +
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"import java.util.stream.*;\n" +
			"interface MyCollector<T, A, R> extends Collector<T, A, R> {\n" + 
			"}\n" +
			"public abstract class X {\n" +
			"	abstract <T, K, U, M extends Map<K, U>>\n" + 
			"    MyCollector<T, ?, M> toMap(Function<? super T, ? extends K> km,\n" + 
			"                                BinaryOperator<U> mf);" +
			"	void test(Stream<Annotation> annotations) {\n" +
			"		annotations\n" +
			"			.collect(toMap(true ? Annotation::annotationType : Annotation::annotationType,\n" +
			"				 (first, second) -> first));\n" +
			"	}\n" +
			"}\n"
		},
		"");
}
// witness for NPE mentioned in https://bugs.eclipse.org/bugs/show_bug.cgi?id=425798#c2
public void testBug425798b() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Objects;\n" + 
			"import java.util.PrimitiveIterator;\n" + 
			"import java.util.Spliterator;\n" + 
			"import java.util.Spliterator.OfInt;\n" + 
			"import java.util.function.Consumer;\n" + 
			"import java.util.function.IntConsumer;\n" + 
			"\n" + 
			"class IntIteratorSpliterator implements OfInt {\n" + 
			"	public IntIteratorSpliterator(PrimitiveIterator.OfInt arg) { }\n" + 
			"	public void forEachRemaining(IntConsumer action) { }\n" + 
			"	public boolean tryAdvance(Consumer<? super Integer> action) { return false; }\n" + 
			"	public long estimateSize() { return 0; }\n" + 
			"	public int characteristics() { return 0; }\n" + 
			"	public OfInt trySplit() { return null; }\n" + 
			"	public boolean tryAdvance(IntConsumer action) { return false; }\n" + 
			"}\n" + 
			"public class X {\n" + 
			"\n" + 
			"	public Spliterator.OfInt spliterator(PrimitiveIterator.OfInt iterator) {\n" + 
			"		return new IntIteratorSpliterator(id(iterator));\n" + 
			"	}\n" + 
			"	<T> T id(T e) { return e; }\n" + 
			"}\n"
		});
}
public void testBug425460orig() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"public class X {\n" +
			"	final Integer[] boom =\n" + 
			"  		Arrays.asList(\"1\", \"22\", \"333\")\n" + 
			"  			.stream()\n" + 
			"  			.map(str -> str.length())\n" + 
			"  			.toArray(i -> new Integer[i]);\n" +
			"}\n"
		});
}
public void testBug425460variant() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"public class X {\n" +
			"	final Integer[] boom =\n" + 
			"  		Arrays.asList(\"1\", \"22\", \"333\")\n" + 
			"  			.stream()\n" + 
			"  			.map(str -> str.length())\n" + 
			"  			.toArray((int i) -> new Integer[i]);\n" +
			"}\n"
		});
}
public void testBug425951() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"\n" + 
			"    public static void main(String[] args) {\n" + 
			"        index(new A().test());\n" + 
			"    }\n" + 
			"\n" + 
			"    public static <X> void index(Iterable<X> collection)\n" + 
			"    {\n" + 
			"    }\n" + 
			"	\n" + 
			"    public class A<S extends A<S>>\n" + 
			"    {\n" + 
			"        protected A() {}\n" + 
			"		\n" + 
			"        public <T> List<T> test()\n" + 
			"       {\n" + 
			"            return null;\n" + 
			"       }\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in Test.java (at line 6)\n" + 
		"	index(new A().test());\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: Unchecked invocation index(List) of the generic method index(Iterable<X>) of type Test\n" + 
		"----------\n" + 
		"2. WARNING in Test.java (at line 6)\n" + 
		"	index(new A().test());\n" + 
		"	      ^^^^^^^^^^^^^^\n" + 
		"Type safety: The expression of type List needs unchecked conversion to conform to Iterable<Object>\n" + 
		"----------\n" + 
		"3. ERROR in Test.java (at line 6)\n" + 
		"	index(new A().test());\n" + 
		"	      ^^^^^^^\n" + 
		"No enclosing instance of type Test is accessible. Must qualify the allocation with an enclosing instance of type Test (e.g. x.new A() where x is an instance of Test).\n" + 
		"----------\n" + 
		"4. WARNING in Test.java (at line 6)\n" + 
		"	index(new A().test());\n" + 
		"	          ^\n" + 
		"Test.A is a raw type. References to generic type Test.A<S> should be parameterized\n" + 
		"----------\n");
}
public void testBug425951a() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"\n" + 
			"    public void test() {\n" + 
			"        index(new A().test());\n" + 
			"    }\n" + 
			"\n" + 
			"    public static <X> void index(Iterable<X> collection)\n" + 
			"    {\n" + 
			"    }\n" + 
			"	\n" + 
			"    public class A<S extends A<S>>\n" + 
			"    {\n" + 
			"        protected A() {}\n" + 
			"		\n" + 
			"        public <T> List<T> test()\n" + 
			"       {\n" + 
			"            return null;\n" + 
			"       }\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug424906() {
	runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" + 
			"	public <T> void test(Result r) {}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Main().test(r -> System.out.println(\"Hmmm...\" + r));\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"interface Result {\n" + 
			"	public void result(Object object);\n" + 
			"}"
		});
}
public void testBug425156() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I<T> {\n" + 
			"    void foo(T t);\n" + 
			"}\n" + 
			"public class X {\n" + 
			"    void bar(I<?> i) {\n" + 
			"        i.foo(null);\n" + 
			"    }\n" + 
			"    void run() {\n" + 
			"        bar((X x) -> {}); // Incompatible error reported\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug425493() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"public class Test {\n" + 
			"    public void addAttributeBogus(Attribute<?> attribute) {\n" + 
			"        addAttribute(java.util.Objects.requireNonNull(attribute, \"\"),\n" + 
			"                attribute.getDefault());\n" + 
			"        addAttribute(attribute, attribute.getDefault());\n" + 
			"    }\n" + 
			"    public <T> void addAttributeOK(Attribute<T> attribute) {\n" + 
			"        addAttribute(java.util.Objects.requireNonNull(attribute, \"\"),\n" + 
			"                attribute.getDefault());\n" + 
			"        addAttribute(attribute, attribute.getDefault());\n" + 
			"    }\n" + 
			"\n" + 
			"    private <T> void addAttribute(Attribute<T> attribute, T defaultValue) {}\n" + 
			"\n" + 
			"    static class Attribute<T> {\n" + 
			"\n" + 
			"        T getDefault() {\n" + 
			"            return null;\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Test.java (at line 3)\n" + 
		"	addAttribute(java.util.Objects.requireNonNull(attribute, \"\"),\n" + 
		"	^^^^^^^^^^^^\n" + 
		"The method addAttribute(Test.Attribute<T>, T) in the type Test is not applicable for the arguments (Test.Attribute<capture#1-of ?>, capture#2-of ?)\n" + 
		"----------\n" + 
		"2. ERROR in Test.java (at line 3)\n" + 
		"	addAttribute(java.util.Objects.requireNonNull(attribute, \"\"),\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Test.Attribute<capture#1-of ?> to Test.Attribute<T>\n" + 
		"----------\n" + 
		"3. ERROR in Test.java (at line 5)\n" + 
		"	addAttribute(attribute, attribute.getDefault());\n" + 
		"	^^^^^^^^^^^^\n" + 
		"The method addAttribute(Test.Attribute<T>, T) in the type Test is not applicable for the arguments (Test.Attribute<capture#3-of ?>, capture#4-of ?)\n" + 
		"----------\n");
}
public void testBug426366() {
	runConformTest(
		new String[] {
			"a/Test.java",
			"package a;\n" + 
			"\n" + 
			"import java.util.Collections;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"/**\n" + 
			" * @author tomschindl\n" + 
			" *\n" + 
			" */\n" + 
			"public class Test {\n" + 
			"	public static class A {\n" + 
			"		public A(B newSelectedObject, String editorController) {\n" + 
			"	    }\n" + 
			"\n" + 
			"	    public A(List<B> newSelectedObjects, String editorController) {\n" + 
			"	    }\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static class B {\n" + 
			"		\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static class C extends A {\n" + 
			"		public C() {\n" + 
			"			super(Collections.emptyList(), \"\");\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug426290() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class X {\n" + 
			"    public static void main(String argv[]) {\n" + 
			"       goo(foo());\n" + 
			"    }\n" + 
			"\n" + 
			"    static <T extends Number> List<T> foo() {\n" + 
			"        return new ArrayList<T>();\n" + 
			"    }\n" + 
			"\n" + 
			"    static void goo(Object p1) {\n" + 
			"        System.out.println(\"goo(Object)\");\n" + 
			"    }\n" + 
			"\n" + 
			"    static void goo(List<Integer> p1) {\n" + 
			"        System.out.println(\"goo(List<Integer>)\");\n" + 
			"    }\n" + 
			"}\n"
		},
		"goo(List<Integer>)");
}
public void testBug425152() {
	runConformTest(
		new String[] {
			"packDown/SorterNew.java",
			"package packDown;\n" + 
			"\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collections;\n" + 
			"import java.util.Comparator;\n" + 
			"\n" + 
			"public class SorterNew {\n" + 
			"	void sort() {\n" + 
			"		Collections.sort(new ArrayList<Person>(),\n" + 
			"				Comparator.comparing((Person p) -> p.getName()));\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"class Person {\n" + 
			"	public String getName() {\n" + 
			"		return \"p\";\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug426048() {
	runNegativeTest(
		new String[] {
			"MyFunction.java",
			"import java.lang.annotation.Annotation;\n" + 
			"import java.lang.annotation.ElementType;\n" + 
			"import java.lang.annotation.Target;\n" + 
			"\n" + 
			"@Target(ElementType.TYPE_USE)\n" + 
			"@interface Throws {\n" + 
			"  Class<? extends Throwable>[] value() default Throwable.class;\n" + 
			"  Returns method() default @Returns(Annotation.class);\n" + 
			"}\n" + 
			"\n" + 
			"@Target(ElementType.TYPE_USE)\n" + 
			"@interface Returns {\n" + 
			"  Class<? extends Annotation> value() default Annotation.class;\n" + 
			"}\n" + 
			"\n" + 
			"@FunctionalInterface public interface MyFunction<T, @Returns R> {\n" + 
			"  @Returns  R apply(T t);\n" + 
			"\n" + 
			"  default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
			"    compose(MyFunction<? super V, ? extends T> before) {\n" + 
			"\n" + 
			"    return (V v) -> apply(before.apply(v));\n" + 
			"  }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in MyFunction.java (at line 19)\n" + 
		"	default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
		"	          ^\n" + 
		"Syntax error, insert \"Type Identifier (\" to complete MethodHeaderName\n" + 
		"----------\n" + 
		"2. ERROR in MyFunction.java (at line 19)\n" + 
		"	default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
		"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from MyFunction<capture#1-of ? super V,capture#2-of ? extends T> to Class<? extends Throwable>[]\n" + 
		"----------\n" + 
		"3. ERROR in MyFunction.java (at line 19)\n" + 
		"	default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
		"	                                                          ^^^^^^\n" + 
		"before cannot be resolved\n" + 
		"----------\n" + 
		"4. ERROR in MyFunction.java (at line 19)\n" + 
		"	default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
		"	                                                                       ^\n" + 
		"Syntax error, insert \")\" to complete Modifiers\n" + 
		"----------\n" + 
		"5. ERROR in MyFunction.java (at line 20)\n" + 
		"	compose(MyFunction<? super V, ? extends T> before) {\n" + 
		"	       ^\n" + 
		"Syntax error on token \"(\", , expected\n" + 
		"----------\n");
}
public void testBug426540() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.stream.Stream;\n" + 
			"import java.util.Collections;\n" + 
			"import static java.util.stream.Collectors.collectingAndThen;\n" + 
			"import static java.util.stream.Collectors.toList;\n" + 
			"public class X {\n" + 
			"	Object o = ((Stream<Integer>) null).collect(collectingAndThen(toList(), Collections::unmodifiableList));\n" + 
			"}\n"
		});
}
public void testBug426671_ok() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.stream.Stream;\n" + 
			"import java.util.*;\n" + 
			"import static java.util.stream.Collectors.collectingAndThen;\n" + 
			"import static java.util.stream.Collectors.toList;\n" + 
			"public class X {\n" +
			"	void test(Stream<List<Integer>> stream) {\n" +
			"		stream.collect(collectingAndThen(toList(), Collections::<List<Integer>>unmodifiableList))\n" +
			"			.remove(0);\n" +
			"	}\n" + 
			"}\n"
		});
}
public void testBug426671_medium() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.stream.Stream;\n" + 
			"import java.util.*;\n" + 
			"import static java.util.stream.Collectors.collectingAndThen;\n" + 
			"import static java.util.stream.Collectors.toList;\n" + 
			"public class X {\n" +
			"	void test(Stream<List<Integer>> stream) {\n" +
			"		stream.collect(collectingAndThen(toList(), Collections::unmodifiableList))\n" +
			"			.remove(0);\n" +
			"	}\n" + 
			"}\n"
		});
}
public void testBug426671_full() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.stream.Stream;\n" + 
			"import java.util.*;\n" + 
			"import static java.util.stream.Collectors.collectingAndThen;\n" + 
			"import static java.util.stream.Collectors.toList;\n" + 
			"public class X {\n" +
			"	void test() {\n" +
			"		Arrays.asList((List<Integer>) null).stream().collect(collectingAndThen(toList(), Collections::unmodifiableList))\n" +
			"			.remove(0);\n" +
			"	}\n" + 
			"}\n"
		});
}
public void testBug426671b() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"interface I<X,Y> {\n" + 
			"	Y fun(X y);\n" + 
			"}\n" + 
			"public class Test {\n" + 
			"	static <S> S id(S s) { return s; }\n" + 
			"	void test() {\n" + 
			"        m1(Test::id, \"Hi\");\n" + 
			"        m2(Test::id, \"Hi\").toUpperCase();\n" + 
			"        m3(Test::id, \"Hi\").toUpperCase();\n" + 
			"   }\n" + 
			"\n" + 
			"	<U,V> void m1(I<V,U> i, U u) { }\n" +
			"	<U,V> V m2(I<V,U> i, U u) {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	<U,V> V m3(I<U,V> i, U u) {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in Test.java (at line 8)\n" + 
		"	m2(Test::id, \"Hi\").toUpperCase();\n" + 
		"	                   ^^^^^^^^^^^\n" + 
		"The method toUpperCase() is undefined for the type Object\n" + 
		"----------\n");
}
public void testBug426652() {
	runConformTest(
		new String[] {
			"X.java",
			"import static java.util.stream.Collectors.toList;\n" + 
			"public class X {\n" + 
			"	Object o = toList();\n" + 
			"}\n"
		});
}
public void testBug426778() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" + 
			"public class X {\n" + 
			"	void test(List<CourseProviderEmploymentStatistics> result) {\n" + 
			"          Collections.sort( result, \n" + 
			"              Comparator.comparingInt(\n" + 
			"                  (CourseProviderEmploymentStatistics stat) ->  stat.doneTrainingsTotal\n" + 
			"				)\n" + 
			"              .reversed()\n" + 
			"              .thenComparing(\n" + 
			"                  (CourseProviderEmploymentStatistics stat) -> stat.courseProviderName ) );\n" + 
			"	}\n" + 
			"}\n" + 
			"class CourseProviderEmploymentStatistics {\n" + 
			"   int doneTrainingsTotal;\n" + 
			"   String courseProviderName;\n" + 
			"}\n"
		});
}
public void testBug426676() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.Arrays;\n" + 
			"import java.util.function.Supplier;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"\n" + 
			"public class Test {\n" + 
			"    public static void main(String[] args) throws Exception {\n" + 
			"        // Type inference works on map call.\n" + 
			"        Stream<String> s1 =\n" + 
			"        Arrays.stream(new Integer[] { 1, 2 })\n" + 
			"              .map(i -> i.toString());\n" + 
			"        \n" + 
			"        // Type inference doesn't work on map call.\n" + 
			"        Stream<String> s2 =\n" + 
			"        Arrays.stream(new Integer[] { 1, 2 })\n" + 
			"              .map(i -> i.toString())\n" + 
			"              .distinct();\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug424591_comment20() {
	runConformTest(
		new String[] {
			"MyList.java",
			"import java.util.Arrays;\n" + 
			"public class MyList {\n" + 
			"    protected Object[] elements;\n" + 
			"    private int size;\n" + 
			"    @SuppressWarnings(\"unchecked\")\n" + 
			"    public <A> A[] toArray(A[] a) {\n" + 
			"        return (A[]) Arrays.copyOf(elements, size, a.getClass());\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug424591_comment20_variant() {
	runNegativeTest(
		new String[] {
			"MyList.java",
			"import java.util.Arrays;\n" + 
			"public class MyList {\n" + 
			"    protected Object[] elements;\n" + 
			"    private int size;\n" + 
			"    @SuppressWarnings(\"unchecked\")\n" + 
			"    public <A> A[] toArray(A[] a) {\n" + 
			"        return (A[]) Arrays.copyOf(elements, size, getClass());\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in MyList.java (at line 7)\n" + 
		"	return (A[]) Arrays.copyOf(elements, size, getClass());\n" + 
		"	                    ^^^^^^\n" + 
		"The method copyOf(U[], int, Class<? extends T[]>) in the type Arrays is not applicable for the arguments (Object[], int, Class<capture#1-of ? extends MyList>)\n" + 
		"----------\n");
}
public void testBug424591_comment22() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.*;\n" + 
			"public class Test {\n" + 
			"	public static void main(String[] args) {\n" + 
			"        Test.forObject(new HashSet<>());\n" + 
			"	}\n" + 
			"    public static Test forObject(Object o) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug425063() {
    runConformTest(
        new String[] {
            "ComparatorUse.java",
            "import java.util.Comparator;\n" + 
            "public class ComparatorUse {\n" + 
            "   Comparator<String> c =\n" + 
            "           Comparator.comparing((String s)->s.toString())\n" + 
            "           .thenComparing(s -> s.length());\n" + 
            "}\n"
        });
}
public void testBug426764() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I {}\n" + 
			"class C1 implements I {}\n" + 
			"class C2 implements I {}\n" + 
			"public class X  {\n" + 
			"    <T > void foo(T p1, I p2) {}\n" + 
			"    <T extends I> void foo(T p1, I p2) {}\n" + 
			"    void bar() {\n" + 
			"        foo(true ? new C1(): new C2(), false ? new C2(): new C1());\n" + 
			"        foo(new C1(), false ? new C2(): new C1());\n" + 
			"    }\n" + 
			"}\n"
		});
}
// simplest: avoid any grief concerning dequeCapacity:
public void testBug424930a() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayDeque;\n" + 
			"import java.util.Deque;\n" + 
			"import java.util.function.Supplier;\n" + 
			"\n" + 
			"public class X<S, T extends Deque<S>> {\n" + 
			"    private final Supplier<T> supplier;\n" + 
			"\n" + 
			"    public X(Supplier<T> supplier) {\n" + 
			"        this.supplier = supplier;\n" + 
			"    }\n" + 
			"    \n" +
			"	 int dequeCapacity;\n" + 
			"    public static <S> X<S, Deque<S>> newDefaultMap() {\n" + 
			"        return new X<>(() -> new ArrayDeque<>(13));\n" + 
			"    }\n" + 
			"}\n"
		});
}
// original test:
public void testBug424930b() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayDeque;\n" + 
			"import java.util.Deque;\n" + 
			"import java.util.function.Supplier;\n" + 
			"\n" + 
			"public class X<S, T extends Deque<S>> {\n" + 
			"    private final Supplier<T> supplier;\n" + 
			"\n" + 
			"    public X(Supplier<T> supplier) {\n" + 
			"        this.supplier = supplier;\n" + 
			"    }\n" + 
			"    \n" + 
			"    public static <S> X<S, Deque<S>> newDefaultMap(int dequeCapacity) {\n" + 
			"        return new X<>(() -> new ArrayDeque<>(dequeCapacity));\n" + 
			"    }\n" + 
			"}\n"
		});
}
// witness for an NPE during experiments
public void testBug424930c() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayDeque;\n" + 
			"import java.util.Deque;\n" + 
			"import java.util.function.Supplier;\n" + 
			"\n" + 
			"public class X<S, T extends Deque<S>> {\n" + 
			"    private final Supplier<T> supplier;\n" + 
			"\n" + 
			"    public X(Supplier<T> supplier) {\n" + 
			"        this.supplier = supplier;\n" + 
			"    }\n" + 
			"    \n" +
			"	 int dequeCapacity;\n" + 
			"    public static <S> X<S, Deque<S>> newDefaultMap() {\n" + 
			"        return new X<>(() -> new ArrayDeque<>(dequeCapacity));\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 14)\n" + 
		"	return new X<>(() -> new ArrayDeque<>(dequeCapacity));\n" + 
		"	                                      ^^^^^^^^^^^^^\n" + 
		"Cannot make a static reference to the non-static field dequeCapacity\n" + 
		"----------\n");
}
public void testBug426998a() {
	runConformTest(
		new String[] {
			"Snippet.java",
			"public class Snippet {\n" + 
			"	static void call(Class type, long init) {\n" + 
			"		String string = new String();\n" + 
			"		method(type, init == 0 ? new String() : string);\n" + 
			"	}\n" + 
			"	private static void method(Class type, String s) {}\n" + 
			"}\n"
		});
}
// from https://bugs.eclipse.org/bugs/show_bug.cgi?id=426764#c5
public void testBug426998b() {
	runConformTest(
		new String[] {
			"Snippet.java",
			"public class Snippet {\n" + 
			"  private static final String PLACEHOLDER_MEMORY = new String();\n" + 
			"\n" + 
			"  static void newInstance(Class type, long init) {\n" + 
			"    method(type, init == 0 ? new String() : PLACEHOLDER_MEMORY);\n" + 
			"  }\n" + 
			"\n" + 
			"  private static void method(Class type, String str) {}\n" + 
			"}\n"
		});
}
public void testBug427164() {
	runNegativeTest(
		new String[] {
			"NNLambda.java",
			"import java.util.*;\n" + 
			"\n" + 
			"@FunctionalInterface\n" + 
			"interface FInter {\n" + 
			"	String allToString(List<String> input);\n" + 
			"}\n" + 
			"\n" + 
			"public abstract class NNLambda {\n" + 
			"	abstract <INP> void printem(FInter conv, INP single);\n" + 
			"	\n" + 
			"	void test() {\n" + 
			"		printem((i) -> {\n" + 
			"				Collections.<String>singletonList(\"const\")\n" + 
			"			}, \n" + 
			"			\"single\");\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in NNLambda.java (at line 1)\n" + 
		"	printem((i) -> {\n" + 
		"	^^^^^^^\n" + 
		"The method printem(FInter, INP) in the type NNLambda is not applicable for the arguments (FInter, String)\n" + 
		"----------\n" + 
		"2. ERROR in NNLambda.java (at line 13)\n" + 
		"	Collections.<String>singletonList(\"const\")\n" + 
		"	                                         ^\n" + 
		"Syntax error, insert \";\" to complete BlockStatements\n" + 
		"----------\n",
		true); // statement recovery
}
public void testBug427168() {
	runNegativeTest(
		new String[] {
			"X.java",
			"interface Producer<T> {\n" + 
			"	<P> P produce();\n" + 
			"}\n" + 
			"public class X {\n" + 
			"	<T> void perform(Producer<T> r) { }\n" + 
			"	void test() {\n" + 
			"		perform(() -> 13); \n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	perform(() -> 13); \n" + 
		"	^^^^^^^\n" + 
		"The method perform(Producer<T>) in the type X is not applicable for the arguments (() -> {})\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	perform(() -> 13); \n" + 
		"	        ^^^^^^^^\n" + 
		"Illegal lambda expression: Method produce of type Producer<T> is generic \n" + 
		"----------\n");
}
public void testBug427196() {
	runConformTest(
		new String[] {
			"MainTest.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"import java.util.List;\n" + 
			"import java.util.function.Function;\n" + 
			"\n" + 
			"public class MainTest {\n" + 
			"    public static <T> List<T> copyOf (Collection<T> c) {\n" + 
			"        return new ArrayList<>(c);\n" + 
			"    }\n" + 
			"    \n" + 
			"    public static <T> List<T> copyOf (Iterable<T> c) {\n" + 
			"        return new ArrayList<>();\n" + 
			"    }\n" + 
			"    \n" + 
			"    public static void main (String[] args) {\n" + 
			"        Function<Collection<String>, List<String>> function1 = c -> MainTest.copyOf(c); //OK\n" + 
			"        Function<Collection<String>, List<String>> function2 = MainTest::copyOf;        //error\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug427224() {
	runConformTest(
		new String[] {
			"Test2.java",
			"import java.util.*;\n" +
			"public class Test2 {\n" + 
			"    public static native <T> T applyToSet(java.util.Set<String> s);\n" + 
			"\n" + 
			"    public static void applyToList(java.util.List<String> s) {\n" + 
			"        applyToSet(new java.util.HashSet<>(s));\n" + 
			"    }\n" + 
			"}\n"
		});
}
// comment 12
public void testBug424637() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"	String foo(X x, String s2);\n" + 
			"}\n" + 
			"\n" + 
			"public class X {\n" + 
			"	String goo(String ...ts) {\n" + 
			"		System.out.println(ts[0]);  \n" + 
			"		return ts[0];\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		I i = X::goo;\n" + 
			"		String s = i.foo(new X(), \"world\");\n" + 
			"		System.out.println(s);     \n" + 
			"	}\n" + 
			"}\n"
		},
		"world\n" +
		"world",
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=427218, [1.8][compiler] Verify error varargs + inference 
public void test427218_reduced() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   public static void main(String[] args) {\n" +
			"      match(getLast(\"a\"), null);\n" +
			"   }\n" +
			"   public static <T> T getLast(T... array) { return null; } // same with T[]\n" +
			"   public static void match(boolean b, Object foo) { }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	match(getLast(\"a\"), null);\n" + 
		"	^^^^^\n" + 
		"The method match(boolean, Object) in the type X is not applicable for the arguments (String, null)\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	match(getLast(\"a\"), null);\n" + 
		"	      ^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from String to boolean\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 5)\n" + 
		"	public static <T> T getLast(T... array) { return null; } // same with T[]\n" + 
		"	                                 ^^^^^\n" + 
		"Type safety: Potential heap pollution via varargs parameter array\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=427218, [1.8][compiler] Verify error varargs + inference 
public void test427218() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   public static void main(String[] args) {\n" +
			"      match(getLast(\"a\"), null);\n" +
			"   }\n" +
			"   public static <T> T getLast(T... array) { return null; } // same with T[]\n" +
			"   public static void match(boolean b, Object foo) { }\n" +
			"   public static <A> void match(Object o, A foo) { }\n" +
			"}\n",
		},
		"");
}
public void testBug427223() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"public class Test {\n" + 
			"\n" + 
			"	List<Object> toList(Object o) {\n" + 
			"		if (o instanceof Optional) {\n" + 
			"			return Arrays.asList(((Optional<?>) o).orElse(null));\n" + 
			"		} else {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"}\n"
		});
}
public void testBug425183_comment8() {
	// similar to what triggered the NPE, but it never did trigger
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String... args) {\n" + 
			"        java.util.Comparator.reverseOrder().thenComparingLong(X::toLong);\n" +
			"        System.out.println(\"ok\");\n" + 
			"    }\n" +
			"    static <T> long toLong(T in) { return 0L; }\n" +
			"}\n"
		},
		"ok");
}
public void testBug427483() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"public class X {\n" +
			"	void test() {\n" +
			"		new TreeSet<>((String qn1, String qn2) -> {\n" + 
			"   		boolean b = true;\n" + 
			"			System.out.println(b); // ok\n" + 
			"   		if(b) { }\n" + 
			"	   		return qn1.compareTo(qn2);\n" + 
			"		});\n" +
			"	}\n" +
			"}\n"
		});
}
public void testBug427504() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" + 
			"\n" + 
			"	public static <T> Tree<T> model(T o) {\n" + 
			"		return Node(Leaf(o), Leaf(o));\n" + 
			"	}\n" + 
			"	\n" + 
			"	interface Tree<T> {}\n" + 
			"	static <T> Tree<T> Node(Tree<T>... children) { return null; }\n" + 
			"	static <T> Tree<T> Leaf(T o) { return null; }\n" + 
			"	\n" + 
			"}\n"
		});
}
public void testBug427479() {
	runConformTest(
		new String[] {
			"Bug.java",
			"import java.util.*;\n" + 
			"import java.util.function.BinaryOperator; \n" + 
			"import java.util.stream.*;\n" + 
			"\n" + 
			"public class Bug {\n" + 
			" \n" + 
			"	static List<String> names = Arrays.asList(\n" + 
			"			\"ddd\",\n" + 
			"			\"s\",\n" + 
			"			\"sdfs\",\n" + 
			"			\"sfdf d\"); \n" + 
			" \n" + 
			"	public static void main(String[] args) {\n" + 
			"			 BinaryOperator<List<String>> merge = (List<String> first, List<String> second) -> {\n" + 
			"				 first.addAll(second);\n" + 
			"				 return first;\n" + 
			"				 };\n" + 
			"				 \n" + 
			"			Collector<String,?,Map<Integer,List<String>>> collector= Collectors.toMap(\n" + 
			"					s -> s.length(), \n" + 
			"					Arrays::asList,\n" + 
			"					merge); \n" + 
			"			Map<Integer, List<String>> lengthToStrings = names.stream().collect(collector);\n" + 
			"			\n" + 
			"			lengthToStrings.forEach((Integer i, List<String> l)-> {\n" + 
			"				System.out.println(i + \" : \" + Arrays.deepToString(l.toArray()));\n" + 
			"			});\n" + 
			"\n" + 
			"	}\n" + 
			"\n" + 
			"}\n"
		});
}
public void testBug427479b() {
	runNegativeTest(
		new String[] {
			"Bug419048.java",
			"import java.util.List;\n" + 
			"import java.util.Map;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"\n" + 
			"\n" + 
			"public class Bug419048 {\n" + 
			"	void test1(List<Object> roster) {\n" + 
			"        Map<String, Object> map = \n" + 
			"                roster\n" + 
			"                    .stream()\n" + 
			"                    .collect(\n" + 
			"                        Collectors.toMap(\n" + 
			"                            p -> p.getLast(),\n" + 
			"                            p -> p.getLast()\n" + 
			"                        ));\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Bug419048.java (at line 9)\n" + 
		"	roster\n" + 
		"                    .stream()\n" + 
		"                    .collect(\n" + 
		"                        Collectors.toMap(\n" + 
		"                            p -> p.getLast(),\n" + 
		"                            p -> p.getLast()\n" + 
		"                        ));\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Map<Object,Object> to Map<String,Object>\n" + 
		"----------\n" + 
		"2. ERROR in Bug419048.java (at line 13)\n" + 
		"	p -> p.getLast(),\n" + 
		"	       ^^^^^^^\n" + 
		"The method getLast() is undefined for the type Object\n" + 
		"----------\n" + 
		"3. ERROR in Bug419048.java (at line 14)\n" + 
		"	p -> p.getLast()\n" + 
		"	       ^^^^^^^\n" + 
		"The method getLast() is undefined for the type Object\n" + 
		"----------\n");
}
public void testBug427626() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	void m() {\n" + 
			"        List<String> ss = Arrays.asList(\"1\", \"2\", \"3\");\n" + 
			"        \n" + 
			"        ss.stream().map(s -> {\n" + 
			"          class L1 {};\n" + 
			"          class L2 {\n" + 
			"            void mm(L1 l) {}\n" + 
			"          }\n" + 
			"          return new L2().mm(new L1());\n" + 
			"        }).forEach(e -> System.out.println(e));\n" + 
			"	}\n" + 
			"}"
		},
		// 8u20 emits just one message inferred type not conforming to upper bound. ECJ's message is actually better.
		// We used to emit only 1 error here. Here the lambda is broken, so inference fails leading to two messages.			
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	ss.stream().map(s -> {\n" + 
		"          class L1 {};\n" + 
		"          class L2 {\n" + 
		"            void mm(L1 l) {}\n" + 
		"          }\n" + 
		"          return new L2().mm(new L1());\n" + 
		"        }).forEach(e -> System.out.println(e));\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Stream<Object> to <unknown>\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 13)\n" + 
		"	return new L2().mm(new L1());\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Cannot return a void result\n" + 
		"----------\n");
}
public void testBug426542() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.Serializable;\n" +
			"public class X {\n" + 
			"	\n" + 
			"	<T extends Comparable & Serializable> void foo(T o1) {\n" + 
			"	}\n" + 
			"\n" + 
			"	<T extends Serializable> void foo(T o1) {\n" + 
			"	}\n" + 
			"\n" + 
			"	void bar() {\n" + 
			"		foo((Comparable & Serializable)0);\n" + 
			"		foo(0);\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug426836() {
	runConformTest(
		new String[] {
			"ReferenceToGetClass.java",
			"import java.util.function.Supplier;\n" + 
			"\n" + 
			"\n" + 
			"public class ReferenceToGetClass {\n" + 
			"	<T> T extract(Supplier<T> s) {\n" + 
			"		return s.get();\n" + 
			"	}\n" + 
			"	Class<?> test() {\n" + 
			"		Class<? extends ReferenceToGetClass> c = extract(this::getClass);\n" + 
			"		return c;\n" + 
			"	}\n" + 
			"}\n"
		} );
}
public void testBug428019() {
    runConformTest(
        new String[] {
            "X.java",
            "final public class X {\n" + 
            "  static class Obj {}\n" + 
            "  static class Dial<T> {}\n" + 
            "\n" + 
            "  <T> void put(Class<T> clazz, T data) {\n" + 
            "  }\n" + 
            "\n" + 
            "  static <T> Dial<T> wrap(Dial<T> dl) {\n" + 
            "	  return null;\n" + 
            "  }\n" + 
            "\n" + 
            "  static void foo(Dial<? super Obj> dial, X context) {\n" + 
            "    context.put(Dial.class, wrap(dial));\n" + 
            "  }\n" + 
            "  \n" + 
            "  public static void main(String[] args) {\n" + 
            "	X.foo(new Dial<Obj>(), new X());\n" + 
            "  }\n" + 
            "}\n"
        });
}
public void testBug428198() {
	runConformTest(
		new String[] {
			"Snippet.java",
			"import java.util.*;\n" + 
			"interface BundleRevision {}\n" + 
			"interface BundleDescription extends BundleRevision {}\n" + 
			"public class Snippet {\n" + 
			"  static Collection<BundleRevision> test(BundleDescription[] triggers) {\n" + 
			"    @SuppressWarnings(\"unchecked\")\n" + 
			"    Collection<BundleRevision> triggerRevisions =\n" + 
			"    //Type mismatch: cannot convert from Collection<Object> to Collection<BundleRevision>\n" + 
			"      Collections\n" + 
			"        .unmodifiableCollection(triggers == null ? Collections.EMPTY_LIST\n" + 
			"        : Arrays.asList((BundleRevision[]) triggers));\n" + 
			"    return triggerRevisions;\n" + 
			"  }\n" + 
			"}\n"
		});
}
public void testBug428198b() {
	runConformTest(
		new String[] {
			"Snippet.java",
			"import java.util.*;\n" + 
			"interface BundleRevision {}\n" + 
			"interface BundleDescription extends BundleRevision {}\n" + 
			"public class Snippet {\n" + 
			"  static Collection<BundleRevision> test(BundleDescription[] triggers) {\n" + 
			"    @SuppressWarnings(\"unchecked\")\n" + 
			"    Collection<BundleRevision> triggerRevisions =\n" + 
			"      Collections\n" + 
			"        .unmodifiableCollection(triggers == null ? Collections.emptyList()\n" + 
			"        : Arrays.asList((BundleRevision[]) triggers));\n" + 
			"    return triggerRevisions;\n" + 
			"  }\n" + 
			"}\n"
		});
}
public void testBug428264() {
	runConformTest(
		new String[] {
			"Y.java",
			"import java.util.function.*;\n" + 
			"import java.util.Optional;\n" + 
			"\n" + 
			"interface I<E,F> {}\n" + 
			"class A<G> implements I<G, Optional<G>> {}\n" + 
			"\n" + 
			"public class Y<S,T> {\n" + 
			"    Y(T o, Predicate<T> p, Supplier<I<S,T>> s) {}\n" + 
			"\n" + 
			"    static <Z> Y<Z, Optional<Z>> m() {\n" + 
			"        return new Y<>(Optional.empty(), Optional::isPresent, A::new);\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug428294() {
	runConformTest(
		new String[] {
			"Junk5.java",
			"import java.util.Collection;\n" + 
			"import java.util.List;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"\n" + 
			"\n" + 
			"public class Junk5 {\n" + 
			"\n" + 
			"    class TestTouchDevice {\n" + 
			"        public Object [] points;\n" + 
			"    }\n" + 
			"    \n" + 
			"    public static List<TestTouchDevice> getTouchDevices() {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"\n" + 
			"    public static Collection<Object[]> getTouchDeviceParameters2(int minPoints) {\n" + 
			"        Collection c = getTouchDevices().stream()\n" + 
			"                .filter(d -> d.points.length >= minPoints)\n" + 
			"                .map(d -> new Object[] { d })\n" + 
			"                .collect(Collectors.toList());\n" + 
			"         return c;\n" + 
			"    }\n" + 
			"    \n" + 
			"    public static Collection<Object[]> getTouchDeviceParameters3(int minPoints) {\n" + 
			"        return getTouchDevices().stream()\n" + 
			"                .filter(d -> d.points.length >= minPoints)\n" + 
			"                .map(d -> new Object[] { d })\n" + 
			"                .collect(Collectors.toList());\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug428291() {
	runConformTest(
		new String[] {
			"AC3.java",
			"import java.util.List;\n" + 
			"\n" + 
			"interface I0<T> { }\n" + 
			"\n" + 
			"interface I1 { }\n" + 
			"interface I1List<E> extends List<E>, I1 {}\n" + 
			"interface I2<T> extends I1 {\n" + 
			"	void foo(I0<? super T> arg1);\n" + 
			"	void bar(I0<? super T> arg2);\n" + 
			"}\n" + 
			"interface I3<T> extends I2<T> {}\n" + 
			"interface I4<T> extends I2<T> { }\n" + 
			"interface I3List<E> extends I3<I1List<E>>, I1List<E> {}\n" + 
			"abstract class AC1<E> implements I3List<E> { }\n" + 
			"\n" + 
			"abstract class AC2<E>  {\n" + 
			"    public static <E> AC2<E> bork(AC2<E> f1, I3List<E> i3l, I0<? super I1List<E>> i1l) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"    public static <E> AC2<E> garp(AC2<E> f2, I0<? super I1List<E>> i1l) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"}\n" + 
			"\n" + 
			"public abstract class AC3<E> extends AC1<E> implements I4<I1List<E>> {\n" + 
			"\n" + 
			"    AC2<E> f = null;\n" + 
			"\n" + 
			"    @Override\n" + 
			"    public void foo(I0<? super I1List<E>> arg1) {\n" + 
			"        f = AC2.bork(f, this, arg1);\n" + 
			"    }\n" + 
			"\n" + 
			"    @Override\n" + 
			"    public void bar(I0<? super I1List<E>> arg2) {\n" + 
			"        f = AC2.garp(f, arg2);\n" + 
			"    }\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428275,  [1.8][compiler] CCE in InferenceContext18.varArgTypes 
public void testBug428275() {
	runConformTest(
		new String[] {
			"p1/C1.java",
			"package p1;\n" + 
			"\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class C1<T1> {\n" + 
			"\n" + 
			"	public static class CInner<T2A,T2B> {\n" + 
			"		public CInner(T2A a, T2B b) {}\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static class CInner2<T3A,T3B> {\n" + 
			"		public CInner2(String n, List<CInner<T3A,T3B>> arg) {}\n" + 
			"	}\n" + 
			"	\n" + 
			"    public static <E> List<E> getList1(E... items) {\n" + 
			"    	return null;\n" + 
			"    }\n" + 
			"}\n",
			"Test.java",
			"import java.util.List;\n" + 
			"\n" + 
			"import p1.C1;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"	void test2(List<C1.CInner2> l) {\n" + 
			"		l.add(\n" + 
			"			new C1.CInner2<>(\"a\",\n" + 
			"				C1.getList1(new C1.CInner<>(\"b\", 13))\n" + 
			"			)\n" + 
			"		);\n" + 
			"	}\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428352, [1.8][compiler] NPE in AllocationExpression.analyseCode when trying to pass Consumer as Function 
public void test428352() {
	runNegativeTest(
		new String[] {
			"OperationsPile.java",
			"import java.util.Collection;\n" +
			"import java.util.List;\n" +
			"import java.util.function.Consumer;\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"class OperationsPile<B> {\n" +
			"  OperationsPile(Function<B, ?> handler) {}\n" +
			"\n" +
			"  private static <T> void addAll3(Collection<T> c, T t) {}\n" +
			"\n" +
			"  static <S> void adaad3(List<OperationsPile<?>> combined, Consumer<S> handler) {\n" +
			"    addAll3(combined, new OperationsPile<>(null));\n" +
			"    addAll3(combined, new OperationsPile<>(handler));\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in OperationsPile.java (at line 13)\n" + 
		"	addAll3(combined, new OperationsPile<>(handler));\n" + 
		"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Cannot infer type arguments for OperationsPile<>\n" + 
		"----------\n");
}
public void test428352b() {
	runConformTest(
		new String[] {
			"OperationsPile.java",
			"import java.util.Collection;\n" +
			"import java.util.List;\n" +
			"import java.util.function.Consumer;\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"public class OperationsPile<B> {\n" +
			"  OperationsPile(Function<B, ?> handler) {}\n" +
			"\n" +
			"  private static <T> void addAll3(Collection<T> c, T t) {}\n" +
			"\n" +
			"  static <S> void adaad3(List<OperationsPile<?>> combined, Consumer<S> handler) {\n" +
			"    addAll3(combined, new OperationsPile<>(null));\n" +
			"  }\n" +
			"	public static void main(String[] args) {\n" +
			"		adaad3(null, null);\n" +
			"		System.out.println(13);\n" +
			"	}\n" +
			"}\n"
		},
		"13");
}
public void testBug428307() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.stream.*;\n" + 
			"\n" + 
			"interface Bar {\n" + 
			"	Class<? extends Bar> type();\n" + 
			"}\n" + 
			"public class X {\n" + 
			" \n" + 
			"    <T extends Bar> T[] test(Class<T> barClass, Stream<Bar> bars) {\n" + 
			"        return get(bars.\n" + 
			"                    collect(Collectors.toMap(Bar::type,\n" + 
			"                                             Function.identity(),\n" + 
			"                                             ((first,second) -> first),\n" + 
			"                                             HashMap::new)),\n" + 
			"                            barClass);\n" + 
			"    }\n" + 
			"    \n" + 
			"    <A extends Bar> A[] get(Map<Class<? extends Bar>,Bar> m, Class<A> c) {\n" + 
			"    	return null;\n" + 
			"    }\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428524, [1.8][compiler] NPE when using JSE8 Class Constructor ref "TheClass::new" and "TheClass" is using default no-arg constructor 
public void test428524() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.function.Supplier;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Supplier<WithNoArgConstructor> works = WithNoArgConstructor::new;\n" +
			"		System.out.println(works.get());\n" +
			"		Supplier<WithoutNoArgConstructor> error = WithoutNoArgConstructor::new;\n" +
			"		System.out.println(error.get());\n" +
			"		\n" +
			"	}\n" +
			"	private static class WithNoArgConstructor {\n" +
			"		public WithNoArgConstructor() {\n" +
			"		}\n" +
			"       public String toString() {\n" +
			"           return(\"WithNoArgConstructor\");\n" +
			"       }\n" +
			"	}\n" +
			"	private static class WithoutNoArgConstructor {\n" +
			"       public String toString() {\n" +
			"           return(\"WithOutNoArgConstructor\");\n" +
			"       }\n" +
			"	}\n" +
			"}\n"
		}, 
		"WithNoArgConstructor\n" + 
		"WithOutNoArgConstructor");
}
public void testBug428786() {
	runConformTest(
		new String[] {
			"Junk9.java",
			"import java.util.*;\n" +
			"public class Junk9 {\n" + 
			"    class Node {\n" + 
			"        public double getLayoutY() {return 12;}\n" + 
			"    }\n" + 
			"    class Node2 extends Node {\n" + 
			"    }\n" + 
			"    void junk() {\n" + 
			"        List<Node2> visibleCells = new ArrayList<>(20);\n" + 
			"        Collections.sort(visibleCells, (Node o1, Node o2) -> Double.compare(o1.getLayoutY(), o2.getLayoutY()));\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug429090_comment1() {
	runNegativeTest(
		new String[] {
			"Junk10.java",
			"\n" + 
			"public class Junk10 {\n" + 
			"    class Observable<T> {}\n" + 
			"    interface InvalidationListener {\n" + 
			"        public void invalidated(Observable<?> observable);\n" + 
			"    }\n" + 
			"    public static abstract class Change<E2> {}\n" + 
			"    interface SetChangeListener<E1> {\n" + 
			"        void onChanged(Change<? extends E1> change);\n" + 
			"    }\n" + 
			"    class SetListenerHelper<T> {}\n" + 
			"    public static <E> SetListenerHelper<E> addListener(\n" +
			"			SetListenerHelper<E> helper, SetChangeListener<? super E> listener) {\n" + 
			"        return helper;\n" + 
			"    }\n" + 
			"    void junk() {\n" + 
			"        addListener(null, (SetChangeListener.Change<?> c) -> {});\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Junk10.java (at line 17)\n" + 
		"	addListener(null, (SetChangeListener.Change<?> c) -> {});\n" + 
		"	^^^^^^^^^^^\n" + 
		"The method addListener(Junk10.SetListenerHelper<E>, Junk10.SetChangeListener<? super E>) in the type Junk10 is not applicable for the arguments (null, (SetChangeListener.Change<?> c) -> {})\n" + 
		"----------\n" + 
		"2. ERROR in Junk10.java (at line 17)\n" + 
		"	addListener(null, (SetChangeListener.Change<?> c) -> {});\n" + 
		"	                   ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"SetChangeListener.Change cannot be resolved to a type\n" + 
		"----------\n");
}
public void testBug429090() {
	runConformTest(
		new String[] {
			"Junk10.java",
			"public class Junk10 {\n" + 
			"    class Observable<T> {}\n" + 
			"    interface InvalidationListener {\n" + 
			"        public void invalidated(Observable observable);\n" + 
			"    }\n" + 
			"    interface SetChangeListener<E> {\n" + 
			"        public static abstract class Change<E> {}\n" + 
			"        void onChanged(Change<? extends E> change);\n" + 
			"    }\n" + 
			"    class SetListenerHelper<T> {}\n" + 
			"    public static <E> SetListenerHelper<E> addListener(SetListenerHelper<E> helper, InvalidationListener listener) {\n" + 
			"        return helper;\n" + 
			"    }\n" + 
			"    public static <E> SetListenerHelper<E> addListener(SetListenerHelper<E> helper, SetChangeListener<? super E> listener) {\n" + 
			"        return helper;\n" + 
			"    }\n" + 
			"    void junk() {\n" + 
			"        addListener(null, new SetChangeListener () {\n" + 
			"            public void onChanged(SetChangeListener.Change change) {}\n" + 
			"        });\n" + 
			"        addListener(null, (SetChangeListener.Change<? extends Object> c) -> {});\n" + // original was without "extends Object" 
			"    }\n" + 
			"}\n"
		});
}
public void testBug429490_comment33() {
    runConformTest(
        new String[] {
            "Junk12.java",
            "public class Junk12 {\n" + 
            "    class Observable<T> {}\n" + 
            "    class ObservableValue<T> {}\n" + 
            "    interface InvalidationListener {\n" + 
            "        public void invalidated(Observable observable);\n" + 
            "    }\n" + 
            "    public interface ChangeListener<T> {\n" + 
            "        void changed(ObservableValue<? extends T> observable, T oldValue, T newValue);\n" + 
            "    }\n" + 
            "    class ExpressionHelper<T> {}\n" + 
            "    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, InvalidationListener listener) {\n" + 
            "        return helper;\n" + 
            "    }\n" + 
            "    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, ChangeListener<? super T> listener) {\n" + 
            "        return helper;\n" + 
            "    }\n" + 
            "    void junk() {\n" + 
            "        addListener(null, null, new ChangeListener () {\n" + 
            "            public void changed(ObservableValue observable, Object oldValue, Object newValue) {\n" + 
            "                throw new RuntimeException();\n" + 
            "            }\n" + 
            "        });\n" + 
            "        addListener(null, null, (value, o1, o2) -> {throw new RuntimeException();});\n" + 
            "    }\n" + 
            "}\n"
        });
}
public void testBug428811() {
	runConformTest(
		new String[] {
			"MoreCollectors.java",
			"import java.util.AbstractList;\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.Arrays;\n" + 
			"import java.util.Collection;\n" + 
			"import java.util.List;\n" + 
			"import java.util.stream.Collector;\n" + 
			"\n" + 
			"public class MoreCollectors {\n" + 
			"    public static void main (String[] args) {\n" + 
			"        ImmutableList<String> list = Arrays.asList(\"a\", \"b\", \"c\").stream().collect(toImmutableList());\n" + 
			"        \n" + 
			"        System.out.println(list);\n" + 
			"    }\n" + 
			"    \n" + 
			"    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList () {\n" + 
			"        return Collector.of(ArrayList<T>::new,\n" + 
			"                List<T>::add,\n" + 
			"                (left, right) -> { left.addAll(right); return left; },\n" + 
			"                ImmutableList::copyOf);\n" + 
			"    }\n" + 
			"    \n" + 
			"    private static class ImmutableList<T> extends AbstractList<T> {\n" + 
			"        public static <T> ImmutableList<T> copyOf (Collection<T> c) {\n" + 
			"            return new ImmutableList<>(c.toArray());\n" + 
			"        }\n" + 
			"\n" + 
			"        private Object[] array;\n" + 
			"        \n" + 
			"        private ImmutableList (Object[] array) {\n" + 
			"            this.array = array;\n" + 
			"        }\n" + 
			"\n" + 
			"        @Override @SuppressWarnings(\"unchecked\")\n" + 
			"        public T get(int index) {\n" + 
			"            return (T)array[index];\n" + 
			"        }\n" + 
			"\n" + 
			"        @Override\n" + 
			"        public int size() {\n" + 
			"            return array.length;\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n"
		},
		"[a, b, c]");
}
// all exceptions can be inferred to match
public void testBug429430() {
	runConformTest(
		new String[] {
			"Main.java",
			"import java.io.*;\n" + 
			"public class Main {\n" + 
			"  public static interface Closer<T, X extends Exception> {\n" + 
			"    void closeIt(T it) throws X;\n" + 
			"  }\n" + 
			"\n" + 
			"  public static <T, X extends Exception> void close( Closer<T, X> closer, T it ) throws X {\n" + 
			"    closer.closeIt(it);\n" + 
			"  }\n" + 
			"\n" + 
			"  public static void main(String[] args) throws IOException {\n" + 
			"    InputStream in = new ByteArrayInputStream(\"hello\".getBytes());\n" + 
			"    close( x -> x.close(), in );\n" +
			"    close( InputStream::close, in );\n" + 
			"    close( (Closer<InputStream, IOException>)InputStream::close, in );\n" + 
			"  }\n" +
			"}\n"
		});
}
// incompatible exceptions prevent suitable inference of exception type
public void testBug429430a() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"import java.io.*;\n" +
			"@SuppressWarnings(\"serial\") class EmptyStream extends Exception {}\n" + 
			"public class Main {\n" + 
			"  public static interface Closer<T, X extends Exception> {\n" + 
			"    void closeIt(T it) throws X;\n" + 
			"  }\n" + 
			"\n" + 
			"  public static <T, X extends Exception> void close( Closer<T, X> closer, T it ) throws X {\n" + 
			"    closer.closeIt(it);\n" + 
			"  }\n" + 
			"\n" + 
			"  public static void main(String[] args) throws IOException, EmptyStream {\n" + 
			"    InputStream in = new ByteArrayInputStream(\"hello\".getBytes());\n" + 
			"    close( x ->  { if (in.available() == 0) throw new EmptyStream(); x.close(); }, in );\n" + 
			"  }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Main.java (at line 14)\n" + 
		"	close( x ->  { if (in.available() == 0) throw new EmptyStream(); x.close(); }, in );\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type Exception\n" + 
		"----------\n");
}
// one of two incompatible exceptions is caught
public void testBug429430b() {
	runConformTest(
		new String[] {
			"Main.java",
			"import java.io.*;\n" +
			"@SuppressWarnings(\"serial\") class EmptyStream extends Exception {}\n" + 
			"public class Main {\n" + 
			"  public static interface Closer<T, X extends Exception> {\n" + 
			"    void closeIt(T it) throws X;\n" + 
			"  }\n" + 
			"\n" + 
			"  public static <T, X extends Exception> void close( Closer<T, X> closer, T it ) throws X {\n" + 
			"    closer.closeIt(it);\n" + 
			"  }\n" + 
			"\n" + 
			"  public static void main(String[] args) throws EmptyStream {\n" + 
			"    InputStream in = new ByteArrayInputStream(\"hello\".getBytes());\n" + 
			"    close( x ->  {\n" + 
			"			try {\n" + 
			"				x.close();\n" + 
			"			} catch (IOException ioex) { throw new EmptyStream(); } \n" + 
			"		}," +
			"		in);\n" + 
			"  }\n" +
			"}\n"
		});
}
public void testBug429430b2() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"@SuppressWarnings(\"serial\") class EmptyStream extends Exception {}\n" +
			"public class X {\n" +
			"  public static interface Closer<T, V extends Exception> {\n" +
			"    void closeIt(T it) throws V;\n" +
			"  }\n" +
			"\n" +
			"  public static void close( Closer<InputStream, EmptyStream> closer, InputStream it ) throws EmptyStream {\n" +
			"    closer.closeIt(it);\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) throws EmptyStream {\n" +
			"    InputStream in = new ByteArrayInputStream(\"hello\".getBytes());\n" +
			"    close( x ->  {\n" +
			"			if (x == null)\n" +
			"				throw new IOException();\n" +
			"			else \n" +
			"				throw new EmptyStream(); \n" +
			"		},\n" +
			"		in);\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 16)\n" + 
		"	throw new IOException();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 18)\n" + 
		"	throw new EmptyStream(); \n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" + 
		"----------\n");
}
// ensure type annotation on exception doesn't confuse the inference
public void testBug429430c() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
	runConformTest(
		new String[] {
			"Main.java",
			"import java.io.*;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.TYPE_USE) @interface Severe {}\n" + 
			"public class Main {\n" + 
			"  public static interface Closer<T, X extends Exception> {\n" + 
			"    void closeIt(T it) throws X;\n" + 
			"  }\n" + 
			"\n" + 
			"  public static <T, X extends Exception> void close( Closer<T, X> closer, T it ) throws X {\n" + 
			"    closer.closeIt(it);\n" + 
			"  }\n" + 
			"\n" +
			"  static @Severe IOException getException() { return new IOException(\"severe\"); }\n" + 
			"  public static void main(String[] args) throws IOException {\n" + 
			"    InputStream in = new ByteArrayInputStream(\"hello\".getBytes());\n" + 
			"    close( x -> {\n" +
			"			if (in.available() > 0)\n" +
			"				x.close();\n" +
			"			else\n" +
			"				throw getException();\n" +
			"		},\n" +
			"		in);\n" + 
			"  }\n" +
			"}\n"
		},
		options);
}
public void testBug429490() {
	runConformTest(
		new String[] {
			"Junk11.java",
			"public class Junk11 {\n" + 
			"    class Observable<T> {}\n" + 
			"    class ObservableValue<T> {}\n" + 
			"    interface InvalidationListener {\n" + 
			"        public void invalidated(Observable observable);\n" + 
			"    }\n" + 
			"    public interface ChangeListener<T> {\n" + 
			"        void changed(ObservableValue<? extends T> observable, T oldValue, T newValue);\n" + 
			"    }\n" + 
			"    class ExpressionHelper<T> {}\n" + 
			"    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, InvalidationListener listener) {\n" + 
			"        return helper;\n" + 
			"    }\n" + 
			"    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, ChangeListener<? super T> listener) {\n" + 
			"        return helper;\n" + 
			"    }\n" + 
			"    void junk() {\n" + 
			"        addListener(null, null, new InvalidationListener () {\n" + 
			"            public void invalidated(Observable o) {throw new RuntimeException();}\n" + 
			"        });\n" + 
			"        addListener(null, null, (o) -> {throw new RuntimeException();});\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug429424() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" + 
			"public class X {\n" + 
			"    public static void main (String[] args) {\n" + 
			"        List<String> list = new ArrayList<>();\n" + 
			"        list.addAll(X.newArrayList());\n" + 
			"        System.out.println(list);\n" + 
			"    }\n" + 
			"    \n" + 
			"    public static <T> List<T> newArrayList () {\n" + 
			"        return new ArrayList<T>();\n" + 
			"    }\n" + 
			"}\n" + 
			"\n"
		});
}
public void testBug426537() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(J[] list, I<J<?>> i) {\n" + 
			"		sort(list, i);\n" + 
			"	}\n" + 
			"	\n" + 
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
public void testBug426537b() {
	runConformTest(
		new String[] {
			"Test.java",
			"interface C<T, A, R> {}\n" + 
			"\n" + 
			"class MImpl<K, V> {}\n" + 
			"\n" + 
			"interface S<T> { T get(); }\n" + 
			"\n" + 
			"public class Test {\n" + 
			"	static <T, K, D> C<T, ?, MImpl<K, D>> m1() {\n" + 
			"        return m2(MImpl::new);\n" + 
			"    }\n" + 
			"    \n" + 
			"    static <T, K, D, M extends MImpl<K, D>> C<T, ?, M> m2(S<M> s) {\n" + 
			"    	return null;\n" + 
			"    }\n" + 
			"}\n" + 
			"\n"
		});
}
public void testBug426537c() {
	// touching MImpl#RAW before type inference we got undesired results from #typeArguments() 
	runConformTest(
		new String[] {
			"Ups.java",
			"public class Ups {\n" + 
			"    static Object innocent(MImpl o) {\n" + 
			"            return o.remove(\"nice\");\n" + // method lookup triggers initialization of the RawTypeBinding.
			"    }\n" + 
			"}\n",
			"Test.java",
			"interface S<T> { T get(); }\n" + 
			"interface F<T, R> { R apply(T t); }\n" + 
			"interface C<T, A, R> { }\n" + 
			"interface IM<K,V> {}\n" + 
			"class MImpl<K,V>  implements IM<K,V> { \n" + 
			"	public V remove(Object key) { return null; } \n" + 
			"}\n" + 
			"public final class Test {\n" + 
			"\n" + 
			"    static <T, K, A, D>\n" + 
			"    C<T, ?, IM<K, D>> m1(F<? super T, ? extends K> f, C<? super T, A, D> c) {\n" + 
			"        return m2(f, MImpl::new, c);\n" + 
			"    }\n" + 
			"\n" + 
			"    static <T, K, D, A, M extends IM<K, D>>\n" + 
			"    C<T, ?, M> m2(F<? super T, ? extends K> classifier,\n" + 
			"                                  S<M> mapFactory,\n" + 
			"                                  C<? super T, A, D> downstream) {\n" + 
			"    	return null;\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug429203() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"DTest.java",
			"import java.util.function.Function;\n" + 
			"\n" + 
			"\n" + 
			"public class DTest<T> {\n" + 
			"	public DTest(Function<T, T> func) { }\n" + 
			"	\n" + 
			"	public DTest(DTest<Integer> dti) {}\n" + 
			"	public DTest() {}\n" + 
			"	\n" + 
			"	public static void main(String[] args) {\n" + 
			"		DTest<String> t1 = new DTest<String>(new DTest<Integer>());\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in DTest.java (at line 11)\n" + 
		"	DTest<String> t1 = new DTest<String>(new DTest<Integer>());\n" + 
		"	                       ^^^^^\n" + 
		"Redundant specification of type arguments <String>\n" + 
		"----------\n", 
		null, true, customOptions);
}
public void testBug430296() {
	runNegativeTest(
		new String[] {
			"AnnotationCollector.java",
			"import java.lang.annotation.*;\n" + 
			"import java.util.*;\n" + 
			"import java.util.function.*;\n" + 
			"import java.util.stream.*;\n" + 
			"\n" + 
			"public abstract class AnnotationCollector {\n" + 
			"        \n" + 
			"        Map<String, Person> test2(Stream<Person> persons) {\n" + 
			"                return persons.collect(Collectors.toMap((Person p) -> p.getLastName(),\n" + 
			"                                                                Function::identity,\n" + 
			"                                                        (p1, p2) -> p1));\n" + 
			"        }\n" + 
			"}\n" + 
			"\n" + 
			"class Person {\n" + 
			"        String getLastName() { return \"\"; }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in AnnotationCollector.java (at line 9)\n" + 
		"	return persons.collect(Collectors.toMap((Person p) -> p.getLastName(),\n" + 
		"	                                  ^^^^^\n" + 
		"The method toMap(Function<? super T,? extends K>, Function<? super T,? extends U>, BinaryOperator<U>) in the type Collectors is not applicable for the arguments ((Person p) -> {}, Function::identity, BinaryOperator<U>)\n" + 
		"----------\n" + 
		"2. ERROR in AnnotationCollector.java (at line 9)\n" + 
		"	return persons.collect(Collectors.toMap((Person p) -> p.getLastName(),\n" + 
		"	                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Function<Person,? extends K> to Function<? super T,? extends K>\n" + 
		"----------\n" + 
		"3. ERROR in AnnotationCollector.java (at line 10)\n" + 
		"	Function::identity,\n" + 
		"	^^^^^^^^^^^^^^^^^^\n" + 
		"The type Function does not define identity(T) that is applicable here\n" + 
		"----------\n");
}
public void testBug430759() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.beans.*;\n" + 
			"import java.util.*;\n" + 
			"import java.util.function.*;\n" + 
			"\n" + 
			"public abstract class Test<T,R> implements Function<T,R> {\n" + 
			"\n" + 
			"  public static final <T,R> Test<T,R> getTest() {\n" + 
			"    return new Test<T,R>() {\n" + 
			"      protected final Map<T,ResultSupplier> results = Collections.synchronizedMap(new HashMap<T,ResultSupplier>());\n" + 
			"      @Override\n" + 
			"      public R apply(final T t) {\n" + 
			"        ResultSupplier result = results.get(t);\n" + 
			"        return result.get();\n" + 
			"      }\n" + 
			"      class ResultSupplier implements Supplier<R> {\n" + 
			"        @Override\n" + 
			"        public synchronized R get() {\n" + 
			"          return null;\n" + 
			"        }\n" + 
			"      }\n" + 
			"    };\n" + 
			"  }\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431577 [1.8][bytecode] Bad type on operand stack (different than Bug 429733)
public void testBug431577() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.function.Function;\n" + 
			"import java.util.function.IntFunction;\n" + 
			"public class Test<R> {\n" + 
			"	public static void main(String[] args) {\n" + 
			"	new Test<>().test((Integer i) -> null);\n" + 
			"	}\n" + 
			"	<T> void test(Function<T, R> f) {\n" + 
			"	}\n" + 
			"	void test(int i, IntFunction<R> f) {\n" + 
			"		new State<>(new Val<>(i));\n" + 
			"	}\n" + 
			"	static class State<R> {\n" + 
			"		State(Val<?> o) {\n" + 
			"		}\n" + 
			"	}\n" + 
			"	static class Val<T> {\n" + 
			"		Val(T t) {}\n" + 
			"	}\n" + 
			"}"
	});
}
public void testBug432110() {
	runConformTest(
		new String[] {
			"Bug.java",
			"import java.util.List;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"class Bug\n" + 
			"{\n" + 
			"    // fully inline\n" + 
			"    // compiles successfully\n" + 
			"    Stream<? extends Integer> flatten1(\n" + 
			"        final Stream<List<Integer>> input)\n" + 
			"    {\n" + 
			"        return input.flatMap(item -> item.stream().map(value -> value));\n" + 
			"    }\n" + 
			"\n" + 
			"    // lambda using braces\n" + 
			"    // compiles with error in eclipse, successfully with javac\n" + 
			"    Stream<? extends Integer> flatten2(\n" + 
			"        final Stream<List<Integer>> input)\n" + 
			"    {\n" + 
			"        return input.flatMap(item -> {\n" + 
			"            return item.stream().map(value -> value);\n" + 
			"        });\n" + 
			"    }\n" + 
			"\n" + 
			"    // without map step\n" + 
			"    // compiles successfully\n" + 
			"    Stream<? extends Integer> flatten3(\n" + 
			"        final Stream<List<Integer>> input)\n" + 
			"    {\n" + 
			"        return input.flatMap(item -> {\n" + 
			"            return item.stream();\n" + 
			"        });\n" + 
			"    }\n" + 
			"\n" + 
			"    // with map step, but not inline\n" + 
			"    // compiles successfully\n" + 
			"    Stream<? extends Integer> flatten4(\n" + 
			"        final Stream<List<Integer>> input)\n" + 
			"    {\n" + 
			"        return input.flatMap(item -> {\n" + 
			"            final Function<? super Integer, ? extends Integer> mapper = value -> value;\n" + 
			"            return item.stream().map(mapper);\n" + 
			"        });\n" + 
			"    }\n" + 
			"\n" + 
			"    // with map step, but outer lambda is not inline\n" + 
			"    // compiles successfully\n" + 
			"    Stream<? extends Integer> flatten5(\n" + 
			"        final Stream<List<Integer>> input)\n" + 
			"    {\n" + 
			"        final Function<? super List<Integer>, ? extends Stream<? extends Integer>> func = item -> {\n" + 
			"            return item.stream().map(value -> value);\n" + 
			"        };\n" + 
			"        return input.flatMap(func);\n" + 
			"    }\n" + 
			"}\n"});
}
public void testBug433158() {
	runNegativeTest(
		new String[] {
			"CollectorsMaps.java",
			"\n" + 
			"import java.util.List;\n" + 
			"import java.util.Map;\n" + 
			"import static java.util.stream.Collectors.*;\n" + 
			"\n" + 
			"public class CollectorsMaps {/*Q*/\n" + 
			"	private static class Pair<L, R> {\n" + 
			"		public final L lhs; public final R rhs;\n" + 
			"		public Pair(L lhs, R rhs) { this.lhs = lhs; this.rhs = rhs; }\n" + 
			"		public R rhs() { return rhs; }\n" + 
			"		public L lhs() { return lhs; }\n" + 
			"		public <N> Pair<N, R> keepingRhs(N newLhs) { return new Pair<>(newLhs, rhs); }\n" + 
			"		/*E*/}\n" + 
			"\n" + 
			"	static Map<String, List<String>> invert(Map<String, List<String>> packages) {\n" + 
			"		return packages.entrySet().stream().map(e -> new Pair<>(e.getValue(), e.getKey())).flatMap(\n" + 
			"			//The method collect(Collector<? super Object,A,R>) in the type Stream<Object>\n" + 
			"			//is not applicable for the arguments \n" + 
			"			//(Collector<CollectorsMaps.Pair<String,String>,capture#3-of ?,Map<String,List<String>>>)\n" + 
			"		  p -> p.lhs.stream().map(p::keepingRhs)).collect(\n" + 
			"		  groupingBy(Pair<String, String>::lhs, mapping(Pair<String, String>::rhs, toList())));\n" + 
			"	}\n" + 
			"}\n"
		},
		"");
}
public void testBug432626() {
	runConformTest(
		new String[] {
			"StreamInterface2.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.HashMap;\n" + 
			"import java.util.Map;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.function.Supplier;\n" + 
			"import java.util.stream.Collector;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"public interface StreamInterface2 {\n" + 
			"\n" + 
			"	static <T, E extends Exception, K> Map<K, ArrayList<T>> terminalAsMapToList(\n" + 
			"	  Function<? super T, ? extends K> classifier,\n" + 
			"	  Supplier<Stream<T>> supplier,\n" + 
			"	  Class<E> classOfE) throws E {\n" + 
			"		return terminalAsCollected(classOfE, Collectors.groupingBy(\n" + 
			"			  classifier,\n" + 
			"			  //This is OK:\n" + 
			"			  //Redundant specification of type arguments <K, ArrayList<T>>\n" + 
			"			  () -> new HashMap<K, ArrayList<T>>(),\n" + 
			"			  Collector.<T, ArrayList<T>> of(\n" + 
			"			    () -> new ArrayList<>(),\n" + 
			"			    (left, value) -> left.add(value),\n" + 
			"			    (left, right) -> combined(left, right))), supplier);\n" + 
			"	}\n" + 
			"	static <T, E extends Exception, K> Map<K, ArrayList<T>> terminalAsMapToList2(\n" + 
			"	  Function<? super T, ? extends K> classifier,\n" + 
			"	  Supplier<Stream<T>> supplier,\n" + 
			"	  Class<E> classOfE) throws E {\n" + 
			"		//After removing type arguments, ECJ shows error, javac doesn't:\n" + 
			"		//Type mismatch: cannot convert from HashMap<capture#2-of ? extends K,ArrayList<T>> to Map<K,ArrayList<T>>\n" + 
			"		return terminalAsCollected(classOfE, Collectors.groupingBy(\n" + 
			"			  classifier,\n" + 
			"			  () -> new HashMap<>(),\n" + 
			"			  Collector.<T, ArrayList<T>> of(\n" + 
			"			    () -> new ArrayList<>(),\n" + 
			"			    (left, value) -> left.add(value),\n" + 
			"			    (left, right) -> combined(left, right))), supplier);\n" + 
			"	}\n" + 
			"	static <E extends Exception, T, M> M terminalAsCollected(\n" + 
			"	  Class<E> classOfE,\n" + 
			"	  Collector<T, ?, M> collector,\n" + 
			"	  Supplier<Stream<T>> supplier) throws E {\n" + 
			"		try(Stream<T> s = supplier.get()) {\n" + 
			"			return s.collect(collector);\n" + 
			"		} catch(RuntimeException e) {\n" + 
			"			throw unwrapCause(classOfE, e);\n" + 
			"		}\n" + 
			"	}\n" + 
			"	static <E extends Exception> E unwrapCause(Class<E> classOfE, RuntimeException e) throws E {\n" + 
			"		Throwable cause = e.getCause();\n" + 
			"		if(classOfE.isInstance(cause) == false) {\n" + 
			"			throw e;\n" + 
			"		}\n" + 
			"		throw classOfE.cast(cause);\n" + 
			"	}\n" + 
			"	static <T> ArrayList<T> combined(ArrayList<T> left, ArrayList<T> right) {\n" + 
			"		left.addAll(right);\n" + 
			"		return left;\n" + 
			"	}\n" +
			"}\n"
		});
}
public void testBug432626_reduced() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.stream.Collector;\n" +
			"import java.util.stream.Collectors;\n" +
			"public interface X {\n" +
			"	static <T, K> Map<K, ArrayList<T>> terminalAsMapToList(Function<? super T, ? extends K> classifier)  {\n" +
			"		return terminalAsCollected(Collectors.groupingBy(\n" +
			"			  classifier,\n" +
			"			  () -> new HashMap<>(),\n" +
			"			  (Collector<T,ArrayList<T>,ArrayList<T>>) null));\n" +
			"	}\n" +
			"	static <T, M> M terminalAsCollected(Collector<T, ?, M> collector) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433825 [1.8][compiler] Internal compiler error: NullPointerException in AllocationExpression#resolvePart3
public void testBug433825() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"import java.util.List;\n" + 
			"public class X {\n" + 
			"  public static void main(String[] args) {\n" + 
			"  }\n" + 
			"  public void bla() {\n" + 
			"    boolean b = Boolean.TRUE.booleanValue();\n" + 
			"    List<String> c1 = new ArrayList<>();\n" + 
			"    new Bar(b ? c1 : new ArrayList<>()); // this line crashes ecj (4.4 I20140429-0800), but not ecj (eclipse 3.8.2) and javac\n" + 
			"  }\n" + 
			"  private static class Bar {\n" + 
			"	  public Bar(Collection<?> col) { }\n" + 
			"  }\n" + 
			"}"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433825 [1.8][compiler] Internal compiler error: NullPointerException in AllocationExpression#resolvePart3
public void testBug433825a() {
	this.runNegativeTest(
		new String[] {
			"X.java", 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"import java.util.List;\n" + 
			"public class X {\n" + 
			"  public static void main(String[] args) {\n" + 
			"  }\n" + 
			"  public void bla() {\n" + 
			"    boolean b = Boolean.TRUE.booleanValue();\n" + 
			"    new Bar(b ? 0 : new ArrayList<>());\n" + 
			"  }\n" + 
			"  private static class Bar {\n" + 
			"	  public Bar(Collection<String> col) { }\n" + 
			"  }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	new Bar(b ? 0 : new ArrayList<>());\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The constructor X.Bar((b ? 0 : new ArrayList<>())) is undefined\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	new Bar(b ? 0 : new ArrayList<>());\n" + 
		"	            ^\n" + 
		"Type mismatch: cannot convert from int to Collection<String>\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 12)\n" + 
		"	public Bar(Collection<String> col) { }\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The constructor X.Bar(Collection<String>) is never used locally\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435462 [1.8] NPE in codegen with nested conditional and allocation expressions
public void testBug435462() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"import java.util.List;\n" + 
			"public class X {\n" + 
			"  public static void main(String[] args) {\n" + 
			"  }\n" + 
			"  public void bla() {\n" + 
			"    boolean b = Boolean.TRUE.booleanValue();\n" + 
			"    List<String> c1 = new ArrayList<>();\n" + 
			"    new Bar(b ? new ArrayList<>(b ? new ArrayList<>() : c1) : c1);\n" + 
			"  }\n" + 
			"  private static class Bar {\n" + 
			"	  public Bar(Collection<?> col) { }\n" + 
			"  }\n" + 
			"}"
	});
}
public void testBug437007() {
	runConformTest(
		new String[] {
			"ExecutorTests.java",
			"import java.util.*;\n" + 
			"\n" + 
			"public class ExecutorTests {\n" + 
			"    List<Runnable> tasks = Arrays.asList(\n" + 
			"            () -> {\n" + 
			"                System.out.println(\"task1 start\");\n" + 
			"            }\n" + 
			"    );\n" + 
			"\n" + 
			"    public void executeInSync(){\n" + 
			"        tasks.stream().forEach(Runnable::run);\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug435689() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.function.*;\n" +
			"class Foo<T> {\n" + 
			"  <U> void apply(Function<T, Consumer<U>> bar) {}\n" + 
			"}\n" + 
			"\n" + 
			"class Bar {\n" + 
			"  void setBar(String bar){}\n" + 
			"}\n" +
			"public class Test {\n" +
			"	void test() {\n" +
			"		new Foo<Bar>().apply(bar -> bar::setBar);\n" + 
			"	}\n" +
			"}\n"
		});
}
public void testBug433845() {
	runNegativeTest(
		new String[] {
			"test/Test.java",
			"package test;\n" + 
			"\n" + 
			"\n" + 
			"public abstract class Test {\n" + 
			"	public interface MUIElement {\n" + 
			"		\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface Listener<W extends WWidget<?>> {\n" + 
			"		public void call(Event<W> event);\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static class Event<W extends WWidget<?>> {\n" + 
			"		\n" + 
			"	}\n" + 
			"	\n" + 
			"	public interface WWidget<M extends MUIElement> {\n" + 
			"		public void set(Listener<? extends WWidget<M>> handler);\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static abstract class A<M extends MUIElement, W extends WWidget<M>> {\n" + 
			"		\n" + 
			"		public final W createWidget(final M element) {\n" + 
			"			W w = get(); \n" + 
			"			// works\n" + 
			"			w.set((Event<W>e) -> call(e));\n" + 
			"			// fails\n" + 
			"			w.set(this::call);\n" + 
			"			// fails\n" + 
			"			w.set((e) -> call(e));\n" + 
			"			return w;\n" + 
			"		}\n" + 
			"		\n" + 
			"		private W get() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"		\n" + 
			"		private void call(Event<W> event) {\n" + 
			"			\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in test\\Test.java (at line 28)\n" + 
		"	w.set(this::call);\n" + 
		"	  ^^^\n" + 
		"The method set(Test.Listener<? extends Test.WWidget<M>>) in the type Test.WWidget<M> is not applicable for the arguments (this::call)\n" + 
		"----------\n" + 
		"2. ERROR in test\\Test.java (at line 28)\n" + 
		"	w.set(this::call);\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The type Test.A<M,W> does not define call(Test.Event<Test.WWidget<M>>) that is applicable here\n" + 
		"----------\n" + 
		"3. ERROR in test\\Test.java (at line 30)\n" + 
		"	w.set((e) -> call(e));\n" + 
		"	             ^^^^\n" + 
		"The method call(Test.Event<W>) in the type Test.A<M,W> is not applicable for the arguments (Test.Event<Test.WWidget<M>>)\n" + 
		"----------\n");
}
public void testBug435187() {
	runNegativeTest(
		new String[] {
			"ExtractLocalLambda.java",
			"\n" + 
			"import java.util.List;\n" + 
			"import java.util.Map;\n" + 
			"import java.util.Map.Entry;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.stream.Collector;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"public class ExtractLocalLambda {\n" + 
			"	static Stream<Entry<List<String>, String>> map;\n" + 
			"	static Collector<Entry<String, String>, ?, Map<String, List<String>>> groupingBy;\n" + 
			"	private static Stream<String> stream(Entry<List<String>, String> p) {		return null;	}\n" + 
			"	private static Entry<String, String> keep(Entry<List<String>, String> p, String leftHS2) {		return null;	}\n" + 
			"\n" + 
			"	static Map<String, List<String>> beforeRefactoring() {\n" + 
			"		// Extract local variable from the parameter to flatMap:\n" + 
			"		return map.flatMap(\n" + 
			"				p -> stream(p).map(leftHS -> {\n" + 
			"					String leftHS2 = leftHS;\n" + 
			"					return keep(p, leftHS2);\n" + 
			"				})\n" + 
			"		).collect(groupingBy);\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in ExtractLocalLambda.java (at line 5)\n" + 
		"	import java.util.function.Function;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.function.Function is never used\n" + 
		"----------\n");
}
public void testBug435767() {
	runConformTest(
		new String[] {
			"DummyClass.java",
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"import java.util.stream.*;\n" +
			"public class DummyClass {\n" + 
			"\n" + 
			"	public void method() {\n" + 
			"\n" + 
			"		// Cases where there is no error\n" + 
			"		final Supplier<Set<String>> suppliers = this.memoize(() -> new HashSet<>());\n" + 
			"\n" + 
			"		final Supplier<Map<Object, Object>> noMemoize = () -> suppliers.get().stream()\n" + 
			"				.filter(path -> path.startsWith(\"\"))\n" + 
			"				.collect(Collectors.toMap(path -> this.getKey(path), path -> this.getValue(path)));\n" + 
			"\n" + 
			"		// Case where there is errors.\n" + 
			"		final Supplier<Map<Object, Object>> memoize = this.memoize(() -> suppliers.get().stream()\n" + 
			"				.filter(path -> path.startsWith(\"\"))\n" + 
			"				.collect(Collectors.toMap(path -> this.getKey(path), path -> this.getValue(path))));\n" + 
			"\n" + 
			"		// Error message are : Description\n" + 
			"		// Resource	Path	Location	Type\n" + 
			"		// The method getKey(String) in the type DummyClass is not applicable for the arguments (Object)	DummyClass.java line 23	Java Problem\n" + 
			"		// The method getValue(String) in the type DummyClass is not applicable for the arguments (Object)	DummyClass.java line 23	Java Problem\n" + 
			"\n" + 
			"	}\n" + 
			"\n" + 
			"	private <T> Supplier<T> memoize(final Supplier<T> delegate) {\n" + 
			"		return delegate;\n" + 
			"	}\n" + 
			"\n" + 
			"	private Object getKey(final String path) {\n" + 
			"		return path;\n" + 
			"	}\n" + 
			"\n" + 
			"	private Object getValue(final String path) {\n" + 
			"		return path;\n" + 
			"	}\n" + 
			"}\n"
		},
		"");
}
public void testBug434483() {
	runConformTest(
		new String[] {
			"Foo.java",
			"import java.util.*;\n" +
			"public class Foo {\n" + 
			"	\n" + 
			"  // Similar to Guava's newLinkedList()\n" + 
			"  public static <E> LinkedList<E> newLinkedList() {\n" + 
			"    return new LinkedList<E>();\n" + 
			"  }\n" + 
			"	\n" + 
			"  private final ThreadLocal<Queue<String>> brokenQueue = ThreadLocal.withInitial(Foo::newLinkedList);\n" + 
			"	\n" + 
			"  private final ThreadLocal<Queue<String>> workingQueue1 = ThreadLocal.withInitial(Foo::<String>newLinkedList);\n" + 
			"	\n" + 
			"  private final ThreadLocal<Queue<String>> workingQueue2 = ThreadLocal.withInitial(() -> Foo.<String>newLinkedList());\n" + 
			"\n" + 
			"}\n"
		});
}
public void testBug441734() {
	runConformTest(
		new String[] {
			"Example.java",
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"class Example {\n" + 
			"    void foo(Iterable<Number> x) { }\n" + 
			"\n" + 
			"    <T> void bar(Consumer<Iterable<T>> f) { }\n" + 
			"\n" + 
			"    void test() {\n" + 
			"        //call 1: lambda w/argument type - OK\n" + 
			"        bar((Iterable<Number> x) -> foo(x));\n" + 
			"\n" + 
			"        //call 2: lambda w/explicit type - OK\n" + 
			"        this.<Number> bar(x -> foo(x));\n" + 
			"\n" + 
			"        //call 3: method ref w/explicit type - OK\n" + 
			"        this.<Number> bar(this::foo);\n" + 
			"\n" + 
			"        //call 4: lambda w/implicit type - correctly(?) fails*\n" + 
			"        //bar(x -> foo(x));\n" + 
			"\n" + 
			"        //call 5: method ref w/implicit type - BUG!\n" + 
			"        bar(this::foo); // errors!\n" + 
			"    }\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442245, [1.8][compiler?] These source files lead eclipse to hang (even just on copy/paste)
public void testBug442245() {
	runConformTest(
		new String[] {
			"test/Pattern.java",
			"package test;\n" +
			"import test.Tuples.Tuple;\n" +
			"import test.Tuples.Tuple1;\n" +
			"import test.Tuples.Tuple10;\n" +
			"import test.Tuples.Tuple11;\n" +
			"import test.Tuples.Tuple12;\n" +
			"import test.Tuples.Tuple13;\n" +
			"import test.Tuples.Tuple2;\n" +
			"import test.Tuples.Tuple3;\n" +
			"import test.Tuples.Tuple4;\n" +
			"import test.Tuples.Tuple5;\n" +
			"import test.Tuples.Tuple6;\n" +
			"import test.Tuples.Tuple7;\n" +
			"import test.Tuples.Tuple8;\n" +
			"import test.Tuples.Tuple9;\n" +
			"\n" +
			"public interface Pattern<R extends Tuple> {\n" +
			"\n" +
			"	boolean isApplicable(Object o);\n" +
			"\n" +
			"	R apply(Object o);\n" +
			"\n" +
			"	static <T, R extends Tuple> Pattern<R> of(Decomposition<T, R> decomposition, R prototype) {\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1> Pattern<Tuple1<E1>> of(Decomposition<T, Tuple1<E1>> decomposition, E1 e1) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2> Pattern<Tuple2<E1, E2>> of(Decomposition<T, Tuple2<E1, E2>> decomposition, E1 e1, E2 e2) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3> Pattern<Tuple3<E1, E2, E3>> of(Decomposition<T, Tuple3<E1, E2, E3>> decomposition, E1 e1,\n" +
			"			E2 e2, E3 e3) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4> Pattern<Tuple4<E1, E2, E3, E4>> of(\n" +
			"			Decomposition<T, Tuple4<E1, E2, E3, E4>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4, E5> Pattern<Tuple5<E1, E2, E3, E4, E5>> of(\n" +
			"			Decomposition<T, Tuple5<E1, E2, E3, E4, E5>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4, E5 e5) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4, E5, E6> Pattern<Tuple6<E1, E2, E3, E4, E5, E6>> of(\n" +
			"			Decomposition<T, Tuple6<E1, E2, E3, E4, E5, E6>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4, E5 e5, E6 e6) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4, E5, E6, E7> Pattern<Tuple7<E1, E2, E3, E4, E5, E6, E7>> of(\n" +
			"			Decomposition<T, Tuple7<E1, E2, E3, E4, E5, E6, E7>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4, E5 e5,\n" +
			"			E6 e6, E7 e7) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4, E5, E6, E7, E8> Pattern<Tuple8<E1, E2, E3, E4, E5, E6, E7, E8>> of(\n" +
			"			Decomposition<T, Tuple8<E1, E2, E3, E4, E5, E6, E7, E8>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4, E5 e5,\n" +
			"			E6 e6, E7 e7, E8 e8) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9> Pattern<Tuple9<E1, E2, E3, E4, E5, E6, E7, E8, E9>> of(\n" +
			"			Decomposition<T, Tuple9<E1, E2, E3, E4, E5, E6, E7, E8, E9>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4,\n" +
			"			E5 e5, E6 e6, E7 e7, E8 e8, E9 e9) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10> Pattern<Tuple10<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10>> of(\n" +
			"			Decomposition<T, Tuple10<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10>> decomposition, E1 e1, E2 e2, E3 e3,\n" +
			"			E4 e4, E5 e5, E6 e6, E7 e7, E8 e8, E9 e9, E10 e10) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> Pattern<Tuple11<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>> of(\n" +
			"			Decomposition<T, Tuple11<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>> decomposition, E1 e1, E2 e2, E3 e3,\n" +
			"			E4 e4, E5 e5, E6 e6, E7 e7, E8 e8, E9 e9, E10 e10, E11 e11) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> Pattern<Tuple12<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>> of(\n" +
			"			Decomposition<T, Tuple12<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>> decomposition, E1 e1, E2 e2,\n" +
			"			E3 e3, E4 e4, E5 e5, E6 e6, E7 e7, E8 e8, E9 e9, E10 e10, E11 e11, E12 e12) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12));\n" +
			"	}\n" +
			"\n" +
			"	static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> Pattern<Tuple13<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>> of(\n" +
			"			Decomposition<T, Tuple13<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>> decomposition, E1 e1,\n" +
			"			E2 e2, E3 e3, E4 e4, E5 e5, E6 e6, E7 e7, E8 e8, E9 e9, E10 e10, E11 e11, E12 e12, E13 e13) {\n" +
			"		return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13));\n" +
			"	}\n" +
			"\n" +
			"	static interface Decomposition<T, R extends Tuple> {\n" +
			"\n" +
			"		R apply(T t);\n" +
			"	}\n" +
			"}\n",
			"test/Tuples.java", 
			"package test;\n"+
			"\n"+
			"import java.io.Serializable;\n"+
			"import java.util.Objects;\n"+
			"import java.util.stream.Collectors;\n"+
			"import java.util.stream.Stream;\n"+
			"\n"+
			"public final class Tuples {\n"+
			"\n"+
			"	private Tuples() {\n"+
			"		throw new AssertionError(Tuples.class.getName() + \" is not intended to be instantiated.\");\n"+
			"	}\n"+
			"\n"+
			"	public static Tuple0 of() {\n"+
			"		return Tuple0.instance();\n"+
			"	}\n"+
			"\n"+
			"	public static <T> Tuple1<T> of(T t) {\n"+
			"		return new Tuple1<>(t);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {\n"+
			"		return new Tuple2<>(t1, t2);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {\n"+
			"		return new Tuple3<>(t1, t2, t3);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 t1, T2 t2, T3 t3, T4 t4) {\n"+
			"		return new Tuple4<>(t1, t2, t3, t4);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {\n"+
			"		return new Tuple5<>(t1, t2, t3, t4, t5);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {\n"+
			"		return new Tuple6<>(t1, t2, t3, t4, t5, t6);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5,\n"+
			"			T6 t6, T7 t7) {\n"+
			"		return new Tuple7<>(t1, t2, t3, t4, t5, t6, t7);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> of(T1 t1, T2 t2, T3 t3,\n"+
			"			T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) {\n"+
			"		return new Tuple8<>(t1, t2, t3, t4, t5, t6, t7, t8);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T1 t1, T2 t2,\n"+
			"			T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9) {\n"+
			"		return new Tuple9<>(t1, t2, t3, t4, t5, t6, t7, t8, t9);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(T1 t1,\n"+
			"			T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10) {\n"+
			"		return new Tuple10<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> of(\n"+
			"			T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11) {\n"+
			"		return new Tuple11<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> of(\n"+
			"			T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12) {\n"+
			"		return new Tuple12<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);\n"+
			"	}\n"+
			"\n"+
			"	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> of(\n"+
			"			T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12, T13 t13) {\n"+
			"		return new Tuple13<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);\n"+
			"	}\n"+
			"\n"+
			"	private static String stringify(Object... objects) {\n"+
			"		return Stream\n"+
			"				.of(objects)\n"+
			"				.map(o -> (o == null) ? \"null\" : o.toString())\n"+
			"				.collect(Collectors.joining(\", \", \"(\", \")\"));\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple0 implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = -8715576573413569748L;\n"+
			"\n"+
			"		private static final Tuple0 INSTANCE = new Tuple0();\n"+
			"\n"+
			"		private Tuple0() {\n"+
			"		}\n"+
			"\n"+
			"		public static Tuple0 instance() {\n"+
			"			return INSTANCE;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 0;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify();\n"+
			"		}\n"+
			"\n"+
			"		private Object readResolve() {\n"+
			"			return INSTANCE;\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple1<T> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = -8005498887610699234L;\n"+
			"\n"+
			"		public final T _1;\n"+
			"\n"+
			"		public Tuple1(T t) {\n"+
			"			this._1 = t;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 1;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple1)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple1<?> that = (Tuple1<?>) o;\n"+
			"				return Objects.equals(this._1, that._1);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple2<T1, T2> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = -1359843718617881431L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"\n"+
			"		public Tuple2(T1 t1, T2 t2) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 2;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple2)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple2<?, ?> that = (Tuple2<?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1) && Objects.equals(this._2, that._2);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple3<T1, T2, T3> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = 1353320010987934190L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"\n"+
			"		public Tuple3(T1 t1, T2 t2, T3 t3) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 3;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple3)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple3<?, ?, ?> that = (Tuple3<?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple4<T1, T2, T3, T4> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = -835853771811712181L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"\n"+
			"		public Tuple4(T1 t1, T2 t2, T3 t3, T4 t4) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 4;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple4)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple4<?, ?, ?, ?> that = (Tuple4<?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple5<T1, T2, T3, T4, T5> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = 8365094604388856720L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"		public final T5 _5;\n"+
			"\n"+
			"		public Tuple5(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"			this._5 = t5;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 5;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple5)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple5<?, ?, ?, ?, ?> that = (Tuple5<?, ?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4)\n"+
			"						&& Objects.equals(this._5, that._5);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4, _5);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4, _5);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple6<T1, T2, T3, T4, T5, T6> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = -5282391675740552818L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"		public final T5 _5;\n"+
			"		public final T6 _6;\n"+
			"\n"+
			"		public Tuple6(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"			this._5 = t5;\n"+
			"			this._6 = t6;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 6;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple6)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple6<?, ?, ?, ?, ?, ?> that = (Tuple6<?, ?, ?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4)\n"+
			"						&& Objects.equals(this._5, that._5)\n"+
			"						&& Objects.equals(this._6, that._6);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4, _5, _6);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4, _5, _6);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple7<T1, T2, T3, T4, T5, T6, T7> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = 6913366542759921153L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"		public final T5 _5;\n"+
			"		public final T6 _6;\n"+
			"		public final T7 _7;\n"+
			"\n"+
			"		public Tuple7(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"			this._5 = t5;\n"+
			"			this._6 = t6;\n"+
			"			this._7 = t7;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 7;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple7)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple7<?, ?, ?, ?, ?, ?, ?> that = (Tuple7<?, ?, ?, ?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4)\n"+
			"						&& Objects.equals(this._5, that._5)\n"+
			"						&& Objects.equals(this._6, that._6)\n"+
			"						&& Objects.equals(this._7, that._7);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4, _5, _6, _7);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = 117641715065938183L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"		public final T5 _5;\n"+
			"		public final T6 _6;\n"+
			"		public final T7 _7;\n"+
			"		public final T8 _8;\n"+
			"\n"+
			"		public Tuple8(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"			this._5 = t5;\n"+
			"			this._6 = t6;\n"+
			"			this._7 = t7;\n"+
			"			this._8 = t8;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 8;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple8)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple8<?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple8<?, ?, ?, ?, ?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4)\n"+
			"						&& Objects.equals(this._5, that._5)\n"+
			"						&& Objects.equals(this._6, that._6)\n"+
			"						&& Objects.equals(this._7, that._7)\n"+
			"						&& Objects.equals(this._8, that._8);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = -1578540921124551840L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"		public final T5 _5;\n"+
			"		public final T6 _6;\n"+
			"		public final T7 _7;\n"+
			"		public final T8 _8;\n"+
			"		public final T9 _9;\n"+
			"\n"+
			"		public Tuple9(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"			this._5 = t5;\n"+
			"			this._6 = t6;\n"+
			"			this._7 = t7;\n"+
			"			this._8 = t8;\n"+
			"			this._9 = t9;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 9;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple9)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple9<?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple9<?, ?, ?, ?, ?, ?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4)\n"+
			"						&& Objects.equals(this._5, that._5)\n"+
			"						&& Objects.equals(this._6, that._6)\n"+
			"						&& Objects.equals(this._7, that._7)\n"+
			"						&& Objects.equals(this._8, that._8)\n"+
			"						&& Objects.equals(this._9, that._9);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = 7991284808329690986L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"		public final T5 _5;\n"+
			"		public final T6 _6;\n"+
			"		public final T7 _7;\n"+
			"		public final T8 _8;\n"+
			"		public final T9 _9;\n"+
			"		public final T10 _10;\n"+
			"\n"+
			"		public Tuple10(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"			this._5 = t5;\n"+
			"			this._6 = t6;\n"+
			"			this._7 = t7;\n"+
			"			this._8 = t8;\n"+
			"			this._9 = t9;\n"+
			"			this._10 = t10;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 10;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple10)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4)\n"+
			"						&& Objects.equals(this._5, that._5)\n"+
			"						&& Objects.equals(this._6, that._6)\n"+
			"						&& Objects.equals(this._7, that._7)\n"+
			"						&& Objects.equals(this._8, that._8)\n"+
			"						&& Objects.equals(this._9, that._9)\n"+
			"						&& Objects.equals(this._10, that._10);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = 3493688489700741360L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"		public final T5 _5;\n"+
			"		public final T6 _6;\n"+
			"		public final T7 _7;\n"+
			"		public final T8 _8;\n"+
			"		public final T9 _9;\n"+
			"		public final T10 _10;\n"+
			"		public final T11 _11;\n"+
			"\n"+
			"		public Tuple11(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"			this._5 = t5;\n"+
			"			this._6 = t6;\n"+
			"			this._7 = t7;\n"+
			"			this._8 = t8;\n"+
			"			this._9 = t9;\n"+
			"			this._10 = t10;\n"+
			"			this._11 = t11;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 11;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple11)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple11<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple11<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4)\n"+
			"						&& Objects.equals(this._5, that._5)\n"+
			"						&& Objects.equals(this._6, that._6)\n"+
			"						&& Objects.equals(this._7, that._7)\n"+
			"						&& Objects.equals(this._8, that._8)\n"+
			"						&& Objects.equals(this._9, that._9)\n"+
			"						&& Objects.equals(this._10, that._10)\n"+
			"						&& Objects.equals(this._11, that._11);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> implements Tuple, Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = -175212910367376967L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"		public final T5 _5;\n"+
			"		public final T6 _6;\n"+
			"		public final T7 _7;\n"+
			"		public final T8 _8;\n"+
			"		public final T9 _9;\n"+
			"		public final T10 _10;\n"+
			"		public final T11 _11;\n"+
			"		public final T12 _12;\n"+
			"\n"+
			"		public Tuple12(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"			this._5 = t5;\n"+
			"			this._6 = t6;\n"+
			"			this._7 = t7;\n"+
			"			this._8 = t8;\n"+
			"			this._9 = t9;\n"+
			"			this._10 = t10;\n"+
			"			this._11 = t11;\n"+
			"			this._12 = t12;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 12;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple12)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple12<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple12<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4)\n"+
			"						&& Objects.equals(this._5, that._5)\n"+
			"						&& Objects.equals(this._6, that._6)\n"+
			"						&& Objects.equals(this._7, that._7)\n"+
			"						&& Objects.equals(this._8, that._8)\n"+
			"						&& Objects.equals(this._9, that._9)\n"+
			"						&& Objects.equals(this._10, that._10)\n"+
			"						&& Objects.equals(this._11, that._11)\n"+
			"						&& Objects.equals(this._12, that._12);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static final class Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> implements Tuple,\n"+
			"			Serializable {\n"+
			"\n"+
			"		private static final long serialVersionUID = 2027952127515234777L;\n"+
			"\n"+
			"		public final T1 _1;\n"+
			"		public final T2 _2;\n"+
			"		public final T3 _3;\n"+
			"		public final T4 _4;\n"+
			"		public final T5 _5;\n"+
			"		public final T6 _6;\n"+
			"		public final T7 _7;\n"+
			"		public final T8 _8;\n"+
			"		public final T9 _9;\n"+
			"		public final T10 _10;\n"+
			"		public final T11 _11;\n"+
			"		public final T12 _12;\n"+
			"		public final T13 _13;\n"+
			"\n"+
			"		public Tuple13(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12, T13 t13) {\n"+
			"			this._1 = t1;\n"+
			"			this._2 = t2;\n"+
			"			this._3 = t3;\n"+
			"			this._4 = t4;\n"+
			"			this._5 = t5;\n"+
			"			this._6 = t6;\n"+
			"			this._7 = t7;\n"+
			"			this._8 = t8;\n"+
			"			this._9 = t9;\n"+
			"			this._10 = t10;\n"+
			"			this._11 = t11;\n"+
			"			this._12 = t12;\n"+
			"			this._13 = t13;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int arity() {\n"+
			"			return 13;\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public boolean equals(Object o) {\n"+
			"			if (o == this) {\n"+
			"				return true;\n"+
			"			} else if (o == null || !(o instanceof Tuple13)) {\n"+
			"				return false;\n"+
			"			} else {\n"+
			"				final Tuple13<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple13<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;\n"+
			"				return Objects.equals(this._1, that._1)\n"+
			"						&& Objects.equals(this._2, that._2)\n"+
			"						&& Objects.equals(this._3, that._3)\n"+
			"						&& Objects.equals(this._4, that._4)\n"+
			"						&& Objects.equals(this._5, that._5)\n"+
			"						&& Objects.equals(this._6, that._6)\n"+
			"						&& Objects.equals(this._7, that._7)\n"+
			"						&& Objects.equals(this._8, that._8)\n"+
			"						&& Objects.equals(this._9, that._9)\n"+
			"						&& Objects.equals(this._10, that._10)\n"+
			"						&& Objects.equals(this._11, that._11)\n"+
			"						&& Objects.equals(this._12, that._12)\n"+
			"						&& Objects.equals(this._13, that._13);\n"+
			"			}\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public int hashCode() {\n"+
			"			return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13);\n"+
			"		}\n"+
			"\n"+
			"		@Override\n"+
			"		public String toString() {\n"+
			"			return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13);\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"	public static interface Tuple {\n"+
			"\n"+
			"		int arity();\n"+
			"\n"+
			"		@Override\n"+
			"		boolean equals(Object obj);\n"+
			"\n"+
			"		@Override\n"+
			"		int hashCode();\n"+
			"\n"+
			"		@Override\n"+
			"		String toString();\n"+
			"	}\n"+
			"\n"+
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439594  [1.8][compiler] nested lambda type incorrectly inferred vs javac
public void test439594() {
	this.runNegativeTest(
		new String[] {
			"X.java", 
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.function.Predicate;\n" +
			"import java.util.stream.Collectors;\n" +
			"import java.util.stream.Stream;\n" +
			"public class X {\n" +
			"	protected static interface IListEntry {\n" +
			"		public <T> T visitRecordsWithResult(Function<Stream<Record>,T> func);		\n" +
			"	}\n" +
			"	protected static final class ImmutableRecord {\n" +
			"		public ImmutableRecord(Record r) { }\n" +
			"	}\n" +
			"	protected static final class Record {}\n" +
			"	public List<ImmutableRecord> compilesWithEclipseAndJavac() \n" +
			"	{\n" +
			"		return visitEntriesWithResult( stream -> {\n" +
			"			return stream.map( entry -> {\n" +
			"				final List<ImmutableRecord> result1 = entry.visitRecordsWithResult( stream2 -> stream2\n" +
			"						.filter( somePredicate() )\n" +
			"						.map( ImmutableRecord::new )\n" +
			"						.collect( Collectors.toList() )\n" +
			"					);	\n" +
			"				return result1;\n" +
			"			}).flatMap( List::stream ).collect( Collectors.toCollection( ArrayList::new ) );\n" +
			"		});		\n" +
			"	}		\n" +
			"	public List<ImmutableRecord> compilesWithJavacButNotEclipse1() \n" +
			"	{\n" +
			"		return visitEntriesWithResult( stream -> {\n" +
			"			return stream.map( entry -> {\n" +
			"				return entry.visitRecordsWithResult( stream2 -> stream2\n" +
			"						.filter( somePredicate() )\n" +
			"						.map( ImmutableRecord::new )\n" +
			"						.collect( Collectors.toList() )\n" +
			"					);	\n" +
			"			}).flatMap( List::stream ).collect( Collectors.toCollection( ArrayList::new ) );\n" +
			"		});		\n" +
			"	}		\n" +
			"	public List<ImmutableRecord> compilesWithJavacButNotEclipse2() \n" +
			"	{\n" +
			"		return visitEntriesWithResult( stream -> {\n" +
			"			return stream.map( entry -> entry.visitRecordsWithResult( stream2 -> stream2\n" +
			"						.filter( somePredicate() )\n" +
			"						.map( ImmutableRecord::new )\n" +
			"						.collect( Collectors.toList() ) )\n" +
			"			).flatMap( List::stream ).collect( Collectors.toCollection( ArrayList::new ) );\n" +
			"		});		\n" +
			"	}	\n" +
			"	public List<ImmutableRecord> compilesWithJavacButNotEclipse3() \n" +
			"	{\n" +
			"		return visitEntriesWithResult( stream -> stream.map( entry -> entry.visitRecordsWithResult( stream2 -> stream2\n" +
			"						.filter( somePredicate() )\n" +
			"						.map( ImmutableRecord::new )\n" +
			"						.collect( Collectors.toList() ) )\n" +
			"			)\n" +
			"			.flatMap( List::stream )\n" +
			"			.collect( Collectors.toCollection( ArrayList::new ) )\n" +
			"		);		\n" +
			"	}	\n" +
			"	private static Predicate<Record> somePredicate() {\n" +
			"		return record -> true;\n" +
			"	}		\n" +
			"	private <T> T visitEntriesWithResult(Function<Stream<IListEntry>,T> func) {\n" +
			"		return func.apply( new ArrayList<IListEntry>().stream() );\n" +
			"	}\n" +
			"}\n"
	},
	"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=433852, [1.8][compiler] Javac rejects type inference results that ECJ accepts
public void test433852() {
	this.runNegativeTest(
		new String[] {
			"X.java", 
			"import java.util.Optional;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.stream.Stream;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(test(Stream.of(Stream.of(\"3\"))));\n" +
			"		System.out.println(test2(Stream.of(Stream.of(\"1\")).skip(1)));\n" +
			"		System.out.println(test31(Stream.of(Stream.of(\"2\")).skip(1)));\n" +
			"	}\n" +
			"	static Optional<Stream<Object>> test(Stream<Stream<String>> s31) {\n" +
			"		return s31.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).findAny();\n" +
			"	}\n" +
			"	static Object test2(Stream<Stream<String>> s3) {\n" +
			"		return s3.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).flatMap(Function.identity()).findAny().orElse(\n" +
			"		  X.class);\n" +
			"	}\n" +
			"	static Stream<Object> test31(Stream<Stream<String>> s3) {\n" +
			"		return s3.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).findAny().orElse(Stream.of(new Object()));\n" +
			"	}\n" +
			"}\n"
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 11)\n" + 
	"	return s31.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).findAny();\n" + 
	"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
	"Type mismatch: cannot convert from Optional<Stream<Integer>> to Optional<Stream<Object>>\n" + 
	"----------\n" + 
	"2. ERROR in X.java (at line 14)\n" + 
	"	return s3.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).flatMap(Function.identity()).findAny().orElse(\n" + 
	"	                                                                                               ^^^^^^\n" + 
	"The method orElse(Integer) in the type Optional<Integer> is not applicable for the arguments (Class<X>)\n" + 
	"----------\n" + 
	"3. ERROR in X.java (at line 18)\n" + 
	"	return s3.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).findAny().orElse(Stream.of(new Object()));\n" + 
	"	                                                                  ^^^^^^\n" + 
	"The method orElse(Stream<Integer>) in the type Optional<Stream<Integer>> is not applicable for the arguments (Stream<Object>)\n" + 
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442916,  [1.8][inference] Type Inference is broken for CompletableFuture then-methods  
public void test442916() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import static java.util.concurrent.CompletableFuture.completedFuture;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.concurrent.CompletableFuture;\n" +
			"public class X {\n" +
			"    public static CompletableFuture<Integer> cf(int value) {\n" +
			"		return completedFuture(value);\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"		cf(1).thenCompose((xInt) -> cf(2).thenApply((zInt) -> Arrays.asList(xInt, zInt)))\n" +
			"		.thenAccept((ints) -> {\n" +
			"			/* !!!! ints is incorrectly inferred to be an Object, but it is List<Integer> */\n" +
			"			System.out.println(ints.get(0) + ints.get(1)); // should print 3;\n" +
			"		});\n" +
			"	}\n" +
			"}\n"
		},
		"3");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442769, [1.8][compiler] Invalid type inference using Stream  
public void test442769() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.nio.file.Path;\n" +
			"import java.nio.file.Paths;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.HashMap;\n" +
			"import java.util.List;\n" +
			"import java.util.Map;\n" +
			"import java.util.Map.Entry;\n" +
			"import java.util.stream.Collector;\n" +
			"import java.util.stream.Collectors;\n" +
			"import java.util.stream.Stream;\n" +
			"import java.io.Serializable;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Map<Object, Integer> allocated = new HashMap<>();\n" +
			"		   Arrays.asList(\"a\", \"b\", \"c\", \"d\", \"e\") // List<String>\n" +
			"	          .stream() // Stream<String>\n" +
			"	          .map(Paths::get) // Stream<Path>\n" +
			"	          .flatMap(path -> allocated.keySet() // Set<Object>\n" +
			"	                                    .stream() // Stream<Object>\n" +
			"	                                    .map(group -> Pair.of(group, path) /*Pair<Object,Path>*/) // Stream<Pair<Object, Path>>\n" +
			"	          ) // Stream<Object> [FAIL]\n" +
			"	          .collect(Collectors.toList()) // List<Object>\n" +
			"	          .forEach(item -> System.out.println(item.getKey() + \": \" + item.getValue())); // Consumer<? super Object>\n" +
			"	    // with explicit type\n" +
			"	    Arrays.asList(\"a\", \"b\", \"c\", \"d\", \"e\") // List<String>\n" +
			"	          .stream() // Stream<String>\n" +
			"	          .map(Paths::get) // Stream<Path>\n" +
			"	          .flatMap(path -> allocated.keySet() // Set<Object>\n" +
			"	                                    .stream() // Stream<Object>\n" +
			"	                                    .<Pair<Object,Path>>map(group -> Pair.of(group, path) /*Pair<Object,Path>*/) // Stream<Pair<Object, Path>>\n" +
			"	          ) // Stream<Pair<Object, Path>>\n" +
			"	          .collect(Collectors.toList()) // List<Pair<Object, Path>>\n" +
			"	          .forEach(item -> System.out.println(item.getKey() + \": \" + item.getValue())); // Consumer<? super Pair<Object, Path>>\n" +
			"	}\n" +
			"}\n" +
			"abstract class Pair<L, R> implements Map.Entry<L, R>, Comparable<Pair<L, R>>, Serializable {\n" +
			"    public static <L, R> Pair<L, R> of(final L left, final R right) {\n" +
			"        return null;\n" +
			"    }\n" +
			"    public final L getKey() {\n" +
			"        return null;\n" +
			"    }\n" +
			"    public R getValue() {\n" +
		"        return null;\n" +
			"    }\n" +
			"}\n"
		},
		"");
}
// Test allocation expression boxing compatibility
public void testAllocationBoxingCompatibility() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X  {\n" +
			"    static <T> int m(T o1, byte o2) {return 1;}       \n" +
			"    static boolean call() {\n" +
			"        return m(new Long(12l), new Byte((byte)1)) == 1;\n" +
			"    }\n" +
			"\n" +
			"    public static void main(String argv[]) {\n" +
			"       System.out.println(call());\n" +
			"    }\n" +
			"}\n",
		}, "true");
}
// NPE while building JRE8: https://bugs.eclipse.org/bugs/show_bug.cgi?id=437444#c113
public void test437444_c113() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"public class X {\n" +
			"	final List<String>[] ls = Util.cast(new List<>[] { null });\n" +
			"	\n" +
			"}\n" +
			"class Util {\n" +
			"	@SuppressWarnings(\"unchecked\")\n" +
			"	public static <T> T cast(Object x) {\n" +
			"		return (T) x;\n" +
			"	}\n" +
			"}\n",
		}, 
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	final List<String>[] ls = Util.cast(new List<>[] { null });\n" + 
		"	                                        ^^^^\n" + 
		"Incorrect number of arguments for type List<E>; it cannot be parameterized with arguments <>\n" + 
		"----------\n");
}
// Error while building JRE8: https://bugs.eclipse.org/bugs/show_bug.cgi?id=437444#c113
public void test437444_c113a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"public class X {\n" +
			"	final List<String>[] ls = Util.cast(new List<?>[] { null });\n" +
			"	\n" +
			"}\n" +
			"class Util {\n" +
			"	@SuppressWarnings(\"unchecked\")\n" +
			"	public static <T> T cast(Object x) {\n" +
			"		return (T) x;\n" +
			"	}\n" +
			"}\n",
		}, 
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434394, [1.8] inference fails in some cases when conditional expression is involved
public void test434394() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Collections;\n" +
			"import java.util.Comparator;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"  public void bla() {\n" +
			"    boolean b = Boolean.TRUE.booleanValue();\n" +
			"    List<String> c1 = new ArrayList<>();\n" +
			"    Collections.sort(c1, new Foo(new State<>((b ? new Val<>(\"AAAA\") : new Val<>(\"BBBB\"))))); // Cannot infer type arguments for State\n" +
			"    Collections.sort(c1,new Foo(b ? new State<>(new Val<>(\"AAAA\")) : new State<>(new Val<>(\"BBBB\")))); // this is fine\n" +
			"  }\n" +
			"  static class Foo implements Comparator<String>{\n" +
			"	  public Foo(State<String> st) {\n" +
			"		  //\n" +
			"	  }\n" +
			"	@Override\n" +
			"	public int compare(String o1, String o2) {\n" +
			"		// TODO Auto-generated method stub\n" +
			"		return 0;\n" +
			"	}\n" +
			"  }\n" +
			"	static class State<R> {\n" +
			"		State(Val<?> o) {\n" +
			"		}\n" +
			"	}\n" +
			"	static class Val<T> {\n" +
			"		Val(T t) {}\n" +
			"	}\n" +
			"}\n",
		}, 
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=445725,  [1.8][inference] Type inference not occurring with lambda expression and constructor reference
public void test445725() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.Collection;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.stream.Collectors;\n" +
			"import java.util.stream.Stream;\n" +
			"public class X {\n" +
			"/**\n" +
			"   * Takes a collection, applies a mapper to it, and then passes the result into the finishing\n" +
			"   * function\n" +
			"   */\n" +
			"  public static <FROM, TO, RESULT> RESULT mapped(Collection<FROM> collection,\n" +
			"                                                 Function<? super FROM, ? extends TO> mapper,\n" +
			"                                                 Function<? super Collection<TO>, RESULT> finisher)\n" +
			"  {\n" +
			"    return mapped(collection.stream(), mapper, finisher);\n" +
			"  }\n" +
			"  /**\n" +
			"   * Takes a stream, applies a mapper to it, and then passes the result into the finishing function\n" +
			"   */\n" +
			"  public static <FROM, TO, RESULT> RESULT mapped(Stream<FROM> stream,\n" +
			"                                                 Function<? super FROM, ? extends TO> mapper,\n" +
			"                                                 Function<? super Collection<TO>, RESULT> finisher)\n" +
			"  {\n" +
			"    return finisher.apply(stream.map(mapper).collect(Collectors.toList()));\n" +
			"  }\n" +
			"  public static void example()\n" +
			"  {\n" +
			"    mapped(Stream.of(\"1, 2, 3\"), Integer::parseInt, ArrayList<Integer>::new);\n" +
			"    mapped(Arrays.asList(\"1, 2, 3\"), Integer::parseInt, ArrayList<Integer>::new);\n" +
			"\n" +
			"    mapped(Stream.of(\"1, 2, 3\"), Integer::parseInt, IntCollection::new);\n" +
			"    mapped(Arrays.asList(\"1, 2, 3\"), Integer::parseInt, IntCollection::new);\n" +
			"  }\n" +
			"  public static class IntCollection extends ArrayList<Integer>\n" +
			"  {\n" +
			"    public IntCollection(Collection<Integer> numbers)\n" +
			"    {\n" +
			"      super(numbers);\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
		}, 
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	void bar(String t);\n" +
			"}\n" +
			"public class X<T> {\n" +
			"	X(String x) {}\n" +
			"	X(T x) { \n" +
			"		System.out.println(\"Here\");\n" +
			"	}\n" +
			"	X(T x, String ...strings) {}\n" +
			"	public void one(X<I> c){}\n" +
			"	public void two() {\n" +
			"		one(new X<>((String s) -> { }));\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X(\"\").two();\n" +
			"	}\n" +
			"}\n",
		}, 
		"Here");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633c() {
	runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	 default <T> void foo (T... p) {}\n" +
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
		"----------\n" + 
		"1. WARNING in X.java (at line 2)\n" + 
		"	default <T> void foo (T... p) {}\n" + 
		"	                           ^\n" + 
		"Type safety: Potential heap pollution via varargs parameter p\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633d() {
	runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	 default <T> void foo (T... p) {}\n" +
			"}\n" +
			"abstract class A  {\n" +
			"	public void foo(Object [] p) {}\n" +
			"}\n" +
			"abstract class B extends A implements I {\n" +
			"}\n" +
			"public abstract class X extends B implements I {\n" +
			"	public static void main(B b) {\n" +
			"		b.foo(\"hello\", \"world\");\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 2)\n" + 
		"	default <T> void foo (T... p) {}\n" + 
		"	                           ^\n" + 
		"Type safety: Potential heap pollution via varargs parameter p\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 7)\n" + 
		"	abstract class B extends A implements I {\n" + 
		"	               ^\n" + 
		"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 9)\n" + 
		"	public abstract class X extends B implements I {\n" + 
		"	                      ^\n" + 
		"Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 11)\n" + 
		"	b.foo(\"hello\", \"world\");\n" + 
		"	  ^^^\n" + 
		"The method foo(T...) of type I cannot be invoked as it is overridden by an inapplicable method\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633e() {
	runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	 default <T> void foo (T... p) {}\n" +
			"}\n" +
			"abstract class A  {\n" +
			"	public void foo(String [] p) {}\n" +
			"}\n" +
			"abstract class B extends A implements I {\n" +
			"}\n" +
			"public abstract class X extends B implements I {\n" +
			"	public static void main(B b) {\n" +
			"		b.foo(\"hello\", \"world\");\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 2)\n" + 
		"	default <T> void foo (T... p) {}\n" + 
		"	                           ^\n" + 
		"Type safety: Potential heap pollution via varargs parameter p\n" + 
		"----------\n");
}
// original:
public void testBug452788a() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.function.Function;\n" + 
			"\n" + 
			"interface Test<A> {\n" + 
			"\n" + 
			"	<B3> Test<B3> create(B3 b);\n" + 
			"\n" + 
			"	<B2> Test<B2> transform(Function<? extends A, Test<B2>> f);\n" + 
			"\n" + 
			"	default <B1> Test<B1> wrap(Function<? super A, ? extends B1> f) {\n" + 
			"		return transform(a -> create(f.apply(a)));\n" + 
			"	}\n" + 
			"}\n"
		});
}
// variants:
public void testBug452788b() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.function.Function;\n" + 
			"\n" + 
			"interface Test<A> {\n" + 
			"\n" + 
			"	<B3> Test<B3> create(B3 b);\n" + 
			"\n" + 
			"	<B2> Test<B2> transform(Function<? extends A, Test<B2>> f);\n" + 
			"\n" + 
			"	default <B1> Test<B1> wrap(Function<? super A, ? extends B1> f) {\n" + 
			"		return transform((A a) -> create(f.apply(a)));\n" + // explicitly typed lambda
			"	}\n" +
			"	default <B> Function<? extends A, Test<B>> test1(Function<? super A, ? extends B> f) {\n" + 
			"		return a -> create(f.apply(a));\n" + // remove outer invocation
			"	}\n" + 
			"	default <B> Function<? extends A, Function<? extends A, Test<B>>> test2(Function<? super A, ? extends B> f) {\n" + 
			"		return a1 -> a2 -> create(f.apply(a2));\n" + // outer lambda instead of outer invocation
			"	}\n" + 
			"}\n"
		});
}
// diamond allocation instead of method (was OK before the patch).
public void testBug452788c() {
	runConformTest(
		new String[] {
			"Test2.java",
			"import java.util.function.Function;\n" + 
			"\n" + 
			"\n" + 
			"public interface Test2<A> {\n" + 
			"	<B2> Test2<B2> transform(Function<? extends A, Test2<B2>> f);\n" + 
			"\n" + 
			"	default <B1> Test2<B1> wrap(Function<? super A, ? extends B1> f) {\n" + 
			"		return transform(a -> new TestImpl<>(f.apply(a)));\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"class TestImpl<A> implements Test2<A> {\n" + 
			"\n" + 
			"	public TestImpl(A a) { }\n" + 
			"\n" + 
			"	@Override\n" + 
			"	public <B2> Test2<B2> transform(Function<? extends A, Test2<B2>> f) {\n" + 
			"		return null;\n" + 
			"	}	\n" + 
			"}\n"
		});
}
public void testBug457079() {
	runConformTest(
		new String[] {
			"Foo.java",
			"import java.util.Collections;\n" + 
			"import java.util.Map;\n" + 
			"import java.util.Set;\n" + 
			"import java.util.function.Function;\n" + 
			"\n" + 
			"class Foo {\n" + 
			"    static <K, V> Map<K, V> foo(K value, Function<? super K, V> function) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"\n" + 
			"    static void bar(Set<String> set) {\n" + 
			"        Map<String, Set<String>> map = foo(\"\", e -> Collections.emptySet());\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug458396() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"import java.util.List;\n" + 
			"\n" + 
			"interface MyTickContext { }\n" + 
			"abstract class MyEntity {\n" + 
			"	abstract void tick(MyTickContext ctx);\n" + 
			"}\n" + 
			"\n" + 
			"public class Main {\n" + 
			"\n" + 
			"	protected static final MyTickContext tickContext = new MyTickContext() {\n" + 
			"		public void method1(MyEntity e) {\n" + 
			"			removeEntity( e );\n" + 
			"		}\n" + 
			"	};\n" + 
			"\n" + 
			"	public static final class Game  {\n" + 
			"		public void method2(List<MyEntity> ents) {\n" + 
			"			ents.forEach( e -> e.tick(tickContext) );\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Main.java (at line 12)\n" + 
		"	removeEntity( e );\n" + 
		"	^^^^^^^^^^^^\n" + 
		"The method removeEntity(MyEntity) is undefined for the type new MyTickContext(){}\n" + 
		"----------\n");
}
public void testBug455945() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.function.BiFunction;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.function.Predicate;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"\n" + 
			"    static <T> Tuple2<Seq<T>, Seq<T>> splitAtDoesntCompile(Stream<T> stream, long position) {\n" + 
			"        return seq(stream)\n" + 
			"            .zipWithIndex()\n" + 
			"            .partition(t -> t.v2 < position)\n" + 
			"            .map((v1, v2) -> tuple(\n" + 
			"                v1.map(t -> t.v1),\n" + 
			"                v2.map(t -> t.v1)\n" + 
			"            ));\n" + 
			"    }\n" + 
			"\n" + 
			"    static <T> Tuple2<Seq<T>, Seq<T>> splitAtCompiles(Stream<T> stream, long position) {\n" + 
			"        return seq(stream)\n" + 
			"            .zipWithIndex()\n" + 
			"            .partition(t -> t.v2 < position)\n" + 
			"            .map((v1, v2) -> Test.<Seq<T>, Seq<T>>tuple(\n" + 
			"                v1.map(t -> t.v1),\n" + 
			"                v2.map(t -> t.v1)\n" + 
			"            ));\n" + 
			"    }\n" + 
			"\n" + 
			"    static <T> Seq<T> seq(Stream<T> stream) {\n" + 
			"    	return null;\n" + 
			"    }\n" + 
			"\n" + 
			"    static <T1, T2> Tuple2<T1, T2> tuple(T1 v1, T2 v2) {\n" + 
			"    	return null;\n" + 
			"    }\n" + 
			"}\n" + 
			"\n" + 
			"interface I<T> {\n" + 
			"	T get();\n" + 
			"	<U> I<U> map(Function<T, U> f);\n" + 
			"}\n" + 
			"\n" + 
			"interface Seq<T> {\n" + 
			"	Seq<Tuple2<T, Long>> zipWithIndex();\n" + 
			"	Tuple2<Seq<T>, Seq<T>> partition(Predicate<? super T> predicate);\n" + 
			"	<R> Seq<R> map(Function<? super T, ? extends R> mapper);\n" + 
			"}\n" + 
			"\n" + 
			"class Tuple2<T1, T2> {\n" + 
			"	T1 v1;\n" + 
			"	T2 v2;\n" + 
			"	\n" + 
			"	<R> R map(BiFunction<T1, T2, R> function) {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=445231, [compiler] IllegalAccessError running Eclipse-compiled class
// This is a bug in Oracle JREs. Workaround in ECJ: https://bugs.eclipse.org/bugs/show_bug.cgi?id=466675
public void testBug445231() {
	runConformTest(
		true,
		new String[] {
		"com/n/Bug.java",
		"package com.n;\n" +
		"public class Bug {\n" +
		"  public static void main(String[] args) {\n" +
		"    try {\n" +
		"      new Bug().go();\n" +
		"      System.err.println(\"Ok\");\n" +
		"    } catch (IllegalAccessError e) {\n" +
		"      System.err.println(\"Error\");\n" +
		"      e.printStackTrace();\n" +
		"    }\n" +
		"  }\n" +
		"  public void go() {\n" +
		"    Class<?> clazz = Buggered.Foo.class;\n" +
		"    System.err.println(\"Here we go\");\n" +
		"    if (clazz.isAnonymousClass()) {\n" +
		"      System.err.println(\"is anon\");\n" +
		"    } else {\n" +
		"      System.err.println(\"not anon\");\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		"com/g/Base.java",
		"package com.g;\n" +
		"class Base2{}\n" +
		"class Base {\n" +
		"	class A {}\n" +
		"	static class Builder<B extends Builder<B>> {\n" +
		"		public B setJobName() {\n" +
		"			return null;\n" +
		"		}\n" +
		"		public Base2 setJobName2(B b) {\n" +
		"			return null;\n" +
		"		}\n" +

		//  Wildcard
		"		public void foo(H<? super H<Base3.A>> h) {\n" +
		"			return;\n" +
		"  		}\n" +
		"		private class H<T> {}\n" +
		"	}\n" +
		"   static class Builder2 {\n" +
		"       public <B extends Builder<B>> B setJobName3() {\n" +
		"	        return null;\n" +
		"       }\n" +
		"   }\n" +
		"	static class R {}\n" +
		"	public static class Builder3<B extends R> {\n" +
		"		public B setJobName() {\n" +
		"			return null;\n" +
		"		}\n" +
		"	}\n" +
		"	public static class Builder4<B extends R> {\n" +
		"		public <Q extends R> Builder3<Q> setJobName() {\n" +
		"			return null;\n" +
		"		}\n" +
		"	}\n" +

		// Testing Parameters
		"	static class Builder5 {\n" +
		"		public <B extends Builder<B>> void  foo(B b) {}\n" +
		"	}\n" +

		"}\n" +

		"class Base3 {\n" +
		"	static class A{}\n" +
		"}\n"
		,

		"com/g/Child.java",
		"package com.g;\n" +
		"import com.g.Base.R;\n" +
		"public final class Child {\n" +
		"  public static class Builder<I> extends Base.Builder<Builder<I>> {\n" +
		"	  public void setDummyName(){}\n" +
		"  }\n" +
		"  public static class Builder2 extends Base.Builder2 {}\n" +
		"  public static class Builder3<I> extends  Base.Builder3<R> {}\n" +
		"  public static class Builder4<I> extends  Base.Builder4<R> {}\n" +

		"  public static class Builder5 extends Base.Builder5 {} \n" +
		"}\n",
		"com/n/Buggered.java",
		"package com.n;\n" +
		"import com.g.Child;\n" +
		"class Z{}\n" +
		"public final class Buggered {\n" +
		"  public static final class Foo {}\n" +
		"  void unused() {\n" +
		"    Child.Builder<Void> c = new Child.Builder<Void>();\n" +
		"    c.setJobName();\n" +
		"    c.setJobName2(new Child.Builder<Void>());\n" +
		"    Child.Builder<Z> cb = new Child.Builder<Z>();\n" +
		"    cb.setJobName();\n" +
		"    cb.setJobName2(new Child.Builder<Z>());\n" +
		"    Child.Builder2 cb2 = new Child.Builder2();\n" +
		"    cb2.setJobName3();\n" +
		"    Child.Builder3<Void> cb3 = new Child.Builder3<Void>();\n" +
		"    cb3.setJobName();\n" +
		"    Child.Builder4<Void> cb4 = new Child.Builder4<Void>();\n" +
		"    cb4.setJobName();\n" +

		"    Child.Builder5 cb5 = new Child.Builder5();\n" +
		"    cb5.foo(null);\n" +

		//   Wildcard
		"	c.foo(null);\n" +
		"  }\n" +
		"}\n"
	},
	null, null,
	"Here we go\n" +
	"not anon\n" +
	"Ok", null);
}
public void testBug463728() {
	runConformTest(
		new String[] {
			"Main.java",
			"import java.util.function.Function;\n" + 
			"\n" + 
			"\n" + 
			"class Color {\n" + 
			"	\n" + 
			"}\n" + 
			"\n" + 
			"class TypeMapper<R> {\n" + 
			"\n" + 
			"	public TypeMapper() {\n" + 
			"	}\n" + 
			"	public R orElse(R result) {\n" + 
			"		return result;\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"public class Main {\n" + 
			"	Color A;\n" + 
			"	Color B;\n" + 
			"\n" + 
			"	public static <T, R> TypeMapper<R> mapType(Function<T, R> mapper) {\n" + 
			"		return new TypeMapper<R>();\n" + 
			"	}\n" + 
			"\n" + 
			"	public Color getForeground(Object element) {\n" + 
			"		return mapType(library -> {\n" + 
			"				return (element != null ? A : B);\n" + 
			"			}).orElse(null);\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug470942() {
	runConformTest(
		new String[] {
			"EclipeMarsLamdaIssueWontBuild.java",
			"import java.util.function.Supplier;\n" + 
			"\n" + 
			"public class EclipeMarsLamdaIssueWontBuild {\n" + 
			"	class MyClass {\n" + 
			"		long getNumber() {\n" + 
			"			return 0;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface VoidSupplier {\n" + 
			"		void perform();\n" + 
			"	}\n" + 
			"\n" + 
			"	long processTxContent() {\n" + 
			"		return withLogging(() -> new MyClass().getNumber());\n" + 
			"	}\n" + 
			"\n" + 
			"	private static void withLogging(final VoidSupplier supplier) {\n" + 
			"		// Do some logging\n" + 
			"	}\n" + 
			"\n" + 
			"	private static <T> T withLogging(final Supplier<T> supplier) {\n" + 
			"		// Do some logging\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug470958() {
	runConformTest(
		new String[] {
			"Bug470958.java",
			"import java.time.*;\n" + 
			"import java.util.*;\n" + 
			"import java.util.concurrent.*;\n" + 
			"import static java.util.concurrent.CompletableFuture.*;\n" + 
			"import static java.util.stream.Collectors.*;\n" + 
			"\n" + 
			"class Hotel {}\n" + 
			"\n" + 
			"class Bug470958 {\n" + 
			"  public Map<String, CompletableFuture<List<Hotel>>> asyncLoadMany(List<String> codes, LocalDate begin, LocalDate end) {\n" + 
			"    return loadMany(codes, begin, end)\n" + 
			"    .entrySet()\n" + 
			"    .stream()\n" + 
			"    .collect(toMap(Map.Entry::getKey, entry -> completedFuture(entry.getValue())));\n" + 
			"  }\n" + 
			"\n" + 
			"  public Map<String, List<Hotel>> loadMany(List<String> codes, LocalDate begin, LocalDate end) {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"}\n"
		});
}
public void testBug469753() {
	runConformTest(
		new String[] {
			"LambdaBug.java",
			"import java.util.AbstractMap;\n" + 
			"import java.util.Iterator;\n" + 
			"import java.util.Map.Entry;\n" + 
			"import java.util.function.Function;\n" + 
			"\n" + 
			"public class LambdaBug {\n" + 
			"\n" + 
			"    class Item {\n" + 
			"        String foo;\n" + 
			"    }\n" + 
			"\n" + 
			"    public void bug(String catalogKey, Iterator<Item> items) {\n" + 
			"        go(transform(items, i -> pair(i.foo, i)));\n" + 
			"    }\n" + 
			"\n" + 
			"    public static <K, V> Entry<K, V> pair(K key, V value) {\n" + 
			"        return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);\n" + 
			"    }\n" + 
			"\n" + 
			"    void go(Iterator<Entry<String, Item>> items) {\n" + 
			"    }\n" + 
			"\n" + 
			"    public static <F, T> Iterator<T> transform(Iterator<F> fromIterator, Function<? super F, ? extends T> function) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"\n" + 
			"}\n"
		});
}
public void testBug470826() {
	runConformTest(
		new String[] {
			"EcjVsCollect.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"public class EcjVsCollect {\n" + 
			"\n" + 
			"  public static void main(String[] args) {\n" + 
			"    try (final Stream<Record<String>> stream = getStream()) {\n" + 
			"      stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);\n" + 
			"//      ArrayList<Record<String>> foo = stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);\n" + 
			"    }\n" + 
			"  }\n" + 
			"\n" + 
			"  private static <K> Stream<Record<K>> getStream() {\n" + 
			"    return Stream.empty();\n" + 
			"  }\n" + 
			"\n" + 
			"  private interface Record<K> {\n" + 
			"    K getKey();\n" + 
			"  }\n" + 
			"}\n"
		});
}
public void testBug470542() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.function.Consumer;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	void test() {\n" + 
			"		process(missing::new);\n" + 
			"	}\n" + 
			"	\n" + 
			"	<T> void process(Consumer<T> c) { }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	process(missing::new);\n" + 
		"	        ^^^^^^^\n" + 
		"missing cannot be resolved\n" + 
		"----------\n");
}
public void testBug478848() {
	runConformTest(
		new String[] {
			"InferenceBug.java",
			"import java.util.*;\n" + 
			"public class InferenceBug {\n" + 
			"    \n" + 
			"    static class Wrapper<T> {\n" + 
			"        T value;\n" + 
			"        public T getValue() {\n" + 
			"            return null;\n" + 
			"        }\n" + 
			"    }\n" + 
			"    \n" + 
			"    static class C1 {\n" + 
			"        //an optional array of String wrappers\n" + 
			"        public Optional<? extends Wrapper<String>[]> optionalArrayOfStringWrappers() {\n" + 
			"            return Optional.empty();\n" + 
			"        }\n" + 
			"    }\n" + 
			"    \n" + 
			"    public static void main(String[] args) {\n" + 
			"        C1 c1 = new C1();\n" + 
			"        try {\n" + 
			"            for (Wrapper<String> attribute: c1.optionalArrayOfStringWrappers().get()) {\n" + 
			"                // error in previous line:\n" + 
			"                // Can only iterate over an array or an instance of java.lang.Iterable\n" +
			"            }\n" + 
			"        } catch (NoSuchElementException nsee) {\n" +
			"            System.out.print(\"No such element\");\n" +
			"        }\n" + 
			"    }\n" + 
			"}\n"
		},
		"No such element");
}
public void testBug479167() {
	runConformTest(
		new String[] {
			"ToArray.java",
			"import java.io.Serializable;\n" + 
			"interface ArrayFunction<E> {\n" + 
			"	<S extends E> E[] apply(@SuppressWarnings(\"unchecked\") S... es);\n" + 
			"}\n" + 
			"public class ToArray<E extends Cloneable & Serializable> implements ArrayFunction<E> {\n" + 
			"	public final @SafeVarargs @Override <S extends E> E[] apply(S... es) {\n" + 
			"		return es;\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		ArrayFunction<String[]> toArray = new ToArray<>();\n" + 
			"		String[][] array = toArray.apply(args);\n" + 
			"		System.out.print(array.getClass().getName());\n" + 
			"	}\n" + 
			"}\n"
		},
		"[[Ljava.lang.String;");
}
}
