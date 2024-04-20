/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EnumDietRecoveryTest extends AbstractCompilerTest {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

public EnumDietRecoveryTest(String testName){
	super(testName);
}

/*
 * Toggle compiler in mode -1.5
 */
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	return options;
}

public void checkParse(
	char[] source,
	String expectedDietUnitToString,
	String expectedDietPlusBodyUnitToString,
	String expectedFullUnitToString,
	String expectedCompletionDietUnitToString,
	String testName) {

	/* using regular parser in DIET mode */
	{
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedDietUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit diet structure" + testName,
			expectedDietUnitToString,
			computedUnitToString);
	}
	/* using regular parser in DIET mode + getMethodBodies */
	{
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedDietUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit diet structure" + testName,
			expectedDietUnitToString,
			computedUnitToString);
		if (computedUnit.types != null) {
			for (int i = 0, length = computedUnit.types.length; i < length; i++){
				computedUnit.types[i].parseMethods(parser, computedUnit);
			}
		}
		computedUnitToString = computedUnit.toString();
		if (!expectedDietPlusBodyUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}

		assertEquals(
			"Invalid unit diet+body structure" + testName,
			expectedDietPlusBodyUnitToString,
			computedUnitToString);
	}
	/* using regular parser in FULL mode */
	{
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.parse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedFullUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit full structure" + testName,
			expectedFullUnitToString,
			computedUnitToString);

	}
	/* using source element parser in DIET mode */
	{
		SourceElementParser parser =
			new SourceElementParser(
				new TestSourceElementRequestor(),
				new DefaultProblemFactory(Locale.getDefault()),
				new CompilerOptions(getCompilerOptions()),
				false/*don't record local declarations*/,
				true/*optimize string literals*/);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedDietUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid source element diet structure" + testName,
			expectedDietUnitToString,
			computedUnitToString);
	}
	/* using source element parser in FULL mode */
	{
		SourceElementParser parser =
			new SourceElementParser(
				new TestSourceElementRequestor(),
				new DefaultProblemFactory(Locale.getDefault()),
				new CompilerOptions(getCompilerOptions()),
				false/*don't record local declarations*/,
				true/*optimize string literals*/);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.parse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedFullUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid source element full structure" + testName,
			expectedFullUnitToString,
			computedUnitToString);
	}
	/* using completion parser in DIET mode */
	{
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		CompletionParser parser =
			new CompletionParser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					options,
					new DefaultProblemFactory(Locale.getDefault())),
				false);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult, Integer.MAX_VALUE);
		String computedUnitToString = computedUnit.toString();
		if (!expectedCompletionDietUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid completion diet structure" + testName,
			expectedCompletionDietUnitToString,
			computedUnitToString);
	}
}

public void test0001() {

	String s =
		"package a;											\n"
			+ "#\n"
			+ "public enum X {								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0002() {

	String s =
		"package a;											\n"
			+ "#\n"
			+ "public enum X {								\n"
			+ "  A,											\n"
			+ "  B;											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  A(),\n" +
		"  B(),\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  A(),\n" +
		"  B(),\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0003() {

	String s =
		"package a;											\n"
			+ "#\n"
			+ "public enum X {								\n"
			+ "  A(10),										\n"
			+ "  B(){};										\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  A(10),\n" +
		"  B() {\n" +
		"  },\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  A(10),\n" +
		"  B() {\n" +
		"  },\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0004() {

	String s =
		"package a;											\n"
			+ "#\n"
			+ "public enum X {								\n"
			+ "  B(){										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  }  										\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0005() {

	String s =
		"package a;											\n"
			+ "#\n"
			+ "public enum X {								\n"
			+ "  B(){										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  };  										\n"
			+ "  public X(){}								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0006() {

	String s =
		"package a;											\n"
			+ "#\n"
			+ "public enum X {								\n"
			+ "  B(){										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  }  										\n"
			+ "  public X(){} 								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"  },\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"  },\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0007() {

	String s =
		"package a;											\n"
			+ "#\n"
			+ "public enum X {								\n"
			+ "  B(){										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  }  										\n"
			+ "  X(){} 								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  X() {\n" +
		"  },\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  X() {\n" +
		"  },\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0008() {

	String s =
		"package a;											\n"
			+ "public enum X {								\n"
			+ "  B(){										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  }  										\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  },\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  },\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0009() {

	String s =
		"package a;											\n"
			+ "#              								\n"
			+ "public enum X {								\n"
			+ "  B(){										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  }  										\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0010() {

	String s =
		"package a;											\n"
			+ "#              								\n"
			+ "public enum X {								\n"
			+ "  B(){										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  ;  										\n"
			+ "  void bar(){}  								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
        "    void bar() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +

        "    void foo() {\n" +
        "    }\n" +
        "    void bar() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0011() {

	String s =
		"package a;											\n"
			+ "#              								\n"
			+ "public enum X {								\n"
			+ "  B(){										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  ;  										\n"
			+ "  X(){}      								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
        "    X() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B() {\n" +
        "    void foo() {\n" +
        "    }\n" +
        "    X() {\n" +
        "    }\n" +
		"  },\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0012() {

	String s =
		"package a;											\n"
			+ "#              								\n"
			+ "public enum X {								\n"
			+ "  B()										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  };  										\n"
			+ "  void bar(){}  								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B(),\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B(),\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0013() {

	String s =
		"package a;											\n"
			+ "#              								\n"
			+ "public enum X {								\n"
			+ "  B( {										\n"
			+ "    void foo(){								\n"
			+ "    }										\n"
			+ "  };  										\n"
			+ "  void bar(){}  								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B,\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X {\n" +
		"  B,\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0014() {

	String s =
		"package a;											\n"
			+ "#              								\n"
			+ "public class X {								\n"
			+ "  class Y { 									\n"
			+ "  }   										\n"
			+ "  enum Z {									\n"
			+ "    B() {									\n"
			+ "      void foo(){							\n"
			+ "      }										\n"
			+ "    };  										\n"
			+ "    Z(){}       								\n"
			+ "  }            								\n"
			+ "  class W {     								\n"
			+ "  }             								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"  }\n" +
		"  enum Z {\n" +
		"    B() {\n" +
        "      void foo() {\n" +
        "      }\n" +
		"    },\n" +
		"    <clinit>() {\n" +
		"    }\n" +
		"    Z() {\n" +
		"    }\n" +
		"  }\n" +
		"  class W {\n" +
		"    W() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  enum Z {\n" +
		"    B() {\n" +
        "      void foo() {\n" +
        "      }\n" +
		"    },\n" +
		"    <clinit>() {\n" +
		"    }\n" +
		"    Z() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  class W {\n" +
		"    W() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76874
 */
public void test0015() {

	String s =
		"public enum Enum1 {						\n"
			+ "  BLEU(){   									\n"
			+ "    void foo() {                             \n"
			+ "       System.out.println();     			\n"
			+ "    }    									\n"
			+ "  },             							\n"
			+ "  BLANC,  									\n"
			+ "  ROUGE;										\n"
			+ "                								\n"
			+ "  main         								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public enum Enum1 {\n" +
		"  BLEU() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  BLANC(),\n" +
		"  ROUGE(),\n" +
		"  public Enum1() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"public enum Enum1 {\n" +
		"  BLEU() {\n" +
        "    void foo() {\n" +
        "    }\n" +
		"  },\n" +
		"  BLANC(),\n" +
		"  ROUGE(),\n" +
		"  public Enum1() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=107580
 */
public void test0016() {
	String s =
		"public enum Enum {								\n"
			+ "  BEGIN(\"blabla\"),						\n"
			+ "  END(\"blabla\").							\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public enum Enum {\n" +
		"  BEGIN(\"blabla\"),\n" +
		"  END(\"blabla\"),\n" +
		"  public Enum() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"public enum Enum {\n" +
		"  BEGIN(\"blabla\"),\n" +
		"  END(\"blabla\"),\n" +
		"  public Enum() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0017() {

	String s =
		"package a;											\n"
			+ "public enum X <T> {							\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public enum X<T> {\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public enum X<T> {\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"package a;\n" +
		"public enum X<T> {\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
}
