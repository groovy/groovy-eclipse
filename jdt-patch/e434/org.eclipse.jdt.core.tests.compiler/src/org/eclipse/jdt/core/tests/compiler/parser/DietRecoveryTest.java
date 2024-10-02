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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;

import junit.framework.Test;

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

public class DietRecoveryTest extends AbstractCompilerTest {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$
static {
//	TESTS_NUMBERS = new int[] { 75 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(DietRecoveryTest.class);
}

public DietRecoveryTest(String testName){
	super(testName);
}

public void checkParse(
	char[] source,
	String expectedDietUnitToString,
	String expectedDietPlusBodyUnitToString,
	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
	String expectedFullUnitToString,
	String expectedCompletionDietUnitToString, String testName) {

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
	/* using regular parser in DIET mode + getMethodBodies + statements recovery */
	{
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		parser.setMethodsFullRecovery(true);
		parser.setStatementsRecovery(true);

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
		if (!expectedDietPlusBodyPlusStatementsRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}

		assertEquals(
			"Invalid unit diet+body structure with statements recovery" + testName,
			expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
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
/*
 * Should treat variables 'h' and 'i' as fields since 'public'.
 */
public void test01() {

	String s =
		"package a;											\n"
			+ "import java.lang.*;							\n"
			+ "import java.util.*;							\n"
			+ "												\n"
			+ "public class X {								\n"
			+ "	void foo() {								\n"
			+ "		System.out.println();					\n"
			+ "												\n"
			+ "	public int h;								\n"
			+ "	public int[] i = { 0, 1 };					\n"
			+ "												\n"
			+ "	void bar(){									\n"
			+ "	void truc(){								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<promote local vars into fields>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out local type altogether
 */
public void test02() {

	String s =
		"package a;											\n"
			+ "import java.lang.*;							\n"
			+ "import java.util.*;							\n"
			+ "												\n"
			+ "public class X {								\n"
			+ "	void foo() {								\n"
			+ "		System.out.println();					\n"
			+ "												\n"
			+ "		class L {								\n"
			+ "			void baz(){}						\n"
			+ "		}										\n"
			+ "												\n"
			+ "	public int h;								\n"
			+ "	public int[] i = { 0, 1 };					\n"
			+ "												\n"
			+ "	void bar(){									\n"
			+ "	void truc(){								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class L {\n" +
		"      L() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void baz() {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<filter out local type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still be finding last method (#baz)
 */

public void test03() {

	String s =
		"package a;											\n"
			+ "import java.lang.*;							\n"
			+ "import java.util.*;							\n"
			+ "												\n"
			+ "public class X {								\n"
			+ "	void foo() {								\n"
			+ "		System.out.println();					\n"
			+ "												\n"
			+ "	public int h;								\n"
			+ "	public int[] i = { 0, 1 };					\n"
			+ "												\n"
			+ "	void bar(){									\n"
			+ "	void baz(){								\n"
			+ "	}											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<should find last method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should finding 5 fields.
 */

public void test04() {

	String s =
		"package a;											\n"
			+ "import java.lang.*;							\n"
			+ "import java.util.*;							\n"
			+ "												\n"
			+ "public class X {								\n"
			+ " X x;										\n"
			+ " Object a, b = null;							\n"
			+ "	void foo() {								\n"
			+ "		System.out.println();					\n"
			+ "												\n"
			+ "	public int h;								\n"
			+ "	public int[] i = { 0, 1 };					\n"
			+ "												\n"
			+ "	void bar(){									\n"
			+ "	void truc(){								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  X x;\n" +
		"  Object a;\n" +
		"  Object b = null;\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  X x;\n" +
		"  Object a;\n" +
		"  Object b = null;\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  X x;\n" +
		"  Object a;\n" +
		"  Object b;\n" +
		"  public int h;\n" +
		"  public int[] i;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<five fields>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Diet parse thinks it is successful - no recovery
 */

public void test05() {

	String s =
		"public class X {									\n"
			+ "	void foo() {								\n"
			+ "		System.out.println();					\n"
			+ " 	void baz(){}							\n"
			+ " }											\n"
			+ "												\n"
			+ "	void bar(){									\n"
			+ " }											\n"
			+ "	void truc(){								\n"
			+ " }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		(this.complianceLevel < ClassFileConstants.JDK14
		?
		"    new baz() {\n" +
		"    };\n"
		:
		"    void baz;\n"
		) +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<diet was successful>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Recovery will not restart from scratch, and miss some signatures (#baz())
 */

public void test06() {

	String s =
			"import java.lang.*;							\n"
			+ "												\n"
			+ "public class X {								\n"
			+ "	void foo() {								\n"
			+ "		System.out.println();					\n"
			+ " 	void baz(){}							\n"
			+ " }											\n"
			+ "												\n"
			+ "	void bar(){									\n"
			+ " }											\n"
			+ "	void truc(){								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"import java.lang.*;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"import java.lang.*;\n" +
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<will not miss nested method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Properly attaching fields/methods to member type
 */

public void test08() {

	String s =
			"public class X {								\n"
			+ " class Y {									\n"
			+ "	  void foo() {								\n"
			+ "	   System.out.println();					\n"
			+ "   }											\n"
			+ " public int h;								\n"
			+ " public int[] i = {0, 1};					\n"
			+ "	void bar(){									\n"
			+ "	void baz(){									\n"
			+ " }											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    public int h;\n" +
		"    public int[] i = {0, 1};\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"    void baz() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    public int h;\n" +
		"    public int[] i = {0, 1};\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      System.out.println();\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"    void baz() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    public int h;\n" +
		"    public int[] i;\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"    void baz() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<attaching to member type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Properly attaching fields/methods to enclosing type
 */

public void test09() {

	String s =
			"public class X {								\n"
			+ " class Y {									\n"
			+ "	  void foo() {								\n"
			+ "	   System.out.println();					\n"
			+ "   }											\n"
			+ " }											\n"
			+ " public int h;								\n"
			+ " public int[] i = {0, 1};					\n"
			+ "	void bar(){									\n"
			+ "	void baz(){									\n"
			+ " }											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      System.out.println();\n" +
		"    }\n" +
		"  }\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  public int h;\n" +
		"  public int[] i;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<attaching to enclosing type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Properly attaching fields/methods to member type in presence of missing
 * member type opening brace (Y) (and array initializer for (i)).
 */

public void test10() {

	String s =
			"public class X {								\n"
			+ " class Y 									\n"
			+ "	  void foo() {								\n"
			+ "	   System.out.println();					\n"
			+ "   }											\n"
			+ " public int h;								\n"
			+ " public int[] i = {0, 1};					\n"
			+ "	void bar(){									\n"
			+ "	void baz(){									\n"
			+ " }											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    public int h;\n" +
		"    public int[] i = {0, 1};\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"    void baz() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    public int h;\n" +
		"    public int[] i = {0, 1};\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      System.out.println();\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"    void baz() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    public int h;\n" +
		"    public int[] i;\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"    void baz() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<missing brace + array initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Attaching orphan methods and fields, by counting brackets
 * variable 'x' should be eliminated (looks like a local variable)
 */

public void test11() {

	String s =
			"public class X {								\n"
			+ "	void foo() {								\n"
			+ "		System.out.println();					\n"
			+ " }											\n"
			+ "}											\n"
			+ "	void bar(){									\n"
			+ "  int x;										\n"
			+ "	void baz(){									\n"
			+ " }											\n"
			+ " int y;										\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"    int x;\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<attaching orphans with missing brackets>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Attaching orphan methods and fields, by counting brackets
 * variable 'x' should NOT be eliminated given it looks like a field
 */

public void test12() {

	String s =
			"public class X {								\n"
			+ "	void foo() {								\n"
			+ "		System.out.println();					\n"
			+ " }											\n"
			+ "}											\n"
			+ "	void bar(){									\n"
			+ " public int x;								\n"
			+ "	void baz(){									\n"
			+ " }											\n"
			+ " int y;										\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<attaching orphans with missing brackets 2>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString		,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete type signature (missing superclass)
 */

public void test13() {

	String s =
			"public class X extends {						\n"
			+ "	void foo() {								\n"
			+ " }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<invalid type header>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete method signature (missing opening brace)
 */

public void test14() {

	String s =
			"public class X extends Thread {				\n"
			+ "	void foo() 									\n"
			+ "	void bar() 									\n"
			+ " }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public class X extends Thread {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X extends Thread {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<method header missing opening brace>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete method signature (missing thrown exceptions)
 */

public void test15() {

	String s =
			"public class X extends Thread {				\n"
			+ "	void foo() throws							\n"
			+ "	void bar() 									\n"
			+ " }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public class X extends Thread {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X extends Thread {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X extends Thread {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    ;\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<method header missing thrown exceptions>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete type signature (missing superinterfaces)
 */

public void test16() {

	String s =
			"public class X implements 						\n"
			+ "	void foo() 									\n"
			+ "	void bar() 									\n"
			+ " }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<type header missing superinterfaces>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete type signature (missing superinterfaces)
 */

public void test17() {

	String s =
			"public class X implements Y,					\n"
			+ "	void foo() 									\n"
			+ "	void bar() 									\n"
			+ " }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public class X implements Y {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X implements Y {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<type header missing superinterfaces 2>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find member type behind incomplete enclosing type header
 */

public void test18() {

	String s =
			"public class X implements 						\n"
			+ " class Y { 									\n"
			+ "	 void bar() 								\n"
			+ " }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<member type behind incomplete enclosing type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find member type when missing opening brace
 */

public void test19() {

	String s =
			"public class X 		 						\n"
			+ " class Y { 									\n"
			+ "	 void bar() 								\n"
			+ " }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n"+
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<member type when missing opening brace>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not find fieldX signature behind missing brace
 */

public void test20() {

	String s =
		"public class X 		 						\n"
		+ " fieldX;										\n"
		+ " class Y { 									\n"
		+ "	 void bar() 								\n"
		+ " }											\n"
		+ "}											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n"+
		"    }\n" +
		"    void bar() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<no field behind missing brace>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find Y as member type
 */

public void test21() {

	String s =
			"public class X 		 						\n"
			+ " fieldX;										\n"
			+ " class Y  									\n"
			+ " }											\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n"+
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find Y as member type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete local type
 */

public void test22() {

	String s =
		"package a;											\n" +
		"import java.lang.*;							\n" +
		"import java.util.*;							\n" +
		"												\n" +
		"public class X {								\n" +
		"	void foo() {								\n" +
		"		System.out.println();					\n" +
		"												\n" +
		"		class L extends {						\n" +
		"			public int l;						\n" +
		"			void baz(){}						\n" +
		"		}										\n" +
		"												\n" +
		"	public int h;								\n" +
		"												\n" +
		"	void bar(){									\n" +
		"	void truc(){								\n" +
		"}	\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class L {\n" +
		"      public int l;\n" +
		"      L() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void baz() {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete local type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete local type and method signature
 */

public void test23() {

	String s =
		"package a;											\n" +
		"import java.lang.*;							\n" +
		"import java.util.*;							\n" +
		"												\n" +
		"public class X {								\n" +
		"	void foo() {								\n" +
		"		System.out.println();					\n" +
		"												\n" +
		"		class L extends {						\n" +
		"			public int l;						\n" +
		"			void baz() throws {}				\n" +
		"		}										\n" +
		"												\n" +
		"	public int h;								\n" +
		"												\n" +
		"	void bar(){									\n" +
		"	void truc(){								\n" +
		"}	\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class L {\n" +
		"      public int l;\n" +
		"      L() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void baz() {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete local type/method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out anonymous type
 */

public void test24() {

	String s =
		"package a;											\n" +
		"import java.lang.*;							\n" +
		"import java.util.*;							\n" +
		"												\n" +
		"public class X {								\n" +
		"	void foo() {								\n" +
		"		System.out.println();					\n" +
		"												\n" +
		"		new X(){								\n" +
		"			void baz() {}						\n" +
		"		}.baz();								\n" +
		"												\n" +
		"	public int h;								\n" +
		"												\n" +
		"	void bar(){									\n" +
		"	void truc(){								\n" +
		"}	\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new X() {\n" +
		"  void baz() {\n" +
		"  }\n" +
		"}.baz();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete anonymous type/method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete anonymous type
 */

public void test25() {

	String s =
		"package a;											\n" +
		"import java.lang.*;							\n" +
		"import java.util.*;							\n" +
		"												\n" +
		"public class X {								\n" +
		"	void foo() {								\n" +
		"		System.out.println();					\n" +
		"												\n" +
		"		new X(){								\n" +
		"			void baz() {}						\n" +
		"												\n" +
		"		public int h;							\n" +
		"												\n" +
		"		void bar(){								\n" +
		"		void truc(){							\n" +
		"}	\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    new X() {\n" +
		"      public int h;\n" +
		"      void baz() {\n" +
		"      }\n" +
		"      void bar() {\n" +
		"      }\n" +
		"      void truc() {\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete anonymous type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete anonymous method
 */

public void test26() {

	String s =
		"package a;											\n" +
		"import java.lang.*;							\n" +
		"import java.util.*;							\n" +
		"												\n" +
		"public class X {								\n" +
		"	void foo() {								\n" +
		"		System.out.println();					\n" +
		"												\n" +
		"		new X(){								\n" +
		"			void baz() 							\n" +
		"	    }										\n" +
		"	}											\n" +
		"	public int h;								\n" +
		"												\n" +
		"	void bar(){									\n" +
		"	void truc(){								\n" +
		"}	\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete anonymous method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete local type and local var h
 */

public void test27() {

	String s =
		"package a;							\n"	+
		"import java.lang.*;				\n"	+
		"import java.util.*;				\n"	+
		"									\n"	+
		"public class X {					\n"	+
		"	void foo() {					\n"	+
		"		System.out.println();		\n"	+
		"									\n"	+
		"		class L extends {			\n"	+
		"			public int l;			\n"	+
		"			void baz(){}			\n"	+
		"		}							\n"	+
		"									\n"	+
		"		int h;						\n"	+
		"									\n"	+
		"	void bar(){						\n"	+
		"	void truc(){					\n"	+
		"}									\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"    class L {\n" +
		"      public int l;\n" +
		"      L() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void baz() {\n" +
		"      }\n" +
		"    }\n" +
		"    int h;\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter incomplete local type L and variable h>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find <y> as a field in Y
 */

public void test28() {

	String s =
		"public class X {		 	\n" +
		"  int x;			 		\n"	+
		"							\n" +
		"  int foo(){ }				\n" +
		"							\n" +
		"  class Y  {				\n"	+
		"    int y;					\n" +
		"}							\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    int y;\n" +
		"    Y() {\n" +
		"    }\n" +
		"  }\n" +
		"  int x;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    int y;\n" +
		"    Y() {\n" +
		"      super();\n"+
		"    }\n" +
		"  }\n" +
		"  int x;\n" +
		"  public X() {\n" +
		"    super();\n"+
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find <y> as a field in Y>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find <y> as a field in X
 */

public void test29() {

	String s =
		"public class X {		 	\n" +
		"  int x;			 		\n"	+
		"							\n" +
		"  int foo(){ }				\n" +
		"							\n" +
		"  class Y  {				\n"	+
		"}							\n" +
		"  int y;					\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"  }\n" +
		"  int x;\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  int x;\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find <y> as a field in X>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find <y> as a field in X
 */

public void test30() {

	String s =
		"public class X {		 	\n" +
		"  int x;			 		\n"	+
		"							\n" +
		"  int foo(){ }				\n" +
		"							\n" +
		"  class Y  				\n"	+
		"}							\n" +
		"  int y;					\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"  }\n" +
		"  int x;\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  int x;\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find <y> as a field in X>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should recover from partial method header foo()
 */

public void test31() {

	String s =
		"package a;								\n"+
		"import java.lang.*;					\n"+
		"import java.util.*;					\n"+
		"										\n"+
		"public class X {						\n"+
		"	void foo() 							\n"+
		"		System.out.println();			\n"+
		"										\n"+
		"	public int h;						\n"+
		"	public int[] i = { 0, 1 };			\n"+
		"										\n"+
		"	void bar(){							\n"+
		"	void truc(){						\n"+
		"}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i = {0, 1};\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"import java.lang.*;\n" +
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public int h;\n" +
		"  public int[] i;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<should recover from partial method header>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should recover from method with missing argument names
 */

public void _test32() {

	String s =
		"public class WB2 {											\n"+
		"	public void foo(java.util.Locale, java.util.Vector) {	\n"+
		"		int i;												\n"+
		"		if(i instanceof O) {								\n"+
		"		}													\n"+
		"		String s = \"hello\";								\n"+
		"		s.													\n"+
		"	}														\n"+
		"}															\n";

	String expectedDietUnitToString =
		"public class WB2 {\n" +
		"  public WB2() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class WB2 {\n" +
		"  public WB2() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"public class WB2 {\n" +
			"  public WB2() {\n" +
			"    super();\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    java.util.Locale.java.util.Vector $missing$;\n" +
			"  }\n" +
			"}\n";
	} else {
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			expectedDietPlusBodyUnitToString;
	}

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should recover from method with missing argument names>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not find message with no argument as a constructor
 */

public void test33() {

	String s =
		"public class X {				\n"+
		"	void hello()				\n"+
		"	public X(int i)				\n"+
		"	void foo() {				\n"+
		"		System.out.println();	\n"+
		"								\n"+
		"}								\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  void hello() {\n" +
		"  }\n" +
		"  public X(int i) {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  void hello() {\n" +
		"  }\n" +
		"  public X(int i) {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not find message with no argument as a constructor>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not find allocation as a constructor
 */

public void test34() {

	String s =
		"public class X {				\n"+
		"	void hello()				\n"+
		"	public X(int i)				\n"+
		"	static void foo() {			\n"+
		"		X x;					\n"+
		"		x = new X(23);			\n"+
		"		System.out.println();	\n"+
		"								\n"+
		"}								\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  void hello() {\n" +
		"  }\n" +
		"  public X(int i) {\n" +
		"  }\n" +
		"  static void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  void hello() {\n" +
		"  }\n" +
		"  public X(int i) {\n" +
		"    super();\n" +
		"  }\n" +
		"  static void foo() {\n" +
		"    X x;\n" +
		"    x = new X(23);\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not find allocation as a constructor>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Incomplete field header
 */

public void test35() {

	String s =
		"public class X {		\n" +
		"	int x				\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  int x;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  int x;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<incomplete field header>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Incomplete multiple field headers
 */

public void test36() {

	String s =
		"public class X {		\n" +
		"	int x, y			\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  int x;\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  int x;\n" +
		"  int y;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<incomplete multiple field headers>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Field header with started string initializer
 */

public void test37() {

	String s =
		"public class X {		\n" +
		"	String s = \"		\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  String s;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  String s;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<field header with started string initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Field header with started string initializer combined with incomplete superinterface
 */

public void test38() {

	String s =
		"public class X implements Y, {		\n" +
		"	String s = \"					\n";

	String expectedDietUnitToString =
		"public class X implements Y {\n" +
		"  String s;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X implements Y {\n" +
		"  String s;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<field header and incomplete superinterface>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Field signature behind keyword implements
 */

public void test39() {

	String s =
		"public class X implements 		\n"+
		"int x							\n"+
		"}								\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  int x;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  int x;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<field signature behind keyword implements>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Field type read as interface
 */

public void test40() {

	String s =
		"public class X implements Y, 		\n" +
		"	String s = \"					\n";

	String expectedDietUnitToString =
		"public class X implements Y, String {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X implements Y, String {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<field type read as interface>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Contiguous headers (checking checkpoint positions)
 */

public void test41() {

	String s =
		"public class X public int foo(int bar(static String s";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  static String s;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public int foo() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  static String s;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public int foo() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<contiguous headers>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Contiguous headers without comma (checking checkpoint positions)
 */

public void test42() {

	String s =
		"public class X public int foo(int x, int bar public String s;";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public String s;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public int foo(int x, int bar) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public String s;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public int foo(int x, int bar) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<contiguous headers without comma>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Contiguous headers without comma (checking checkpoint positions)
 */

public void test43() {

	String s =
		"public class X 			\n" +
		"	public int foo(			\n" +
		"	int bar(				\n" +
		" 	static String s, int x	\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  static String s;\n" +
		"  int x;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public int foo() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  static String s;\n" +
		"  int x;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public int foo() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<contiguous headers without comma>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find static field <x>
 */

public void test44() {

	String s =
		"class X {					\n" +
		"	String s;				\n" +
		"							\n" +
		"	public void foo(		\n" +
		"		static int x		\n" +
		"}							\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  String s;\n" +
		"  static int x;\n" +
		"  X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  String s;\n" +
		"  static int x;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find static field x>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Missing string literal quote inside method
 */

public void test45() {

	String s =
		"public class X {			\n"+
		"	int foo(){				\n"+
		"		String s = \"		\n"+
		"	}						\n"+
		"}							\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<missing string literal quote inside method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Detecting member type closing when missing brackets
 */

public void test46() {

	String s =
		"class X 					\n"+
		"  String s = \"class y 	\n"+
		"  class Member 			\n"+
		"	int foo() 				\n"+
		"        public int x;    	\n"+
		"  } 						\n"+
		" int bar() 				\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  class Member {\n" +
		"    public int x;\n" +
		"    Member() {\n" +
		"    }\n" +
		"    int foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  String s;\n" +
		"  X() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  class Member {\n" +
		"    public int x;\n" +
		"    Member() {\n" +
		"      super();\n" +
		"    }\n" +
		"    int foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  String s;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<detecting member type closing when missing brackets>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated method arguments
 */

public void test47() {

	String s =

		"class X {									\n" +
		"	int foo(AA a, BB b, IOEx				\n" +
		"											\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  int foo(AA a, BB b) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo(AA a, BB b) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated method arguments>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated literal string in method body
 */

public void test48() {

	String s =
		"public class X {							\n"+
		"	final static int foo(){ 				\n"+
		"		return \"1; 						\n"+
		"	} 										\n"+
		"	public static void main(String argv[]){ \n"+
		"		foo();								\n"+
		"	} 										\n"+
		"}											\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  static final int foo() {\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"  }\n" +
		"}\n";
	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  static final int foo() {\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"    foo();\n" +
		"  }\n" +
		"}\n";
	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated literal string in method body>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated initializer with local declaration
 */

public void test49() {

	String s =
		"public class X {							\n"+
		"	{										\n"+
		"     int x;								\n"+
		"	 										\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated initializer with local declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated if statement
 */

public void test50() {

	String s =
		"public class X {							\n"+
		"   int foo(){								\n"+
		"	  if(true){								\n"+
		"     	int x;								\n"+
		"	 										\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    if (true)\n" +
		"        {\n" +
		"          int x;\n" +
		"        }\n" +
		"    else\n" +
		"        ;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated if statement>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated nested block with local declaration
 */

public void test51() {

	String s =
		"public class X {							\n"+
		"   int foo(){								\n"+
		"	  {										\n"+
		"     	int x;								\n"+
		"	 										\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    {\n" +
		"      int x;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated nested block with local declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated nested block with field declaration
 */

public void test52() {

	String s =
		"public class X {							\n"+
		"   int foo(){								\n"+
		"	  {										\n"+
		"     	public int x;						\n"+
		"	 										\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public int x;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public int x;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated nested block with field declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated initializer with field declaration
 */

public void test53() {

	String s =
		"public class X {							\n"+
		"	{										\n"+
		"     public int x;							\n"+
		"	 										\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated initializer with field declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Invalid class name
 */

public void test54() {

	String s =
		"package p;								\n"+
		"public class ZPro.Sev.Blo {														\n"+
		"void ThisIsADummyMethodThatIsCreatedOnlyForThePurposesOfTheCompletionEngine() {	\n"+
		"	System.out.println(this.getClass());											\n"+
		"}																					\n"+
		"	// COMMENT																		\n"+
		"}																					\n";

	String expectedDietUnitToString =
		"package p;\n" +
		"public class ZPro {\n" +
		"  {\n" +
		"  }\n" +
		"  public ZPro() {\n" +
		"  }\n" +
		"  void ThisIsADummyMethodThatIsCreatedOnlyForThePurposesOfTheCompletionEngine() {\n" +
		"  }\n" +
		"}\n";
	String expectedDietPlusBodyUnitToString =
		"package p;\n" +
		"public class ZPro {\n" +
		"  {\n" +
		"  }\n" +
		"  public ZPro() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void ThisIsADummyMethodThatIsCreatedOnlyForThePurposesOfTheCompletionEngine() {\n" +
		"    System.out.println(this.getClass());\n" +
		"  }\n" +
		"}\n";
	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<Invalid class name>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated static initializer with field declaration
 */

public void test55() {

	String s =
		"public class X {							\n"+
		"	static {								\n"+
		"     public int x;							\n"+
		"	 										\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  static {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  static {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated static initializer with field declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Multiple initializers combined with array initializer
 */

public void test56() {

	String s =
		"public class X 				\n"+
		"	static int zz				\n"+
		"	{							\n"+
		"	}							\n"+
		"	static {					\n"+
		"   public int x;				\n"+
		"	int[] y = { 0, 1};			\n"+
		"	{							\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  static int zz;\n" +
		"  {\n" +
		"  }\n" +
		"  static {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  int[] y = {0, 1};\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  static int zz;\n" +
		"  {\n" +
		"  }\n" +
		"  static {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  int[] y = {0, 1};\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  static int zz;\n" +
		"  {\n" +
		"  }\n" +
		"  static {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  int[] y;\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<multiple initializers combined with array initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Combination of unterminated methods and fields
 */

public void test57() {

	String s =
		"class X						\n"+
		"	void foo(){					\n"+
		"		{						\n"+
		"	public static int x;		\n"+
		"	void bar()					\n"+
		"	}							\n"+
		"	int y;						\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  public static int x;\n" +
		"  int y;\n" +
		"  X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  public static int x;\n" +
		"  int y;\n" +
		"  X() {\n" +
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

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<combination of unterminated methods and fields>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Illegal unicode inside method body
 */

public void test58() {

	String s =
		"package p; \n"+
		"													\n"+
		"class A {											\n"+
		"	void bar() {									\n"+
		"		String s = \"\\u000D\";						\n"+
		"	}												\n"+
		"}													\n";

	String expectedDietUnitToString =
		"package p;\n" +
		"class A {\n" +
		"  A() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package p;\n" +
		"class A {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<illegal unicode inside method body>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Extra identifier in type signature
 */

public void test59() {

	String s =
		"public class X extends java.io.IOException IOException  {			\n" +
		"}																	\n";

	String expectedDietUnitToString =
		"public class X extends java.io.IOException {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X extends java.io.IOException {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<extra identifier in type signature>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Extra identifier in method signature
 */

public void test60() {

	String s =
		"public class X extends java.io.IOException  {		\n" +
		"	int foo() ExtraIdentifier {						\n" +
		"}													\n";

	String expectedDietUnitToString =
		"public class X extends java.io.IOException {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X extends java.io.IOException {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<extra identifier in method signature>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Extra identifier behind thrown exception
 */

public void test61() {

	String s =
		"public class X extends  {							\n" +
		"	int foo() throws IOException ExtraIdentifier {	\n" +
		"}													\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() throws IOException {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() throws IOException {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<extra identifier behind thrown exception>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated array initializer
 */

public void test62() {

	String s =
		"class X {				\n"+
		" class Y 				\n"+
		"   public String s;	\n"+
		"   int foo(){			\n"+
		"	return 1;			\n"+
		"   static int y = {;	\n"+ // can only be an initializer since type is not array one
		" }						\n"+
		" public int i = 0;		\n"+
		" 						\n"+
		" int baz()				\n"+
		"						\n"+
		"}						\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  class Y {\n" +
		"    public String s;\n" +
		"    static int y;\n" +
		"    public int i = 0;\n" +
		"    Y() {\n" +
		"    }\n" +
		"    <clinit>() {\n" +
		"    }\n" +
		"    int foo() {\n" +
		"    }\n" +
		"    int baz() {\n" +
		"    }\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  class Y {\n" +
		"    public String s;\n" +
		"    static int y;\n" +
		"    public int i = 0;\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"    <clinit>() {\n" +
		"    }\n" +
		"    int foo() {\n" +
		"      return 1;\n" +
		"    }\n" +
		"    int baz() {\n" +
		"    }\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"class X {\n" +
		"  class Y {\n" +
		"    public String s;\n" +
		"    static int y;\n" +
		"    public int i;\n" +
		"    Y() {\n" +
		"    }\n" +
		"    <clinit>() {\n" +
		"    }\n" +
		"    int foo() {\n" +
		"    }\n" +
		"    int baz() {\n" +
		"    }\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<unterminated array initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Initializer behind array initializer
 */

public void test63() {

	String s =
		"class X {				\n"+
		" int x[] = {0, 1}		\n"+
		" {						\n"+
		" }						\n"+
		"}						\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  int[] x = {0, 1};\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  int[] x = {0, 1};\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"class X {\n" +
		"  int[] x;\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<initializer behind array initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Initializers mixed with fields
 */

public void test64() {

	String s =
		"public class X 			\n"+
		"	int[] x = { 0, 1};		\n"+
		"	static int zz			\n"+
		"	{						\n"+
		"	}						\n"+
		"	static {				\n"+
		"    public int x;			\n"+
		"	{						\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  int[] x = {0, 1};\n" +
		"  static int zz;\n" +
		"  {\n" +
		"  }\n" +
		"  static {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  int[] x = {0, 1};\n" +
		"  static int zz;\n" +
		"  {\n" +
		"  }\n" +
		"  static {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  int[] x;\n" +
		"  static int zz;\n" +
		"  {\n" +
		"  }\n" +
		"  static {\n" +
		"  }\n" +
		"  public int x;\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<initializers mixed with fields>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find method behind some()
 */

public void test65() {

	String s =
		"import java.lang.*;													\n" +
		"																		\n" +
		"public class Hanoi {													\n" +
		"private    Post[] posts;												\n" +
		"public static void main (String args[]) {								\n" +
		"}																		\n" +
		"public void some(){													\n" +
		"																		\n" +
		"private void moveDisk (Post source, Post destination) {				\n" +
		"}																		\n" +
		"protected void reportMove (Post source, Post destination) {			\n" +
		"}																		\n" +
		"private void reset () {												\n" +
		"}																		\n" +
		"public void solve () {													\n" +
		"}																		\n" +
		"private void solve (int depth, Post start, Post free, Post end) {		\n" +
		"}																		\n" +
		"}																		\n";

	String expectedDietUnitToString =
		"import java.lang.*;\n" +
		"public class Hanoi {\n" +
		"  private Post[] posts;\n" +
		"  public Hanoi() {\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"  }\n" +
		"  public void some() {\n" +
		"  }\n" +
		"  private void moveDisk(Post source, Post destination) {\n" +
		"  }\n" +
		"  protected void reportMove(Post source, Post destination) {\n" +
		"  }\n" +
		"  private void reset() {\n" +
		"  }\n" +
		"  public void solve() {\n" +
		"  }\n" +
		"  private void solve(int depth, Post start, Post free, Post end) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"import java.lang.*;\n" +
		"public class Hanoi {\n" +
		"  private Post[] posts;\n" +
		"  public Hanoi() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"  }\n" +
		"  public void some() {\n" +
		"  }\n" +
		"  private void moveDisk(Post source, Post destination) {\n" +
		"  }\n" +
		"  protected void reportMove(Post source, Post destination) {\n" +
		"  }\n" +
		"  private void reset() {\n" +
		"  }\n" +
		"  public void solve() {\n" +
		"  }\n" +
		"  private void solve(int depth, Post start, Post free, Post end) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find method behind some()>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should detect X(int) as a method with no return type
 */

public void test66() {

	String s =
		"class X {			\n"+
		"	class Y {		\n"+
		"	X(int i){}		\n"+
		"}					\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"    X(int i) {\n" +
		"    }\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"    X(int i) {\n" +
		"    }\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should detect X(int) as a method with no return type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should detect orphan X(int) as a constructor
 */

public void test67() {

	String s =
		"class X {			\n"+
		"	class Y {		\n"+
		"	}				\n"+
		"}					\n"+
		"	X(int i){		\n"+
		"   }				\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"  }\n" +
		"  X(int i) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  class Y {\n" +
		"    Y() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  {\n" +
		"  }\n" +
		"  X(int i) {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should detect orphan X(int) as a constructor>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Empty unit
 */

public void test68() {

	String s = "";

	String expectedDietUnitToString = "";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<empty unit>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unit reduced to a method declaration
 */

/*
 * Unit reduced to a constructor declaration
 */

public void test70() {

	String s =
		"	X(){						\n" +
		"		System.out.println();	\n" +
		"	}							\n";

	String expectedDietUnitToString = "";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unit reduced to a constructor declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not pick-up any constructor with no arg
 */

public void test73() {

	String s =
		"	class X {			\n" +
		"		X(int i){}		\n" +
		"		int foo(){		\n" +
		"			new X(		\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  X(int i) {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  X(int i) {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"class X {\n" +
		"  X(int i) {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    new X();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not pick-up any constructor with no arg>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not detect any field
 */

public void test74() {

	String s =
		"package pack;						\n" +
		"									\n" +
		"class A extends IOException {		\n" +
		"									\n" +
		"	S{								\n" +
		"		int x;						\n" +
		"	}								\n" +
		"}									\n";

	String expectedDietUnitToString =
		"package pack;\n" +
		"class A extends IOException {\n" +
		"  {\n" +
		"  }\n" +
		"  A() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package pack;\n" +
		"class A extends IOException {\n" +
		"  {\n" +
		"    int x;\n" +
		"  }\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not detect any field>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Bunch of syntax errors
 */

public void test75() {

	String s =
		"package ZKentTest;\n"+
		"\n"+
		"import java.awt.color.*;\n"+
		"\n"+
		"public class A {\n"+
		"	A foo(int i) { return this; }\n"+
		"	int[] ii = {0, 1clone()\n"+
		"\n"+
		"	int bar() {\n"+
		"		class Local {\n"+
		"			int hello(){\n"+
		"				fo\n"+
		"			}\n"+
		"			int world()\n"+
		"			}\n"+
		"	void foo() {\n"+
		"		ba		\n";

	String expectedDietUnitToString =
		"package ZKentTest;\n" +
		"import java.awt.color.*;\n" +
		"public class A {\n" +
		"  int[] ii;\n" +
		"  public A() {\n" +
		"  }\n" +
		"  A foo(int i) {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package ZKentTest;\n" +
		"import java.awt.color.*;\n" +
		"public class A {\n" +
		"  int[] ii;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  A foo(int i) {\n" +
		"    return this;\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package ZKentTest;\n" +
		"import java.awt.color.*;\n" +
		"public class A {\n" +
		"  int[] ii;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  A foo(int i) {\n" +
		"    return this;\n" +
		"  }\n" +
		"  int bar() {\n" +
		"    class Local {\n" +
		"      Local() {\n" +
		"        super();\n" +
		"      }\n" +
		"      int hello() {\n" +
		"        fo = $missing$;\n" +
		"      }\n" +
		"      int world() {\n" +
		"      }\n" +
		"      void foo() {\n" +
		"      }\n" +
		"    }\n" +
		"    ba = $missing$;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package ZKentTest;\n" +
		"import java.awt.color.*;\n" +
		"public class A {\n" +
		"  int[] ii;\n" +
		"  public A() {\n" +
		"  }\n" +
		"  A foo(int i) {\n" +
		"  }\n" +
		"}\n" +
		"class Local {\n" +
		"  Local() {\n" +
		"  }\n" +
		"  int hello() {\n" +
		"  }\n" +
		"  int world() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<bunch of syntax errors>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find Member as a member type
 */

public void test76() {

	String s =
		"package pack;								\n"+
		"class A  {									\n"+
		"											\n"+
		"	public static void main(String[] argv)	\n"+
		"			new Member().f					\n"+
		"			;								\n"+
		"	}										\n"+
		"	class Member {							\n"+
		"		int foo()							\n"+
		"		}									\n"+
		"	}										\n"+
		"};											\n";

	String expectedDietUnitToString =
		"package pack;\n" +
		"class A {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"    }\n" +
		"    int foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  A() {\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package pack;\n" +
		"class A {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"      super();\n" +
		"    }\n" +
		"    int foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package pack;\n" +
		"class A {\n" +
		"  class Member {\n" +
		"    Member() {\n" +
		"      super();\n" +
		"    }\n" +
		"    int foo() {\n" +
		"    }\n" +
		"  }\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"    new Member().f = $missing$;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find Member as a member type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not recover duplicate field numberOfDisks
 */

public void test77() {

	String s =
		"package p;															\n"+
		"																		\n"+
		"import java.lang.*;													\n"+
		"																		\n"+
		"class IncompleteHanoi {												\n"+
		"private    Post[] posts;												\n"+
		"private    int numberOfDisks;											\n"+
		"																		\n"+
		"public Hanoi (int numberOfDisks) {										\n"+
		" this.numberOfDisks = numberOfDisks;									\n"+
		"'' this.posts = new Post[3];											\n"+
		" String[] postNames = new String[]{\"Left\", \"Middle\", \"Right\"};	\n"+
		"																		\n"+
		" for (int i = 0; i < 3; ++i)											\n"+
		"  this.posts[i] = new Post(postNames[i], numberOfDisks);				\n"+
		"}																		\n"+
		"																		\n"+
		"private void solve (int depth, Post start, Post free, Post end) {		\n"+
		" if (depth == 1)														\n"+
		"  moveDisk(start, end);												\n"+
		" else if (depth > 1) {													\n"+
		"  sol																	\n";

	String expectedDietUnitToString =
		"package p;\n" +
		"import java.lang.*;\n" +
		"class IncompleteHanoi {\n" +
		"  private Post[] posts;\n" +
		"  private int numberOfDisks;\n" +
		"  IncompleteHanoi() {\n" +
		"  }\n" +
		"  public Hanoi(int numberOfDisks) {\n" +
		"  }\n" +
		"  private void solve(int depth, Post start, Post free, Post end) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package p;\n" +
		"import java.lang.*;\n" +
		"class IncompleteHanoi {\n" +
		"  private Post[] posts;\n" +
		"  private int numberOfDisks;\n" +
		"  IncompleteHanoi() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public Hanoi(int numberOfDisks) {\n" +
		"  }\n" +
		"  private void solve(int depth, Post start, Post free, Post end) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package p;\n" +
		"import java.lang.*;\n" +
		"class IncompleteHanoi {\n" +
		"  private Post[] posts;\n" +
		"  private int numberOfDisks;\n" +
		"  IncompleteHanoi() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public Hanoi(int numberOfDisks) {\n" +
		"  }\n" +
		"  private void solve(int depth, Post start, Post free, Post end) {\n" +
		"    if ((depth == 1))\n" +
		"        moveDisk(start, end);\n" +
		"    else\n" +
		"        if ((depth > 1))\n" +
		"            {\n" +
		"              sol = $missing$;\n" +
		"            }\n" +
		"        else\n" +
		"            ;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not recover duplicate field numberOfDisks>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not detect a field v (1/2)
 */

public void test78() {

	String s =
		"class X {								\n" +
		"	int foo(){							\n" +
		"		Vector v = new Vector();		\n" +
		"		s								\n" +
		"		v.addElement(					\n" +
		"	}									\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    Vector v = new Vector();\n" +
		"    s v;\n" +
		"    addElement();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not detect a field v>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not detect a field v (2/2)
 */

public void test79() {

	String s =
		"class X {								\n" +
		"	int foo(){							\n" +
		"		Vector v = new Vector();		\n" +
		"		public s   v.addElement(		\n" +
		"	}									\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    Vector v = new Vector();\n" +
		"    public s v;\n" +
		"    addElement();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not detect a field v>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not detect a method bar
 */

public void test80() {

	String s =
		"class X {								\n" +
		"	int test(){							\n" +
		"		int[] i;						\n" +
		"		i								\n" +
		"		// some comment					\n" +
		"		bar(1);							\n" +
		"	}									\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"  int test() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int test() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int test() {\n" +
		"    int[] i;\n" +
		"    i bar = 1;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not detect a method bar>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not pick-up any constructor with no arg
 */

public void test81() {

	String s =
		"	class X {				\n" +
		"		X(int i){}			\n" +
		"		int foo(){			\n" +
		"			X(12)			\n";

	String expectedDietUnitToString =
		"class X {\n" +
		"  X(int i) {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  X(int i) {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"class X {\n" +
		"  X(int i) {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    X(12);\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not pick-up any constructor with no arg>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not promote message sending as a method
 */

public void test82() {

	String s =
		"public class A {		\n"+
		"						\n"+
		"	void foo() 			\n"+
		"		if (true) {		\n"+
		"		} else {		\n"+
		"			Bar s; 		\n"+
		"			s.fred();	\n"+
		"		}				\n"+
		"	}					\n"+
		"}						\n";
	String expectedDietUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    if (true)\n" +
		"        {\n" +
		"        }\n" +
		"    else\n" +
		"        {\n" +
		"          Bar s;\n" +
		"          s.fred();\n" +
		"        }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not promote message sending as a method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not promote message sending as a method 2
 */

public void test83() {

	String s =
		"public class A {			\n"+
		"							\n"+
		"	void foo() if (true) {	\n"+
		"		} else {			\n"+
		"			Bar s; 			\n"+
		"			s.fred();		\n"+
		"		}					\n"+
		"	}						\n"+
		"}							\n";
	String expectedDietUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    if (true)\n" +
		"        {\n" +
		"        }\n" +
		"    else\n" +
		"        {\n" +
		"          Bar s;\n" +
		"          s.fred();\n" +
		"        }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not promote message sending as a method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find a static initializer
 */

public void test84() {

	String s =
		"public class A extends			\n" +
		"								\n" +
		"	static {					\n" +
		"}								\n";
	String expectedDietUnitToString =
		"public class A {\n" +
		"  static {\n" +
		"  }\n" +
		"  public A() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" +
		"  static {\n" +
		"  }\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find a static initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find a static initializer
 */

public void test85() {

	String s =
		"public class A 			\n" +
		"							\n" +
		"	static {				\n" +
		"}							\n";
	String expectedDietUnitToString =
		"public class A {\n" +
		"  static {\n" +
		"  }\n" +
		"  public A() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" +
		"  static {\n" +
		"  }\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find a static initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find an initializer
 */

public void test86() {

	String s =
		"public class A 			\n" +
		"							\n" +
		"	int 					\n" +
		"	{						\n" +
		"}							\n";
	String expectedDietUnitToString =
		"public class A {\n" +
		"  {\n" +
		"  }\n" +
		"  public A() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" +
		"  {\n" +
		"  }\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find an initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find an initializer
 */

public void test87() {

	String s =
		"public class A 			\n" +
		"							\n" +
		"	int x;					\n" +
		"  {						\n" +
		"	int y;					\n" +
		"}							\n";
	String expectedDietUnitToString =
		"public class A {\n" +
		"  int x;\n" +
		"  {\n" +
		"  }\n" +
		"  public A() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" +
		"  int x;\n" +
		"  {\n" +
		"    int y;\n" +
		"  }\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find an initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FVRQG0: ITPCOM:WINNT - NullPointerException in recovery mode
 */

public void test88() {

	String s =
		"package p1;					\n" +
		"								\n" +
		"public class X {				\n" +
		"	int foo(String s, int x) 	\n" +
		"	public int y = new X() { 	\n" +
		"								\n" +
		"}								\n";

	String expectedDietUnitToString =
		"package p1;\n" +
		"public class X {\n" +
		"  public int y;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo(String s, int x) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package p1;\n" +
		"public class X {\n" +
		"  public int y;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo(String s, int x) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package p1;\n" +
		"public class X {\n" +
		"  public int y;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo(String s, int x) {\n" +
		"  }\n" +
		"}\n";

	String testName = "<1FVRQG0: ITPCOM:WINNT - NullPointerException in recovery mode>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FVRN9V: ITPJCORE:WIN98 - Internal builder error compiling servlet
 */

public void test89() {

	String s =
		"import javax.servlet.*;											\n" +
		"import javax.servlet.http.*;										\n" +
		"																	\n" +
		"public class Servlet1 extends HttpServlet {						\n" +
		"	protected (HttpServletRequest req, HttpServletResponse resp) {	\n" +
		"	}																\n" +
		"}																	\n";

	String expectedDietUnitToString =
		"import javax.servlet.*;\n" +
		"import javax.servlet.http.*;\n" +
		"public class Servlet1 extends HttpServlet {\n" +
		"  HttpServletRequest req;\n" +
		"  HttpServletRequest HttpServletResponse;\n" +
		"  {\n" +
		"  }\n" +
		"  public Servlet1() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"import javax.servlet.*;\n" +
		"import javax.servlet.http.*;\n" +
		"public class Servlet1 extends HttpServlet {\n" +
		"  HttpServletRequest req;\n" +
		"  HttpServletRequest HttpServletResponse;\n" +
		"  {\n" +
		"  }\n" +
		"  public Servlet1() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FVRN9V: ITPJCORE:WIN98 - Internal builder error compiling servlet>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FVXQZ4: ITPCOM:WIN98 - Walkback during parsing recovery
 */

public void test90() {

	String s =
		"public class Test {	\n"+
		"						\n"+
		"	int x;				\n"+
		"	int foo(			\n"+
		"	int bar(			\n"+
		"	baz(A a				\n"+
		"}						\n";
	String expectedDietUnitToString =
		"public class Test {\n" +
		"  int x;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"  baz(A a) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class Test {\n" +
		"  int x;\n" +
		"  public Test() {\n" +
		"    super();\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"  int bar() {\n" +
		"  }\n" +
		"  baz(A a) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FVXQZ4: ITPCOM:WIN98 - Walkback during parsing recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FVXWKI: ITPCOM:WIN98 - Walkback when parsing a bogus interface
 */

public void test91() {

	String s =
		"public interface Fred {		\n" +
		"	void foo();					\n" +
		"	void bar();					\n" +
		"	public fred(X x, int y);	\n" +
		"}								\n";
	String expectedDietUnitToString =
		"public interface Fred {\n" +
		"  void foo();\n" +
		"  void bar();\n" +
		"  public fred(X x, int y);\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FVXWKI: ITPCOM:WIN98 - Walkback when parsing a bogus interface>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Variation on 1FVXWKI: ITPCOM:WIN98 - Walkback when parsing a bogus interface
 */

public void test92() {
	String s =
		"public interface Test {		\n"+
		"	void foo();					\n"+
		"								\n"+
		"	public fred(Fred x, int y);	\n"+
		"}								\n";
	String expectedDietUnitToString =
		"public interface Test {\n" +
		"  void foo();\n" +
		"  public fred(Fred x, int y);\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FVXWKI: ITPCOM:WIN98 - Walkback when parsing a bogus interface>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FW5A4E: ITPCOM:WIN98 - Walkback reconciling
 */

public void test93() {
	String s =
		"class X{			\n" +
		"	int foo()		\n" +
		"	static { }		\n" +
		"}					\n";
	String expectedDietUnitToString =
		"class X {\n" +
		"  static {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  static {\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FW5A4E: ITPCOM:WIN98 - Walkback reconciling>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FW3663: ITPCOM:WIN98 - Outline - does not show method #fred()
 */

public void test94() {
	String s =
		"public class X {						\n" +
		"	int[] array;						\n" +
		"										\n" +
		"void foo() {							\n" +
		"	bar(this.array.length, 10, fred(	\n" +
		"										\n" +
		"int fred(								\n" +
		"}										\n";
	String expectedDietUnitToString =
		"public class X {\n" +
		"  int[] array;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  int fred() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  int[] array;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"  int fred() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  int[] array;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    bar(this.array.length, 10, fred());\n" +
		"  }\n" +
		"  int fred() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FW3663: ITPCOM:WIN98 - Outline - does not show method #fred()>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FW6M5M: ITPJUI:ALL - NPE in SourceElementParser
 */

public void test95() {
	String s =
		"public interface IP {			\n"+
		"	public static toString() {	\n"+
		"	}							\n"+
		"}								\n";
	String expectedDietUnitToString =
		"public interface IP {\n" +
		"  public static toString() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FW6M5M: ITPJUI:ALL - NPE in SourceElementParser>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FWHXX7: ITPCOM:WINNT - ClassCastException compiling invalid import
 */

public void test96() {
	String s =
		"import ;\n"+
		"class X {\n"+
		"	int foo(){\n"+
		"		System.out.println();\n"+
		"	}\n"+
		"	static {\n"+
		"		int i = j;\n"+
		"	}\n"+
		"}\n";
	String expectedDietUnitToString =
		"class X {\n" +
		"  static {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  static {\n" +
		"    int i = j;\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";
	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FWHXX7: ITPCOM:WINNT - ClassCastException compiling invalid import>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * variation on 1FWHXX7: ITPCOM:WINNT - ClassCastException compiling invalid import
 */

public void test97() {
	String s =
		"import ;\n"+
		"class X {\n"+
		"	int foo(){\n"+
		"		System.out.println();\n"+
		"	}\n"+
		"	static {\n"+
		"	}\n"+
		"}\n";
	String expectedDietUnitToString =
		"class X {\n" +
		"  static {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";
	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  static {\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";
	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<variation on 1FWHXX7: ITPCOM:WINNT - ClassCastException compiling invalid import>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=9084
 */

public void test98() {

	String s =
		"public class A {		                                                \n"+
		"	class Platform {		                                            \n"+
		"		public static void run(Runnable r) {		                    \n"+
		"		}		                                                        \n"+
		"	}			                                                        \n"+
		"	Object [] array = null;		                                        \n"+
		"	for (int nX = 0; nX < array.length; nX ++) {		                \n"+
		"		final String part = \"\";		                                \n"+
		"		final String sel = \"\";		                                \n"+
		"		Object l = null;		                                        \n"+
		"		if ((part != null && sel != null) || l instanceof String) {	\n"+
		"			Platform.run(new Runnable() {		                        \n"+
		"				public void run() {		                                \n"+
		"				}		                                                \n"+
		"				public void handleException(Throwable e) {		        \n"+
		"				}		                                                \n"+
		"			});		                                                    \n"+
		"		}		                                                        \n"+
		"	}		                                                            \n"+
		"}                                                                      \n";

	String expectedDietUnitToString =
		"public class A {\n"+
		"  class Platform {\n"+
		"    Platform() {\n"+
		"    }\n"+
		"    public static void run(Runnable r) {\n"+
		"    }\n"+
		"  }\n"+
		"  Object[] array = null;\n"+
		"  int nX = 0;\n"+
		"  {\n"+
		"  }\n"+
		"  public A() {\n"+
		"  }\n"+
		"}\n";


	String expectedDietPlusBodyUnitToString = "public class A {\n"+
		"  class Platform {\n"+
		"    Platform() {\n"+
		"      super();\n"+
		"    }\n"+
		"    public static void run(Runnable r) {\n"+
		"    }\n"+
		"  }\n"+
		"  Object[] array = null;\n"+
		"  int nX = 0;\n"+
		"  {\n"+
		"    final String part = \"\";\n"+
		"    final String sel = \"\";\n"+
		"    Object l = null;\n"+
    	"    if ((((part != null) && (sel != null)) || (l instanceof String)))\n"+
    	"        {\n"+
     	"          Platform.run(new Runnable() {\n"+
		"  public void run() {\n"+
		"  }\n"+
		"  public void handleException(Throwable e) {\n"+
		"  }\n"+
		"});\n"+
		"        }\n"+
		"  }\n"+
		"  public A() {\n"+
		"    super();\n"+
		"  }\n"+
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class A {\n"+
		"  class Platform {\n"+
		"    Platform() {\n"+
		"    }\n"+
		"    public static void run(Runnable r) {\n"+
		"    }\n"+
		"  }\n"+
		"  Object[] array;\n"+
		"  int nX;\n"+
		"  {\n"+
		"  }\n"+
		"  public A() {\n"+
		"  }\n"+
		"}\n";

	String testName = "<check for null inside RecoveredInitializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}

public void test99() {
	String s =
		"import ;\n"+
		"class X {\n"+
		"}\n"+
		"- public void addThreadFilter(IJavaThread thread) - restricts breakpoint to \n"+
		"given thread and any other previously specified threads\n"+
		"- public void removeThreadFilter(IJavaThread thread)- removes the given thread \n"+
		"restriction (will need to re-create breakpoint request as JDI does not support \n"+
		"the removal of thread filters)\n"+
		"- public IJavaThread[] getThreadFilters() - return the set of threads this \n"+
		"breakpoint is currently restricted to\n";
	String expectedDietUnitToString =
		"class X {\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"  public void addThreadFilter(IJavaThread thread) {\n" +
		"  }\n" +
		"  public void removeThreadFilter(IJavaThread thread) {\n" +
		"  }\n" +
		"  public IJavaThread[] getThreadFilters() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"class X {\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void addThreadFilter(IJavaThread thread) {\n" +
		"  }\n" +
		"  public void removeThreadFilter(IJavaThread thread) {\n" +
		"  }\n" +
		"  public IJavaThread[] getThreadFilters() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"class X {\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void addThreadFilter(IJavaThread thread) {\n" +
		"    restricts breakpoint;\n" +
		"    given thread;\n" +
		"    any other;\n" +
		"    specified = $missing$;\n" +
		"  }\n" +
		"  public void removeThreadFilter(IJavaThread thread) {\n" +
		"    removes the;\n" +
		"    thread restriction;\n" +
		"    will need = (re - create);\n" +
		"    request as;\n" +
		"    does not;\n" +
		"    the removal;\n" +
		"    thread = $missing$;\n" +
		"  }\n" +
		"  public IJavaThread[] getThreadFilters() {\n" +
		"    return the;\n" +
		"    of threads;\n" +
		"    breakpoint is;\n" +
		"    restricted to;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<9101 - Parse error while typing in Java editor>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test100() {
	String s =
		"public class Bug {\n" +
		"	static boolean bold = false;\n" +
		"public static void main(String arguments[]) {\n" +
		"	Shell shell = new Shell(SWT.MENU | SWT.RESIZE | SWT.TITLE | SWT.H_SCROLL);\n" +
		"	StyledText text = new StyledText(shell, SWT.WRAP); \n" +
		"	shell.addListener(SWT.Resize, new Listener() {\n" +
		"		public void handleEvent(Event e) {\n" +
		"			text.setBounds(shell.getClientArea());			 \n" +
		"		}  \n" +
		"	});	\n" +
		"	shell.addListener(SWT.KeyDown, bew Listener () {\n" +
		"		public void handleEvent(Event e) {\n" +
		"			bold = !bold;\n" +
		"		}\n" +
		"	}); \n" +
		"	text.addLineStyleListener(new LineStyleListener() { \n" +
		"		public void lineGetStyle(LineStyleEvent event) {\n" +
		"		}\n" +
		"	});\n" +
		"}\n" +
		"}\n";

	String expectedDietUnitToString =
		"public class Bug {\n" +
		"  static boolean bold = false;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public Bug() {\n" +
		"  }\n" +
		"  public static void main(String[] arguments) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class Bug {\n" +
		"  static boolean bold = false;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public Bug() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] arguments) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class Bug {\n" +
		"  static boolean bold = false;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public Bug() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] arguments) {\n" +
		"    Shell shell = new Shell((((SWT.MENU | SWT.RESIZE) | SWT.TITLE) | SWT.H_SCROLL));\n" +
		"    StyledText text = new StyledText(shell, SWT.WRAP);\n" +
		"    shell.addListener(SWT.Resize, new Listener() {\n" +
		"  public void handleEvent(Event e) {\n" +
		"    text.setBounds(shell.getClientArea());\n" +
		"  }\n" +
		"});\n" +
		"    shell.addListener(SWT.KeyDown, new Listener() {\n" +
		"  public void handleEvent(Event e) {\n" +
		"    bold = (! bold);\n" +
		"  }\n" +
		"});\n" +
		"    text.addLineStyleListener(new LineStyleListener() {\n" +
		"  public void lineGetStyle(LineStyleEvent event) {\n" +
		"  }\n" +
		"});\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"public class Bug {\n" +
		"  static boolean bold = false;\n" +
		"  public Bug() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public static void main(String[] arguments) {\n" +
		"  }\n" +
		"  bew Listener() {\n" +
		"  }\n" +
		"  public void handleEvent(Event e) {\n" +
		"  }\n" +
		"}\n";

	String expectedCompletionDietUnitToString =
		"public class Bug {\n" +
		"  static boolean bold;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public Bug() {\n" +
		"  }\n" +
		"  public static void main(String[] arguments) {\n" +
		"  }\n" +
		"}\n";

	String testName = "<10616 - local type outside method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void _test101() {
	String s =
		"public class X {	\n"+
		"    Object foo(Stack<X> s) {	\n"+
		"    }	\n"+
		"   List<T> bar(int pos, T x1, T x2, List<T> l) {	\n"+
		"    }	\n"+
		"}	\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  Object foo() {\n" +
		"  }\n" +
		"  bar(int pos, T x1, T x2) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  Object foo() {\n" +
		"  }\n" +
		"  bar(int pos, T x1, T x2) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<12387 out of memory with generics>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test102() {
	String s =
		"""
		void ___eval() {\t
		new Runnable(){\t
		void ___run() throws Throwable {\t
		return blah;\t
		}\t
		private String blarg;\t
		public void run (){\t
				class Local {\s
					void baz() {\t
					}\t
				}\t\s
		}\t
		}\t
		;}\t
		public class Hello{\t
		private static int x;\t
		private String blah;\t
		public static void main (String[] args){\t
		}\t
		public void hello (){\t
		}\t
		public boolean blah (){\t
		return false;}\t
		public void foo (){\t
		}\t
		}\t
		""";

	String expectedDietUnitToString = """
final class handlingtoplevelanonymoustest102 {
  public class Hello {
    private static int x;
    private String blah;
    <clinit>() {
    }
    public Hello() {
    }
    public static void main(String[] args) {
    }
    public void hello() {
    }
    public boolean blah() {
    }
    public void foo() {
    }
  }
  handlingtoplevelanonymoustest102() {
  }
  void ___eval() {
  }
}
""";

	String expectedDietPlusBodyUnitToString = """
final class handlingtoplevelanonymoustest102 {
  public class Hello {
    private static int x;
    private String blah;
    <clinit>() {
    }
    public Hello() {
      super();
    }
    public static void main(String[] args) {
    }
    public void hello() {
    }
    public boolean blah() {
      return false;
    }
    public void foo() {
    }
  }
  handlingtoplevelanonymoustest102() {
    super();
  }
  void ___eval() {
    new Runnable() {
      private String blarg;
      void ___run() throws Throwable {
        return blah;
      }
      public void run() {
        class Local {
          Local() {
            super();
          }
          void baz() {
          }
        }
      }
    };
  }
}
""";

	String expectedFullUnitToString = """
final class handlingtoplevelanonymoustest102 {
  public class Hello {
    private static int x;
    private String blah;
    <clinit>() {
    }
    public Hello() {
      super();
    }
    public static void main(String[] args) {
    }
    public void hello() {
    }
    public boolean blah() {
      return false;
    }
    public void foo() {
    }
  }
  handlingtoplevelanonymoustest102() {
  }
  void ___eval() {
    new Runnable() {
      private String blarg;
      void ___run() throws Throwable {
        return blah;
      }
      public void run() {
        class Local {
          Local() {
            super();
          }
          void baz() {
          }
        }
      }
    };
  }
}
""";

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "handlingtoplevelanonymoustest102";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test103() {
	String s =
		"public class X{	\n"+
		"   void foo(int x, int y, void z";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  void z;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(int x, int y) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  void z;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo(int x, int y) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<14038 - third argument type is void>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test104() {
	String s =
		"public class P#AField {\n" +
		"	public void setP#A(String P#A) {\n" +
		"		this.P#A = P#A;\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class P {\n" +
		"  {\n" +
		"  }\n" +
		"  public void setP;\n" +
		"  public P() {\n" +
		"  }\n" +
		"  A(String P) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class P {\n" +
		"  {\n" +
		"  }\n" +
		"  public void setP;\n" +
		"  public P() {\n" +
		"    super();\n" +
		"  }\n" +
		"  A(String P) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<16126>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test105() {
	String s =
		"public class X {\n" +
		"	static int foo(int[] a, int[] b) {\n" +
		"		return 0;\n" +
		"	}\n" +
		"	static int B =\n" +
		"		foo(\n" +
		"			new int[]{0, 0},\n" +
		"			new int[]{0, 0}\n" +
		"		);\n" +
		"	#\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  static int B = foo(new int[]{0, 0}, new int[]{0, 0});\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  static int foo(int[] a, int[] b) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  static int B = foo(new int[]{0, 0}, new int[]{0, 0});\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  static int foo(int[] a, int[] b) {\n" +
		"    return 0;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  static int B;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  static int foo(int[] a, int[] b) {\n" +
		"  }\n" +
		"}\n";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test106() {
	String s =
		"public class X {\n" +
		"  clon\n" +
		"  foo();\n" +
		"}\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  clon foo();\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  clon foo();\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  clon foo();\n" +
		"}\n";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test107() {
	String s =
		"public class X {\n" +
		"	int[] a = new int[]{0, 0}, b = new int[]{0, 0};\n" +
		"	#\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  int[] a = new int[]{0, 0};\n" +
		"  int[] b = new int[]{0, 0};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  int[] a = new int[]{0, 0};\n" +
		"  int[] b = new int[]{0, 0};\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  int[] a;\n" +
		"  int[] b;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test108() {
	String s =
		"public class X {\n" +
		"	int a = new int[]{0, 0}, b = new int[]{0, 0};\n" +
		"	#\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  int a = new int[]{0, 0};\n" +
		"  int b = new int[]{0, 0};\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  int a = new int[]{0, 0};\n" +
		"  int b = new int[]{0, 0};\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  int a;\n" +
		"  int b;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test109() {
	String s =
		"public class X {\n" +
		"	Object o = new Object() {\n" +
		"		void foo() {\n" +
		"			try {\n" +
		"			} catch(Exception e) {\n" +
		"				e.\n" +
		"			}\n" +
		"		}\n" +
		"	};\n" +
		"}";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  Object o;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  Object o;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  Object o;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test110() {
	String s =
		"public class X {\n" +
		"	void bar(){\n" +
		"		#\n" +
		"		class Inner {\n" +
		"			void foo() {\n" +
		"				try {\n" +
		"				} catch(Exception e) {\n" +
		"					e.\n" +
		"				}\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"    class Inner {\n" +
		"      Inner() {\n" +
		"        super();\n" +
		"      }\n" +
		"      void foo() {\n" +
		"        try\n" +
		"          {\n" +
		"          }\n" +
		"        catch (Exception e)\n" +
		"          {\n" +
		"          }\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}\n";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test111() {
	String s =
		"public class X {\n" +
		"	void bar(){\n" +
		"	}\n" +
		"	}\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100797
public void test112() {
	String s =
		"public class X {\n"+
		"  public void foo()\n"+
		"    try {			\n"+
		"    }  catch (Exception e) {\n"+
 		"     bar(\"blabla\");\n"+
		"      throw new Exception(prefix  \"bloblo\");\n"+
		"    }\n"+
		"  }\n"+
		"}\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    try\n" +
		"      {\n" +
		"      }\n" +
		"    catch (Exception e)\n" +
		"      {\n" +
		"        bar(\"blabla\");\n" +
		"        throw new Exception(prefix);\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111618
public void test113() {
	String s =
		"public class X {\n"+
		"  public void foo(Object[] tab)\n"+
		"    for (Object o : tab) {\n"+
		"		o.toString();\n"+
		"	 }\n"+
		"  }\n"+
		"}\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo(Object[] tab) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo(Object[] tab) {\n" +
		"    for (Object o : tab) \n" +
		"      {\n" +
		"        o.toString();\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129142
public void test114() {
	String s =
		"public class X {\n"+
		"  public void foo() {\n"+
		"    int int;\n"+
		"  }\n"+
		"}\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    int $missing$;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test115() {
	String s =
		"public interface Test {\n"+
		"  public void myMethod()\n"+
		"}\n";

	String expectedDietUnitToString =
		"public interface Test {\n" +
		"  public void myMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public interface Test {\n" +
		"  public void myMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public interface Test {\n" +
		"  public void myMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test116() {
	String s =
		"public interface Test {\n"+
		"  public void myMethod()\n"+
		"    System.out.println();\n"+
		"}\n";

	String expectedDietUnitToString =
		"public interface Test {\n" +
		"  public void myMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public interface Test {\n" +
		"  public void myMethod() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public interface Test {\n" +
		"  public void myMethod() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154811
public void test117() {
	String s =
		"public class X {\n" +
		"	void foo1() {\n" +
		"		class Y  {\n" +
		"		}\n" +
		"		void foo2() {\n" +
		"		}\n" +
		"		class Z<T> { \n" +
		"		}\n" +
		"	}\n" +
		"} \n";

	String expectedDietUnitToString = null;
	String expectedDietPlusBodyUnitToString = null;
	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString = null;
	String expectedFullUnitToString = null;
	String expectedCompletionDietUnitToString = null;


	if(this.complianceLevel <= ClassFileConstants.JDK1_4) {

		expectedDietUnitToString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo1() {\n" +
			"  }\n" +
			"}\n";

		expectedDietPlusBodyUnitToString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"  }\n" +
			"  void foo1() {\n" +
			"  }\n" +
			"}\n";

		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"  }\n" +
			"  void foo1() {\n" +
			"    class Y {\n" +
			"      Y() {\n" +
			"        super();\n" +
			"      }\n" +
			"    }\n" +
			"    class Z<T> {\n" +
			"      Z() {\n" +
			"        super();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n";

		expectedFullUnitToString =
			"public class X {\n" +
			"  class Z<T> {\n" +
			"    Z() {\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo1() {\n" +
			"  }\n" +
			"  void foo2() {\n" +
			"  }\n" +
			"}\n";

		expectedCompletionDietUnitToString =
			expectedDietUnitToString;
	} else if(this.complianceLevel >= ClassFileConstants.JDK1_5) {

		expectedDietUnitToString =
			"public class X {\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo1() {\n" +
			"  }\n" +
			"}\n";

		expectedDietPlusBodyUnitToString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"  }\n" +
			"  void foo1() {\n" +
			"  }\n" +
			"}\n";

		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"  }\n" +
			"  void foo1() {\n" +
			"    class Y {\n" +
			"      Y() {\n" +
			"        super();\n" +
			"      }\n" +
			"    }\n" +
			(this.complianceLevel < ClassFileConstants.JDK14
			?
			"    new foo2() {\n" +
			"    };\n" +
			"    class Z<T> {\n" +
			"      Z() {\n" +
			"        super();\n" +
			"      }\n" +
			"    }\n"
			:
			"    void foo2;\n"
			) +
			"  }\n" +
			"}\n";

		expectedFullUnitToString =
			"public class X {\n" +
			"  class Z<T> {\n" +
			"    Z() {\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"  }\n" +
			"  void foo1() {\n" +
			"  }\n" +
			"  void foo2() {\n" +
			"  }\n" +
			"}\n";

		expectedCompletionDietUnitToString =
			expectedDietUnitToString;
	}

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154811
public void test117_2() {
	String s =
		"public class X {\n" +
		"	void foo1() {\n" +
		"		class Y  {\n" +
		"		}\n" +
		"		void foo2() {\n" +
		"		}\n" +
		"		class Z { \n" +
		"		}\n" +
		"	}\n" +
		"} \n";

	String expectedDietUnitToString = null;
	String expectedDietPlusBodyUnitToString = null;
	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString = null;
	String expectedFullUnitToString = null;
	String expectedCompletionDietUnitToString = null;

	expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo1() {\n" +
		"  }\n" +
		"}\n";

	expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo1() {\n" +
		"  }\n" +
		"}\n";

	expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo1() {\n" +
		"    class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		(this.complianceLevel < ClassFileConstants.JDK14
		?
		"    new foo2() {\n" +
		"    };\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n"
		:
		"    void foo2;\n"
		) +
		"  }\n" +
		"}\n";

	expectedFullUnitToString =
		"public class X {\n" +
		"  class Z {\n" +
		"    Z() {\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo1() {\n" +
		"  }\n" +
		"  void foo2() {\n" +
		"  }\n" +
		"}\n";

	expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162056
public void test118() {
	String s =
		"interface Irrelevant {}\n"+
		"interface I {\n"+
		"	Object foo(Number n);\n"+
		"}\n"+
		"interface J extends I {\n"+
		"	String foo(Number n);\n"+
		"}\n"+
		"interface K {\n"+
		"	Object foo(Number n);\n"+
		"}\n"+
		"public class  {\n"+
		"	void foo() {\n"+
		"\n"+
		"	}\n"+
		"} \n";

	String expectedDietUnitToString =
		"interface Irrelevant {\n" +
		"}\n" +
		"interface I {\n" +
		"  Object foo(Number n);\n" +
		"}\n" +
		"interface J extends I {\n" +
		"  String foo(Number n);\n" +
		"}\n" +
		"interface K {\n" +
		"  Object foo(Number n);\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"interface Irrelevant {\n" +
		"}\n" +
		"interface I {\n" +
		"  Object foo(Number n);\n" +
		"}\n" +
		"interface J extends I {\n" +
		"  String foo(Number n);\n" +
		"}\n" +
		"interface K {\n" +
		"  Object foo(Number n);\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162056
public void test119() {
	String s =
		"interface Irrelevant {}\n"+
		"interface I {\n"+
		"	Object foo(Number n);\n"+
		"}\n"+
		"interface J extends I {\n"+
		"	String foo(Number n);\n"+
		"}\n"+
		"abstract class K {\n"+
		"	abstract Object foo(Number n);\n"+
		"}\n"+
		"public class  {\n"+
		"	void foo() {\n"+
		"\n"+
		"	}\n"+
		"} \n";

	String expectedDietUnitToString =
		"interface Irrelevant {\n" +
		"}\n" +
		"interface I {\n" +
		"  Object foo(Number n);\n" +
		"}\n" +
		"interface J extends I {\n" +
		"  String foo(Number n);\n" +
		"}\n" +
		"abstract class K {\n" +
		"  {\n" +
		"  }\n" +
		"  K() {\n" +
		"  }\n" +
		"  abstract Object foo(Number n);\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"interface Irrelevant {\n" +
		"}\n" +
		"interface I {\n" +
		"  Object foo(Number n);\n" +
		"}\n" +
		"interface J extends I {\n" +
		"  String foo(Number n);\n" +
		"}\n" +
		"abstract class K {\n" +
		"  {\n" +
		"  }\n" +
		"  K() {\n" +
		"    super();\n" +
		"  }\n" +
		"  abstract Object foo(Number n);\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140980
public void test120() {
	String s =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    try {\n" +
		"      System.out.println(); \n" +
		"    } catch (Exception e) {\n" +
		"    }\n" +
		"    class Z {}\n" +
		" }\n" +
		"}\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    try\n" +
		"      {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    catch (Exception e)\n" +
		"      {\n" +
		"      }\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140980
public void test121() {
	String s =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    try {\n" +
		"      System.out.println(); \n" +
		"    } catch (Exception e) {\n" +
		"      class Z {}\n" +
		"    }\n" +
		" }\n" +
		"}\n";

	String expectedDietUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    try\n" +
		"      {\n" +
		"        System.out.println();\n" +
		"      }\n" +
		"    catch (Exception e)\n" +
		"      {\n" +
		"        class Z {\n" +
		"          Z() {\n" +
		"            super();\n" +
		"          }\n" +
		"        }\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140980
public void test122() {
	String s =
		"public class Test\n" +
		"{\n" +
		"  public void func1()\n" +
		"  {\n" +
		"    try\n" +
		"    {\n" +
		"    catch ( Exception exception)\n" +
		"    {\n" +
		"      exception.printStackTrace();\n" +
		"    }\n" +
		"  }\n" +
		"\n" +
		"  class Clazz\n" +
		"  {\n" +
		"  }\n" +
		"}\n" +
		"\n";

	String expectedDietUnitToString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public void func1() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void func1() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void func1() {\n" +
		"    try\n" +
		"      {\n" +
		"      }\n" +
		"    catch (Exception exception)\n" +
		"      {\n" +
		"        exception.printStackTrace();\n" +
		"      }\n" +
		"    class Clazz {\n" +
		"      Clazz() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140980
public void test123() {
	String s =
		"public class SwitchBug {\n" +
		"       void aMethod() {\n" +
		"               int i=0;\n" +
		"               try {\n" +
		"                        switch( i ) {\n" +
		"                } catch( Exception ex ) {\n" +
		"                }\n" +
		"        }\n" +
		"        class Nested {\n" +
		"        }\n" +
		"}\n";

	String expectedDietUnitToString =
		"public class SwitchBug {\n" +
		"  public SwitchBug() {\n" +
		"  }\n" +
		"  void aMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class SwitchBug {\n" +
		"  public SwitchBug() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void aMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class SwitchBug {\n" +
		"  public SwitchBug() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void aMethod() {\n" +
		"    int i = 0;\n" +
		"    try\n" +
		"      {\n" +
		"        switch (i) {\n" +
		"        }\n" +
		"      }\n" +
		"    catch (Exception ex)\n" +
		"      {\n" +
		"      }\n" +
		"    class Nested {\n" +
		"      Nested() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=157570
public void _test124() {
	String s =
		"public class Test {\n" +
		"	void aMethod() {\n" +
		"		public static void m1()\n" +
		"		{\n" +
		"			int a;\n" +
		"			int b;\n" +
		"		}\n" +
		"		public static void m2()\n" +
		"		{\n" +
		"			int c;\n" +
		"			int d;\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	String expectedDietUnitToString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void aMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void aMethod() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString = null;
	if(this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"    super();\n" +
			"  }\n" +
			"  void aMethod() {\n" +
			"    m1();\n" +
			"    {\n" +
			"      int a;\n" +
			"      int b;\n" +
			"    }\n" +
			"    m2();\n" +
			"    {\n" +
			"      int c;\n" +
			"      int d;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	} else {
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"public class Test {\n" +
			"  public Test() {\n" +
			"    super();\n" +
			"  }\n" +
			"  void aMethod() {\n" +
			"  }\n" +
			"}\n";
	}

	String expectedFullUnitToString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  void aMethod() {\n" +
		"  }\n" +
		"  public static void m1() {\n" +
		"  }\n" +
		"  public static void m2() {\n" +
		"  }\n" +
		"}\n";

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=271680
public void test125() {
	String s =
		"public class Test {\n" +
		"}\n";

	StringBuilder buf = new StringBuilder();
	for (int i = 0; i < 1000; i++) {
		buf.append("class AClass #\n");
	}
	s+= buf.toString();

	// expectedDietUnitToString
	String expectedDietUnitToString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";
	buf = new StringBuilder();
	int max = 256;
	for (int i = 0; i < max; i++) {
		String indent = "";
		for (int j = 0; j < i; j++) {
			indent+= "  ";
		}
		buf.append(indent).append("class AClass {\n");
	}
	for (int i = max - 1; i >= 0; i--) {
		String indent = "";
		for (int j = 0; j < i; j++) {
			indent+= "  ";
		}
		buf.append(indent).append("  AClass() {\n");
		buf.append(indent).append("  }\n");
		buf.append(indent).append("}\n");
	}

	expectedDietUnitToString += buf.toString();

	// expectedDietPlusBodyUnitToString
	String expectedDietPlusBodyUnitToString =
		"public class Test {\n" +
		"  public Test() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	buf = new StringBuilder();
	for (int i = 0; i < max; i++) {
		String indent = "";
		for (int j = 0; j < i; j++) {
			indent+= "  ";
		}
		buf.append(indent).append("class AClass {\n");
	}
	for (int i = max - 1; i >= 0; i--) {
		String indent = "";
		for (int j = 0; j < i; j++) {
			indent+= "  ";
		}
		buf.append(indent).append("  AClass() {\n");
		buf.append(indent).append("    super();\n");
		buf.append(indent).append("  }\n");
		buf.append(indent).append("}\n");
	}
	expectedDietPlusBodyUnitToString += buf.toString();

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132679
public void test126() {
	String s =
		"package p;\n" +
		"public class ContextTest {\n" +
		"  private Context context = new Context();\n" +
		"  public void test() {\n" +
		"      context.new Callback() {\n" +
		"      public void doit(int value) {\n" +
		"       #\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	String expectedDietUnitToString =
		"package p;\n" +
		"public class ContextTest {\n" +
		"  private Context context = new Context();\n" +
		"  public ContextTest() {\n" +
		"  }\n" +
		"  public void test() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package p;\n" +
		"public class ContextTest {\n" +
		"  private Context context = new Context();\n" +
		"  public ContextTest() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"package p;\n" +
		"public class ContextTest {\n" +
		"  private Context context = new Context();\n" +
		"  public ContextTest() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test() {\n" +
		"    context.new Callback() {\n" +
		"      public void doit(int value) {\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package p;\n" +
		"public class ContextTest {\n" +
		"  private Context context;\n" +
		"  public ContextTest() {\n" +
		"  }\n" +
		"  public void test() {\n" +
		"  }\n" +
		"}\n";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201762
public void test127() {
	String s =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"\n" +
		"public class Try {\n" +
		"\n" +
		"    void main(Shell shell) {\n" +
		"\n" +
		"        final Label label= new Label(shell, SWT.WRAP);\n" +
		"        label.addPaintListener(new PaintListener() {\n" +
		"            public void paintControl(PaintEvent e) {\n" +
		"                e.gc.setLineCap(SWT.CAP_); // content assist after CAP_\n" +
		"            }\n" +
		"        });\n" +
		"\n" +
		"        shell.addControlListener(new ControlAdapter() { });\n" +
		"\n" +
		"        while (!shell.isDisposed()) { }\n" +
		"    }\n" +
		"}\n" +
		"\n";

	String expectedDietUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"    final Label label = new Label(shell, SWT.WRAP);\n" +
		"    label.addPaintListener(new PaintListener() {\n" +
		"  public void paintControl(PaintEvent e) {\n" +
		"    e.gc.setLineCap(SWT.CAP_);\n" +
		"  }\n" +
		"});\n" +
		"    shell.addControlListener(new ControlAdapter() {\n" +
		"});\n" +
		"    while ((! shell.isDisposed()))      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"    final Label label = new Label(shell, SWT.WRAP);\n" +
		"    label.addPaintListener(new PaintListener() {\n" +
		"  public void paintControl(PaintEvent e) {\n" +
		"    e.gc.setLineCap(SWT.CAP_);\n" +
		"  }\n" +
		"});\n" +
		"    shell.addControlListener(new ControlAdapter() {\n" +
		"});\n" +
		"    while ((! shell.isDisposed()))      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"    final Label label = new Label(shell, SWT.WRAP);\n" +
		"    label.addPaintListener(new PaintListener() {\n" +
		"  public void paintControl(PaintEvent e) {\n" +
		"    e.gc.setLineCap(SWT.CAP_);\n" +
		"  }\n" +
		"});\n" +
		"    shell.addControlListener(new ControlAdapter() {\n" +
		"});\n" +
		"    while ((! shell.isDisposed()))      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	String expectedCompletionDietUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"  }\n" +
		"}\n";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201762
public void test128() {
	String s =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"\n" +
		"public class Try {\n" +
		"\n" +
		"    void main(Shell shell) {\n" +
		"\n" +
		"        final Label label= new Label(shell, SWT.WRAP);\n" +
		"        label.addPaintListener(new PaintListener() {\n" +
		"            public void paintControl(PaintEvent e) {\n" +
		"                e.gc.setLineCap(SWT.CAP_#); // content assist after CAP_\n" +
		"            }\n" +
		"        });\n" +
		"\n" +
		"        shell.addControlListener(new ControlAdapter() { });\n" +
		"\n" +
		"        while (!shell.isDisposed()) { }\n" +
		"    }\n" +
		"}\n" +
		"\n";

	String expectedDietUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"    final Label label = new Label(shell, SWT.WRAP);\n" +
		"    label.addPaintListener(new PaintListener() {\n" +
		"  public void paintControl(PaintEvent e) {\n" +
		"    e.gc.setLineCap(SWT.CAP_);\n" +
		"  }\n" +
		"});\n" +
		"    shell.addControlListener(new ControlAdapter() {\n" +
		"});\n" +
		"    while ((! shell.isDisposed()))      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"  }\n" +
		"}\n";

	String expectedCompletionDietUnitToString =
		"import org.eclipse.swt.*;\n" +
		"import org.eclipse.swt.events.*;\n" +
		"import org.eclipse.swt.widgets.*;\n" +
		"public class Try {\n" +
		"  public Try() {\n" +
		"  }\n" +
		"  void main(Shell shell) {\n" +
		"  }\n" +
		"}\n";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405778 - [1.8][dom ast] method body recovery broken (empty body)
public void test405778() {
		String s =
			"import java.util.Collection;\n" +
			"public class E {\n" +
			"    public void __test1() {\n" +
			"        Object o = new Object();\n" +
			"        if (o.hashCode() != 0) {\n" +
			"           o.\n" +
			"         \n" +
			"        }\n" +
			"     }\n" +
			"}" +
			"\n";

		String expectedDietUnitToString =
			"import java.util.Collection;\n" +
			"public class E {\n" +
			"  public E() {\n" +
			"  }\n" +
			"  public void __test1() {\n" +
			"  }\n" +
			"}\n";

		String expectedDietPlusBodyUnitToString =
			"import java.util.Collection;\n" +
			"public class E {\n" +
			"  public E() {\n" +
			"    super();\n" +
			"  }\n" +
			"  public void __test1() {\n" +
			"  }\n" +
			"}\n";

		String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"import java.util.Collection;\n" +
			"public class E {\n" +
			"  public E() {\n" +
			"    super();\n" +
			"  }\n" +
			"  public void __test1() {\n" +
			"    Object o = new Object();\n" +
			"    if ((o.hashCode() != 0))\n" +
			"        {\n" +
			"          o = $missing$;\n" +
			"        }\n" +
			"  }\n" +
			"}\n";

		String expectedFullUnitToString =
			"import java.util.Collection;\n" +
			"public class E {\n" +
			"  public E() {\n" +
			"  }\n" +
			"  public void __test1() {\n" +
			"  }\n" +
			"}\n";

		String expectedCompletionDietUnitToString =
			"import java.util.Collection;\n" +
			"public class E {\n" +
			"  public E() {\n" +
			"  }\n" +
			"  public void __test1() {\n" +
			"  }\n" +
			"}\n";

		String testName = "test";
		checkParse(
			s.toCharArray(),
			expectedDietUnitToString,
			expectedDietPlusBodyUnitToString,
			expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
			expectedFullUnitToString,
			expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405778 - [1.8][dom ast] method body recovery broken (empty body)
public void test405778a() {
	String s =
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"    void m(String[] names) {\n"
			+ "/*[*/\n"
			+ "for (String string : names) {\n"
			+ "System.out.println(string.);\n"
			+ "}\n"
			+ "/*]*/\n"
			+ "}\n"
			+ "}\n" +
		"\n";

	String expectedDietUnitToString =
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"  public E() {\n" +
		"  }\n" +
		"  void m(String[] names) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"  public E() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void m(String[] names) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"  public E() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void m(String[] names) {\n" +
		"    for (String string : names) \n" +
		"      {\n" +
		"        System.out.println(string.class);\n" +
		"      }\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"  public E() {\n" +
		"  }\n" +
		"  void m(String[] names) {\n" +
		"  }\n" +
		"}\n";

	String expectedCompletionDietUnitToString =
		"import java.util.Collection;\n" +
		"public class E {\n" +
		"  public E() {\n" +
		"  }\n" +
		"  void m(String[] names) {\n" +
		"  }\n" +
		"}\n";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=456861 - [recovery] NPE in RecoveryScanner since Mars M4
public void test456861() {
	String s =
		"import java.awt.Point;\n" +
		"public class Test {\n" +
		"	public void foo(Point p, int[] a) {\n" +
		"		String s1 = \"\";\n" +
		"		s.;\n" +
		"	}\n" +
		" }";

	String expectedDietUnitToString =
		"import java.awt.Point;\n" +
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public void foo(Point p, int[] a) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"import java.awt.Point;\n" +
		"public class Test {\n" +
		"  public Test() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo(Point p, int[] a) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"import java.awt.Point;\n" +
		"public class Test {\n" +
		"  public Test() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo(Point p, int[] a) {\n" +
		"    String s1 = \"\";\n" +
		"    s = $missing$;\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"import java.awt.Point;\n" +
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public void foo(Point p, int[] a) {\n" +
		"  }\n" +
		"}\n";

	String expectedCompletionDietUnitToString =
		"import java.awt.Point;\n" +
		"public class Test {\n" +
		"  public Test() {\n" +
		"  }\n" +
		"  public void foo(Point p, int[] a) {\n" +
		"  }\n" +
		"}\n";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
}
