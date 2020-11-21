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
package org.codehaus.groovy.eclipse.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.antlr.LocationSupport;
import org.codehaus.groovy.ast.Comment;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetParser;
import org.codehaus.groovy.eclipse.core.impl.ReverseSourceBuffer;

/**
 * Token stream used by the ExpressionFinder parser.
 */
public class TokenStream {

    private static final Pattern MULTI_LINE_COMMENT = Pattern.compile("(?s)/\\*.*\\*/");
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile(".*//");
    private static final Pattern SINGLE_QUOTE1 = Pattern.compile("^\'.*\'");
    private static final Pattern SINGLE_QUOTE2 = Pattern.compile("^\".*\"");
    private static final Pattern TRIPLE_QUOTE1 = Pattern.compile("^\'\'\'.*\'\'\'");
    private static final Pattern TRIPLE_QUOTE2 = Pattern.compile("^\"\"\".*\"\"\"");

    private static final Token TOKEN_EOF = new Token(Token.Type.EOF, -1, -1, null);

    private final ISourceBuffer buffer;
    private Token last, next;
    private int offset;
    private char ch;

    public TokenStream(final ISourceBuffer buffer, final int offset) {
        this.buffer = buffer;
        this.offset = offset;
        this.ch = buffer.charAt(offset);
    }

    public char getCurrentChar() {
        return ch;
    }

    /**
     * Returns the next token in the stream without changing the offset.
     */
    public Token peek() throws TokenStreamException {
        int offset = this.offset;
        Token last = this.last;
        Token next = this.next;
        char ch = this.ch;

        Token ret = next();

        this.offset = offset;
        this.last = last;
        this.next = next;
        this.ch = ch;

        return ret;
    }

    /**
     * Returns the last token produced by {@link #next()}
     */
    public Token last() {
        return last;
    }

    /**
     * Returns the next token in the stream.
     */
    public Token next() throws TokenStreamException {
        if (next != null) {
            last = next;
            next = null;
            return last;
        }
        if (offset == -1) {
            return TOKEN_EOF;
        }

        if (Character.isWhitespace(ch)) {
            skipWhite();
            if (offset == -1) {
                return TOKEN_EOF;
            }
        }

        if (isLineBreakChar()) {
            last = skipLineBreak();
            next = skipLineComment();
            return last;
        }

        if (ch == '/' && la(1) == '*') {
            last = scanBlockComment();
            return last;
        }

        if (Character.isJavaIdentifierPart(ch)) {
            last = scanIdent();
        } else {
            switch (ch) {
            case '.':
                last = scanDot();
                break;
            case '}':
                last = scanPair('{', '}', Token.Type.BRACE_BLOCK);
                break;
            case ')':
                last = scanPair('(', ')', Token.Type.PAREN_BLOCK);
                break;
            case ']':
                last = scanPair('[', ']', Token.Type.BRACK_BLOCK);
                break;
            case '\'':
                last = scanQuote('\'');
                break;
            case '"':
                last = scanQuote('"');
                break;
            case '@':
                nextChar();
                if (ch == '.') {
                    nextChar();
                    last = new Token(Token.Type.FIELD_ACCESS, offset + 1, offset + 3, buffer.subSequence(offset + 1, offset + 3).toString());
                } else {
                    last = new Token(Token.Type.IDENT, offset + 1, offset + 2, buffer.subSequence(offset + 1, offset + 2).toString());
                }
                break;
            case '&':
                nextChar();
                if (ch == '.') {
                    nextChar();
                    last = new Token(Token.Type.METHOD_POINTER, offset + 1, offset + 3, buffer.subSequence(offset + 1, offset + 3).toString());
                }
                break;
            case ':':
                nextChar();
                if (ch == ':') {
                    nextChar();
                    last = new Token(Token.Type.METHOD_POINTER, offset + 1, offset + 3, buffer.subSequence(offset + 1, offset + 3).toString());
                }
                break;
            case ';':
                nextChar();
                last = new Token(Token.Type.SEMI, offset + 1, offset + 2, buffer.subSequence(offset + 1, offset + 2).toString());
                break;
            default:
                throw new TokenStreamException(ch);
            }
        }
        return last;
    }

    //--------------------------------------------------------------------------

    private Token scanDot() {
        nextChar();
        if (offset == -1) {
            return TOKEN_EOF;
        }
        if (ch == '.') {
            nextChar();
            return new Token(Token.Type.DOUBLE_DOT, offset + 1, offset + 3, buffer.subSequence(offset + 1, offset + 3).toString());
        }
        if (ch == '?') {
            nextChar();
            return new Token(Token.Type.SAFE_DEREF, offset + 1, offset + 3, buffer.subSequence(offset + 1, offset + 3).toString());
        }
        if (ch == '*') {
            nextChar();
            return new Token(Token.Type.SPREAD, offset + 1, offset + 3, buffer.subSequence(offset + 1, offset + 3).toString());
        }
        return new Token(Token.Type.DOT, offset + 1, offset + 2, buffer.subSequence(offset + 1, offset + 2).toString());
    }

    private Token skipLineBreak() {
        int endOffset = offset + 1;
        char firstChar = ch;
        nextChar();
        if (offset != -1 && isLineBreakChar()) {
            char secondChar = ch;
            nextChar();
            return new Token(Token.Type.LINE_BREAK, offset + 1, endOffset, new String(new char[] {firstChar, secondChar}));
        }
        return new Token(Token.Type.LINE_BREAK, offset + 1, endOffset, new String(new char[] {firstChar}));
    }

    private boolean isLineBreakChar() {
        return ch == '\n' || ch == '\r';
    }

    private void nextChar() {
        if (offset == -1)
            throw new IllegalStateException("tried to get next char after eof");
        if (offset == 0) {
            offset = -1;
        } else {
            ch = buffer.charAt(--offset);
        }
    }

    /**
     * Scans closing and opening pairs, ignoring nested pairs.
     */
    private Token scanPair(final char open, final char close, final Token.Type type) throws TokenStreamException {
        int endOffset = offset + 1;
        int pairCount = 1;
        while (pairCount > 0 && offset > 0) {
            ch = buffer.charAt(--offset);
            if (ch == open) {
                pairCount -= 1;
            } else if (ch == close) {
                pairCount += 1;
            }
        }
        if (offset != 0) {
            ch = buffer.charAt(--offset);
        } else {
            offset = -1;
            if (pairCount != 0) {
                throw new TokenStreamException("Unclosed pair at EOF");
            }
        }

        return new Token(type, offset + 1, endOffset, buffer.subSequence(offset + 1, endOffset).toString());
    }

    private Token scanIdent() {
        int endOffset = offset + 1;
        do {
            nextChar();
        } while (offset > -1 && Character.isJavaIdentifierPart(ch));

        return new Token(Token.Type.IDENT, offset + 1, endOffset, buffer.subSequence(offset + 1, endOffset).toString());
    }

    private Token scanQuote(final char quote) throws TokenStreamException {
        Pattern singleQuote;
        Pattern tripleQuote;
        if (quote == '\'') {
            singleQuote = SINGLE_QUOTE1;
            tripleQuote = TRIPLE_QUOTE1;
        } else {
            singleQuote = SINGLE_QUOTE2;
            tripleQuote = TRIPLE_QUOTE2;
        }

        Token token = matchQuote(tripleQuote);
        if (token != null) {
            return token;
        }

        token = matchQuote(singleQuote);
        if (token != null) {
            return token;
        }

        throw new TokenStreamException("Could not close quoted string, end offset = " + offset);
    }

    private Token matchQuote(final Pattern quotePattern) {
        ISourceBuffer matchBuffer = new ReverseSourceBuffer(buffer, offset);
        Matcher matcher = quotePattern.matcher(matchBuffer);
        if (matcher.find()) {
            String match = matcher.group(0);
            int endOffset = offset + 1;
            int startOffset = offset - match.length() + 1;
            offset = startOffset;
            if (offset == 0) {
                offset = -1;
            }
            if (offset != -1) {
                offset -= 1;
                ch = buffer.charAt(offset);
            }
            return new Token(Token.Type.QUOTED_STRING, startOffset, endOffset, match);
        }
        return null;
    }

    private void skipWhite() {
        if (isLineBreakChar())
            return;
        do {
            nextChar();
        } while (Character.isWhitespace(ch) && !isLineBreakChar() && offset > -1);
    }

    private Token skipLineComment() {
        Matcher matcher = SINGLE_LINE_COMMENT.matcher(new ReverseSourceBuffer(buffer, offset));
        if (matcher.find() && matcher.start() == 0) {
            String match = matcher.group(0);
            int endOffset = offset + 1;
            int startOffset = offset - match.length() + 1;
            // check to be sure offset is within a line comment
            if (isComment(startOffset)) {
                offset = startOffset;
                if (offset != 0) {
                    ch = buffer.charAt(--offset);
                } else {
                    ch = buffer.charAt(offset--);
                }
                return new Token(Token.Type.LINE_COMMENT, startOffset, endOffset, match);
            }
        }
        return null;
    }

    private Token scanBlockComment() {
        Matcher matcher = MULTI_LINE_COMMENT.matcher(new ReverseSourceBuffer(buffer, offset));
        if (matcher.find()) {
            String match = matcher.group(0);
            int endOffset = offset + 1;
            int startOffset = offset - match.length() + 1;
            // check to be sure offset is within a block comment
            if (isComment(startOffset)) {
                offset = startOffset;
                if (offset != 0) {
                    ch = buffer.charAt(--offset);
                } else {
                    ch = buffer.charAt(offset--);
                }
                return new Token(Token.Type.BLOCK_COMMENT, startOffset, endOffset, match);
            }
        }
        ch = buffer.charAt(--offset); // return the slash to the stream
        return null;
    }

    private boolean isComment(final int index) {
        if (comments == null) {
            CharSequence source = buffer.subSequence(0, buffer.length());
            GroovySnippetParser parser = new GroovySnippetParser();
            ModuleNode module = parser.parse(source);

            LocationSupport locator = module.getNodeMetaData(LocationSupport.class);

            // extract the comment ranges from the parse results
            List<Comment> list = module.getContext().getComments();
            int i = 0, n = (list == null ? 0 : list.size());
            comments = new int[n * 2];
            if (n > 0) {
                for (Comment comment : list) {
                    comments[i++] = locator.findOffset(comment.sline, comment.scol);
                    comments[i++] = locator.findOffset(comment.eline, comment.ecol);
                }
                Arrays.sort(comments); // should be sorted already, but let's be sure
            }
        }
        return (Arrays.binarySearch(comments, index) % 2) == 0;
    }
    private int[] comments;

    private char la(final int index) {
        if (offset - index >= 0) {
            return buffer.charAt(offset - index);
        }
        return 0;
    }
}
