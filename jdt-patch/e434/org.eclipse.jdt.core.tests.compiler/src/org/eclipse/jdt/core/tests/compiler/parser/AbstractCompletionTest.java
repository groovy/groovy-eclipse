/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.codeassist.complete.CompletionScanner;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public abstract class AbstractCompletionTest extends AbstractCompilerTest {

	public final static String NONE = "<NONE>";
	public final static String NULL = "null";
public AbstractCompletionTest(String testName){
	super(testName);
}
/*
 * DietParse with completionNode check
 */
public void checkDietParse(
	char[] source,
	int cursorLocation,
	String expectedCompletion,
	String expectedUnitToString,
	String expectedCompletionIdentifier,
	String expectedReplacedSource,
	String testName) {
	this.checkDietParse(
		source,
		cursorLocation,
		expectedCompletion,
		null,
		expectedUnitToString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * DietParse with completionNode check
 */
public void checkDietParse(
	char[] source,
	int cursorLocation,
	String expectedCompletion,
	String expectedParentCompletion,
	String expectedUnitToString,
	String expectedCompletionIdentifier,
	String expectedReplacedSource,
	String testName) {

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

	CompilationUnitDeclaration unit = parser.dietParse(sourceUnit, compilationResult, cursorLocation);

	checkParse(
			expectedCompletion,
			expectedParentCompletion,
			expectedUnitToString,
			expectedCompletionIdentifier,
			expectedReplacedSource,
			testName,
			parser,
			unit);
}
/*
 * Parse a method with completionNode check
 */
public void checkMethodParse(
		char[] source,
		int cursorLocation,
		String expectedCompletion,
		String expectedParentCompletion,
		String expectedUnitToString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String[] expectedLabels,
		String testName) {

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

	CompilationUnitDeclaration unit = parser.dietParse(sourceUnit, compilationResult, cursorLocation);

	ASTNode foundMethod = null;
	if (unit.types != null) {
		for (int i = 0; i < unit.types.length; i++) {
			TypeDeclaration type = unit.types[i];
			ASTNode method = findMethod(type, cursorLocation);
			if (method != null) {
				foundMethod = method;
				break;
			}
		}
	}

	if (foundMethod != null) {
		if (foundMethod instanceof AbstractMethodDeclaration) {
			parser.parseBlockStatements((AbstractMethodDeclaration)foundMethod, unit);
		} else {
			TypeDeclaration type = (TypeDeclaration)foundMethod;
			FieldDeclaration[] fields = type.fields;
			if (fields != null) {
				done : for (int i = 0; i < fields.length; i++) {
					FieldDeclaration field = fields[i];
					if (field.declarationSourceStart <= cursorLocation && (cursorLocation <= field.declarationSourceEnd || field.declarationSourceEnd == 0)) {
						if (field instanceof Initializer) {
							parser.parseBlockStatements((Initializer)field, type, unit);
							break;
						}
						break done; // field initializer
					}
				}
			}
		}
	}

	checkParse(
			expectedCompletion,
			expectedParentCompletion,
			expectedUnitToString,
			expectedCompletionIdentifier,
			expectedReplacedSource,
			testName,
			parser,
			unit);
}
/*
 * Parse a method with completionNode check
 */
public void checkMethodParse(
		char[] source,
		int cursorLocation,
		String expectedCompletion,
		String expectedUnitToString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String testName) {

	this.checkMethodParse(
		source,
		cursorLocation,
		expectedCompletion,
		null,
		expectedUnitToString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		null,
		testName);
}
/*
 * Parse a method with completionNode check
 */
public void checkMethodParse(
		char[] source,
		int cursorLocation,
		String expectedCompletion,
		String expectedParentCompletion,
		String expectedUnitToString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String testName) {

	this.checkMethodParse(
		source,
		cursorLocation,
		expectedCompletion,
		expectedParentCompletion,
		expectedUnitToString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		null,
		testName);
}
/*
 * Parse a method with completionNode check
 */
public void checkMethodParse(
		char[] source,
		int cursorLocation,
		String expectedCompletion,
		String expectedUnitToString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String[] expectedLabels,
		String testName) {

	this.checkMethodParse(
		source,
		cursorLocation,
		expectedCompletion,
		null,
		expectedUnitToString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		expectedLabels,
		testName);
}
private void checkParse(
		String expectedCompletion,
		String expectedParentCompletion,
		String expectedUnitToString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String testName,
		CompletionParser parser,
		CompilationUnitDeclaration unit) {
	String computedCompletion = parser.assistNode == null
									? NONE
									: parser.assistNode.toString();

	String computedParentCompletion = NULL;
	if (expectedParentCompletion != null) {
		computedParentCompletion = parser.assistNodeParent == null
								? NONE
								: parser.assistNodeParent.toString();
	}

	String computedUnitToString = unit.toString();
	//System.out.println(computedUnitToString);
	//System.out.println(Util.displayString(computedUnitToString));
	//System.out.println(expectedUnitToString);

	if (!expectedCompletion.equals(computedCompletion)) {
		System.out.println(Util.displayString(computedCompletion));
	}

	if(expectedParentCompletion != null) {
		if (!expectedParentCompletion.equals(computedParentCompletion)) {
			System.out.println(Util.displayString(computedParentCompletion));
		}
	}

	if (!expectedUnitToString.equals(computedUnitToString)) {
		System.out.println(Util.displayString(computedUnitToString));
	}

	String computedCompletionIdentifier = NULL;
	if (expectedCompletionIdentifier != null){
		char[] chars = ((CompletionScanner)parser.scanner).completionIdentifier;
		computedCompletionIdentifier = chars == null ? NONE : new String(chars);
	}

	String computedReplacedSource = NULL;
	if (expectedReplacedSource != null){
		char[] chars = null;
		if (parser.assistNode != null){
			int start = parser.assistNode.sourceStart;
			int end = parser.assistNode.sourceEnd;
			if (parser.assistNode instanceof CompletionOnMemberAccess) {
				CompletionOnMemberAccess memberAccess = (CompletionOnMemberAccess) parser.assistNode;
				if (!(memberAccess.receiver instanceof ThisReference)) {
					// for these CompletionEngine uses a more specific position:
					long position = memberAccess.nameSourcePosition;
					start = (int) (position >>> 32);
					end = (int) position;
				}
			}
			chars = CharOperation.subarray(parser.scanner.source, start, end + 1);
		} else {
			if (parser.assistIdentifier() != null){
				if (((CompletionScanner)parser.scanner).completedIdentifierEnd
					>= ((CompletionScanner)parser.scanner).completedIdentifierStart){
					chars = CharOperation.subarray(
						parser.scanner.source,
						((CompletionScanner)parser.scanner).completedIdentifierStart,
						((CompletionScanner)parser.scanner).completedIdentifierEnd + 1);
				}
			}
		}
		computedReplacedSource  = chars == null ? NONE : new String(chars);
	}
	assertEquals(
			testName,
			concatResults(
					expectedCompletion,
					expectedParentCompletion,
					expectedUnitToString,
					expectedCompletionIdentifier,
					expectedReplacedSource),
			concatResults(computedCompletion,
					computedParentCompletion,
					computedUnitToString,
					computedCompletionIdentifier,
					computedReplacedSource));
}
private String concatResults(
		String completionNode,
		String parentCompletionNode,
		String unitToString,
		String completionIdentifier,
		String replacedSource) {
	StringBuilder buffer = new StringBuilder();
	buffer.append("### Completion node ###\n");
	buffer.append(completionNode);
	buffer.append("\n### Parent completion node ###\n");
	buffer.append(parentCompletionNode);
	buffer.append("\n### Completed identifier ###\n");
	buffer.append(completionIdentifier);
	buffer.append("\n### Replaced source ###\n");
	buffer.append(replacedSource);
	buffer.append("\n### Completed unit ###\n");
	buffer.append(unitToString);
	return buffer.toString();
}
/*
 * Returns the method, the constructor or the type declaring the initializer
 * at the cursor location in the given type.
 * Returns null if not found.
 */
private ASTNode findMethod(TypeDeclaration type, int cursorLocation) {
	if (type.methods != null) {
		for (int i = 0; i < type.methods.length; i++) {
			AbstractMethodDeclaration method = type.methods[i];
			if (method.declarationSourceStart <= cursorLocation && (cursorLocation <= method.declarationSourceEnd || method.declarationSourceEnd == 0)) {
				return method;
			}
		}
	}
	if (type.memberTypes != null) {
		for (int i = 0; i < type.memberTypes.length; i++) {
			TypeDeclaration memberType = type.memberTypes[i];
			ASTNode method = findMethod(memberType, cursorLocation);
			if (method != null) {
				return method;
			}
		}
	}
	FieldDeclaration[] fields = type.fields;
	if (fields != null) {
		for (int i = 0; i < fields.length; i++) {
			FieldDeclaration field = fields[i];
			if (field instanceof Initializer && field.declarationSourceStart <= cursorLocation && (cursorLocation <= field.declarationSourceEnd || field.declarationSourceEnd == 0)) {
				return type;
			}
		}
	}
	return null;
}
/**
 * Runs the given test that checks that diet completion parsing returns the given completion.
 */
protected void runTestCheckDietParse(
		String compilationUnit,
		String completeBehind,
		String expectedCompletionNodeToString,
		String expectedUnitDisplayString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String testName) {

	int cursorLocation = compilationUnit.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		compilationUnit.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Runs the given test that checks that method completion parsing returns the given completion.
 */
protected void runTestCheckMethodParse(
		String compilationUnit,
		String completeBehind,
		String expectedCompletionNodeToString,
		String expectedUnitDisplayString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String[] expectedLabels,
		String testName) {

	int completeBehindStart = compilationUnit.indexOf(completeBehind);
	assertTrue("completeBehind string not found", completeBehindStart >= 0);
	int cursorLocation = completeBehindStart + completeBehind.length() - 1;
	this.checkMethodParse(
		compilationUnit.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		expectedLabels,
		testName);
}
/**
 * Runs the given test that checks that method completion parsing returns the given completion.
 */
protected void runTestCheckMethodParse(
		String compilationUnit,
		String completeBehind,
		String expectedCompletionNodeToString,
		String expectedUnitDisplayString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String testName) {

	this.runTestCheckMethodParse(
		compilationUnit,
		completeBehind,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		null,
		testName);
}
}
