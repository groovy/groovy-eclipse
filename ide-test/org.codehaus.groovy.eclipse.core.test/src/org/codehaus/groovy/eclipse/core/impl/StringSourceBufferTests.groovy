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
package org.codehaus.groovy.eclipse.core.impl

import junit.framework.TestCase

final class StringSourceBufferTests extends TestCase {

    private StringSourceBuffer buffer
    private static final String TEST_STRING = 'Hello\nGroovy\nWorld!'

    @Override
    protected void setUp() throws Exception {
        println '----------------------------------------'
        println 'Starting: ' + getName()
        super.setUp()

        buffer = new StringSourceBuffer(TEST_STRING)
    }

    void testLength() {
        assertEquals(TEST_STRING.length(), buffer.length())
    }

    void testGetChar() {
        assertEquals('H' as char, buffer.charAt(0))
        assertEquals('!' as char, buffer.charAt(TEST_STRING.length() - 1))
    }

    void testGetText() {
        assertEquals('Hello', buffer.subSequence(0, 'Hello'.length()))
        int start = 'Hello\nGroovy\n'.length()
        int end = start + 'World!'.length()
        assertEquals('World!', buffer.subSequence(start, end))
    }

    void testToAndFrom() {
        int[] lineCol = buffer.toLineColumn(0)
        assertEquals(0, buffer.toOffset(lineCol[0], lineCol[1]))
    }

    void testToLineColumn() {
        int[] lineCol = buffer.toLineColumn(0)
        assertEquals(1, lineCol[0])
        assertEquals(1, lineCol[1])

        lineCol = buffer.toLineColumn('Hello\n'.length())
        assertEquals(2, lineCol[0])
        assertEquals(1, lineCol[1])

        lineCol = buffer.toLineColumn(TEST_STRING.length() - 1)
        assertEquals(3, lineCol[0])
        assertEquals('World!'.length(), lineCol[1])
    }

    void testToOffset() {
        assertEquals(0, buffer.toOffset(1, 1))
        assertEquals('Hello\n'.length(), buffer.toOffset(2, 1))
        assertEquals('Hello\nGroovy\n'.length(), buffer.toOffset(3, 1))
        assertEquals(TEST_STRING.length() - 1, buffer.toOffset(3, 'World!'.length()))
    }

    void testWhite() {
        new StringSourceBuffer('hello\r\n')
        new StringSourceBuffer('hello\r')
    }
}
