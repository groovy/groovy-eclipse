/*******************************************************************************
 * Copyright (c) 2021 GK Software SE, and others.
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

import static org.eclipse.jdt.core.tests.util.Util.createJar;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class NullAnnotationTests17 extends AbstractNullAnnotationTest {

	public NullAnnotationTests17(String name) {
		super(name);
	}

	static {
//			TESTS_NAMES = new String[] { "test_totalTypePatternNonNullExpression" };
//			TESTS_NUMBERS = new int[] { 001 };
//			TESTS_RANGE = new int[] { 1, 12 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}

	public static Class<?> testClass() {
		return NullAnnotationTests17.class;
	}

	@Deprecated // super method is deprecated
	@Override
	protected void setUpAnnotationLib() throws IOException {
		if (this.LIBS == null) {
			String[] defaultLibs = getDefaultClassPaths();
			int len = defaultLibs.length;
			this.LIBS = new String[len+1];
			System.arraycopy(defaultLibs, 0, this.LIBS, 0, len);
			this.LIBS[len] = createAnnotation_2_2_jar(Util.getOutputDirectory() + File.separator, null);
		}
	}

	public static String createAnnotation_2_2_jar(String dirName, String jcl17Path) throws IOException {
		// role our own annotation library as long as o.e.j.annotation is still at BREE 1.8:
		String jarFileName = dirName + "org.eclipse.jdt.annotation_2.2.0.jar";
		createJar(new String[] {
				"module-info.java",
				"module org.eclipse.jdt.annotation {\n" +
				"	exports org.eclipse.jdt.annotation;\n" +
				"}\n",

				"org/eclipse/jdt/annotation/DefaultLocation.java",
				"package org.eclipse.jdt.annotation;\n" +
				"\n" +
				"public enum DefaultLocation {\n" +
				"	\n" +
				"	PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
				"}\n",

				"org/eclipse/jdt/annotation/NonNullByDefault.java",
				"package org.eclipse.jdt.annotation;\n" +
				"\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
				"\n" +
				"import java.lang.annotation.*;\n" +
				" \n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@Target({ ElementType.MODULE, ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE })\n" +
				"public @interface NonNullByDefault {\n" +
				"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
				"}",

				"org/eclipse/jdt/annotation/NonNull.java",
				"package org.eclipse.jdt.annotation;\n" +
				"import static java.lang.annotation.ElementType.TYPE_USE;\n" +
				"\n" +
				"import java.lang.annotation.*;\n" +
				" \n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@Target({ TYPE_USE })\n" +
				"public @interface NonNull {\n" +
				"	// marker annotation with no members\n" +
				"}\n",

				"org/eclipse/jdt/annotation/Nullable.java",
				"package org.eclipse.jdt.annotation;\n" +
				"\n" +
				"import static java.lang.annotation.ElementType.TYPE_USE;\n" +
				"\n" +
				"import java.lang.annotation.*;\n" +
				" \n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@Target({ TYPE_USE })\n" +
				"public @interface Nullable {\n" +
				"	// marker annotation with no members\n" +
				"}\n"
			},
			null,
			jarFileName,
			jcl17Path != null ? new String[] { jcl17Path } : null,
			"17");
		return jarFileName;
	}

	// -------- helper ------------

	private Runner getDefaultRunner() {
		Runner runner = new Runner();
		runner.classLibraries = this.LIBS;
		Map<String,String> opts = getCompilerOptions();
		opts.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
		opts.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		opts.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		runner.customOptions = opts;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions =
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		return runner;
	}

	// --------- tests start -----------

	public void test_typePatternIsNN() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	void foo(Object o) {\n" +
				  "		switch (o) {\n" +
				  "			case Integer i -> consumeInt(i);\n" +
				  "			default -> System.out.println(\"default\");\n" +
				  "		}\n" +
				  "	}\n" +
				  "	void consumeInt(@NonNull Integer i) {\n" +
				  "		System.out.print(i);\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(3);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "3";
		runner.runConformTest();
	}

	public void test_totalTypePatternAdmitsNull() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	void foo(Number n) {\n" +
				  "		try {\n" +
				  "			switch (n) {\n" +
				  "				case Integer i -> consumeInt(i);\n" +
				  "				case Number n0 -> consumeNumber(n0);\n" +
				  "			}\n" +
				  "		} catch (NullPointerException npe) {\n" +
				  "			// ignoring the unchecked warning, and expecting the NPE:\n" +
				  "			System.out.print(npe.getMessage());\n" +
				  "		}\n" +
				  "	}\n" +
				  "	void consumeInt(@NonNull Integer i) {\n" +
				  "		System.out.print(i);\n" +
				  "	}\n" +
				  "	void consumeNumber(@NonNull Number n) {\n" +
				  "		System.out.print(n.toString());\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(null);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 7)\n" +
				"	case Number n0 -> consumeNumber(n0);\n" +
				"	                                ^^\n" +
				"Null type safety (type annotations): The expression of type \'Number\' needs unchecked conversion to conform to \'@NonNull Number\'\n" +
				"----------\n";
		runner.expectedOutputString = "Cannot invoke \"Object.toString()\" because \"n\" is null";
		runner.runConformTest();
	}

	public void test_totalTypePatternNonNullExpression() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	void foo(Number n) {\n" +
				  "		if (n == null) return;\n" + // this prevents the NPE -> no need to warn
				  "		switch (n) {\n" +
				  "			case Integer i -> System.out.print(i);\n" +
				  "			case Number n0 -> consumeNumber(n0);\n" +
				  "		}\n" +
				  "	}\n" +
				  "	void consumeNumber(@NonNull Number n) {\n" +
				  "		System.out.print(n.toString());\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(null);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "";
		runner.runConformTest();
	}

	public void test_totalTypePatternNonNullExpression_swExpr() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	int foo(Number n) {\n" +
				  "		if (n == null) return -1;\n" + // this prevents the NPE -> no need to warn
				  "		return switch (n) {\n" +
				  "			case Integer i -> i;\n" +
				  "			case Number n0 -> consumeNumber(n0);\n" +
				  "		};\n" +
				  "	}\n" +
				  "	int consumeNumber(@NonNull Number n) {\n" +
				  "		return Integer.valueOf(n.toString());\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(null);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "";
		runner.runConformTest();
	}

	public void test_totalTypePatternPlusNullPattern() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	void foo(Number n) {\n" +
				  "		switch (n) {\n" +
				  "			case null -> System.out.print(\"null\");\n" + // this prevents the NPE
				  "			case Integer i -> System.out.print(i);\n" +
				  "			case Number n0 -> consumeNumber(n0);\n" +
				  "		}\n" +
				  "	}\n" +
				  "	void consumeNumber(@NonNull Number n) {\n" +
				  "		System.out.print(n.toString());\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(null);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "null";
		runner.runConformTest();
	}

	public void test_totalTypePatternNullableExpression() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	void foo(@Nullable Number n) {\n" + // @Nullable here turns "unchecked" into "null type mismatch"
				  "		switch (n) {\n" +
				  "			case Integer i -> System.out.print(i);\n" +
				  "			case Number n0 -> consumeNumber(n0);\n" +
				  "		}\n" +
				  "	}\n" +
				  "	void consumeNumber(@NonNull Number n) {\n" +
				  "		System.out.print(n.toString());\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(null);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case Number n0 -> consumeNumber(n0);\n" +
				"	                                ^^\n" +
				"Null type mismatch: required \'@NonNull Number\' but the provided value is inferred as @Nullable\n" +
				"----------\n";
		runner.runNegativeTest();
	}

	public void test_switchOverNNValueWithNullCase() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	void foo(@NonNull Object o) {\n" +
				  "		switch (o) {\n" +
				  "			case Integer i -> consumeInt(i);\n" +
				  "			case null -> System.out.print(\"null\");\n" +
				  "			default -> System.out.println(\"default\");\n" +
				  "		}\n" +
				  "	}\n" +
				  "	void consumeInt(@NonNull Integer i) {\n" +
				  "		System.out.print(i);\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(3);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 6)\n" +
				"	case null -> System.out.print(\"null\");\n" +
				"	^^^^^^^^^\n" +
				"Unnecessary \'null\' pattern, the switch selector expression cannot be null\n" +
				"----------\n";
		runner.expectedOutputString = "3";
		runner.runConformTest();
	}

	public void test_switchNullInSameCase() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	void foo(Object o) {\n" +
				  "		switch (o) {\n" +
				  "			case null, Integer i -> consumeInt(i);\n" +
				  "			default -> System.out.println(\"default\");\n" +
				  "		}\n" +
				  "	}\n" +
				  "	void consumeInt(@NonNull Integer i) {\n" +
				  "		System.out.print(i);\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(3);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case null, Integer i -> consumeInt(i);\n" +
				"	                                   ^\n" +
				"Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable\n" +
				"----------\n";
		runner.runNegativeTest();
	}

	public void test_switchOverNNValueWithNullCase_swExpr() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	int foo(@NonNull Object o) {\n" +
				  "		return switch (o) {\n" +
				  "			case Integer i -> consumeInt(i);\n" +
				  "			case null -> 0;\n" +
				  "			default -> -1;\n" +
				  "		};\n" +
				  "	}\n" +
				  "	int consumeInt(@NonNull Integer i) {\n" +
				  "		return i;\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		System.out.print(new X().foo(3));\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 6)\n" +
				"	case null -> 0;\n" +
				"	^^^^^^^^^\n" +
				"Unnecessary \'null\' pattern, the switch selector expression cannot be null\n" +
				"----------\n";
		runner.expectedOutputString = "3";
		runner.runConformTest();
	}

	public void test_nullHostileSwitch() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	void foo(@Nullable Object o) {\n" +
				  "		switch (o) {\n" +
				  "			case Integer i -> consumeInt(i);\n" +
				  "			default -> System.out.println(o);\n" +
				  "		};\n" +
				  "	}\n" +
				  "	void consumeInt(@NonNull Integer i) {\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(null);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	switch (o) {\n" +
				"	        ^\n" +
				"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
				"----------\n";
		runner.runNegativeTest();
	}

	public void test_defaultDoesNotApplyToNull() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	void foo(@Nullable Object o) {\n" +
				  "		switch (o) {\n" +
				  "			case Integer i -> consumeInt(i);\n" +
				  "			default -> System.out.println(o.toString());\n" +
				  "			case null -> System.out.print(\"null\");\n" +
				  "		};\n" +
				  "	}\n" +
				  "	void consumeInt(@NonNull Integer i) {\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(null);\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "null";
		runner.runConformTest();
	}

	public void test_defaultDoesNotApplyToNull_field() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_SyntacticNullAnalysisForFields, CompilerOptions.ENABLED);
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	@Nullable Object o;\n" +
				  "	void foo() {\n" +
				  "		switch (this.o) {\n" +
				  "			case Integer i -> consumeInt(i);\n" +
				  "			default -> System.out.println(this.o.toString());\n" +
				  "			case null -> System.out.print(\"null\");\n" +
				  "		};\n" +
				  "	}\n" +
				  "	void consumeInt(@NonNull Integer i) {\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo();\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "null";
		runner.runConformTest();
	}

	public void test_defaultDoesNotApplyToNull_field2() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_SyntacticNullAnalysisForFields, CompilerOptions.ENABLED);
		runner.testFiles = new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class X {\n" +
				  "	@Nullable Object o;\n" +
				  "	void foo(X x) {\n" +
				  "		switch (x.o) {\n" +
				  "			case Integer i -> consumeInt(i);\n" +
				  "			default -> System.out.println(x.o.toString());\n" +
				  "			case null -> System.out.print(\"null\");\n" +
				  "		};\n" +
				  "	}\n" +
				  "	void consumeInt(@NonNull Integer i) {\n" +
				  "	}\n" +
				  "	public static void main(String... args) {\n" +
				  "		new X().foo(new X());\n" +
				  "	}\n" +
				  "}\n"
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "null";
		runner.runConformTest();
	}

	public void testBug576329() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_SyntacticNullAnalysisForFields, CompilerOptions.ENABLED);
		runner.testFiles = new String[] {
				"Main.java",
				"public class Main {\n" +
				"    int length;\n" +
				"    public String switchOnArray(Object argv[]) {\n" +
				"        return switch(argv.length) {\n" +
				"        case 0 -> \"0\";\n" +
				"        default -> \"x\";\n" +
				"        };\n" +
				"    }\n" +
				"	public static void main(String... args) {\n" +
				"		System.out.print(new Main().switchOnArray(args));\n" +
				"	}\n" +
				"}\n"
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "0";
		runner.runConformTest();
	}
}
