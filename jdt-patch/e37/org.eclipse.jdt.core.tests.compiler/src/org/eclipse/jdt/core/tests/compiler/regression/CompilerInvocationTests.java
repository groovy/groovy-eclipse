/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla - Contribution for bug 239066
 *     Stephan Herrmann  - Contribution for bug 236385
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import junit.framework.Test;

/**
 * This class is meant to gather test cases related to the invocation of the
 * compiler, be it at an API or non API level.
 */
public class CompilerInvocationTests extends AbstractRegressionTest {

public CompilerInvocationTests(String name) {
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test003_task_tags_options" };
//    	TESTS_NUMBERS = new int[] { 1 };
//    	TESTS_RANGE = new int[] { 1, -1 };
//  	TESTS_RANGE = new int[] { 1, 2049 };
//  	TESTS_RANGE = new int[] { 449, 451 };
//    	TESTS_RANGE = new int[] { 900, 999 };
  	}

public static Test suite() {
    return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
    return CompilerInvocationTests.class;
}

// irritant vs warning token - check To/From symmetry
public void test001_irritant_warning_token() {
	Map matcher = new HashMap();
	for (int group = 0; group < IrritantSet.GROUP_MAX; group++) {
		for (int i = 0; i < 29; i++) {
			int irritant = (group << IrritantSet.GROUP_SHIFT) + (1 << i);
			String token = CompilerOptions.warningTokenFromIrritant(irritant);
			if (token != null) {
				matcher.put(token, token);
				assertTrue(CompilerOptions.warningTokenToIrritants(token) != null);
			}
		}
	}
	String [] allTokens = CompilerOptions.warningTokens;
	int length = allTokens.length;
	matcher.put("all", "all"); // all gets undetected in the From/To loop
	assertEquals(allTokens.length, matcher.size());
	for (int i = 0; i < length; i++) {
		assertNotNull(matcher.get(allTokens[i]));
	}
}

// problem categories - check that none is left unspecified
// see also discussion in https://bugs.eclipse.org/bugs/show_bug.cgi?id=208383
public void test002_problem_categories() {
	try {
		Class iProblemClass;
		Field[] fields = (iProblemClass = IProblem.class).getFields();
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			if (field.getType() == Integer.TYPE) {
				int problemId = field.getInt(iProblemClass), maskedProblemId = problemId & IProblem.IgnoreCategoriesMask;
				if (maskedProblemId != 0 && maskedProblemId != IProblem.IgnoreCategoriesMask
						&& ProblemReporter.getProblemCategory(ProblemSeverities.Error, problemId)
							== CategorizedProblem.CAT_UNSPECIFIED) {
					 fail("unspecified category for problem " + field.getName());
				}
			}
		}
	}
	catch (IllegalAccessException e) {
		fail("could not access members");
	}
}
class TasksReader implements ICompilerRequestor {
	CompilationResult result;
	public void acceptResult(CompilationResult compilationResult) {
		this.result = compilationResult;
	}
}
static String taskTagsAsCutAndPaste(CategorizedProblem tasks[]) {
	StringBuffer result = new StringBuffer();
	String arguments[];
	for (int i = 0; i < tasks.length - 1; i++) {
		arguments = tasks[i].getArguments();
		System.out.print("\t\t\"[");
		System.out.print(arguments[0]);
		System.out.print(',');
		System.out.print(arguments[1]);
		System.out.print(',');
		System.out.print(arguments[2]);
		System.out.println("]\\n\" +");
	}
	arguments = tasks[tasks.length - 1].getArguments();
	System.out.print("\t\t\"[");
	System.out.print(arguments[0]);
	System.out.print(',');
	System.out.print(arguments[1]);
	System.out.print(',');
	System.out.print(arguments[2]);
	System.out.println("]\\n\"");
	return result.toString();
}
static String taskTagsAsStrings(CategorizedProblem tasks[]) {
	StringBuffer result = new StringBuffer();
	String arguments[];
	for (int i = 0; i < tasks.length; i++) {
		arguments = tasks[i].getArguments();
		result.append('[');
		result.append(arguments[0]);
		result.append(',');
		result.append(arguments[1]);
		result.append(',');
		result.append(arguments[2]);
		result.append(']');
		result.append("\n");
	}
	return result.toString();
}
public void runTaskTagsOptionsTest(
		String[] testFiles,
		Map customOptions,
		String expectedTags) {
	TasksReader reader = new TasksReader();
	Map options = JavaCore.getDefaultOptions();
	if (customOptions != null) {
		options.putAll(customOptions);
	}
	this.runConformTest(
		testFiles,
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null, /* no VM args */
		options,
		reader,
		true /* skip javac */);
	String tags = taskTagsAsStrings(reader.result.tasks);
	if (! tags.equals(expectedTags)) {
		System.out.println(getClass().getName() + '#' + getName());
		System.out.println("Effective results:");
		System.out.println(tags);
		System.out.println("Cut and paste:");
		taskTagsAsCutAndPaste(reader.result.tasks);
		assertEquals(expectedTags, tags);
	}
}
// Basic test on task tags: watch default behavior
public void test003_task_tags_options() {
	runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    // FIXME TODO XXX message contents\n" +
			"  }\n" +
			"}\n"},
		null,
		"[FIXME, message contents,HIGH]\n" +
		"[TODO, message contents,NORMAL]\n" +
		"[XXX, message contents,NORMAL]\n");
}
// effect of cancelling priorities
// reactivate when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=143402 is fixed
public void _test004_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "");
	runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    // FIXME TODO XXX message contents\n" +
			"  }\n" +
			"}\n"},
		customOptions,
		"[FIXME, message contents,NORMAL]\n" +
		"[TODO, message contents,NORMAL]\n" +
		"[XXX, message contents,NORMAL]\n");
}
// effect of cancelling priorities
// reactivate when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=143402 is fixed
public void _test005_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, ",,");
	runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    // FIXME TODO XXX message contents\n" +
			"  }\n" +
			"}\n"},
		customOptions,
		"[FIXME,message contents,NORMAL]\n" +
		"[TODO,message contents,NORMAL]\n" +
		"[XXX,message contents,NORMAL]\n");
	// would expect an exception of some sort
}
// effect of changing priorities
// reactivate when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=143402 is fixed
public void _test006_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "A,B,C,D,E");
	runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    // FIXME TODO XXX message contents\n" +
			"  }\n" +
			"}\n"},
		customOptions,
		"[FIXME,message contents,NORMAL]\n" +
		"[TODO,message contents,NORMAL]\n" +
		"[XXX,message contents,NORMAL]\n");
	// would expect an exception of some sort
}
// effect of changing priorities
public void test007_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,NORMAL,NORMAL");
	runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    // FIXME TODO XXX message contents\n" +
			"  }\n" +
			"}\n"},
		customOptions,
		"[FIXME, message contents,NORMAL]\n" +
		"[TODO, message contents,NORMAL]\n" +
		"[XXX, message contents,NORMAL]\n");
}
// effect of changing priorities
// reactivate when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=143402 is fixed
public void _test008_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,NORMAL"); // one less than the number of tags
	runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    // FIXME TODO XXX message contents\n" +
			"  }\n" +
			"}\n"},
		customOptions,
		"[FIXME,message contents,NORMAL]\n" +
		"[TODO,message contents,NORMAL]\n" +
		"[XXX,message contents,NORMAL]\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206423
// that bug showed that we had no coverage in the area of missing message
// templates, which can occur downstream in the localization process (assuming
// that we always release the English version right)
public void test009_missing_message_templates() {
	assertEquals("Unable to retrieve the error message for problem id: 16777215. Check compiler resources.",
			new DefaultProblemFactory().getLocalizedMessage(Integer.MAX_VALUE, new String[]{}));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206423
public void test010_missing_elaboration_templates() {
	assertEquals("Unable to retrieve the error message elaboration for elaboration id: 1073741823. Check compiler resources.",
			new DefaultProblemFactory().getLocalizedMessage(0, Integer.MAX_VALUE / 2, new String[]{"Zork"}));
}
// problem categories - check that categories match expected ones
// see also discussion in https://bugs.eclipse.org/bugs/show_bug.cgi?id=208383
public void test011_problem_categories() {
	try {
		Class iProblemClass;
		class ProblemAttributes {
			boolean deprecated;
			int category;
			ProblemAttributes(int category) {
				this.category = category;
			}
			ProblemAttributes(boolean deprecated) {
				this.deprecated = deprecated;
			}
		}
		ProblemAttributes DEPRECATED = new ProblemAttributes(true);
		Map expectedProblemAttributes = new HashMap();
		expectedProblemAttributes.put("AbstractMethodCannotBeOverridden", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AbstractMethodInAbstractClass", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AbstractMethodInEnum", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AbstractMethodMustBeImplemented", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AbstractMethodMustBeImplementedOverConcreteMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AbstractMethodsInConcreteClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("AmbiguousConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousConstructorInDefaultConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousConstructorInImplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("AnnotationCannotOverrideMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AnnotationCircularity", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("AnnotationCircularitySelfReference", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("AnnotationFieldNeedConstantInitialization", DEPRECATED);
		expectedProblemAttributes.put("AnnotationMembersCannotHaveParameters", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("AnnotationMembersCannotHaveTypeParameters", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("AnnotationTypeDeclarationCannotHaveConstructor", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("AnnotationTypeDeclarationCannotHaveSuperclass", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("AnnotationTypeDeclarationCannotHaveSuperinterfaces", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("AnnotationTypeUsedAsSuperInterface", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("AnnotationValueMustBeAnEnumConstant", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnnotationValueMustBeAnnotation", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnnotationValueMustBeArrayInitializer", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnnotationValueMustBeClassLiteral", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnnotationValueMustBeConstant", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnonymousClassCannotExtendFinalClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ArgumentHidingField", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("ArgumentHidingLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("ArgumentIsNeverUsed", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("ArgumentTypeAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("ArgumentTypeCannotBeVoid", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ArgumentTypeCannotBeVoidArray", DEPRECATED);
		expectedProblemAttributes.put("ArgumentTypeInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("ArgumentTypeInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("ArgumentTypeNotFound", DEPRECATED);
		expectedProblemAttributes.put("ArgumentTypeNotVisible", DEPRECATED);
		expectedProblemAttributes.put("ArrayConstantsOnlyInArrayInitializers", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ArrayReferenceRequired", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AssignmentHasNoEffect", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("AssignmentToMultiCatchParameter", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AssignmentToResource", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AutoManagedResourceNotBelow17", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("BinaryLiteralNotBelow17", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("BodyForAbstractMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("BodyForNativeMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("BoundCannotBeArray", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("BoundHasConflictingArguments", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("BoundMustBeAnInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("BoxingConversion", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("BytecodeExceeds64KLimit", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("BytecodeExceeds64KLimitForClinit", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("BytecodeExceeds64KLimitForConstructor", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotAllocateVoidArray", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotDeclareEnumSpecialMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("CannotDefineAnnotationInLocalType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotDefineDimensionExpressionsWithInit", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotDefineEnumInLocalType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotDefineInterfaceInLocalType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotDefineStaticInitializerInLocalType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotExtendEnum", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotHideAnInstanceMethodWithAStaticMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("CannotImportPackage", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("CannotInferElidedTypes", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotInvokeSuperConstructorInEnum", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("CannotOverrideAStaticMethodWithAnInstanceMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("CannotReadSource", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotReturnInInitializer", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotThrowNull", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotThrowType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotUseDiamondWithAnonymousClasses", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotUseDiamondWithExplicitTypeArguments", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotUseSuperInCodeSnippet", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ClassExtendFinalClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CodeCannotBeReached", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CodeSnippetMissingClass", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CodeSnippetMissingMethod", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ComparingIdentical", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("ConflictingImport", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("ConstructorVarargsArgumentNeedCast", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("CorruptedSignature", new ProblemAttributes(CategorizedProblem.CAT_BUILDPATH));
		expectedProblemAttributes.put("DeadCode", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("DiamondNotBelow17", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DirectInvocationOfAbstractMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DisallowedTargetForAnnotation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DiscouragedReference", new ProblemAttributes(CategorizedProblem.CAT_RESTRICTION));
		expectedProblemAttributes.put("DuplicateAnnotation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateAnnotationMember", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("DuplicateBlankFinalFieldInitialization", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateBounds", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateCase", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateDefaultCase", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("DuplicateField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateFinalLocalInitialization", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("DuplicateImport", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("DuplicateInheritedMethods", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateLabel", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("DuplicateMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateMethodErasure", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateModifierForArgument", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateModifierForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateModifierForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateModifierForType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateModifierForVariable", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateNestedType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateParameterizedMethods", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateSuperInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateTargetInTargetAnnotation", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("DuplicateTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("DuplicateTypes", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("EmptyControlFlowStatement", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("EnclosingInstanceInConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("EndOfSource", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("EnumAbstractMethodMustBeImplemented", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("EnumConstantCannotDefineAbstractMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("EnumConstantMustImplementAbstractMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("EnumConstantsCannotBeSurroundedByParenthesis", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("EnumStaticFieldInInInitializerContext", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("EnumSwitchCannotTargetField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ExceptionTypeAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("ExceptionTypeInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("ExceptionTypeInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("ExceptionTypeNotFound", DEPRECATED);
		expectedProblemAttributes.put("ExceptionTypeNotVisible", DEPRECATED);
		expectedProblemAttributes.put("ExpressionShouldBeAVariable", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ExternalProblemFixable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ExternalProblemNotFixable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("FallthroughCase", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("FieldHidingField", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("FieldHidingLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("FieldMissingDeprecatedAnnotation", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("FieldTypeAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("FieldTypeInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("FieldTypeInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("FieldTypeNotFound", DEPRECATED);
		expectedProblemAttributes.put("FieldTypeNotVisible", DEPRECATED);
		expectedProblemAttributes.put("FinalBoundForTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("FinalFieldAssignment", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("FinalMethodCannotBeOverridden", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("FinalOuterLocalAssignment", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("FinallyMustCompleteNormally", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("ForbiddenReference", new ProblemAttributes(CategorizedProblem.CAT_RESTRICTION));
		expectedProblemAttributes.put("GenericConstructorTypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("GenericMethodTypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("GenericTypeCannotExtendThrowable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("HidingEnclosingType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("HierarchyCircularity", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("HierarchyCircularitySelfReference", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("HierarchyHasProblems", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalAbstractModifierCombinationForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalAccessFromTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalCast", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalClassLiteralForTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalDimension", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IllegalEnclosingInstanceSpecification", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalExtendedDimensions", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalExtendedDimensionsForVarArgs", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("IllegalGenericArray", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalHexaLiteral", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("IllegalInstanceofParameterizedType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IllegalInstanceofTypeParameter", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IllegalModifierCombinationFinalAbstractForClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierCombinationFinalVolatileForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForAnnotationField", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IllegalModifierForAnnotationMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForAnnotationMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForAnnotationType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForArgument", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForEnum", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForEnumConstant", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForEnumConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForInterfaceField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForInterfaceMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForLocalClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForLocalEnum", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForMemberClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForMemberEnum", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForMemberInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForVariable", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalPrimitiveOrArrayTypeForEnclosingInstance", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalQualifiedEnumConstantLabel", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalQualifiedParameterizedTypeAllocation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalStaticModifierForMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalTypeVariableSuperReference", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IllegalUnderscorePosition", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("IllegalUsageOfQualifiedTypeReference", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("IllegalVararg", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalVisibilityModifierCombinationForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalVisibilityModifierCombinationForMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalVisibilityModifierCombinationForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalVisibilityModifierForInterfaceMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ImportAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("ImportInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("ImportInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("ImportNotFound", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("ImportNotVisible", DEPRECATED);
		expectedProblemAttributes.put("IncompatibleExceptionInInheritedMethodThrowsClause", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IncompatibleExceptionInThrowsClause", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("IncompatibleReturnType", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IncompatibleReturnTypeForNonInheritedInterfaceMethod", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("IncompatibleTypesInConditionalOperator", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncompatibleTypesInEqualityOperator", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncompatibleTypesInForeach", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectArityForParameterizedConstructor", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectArityForParameterizedMethod", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectArityForParameterizedType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectEnclosingInstanceReference", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectSwitchType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectSwitchType17", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IndirectAccessToStaticField", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("IndirectAccessToStaticMethod", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("IndirectAccessToStaticType", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("InheritedFieldHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InheritedIncompatibleReturnType", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InheritedMethodHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InheritedMethodReducesVisibility", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InheritedTypeHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InitializerMustCompleteNormally", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InstanceFieldDuringConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InstanceMethodDuringConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InterfaceAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("InterfaceCannotHaveConstructors", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InterfaceCannotHaveInitializers", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InterfaceInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("InterfaceInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("InterfaceNotFound", DEPRECATED);
		expectedProblemAttributes.put("InterfaceNotVisible", DEPRECATED);
		expectedProblemAttributes.put("InternalTypeNameProvided", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidAnnotationMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidBinary", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidBreak", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidCatchBlockSequence", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidCharacterConstant", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidClassInstantiation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidContinue", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidDigit", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidEncoding", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidEscape", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidExplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidExpressionAsStatement", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidFileNameForPackageAnnotations", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidFloat", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidHexa", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidHighSurrogate", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidInput", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidLowSurrogate", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidNullToSynchronized", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidOctal", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidOperator", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidParameterizedExceptionType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidParenthesizedExpression", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidTypeExpression", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidTypeForCollection", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidTypeForCollectionTarget14", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidTypeForStaticImport", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("InvalidTypeToSynchronized", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidTypeVariableExceptionType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidUnaryExpression", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUnicodeEscape", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUnionTypeReferenceSequence", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidUsageOfAnnotationDeclarations", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfAnnotations", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfEnumDeclarations", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfForeachStatements", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfStaticImports", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfTypeArguments", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfTypeParameters", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfTypeParametersForAnnotationDeclaration", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfTypeParametersForEnumDeclaration", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfVarargs", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfWildcard", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidVoidExpression", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IsClassPathCorrect", new ProblemAttributes(CategorizedProblem.CAT_BUILDPATH));
		expectedProblemAttributes.put("JavadocAmbiguousConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousField", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousMethodReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateParamName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateReturnTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateThrowsClassName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocEmptyReturnTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocGenericConstructorTypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocGenericMethodTypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocHiddenReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocIncorrectArityForParameterizedConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocIncorrectArityForParameterizedMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInheritedFieldHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInheritedMethodHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInheritedNameHidesEnclosingTypeName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInternalTypeNameProvided", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidMemberTypeQualification", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidParamName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidParamTagName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidParamTagTypeParameter", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeArgs", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeHref", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeUrlReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidThrowsClass", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidThrowsClassName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidValueReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMalformedSeeReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMessagePrefix", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("JavadocMissing", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingHashCharacter", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingIdentifier", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingParamName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingParamTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingReturnTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingSeeReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingTagDescription", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingThrowsClassName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingThrowsTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNoMessageSendOnArrayType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNoMessageSendOnBaseType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNonGenericConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNonGenericMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNonStaticTypeFromStaticInvocation", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleField", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocParameterMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocParameterizedConstructorArgumentTypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocParameterizedMethodArgumentTypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocTypeArgumentsForRawGenericConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocTypeArgumentsForRawGenericMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedField", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUnexpectedTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUnexpectedText", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUnterminatedInlineTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedField", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("LocalVariableCanOnlyBeNull", DEPRECATED);
		expectedProblemAttributes.put("LocalVariableCannotBeNull", DEPRECATED);
		expectedProblemAttributes.put("LocalVariableHidingField", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("LocalVariableHidingLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("LocalVariableIsNeverUsed", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("LocalVariableMayBeNull", DEPRECATED);
		expectedProblemAttributes.put("MaskedCatch", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("MethodButWithConstructorName", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MethodCanBePotentiallyStatic", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MethodCanBeStatic", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MethodMissingDeprecatedAnnotation", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MethodMustOverride", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodMustOverrideOrImplement", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodNameClash", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodReducesVisibility", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodRequiresBody", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodReturnsVoid", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodVarargsArgumentNeedCast", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("MissingArgumentsForParameterizedMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MissingEnclosingInstance", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MissingEnclosingInstanceForConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MissingEnumConstantCase", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("MissingOverrideAnnotation", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MissingOverrideAnnotationForInterfaceMethodImplementation", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MissingReturnType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MissingSemiColon", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("MissingSerialVersion", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("MissingSynchronizedModifierInInheritedMethod", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("MissingTypeInConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MissingTypeInMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MissingValueForAnnotationMember", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MultiCatchNotBelow17", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("MustDefineEitherDimensionExpressionsOrInitializer", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("MustSpecifyPackage", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NativeMethodsCannotBeStrictfp", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NeedToEmulateConstructorAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("NeedToEmulateFieldReadAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("NeedToEmulateFieldWriteAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("NeedToEmulateMethodAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("NoAdditionalBoundAfterTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("NoFieldOnBaseType", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NoImplicitStringConversionForCharArrayExpression", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("NoMessageSendOnArrayType", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NoMessageSendOnBaseType", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NonBlankFinalLocalAssignment", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NonConstantExpression", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NonExternalizedStringLiteral", new ProblemAttributes(CategorizedProblem.CAT_NLS));
		expectedProblemAttributes.put("NonGenericConstructor", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("NonGenericMethod", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("NonGenericType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("NonNullLocalVariableComparisonYieldsFalse", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("NonStaticAccessToStaticField", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("NonStaticAccessToStaticMethod", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("NonStaticContextForEnumMemberType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NonStaticFieldFromStaticInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NonStaticTypeFromStaticInvocation", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NotVisibleConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleConstructorInDefaultConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleConstructorInImplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("NullLocalVariableComparisonYieldsFalse", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("NullLocalVariableInstanceofYieldsFalse", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("NullLocalVariableReference", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("NullSourceString", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("NumericValueOutOfRange", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ObjectCannotBeGeneric", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ObjectCannotHaveSuperTypes", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ObjectHasNoSuperclass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ObjectMustBeClass", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("OuterLocalMustBeFinal", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("OverridingDeprecatedMethod", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("OverridingMethodWithoutSuperInvocation", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("OverridingNonVisibleMethod", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("PackageCollidesWithType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("PackageIsNotExpectedPackage", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ParameterAssignment", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("ParameterMismatch", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ParameterizedConstructorArgumentTypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ParameterizedMethodArgumentTypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ParsingError", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorDeleteToken", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorDeleteTokens", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertToComplete", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertToCompletePhrase", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertToCompleteScope", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertTokenAfter", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertTokenBefore", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInvalidToken", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorMergeTokens", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorMisplacedConstruct", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorNoSuggestion", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorNoSuggestionForTokens", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorOnKeyword", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorOnKeywordNoSuggestion", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorReplaceTokens", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorUnexpectedEOF", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("PolymorphicMethodNotBelow17", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("PossibleAccidentalBooleanAssignment", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("PotentialHeapPollutionFromVararg", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("PotentialNullLocalVariableReference", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("PublicClassMustMatchFileName", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("RawMemberTypeCannotBeParameterized", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("RawTypeReference", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("RecursiveConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("RedefinedArgument", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("RedefinedLocal", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("RedundantSpecificationOfTypeArguments", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("RedundantLocalVariableNullAssignment", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("RedundantNullCheckOnNonNullLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("RedundantNullCheckOnNullLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("RedundantSuperinterface", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("ReferenceToForwardField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ReferenceToForwardTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ResourceHasToImplementAutoCloseable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ReturnTypeAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeCannotBeVoidArray", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeNotFound", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeNotVisible", DEPRECATED);
		expectedProblemAttributes.put("SafeVarargsOnFixedArityMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("SafeVarargsOnNonFinalInstanceMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ShouldImplementHashcode", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("ShouldReturnValue", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("StaticInheritedMethodConflicts", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("StaticMemberOfParameterizedType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("StaticMethodRequested", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("StringConstantIsExceedingUtf8Limit", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("SuperInterfaceMustBeAnInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("SuperInterfacesCollide", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("SuperTypeUsingWildcard", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("SuperclassAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("SuperclassInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("SuperclassInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("SuperclassMustBeAClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("SuperclassNotFound", DEPRECATED);
		expectedProblemAttributes.put("SuperclassNotVisible", DEPRECATED);
		expectedProblemAttributes.put("SuperfluousSemicolon", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("SwitchOnStringsNotBelow17", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("Task", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ThisInStaticContext", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ThisSuperDuringConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("TooManyArgumentSlots", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyArrayDimensions", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyBytesForStringConstant", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyConstantsInConstantPool", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyFields", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyLocalVariableSlots", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyMethods", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyParametersForSyntheticMethod", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManySyntheticArgumentSlots", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeArgumentsForRawGenericConstructor", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeArgumentsForRawGenericMethod", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeCollidesWithPackage", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeHidingType", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("TypeHidingTypeParameterFromMethod", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("TypeHidingTypeParameterFromType", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("TypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeMissingDeprecatedAnnotation", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("TypeParameterHidingType", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("UnboxingConversion", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UndefinedAnnotationMember", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UndefinedConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UndefinedConstructorInDefaultConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UndefinedConstructorInImplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UndefinedField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UndefinedLabel", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("UndefinedMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UndefinedName", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UndefinedType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UndefinedTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("UnderscoresInLiteralsNotBelow17", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UndocumentedEmptyBlock", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UnexpectedStaticModifierForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UnexpectedStaticModifierForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UnhandledException", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnhandledExceptionInDefaultConstructor", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnhandledExceptionInImplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnhandledExceptionOnAutoClose", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnhandledWarningToken", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UninitializedBlankFinalField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UninitializedLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("UnmatchedBracket", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UnnecessaryArgumentCast", DEPRECATED);
		expectedProblemAttributes.put("UnnecessaryCast", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnnecessaryElse", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnnecessaryInstanceof", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnnecessaryNLSTag", new ProblemAttributes(CategorizedProblem.CAT_NLS));
		expectedProblemAttributes.put("UnqualifiedFieldAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UnreachableCatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnresolvedVariable", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UnsafeGenericArrayForVarargs", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeGenericCast", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeRawConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeRawFieldAssignment", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeRawGenericConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeRawGenericMethodInvocation", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeRawMethodInvocation", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeReturnTypeOverride", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeTypeConversion", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnterminatedComment", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UnterminatedString", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UnusedConstructorDeclaredThrownException", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedImport", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedLabel", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedMethodDeclaredThrownException", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedObjectAllocation", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("UnusedPrivateConstructor", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedPrivateField", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedPrivateMethod", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedPrivateType", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedTypeArgumentsForConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UnusedTypeArgumentsForMethodInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UnusedWarningToken", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UseAssertAsAnIdentifier", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UseEnumAsAnIdentifier", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UsingDeprecatedConstructor", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("UsingDeprecatedField", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("UsingDeprecatedMethod", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("UsingDeprecatedType", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("VarargsConflict", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("VariableTypeCannotBeVoid", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("VariableTypeCannotBeVoidArray", DEPRECATED);
		expectedProblemAttributes.put("VoidMethodReturnsValue", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("WildcardConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("WildcardFieldAssignment", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("WildcardMethodInvocation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		StringBuffer failures = new StringBuffer();
		StringBuffer correctResult = new StringBuffer(70000);
		Field[] fields = (iProblemClass = IProblem.class).getFields();
		Arrays.sort(fields, new Comparator() {
			public int compare(Object o1, Object o2) {
				Field field1 = (Field) o1;
				Field field2 = (Field) o2;
				return field1.getName().compareTo(field2.getName());
			}
		});
		boolean watchInternalCategory = false, printHeader = true;
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			if (field.getType() == Integer.TYPE) {
				int problemId = field.getInt(iProblemClass);
				int maskedProblemId = problemId & IProblem.IgnoreCategoriesMask;
				if (maskedProblemId != 0 && maskedProblemId != IProblem.IgnoreCategoriesMask) {
					String name = field.getName();
					ProblemAttributes expectedAttributes = (ProblemAttributes) expectedProblemAttributes.get(name);
					if (expectedAttributes == null) {
						failures.append("missing expected problem attributes for problem " + name + "\n");
						int actualCategory = ProblemReporter.getProblemCategory(ProblemSeverities.Error, problemId);
						correctResult.append("\t\texpectedProblemAttributes.put(\"" + name + "\", new ProblemAttributes(CategorizedProblem." + categoryName(actualCategory) + "));\n");
					} else if (!expectedAttributes.deprecated) {
						int actualCategory = ProblemReporter.getProblemCategory(ProblemSeverities.Error, problemId);
						correctResult.append("\t\texpectedProblemAttributes.put(\"" + name + "\", new ProblemAttributes(CategorizedProblem." + categoryName(actualCategory) + "));\n");
						if (expectedAttributes.category != actualCategory) {
							failures.append("category mismatch for problem " + name + " (expected " + categoryName(expectedAttributes.category) + ", got " + categoryName(actualCategory) + ")\n");
						}
						if (watchInternalCategory && actualCategory == CategorizedProblem.CAT_INTERNAL) {
							if (printHeader) {
								printHeader = false;
								System.err.println("CAT_INTERNAL for problems:");
							}
							System.err.println("\t" + name);
						}
					} else {
						correctResult.append("\t\texpectedProblemAttributes.put(\"" + name + "\", DEPRECATED);\n");
					}
				}
			}
		}
		if (failures.length() > 0) {
			System.out.println(correctResult);
			System.out.println();
		}
		assertEquals(failures.toString(), 0, failures.length());
	}
	catch (IllegalAccessException e) {
		fail("could not access members");
	}
}
private static Map categoryNames;
private String categoryName(int category) {
	if (categoryNames == null) {
		categoryNames = new HashMap();
		Field[] fields = CategorizedProblem.class.getFields();
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			if (field.getType() == Integer.TYPE) {
				String name = field.getName();
				if (name.startsWith("CAT_")) {
					try {
						categoryNames.put(new Integer(field.getInt(CategorizedProblem.class)), name);
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
				}
			}
		}
	}
	return (String) categoryNames.get(new Integer(category));
}
// compiler problems tuning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=218603
public void test012_compiler_problems_tuning() {
	try {
		class ProblemAttributes {
			boolean skip;
			String option;
			ProblemAttributes(String option) {
				this.option = option;
			}
			ProblemAttributes(boolean skip) {
				this.skip = skip;
			}
		}
		ProblemAttributes SKIP = new ProblemAttributes(true);
		Map expectedProblemAttributes = new HashMap();
		expectedProblemAttributes.put("AbstractMethodCannotBeOverridden", SKIP);
		expectedProblemAttributes.put("AbstractMethodInAbstractClass", SKIP);
		expectedProblemAttributes.put("AbstractMethodInEnum", SKIP);
		expectedProblemAttributes.put("AbstractMethodMustBeImplemented", SKIP);
		expectedProblemAttributes.put("AbstractMethodMustBeImplementedOverConcreteMethod", SKIP);
		expectedProblemAttributes.put("AbstractMethodsInConcreteClass", SKIP);
		expectedProblemAttributes.put("AmbiguousConstructor", SKIP);
		expectedProblemAttributes.put("AmbiguousConstructorInDefaultConstructor", SKIP);
		expectedProblemAttributes.put("AmbiguousConstructorInImplicitConstructorCall", SKIP);
		expectedProblemAttributes.put("AmbiguousField", SKIP);
		expectedProblemAttributes.put("AmbiguousMethod", SKIP);
		expectedProblemAttributes.put("AmbiguousType", SKIP);
		expectedProblemAttributes.put("AnnotationCannotOverrideMethod", SKIP);
		expectedProblemAttributes.put("AnnotationCircularity", SKIP);
		expectedProblemAttributes.put("AnnotationCircularitySelfReference", SKIP);
		expectedProblemAttributes.put("AnnotationFieldNeedConstantInitialization", SKIP);
		expectedProblemAttributes.put("AnnotationMembersCannotHaveParameters", SKIP);
		expectedProblemAttributes.put("AnnotationMembersCannotHaveTypeParameters", SKIP);
		expectedProblemAttributes.put("AnnotationTypeDeclarationCannotHaveConstructor", SKIP);
		expectedProblemAttributes.put("AnnotationTypeDeclarationCannotHaveSuperclass", SKIP);
		expectedProblemAttributes.put("AnnotationTypeDeclarationCannotHaveSuperinterfaces", SKIP);
		expectedProblemAttributes.put("AnnotationTypeUsedAsSuperInterface", new ProblemAttributes(JavaCore.COMPILER_PB_ANNOTATION_SUPER_INTERFACE));
		expectedProblemAttributes.put("AnnotationValueMustBeAnEnumConstant", SKIP);
		expectedProblemAttributes.put("AnnotationValueMustBeAnnotation", SKIP);
		expectedProblemAttributes.put("AnnotationValueMustBeArrayInitializer", SKIP);
		expectedProblemAttributes.put("AnnotationValueMustBeClassLiteral", SKIP);
		expectedProblemAttributes.put("AnnotationValueMustBeConstant", SKIP);
		expectedProblemAttributes.put("AnonymousClassCannotExtendFinalClass", SKIP);
		expectedProblemAttributes.put("ArgumentHidingField", new ProblemAttributes(JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING));
		expectedProblemAttributes.put("ArgumentHidingLocalVariable", new ProblemAttributes(JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING));
		expectedProblemAttributes.put("ArgumentIsNeverUsed", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_PARAMETER));
		expectedProblemAttributes.put("ArgumentTypeAmbiguous", SKIP);
		expectedProblemAttributes.put("ArgumentTypeCannotBeVoid", SKIP);
		expectedProblemAttributes.put("ArgumentTypeCannotBeVoidArray", SKIP);
		expectedProblemAttributes.put("ArgumentTypeInheritedNameHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("ArgumentTypeInternalNameProvided", SKIP);
		expectedProblemAttributes.put("ArgumentTypeNotFound", SKIP);
		expectedProblemAttributes.put("ArgumentTypeNotVisible", SKIP);
		expectedProblemAttributes.put("ArrayConstantsOnlyInArrayInitializers", SKIP);
		expectedProblemAttributes.put("ArrayReferenceRequired", SKIP);
		expectedProblemAttributes.put("AssignmentHasNoEffect", new ProblemAttributes(JavaCore.COMPILER_PB_NO_EFFECT_ASSIGNMENT));
		expectedProblemAttributes.put("AssignmentToMultiCatchParameter", SKIP);
		expectedProblemAttributes.put("AssignmentToResource", SKIP);
		expectedProblemAttributes.put("AutoManagedResourceNotBelow17", SKIP);
		expectedProblemAttributes.put("BinaryLiteralNotBelow17", SKIP);
		expectedProblemAttributes.put("BodyForAbstractMethod", SKIP);
		expectedProblemAttributes.put("BodyForNativeMethod", SKIP);
		expectedProblemAttributes.put("BoundCannotBeArray", SKIP);
		expectedProblemAttributes.put("BoundHasConflictingArguments", SKIP);
		expectedProblemAttributes.put("BoundMustBeAnInterface", SKIP);
		expectedProblemAttributes.put("BoxingConversion", new ProblemAttributes(JavaCore.COMPILER_PB_AUTOBOXING));
		expectedProblemAttributes.put("BytecodeExceeds64KLimit", SKIP);
		expectedProblemAttributes.put("BytecodeExceeds64KLimitForClinit", SKIP);
		expectedProblemAttributes.put("BytecodeExceeds64KLimitForConstructor", SKIP);
		expectedProblemAttributes.put("CannotAllocateVoidArray", SKIP);
		expectedProblemAttributes.put("CannotDeclareEnumSpecialMethod", SKIP);
		expectedProblemAttributes.put("CannotDefineAnnotationInLocalType", SKIP);
		expectedProblemAttributes.put("CannotDefineDimensionExpressionsWithInit", SKIP);
		expectedProblemAttributes.put("CannotDefineEnumInLocalType", SKIP);
		expectedProblemAttributes.put("CannotDefineInterfaceInLocalType", SKIP);
		expectedProblemAttributes.put("CannotDefineStaticInitializerInLocalType", SKIP);
		expectedProblemAttributes.put("CannotExtendEnum", SKIP);
		expectedProblemAttributes.put("CannotHideAnInstanceMethodWithAStaticMethod", SKIP);
		expectedProblemAttributes.put("CannotImportPackage", SKIP);
		expectedProblemAttributes.put("CannotInferElidedTypes", SKIP);
		expectedProblemAttributes.put("CannotInvokeSuperConstructorInEnum", SKIP);
		expectedProblemAttributes.put("CannotOverrideAStaticMethodWithAnInstanceMethod", SKIP);
		expectedProblemAttributes.put("CannotReadSource", SKIP);
		expectedProblemAttributes.put("CannotReturnInInitializer", SKIP);
		expectedProblemAttributes.put("CannotThrowNull", SKIP);
		expectedProblemAttributes.put("CannotThrowType", SKIP);
		expectedProblemAttributes.put("CannotUseDiamondWithAnonymousClasses", SKIP);
		expectedProblemAttributes.put("CannotUseDiamondWithExplicitTypeArguments", SKIP);
		expectedProblemAttributes.put("CannotUseSuperInCodeSnippet", SKIP);
		expectedProblemAttributes.put("ClassExtendFinalClass", SKIP);
		expectedProblemAttributes.put("CodeCannotBeReached", SKIP);
		expectedProblemAttributes.put("CodeSnippetMissingClass", SKIP);
		expectedProblemAttributes.put("CodeSnippetMissingMethod", SKIP);
		expectedProblemAttributes.put("ComparingIdentical", new ProblemAttributes(JavaCore.COMPILER_PB_COMPARING_IDENTICAL));
		expectedProblemAttributes.put("ConflictingImport", SKIP);
		expectedProblemAttributes.put("ConstructorVarargsArgumentNeedCast", new ProblemAttributes(JavaCore.COMPILER_PB_VARARGS_ARGUMENT_NEED_CAST));
		expectedProblemAttributes.put("CorruptedSignature", SKIP);
		expectedProblemAttributes.put("DeadCode", new ProblemAttributes(JavaCore.COMPILER_PB_DEAD_CODE));
		expectedProblemAttributes.put("DiamondNotBelow17", SKIP);
		expectedProblemAttributes.put("DirectInvocationOfAbstractMethod", SKIP);
		expectedProblemAttributes.put("DisallowedTargetForAnnotation", SKIP);
		expectedProblemAttributes.put("DiscouragedReference", new ProblemAttributes(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE));
		expectedProblemAttributes.put("DuplicateAnnotation", SKIP);
		expectedProblemAttributes.put("DuplicateAnnotationMember", SKIP);
		expectedProblemAttributes.put("DuplicateBlankFinalFieldInitialization", SKIP);
		expectedProblemAttributes.put("DuplicateBounds", SKIP);
		expectedProblemAttributes.put("DuplicateCase", SKIP);
		expectedProblemAttributes.put("DuplicateDefaultCase", SKIP);
		expectedProblemAttributes.put("DuplicateField", SKIP);
		expectedProblemAttributes.put("DuplicateFinalLocalInitialization", SKIP);
		expectedProblemAttributes.put("DuplicateImport", SKIP);
		expectedProblemAttributes.put("DuplicateInheritedMethods", SKIP);
		expectedProblemAttributes.put("DuplicateLabel", SKIP);
		expectedProblemAttributes.put("DuplicateMethod", SKIP);
		expectedProblemAttributes.put("DuplicateMethodErasure", SKIP);
		expectedProblemAttributes.put("DuplicateModifierForArgument", SKIP);
		expectedProblemAttributes.put("DuplicateModifierForField", SKIP);
		expectedProblemAttributes.put("DuplicateModifierForMethod", SKIP);
		expectedProblemAttributes.put("DuplicateModifierForType", SKIP);
		expectedProblemAttributes.put("DuplicateModifierForVariable", SKIP);
		expectedProblemAttributes.put("DuplicateNestedType", SKIP);
		expectedProblemAttributes.put("DuplicateParameterizedMethods", SKIP);
		expectedProblemAttributes.put("DuplicateSuperInterface", SKIP);
		expectedProblemAttributes.put("DuplicateTargetInTargetAnnotation", SKIP);
		expectedProblemAttributes.put("DuplicateTypeVariable", SKIP);
		expectedProblemAttributes.put("DuplicateTypes", SKIP);
		expectedProblemAttributes.put("EmptyControlFlowStatement", new ProblemAttributes(JavaCore.COMPILER_PB_EMPTY_STATEMENT));
		expectedProblemAttributes.put("EnclosingInstanceInConstructorCall", SKIP);
		expectedProblemAttributes.put("EndOfSource", SKIP);
		expectedProblemAttributes.put("EnumAbstractMethodMustBeImplemented", SKIP);
		expectedProblemAttributes.put("EnumConstantCannotDefineAbstractMethod", SKIP);
		expectedProblemAttributes.put("EnumConstantMustImplementAbstractMethod", SKIP);
		expectedProblemAttributes.put("EnumConstantsCannotBeSurroundedByParenthesis", SKIP);
		expectedProblemAttributes.put("EnumStaticFieldInInInitializerContext", SKIP);
		expectedProblemAttributes.put("EnumSwitchCannotTargetField", SKIP);
		expectedProblemAttributes.put("ExceptionTypeAmbiguous", SKIP);
		expectedProblemAttributes.put("ExceptionTypeInheritedNameHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("ExceptionTypeInternalNameProvided", SKIP);
		expectedProblemAttributes.put("ExceptionTypeNotFound", SKIP);
		expectedProblemAttributes.put("ExceptionTypeNotVisible", SKIP);
		expectedProblemAttributes.put("ExpressionShouldBeAVariable", SKIP);
		expectedProblemAttributes.put("ExternalProblemFixable", SKIP);
		expectedProblemAttributes.put("ExternalProblemNotFixable", SKIP);
		expectedProblemAttributes.put("FallthroughCase", new ProblemAttributes(JavaCore.COMPILER_PB_FALLTHROUGH_CASE));
		expectedProblemAttributes.put("FieldHidingField", new ProblemAttributes(JavaCore.COMPILER_PB_FIELD_HIDING));
		expectedProblemAttributes.put("FieldHidingLocalVariable", new ProblemAttributes(JavaCore.COMPILER_PB_FIELD_HIDING));
		expectedProblemAttributes.put("FieldMissingDeprecatedAnnotation", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_DEPRECATED_ANNOTATION));
		expectedProblemAttributes.put("FieldTypeAmbiguous", SKIP);
		expectedProblemAttributes.put("FieldTypeInheritedNameHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("FieldTypeInternalNameProvided", SKIP);
		expectedProblemAttributes.put("FieldTypeNotFound", SKIP);
		expectedProblemAttributes.put("FieldTypeNotVisible", SKIP);
		expectedProblemAttributes.put("FinalBoundForTypeVariable", new ProblemAttributes(JavaCore.COMPILER_PB_FINAL_PARAMETER_BOUND));
		expectedProblemAttributes.put("FinalFieldAssignment", SKIP);
		expectedProblemAttributes.put("FinalMethodCannotBeOverridden", SKIP);
		expectedProblemAttributes.put("FinalOuterLocalAssignment", SKIP);
		expectedProblemAttributes.put("FinallyMustCompleteNormally", new ProblemAttributes(JavaCore.COMPILER_PB_FINALLY_BLOCK_NOT_COMPLETING));
		expectedProblemAttributes.put("ForbiddenReference", new ProblemAttributes(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE));
		expectedProblemAttributes.put("GenericConstructorTypeArgumentMismatch", SKIP);
		expectedProblemAttributes.put("GenericMethodTypeArgumentMismatch", SKIP);
		expectedProblemAttributes.put("GenericTypeCannotExtendThrowable", SKIP);
		expectedProblemAttributes.put("HidingEnclosingType", SKIP);
		expectedProblemAttributes.put("HierarchyCircularity", SKIP);
		expectedProblemAttributes.put("HierarchyCircularitySelfReference", SKIP);
		expectedProblemAttributes.put("HierarchyHasProblems", SKIP);
		expectedProblemAttributes.put("IllegalAbstractModifierCombinationForMethod", SKIP);
		expectedProblemAttributes.put("IllegalAccessFromTypeVariable", SKIP);
		expectedProblemAttributes.put("IllegalCast", SKIP);
		expectedProblemAttributes.put("IllegalClassLiteralForTypeVariable", SKIP);
		expectedProblemAttributes.put("IllegalDimension", SKIP);
		expectedProblemAttributes.put("IllegalEnclosingInstanceSpecification", SKIP);
		expectedProblemAttributes.put("IllegalExtendedDimensions", SKIP);
		expectedProblemAttributes.put("IllegalExtendedDimensionsForVarArgs", SKIP);
		expectedProblemAttributes.put("IllegalGenericArray", SKIP);
		expectedProblemAttributes.put("IllegalHexaLiteral", SKIP);
		expectedProblemAttributes.put("IllegalInstanceofParameterizedType", SKIP);
		expectedProblemAttributes.put("IllegalInstanceofTypeParameter", SKIP);
		expectedProblemAttributes.put("IllegalModifierCombinationFinalAbstractForClass", SKIP);
		expectedProblemAttributes.put("IllegalModifierCombinationFinalVolatileForField", SKIP);
		expectedProblemAttributes.put("IllegalModifierForAnnotationField", SKIP);
		expectedProblemAttributes.put("IllegalModifierForAnnotationMemberType", SKIP);
		expectedProblemAttributes.put("IllegalModifierForAnnotationMethod", SKIP);
		expectedProblemAttributes.put("IllegalModifierForAnnotationType", SKIP);
		expectedProblemAttributes.put("IllegalModifierForArgument", SKIP);
		expectedProblemAttributes.put("IllegalModifierForClass", SKIP);
		expectedProblemAttributes.put("IllegalModifierForConstructor", SKIP);
		expectedProblemAttributes.put("IllegalModifierForEnum", SKIP);
		expectedProblemAttributes.put("IllegalModifierForEnumConstant", SKIP);
		expectedProblemAttributes.put("IllegalModifierForEnumConstructor", SKIP);
		expectedProblemAttributes.put("IllegalModifierForField", SKIP);
		expectedProblemAttributes.put("IllegalModifierForInterface", SKIP);
		expectedProblemAttributes.put("IllegalModifierForInterfaceField", SKIP);
		expectedProblemAttributes.put("IllegalModifierForInterfaceMethod", SKIP);
		expectedProblemAttributes.put("IllegalModifierForLocalClass", SKIP);
		expectedProblemAttributes.put("IllegalModifierForLocalEnum", SKIP);
		expectedProblemAttributes.put("IllegalModifierForMemberClass", SKIP);
		expectedProblemAttributes.put("IllegalModifierForMemberEnum", SKIP);
		expectedProblemAttributes.put("IllegalModifierForMemberInterface", SKIP);
		expectedProblemAttributes.put("IllegalModifierForMethod", SKIP);
		expectedProblemAttributes.put("IllegalModifierForVariable", SKIP);
		expectedProblemAttributes.put("IllegalPrimitiveOrArrayTypeForEnclosingInstance", SKIP);
		expectedProblemAttributes.put("IllegalQualifiedEnumConstantLabel", SKIP);
		expectedProblemAttributes.put("IllegalQualifiedParameterizedTypeAllocation", SKIP);
		expectedProblemAttributes.put("IllegalStaticModifierForMemberType", SKIP);
		expectedProblemAttributes.put("IllegalTypeVariableSuperReference", SKIP);
		expectedProblemAttributes.put("IllegalUnderscorePosition", SKIP);
		expectedProblemAttributes.put("IllegalUsageOfQualifiedTypeReference", SKIP);
		expectedProblemAttributes.put("IllegalVararg", SKIP);
		expectedProblemAttributes.put("IllegalVisibilityModifierCombinationForField", SKIP);
		expectedProblemAttributes.put("IllegalVisibilityModifierCombinationForMemberType", SKIP);
		expectedProblemAttributes.put("IllegalVisibilityModifierCombinationForMethod", SKIP);
		expectedProblemAttributes.put("IllegalVisibilityModifierForInterfaceMemberType", SKIP);
		expectedProblemAttributes.put("ImportAmbiguous", SKIP);
		expectedProblemAttributes.put("ImportInheritedNameHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("ImportInternalNameProvided", SKIP);
		expectedProblemAttributes.put("ImportNotFound", SKIP);
		expectedProblemAttributes.put("ImportNotVisible", SKIP);
		expectedProblemAttributes.put("IncompatibleExceptionInInheritedMethodThrowsClause", SKIP);
		expectedProblemAttributes.put("IncompatibleExceptionInThrowsClause", SKIP);
		expectedProblemAttributes.put("IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD));
		expectedProblemAttributes.put("IncompatibleReturnType", SKIP);
		expectedProblemAttributes.put("IncompatibleReturnTypeForNonInheritedInterfaceMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD));
		expectedProblemAttributes.put("IncompatibleTypesInConditionalOperator", SKIP);
		expectedProblemAttributes.put("IncompatibleTypesInEqualityOperator", SKIP);
		expectedProblemAttributes.put("IncompatibleTypesInForeach", SKIP);
		expectedProblemAttributes.put("IncorrectArityForParameterizedConstructor", SKIP);
		expectedProblemAttributes.put("IncorrectArityForParameterizedMethod", SKIP);
		expectedProblemAttributes.put("IncorrectArityForParameterizedType", SKIP);
		expectedProblemAttributes.put("IncorrectEnclosingInstanceReference", SKIP);
		expectedProblemAttributes.put("IncorrectSwitchType", SKIP);
		expectedProblemAttributes.put("IncorrectSwitchType17", SKIP);
		expectedProblemAttributes.put("IndirectAccessToStaticField", new ProblemAttributes(JavaCore.COMPILER_PB_INDIRECT_STATIC_ACCESS));
		expectedProblemAttributes.put("IndirectAccessToStaticMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INDIRECT_STATIC_ACCESS));
		expectedProblemAttributes.put("IndirectAccessToStaticType", new ProblemAttributes(JavaCore.COMPILER_PB_INDIRECT_STATIC_ACCESS));
		expectedProblemAttributes.put("InheritedFieldHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("InheritedIncompatibleReturnType", SKIP);
		expectedProblemAttributes.put("InheritedMethodHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("InheritedMethodReducesVisibility", SKIP);
		expectedProblemAttributes.put("InheritedTypeHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("InitializerMustCompleteNormally", SKIP);
		expectedProblemAttributes.put("InstanceFieldDuringConstructorInvocation", SKIP);
		expectedProblemAttributes.put("InstanceMethodDuringConstructorInvocation", SKIP);
		expectedProblemAttributes.put("InterfaceAmbiguous", SKIP);
		expectedProblemAttributes.put("InterfaceCannotHaveConstructors", SKIP);
		expectedProblemAttributes.put("InterfaceCannotHaveInitializers", SKIP);
		expectedProblemAttributes.put("InterfaceInheritedNameHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("InterfaceInternalNameProvided", SKIP);
		expectedProblemAttributes.put("InterfaceNotFound", SKIP);
		expectedProblemAttributes.put("InterfaceNotVisible", SKIP);
		expectedProblemAttributes.put("InternalTypeNameProvided", SKIP);
		expectedProblemAttributes.put("InvalidAnnotationMemberType", SKIP);
		expectedProblemAttributes.put("InvalidBinary", SKIP);
		expectedProblemAttributes.put("InvalidBreak", SKIP);
		expectedProblemAttributes.put("InvalidCatchBlockSequence", SKIP);
		expectedProblemAttributes.put("InvalidCharacterConstant", SKIP);
		expectedProblemAttributes.put("InvalidClassInstantiation", SKIP);
		expectedProblemAttributes.put("InvalidContinue", SKIP);
		expectedProblemAttributes.put("InvalidDigit", SKIP);
		expectedProblemAttributes.put("InvalidEncoding", SKIP);
		expectedProblemAttributes.put("InvalidEscape", SKIP);
		expectedProblemAttributes.put("InvalidExplicitConstructorCall", SKIP);
		expectedProblemAttributes.put("InvalidExpressionAsStatement", SKIP);
		expectedProblemAttributes.put("InvalidFileNameForPackageAnnotations", SKIP);
		expectedProblemAttributes.put("InvalidFloat", SKIP);
		expectedProblemAttributes.put("InvalidHexa", SKIP);
		expectedProblemAttributes.put("InvalidHighSurrogate", SKIP);
		expectedProblemAttributes.put("InvalidInput", SKIP);
		expectedProblemAttributes.put("InvalidLowSurrogate", SKIP);
		expectedProblemAttributes.put("InvalidNullToSynchronized", SKIP);
		expectedProblemAttributes.put("InvalidOctal", SKIP);
		expectedProblemAttributes.put("InvalidOperator", SKIP);
		expectedProblemAttributes.put("InvalidParameterizedExceptionType", SKIP);
		expectedProblemAttributes.put("InvalidParenthesizedExpression", SKIP);
		expectedProblemAttributes.put("InvalidTypeExpression", SKIP);
		expectedProblemAttributes.put("InvalidTypeForCollection", SKIP);
		expectedProblemAttributes.put("InvalidTypeForCollectionTarget14", SKIP);
		expectedProblemAttributes.put("InvalidTypeForStaticImport", SKIP);
		expectedProblemAttributes.put("InvalidTypeToSynchronized", SKIP);
		expectedProblemAttributes.put("InvalidTypeVariableExceptionType", SKIP);
		expectedProblemAttributes.put("InvalidUnaryExpression", SKIP);
		expectedProblemAttributes.put("InvalidUnicodeEscape", SKIP);
		expectedProblemAttributes.put("InvalidUnionTypeReferenceSequence", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfAnnotationDeclarations", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfAnnotations", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfEnumDeclarations", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfForeachStatements", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfStaticImports", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfTypeArguments", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfTypeParameters", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfTypeParametersForAnnotationDeclaration", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfTypeParametersForEnumDeclaration", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfVarargs", SKIP);
		expectedProblemAttributes.put("InvalidUsageOfWildcard", SKIP);
		expectedProblemAttributes.put("InvalidVoidExpression", SKIP);
		expectedProblemAttributes.put("IsClassPathCorrect", SKIP);
		expectedProblemAttributes.put("JavadocAmbiguousConstructor", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousField", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousMethodReference", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousType", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateParamName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateReturnTag", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateTag", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateThrowsClassName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocEmptyReturnTag", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocGenericConstructorTypeArgumentMismatch", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocGenericMethodTypeArgumentMismatch", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocHiddenReference", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocIncorrectArityForParameterizedConstructor", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocIncorrectArityForParameterizedMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInheritedFieldHidesEnclosingName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInheritedMethodHidesEnclosingName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInheritedNameHidesEnclosingTypeName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInternalTypeNameProvided", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidMemberTypeQualification", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidParamName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidParamTagName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidParamTagTypeParameter", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeArgs", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeHref", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeReference", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeUrlReference", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidTag", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidThrowsClass", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidThrowsClassName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidValueReference", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocMalformedSeeReference", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocMessagePrefix", SKIP);
		expectedProblemAttributes.put("JavadocMissing", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS));
		expectedProblemAttributes.put("JavadocMissingHashCharacter", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingIdentifier", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingParamName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingParamTag", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS));
		expectedProblemAttributes.put("JavadocMissingReturnTag", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS));
		expectedProblemAttributes.put("JavadocMissingSeeReference", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingTagDescription", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingThrowsClassName", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingThrowsTag", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS));
		expectedProblemAttributes.put("JavadocNoMessageSendOnArrayType", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocNoMessageSendOnBaseType", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocNonGenericConstructor", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocNonGenericMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocNonStaticTypeFromStaticInvocation", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleConstructor", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleField", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleType", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocParameterMismatch", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocParameterizedConstructorArgumentTypeMismatch", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocParameterizedMethodArgumentTypeMismatch", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocTypeArgumentsForRawGenericConstructor", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocTypeArgumentsForRawGenericMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedConstructor", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedField", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedType", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUnexpectedTag", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUnexpectedText", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUnterminatedInlineTag", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedConstructor", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedField", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedMethod", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedType", new ProblemAttributes(JavaCore.COMPILER_PB_INVALID_JAVADOC));
		expectedProblemAttributes.put("LocalVariableCanOnlyBeNull", SKIP);
		expectedProblemAttributes.put("LocalVariableCannotBeNull", SKIP);
		expectedProblemAttributes.put("LocalVariableHidingField", new ProblemAttributes(JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING));
		expectedProblemAttributes.put("LocalVariableHidingLocalVariable", new ProblemAttributes(JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING));
		expectedProblemAttributes.put("LocalVariableIsNeverUsed", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_LOCAL));
		expectedProblemAttributes.put("LocalVariableMayBeNull", SKIP);
		expectedProblemAttributes.put("MaskedCatch", new ProblemAttributes(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));
		expectedProblemAttributes.put("MethodButWithConstructorName", new ProblemAttributes(JavaCore.COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME));
		expectedProblemAttributes.put("MethodCanBePotentiallyStatic", new ProblemAttributes(JavaCore.COMPILER_PB_POTENTIALLY_MISSING_STATIC_ON_METHOD));
		expectedProblemAttributes.put("MethodCanBeStatic", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_STATIC_ON_METHOD));
		expectedProblemAttributes.put("MethodMissingDeprecatedAnnotation", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_DEPRECATED_ANNOTATION));
		expectedProblemAttributes.put("MethodMustOverride", SKIP);
		expectedProblemAttributes.put("MethodMustOverrideOrImplement", SKIP);
		expectedProblemAttributes.put("MethodNameClash", SKIP);
		expectedProblemAttributes.put("MethodReducesVisibility", SKIP);
		expectedProblemAttributes.put("MethodRequiresBody", SKIP);
		expectedProblemAttributes.put("MethodReturnsVoid", SKIP);
		expectedProblemAttributes.put("MethodVarargsArgumentNeedCast", new ProblemAttributes(JavaCore.COMPILER_PB_VARARGS_ARGUMENT_NEED_CAST));
		expectedProblemAttributes.put("MissingArgumentsForParameterizedMemberType", SKIP);
		expectedProblemAttributes.put("MissingEnclosingInstance", SKIP);
		expectedProblemAttributes.put("MissingEnclosingInstanceForConstructorCall", SKIP);
		expectedProblemAttributes.put("MissingEnumConstantCase", new ProblemAttributes(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH));
		expectedProblemAttributes.put("MissingOverrideAnnotation", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION));
		expectedProblemAttributes.put("MissingOverrideAnnotationForInterfaceMethodImplementation", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION));
		expectedProblemAttributes.put("MissingReturnType", SKIP);
		expectedProblemAttributes.put("MissingSemiColon", SKIP);
		expectedProblemAttributes.put("MissingSerialVersion", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION));
		expectedProblemAttributes.put("MissingSynchronizedModifierInInheritedMethod", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_SYNCHRONIZED_ON_INHERITED_METHOD));
		expectedProblemAttributes.put("MissingTypeInConstructor", SKIP);
		expectedProblemAttributes.put("MissingTypeInMethod", SKIP);
		expectedProblemAttributes.put("MissingValueForAnnotationMember", SKIP);
		expectedProblemAttributes.put("MultiCatchNotBelow17", SKIP);
		expectedProblemAttributes.put("MustDefineEitherDimensionExpressionsOrInitializer", SKIP);
		expectedProblemAttributes.put("MustSpecifyPackage", SKIP);
		expectedProblemAttributes.put("NativeMethodsCannotBeStrictfp", SKIP);
		expectedProblemAttributes.put("NeedToEmulateConstructorAccess", new ProblemAttributes(JavaCore.COMPILER_PB_SYNTHETIC_ACCESS_EMULATION));
		expectedProblemAttributes.put("NeedToEmulateFieldReadAccess", new ProblemAttributes(JavaCore.COMPILER_PB_SYNTHETIC_ACCESS_EMULATION));
		expectedProblemAttributes.put("NeedToEmulateFieldWriteAccess", new ProblemAttributes(JavaCore.COMPILER_PB_SYNTHETIC_ACCESS_EMULATION));
		expectedProblemAttributes.put("NeedToEmulateMethodAccess", new ProblemAttributes(JavaCore.COMPILER_PB_SYNTHETIC_ACCESS_EMULATION));
		expectedProblemAttributes.put("NoAdditionalBoundAfterTypeVariable", SKIP);
		expectedProblemAttributes.put("NoFieldOnBaseType", SKIP);
		expectedProblemAttributes.put("NoImplicitStringConversionForCharArrayExpression", new ProblemAttributes(JavaCore.COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION));
		expectedProblemAttributes.put("NoMessageSendOnArrayType", SKIP);
		expectedProblemAttributes.put("NoMessageSendOnBaseType", SKIP);
		expectedProblemAttributes.put("NonBlankFinalLocalAssignment", SKIP);
		expectedProblemAttributes.put("NonConstantExpression", SKIP);
		expectedProblemAttributes.put("NonExternalizedStringLiteral", new ProblemAttributes(JavaCore.COMPILER_PB_NON_NLS_STRING_LITERAL));
		expectedProblemAttributes.put("NonGenericConstructor", SKIP);
		expectedProblemAttributes.put("NonGenericMethod", SKIP);
		expectedProblemAttributes.put("NonGenericType", SKIP);
		expectedProblemAttributes.put("NonNullLocalVariableComparisonYieldsFalse", new ProblemAttributes(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK));
		expectedProblemAttributes.put("NonStaticAccessToStaticField", new ProblemAttributes(JavaCore.COMPILER_PB_STATIC_ACCESS_RECEIVER));
		expectedProblemAttributes.put("NonStaticAccessToStaticMethod", new ProblemAttributes(JavaCore.COMPILER_PB_STATIC_ACCESS_RECEIVER));
		expectedProblemAttributes.put("NonStaticContextForEnumMemberType", SKIP);
		expectedProblemAttributes.put("NonStaticFieldFromStaticInvocation", SKIP);
		expectedProblemAttributes.put("NonStaticTypeFromStaticInvocation", SKIP);
		expectedProblemAttributes.put("NotVisibleConstructor", SKIP);
		expectedProblemAttributes.put("NotVisibleConstructorInDefaultConstructor", SKIP);
		expectedProblemAttributes.put("NotVisibleConstructorInImplicitConstructorCall", SKIP);
		expectedProblemAttributes.put("NotVisibleField", SKIP);
		expectedProblemAttributes.put("NotVisibleMethod", SKIP);
		expectedProblemAttributes.put("NotVisibleType", SKIP);
		expectedProblemAttributes.put("NullLocalVariableComparisonYieldsFalse", new ProblemAttributes(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK));
		expectedProblemAttributes.put("NullLocalVariableInstanceofYieldsFalse", new ProblemAttributes(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK));
		expectedProblemAttributes.put("NullLocalVariableReference", new ProblemAttributes(JavaCore.COMPILER_PB_NULL_REFERENCE));
		expectedProblemAttributes.put("NullSourceString", SKIP);
		expectedProblemAttributes.put("NumericValueOutOfRange", SKIP);
		expectedProblemAttributes.put("ObjectCannotBeGeneric", SKIP);
		expectedProblemAttributes.put("ObjectCannotHaveSuperTypes", SKIP);
		expectedProblemAttributes.put("ObjectHasNoSuperclass", SKIP);
		expectedProblemAttributes.put("ObjectMustBeClass", SKIP);
		expectedProblemAttributes.put("OuterLocalMustBeFinal", SKIP);
		expectedProblemAttributes.put("OverridingDeprecatedMethod", new ProblemAttributes(JavaCore.COMPILER_PB_DEPRECATION));
		expectedProblemAttributes.put("OverridingMethodWithoutSuperInvocation", new ProblemAttributes(JavaCore.COMPILER_PB_OVERRIDING_METHOD_WITHOUT_SUPER_INVOCATION));
		expectedProblemAttributes.put("OverridingNonVisibleMethod", new ProblemAttributes(JavaCore.COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD));
		expectedProblemAttributes.put("PackageCollidesWithType", SKIP);
		expectedProblemAttributes.put("PackageIsNotExpectedPackage", SKIP);
		expectedProblemAttributes.put("ParameterAssignment", new ProblemAttributes(JavaCore.COMPILER_PB_PARAMETER_ASSIGNMENT));
		expectedProblemAttributes.put("ParameterMismatch", SKIP);
		expectedProblemAttributes.put("ParameterizedConstructorArgumentTypeMismatch", SKIP);
		expectedProblemAttributes.put("ParameterizedMethodArgumentTypeMismatch", SKIP);
		expectedProblemAttributes.put("ParsingError", SKIP);
		expectedProblemAttributes.put("ParsingErrorDeleteToken", SKIP);
		expectedProblemAttributes.put("ParsingErrorDeleteTokens", SKIP);
		expectedProblemAttributes.put("ParsingErrorInsertToComplete", SKIP);
		expectedProblemAttributes.put("ParsingErrorInsertToCompletePhrase", SKIP);
		expectedProblemAttributes.put("ParsingErrorInsertToCompleteScope", SKIP);
		expectedProblemAttributes.put("ParsingErrorInsertTokenAfter", SKIP);
		expectedProblemAttributes.put("ParsingErrorInsertTokenBefore", SKIP);
		expectedProblemAttributes.put("ParsingErrorInvalidToken", SKIP);
		expectedProblemAttributes.put("ParsingErrorMergeTokens", SKIP);
		expectedProblemAttributes.put("ParsingErrorMisplacedConstruct", SKIP);
		expectedProblemAttributes.put("ParsingErrorNoSuggestion", SKIP);
		expectedProblemAttributes.put("ParsingErrorNoSuggestionForTokens", SKIP);
		expectedProblemAttributes.put("ParsingErrorOnKeyword", SKIP);
		expectedProblemAttributes.put("ParsingErrorOnKeywordNoSuggestion", SKIP);
		expectedProblemAttributes.put("ParsingErrorReplaceTokens", SKIP);
		expectedProblemAttributes.put("ParsingErrorUnexpectedEOF", SKIP);
		expectedProblemAttributes.put("PolymorphicMethodNotBelow17", SKIP);
		expectedProblemAttributes.put("PossibleAccidentalBooleanAssignment", new ProblemAttributes(JavaCore.COMPILER_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT));
		expectedProblemAttributes.put("PotentialHeapPollutionFromVararg", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("PotentialNullLocalVariableReference", new ProblemAttributes(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE));
		expectedProblemAttributes.put("PublicClassMustMatchFileName", SKIP);
		expectedProblemAttributes.put("RawMemberTypeCannotBeParameterized", SKIP);
		expectedProblemAttributes.put("RawTypeReference", new ProblemAttributes(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE));
		expectedProblemAttributes.put("RecursiveConstructorInvocation", SKIP);
		expectedProblemAttributes.put("RedefinedArgument", SKIP);
		expectedProblemAttributes.put("RedefinedLocal", SKIP);
		expectedProblemAttributes.put("RedundantSpecificationOfTypeArguments", new ProblemAttributes(JavaCore.COMPILER_PB_REDUNDANT_TYPE_ARGUMENTS));
		expectedProblemAttributes.put("RedundantLocalVariableNullAssignment", new ProblemAttributes(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK));
		expectedProblemAttributes.put("RedundantNullCheckOnNonNullLocalVariable", new ProblemAttributes(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK));
		expectedProblemAttributes.put("RedundantNullCheckOnNullLocalVariable", new ProblemAttributes(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK));
		expectedProblemAttributes.put("RedundantSuperinterface", new ProblemAttributes(JavaCore.COMPILER_PB_REDUNDANT_SUPERINTERFACE));
		expectedProblemAttributes.put("ReferenceToForwardField", SKIP);
		expectedProblemAttributes.put("ReferenceToForwardTypeVariable", SKIP);
		expectedProblemAttributes.put("ResourceHasToImplementAutoCloseable", SKIP);
		expectedProblemAttributes.put("ReturnTypeAmbiguous", SKIP);
		expectedProblemAttributes.put("ReturnTypeCannotBeVoidArray", SKIP);
		expectedProblemAttributes.put("ReturnTypeInheritedNameHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("ReturnTypeInternalNameProvided", SKIP);
		expectedProblemAttributes.put("ReturnTypeNotFound", SKIP);
		expectedProblemAttributes.put("ReturnTypeNotVisible", SKIP);
		expectedProblemAttributes.put("SafeVarargsOnFixedArityMethod", SKIP);
		expectedProblemAttributes.put("SafeVarargsOnNonFinalInstanceMethod", SKIP);
		expectedProblemAttributes.put("ShouldImplementHashcode", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_HASHCODE_METHOD));
		expectedProblemAttributes.put("ShouldReturnValue", SKIP);
		expectedProblemAttributes.put("StaticInheritedMethodConflicts", SKIP);
		expectedProblemAttributes.put("StaticMemberOfParameterizedType", SKIP);
		expectedProblemAttributes.put("StaticMethodRequested", SKIP);
		expectedProblemAttributes.put("StringConstantIsExceedingUtf8Limit", SKIP);
		expectedProblemAttributes.put("SuperInterfaceMustBeAnInterface", SKIP);
		expectedProblemAttributes.put("SuperInterfacesCollide", SKIP);
		expectedProblemAttributes.put("SuperTypeUsingWildcard", SKIP);
		expectedProblemAttributes.put("SuperclassAmbiguous", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_HASHCODE_METHOD));
		expectedProblemAttributes.put("SuperclassInheritedNameHidesEnclosingName", SKIP);
		expectedProblemAttributes.put("SuperclassInternalNameProvided", SKIP);
		expectedProblemAttributes.put("SuperclassMustBeAClass", SKIP);
		expectedProblemAttributes.put("SuperclassNotFound", SKIP);
		expectedProblemAttributes.put("SuperclassNotVisible", new ProblemAttributes(JavaCore.COMPILER_PB_REDUNDANT_SUPERINTERFACE));
		expectedProblemAttributes.put("SuperfluousSemicolon", new ProblemAttributes(JavaCore.COMPILER_PB_EMPTY_STATEMENT));
		expectedProblemAttributes.put("SwitchOnStringsNotBelow17", SKIP);
		expectedProblemAttributes.put("Task", SKIP);
		expectedProblemAttributes.put("ThisInStaticContext", SKIP);
		expectedProblemAttributes.put("ThisSuperDuringConstructorInvocation", SKIP);
		expectedProblemAttributes.put("TooManyArgumentSlots", SKIP);
		expectedProblemAttributes.put("TooManyArrayDimensions", SKIP);
		expectedProblemAttributes.put("TooManyBytesForStringConstant", SKIP);
		expectedProblemAttributes.put("TooManyConstantsInConstantPool", SKIP);
		expectedProblemAttributes.put("TooManyFields", SKIP);
		expectedProblemAttributes.put("TooManyLocalVariableSlots", SKIP);
		expectedProblemAttributes.put("TooManyMethods", SKIP);
		expectedProblemAttributes.put("TooManyParametersForSyntheticMethod", SKIP);
		expectedProblemAttributes.put("TooManySyntheticArgumentSlots", SKIP);
		expectedProblemAttributes.put("TypeArgumentMismatch", SKIP);
		expectedProblemAttributes.put("TypeArgumentsForRawGenericConstructor", SKIP);
		expectedProblemAttributes.put("TypeArgumentsForRawGenericMethod", SKIP);
		expectedProblemAttributes.put("TypeCollidesWithPackage", SKIP);
		expectedProblemAttributes.put("TypeHidingType", new ProblemAttributes(JavaCore.COMPILER_PB_TYPE_PARAMETER_HIDING));
		expectedProblemAttributes.put("TypeHidingTypeParameterFromMethod", new ProblemAttributes(JavaCore.COMPILER_PB_TYPE_PARAMETER_HIDING));
		expectedProblemAttributes.put("TypeHidingTypeParameterFromType", new ProblemAttributes(JavaCore.COMPILER_PB_TYPE_PARAMETER_HIDING));
		expectedProblemAttributes.put("TypeMismatch", SKIP);
		expectedProblemAttributes.put("TypeMissingDeprecatedAnnotation", new ProblemAttributes(JavaCore.COMPILER_PB_MISSING_DEPRECATED_ANNOTATION));
		expectedProblemAttributes.put("TypeParameterHidingType", new ProblemAttributes(JavaCore.COMPILER_PB_TYPE_PARAMETER_HIDING));
		expectedProblemAttributes.put("UnboxingConversion", new ProblemAttributes(JavaCore.COMPILER_PB_AUTOBOXING));
		expectedProblemAttributes.put("UndefinedAnnotationMember", SKIP);
		expectedProblemAttributes.put("UndefinedConstructor", SKIP);
		expectedProblemAttributes.put("UndefinedConstructorInDefaultConstructor", SKIP);
		expectedProblemAttributes.put("UndefinedConstructorInImplicitConstructorCall", SKIP);
		expectedProblemAttributes.put("UndefinedField", SKIP);
		expectedProblemAttributes.put("UndefinedLabel", SKIP);
		expectedProblemAttributes.put("UndefinedMethod", SKIP);
		expectedProblemAttributes.put("UndefinedName", SKIP);
		expectedProblemAttributes.put("UndefinedType", SKIP);
		expectedProblemAttributes.put("UndefinedTypeVariable", SKIP);
		expectedProblemAttributes.put("UnderscoresInLiteralsNotBelow17", SKIP);
		expectedProblemAttributes.put("UndocumentedEmptyBlock", new ProblemAttributes(JavaCore.COMPILER_PB_UNDOCUMENTED_EMPTY_BLOCK));
		expectedProblemAttributes.put("UnexpectedStaticModifierForField", SKIP);
		expectedProblemAttributes.put("UnexpectedStaticModifierForMethod", SKIP);
		expectedProblemAttributes.put("UnhandledException", SKIP);
		expectedProblemAttributes.put("UnhandledExceptionInDefaultConstructor", SKIP);
		expectedProblemAttributes.put("UnhandledExceptionInImplicitConstructorCall", SKIP);
		expectedProblemAttributes.put("UnhandledExceptionOnAutoClose", SKIP);
		expectedProblemAttributes.put("UnhandledWarningToken", new ProblemAttributes(JavaCore.COMPILER_PB_UNHANDLED_WARNING_TOKEN));
		expectedProblemAttributes.put("UninitializedBlankFinalField", SKIP);
		expectedProblemAttributes.put("UninitializedLocalVariable", SKIP);
		expectedProblemAttributes.put("UnmatchedBracket", SKIP);
		expectedProblemAttributes.put("UnnecessaryArgumentCast", SKIP);
		expectedProblemAttributes.put("UnnecessaryCast", new ProblemAttributes(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK));
		expectedProblemAttributes.put("UnnecessaryElse", new ProblemAttributes(JavaCore.COMPILER_PB_UNNECESSARY_ELSE));
		expectedProblemAttributes.put("UnnecessaryInstanceof", new ProblemAttributes(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK));
		expectedProblemAttributes.put("UnnecessaryNLSTag", new ProblemAttributes(JavaCore.COMPILER_PB_NON_NLS_STRING_LITERAL));
		expectedProblemAttributes.put("UnqualifiedFieldAccess", new ProblemAttributes(JavaCore.COMPILER_PB_UNQUALIFIED_FIELD_ACCESS));
		expectedProblemAttributes.put("UnreachableCatch", SKIP);
		expectedProblemAttributes.put("UnresolvedVariable", SKIP);
		expectedProblemAttributes.put("UnsafeGenericArrayForVarargs", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("UnsafeGenericCast", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("UnsafeRawConstructorInvocation", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("UnsafeRawFieldAssignment", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("UnsafeRawGenericConstructorInvocation", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("UnsafeRawGenericMethodInvocation", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("UnsafeRawMethodInvocation", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("UnsafeReturnTypeOverride", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("UnsafeTypeConversion", new ProblemAttributes(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION));
		expectedProblemAttributes.put("UnterminatedComment", SKIP);
		expectedProblemAttributes.put("UnterminatedString", SKIP);
		expectedProblemAttributes.put("UnusedConstructorDeclaredThrownException", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING));
		expectedProblemAttributes.put("UnusedImport", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_IMPORT));
		expectedProblemAttributes.put("UnusedLabel", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_LABEL));
		expectedProblemAttributes.put("UnusedMethodDeclaredThrownException", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING));
		expectedProblemAttributes.put("UnusedObjectAllocation", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_OBJECT_ALLOCATION));
		expectedProblemAttributes.put("UnusedPrivateConstructor", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER));
		expectedProblemAttributes.put("UnusedPrivateField", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER));
		expectedProblemAttributes.put("UnusedPrivateMethod", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER));
		expectedProblemAttributes.put("UnusedPrivateType", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER));
		expectedProblemAttributes.put("UnusedTypeArgumentsForConstructorInvocation", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_TYPE_ARGUMENTS_FOR_METHOD_INVOCATION));
		expectedProblemAttributes.put("UnusedTypeArgumentsForMethodInvocation", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_TYPE_ARGUMENTS_FOR_METHOD_INVOCATION));
		expectedProblemAttributes.put("UnusedWarningToken", new ProblemAttributes(JavaCore.COMPILER_PB_UNUSED_WARNING_TOKEN));
		expectedProblemAttributes.put("UseAssertAsAnIdentifier", new ProblemAttributes(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER));
		expectedProblemAttributes.put("UseEnumAsAnIdentifier", new ProblemAttributes(JavaCore.COMPILER_PB_ENUM_IDENTIFIER));
		expectedProblemAttributes.put("UsingDeprecatedConstructor", new ProblemAttributes(JavaCore.COMPILER_PB_DEPRECATION));
		expectedProblemAttributes.put("UsingDeprecatedField", new ProblemAttributes(JavaCore.COMPILER_PB_DEPRECATION));
		expectedProblemAttributes.put("UsingDeprecatedMethod", new ProblemAttributes(JavaCore.COMPILER_PB_DEPRECATION));
		expectedProblemAttributes.put("UsingDeprecatedType", new ProblemAttributes(JavaCore.COMPILER_PB_DEPRECATION));
		expectedProblemAttributes.put("VarargsConflict", SKIP);
		expectedProblemAttributes.put("VariableTypeCannotBeVoid", SKIP);
		expectedProblemAttributes.put("VariableTypeCannotBeVoidArray", SKIP);
		expectedProblemAttributes.put("VoidMethodReturnsValue", SKIP);
		expectedProblemAttributes.put("WildcardConstructorInvocation", SKIP);
		expectedProblemAttributes.put("WildcardFieldAssignment", SKIP);
		expectedProblemAttributes.put("WildcardMethodInvocation", SKIP);
		Map constantNamesIndex = new HashMap();
		Field[] fields = JavaCore.class.getFields();
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			String fieldName;
			if (field.getType() == String.class && (fieldName = field.getName()).startsWith("COMPILER_PB_")) {
				constantNamesIndex.put(field.get(null), fieldName);
			}
		}
		fields = IProblem.class.getFields();
		StringBuffer failures = new StringBuffer();
		StringBuffer correctResult = new StringBuffer(70000);
		Arrays.sort(fields, new Comparator() {
			public int compare(Object o1, Object o2) {
				Field field1 = (Field) o1;
				Field field2 = (Field) o2;
				return field1.getName().compareTo(field2.getName());
			}
		});
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			if (field.getType() == Integer.TYPE) {
				int problemId = field.getInt(null), maskedProblemId = problemId & IProblem.IgnoreCategoriesMask;
				if (maskedProblemId != 0 && maskedProblemId != IProblem.IgnoreCategoriesMask) {
					String name = field.getName();
					ProblemAttributes expectedAttributes = (ProblemAttributes) expectedProblemAttributes.get(name);
					String actualTuningOption = JavaCore.getOptionForConfigurableSeverity(problemId);
					if (expectedAttributes == null) {
						failures.append("missing expected problem attributes for problem " + name + "\n");
					} else if (!expectedAttributes.skip && !expectedAttributes.option.equals(actualTuningOption)) {
						failures.append("tuning option mismatch for problem " + name + " (expected " + expectedAttributes.option + ", got " + actualTuningOption + ")\n");
					}
					String optionFieldName = (String) constantNamesIndex.get(actualTuningOption);
					correctResult.append("\t\texpectedProblemAttributes.put(\"" + name + "\", " +
						(optionFieldName != null ? "new ProblemAttributes(JavaCore." + optionFieldName + ")" :
							"SKIP") + ");\n");
				}
			}
		}
		if (failures.length() > 0) {
			System.out.println(correctResult);
			System.out.println();
		}
		assertEquals(failures.toString(), 0, failures.length());
	}
	catch (IllegalAccessException e) {
		fail("could not access members");
	}
}
}
