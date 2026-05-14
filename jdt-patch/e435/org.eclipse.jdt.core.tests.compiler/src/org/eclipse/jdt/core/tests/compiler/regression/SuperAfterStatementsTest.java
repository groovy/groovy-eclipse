/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class SuperAfterStatementsTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 23");
	private static final String[] VMARGS = new String[] {"--enable-preview"};
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
		return buildMinimalComplianceTestSuite(testClass(), F_23);
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
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
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
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE22Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, VMARGS, JAVAC_OPTIONS);
	}
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Map<String, String> customOptions = getCompilerOptions(true);
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}
	class Runner extends AbstractRegressionTest.Runner {
		public Runner(boolean reportPreview) {
			this();
			this.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, reportPreview ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
		}
		public Runner() {
			super();
			this.vmArguments = VMARGS;
			this.javacTestOptions = JAVAC_OPTIONS;
			this.customOptions = getCompilerOptions();
			this.customOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
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
			"1. WARNING in X.java (at line 12)\n" +
			"	super(value);\n" +
			"	^^^^^^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 16)\n" +
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
				"----------\n" +
				"4. WARNING in X.java (at line 7)\n" +
				"	super();\n" +
				"	^^^^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
					class X {
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
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
		runner.testFiles = new String[] {
				"X.java",
				"""
					class X {
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
			"----------\n" +
			"3. WARNING in X.java (at line 6)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in B.java (at line 6)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			2. WARNING in X.java (at line 7)
				super();
				^^^^^^^^
			You are using a preview language feature that may or may not be supported in a future release
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
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in X.java (at line 10)\n" +
			"	super(i);\n" +
			"	^^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	this(i, 0);\n" +
			"	^^^^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in X.java (at line 13)\n" +
			"	this.b = j == 0;\n" +
			"	^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 14)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in X.java (at line 9)\n" +
			"	this(i, 0);\n" +
			"	^^^^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in X.java (at line 14)\n" +
			"	this.b = j == 0;\n" +
			"	^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 15)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in X.java (at line 9)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
			"----------\n" +
			"2. WARNING in X.java (at line 9)\n" +
			"	this(j);\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
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
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(new String[] {
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
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	super(value);\n" +
			"	^^^^^^^^^^^^^\n" +
			"Flexible Constructor Bodies is a preview feature and disabled by default. Use --enable-preview to enable\n" +
			"----------\n",
			null,
			true,
			options);
	}
	public void testGH2467() {
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		runner.javacTestOptions = JavacTestOptions.DEFAULT;
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
				Flexible Constructor Bodies is a preview feature and disabled by default. Use --enable-preview to enable
				----------
				2. ERROR in Test.java (at line 10)
					super(this.i=n);		// old syntax, this-qualified field reference
					      ^^^^^^
				Flexible Constructor Bodies is a preview feature and disabled by default. Use --enable-preview to enable
				----------
				3. ERROR in Test.java (at line 14)
					super(i=s);				// new syntax, single name reference
					^^^^^^^^^^^
				Flexible Constructor Bodies is a preview feature and disabled by default. Use --enable-preview to enable
				----------
				4. ERROR in Test.java (at line 14)
					super(i=s);				// new syntax, single name reference
					      ^
				Flexible Constructor Bodies is a preview feature and disabled by default. Use --enable-preview to enable
				----------
				5. ERROR in Test.java (at line 18)
					super(this.i=s);		// new syntax, this-qualified field reference
					^^^^^^^^^^^^^^^^
				Flexible Constructor Bodies is a preview feature and disabled by default. Use --enable-preview to enable
				----------
				6. ERROR in Test.java (at line 18)
					super(this.i=s);		// new syntax, this-qualified field reference
					      ^^^^^^
				Flexible Constructor Bodies is a preview feature and disabled by default. Use --enable-preview to enable
				----------
				""";
		runner.runNegativeTest();
	}
	public void testFieldCompoundAssignedInSuperArgument() {
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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
			class C1 {
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
		runner.javacTestOptions = JavacTestOptions.JavacHasABug.JavacBug8336255;
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
				2. WARNING in C1.java (at line 18)
					super();
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""";
		runner.runNegativeTest();
	}

	public void testDuplicateCalls() {
		// but no access to outer this from local class
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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

	public void testGH2466() {
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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
		Runner runner = new Runner(false);
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

}
