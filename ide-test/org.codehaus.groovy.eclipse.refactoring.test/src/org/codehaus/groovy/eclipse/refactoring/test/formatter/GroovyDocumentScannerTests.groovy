/*
 * Copyright 2009-2020 the original author or authors.
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

import static org.junit.Assert.*

import groovy.transform.CompileStatic

import groovyjarjarantlr.Token
import org.codehaus.groovy.antlr.GroovyTokenTypeBridge
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyDocumentScanner
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.junit.Test

@CompileStatic
final class GroovyDocumentScannerTests {

    private int caret
    private Document editDoc

    private IDocument getDocument() {
        return editDoc
    }

    private void makeEditor(String string) {
        caret = string.indexOf('<***>')
        if (caret < 0) {
            caret = 0
        } else {
            string = string.substring(0, caret) + string.substring(caret + '<***>'.length())
        }
        editDoc = new Document(string)
    }

    /**
     * Simulates an edit cause by typing some text.
     * <p>
     * Note: unlike in GroovyEditorTests this doesn't actually use the editor, so there
     * is no smart processing at all. The text is just inserted into the document, at the
     * position of the caret.
     */
    private void send(String insertionText) {
        editDoc.replace(caret, 0, insertionText)
        caret = caret + insertionText.length()
    }

    private void assertTokens(Collection<String> expected, Collection<Token> tokens) {
        assertEquals(expected.size(), tokens.size())
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected[i], tokens[i].text)
        }
    }

    //

    @Test
    void testGetTokenBefore() {
        makeEditor('a b c' + '\n' + 'd e f')

        IDocument doc = getDocument()
        GroovyDocumentScanner scanner = new GroovyDocumentScanner(doc)

        //See if we can fetch tokens in reverse order...
        String[] expected = [ '', // EOF token has no text
                'f', 'e', 'd', '<newline>', 'c', 'b', 'a'
        ]

        int expect = 0
        Token token = scanner.lastToken
        assertEquals(GroovyTokenTypeBridge.EOF, token.type)

        while (token != null) {
            assertEquals(expected[expect++], token.text)
            token = scanner.getLastTokenBefore(token)
        }

        assertEquals(expected.length, expect)
        scanner.dispose()
    }

    @Test
    void testGetLineTokens() {
        String text = 'a b c\n' + 'd e f'
        makeEditor(text)

        IDocument doc = getDocument()
        GroovyDocumentScanner scanner = new GroovyDocumentScanner(doc)

        List<Token> tokens = scanner.getLineTokensUpto(text.indexOf('c'))
        assertTokens(['a', 'b'], tokens)

        tokens = scanner.getLineTokensUpto(text.indexOf('f'))
        assertTokens(['d', 'e'], tokens)
        scanner.dispose()
    }

    @Test
    void testGetEmptyLineTokens() {
        String text =
            'class Foo {\n' +
            '   \n' +
            '   \n' +
            '}'
        makeEditor(text)

        IDocument doc = getDocument()
        GroovyDocumentScanner scanner = new GroovyDocumentScanner(doc)

        List<Token> tokens = scanner.getLineTokens(0)
        assertTokens(['class', 'Foo', '{', '<newline>'], tokens)

        tokens = scanner.getLineTokens(1)
        assertTokens([], tokens)

        tokens = scanner.getLineTokens(2)
        assertTokens([], tokens)

        tokens = scanner.getLineTokens(3)
        assertTokens(['}'], tokens)
        scanner.dispose()
    }

    @Test
    void testDocumentEdits() {
        String text =
            'class Foo {\n' +
            '    def a = <***>\n' +
            '}\n'
        makeEditor(text)

        IDocument doc = getDocument()
        GroovyDocumentScanner scanner = new GroovyDocumentScanner(doc)

        List<Token> tokens = scanner.getLineTokens(1)
        assertTokens(['def', 'a', '=', '<newline>'], tokens)

        send('3+4')
        tokens = scanner.getLineTokens(1)
        assertTokens(['def', 'a', '=', '3', '+', '4', '<newline>'], tokens)
        scanner.dispose()
    }
}
