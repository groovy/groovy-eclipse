/*******************************************************************************
 * Copyright (c) 2013, 2020 GK Software AG and others.
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

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class FlowAnalysisTest8 extends AbstractNullAnnotationTest {

//Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "testReferenceExpression" };
//	TESTS_NUMBERS = new int[] { 561 };
//	TESTS_RANGE = new int[] { 1, 2049 };
}

public FlowAnalysisTest8(String name) {
	super(name);
}

public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

public static Class testClass() {
	return FlowAnalysisTest8.class;
}

@Override
protected Map getCompilerOptions() {
	Map defaultOptions = super.getCompilerOptions();
	defaultOptions.put(CompilerOptions.OPTION_ReportUnusedLambdaParameter, CompilerOptions.IGNORE);
	return defaultOptions;
}

// Lambda with elided args inherits null contract from the super method
public void testLambda_01() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface ISAM {\n" +
			"	@NonNull String toString(@NonNull String prefix, @Nullable Object o);\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"	void test() {\n" +
			"		ISAM printer = (p,o) -> p.concat(o.toString());\n" +
			"	}\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	ISAM printer = (p,o) -> p.concat(o.toString());\n" +
		"	                        ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	ISAM printer = (p,o) -> p.concat(o.toString());\n" +
		"	                                 ^\n" +
		"Potential null pointer access: this expression has a '@Nullable' type\n" +
		"----------\n");
}

// Lambda with declared args violates null contract of super
public void testLambda_02() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface ISAM {\n" +
			"	void process(@NonNull Object nn, @Nullable Object n, Object u);\n" +
			"}\n",
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	void test() {\n" +
					// try to override, illegal except for unchanged o1:
			"		ISAM printer = (@NonNull  Object o1, @NonNull 	Object o2, @NonNull	 Object o3) -> System.out.println(2);\n" +
			"	}\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	ISAM printer = (@NonNull  Object o1, @NonNull 	Object o2, @NonNull	 Object o3) -> System.out.println(2);\n" +
		"	                                     ^^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter o2, inherited method from ISAM declares this parameter as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	ISAM printer = (@NonNull  Object o1, @NonNull 	Object o2, @NonNull	 Object o3) -> System.out.println(2);\n" +
		"	                                              	           ^^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter o3, inherited method from ISAM does not constrain this parameter\n" +
		"----------\n");
}

// Lambda with declared args inherits / modifies contract of super
public void testLambda_03() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface ISAM {\n" +
			"	void process(@NonNull Object nn, @Nullable Object n, Object u);\n" +
			"}\n",
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	void test() {\n" +
					// fill-in all from super:
			"		ISAM printer1 = (Object 		  o1, 			Object o2, 			 Object o3) \n" +
			"							-> System.out.println(o1.toString()+o2.toString()+o3.toString());\n" +
					// legal overrides: (however, @NonNull -> @Nullable is probably nonsense)
			"		ISAM printer3 = (@Nullable Object o1, @Nullable Object o2, @Nullable Object o3) \n" +
			"							-> System.out.println(o1.toString()+o2.toString()+o3.toString());\n" +
			"	}\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	-> System.out.println(o1.toString()+o2.toString()+o3.toString());\n" +
		"	                                    ^^\n" +
		"Potential null pointer access: The variable o2 may be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	-> System.out.println(o1.toString()+o2.toString()+o3.toString());\n" +
		"	                      ^^\n" +
		"Potential null pointer access: this expression has a '@Nullable' type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	-> System.out.println(o1.toString()+o2.toString()+o3.toString());\n" +
		"	                                    ^^\n" +
		"Potential null pointer access: this expression has a '@Nullable' type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	-> System.out.println(o1.toString()+o2.toString()+o3.toString());\n" +
		"	                                                  ^^\n" +
		"Potential null pointer access: this expression has a '@Nullable' type\n" +
		"----------\n");
}

// Lambda with declared args has illegal @NonNull an primitive argument
public void testLambda_04() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"public interface ISAM {\n" +
			"	void process(int i);\n" +
			"}\n",
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	void test() {\n" +
			"		ISAM printer1 = (@NonNull int i) \n" +
			"							-> System.out.println(i);\n" +
			"	}\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	ISAM printer1 = (@NonNull int i) \n" +
		"	                 ^^^^^^^^\n" +
		"The nullness annotation @NonNull is not applicable for the primitive type int\n" +
		"----------\n");
}

// Lambda inherits null contract and has block with return statement
public void testLambda_05() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface ISAM {\n" +
			"	@NonNull String toString(Object o);\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"	void test() {\n" +
			"		ISAM printer = (o) -> {\n" +
			"			System.out.print(13);\n" +
			"			return null; // error\n" +
			"		};\n" +
			"	}\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return null; // error\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n");
}
// Lambda has no descriptor (overriding method from Object), don't bail out with NPE during analysis
public void testLambda_05a() {
	Map customOptions = getCompilerOptions();
	runNegativeTest(
		new String[] {
			"ISAM.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface ISAM {\n" +
			"	@NonNull String toString();\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"	void test() {\n" +
			"		ISAM printer = () -> {\n" +
			"			System.out.print(13);\n" +
			"			return null;\n" +
			"		};\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	ISAM printer = () -> {\n" +
		"	               ^^^^^\n" +
		"The target type of this expression must be a functional interface\n" +
		"----------\n",
		this.LIBS,
		true /*flush*/,
		customOptions);
}
// Test flows with ReferenceExpression regarding:
// - definite assignment
// - unused local
public void testReferenceExpression1() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runNegativeTest(
		new String[] {
			 "I.java",
			 "public interface I {\n" +
			 "	public void bar();\n" +
			 "}\n",
			 "X.java",
			 "public class X {\n" +
			 "	public void moo() {}\n" +
			 "	public static void soo() {}\n" +
			 "	void testAssignment() {\n" +
			 "		X x;\n" +
			 "		I i = x::moo; // x is unassigned\n" +
			 "		i.bar();\n" +
			 "		I i2 = X::soo;\n" + // OK
			 "	}\n" +
			 "	void testStatic() {\n" +
			 "		X xs;\n" +
			 "		I is = xs::soo;\n" +
			 "	}\n" +
			 "	void testUse() {\n" +
			 "		X x1 = this, x2 = this; // x2 is not used, only x is\n" +
			 "		I i = x1::moo;\n" +
			 "		i.bar();\n" +
			 "	}\n" +
			 "}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	I i = x::moo; // x is unassigned\n" +
		"	      ^\n" +
		"The local variable x may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	I is = xs::soo;\n" +
		"	       ^^^^^^^\n" +
		"The method soo() from the type X should be accessed in a static way \n" +
		"----------\n" +
		"3. ERROR in X.java (at line 15)\n" +
		"	X x1 = this, x2 = this; // x2 is not used, only x is\n" +
		"	             ^^\n" +
		"The value of the local variable x2 is not used\n" +
		"----------\n",
		null/*libs*/, true/*flush*/, options);
}
public void testReferenceExpression_null_1() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		false /*skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		new String[] {
			 "I.java",
			 "public interface I {\n" +
			 "	public void foo();\n" +
			 "}\n",
			 "X.java",
			 "public class X {\n" +
			 "	public void bar() {}\n" +
			 "	void test() {\n" +
			 "		X x = null;\n" +
			 "		I i = x::bar;\n" +
			 "		i.foo();\n" +
			 "	}\n" +
			 "}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	I i = x::bar;\n" +
		"	      ^\n" +
		"Null pointer access: The variable x can only be null at this location\n" +
		"----------\n",
		null/*libs*/, true/*flush*/, options);
}
public void testReferenceExpression_nullAnnotation_1() {
	runNegativeTestWithLibs(
		new String[] {
			 "I.java",
			 "import org.eclipse.jdt.annotation.*;\n" +
			 "public interface I {\n" +
			 "	public @NonNull String foo(@Nullable Object s);\n" +
			 "}\n",
			 "X.java",
			 "import org.eclipse.jdt.annotation.*;\n" +
			 "public class X {\n" +
			 "	public @Nullable String bar(@NonNull Object s) { return s.toString(); }\n" +
			 "	void test() {\n" +
			 "		I i = this::bar;\n" +
			 "		System.out.print(i.foo(null));\n" +
			 "	}\n" +
			 "}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	I i = this::bar;\n" +
		"	      ^^^^^^^^^\n" +
		"Null type mismatch at parameter 1: required '@NonNull Object' but provided '@Nullable Object' via method descriptor I.foo(Object)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	I i = this::bar;\n" +
		"	      ^^^^^^^^^\n" +
		"Null type mismatch at method return type: Method descriptor I.foo(Object) promises '@NonNull String' but referenced method provides '@Nullable String'\n" +
		"----------\n");
}
public void testReferenceExpression_nullAnnotation_2() {
	runWarningTestWithLibs(
		true, /* skipJavac */
		new String[] {
			 "I.java",
			 "import org.eclipse.jdt.annotation.*;\n" +
			 "public interface I {\n" +
			 "	public @NonNull String foo(@Nullable Object s);\n" +
			 "}\n",
			 "X.java",
			 "public class X {\n" +
			 "	public String bar(Object s) { return s.toString(); }\n" +
			 "	void test() {\n" +
			 "		I i = this::bar;\n" +
			 "		System.out.print(i.foo(null));\n" +
			 "	}\n" +
			 "}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	I i = this::bar;\n" +
		"	      ^^^^^^^^^\n" +
		"Null type safety at method return type: Method descriptor I.foo(Object) promises \'@NonNull String\' but referenced method provides \'String\'\n" +
		"----------\n");
}
public void testReferenceExpression_nullAnnotation_3() {
	runNegativeTest(
		new String[] {
			 "I.java",
			 "import org.eclipse.jdt.annotation.*;\n" +
			 "public interface I {\n" +
			 "	public @NonNull String foo(Object s);\n" +
			 "}\n",
			 "X.java",
			 "import org.eclipse.jdt.annotation.*;\n" +
			 "public class X {\n" +
			 "	public @NonNull String bar(@NonNull Object s) { return \"\"; }\n" +
			 "	void test() {\n" +
			 "		I i = this::bar;\n" +
			 "		System.out.print(i.foo(null));\n" +
			 "	}\n" +
			 "	Zork zork;\n" + // make warning visible by forcing an error
			 "}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	I i = this::bar;\n" +
		"	      ^^^^^^^^^\n" +
		"Null type safety: parameter 1 provided via method descriptor I.foo(Object) needs unchecked conversion to conform to '@NonNull Object'\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	Zork zork;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		this.LIBS,
		true /*flush*/,
		getCompilerOptions());
}
public void testBug535308a() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 "public class X {\n" +
				 "	public int someTest() {\n" +
				 "		boolean unused = false;\n" +
				 "		final boolean thisIsFalse = false;\n" +
				 "		if (getSomeValue() == thisIsFalse) {\n" +
				 "			return 0;\n" +
				 "		}\n" +
				 "		return 1;\n" +
				 "	}\n" +
				 "	private boolean getSomeValue() {\n" +
				 "		return true;\n" +
				 "	}\n" +
				 "}"
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	boolean unused = false;\n" +
			"	        ^^^^^^\n" +
			"The value of the local variable unused is not used\n" +
			"----------\n";
	runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug535308b() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 "public class X {\n" +
				 "	public int someTest() {\n" +
				 "		boolean unused = false;\n" +
				 "		final boolean thisIsFalse = false;\n" +
				 "		if (getSomeValue() != thisIsFalse) {\n" +
				 "			return 0;\n" +
				 "		}\n" +
				 "		return 1;\n" +
				 "	}\n" +
				 "\n" +
				 "	private boolean getSomeValue() {\n" +
				 "		return true;\n" +
				 "	}\n" +
				 "}"
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	boolean unused = false;\n" +
			"	        ^^^^^^\n" +
			"The value of the local variable unused is not used\n" +
			"----------\n";
	runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug535308c() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 "public class X {\n" +
				 "	public int someTest() {\n" +
				 "		boolean unused = false;\n" +
				 "		final boolean thisIsFalse = false;\n" +
				 "		if (thisIsFalse != getSomeValue()) {\n" +
				 "			return 0;\n" +
				 "		}\n" +
				 "		return 1;\n" +
				 "	}\n" +
				 "\n" +
				 "	private boolean getSomeValue() {\n" +
				 "		return true;\n" +
				 "	}\n" +
				 "}"
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	boolean unused = false;\n" +
			"	        ^^^^^^\n" +
			"The value of the local variable unused is not used\n" +
			"----------\n";
	runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug535308d() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 "public class X {\n" +
				 "	public int someTest() {\n" +
				 "		boolean unused = false;\n" +
				 "		final boolean thisIsFalse = false;\n" +
				 "		if (thisIsFalse == getSomeValue()) {\n" +
				 "			return 0;\n" +
				 "		}\n" +
				 "		return 1;\n" +
				 "	}\n" +
				 "\n" +
				 "	private boolean getSomeValue() {\n" +
				 "		return true;\n" +
				 "	}\n" +
				 "}"
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	boolean unused = false;\n" +
			"	        ^^^^^^\n" +
			"The value of the local variable unused is not used\n" +
			"----------\n";
	runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug535308e() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 "public class X {\n" +
				 "	public int someTest() {\n" +
				 "		boolean used = false;\n" +
				 "		final boolean thisIsFalse = false;\n" +
				 "		if (used == getSomeValue()) {\n" +
				 "			return 0;\n" +
				 "		}\n" +
				 "		return 1;\n" +
				 "	}\n" +
				 "\n" +
				 "	private boolean getSomeValue() {\n" +
				 "		return true;\n" +
				 "	}\n" +
				 "}"
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	final boolean thisIsFalse = false;\n" +
			"	              ^^^^^^^^^^^\n" +
			"The value of the local variable thisIsFalse is not used\n" +
			"----------\n";
			runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug474080() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"import java.io.IOException;\n" +
			"interface Config {\n" +
			"	double getTime(long l);\n" +
			"}\n" +
			"class MarketMaker {\n" +
			"	static double closeTime;\n" +
			"}\n" +
			"public class Test {\n" +
			"	 boolean stopped;\n" +
			"    Config config;\n" +
			"    long tick() { return 0L; }\n" +
			"    void doStuff() {}\n" +
			"    public void start() {\n" +
			"        new Thread(() -> {\n" +
			"            while (eventSequence.running) {\n" + // unresolved
			"                try\n" +
			"                    {\n" +
			"                    double t = config.getTime(tick());\n" +
			"                    if ((t >= MarketMaker.closeTime)) {\n" +
			"                        if ((! stopped)) {\n" +
			"                            try {\n" +
			"                                System.out.println(\"Stopping...\");\n" +
			"                                doStuff();\n" +
			"                                Thread.sleep(250);\n" +
			"                                System.out.println(\"Simulation finished\");\n" +
			"                            } catch (InterruptedException e) {\n" +
			"                                e.printStackTrace();\n" +
			"                            }\n" +
//			"                            mainFrame.simulator.stop();\n" +
			"                            stopped = true;\n" +
			"                        }\n" +
			"                    }\n" +
			"                }\n" +
			"                catch (IOException e) {\n" +
			"                    e.printStackTrace();\n" +
			"                }\n" +
			"            }\n" +
			"        }).start();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 15)\n" +
		"	while (eventSequence.running) {\n" +
		"	       ^^^^^^^^^^^^^\n" +
		"eventSequence cannot be resolved to a variable\n" +
		"----------\n");
}
}
