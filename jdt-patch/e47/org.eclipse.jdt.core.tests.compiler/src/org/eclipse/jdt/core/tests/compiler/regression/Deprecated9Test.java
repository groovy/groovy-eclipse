/*******************************************************************************
 * Copyright (c) 2017 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class Deprecated9Test extends AbstractRegressionTest {
	public Deprecated9Test(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	static {
//		TESTS_NAMES = new String[] { "test007" };
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
	// guard variant for DeprecatedTest#test015 using an annotation
	public void test002() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
		this.runNegativeTest(
			true,
			new String[] {
				"p/M1.java",
				"package p;\n" +
				"public class M1 {\n" +
				"  void bar() {\n" +
				"    a.N1.N2.N3 m = null;\n" +
				"    m.foo();\n" +
				"  }\n" +
				"}\n",
				"a/N1.java",
				"package a;\n" +
				"public class N1 {\n" +
				"  @Deprecated(since=\"1.2\",forRemoval=true)\n" +
				"  public class N2 {" +
				"    public void foo() {}" +
				"    public class N3 {" +
				"      public void foo() {}" +
				"    }" +
				"  }" +
				"}\n",
			},
			null, customOptions,
			"----------\n" +
			"1. ERROR in p\\M1.java (at line 4)\n" +
			"	a.N1.N2.N3 m = null;\n" +
			"	     ^^\n" +
			"The type N1.N2 has been deprecated since version 1.2 and marked for removal\n" +
			"----------\n" +
			"2. ERROR in p\\M1.java (at line 4)\n" +
			"	a.N1.N2.N3 m = null;\n" +
			"	        ^^\n" +
			"The type N1.N2.N3 has been deprecated and marked for removal\n" +
			"----------\n" +
			"3. ERROR in p\\M1.java (at line 5)\n" +
			"	m.foo();\n" +
			"	  ^^^^^\n" +
			"The method foo() from the type N1.N2.N3 has been deprecated and marked for removal\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test002binary() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
		runner.testFiles =
			new String[] {
				"a/N1.java",
				"package a;\n" +
				"public class N1 {\n" +
				"  @Deprecated(since=\"1.2\",forRemoval=true)\n" +
				"  public class N2 {" +
				"    public void foo() {}" +
				"    public class N3 {" +
				"      public void foo() {}" +
				"    }" +
				"  }" +
				"}\n"
			};
		runner.runConformTest();

		runner.shouldFlushOutputDirectory = false;
		runner.testFiles =
			new String[] {
				"p/M1.java",
				"package p;\n" +
				"public class M1 {\n" +
				"  void bar() {\n" +
				"    a.N1.N2.N3 m = null;\n" +
				"    m.foo();\n" +
				"  }\n" +
				"}\n"
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p\\M1.java (at line 4)\n" +
			"	a.N1.N2.N3 m = null;\n" +
			"	     ^^\n" +
			"The type N1.N2 has been deprecated since version 1.2 and marked for removal\n" +
			"----------\n" +
			"2. ERROR in p\\M1.java (at line 4)\n" +
			"	a.N1.N2.N3 m = null;\n" +
			"	        ^^\n" +
			"The type N1.N2.N3 has been deprecated and marked for removal\n" +
			"----------\n" +
			"3. ERROR in p\\M1.java (at line 5)\n" +
			"	m.foo();\n" +
			"	  ^^^^^\n" +
			"The method foo() from the type N1.N2.N3 has been deprecated and marked for removal\n" +
			"----------\n";
		runner.javacTestOptions =
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191909
	public void test004() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		this.runNegativeTest(
			true,
			new String[] {
				"test1/E01.java",
				"package test1;\n" +
				"public class E01 {\n" +
				"	@Deprecated(forRemoval=true,since=\"3\")\n" +
				"	public static int x = 5, y= 10;\n" +
				"}",
				"test1/E02.java",
				"package test1;\n" +
				"public class E02 {\n" +
				"	public void foo() {\n" +
				"		System.out.println(E01.x);\n" +
				"		System.out.println(E01.y);\n" +
				"	}\n" +
				"}"
			},
			null, customOptions,
			"----------\n" +
			"1. ERROR in test1\\E02.java (at line 4)\n" +
			"	System.out.println(E01.x);\n" +
			"	                       ^\n" +
			"The field E01.x has been deprecated since version 3 and marked for removal\n" +
			"----------\n" +
			"2. ERROR in test1\\E02.java (at line 5)\n" +
			"	System.out.println(E01.y);\n" +
			"	                       ^\n" +
			"The field E01.y has been deprecated since version 3 and marked for removal\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	// Bug 354536 - compiling package-info.java still depends on the order of compilation units
	public void test005a() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.ERROR);
		this.runNegativeTest(
			true,
			new String[] {
				"p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"    public static class Inner {" +
				"        public void foo() {}\n" +
				"    }\n" +
				"}\n",
				"p1/package-info.java",
				"@java.lang.Deprecated(forRemoval=true)\n" +
				"package p1;\n",
				"p2/C.java",
				"package p2;\n" +
				"public class C {\n" +
				"    void bar(p1.X.Inner a) {\n" +
				"        a.foo();\n" +
				"    }\n" +
				"}\n",
			},
			null, customOptions,
			"----------\n" +
			"1. ERROR in p2\\C.java (at line 3)\n" + 
			"	void bar(p1.X.Inner a) {\n" + 
			"	            ^\n" + 
			"The type X has been deprecated and marked for removal\n" + 
			"----------\n" + 
			"2. ERROR in p2\\C.java (at line 3)\n" + 
			"	void bar(p1.X.Inner a) {\n" + 
			"	              ^^^^^\n" + 
			"The type X.Inner has been deprecated and marked for removal\n" + 
			"----------\n" + 
			"3. ERROR in p2\\C.java (at line 4)\n" + 
			"	a.foo();\n" + 
			"	  ^^^^^\n" + 
			"The method foo() from the type X.Inner has been deprecated and marked for removal\n" + 
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test005b() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.IGNORE);
		this.runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"    public static class Inner {" +
				"        public void foo() {}\n" +
				"    }\n" +
				"}\n",
				"p1/package-info.java",
				"@java.lang.Deprecated(forRemoval=true)\n" +
				"package p1;\n",
				"p2/C.java",
				"package p2;\n" +
				"public class C {\n" +
				"    void bar(p1.X.Inner a) {\n" +
				"        a.foo();\n" +
				"    }\n" +
				"}\n",
			},
			customOptions);
	}
	public void test005c() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		runner.customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.WARNING);
		runner.testFiles =
			new String[] {
				"p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"    public static class Inner {\n" +
				"		 @java.lang.Deprecated(forRemoval=true)\n" + 
				"        public void foo() {}\n" +
				"    }\n" +
				"}\n",
				"p1/package-info.java",
				"@java.lang.Deprecated(forRemoval=false)\n" +
				"package p1;\n",
				"p2/C.java",
				"package p2;\n" +
				"public class C {\n" +
				"	 @SuppressWarnings(\"deprecation\")\n" +
				"    void bar(p1.X.Inner a) {\n" +
				"        a.foo();\n" +
				"    }\n" +
				"}\n",
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in p2\\C.java (at line 5)\n" + 
			"	a.foo();\n" + 
			"	  ^^^^^\n" + 
			"The method foo() from the type X.Inner has been deprecated and marked for removal\n" + 
			"----------\n";
		runner.runWarningTest();
	}
	public void test006() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		runner.customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"test1/E02.java",
				"package test1;\n" +
				"public class E02 {\n" +
				"	public void foo(E01 arg) {\n" +
				"		// nop\n" +
				"	}\n" +
				"}",
				"test1/E01.java",
				"package test1;\n" +
				"@SuppressWarnings(\"all\") @Deprecated(since=\"4\")\n" +
				"public class E01 {\n" +
				"	public static int x = 5;\n" +
				"}"
			};
		runner.expectedCompilerLog =
			"----------\n" + 
			"1. WARNING in test1\\E02.java (at line 3)\n" + 
			"	public void foo(E01 arg) {\n" + 
			"	                ^^^\n" + 
			"The type E01 is deprecated since version 4\n" + 
			"----------\n";
		runner.runWarningTest();
	}
	// method overriding
	public void test007() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, CompilerOptions.ENABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"	 @java.lang.Deprecated(forRemoval=false)\n" +
				"    public void foo() {}\n" +
				"	 @java.lang.Deprecated(forRemoval=true)\n" +
				"	 public void bar() {}\n" +
				"}\n",
				"p2/C.java",
				"package p2;\n" +
				"import p1.X;\n" +
				"public class C extends X {\n" +
				"    @Override public void foo() {}\n" +
				"    @Override public void bar() {}\n" +
				"}\n",
			},
			null, customOptions,
			"----------\n" + 
			"1. WARNING in p2\\C.java (at line 4)\n" + 
			"	@Override public void foo() {}\n" + 
			"	                      ^^^^^\n" + 
			"The method C.foo() overrides a deprecated method from X\n" + 
			"----------\n" + 
			"2. ERROR in p2\\C.java (at line 5)\n" + 
			"	@Override public void bar() {}\n" + 
			"	                      ^^^^^\n" + 
			"The method C.bar() overrides a method from X that has been deprecated and marked for removal\n" + 
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testSinceSource() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD, CompilerOptions.ENABLED);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"test1/E01.java",
				"package test1;\n" +
				"public class E01 {\n" +
				"	@Deprecated(since=\"1.0\") protected static class Old {}\n" +
				"	@Deprecated(since=\"2\") public static int x = 5, y= 10;\n" +
				"	@Deprecated(since=\"3.0.0\") public E01() {}\n" +
				"	@Deprecated(since=\"4-SNAPSHOT\") protected void old() {}\n" +
				"}",
				"test1/E02.java",
				"package test1;\n" +
				"public class E02 {\n" +
				"	public void foo() {\n" +
				"		System.out.println(new E01.Old());\n" +
				"		E01 e = new E01();\n" +
				"		e.old();\n" +
				"		System.out.println(E01.x);\n" +
				"		System.out.println(E01.y);\n" +
				"	}\n" +
				"	class E03 extends E01 {\n" +
				"		protected void old() {}\n" +
				"	}\n" +
				"}"
			};
		runner.expectedCompilerLog =
			"----------\n" + 
			"1. WARNING in test1\\E02.java (at line 4)\n" + 
			"	System.out.println(new E01.Old());\n" + 
			"	                       ^^^^^^^^^\n" + 
			"The constructor E01.Old() is deprecated since version 1.0\n" + 
			"----------\n" + 
			"2. WARNING in test1\\E02.java (at line 4)\n" + 
			"	System.out.println(new E01.Old());\n" + 
			"	                           ^^^\n" + 
			"The type E01.Old is deprecated since version 1.0\n" + 
			"----------\n" + 
			"3. WARNING in test1\\E02.java (at line 5)\n" + 
			"	E01 e = new E01();\n" + 
			"	            ^^^^^\n" + 
			"The constructor E01() is deprecated since version 3.0.0\n" + 
			"----------\n" + 
			"4. WARNING in test1\\E02.java (at line 6)\n" + 
			"	e.old();\n" + 
			"	  ^^^^^\n" + 
			"The method old() from the type E01 is deprecated since version 4-SNAPSHOT\n" + 
			"----------\n" + 
			"5. WARNING in test1\\E02.java (at line 7)\n" + 
			"	System.out.println(E01.x);\n" + 
			"	                       ^\n" + 
			"The field E01.x is deprecated since version 2\n" + 
			"----------\n" + 
			"6. WARNING in test1\\E02.java (at line 8)\n" + 
			"	System.out.println(E01.y);\n" + 
			"	                       ^\n" + 
			"The field E01.y is deprecated since version 2\n" + 
			"----------\n" + 
			"7. WARNING in test1\\E02.java (at line 10)\n" + 
			"	class E03 extends E01 {\n" + 
			"	      ^^^\n" + 
			"The constructor E01() is deprecated since version 3.0.0\n" + 
			"----------\n" + 
			"8. WARNING in test1\\E02.java (at line 11)\n" + 
			"	protected void old() {}\n" + 
			"	               ^^^^^\n" + 
			"The method E02.E03.old() overrides a method from E01 that is deprecated since version 4-SNAPSHOT\n" + 
			"----------\n";
		runner.runWarningTest();
	}
	public void testSinceBinary() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD, CompilerOptions.ENABLED);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"test1/E01.java",
				"package test1;\n" +
				"public class E01 {\n" +
				"	@Deprecated(since=\"1.0\") protected static class Old {}\n" +
				"	@Deprecated(since=\"2\") public static int x = 5, y= 10;\n" +
				"	@Deprecated(since=\"3.0.0\") public E01() {}\n" +
				"	@Deprecated(since=\"4-SNAPSHOT\") protected void old() {}\n" +
				"}"
			};
		runner.runConformTest();

		runner.shouldFlushOutputDirectory = false;
		runner.testFiles =
			new String[] {
				"test1/E02.java",
				"package test1;\n" +
				"public class E02 {\n" +
				"	public void foo() {\n" +
				"		System.out.println(new E01.Old());\n" +
				"		E01 e = new E01();\n" +
				"		e.old();\n" +
				"		System.out.println(E01.x);\n" +
				"		System.out.println(E01.y);\n" +
				"	}\n" +
				"	class E03 extends E01 {\n" +
				"		protected void old() {}\n" +
				"	}\n" +
				"}"
			};
		runner.expectedCompilerLog =
			"----------\n" + 
			"1. WARNING in test1\\E02.java (at line 4)\n" + 
			"	System.out.println(new E01.Old());\n" + 
			"	                       ^^^^^^^^^\n" + 
			"The constructor E01.Old() is deprecated since version 1.0\n" + 
			"----------\n" + 
			"2. WARNING in test1\\E02.java (at line 4)\n" + 
			"	System.out.println(new E01.Old());\n" + 
			"	                           ^^^\n" + 
			"The type E01.Old is deprecated since version 1.0\n" + 
			"----------\n" + 
			"3. WARNING in test1\\E02.java (at line 5)\n" + 
			"	E01 e = new E01();\n" + 
			"	            ^^^^^\n" + 
			"The constructor E01() is deprecated since version 3.0.0\n" + 
			"----------\n" + 
			"4. WARNING in test1\\E02.java (at line 6)\n" + 
			"	e.old();\n" + 
			"	  ^^^^^\n" + 
			"The method old() from the type E01 is deprecated since version 4-SNAPSHOT\n" + 
			"----------\n" + 
			"5. WARNING in test1\\E02.java (at line 7)\n" + 
			"	System.out.println(E01.x);\n" + 
			"	                       ^\n" + 
			"The field E01.x is deprecated since version 2\n" + 
			"----------\n" + 
			"6. WARNING in test1\\E02.java (at line 8)\n" + 
			"	System.out.println(E01.y);\n" + 
			"	                       ^\n" + 
			"The field E01.y is deprecated since version 2\n" + 
			"----------\n" + 
			"7. WARNING in test1\\E02.java (at line 10)\n" + 
			"	class E03 extends E01 {\n" + 
			"	      ^^^\n" + 
			"The constructor E01() is deprecated since version 3.0.0\n" + 
			"----------\n" + 
			"8. WARNING in test1\\E02.java (at line 11)\n" + 
			"	protected void old() {}\n" + 
			"	               ^^^^^\n" + 
			"The method E02.E03.old() overrides a method from E01 that is deprecated since version 4-SNAPSHOT\n" + 
			"----------\n";
		runner.runWarningTest();
	}
	public void testSinceTerminally() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD, CompilerOptions.ENABLED);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"test1/E01.java",
				"package test1;\n" +
				"public class E01 {\n" +
				"	@Deprecated(since=\"1.0\", forRemoval=true) protected static class Old {}\n" +
				"	@Deprecated(since=\"2\", forRemoval=true) public static int x = 5, y= 10;\n" +
				"	@Deprecated(since=\"3.0.0\", forRemoval=true) public E01() {}\n" +
				"	@Deprecated(since=\"4-SNAPSHOT\", forRemoval=true) protected void old() {}\n" +
				"}",
				"test1/E02.java",
				"package test1;\n" +
				"public class E02 {\n" +
				"	public void foo() {\n" +
				"		System.out.println(new E01.Old());\n" +
				"		E01 e = new E01();\n" +
				"		e.old();\n" +
				"		System.out.println(E01.x);\n" +
				"		System.out.println(E01.y);\n" +
				"	}\n" +
				"	class E03 extends E01 {\n" +
				"		protected void old() {}\n" +
				"	}\n" +
				"}"
			};
		runner.expectedCompilerLog =
			"----------\n" + 
			"1. ERROR in test1\\E02.java (at line 4)\n" + 
			"	System.out.println(new E01.Old());\n" + 
			"	                       ^^^^^^^^^\n" + 
			"The constructor E01.Old() has been deprecated since version 1.0 and marked for removal\n" + 
			"----------\n" + 
			"2. ERROR in test1\\E02.java (at line 4)\n" + 
			"	System.out.println(new E01.Old());\n" + 
			"	                           ^^^\n" + 
			"The type E01.Old has been deprecated since version 1.0 and marked for removal\n" + 
			"----------\n" + 
			"3. ERROR in test1\\E02.java (at line 5)\n" + 
			"	E01 e = new E01();\n" + 
			"	            ^^^^^\n" + 
			"The constructor E01() has been deprecated since version 3.0.0 and marked for removal\n" + 
			"----------\n" + 
			"4. ERROR in test1\\E02.java (at line 6)\n" + 
			"	e.old();\n" + 
			"	  ^^^^^\n" + 
			"The method old() from the type E01 has been deprecated since version 4-SNAPSHOT and marked for removal\n" + 
			"----------\n" + 
			"5. ERROR in test1\\E02.java (at line 7)\n" + 
			"	System.out.println(E01.x);\n" + 
			"	                       ^\n" + 
			"The field E01.x has been deprecated since version 2 and marked for removal\n" + 
			"----------\n" + 
			"6. ERROR in test1\\E02.java (at line 8)\n" + 
			"	System.out.println(E01.y);\n" + 
			"	                       ^\n" + 
			"The field E01.y has been deprecated since version 2 and marked for removal\n" + 
			"----------\n" + 
			"7. ERROR in test1\\E02.java (at line 10)\n" + 
			"	class E03 extends E01 {\n" + 
			"	      ^^^\n" + 
			"The constructor E01() has been deprecated since version 3.0.0 and marked for removal\n" + 
			"----------\n" + 
			"8. ERROR in test1\\E02.java (at line 11)\n" + 
			"	protected void old() {}\n" + 
			"	               ^^^^^\n" + 
			"The method E02.E03.old() overrides a method from E01 that has been deprecated since version 4-SNAPSHOT and marked for removal\n" + 
			"----------\n";
		runner.javacTestOptions =
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
	public static Class<?> testClass() {
		return Deprecated9Test.class;
	}

}
