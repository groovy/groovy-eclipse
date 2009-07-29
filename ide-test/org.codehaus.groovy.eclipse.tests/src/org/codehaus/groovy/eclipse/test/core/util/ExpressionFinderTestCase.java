/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.test.core.util;

import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;

import junit.framework.*;

/**
 * @author Heiko Boettger
 */
public class ExpressionFinderTestCase extends TestCase {

	public void test_identifierExpression() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { bbbb. } }";
		String completionLocation = "class Test { public testFunction() { bbbb.";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("bbbb.", expression);
	}

	public void test_parenExpression() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { (bbbb). } }";
		String completionLocation = "class Test { public testFunction() { (bbbb).";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("(bbbb).", expression);
	}

	public void test_arrayElementExpressionInParen() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { (bbbb[10]). } }";
		String completionLocation = "class Test { public testFunction() { (bbbb[10]).";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("(bbbb[10]).", expression);
	}

	public void test_arrayElementExpression() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { bbbb[10]. } }";
		String completionLocation = "class Test { public testFunction() { bbbb[10].";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("bbbb[10].", expression);
	}

	public void test_parenInParenExpression() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { (([10])). } }";
		String completionLocation = "class Test { public testFunction() { (([10])).";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("(([10])).", expression);
	}

	public void test_parenInParenExpression2() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { (([10\n\n  ])). } }";
		String completionLocation = "class Test { public testFunction() { (([10\n\n  ])).";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("(([10\n\n  ])).", expression);
	}

	public void test_stringExpression() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { \"13\". } }";
		String completionLocation = "class Test { public testFunction() { \"13\".";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("\"13\".", expression);
	}

	public void test_stringExpressionAfterOtherExpressionAndNewLine()
			throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { int a = 10\n   \"13\". } }";
		String completionLocation = "class Test { public testFunction() { int a = 10\n   \"13\".";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("\"13\".", expression);
	}

	public void test_stringExpressionAfterOtherExpressionAndSemi()
			throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { int a = 10;   \"13\". } }";
		String completionLocation = "class Test { public testFunction() { int a = 10;   \"13\".";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("\"13\".", expression);
	}

	public void test_expressionInGString() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { \"anyString${a.}\" } }";
		String completionLocation = "class Test { public testFunction() { \"anyString${a.";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("a.", expression);
	}

	public void test_expressionInParenAfterNewLine() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { int a = 100\n    (new ArrayList()). } }";
		String completionLocation = "class Test { public testFunction() { int a = 100\n    (new ArrayList()).";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("(new ArrayList()).", expression);
	}

	public void test_expressionAfterNewLine() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { int a = 100\n    new ArrayList(). } }";
		String completionLocation = "class Test { public testFunction() { int a = 100\n    new ArrayList().";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("new ArrayList().", expression);
	}

	public void test_noExpression1() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() {    } }";
		String completionLocation = "class Test { public testFunction() { ";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals(null, expression);
	}

	public void test_noExpression2() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { int a = 100    } }";
		String completionLocation = "class Test { public testFunction() { int a = 100  ";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("", expression);
	}

	public void test_noExpression3() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { int a = 100\n    } }";
		String completionLocation = "class Test { public testFunction() { int a = 100\n  ";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("", expression);
	}

	public void test_expressionAfterNumber() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { int a = 100\n    } }";
		String completionLocation = "class Test { public testFunction() { int a = 100";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("100", expression);
	}
	public void test_expressionAfterStringLiteral() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { def a = \"fff\"\n    } }";
		String completionLocation = "class Test { public testFunction() { int a = \"fff\"";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals(null, expression);
	}

	public void test_expressionInParenAfterSemi() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { int a = 100;    (new ArrayList()). } }";
		String completionLocation = "class Test { public testFunction() { int a = 100;    (new ArrayList()).";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("(new ArrayList()).", expression);
	}

	public void test_expressionForNewGenericType() throws Exception {
		ExpressionFinder finder = new ExpressionFinder();
		String source = "class Test { public testFunction() { int a = 100;    (new ArrayList<String>()). } }";
		String completionLocation = "class Test { public testFunction() { int a = 100;    (new ArrayList<String>()).";
		ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

		String expression = finder.findForCompletions(sourceBuffer, completionLocation
				.length() - 1);
		Assert.assertEquals("(new ArrayList<String>()).", expression);
	}
}
