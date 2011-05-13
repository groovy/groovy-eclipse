 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.core.util;

import junit.framework.TestCase;

import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;

public class ExpressionFinderTests extends TestCase {

    @Override
    protected void setUp() throws Exception {
        System.out.println("------------------------------");
        System.out.println("Starting: " + getName());
        super.setUp();
    }
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
		assertEquals(2, split.length);
		assertEquals("", split[0]);
        assertEquals(null, split[1]);
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

    public void testFailSplit5() {
        failSplit("//\n");
    }

    public void testFailSplit6() {
        failSplit("//fdsafdsfasddsfa\n     ");
    }

    public void testFailSplit7() {
        failSplit("//fdsafdsfasddsfa\nboo[]     ");
    }

    public void testFailSplit8() {
        failSplit("/*fdsafdsfasddsfa*/ \nboo[]     ");
    }

    public void testFailSplit9() {
        failSplit("/* // fdsafdsfasddsfa*/ \nboo[]     ");
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

    public void testProblem() throws Exception {
        // this used to throw an exception, but should not
        ExpressionFinder finder = new ExpressionFinder();
        String test = ".";
        StringSourceBuffer sb = new StringSourceBuffer(test);
        assertNull(finder.findForCompletions(sb, test.length() - 1));
    }
}
