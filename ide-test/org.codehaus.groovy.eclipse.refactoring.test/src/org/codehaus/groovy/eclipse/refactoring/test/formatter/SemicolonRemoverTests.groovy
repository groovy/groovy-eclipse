/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.formatter

import org.codehaus.groovy.eclipse.refactoring.formatter.SemicolonRemover
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.ITextSelection
import org.eclipse.jface.text.TextSelection
import org.junit.Assert
import org.junit.Test

final class SemicolonRemoverTests {

    private void assertContentUnchanged(String input) {
        assertContentChangedFromTo(input, input)
    }

    private void assertContentChangedFromTo(String input, String expectedOutput) {
        assertSelectedContentChangedFromTo(null, input, expectedOutput)
    }

    private void assertSelectedContentChangedFromTo(ITextSelection selection, String input, String expectedOutput) {
        def document = new Document(input)
        def formatter = new SemicolonRemover(selection, document)

        def semicolonRemoval = formatter.format()
        semicolonRemoval.apply(document)
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
        assertContentUnchanged('def a = 10')
        assertContentUnchanged('def a = {}')
        assertContentUnchanged('def a = []')
        assertContentUnchanged('for (int i = 0; i < 5; i++) {}')
    }

    @Test
    void testFullLineComment() {
        assertContentUnchanged('// def a')
        assertContentUnchanged('/* def a; */')
        assertContentUnchanged('/* def a;\n*/')
    }

    @Test
    void testSimpleComment() {
        assertContentChangedFromTo('def a; // comment;',     'def a // comment;')
        assertContentChangedFromTo('def a; /* comment; */',  'def a /* comment; */')
        assertContentChangedFromTo('def a; /* comment;\n*/', 'def a /* comment;\n*/')
    }

    @Test
    void testCommentInComment() {
        assertContentChangedFromTo('def a; /* comment 1; // comment 2; */', 'def a /* comment 1; // comment 2; */')
        assertContentChangedFromTo('def a; /* comment 1; /* comment 2; */', 'def a /* comment 1; /* comment 2; */')
        assertContentChangedFromTo('def a; // comment 1; /* comment 2; */', 'def a // comment 1; /* comment 2; */')
        assertContentChangedFromTo('def a; // comment 1; /* comment 2;',    'def a // comment 1; /* comment 2;')
    }

    @Test
    void testMultipleTrailingComments() {
        assertContentChangedFromTo('def a; /* comment 1; */ // comment 2;',     'def a /* comment 1; */ // comment 2;')
        assertContentChangedFromTo('def a; /* comment 1; */ /* comment 2; */',  'def a /* comment 1; */ /* comment 2; */')
        assertContentChangedFromTo('def a; /* comment 1; */ /* comment 2;\n*/', 'def a /* comment 1; */ /* comment 2;\n*/')
    }

    @Test
    void testMultipleInlinedComments() {
        assertContentChangedFromTo('a = 1; /* comment 1; */ b = 2; // comment 2;',     'a = 1; /* comment 1; */ b = 2 // comment 2;')
        assertContentChangedFromTo('a = 1; /* comment 1; */ b = 2; /* comment 2; */',  'a = 1; /* comment 1; */ b = 2 /* comment 2; */')
        assertContentChangedFromTo('a = 1; /* comment 1; */ b = 2; /* comment 2;\n*/', 'a = 1; /* comment 1; */ b = 2 /* comment 2;\n*/')
    }

    @Test
    void testCommentInString() {
        assertContentChangedFromTo('def a = \'foo; // bar\'; // baz;', 'def a = \'foo; // bar\' // baz;')
        assertContentChangedFromTo('def a = "foo; // bar"; // baz;', 'def a = "foo; // bar" // baz;')
    }

    @Test
    void testSimpleRemoval() {
        assertContentChangedFromTo('def a = 10;', 'def a = 10')
        assertContentChangedFromTo('def a = {};', 'def a = {}')
        assertContentChangedFromTo('def a = [];', 'def a = []')
        assertContentChangedFromTo('def a = x;;', 'def a = x')
    }

    @Test
    void testTrailingSpacesAndTabs() {
        assertContentChangedFromTo('def a = 1 ; ',   'def a = 1  ')
        assertContentChangedFromTo('def a = 1\t;\t', 'def a = 1\t\t')
    }

    @Test
    void testCurlyBraces() {
        assertContentChangedFromTo('def a = { 1; }',           'def a = { 1 }')
        assertContentChangedFromTo('def a = [{ 1; }, { 2; }]', 'def a = [{ 1 }, { 2 }]')
        assertContentChangedFromTo('class A { def a = 1; }',   'class A { def a = 1 }')
    }

    @Test
    void testMultipleLines() {
        assertContentChangedFromTo('def a = 1;\ndef b = 2;', 'def a = 1\ndef b = 2')
    }

    @Test
    void testClosureOnNextLine1() {
        assertContentUnchanged 'def a = m();\n\t{ -> print a }'
    }

    @Test
    void testClosureOnNextLine2() {
        assertContentUnchanged 'def b = "123";\n\t{ -> b = 123 }'
    }

    @Test
    void testSelection_ifNothingIsSelected_theWholeDocumentShouldBeFormatted() {
        assertSelectedContentChangedFromTo(null, 'a = [{ 1; }, { 2; }];', 'a = [{ 1 }, { 2 }]')

        def selection = new TextSelection(5, 0) // selecting nothing
        assertSelectedContentChangedFromTo(selection, 'a = [{ 1; }, { 2; }];', 'a = [{ 1 }, { 2 }]')
    }

    @Test
    void testSelection_ifEverythingIsSelected_theWholeDocumentShouldBeFormatted() {
        def selection = new TextSelection(0, 21) // selecting everything
        assertSelectedContentChangedFromTo(selection, 'a = [{ 1; }, { 2; }];', 'a = [{ 1 }, { 2 }]')
    }

    @Test
    void testSelection_ifARegionWithAnUnnecessarySemicolonIsSelected_theSemicolonShouldBeRemoved() {
        def selection = new TextSelection(13, 6) // selecting '{ 2; }'
        assertSelectedContentChangedFromTo(selection, 'a = [{ 1; }, { 2; }];', 'a = [{ 1; }, { 2 }];')
    }

    @Test
    void testSelection_ifARegionWithANecessarySemicolonIsSelected_theSemicolonShouldNotBeRemoved() {
        def selection = new TextSelection(0, 6) // selecting 'a = 1;'
        assertSelectedContentChangedFromTo(selection, 'a = 1; b = 2;', 'a = 1; b = 2;')
    }
}
