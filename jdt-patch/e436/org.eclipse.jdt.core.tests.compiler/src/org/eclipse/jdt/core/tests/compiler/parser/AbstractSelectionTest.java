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
import org.eclipse.jdt.internal.codeassist.select.SelectionParser;
import org.eclipse.jdt.internal.codeassist.select.SelectionScanner;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public abstract class AbstractSelectionTest extends AbstractCompilerTest {

	public final static String NONE = "<NONE>";
public AbstractSelectionTest(String testName){
	super(testName);
}
/*
 * DietParse with selectionNode check
 */
public void checkDietParse(
	char[] source,
	int selectionStart,
	int selectionEnd,
	String expectedSelection,
	String expectedUnitToString,
	String expectedSelectionIdentifier,
	String expectedSelectedSource,
	String testName) {

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	SelectionParser parser =
		new SelectionParser(
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options,
				new DefaultProblemFactory(Locale.getDefault())));

	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

	CompilationUnitDeclaration unit = parser.dietParse(sourceUnit, compilationResult, selectionStart, selectionEnd);

	String computedUnitToString = unit.toString();
	//System.out.println(computedUnitToString);
	//System.out.println(Util.displayString(computedUnitToString));
	//System.out.println(expectedUnitToString);

	String computedSelection = parser.assistNode == null
									? NONE
									: parser.assistNode.toString();
	assertEquals(
		"invalid selection node-" + testName,
		expectedSelection,
		computedSelection);

	if (!expectedUnitToString.equals(computedUnitToString)) {
		System.out.println(Util.displayString(computedUnitToString, 2));
	}
	assertEquals(
		"invalid selection unit-" + testName,
		expectedUnitToString,
		computedUnitToString);

	if (expectedSelectionIdentifier != null){
		char[] chars = ((SelectionScanner)parser.scanner).selectionIdentifier;
		String computedSelectionIdentifier = chars == null ? NONE : new String(chars);
		assertEquals(
			"invalid selection identifier-" + testName,
			expectedSelectionIdentifier,
			computedSelectionIdentifier);
	}
	if (expectedSelectedSource != null){
		char[] chars = null;
		if (parser.assistNode != null){
			chars = CharOperation.subarray(
				parser.scanner.source,
				parser.assistNode.sourceStart,
				parser.assistNode.sourceEnd + 1);
		} else {
			if (parser.assistIdentifier() != null){
				if (((SelectionScanner)parser.scanner).selectionEnd
					>= ((SelectionScanner)parser.scanner).selectionStart){
					chars = CharOperation.subarray(
						parser.scanner.source,
						((SelectionScanner)parser.scanner).selectionStart,
						((SelectionScanner)parser.scanner).selectionEnd + 1);
				}
			}
		}
		String computedSelectedSource  = chars == null ? NONE : new String(chars);
		assertEquals(
			"invalid replaced source-" + testName,
			expectedSelectedSource,
			computedSelectedSource);
	}
}
/*
 * Parse a method with selectionNode check
 */
public void checkMethodParse(
		char[] source,
		int selectionStart,
		int selectionEnd,
		String expectedSelection,
		String expectedUnitToString,
		String expectedSelectionIdentifier,
		String expectedSelectedSource,
		String[] expectedLabels,
		String testName) {

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	SelectionParser parser =
		new SelectionParser(
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options,
				new DefaultProblemFactory(Locale.getDefault())));

	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

	CompilationUnitDeclaration unit = parser.dietParse(sourceUnit, compilationResult, selectionStart, selectionEnd);

	ASTNode foundMethod = null;
	if (unit.types != null) {
		for (int i = 0; i < unit.types.length; i++) {
			TypeDeclaration type = unit.types[i];
			ASTNode method = findMethod(type, selectionStart);
			if (method != null) {
				foundMethod = method;
				break;
			}
		}
	}
	assertTrue("no method found at cursor location", foundMethod != null);
	if (foundMethod instanceof AbstractMethodDeclaration) {
		parser.parseBlockStatements((AbstractMethodDeclaration)foundMethod, unit);
	} else {
		TypeDeclaration type = (TypeDeclaration)foundMethod;
		FieldDeclaration[] fields = type.fields;
		if (fields != null) {
			for (int i = 0; i < fields.length; i++) {
				FieldDeclaration field = fields[i];
				if (field instanceof Initializer && field.sourceStart <= selectionStart && selectionStart <= field.sourceEnd) {
					parser.parseBlockStatements((Initializer)field, type, unit);
					break;
				}
			}
		}
	}

	String computedUnitToString = unit.toString();
	//System.out.println(computedUnitToString);
	//System.out.println(expectedUnitToString);

	String computedCompletion = parser.assistNode == null
								? NONE
								: parser.assistNode.toString();
	assertEquals(
		"invalid selection node-" + testName,
		expectedSelection,
		computedCompletion);

	if (!expectedUnitToString.equals(computedUnitToString)) {
		System.out.println(Util.displayString(computedUnitToString, 2));
	}
	assertEquals(
		"invalid selection unit-"+testName,
		expectedUnitToString,
		computedUnitToString);

	if (expectedSelectionIdentifier != null){
		char[] chars = ((SelectionScanner)parser.scanner).selectionIdentifier;
		String computedSelectionIdentifier = chars == null ? NONE : new String(chars);
		assertEquals(
			"invalid selection identifier-" + testName,
			expectedSelectionIdentifier,
			computedSelectionIdentifier);
	}
	if (expectedSelectedSource != null){
		char[] chars = null;
		if (parser.assistNode != null){
			chars = CharOperation.subarray(
				parser.scanner.source,
				parser.assistNode.sourceStart,
				parser.assistNode.sourceEnd + 1);
		} else {
			if (parser.assistIdentifier() != null){
				if (((SelectionScanner)parser.scanner).selectionEnd
					>= ((SelectionScanner)parser.scanner).selectionStart){
					chars = CharOperation.subarray(
						parser.scanner.source,
						((SelectionScanner)parser.scanner).selectionStart,
						((SelectionScanner)parser.scanner).selectionEnd + 1);
				}
			}
		}
		String computedReplacedSource  = chars == null ? NONE : new String(chars);
		assertEquals(
			"invalid replaced source-" + testName,
			expectedSelectedSource,
			computedReplacedSource);
	}
	if (expectedLabels != null) {
/*
		assert("no labels-" + testName, parser.labels != null);
		int length = parser.labels.length;
		assertEquals("invalid number of labels-" + testName, expectedLabels.length, length);
		for (int i = 0; i < length; i++) {
			String label = new String(parser.labels[i]);
			assertEquals("invalid label-" + testName, expectedLabels[i], label);
		}
*/
	}
}
/*
 * Parse a method with selectionNode check
 */
public void checkMethodParse(
		char[] source,
		int selectionStart,
		int selectionEnd,
		String expectedSelection,
		String expectedUnitToString,
		String expectedSelectionIdentifier,
		String expectedSelectedSource,
		String testName) {

	this.checkMethodParse(
		source,
		selectionStart,
		selectionEnd,
		expectedSelection,
		expectedUnitToString,
		expectedSelectionIdentifier,
		expectedSelectedSource,
		null,
		testName);
}
/*
 * Returns the method, the constructor or the type declaring the initializer
 * at the cursor location in the given type.
 * Returns null if not found.
 */
protected ASTNode findMethod(TypeDeclaration type, int cursorLocation) {
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
			if (field instanceof Initializer) {
				Initializer initializer = (Initializer)field;
				Block block = initializer.block;
				if (block != null && block.sourceStart <= cursorLocation && (cursorLocation <= block.sourceEnd || block.sourceEnd == 0)) {
					return type;
				}
			}
		}
	}
	return null;
}
/**
 * Runs the given test that checks that method completion parsing returns the given completion.
 */
protected void runTestCheckMethodParse(
		String compilationUnit,
		String selectionStartBehind,
		String selectionEndBehind,
		String expectedSelectionNodeToString,
		String expectedUnitDisplayString,
		String expectedSelectionIdentifier,
		String expectedReplacedSource,
		String testName) {

	int selectionStartBehindStart = compilationUnit.indexOf(selectionStartBehind);
	assertTrue("selectionStartBehind string not found", selectionStartBehindStart != -1);
	int selectionStart = selectionStartBehindStart + selectionStartBehind.length();
	int selectionEndBehindStart = compilationUnit.indexOf(selectionEndBehind);
	assertTrue("selectionEndBehind string not found", selectionEndBehindStart != -1);
	int selectionEnd = selectionEndBehindStart + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		compilationUnit.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedSelectionNodeToString,
		expectedUnitDisplayString,
		expectedSelectionIdentifier,
		expectedReplacedSource,
		testName);
}
}
