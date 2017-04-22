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
package org.codehaus.groovy.eclipse.refactoring.test.formatter

import org.codehaus.groovy.eclipse.refactoring.formatter.WhitespaceRemover
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.ITextSelection
import org.junit.Assert
import org.junit.Test

final class WhitespaceRemoverTests {

    private void assertContentUnchanged(String input) {
        assertContentChangedFromTo(input, input)
    }

    private void assertContentChangedFromTo(String input, String expectedOutput) {
        assertSelectedContentChangedFromTo(null, input, expectedOutput)
    }

    private void assertSelectedContentChangedFromTo(ITextSelection selection, String input, String expectedOutput) {
        def document = new Document(input)
        def formatter = new WhitespaceRemover(selection, document)

        def whitespaceRemoval = formatter.format()
        whitespaceRemoval.apply(document)
        String actualOutput = document.get()

        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    void testNullContent() {
        assertContentChangedFromTo(null, '')
    }

    @Test
    void testEmptyDocument() {
        assertContentUnchanged('')
    }

    @Test
    void testNothingToRemove() {
        assertContentUnchanged(' def a')
        assertContentUnchanged(' def a\n def b')
        assertContentUnchanged(' // def a')
        assertContentUnchanged(' /* def a */')
    }

    @Test
    void testRemoveTrailingSpacesInComments() {
        assertContentChangedFromTo('// def a  ',     '// def a')
        assertContentChangedFromTo('/* def a */  ',  '/* def a */')
        assertContentChangedFromTo('/* def a  \n*/', '/* def a\n*/')
    }

    @Test
    void testRemoveTrailingTabsInComments() {
        assertContentChangedFromTo('// def a\t\t',     '// def a')
        assertContentChangedFromTo('/* def a */\t\t',  '/* def a */')
        assertContentChangedFromTo('/* def a\t\t\n*/', '/* def a\n*/')
    }

    @Test
    void testRemoveTrailingSpacesInCode() {
        assertContentChangedFromTo('def a  ', 'def a')
    }

    @Test
    void testRemoveTrailingTabsInCode() {
        assertContentChangedFromTo('def a\t\t', 'def a')
    }

    @Test
    void testRemoveTrailingSpacesInMultipleLines() {
        assertContentChangedFromTo('def a = 1  \ndef b = 2  ',   'def a = 1\ndef b = 2')
        assertContentChangedFromTo('def a = 1  \rdef b = 2  ',   'def a = 1\rdef b = 2')
        assertContentChangedFromTo('def a = 1  \r\ndef b = 2  ', 'def a = 1\r\ndef b = 2')
    }
}
