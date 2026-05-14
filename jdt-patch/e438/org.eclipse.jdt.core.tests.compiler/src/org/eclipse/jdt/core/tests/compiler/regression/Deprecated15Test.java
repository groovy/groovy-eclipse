/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *								bug 354536 - compiling package-info.java still depends on the order of compilation units
 *								bug 384870 - [compiler] @Deprecated annotation not detected if preceded by other annotation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.Excuse;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Deprecated15Test extends AbstractRegressionTest {
public Deprecated15Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
}
public void test001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.WARNING);
	boolean isJDK9 = this.complianceLevel >= ClassFileConstants.JDK9;
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public class X<T> {\n" +
			"}\n",
			"Y.java",
			"import p.X;\n" +
			"public class Y {\n" +
			"  Zork z;\n" +
			"  void foo() {\n" +
			"    X x;\n" +
			"    X[] xs = { x };\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    p.X x;\n" +
			"    p.X[] xs = { x };\n" +
			"  }\n" +
			"}\n",
		},
		(isJDK9 ? "" :
		"----------\n" +
		"1. WARNING in Y.java (at line 1)\n" +
		"	import p.X;\n" +
		"	       ^^^\n" +
		"The type X<T> is deprecated\n"
		) +
		"----------\n" +
		"2. ERROR in Y.java (at line 3)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. WARNING in Y.java (at line 5)\n" +
		"	X x;\n" +
		"	^\n" +
		"The type X<T> is deprecated\n" +
		"----------\n" +
		"3. WARNING in Y.java (at line 5)\n" +
		"	X x;\n" +
		"	^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"4. WARNING in Y.java (at line 6)\n" +
		"	X[] xs = { x };\n" +
		"	^\n" +
		"The type X<T> is deprecated\n" +
		"----------\n" +
		"5. WARNING in Y.java (at line 6)\n" +
		"	X[] xs = { x };\n" +
		"	^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"6. WARNING in Y.java (at line 9)\n" +
		"	p.X x;\n" +
		"	^^^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"7. WARNING in Y.java (at line 9)\n" +
		"	p.X x;\n" +
		"	  ^\n" +
		"The type X<T> is deprecated\n" +
		"----------\n" +
		"8. WARNING in Y.java (at line 10)\n" +
		"	p.X[] xs = { x };\n" +
		"	^^^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"9. WARNING in Y.java (at line 10)\n" +
		"	p.X[] xs = { x };\n" +
		"	  ^\n" +
		"The type X<T> is deprecated\n" +
		"----------\n",
		null,
		true,
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// guard variant for DeprecatedTest#test015 using an annotation
public void test002() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
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
			"  @Deprecated\n" +
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
		"The type N1.N2 is deprecated\n" +
		"----------\n" +
		"----------\n" +
		"1. INFO in a\\N1.java (at line 4)\n" +
		"	public class N2 {    public void foo() {}    public class N3 {      public void foo() {}    }  }}\n" +
		"	                                 ^^^^^\n" +
		"The enclosing type N1.N2 is deprecated, perhaps this member should be marked as deprecated, too?\n" +
		"----------\n" +
		"2. INFO in a\\N1.java (at line 4)\n" +
		"	public class N2 {    public void foo() {}    public class N3 {      public void foo() {}    }  }}\n" +
		"	                                                          ^^\n" +
		"The enclosing type N1.N2 is deprecated, perhaps this member should be marked as deprecated, too?\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161214
// shows that Member2 is properly tagged as deprecated (use the debugger, since
// we do not report deprecation in the unit where the deprecated type is
// declared anyway)
public void test003() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo() {\n" +
			"    class Local {\n" +
			"      class Member1 {\n" +
			"        void bar() {\n" +
			"          Member2 m2; // Member2 is deprecated\n" +
			"        }\n" +
			"      }\n" +
			"      @Deprecated\n" +
			"      class Member2 {\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191909
public void test004() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"test1/E01.java",
			"package test1;\n" +
			"public class E01 {\n" +
			"	@Deprecated\n" +
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
		"The field E01.x is deprecated\n" +
		"----------\n" +
		"2. ERROR in test1\\E02.java (at line 5)\n" +
		"	System.out.println(E01.y);\n" +
		"	                       ^\n" +
		"The field E01.y is deprecated\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 354536 - compiling package-info.java still depends on the order of compilation units
public void test005() {
	Runner runner = new Runner();
	runner.customOptions = new HashMap();
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			"p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"    public static class Inner {" +
			"        public void foo() {}\n" +
			"    }\n" +
			"}\n",
			"p1/package-info.java",
			"@java.lang.Deprecated\n" +
			"package p1;\n",
			"p2/C.java",
			"package p2;\n" +
			"public class C {\n" +
			"    void bar(p1.X.Inner a) {\n" +
			"        a.foo();\n" +
			"    }\n" +
			"}\n"
		};
	runner.runConformTest();
}
// https://bugs.eclipse.org/384870 - [compiler] @Deprecated annotation not detected if preceded by other annotation
public void test006() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
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
			"@SuppressWarnings(\"all\") @Deprecated\n" +
			"public class E01 {\n" +
			"	public static int x = 5;\n" +
			"}"
		},
		null, customOptions,
		"----------\n" +
		"1. ERROR in test1\\E02.java (at line 3)\n" +
		"	public void foo(E01 arg) {\n" +
		"	                ^^^\n" +
		"The type E01 is deprecated\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void testGH4562() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"c/OldClass.java",
		"""
		package c;
		@Deprecated
		public class OldClass {
		//  @Deprecated
		    public void foo() {
		    }
		//  @Deprecated
		    public void bar() {
		    }
		}
		""",
		"c/ExtendsOldClass.java",
		"""
		package c;
		public class ExtendsOldClass extends OldClass {
			@Override
			public void foo() {
				super.foo();
			}
			public void callingFoo() {
				super.foo();
			}
			public void callingBar() {
				bar();
			}
		}
		""",
		"c/UseOldClass.java",
		"""
		package c;
		public class UseOldClass {
			public void callingFoo() {
				new ExtendsOldClass().foo();
				new OldClass().foo();
			}
			public void callingBar() {
				new ExtendsOldClass().bar();
				new OldClass().bar();
			}
		}
		"""
	};
	runner.expectedCompilerLog =
		"""
		----------
		1. INFO in c\\OldClass.java (at line 5)
			public void foo() {
			            ^^^^^
		The enclosing type OldClass is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		2. INFO in c\\OldClass.java (at line 8)
			public void bar() {
			            ^^^^^
		The enclosing type OldClass is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		----------
		1. WARNING in c\\ExtendsOldClass.java (at line 2)
			public class ExtendsOldClass extends OldClass {
			                                     ^^^^^^^^
		The type OldClass is deprecated
		----------
		----------
		1. WARNING in c\\UseOldClass.java (at line 5)
			new OldClass().foo();
			    ^^^^^^^^
		The type OldClass is deprecated
		----------
		2. WARNING in c\\UseOldClass.java (at line 9)
			new OldClass().bar();
			    ^^^^^^^^
		The type OldClass is deprecated
		----------
		""";
	runner.runWarningTest();
}
public void testDeprecatedReferenceNestedInDeprecated() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	runner.testFiles = new String[] {
		"Old.java",
		"""
		public class Old {
			@Deprecated void old() {}
		}
		""",
		"X.java",
		"""
		public class X {
			/** @deprecated */
			void test1(final Old old) {
				Runnable r = new Runnable() {
					public void run() {
						old.old();
					}
				};
			}
			/** @deprecated */
			void test2(final Old old) {
				Runnable r = () -> {
					old.old();
				};
			}
			@Deprecated
			class Inner {
				void test3(final Old old) {
					Runnable r = () -> {
						Runnable r2 = new Runnable() {
							public void run() {
								old.old();
							}
						};
					};
				}
			}
		}
		"""};
}
public void testGH4563_cu() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		class A {
			@Deprecated void m() {}
			@Deprecated public String f;
			@Deprecated class Inner {}
		}
		public class X {
			void test(A a) {
				a.m();
				a.f = "";
				A.Inner o1 = a.new Inner();
				Runnable r = () -> {
					a.m();
					a.f = "";
					A.Inner o2 = a.new Inner();
				};
			}
		}
		"""
	};
	runner.expectedCompilerLog =
		"""
		----------
		1. WARNING in X.java (at line 8)
			a.m();
			  ^
		The method m() from the type A is deprecated
		----------
		2. WARNING in X.java (at line 9)
			a.f = "";
			  ^
		The field A.f is deprecated
		----------
		3. WARNING in X.java (at line 10)
			A.Inner o1 = a.new Inner();
			  ^^^^^
		The type A.Inner is deprecated
		----------
		4. WARNING in X.java (at line 10)
			A.Inner o1 = a.new Inner();
			                   ^^^^^
		The type A.Inner is deprecated
		----------
		5. WARNING in X.java (at line 12)
			a.m();
			  ^
		The method m() from the type A is deprecated
		----------
		6. WARNING in X.java (at line 13)
			a.f = "";
			  ^
		The field A.f is deprecated
		----------
		7. WARNING in X.java (at line 14)
			A.Inner o2 = a.new Inner();
			  ^^^^^
		The type A.Inner is deprecated
		----------
		8. WARNING in X.java (at line 14)
			A.Inner o2 = a.new Inner();
			                   ^^^^^
		The type A.Inner is deprecated
		----------
		""";
	runner.runWarningTest();
}
public void testGH4563_class() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		public class X {
			class A {
				@Deprecated void m() {}
				@Deprecated public String f;
				@Deprecated class Inner {}
			}
			class B {
				void test(A a) {
					a.m();
					a.f = "";
					A.Inner o1 = a. new Inner();
					Runnable r = () -> {
						a.m();
						a.f = "";
						A.Inner o2 = a.new Inner();
					};
				}
			}
		}
		"""
	};
	runner.runConformTest();
}
public void testMissingDeprecation() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		public @Deprecated class X {
			public String f;
			public void m() {}
			public class Inner {}
		}
		@Deprecated class Y {
			public Y() {}
		}
		@Deprecated @interface Ann {
			String value();
		}
		"""
	};
	runner.expectedCompilerLog =
		"""
		----------
		1. INFO in X.java (at line 2)
			public String f;
			              ^
		The enclosing type X is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		2. INFO in X.java (at line 3)
			public void m() {}
			            ^^^
		The enclosing type X is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		3. INFO in X.java (at line 4)
			public class Inner {}
			             ^^^^^
		The enclosing type X is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		4. INFO in X.java (at line 7)
			public Y() {}
			       ^^^
		The enclosing type Y is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		""";
	runner.javacTestOptions = Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void testMissingDeprecation_error() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_MEMBER_OF_DEPRECATED_TYPE, JavaCore.ERROR);
	runner.testFiles = new String[] {
		"X.java",
		"""
		public @Deprecated class X {
			public String f;
			public void m() {}
			public class Inner {}
		}
		@Deprecated class Y {
			public Y() {}
		}
		"""
	};
	runner.expectedCompilerLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public String f;
			              ^
		The enclosing type X is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		2. ERROR in X.java (at line 3)
			public void m() {}
			            ^^^
		The enclosing type X is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		3. ERROR in X.java (at line 4)
			public class Inner {}
			             ^^^^^
		The enclosing type X is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		4. ERROR in X.java (at line 7)
			public Y() {}
			       ^^^
		The enclosing type Y is deprecated, perhaps this member should be marked as deprecated, too?
		----------
		""";
	runner.javacTestOptions = Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testMissingDeprecation_ignore() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_MEMBER_OF_DEPRECATED_TYPE, JavaCore.IGNORE);
	runner.testFiles = new String[] {
		"X.java",
		"""
		public @Deprecated class X {
			public String f;
			public void m() {}
			public class Inner {}
		}
		@Deprecated class Y {
			public Y() {}
		}
		"""
	};
	runner.runConformTest();
}
public void testAnnotationElement() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X.java",
			"""
			@interface A1 {
				@Deprecated int value() default 0;
			}
			@interface A2 {
				@Deprecated int oldVal() default 0;
				int newVal() default 0;
			}
			public class X {
				@A1
				public String f1;
				@A1(1)
				public void m1() {}
				@A1(value=2)
				public class Inner1 {}
				@A2
				public String f2;
				@A2(oldVal=2)
				public void m2() {}
				@A2(newVal=4)
				public class Inner2 {}
			}
			"""
		};
	runner.expectedCompilerLog =
		"""
		----------
		1. WARNING in X.java (at line 11)
			@A1(1)
			    ^
		The method value() from the type A1 is deprecated
		----------
		2. WARNING in X.java (at line 13)
			@A1(value=2)
			    ^^^^^
		The method value() from the type A1 is deprecated
		----------
		3. WARNING in X.java (at line 17)
			@A2(oldVal=2)
			    ^^^^^^
		The method oldVal() from the type A2 is deprecated
		----------
		""";
	runner.runWarningTest();
}
public void testAnnotationElement_repeatable() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X.java",
			"""
			@interface TC1 {
				@Deprecated public T1[] value();
			}
			@java.lang.annotation.Repeatable(TC1.class)
			@interface T1 {
				public int value() default -1;
			}
			@interface TC2 {
				public T2[] value();
			}
			@java.lang.annotation.Repeatable(TC2.class)
			@interface T2 {
				@Deprecated public int value() default -1;
				public int alt() default 3;
			}
			public class X {
				@T1(1) @T1(value=2) @T1
				String f1;
				@T1 @T1(3) @T1(value=4)
				String f2;
				@T2 @T2(5) @T2(value=6) @T2(alt=7)
				String f3;
			}
			"""
		};
	runner.expectedCompilerLog =
		"""
		----------
		1. WARNING in X.java (at line 17)
			@T1(1) @T1(value=2) @T1
			^^^^^^^^^^^^^^^^^^^^^^^
		The method value() from the type TC1 is deprecated
		----------
		2. WARNING in X.java (at line 19)
			@T1 @T1(3) @T1(value=4)
			^^^^^^^^^^^^^^^^^^^^^^^
		The method value() from the type TC1 is deprecated
		----------
		3. WARNING in X.java (at line 21)
			@T2 @T2(5) @T2(value=6) @T2(alt=7)
			        ^
		The method value() from the type T2 is deprecated
		----------
		4. WARNING in X.java (at line 21)
			@T2 @T2(5) @T2(value=6) @T2(alt=7)
			               ^^^^^
		The method value() from the type T2 is deprecated
		----------
		""";
	runner.runWarningTest();
}
public static Class testClass() {
	return Deprecated15Test.class;
}
}
