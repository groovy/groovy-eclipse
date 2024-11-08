/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImplicitTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.junit.Test;

public class ImplicitlyDeclaredClassesTest extends AbstractRegressionTest9 {
	public static boolean optimizeStringLiterals = false;
	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 23");
	private static final String[] VMARGS = new String[] {"--enable-preview"};

	static {
//		TESTS_NAMES = new String[] {"testImplicitType001"};
	}
	public ImplicitlyDeclaredClassesTest(String testName){
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

	public static Class<?> testClass() {
		return ImplicitlyDeclaredClassesTest.class;
	}

	public static junit.framework.Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_23);
	}
	@Override
	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean previewFlag) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, previewFlag ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		return defaultOptions;
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		if(!isJRE23Plus)
			return;
		runConformTest(testFiles, expectedOutput, null, VMARGS, new JavacTestOptions("-source 23 --enable-preview"));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE23Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, VMARGS, JAVAC_OPTIONS);
	}
	@Override
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
	private CompilationUnitDeclaration parse(String source, String testName) {
		this.complianceLevel = ClassFileConstants.JDK23;
		/* using regular parser in DIET mode */
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.enablePreviewFeatures = true;
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					options,
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		ICompilationUnit sourceUnit = new CompilationUnit(source.toCharArray(), testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		return parser.parse(sourceUnit, compilationResult);
	}

	private ImplicitTypeDeclaration implicitTypeDeclaration(CompilationUnitDeclaration cu) {
		return cu == null || cu.types == null ? null :
			Stream.of(cu.types).filter(ImplicitTypeDeclaration.class::isInstance).map(ImplicitTypeDeclaration.class::cast).findAny().orElse(null);
	}

	@Test
	public void testParseExplicitClass() {
		CompilationUnitDeclaration res = parse("import java.lang.*;\npublic class A {}", "A.java");
		assertFalse(res.compilationResult.hasErrors());
		assertNull(implicitTypeDeclaration(res));
	}

	@Test
	public void testParseOnlyMain() {
		CompilationUnitDeclaration res = parse("void main() {}", "A.java");
		assertFalse(res.hasErrors());
		ImplicitTypeDeclaration implicitTypeDeclaration = implicitTypeDeclaration(res);
		assertNotNull(implicitTypeDeclaration);
		assertTrue(Stream.of(implicitTypeDeclaration.methods).anyMatch(m -> m instanceof MethodDeclaration method && "main".equals(new String(method.selector))));
		// should generated A.class (unnamed)
	}

	@Test
	public void testParseMixedMethodAndTypes() {
		CompilationUnitDeclaration res = parse("""
			void hello() {}
			public class B {}
			void main() {}
			""", "A.java");
		assertFalse(res.compilationResult.hasErrors());
		// hello, main, and the implicit constructor
		assertEquals(3, implicitTypeDeclaration(res).methods.length);
		// should generated A.class (unnamed) and A$B.class
		assertEquals(1, res.types[0].memberTypes.length);
	}
	// Test that no reference to the implicit type name can be made within the same CU
	@Test
	public void testImplicitType001() {
		runNegativeTest(
					new String[] {"X.java",
					"""
					public static void main(String[] args) {
						X x = new X();
					}
					class XYZ {
						X x = new X();
					}"""},
					"----------\n" +
					"1. ERROR in X.java (at line 2)\n" +
					"	X x = new X();\n" +
					"	^\n" +
					"X cannot be resolved to a type\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 2)\n" +
					"	X x = new X();\n" +
					"	          ^\n" +
					"X cannot be resolved to a type\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 5)\n" +
					"	X x = new X();\n" +
					"	^\n" +
					"X cannot be resolved to a type\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 5)\n" +
					"	X x = new X();\n" +
					"	          ^\n" +
					"X cannot be resolved to a type\n" +
					"----------\n");

	}
	// Test implicit type without a valid candidate main method - 1
	@Test
	public void testImplicitType002() {
		runNegativeTest(
					new String[] {"X.java",
						"""
						private static void main(String[] args) {
						}
						class XYZ {
						}"""},
					"----------\n" +
					"1. ERROR in X.java (at line 1)\n" +
					"	private static void main(String[] args) {\n" +
					"	^\n" +
					"Implicitly declared class must have a candidate main method\n" +
					"----------\n");

	}
	// Test implicit type without a valid candidate main method - 2
	@Test
	public void testImplicitType003() {
		runNegativeTest(
					new String[] {"X.java",
						"""
						public static void main(int[] args) {
						}
						class XYZ {
						}"""},
					"----------\n" +
					"1. ERROR in X.java (at line 1)\n" +
					"	public static void main(int[] args) {\n" +
					"	^\n" +
					"Implicitly declared class must have a candidate main method\n" +
					"----------\n");

	}
	// Test implicit type without a valid candidate main method - 3
	@Test
	public void testImplicitType004() {
		runNegativeTest(
					new String[] {"X.java",
						"""
						public static void main(String args) {
						}
						class XYZ {
						}"""},
					"----------\n" +
					"1. ERROR in X.java (at line 1)\n" +
					"	public static void main(String args) {\n" +
					"	^\n" +
					"Implicitly declared class must have a candidate main method\n" +
					"----------\n");

	}

	// Test implicit type without a valid candidate main method - 4
	@Test
	public void testImplicitType005() {
		runNegativeTest(
					new String[] {"X.java",
						"""
						public static void not_main(String ... args) {
						}
						class XYZ {
						}"""},
					"----------\n" +
					"1. ERROR in X.java (at line 1)\n" +
					"	public static void not_main(String ... args) {\n" +
					"	^\n" +
					"Implicitly declared class must have a candidate main method\n" +
					"----------\n");

	}
	// Test implicit type with a valid candidate main method (public but no static, and String[] argument)
	@Test
	public void testImplicitType006() {
		try {
			runConformTest(
					new String[] {"X.java",
						"""
						public static void main() {
							System.out.println("Hello");
						}"""},
					"Hello");
		} finally {
		}
	}
	// Test implicit type with a valid candidate main method (public but no static, and String[] argument)
	@Test
	public void testImplicitType007() {
		try {
			runConformTest(
						new String[] {"X.java",
							"""
							public static void main(String[] args) {
								System.out.println("Hello");
							}"""},
						"Hello");
		} finally {
		}
	}
	// Test implicit type with a valid candidate main method (public but no static, and String ... argument)
	@Test
	public void testImplicitType008() {
		try {
			runConformTest(
						new String[] {"X.java",
							"""
							public static void main(String ... args) {
								System.out.println("Hello");
							}"""},
						"Hello");
		} finally {
		}
	}
	@Test
	public void testImplicitType009() {
		runConformTest(
					new String[] {"X.java",
						"""
						static String str = "1";
						public static void main(String ... args) {
							System.out.println(str);
						}"""},
					"1");
	}
	@Test
	public void testImplicitType011() {
		runConformTest(
					new String[] {"X.java",
						"""
						interface I { int i = 1; }
						static int i = 1;
						public static void main(String ... args) {
							System.out.println(i == 1 && I.i == 1);
						}"""},
					"true");
	}
	@Test
	public void testImplicitImport() {
		// the explicit class must be given first to trigger https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2952
		// the test is made as negative because we can't execute the second class
		// and we are not interested in executing the first, which cannot see the second
		runNegativeTest(
				new String[] {
					"b/B.java",
					"""
					package b;
					import java.util.Collection;
					public class B {
						public static void print(Collection<?> col) {
							System.out.print(col.size());
						}
						Zork zork;
					}
					""",
					"X.java",
					"""
					void main() {
						b.B.print(Collections.emptySet());
					}"""
				},
				"----------\n" +
				"1. ERROR in b\\B.java (at line 7)\n" +
				"	Zork zork;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	public void testGH3137a() {
		runConformTest(new String[] {
				"X.java",
				"""
				public static void main(String[] args) {
					println("Hello1");
					println("Hello2");
				}"""
		},
		"Hello1\n" +
		"Hello2");
	}
	public void testGH3137b() {
		runConformTest(new String[] {
				"X.java",
				"""
				public static void main(String[] args) {
					String str = readln("Enter:");
					println(str);
				}
				"""
		},
		"Enter:",
		null,
		VMARGS,
		JavacTestOptions.SKIP);
	}
}
