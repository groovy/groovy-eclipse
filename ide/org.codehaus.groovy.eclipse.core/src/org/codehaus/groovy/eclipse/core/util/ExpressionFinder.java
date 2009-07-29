/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.util;

import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator;

/**
 * An expression finder. Used to find expressions that are suitable for type evaluation and code completion.
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
			// there is a '.' or '..'
			if (offsetIsWhitespace && token.type != Token.DOT && token.type != Token.DOUBLE_DOT) {
				return "";
			}
			
			if (token.type == Token.EOF) {
				return null;
			}
			
			switch (token.type) {
				case Token.DOT:
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
		}
		if (token != null) {
			return sourceBuffer.subSequence(token.startOffset, endOffset).toString().trim();
		}
		return null;
	}

	/**
	 * Splits the given expression into two parts: the type evaluation part, and the code completion part.
	 * 
	 * @param expression
	 *            The expression returned by the {@link #findForCompletions(ISourceBuffer, int)} method.
	 * @return A string pair, the expression to complete, and the prefix to be completed.<br>
	 *         Null if the string cannot be split for code completion.<br>
	 *         String[0] is an expression suitable for the {@link TypeEvaluator}.<br>
	 *         String[1] is the empty string if the last character is a '.'.<br>
	 *         String[1] is 'ident' if the expression ends with '.ident'.<br>
	 *         String[1] is null if the expression itself is to be used for completion.
	 */
	public String[] splitForCompletion(String expression) {
		String[] ret = new String[2];

		if (expression.length() < 1 ){
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

			if (token0.type == Token.DOT && isValidBeforeDot(token1.type)) {
				ret[0] = expression.substring(0, token1.endOffset);
				ret[1] = "";
			} else if (token0.type == Token.IDENT && token1.type == Token.DOT && isValidBeforeDot(token2.type)) {
				ret[0] = expression.substring(0, token2.endOffset);
				ret[1] = expression.substring(token0.startOffset, expression.length());
			} else if (token0.type == Token.IDENT) {
				ret[0] = expression;
			} else {
				ret = null;
			}
		} catch (TokenStreamException e) {
		}

		return ret;
	}

	/**
	 * TODO: only skip line breaks if the previous character is a '.' otherwise
	 * line breaks should signify the end of the completion
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
				if (token.type != Token.DOT) {
					return new Token(Token.EOF, last.startOffset, last.endOffset, null);
				}
				stream.next();
				return dot(stream);
			case Token.DOUBLE_DOT:
				return new Token(Token.EOF, last.startOffset, last.endOffset, null);
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
			default:
				throw new ParseException(token);
		}
	}
}
