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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.antlr.GroovySourceToken;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.groovy.core.util.GroovyScanner;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import antlr.Token;
import antlr.TokenStreamException;

/**
 * This class provides methods to retrieve tokens for a given IDocument
 * presumed to contain Groovy source code.
 * <p>
 * A sophisticated implementation could try to work incrementally and invalidate
 * only some tokens when the document is changed. This implementation is rather
 * naive and clears the whole cache on every document change.
 *
 * @author kdvolder
 * @created 2010-05-26
 */
public class GroovyDocumentScanner implements IDocumentListener {

    private static final boolean TOKEN_POSITION_ASSERTS = true;

    /**
     * This is the document that we are chopping into tokens. This may not be
     * null,
     * except when the GroovyDocumentScanner has been disposed (in which case
     * it should no longer be used.
     */
    private IDocument document;

    /**
     * This caches the list of tokens we have gotten from the document so far.
     * This may be null before we have started reading tokens.
     */
    protected List<Token> tokens;

    private GroovyScanner tokenScanner;

    private static int logLimit = 4; // At most this number scanner errors will
                                     // be reported

    public GroovyDocumentScanner(IDocument d) {
        this.document = d;
        document.addDocumentListener(this);
        reset();
    }

    public void documentChanged(DocumentEvent event) {
        reset();
    }

    public void documentAboutToBeChanged(DocumentEvent event) {}

    /**
     * This method must be called internally before operating on the list of
     * scanned tokens, to ensure that we have scanned the file at least upto the
     * position that we are interested in.
     * <p>
     * Current implementation is very naive and just scans the whole file at
     * once. A smarter implementation could stop scanning when the position of
     * interest is reached, and keep enough state to be able to scan onward
     * later if a request for tokens requires it.
     */
    protected void ensureScanned(int end) {
        if (tokens == null) {
            // We haven't started scanning yet. Initialise the scanner and token
            // list.
            tokenScanner = new GroovyScanner(document.get());
            tokens = getTokensIncludingEOF();
        }
    }

    private List<Token> getTokensIncludingEOF() {
        List<Token> result = new ArrayList<Token>();
        Token t;
        try {
            do {
                t = nextToken();
                result.add(t);
            } while (t.getType() != GroovyTokenTypeBridge.EOF);
        } catch (Exception e) {
            if (logLimit-- > 0) {
                Util.log(e);
            }
        }
        return result;
    }

    private Token nextToken() throws TokenStreamException, BadLocationException {
        Token t;
        try {
            t = tokenScanner.nextToken();
        } catch (TokenStreamException e) {
            //Try to recover
            tokenScanner.recover(document);
            // No try catch:
            // If it fails again we give up:
            t = tokenScanner.nextToken();
        }
        return t;
    }

    /**
     * Called upon initialisation and upon any change to the document to
     * invalidate the list of cached tokens.
     */
    private void reset() {
        tokens = null;
    }

    /**
     * Translate Antlr line/column positions of a token into Eclipse document
     * offset.
     *
     * @param token
     * @return offset of the start of the token in the document.
     * @throws BadLocationException
     */
    public int getOffset(Token token) throws BadLocationException {
        int offset = GroovyScanner.getOffset(document, token.getLine(), token.getColumn());
        if (TOKEN_POSITION_ASSERTS) {
            // These asserts should give some confidence we compute
            // positions correctly.
            if (token.getType() == GroovyTokenTypeBridge.EOF) {
                // EOF token is an exception, it is not actually in the
                // document so its position info doesn't seem to obey these
                // assumptions.
            } else {
                int col = token.getColumn() - 1;
                int line = token.getLine() - 1;
                Assert.isTrue(col >= 0);
                Assert.isTrue(col < document.getLineLength(line), "Token: " + token);
                Assert.isTrue(offset < document.getLength());
                if (token.getType() == GroovyTokenTypeBridge.IDENT) {
                    // Don't check this for other tokens, because the Antlr
                    // token's
                    // "getText()" method doesn't always return the actual text
                    // from the document (e.g. it returns "<newline>" for
                    // newline
                    // tokens.).
                    String antlrText = token.getText();
                    String eclipseText = document.get(offset, antlrText.length());
                    Assert.isTrue(eclipseText.equals(antlrText));
                }
            }
        }
        return offset;
    }

    /**
     * Translate antlr line/column position of the end of the token into
     * Eclipse document offset.
     *
     * @throws BadLocationException
     */
    public int getEnd(Token token) throws BadLocationException {
        GroovySourceToken gToken = ((GroovySourceToken) token);
        return GroovyScanner.getOffset(document, gToken.getLineLast(), gToken.getColumnLast());
    }

    /**
     * Call this method when you don't need the scanner anymore, to release
     * resources it may be holding on to.
     * <p>
     * Disposing an already disposed object is tolerated.
     */
    public void dispose() {
        if (this.document != null) {
            document.removeDocumentListener(this);
            this.document = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.dispose();
        super.finalize();
    }

    /**
     * @return The document that this scanner is operating on.
     */
    public IDocument getDocument() {
        return document;
    }

    /**
     * Retrieve a list of tokens for a range of text in the document.
     * Any token who's starting position is in the range [start..end[
     * range will be included in the list.
     *
     * @param start
     * @param end
     * @return
     */
    public List<Token> getTokens(int start, int end) {
        if (start>=end)
            return new ArrayList<Token>();
        try {
            int startToken = findTokenFrom(start);
            if (startToken == NOT_FOUND)
                return new ArrayList<Token>();
            if (getOffset(tokens.get(startToken)) >= end)
                return new ArrayList<Token>();
            int endToken = findTokenFrom(end);
            if (endToken == NOT_FOUND) {
                endToken = tokens.size() - 1;
                // Take the last token in the file as end token:
                // Since startToken is between start and end, and since there
                // are no
                // tokens after end, all tokens from startToken onward should be
                // returned!
            }
            else {
                endToken = endToken - 1; // Actually the token before is the one we want!
                // The one we found is >= end
            }
            Assert.isTrue(startToken <= endToken);
            return tokens.subList(startToken, endToken + 1);
        } catch (BadLocationException e) {
            throw new Error(e);
        }
    }

    /**
     * Get the tokens up to a given offset, from the start of the line that this
     * offset is in.
     *
     * @param d
     * @param offset
     * @return List of tokens from start of line to given offset.
     */
    public List<Token> getLineTokensUpto(int offset) {
        try {
            int start = document.getLineOffset(document.getLineOfOffset(offset));
            return getTokens(start, offset);
        } catch (BadLocationException e) {
            return new ArrayList<Token>();
        }
    }

    /**
     * Get the tokens starting from a given offset, upto the end of the line
     * offset is in.
     *
     * @param d
     * @param offset
     * @return List of tokens from start of line to given offset.
     */
    public List<Token> getLineTokensFrom(int offset) {
        try {
            int line = document.getLineOfOffset(offset);
            int lineEnd = document.getLineOffset(line) + document.getLineLength(line);
            return getTokens(offset, lineEnd);
        } catch (BadLocationException e) {
            GroovyCore.logException("Recoverable internal error", e);
            return new ArrayList<Token>();
        }
    }

    private static final int NOT_FOUND = -1;

    /**
     * Find the index of the first token that has an offset
     * greater or equal to a given offset.
     * If such a token is not found, then NOT_FOUND is returned.
     */
    protected int findTokenFrom(int offset) {
        Assert.isLegal(offset >= 0);
        ensureScanned(offset);
        try {
            int beg = 0;
            int end = tokens.size() - 1;

            // The "candidates" are all indexes in range [beg..end]
            // We will binary search, until we either exhaust the range,
            // or we find a beg that matches the condition.

            while (beg <= end) {
                if (getOffset(tokens.get(beg)) >= offset) {
                    // beg token is good: done
                    return beg;
                } else {
                    // beg token was bad. No need to consider it anymore
                    beg++;
                    if (beg > end)
                        return NOT_FOUND;
                }
                int mid = (beg + end) / 2;
                int midOfs = getOffset(tokens.get(mid));
                if (midOfs >= offset) {
                    // Mid token is already good (matches condition)
                    end = mid;
                } else {
                    // Mid token is still bad (does not match condition)
                    beg = mid + 1;
                }
            }
            return NOT_FOUND;
        } catch (BadLocationException e) {
            throw new Error(e); // If this code works as it should exceptions
                                // should not happen!
        }
    }

    /**
     * Find the last token who's starting position occurs before
     * a given offset.
     *
     * @return A token, or null if no such token exists.
     */
    public Token getLastTokenBefore(int offset) {
        int nextToken = findTokenFrom(offset);
        if (nextToken == NOT_FOUND) {
            return getLastToken();
        }
        if (nextToken > 0) {
            return tokens.get(nextToken - 1);
        }
        return null;
    }

    /**
     * Get the last token who's offset is before this token's offset.
     */
    public Token getLastTokenBefore(Token token) throws BadLocationException {
        return getLastTokenBefore(getOffset(token));
    }

    /**
     * @return The last token in the document.
     */
    public Token getLastToken() {
        ensureScanned(Integer.MAX_VALUE);
        return tokens.get(tokens.size() - 1);
    }

    /**
     * Get tokens on a given line, this include tokens corresponding to
     * newlines. However, newline tokens are only returned for non empty lines,
     * since the GroovyScanner only returns a single newline token for a
     * sequence of newlines.
     */
    public List<Token> getLineTokens(int line) throws BadLocationException {
        int lineOffset = document.getLineOffset(line);
        return getTokens(lineOffset, lineOffset + document.getLineLength(line));
    }

    /**
     * Get first token with position >= pos.
     *
     * @return The token or null, if such a token doesn't exist.
     */
    public Token getTokenFrom(int offset) {
        int index = findTokenFrom(offset);
        if (index == NOT_FOUND)
            return null;
        return tokens.get(index);
    }

    public Token getNextToken(Token token) throws BadLocationException {
        return getTokenFrom(getOffset(token) + 1);
    }

    public Token getLastNonWhitespaceTokenBefore(int offset) throws BadLocationException {
        Token result = getLastTokenBefore(offset);
        while (result != null && isWhitespace(result))
            result = getLastTokenBefore(result);
        return result;
    }

    /**
     * @param result
     * @return
     */
    private boolean isWhitespace(Token result) {
        int type = result.getType();
        return type == GroovyTokenTypeBridge.WS || type == GroovyTokenTypeBridge.NLS;
    }

}
