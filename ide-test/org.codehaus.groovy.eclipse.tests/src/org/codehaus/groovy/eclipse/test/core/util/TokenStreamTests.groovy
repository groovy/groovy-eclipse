/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.core.util

import static org.codehaus.groovy.eclipse.core.util.Token.Type.*

import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer
import org.codehaus.groovy.eclipse.core.util.Token
import org.codehaus.groovy.eclipse.core.util.TokenStream
import org.codehaus.groovy.eclipse.core.util.TokenStreamException
import org.junit.Test

final class TokenStreamTests {

    private void doTest(String sample, int off, Token.Type... expected) {
        int offset = off
        if (offset == -1) {
            offset = sample.length() - 1
        }
        StringSourceBuffer sb = new StringSourceBuffer(sample)
        TokenStream stream = new TokenStream(sb, offset)
        List<Token> list = []
        Token token = null
        while (!(token = stream.next()).isType(EOF)) {
            list.add(token)
        }
        list.add(token)
        for (int i = 0; i < expected.length; i += 1) {
            assert list.get(i) == new Token(expected[i], -1, -1, null)
        }
    }

    private void doTestOffsets(String sample, int[] offsetPairs) {
        int offset = sample.length() - 1
        StringSourceBuffer sb = new StringSourceBuffer(sample)
        TokenStream stream = new TokenStream(sb, offset)
        List<Token> list = []
        Token token = null
        while (!(token = stream.next()).isType(EOF)) {
            list.add(token)
        }
        list.add(token)
        for (int i = 0; i < offsetPairs.length; i += 2) {
            assert list.get(i >> 1).startOffset == offsetPairs[i]
            assert list.get(i >> 1).endOffset == offsetPairs[i + 1]
        }
    }

    @Test
    void testWhite1() {
        doTest(' hello ', -1, IDENT, EOF)
    }

    @Test
    void testWhite2() {
        doTest('\thello\t', -1, IDENT, EOF)
    }

    @Test
    void testWhite3() {
        doTest('\rhello\r', -1, LINE_BREAK, IDENT, LINE_BREAK, EOF)
    }

    @Test
    void testWhite4() {
        doTest('\nhello\n', -1, LINE_BREAK, IDENT, LINE_BREAK, EOF)
    }

    @Test
    void testWhite5() {
        doTest('\r\nhello\r\n', -1, LINE_BREAK, IDENT, LINE_BREAK, EOF)
    }

    @Test
    void testSemi() {
        doTest(';', -1, SEMI, EOF)
    }

    @Test
    void testSimple1() {
        doTest('hello', -1, IDENT, EOF)
    }

    @Test
    void testSimple2() {
        doTest('hello.there', -1, IDENT, DOT, IDENT, EOF)
    }

    @Test
    void testSimple3() {
        doTest('10.times', -1, IDENT, DOT, IDENT, EOF)
    }

    @Test
    void testSimple4() {
        doTest('10.times', 2, DOT, IDENT, EOF)
    }

    @Test
    void testComplex1() {
        doTest('10.times { println it }', -1, BRACE_BLOCK, IDENT, DOT, IDENT, EOF)
    }

    @Test
    void testComplex2() {
        doTest('10.times { it.times { println it } }', -1, BRACE_BLOCK, IDENT, DOT, IDENT, EOF)
    }

    @Test
    void testComplex3() {
        doTest('hello.getName().', -1, DOT, PAREN_BLOCK, IDENT, DOT, IDENT, EOF)
    }

    @Test
    void testComplex4() {
        doTest('list[thing[i]].name', -1, IDENT, DOT, BRACK_BLOCK, IDENT, EOF)
    }

    @Test
    void testComplex5() {
        doTest('[1, 2, 3].collect { it.toString() } .', -1, DOT, BRACE_BLOCK, IDENT, DOT, BRACK_BLOCK, EOF)
    }

    @Test
    void testComplex6() {
        doTest('a[20].', -1, DOT, BRACK_BLOCK, IDENT, EOF)
    }

    @Test
    void testQuote1() {
        doTest('\"hello\"', -1, QUOTED_STRING, EOF)
    }

    @Test
    void testQuote2() {
        doTest('\'hello\'', -1, QUOTED_STRING, EOF)
    }

    @Test
    void testQuote3() {
        doTest('\"\"\"hello\"\"\"', -1, QUOTED_STRING, EOF)
    }

    @Test
    void testQuote4() {
        doTest('\'\'\'hello\'\'\'', -1, QUOTED_STRING, EOF)
    }

    @Test
    void testQuote5() {
        doTest('\'boo\'\n\'hello\'', -1, QUOTED_STRING, LINE_BREAK, QUOTED_STRING, EOF)
    }

    @Test
    void testArray1() {
        doTest('foo[\'foo\'].x', -1, IDENT, DOT, BRACK_BLOCK, IDENT, EOF)
    }

    @Test
    void testArray2() {
        doTest('foo[\'foo\'][\'foo\'].x', -1, IDENT, DOT, BRACK_BLOCK, BRACK_BLOCK, IDENT, EOF)
    }

    @Test
    void testArray3() {
        doTest('foo[foo[0]][\'foo\'].x', -1, IDENT, DOT, BRACK_BLOCK, BRACK_BLOCK, IDENT, EOF)
    }

    @Test
    void testArray4() {
        doTest('foo{ }[\'foo\'].x', -1, IDENT, DOT, BRACK_BLOCK, BRACE_BLOCK, IDENT, EOF)
    }

    @Test
    void testArray5() {
        doTest('foo[\'foo\']{ }', -1, BRACE_BLOCK, BRACK_BLOCK, IDENT, EOF)
    }

    @Test
    void testBlockComment1() {
        doTest('/* This is a block comment in a line */\n\'hello\'', -1, QUOTED_STRING, LINE_BREAK, BLOCK_COMMENT, EOF)
    }

    @Test
    void testBlockComment2() {
        doTest('/*\nThis is a block comment\n*/\n\'hello\'', -1, QUOTED_STRING, LINE_BREAK, BLOCK_COMMENT, EOF)
    }

    @Test
    void testLineComment1() {
        doTest('// This is a comment\n\'hello\'', -1, QUOTED_STRING, LINE_BREAK, LINE_COMMENT, EOF)
    }

    @Test
    void testLineComment2() {
        doTest('//\t\thelp.\n\t\ta.', -1, DOT, IDENT, LINE_BREAK, LINE_COMMENT, EOF)
    }

    @Test
    void testLineComment3() {
        doTest('thing\n//\t\thelp.\n\t\ta.', -1, DOT, IDENT, LINE_BREAK, LINE_COMMENT, LINE_BREAK, IDENT, EOF)
    }

    @Test
    void testFauxLineComment() {
        doTest('"//";\nfrag', -1, IDENT, LINE_BREAK, SEMI, QUOTED_STRING, EOF)
    }

    @Test
    void testSafeDeref() {
        doTest('foo?.bar', -1, IDENT, SAFE_DEREF, IDENT, EOF)
    }

    @Test
    void testSpread() {
        doTest('foo*.bar', -1, IDENT, SPREAD, IDENT, EOF)
    }

    @Test
    void testFieldAccess() {
        doTest('foo.@bar', -1, IDENT, FIELD_ACCESS, IDENT, EOF)
    }

    @Test
    void testMethodPointer() {
        doTest('foo.&bar', -1, IDENT, METHOD_POINTER, IDENT, EOF)
    }

    @Test
    void testPeek() {
        TokenStream stream = new TokenStream(new StringSourceBuffer('hello'), 4)
        assert stream.peek().isType(IDENT)
    }

    @Test
    void testLast() {
        TokenStream stream = new TokenStream(new StringSourceBuffer('hello.'), 5)
        Token next = stream.next()
        assert stream.last() == next
        assert stream.peek().isType(IDENT)
        assert stream.last() == next
    }

    @Test(expected=TokenStreamException)
    void testError1() throws Exception {
        TokenStream stream = new TokenStream(new StringSourceBuffer('0..1]'), 4)
        stream.next()
    }

    @Test
    void testOffsets1() {
        //.............0.........1.........2..
        //.............01234567890123456789012
        doTestOffsets('10.times { println it }', [9, 23, 3, 8, 2, 3, 0, 2] as int[])
    }

    @Test
    void testOffsets2() {
        //.............0.........1.........2.........3
        //.............0123456789012345678901234567890123456789
        doTestOffsets('list[thing[i]].name', [15, 19, 14, 15, 4, 14, 0, 4] as int[])
    }
}
