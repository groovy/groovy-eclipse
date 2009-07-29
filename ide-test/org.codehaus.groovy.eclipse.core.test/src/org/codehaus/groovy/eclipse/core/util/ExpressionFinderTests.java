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
package org.codehaus.groovy.eclipse.core.util;

import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ParseException;
import org.codehaus.groovy.eclipse.core.util.TokenStreamException;

import junit.framework.TestCase;

public class ExpressionFinderTests extends TestCase {
	/**
	 * Tests the given expression, assuming the cursor is at end if the test string.
	 * 
	 * @param expression
	 * @throws ParseException
	 * @throws TokenStreamException
	 */
	private void doFind(String test) {
		ExpressionFinder finder = new ExpressionFinder();
		StringSourceBuffer sb = new StringSourceBuffer(test);
		try {
			assertEquals(test, finder.findForCompletions(sb, test.length() - 1));
		} catch (ParseException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Tests the expression from a given offset in the test string.
	 * 
	 * @param test
	 * @param expected
	 * @param offset
	 * @throws TokenStreamException
	 * @throws ParseException
	 */
	private void doFind(String test, String expected, int offset) {
		ExpressionFinder finder = new ExpressionFinder();
		StringSourceBuffer sb = new StringSourceBuffer(test);
		try {
			assertEquals(expected, finder.findForCompletions(sb, offset));
		} catch (ParseException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Tests the splitting of an expression into an expression and prefix part for completion.
	 * 
	 * @param test
	 *            The test expression.
	 * @param expr
	 *            The sub expression to evaluate.
	 * @param prefix
	 *            The prefix to complete.
	 */
	void doSplit(String test, String expr, String prefix) {
		ExpressionFinder finder = new ExpressionFinder();
		StringSourceBuffer sb = new StringSourceBuffer(test);
		String foundExpr;
		try {
			foundExpr = finder.findForCompletions(sb, test.length() - 1);
			String[] split = finder.splitForCompletion(foundExpr);
			assertEquals(expr, split[0]);
			assertEquals(prefix, split[1]);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test that splitting for completion fails as expected.
	 * 
	 * @param test
	 */
	void failSplit(String test) {
		ExpressionFinder finder = new ExpressionFinder();
		String[] split = finder.splitForCompletion(test);
		assertNull(split);
	}

	public void testSimple1() {
		doFind("hello");
	}

	public void testSimple2() {
		doFind("hello.name");
	}

	public void testSimple3() {
		doFind("hello.getName().");
	}

	public void testMultiple1() {
		doFind("hello.location.name");
	}

	public void testMultiple2() {
		doFind("hello.getLocation().name");
	}

	public void testMultiple3() {
		doFind("hello.location.getName().");
	}

	public void testDotted1() {
		doFind("hello.");
	}

	public void testDotted2() {
		doFind("hello.location.");
	}

	public void testComplex1() {
		doFind("10.times { println } .");
	}

	public void testComplex2() {
		doFind("a[20].");
	}

	public void testComplex3() {
		doFind("a[20].thing");
	}

	// DEFERRED: until from within method completion is implemented.
	// public void testComplex4() {
	// doFind("method(a, b,");
	// }

	public void testComplex5() {
		doFind("[1, 2, 3].collect { it.toString() } .");
	}

	public void testComplex6() {
		doFind("[1, 2, 3].collect { it.toString() }[0].");
	}

	public void testInString1() {
		// Stop when newline separates expressions.
		// Don't mess with the dot's, the auto formatter eats spaces.
		// .....0.........1.........2..
		// .....012345678901234.5678901
		doFind("println 'hello'\n10.times", "10.tim", 21);
	}

	public void testInString2() {
		// Find funky expression.
		// .....0.........1.........2.........3.........4....
		// .....012345678901234567890123456789012345678901234
		doFind("a = [1, 2, 3]; a.collect { it.toString() }.each {", "a.collect { it.toString() }.ea", 44);
	}

	public void testInString3() {
		// Do it though newlines.
		// .....0.........1.........2.........3.........4........
		// .....01234567890123456.78901234567890123456789012.3456
		doFind("a = [1, 2, 3]; a.\ncollect { it.toString() }\n.each {", "a.\ncollect { it.toString() }\n.ea", 46);
	}

	public void testSplit1() {
		doSplit("hello", "hello", null);
	}

	public void testSplit2() {
		doSplit("hello.", "hello", "");
	}

	public void testSplit3() {
		doSplit("hello.name", "hello", "name");
	}

	public void testSplit4() {
		doSplit("greet().name", "greet()", "name");
	}

	public void testSplit5() {
		doSplit("list[10].do", "list[10]", "do");
	}

	public void testSplit6() {
		doSplit("list.collect { it.toString } .", "list.collect { it.toString }", "");
	}

	public void testSplit7() {
		doSplit("list.collect { it.toString } .class", "list.collect { it.toString }", "class");
	}

	public void testFailSplit1() {
		failSplit("boo()");
	}

	public void testFailSplit2() {
		failSplit("boo[]");
	}

	public void testFailSplit3() {
		failSplit("boo{}");
	}

	public void testFailSplit4() {
		failSplit("'boo'");
	}
	
	public void testParenEOF() {
		String test = "def b = thing()\na."; 
		doFind(test, "a.", test.length() - 1);
	}
	
	public void testBraceEOF() {
		String test = "def blah() { a.";
		doFind(test, "a.", test.length() - 1);
	}

	
	public void testBraceEOFNoSpace() {
		String test = "def blah() {a.";
		doFind(test, "a.", test.length() - 1);
	}
	
	public void testWithLineComment() {
		String test = "// a comment\na.";
		doFind(test, "a.", test.length() - 1);
	}
	
	public void testWithLineComment2() {
		String test = "//\t\thelp.\n\t\ta.";
		doFind(test, "a.", test.length() - 1);
	}
	
	public void testWithLineComment3() {
		String test = "def a = 10\n//\t\thelp.\n\t\ta.";
		doFind(test, "a.", test.length() - 1);
	}
	
	public void testWithBlockComment() {
		String test = "/* a block comment */\na.";
		doFind(test, "a.", test.length() - 1);
	}
	
	public void testNewExpression() {
		doFind("new File('.').");
		doFind("new File('.').canon");
	}
}
