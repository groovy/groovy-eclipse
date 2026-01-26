/*******************************************************************************
 * Copyright (c) 2024, 2025 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class SuperAfterStatementsTest extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testComplexNesting_OK" };
//		TESTS_NAMES = new String[] { "test037" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return SuperAfterStatementsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_25);
	}
	public SuperAfterStatementsTest(String testName) {
		super(testName);
	}

	// ========= OPT-IN to run.javac mode: ===========
	@Override
	protected void setUp() throws Exception {
		this.runJavacOptIn = true;
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.runJavacOptIn = false; // do it last, so super can still clean up
	}
	// =================================================

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_25);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_25);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_25);
		return defaultOptions;
	}

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(false);
	}
	protected String[] getDefaultClassPaths() {
		String[] libs = DefaultJavaRuntimeEnvironment.getDefaultClassPaths();
		if (this.extraLibPath != null) {
			String[] l = new String[libs.length + 1];
			System.arraycopy(libs, 0, l, 0, libs.length);
			l[libs.length] = this.extraLibPath;
			return l;
		}
		return libs;
	}
	@Override
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		INameEnvironment[] classLibs = getClassLibs(false, options);
		for (INameEnvironment nameEnvironment : classLibs) {
			((FileSystem) nameEnvironment).scanForModules(createParser());
		}
		return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE22Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[0], JavacTestOptions.DEFAULT);
	}
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Map<String, String> customOptions = getCompilerOptions(true);
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}
	class Runner extends AbstractRegressionTest.Runner {
		public Runner() {
			super();
			this.customOptions = getCompilerOptions();
		}
	}
	public void test001() {
		runNegativeTest(new String[] {
			"X.java",
				"""
				class Y {
					public int v;
					Y(int v) {
						this.v = v;
					}
				}
				public class X extends Y {

				    public X(int value) {
				        if (value <= 0)
				            throw new IllegalArgumentException("non-positive value");
				        super(value);
				    }
				    public static void main(String[] args) {
						System.out.println(new X(100).v);
						Zork();
					}
				}
      			"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 16)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void test002() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y {
						public int v;
						Y(int v) {
							this.v = v;
						}
					}
					@SuppressWarnings("preview")
					public class X extends Y {

					    public X(int value) {
					        if (value <= 0)
					            throw new IllegalArgumentException("non-positive value");
					        super(value);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.v);
						}
					}
				"""
			},
			"100");
	}
	public void test003() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y {
						public int v;
						Y(int v) {
							this.v = v;
						}
					}
					@SuppressWarnings("preview")
					public class X extends Y {

					    public X(int value) {
					        if (value <= 0)
					            throw new IllegalArgumentException("non-positive value");
					        super(value);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.v);
						}
					}
				"""
			},
			"100");
	}
	public void test004() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y {
						public int[] vArr;
						Y(int[] vArr) {
							this.vArr = new int[vArr.length];
							for (int i = 0, l = vArr.length; i < l; ++i) {
								this.vArr[i] = vArr[i];
							}
						}
					}
					public class X extends Y {

						public X(int v) {
					        var iVal = Integer.valueOf(v);
					        if (iVal == 0)
					            throw new IllegalArgumentException("0 not allowed");
					        final int[] vArray = switch (iVal) {
					            case 1 -> new int[] { 1, 2, 3, 4};
					            case 2 -> new int[] { 2,3,4};
					            default -> new int[] {100, 200};
					        };
					        super(vArray);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.vArr[0]);
							X x2 = new X(1);
							System.out.println(x2.vArr[0]);
						}
					}
				"""
			},
			"100\n" +
			"1");
	}
	public void test005() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y {
						public int[] vArr;
						private F f1;
						private F f2;

						Y(F f1, F f2) {
							this.f1 = f1;
							this.f2 = f2;
						}
					}
					class F {}
					public class X extends Y {
						public int i;
						public X(int i) {
					        var f = new F();
					        super(f, f);
					        this.i = i;
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
							X x2 = new X(1);
							System.out.println(x2.i);
						}
					}
				"""
			},
			"100\n" +
			"1");
	}
	// any unqualified this expression is disallowed in a pre-construction context:
	public void test006() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					class A {
					    int i;
					    A() {
					        this.i++;                   // Error
					        this.hashCode();            // Error
					        System.out.print(this);     // Error
					        super();
					    }
					}
				"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	this.i++;                   // Error\n" +
				"	^^^^^^\n" +
				"Cannot read field i in an early construction context\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	this.hashCode();            // Error\n" +
				"	^^^^\n" +
				"Cannot use 'this' in an early construction context\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	System.out.print(this);     // Error\n" +
				"	                 ^^^^\n" +
				"Cannot use 'this' in an early construction context\n" +
				"----------\n");
	}
	// any field access, method invocation, or method reference
	// qualified by super is disallowed in a pre-construction context:
	public void test007() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class D {
					    int i;
					}
					class E extends D {
					    E() {
					        super.i++;                  // Error
					        super();
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	super.i++;                  // Error\n" +
			"	^^^^^\n" +
			"Cannot use 'super' in an early construction context (except in a simple field assignment)\n" +
			"----------\n"
		);
	}
	public void test007b() {
		// not a problem in outer early construction context
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					class D {
						int i;
					}
					public class X {
						X() {
							class E extends D {
								E() {
									super.i++;
								}
							}
							System.out.print(new E().i);
							super();
						}
						public static void main(String... args) {
							new X();
						}
					}
				"""
			};
		runner.expectedOutputString = "1";
		runner.runConformTest();
	}
	public void test007c() {
		// but no access to outer this from local class
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					class X {
						X() {
							class E {
							    void m() {
							        System.out.print(X.this);
							    }
							}
							super();
							new E();
						}
					}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in X.java (at line 5)
					System.out.print(X.this);
					                 ^^^^^^
				Cannot use 'X.this' in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}
	public void test007d() {
		// early construction context of far outer, while inners happily use 'this'
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					public class X {
						X() {
							class E {
								E() {
									this.i = new Inner();
									this.i.j++;
								}
								class Inner {
									int j = 3;;
									void m() {
										E.this.i = this;
									}
								}
								Inner i;
							}
							super();
							System.out.print(new E().i.j);
						}
						public static void main(String... args) {
							new X();
						}
					}
				"""
			};
		runner.expectedOutputString = "4";
		runner.runConformTest();
	}
	// an illegal access does not need to contain a this or super keyword:
	public void test008() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class A {
					    int i;
					    A() {
					        i++;                        // Error
					        hashCode();                 // Error
					        super();
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	i++;                        // Error\n" +
			"	^\n" +
			"Cannot read field i in an early construction context\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	hashCode();                 // Error\n" +
			"	^^^^^^^^^^\n" +
			"Cannot invoke method hashCode() in an early construction context\n" +
			"----------\n");
	}
	public void test008_OK() {
		// early construction context of outer
		runConformTest(new String[] {
			"A.java",
				"""
					class A {
						A() {
							class Local {
								int i;
								int getI() { return i; }
								Local() {
									i++;
									System.out.print(getI());
								}
							}
							new Local();
							super();
						}
						public static void main(String... args) {
							new A();
						}
					}
				"""
			},
			"1");
	}
	//an expression involving this does not refer to the current instance but,
	// rather, to the enclosing instance of an inner class:
	public void test009_NOK() {
		runNegativeTest(new String[] {
			"B.java",
				"""
					class B {
					    class C {
					        int c;
					        C() {
					            C.this.c++;             // Error - same instance
					            super();
					        }
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in B.java (at line 5)\n" +
			"	C.this.c++;             // Error - same instance\n" +
			"	^^^^^^^^\n" +
			"Cannot read field c in an early construction context\n" +
			"----------\n");
	}
	public void test009_OK() {
		runConformTest(new String[] {
			"B.java",
				"""
					class B {
					    int b;
					    class C {
					        C() {
					            B.this.b++;             // Allowed - enclosing instance
					            super();
					        }
					    }
					    public static void main(String... args) {
					    	B b = new B();
					    	C c = b.new C();
					    	System.out.print(b.b);
					    }
					}
				"""
			},
			"1");
	}
	/* The invocation hello() that appears in the pre-construction context of the
	 * Inner constructor is allowed because it refers to the enclosing instance of
	 * Inner (which, in this case, has the type Outer), not the instance of Inner
	 * that is being constructed
	 */
	public void test010() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    void hello() {
					        System.out.println("Hello");
					    }
					    class Inner {
					        Inner() {
					            hello();                // Allowed - enclosing instance method
					            super();
					        }
					    }
					    public static void main(String[] args) {
							new X().new Inner();
						}
					}
				"""
			},
			"Hello");
	}
	/* The expression new Inner() is illegal because it requires providing the Inner constructor
	 * with an enclosing instance of Outer, but the instance of Outer that would be provided is
	 * still under construction and therefore inaccessible.
	 */
	public void test011() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Outer {
					    class Inner {}
					    Outer() {
					        new Inner(); // Error - 'this' is enclosing instance
					        super();
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	new Inner(); // Error - \'this\' is enclosing instance\n" +
			"	^^^^^^^^^^^\n" +
			"Cannot instantiate class Outer.Inner in an early construction context of class Outer\n" +
			"----------\n");
	}
	public void test011_inherited() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Super {
					    class Inner {}
					}
					class Outer extends Super {
					    Outer() {
					        new Inner(); // Error - 'this' is enclosing instance
					        super();
					    }
					}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 6)
				new Inner(); // Error - 'this' is enclosing instance
				^^^^^^^^^^^
			Cannot instantiate class Super.Inner in an early construction context of class Outer
			----------
			""");
	}
	public void test011_nested() {
		runConformTest(new String[] {
			"Outer.java",
				"""
					class Outer {
						Outer() {
							class Local {
								class Inner { }
								Local() {
									Object o = new Inner(); // No Error - enclosing Local is not in early construction
									System.out.print(o.getClass().getName());
								}
							};
							Local l = new Local();
							Local.Inner i = l.new Inner();
							super();
						}
						public static void main(String... args) {
							new Outer();
						}
					}
				"""
			},
			"Outer$1Local$Inner");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes cannot have the newly created object as the implicit enclosing
	 * instance
	 */
	public void test012() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class X {
					    class S {}
					    X() {
					        var tmp = new S() { };      // Error
					        super();
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	var tmp = new S() { };      // Error\n" +
			"	          ^^^^^^^^^^^\n" +
			"Cannot instantiate class new X.S(){} in an early construction context of class X\n" +
			"----------\n");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes are allowed if the class S is declared static, or if it were
	 * an interface instead of a class, then it would have no enclosing instance and
	 * hence there would be no compile-time error.
	 */
	public void test013() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    X() {
					        S tmp = new S(){};    // OK
					        super();
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
					class S {}
				"""
			},
			"hello");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes are allowed if the class S is declared static, or if it were
	 * an interface instead of a class, then it would have no enclosing instance and
	 * hence there would be no compile-time error.
	 */
	public void test014() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    static class S {}
					    X() {
					        S tmp = new S() { };    // OK
					        super();
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
				"""
			},
			"hello");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes are allowed if the class S is declared static, or if it were
	 * an interface instead of a class, then it would have no enclosing instance and
	 * hence there would be no compile-time error.
	 */
	public void test015() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    static class S {}
					    X() {
					        var tmp = new S() { };    // OK
					        super();
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
				"""
			},
			"hello");
	}
	/* Here the enclosing instance of the class instance creation expression is not
	 * the newly created U object but, rather, the lexically enclosing O instance.
	 */
	public void test016() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    class S {}
					    class U {
					        U() {
					            var tmp = new S() { };  // Allowed
					            super();
					        }
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
				"""
			},
			"hello");
	}
	/* A return statement may be used in the epilogue of a constructor body
	 * if it does not include an expression (i.e. return; is allowed,
	 * but return e; is not).
	 */
	public void test017() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
							super(i);
					        return;
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"100");
	}
	/* It is a compile-time error if a return statement that includes an expression
	 *  appears in the epilogue of a constructor body.
	 */
	public void test018() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
							super(i);
					        return 0; // Error - return not allowed here
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	return 0; // Error - return not allowed here\n" +
			"	^^^^^^^^^\n" +
			"Void methods cannot return a value\n" +
			"----------\n");
	}
	/* It is a compile-time error if a return statement appears in the prologue of a constructor body.
	 */
	public void test019() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
					        return; // Error - return not allowed here
							super(i);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	return; // Error - return not allowed here\n" +
			"	^^^^^^^\n" +
			"return; statement not allowed in an early construction context\n" +
			"----------\n");
	}
	/* It is a compile-time error if a return statement appears in the prologue of a constructor body.
	 */
	public void test020() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					public class X {
						public int i;
						public X(int i) {
					        return; // Error - return not allowed here
							this(i, 0);
					    }
						public X(int i, int j) {
							this.i = i + j;
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	return; // Error - return not allowed here\n" +
			"	^^^^^^^\n" +
			"return; statement not allowed in an early construction context\n" +
			"----------\n");
	}
	/* Throwing an exception in a prologue of a constructor body is permitted.
	 */
	public void test021() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
							if (i < 0)
								throw new IllegalArgumentException();
							super(i);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"100");
	}
	/* Throwing an exception in a prologue of a constructor body is permitted.
	 */
	public void test022() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
							if (i < 0)
								throw new IllegalArgumentException();
							super(i);
					    }
					    public static void main(String[] args) {
					    	try {
					    		X x = new X(-1);
					    	} catch (IllegalArgumentException e) {
					    		System.out.println("hello");
					    	}
						}
					}
				"""
			},
			"hello");
	}
	/* Unlike in a static context, code in a pre-construction context may refer to the type
	 * of the instance under construction, as long as it does not access the instance itself:
	 */
	public void test023() {
		runConformTest(new String[] {
			"X.java",
				"""
					class B {
						B(Object o) {}
					 }
					class X<T> extends B {
					    X(Z<?> z) {
					        super((T)z.get(0));      // Allowed - refers to 'T' but not 'this'
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
					class Z<T> {
						T get(int i) {
							return null;
						}
					}
				"""
			},
			"hello");
	}
	public void test024() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class A{
					    public int i;
					    A(int i) {
					        this.i = i;
					    }
					}

					public class X{
					    A a = new A(0);
					    public boolean b;
					    X(int i) {
					    	int j = a.i;
					    	this.b = j == 0;
					        super();
					    }
					    public static void main(String[] argv) {
					    	System.out.println(new X(0).b);
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	int j = a.i;\n" +
			"	        ^\n" +
			"Cannot read field a in an early construction context\n" +
			"----------\n");
	}
	/* Its an error of this is used in super(this) - no change for this error
	 */
	public void test025() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class B {
						B(Object o) {}
					 }
					public class X<T> extends B {
				    	X() {
			        		super(this); // Error - refers to 'this'
			   			}
					}
					class Z<T> {
						T get(int i) {
							return null;
						}
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	super(this); // Error - refers to \'this\'\n" +
			"	      ^^^^\n" +
			"Cannot use 'this' in an early construction context\n" +
			"----------\n");
		}
	/**
	 *
	 */
	public void test026() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y {}
					interface I {
					  String tos();
					 }
					public class X {
						int i;
						public X(int i) {
							I tos = super::toString;
							this(i, 0);
					    }
						public X(int i, int j) {
							this.i = i + j;
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	I tos = super::toString;\n" +
			"	        ^^^^^\n" +
			"Cannot use 'super' in an early construction context\n" +
			"----------\n");
	}
	public void test027() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class A{
					    public int i;
					    A(int i) {
					        this.i = i;
					    }
						public int getI() { return this.i; }
					}

					public class X{
					    A a = new A(0);
					    public boolean b;
					    X(int i) {
					    	int j = a.getI();
					    	this.b = j == 0;
					        super();
					    }
					    public static void main(String[] argv) {
					    	System.out.println(new X(0).b);
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	int j = a.getI();\n" +
			"	        ^\n" +
			"Cannot read field a in an early construction context\n" +
			"----------\n");
	}
	public void test028() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					interface I {
					    default int getI() { return 0; }
					}
					interface J extends I {}
					public class X implements J {
						int i;
					    X() {
					        int j = J.super.getI();
					        super();
					    }
					    X(int i) {
					    	this.i = i;
					    }
					    public static void main(String argv[]) {
					    	System.out.println(new X(0).getI() == 0);
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	int j = J.super.getI();\n" +
			"	        ^^^^^^^\n" +
			"Cannot use 'J.super' in an early construction context\n" +
			"----------\n");
	}
	public void test029() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					interface I {
					    default int getI() { return 0; }
					}
					interface J extends I {}
					public class X implements J {
						int i;
					    X() {
					        int j = J.super.getI();
					        this(j);
					    }
					    X(int i) {
					    	this.i = i;
					    }
					    public static void main(String argv[]) {
					    	System.out.println(new X(0).getI() == 0);
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	int j = J.super.getI();\n" +
			"	        ^^^^^^^\n" +
			"Cannot use 'J.super' in an early construction context\n" +
			"----------\n");
	}
	public void test030() {
		runConformTest(new String[] {
			"X.java",
				"""
					public class X {
					    int v = 0;
					    public boolean foo() {
					        class Inner {
					            int getV() {
					                return v;
					            }
					        }
					        return new Inner(){}.getV() == v;
					    }
					    public static void main(String args[]) {
					    	System.out.println(new X().foo());
					    }
					}
				"""
			},
			"true");
	}
	public void test032() {
		runConformTest(new String[] {
			"X.java",
				"""
				abstract class Y {
					public abstract int getI();
				}
				public class X {
					public int i;
				    X() {
				         new Y() {
				            public int getI() {
				                return 0;
				            }
				        }.getI();
				        super();
				    }
				   public static void main(String argv[]) {
					   System.out.println(new X().i);
				    }
				}
			"""
			},
			"0"
		);
	}
	public void test033() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y {
						public Y() {}
						public Y(int i){}
					}
					class X extends Y {
						public boolean b;;
						public X(int i){}
					    	public X (boolean b) {
					          super();
					          this(0);
					          this.b = b;
					    }
					    public static void main(String argv[]) {
					        System.out.println(new X(true).b);
					    }
					}
				"""
			},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	this(0);\n" +
		"	^^^^^^^^\n" +
		"Constructor cannot have more than one explicit constructor call\n" +
		"----------\n"
			);
	}
	public void test034() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y {
						public Y(int i){}
					}
					class X extends Y {
						public boolean b;;
						public X(int i){}
					    	public X (boolean b) {
					          super();
					          this(0);
					          this.b = b;
					    }
					    public static void main(String argv[]) {
					        System.out.println(new X(true).b);
					    }
					}
				"""
			},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	public X(int i){}\n" +
		"	       ^^^^^^^^\n" +
		"Implicit super constructor Y() is undefined. Must explicitly invoke another constructor\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	super();\n" +
		"	^^^^^^^^\n" +
		"The constructor Y() is undefined\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	this(0);\n" +
		"	^^^^^^^^\n" +
		"Constructor cannot have more than one explicit constructor call\n" +
		"----------\n"
			);
	}
	public void test035() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y {
						public Y(int i){}
					}
					class X extends Y {
						public boolean b;;
						public X(int i){ super(i);}
					    	public X (boolean b) {
					          super();
					          this(0);
					          this.b = b;
					    }
					    public static void main(String argv[]) {
					        System.out.println(new X(true).b);
					    }
					}
				"""
			},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	super();\n" +
		"	^^^^^^^^\n" +
		"The constructor Y() is undefined\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	this(0);\n" +
		"	^^^^^^^^\n" +
		"Constructor cannot have more than one explicit constructor call\n" +
		"----------\n"
			);
	}
	public void test036() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y { }
					class X extends Y {
					        public boolean b;;
					        public X(){}
					        public X (boolean b) {
					          super();
					          this();
					          this.b = b;
					    }
					    public static void main(String argv[]) {
					        System.out.println(new X(true).b);
					    }
					}
				"""
			},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	this();\n" +
		"	^^^^^^^\n" +
		"Constructor cannot have more than one explicit constructor call\n" +
		"----------\n"
			);
	}
	// regression test for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2373
	public void test037() {
		runConformTest(new String[] {
			"X.java",
				"""
					interface I {
					    void foo();
					}
					public class X {
					    public static boolean b;
					    static class Y { boolean b = true;}
					    X() {}
					    X(boolean b) {
					        I l = () -> {
					            X.b = new Y() {
					                public boolean b() {
					                    return this.b;
					                }
					            }.b();
					        };
					        l.foo();
					        super();
					    }

					    public static void main(String argv[]) {
					    	new X(true);
					        System.out.println(X.b);
					    }
					}
      			"""
			},
			"true"
		);
	}
	public void test038() {
		runConformTest(new String[] {
			"X.java",
				"""
					interface I {
						void foo();
					}

					public class X {
						public static boolean b;

						static class Y {
							boolean b = true;
						}
						X() {}
						X(boolean b) {
							I l = () -> {
								X.b = new Y() {
									public boolean b() {
										return this.b;
									}
								}.b();
								System.out.println(switch (42) {
								default -> {
									try {
										yield 42;
									} finally {

									}
								}
								});
							};
							l.foo();
							super();
						}

						public static void main(String argv[]) {
							new X(true);
							System.out.println(X.b);
						}
					}
				"""
			},
			"42\n" +
			"true"
		);
	}
	public void test039() {
		runConformTest(new String[] {
			"X.java",
				"""
					interface I {
						void foo();
					}

					public class X {
						public static boolean b;

						static class Y {
							boolean b = true;
						}
						X() {}
						X(boolean b) {
							I l = () -> {
								X.b = new Y() {
									public boolean b() {
										return this.b;
									}
								}.b();
								System.out.println(switch (42) {
								default -> {
									try {
										yield 42;
									} finally {

									}
								}
								});
							};
							l.foo();
							super();
						}

						public static void main(String argv[]) {
							new X(true);
							System.out.println(X.b);
						}
					}
				"""
			},
			"42\n" +
			"true"
		);
	}
	public void test040() {
		Runner runner = new Runner();
		runner.customOptions.put(CompilerOptions.OPTION_Source, "24");
		runner.customOptions.put(CompilerOptions.OPTION_Compliance, "24");
		runner.customOptions.put(CompilerOptions.OPTION_TargetPlatform, "24");
		runner.javacTestOptions = JavacTestOptions.forRelease(JavaCore.VERSION_24);
		runner.testFiles = new String[] {
				"X.java",
					"""
						class Y {
							public int v;
							Y(int v) {
								this.v = v;
							}
						}
						@SuppressWarnings("preview")
						public class X extends Y {
						    public X(int value) {
						        if (value <= 0)
						            throw new IllegalArgumentException("non-positive value");
						        super(value);
						    }
						}
					"""
			};
		runner.expectedCompilerLog =
			"""
			----------
			1. ERROR in X.java (at line 12)
				super(value);
				^^^^^^^^^^^^^
			The Java feature 'Flexible Constructor Bodies' is only available with source level 25 and above
			----------
			""";
		runner.runNegativeTest();
	}
	public void testGH2467() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Test3.java",
				"""
				class Super {}
				public class Test3 extends Super {
					Test3(Test3 other) {
						other.foo(); // bogus error:
						foo(); // error is correct
						super();
					}
					void foo() {}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in Test3.java (at line 5)
					foo(); // error is correct
					^^^^^
				Cannot invoke method foo() in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}
	public void testOuterConstruction_1() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Test.java",
				"""
				public class Test {
					Test() {
						class Local {
							Local() {
								foo(this);
							}
						};
						super();
						new Local();
					}
					void foo(Object r) { System.out.print(r.getClass().getName()); }
					public static void main(String... args) {
						new Test();
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in Test.java (at line 5)
					foo(this);
					^^^^^^^^^
				Cannot invoke method foo() in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}

	public void testOuterConstruction_2() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Test.java",
				"""
				public class Test {
					static class Inner {
						Inner() {
							foo(this);
						}
					};
					Test() {
						new Inner();
						super();
					}
					static void foo(Object r) { System.out.print(r.getClass().getName()); }
					public static void main(String... args) {
						new Test();
					}
				}
				"""
			};
		runner.expectedOutputString = "Test$Inner";
		runner.runConformTest();
	}
	public void testFieldAssignedInSuperArgument_OK() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Test.java",
				"""
				class Super {
					Super (int i) {}
				}
				public class Test extends Super {
					int i;
					Test(int n) {
						super(i=n);				// old syntax, single name reference
					}
					Test(int n, boolean f) {
						super(this.i=n);		// old syntax, this-qualified field reference
					}
					Test(int n, int m) {
						int s = n+m;
						super(i=s);				// new syntax, single name reference
					}
					Test(int n, int m, boolean f) {
						int s = n+m;
						super(this.i=s);		// new syntax, this-qualified field reference
					}
					public static void main(String... args) {
						System.out.print(new Test(3).i);
						System.out.print(new Test(4, true).i);
						System.out.print(new Test(2,3).i);
						System.out.print(new Test(3,3, true).i);
					}
				}
				"""
			};
		runner.expectedOutputString = "3456";
		runner.runConformTest();
	}
	public void testFieldAssignedInSuperArgument_NOK_superclass() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Test.java",
				"""
				class Super {
					int i;
					Super (int i) {}
				}
				public class Test extends Super {
					Test(int n) {
						super(i=n);				// old syntax, single name reference
					}
					Test(int n, boolean f) {
						super(this.i=n);		// old syntax, this-qualified field reference
					}
					Test(int n, int m) {
						int s = n+m;
						super(i=s);				// new syntax, single name reference
					}
					Test(int n, int m, boolean f) {
						int s = n+m;
						super(this.i=s);		// new syntax, this-qualified field reference
					}
					public static void main(String... args) {
						System.out.print(new Test(3).i);
						System.out.print(new Test(4, true).i);
						System.out.print(new Test(2,3).i);
						System.out.print(new Test(3,3, true).i);
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in Test.java (at line 7)
					super(i=n);				// old syntax, single name reference
					      ^
				Cannot assign field 'i' from class 'Super' in an early construction context
				----------
				2. ERROR in Test.java (at line 10)
					super(this.i=n);		// old syntax, this-qualified field reference
					      ^^^^^^
				Cannot assign field 'i' from class 'Super' in an early construction context
				----------
				3. ERROR in Test.java (at line 14)
					super(i=s);				// new syntax, single name reference
					      ^
				Cannot assign field 'i' from class 'Super' in an early construction context
				----------
				4. ERROR in Test.java (at line 18)
					super(this.i=s);		// new syntax, this-qualified field reference
					      ^^^^^^
				Cannot assign field 'i' from class 'Super' in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}
	public void testFieldAssignedInSuperArgument_NOK_hasInitializer() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Test.java",
				"""
				class Super {
					Super (int i) {}
				}
				public class Test extends Super {
					int i = 3;
					Test(int n) {
						super(i=n);				// old syntax, single name reference
					}
					Test(int n, boolean f) {
						super(this.i=n);		// old syntax, this-qualified field reference
					}
					Test(int n, int m) {
						int s = n+m;
						super(i=s);				// new syntax, single name reference
					}
					Test(int n, int m, boolean f) {
						int s = n+m;
						super(this.i=s);		// new syntax, this-qualified field reference
					}
					public static void main(String... args) {
						System.out.print(new Test(3).i);
						System.out.print(new Test(4, true).i);
						System.out.print(new Test(2,3).i);
						System.out.print(new Test(3,3, true).i);
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in Test.java (at line 7)
					super(i=n);				// old syntax, single name reference
					      ^
				Cannot assign field 'i' in an early construction context, because it has an initializer
				----------
				2. ERROR in Test.java (at line 10)
					super(this.i=n);		// old syntax, this-qualified field reference
					      ^^^^^^
				Cannot assign field 'i' in an early construction context, because it has an initializer
				----------
				3. ERROR in Test.java (at line 14)
					super(i=s);				// new syntax, single name reference
					      ^
				Cannot assign field 'i' in an early construction context, because it has an initializer
				----------
				4. ERROR in Test.java (at line 18)
					super(this.i=s);		// new syntax, this-qualified field reference
					      ^^^^^^
				Cannot assign field 'i' in an early construction context, because it has an initializer
				----------
				""";
		runner.runNegativeTest();
	}
	public void testFieldAssignedInSuperArgument_notEnabled() {
		Runner runner = new Runner();
		runner.customOptions.put(CompilerOptions.OPTION_Source, "24");
		runner.customOptions.put(CompilerOptions.OPTION_Compliance, "24");
		runner.customOptions.put(CompilerOptions.OPTION_TargetPlatform, "24");
		runner.javacTestOptions = JavacTestOptions.forRelease(JavaCore.VERSION_24);
		runner.testFiles = new String[] {
				"Test.java",
				"""
				class Super {
					Super (int i) {}
				}
				public class Test extends Super {
					int i;
					Test(int n) {
						super(i=n);				// old syntax, single name reference
					}
					Test(int n, boolean f) {
						super(this.i=n);		// old syntax, this-qualified field reference
					}
					Test(int n, int m) {
						int s = n+m;
						super(i=s);				// new syntax, single name reference
					}
					Test(int n, int m, boolean f) {
						int s = n+m;
						super(this.i=s);		// new syntax, this-qualified field reference
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
			----------
			1. ERROR in Test.java (at line 7)
				super(i=n);				// old syntax, single name reference
				      ^
			Cannot refer to an instance field i while explicitly invoking a constructor
			----------
			2. ERROR in Test.java (at line 10)
				super(this.i=n);		// old syntax, this-qualified field reference
				      ^^^^^^
			The Java feature 'Flexible Constructor Bodies' is only available with source level 25 and above
			----------
			3. ERROR in Test.java (at line 14)
				super(i=s);				// new syntax, single name reference
				^^^^^^^^^^^
			The Java feature 'Flexible Constructor Bodies' is only available with source level 25 and above
			----------
			4. ERROR in Test.java (at line 18)
				super(this.i=s);		// new syntax, this-qualified field reference
				^^^^^^^^^^^^^^^^
			The Java feature 'Flexible Constructor Bodies' is only available with source level 25 and above
			----------
			""";
		runner.runNegativeTest();
	}
	public void testFieldCompoundAssignedInSuperArgument() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Test.java",
				"""
				class Super {
					Super (int i) {}
				}
				public class Test extends Super {
					int i;
					Test(int n) {
						super(i+=n);			// old syntax, single name reference
					}
					Test(int n, boolean f) {
						super(this.i-=n);		// old syntax, this-qualified field reference
					}
					Test(int n, int m) {
						int s = n+m;
						super(i*=s);			// new syntax, single name reference
					}
					Test(int n, int m, boolean f) {
						int s = n+m;
						super(this.i/=s);		// new syntax, this-qualified field reference
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in Test.java (at line 7)
					super(i+=n);			// old syntax, single name reference
					      ^
				Cannot read field i in an early construction context
				----------
				2. ERROR in Test.java (at line 10)
					super(this.i-=n);		// old syntax, this-qualified field reference
					      ^^^^^^
				Cannot read field i in an early construction context
				----------
				3. ERROR in Test.java (at line 14)
					super(i*=s);			// new syntax, single name reference
					      ^
				Cannot read field i in an early construction context
				----------
				4. ERROR in Test.java (at line 18)
					super(this.i/=s);		// new syntax, this-qualified field reference
					      ^^^^^^
				Cannot read field i in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}
	public void testFieldReadInSuperArgument() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Test.java",
				"""
				class Super {
					Super (int i) {}
				}
				public class Test extends Super {
					int i;
					Test() {
						super(i);
					}
					Test(boolean f) {
						System.out.print(f);
						super(i);
					}
					Test(String s) {
						super(this.i);
					}
					Test(String s, boolean f) {
						System.out.print(f);
						super(this.i);
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in Test.java (at line 7)
					super(i);
					      ^
				Cannot read field i in an early construction context
				----------
				2. ERROR in Test.java (at line 11)
					super(i);
					      ^
				Cannot read field i in an early construction context
				----------
				3. ERROR in Test.java (at line 14)
					super(this.i);
					      ^^^^^^
				Cannot read field i in an early construction context
				----------
				4. ERROR in Test.java (at line 18)
					super(this.i);
					      ^^^^^^
				Cannot read field i in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}
	public void testBug564263() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"Test.java",
			"""
			import java.util.function.Supplier;

			public class Test {
				private String message;

				public class LambaError {
					public LambaError(Supplier<String> message) {
						System.out.print(message.get());
					}
				}

				public class Broken extends LambaError {
					public Broken(String message) {
						super(() -> Test.this.message);
					}
				}
				void test() {
					this.message = "OK";
					new Broken("NOK");
				}
				public static void main(String... args) {
					new Test().test();
				}
			}
			"""};
		runner.expectedOutputString = "OK";
		runner.runConformTest();
	}
	public void testComplexNesting_OK() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"C1.java",
			"""
			public class C1 {
				String f1 = "f1";
			    C1() {
			        super();
			        class C2 { // not early for C1
			            C2() {
			                class C3 { // early for C2
			                	String f3 = "f3";
			                    C3() {
			                        super();
			                        class C4 { // not early for C3
			                        	C4() {
			                            	System.out.print(f3);
			                            	System.out.print(f1);
		                            	}
			                        }
			                        new C4();
			                    }
			                }
			                super();
			                new C3();
			            }
			        }
			        new C2();
			    }
		        public static void main(String... args) {
		        	new C1();
		        }
			}
			"""};
		runner.expectedOutputString = "f3f1";
		runner.runConformTest();
	}
	public void testComplexNesting_NOK() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"C1.java",
			"""
			class C1 {
			    C1() {
			        super();
			        class C2 { // not early for C1
			        	String f2 = "f2";
			            C2() {
			                class C3 { // early for C2
			                    C3() {
			                        super();
			                        class C4 { // not early for C3
			                        	C4() {
			                        		System.out.print(f2);
		                            	}
			                        }
			                        new C4();
			                    }
			                }
			                super();
			                new C3();
			            }
			        }
			        new C2();
			    }
			}
			"""};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in C1.java (at line 12)
					System.out.print(f2);
					                 ^^
				Cannot read field f2 in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}

	public void testDuplicateCalls() {
		// but no access to outer this from local class
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					class X {
						X() {
							System.out.println();
							super();
							super(); // illegal
						}
					}
				"""
			};
		runner.expectedCompilerLog = "----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	super(); // illegal\n" +
				"	^^^^^^^^\n" +
				"Constructor cannot have more than one explicit constructor call\n" +
				"----------\n";
		runner.runNegativeTest();
	}

	public void testGH2464() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Test.java",
				"""
				public class Test {
					String name = "Test";
					Test() {
						Runnable r = new Runnable() {
							@Override
							public void run() {
								System.out.println(Test.this);
								System.out.println(name);
							}
						};
						r.run();
						super();
					}
					public static void main(String[] args) {
						new Test();
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in Test.java (at line 7)
					System.out.println(Test.this);
					                   ^^^^^^^^^
				Cannot use 'Test.this' in an early construction context
				----------
				2. ERROR in Test.java (at line 8)
					System.out.println(name);
					                   ^^^^
				Cannot read field name in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}

	public void testGH2468() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"TestFlow.java",
				"""
				public class TestFlow {
					TestFlow() {}
					TestFlow(boolean f) {
						if (f)
							super();
						else
							this();
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in TestFlow.java (at line 5)
					super();
					^^^^^^^^
				Constructor call is not allowed here
				----------
				2. ERROR in TestFlow.java (at line 7)
					this();
					^^^^^^^
				Constructor call is not allowed here
				----------
				""";
		runner.runNegativeTest();
	}

	public void testGH666() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"TestFlow.java",
				"""
				public class TestFlow {
					TestFlow() {}
					TestFlow(boolean f) {
						if (f)
							super();
						this();
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in TestFlow.java (at line 5)
					super();
					^^^^^^^^
				Constructor call is not allowed here
				----------
				""";
		runner.runNegativeTest();
	}

	public void testGH3094() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
				public class X {
					class Nested extends X1 {
						X1 xy;
						class DeeplyNested extends NestedInX1 {
							DeeplyNested(float f) {
								Nested.super.x1.super(); // Error here
							}
						}
					}
					public static void main(String... args) {
						Nested nest = new X().new Nested();
						nest.x1 = new X1();
						nest.new DeeplyNested(1.1f);
					}
				}
				class X1 {
					X1 x1;
					class NestedInX1 {}
				}
				"""
			};
		runner.runConformTest();
	}

	public void testGH3094_2() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
				public class X {
					class Nested extends X1 {
						X1 xy;
						class DeeplyNested extends NestedInX1 {
							DeeplyNested(float f) {
								Nested.this.x1.super();
							}
						}
					}
					public static void main(String... args) {
						Nested nest = new X().new Nested();
						nest.x1 = new X1();
						nest.new DeeplyNested(1.1f);
					}
				}
				class X1 {
					X1 x1;
					class NestedInX1 {}
				}
				"""
			};
		runner.runConformTest();
	}

	public void testGH3094_3() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
				public class X {
					class Nested extends X1 {
						X1 xy;
						Nested() {
							class DeeplyNested extends NestedInX1 {
								DeeplyNested(float f) {
									Nested.this.x1.super();
								}
							}
							super();
						}
					}
				}
				class X1 {
					X1 x1;
					class NestedInX1 {}
				}
				"""
			};
		runner.expectedCompilerLog =
			"""
			----------
			1. WARNING in X.java (at line 5)
				class DeeplyNested extends NestedInX1 {
				      ^^^^^^^^^^^^
			The type DeeplyNested is never used locally
			----------
			2. ERROR in X.java (at line 7)
				Nested.this.x1.super();
				^^^^^^^^^^^^^^
			Cannot read field x1 in an early construction context
			----------
			""";
		runner.runNegativeTest();
	}

	public void testGH3132() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
				public class X {
					class Nested {
						Nested(Object o) {}
					}
					class AnotherNested extends Nested {
						AnotherNested() {
							super(new Object() { // Cannot instantiate class new Object(){} in an early construction context of class X.AnotherNested
							});
						}
					}
					public static void main(String... args) {
						new X().new AnotherNested();
					}
				}
				"""
			};
		runner.runConformTest();
	}

	public void testGH3132_2() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
				class O {} // demonstrates the the bug was not specific to j.l.Object
				public class X {
					class Nested extends O {
						Nested(Object o) {}
					}
					class AnotherNested extends Nested {
						AnotherNested() {
							super(new O() {
							});
						}
					}
					public static void main(String... args) {
						new X().new AnotherNested();
					}
				}
				"""
			};
		runner.runConformTest();
	}

	public void testGH3153() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
			  public static void main(String[] argv) {
			    class Inner {
			      Inner() {
			        class Local {}
			        new Local() {}; // Error: No enclosing instance of the type X is accessible in scope
			        super();
			      }
			    }
			    new Inner();
			  }
			}
			""" },
			"");
	}

	public void testLocalAccesToOuter () {
		runConformTest(new String[] {
			"Outer.java",
			"""
			import java.util.function.Supplier;
			@SuppressWarnings("unused")
			class Outer {
				void m() {
					System.out.print("m");
				}
				class Inner {
					Inner() {
						class Foo {
							void g() {
								m();
							}
						}
						super();
						new Foo().g();
					}
				}
				public static void main(String... args) {
					new Outer().new Inner();
				}
			}
			"""
			},
			"m");
	}

	public void testCtorRef_staticContext () {
		runNegativeTest(new String[] {
			"Outer.java",
			"""
			import java.util.function.Supplier;
			class Outer {
				class Inner {
					Inner() {
						class Foo {
							void g() {
								System.out.print("g");
							}
						}
						super();
						class Bar {
							static void p() {
								new Foo().g();
							}
							static void r() {
								Supplier<Foo> sfoo = Foo::new;
								sfoo.get().g();
							}
						};
						Bar.r();
					}
				}
				public static void main(String... args) {
					new Outer().new Inner();
				}
			}
			"""
			},
			"""
			----------
			1. ERROR in Outer.java (at line 13)
				new Foo().g();
				^^^^^^^^^
			Cannot instantiate local class 'Foo' in a static context
			----------
			2. ERROR in Outer.java (at line 16)
				Supplier<Foo> sfoo = Foo::new;
				                     ^^^^^^^^
			Cannot instantiate local class 'Foo' in a static context
			----------
			""");
	}

	public void testCtorRef_nonStatic () {
		runConformTest(new String[] {
			"Outer.java",
			"""
			import java.util.function.Supplier;
			class Outer {
				class Inner {
					Inner() {
						class Foo {
							void g() {
								System.out.print("g");
							}
						}
						super();
						class Bar {
							void p() {
								new Foo().g();
							}
							void r() {
								Supplier<Foo> sfoo = Foo::new;
								sfoo.get().g();
							}
						};
						new Bar().r();
					}
				}
				public static void main(String... args) {
					new Outer().new Inner();
				}
			}
			"""
			},
			"g");
	}

	public void testGH3188() {
		runConformTest(new String[] {
			"EarlyLocalCtorRef.java",
			"""
			import java.util.function.Supplier;
			class EarlyLocalCtorRef {
			    EarlyLocalCtorRef() {
			        class InnerLocal { }
			        this(InnerLocal::new);
			    }
			    EarlyLocalCtorRef(Supplier<Object> s) {
			    }
			    public static void main(String... args0) {
			    	new EarlyLocalCtorRef();
			    }
			}
			"""},
			"");
	}

	public void testGH3194_reopen() {
		runConformTest(new String[] {
			"X.java",
			"""
			sealed interface A permits X {}
			public final  class X implements A {
				int a = 1;
				class One {
					int b = 2;
					class Two {
						int c = 3;
						class Three {
							int r = a + b + c;
						}
					}
				}
				public static void main(String argv[]) {
					X x = new X();
					One.Two.Three ci = x.new One().new Two().new Three(); // No enclosing instance of type X is accessible. Must qualify the allocation...
					System.out.println(ci.r);
				}
			}
			"""
		});
	}

	public void testGH3116() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"X.java",
			"""
			public class X {
				final int final_field;
				int x;
				{ x = final_field; } // Error: The blank final field final_field may not have been initialized
				X() {
					final_field = -1;
					super();
				}
				public static void main(String... args) {
					System.out.print(new X().x);
				}
			}
			"""
		};
		runner.expectedOutputString = "-1";
		runner.runConformTest();
	}

	public void testGH3115() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"X.java",
			"""
			public class X {
				public static void main(String argv[]) {
					X test = new X();
				}
				X() {
					class InnerLocal {}
					java.util.function.IntSupplier foo = () -> {
						new InnerLocal() {}; // This is trouble
						return 0;
					};
					super();
				}
			}
			"""
		};
		runner.runConformTest();
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406614, [1.8][compiler] Missing and incorrect errors for lambda in explicit constructor call.
	// Variant for early construction context
	public void test406614() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
					interface I {
						int doit();
					}
					@SuppressWarnings("preview")
					public class X {
						int f;
						X() {
						}
						X(byte b) {
							I i = () -> this.f;
							this();
						}
						X(short s) {
							I i = () -> this.g();
							this();
						}
						X (int x) {
							I i = () -> f;
						    this();
						}
						X (long x) {
							I i = () -> g();
						    this();
						}
						int g() {
							return 0;
						}
						class Member {
							Member() {}
							Member(byte b) {
								I i = () -> X.this.f;
								this();
							}
							Member(short s) {
								I i = () -> X.this.g();
								this();
							}
							Member(int x) {
								I i = () -> f;
							    this();
							}
							Member(long x) {
								I i = () -> g();
							    this();
							}
						}
					}
					"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	I i = () -> this.f;\n" +
				"	            ^^^^^^\n" +
				"Cannot read field f in an early construction context\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 14)\n" +
				"	I i = () -> this.g();\n" +
				"	            ^^^^\n" +
				"Cannot use \'this\' in an early construction context\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 18)\n" +
				"	I i = () -> f;\n" +
				"	            ^\n" +
				"Cannot read field f in an early construction context\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 22)\n" +
				"	I i = () -> g();\n" +
				"	            ^^^\n" +
				"Cannot invoke method g() in an early construction context\n" +
				"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406614, [1.8][compiler] Missing and incorrect errors for lambda in explicit constructor call.
	// Variant for early construction context - this time a lambda in early construction of a member accesses the outer this - OK.
	public void test406614_member() throws IOException, ClassFormatException {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					interface I {
						int doit();
					}
					@SuppressWarnings("preview")
					public class X {
						int f;
						int g() {
							return 0;
						}
						class Member {
							Member() {}
							Member(byte b) {
								I i = () -> X.this.f;
								this();
							}
							Member(short s) {
								I i = () -> X.this.g();
								this();
							}
							Member(int x) {
								I i = () -> f;
							    this();
							}
							Member(long x) {
								I i = () -> g();
							    this();
							}
						}
						public static void main(String... args) {
							new X().new Member((byte)13);
						}
					}
					"""
				},
				"");
		verifyClassFile("version 25 : 69.0", "X.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testFieldAssignment_OK() throws Exception {
		runConformTest(new String[] {
				"X.java",
				"""
				public class X {
					final String s;
					X(String s0) {
						s = s0;
						super();
					}
					X() {
						s = "";
						super();
					}
					public static void main(String... args) {
						System.out.print(new X("OK").s);
					}
				}
				"""
		},
		"OK");
	}

	public void testFieldAssignmentNotAlways_NOK() throws Exception {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					final String s;
					X(String s0) {
						s = s0;
						super();
					}
					X() {
					}
					public static void main(String... args) {
						System.out.print(new X("OK").s);
					}
				}
				"""
		},
		"""
		----------
		3. ERROR in X.java (at line 7)
			X() {
			^^^
		The blank final field s may not have been initialized
		----------
		""");
	}

	public void testFieldAssignmentInLambda_NOK() throws Exception {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					String s;
					X(String s0) {
						s = s0;
						super();
					}
					X() {
						Runnable r = () -> s = "";
						super();
					}
					public static void main(String... args) {
						System.out.print(new X("OK").s);
					}
				}
				"""
		},
		"""
		----------
		1. ERROR in X.java (at line 8)
			Runnable r = () -> s = "";
			                   ^
		Cannot assign field 's' inside a lambda expression within an early construction context of class X
		----------
		""");
	}

	public void testFieldAssignmentInLocal_NOK() throws Exception {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					String s;
					X(String s0) {
						s = s0;
						super();
					}
					X() {
						Runnable r = new Runnable() {
							public void run() { s = "Anonymous"; };
						};
						class Local {
							{
								s = "Local";
							}
						}
						super();
					}
					public static void main(String... args) {
						System.out.print(new X("OK").s);
					}
				}
				"""
		},
		"""
		----------
		3. ERROR in X.java (at line 9)
			public void run() { s = "Anonymous"; };
			                    ^
		Cannot assign field 's' from class 'X' in an early construction context
		----------
		4. ERROR in X.java (at line 13)
			s = "Local";
			^
		Cannot assign field 's' from class 'X' in an early construction context
		----------
		""");
	}
	// test case from https://bugs.openjdk.org/browse/JDK-8322882
	public void testJDK8322882() throws Exception {
		runNegativeTest(
			new String[] {
				"TestUseTree2.java",
				"""
				 public class TestUseTree2 {
					public static void main(String[] args) {
						String i = "111";
						class TestUseTree2_ROOT {
							// ctor arg i
							void f() {
								System.out.println(i);
							}
						}

						class TestUseTree2_ROOT1 {
							// clinit args: i?
							// should be prohibited to use `new TestUseTree2_ROOT()` in a static context
							static TestUseTree2_ROOT r = new TestUseTree2_ROOT();
						}

						TestUseTree2_ROOT1.r.f();
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in TestUseTree2.java (at line 14)
				static TestUseTree2_ROOT r = new TestUseTree2_ROOT();
				                             ^^^^^^^^^^^^^^^^^^^^^^^
			Cannot instantiate local class 'TestUseTree2_ROOT' in a static context
			----------
			""");
	}
	public void testGH3654() throws Exception {
		// from https://bugs.openjdk.org/browse/JDK-8334252
		runConformTest(new String[] {
				"LambdaOuterCapture.java",
				"""
				 public class LambdaOuterCapture {

					public class Inner {

						public Inner() {
							Runnable r = () -> System.out.println(LambdaOuterCapture.this);
							this(r);
						}

						public Inner(Runnable r) {
						}
					}

					public static void main(String[] args) {
						new LambdaOuterCapture().new Inner();
					}
				}
				"""},
				"");
	}
	public void testGH3655() {
		runConformTest(new String[] {
			"Test1.java",
			"""
			public class Test1 {
				class Inner {
					Inner() {
						this(() -> new Object() { { Test1.this.print(); } });
					}
					Inner(Runnable r) {
						r.run();
					}
				}
				void print() {
					System.out.print(3);
				}
				public static void main(String... args) {
					new Test1().new Inner();
				}
			}
			"""
			},
			"3");
	}
	public void testGH3653() {
		runConformTest(new String[] {
			"Outer.java",
			"""
			class Outer { // bug
				interface A { }

				class Inner {
				   Inner() {
					  this(() -> {
							class Local {
								void g() {
									m();
								}
							}
							new Object() {
								void k() { new Local().g(); }
							}.k();
						});
					}

					Inner(Runnable tr) {
						tr.run();
					}
				}

				void m() {
					System.out.println("Hello");
				}

				public static void main(String[] args) {
					new Outer().new Inner();
				}
			}
			"""
			},
			"Hello");
	}
	public void testGH3652() {
		runConformTest(new String[] {
			"Outer.java",
			"""
			class Outer {
				interface A {
					void run();
				}
				interface B {
					void run();
				}

				class Inner1 {
					Inner1() {
						this(new A() {
							class Inner2 {
								Inner2() {
									this(new B() {
										public void run() {
											m();
											g();
										}
									});
								}

								Inner2(B o) {
									o.run();
								}
							}

							public void run() {
								new Inner2();
							}

							void m() { System.out.print(getClass().getName() + ".m() "); }
						});
					}

					Inner1(A o) { o.run(); }
				}
				void g() { System.out.print(getClass().getName() +".g()"); }

				public static void main(String[] args) {
					new Outer().new Inner1();
				}
			}
			"""
		},
		"Outer$Inner1$1.m() Outer.g()");
	}
	public void testGH3687a() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					public void main(String[] args) {
						System.out.println(foo() != 0);
					}
					static int foo() {
						class Local {
							int value = 0;
							class Local2 {
								public static int bar() {
									return new Local().value;
								}
							}
						}
						return Local.Local2.bar();
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 10)
				return new Local().value;
				       ^^^^^^^^^^^
			Cannot instantiate local class 'Local' in a static context
			----------
			""");
	}
	public void testGH3687b() {
		runConformTest(new String[] {
				"X.java",
				"""
				public class X {
					@SuppressWarnings("unused")
					static int INT_FIELD = new Object() {
						class Local {
							int value = 10;
							class Local2 {
								public int bar() {
									int v = new Local().value;
									System.out.print(v);
									return v;
								}
							}
						}
						int l = new Local().new Local2().bar();
					}.hashCode();
					public static void main(String... args) {
						int h = INT_FIELD / 2;
					}
				}
				"""
			},
			"10");
	}
	public void testGH3700() {
		// test case from https://bugs.openjdk.org/browse/JDK-8333313
		runConformTest(new String[] {
				"Main.java",
				"""
				interface Foo {
					void foo();
				}

				public class Main {
					static int check = 0;

					class Test {
						Test() {}
						Test(int a) {
							class InnerLocal {
								int a = 1;
							}
							Foo lmb = () -> {
								Main.check = new InnerLocal() {
										public int a() {
											return this.a;
										}
									}.a();
							};
							lmb.foo();
							this();
						}
					}
					public static void main(String... args) {
						new Main().new Test(3);
						System.out.print(check);
					}
				}
				"""
		},
		"1");
	}

	public void testGH3748a() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					final int fin1, fin2;
					{
						fin1 = 0;
						fin2 = 1;
					}
					X() {
						int abc = 0; // Commenting out this line brings out the error
						this(fin1 = 10);
						fin2 = 11;
					}
					X(int x) {}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 4)
				fin1 = 0;
				^^^^
			The final field fin1 may already have been assigned
			----------
			4. ERROR in X.java (at line 10)
				fin2 = 11;
				^^^^
			The final field fin2 may already have been assigned
			----------
			""");
	}

	public void testGH3748b() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					final int fin1;
					final int fin2;
					{
						fin1 = 0;
						fin2 = 1;
					}
					X() {
						this(fin1 = 10);
						fin2 = 11;
					}
					X(int x) {}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 5)
				fin1 = 0;
				^^^^
			The final field fin1 may already have been assigned
			----------
			3. ERROR in X.java (at line 10)
				fin2 = 11;
				^^^^
			The final field fin2 may already have been assigned
			----------
			""");
	}
	public void testGH3687c() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					import java.util.function.IntSupplier;
					@SuppressWarnings("unused")
					public class X {
						public static void main(String argv[]) {
							class Parent {
								int value;
								Parent(int i) {
									this.value = i;
								}
							}
							class Outer {
								static IntSupplier supplier = () -> {
									class InnerLocal extends Parent {
										InnerLocal() {
											super(10);
										}
									}
									return new InnerLocal().value;
								};
							}
							System.out.println(Outer.supplier.getAsInt());
						}
					}"""
			},
				"----------\n" +
				"1. ERROR in X.java (at line 15)\n" +
				"	super(10);\n" +
				"	^^^^^^^^^^\n" +
				"Cannot instantiate local class \'Parent\' in a static context\n" +
				"----------\n");
	}
	public void testGH3687d() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					import java.util.function.IntSupplier;
					import java.util.function.Predicate;
					@SuppressWarnings("unused")
					public class X {
						public static void main(String argv[]) {
							Predicate<Integer> condition = (Integer param) -> {
								class Parent {
									int value;
									Parent(int i) {
										value = i;
									}
								}
								class Outer {
									static {
										class Inner extends Parent {
											Inner(int i) {
												super(i);
											}
										}
									}
								}
								return param <= 10;
							};
							System.out.println(condition.test(10));
						}
					}"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 17)\n" +
			"	super(i);\n" +
			"	^^^^^^^^^\n" +
			"Cannot instantiate local class \'Parent\' in a static context\n" +
			"----------\n");
	}
	public void testGH3687e() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					import java.io.PrintStream;
					import java.util.function.Predicate;
					@SuppressWarnings("unused")
					public class X {
						static {
							class SuperClass {}
							class Outer {
								static int a;
								static Predicate<Object> test = (o) -> new SuperClass() {
									public boolean test() {
										return true;
									}
								}.test();
							}
						}
						public static void main(String argv[]) {
						}
					}"""
			},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	static Predicate<Object> test = (o) -> new SuperClass() {\n" +
				"	                                           ^^^^^^^^^^^^\n" +
				"Cannot instantiate local class \'SuperClass\' in a static context\n" +
				"----------\n");
	}
	public void testGH3753() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					static int value = 0;
					public static void main(String[] argv) {
						class Local {
							class Inner {}
							Local(int a) {
								X.value = new Inner() { // This is reported by Javac
									public int foo() {
										return 1;
									}
								}.foo();
								super();
							}
						}
					}
				}
				"""
		},
		"""
		----------
		1. WARNING in X.java (at line 4)
			class Local {
			      ^^^^^
		The type Local is never used locally
		----------
		2. ERROR in X.java (at line 7)
			X.value = new Inner() { // This is reported by Javac
			          ^^^^^^^^^^^
		No enclosing instance of type Local is accessible. Must qualify the allocation with an enclosing instance of type Local (e.g. x.new A() where x is an instance of Local).
		----------
		""");
	}

	public void testGH3844() {
		runConformTest(new String[] {
			"SubClass.java",
			"""
			class Base {
			  public Base(long n) {
			  }
			}

			class SubClass extends Base {
			  private final String anyFinalWithInitializer = "init";

			  public SubClass() {
			    super(System.currentTimeMillis());
			  }
			  public static void main(String... args) {
			    SubClass s = new SubClass();
			    System.out.print(s.anyFinalWithInitializer);
			  }
			}
			"""},
			"init");
	}

	public void testJDK8346380() {
		runNegativeTest(new String[] {
			"O.java",
			"""
			@SuppressWarnings("unused")
			class O {
				static void foo(int x) { // static context, so no enclosing instance
					class X { /* capture x */ } // inner local class in a static context
					class U {
						static void test() { new X(){  }; } // just as bad as saying `new X()`
					}
				}
			}
			"""
			},
			"""
			----------
			1. ERROR in O.java (at line 6)
				static void test() { new X(){  }; } // just as bad as saying `new X()`
				                         ^^^
			Cannot instantiate local class 'X' in a static context
			----------
			""");
	}

	public void testGH4193() {
		runConformTest(new String[] {
				"C.java",
				"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.Objects;
				import java.util.function.Consumer;
				import java.util.function.Supplier;

				public final class C {
				  private C() { }

				  public static record CN<T>(List<Class<?>> types,
				    Supplier<List<Object>> values, Consumer<T> consumer) {
				    public CN {
				      Objects.requireNonNull(types, "types == null");
				      Objects.requireNonNull(values, "values == null");
				    }
				    public CN(final List<Class<?>> types, final Supplier<List<Object>> values) {
				      System.out.println("before this");
				      this(types, values, null);
				    }
				    public CN(final List<Class<?>> xtypes, final Object... xvalues) {
				      this(xtypes, () -> Arrays.asList(xvalues), null);
				    }
				    public CN(final Consumer<T> c) {
				      this(List.of(), () -> List.of(), c);
				    }
				    public CN() {
				      this(List.of(), () -> List.of(), null);
				    }
				  }
				}
				"""
		});
	}
	public void testGH4193_neg() {
		runNegativeTest(new String[] {
				"C.java",
				"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.Objects;
				import java.util.function.Consumer;
				import java.util.function.Supplier;

				public final class C {
				  private C() { }

				  public static record CN<T>(List<Class<?>> types,
				    Supplier<List<Object>> values, Consumer<T> consumer) {
				    public CN {
				      Objects.requireNonNull(types, "types == null");
				      Objects.requireNonNull(values, "values == null");
				      super();
				    }
				    public CN(final List<Class<?>> types, final Supplier<List<Object>> values) {
				      System.out.println("before this");
				      super();
				    }
				    public CN(final List<Class<?>> xtypes, final Object... xvalues) {
				      super();
				    }
				    public CN(final List<Class<?>> xtypes) {
				    }
				  }
				}
				"""
		},
		"""
		----------
		1. ERROR in C.java (at line 15)
			super();
			^^^^^^^^
		The body of a compact constructor must not contain an explicit constructor call
		----------
		2. ERROR in C.java (at line 19)
			super();
			^^^^^^^^
		A non-canonical constructor must invoke another constructor of the same class
		----------
		3. ERROR in C.java (at line 22)
			super();
			^^^^^^^^
		A non-canonical constructor must invoke another constructor of the same class
		----------
		4. ERROR in C.java (at line 24)
			public CN(final List<Class<?>> xtypes) {
			       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		A non-canonical constructor must invoke another constructor of the same class
		----------
		""");
	}
	public void testGH4449a() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

			  final int i;

			  public X() {
			    this.i = 1;
			    this(2);
			    System.out.print(this.i);
			  }

			  public X(int i) {
			    this.i = i;
			  }

			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 7)
			this(2);
			^^^^^^^^
		The final field i may already have been assigned
		----------
		""");
	}
	public void testGH4449b() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

			  final int i;
			  public X(Integer o) { this(o.intValue()); }
			  public X() {
			    this.i = 1;
			    this(2);
			    System.out.print(this.i);
			  }

			  public X(int i) {
			    this.i = i;
			  }

			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 7)
			this(2);
			^^^^^^^^
		The final field i may already have been assigned
		----------
		""");
	}
	public void testGH4449c() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

			  final int i;
			  public X(Integer o) { this.i = o.intValue(); }
			  public X() {
			    this.i = 1;
			    this(2);
			    System.out.print(this.i);
			  }

			  public X(int i) {
			    this.i = i;
			  }

			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 7)
			this(2);
			^^^^^^^^
		The final field i may already have been assigned
		----------
		""");
	}
	public void testGH4449d() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			class X {
				final Integer f1 = 1;
				final Integer f2;
				final Integer f3;
				final Integer f4;
				{
					// f1 DA
					// f2 DU
					// f3 PUA
					// f4 PUA
					Integer l1;
					f2 = 2 * f1;
					if (cond())
						l1 = f3;
					else
						f3 = 3;
					// f1 DA
					// f2 DA
					// f3 PUA
					// f4 PUA
				}

				X() {
					f3 = 31;
					f4 = 4;
					// after prologue:
					// f1 DU
					// f2 DU
					// f3 DA
					// f4 DA
					super();
				}
				X(Double d) {
					// f1 DA
					// f2 DA
					// f3 PUA
					// f4 DU
					f3 = 33;
					f4 = 44;
				}
				static boolean cond() { return false; }
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 14)
			l1 = f3;
			     ^^
		The blank final field f3 may not have been initialized
		----------
		2. ERROR in X.java (at line 16)
			f3 = 3;
			^^
		The final field f3 may already have been assigned
		----------
		3. ERROR in X.java (at line 38)
			f3 = 33;
			^^
		The final field f3 may already have been assigned
		----------
		""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4585
	// [25] ECJ reports a bogus "The final field t may already have been assigned" error
	public void testIssue4585() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
				class TestClass<T extends Object> {
					T t;
					TestClass() {}

					TestClass(T v) {
						switch (0) {
							case 2:
								t = v;
								break;
						}
						this(); // Error here
					}
				}
			}
			"""
		});
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4696
	public void testIssue4696a() {
	    runConformTest(new String[] {
	        "EcjBugRepro.java",
	        """
	        import java.util.HashMap;
            import java.util.Map;

            public class EcjBugRepro {

                // 1. Field with implicit initializer
                private final Map<String, String> data = new HashMap<>();

                // Constructor 1: Uses Java 25 Pre-construction context + Chaining
                public EcjBugRepro(int check) {
                    if (check < 0) throw new IllegalArgumentException(); // Pre-construction statement
                    this("delegated"); // Chaining
                    // ERROR: ECJ seems to re-run 'data = new HashMap()' here, wiping the map
                }

                // Constructor 2: Standard
                public EcjBugRepro(String val) {
                    super();
                    // Field init runs here (Correct)
                    data.put("key", val);
                    System.out.println("Constructor 2: Added item. Size: " + data.size());
                }

                public static void main(String[] args) {
                    // Trigger via Constructor 1
                    EcjBugRepro instance = new EcjBugRepro(1);

                    if (instance.data.isEmpty()) {
                        System.err.println("FAILURE: Map is empty! Field was re-initialized.");
                    } else {
                        System.out.println("Success: Map size is " + instance.data.size());
                    }
                }
            }
	        """
	    },
        "Constructor 2: Added item. Size: 1\n" +
        "Success: Map size is 1");
	}
	public void testIssue4696b() {
	    runConformTest(new String[] {
	        "Problem.java",
	        """
  	        public class Problem {
                private int counter = 0;

                Problem(int counter) {
                    this.counter = counter;
                    System.out.println("counter=" + this.counter);
                }

                Problem(boolean b) {
                    if (b) {
                        System.out.println("constructor called");
                    }
                    this(1);
                }

                private Problem() {
                    this(true);
                }

                public static void main(String[] args) {
                	System.out.println(new Problem().counter);
                }
            }
	        """
	        },
	        "constructor called\n" +
	        "counter=1\n" +
	        "1");
	}
	public void testIssue4720() {
		runNegativeTest(new String[] {
			"Test.java",
			"""
			public class Test {
				private final Object o;

				public Test() {
					System.out.println();
					super();
				}
			}
			"""
			},
			"""
			----------
			1. WARNING in Test.java (at line 2)
				private final Object o;
				                     ^
			The value of the field Test.o is not used
			----------
			2. ERROR in Test.java (at line 4)
				public Test() {
				       ^^^^^^
			The blank final field o may not have been initialized
			----------
			""");
	}
}

