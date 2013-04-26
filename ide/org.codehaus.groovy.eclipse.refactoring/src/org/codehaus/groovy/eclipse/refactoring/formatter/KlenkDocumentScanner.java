/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import groovyjarjarantlr.Token;
import groovyjarjarantlr.TokenStream;
import groovyjarjarantlr.TokenStreamException;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Vector;

import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * This subclass of GroovyDocumentScanner overrides ensureScanned, to produce
 * exactly the same tokens as the Vector<Token> that was in Mike Klenk's
 * Formatter implementation. It also keeps track of a Vector<Vector<Token>>
 * in exactly the same way that M. Klenk likes it (the line numbers of the file
 * do not match with the indexes of the lines vector).
 * <p>
 * This allows us to make use of the nice operations of GroovyDocumentScanner, without
 * storing two copies of all the tokens, and without having to port all Mike's
 * code all at once.
 *
 * @author kdvolder
 * @created 2010-06-06
 */
public class KlenkDocumentScanner extends GroovyDocumentScanner {

    /** Tokens split up into lines */
    private Vector<Vector<Token>> tokenLines; // FIXKDV:
    // If we must have something like this, it could be much cheaper (memory
    // wise) represented
    // by only keeping an array of indexes that give the position of the first
    // line.
    // (Note, we could not use the document's line info for this purpose,
    // because Mike
    // Klenk's lines are not actually the same as document lines (some lines
    // containing
    // multiline stuff are skipped, so indexes in this vector do not correspond
    // to line numbers
    // in any meaningful way.

    public KlenkDocumentScanner(IDocument doc) {
        super(doc);
    }

    @Override
    protected void ensureScanned(int end) {
        if (tokens != null)
            return; // already scanned!

        // Code in this method copied from Mike Klenk's
        // DefaultGroovyFormatter.initCodeBase.
        // This is copied as much as possible unchanged, to ensure identical
        // behaviour, and avoid
        // breaking Mike's formatter,
        Reader input = new StringReader(getDocument().get());
        GroovyLexer lexer = new GroovyLexer(input);
        lexer.setWhitespaceIncluded(true);
        TokenStream stream = (TokenStream) lexer.plumb();

        Token token = null;
        tokens = new ArrayList<Token>();
        tokenLines = new Vector<Vector<Token>>();
        Vector<Token> line = new Vector<Token>();
        try {
            while ((token = stream.nextToken()).getType() != GroovyTokenTypeBridge.EOF) {
                if (token.getType() != GroovyTokenTypeBridge.WS) {
                    // Ignore Tokens inside a String
                    if (token.getType() == GroovyTokenTypeBridge.STRING_CTOR_START) {
                        tokens.add(token);
                        Token prevToken = token;
                        inner: while ((token = stream.nextToken()).getType() != GroovyTokenTypeBridge.STRING_CTOR_END) {
                            if (equalTokens(prevToken, token)) {
                                break inner;
                            }
                            prevToken = token;
                        }
                    }
                    tokens.add(token);
                    line.add(token);
                    if (token.getType() == GroovyTokenTypeBridge.NLS) {
                        tokenLines.add(line);
                        line = new Vector<Token>();
                    }
                }
            }
        } catch (TokenStreamException e) {
            GroovyCore.logException("Scanning tokens threw an exception", e);
        }
        // Adding last Line with EOF at End
        tokens.add(token);
        line.add(token);
        tokenLines.add(line);
    }

    private boolean equalTokens(Token t1, Token t2) {
        return t1.getType() == t2.getType() && t1.getColumn() == t2.getColumn() && t1.getLine() == t2.getLine()
                && nullEquals(t1.getFilename(), t2.getFilename()) && nullEquals(t1.getText(), t2.getText());
    }

    private boolean nullEquals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        } else if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

    @Deprecated
    public Vector<Vector<Token>> getLineTokensVector() {
        ensureScanned(Integer.MAX_VALUE);
        return tokenLines;
    }

    /**
     * This method is deprecated. It provided for the more easy porting of Mike
     * Klenk's
     * code, which keeps a vector of tokens to operate on. It uses this to
     * iterate
     * over the tokens. This class provides more convenient ways to iterate over
     * tokens,
     * but we provide this method to make it possible to gradually get rid of
     * the
     * vector-indexing based logic.
     * <p>
     * Eventually all such indexing should be replaced with more high-level
     * operations supported by this class.
     *
     * @return Return the i-th token in the document. Returns null if there is
     *         no such token.
     */
    @Deprecated()
    public Token get(int i) {
        ensureScanned(Integer.MAX_VALUE);
        if (i < tokens.size()) {
            return tokens.get(i);
        } else {
            return null;
        }
    }

    /**
     * This method is deprecated. It is provided for the more easy porting of
     * Mike Klenk's code, which keeps a vector of tokens to operate on. It uses
     * this to iterate over the tokens. This class provides more convenient ways
     * to iterate over tokens, but we provide this method to make it possible to
     * gradually get rid of
     * the
     * vector-indexing based logic.
     * <p>
     * Eventually all such indexing should be replaced with more high-level
     * operations supported by this class.
     * <p>
     * Note that using this method is undesirable since it cannot be implemented
     * in an efficient manner (the whole file must be scanned to count the
     * number of tokens in the file). Very often this number is really only used
     * to determine when the end of file is reached, and another method of
     * iteration not based on "vector-indexing" logic would not require
     *
     * @return Return the number of tokens in the file.
     */
    @Deprecated
    public int size() {
        ensureScanned(Integer.MAX_VALUE);
        return tokens.size();
    }

    /**
     * This method is deprecated. It is provided for the more easy porting of
     * Mike Klenk's code, which keeps a vector of tokens to operate on. It uses
     * this to
     *
     * @param token
     * @return
     * @throws BadLocationException
     */
    @Deprecated
    public int indexOf(Token token) throws BadLocationException {
        int pos = findTokenFrom(getOffset(token));
        Assert.isTrue(token == tokens.get(pos));
        return pos;
    }
}
