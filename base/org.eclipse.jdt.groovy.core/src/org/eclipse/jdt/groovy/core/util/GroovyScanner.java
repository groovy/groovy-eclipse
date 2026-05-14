/*
 * Copyright 2009-2023 the original author or authors.
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
package org.eclipse.jdt.groovy.core.util;

import java.io.Reader;
import java.io.StringReader;

import groovyjarjarantlr.Token;
import groovyjarjarantlr.TokenStream;
import groovyjarjarantlr.TokenStreamException;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * A wrapper around an Antlr Groovy Scanner, providing a convenient interface to
 * tokenize a snippet of groovy code.
 */
public class GroovyScanner {

    private GroovyLexer lexer;
    private TokenStream stream;
    private boolean whitespaceIncluded;

    public GroovyScanner(final String text) {
        this(new StringReader(text), false);
    }

    public GroovyScanner(final Reader input) {
        this(input, false);
    }

    public GroovyScanner(final Reader input, final boolean whitespaceIncluded) {
        init(input, whitespaceIncluded);
    }

    private void init(final Reader input, final boolean whitespaceIncluded) {
        this.whitespaceIncluded = whitespaceIncluded;

        lexer = new GroovyLexer(input);
        lexer.setWhitespaceIncluded(whitespaceIncluded);

        stream = lexer.plumb();

        // TODO Remove once GROOVY-6608 is fixed. Initializes the parser to avoid NPE in Groovy code
        GroovyRecognizer.make(lexer);
    }

    public Token nextToken() throws TokenStreamException {
        return stream.nextToken();
    }

    /**
     * Attempts to recover after a scanning error by recreating the Antlr lexer
     * one character past the place where we got an error and tries to continue
     * scanning from there.
     *
     * @throws BadLocationException
     */
    public void recover(final IDocument document) throws BadLocationException {
        int line = lexer.getInputState().getLine(); // line and
        int col = lexer.getInputState().getColumn(); // column where error happened
        int offset = getOffset(document, line, col) + 1; // +1 to skip one character
        line = document.getLineOfOffset(offset);
        int lineStart = document.getLineOffset(line);
        line = line + 1; // antlr lines start at 1
        col = offset - lineStart + 1; // antlr cols start at 1
        String remainingInput = document.get(offset, document.getLength() - offset);
        init(new StringReader(remainingInput), whitespaceIncluded); // eeinitialize with remaining input
        lexer.setLine(line); // fix antlr line and
        lexer.setColumn(col); // column infos because we are not starting at the start
    }

    /**
     * Converts antlr line / col position into a IDocument offset.
     *
     * @param document the reference document
     * @param line antlr-style line number (starts at 1)
     * @param column antlr-style column number (starts at 1)
     */
    public static int getOffset(final IDocument document, final int line, final int column) throws BadLocationException {
        return document.getLineOffset(line - 1) + column - 1;
    }
}
