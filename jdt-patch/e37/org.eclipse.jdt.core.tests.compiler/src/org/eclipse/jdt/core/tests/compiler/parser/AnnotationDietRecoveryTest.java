/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

// This test suite test the first implementation of the annotation recovery.
// Tests must be updated with annotation recovery improvment
// TODO(david) update test suite
public class AnnotationDietRecoveryTest extends AbstractCompilerTest {
	private static final boolean CHECK_ALL_PARSE = true;
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

public AnnotationDietRecoveryTest(String testName){
	super(testName);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
public static Class testClass() {
	return AnnotationDietRecoveryTest.class;
}
/*
 * Toggle compiler in mode -1.5
 */
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
	if (CHECK_ALL_PARSE){
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
	if (CHECK_ALL_PARSE){
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
	if (CHECK_ALL_PARSE){
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
	if (CHECK_ALL_PARSE){
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
	if (CHECK_ALL_PARSE){
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
			+ "public @interface X							\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @interface X {\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
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
			+ "public @interface X <T> {							\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @interface X<T> {\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
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
			+ "public @interface X {							\n"
			+ "  String foo()							\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @interface X {\n" +
		"  String foo() {\n" +
"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
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
			+ "public @interface X {						\n"
			+ "  String foo() default \"blabla\"			\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @interface X {\n" +
		"  String foo() default \"blabla\" {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=79770
 */
public void test0005() {

	String s =
		"@Documented			\n"
			+ "@Rentention(RententionPolicy.RUNTIME)				\n"
			+ "@Target(ElementType.TYPE)							\n"
			+ "@interface MyAnn { 									\n"
			+ "  String value() default \"Default Message\"			\n"
			+ "}													\n"
			+ "public class X {										\n"
			+ "	public @MyAnn void something() { }					\n"
			+ "}													\n";

	String expectedDietUnitToString =
		"@Documented @Rentention(RententionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface MyAnn {\n" +
		"  String value() default \"Default Message\" {\n" +
		"  }\n" +
		"}\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public @MyAnn void something() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"@Documented @Rentention(RententionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface MyAnn {\n" +
		"  String value() default \"Default Message\" {\n" +
		"  }\n" +
		"}\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @MyAnn void something() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
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
			+ "public @interface X {						\n"
			+ "  String foo() {}			\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @interface X {\n" +
		"  String foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
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
			+ "public @interface X {						\n"
			+ "  String foo(								\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @interface X {\n" +
		"  String foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
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
			+ "public class X {				        		\n"
			+ "  void foo(int var1, @Annot(at1=zzz, at2) int var2 {	\n"
			+ "  }							        		\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(int var1) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo(int var1) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
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
			+ "public class X {				        		\n"
			+ "  @SuppressWarnings(\"unchecked\");\n"
			+ "  List<Test> l;		\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  @SuppressWarnings(\"unchecked\") List<Test> l;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  @SuppressWarnings(\"unchecked\") List<Test> l;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  List<Test> l;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
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
			+ "public class X {							\n"
			+ "  String foo() {							\n"
			+ "       @interface Y {						\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  @interface Y {\n" +
		"  }\n" +
		"  public X() {\n" +
		"  }\n" +
		"  String foo() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  @interface Y {\n" +
		"  }\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  String foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=176725
public void test0011() {

	String s =
		"package a;										\n"
			+ "public class X {							\n"
			+ "  #										\n"
			+ "  @AnAnnotation({var})					\n"
			+ "  public void foo() {					\n"
			+ "  }										\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  public @AnAnnotation({var}) void foo() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @AnAnnotation({var}) void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210404
public void test0012() {

	String s =
		"package a;											\n"
			+ "public class X {				        		\n"
			+ "  void foo(int var1, @Annot(at1=zzz, at2=@Annot(at3=zzz, at4)) int var2 {	\n"
			+ "  }							        		\n"
			+ "}											\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(int var1) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo(int var1) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0013() {

	String s =
		"package a;										\n"
			+ "@AnAnnotation(name)						\n"
			+ "@AnAnnotation2(name2)					\n"
			+ "public class X {							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0014() {

	String s =
		"package a;										\n"
			+ "#										\n"
			+ "@AnAnnotation(name)						\n"
			+ "@AnAnnotation2(name2)					\n"
			+ "public class X {							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0015() {

	String s =
		"package a;										\n"
			+ "@AnAnnotation(name)						\n"
			+ "@AnAnnotation2(name2)					\n"
			+ "public class X {							\n"
			+ "#										\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0016() {

	String s =
		"package a;										\n"
			+ "@AnAnnotation(name)						\n"
			+ "@AnAnnotation2(name2)					\n"
			+ "#										\n"
			+ "public class X {							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0017() {

	String s =
		"package a;										\n"
			+ "@AnAnnotation(name)						\n"
			+ "#										\n"
			+ "@AnAnnotation2(name2)					\n"
			+ "public class X {							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0018() {

	String s =
		"package a;										\n"
			+ "@AnAnnotation(name=)						\n"
			+ "@AnAnnotation2(name2)					\n"
			+ "public class X {							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name = $missing$) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name = $missing$) @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation2(name2) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0019() {

	String s =
		"package a;										\n"
			+ "@AnAnnotation(name)						\n"
			+ "@AnAnnotation2(name2=)					\n"
			+ "public class X {							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2 = $missing$) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name) @AnAnnotation2(name2 = $missing$) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0020() {

	String s =
		"package a;										\n"
			+ "public class X {							\n"
			+ "  @AnAnnotation(name) #					\n"
			+ "  int field;								\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  @AnAnnotation(name) int field;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  @AnAnnotation(name) int field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  int field;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0021() {

	String s =
		"package a;										\n"
			+ "public class X {							\n"
			+ "  @AnAnnotation(name=)					\n"
			+ "  int field;								\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  @AnAnnotation(name = $missing$) int field;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  @AnAnnotation(name = $missing$) int field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  int field;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0022() {

	String s =
		"package a;										\n"
			+ "public class X {							\n"
			+ "  @AnAnnotation(name) #					\n"
			+ "  void foo() {}							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  @AnAnnotation(name) void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  @AnAnnotation(name) void foo() {\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0023() {

	String s =
		"package a;										\n"
			+ "public class X {							\n"
			+ "  @AnAnnotation(name=)					\n"
			+ "  void foo() {}							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  @AnAnnotation(name = $missing$) void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  @AnAnnotation(name = $missing$) void foo() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0024() {

	String s =
		"package a;															\n"
			+ "public class X {												\n"
			+ "  void foo(int param1, @AnAnnotation(name) # int param2) {}	\n"
			+ "}															\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(int param1) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo(int param1) {\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0025() {

	String s =
		"package a;															\n"
			+ "public class X {												\n"
			+ "  void foo(int param1, @AnAnnotation(name=) int param2) {}	\n"
			+ "}															\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(int param1) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo(int param1) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0026() {

	String s =
		"package a;															\n"
			+ "public class X {												\n"
			+ "  void foo(int param1, @AnAnnotation(@AnAnnotation1(name1=\"a\", name2=) int param2) {}	\n"
			+ "}															\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo(int param1) {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo(int param1) {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0027() {

	String s =
		"package a;																					\n"
			+ "@AnAnnotation1(name1=\"a\", #)														\n"
			+ "public class X {																		\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name1 = \"a\") class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name1 = \"a\") class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0028() {

	String s =
		"package a;																					\n"
			+ "@AnAnnotation1(name1=\"a\", name2=@AnAnnotation2(name3=\"b\"), #)					\n"
			+ "public class X {																		\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name1 = \"a\",name2 = @AnAnnotation2(name3 = \"b\")) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name1 = \"a\",name2 = @AnAnnotation2(name3 = \"b\")) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0030() {

	String s =
		"package a;																					\n"
			+ "@AnAnnotation1(\"a\"#)																\n"
			+ "public class X {																		\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(\"a\") class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(\"a\") class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0031() {

	String s =
		"package a;																					\n"
			+ "@AnAnnotation1(\"a\", name2=@AnAnnotation2(name3=\"b\"), #)							\n"
			+ "public class X {																		\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(\"a\") @AnAnnotation2(name3 = \"b\") class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(\"a\") @AnAnnotation2(name3 = \"b\") class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0032() {

	String s =
		"package a;																					\n"
			+ "@AnAnnotation1(\"a\", name2=@AnAnnotation2(name3=\"b\"))								\n"
			+ "public class X {																		\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(\"a\") @AnAnnotation2(name3 = \"b\") class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(\"a\") @AnAnnotation2(name3 = \"b\") class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0033() {

	String s =
		"package a;																					\n"
			+ "@AnAnnotation1(name=new Object() {})	#												\n"
			+ "public class X {																		\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name = new Object() {\n" +
		"}) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name = new Object() {\n" +
		"}) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0034() {

	String s =
		"package a;																					\n"
			+ "@AnAnnotation1(name=new Object() {},#)												\n"
			+ "public class X {																		\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name = new Object() {\n" +
		"}) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name = new Object() {\n" +
		"}) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0035() {

	String s =
		"package a;																					\n"
			+ "@AnAnnotation1(name=new Object() {#})												\n"
			+ "public class X {																		\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name = $missing$) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation1(name = $missing$) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227855
public void test0036() {

	String s =
		"package a;																					\n"
			+ "#																					\n"
			+ "public class Test {																	\n"
			+ "  public Test() {}																	\n"
			+ "  @SuppressWarnings(value=\"\")														\n"
			+ "  private int id;																	\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class Test {\n" +
		"  private @SuppressWarnings(value = \"\") int id;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class Test {\n" +
		"  private @SuppressWarnings(value = \"\") int id;\n" +
		"  public Test() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class Test {\n" +
		"  private @SuppressWarnings(value = \"\") int id;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227855
public void test0037() {

	String s =
		"package a;																					\n"
			+ "#																					\n"
			+ "public class Test {																	\n"
			+ "  public int id0;																	\n"
			+ "  @SuppressWarnings(value=\"\")														\n"
			+ "  private int id;																	\n"
			+ "}																					\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public class Test {\n" +
		"  public int id0;\n" +
		"  private @SuppressWarnings(value = \"\") int id;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";

	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public class Test {\n" +
		"  public int id0;\n" +
		"  private @SuppressWarnings(value = \"\") int id;\n" +
		"  public Test() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class Test {\n" +
		"  public int id0;\n" +
		"  private @SuppressWarnings(value = \"\") int id;\n" +
		"  public Test() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228464
public void test0038() {

	String s =
		"package a;										\n"
			+ "@AnAnnotation(name=)						\n"
			+ "public class X {							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name = $missing$) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name = $missing$) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228464
public void test0039() {

	String s =
		"package a;										\n"
			+ "@AnAnnotation(name1=a,name2=)			\n"
			+ "public class X {							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name1 = a,name2 = $missing$) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name1 = a,name2 = $missing$) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228464
public void test0040() {

	String s =
		"package a;										\n"
			+ "@AnAnnotation(name1=a,name2=,name3=c)	\n"
			+ "public class X {							\n"
			+ "}										\n";

	String expectedDietUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name1 = a,name2 = $missing$) class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";


	String expectedDietPlusBodyUnitToString =
		"package a;\n" +
		"public @AnAnnotation(name1 = a,name2 = $missing$) class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"package a;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
}
