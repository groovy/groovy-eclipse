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
public class ReferenceExpressionSyntaxTest extends AbstractSyntaxTreeTest {

	private static String  jsr335TestScratchArea = "c:\\Jsr335TestScratchArea";
	private static String referenceCompiler = "C:\\jdk-7-ea-bin-b75-windows-i586-30_oct_2009\\jdk7\\bin\\javac.exe"; // TODO: Patch when RI becomes available.

	public static Class testClass() {
		return ReferenceExpressionSyntaxTest.class;
	}
	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public ReferenceExpressionSyntaxTest(String testName){
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
	// Reference expression - super:: form, without type arguments.
	public void test0001() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(int x);\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"    public static void main(String [] args) {\n" +
				"	new X().doit();\n" +
				"    }\n" +
				"    void doit() {\n" +
				"        I i = super::foo;\n" +
				"        i.foo(10); \n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"    public void foo(int x) {\n" +
				"	System.out.println(x);\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(int x);\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doit();\n" +
				"  }\n" +
				"  void doit() {\n" +
				"    I i = super::foo;\n" +
				"    i.foo(10);\n" +
				"  }\n" +
				"}\n" +
				"class Y {\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public void foo(int x) {\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0001", expectedUnitToString);
	}
	// Reference expression - super:: form, with type arguments.
	public void test0002() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(int x);\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"    public static void main(String [] args) {\n" +
				"	new X().doit();\n" +
				"    }\n" +
				"    void doit() {\n" +
				"        I i = super::<String>foo;\n" +
				"        i.foo(10); \n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"    public void foo(int x) {\n" +
				"	System.out.println(x);\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(int x);\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doit();\n" +
				"  }\n" +
				"  void doit() {\n" +
				"    I i = super::<String>foo;\n" +
				"    i.foo(10);\n" +
				"  }\n" +
				"}\n" +
				"class Y {\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public void foo(int x) {\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0002", expectedUnitToString);
	}
	// Reference expression - SimpleName:: form, without type arguments.
	public void test0003() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y::foo;\n" +
				"        i.foo(10); \n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"    public static void foo(int x) {\n" +
				"	System.out.println(x);\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y::foo;\n" +
				"    i.foo(10);\n" +
				"  }\n" +
				"}\n" +
				"class Y {\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void foo(int x) {\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0003", expectedUnitToString);
	}
	// Reference expression - SimpleName:: form, with type arguments.
	public void test0004() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y::<String>foo;\n" +
				"        i.foo(10); \n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"    public static void foo(int x) {\n" +
				"	System.out.println(x);\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y::<String>foo;\n" +
				"    i.foo(10);\n" +
				"  }\n" +
				"}\n" +
				"class Y {\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void foo(int x) {\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0004", expectedUnitToString);
	}
	// Reference expression - QualifiedName:: form, without type arguments.
	public void test0005() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y.Z::foo;\n" +
				"        i.foo(10); \n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"    static class Z {\n" +
				"        public static void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y.Z::foo;\n" +
				"    i.foo(10);\n" +
				"  }\n" +
				"}\n" +
				"class Y {\n" +
				"  static class Z {\n" +
				"    Z() {\n" +
				"      super();\n" +
				"    }\n" +
				"    public static void foo(int x) {\n" +
				"      System.out.println(x);\n" +
				"    }\n" +
				"  }\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0005", expectedUnitToString);
	}
	// Reference expression - QualifiedName:: form, with type arguments.
	public void test0006() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y.Z::<String>foo;\n" +
				"        i.foo(10); \n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"    static class Z {\n" +
				"        public static void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y.Z::<String>foo;\n" +
				"    i.foo(10);\n" +
				"  }\n" +
				"}\n" +
				"class Y {\n" +
				"  static class Z {\n" +
				"    Z() {\n" +
				"      super();\n" +
				"    }\n" +
				"    public static void foo(int x) {\n" +
				"      System.out.println(x);\n" +
				"    }\n" +
				"  }\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0006", expectedUnitToString);
	}
	// Reference expression - Primary:: form, without type arguments.
	public void test0007() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = new Y()::foo;\n" +
				"        i.foo(10); \n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"        void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = new Y()::foo;\n" +
				"    i.foo(10);\n" +
				"  }\n" +
				"}\n" +
				"class Y {\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"  void foo(int x) {\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0007", expectedUnitToString);
	}
	// Reference expression - primary:: form, with type arguments.
	public void test0008() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = new Y()::<String>foo;\n" +
				"        i.foo(10); \n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"        void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = new Y()::<String>foo;\n" +
				"    i.foo(10);\n" +
				"  }\n" +
				"}\n" +
				"class Y {\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"  void foo(int x) {\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0008", expectedUnitToString);
	}
	// Reference expression - X<T>:: form, without type arguments.
	public void test0009() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(Y<String> y, int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y<String>::foo;\n" +
				"        i.foo(new Y<String>(), 10); \n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"        void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(Y<String> y, int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y<String>::foo;\n" +
				"    i.foo(new Y<String>(), 10);\n" +
				"  }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"  void foo(int x) {\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0009", expectedUnitToString);
	}
	// Reference expression - X<T>:: form, with type arguments.
	public void test0010() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(Y<String> y, int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y<String>::<String>foo;\n" +
				"        i.foo(new Y<String>(), 10); \n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"        void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(Y<String> y, int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y<String>::<String>foo;\n" +
				"    i.foo(new Y<String>(), 10);\n" +
				"  }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"  void foo(int x) {\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0010", expectedUnitToString);
	}
	// Reference expression - X<T>.Name:: form, without type arguments.
	public void test0011() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(Y<String>.Z z, int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y<String>.Z::foo;\n" +
				"        i.foo(new Y<String>().new Z(), 10); \n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"    class Z {\n" +
				"        void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(Y<String>.Z z, int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y<String>.Z::foo;\n" +
				"    i.foo(new Y<String>().new Z(), 10);\n" +
				"  }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"  class Z {\n" +
				"    Z() {\n" +
				"      super();\n" +
				"    }\n" +
				"    void foo(int x) {\n" +
				"      System.out.println(x);\n" +
				"    }\n" +
				"  }\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0011", expectedUnitToString);
	}
	// Reference expression - X<T>.Name:: form, with type arguments.
	public void test0012() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(Y<String>.Z z, int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y<String>.Z::<String>foo;\n" +
				"        i.foo(new Y<String>().new Z(), 10); \n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"    class Z {\n" +
				"        void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(Y<String>.Z z, int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y<String>.Z::<String>foo;\n" +
				"    i.foo(new Y<String>().new Z(), 10);\n" +
				"  }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"  class Z {\n" +
				"    Z() {\n" +
				"      super();\n" +
				"    }\n" +
				"    void foo(int x) {\n" +
				"      System.out.println(x);\n" +
				"    }\n" +
				"  }\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0012", expectedUnitToString);
	}
	// Reference expression - X<T>.Y<K>:: form, without type arguments.
	public void test0013() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(Y<String>.Z<Integer> z, int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y<String>.Z<Integer>::foo;\n" +
				"        i.foo(new Y<String>().new Z<Integer>(), 10); \n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"    class Z<K> {\n" +
				"        void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(Y<String>.Z<Integer> z, int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y<String>.Z<Integer>::foo;\n" +
				"    i.foo(new Y<String>().new Z<Integer>(), 10);\n" +
				"  }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"  class Z<K> {\n" +
				"    Z() {\n" +
				"      super();\n" +
				"    }\n" +
				"    void foo(int x) {\n" +
				"      System.out.println(x);\n" +
				"    }\n" +
				"  }\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0013", expectedUnitToString);
	}
	// Reference expression - X<T>.Y<K>:: form, with type arguments.
	public void test0014() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(Y<String>.Z<Integer> z, int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y<String>.Z<Integer>::<String>foo;\n" +
				"        i.foo(new Y<String>().new Z<Integer>(), 10); \n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"    class Z<K> {\n" +
				"        void foo(int x) {\n" +
				"	    System.out.println(x);\n" +
				"        }\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(Y<String>.Z<Integer> z, int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = Y<String>.Z<Integer>::<String>foo;\n" +
				"    i.foo(new Y<String>().new Z<Integer>(), 10);\n" +
				"  }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"  class Z<K> {\n" +
				"    Z() {\n" +
				"      super();\n" +
				"    }\n" +
				"    void foo(int x) {\n" +
				"      System.out.println(x);\n" +
				"    }\n" +
				"  }\n" +
				"  Y() {\n" +
				"    super();\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0014", expectedUnitToString);
	}
	// Constructor reference expression - X<T>.Y<K>::new form, with type arguments.
	public void test0015() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(Y<String> y);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y<String>.Z<Integer>::<String>new;\n" +
				"        i.foo(new Y<String>()); \n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"    class Z<K> {\n" +
				"        Z() {\n" +
				"            System.out.println(\"Y<T>.Z<K>::new\");\n" +
				"        }\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
					"  void foo(Y<String> y);\n" +
					"}\n" +
					"public class X {\n" +
					"  public X() {\n" +
					"    super();\n" +
					"  }\n" +
					"  public static void main(String[] args) {\n" +
					"    I i = Y<String>.Z<Integer>::<String>new;\n" +
					"    i.foo(new Y<String>());\n" +
					"  }\n" +
					"}\n" +
					"class Y<T> {\n" +
					"  class Z<K> {\n" +
					"    Z() {\n" +
					"      super();\n" +
					"      System.out.println(\"Y<T>.Z<K>::new\");\n" +
					"    }\n" +
					"  }\n" +
					"  Y() {\n" +
					"    super();\n" +
					"  }\n" +
					"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0015", expectedUnitToString);
	}
	// Reference expression - PrimitiveType[]:: form, with type arguments.
	public void test0016() throws IOException {
		String source =
				"interface I {\n" +
				"    Object copy(int [] ia);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = int[]::<String>clone;\n" +
				"        i.copy(new int[10]); \n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  Object copy(int[] ia);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = int[]::<String>clone;\n" +
				"    i.copy(new int[10]);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0016", expectedUnitToString);
	}
	// Reference expression - Name[]:: form, with type arguments.
	public void test0017() throws IOException {
		String source =
				"interface I {\n" +
				"    Object copy(X [] ia);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = X[]::<String>clone;\n" +
				"        i.copy(new X[10]); \n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  Object copy(X[] ia);\n" +
				"}\n" +
				"public class X {\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = X[]::<String>clone;\n" +
				"    i.copy(new X[10]);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0017", expectedUnitToString);
	}
	// Reference expression - X<T>.Y<K>[]:: form, with type arguments.
	public void test0018() throws IOException {
		String source =
				"interface I {\n" +
				"    Object copy(X<String>.Y<Integer> [] p);\n" +
				"}\n" +
				"public class X<T>  {\n" +
				"    class Y<K> {\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = X<String>.Y<Integer>[]::<String>clone;\n" +
				"        X<String>.Y<Integer>[] xs = null;\n" +
				"        i.copy(xs); \n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  Object copy(X<String>.Y<Integer>[] p);\n" +
				"}\n" +
				"public class X<T> {\n" +
				"  class Y<K> {\n" +
				"    Y() {\n" +
				"      super();\n" +
				"    }\n" +
				"  }\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = X<String>.Y<Integer>[]::<String>clone;\n" +
				"    X<String>.Y<Integer>[] xs = null;\n" +
				"    i.copy(xs);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0018", expectedUnitToString);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384320, syntax error while mixing 308 and 335.
	public void test0019() throws IOException {
		String source =
				"interface I {\n" +
				"    void foo(X<String> s, int x);\n" +
				"}\n" +
				"public class X<T> {\n" +
				"    I i = X<@Foo({\"hello\"}) String>::foo;\n" +
				"    void foo(int x) {\n" +
				"    }\n" +
				"}\n" +
				"@interface Foo {\n" +
				"    String [] value();\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  void foo(X<String> s, int x);\n" +
				"}\n" +
				"public class X<T> {\n" +
				"  I i = X<@Foo({\"hello\"}) String>::foo;\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  void foo(int x) {\n" +
				"  }\n" +
				"}\n" +
				"@interface Foo {\n" +
				"  String[] value();\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0019", expectedUnitToString);
	}

	// Reference expression - Name::new forms, with/without type arguments.
	public void test0020() throws IOException {
		String source =
				"interface I {\n" +
				"    Y foo(int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    class Z extends Y {\n" +
				"        public Z(int x) {\n" +
				"            super(x);\n" +
				"            System.out.println(\"Z\"+x);\n" +
				"        }\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        Y y;\n" +
				"        I i = Y::new;\n" +
				"        y = i.foo(10); \n" +
				"        i = X.Z::new;\n" +
				"        y = i.foo(20); \n" +
				"        i = W<Integer>::new;\n" +
				"        y = i.foo(23);\n" +
				"    }\n" +
				"}\n" +
				"class W<T> extends Y {\n" +
				"    public W(T x) {\n" +
				"        super(0);\n" +
				"        System.out.println(x);\n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"    public Y(int x) {\n" +
				"        System.out.println(x);\n" +
				"    }\n" +
				"}\n";
		String expectedUnitToString =
				"interface I {\n" +
				"  Y foo(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"  class Z extends Y {\n" +
				"    public Z(int x) {\n" +
				"      super(x);\n" +
				"      System.out.println((\"Z\" + x));\n" +
				"    }\n" +
				"  }\n" +
				"  public X() {\n" +
				"    super();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    Y y;\n" +
				"    I i = Y::new;\n" +
				"    y = i.foo(10);\n" +
				"    i = X.Z::new;\n" +
				"    y = i.foo(20);\n" +
				"    i = W<Integer>::new;\n" +
				"    y = i.foo(23);\n" +
				"  }\n" +
				"}\n" +
				"class W<T> extends Y {\n" +
				"  public W(T x) {\n" +
				"    super(0);\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n" +
				"class Y {\n" +
				"  public Y(int x) {\n" +
				"    super();\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0003", expectedUnitToString);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385132
	public void test385132() throws IOException {
		String source = "::";
		String expectedErrorString =
				"----------\n" +
				"1. ERROR in test385132 (at line 1)\n" +
				"	::\n" +
				"	^^\n" +
				"Syntax error on token \"::\", delete this token\n" +
				"----------\n";

		checkParse(CHECK_PARSER , source.toCharArray(), expectedErrorString, "test385132", null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385374, Support for 308 style type annotations on 335 constructs.
	public void test385374() throws IOException {
		String source =
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"@interface TypeAnnotation {\n" +
				"}\n" +
				"\n" +
				"class X<T> {\n" +
				"	 // Primitive array form\n" +
				"	 I x1 = @TypeAnnotation int []::clone;\n" +
				"	 // Primitive array form with dimension annotations.\n" +
				"	 I x2 = @TypeAnnotation int @ArrayAnnotation[]@ArrayAnnotation[]::clone; \n" +
				"	 // Primitive array form with dimension annotations and type parameter annotations.\n" +
				"	 I x3 = @TypeAnnotation int @ArrayAnnotation[]@ArrayAnnotation[]::<@TypeParameterAnnotation String>clone; \n" +
				"	 // Reference type name form\n" +
				"	 I x4 = @TypeAnnotation X::clone;\n" +
				"	 // Reference type name array form\n" +
				"	 I x5 = @TypeAnnotation X []::clone;\n" +
				"	 // Reference type name array form with dimension annotations.\n" +
				"	 I x6 = @TypeAnnotation X @ArrayAnnotation[]@ArrayAnnotation[]::clone; \n" +
				"	 // Reference type name array form with dimension annotations and type parameter annotations.\n" +
				"	 I x7 = @TypeAnnotation X @ArrayAnnotation[]@ArrayAnnotation[]::<@TypeParameterAnnotation String>clone; \n" +
				"	 // Generic type array form with dimension annotations and type parameter annotations.\n" +
				"	 I x8 = @TypeAnnotation X<@TypeParameterAnnotation String> @ArrayAnnotation[]@ArrayAnnotation[]::<@TypeParameterAnnotation String>clone; \n" +
				"	 // Qualified generic type array form with dimension annotations and type parameter annotations.\n" +
				"	 I x9 = @TypeAnnotation X<@TypeParameterAnnotation String>.Y<@TypeParameterAnnotation String> @ArrayAnnotation[]@ArrayAnnotation[]::<@TypeParameterAnnotation String>clone; \n" +
				"}\n";

		String expectedUnitToString =
				"interface I {\n" +
				"  void foo();\n" +
				"}\n" +
				"@interface TypeAnnotation {\n" +
				"}\n" +
				"class X<T> {\n" +
				"  I x1 = @TypeAnnotation int[]::clone;\n" +
				"  I x2 = @TypeAnnotation int @ArrayAnnotation [] @ArrayAnnotation []::clone;\n" +
				"  I x3 = @TypeAnnotation int @ArrayAnnotation [] @ArrayAnnotation []::<@TypeParameterAnnotation String>clone;\n" +
				"  I x4 = @TypeAnnotation X::clone;\n" +
				"  I x5 = @TypeAnnotation X[]::clone;\n" +
				"  I x6 = @TypeAnnotation X @ArrayAnnotation [] @ArrayAnnotation []::clone;\n" +
				"  I x7 = @TypeAnnotation X @ArrayAnnotation [] @ArrayAnnotation []::<@TypeParameterAnnotation String>clone;\n" +
				"  I x8 = @TypeAnnotation X<@TypeParameterAnnotation String> @ArrayAnnotation [] @ArrayAnnotation []::<@TypeParameterAnnotation String>clone;\n" +
				"  I x9 = @TypeAnnotation X<@TypeParameterAnnotation String>.Y<@TypeParameterAnnotation String> @ArrayAnnotation [] @ArrayAnnotation []::<@TypeParameterAnnotation String>clone;\n" +
				"  X() {\n" +
				"    super();\n" +
				"  }\n" +
				"}\n";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test385374", expectedUnitToString);
	}
	/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=385374, Support for 308 style type annotations on 335 constructs - make sure illegal modifiers are rejected
	   This test has been rendered meaningless as the grammar has been so throughly changed - Type annotations are not accepted via modifiers in the first place.
	   Disabling this test as we don't want fragile and unstable tests that are at the whimsy of the diagnose parser's complex algorithms.
	*/
	public void test385374a() throws IOException {
		// Nop.
	}
}
