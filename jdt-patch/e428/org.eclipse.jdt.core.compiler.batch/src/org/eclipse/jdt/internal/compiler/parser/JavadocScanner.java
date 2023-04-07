/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class JavadocScanner extends Scanner{

	public boolean tokenizeSingleQuotes = false;
	
	public boolean considerRegexInStringLiteral = false;
	
	public JavadocScanner() {
		this(false /*comment*/, false /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3 /*sourceLevel*/, null/*taskTag*/, null/*taskPriorities*/, true /*taskCaseSensitive*/);
	}

	public JavadocScanner(
			boolean tokenizeComments,
			boolean tokenizeWhiteSpace,
			boolean checkNonExternalizedStringLiterals,
			long sourceLevel,
			long complianceLevel,
			char[][] taskTags,
			char[][] taskPriorities,
			boolean isTaskCaseSensitive,
			boolean isPreviewEnabled) {

		this(
				tokenizeComments,
				tokenizeWhiteSpace,
				checkNonExternalizedStringLiterals,
				sourceLevel,
				complianceLevel,
				taskTags,
				taskPriorities,
				isTaskCaseSensitive,
				isPreviewEnabled,
				false,
				false);
	}

	public JavadocScanner(
			boolean tokenizeComments,
			boolean tokenizeWhiteSpace,
			boolean checkNonExternalizedStringLiterals,
			long sourceLevel,
			char[][] taskTags,
			char[][] taskPriorities,
			boolean isTaskCaseSensitive,
			boolean isPreviewEnabled) {

		this(
			tokenizeComments,
			tokenizeWhiteSpace,
			checkNonExternalizedStringLiterals,
			sourceLevel,
			sourceLevel,
			taskTags,
			taskPriorities,
			isTaskCaseSensitive,
			isPreviewEnabled);
		
	}

	public JavadocScanner(
			boolean tokenizeComments,
			boolean tokenizeWhiteSpace,
			boolean checkNonExternalizedStringLiterals,
			long sourceLevel,
			char[][] taskTags,
			char[][] taskPriorities,
			boolean isTaskCaseSensitive) {

		this(
			tokenizeComments,
			tokenizeWhiteSpace,
			checkNonExternalizedStringLiterals,
			sourceLevel,
			sourceLevel,
			taskTags,
			taskPriorities,
			isTaskCaseSensitive,
			false);
	}
	
	public JavadocScanner(
			boolean tokenizeComments,
			boolean tokenizeWhiteSpace,
			boolean checkNonExternalizedStringLiterals,
			long sourceLevel,
			long complianceLevel,
			char[][] taskTags,
			char[][] taskPriorities,
			boolean isTaskCaseSensitive,
			boolean isPreviewEnabled,
			boolean tokenizeSingleQuotes,
			boolean considerRegexInStringLiteral) {

		super(
				tokenizeComments,
				tokenizeWhiteSpace,
				checkNonExternalizedStringLiterals,
				sourceLevel,
				complianceLevel,
				taskTags,
				taskPriorities,
				isTaskCaseSensitive,
				isPreviewEnabled);
		this.tokenizeSingleQuotes = tokenizeSingleQuotes;
		this.considerRegexInStringLiteral = considerRegexInStringLiteral;
	}

	@Override
	protected int scanForStringLiteral() throws InvalidInputException {
		if (this.considerRegexInStringLiteral) {
			this.unicodeAsBackSlash = false;
			boolean isUnicode = false;
			try {
				// consume next character
				this.unicodeAsBackSlash = false;
				isUnicode = false;
				if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
						&& (this.source[this.currentPosition] == 'u')) {
					getNextUnicodeChar();
					isUnicode = true;
				} else {
					if (this.withoutUnicodePtr != 0) {
						unicodeStore();
					}
				}

				while (this.currentCharacter != '"') {
					boolean isRegex = false;
					if (this.currentPosition >= this.eofPosition) {
						throw new InvalidInputException(UNTERMINATED_STRING);
					}
					/**** \r and \n are not valid in string literals ****/
					if ((this.currentCharacter == '\n') || (this.currentCharacter == '\r')) {
						// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
						if (isUnicode) {
							int start = this.currentPosition;
							for (int lookAhead = 0; lookAhead < 50; lookAhead++) {
								if (this.currentPosition >= this.eofPosition) {
									this.currentPosition = start;
									break;
								}
								if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\') && (this.source[this.currentPosition] == 'u')) {
									isUnicode = true;
									getNextUnicodeChar();
								} else {
									isUnicode = false;
								}
								if (!isUnicode && this.currentCharacter == '\n') {
									this.currentPosition--; // set current position on new line character
									break;
								}
								if (this.currentCharacter == '\"') {
									throw new InvalidInputException(INVALID_CHAR_IN_STRING);
								}
							}
						} else {
							this.currentPosition--; // set current position on new line character
						}
						throw new InvalidInputException(INVALID_CHAR_IN_STRING);
					}
					if (this.currentCharacter == '\\') {
						if (this.unicodeAsBackSlash) {
							this.withoutUnicodePtr--;
							// consume next character
							this.unicodeAsBackSlash = false;
							if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\') && (this.source[this.currentPosition] == 'u')) {
								getNextUnicodeChar();
								isUnicode = true;
								this.withoutUnicodePtr--;
							} else {
								isUnicode = false;
							}
						} else {
							if (this.withoutUnicodePtr == 0) {
								unicodeInitializeBuffer(this.currentPosition - this.startPosition);
							}
							this.withoutUnicodePtr --;
							this.currentCharacter = this.source[this.currentPosition++];
						}
						// we need to compute the escape character in a separate buffer
						isRegex = scanRegexCharacter();
						if (!isRegex) {
							scanEscapeCharacter();
						}
						if (this.withoutUnicodePtr != 0) {
							if (isRegex) {
								char ch = this.currentCharacter;
								this.currentCharacter = '\\';
								unicodeStore();
								this.currentCharacter = ch;
							}
							unicodeStore();
						}
					}
					// consume next character
					this.unicodeAsBackSlash = false;
					if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
							&& (this.source[this.currentPosition] == 'u')) {
						getNextUnicodeChar();
						isUnicode = true;
					} else {
						isUnicode = false;
						if (this.withoutUnicodePtr != 0) {
							unicodeStore();
						}
					}

				}
			} catch (IndexOutOfBoundsException e) {
				this.currentPosition--;
				throw new InvalidInputException(UNTERMINATED_STRING);
			} catch (InvalidInputException e) {
				if (e.getMessage().equals(INVALID_ESCAPE)) {
					// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
					for (int lookAhead = 0; lookAhead < 50; lookAhead++) {
						if (this.currentPosition + lookAhead == this.eofPosition)
							break;
						if (this.source[this.currentPosition + lookAhead] == '\n')
							break;
						if (this.source[this.currentPosition + lookAhead] == '\"') {
							this.currentPosition += lookAhead + 1;
							break;
						}
					}

				}
				throw e; // rethrow
			}
			return TokenNameStringLiteral;
		} else {
			return super.scanForStringLiteral();
		}		
	}
	
	@Override
	protected int processSingleQuotes(boolean checkIfUnicode) throws InvalidInputException{
		if (this.tokenizeSingleQuotes) {
			return scanForSingleQuoteStringLiteral();
		} else {
			return super.processSingleQuotes(checkIfUnicode);
		}
	}

	protected int scanForSingleQuoteStringLiteral() throws InvalidInputException {
		this.unicodeAsBackSlash = false;
		boolean isUnicode = false;
		try {
			// consume next character
			this.unicodeAsBackSlash = false;
			isUnicode = false;
			if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
					&& (this.source[this.currentPosition] == 'u')) {
				getNextUnicodeChar();
				isUnicode = true;
			} else {
				if (this.withoutUnicodePtr != 0) {
					unicodeStore();
				}
			}

			while (this.currentCharacter != '\'') {
				boolean isRegex = false;
				if (this.currentPosition >= this.eofPosition) {
					throw new InvalidInputException(UNTERMINATED_STRING);
				}
				/**** \r and \n are not valid in string literals ****/
				if ((this.currentCharacter == '\n') || (this.currentCharacter == '\r')) {
					// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
					if (isUnicode) {
						int start = this.currentPosition;
						for (int lookAhead = 0; lookAhead < 50; lookAhead++) {
							if (this.currentPosition >= this.eofPosition) {
								this.currentPosition = start;
								break;
							}
							if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\') && (this.source[this.currentPosition] == 'u')) {
								isUnicode = true;
								getNextUnicodeChar();
							} else {
								isUnicode = false;
							}
							if (!isUnicode && this.currentCharacter == '\n') {
								this.currentPosition--; // set current position on new line character
								break;
							}
							if (this.currentCharacter == '\'') {
								throw new InvalidInputException(INVALID_CHAR_IN_STRING);
							}
						}
					} else {
						this.currentPosition--; // set current position on new line character
					}
					throw new InvalidInputException(INVALID_CHAR_IN_STRING);
				}
				if (this.currentCharacter == '\\') {
					if (this.unicodeAsBackSlash) {
						this.withoutUnicodePtr--;
						// consume next character
						this.unicodeAsBackSlash = false;
						if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\') && (this.source[this.currentPosition] == 'u')) {
							getNextUnicodeChar();
							isUnicode = true;
							this.withoutUnicodePtr--;
						} else {
							isUnicode = false;
						}
					} else {
						if (this.withoutUnicodePtr == 0) {
							unicodeInitializeBuffer(this.currentPosition - this.startPosition);
						}
						this.withoutUnicodePtr --;
						this.currentCharacter = this.source[this.currentPosition++];
					}
					// we need to compute the escape character in a separate buffer
					if (this.considerRegexInStringLiteral) {
						isRegex = scanRegexCharacter();
					}
					if (!isRegex) {
						scanEscapeCharacter();
					}
					if (this.withoutUnicodePtr != 0) {
						if (isRegex) {
							char ch = this.currentCharacter;
							this.currentCharacter = '\\';
							unicodeStore();
							this.currentCharacter = ch;
						}
						unicodeStore();
					}
				}
				// consume next character
				this.unicodeAsBackSlash = false;
				if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
						&& (this.source[this.currentPosition] == 'u')) {
					getNextUnicodeChar();
					isUnicode = true;
				} else {
					isUnicode = false;
					if (this.withoutUnicodePtr != 0) {
						unicodeStore();
					}
				}

			}
		} catch (IndexOutOfBoundsException e) {
			this.currentPosition--;
			throw new InvalidInputException(UNTERMINATED_STRING);
		} catch (InvalidInputException e) {
			if (e.getMessage().equals(INVALID_ESCAPE)) {
				// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
				for (int lookAhead = 0; lookAhead < 50; lookAhead++) {
					if (this.currentPosition + lookAhead == this.eofPosition)
						break;
					if (this.source[this.currentPosition + lookAhead] == '\n')
						break;
					if (this.source[this.currentPosition + lookAhead] == '\'') {
						this.currentPosition += lookAhead + 1;
						break;
					}
				}

			}
			throw e; // rethrow
		}
		return TokenNameSingleQuoteStringLiteral;		
	}

	protected boolean scanRegexCharacter() {
		boolean isRegex = false;
		switch (this.currentCharacter) {
			case 'd' :
			case 'D' :
			case 's' :
			case 'S' :
			case 'w' :
			case 'W' :
			case 'p' :
			case 'b' :
			case 'B' :
			case 'A' :
			case 'G' :
			case 'z' :
			case 'Z' :
			case 'Q' :
			case 'E' :
			case 'a' :
			case 'e' :
				isRegex = true;
				break;
			default:
				//do nothing
		}
		return isRegex;
	}
}
