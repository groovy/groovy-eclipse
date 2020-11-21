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

import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;

/**
 * Finds expressions suitable for content assist.
 * <p>
 * Examples:
 * <ul>
 * <li>hello</li>
 * <li>thing.value</li>
 * <li>thing[10].value</li>
 * <li>[1, 2, 3].collect { it.toString() }. // note the '.'</li>
 * </ul>
 */
public class ExpressionFinder {

    /**
     * Splits the given expression into two parts: the type evaluation part, and
     * the code completion part.
     *
     * @param expression returned by {@link #findForCompletions}
     * @return A string pair, the expression to complete, and the prefix to be
     *         completed.<br>
     *         { "", null } if no completion expression could be found
     *         String[0] is an expression .<br>
     *         String[1] is the empty string if the last character is a '.'.<br>
     *         String[1] is 'ident' if the expression ends with '.ident'.<br>
     *         String[1] is null if the expression itself is to be used for
     *         completion.
     *         Also, remove starting '$'. These only occur when inside GStrings,
     *         and should not be completed against.
     */
    public String[] splitForCompletion(final String expression) {
        String[] result = splitForCompletionNoTrim(expression);
        if (result[0] != null) {
            result[0] = result[0].trim();
            if (result[0].startsWith("$")) {
                result[0] = result[0].substring(1);
            }
        }
        if (result[1] != null) {
            result[1] = result[1].trim();
            if (result[1].startsWith("$")) {
                result[1] = result[1].substring(1);
            }
        }
        return result;
    }

    public String[] splitForCompletionNoTrim(final String expression) {
        String[] result = {"", null};
        if (!expression.trim().isEmpty()) {
            TokenStream stream = new TokenStream(new StringSourceBuffer(expression), expression.length() - 1);
            try {
                skipLineBreaksAndComments(stream);
                Token token0 = stream.next();
                skipLineBreaksAndComments(stream);
                Token token1 = stream.next();
                skipLineBreaksAndComments(stream);
                Token token2 = stream.next();

                if (token0.isDotAccess() && token1.isValidBeforeDot()) {
                    result[0] = expression.substring(0, token1.endOffset);
                    result[1] = "";
                } else if (token0.isType(Token.Type.IDENT) && token1.isDotAccess() && token2.isValidBeforeDot()) {
                    result[0] = expression.substring(0, token2.endOffset);
                    result[1] = expression.substring(token0.startOffset, expression.length());
                } else if (token0.isType(Token.Type.IDENT)) {
                    result[0] = expression;
                }
            } catch (IllegalStateException | NullPointerException | TokenStreamException ignore) {
            }
        }
        return result;
    }

    /**
     * @return The offset of the first non-identifier character after {@code offset}.
     */
    public int findTokenEnd(final ISourceBuffer buffer, final int offset) {
        int result = offset;
        while (buffer.length() > result) {
            if (!Character.isJavaIdentifierPart(buffer.charAt(result))) {
                break;
            }
            result += 1;
        }
        return result;
    }

    /**
     * Finds an expression starting at the offset and working backwards.
     * The found expression is one that could possibly have completions.
     *
     * @return The expression or {@code null} if no suitable expression found.
     */
    public String findForCompletions(final ISourceBuffer buffer, final int offset) throws ParseException {
        Token token = null;
        int endOffset = 0;
        TokenStream stream = new TokenStream(buffer, offset);
        try {
            token = stream.peek();
            if (token == null || token.isType(Token.Type.EOF)) {
                return null;
            }

            endOffset = token.endOffset;

            boolean offsetIsWhitespace = Character.isWhitespace(stream.getCurrentChar());
            boolean offsetIsQuote = stream.getCurrentChar() == '\"' || stream.getCurrentChar() == '\'';
            // no expression associated with a quote
            if (offsetIsQuote) {
                return null;
            }
            skipLineBreaksAndComments(stream);
            token = stream.next();

            // if the offset is a whitespace, then content assist should be on a blank expression unless there is a '.', '..', '?.', '*.', '.@', or '.&'
            if (offsetIsWhitespace && !token.isDotAccess() && !token.isType(Token.Type.DOUBLE_DOT)) {
                return "";
            }
            if ("@".equals(token.text)) {
                return "@";
            }

            switch (token.getType()) {
            case EOF:
                return null;
            case DOT:
            case DOUBLE_DOT:
            case SAFE_DEREF:
            case SPREAD:
            case FIELD_ACCESS:
            case METHOD_POINTER:
                token = dot(stream);
                break;
            case IDENT:
                token = ident(stream);
                break;
            case BRACK_BLOCK:
                token = null;
                break;
            default:
                throw new ParseException(token);
            }
        } catch (TokenStreamException e) {
            // TODO: the token stream should return EOF, for tokens '[', '{', '(',
            // etc. or the tokens themselves. This can happen: if () { a._ as '{'
            // is unexpected without '}' - there are no tokens for the block delimiters.
            // Because of this exception, the last token has not been returned. Patch that here.
            Token last = stream.last();
            if (last != null) {
                token = last;
            }
        } catch (IllegalStateException | NullPointerException ignore) {
        }
        if (token != null) {
            return buffer.subSequence(token.startOffset, endOffset).toString();
        } else {
            return "";
        }
    }

    public NameAndLocation findPreviousTypeNameToken(final ISourceBuffer buffer, final int start) {
        int current = Math.min(start, buffer.length()) - 1;
        while (current >= 0 && !Character.isWhitespace(buffer.charAt(current)) &&
                Character.isJavaIdentifierPart(buffer.charAt(current))) {
            current -= 1;
        }
        if (current < 0 || !Character.isWhitespace(buffer.charAt(current))) {
            return null;
        }
        // don't allow newline chars, but do allow [] and whitespace
        StringBuilder sb = new StringBuilder();
        while (current >= 0 &&
                (Character.isWhitespace(buffer.charAt(current)) || buffer.charAt(current) == '[' || buffer.charAt(current) == ']') &&
                buffer.charAt(current) != '\n' && buffer.charAt(current) != '\r') {
            sb.append(buffer.charAt(current--));
        }

        if (current < 0 || !Character.isJavaIdentifierPart(buffer.charAt(current))) {
            return null;
        }

        while (current >= 0 && Character.isJavaIdentifierPart(buffer.charAt(current))) {
            sb.append(buffer.charAt(current--));
        }

        if (sb.length() > 0) {
            return new NameAndLocation(sb.reverse().toString(), current + 1);
        } else {
            return null;
        }
    }

    //--------------------------------------------------------------------------

    private static Token dot(final TokenStream stream) throws TokenStreamException, ParseException {
        skipLineBreaksAndComments(stream);
        Token token = stream.next();
        switch (token.getType()) {
        case IDENT:
            return ident(stream);
        case BRACE_BLOCK:
            return braceBlock(stream);
        case BRACK_BLOCK:
            return brackBlock(stream);
        case PAREN_BLOCK:
            return parenBlock(stream);
        case QUOTED_STRING:
            return quotedString(stream);
        default:
            throw new ParseException(token);
        }
    }

    private static Token ident(final TokenStream stream) throws TokenStreamException, ParseException {
        Token last = stream.last();
        Token token = peek(stream);
        switch (token.getType()) {
        case LINE_BREAK:
            skipLineBreaksAndComments(stream); // TODO: move to top?
            if (peek(stream).isDotAccess()) {
                stream.next();
                return dot(stream);
            }
            break;
        case DOUBLE_DOT:
            break;
        case METHOD_POINTER:
        case FIELD_ACCESS:
        case SAFE_DEREF:
        case SPREAD:
        case DOT:
            stream.next();
            return dot(stream);
        // Anything that is not a dot before an ident is assumed to be EOF, unless it is the 'new' keyword.
        // This is because, a previous line of code can end with ) ] } ident ; etc.
        case IDENT:
            // A 'new' keyword is the beginning of the expression to find.
            if ("new".equals(token.text)) {
                return stop(stream.next());
            }
        }
        return stop(last);
    }

    private static Token braceBlock(final TokenStream stream) throws TokenStreamException, ParseException {
        Token token = stream.next();
        switch (token.getType()) {
        case IDENT:
            return ident(stream);
        case PAREN_BLOCK:
            return parenBlock(stream);
        default:
            throw new ParseException(token);
        }
    }

    private static Token brackBlock(final TokenStream stream) throws TokenStreamException, ParseException {
        Token last = stream.last();
        Token token = stream.next();
        switch (token.getType()) {
        case EOF:
            return stop(last);
        case SEMI:
        case LINE_BREAK:
            return stream.last();
        case IDENT:
            return ident(stream);
        case BRACE_BLOCK:
            return braceBlock(stream);
        case BRACK_BLOCK:
            return brackBlock(stream);
        case PAREN_BLOCK:
            return parenBlock(stream);
        default:
            throw new ParseException(token);
        }
    }

    private static Token parenBlock(final TokenStream stream) throws TokenStreamException, ParseException {
        Token token = stream.peek();
        switch (token.getType()) {
        case IDENT:
            stream.next();
            return ident(stream);
        case EOF:
        case SEMI:
        case LINE_BREAK:
            return stream.last();
        default:
            throw new ParseException(token);
        }
    }

    private static Token quotedString(final TokenStream stream) throws TokenStreamException, ParseException {
        Token token = stream.peek();
        switch (token.getType()) {
        case EOF:
        case SEMI:
        case IDENT:
        case LINE_BREAK:
            return stop(stream.last());
        default:
            throw new ParseException(token);
        }
    }

    /**
     * FIXADE Only skip line breaks if the previous character is a '.' otherwise
     * line breaks should signify the end of the completion. For now though just
     * ignore skipping all line breaks.
     */
    private static void skipLineBreaksAndComments(final TokenStream stream) throws TokenStreamException {
        while (peek(stream).isType(Token.Type.LINE_BREAK)) {
            stream.next();
        }
        while (peek(stream).isType(Token.Type.LINE_COMMENT)) {
            stream.next();
        }
    }

    private static Token peek(final TokenStream stream) throws TokenStreamException {
        Token token = stream.peek();
        return (token != null ? token : stop(stream.last()));
    }

    private static Token stop(final Token token) throws TokenStreamException {
        return new Token(Token.Type.EOF, token.startOffset, token.endOffset, null);
    }

    //--------------------------------------------------------------------------

    public static class NameAndLocation {

        public final String name;
        public final int location;

        public NameAndLocation(final String name, final int locaiton) {
            this.name = name;
            this.location = locaiton;
        }

        public String toTypeName() {
            int i = 0;
            StringBuilder sb = new StringBuilder();
            while (i < name.length() && Character.isJavaIdentifierPart(name.charAt(i))) {
                sb.append(name.charAt(i++));
            }
            return sb.toString();
        }

        public int dims() {
            int i = 0, dims = 0;
            while (i < name.length()) {
                if (name.charAt(i++) == ']') {
                    dims += 1;
                }
            }
            return dims;
        }
    }
}
