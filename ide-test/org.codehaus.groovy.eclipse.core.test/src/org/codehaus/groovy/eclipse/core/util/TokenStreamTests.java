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

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;

public class TokenStreamTests extends TestCase {
	static int EOF = Token.EOF;

	static int IDENT = Token.IDENT;

	static int DOT = Token.DOT;
	
	static int SEMI = Token.SEMI;
	
	static int QUOTED_STRING = Token.QUOTED_STRING;

	static int PAREN_BLOCK = Token.PAREN_BLOCK;

	static int BRACE_BLOCK = Token.BRACE_BLOCK;

	static int BRACK_BLOCK = Token.BRACK_BLOCK;
	
	static int LINE_COMMENT = Token.LINE_COMMENT;
	
	static int BLOCK_COMMENT = Token.BLOCK_COMMENT;
	
	static int LINE_BREAK = Token.LINE_BREAK;

	@Override
	protected void setUp() throws Exception {
        System.out.println("------------------------------");
        System.out.println("Starting: " + getName());
	    super.setUp();
	}
	
	void doTest(String sample, int off, int[] expected) {
	    int offset = off;
		if (offset == -1) {
			offset = sample.length() - 1;
		}

		StringSourceBuffer sb = new StringSourceBuffer(sample);
		TokenStream stream = new TokenStream(sb, offset);
		List list = new ArrayList();

		Token token = null;
		try {
			while ((token = stream.next()).type != Token.EOF) {
				list.add(token);
			}
		} catch (TokenStreamException e) {
			fail(e.getMessage());
		}
		list.add(token);

		for (int i = 0; i < expected.length; ++i) {
			assertEquals(new Token(expected[i], -1, -1, null), list.get(i));
		}
	}

	private void doTestOffsets(String sample, int[] offsetPairs) {
		int offset = sample.length() - 1;
		StringSourceBuffer sb = new StringSourceBuffer(sample);
		TokenStream stream = new TokenStream(sb, offset);
		List list = new ArrayList();

		Token token = null;
		try {
			while ((token = stream.next()).type != Token.EOF) {
				list.add(token);
			}
		} catch (TokenStreamException e) {
			fail(e.getMessage());
		}
		list.add(token);

		for (int i = 0; i < offsetPairs.length; i += 2) {
			assertEquals(offsetPairs[i], ((Token) list.get(i >> 1)).startOffset);
			assertEquals(offsetPairs[i + 1], ((Token) list.get(i >> 1)).endOffset);
		}
	}

	public void testWhite1() {
		doTest(" hello ", -1, new int[] { IDENT, EOF });
	}

	public void testWhite2() {
		doTest("\thello\t", -1, new int[] { IDENT, EOF });
	}

	public void testWhite3() {
		doTest("\rhello\r", -1, new int[] { LINE_BREAK, IDENT, LINE_BREAK, EOF });
	}

	public void testWhite4() {
		doTest("\nhello\n", -1, new int[] { LINE_BREAK, IDENT, LINE_BREAK, EOF });
	}

	public void testWhite5() {
		doTest("\r\nhello\r\n", -1, new int[] { LINE_BREAK, IDENT, LINE_BREAK, EOF });
	}

	public void testSemi() {
		doTest(";", -1, new int[] { SEMI, EOF });
	}

	public void testSimple1() {
		doTest("hello", -1, new int[] { IDENT, EOF });
	}

	public void testSimple2() {
		doTest("hello.there", -1, new int[] { IDENT, DOT, IDENT, EOF });
	}

	public void testSimple3() {
		doTest("10.times", -1, new int[] { IDENT, DOT, IDENT, EOF });
	}

	public void testSimple4() {
		doTest("10.times", 2, new int[] { DOT, IDENT, EOF });
	}

	public void testComplex1() {
		doTest("10.times { println it }", -1, new int[] { BRACE_BLOCK, IDENT, DOT, IDENT, EOF });
	}

	public void testComplex2() {
		doTest("10.times { it.times { println it } }", -1, new int[] { BRACE_BLOCK, IDENT, DOT, IDENT, EOF });
	}

	public void testComplex3() {
		doTest("hello.getName().", -1, new int[] { DOT, PAREN_BLOCK, IDENT, DOT, IDENT, EOF });
	}

	public void testComplex4() {
		doTest("list[thing[i]].name", -1, new int[] { IDENT, DOT, BRACK_BLOCK, IDENT, EOF });
	}

	public void testComplex5() {
		doTest("[1, 2, 3].collect { it.toString() } .", -1,
				new int[] { DOT, BRACE_BLOCK, IDENT, DOT, BRACK_BLOCK, EOF });
	}

	public void testComplex6() {
		doTest("a[20].", -1, new int[] { DOT, BRACK_BLOCK, IDENT, EOF });
	}

	public void testOffsets1() {
		// Don't mess with the dot's, the auto formatter eats spaces.
		// ............0.........1.........2.........3
		// ............0123456789012345678901234567890123456789
		doTestOffsets("10.times { println it }", new int[] { 9, 23, 3, 8, 2, 3, 0, 2 });
	}

	public void testOffsets2() {
		// Don't mess with the dot's, the auto formatter eats spaces.
		// ............0.........1.........2.........3
		// ............0123456789012345678901234567890123456789
		doTestOffsets("list[thing[i]].name", new int[] { 15, 19, 14, 15, 4, 14, 0, 4 });
	}
	
	public void testQuote1() {
		doTest("\"hello\"", -1, new int[] { QUOTED_STRING, EOF });
	}
	
	public void testQuote2() {
		doTest("'hello'", -1, new int[] { QUOTED_STRING, EOF });
	}
	
	public void testQuote3() {
		doTest("\"\"\"hello\"\"\"", -1, new int[] { QUOTED_STRING, EOF });
	}

	public void testQuote4() {
		doTest("'''hello'''", -1, new int[] { QUOTED_STRING, EOF });
	}
	
	public void testQuote5() {
		doTest("'boo'\n'hello'", -1, new int[] { QUOTED_STRING, LINE_BREAK, QUOTED_STRING, EOF });
	}
	
	public void testLineComment1() {
		doTest("// This is a comment\n'hello'", -1, new int[] { QUOTED_STRING, LINE_BREAK, LINE_COMMENT, EOF });
	}

	public void testLineComment2() {
		doTest("//\t\thelp.\n\t\ta.", -1, new int[] { DOT, IDENT, LINE_BREAK, LINE_COMMENT, EOF });
	}
	
	public void testLineComment3() {
		doTest("thing\n//\t\thelp.\n\t\ta.", -1, new int[] { DOT, IDENT, LINE_BREAK, LINE_COMMENT, LINE_BREAK, IDENT, EOF });
	}
	
	public void testBlockComment1() {
		doTest("/* This is a block comment in a line */\n'hello'", -1, new int[] { QUOTED_STRING, LINE_BREAK, BLOCK_COMMENT, EOF });
	}
	
	public void testBlockComment2() {
		doTest("/*\nThis is a block comment\n*/\n'hello'", -1, new int[] { QUOTED_STRING, LINE_BREAK, BLOCK_COMMENT, EOF });
	}
	

	public void testError1() {
		StringSourceBuffer sb = new StringSourceBuffer("0..1]");
		TokenStream stream = new TokenStream(sb, "0..1]".length() - 1);

		try {
			stream.next();
			fail("Expecting TokenStreamException");
		} catch (TokenStreamException e) {
		}
	}

	public void testPeek() throws TokenStreamException {
		StringSourceBuffer sb = new StringSourceBuffer("hello");
		TokenStream stream = new TokenStream(sb, "hello".length() - 1);
		assertTrue(stream.peek().type == Token.IDENT);
	}
	
	public void testLast() throws TokenStreamException {
		StringSourceBuffer sb = new StringSourceBuffer("hello.");
		TokenStream stream = new TokenStream(sb, "hello.".length() - 1);
		Token next = stream.next();
		assertTrue(stream.last() == next);
		assertTrue(stream.peek().type == Token.IDENT);
		assertTrue(stream.last() == next);
	}
}
