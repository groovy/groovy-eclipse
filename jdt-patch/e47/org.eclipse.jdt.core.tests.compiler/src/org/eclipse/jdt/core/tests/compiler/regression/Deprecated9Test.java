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
			"The type N1.N2 has been deprecated and marked for removal\n" +
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
			"The field E01.x has been deprecated and marked for removal\n" +
			"----------\n" +
			"2. ERROR in test1\\E02.java (at line 5)\n" +
			"	System.out.println(E01.y);\n" +
			"	                       ^\n" +
			"The field E01.y has been deprecated and marked for removal\n" +
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
	// Bug 354536 - compiling package-info.java still depends on the order of compilation units
	// - option is disabled
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
	// Bug 354536 - compiling package-info.java still depends on the order of compilation units
	// some warnings suppressed
	public void test005c() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.WARNING);
		this.runNegativeTest(
			true,
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
			},
			null, customOptions,
			"----------\n" +
			"1. WARNING in p2\\C.java (at line 5)\n" + 
			"	a.foo();\n" + 
			"	  ^^^^^\n" + 
			"The method foo() from the type X.Inner has been deprecated and marked for removal\n" + 
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	// https://bugs.eclipse.org/384870 - [compiler] @Deprecated annotation not detected if preceded by other annotation
	// old-style deprecation
	public void test006() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.ERROR);
		this.runNegativeTest(
			true,
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
			},
			null, customOptions,
			"----------\n" + 
			"1. WARNING in test1\\E02.java (at line 3)\n" + 
			"	public void foo(E01 arg) {\n" + 
			"	                ^^^\n" + 
			"The type E01 is deprecated\n" + 
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
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

	public static Class<?> testClass() {
		return Deprecated9Test.class;
	}

}
