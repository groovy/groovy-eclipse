/*******************************************************************************
 * Copyright (c) 2017, 2018 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class Deprecated9Test extends AbstractRegressionTest9 {
	public Deprecated9Test(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	static {
//		TESTS_NAMES = new String[] { "test007" };
	}

	@Override
	protected INameEnvironment[] getClassLibs(boolean useDefaultClasspaths) {
		if (this.javaClassLib != null) {
			String encoding = getCompilerOptions().get(CompilerOptions.OPTION_Encoding);
			if ("".equals(encoding))
				encoding = null;
			return new INameEnvironment[] {
					this.javaClassLib,
					new FileSystem(this.classpaths, new String[]{}, // ignore initial file names
							encoding // default encoding
					)};
		}
		return super.getClassLibs(useDefaultClasspaths);
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
	public void testDeprecatedPackageExport() {
		associateToModule("mod1",
				"p1/package-info.java", "p1/C1.java",
				"p2/package-info.java", "p2/C2.java",
				"p3/package-info.java", "p3/C3.java",
				"p4/package-info.java", "p4/C4.java");
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.ERROR);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"p1/package-info.java",
				"@Deprecated package p1;\n",
				"p1/C1.java",
				"package p1; public class C1 {}\n",
				"p2/package-info.java",
				"@Deprecated(since=\"13\") package p2;\n",
				"p2/C2.java",
				"package p2; public class C2 {}\n",
				"p3/package-info.java",
				"@Deprecated(since=\"13\",forRemoval=true) package p3;\n",
				"p3/C3.java",
				"package p3; public class C3 {}\n",
				"p4/package-info.java",
				"@Deprecated(since=\"14\",forRemoval=true) package p4;\n",
				"p4/C4.java",
				"package p4; public class C4 {}\n",
				"module-info.java",
				"module mod1 {\n" +
				"	exports p1;\n" +
				"	exports p2;\n" +
				"	exports p3;\n" +
				"	opens p4;\n" +
				"}\n"
			};
		runner.runConformTest();
	}
	public void testDeprecatedModule() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"folder0/module-info.java",
				"@Deprecated module mod.dep {}\n",
				"folder1/module-info.java",
				"@Deprecated(since=\"42\") module mod.dep.since {}\n",
				"folder2/module-info.java",
				"@Deprecated(forRemoval=true) module mod.dep.terminally {}\n",
				"folder3/module-info.java",
				"@Deprecated(since=\"42\",forRemoval=true) module mod.dep.since.terminally {}\n",
				"module-info.java",
				"module mod1 {\n" +
				"	requires mod.dep;\n" +
				"	requires mod.dep.since;\n" +
				"	requires mod.dep.terminally;\n" +
				"	requires mod.dep.since.terminally;\n" +
				"}\n"
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in module-info.java (at line 2)\n" +
			"	requires mod.dep;\n" +
			"	         ^^^^^^^\n" +
			"The module mod.dep is deprecated\n" +
			"----------\n" +
			"2. WARNING in module-info.java (at line 3)\n" +
			"	requires mod.dep.since;\n" +
			"	         ^^^^^^^^^^^^^\n" +
			"The module mod.dep.since is deprecated since version 42\n" +
			"----------\n" +
			"3. ERROR in module-info.java (at line 4)\n" +
			"	requires mod.dep.terminally;\n" +
			"	         ^^^^^^^^^^^^^^^^^^\n" +
			"The module mod.dep.terminally has been deprecated and marked for removal\n" +
			"----------\n" +
			"4. ERROR in module-info.java (at line 5)\n" +
			"	requires mod.dep.since.terminally;\n" +
			"	         ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The module mod.dep.since.terminally has been deprecated since version 42 and marked for removal\n" +
			"----------\n";
		runner.runNegativeTest();
	}
	public void testDeprecatedProvidedServices() {
		javacUsePathOption(" --module-source-path ");
		associateToModule("mod0", "module-info.java", "p1/IServiceDep.java", "p1/IServiceDepSince.java", "p1/IServiceTermDep.java", "p1/IServiceTermDepSince.java");
		associateToModule("mod1", "p1impl/ServiceDep.java", "p1impl/ServiceDepSince.java", "p1impl/ServiceTermDep.java", "p1impl/ServiceTermDepSince.java");
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.INFO);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.WARNING);
		runner.testFiles =
			new String[] {
				"p1/IServiceDep.java",
				"package p1;\n" +
				"@Deprecated\n" +
				"public interface IServiceDep {}\n",
				"p1/IServiceDepSince.java",
				"package p1;\n" +
				"@Deprecated(since=\"2\")\n" +
				"public interface IServiceDepSince {}\n",
				"p1/IServiceTermDep.java",
				"package p1;\n" +
				"@Deprecated(forRemoval=true)\n" +
				"public interface IServiceTermDep {}\n",
				"p1/IServiceTermDepSince.java",
				"package p1;\n" +
				"@Deprecated(since=\"3\",forRemoval=true)\n" +
				"public interface IServiceTermDepSince {}\n",
				"module-info.java",
				"module mod0 {\n" +
				"	exports p1;\n" +
				"}\n",
				"p1impl/ServiceDep.java",
				"package p1impl;\n" +
				"@Deprecated\n" +
				"public class ServiceDep implements p1.IServiceDep {}\n",
				"p1impl/ServiceDepSince.java",
				"package p1impl;\n" +
				"@Deprecated(since=\"2\")\n" +
				"public class ServiceDepSince implements p1.IServiceDepSince {}\n",
				"p1impl/ServiceTermDep.java",
				"package p1impl;\n" +
				"@Deprecated(forRemoval=true)\n" +
				"public class ServiceTermDep implements p1.IServiceTermDep {}\n",
				"p1impl/ServiceTermDepSince.java",
				"package p1impl;\n" +
				"@Deprecated(since=\"3\",forRemoval=true)\n" +
				"public class ServiceTermDepSince implements p1.IServiceTermDepSince {}\n",
				"mod1/module-info.java",
				"module mod1 {\n" +
				"	requires mod0;\n" +
				"	provides p1.IServiceDep with p1impl.ServiceDep;\n" +
				"	provides p1.IServiceDepSince with p1impl.ServiceDepSince;\n" +
				"	provides p1.IServiceTermDep with p1impl.ServiceTermDep;\n" +
				"	provides p1.IServiceTermDepSince with p1impl.ServiceTermDepSince;\n" +
				"}\n"
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. INFO in mod1\\module-info.java (at line 3)\n" +
			"	provides p1.IServiceDep with p1impl.ServiceDep;\n" +
			"	            ^^^^^^^^^^^\n" +
			"The type IServiceDep is deprecated\n" +
			"----------\n" +
			"2. INFO in mod1\\module-info.java (at line 3)\n" +
			"	provides p1.IServiceDep with p1impl.ServiceDep;\n" +
			"	                                    ^^^^^^^^^^\n" +
			"The type ServiceDep is deprecated\n" +
			"----------\n" +
			"3. INFO in mod1\\module-info.java (at line 4)\n" +
			"	provides p1.IServiceDepSince with p1impl.ServiceDepSince;\n" +
			"	            ^^^^^^^^^^^^^^^^\n" +
			"The type IServiceDepSince is deprecated since version 2\n" +
			"----------\n" +
			"4. INFO in mod1\\module-info.java (at line 4)\n" +
			"	provides p1.IServiceDepSince with p1impl.ServiceDepSince;\n" +
			"	                                         ^^^^^^^^^^^^^^^\n" +
			"The type ServiceDepSince is deprecated since version 2\n" +
			"----------\n" +
			"5. WARNING in mod1\\module-info.java (at line 5)\n" +
			"	provides p1.IServiceTermDep with p1impl.ServiceTermDep;\n" +
			"	            ^^^^^^^^^^^^^^^\n" +
			"The type IServiceTermDep has been deprecated and marked for removal\n" +
			"----------\n" +
			"6. WARNING in mod1\\module-info.java (at line 5)\n" +
			"	provides p1.IServiceTermDep with p1impl.ServiceTermDep;\n" +
			"	                                        ^^^^^^^^^^^^^^\n" +
			"The type ServiceTermDep has been deprecated and marked for removal\n" +
			"----------\n" +
			"7. WARNING in mod1\\module-info.java (at line 6)\n" +
			"	provides p1.IServiceTermDepSince with p1impl.ServiceTermDepSince;\n" +
			"	            ^^^^^^^^^^^^^^^^^^^^\n" +
			"The type IServiceTermDepSince has been deprecated since version 3 and marked for removal\n" +
			"----------\n" +
			"8. WARNING in mod1\\module-info.java (at line 6)\n" +
			"	provides p1.IServiceTermDepSince with p1impl.ServiceTermDepSince;\n" +
			"	                                             ^^^^^^^^^^^^^^^^^^^\n" +
			"The type ServiceTermDepSince has been deprecated since version 3 and marked for removal\n" +
			"----------\n";
		runner.runWarningTest();
	}
	public void testDeprecatedUsedServices() {
		javacUsePathOption(" --module-path ");

		associateToModule("mod0", "p1/IServiceDep.java", "p1/IServiceDepSince.java", "p1/IServiceTermDep.java", "p1/IServiceTermDepSince.java");
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.INFO);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.WARNING);
		runner.testFiles =
			new String[] {
				"p1/IServiceDep.java",
				"package p1;\n" +
				"@Deprecated\n" +
				"public interface IServiceDep {}\n",
				"p1/IServiceDepSince.java",
				"package p1;\n" +
				"@Deprecated(since=\"2\")\n" +
				"public interface IServiceDepSince {}\n",
				"p1/IServiceTermDep.java",
				"package p1;\n" +
				"@Deprecated(forRemoval=true)\n" +
				"public interface IServiceTermDep {}\n",
				"p1/IServiceTermDepSince.java",
				"package p1;\n" +
				"@Deprecated(since=\"3\",forRemoval=true)\n" +
				"public interface IServiceTermDepSince {}\n",
				"module-info.java",
				"module mod0 {\n" +
				"	exports p1;\n" +
				"}\n",
			};
		runner.runConformTest();

		runner.shouldFlushOutputDirectory = false;
		runner.testFiles =
			new String[] {
				"module-info.java",
				"module mod2 {\n" +
				"	requires mod0;\n" +
				"	uses p1.IServiceDep;\n" +
				"	uses p1.IServiceDepSince;\n" +
				"	uses p1.IServiceTermDep;\n" +
				"	uses p1.IServiceTermDepSince;\n" +
				"}\n"
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. INFO in module-info.java (at line 3)\n" +
			"	uses p1.IServiceDep;\n" +
			"	        ^^^^^^^^^^^\n" +
			"The type IServiceDep is deprecated\n" +
			"----------\n" +
			"2. INFO in module-info.java (at line 4)\n" +
			"	uses p1.IServiceDepSince;\n" +
			"	        ^^^^^^^^^^^^^^^^\n" +
			"The type IServiceDepSince is deprecated since version 2\n" +
			"----------\n" +
			"3. WARNING in module-info.java (at line 5)\n" +
			"	uses p1.IServiceTermDep;\n" +
			"	        ^^^^^^^^^^^^^^^\n" +
			"The type IServiceTermDep has been deprecated and marked for removal\n" +
			"----------\n" +
			"4. WARNING in module-info.java (at line 6)\n" +
			"	uses p1.IServiceTermDepSince;\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^\n" +
			"The type IServiceTermDepSince has been deprecated since version 3 and marked for removal\n" +
			"----------\n";
		runner.runWarningTest();
	}
	public void testBug533063_1() throws Exception {
		INameEnvironment save = this.javaClassLib;
		try {
			List<String> limitModules = Arrays.asList("java.se", "jdk.xml.bind");
			this.javaClassLib = new CustomFileSystem(limitModules);
			Runner runner = new Runner();
			runner.testFiles = new String[] {
				"module-info.java",
				"module my.mod {\n" +
				"	requires jdk.xml.bind;\n" +
				"}\n"
			};
			if (isJRE11Plus) {
				runner.expectedCompilerLog =
					"----------\n" +
					"1. ERROR in module-info.java (at line 2)\n" +
					"	requires jdk.xml.bind;\n" +
					"	         ^^^^^^^^^^^^\n" +
					"jdk.xml.bind cannot be resolved to a module\n" +
					"----------\n";
				runner.runNegativeTest();
			} else {
				runner.expectedCompilerLog =
					"----------\n" +
					"1. WARNING in module-info.java (at line 2)\n" +
					"	requires jdk.xml.bind;\n" +
					"	         ^^^^^^^^^^^^\n" +
					"The module jdk.xml.bind has been deprecated since version 9 and marked for removal\n" +
					"----------\n";
				runner.runWarningTest();
			}
		} finally {
			this.javaClassLib = save;
		}
	}
	public void testBug533063_2() throws Exception {
		javacUsePathOption(" --module-path ");

		runConformTest(new String[] {
			"dont.use/module-info.java",
			"@Deprecated(forRemoval=true,since=\"9\") module dont.use {}\n"
		});
		this.moduleMap.clear(); // don't use the source module beyond this point
		Runner runner = new Runner();
		runner.shouldFlushOutputDirectory = false;
		runner.testFiles = new String[] {
			"my.mod/module-info.java",
			"module my.mod {\n" +
			"	requires dont.use;\n" +
			"}\n"
		};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in my.mod\\module-info.java (at line 2)\n" +
			"	requires dont.use;\n" +
			"	         ^^^^^^^^\n" +
			"The module dont.use has been deprecated since version 9 and marked for removal\n" +
			"----------\n";
		runner.runWarningTest();
	}
	public void testBug534304_1() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK13) {
			return;
		}
		runNegativeTest(
			new String[] {
				"p1/C1.java",
				"package p1;\n" +
				"\n" +
				"import pdep.Dep1;\n" +
				"\n" +
				"public class C1 {\n" +
				"	Dep1 f;\n" +
				"}\n",
				"pdep/Dep1.java",
				"package pdep;\n" +
				"\n" +
				"import pmissing.CMissing;\n" +
				"\n" +
				"@Deprecated(since=\"13\")\n" +
				"@CMissing\n" +
				"public class Dep1 {\n" +
				"\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in p1\\C1.java (at line 6)\n" +
			"	Dep1 f;\n" +
			"	^^^^\n" +
			"The type Dep1 is deprecated since version 13\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in pdep\\Dep1.java (at line 3)\n" +
			"	import pmissing.CMissing;\n" +
			"	       ^^^^^^^^\n" +
			"The import pmissing cannot be resolved\n" +
			"----------\n" +
			"2. ERROR in pdep\\Dep1.java (at line 6)\n" +
			"	@CMissing\n" +
			"	 ^^^^^^^^\n" +
			"CMissing cannot be resolved to a type\n" +
			"----------\n");
	}
	public void testBug534304_2() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK13) {
			runNegativeTest(
				new String[] {
					"p1/C1.java",
					"package p1;\n" +
					"\n" +
					"import pdep.Dep1;\n" +
					"\n" +
					"public class C1 {\n" +
					"	Dep1 f;\n" +
					"}\n",
					"pdep/Dep1.java",
					"package pdep;\n" +
					"\n" +
					"import pmissing.CMissing;\n" +
					"\n" +
					"@Deprecated(since=\"13\")\n" +
					"@CMissing\n" +
					"public class Dep1 {\n" +
					"\n" +
					"}\n"
				},
				"----------\n" +
				"----------\n" +
				"1. ERROR in pdep\\Dep1.java (at line 3)\n" +
				"	import pmissing.CMissing;\n" +
				"	       ^^^^^^^^\n" +
				"The import pmissing cannot be resolved\n" +
				"----------\n" +
				"2. ERROR in pdep\\Dep1.java (at line 6)\n" +
				"	@CMissing\n" +
				"	 ^^^^^^^^\n" +
				"CMissing cannot be resolved to a type\n" +
				"----------\n");
		}
	}
	public void testBug542795() throws Exception {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles = new String[] {
			"test/ReaderWarningView.java",
			"package test;\n" +
			"@java.lang.Deprecated\n" +
			"public class ReaderWarningView {}\n",
			"Test.java",
			"public class Test implements test.Screen.Component {}\n",
			"test/Screen.java",
			"package test;\n" +
			"@interface Annot{ Class<?> value(); }\n" +
			"@Annot(test.Screen.Component.class)\n" +
			"@java.lang.Deprecated\n" +
			"public final class Screen {\n" +
			"	@java.lang.Deprecated\n" +
			"	public interface Component extends test.ReaderWarningView.Component {\n" +
			"	}\n" +
			"}\n",
		};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. ERROR in Test.java (at line 1)\n" +
				"	public class Test implements test.Screen.Component {}\n" +
				"	             ^^^^\n" +
				"The hierarchy of the type Test is inconsistent\n" +
				"----------\n" +
				"2. ERROR in Test.java (at line 1)\n" +
				"	public class Test implements test.Screen.Component {}\n" +
				"	                                  ^^^^^^\n" +
				"The type Screen is deprecated\n" +
				"----------\n" +
				"3. ERROR in Test.java (at line 1)\n" +
				"	public class Test implements test.Screen.Component {}\n" +
				"	                                         ^^^^^^^^^\n" +
				"The type Screen.Component is deprecated\n" +
				"----------\n" +
				"----------\n" +
				"1. ERROR in test\\Screen.java (at line 7)\n" +
				"	public interface Component extends test.ReaderWarningView.Component {\n" +
				"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"test.ReaderWarningView.Component cannot be resolved to a type\n" +
				"----------\n";
		runner.runNegativeTest();
	}
	public void testGH1431() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Parent.java",
				"""
				@Deprecated(since = AbstractChild.TEST_CONSTANT) // this now fails
				public class Parent extends AbstractChild {
				    private static final String REF_OK = AbstractChild.TEST_CONSTANT; // this compiles OK
				}
				""",
				"AbstractChild.java",
				"""
				public abstract class AbstractChild implements Constants {
				    // redacted for brevity
				}
				""",
				"Constants.java",
				"""
				public interface Constants {
				    public static final String TEST_CONSTANT = "this is a test";
				}
				"""
			};
		runner.runConformTest();
	}
	public void testGH1412() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"AbstractClass.java",
			"""
			public abstract class AbstractClass<T> {}
			""",
			"AnnotationWithClassValue.java",
			"""
			public @interface AnnotationWithClassValue {
				Class<? extends AbstractClass<?>> value();
			}
			""",
			"ConcreteClass.java",
			"""
			//Adding @Deprecated here fixes the bug
			//@Deprecated
			public class ConcreteClass extends AbstractClass<AnnotatedClass> {}
			""",
			"AnnotatedClass.java",
			"""
			@Deprecated
			@AnnotationWithClassValue(ConcreteClass.class) //Type mismatch: cannot convert from Class<ConcreteClass> to Class<? extends AbstractClass<?>>
			public class AnnotatedClass {}
			"""
		};
		runner.runConformTest();
	}
	public void testJEP211_2() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"p1/C1.java",
				"""
				package p1;
				public class C1 {
					@Deprecated public class CInner {}
					@Deprecated(forRemoval=true) public static int ZERO = 0;
				}
				""",
				"Test.java",
				"""
				import p1.C1.CInner;
				import static p1.C1.ZERO;
				public class Test {
					CInner c;
					int z = ZERO;
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. WARNING in Test.java (at line 4)
					CInner c;
					^^^^^^
				The type C1.CInner is deprecated
				----------
				2. WARNING in Test.java (at line 5)
					int z = ZERO;
					        ^^^^
				The field C1.ZERO has been deprecated and marked for removal
				----------
				""";
		runner.runWarningTest();
	}
	public void testJEP211_3() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"p1/C1.java",
				"""
				package p1;
				public class C1 {
					@Deprecated public static int ZERO = 0;
					@Deprecated public static int nothing() { return 0; };
				}
				""",
				"Test.java",
				"""
				import static p1.C1.*;
				public class Test {
					int z = ZERO;
					int zz = nothing();
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. WARNING in Test.java (at line 3)
					int z = ZERO;
					        ^^^^
				The field C1.ZERO is deprecated
				----------
				2. WARNING in Test.java (at line 4)
					int zz = nothing();
					         ^^^^^^^^^
				The method nothing() from the type C1 is deprecated
				----------
				""";
		runner.runWarningTest();
	}
	public static Class<?> testClass() {
		return Deprecated9Test.class;
	}
}
