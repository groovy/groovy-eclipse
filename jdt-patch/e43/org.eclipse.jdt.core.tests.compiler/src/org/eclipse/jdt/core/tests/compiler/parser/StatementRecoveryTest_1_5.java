/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;
import java.util.Map;

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

public class StatementRecoveryTest_1_5 extends AbstractCompilerTest {
	public static final boolean ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY = false;

	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

static {
//	TESTS_NAMES = new String[] { "test0037"};
//	TESTS_RANGE = new int[] {10, 20};
}
public static Test suite() {
	return buildAllCompliancesTestSuite(StatementRecoveryTest_1_5.class);
}
public StatementRecoveryTest_1_5(String testName){
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

protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	return options;
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=142793
public void test0001() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo(Collection c) {					\n"
			+ "    for(String s: c) {						\n"
			+ "      try {									\n"
			+ "        foo();		`						\n"
			+ "      }				`						\n"
			+ "    }										\n"
			+ "  }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(Collection c) {\n" +
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
		"  void foo(Collection c) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo(Collection c) {\n" +
		"    for (String s : c) \n" +
		"      {\n" +
		"        try\n" +
		"          {\n" +
		"            foo();\n" +
		"          }\n" +
		"        finally\n" +
		"          {\n" +
		"          }\n" +
		"      }\n" +
		"    ;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(Collection c) {\n" +
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=211180
public void test0002() {

	String s =
		"package a;											\n"
			+ "public class X {								\n"
			+ "  void foo() {								\n"
			+ "    #										\n"
			+ "    @MyAnnot(value=)							\n"
			+ "    int i;			`						\n"
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
		"    @MyAnnot(value = $missing$) int i;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0003() {

	String s =
		"package a;															\n"
			+ "public class X {												\n"
			+ "  void foo() {												\n"
			+ "    @AnAnnotation(name) #									\n"
			+ "    int var;													\n"
			+ "  }															\n"
			+ "}															\n";

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
		"    @AnAnnotation(name) int var;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0004() {

	String s =
		"package a;															\n"
			+ "public class X {												\n"
			+ "  void foo() {												\n"
			+ "    @AnAnnotation(name=)										\n"
			+ "    int var;													\n"
			+ "  }															\n"
			+ "}															\n";

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
		"    @AnAnnotation(name = $missing$) int var;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0005() {

	String s =
		"package a;															\n"
			+ "public class X {												\n"
			+ "  void foo() {												\n"
			+ "    foo1();													\n"
			+ "    @AnAnnotation(name) #									\n"
			+ "    class Y {}												\n"
			+ "    foo2();													\n"
			+ "  }															\n"
			+ "}															\n";

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
		"    @AnAnnotation(name) class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    foo1();\n" +
		"    @AnAnnotation(name) class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"    foo2();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0006() {

	String s =
		"package a;															\n"
			+ "public class X {												\n"
			+ "  void foo() {												\n"
			+ "    foo1();													\n"
			+ "    @AnAnnotation(name=)										\n"
			+ "    class Y {}												\n"
			+ "    foo2();													\n"
			+ "  }															\n"
			+ "}															\n";

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
		"    @AnAnnotation(name = $missing$) class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    foo1();\n" +
		"    @AnAnnotation(name = $missing$) class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"    foo2();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0007() {

	String s =
		"package a;															\n"
			+ "public class X {												\n"
			+ "  void foo() {												\n"
			+ "    foo1();													\n"
			+ "    final @AnAnnotation(name) #								\n"
			+ "    class Y {}												\n"
			+ "    foo2();													\n"
			+ "  }															\n"
			+ "}															\n";

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
		"    final @AnAnnotation(name) class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    foo1();\n" +
		"    final @AnAnnotation(name) class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"    foo2();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340691
// Verify that we don't get a recovered enum declaration when the error token is after an
// incorrectly used modifier
public void test0008() {
	String s =
		"public class Try {\n" + 
		"\n" + 
		"    void m() {\n" + 
		"\n" + 
		"        synchronized new Object();\n" + 
		"\n" + 
		"    }\n" +
		"}\n" + 
		"\n";

	String expectedDietUnitToString =
			"public class Try {\n" + 
			"  public Try() {\n" + 
			"  }\n" + 
			"  void m() {\n" + 
			"  }\n" + 
			"}\n";

		String expectedDietWithStatementRecoveryUnitToString =
			expectedDietUnitToString;

		String expectedDietPlusBodyUnitToString =
				"public class Try {\n" + 
				"  public Try() {\n" + 
				"    super();\n" + 
				"  }\n" + 
				"  void m() {\n" + 
				"  }\n" + 
				"}\n";

		String expectedDietPlusBodyWithStatementRecoveryUnitToString =
				"public class Try {\n" + 
				"  public Try() {\n" + 
				"    super();\n" + 
				"  }\n" + 
				"  void m() {\n" + 
				"  }\n" + 
				"}\n";

		String expectedFullUnitToString =
			expectedDietUnitToString;

		String expectedFullWithStatementRecoveryUnitToString =
			expectedFullUnitToString;

	String testName = "test";
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
