/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
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
 *     Stephan Herrmann - contributions for
 *								bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
 *								bug 383690 - [compiler] location of error re uninitialized final field should be aligned
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class InitializationTests extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String [] { "testIssue4416" };
}
public InitializationTests(String name) {
		super(name);
}

public static Test suite() {
	Test suite = buildAllCompliancesTestSuite(testClass());
	return suite;
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	public void foo() throws Exception{\n" +
			"		String temp;\n" +
			"		Object temp2= new String(\"test\");\n" +
			"		if(temp2 instanceof String) {\n" +
			"			temp = (String) temp2;\n" +
			"		} else {\n" +
			"			if (true) {\n" +
			"				throw new Exception(\"not a string\");\n" +
			"			}\n" +
			"		}\n" +
			"		temp.trim();\n" +
			"	}\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	{\n" +
			"		if (true)\n" +
			"			throw new NullPointerException();\n" +
			"	}\n" +
			"	public X(){}\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020c() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;" +
				"		if (true || (abc = new X(2)).returnA() == 2) {\n" +
				"			System.out.println(\"Hello\");\n" +
				"       } else { \n" +
				"			abc = new X(1);\n" +
				"		}\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	abc = new X(1);\n" +
			"	^^^\n" +
			"The final local variable abc may already have been assigned\n" +
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020d() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public static boolean comparison (X x, int val) {\n" +
				"		return (x.returnA() == val);\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;\n" +
				"		boolean comp = X.comparison((abc = new X(2)), (abc = new X(1)).returnA());\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	boolean comp = X.comparison((abc = new X(2)), (abc = new X(1)).returnA());\n" +
			"	                                               ^^^\n" +
			"The final local variable abc may already have been assigned\n" +
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;\n" +
				"		boolean comp = ((abc = new X(2)).returnA() == 1 || (abc = new X(1)).returnA() == 1);\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	boolean comp = ((abc = new X(2)).returnA() == 1 || (abc = new X(1)).returnA() == 1);\n" +
			"	                                                    ^^^\n" +
			"The final local variable abc may already have been assigned\n" +
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020f() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;\n" +
				"		int val;\n" +
				"		if (true || (abc = new X(1)).returnA() == 1)\n" +
				"			val = (abc = new X(2)).returnA();\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	val = (abc = new X(2)).returnA();\n" +
			"	       ^^^\n" +
			"The final local variable abc may already have been assigned\n" +
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020g() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;\n" +
				"		int val;\n" +
				"		if (true) {\n" +
				"			val = 0;\n" +
				"		} else {\n" +
				"			val = (abc = new X(1)).returnA();\n" +
				"		}\n" +
				"		abc = new X(2);\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" +
			"1. ERROR in X.java (at line 16)\n" +
			"	abc = new X(2);\n" +
			"	^^^\n" +
			"The final local variable abc may already have been assigned\n" +
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020h() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	final static X[] abc;\n" +
				"	static {\n" +
				"		for (Object[] j = new Object[1]; !(((abc = new X[10]).length) == 10); ){\n" +
				"			break;\n" +
				"		}\n" +
				"	}\n" +
				"	//Zork z;\n" +
				"}\n"

			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020i() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	class Inner {\n" +
				"		public int aInner;\n" +
				"		public Inner(int a){\n" +
				"			this.aInner = a;\n" +
				"		}\n" +
				"	}\n" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		int val;" +
				"		final int int1;\n" +
				"		final  int int2;\n" +
				"		val = new X(int1 = 1).new Inner(int2 = int1).aInner;\n" +
				"		System.out.println(int1 + int2);\n" +
				"	}\n" +
				"}\n"

			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020j() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final int abc;\n" +
				"		abc = new X(abc = 2).returnA();\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	abc = new X(abc = 2).returnA();\n" +
			"	^^^\n" +
			"The final local variable abc may already have been assigned\n" +
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020k() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;\n" +
				"	final int x;\n" +
				"	{\n" +
				"		x = new X(x = 2).returnA();" +
				"	}\n" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	x = new X(x = 2).returnA();	}\n" +
			"	^\n" +
			"The final field x may already have been assigned\n" +
			"----------\n",
			null, false, options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=325567
public void test325567() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		bar(3);\n" +
				"	}\n" +
				"	public static void bar(int i) {\n" +
				"		final String before;\n" +
				"		try {\n" +
				"			before = foo();\n" +
				"		} catch (IOException e) {\n" +
				"			// ignore\n" +
				"		}\n" +
				"		B b = new B(new I() {\n" +
				"			public String bar() {\n" +
				"				return new String(before);\n" +
				"			}\n" +
				"		});\n" +
				"		try {\n" +
				"			b.i.bar();\n" +
				"		} catch(Exception e) {\n" +
				"			// ignore\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	private static String foo() throws IOException {\n" +
				"		return null;\n" +
				"	}\n" +
				"	\n" +
				"	static class B {\n" +
				"		I i;\n" +
				"		B(I i) {\n" +
				"			this.i = i;\n" +
				"		}\n" +
				"	}\n" +
				"	static interface I {\n" +
				"		String bar();\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 16)\n" +
			"	return new String(before);\n" +
			"	                  ^^^^^^\n" +
			"The local variable before may not have been initialized\n" +
			"----------\n",
			null, false, options);
}

// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
// definite assignment along all true-yielding paths is sufficient
public void testBug324178b() {
	this.runConformTest(
		new String[] {
			"Bug324178.java",
			"public class Bug324178 {\n" +
			"	 boolean foo(boolean b) {\n" +
			"        boolean v;\n" +
			"        if (b ? false : (true && (v = true)))\n" +
			"            return v;\n" + // OK to read v!
			"        return false;\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.print(new Bug324178().foo(false));\n" +
			"    }\n" +
			"}\n"
		},
		"true");
}

// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
// definite assignment along all true-yielding paths is sufficient
public void testBug324178c() {
	this.runConformTest(
		new String[] {
			"Bug324178.java",
			"public class Bug324178 {\n" +
			"	 boolean foo() {\n" +
			"        boolean r=false;" +
			"        boolean v;\n" +
			"        if ((true && (v = true)) ? true : true && (v = false)) r = v;\n" +
			"        return r;\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.print(new Bug324178().foo());\n" +
			"    }\n" +
			"}\n"
		},
		"true");
}
// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
// must detect that b2 may be uninitialized, no special semantics for Boolean
public void testBug324178d() {
	this.runNegativeTest(
		new String[] {
			"Bug324178.java",
			"public class Bug324178 {\n" +
			"	 boolean foo(boolean b1) {\n" +
			"  		 Boolean b2;\n" +
			"        if (b1 ? (b2 = Boolean.TRUE) : null)\n" +
			"          return b2;\n" +
			"        return false;\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.print(new Bug324178().foo(true));\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Bug324178.java (at line 5)\n" +
		"	return b2;\n" +
		"	       ^^\n" +
		"The local variable b2 may not have been initialized\n" +
		"----------\n");
}
// Bug 383690 - [compiler] location of error re uninitialized final field should be aligned
public void testBug383690() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	 final Object o; // report here!\n" +
			"	 final static Object oStatic; // report here!\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	final Object o; // report here!\n" +
		"	             ^\n" +
		"The blank final field o may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	final static Object oStatic; // report here!\n" +
		"	                    ^^^^^^^\n" +
		"The blank final field oStatic may not have been initialized\n" +
		"----------\n");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4416
// Bogus error: The blank final field o may not have been initialized
public void testIssue4416() {
	runConformTest(new String[] {
			"WrongNotInitialized.java",
			"""
			public class WrongNotInitialized {
			  private final Object o;

			  public WrongNotInitialized(final Object o) {
			    super();
			    this.o = o;
			  }

			  public WrongNotInitialized() {
			    this(new Object());
			    System.out.println(o.toString());
			  }
			}
			"""
	});
}
public void testIssue4416_withPrologue() {
	if (this.complianceLevel < ClassFileConstants.JDK25)
		return; // uses flexible constructor bodies
	runConformTest(new String[] {
			"WrongNotInitialized.java",
			"""
			public class WrongNotInitialized {
			  private final Object o;

			  public WrongNotInitialized(final Object o) {
			    super();
			    this.o = o;
			  }

			  public WrongNotInitialized() {
			    System.out.println();
			    this(new Object());
			    System.out.println(o.toString());
			  }
			}
			"""
	});
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4416
// Bogus error: The blank final field o may not have been initialized
public void testIssue4416b() {
	runConformTest(new String[] {
			"Test.java",
			"""
			public class Test {

				final int x;

				Test(int x) {
					this.x = x;
				}

				Test(Test a) {
					this(a.x);
					//this(1);

					System.out.println(x);
				}
			}
			"""
	});
}
public void testIssue4416b_withPrologue() {
	if (this.complianceLevel < ClassFileConstants.JDK25)
		return; // uses flexible constructor bodies
	runConformTest(new String[] {
			"Test.java",
			"""
			public class Test {

				final int x;

				Test(int x) {
					this.x = x;
				}

				Test(Test a) {
					System.out.println();
					this(a.x);
					//this(1);

					System.out.println(x);
				}
			}
			"""
	});
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4416
// Bogus error: The blank final field o may not have been initialized
public void testIssue4416c() {
	if (this.complianceLevel < ClassFileConstants.JDK16)
		return;
	runConformTest(new String[] {
			"TestRecord.java",
			"""
			public record TestRecord(String a)
			{
			    public TestRecord(int b)
			    {
			        this(String.valueOf(b));
			        System.out.println(a.length());
			    }
			}
			"""
	});
}
public void testIssue4416c_withPrologue() {
	if (this.complianceLevel < ClassFileConstants.JDK25)
		return; // uses flexible constructor bodies
	runConformTest(new String[] {
			"TestRecord.java",
			"""
			public record TestRecord(String a)
			{
			    public TestRecord(int b)
			    {
			        System.out.println();
			        this(String.valueOf(b));
			        System.out.println(a.length());
			    }
			}
			"""
	});
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4416
// Bogus error: The blank final field o may not have been initialized
public void testIssue4416d() {
	runConformTest(new String[] {
			"Test.java",
			"""
			public class Test {

			        private final Object r;

			        Test() {
			                this(Integer.valueOf(1));
			                r.hashCode();
			        }

			        Test(Object r) {
			                this.r = r;
			        }
			}
			"""
	});
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4416
//Bogus error: The blank final field o may not have been initialized
public void testIssue4416d_withPrologue() {
	if (this.complianceLevel < ClassFileConstants.JDK25)
		return; // uses flexible constructor bodies
	runConformTest(new String[] {
			"Test.java",
			"""
			public class Test {

			        private final Object r;

			        Test() {
			        		System.out.println();
			                this(Integer.valueOf(1));
			                r.hashCode();
			        }

			        Test(Object r) {
			                this.r = r;
			        }
			}
			"""
	});
}
public void testGH4865() {
	runConformTest(new String[] {
			"Foo.java",
			"""
			import java.util.Comparator;
			public class Foo {
				private final String greeting;

				public Foo() {
					this.greeting = "Hello";

					Comparator<String> comp = new Comparator<String>() {

						{
							System.out.println(greeting);
						}

						@Override
						public int compare(String o1, String o2) {
							System.out.println(greeting);
							return o1.compareTo(o2);
						}
					};
				}
			}
			"""
	});
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5644
// Missing error: static blank final field read in an anonymous class initializer before it is assigned
public void testIssue5644() {
	runNegativeTest(new String[] {
			"X.java",
			"""
			class S1 {
			    static final int F;
			    static final Object o = new Object() {
			        { System.out.println(F); }      // javac: error
			    };
			    static { F = 1; }
			}
			class S2 {
			    static final int F;
			    static { Object o = new Object() { { int v = F; } }; F = 1; }   // javac: error
			}
			class S3 {
			    static final int F;
			    static final Object o = new Object() { int v = F; };   // javac: error
			    static { F = 1; }
			}
			class S4 {
			    interface I { int get(); }
			    static final int F;
			    static final Object o = new Object() {
			        { I i = () -> F; }              // javac: error
			    };
			    static { F = 1; }
			}
			class S5 {
			    interface I { Object get(); }
			    static final int F;
			    static final I i = () -> new Object() { int v = F; };   // javac: error
			    static { F = 1; }
			}
			"""
	},
	"----------\n" +
	"1. ERROR in X.java (at line 4)\n" +
	"	{ System.out.println(F); }      // javac: error\n" +
	"	                     ^\n" +
	"The blank final field F may not have been initialized\n" +
	"----------\n" +
	"2. ERROR in X.java (at line 10)\n" +
	"	static { Object o = new Object() { { int v = F; } }; F = 1; }   // javac: error\n" +
	"	                                             ^\n" +
	"The blank final field F may not have been initialized\n" +
	"----------\n" +
	"3. WARNING in X.java (at line 14)\n" +
	"	static final Object o = new Object() { int v = F; };   // javac: error\n" +
	"	                                           ^\n" +
	"The value of the field new Object(){}.v is not used\n" +
	"----------\n" +
	"4. ERROR in X.java (at line 14)\n" +
	"	static final Object o = new Object() { int v = F; };   // javac: error\n" +
	"	                                               ^\n" +
	"The blank final field F may not have been initialized\n" +
	"----------\n" +
	"5. ERROR in X.java (at line 21)\n" +
	"	{ I i = () -> F; }              // javac: error\n" +
	"	              ^\n" +
	"The blank final field F may not have been initialized\n" +
	"----------\n" +
	"6. WARNING in X.java (at line 28)\n" +
	"	static final I i = () -> new Object() { int v = F; };   // javac: error\n" +
	"	                                            ^\n" +
	"The value of the field new Object(){}.v is not used\n" +
	"----------\n" +
	"7. ERROR in X.java (at line 28)\n" +
	"	static final I i = () -> new Object() { int v = F; };   // javac: error\n" +
	"	                                                ^\n" +
	"The blank final field F may not have been initialized\n" +
	"----------\n");
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5593
// Bogus error: blank final field may not have been initialized for Outer.this.field in a lambda
public void testIssue5593() {
	runConformTest(new String[] {
			"X.java",
			"""
			class P { int x; }
			public class X {
				final String store;
				final P q;
				final Runnable r = () -> {
					System.out.print(X.this.store);
					X.this.q.x = 1;
					X.this.q.x += 1;
					X.this.q.x++;
				};
				final Object o = new Object() {
					String s = X.this.store;
					{ System.out.print(this.s + " " + X.this.store + " "); }
				};
				X() {
					System.out.print((X.this).store + " ");
					X.this.q.x = 1;
					this.store = "set";
					this.q = new P();
				}
			}
			"""
		});
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5593
// this.field, (this).field and the simple name of a blank final field are still checked
public void testIssue5593_negative() {
	runNegativeTest(new String[] {
			"X.java",
			"""
			class P { int x; }
			public class X {
				private final int a;
				private final int b;
				private final P p;
				private final P q;
				final Runnable r = () -> System.out.print(this.a);
				X(int c) {
					System.out.print((this).b);
					this.p.x += 1;
					q.x++;
					this.a = c;
					this.b = c;
					this.p = new P();
					this.q = new P();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 7)
			final Runnable r = () -> System.out.print(this.a);
			                                               ^
		The blank final field a may not have been initialized
		----------
		2. ERROR in X.java (at line 9)
			System.out.print((this).b);
			                        ^
		The blank final field b may not have been initialized
		----------
		3. ERROR in X.java (at line 10)
			this.p.x += 1;
			     ^
		The blank final field p may not have been initialized
		----------
		4. ERROR in X.java (at line 11)
			q.x++;
			^
		The blank final field q may not have been initialized
		----------
		""");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5593
// a compound assignment through X.this or X only reports the final field
public void testIssue5593_cannotAssign() {
	runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				private final int count;
				static final int F;
				final Runnable r = () -> X.this.count++;
				static {
					X.F += 1;
				}
				X(int c) {
					X.this.count += 1;
					X.this.count++;
					this.count = c;
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 4)
			final Runnable r = () -> X.this.count++;
			                                ^^^^^
		The final field X.count cannot be assigned
		----------
		2. ERROR in X.java (at line 6)
			X.F += 1;
			  ^
		The final field X.F cannot be assigned
		----------
		3. ERROR in X.java (at line 9)
			X.this.count += 1;
			       ^^^^^
		The final field X.count cannot be assigned
		----------
		4. ERROR in X.java (at line 10)
			X.this.count++;
			       ^^^^^
		The final field X.count cannot be assigned
		----------
		""");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5593
// a static field read through its type name (X.Q.x = 1) is not checked either
public void testIssue5593_static() {
	runConformTest(new String[] {
			"X.java",
			"""
			class P { int x; }
			public class X {
				static final P Q;
				static {
					X.Q.x = 1;
					X.Q.x += 1;
					X.Q.x++;
					Q = new P();
				}
			}
			"""
		});
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5593
// the simple name of a static blank final field is still checked
public void testIssue5593_static_negative() {
	runNegativeTest(new String[] {
			"X.java",
			"""
			class P { int x; }
			public class X {
				static final P Q1;
				static final P Q2;
				static final P Q3;
				static {
					Q1.x = 1;
					Q2.x += 1;
					Q3.x++;
					Q1 = new P();
					Q2 = new P();
					Q3 = new P();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 7)
			Q1.x = 1;
			^^
		The blank final field Q1 may not have been initialized
		----------
		2. ERROR in X.java (at line 8)
			Q2.x += 1;
			^^
		The blank final field Q2 may not have been initialized
		----------
		3. ERROR in X.java (at line 9)
			Q3.x++;
			^^
		The blank final field Q3 may not have been initialized
		----------
		""");
}
public static Class testClass() {
	return InitializationTests.class;
}
}
