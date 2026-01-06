/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalToken;

/**
 * Wraps a scanner and offers convenient methods for finding tokens
 */
public class TokenScanner {

	public static final int END_OF_FILE= 20001;
	public static final int LEXICAL_ERROR= 20002;
	public static final int DOCUMENT_ERROR= 20003;

	private final Scanner scanner;
	private final int endPosition;

	/**
	 * Creates a TokenScanner
	 * @param scanner The scanner to be wrapped
	 */
	public TokenScanner(Scanner scanner) {
		this.scanner= scanner;
		this.endPosition= this.scanner.getSource().length - 1;
	}

	/**
	 * Returns the wrapped scanner
	 * @return IScanner
	 */
	public Scanner getScanner() {
		return this.scanner;
	}

	/**
	 * Sets the scanner offset to the given offset.
	 * @param offset The offset to set
	 */
	public void setOffset(int offset) {
		this.scanner.resetTo(offset, this.endPosition);
	}

	/**
	 * @return Returns the offset after the current token
	 */
	public int getCurrentEndOffset() {
		return this.scanner.getCurrentTokenEndPosition() + 1;
	}

	/**
	 * @return Returns the start offset of the current token
	 */
	public int getCurrentStartOffset() {
		return this.scanner.getCurrentTokenStartPosition();
	}

	/**
	 * @return Returns the length of the current token
	 */
	public int getCurrentLength() {
		return getCurrentEndOffset() - getCurrentStartOffset();
	}

	/**
	 * Reads the next token.
	 * @param ignoreComments If set, comments will be overread
	 * @return Return the token id.
	 * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE)
	 * or a lexical error was detected while scanning (code LEXICAL_ERROR)
	 */
	public TerminalToken readNext(boolean ignoreComments) throws CoreException {
		TerminalToken curr= TerminalToken.TokenNameNotAToken;
		do {
			try {
				curr= this.scanner.getNextToken();
				if (curr == TerminalToken.TokenNameEOF) {
					throw new CoreException(createError(END_OF_FILE, "End Of File", null)); //$NON-NLS-1$
				}
			} catch (InvalidInputException e) {
				throw new CoreException(createError(LEXICAL_ERROR, e.getMessage(), e));
			}
		} while (ignoreComments && isComment(curr));
		return curr;
	}

	/**
	 * Reads the next token from the given offset.
	 * @param offset The offset to start reading from.
	 * @param ignoreComments If set, comments will be overread.
	 * @return Returns the token id.
	 * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE)
	 * or a lexical error was detected while scanning (code LEXICAL_ERROR)
	 */
	public TerminalToken readNext(int offset, boolean ignoreComments) throws CoreException {
		setOffset(offset);
		return readNext(ignoreComments);
	}

	/**
	 * Reads the next token from the given offset and returns the start offset of the token.
	 * @param offset The offset to start reading from.
	 * @param ignoreComments If set, comments will be overread
	 * @return Returns the start position of the next token.
	 * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE)
	 * or a lexical error was detected while scanning (code LEXICAL_ERROR)
	 */
	public int getNextStartOffset(int offset, boolean ignoreComments) throws CoreException {
		readNext(offset, ignoreComments);
		return getCurrentStartOffset();
	}

	/**
	 * Reads the next token from the given offset and returns the offset after the token.
	 * @param offset The offset to start reading from.
	 * @param ignoreComments If set, comments will be overread
	 * @return Returns the start position of the next token.
	 * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE)
	 * or a lexical error was detected while scanning (code LEXICAL_ERROR)
	 */
	public int getNextEndOffset(int offset, boolean ignoreComments) throws CoreException {
		readNext(offset, ignoreComments);
		return getCurrentEndOffset();
	}

	/**
	 * Reads until a token is reached.
	 * @param tok The token to read to.
	 * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE)
	 * or a lexical error was detected while scanning (code LEXICAL_ERROR)
	 */
	public void readToToken(TerminalToken tok) throws CoreException {
		TerminalToken curr= TerminalToken.TokenNameNotAToken;
		do {
			curr= readNext(false);
		} while (curr != tok);
	}

	/**
	 * Reads until a token is reached, starting from the given offset.
	 * @param tok The token to read to.
	 * @param offset The offset to start reading from.
	 * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE)
	 * or a lexical error was detected while scanning (code LEXICAL_ERROR)
	 */
	public void readToToken(TerminalToken tok, int offset) throws CoreException {
		setOffset(offset);
		readToToken(tok);
	}

	/**
	 * Reads from the given offset until a token is reached and returns the start offset of the token.
	 * @param token The token to be found.
	 * @param startOffset The offset to start reading from.
	 * @return Returns the start position of the found token.
	 * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE)
	 * or a lexical error was detected while scanning (code LEXICAL_ERROR)
	 */
	public int getTokenStartOffset(TerminalToken token, int startOffset) throws CoreException {
		readToToken(token, startOffset);
		return getCurrentStartOffset();
	}

	/**
	 * Reads from the given offset until a token is reached and returns the offset after the token.
	 * @param token The token to be found.
	 * @param startOffset Offset to start reading from
	 * @return Returns the end position of the found token.
	 * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE)
	 * or a lexical error was detected while scanning (code LEXICAL_ERROR)
	 */
	public int getTokenEndOffset(TerminalToken token, int startOffset) throws CoreException {
		readToToken(token, startOffset);
		return getCurrentEndOffset();
	}

	/**
	 * Reads from the given offset until a token is reached and returns the offset after the previous token.
	 * @param token The token to be found.
	 * @param startOffset The offset to start scanning from.
	 * @return Returns the end offset of the token previous to the given token.
	 * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE)
	 * or a lexical error was detected while scanning (code LEXICAL_ERROR)
	 */
	public int getPreviousTokenEndOffset(TerminalToken token, int startOffset) throws CoreException {
		setOffset(startOffset);
		int res= startOffset;
		TerminalToken curr= readNext(false);
		while (curr != token) {
			res= getCurrentEndOffset();
			curr= readNext(false);
		}
		return res;
	}

	public static boolean isComment(TerminalToken token) {
		return token == TerminalToken.TokenNameCOMMENT_BLOCK || token == TerminalToken.TokenNameCOMMENT_JAVADOC
			|| token == TerminalToken.TokenNameCOMMENT_LINE;
	}

	public static boolean isModifier(TerminalToken token) {
		switch (token) {
			case TokenNamepublic:
			case TokenNameprotected:
			case TokenNameprivate:
			case TokenNamestatic:
			case TokenNamefinal:
			case TokenNameabstract:
			case TokenNamenative:
			case TokenNamevolatile:
			case TokenNamestrictfp:
			case TokenNametransient:
			case TokenNamesynchronized:
				return true;
			default:
				return false;
		}
	}

	public static IStatus createError(int code, String message, Throwable throwable) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, code, message, throwable);
	}

}
