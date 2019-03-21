/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.test.core.util

import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer
import org.junit.Before
import org.junit.Test

final class StringSourceBufferTests {

    private static final String TEST_STRING = 'Hello\nGroovy\nWorld!'

    private StringSourceBuffer buffer

    @Before
    void setUp() {
        buffer = new StringSourceBuffer(TEST_STRING)
    }

    @Test
    void testLength() {
        assert buffer.length() == TEST_STRING.length()
    }

    @Test
    void testGetChar() {
        assert buffer.charAt(0) == ('H' as char)
        assert buffer.charAt(TEST_STRING.length() - 1) == ('!' as char)
    }

    @Test
    void testGetText() {
        assert buffer.subSequence(0, 'Hello'.length()) == 'Hello'
        int start = 'Hello\nGroovy\n'.length()
        int end = start + 'World!'.length()
        assert buffer.subSequence(start, end) == 'World!'
    }

    @Test
    void testToAndFrom() {
        int[] lineCol = buffer.toLineColumn(0)
        assert buffer.toOffset(lineCol[0], lineCol[1]) == 0
    }

    @Test
    void testToLineColumn() {
        int[] lineCol = buffer.toLineColumn(0)
        assert lineCol[0] == 1
        assert lineCol[1] == 1

        lineCol = buffer.toLineColumn('Hello\n'.length())
        assert lineCol[0] == 2
        assert lineCol[1] == 1

        lineCol = buffer.toLineColumn(TEST_STRING.length() - 1)
        assert lineCol[0] == 3
        assert lineCol[1] == 'World!'.length()
    }

    @Test
    void testToOffset() {
        assert buffer.toOffset(1, 1) == 0
        assert buffer.toOffset(2, 1) == 'Hello\n'.length()
        assert buffer.toOffset(3, 1) == 'Hello\nGroovy\n'.length()
        assert buffer.toOffset(3, 'World!'.length()) == TEST_STRING.length() - 1
    }

    @Test
    void testWhitespace() {
        new StringSourceBuffer('hello\r\n')
        new StringSourceBuffer('hello\r')
    }
}
