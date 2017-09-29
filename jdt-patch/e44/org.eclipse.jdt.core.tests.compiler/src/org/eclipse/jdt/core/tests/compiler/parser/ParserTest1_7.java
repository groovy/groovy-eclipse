/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class ParserTest1_7 extends AbstractCompilerTest {
	public static final boolean ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY = false;

static {
//	TESTS_NAMES = new String[] { "test0037"};
//	TESTS_RANGE = new int[] {10, 20};
//	TESTS_NUMBERS = new int[] { 10 };
}
public static Class testClass() {
	return ParserTest1_7.class;
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
public ParserTest1_7(String testName){
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
				true);
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
				true);

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
				true);
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
				true);

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
	{
		/* using regular parser in FULL mode */
		if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
			Parser parser =
				new Parser(
					new ProblemReporter(
						DefaultErrorHandlingPolicies.proceedWithAllProblems(),
						new CompilerOptions(getCompilerOptions()),
						new DefaultProblemFactory(Locale.getDefault())),
					true);
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
	}
	/* using regular parser in FULL mode and statementRecoveryEnabled */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				true);

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
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
	return options;
}
public void test0001() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		try (Reader reader = new FileReader(\"fileName\")) {\n" +
		"			System.out.println(reader.read());\n" +
		"		} catch(FileNotFoundException | IOException | Exception e) {\n" +
		"			e.printStackTrace();\n" +
		"		} finally {\n" +
		"			System.out.println(\"Finishing try-with-resources\");\n" +
		"		}\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException | IOException | Exception e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException | IOException | Exception e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException | IOException | Exception e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
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
public void test0002() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		try (Reader reader = new FileReader(\"fileName\")) {\n" +
		"			System.out.println(reader.read());\n" +
		"		} catch(FileNotFoundException e) {\n" +
		"			e.printStackTrace();\n" +
		"		} finally {\n" +
		"			System.out.println(\"Finishing try-with-resources\");\n" +
		"		}\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
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
public void test0003() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		try (Reader reader = new FileReader(\"fileName\")) {\n" +
		"			System.out.println(reader.read());\n" +
		"		}\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" +
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
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
public void test0004() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		try (Reader reader = new FileReader(\"fileName\")) {\n" +
		"			System.out.println(reader.read());\n" +
		"		} finally {\n" +
		"			System.out.println(\"Finishing try-with-resources\");\n" +
		"		}\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
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
public void test0005() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		try (Reader reader = new FileReader(\"fileName\")) {\n" +
		"			System.out.println(reader.read());\n" +
		"		} catch(FileNotFoundException e) {\n" +
		"			e.printStackTrace();\n" +
		"		}\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
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
public void test0006() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		try {\n" +
		"			System.out.println(reader.read());\n" +
		"		} catch(FileNotFoundException | IOException | Exception e) {\n" +
		"			e.printStackTrace();\n" +
		"		} finally {\n" +
		"			System.out.println(\"Finishing try-with-resources\");\n" +
		"		}\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException | IOException | Exception e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException | IOException | Exception e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException | IOException | Exception e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"    finally\n" + 
		"      {\n" + 
		"        System.out.println(\"Finishing try-with-resources\");\n" + 
		"      }\n" + 
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
public void test0007() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		List<String> l = new ArrayList<>();\n" +
		"		System.out.println(l);\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    List<String> l = new ArrayList<>();\n" + 
		"    System.out.println(l);\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    List<String> l = new ArrayList<>();\n" + 
		"    System.out.println(l);\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    List<String> l = new ArrayList<>();\n" + 
		"    System.out.println(l);\n" + 
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
public void test0008() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		List<> l = new ArrayList<>();\n" +
		"		System.out.println(l);\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    List<> l = new ArrayList<>();\n" + 
		"    System.out.println(l);\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    List<> l = new ArrayList<>();\n" + 
		"    System.out.println(l);\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    List<> l = new ArrayList<>();\n" + 
		"    System.out.println(l);\n" + 
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
public void test0009() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		List<String> l = new java.util.ArrayList<>();\n" +
		"		System.out.println(l);\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    List<String> l = new java.util.ArrayList<>();\n" + 
		"    System.out.println(l);\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    List<String> l = new java.util.ArrayList<>();\n" + 
		"    System.out.println(l);\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    List<String> l = new java.util.ArrayList<>();\n" + 
		"    System.out.println(l);\n" + 
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
public void test0010() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		B<String>.C<Integer> o = new B<>.C<>();\n" +
		"		System.out.println(l);\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    B<String>.C<Integer> o = new B<>.C<>();\n" + 
		"    System.out.println(l);\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    B<String>.C<Integer> o = new B<>.C<>();\n" + 
		"    System.out.println(l);\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    B<String>.C<Integer> o = new B<>.C<>();\n" + 
		"    System.out.println(l);\n" + 
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
public void test0011() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		try (Reader reader = new FileReader(\"fileName\");) {\n" +
		"			System.out.println(reader.read());\n" +
		"		} catch(FileNotFoundException e) {\n" +
		"			e.printStackTrace();\n" +
		"		}\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
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
public void test0012() {

	String s =
		"public class A {\n" +
		"	public void foo(String fileName) {\n" +
		"		try (Reader reader = new FileReader(\"fileName\");\n" +
		"			Reader reader2 = new FileReader(\"fileName\");) {\n" +
		"			System.out.println(reader.read());\n" +
		"		} catch(FileNotFoundException e) {\n" +
		"			e.printStackTrace();\n" +
		"		}\n" +
		"	}\n" +
		"}";

	String expectedDietUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\");\n" + 
		"        Reader reader2 = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\");\n" + 
		"        Reader reader2 = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";

	String expectedFullUnitToString =
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo(String fileName) {\n" + 
		"    try (Reader reader = new FileReader(\"fileName\");\n" + 
		"        Reader reader2 = new FileReader(\"fileName\"))\n" + 
		"      {\n" + 
		"        System.out.println(reader.read());\n" + 
		"      }\n" + 
		"    catch (FileNotFoundException e)\n" + 
		"      {\n" + 
		"        e.printStackTrace();\n" + 
		"      }\n" + 
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
}
