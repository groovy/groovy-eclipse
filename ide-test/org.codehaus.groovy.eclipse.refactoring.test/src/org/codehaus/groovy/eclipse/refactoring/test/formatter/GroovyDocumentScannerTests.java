/*
 * Copyright 2010-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.formatter;

import java.util.List;

import junit.framework.TestCase;

import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyDocumentScanner;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import antlr.Token;

/**
 * @author kdvolder
 * @created 2010-05-26
 */
public class GroovyDocumentScannerTests extends TestCase {

    private Document editDoc;
    private int caret = 0;

    public void testGetTokenBefore() throws Exception {
        makeEditor("a b c" + "\n" + "d e f");

        IDocument doc = getDocument();
        GroovyDocumentScanner scanner = new GroovyDocumentScanner(doc);

        //See if we can fetch tokens in reverse order...
        String[] expected = new String[] { "", // EOF token has no text
                "f", "e", "d", "<newline>", "c", "b", "a"
        };

        int expect = 0;
        Token token = scanner.getLastToken();
        assertEquals(GroovyTokenTypeBridge.EOF, token.getType());

        while (token != null) {
            assertEquals(expected[expect++], token.getText());
            token = scanner.getLastTokenBefore(token);
        }

        assertEquals(expected.length, expect);
        scanner.dispose();
    }

    public void testGetLineTokens() throws Exception {
        String text = "a b c\n" + "d e f";
        makeEditor(text);

        IDocument doc = getDocument();
        GroovyDocumentScanner scanner = new GroovyDocumentScanner(doc);

        List<Token> tokens = scanner.getLineTokensUpto(text.indexOf("c"));
        assertTokens(new String[] { "a", "b" }, tokens);

        tokens = scanner.getLineTokensUpto(text.indexOf("f"));
        assertTokens(new String[] { "d", "e" }, tokens);
        scanner.dispose();
    }

    public void testGetEmptyLineTokens() throws Exception {
        String text =
            "class Foo {\n" +
            "   \n" +
            "   \n" +
            "}";
        makeEditor(text);

        IDocument doc = getDocument();
        GroovyDocumentScanner scanner = new GroovyDocumentScanner(doc);

        List<Token> tokens = scanner.getLineTokens(0);
        assertTokens(new String[] { "class", "Foo", "{", "<newline>" }, tokens);

        tokens = scanner.getLineTokens(1);
        assertTokens(new String[] { }, tokens);

        tokens = scanner.getLineTokens(2);
        assertTokens(new String[] { }, tokens);

        tokens = scanner.getLineTokens(3);
        assertTokens(new String[] { "}" }, tokens);
        scanner.dispose();
    }

    public void testDocumentEdits() throws Exception {
        String text =
            "class Foo {\n" +
            "    def a = <***>\n" +
            "}\n";
        makeEditor(text);

        IDocument doc = getDocument();
        GroovyDocumentScanner scanner = new GroovyDocumentScanner(doc);

        List<Token> tokens = scanner.getLineTokens(1);
        assertTokens(new String[] { "def", "a", "=", "<newline>" }, tokens);

        send("3+4");
        tokens = scanner.getLineTokens(1);
        assertTokens(new String[] { "def", "a", "=", "3", "+", "4", "<newline>" }, tokens);
        scanner.dispose();
    }

    private IDocument getDocument() {
        return editDoc;
    }

    private void makeEditor(String string) {
        caret = string.indexOf("<***>");
        if (caret < 0) {
            caret = 0;
        } else {
            string = string.substring(0, caret) + string.substring(caret + "<***>".length());
        }
        editDoc = new Document(string);
    }

    /**
     * Simulate an edit cause by typing some text.
     * Note: unlike in GroovyEditorTests this doesn't actually use the editor, so there
     * is no smart processing at all. The text is just inserted into the document, at the
     * position of the caret.
     * @throws BadLocationException
     */
    private void send(String insertionText) throws BadLocationException {
        editDoc.replace(caret, 0, insertionText);
        caret = caret + insertionText.length();
    }

    private void assertTokens(String[] expected, List<Token> tokens) {
        assertEquals(expected.length, tokens.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tokens.get(i).getText());
        }
    }
}
