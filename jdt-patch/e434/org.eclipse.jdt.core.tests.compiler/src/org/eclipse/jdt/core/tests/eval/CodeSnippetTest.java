/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.eval;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.eval.EvaluationResult;
import org.eclipse.jdt.internal.eval.IRequestor;
/**
 * Test the code snippet evaluation.
 * This assumes that the EvaluationContext class and that the EvaluationResult class
 * are working correctly.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CodeSnippetTest extends EvaluationTest {
/**
 * Creates a new CodeSnippetTest.
 */
public CodeSnippetTest(String name) {
	super(name);
}
/**
 * Returns a requestor that expects no result.
 */
private IRequestor getNoResultRequestor() {
	return new Requestor() {
		@Override
		public void acceptResult(EvaluationResult result) {
			assertTrue("No result", false);
		}
	};
}
static {
//	TESTS_NAMES = new String[] {"testDiamond"};
}
public static Test suite() {
	return setupSuite(testClass());
}
@Override
public Map getCompilerOptions() {
	Map defaultOptions = super.getCompilerOptions();
	defaultOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
	defaultOptions.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.DO_NOT_GENERATE);
	defaultOptions.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.DO_NOT_GENERATE);
	defaultOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	defaultOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.IGNORE);
	defaultOptions.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);
	defaultOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	defaultOptions.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, CompilerOptions.WARNING);
	return defaultOptions;
}
/**
 * Tests code snippet that throws a checked exception.
 * (Regression test for 1G0T5XK)
 */
public void testCheckedException() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"try {",
		"	throw new java.io.IOException();",
		"} finally {",
		"	System.err.println(\"This is an expected exception printed by the target VM:\");",
		"}"}),
		null);
}
public static Class testClass() {
	return CodeSnippetTest.class;
}
/**
 * Tests that no errors are reported for an empty statement followed by an expression.
 * (Regression test for PR #1G1HGHE)
 */
public void testEmptyStatement() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	public int fact(int n){",
		"		return fact0(n, 1);",
		"	}",
		"	public int fact0(int n, int acc){",
		"		if (n <= 1) return acc;",
		"		return fact0(n - 1, acc * n);",
		"	}",
		"};",
		"new X().fact(10)"}),
		"3628800".toCharArray());
}
/**
 * Tests that an error is reported for an empty import.
 */
public void testEvaluateEmptyImport() {
	try {
		char[] importName = new char[0];
		this.context.setImports(new char[][] {importName});
		Requestor requestor = new Requestor();
		this.context.evaluateImports(getEnv(), requestor, getProblemFactory());
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Problems", result.hasProblems());
		assertEquals("Evaluation type", EvaluationResult.T_IMPORT, result.getEvaluationType());
		assertEquals("Evaluation ID", importName, result.getEvaluationID());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests that an error is reported for an existing package and a non existing type imports.
 */
public void testEvaluateExistingPackageAndNonExistingTypeImports() {
	try {
		char[] importName = "non.existing.Type".toCharArray();
		this.context.setImports(new char[][] {"java.lang.reflect.*".toCharArray(), importName});
		Requestor requestor = new Requestor();
		this.context.evaluateImports(getEnv(), requestor, getProblemFactory());
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Problems", result.hasProblems());
		assertEquals("Evaluation type", EvaluationResult.T_IMPORT, result.getEvaluationType());
		assertEquals("Evaluation ID", importName, result.getEvaluationID());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests that an error is reported for a non existing package and an existing type imports.
 */
public void testEvaluateNonExistingPackageAndExistingTypeImports() {
	try {
		char[] importName = "non.existing.*".toCharArray();
		this.context.setImports(new char[][] {importName, "java.math.BigInteger".toCharArray()});
		Requestor requestor = new Requestor();
		this.context.evaluateImports(getEnv(), requestor, getProblemFactory());
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Problems", result.hasProblems());
		assertEquals("Evaluation type", EvaluationResult.T_IMPORT, result.getEvaluationType());
		assertEquals("Evaluation ID", importName, result.getEvaluationID());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests that an error is reported for a non existing package import.
 */
public void testEvaluateNonExistingPackageImport() {
	try {
		char[] importName = "non.existing.*".toCharArray();
		this.context.setImports(new char[][] {importName});
		Requestor requestor = new Requestor();
		this.context.evaluateImports(getEnv(), requestor, getProblemFactory());
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Problems", result.hasProblems());
		assertEquals("Evaluation type", EvaluationResult.T_IMPORT, result.getEvaluationType());
		assertEquals("Evaluation ID", importName, result.getEvaluationID());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests that an error is reported for a non existing type import.
 */
public void testEvaluateNonExistingTypeImport() {
	try {
		char[] importName = "non.existing.Type".toCharArray();
		this.context.setImports(new char[][] {importName});
		Requestor requestor = new Requestor();
		this.context.evaluateImports(getEnv(), requestor, getProblemFactory());
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Problems", result.hasProblems());
		assertEquals("Evaluation type", EvaluationResult.T_IMPORT, result.getEvaluationType());
		assertEquals("Evaluation ID", importName, result.getEvaluationID());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests that no errors are reported for valid package and type imports.
 */
public void testEvaluateValidPackageAndTypeImports() {
	try {
		this.context.setImports(new char[][] {"java.util.Enumeration".toCharArray(), "java.lang.reflect.*".toCharArray()});
		this.context.evaluateImports(getEnv(), getNoResultRequestor(), getProblemFactory());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests that no errors are reported for a valid package import.
 */
public void testEvaluateValidPackageImport() {
	try {
		this.context.setImports(new char[][] {"java.io.*".toCharArray()});
		this.context.evaluateImports(getEnv(), getNoResultRequestor(), getProblemFactory());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests that no errors are reported for a valid type import.
 */
public void testEvaluateValidTypeImport() {
	try {
		this.context.setImports(new char[][] {"java.math.BigInteger".toCharArray()});
		this.context.evaluateImports(getEnv(), getNoResultRequestor(), getProblemFactory());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests code snippet that include a finally block triggered by an error.
 */
public void testFinallyError() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"try {",
		"	throw new Error();",
		"} finally {",
		"	System.err.println(\"This is an expected error printed by the target VM:\");",
		"}"}),
		null);
}
/**
 * Tests code snippet that include one finally block.
 */
public void testFinallyOneBlock() {
	evaluateWithExpectedWarningAndDisplayString(buildCharArray(new String[] {
		"try {",
		"	return 1;",
		"} finally {",
		"	return 2;",
		"}"}),
		new CategorizedProblem[] {
			newProblem(IProblem.FinallyMustCompleteNormally, ProblemSeverities.Warning, 30, 40, 4),
		},
		"2".toCharArray());
}
/**
 * Tests code snippet that include 2 finally blocks.
 */
public void testFinallyTwoBlock() {
	evaluateWithExpectedWarningAndDisplayString(buildCharArray(new String[] {
		"try {",
		"	try {",
		"		return 1;",
		"	} finally {",
		"		return 2;",
		"	}",
		"} finally {",
		"	return 3;",
		"}"}),
		new CategorizedProblem[] {
			newProblem(IProblem.FinallyMustCompleteNormally, ProblemSeverities.Warning, 40, 51, 5),
			newProblem(IProblem.FinallyMustCompleteNormally, ProblemSeverities.Warning, 66, 76, 8),
		},
		"3".toCharArray());
}
/**
 * Tests the free return addition at the end of the code snippet
 * for an anonymous inner class.
 */
public void testFreeReturnAnonymous() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"new Object() {",
		"	public String toString() {",
		"		return \"an object\";",
		"	}",
		"}"}),
		"an object".toCharArray());
}
/**
 * Tests the free return addition at the end of the code snippet
 * for a class declaration followed by an expression.
 */
public void testFreeReturnClassDeclaration() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class Point {",
		"	int x;",
		"	int y;",
		"	Point(int x, int y) {",
		"		this.x = x;",
		"		this.y = y;",
		"	}",
		"}",
		"new Point(56, 99).x"}),
		"56".toCharArray());
}
/**
 * Tests the free return addition at the end of the code snippet
 * for an expression.
 */
public void testFreeReturnExpression() {
	evaluateWithExpectedDisplayString("(1 + 2) * 3.0 / 4".toCharArray(), "2.25".toCharArray());
}
/**
 * Tests the free return addition at the end of the code snippet
 * for an integer.
 */
public void testFreeReturnInteger() {
	evaluateWithExpectedDisplayString("1".toCharArray(), "1".toCharArray());
}
/**
 * Tests the free return addition at the end of the code snippet
 * for a local variable declaration followed by an expression.
 */
public void testFreeReturnLocalVar() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"int i = 99;",
		"i + 4"}),
		"103".toCharArray());
}
/**
 * Tests the free return addition at the end of the code snippet
 * for a statement followed by an expression.
 */
public void testFreeReturnStatement() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"int i = 2;",
		"for (int j=0;j<10;j++) {",
		"	i++;",
		"}",
		"i"}),
		"12".toCharArray());
}
/**
 * Tests code snippet that include a package import.
 */
public void testImportPackage() {
	try {
		this.context.setImports(new char[][] {"java.io.*".toCharArray()});
		evaluateWithExpectedDisplayString("return new File(\"!@#%\").exists();".toCharArray(), "false".toCharArray());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests code snippet that include a type import.
 */
public void testImportType() {
	try {
		this.context.setImports(new char[][] {"java.math.BigInteger".toCharArray()});
		evaluateWithExpectedDisplayString("return new BigInteger(\"123456789012345678901234567890\");".toCharArray(), "123456789012345678901234567890".toCharArray());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests code snippet that include a type import and a package import.
 */
public void testImportTypeAndPackage() {
	try {
		this.context.setImports(new char[][] {"java.util.Enumeration".toCharArray(), "java.lang.reflect.*".toCharArray()});
		evaluateWithExpectedDisplayString("Field[] fields = Enumeration.class.getDeclaredFields(); return fields.length;".toCharArray(), "0".toCharArray());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests code snippet that include an anonymous inner class.
 */
public void testInnerClassAnonymous() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"return new Object() {",
		"	public String toString() {",
		"		return \"an inner class\";",
		"	}",
		"};"}),
		"an inner class".toCharArray());
}
/**
 * Tests code snippet that include a named inner class.
 */
public void testInnerClassNamed() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	int foo = 1;",
		"}",
		"return new X().foo;"}),
		"1".toCharArray());
}
/**
 * Tests code snippet that include a package declaration.
 */
public void _testPackage() {
	if (isJRockitVM()) return;
	// TBD: Test access to package private member
	// TBD: Test access to package class and members in another package than a java.* package
	try {
		// declare that the code snippet is run in java.util.zip and access a package private class
		this.context.setPackageName("java.util.zip".toCharArray());
		evaluateWithExpectedDisplayString("return ZipConstants.LOCSIG;".toCharArray(), "67324752".toCharArray());
	} finally {
		// clean up
		this.context.setPackageName(new char[0]);
	}
}
/**
 * Tests the return value display strings for a boolean.
 */
public void testReturnDisplayStringBoolean() {
	evaluateWithExpectedDisplayString("return true;".toCharArray(), "true".toCharArray());
}
/**
 * Tests the return value display strings for a character.
 */
public void testReturnDisplayStringCharacter() {
	evaluateWithExpectedDisplayString("return 'c';".toCharArray(), "c".toCharArray());
}
/**
 * Tests the return value display strings for a double.
 */
public void testReturnDisplayStringDouble() {
	evaluateWithExpectedDisplayString("return 1.0;".toCharArray(), "1.0".toCharArray());
}
/**
 * Tests the return value display strings for no explicit returned value
 * because of an empty code snippet.
 */
public void testReturnDisplayStringEmptyCodeSnippet() {
	evaluateWithExpectedDisplayString(new char[0], null);
}
/**
 * Tests the return value display strings for a float.
 */
public void testReturnDisplayStringFloat() {
	evaluateWithExpectedDisplayString("return (float)1.0;".toCharArray(), "1.0".toCharArray());
}
/**
 * Tests the return value display strings for an integer.
 */
public void testReturnDisplayStringInteger() {
	evaluateWithExpectedDisplayString("return 1;".toCharArray(), "1".toCharArray());
}
/**
 * Tests the return value display strings for a long.
 */
public void testReturnDisplayStringLong() {
	evaluateWithExpectedDisplayString("return (long)1;".toCharArray(), "1".toCharArray());
}
/**
 * Tests the return value display strings for a code snippet
 * with no explicit returned value (implicitly the last expression is
 * wrapped inside a return statement).
 */
public void testReturnDisplayStringNoExplicitReturnedValue() {
	evaluateWithExpectedDisplayString("new String(\"hello\".toCharArray());".toCharArray(), "hello".toCharArray());
}
/**
 * Tests the return value display strings for an object.
 */
public void testReturnDisplayStringObject() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"return new Object() {",
		"	public String toString() {",
		"		return \"an object\";",
		"	}",
		"};"}),
		"an object".toCharArray());
}
/**
 * Tests the return value display strings for a short.
 */
public void testReturnDisplayStringShort() {
	evaluateWithExpectedDisplayString("return (short)1;".toCharArray(), "1".toCharArray());
}
/**
 * Tests the return value display strings for a String.
 */
public void testReturnDisplayStringString() {
	evaluateWithExpectedDisplayString("return \"hello\";".toCharArray(), "hello".toCharArray());
}
/**
 * Tests the return value display strings for a code snippet
 * with a void returned value.
 */
public void testReturnDisplayStringVoid() {
	evaluateWithExpectedDisplayString("new Thread().start()".toCharArray(), null);
}
/**
 * Tests the return type for a boolean.
 */
public void testReturnTypeBoolean() {
	evaluateWithExpectedType("return true;".toCharArray(), "boolean".toCharArray());
}
/**
 * Tests the return type for a boolean array.
 */
public void testReturnTypeBooleanArray() {
	evaluateWithExpectedType("return new boolean[] {true};".toCharArray(), "[Z".toCharArray());
}
/**
 * Tests the return type for a Boolean object.
 */
public void testReturnTypeBooleanObject() {
	evaluateWithExpectedType("return Boolean.valueOf(true);".toCharArray(), "java.lang.Boolean".toCharArray());
}
/**
 * Tests the return type for a char.
 */
public void testReturnTypeChar() {
	evaluateWithExpectedType("return 'c';".toCharArray(), "char".toCharArray());
}
/**
 * Tests the return type for a Character object.
 */
public void testReturnTypeCharacterObject() {
	evaluateWithExpectedType("return Character.valueOf('c');".toCharArray(), "java.lang.Character".toCharArray());
}
/**
 * Tests the return type for a char array.
 */
public void testReturnTypeCharArray() {
	evaluateWithExpectedType("return new char[] {'c'};".toCharArray(), "[C".toCharArray());
}
/**
 * Tests the return type for a double.
 */
public void testReturnTypeDouble() {
	evaluateWithExpectedType("return 1.0;".toCharArray(), "double".toCharArray());
}
/**
 * Tests the return type for a double array.
 */
public void testReturnTypeDoubleArray() {
	evaluateWithExpectedType("return new double[] {1.0};".toCharArray(), "[D".toCharArray());
}
/**
 * Tests the return type for a Double object.
 */
public void testReturnTypeDoubleObject() {
	evaluateWithExpectedType("return Double.valueOf(1.0);".toCharArray(), "java.lang.Double".toCharArray());
}
/**
 * Tests the return type for an empty code snippet.
 */
public void testReturnTypeEmptyCodeSnippet() {
	evaluateWithExpectedType(new char[0], null);
}
/**
 * Tests the return type for a float.
 */
public void testReturnTypeFloat() {
	evaluateWithExpectedType("return (float)1.0;".toCharArray(), "float".toCharArray());
}
/**
 * Tests the return type for a float array.
 */
public void testReturnTypeFloatArray() {
	evaluateWithExpectedType("return new float[] {(float)1.0};".toCharArray(), "[F".toCharArray());
}
/**
 * Tests the return type for a Float object.
 */
public void testReturnTypeFloatObject() {
	evaluateWithExpectedType("return Float.valueOf(1.0f);".toCharArray(), "java.lang.Float".toCharArray());
}
/**
 * Tests the return type for an int.
 */
public void testReturnTypeInt() {
	evaluateWithExpectedType("return 1;".toCharArray(), "int".toCharArray());
}
/**
 * Tests the return type for an Integer object.
 */
public void testReturnTypeIntegerObject() {
	evaluateWithExpectedType("return Integer.valueOf(1);".toCharArray(), "java.lang.Integer".toCharArray());
}
/**
 * Tests the return type for a long.
 */
public void testReturnTypeLong() {
	evaluateWithExpectedType("return (long)1;".toCharArray(), "long".toCharArray());
}
/**
 * Tests the return type for a long array.
 */
public void testReturnTypeLongArray() {
	evaluateWithExpectedType("return new long[] {(long)1};".toCharArray(), "[J".toCharArray());
}
/**
 * Tests the return type for a Long object.
 */
public void testReturnTypeLongObject() {
	evaluateWithExpectedType("return Long.valueOf(1);".toCharArray(), "java.lang.Long".toCharArray());
}
/**
 * Tests the return type for no explicit returned value.
 */
public void testReturnTypeNoExplicitReturnedValue() {
	evaluateWithExpectedType("new Object();".toCharArray(), "java.lang.Object".toCharArray());
}
/**
 * Tests the return type for an Object.
 */
public void testReturnTypeObject() {
	evaluateWithExpectedType("return new Object();".toCharArray(), "java.lang.Object".toCharArray());
}
/**
 * Tests the return type for an Object array.
 */
public void testReturnTypeObjectArray() {
	evaluateWithExpectedType("return new Object[] {new Object()};".toCharArray(), "[Ljava.lang.Object;".toCharArray());
}
/**
 * Tests the return type for a short.
 */
public void testReturnTypeShort() {
	evaluateWithExpectedType("return (short)1;".toCharArray(), "short".toCharArray());
}
/**
 * Tests the return type for a short array.
 */
public void testReturnTypeShortArray() {
	evaluateWithExpectedType("return new short[] {(short)1};".toCharArray(), "[S".toCharArray());
}
/**
 * Tests the return type for a Short object.
 */
public void testReturnTypeShortObject() {
	evaluateWithExpectedType("return Short.valueOf((short)1);".toCharArray(), "java.lang.Short".toCharArray());
}
/**
 * Tests the return type for an int array.
 */
public void testReturnTypesIntArray() {
	evaluateWithExpectedType("return new int[] {1};".toCharArray(), "[I".toCharArray());
}
/**
 * Tests the return type for a String.
 */
public void testReturnTypeString() {
	evaluateWithExpectedType("return \"hello\";".toCharArray(), "java.lang.String".toCharArray());
}
/**
 * Tests the return type for a String array.
 */
public void testReturnTypeStringArray() {
	evaluateWithExpectedType("return new String[] {\"hello\"};".toCharArray(), "[Ljava.lang.String;".toCharArray());
}
/**
 * Tests the return type for a void value.
 */
public void testReturnTypeVoid() {
	evaluateWithExpectedType("new Thread().start()".toCharArray(), null);
}
/**
 * Tests that the run() method defined in an anonymous class doesn't interfer with the
 * code snippet support (see PR #1G1C0HR).
 */
public void testRunMethodInAnonymous() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	public int i = 0;",
		"   public boolean finished = false;",
		"}",
		"final X x = new X();",
		"(new Thread() {",
		"	public void run() {",
		"		for (int i=0;i<10;i++) {",
		"			try {",
		"				Thread.currentThread().sleep(10);",
		"			} catch (InterruptedException e) {",
		"			}",
		"			x.i++;",
		"		}",
		"       x.finished = true;",
		"	}",
		"}).start();",
		"while (!x.finished) Thread.currentThread().sleep(100);",
		"x.i"}),
		"10".toCharArray());
}
/**
 * Tests that the run() method defined in an anonymous class doesn't interfer with the
 * code snippet support (see PR #1G1C0HR).
 */
public void testRunMethodInAnonymous2() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	public int i = 0;",
		"   public boolean finished = false;",
		"}",
		"final X x = new X();",
		"(new Thread() {",
		"	public void run() {",
		"		for (int i=0;i<10;i++) {",
		"			try {",
		"				Thread.currentThread().sleep(10);",
		"			} catch (InterruptedException e) {",
		"			}",
		"			++x.i;",
		"		}",
		"       x.finished = true;",
		"	}",
		"}).start();",
		"while (!x.finished) Thread.currentThread().sleep(100);",
		"x.i"}),
		"10".toCharArray());
}
/**
 * Tests that the run() method defined in an anonymous class doesn't interfer with the
 * code snippet support (see PR #1G1C0HR).
 */
public void testRunMethodInAnonymous3() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	public int i = 0;",
		"   public boolean finished = false;",
		"}",
		"final X x = new X();",
		"(new Thread() {",
		"	public void run() {",
		"		for (int i=0;i<10;i++) {",
		"			try {",
		"				Thread.currentThread().sleep(10);",
		"			} catch (InterruptedException e) {",
		"			}",
		"			x.i += 1;",
		"		}",
		"       x.finished = true;",
		"	}",
		"}).start();",
		"while (!x.finished) Thread.currentThread().sleep(100);",
		"x.i"}),
		"10".toCharArray());
}
/**
 * Tests that the run() method defined in an anonymous class doesn't interfer with the
 * code snippet support (see PR #1G1C0HR).
 */
public void testRunMethodInAnonymous4() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	public int i = 0;",
		"   public boolean finished = false;",
		"}",
		"final X x = new X();",
		"(new Thread() {",
		"	public void run() {",
		"		for (int i=0;i<10;i++) {",
		"			try {",
		"				Thread.currentThread().sleep(10);",
		"			} catch (InterruptedException e) {",
		"			}",
		"			x.i = x.i + 1;",
		"		}",
		"       x.finished = true;",
		"	}",
		"}).start();",
		"while (!x.finished) Thread.currentThread().sleep(100);",
		"x.i"}),
		"10".toCharArray());
}
/**
 * Tests that the run() method defined in an anonymous class doesn't interfer with the
 * code snippet support (see PR #1G1C0HR).
 */
public void testRunMethodInAnonymous5() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	public int i = 0;",
		"   public boolean finished = false;",
		"	public X self() { return this; }",
		"}",
		"final X x = new X();",
		"(new Thread() {",
		"	public void run() {",
		"		for (int i=0;i<10;i++) {",
		"			try {",
		"				Thread.currentThread().sleep(10);",
		"			} catch (InterruptedException e) {",
		"			}",
		"			x.self().i++;",
		"		}",
		"       x.finished = true;",
		"	}",
		"}).start();",
		"while (!x.finished) Thread.currentThread().sleep(100);",
		"x.i"}),
		"10".toCharArray());
}
/**
 * Tests that the run() method defined in an anonymous class doesn't interfer with the
 * code snippet support (see PR #1G1C0HR).
 */
public void testRunMethodInAnonymous6() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	public int i = 0;",
		"   public boolean finished = false;",
		"	public X self() { return this; }",
		"}",
		"final X x = new X();",
		"(new Thread() {",
		"	public void run() {",
		"		for (int i=0;i<10;i++) {",
		"			try {",
		"				Thread.currentThread().sleep(10);",
		"			} catch (InterruptedException e) {",
		"			}",
		"			++ (x.self().i);",
		"		}",
		"       x.finished = true;",
		"	}",
		"}).start();",
		"while (!x.finished) Thread.currentThread().sleep(100);",
		"x.i"}),
		"10".toCharArray());
}
/**
 * Tests that the run() method defined in an anonymous class doesn't interfer with the
 * code snippet support (see PR #1G1C0HR).
 */
public void testRunMethodInAnonymous7() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	public int i = 0;",
		"   public boolean finished = false;",
		"	public X self() { return this; }",
		"}",
		"final X x = new X();",
		"(new Thread() {",
		"	public void run() {",
		"		for (int i=0;i<10;i++) {",
		"			try {",
		"				Thread.currentThread().sleep(10);",
		"			} catch (InterruptedException e) {",
		"			}",
		"			x.self().i += 1;",
		"		}",
		"       x.finished = true;",
		"	}",
		"}).start();",
		"while (!x.finished) Thread.currentThread().sleep(100);",
		"x.i"}),
		"10".toCharArray());
}
/**
 * Tests that the run() method defined in an anonymous class doesn't interfer with the
 * code snippet support (see PR #1G1C0HR).
 */
public void testRunMethodInAnonymous8() {
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
		"class X {",
		"	public int i = 0;",
		"   public boolean finished = false;",
		"	public X self() { return this; }",
		"}",
		"final X x = new X();",
		"(new Thread() {",
		"	public void run() {",
		"		for (int i=0;i<10;i++) {",
		"			try {",
		"				Thread.currentThread().sleep(10);",
		"			} catch (InterruptedException e) {",
		"			}",
		"			x.self().i = x.self().i + 1;",
		"		}",
		"       x.finished = true;",
		"	}",
		"}).start();",
		"while (!x.finished) Thread.currentThread().sleep(100);",
		"x.i"}),
		"10".toCharArray());
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=89632
 */
public void testFor89632() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	try {
		this.context.setImports(new char[][] {"java.util.*".toCharArray()});
		evaluateWithExpectedDisplayString(
				buildCharArray(new String[] {
					"Collection<String> c = new ArrayList<String>();\n" +
					"c.add(\"a\");\n" +
					"c.add(\"b\");\n" +
					"c.add(\"c\");  \n" +
					"Iterator<String> i = c.iterator();\n" +
					"StringBuffer buffer = new StringBuffer();\n" +
					"while (i.hasNext()) {\n" +
					"	buffer.append(i.next());\n" +
					"}" +
					"return String.valueOf(buffer);"
				}),
				"abc".toCharArray());
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=345334
 */
public void testBug345334() {
	Map options = getCompilerOptions();
	if (this.complianceLevel == ClassFileConstants.JDK1_5) {
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.ERROR);
	} else if (this.complianceLevel >= ClassFileConstants.JDK1_6) {
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.ERROR);
	}
	evaluateWithExpectedDisplayString(
			options,
			buildCharArray(new String[] {
			"return \"SUCCESS\";\n",
			}),
			"SUCCESS".toCharArray());
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=343693
 */
public void testDiamond() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) {
		return;
	}
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
			"class X<T> {",
			"	T field;",
			"	public X(T param) {",
			"		field = param;",
			"	}",
			"	public T foo() {",
			"		return field;",
			"	}",
			"};",
			"new X<>(\"SUCCESS\").foo();\n"}),
			"SUCCESS".toCharArray());
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=464656
 */
public void testBug464656() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
			"java.util.stream.Stream<String> s = java.util.stream.Stream.of(\"a\",\"b\");\n" +
			"return s.findFirst();"}),
			"Optional[a]".toCharArray());
}

public void testBug571310_ThisReciever() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
			"class Outer {",
			"	public int outerFoo() {",
			"		return 10;",
			"	}",
			"	public int boo() {",
			"		java.util.function.Function<Integer,Integer> f = i -> this.outerFoo() + i;",
			"		return f.apply(5);",
			"	}",
			"};",
			"(new Outer()).boo();"}),
			"15".toCharArray());
}

public void testBug571310_LocalVarReciever() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
			"class Outer {",
			"	public int outerFoo(){",
			"		return 10;",
			"	}",
			"	public int boo() {",
			"		Outer thisVar = this;",
			"		java.util.function.Function<Integer,Integer> f = (i) -> thisVar.outerFoo() + i;",
			"		return f.apply(5);",
			"	}",
			"};",
			"new Outer().boo();"}),
			"15".toCharArray());
}

public void testBug571310_QualifiedReciever() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
			"class Outer {",
			"	public int outerFoo(){",
			"		return 10;",
			"	}",
			"	public java.util.function.Supplier<Integer> boo() {",
			"		return new java.util.function.Supplier<Integer>() {",
			"			public Integer get() {",
			"				java.util.function.Function<Integer,Integer> f = (i) -> Outer.this.outerFoo() + i;",
			"				return f.apply(5);",
			"			}",
			"		};",
			"	}",
			"};",
			"new Outer().boo().get();"}),
			"15".toCharArray());
}
public void testBug571310_SynthVarReciever() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
			"class Outer {",
			"	Integer intField = 10;",
			"	public java.util.function.Supplier<Integer> boo() {",
			"		intField ++;",
			"		return new java.util.function.Supplier<Integer>() {",
			"			public Integer get() {",
			"				java.util.function.Function<Integer,Integer> f = (i) -> intField.intValue() + i;",
			"				return f.apply(5);",
			"			}",
			"		};",
			"	}",
			"};",
			"new Outer().boo().get();"}),
			"16".toCharArray());
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1060
// NPE when inspecting scrapbook expression that uses Java 8 features
public void testGH1060() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	evaluateWithExpectedDisplayString(buildCharArray(new String[] {
			"return java.time.format.DateTimeFormatter\r\n"
			+ "				.ofPattern(\"yyyyMMddHHmmss.SSS000\")\r\n"
			+ "				.format(java.time.format.DateTimeFormatter.ofPattern(\"dd.MM.yyyy HHmmss\").parse(\"30.03.2021 112430\", java.time.LocalDateTime::from));"}),
			"20210330112430.000000".toCharArray());
}
}
