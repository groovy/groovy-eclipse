/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.select.SelectionParser;
import org.eclipse.jdt.internal.codeassist.select.SelectionScanner;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class SelectionTest2 extends AbstractSelectionTest {

public SelectionTest2(String testName) {
	super(testName);
}
boolean thereWasAnNPE = false;
private class SpecialSelectionParser extends SelectionParser {
	public SpecialSelectionParser(ProblemReporter problemReporter) {
		super(problemReporter);
	}
	public void doNPEInParser(){
		this.stack = null;
	}
}

 SpecialSelectionParser createParser(){
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	SpecialSelectionParser parser =
		new SpecialSelectionParser(
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options,
				new DefaultProblemFactory(Locale.getDefault())));
	return parser;
}
void checkMethodParse(
		SelectionParser parser,
		char[] source,
		int selectionStart,
		int selectionEnd,
		String expectedSelection,
		String expectedUnitToString,
		String expectedSelectionIdentifier,
		String expectedSelectedSource,

		String testName) {

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
		if (type.fields != null) {
			for (int i = 0; i < type.fields.length; i++) {
				FieldDeclaration field = type.fields[i];
				if (field instanceof Initializer && field.sourceStart <= selectionStart && selectionStart <= field.sourceEnd) {
					parser.parseBlockStatements((Initializer)field, type, unit);
					break;
				}
			}
		}
	}

	String computedUnitToString = unit.toString();
	//System.out.println(computedUnitToString);
	//System.out.println(Util.displayString(computedUnitToString));
	//System.out.println(expectedUnitToString);

	String computedCompletion = parser.assistNode == null
								? NONE
								: parser.assistNode.toString();
	assertEquals(
		"invalid selection node-" + testName,
		expectedSelection,
		computedCompletion);

	assertEquals(
		"invalid selection location-"+testName,
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
}
/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=30946
 */
public void testBug30946() {
	final SpecialSelectionParser parser = createParser();
	Thread query = new Thread(
		new Runnable(){
			public void run(){
				String str =
					"public class A {\n" +
					"	void foo() {\n" +
					"		if (true) {\n" +
					"			if()\n" +
					"			switch (1) {\n" +
					"				case A.B:\n" +
					"					C d= (C) s;\n" +
					"					here\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"}n";

				String selection = "here";

				String expectedCompletionNodeToString = "<SelectOnName:here>";

				String completionIdentifier = "here";
				String expectedUnitDisplayString =
					"public class A {\n" +
					"  public A() {\n" +
					"  }\n" +
					"  void foo() {\n" +
					"    {\n" +
					"      {\n" +
					"        C d;\n" +
					"        <SelectOnName:here>;\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
				String expectedReplacedSource = "here";
				String testName = "<inifinite loop test>";

				int selectionStart = str.lastIndexOf(selection);
				int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

				try {
					SelectionTest2.this.checkMethodParse(
						parser,
						str.toCharArray(),
						selectionStart,
						selectionEnd,
						expectedCompletionNodeToString,
						expectedUnitDisplayString,
						completionIdentifier,
						expectedReplacedSource,
						testName);
				} catch (NullPointerException e) {
					SelectionTest2.this.thereWasAnNPE = true;
				}
		}
	});

	query.start();
	try {
		Thread.sleep(500);
	} catch (InterruptedException e) {
	}
	// force parser to stop
	parser.doNPEInParser();
	try {
		Thread.sleep(500);
	} catch (InterruptedException e) {
	}
	assertTrue("there is an infinite loop", !this.thereWasAnNPE);

}
}
