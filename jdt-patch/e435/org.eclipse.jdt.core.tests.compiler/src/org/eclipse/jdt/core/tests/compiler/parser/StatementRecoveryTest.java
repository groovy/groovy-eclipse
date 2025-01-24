/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class StatementRecoveryTest extends AbstractCompilerTest {
	public static final boolean ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY = false;

	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

static {
//	TESTS_NAMES = new String[] { "test0037"};
//	TESTS_RANGE = new int[] {10, 20};
}
public static Test suite() {
	return buildAllCompliancesTestSuite(StatementRecoveryTest.class);
}
public StatementRecoveryTest(String testName){
	super(testName);
}
public void checkParse(
	char[] source,
	String expectedDietUnitToString,
	String expectedDietWithStatementRecoveryUnitToString,
	String expectedDietPlusBodyUnitToString,
	String expectedDietPlusBodyWithStatementRecoveryUnitToString,
	String expectedFullUnitToString,
	String expectedFullWithStatementRecoveryUnitToString,
	String testName) {

	/* using regular parser in DIET mode */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		parser.setMethodsFullRecovery(false);
		parser.setStatementsRecovery(false);

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
	/* using regular parser in DIET mode and statementRecoveryEnabled */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
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
		if (!expectedDietWithStatementRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit diet structure with statement recovery enabled" + testName,
			expectedDietWithStatementRecoveryUnitToString,
			computedUnitToString);
	}
	/* using regular parser in DIET mode + getMethodBodies */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		parser.setMethodsFullRecovery(false);
		parser.setStatementsRecovery(false);

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
	/* using regular parser in DIET mode + getMethodBodies and statementRecoveryEnabled */
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
		if (!expectedDietWithStatementRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit diet structure" + testName,
			expectedDietWithStatementRecoveryUnitToString,
			computedUnitToString);
		if (computedUnit.types != null) {
			for (int i = 0, length = computedUnit.types.length; i < length; i++){
				computedUnit.types[i].parseMethods(parser, computedUnit);
			}
		}
		computedUnitToString = computedUnit.toString();
		if (!expectedDietPlusBodyWithStatementRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}

		assertEquals(
			"Invalid unit diet+body structure with statement recovery enabled" + testName,
			expectedDietPlusBodyWithStatementRecoveryUnitToString,
			computedUnitToString);
	}
	/* using regular parser in FULL mode */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		parser.setMethodsFullRecovery(false);
		parser.setStatementsRecovery(false);

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
	/* using regular parser in FULL mode and statementRecoveryEnabled */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
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
		if (!expectedFullWithStatementRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit full structure with statement recovery enabled" + testName,
			expectedFullWithStatementRecoveryUnitToString,
			computedUnitToString);

	}
}

public void test0001() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedFullUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0002() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #                    					\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0003() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    #                    					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0004() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #                    					\n"
			+ "    System.out.println();					\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0005() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    System.out.println();					\n"
			+ "    #                    					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0006() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    System.out.println();					\n"
			+ "    #                    					\n"
			+ "    System.out.println();					\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0007() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #                    					\n"
			+ "    System.out.println();					\n"
			+ "    if(true) {								\n"
			+ "      System.out.println();					\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    if (true)\n" +
		"        {\n" +
		"          System.out.println();\n" +
		"        }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0008() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    if(true) {								\n"
			+ "      System.out.println();					\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "    #                    					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    if (true)\n" +
		"        {\n" +
		"          System.out.println();\n" +
		"        }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0009() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    if(true) {								\n"
			+ "      System.out.println();					\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "    #                    					\n"
			+ "    System.out.println();					\n"
			+ "    if(true) {								\n"
			+ "      System.out.println();					\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    if (true)\n" +
		"        {\n" +
		"          System.out.println();\n" +
		"        }\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"    if (true)\n" +
		"        {\n" +
		"          System.out.println();\n" +
		"        }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0010() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {}							\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0011() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {}							\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "    #										\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0012() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {}							\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0013() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {}							\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    class Y {								\n"
			+ "      void foo() {}							\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"    class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0014() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "    #										\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0015() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0016() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0017() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "        #									\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0018() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {							\n"
			+ "        #									\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0019() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "        #									\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0020() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "        #									\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0021() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {}							\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0022() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {}							\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "    #										\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0023() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void bar() {}							\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void bar() {\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0024() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void bar() {}							\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void bar() {}							\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void bar() {\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void bar() {\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0025() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "    #										\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0026() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0027() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0028() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "        #									\n"
			+ "      }										\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0029() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {							\n"
			+ "        #									\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0030() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "        #									\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0031() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    new Object() {							\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "        #									\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    };										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"        System.out.println();\n" +
		"        if (true)\n" +
		"            {\n" +
		"              System.out.println();\n" +
		"            }\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    };\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0032() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    System.out.println();					\n"
			+ "    bar(new Object() {						\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "        #									\n"
			+ "        System.out.println();				\n"
			+ "        if(true) {							\n"
			+ "          System.out.println();				\n"
			+ "        }									\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    });										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    bar(new Object() {\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    if (true)\n" +
		"        {\n" +
		"          System.out.println();\n" +
		"        }\n" +
		"    System.out.println();\n" +
		"    System.out.println();\n" +
		"    if (true)\n" +
		"        {\n" +
		"          System.out.println();\n" +
		"        }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"});\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0033() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    class Z {								\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0034() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    new Object() {							\n"
			+ "      void foo() {							\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    };										\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new Object() {\n" +
		"      void foo() {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0035() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    bar(\\u0029								\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    bar();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0036() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    if(true) {								\n"
			+ "      foo();									\n"
			+ "    }										\n"
			+ "    for(;									\n"
			+ "    if(true) {								\n"
			+ "      foo();									\n"
			+ "    }										\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    if (true)\n" +
		"        {\n" +
		"          foo();\n" +
		"        }\n" +
		"    for (; ; ) \n" +
		"      ;\n" +
		"    if (true)\n" +
		"        {\n" +
		"          foo();\n" +
		"        }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0037() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    if() {									\n"
			+ "      foo();									\n"
			+ "    }										\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    if ($missing$)\n" +
		"        {\n" +
		"          foo();\n" +
		"        }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0038() {
	String s =
		"package p1;										\n"+
			"public class A {									\n"+
			"	public interface B {							\n"+
			"		public abstract void aMethod (int A);		\n"+
			"		public interface C {						\n"+
			"			public abstract void anotherMethod(int A);\n"+
			"		}											\n"+
			"	}												\n"+
			"	public class aClass implements B, B.C {			\n"+
			"		public void aMethod (int A) {				\n"+
			"			public void anotherMethod(int A) {};	\n"+
			"		}											\n"+
			"	}												\n"+
			"   	public static void main (String argv[]) {	\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}";

	String expectedDietUnitToString =
		"package p1;\n" +
		"public class A {\n" +
		"  public interface B {\n" +
		"    public interface C {\n" +
		"      public abstract void anotherMethod(int A);\n" +
		"    }\n" +
		"    public abstract void aMethod(int A);\n" +
		"  }\n" +
		"  public class aClass implements B, B.C {\n" +
		"    public aClass() {\n" +
		"    }\n" +
		"    public void aMethod(int A) {\n" +
		"    }\n" +
		"  }\n" +
		"  public A() {\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package p1;\n" +
		"public class A {\n" +
		"  public interface B {\n" +
		"    public interface C {\n" +
		"      public abstract void anotherMethod(int A);\n" +
		"    }\n" +
		"    public abstract void aMethod(int A);\n" +
		"  }\n" +
		"  public class aClass implements B, B.C {\n" +
		"    public aClass() {\n" +
		"      super();\n" +
		"    }\n" +
		"    public void aMethod(int A) {\n" +
		"    }\n" +
		"  }\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"    System.out.println(\"SUCCESS\");\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package p1;\n" +
		"public class A {\n" +
		"  public interface B {\n" +
		"    public interface C {\n" +
		"      public abstract void anotherMethod(int A);\n" +
		"    }\n" +
		"    public abstract void aMethod(int A);\n" +
		"  }\n" +
		"  public class aClass implements B, B.C {\n" +
		"    public aClass() {\n" +
		"      super();\n" +
		"    }\n" +
		"    public void aMethod(int A) {\n" +
		"      public void anotherMethod;\n" +
		(this.complianceLevel < ClassFileConstants.JDK14
		?
		"      int A;\n" +
		"      ;\n"
		:
		""
		) +
		"    }\n" +
		"  }\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"    System.out.println(\"SUCCESS\");\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"package p1;\n" +
		"public class A {\n" +
		"  public interface B {\n" +
		"    public interface C {\n" +
		"      public abstract void anotherMethod(int A);\n" +
		"    }\n" +
		"    public abstract void aMethod(int A);\n" +
		"  }\n" +
		"  public class aClass implements B, B.C {\n" +
		"    public aClass() {\n" +
		"    }\n" +
		"    public void aMethod(int A) {\n" +
		"    }\n" +
		"    public void anotherMethod(int A) {\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"  }\n" +
		"  public A() {\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullWithStatementRecoveryUnitToString =
		expectedFullUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0039() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  {											\n"
			+ "    System.out.println();					\n"
			+ "    foo()									\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  {\n" +
		"    System.out.println();\n" +
		"    foo();\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0040() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  {											\n"
			+ "    System.out.println();					\n"
			+ "    class Y {								\n"
			+ "      {										\n"
			+ "        System.out.println();				\n"
			+ "        foo()								\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  {\n" +
		"    System.out.println();\n" +
		"    class Y {\n" +
		"      {\n" +
		"        System.out.println();\n" +
		"        foo();\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0041() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  {											\n"
			+ "    System.out.println();					\n"
			+ "    class Y {								\n"
			+ "      {										\n"
			+ "        System.out.println();				\n"
			+ "        foo()								\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "    class Z {								\n"
			+ "      {										\n"
			+ "        System.out.println();				\n"
			+ "        foo()								\n"
			+ "        System.out.println();				\n"
			+ "      }										\n"
			+ "    }										\n"
			+ "    System.out.println();					\n"
			+ "    foo()									\n"
			+ "    System.out.println();					\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  {\n" +
		"    System.out.println();\n" +
		"    class Y {\n" +
		"      {\n" +
		"        System.out.println();\n" +
		"        foo();\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"    class Z {\n" +
		"      {\n" +
		"        System.out.println();\n" +
		"        foo();\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"    System.out.println();\n" +
		"    foo();\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0042() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    for(int i								\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    for (int i;; ; ) \n" +
		"      ;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test0043() {

	String s =
		"package a;											\n"
			+ "public interface Test {						\n"
			+ "  public void myMethod()						\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public interface Test {\n" +
		"  public void myMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public interface Test {\n" +
		"  public void myMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public interface Test {\n" +
		"  public void myMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173992
public void test0044() {

	String s =
		"import java.io.EOFException;\n" +
		"import java.io.FileNotFoundException;\n" +
		"import java.io.IOException;\n" +
		"import org.xml.sax.SAXException;\n" +
		"public class X {\n" +
    		"public void doSomething() throws FileNotFoundException, EOFException, SAXException{\n" +
    		"\n" +
    		"}\n" +
		"public void doSomethingElse() {\n" +
    		"try {\n" +
            	"	doSomething();\n" +
    		"}\n" +
   			" catch ( SAXException exception) {\n" +
		"\n" +
  			"}  \n" +
    		"catch ( FileNotFoundException exception ) {\n" +
		"\n" +
    		"}    \n" +
   			"catch (\n" +
            	"	// working before the slashes\n" +
    		") {\n" +
		"\n" +
    		"} \n" +
    		"} \n" +
    	"}\n";

	String expectedDietUnitToString =
		"import java.io.EOFException;\n" +
		"import java.io.FileNotFoundException;\n" +
		"import java.io.IOException;\n" +
		"import org.xml.sax.SAXException;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void doSomething() throws FileNotFoundException, EOFException, SAXException {\n" +
		"  }\n" +
		"  public void doSomethingElse() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"import java.io.EOFException;\n" +
		"import java.io.FileNotFoundException;\n" +
		"import java.io.IOException;\n" +
		"import org.xml.sax.SAXException;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void doSomething() throws FileNotFoundException, EOFException, SAXException {\n" +
		"  }\n" +
		"  public void doSomethingElse() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"import java.io.EOFException;\n" +
		"import java.io.FileNotFoundException;\n" +
		"import java.io.IOException;\n" +
		"import org.xml.sax.SAXException;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void doSomething() throws FileNotFoundException, EOFException, SAXException {\n" +
		"  }\n" +
		"  public void doSomethingElse() {\n" +
		"    try\n" +
		"      {\n" +
		"        doSomething();\n" +
		"      }\n" +
		"    catch (SAXException exception)\n" +
		"      {\n" +
		"      }\n" +
		"    catch (FileNotFoundException exception)\n" +
		"      {\n" +
		"      }\n" +
		"    catch ($missing$ $missing$)\n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=204662
public void test0045() {

	String s =
		"public class BadClass {\n" +
		"\n" +
		"   public void method(Object obj) {\n" +
		"\n" +
		"	  /*//this version compiles\n" +
		"	  People oPeople;\n" +
		"	  {oPeople= (People) obj;}//*/\n" +
		"\n" +
		"	  /*//this version fails, but the compiler errors are fine\n" +
		"      class People oPeople;\n" +
		"	  oPeople= (class People) obj;//*/\n" +
		"\n" +
		"	  //this version fails with internal compiler error\n" +
		"	  class People oPeople;\n" +
		"	  {oPeople= (class People) obj;}\n" +
		"   }\n" +
		"\n" +
		"}\n";

	String expectedDietUnitToString =
		"public class BadClass {\n" +
		"  public BadClass() {\n" +
		"  }\n" +
		"  public void method(Object obj) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class BadClass {\n" +
		"  public BadClass() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void method(Object obj) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class BadClass {\n" +
		"  public BadClass() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void method(Object obj) {\n" +
		"    class People {\n" +
		"      {\n" +
		"        class People {\n" +
		"          People() {\n" +
		"            super();\n" +
		"          }\n" +
		"        }\n" +
		"      }\n" +
		"      People() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=204662
public void test0046() {

	String s =
		"public class X {\n" +
		"	public void foo() { \n" +
		"		class Y ;\n" +
		"		\n" +
		"			{\n" +
		"				class Z ;\n" +
		"				}\n" +
		"	}\n" +
		"}\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    class Y {\n" +
		"      {\n" +
		"        class Z {\n" +
		"          Z() {\n" +
		"            super();\n" +
		"          }\n" +
		"        }\n" +
		"      }\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=204662
public void test0047() {

	String s =
		"public class X {\n" +
		"	public void foo() { \n" +
		"		class Y ;\n" +
		"		\n" +
		"			void bar() {\n" +
		"				class Z ;\n" +
		"				}\n" +
		"	}\n" +
		"}\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void bar() {\n" +
		"        class Z {\n" +
		"          Z() {\n" +
		"            super();\n" +
		"          }\n" +
		"        }\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void testBug430336() {

	String s =
		"package test1;\n" +
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"    void foo(Collection collection) {\n" +
		"        collection\n" +
		"    }\n" +
		"}\n";

	String expectedDietUnitToString =
		"package test1;\n" +
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"  public E() {\n" +
		"  }\n" +
		"  void foo(Collection collection) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"package test1;\n" +
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"  public E() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo(Collection collection) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package test1;\n" +
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"  public E() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo(Collection collection) {\n" +
		"    collection = $missing$;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
}
