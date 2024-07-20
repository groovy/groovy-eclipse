/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for Bug 343713 - [compiler] bogus line number in constructor of inner class in 1.5 compliance
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class InnerEmulationTest_1_5 extends AbstractRegressionTest {
static {
//		TESTS_NAMES = new String[] { "Bug58069" };
//		TESTS_NUMBERS = new int[] { 13 };
//		TESTS_RANGE = new int[] { 144, -1 };
}
public InnerEmulationTest_1_5(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test1() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"public class X {\n" +
		"	public void foo(Collection<? extends Map.Entry> args) { /* dummy */ }\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #25 java/util/Map$Entry, outer class info: #27 java/util/Map\n" +
		"     inner name: #29 Entry, accessflags: 1545 public abstract static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test2() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"public class X {\n" +
		"	public void foo(Map.Entry args) { /* dummy */ }\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map\n" +
		"     inner name: #25 Entry, accessflags: 1545 public abstract static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test3() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"import java.util.Map;\n" +
		"import java.util.List;\n" +
		"public class X {\n" +
		"	<U extends List<?>, T extends Map.Entry> X(List<U> lu, T t) {\n" +
		"	}\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #27 java/util/Map$Entry, outer class info: #29 java/util/Map\n" +
		"     inner name: #31 Entry, accessflags: 1545 public abstract static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test4() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"import java.util.Map;\n" +
		"import java.util.List;\n" +
		"public class X<T extends Object & Comparable<? super Map.Entry>> {}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map\n" +
		"     inner name: #25 Entry, accessflags: 1545 public abstract static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test5() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"public class X {\n" +
		"	public void foo(Map.Entry<String, String> args) { /* dummy */ }\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #25 java/util/Map$Entry, outer class info: #27 java/util/Map\n" +
		"     inner name: #29 Entry, accessflags: 1545 public abstract static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test6() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"public class X {\n" +
		"	Map.Entry<String, String> f;\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map\n" +
		"     inner name: #25 Entry, accessflags: 1545 public abstract static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test7() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"public class X<E extends Object & Map.Entry<String, E>> {\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map\n" +
		"     inner name: #25 Entry, accessflags: 1545 public abstract static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test8() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"class A {\n" +
		"	static class B{}\n" +
		"}\n" +
		"public class X<E extends Object & Map.Entry<E, A.B>> {\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map\n" +
		"     inner name: #25 Entry, accessflags: 1545 public abstract static],\n" +
		"    [inner class info: #26 p/A$B, outer class info: #28 p/A\n" +
		"     inner name: #30 B, accessflags: 8 static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test9() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"class A {\n" +
		"	static class B{}\n" +
		"}\n" +
		"public class X {\n" +
		"	<E extends Object & Map.Entry<E, A.B>> void foo(E e) {}\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #25 java/util/Map$Entry, outer class info: #27 java/util/Map\n" +
		"     inner name: #29 Entry, accessflags: 1545 public abstract static],\n" +
		"    [inner class info: #30 p/A$B, outer class info: #32 p/A\n" +
		"     inner name: #34 B, accessflags: 8 static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test10() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"class A {\n" +
		"	static interface B<U, T>{}\n" +
		"}\n" +
		"class C {\n" +
		"	static class D<U, T>{}\n" +
		"}\n" +
		"public class X {\n" +
		"	<E extends Object & A.B<E, Map.Entry<E, C.D>>> void foo(E e) {}\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #25 java/util/Map$Entry, outer class info: #27 java/util/Map\n" +
		"     inner name: #29 Entry, accessflags: 1545 public abstract static],\n" +
		"    [inner class info: #30 p/A$B, outer class info: #32 p/A\n" +
		"     inner name: #34 B, accessflags: 1544 abstract static],\n" +
		"    [inner class info: #35 p/C$D, outer class info: #37 p/C\n" +
		"     inner name: #39 D, accessflags: 8 static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test11() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"import java.util.*;\n" +
		"\n" +
		" public class X {\n" +
		" \n" +
		"	static abstract class SelfType<T extends SelfType<T>>{\n" +
		"	}\n" +
		" \n" +
		"	static class SuperType extends SelfType<SuperType>{\n" +
		"	}\n" +
		" \n" +
		"	static class SubType extends SuperType{}\n" +
		" \n" +
		"	static <T extends SelfType<T>> List<T> makeSingletonList(T t){\n" +
		"		return Collections.singletonList(t);\n" +
		"	}\n" +
		" \n" +
		"	static <T extends SelfType<T>,S extends T> List<T> makeSingletonList2(S s){\n" +
		"		return Collections.singletonList((T)s); // #0\n" +
		"	}\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #35 X$SelfType, outer class info: #1 X\n" +
		"     inner name: #37 SelfType, accessflags: 1032 abstract static],\n" +
		"    [inner class info: #38 X$SubType, outer class info: #1 X\n" +
		"     inner name: #40 SubType, accessflags: 8 static],\n" +
		"    [inner class info: #41 X$SuperType, outer class info: #1 X\n" +
		"     inner name: #43 SuperType, accessflags: 8 static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test12() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"public class X {\n" +
		"	Collection<Map.Entry> field;\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map\n" +
		"     inner name: #25 Entry, accessflags: 1545 public abstract static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test13() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.util.Collection;\n" +
		"import java.util.Map;\n" +
		"class A {\n" +
		"	static interface B<U, T>{}\n" +
		"}\n" +
		"class C {\n" +
		"	static class D<U, T>{}\n" +
		"}\n" +
		"public class X extends C.D implements A.B {\n" +
		"}"
	});
	String expectedOutput =
		"  Inner classes:\n" +
		"    [inner class info: #5 p/A$B, outer class info: #19 p/A\n" +
		"     inner name: #21 B, accessflags: 1544 abstract static],\n" +
		"    [inner class info: #3 p/C$D, outer class info: #22 p/C\n" +
		"     inner name: #24 D, accessflags: 8 static]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=343713
// [compiler] bogus line number in constructor of inner class in 1.5 compliance
public void test14() throws Exception {
	runConformTest(new String[] {
		"LineNumberBug.java",
		"public class LineNumberBug {\n" +
		"    class Inner {\n" +
		"		public Inner() {\n" +
		"			System.out.println(\"Inner()\");\n" +
		"		}\n" +
		"    }\n" +
		"	public static void main(String[] args) {\n" +
		"		new LineNumberBug().new Inner();\n" +
		"	}\n" +
		"}\n"
	});
	String expectedOutput =
		"  // Method descriptor #8 (LLineNumberBug;)V\n" +
		"  // Stack: 2, Locals: 2\n" +
		"  public LineNumberBug$Inner(LineNumberBug arg0);\n" +
		"     0  aload_0 [this]\n" +
		"     1  aload_1 [arg0]\n" +
		"     2  putfield LineNumberBug$Inner.this$0 : LineNumberBug [10]\n" +
		"     5  aload_0 [this]\n" +
		"     6  invokespecial java.lang.Object() [12]\n" +
		"     9  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
		"    12  ldc <String \"Inner()\"> [21]\n" +
		"    14  invokevirtual java.io.PrintStream.println(java.lang.String) : void [23]\n" +
		"    17  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 9, line: 4]\n" +
		"        [pc: 17, line: 5]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "LineNumberBug$Inner.class", "LineNumberBug$Inner", expectedOutput);
}
public void testBug546362() throws Exception {
	runConformTest(new String[] {
		"Schema.java",
		"import java.util.HashMap;\n" +
		"\n" +
		"public class Schema {\n" +
		"    public Integer[] getElements(HashMap<String, Integer> map) {\n" +
		"        return map.entrySet().toArray(new Integer[0]);\n" +
		"    }\n" +
		"}\n" +
		""
	});
	String expectedOutput =
			"  Inner classes:\n" +
			"    [inner class info: #41 java/util/Map$Entry, outer class info: #43 java/util/Map\n" +
			"     inner name: #45 Entry, accessflags: 1545 public abstract static]\n" +
			"";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "Schema.class", "X", expectedOutput);
}
public static Class testClass() {
	return InnerEmulationTest_1_5.class;
}
}
