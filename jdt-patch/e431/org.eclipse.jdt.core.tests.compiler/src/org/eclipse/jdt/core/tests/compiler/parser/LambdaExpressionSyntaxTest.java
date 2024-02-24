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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;

@SuppressWarnings({ "rawtypes" })
public class LambdaExpressionSyntaxTest extends AbstractSyntaxTreeTest {

	private static String  jsr335TestScratchArea = "c:\\Jsr335TestScratchArea";
	private static String referenceCompiler = "C:\\jdk-7-ea-bin-b75-windows-i586-30_oct_2009\\jdk7\\bin\\javac.exe"; // TODO: Patch when RI becomes available.

	public static Class testClass() {
		return LambdaExpressionSyntaxTest.class;
	}
	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public LambdaExpressionSyntaxTest(String testName){
		super(testName, referenceCompiler, jsr335TestScratchArea);
		if (referenceCompiler != null) {
			File f = new File(jsr335TestScratchArea);
			if (!f.exists()) {
				f.mkdir();
			}
			CHECK_ALL |= CHECK_JAVAC_PARSER;
		}
	}

	static {
		//		TESTS_NAMES = new String[] { "test0012" };
		//		TESTS_NUMBERS = new int[] { 133, 134, 135 };
		if (!(new File(referenceCompiler).exists())) {
			referenceCompiler = null;
			jsr335TestScratchArea = null;
		}
	}
	// type elided, unparenthesized parameter + expression body lambda in casting context.
	public void test0001() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        System.out.println(((I) x -> x * x).square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.println(((I) (<no type> x) -> (x * x)).square(10));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0001", expectedUnitToString);
	}
	// type elided, unparenthesized parameter + expression body lambda as initializer.
	public void test0002() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i =  x -> x * x;\n" +
				"        System.out.println(i.square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = (<no type> x) -> (x * x);\n" +
				"    System.out.println(i.square(10));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0002", expectedUnitToString);
	}
	// type elided, unparenthesized parameter + expression body lambda as initializer, full lambda is parenthesized.
	public void test0003() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i =  ((((x -> x * x))));\n" +
				"        System.out.println(i.square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = (((((<no type> x) -> (x * x)))));\n" +
				"    System.out.println(i.square(10));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0003", expectedUnitToString);
	}
	// type elided, unparenthesized parameter + expression body lambda as RHS of assignment, full lambda is parenthesized.
	public void test0004() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i;\n" +
				"        i =  (x -> x * x);\n" +
				"        System.out.println(i.square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i;\n" +
				"    i = ((<no type> x) -> (x * x));\n" +
				"    System.out.println(i.square(10));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0004", expectedUnitToString);
	}
	// type elided, unparenthesized parameter + expression body lambda in return statement, full lambda is parenthesized.
	public void test0005() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    static I getI() {\n" +
				"        return (x -> x * x);\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = getI();\n" +
				"        System.out.println(i.square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  static I getI() {\n" +
				"    return ((<no type> x) -> (x * x));\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = getI();\n" +
				"    System.out.println(i.square(10));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0005", expectedUnitToString);
	}
	// type elided, unparenthesized parameter + expression body lambda in conditional expression.
	public void test0006() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = args == null ? x -> x * x : x -> x * x * x;\n" +
				"        System.out.println(i.square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = ((args == null) ? (<no type> x) -> (x * x) : (<no type> x) -> ((x * x) * x));\n" +
				"    System.out.println(i.square(10));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0006", expectedUnitToString);
	}
	// type elided, unparenthesized parameter + expression body lambda in message send.
	public void test0007() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    static void foo(I i1, I i2) {\n" +
				"        System.out.println(i1.square(10));\n" +
				"        System.out.println(i2.square(10));\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        foo(x -> x * x, x -> x * x * x);\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  static void foo(I i1, I i2) {\n" +
				"    System.out.println(i1.square(10));\n" +
				"    System.out.println(i2.square(10));\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    foo((<no type> x) -> (x * x), (<no type> x) -> ((x * x) * x));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0007", expectedUnitToString);
	}
	// type elided, unparenthesized parameter + expression body lambda in constructor call.
	public void test0008() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    X (I i1, I i2) {\n" +
				"        System.out.println(i1.square(10));\n" +
				"        System.out.println(i2.square(10));\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        new X(x -> x * x, x -> x * x * x);\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  X(I i1, I i2) {\n" +
				"    super();\n" +
				"    System.out.println(i1.square(10));\n" +
				"    System.out.println(i2.square(10));\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    new X((<no type> x) -> (x * x), (<no type> x) -> ((x * x) * x));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0008", expectedUnitToString);
	}
	// type elided, unparenthesized parameter + expression body lambda in lambda.
	public void test0009() throws IOException {
		String source =
				"interface I {\n" +
				"    I square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"      System.out.println (((I) a->b->c->d->e->f->g-> null).square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  I square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.println(((I) (<no type> a) -> (<no type> b) -> (<no type> c) -> (<no type> d) -> (<no type> e) -> (<no type> f) -> (<no type> g) -> null).square(10));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0009", expectedUnitToString);
	}
	// type elided, unparenthesized parameter + expression body lambda in an initializer block
	public void test00010() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    static I i = x -> x * x;\n" +
				"    {\n" +
				"        i = x -> x * x * x;\n" +
				"    }\n" +
				"    static {\n" +
				"        i = x -> x * x * x;\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  static I i = (<no type> x) -> (x * x);\n" +
				"  {\n" +
				"    i = (<no type> x) -> ((x * x) * x);\n" +
				"  }\n" +
				"  static {\n" +
				"    i = (<no type> x) -> ((x * x) * x);\n" +
				"  }\n" +
				"  <clinit>() {\n" +
				"  }\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test00010", expectedUnitToString);
	}
	// type elided, parenthesized parameter + expression body lambda in casting context.
	public void test0011() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        System.out.println(((I) (x) -> x * x).square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.println(((I) (<no type> x) -> (x * x)).square(10));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0011", expectedUnitToString);
	}
	// Normal & minimal parameter list + expression body lambda in assignment context.
	public void test0012() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = (int x) -> x * x;\n" +
				"        System.out.println(i.square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = (int x) -> (x * x);\n" +
				"    System.out.println(i.square(10));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0012", expectedUnitToString);
	}
	// Normal parameter list, with modifiers & annotations  + expression body lambda in invocation context.
	public void test0013() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    @interface Positive {}\n" +
				"    static void foo(I i1, I i2) {\n" +
				"        System.out.println(i1.square(10));\n" +
				"        System.out.println(i2.square(10));\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        foo((final int x) -> x * x, (final @Positive int x) -> x * x * x);\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  @interface Positive {\n" +
				"  }\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  static void foo(I i1, I i2) {\n" +
				"    System.out.println(i1.square(10));\n" +
				"    System.out.println(i2.square(10));\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    foo((final int x) -> (x * x), (final @Positive int x) -> ((x * x) * x));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0013", expectedUnitToString);
	}
	// Vararg parameter list, with modifiers & annotations + expression body lambda in message send context.
	public void test0014() throws IOException {
		String source =
				"interface I {\n" +
				"    int square(int ... x);\n" +
				"}\n" +
				"public class X {\n" +
				"    @interface Positive {}\n" +
				"    static void foo(I i1, I i2) {\n" +
				"        System.out.println(i1.square(10));\n" +
				"        System.out.println(i2.square(10));\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        foo((final int ... x) -> 10, (final @Positive int [] x) -> 20);\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int square(int... x);\n" +
				"}\n" +
				"public class X {\n" +
				"  @interface Positive {\n" +
				"  }\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  static void foo(I i1, I i2) {\n" +
				"    System.out.println(i1.square(10));\n" +
				"    System.out.println(i2.square(10));\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    foo((final int... x) -> 10, (final @Positive int[] x) -> 20);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0014", expectedUnitToString);
	}
	// multi parameter type elided list + expression body lambda in return statement.
	public void test0015() throws IOException {
		String source =
				"interface I {\n" +
				"    int product(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"    static I getI() {\n" +
				"        return ((x, y) -> x * y);\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = getI();\n" +
				"        System.out.println(i.product(5, 6));\n" +
				"    }\n" +
				"};\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int product(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  static I getI() {\n" +
				"    return ((<no type> x, <no type> y) -> (x * y));\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = getI();\n" +
				"    System.out.println(i.product(5, 6));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0015", expectedUnitToString);
	}
	// multi parameter type specified list + block body lambda in return statement.
	public void test0016() throws IOException {
		String source =
				"interface I {\n" +
				"    int product(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"    static I getI() {\n" +
				"        return (int x, int y) -> { return x * y; };\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = getI();\n" +
				"        System.out.println(i.product(5, 6));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  int product(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  static I getI() {\n" +
				"    return (int x, int y) -> {\n" +
				"  return (x * y);\n" +
				"};\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = getI();\n" +
				"    System.out.println(i.product(5, 6));\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0016", expectedUnitToString);
	}
	// noarg + block body lambda
	public void test0017() throws IOException {
		String source =
				"interface I {\n" +
				"    String noarg();\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        System.out.println( ((I) () -> { return \"noarg\"; }).noarg());\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  String noarg();\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.println(((I) () -> {\n" +
				"  return \"noarg\";\n" +
				"}).noarg());\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0017", expectedUnitToString);
	}
	// Assorted tests.
	public void test0018() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo();\n" +
				"}\n" +
				"\n" +
				"interface J {\n" +
				"    int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"  I i1 = ()->{}; \n" +
				"  J j1 = ()->0;\n" +
				"  J j2 = ()->{ return 0; };\n" +
				"  I i2 = ()->{ System.gc(); };\n" +
				"  J j3 = ()->{\n" +
				"    if (true) return 0;\n" +
				"    else {\n" +
				"      int r = 12;\n" +
				"      for (int i = 1; i < 8; i++)\n" +
				"        r += i;\n" +
				"      return r;\n" +
				"    }\n" +
				"  };\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"  int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"  I i1 = () ->   {\n" +
				"  };\n" +
				"  J j1 = () -> 0;\n" +
				"  J j2 = () ->   {\n" +
				"    return 0;\n" +
				"  };\n" +
				"  I i2 = () ->   {\n" +
				"    System.gc();\n" +
				"  };\n" +
				"  J j3 = () ->   {\n" +
				"    if (true)\n" +
				"        return 0;\n" +
				"    else\n" +
				"        {\n" +
				"          int r = 12;\n" +
				"          for (int i = 1;; (i < 8); i ++) \n" +
				"            r += i;\n" +
				"          return r;\n" +
				"        }\n" +
				"  };\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0018", expectedUnitToString);
	}

	// like test0001() but body expression is an assignment
	public void test0019() throws IOException {
		String source =
				"interface I {\n" +
						"    int square(int x);\n" +
						"}\n" +
						"public class X {\n" +
						"    int y;\n" +
						"    public static void main(String [] args) {\n" +
						"        System.out.println(((I) x -> y = x * x ).square(10));\n" +
						"    }\n" +
						"}\n";

		String expectedUnitToString =
				"interface I {\n" +
						"  int square(int x);\n" +
						"}\n" +
						"public class X {\n" +
						"  int y;\n" +
						"  public X() {\n" +
						"    super();\n" +
						"  }\n" +
						"  public static void main(String[] args) {\n" +
						"    System.out.println(((I) (<no type> x) -> y = (x * x)).square(10));\n" +
						"  }\n" +
						"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0019", expectedUnitToString);
	}

	// Coverage:  exercise this condition in Parser.consumeExpression():
	//   if (this.valueLambdaNestDepth >= 0 && this.stateStackLengthStack[this.valueLambdaNestDepth] == this.stateStackTop - 1)
	// make sure we see a (true && false) combination
	public void testNestedLambda01() throws IOException {
		String source =
				"public class C {\n" +
				"	I foo() {\n" +
				"		return (i1, i2) -> 	(String x1, String x2) -> { \n" +
				"								return x1+x2; \n" + // here end-of-expression does not finish the type-eliding lambda (i1,i2)->...
				"							};\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"interface I {\n" +
				"	String doit(String s1, String s2);\n" +
				"}\n";
		String expectedUnitToString =
				"public class C {\n" +
				"  public C() {\n" +
				"    super();\n" +
				"  }\n" +
				"  I foo() {\n" +
				"    return (<no type> i1, <no type> i2) -> (String x1, String x2) -> {\n" +
				"  return (x1 + x2);\n" +
				"};\n" +
				"  }\n" +
				"}\n" +
				"interface I {\n" +
				"  String doit(String s1, String s2);\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "testNestedLambda01", expectedUnitToString);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385132
	public void test385132() throws IOException {
		String source = "->";
		String expectedErrorString =
				"----------\n" +
				"1. ERROR in test385132 (at line 1)\n" +
				"	->\n" +
				"	^^\n" +
				"Syntax error on token \"->\", delete this token\n" +
				"----------\n";

		checkParse(CHECK_PARSER , source.toCharArray(), expectedErrorString, "test385132", null);
	}
}
