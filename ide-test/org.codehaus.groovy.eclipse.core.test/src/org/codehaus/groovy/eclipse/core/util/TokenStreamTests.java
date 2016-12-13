/*
 * Copyright 2009-2016 the original author or authors.
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

import static org.codehaus.groovy.eclipse.core.util.Token.Type.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;

public class TokenStreamTests extends TestCase {

    @Override
    protected void setUp() throws Exception {
        System.out.println("----------------------------------------");
        System.out.println("Starting: " + getName());
        super.setUp();
    }

    void doTest(String sample, int off, Token.Type... expected) throws TokenStreamException {
        int offset = off;
        if (offset == -1) {
            offset = sample.length() - 1;
        }

        StringSourceBuffer sb = new StringSourceBuffer(sample);
        TokenStream stream = new TokenStream(sb, offset);
        List<Token> list = new ArrayList<Token>();

        Token token = null;
        while (!(token = stream.next()).isType(Token.Type.EOF)) {
            list.add(token);
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
        List<Token> list = new ArrayList<Token>();

        Token token = null;
        try {
            while (!(token = stream.next()).isType(Token.Type.EOF)) {
                list.add(token);
            }
        } catch (TokenStreamException e) {
            fail(e.getMessage());
        }
        list.add(token);

        for (int i = 0; i < offsetPairs.length; i += 2) {
            assertEquals(offsetPairs[i], list.get(i >> 1).startOffset);
            assertEquals(offsetPairs[i + 1], list.get(i >> 1).endOffset);
        }
    }

    public void testWhite1() throws Exception {
        doTest(" hello ", -1, IDENT, EOF);
    }

    public void testWhite2() throws Exception {
        doTest("\thello\t", -1, IDENT, EOF);
    }

    public void testWhite3() throws Exception {
        doTest("\rhello\r", -1, LINE_BREAK, IDENT, LINE_BREAK, EOF);
    }

    public void testWhite4() throws Exception {
        doTest("\nhello\n", -1, LINE_BREAK, IDENT, LINE_BREAK, EOF);
    }

    public void testWhite5() throws Exception {
        doTest("\r\nhello\r\n", -1, LINE_BREAK, IDENT, LINE_BREAK, EOF);
    }

    public void testSemi() throws Exception {
        doTest(";", -1, SEMI, EOF);
    }

    public void testSimple1() throws Exception {
        doTest("hello", -1, IDENT, EOF);
    }

    public void testSimple2() throws Exception {
        doTest("hello.there", -1, IDENT, DOT, IDENT, EOF);
    }

    public void testSimple3() throws Exception {
        doTest("10.times", -1, IDENT, DOT, IDENT, EOF);
    }

    public void testSimple4() throws Exception {
        doTest("10.times", 2, DOT, IDENT, EOF);
    }

    public void testComplex1() throws Exception {
        doTest("10.times { println it }", -1, BRACE_BLOCK, IDENT, DOT, IDENT, EOF);
    }

    public void testComplex2() throws Exception {
        doTest("10.times { it.times { println it } }", -1, BRACE_BLOCK, IDENT, DOT, IDENT, EOF);
    }

    public void testComplex3() throws Exception {
        doTest("hello.getName().", -1, DOT, PAREN_BLOCK, IDENT, DOT, IDENT, EOF);
    }

    public void testComplex4() throws Exception {
        doTest("list[thing[i]].name", -1, IDENT, DOT, BRACK_BLOCK, IDENT, EOF);
    }

    public void testComplex5() throws Exception {
        doTest("[1, 2, 3].collect { it.toString() } .", -1, DOT, BRACE_BLOCK, IDENT, DOT, BRACK_BLOCK, EOF);
    }

    public void testComplex6() throws Exception {
        doTest("a[20].", -1, DOT, BRACK_BLOCK, IDENT, EOF);
    }

    public void testQuote1() throws Exception {
        doTest("\"hello\"", -1, QUOTED_STRING, EOF);
    }

    public void testQuote2() throws Exception {
        doTest("'hello'", -1, QUOTED_STRING, EOF);
    }

    public void testQuote3() throws Exception {
        doTest("\"\"\"hello\"\"\"", -1, QUOTED_STRING, EOF);
    }

    public void testQuote4() throws Exception {
        doTest("'''hello'''", -1, QUOTED_STRING, EOF);
    }

    public void testQuote5() throws Exception {
        doTest("'boo'\n'hello'", -1, QUOTED_STRING, LINE_BREAK, QUOTED_STRING, EOF);
    }

    public void testArray1() throws TokenStreamException {
        doTest("foo['foo'].x", -1, IDENT, DOT, BRACK_BLOCK, IDENT, EOF);
    }

    public void testArray2() throws TokenStreamException {
        doTest("foo['foo']['foo'].x", -1, IDENT, DOT, BRACK_BLOCK, BRACK_BLOCK, IDENT, EOF);
    }

    public void testArray3() throws TokenStreamException {
        doTest("foo[foo[0]]['foo'].x", -1, IDENT, DOT, BRACK_BLOCK, BRACK_BLOCK, IDENT, EOF);
    }

    public void testArray4() throws TokenStreamException {
        doTest("foo{ }['foo'].x", -1, IDENT, DOT, BRACK_BLOCK, BRACE_BLOCK, IDENT, EOF);
    }

    public void testArray5() throws TokenStreamException {
        doTest("foo['foo']{ }", -1, BRACE_BLOCK, BRACK_BLOCK, IDENT, EOF);
    }

    public void testLineComment1() throws Exception {
        doTest("// This is a comment\n'hello'", -1, QUOTED_STRING, LINE_BREAK, LINE_COMMENT, EOF);
    }

    public void testLineComment2() throws Exception {
        doTest("//\t\thelp.\n\t\ta.", -1, DOT, IDENT, LINE_BREAK, LINE_COMMENT, EOF);
    }

    public void testLineComment3() throws Exception {
        doTest("thing\n//\t\thelp.\n\t\ta.", -1, DOT, IDENT, LINE_BREAK, LINE_COMMENT, LINE_BREAK, IDENT, EOF);
    }

    public void testBlockComment1() throws Exception {
        doTest("/* This is a block comment in a line */\n'hello'", -1, QUOTED_STRING, LINE_BREAK, BLOCK_COMMENT, EOF);
    }

    public void testBlockComment2() throws Exception {
        doTest("/*\nThis is a block comment\n*/\n'hello'", -1, QUOTED_STRING, LINE_BREAK, BLOCK_COMMENT, EOF);
    }

    public void testSafeDeref() throws Exception {
        doTest("foo?.bar", -1, IDENT, SAFE_DEREF, IDENT, EOF);
    }

    public void testSpread() throws Exception {
        doTest("foo*.bar", -1, IDENT, SPREAD, IDENT, EOF);
    }

    public void testFieldAccess() throws Exception {
        doTest("foo.@bar", -1, IDENT, FIELD_ACCESS, IDENT, EOF);
    }

    public void testMethodPointer() throws Exception {
        doTest("foo.&bar", -1, IDENT, METHOD_POINTER, IDENT, EOF);
    }

    public void testError1() throws Exception {
        StringSourceBuffer sb = new StringSourceBuffer("0..1]");
        TokenStream stream = new TokenStream(sb, "0..1]".length() - 1);

        try {
            stream.next();
            fail("Expecting TokenStreamException");
        } catch (TokenStreamException e) {
        }
    }

    public void testOffsets1() throws Exception {
        // Don't mess with the dot's, the auto formatter eats spaces.
        // ............0.........1.........2.........3
        // ............0123456789012345678901234567890123456789
        doTestOffsets("10.times { println it }", new int[] { 9, 23, 3, 8, 2, 3, 0, 2 });
    }

    public void testOffsets2() throws Exception {
        // Don't mess with the dot's, the auto formatter eats spaces.
        // ............0.........1.........2.........3
        // ............0123456789012345678901234567890123456789
        doTestOffsets("list[thing[i]].name", new int[] { 15, 19, 14, 15, 4, 14, 0, 4 });
    }

    public void testPeek() throws TokenStreamException {
        StringSourceBuffer sb = new StringSourceBuffer("hello");
        TokenStream stream = new TokenStream(sb, "hello".length() - 1);
        assertTrue(stream.peek().isType(Token.Type.IDENT));
    }

    public void testLast() throws TokenStreamException {
        StringSourceBuffer sb = new StringSourceBuffer("hello.");
        TokenStream stream = new TokenStream(sb, "hello.".length() - 1);
        Token next = stream.next();
        assertTrue(stream.last() == next);
        assertTrue(stream.peek().isType(Token.Type.IDENT));
        assertTrue(stream.last() == next);
    }
}
