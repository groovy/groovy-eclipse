 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.util;

import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;

/**
 * An expression finder. Used to find expressions that are suitable for content assist.
 * <p>
 * Examples are:
 * <ul>
 * <li>hello</li>
 * <li>thing.value</li>
 * <li>thing[10].value</li>
 * <li>[1, 2, 3].collect { it.toString() }. // Note the '.'</li>
 * </ul>
 *
 * @author empovazan
 */
public class ExpressionFinder {
	/**
	 * Find an expression starting at the offset and working backwards. The found expression is one that could possibly
	 * have completions.
	 *
	 * @param sourceBuffer
	 * @param offset
	 * @return The expression, or null if no suitable expression was found.
	 * @throws ParseException
	 */
	public String findForCompletions(ISourceBuffer sourceBuffer, int offset) throws ParseException {
		Token token = null;
		int endOffset = 0;
		TokenStream stream = new TokenStream(sourceBuffer, offset);
		try {
			token = stream.peek();
			if (token.type == Token.EOF) {
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

			// if the offset is a whitespace, then content assist should be on a blank expression unless
			// there is a '.', '..', or '?.'
			if (offsetIsWhitespace && token.type != Token.DOT && token.type != Token.DOUBLE_DOT && token.type != Token.SAFE_DEREF && token.type != Token.SPREAD) {
				return "";
			}

			if (token.type == Token.EOF) {
				return null;
			}

			switch (token.type) {
				case Token.DOT:
				case Token.DOUBLE_DOT:
				case Token.SAFE_DEREF:
				case Token.SPREAD:
					token = dot(stream);
					break;
				case Token.IDENT:
					token = ident(stream);
					break;
				case Token.BRACK_BLOCK:
					token = null;
					break;
				default:
					throw new ParseException(token);
			}
		} catch (TokenStreamException e) {
			// FUTURE: emp - the token stream should return EOF, for tokens [ { ( etc. or the tokens themselves.
			// This can happen: if () { a._
			// as '{' is unexpected without '}' - there are no tokens for the block delimiters.
			// Because of this exception, the last token has not been returned. Patch that here.
			Token last = stream.last();
			if (last != null) {
				token = last;
			}
        } catch (IllegalStateException e) {

		}
		if (token != null) {
			return sourceBuffer.subSequence(token.startOffset, endOffset).toString();
		}
		return "";
	}


	/**
     * Finds the end of the String token that exists at initialOffset.
     * searches the document for the next non-word character and returns that
     * as the end
     *
     * @param buffer the document to search
     * @param initialOffset the initial offset
     * @return the offset of the first non-word character starting at
     *         initialOffset
     */
    public int findTokenEnd(ISourceBuffer buffer, int initialOffset) {
        int candidate = initialOffset;
        while (buffer.length() > candidate) {
            if (!Character.isJavaIdentifierPart(buffer.charAt(candidate))) {
                break;
            }
            candidate++;
        }
        return candidate;
    }

    /**
     * Splits the given expression into two parts: the type evaluation part, and
     * the code completion part.
     *
     * @param expression
     *            The expression returned by the
     *            {@link #findForCompletions(ISourceBuffer, int)} method.
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
	public String[] splitForCompletion(String expression) {
	    String[] split = splitForCompletionNoTrim(expression);
	    if (split[0] != null) {
	        split[0] = split[0].trim();
	        if (split[0].startsWith("$")) {
	            split[0] = split[0].substring(1);
	        }
	    }
	    if (split[1] != null) {
	        split[1] = split[1].trim();
            if (split[1].startsWith("$")) {
                split[1] = split[1].substring(1);
            }
	    }
	    return split;
	}


	public String[] splitForCompletionNoTrim(String expression) {
       String[] ret = new String[2];

        if (expression == null || expression.trim().length() < 1 ){
            ret[0] = "";
            ret[1] = null;
            return ret;
        }

        StringSourceBuffer sb = new StringSourceBuffer(expression);
        TokenStream stream = new TokenStream(sb, expression.length() - 1);
        Token token0, token1, token2;
        try {
            skipLineBreaksAndComments(stream);
            token0 = stream.next();
            skipLineBreaksAndComments(stream);
            token1 = stream.next();
            skipLineBreaksAndComments(stream);
            token2 = stream.next();

            if ((token0.type == Token.DOT || token0.type == Token.SAFE_DEREF || token0.type == Token.SPREAD) && isValidBeforeDot(token1.type)) {
                ret[0] = expression.substring(0, token1.endOffset);
                ret[1] = "";
            } else if (token0.type == Token.IDENT
                    && (token1.type == Token.DOT || token1.type == Token.SAFE_DEREF || token1.type == Token.SPREAD)
                    && isValidBeforeDot(token2.type)) {
                ret[0] = expression.substring(0, token2.endOffset);
                ret[1] = expression.substring(token0.startOffset, expression.length());
            } else if (token0.type == Token.IDENT) {
                ret[0] = expression;
            } else {
                ret = new String[] { "", null };
            }
        } catch (TokenStreamException e) {
            ret = new String[] { "", null };
        } catch (IllegalStateException e) {
            ret = new String[] { "", null };
        }

        return ret;
	}

    public class NameAndLocation {
        public final String name;

        public final int location;

        public NameAndLocation(String name, int locaiton) {
            this.name = name;
            this.location = locaiton;
        }

        public String toTypeName() {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < name.length() && Character.isJavaIdentifierPart(name.charAt(i))) {
                sb.append(name.charAt(i++));
            }
            return sb.toString();
        }

        public int dims() {
            int i = 0;
            int dims = 0;
            while (i < name.length()) {
                if (name.charAt(i++) == ']') {
                    dims++;
                }
            }
            return dims;
        }
    }

    public NameAndLocation findPreviousTypeNameToken(ISourceBuffer buffer, int start) {
        int current = start;
        current--;
        while (current >= 0 && !Character.isWhitespace(buffer.charAt(current))
                && Character.isJavaIdentifierPart(buffer.charAt(current))) {
            current--;
        }

        if (current < 0 || !Character.isWhitespace(buffer.charAt(current))) {
            return null;
        }
        // don't allow newline chars, but do allow [] and whitespace
        StringBuilder sb = new StringBuilder();
        while (current >= 0
                && (Character.isWhitespace(buffer.charAt(current)) || buffer.charAt(current) == '[' || buffer.charAt(current) == ']')
                && buffer.charAt(current) != '\n' && buffer.charAt(current) != '\r') {
            // current--;
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

    /**
     * FIXADE only skip line breaks if the previous character is a '.' otherwise
     * line breaks should signify the end of the completion.
     * For now, though we just ignore skipping all line breaks
     *
     */
	private void skipLineBreaksAndComments(TokenStream stream)
			throws TokenStreamException {
		skipLineBreaks(stream);
		skipLineComments(stream);
	}

	private boolean isValidBeforeDot(int type) {
		int beforeDot[] = new int[] { Token.IDENT, Token.QUOTED_STRING, Token.BRACE_BLOCK, Token.BRACK_BLOCK,
				Token.PAREN_BLOCK };
		for (int i = 0; i < beforeDot.length; ++i) {
			if (type == beforeDot[i]) {
				return true;
			}
		}
		return false;
	}

	private Token dot(TokenStream stream) throws TokenStreamException, ParseException {
		skipLineBreaksAndComments(stream);
		Token token = stream.next();

		switch (token.type) {
			case Token.IDENT:
				return ident(stream);
			case Token.QUOTED_STRING:
				return quotedString(stream);
			case Token.PAREN_BLOCK:
				return parenBlock(stream);
			case Token.BRACE_BLOCK:
				return braceBlock(stream);
			case Token.BRACK_BLOCK:
				return brackBlock(stream);
			default:
				throw new ParseException(token);
		}
	}

	private void skipLineComments(TokenStream stream) throws TokenStreamException {
		while (stream.peek().type == Token.LINE_COMMENT) {
			stream.next();
		}
	}

	private void skipLineBreaks(TokenStream stream) throws TokenStreamException {
		while (stream.peek().type == Token.LINE_BREAK) {
			stream.next();
		}
	}

	private Token ident(TokenStream stream) throws TokenStreamException, ParseException {
		Token token = stream.peek();
		Token last = stream.last();
		switch (token.type) {
			case Token.LINE_BREAK:
				skipLineBreaksAndComments(stream);
				token = stream.peek();
				if (token.type != Token.DOT && token.type != Token.SAFE_DEREF && token.type != Token.SPREAD) {
					return new Token(Token.EOF, last.startOffset, last.endOffset, null);
				}
				stream.next();
				return dot(stream);
			case Token.DOUBLE_DOT:
			    return new Token(Token.EOF, last.startOffset, last.endOffset, null);
			case Token.SAFE_DEREF:
			case Token.SPREAD:
			case Token.DOT: {
				stream.next();
				return dot(stream);
			}

			// Anything that is not a dot before an ident is assumed to be EOF, unless it is the 'new' keyword.
			// This is because, a previous line of code can end with ) ] } ident ; etc.
			case Token.IDENT:
				// A 'new' keyword is the beginning of the expression to find.
				if (token.text.equals("new")) {
					Token next = stream.next();
					return new Token(Token.EOF, next.startOffset, next.endOffset, null);
				}
                // fall through
			default:
				return new Token(Token.EOF, last.startOffset, last.endOffset, null);
		}
	}

	private Token quotedString(TokenStream stream) throws TokenStreamException, ParseException {
		Token token = stream.peek();
		Token last;
    switch (token.type) {
      case Token.EOF:
      case Token.LINE_BREAK:
        last = stream.last();
        return new Token(Token.EOF, last.startOffset, last.startOffset, null);
      case Token.SEMI:
        last = stream.last();
        return new Token(Token.EOF, last.startOffset, last.startOffset, null);
      case Token.IDENT:
        last = stream.last();
        return new Token(Token.EOF, last.startOffset, last.startOffset, null);
      default:
        throw new ParseException(token);
    }
	}


	private Token parenBlock(TokenStream stream) throws TokenStreamException, ParseException {
		Token token = stream.peek();
		switch (token.type) {
		  case Token.IDENT:
		    stream.next();
		    return ident(stream);
		  case Token.EOF:
		  case Token.SEMI:
		  case Token.LINE_BREAK:
		    //expression in paren
		    return stream.last();
		  default:
		    throw new ParseException(token);
		}
	}

	private Token braceBlock(TokenStream stream) throws TokenStreamException, ParseException {
		Token token = stream.next();
		switch (token.type) {
			case Token.IDENT:
				return ident(stream);
			case Token.PAREN_BLOCK:
				return parenBlock(stream);
			default:
				throw new ParseException(token);
		}
	}

	private Token brackBlock(TokenStream stream) throws TokenStreamException, ParseException {
		Token last = stream.last();
		Token token = stream.next();
		switch (token.type) {
			case Token.EOF:
				return new Token(Token.EOF, last.startOffset, last.startOffset, null);
			case Token.IDENT:
				return ident(stream);
			case Token.PAREN_BLOCK:
				return parenBlock(stream);
			case Token.BRACE_BLOCK:
				return braceBlock(stream);
            case Token.BRACK_BLOCK:
                return brackBlock(stream);
            case Token.SEMI:
            case Token.LINE_BREAK:
                // expression in paren
                return stream.last();
			default:
				throw new ParseException(token);
		}
	}
}
