/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameEOF;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * Parser specialized for decoding javadoc comments
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractCommentParser implements JavadocTagConstants {

	// Kind of comment parser
	public final static int COMPIL_PARSER = 0x0001;
	public final static int DOM_PARSER = 0x0002;
	public final static int SELECTION_PARSER = 0x0004;
	public final static int COMPLETION_PARSER = 0x0008;
	public final static int SOURCE_PARSER = 0x0010;
	public final static int FORMATTER_COMMENT_PARSER = 0x0020;
	protected final static int PARSER_KIND = 0x00FF;
	protected final static int TEXT_PARSE = 0x0100; // flag saying that text must be stored
	protected final static int TEXT_VERIF = 0x0200; // flag saying that text must be verified

	// Parser recovery states
	protected final static int QUALIFIED_NAME_RECOVERY = 1;
	protected final static int ARGUMENT_RECOVERY= 2;
	protected final static int ARGUMENT_TYPE_RECOVERY = 3;
	protected final static int EMPTY_ARGUMENT_RECOVERY = 4;

	// Parse infos
	public Scanner scanner;
	public char[] source;
	protected Parser sourceParser;
	private int currentTokenType = -1;

	// Options
	public boolean checkDocComment = false;
	public boolean setJavadocPositions = false;
	public boolean reportProblems;
	protected long complianceLevel;
	protected long sourceLevel;

	// Support for {@inheritDoc}
	protected long [] inheritedPositions;
	protected int inheritedPositionsPtr;
	private final static int INHERITED_POSITIONS_ARRAY_INCREMENT = 4;

	// Results
	protected boolean deprecated;
	protected Object returnStatement;

	// Positions
	protected int javadocStart, javadocEnd;
	protected int javadocTextStart, javadocTextEnd = -1;
	protected int firstTagPosition;
	protected int index, lineEnd;
	protected int tokenPreviousPosition, lastIdentifierEndPosition, starPosition;
	protected int textStart, memberStart;
	protected int tagSourceStart, tagSourceEnd;
	protected int inlineTagStart;
	protected int[] lineEnds;

	// Flags
	protected boolean lineStarted = false;
	protected boolean inlineTagStarted = false;
	protected boolean abort = false;
	protected int kind;
	protected int tagValue = NO_TAG_VALUE;
	protected int lastBlockTagValue = NO_TAG_VALUE;
	protected boolean snippetInlineTagStarted = false;
	private int nonRegionTagCount, inlineTagCount;
	final static String SINGLE_LINE_COMMENT = "//"; //$NON-NLS-1$

	// Line pointers
	private int linePtr, lastLinePtr;

	// Identifier stack
	protected int identifierPtr;
	protected char[][] identifierStack;
	protected int identifierLengthPtr;
	protected int[] identifierLengthStack;
	protected long[] identifierPositionStack;

	// Ast stack
	protected final static int AST_STACK_INCREMENT = 10;
	protected int astPtr;
	protected Object[] astStack;
	protected int astLengthPtr;
	protected int[] astLengthStack;

	// Uses stack
	protected int usesReferencesPtr = -1;
	protected TypeReference[] usesReferencesStack;

	// Provides stack
	protected int providesReferencesPtr = -1;
	protected TypeReference[] providesReferencesStack;

	// Snippet search project path as src classpath for file/class support
	private String projectPath;
	private List srcClasspath;

	protected AbstractCommentParser(Parser sourceParser) {
		this.sourceParser = sourceParser;
		this.scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_3, null, null, true/*taskCaseSensitive*/,
				sourceParser != null ? this.sourceParser.options.enablePreviewFeatures : false);
		this.identifierStack = new char[20][];
		this.identifierPositionStack = new long[20];
		this.identifierLengthStack = new int[10];
		this.astStack = new Object[30];
		this.astLengthStack = new int[20];
		this.reportProblems = sourceParser != null;
		setSourceComplianceLevel();
	}

	/* (non-Javadoc)
	 * Returns true if tag @deprecated is present in javadoc comment.
	 *
	 * If javadoc checking is enabled, will also construct an Javadoc node,
	 * which will be stored into Parser.javadoc slot for being consumed later on.
	 */
	protected boolean commentParse() {

		boolean validComment = true;
		try {
			// Init local variables
			this.astLengthPtr = -1;
			this.astPtr = -1;
			this.identifierPtr = -1;
			this.currentTokenType = -1;
			setInlineTagStarted(false);
			this.inlineTagStart = -1;
			this.lineStarted = false;
			this.returnStatement = null;
			this.inheritedPositions = null;
			this.lastBlockTagValue = NO_TAG_VALUE;
			this.deprecated = false;
			this.lastLinePtr = getLineNumber(this.javadocEnd);
			this.textStart = -1;
			this.abort = false;
			char previousChar = 0;
			int invalidTagLineEnd = -1;
			int invalidInlineTagLineEnd = -1;
			boolean lineHasStar = true;
			boolean verifText = (this.kind & TEXT_VERIF) != 0;
			boolean isDomParser = (this.kind & DOM_PARSER) != 0;
			boolean isFormatterParser = (this.kind & FORMATTER_COMMENT_PARSER) != 0;
			int lastStarPosition = -1;

			// Init scanner position
			this.linePtr = getLineNumber(this.firstTagPosition);
			int realStart = this.linePtr==1 ? this.javadocStart : this.scanner.getLineEnd(this.linePtr-1)+1;
			if (realStart < this.javadocStart) realStart = this.javadocStart;
			this.scanner.resetTo(realStart, this.javadocEnd);
			this.index = realStart;
			if (realStart == this.javadocStart) {
				readChar(); // starting '/'
				readChar(); // first '*'
			}
			int previousPosition = this.index;
			char nextCharacter = 0;
			if (realStart == this.javadocStart) {
				nextCharacter = readChar(); // second '*'
				while (peekChar() == '*') {
					nextCharacter = readChar(); // read all contiguous '*'
				}
				this.javadocTextStart = this.index;
			}
			this.lineEnd = (this.linePtr == this.lastLinePtr) ? this.javadocEnd: this.scanner.getLineEnd(this.linePtr) - 1;
			this.javadocTextEnd = this.javadocEnd - 2; // supposed text end, it will be refined later...
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345
						// when parsing tags such as @code and @literal,
						// any tag should be discarded and considered as plain text until
						// properly closed with closing brace
						boolean considerTagAsPlainText = false;
						// internal counter for opening braces
						int openingBraces = 0;
			// Loop on each comment character
			int textEndPosition = -1;
			while (!this.abort && this.index < this.javadocEnd) {

				// Store previous position and char
				previousPosition = this.index;
				previousChar = nextCharacter;

				// Calculate line end (cannot use this.scanner.linePtr as scanner does not parse line ends again)
				if (this.index > (this.lineEnd+1)) {
					updateLineEnd();
				}

				// Read next char only if token was consumed
				if (this.currentTokenType < 0) {
					nextCharacter = readChar(); // consider unicodes
				} else {
					previousPosition = this.scanner.getCurrentTokenStartPosition();
					switch (this.currentTokenType) {
						case TerminalTokens.TokenNameRBRACE:
							nextCharacter = '}';
							break;
						case TerminalTokens.TokenNameMULTIPLY:
							nextCharacter = '*';
							break;
					default:
							nextCharacter = this.scanner.currentCharacter;
					}
					consumeToken();
				}

				// Consume rules depending on the read character
				switch (nextCharacter) {
					case '@' :
						// Start tag parsing only if we are on line beginning or at inline tag beginning
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345: ignore all tags when inside @literal or @code tags
						if (considerTagAsPlainText) {
							// new tag found
							if (!this.lineStarted) {
								// we may want to report invalid syntax when no closing brace found,
								// or when incoherent number of closing braces found
								if (openingBraces > 0 && this.reportProblems) {
									this.sourceParser.problemReporter().javadocUnterminatedInlineTag(this.inlineTagStart, invalidInlineTagLineEnd);
								}
								considerTagAsPlainText = false;
								this.inlineTagStarted = false;
								openingBraces = 0;
							}
						} else if ((!this.lineStarted || previousChar == '{') || lookForTagsInSnippets()) {
							if (this.inlineTagStarted) {
								setInlineTagStarted(false);
								// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53279
								// Cannot have @ inside inline comment
								if (this.reportProblems) {
									int end = previousPosition<invalidInlineTagLineEnd ? previousPosition : invalidInlineTagLineEnd;
									this.sourceParser.problemReporter().javadocUnterminatedInlineTag(this.inlineTagStart, end);
								}
								validComment = false;
								if (this.textStart != -1 && this.textStart < textEndPosition) {
									pushText(this.textStart, textEndPosition);
								}
								if (isDomParser || isFormatterParser) {
									refreshInlineTagPosition(textEndPosition);
								}
							}
							if (previousChar == '{') {
								if (this.textStart != -1) {
									if (this.textStart < textEndPosition) {
										pushText(this.textStart, textEndPosition);
									}
								}
								setInlineTagStarted(true);
								invalidInlineTagLineEnd = this.lineEnd;
							} else if (this.textStart != -1 && this.textStart < invalidTagLineEnd) {
								if(!lookForTagsInSnippets())
								pushText(this.textStart, invalidTagLineEnd);
							}
							this.scanner.resetTo(this.index, this.javadocEnd);
							this.currentTokenType = -1; // flush token cache at line begin
							try {
								if (!parseTag(previousPosition)) {
									// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
									// do not stop the inline tag when error is encountered to get text after
									validComment = false;
									// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
									// for DOM AST node, store tag as text in case of invalid syntax
									if (isDomParser) {
										createTag();
									}
									this.textStart = this.tagSourceEnd+1;
									invalidTagLineEnd  = this.lineEnd;
									textEndPosition = this.index;
								}
								// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345
								// dealing with @literal or @code tags: ignore next tags
								if (!isFormatterParser && (this.tagValue == TAG_LITERAL_VALUE || this.tagValue == TAG_CODE_VALUE)) {
									considerTagAsPlainText = true;
									openingBraces++;
								}
							} catch (InvalidInputException e) {
								consumeToken();
							}
						} else {
							textEndPosition = this.index;
							if (verifText && this.tagValue == TAG_RETURN_VALUE && this.returnStatement != null) {
								refreshReturnStatement();
							} else if (isFormatterParser) {
								if (this.textStart == -1) this.textStart = previousPosition;
							}
						}
						this.lineStarted = true;
						break;
					case '\r':
					case '\n':
						if (this.lineStarted) {
							if (isFormatterParser && !ScannerHelper.isWhitespace(previousChar)) {
								textEndPosition = previousPosition;
							}
							if (this.textStart != -1 && this.textStart < textEndPosition) {
								pushText(this.textStart, textEndPosition);
							}
						}
						this.lineStarted = false;
						lineHasStar = false;
						// Fix bug 51650
						this.textStart = -1;
						break;
					case '}' :
						if (verifText && this.tagValue == TAG_RETURN_VALUE && this.returnStatement != null) {
							refreshReturnStatement();
						}
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345: when ignoring tags, only decrement the opening braces counter
						if (considerTagAsPlainText) {
							invalidInlineTagLineEnd = this.lineEnd;
							if (--openingBraces == 0) {
								considerTagAsPlainText = false; // re-enable tag validation
							}
						}
						if (this.inlineTagStarted) {
							textEndPosition = this.index - 1;
							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345: do not push text yet if ignoring tags
							if (!considerTagAsPlainText) {
								if (this.lineStarted && this.textStart != -1 && this.textStart < textEndPosition) {
									pushText(this.textStart, textEndPosition);
								}
								refreshInlineTagPosition(previousPosition);
							}
							if (!isFormatterParser && !considerTagAsPlainText)
								this.textStart = this.index;
							setInlineTagStarted(false);
						} else {
							if (!this.lineStarted) {
								this.textStart = previousPosition;
							}
						}
						this.lineStarted = true;
						textEndPosition = this.index;
						break;
					case '{' :
						if (verifText && this.tagValue == TAG_RETURN_VALUE && this.returnStatement != null) {
							refreshReturnStatement();
						}
												// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345: count opening braces when ignoring tags
						if (considerTagAsPlainText) {
							openingBraces++;
						} else if (this.inlineTagStarted) {
							setInlineTagStarted(false);
							// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53279
							// Cannot have opening brace in inline comment
							if (this.reportProblems) {
								int end = previousPosition<invalidInlineTagLineEnd ? previousPosition : invalidInlineTagLineEnd;
								this.sourceParser.problemReporter().javadocUnterminatedInlineTag(this.inlineTagStart, end);
							}
							if (this.lineStarted && this.textStart != -1 && this.textStart < textEndPosition) {
								pushText(this.textStart, textEndPosition);
							}
							refreshInlineTagPosition(textEndPosition);
							textEndPosition = this.index;
						} else if (peekChar() != '@') {
							if (this.textStart == -1) this.textStart = previousPosition;
							textEndPosition = this.index;
						}
						if (!this.lineStarted) {
							this.textStart = previousPosition;
						}
						this.lineStarted = true;
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345: do not update tag start position when ignoring tags
						if (!considerTagAsPlainText) this.inlineTagStart = previousPosition;
						break;
					case '*' :
						// Store the star position as text start while formatting
						lastStarPosition = previousPosition;
						if (previousChar != '*') {
							this.starPosition = previousPosition;
							if (isDomParser || isFormatterParser) {
								if (lineHasStar) {
									this.lineStarted = true;
									if (this.textStart == -1) {
										this.textStart = previousPosition;
										if (this.index <= this.javadocTextEnd) textEndPosition = this.index;
									}
								}
								if (!this.lineStarted) {
									lineHasStar = true;
								}
							}
						}
						break;
					case '\u000c' :	/* FORM FEED               */
					case ' ' :			/* SPACE                   */
					case '\t' :			/* HORIZONTAL TABULATION   */
						// Do not include trailing spaces in text while formatting
						if (isFormatterParser) {
							if (!ScannerHelper.isWhitespace(previousChar)) {
								textEndPosition = previousPosition;
							}
						} else if (this.lineStarted && isDomParser) {
							textEndPosition = this.index;
						}
						break;
					case '/':
						if (previousChar == '*') {
							// End of javadoc
							break;
						}
						// $FALL-THROUGH$ - fall through default case
					default :
						if (isFormatterParser && nextCharacter == '<') {
							// html tags are meaningful for formatter parser
							int initialIndex = this.index;
							this.scanner.resetTo(this.index, this.javadocEnd);
							if (!ScannerHelper.isWhitespace(previousChar)) {
								textEndPosition = previousPosition;
							}
							if (parseHtmlTag(previousPosition, textEndPosition)) {
								break;
							}
							if (this.abort) return false;
							// Wrong html syntax continue to process character normally
							this.scanner.currentPosition = initialIndex;
							this.index = initialIndex;
						}
						if (verifText && this.tagValue == TAG_RETURN_VALUE && this.returnStatement != null) {
							refreshReturnStatement();
						}
						if (!this.lineStarted || this.textStart == -1) {
							this.textStart = previousPosition;
						}
						this.lineStarted = true;
						textEndPosition = this.index;
						break;
				}
			}
			this.javadocTextEnd = this.starPosition-1;

			// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53279
			// Cannot leave comment inside inline comment
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345: handle unterminated @code or @literal tag
			if (this.inlineTagStarted || considerTagAsPlainText) {
				if (this.reportProblems) {
					int end = this.javadocTextEnd<invalidInlineTagLineEnd ? this.javadocTextEnd : invalidInlineTagLineEnd;
					if (this.index >= this.javadocEnd) end = invalidInlineTagLineEnd;
					this.sourceParser.problemReporter().javadocUnterminatedInlineTag(this.inlineTagStart, end);
				}
				if (this.lineStarted && this.textStart != -1 && this.textStart < textEndPosition) {
					pushText(this.textStart, textEndPosition);
				}
				refreshInlineTagPosition(textEndPosition);
				setInlineTagStarted(false);
			} else if (this.lineStarted && this.textStart != -1 && this.textStart <= textEndPosition && (this.textStart < this.starPosition || this.starPosition == lastStarPosition)) {
				pushText(this.textStart, textEndPosition);
			}
			updateDocComment();
		} catch (Exception ex) {
			validComment = false;
		}
		return validComment;
	}

	protected void consumeToken() {
		this.currentTokenType = -1; // flush token cache
		updateLineEnd();
	}

	protected abstract Object createArgumentReference(char[] name, int dim, boolean isVarargs, Object typeRef, long[] dimPos, long argNamePos) throws InvalidInputException;
	protected boolean createFakeReference(int start) {
		// Do nothing by default
		return true;
	}
	protected abstract Object createFieldReference(Object receiver) throws InvalidInputException;
	protected abstract Object createMethodReference(Object receiver, List arguments) throws InvalidInputException;
	protected Object createReturnStatement() { return null; }
	protected abstract void createTag();
	protected abstract Object createTypeReference(int primitiveToken);
	protected abstract Object createTypeReference(int primitiveToken, boolean canBeModule);
	protected abstract Object createModuleTypeReference(int primitiveToken, int moduleRefTokenCount);

	private int getIndexPosition() {
		if (this.index > this.lineEnd) {
			return this.lineEnd;
		} else {
			return this.index-1;
		}
	}

	/**
	 * Search the line number corresponding to a specific position.
	 * Warning: returned position is 1-based index!
	 * @see Scanner#getLineNumber(int) We cannot directly use this method
	 * when linePtr field is not initialized.
	 */
	private int getLineNumber(int position) {

		if (this.scanner.linePtr != -1) {
			return Util.getLineNumber(position, this.scanner.lineEnds, 0, this.scanner.linePtr);
		}
		if (this.lineEnds == null)
			return 1;
		return Util.getLineNumber(position, this.lineEnds, 0, this.lineEnds.length-1);
	}

	protected int getTokenEndPosition() {
		if (this.scanner.getCurrentTokenEndPosition() > this.lineEnd) {
			return this.lineEnd;
		} else {
			return this.scanner.getCurrentTokenEndPosition();
		}
	}

	/**
	 * @return Returns the currentTokenType.
	 */
	protected int getCurrentTokenType() {
		return this.currentTokenType;
	}

	/*
	 * Parse argument in @see tag method reference
	 */
	protected Object parseArguments(Object receiver) throws InvalidInputException {
		return parseArguments(receiver, true);
	}

	/*
	 * Parse argument in @see tag method reference
	 */
	protected Object parseArguments(Object receiver, boolean checkVerifySpaceOrEndComment) throws InvalidInputException {

		// Init
		int modulo = 0; // should be 2 for (Type,Type,...) or 3 for (Type arg,Type arg,...)
		int iToken = 0;
		char[] argName = null;
		List arguments = new ArrayList(10);
		int start = this.scanner.getCurrentTokenStartPosition();
		Object typeRef = null;
		int dim = 0;
		boolean isVarargs = false;
		long[] dimPositions = new long[20]; // assume that there won't be more than 20 dimensions...
		char[] name = null;
		long argNamePos = -1;
		boolean tokenWhiteSpace = this.scanner.tokenizeWhiteSpace;
		this.scanner.tokenizeWhiteSpace = false;

		try {
			// Parse arguments declaration if method reference
			nextArg : while (this.index < this.scanner.eofPosition) {

				// Read argument type reference
				try {
					typeRef = parseQualifiedName(false);
					if (this.abort) return null; // May be aborted by specialized parser
				} catch (InvalidInputException e) {
					break nextArg;
				}
				boolean firstArg = modulo == 0;
				if (firstArg) { // verify position
					if (iToken != 0)
						break nextArg;
				} else if ((iToken % modulo) != 0) {
						break nextArg;
				}
				if (typeRef == null) {
					if (firstArg && this.currentTokenType == TerminalTokens.TokenNameRPAREN) {
						// verify characters after arguments declaration (expecting white space or end comment)
						if (!verifySpaceOrEndComment()) {
							int end = this.starPosition == -1 ? this.lineEnd : this.starPosition;
							if (this.source[end]=='\n') end--;
							if (this.reportProblems) this.sourceParser.problemReporter().javadocMalformedSeeReference(start, end);
							return null;
						}
						this.lineStarted = true;
						return createMethodReference(receiver, null);
					}
					break nextArg;
				}
				iToken++;

				// Read possible additional type info
				dim = 0;
				isVarargs = false;
				if (readToken() == TerminalTokens.TokenNameLBRACKET) {
					// array declaration
					while (readToken() == TerminalTokens.TokenNameLBRACKET) {
						int dimStart = this.scanner.getCurrentTokenStartPosition();
						consumeToken();
						if (readToken() != TerminalTokens.TokenNameRBRACKET) {
							break nextArg;
						}
						consumeToken();
						dimPositions[dim++] = (((long) dimStart) << 32) + this.scanner.getCurrentTokenEndPosition();
					}
				} else if (readToken() == TerminalTokens.TokenNameELLIPSIS) {
					// ellipsis declaration
					int dimStart = this.scanner.getCurrentTokenStartPosition();
					dimPositions[dim++] = (((long) dimStart) << 32) + this.scanner.getCurrentTokenEndPosition();
					consumeToken();
					isVarargs = true;
				}

				// Read argument name
				argNamePos = -1;
				int argumentName = readToken();
				if (argumentName == TerminalTokens.TokenNameIdentifier || argumentName == TerminalTokens.TokenNameUNDERSCORE) {
					consumeToken();
					if (firstArg) { // verify position
						if (iToken != 1)
							break nextArg;
					} else if ((iToken % modulo) != 1) {
							break nextArg;
					}
					if (argName == null) { // verify that all arguments name are declared
						if (!firstArg) {
							break nextArg;
						}
					}
					argName = this.scanner.getCurrentIdentifierSource();
					argNamePos = (((long)this.scanner.getCurrentTokenStartPosition())<<32)+this.scanner.getCurrentTokenEndPosition();
					iToken++;
				} else if (argName != null) { // verify that no argument name is declared
					break nextArg;
				}

				// Verify token position
				if (firstArg) {
					modulo = iToken + 1;
				} else {
					if ((iToken % modulo) != (modulo - 1)) {
						break nextArg;
					}
				}

				// Read separator or end arguments declaration
				int token = readToken();
				name = argName == null ? CharOperation.NO_CHAR : argName;
				if (token == TerminalTokens.TokenNameCOMMA) {
					// Create new argument
					Object argument = createArgumentReference(name, dim, isVarargs, typeRef, dimPositions, argNamePos);
					if (this.abort) return null; // May be aborted by specialized parser
					arguments.add(argument);
					consumeToken();
					iToken++;
				} else if (token == TerminalTokens.TokenNameRPAREN) {
					// verify characters after arguments declaration (expecting white space or end comment)
					if (checkVerifySpaceOrEndComment && !verifySpaceOrEndComment()) {
						int end = this.starPosition == -1 ? this.lineEnd : this.starPosition;
						if (this.source[end]=='\n') end--;
						if (this.reportProblems) this.sourceParser.problemReporter().javadocMalformedSeeReference(start, end);
						return null;
					}
					// Create new argument
					Object argument = createArgumentReference(name, dim, isVarargs, typeRef, dimPositions, argNamePos);
					if (this.abort) return null; // May be aborted by specialized parser
					arguments.add(argument);
					consumeToken();
					return createMethodReference(receiver, arguments);
				} else {
					break nextArg;
				}
			}

			// Something wrong happened => Invalid input
			throw Scanner.invalidInput();
		} finally {
			// we have to make sure that this is reset to the previous value even if an exception occurs
			this.scanner.tokenizeWhiteSpace = tokenWhiteSpace;
		}
	}

	/**
	 * Parse a possible HTML tag like:
	 * <ul>
	 * 	<li>&lt;code&gt;
	 * 	<li>&lt;br&gt;
	 * 	<li>&lt;h?&gt;
	 * </ul>
	 *
	 * Note that the default is to do nothing!
	 *
	 * @param previousPosition The position of the {@code '<'} character on which the tag might start
	 * @param endTextPosition The position of the end of the previous text
	 * @return <code>true</code> if a valid html tag has been parsed, <code>false</code>
	 * 	otherwise
	 * @throws InvalidInputException If any problem happens during the parse in this area
	 */
	protected boolean parseHtmlTag(int previousPosition, int endTextPosition) throws InvalidInputException {
		return false;
	}

	protected boolean lookForTagsInSnippets() {
		return false;
	}

	/*
	 * Parse an URL link reference in @see tag
	 */
	protected boolean parseHref() throws InvalidInputException {
		boolean skipComments = this.scanner.skipComments;
		this.scanner.skipComments = true;
		boolean tokenWhiteSpace = this.scanner.tokenizeWhiteSpace;
		this.scanner.tokenizeWhiteSpace = false;
		try {
			int start = this.scanner.getCurrentTokenStartPosition();
			char currentChar = readChar();
			if (currentChar == 'a' || currentChar == 'A') {
				this.scanner.currentPosition = this.index;
				int token = readToken();
				if (token == TerminalTokens.TokenNameIdentifier || token == TerminalTokens.TokenNameUNDERSCORE) {
					consumeToken();
					try {
						if (CharOperation.equals(this.scanner.getCurrentIdentifierSource(), HREF_TAG, false) &&
							readToken() == TerminalTokens.TokenNameEQUAL) {
							consumeToken();
							if (readToken() == TerminalTokens.TokenNameStringLiteral) {
								consumeToken();
								while (this.index < this.javadocEnd) { // main loop to search for the </a> pattern
									// Skip all characters after string literal until closing '>' (see bug 68726)
									while (readToken() != TerminalTokens.TokenNameGREATER) {
										if (this.scanner.currentPosition >= this.scanner.eofPosition || this.scanner.currentCharacter == '@' ||
												(this.inlineTagStarted && this.scanner.currentCharacter == '}')) {
											// Reset position: we want to rescan last token
											this.index = this.tokenPreviousPosition;
											this.scanner.currentPosition = this.tokenPreviousPosition;
											this.currentTokenType = -1;
											// Signal syntax error
											if (this.tagValue != TAG_VALUE_VALUE) { // do not report error for @value tag, this will be done after...
												if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidSeeHref(start, this.lineEnd);
											}
											return false;
										}
										this.currentTokenType = -1; // consume token without updating line end
									}
									consumeToken(); // update line end as new lines are allowed in URL description
									while (readToken() != TerminalTokens.TokenNameLESS) {
										if (this.scanner.currentPosition >= this.scanner.eofPosition || this.scanner.currentCharacter == '@' ||
												(this.inlineTagStarted && this.scanner.currentCharacter == '}')) {
											// Reset position: we want to rescan last token
											this.index = this.tokenPreviousPosition;
											this.scanner.currentPosition = this.tokenPreviousPosition;
											this.currentTokenType = -1;
											// Signal syntax error
											if (this.tagValue != TAG_VALUE_VALUE) { // do not report error for @value tag, this will be done after...
												if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidSeeHref(start, this.lineEnd);
											}
											return false;
										}
										consumeToken();
									}
									consumeToken();
									start = this.scanner.getCurrentTokenStartPosition();
									currentChar = readChar();
									// search for the </a> pattern and store last char read
									if (currentChar == '/') {
										currentChar = readChar();
										if (currentChar == 'a' || currentChar =='A') {
											currentChar = readChar();
											if (currentChar == '>') {
												return true; // valid href
											}
										}
									}
									// search for invalid char in tags
									if (currentChar == '\r' || currentChar == '\n' || currentChar == '\t' || currentChar == ' ') {
										break;
									}
								}
							}
						}
					} catch (InvalidInputException ex) {
						// Do nothing as we want to keep positions for error message
					}
				}
			}
			// Reset position: we want to rescan last token
			this.index = this.tokenPreviousPosition;
			this.scanner.currentPosition = this.tokenPreviousPosition;
			this.currentTokenType = -1;
			// Signal syntax error
			if (this.tagValue != TAG_VALUE_VALUE) { // do not report error for @value tag, this will be done after...
				if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidSeeHref(start, this.lineEnd);
			}
		}
		finally {
			this.scanner.skipComments = skipComments;
			this.scanner.tokenizeWhiteSpace = tokenWhiteSpace;
		}
		return false;
	}

	/*
	 * Parse tag followed by an identifier
	 */
	protected boolean parseIdentifierTag(boolean report) {
		int token = readTokenSafely();
		switch (token) {
			case TerminalTokens.TokenNameUNDERSCORE:
			case TerminalTokens.TokenNameIdentifier:
				pushIdentifier(true, false);
				return true;
		}
		if (report) {
			this.sourceParser.problemReporter().javadocMissingIdentifier(this.tagSourceStart, this.tagSourceEnd, this.sourceParser.modifiers);
		}
		return false;
	}

	protected Object parseMember(Object receiver) throws InvalidInputException {
		return parseMember(receiver, false);
	}

	/*
	 * Parse a method reference in @see tag
	 */
	protected Object parseMember(Object receiver, boolean refInStringLiteral) throws InvalidInputException {
		// Init
		this.identifierPtr = -1;
		this.identifierLengthPtr = -1;
		int start = this.scanner.getCurrentTokenStartPosition();
		this.memberStart = start;

		// Get member identifier
		int memberIdentifier = readToken();
		if (memberIdentifier == TerminalTokens.TokenNameIdentifier || memberIdentifier == TerminalTokens.TokenNameUNDERSCORE) {
			if (this.scanner.currentCharacter == '.') { // member name may be qualified (inner class constructor reference)
				parseQualifiedName(true);
			} else {
				consumeToken();
				pushIdentifier(true, false);
			}
			boolean tokenWhiteSpace = this.scanner.tokenizeWhiteSpace;
			this.scanner.tokenizeWhiteSpace = false;
			try {
				// Look for next token to know whether it's a field or method reference
				int previousPosition = this.index;
				try {
					int token = readToken();
					if (token == TerminalTokens.TokenNameLPAREN) {
						consumeToken();
						start = this.scanner.getCurrentTokenStartPosition();
						try {
							return parseArguments(receiver, !refInStringLiteral);
						} catch (InvalidInputException e) {
							int end = this.scanner.getCurrentTokenEndPosition() < this.lineEnd ?
									this.scanner.getCurrentTokenEndPosition() :
									this.scanner.getCurrentTokenStartPosition();
							end = end < this.lineEnd ? end : this.lineEnd;
							if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidSeeReferenceArgs(start, end);
						}
						return null;
					}
				} catch (InvalidInputException e) {
					if (!refInStringLiteral || (!Scanner.INVALID_CHAR_IN_STRING.equals(e.getMessage())
							&& !Scanner.INVALID_CHARACTER_CONSTANT.equals(e.getMessage()))) {
						throw e;
					}
				}

				// Reset position: we want to rescan last token
				this.index = previousPosition;
				this.scanner.currentPosition = previousPosition;
				this.currentTokenType = -1;

				// Verify character(s) after identifier (expecting space or end comment)
				if (!refInStringLiteral && !verifySpaceOrEndComment()) {
					int end = this.starPosition == -1 ? this.lineEnd : this.starPosition;
					if (this.source[end]=='\n') end--;
					if (this.reportProblems) this.sourceParser.problemReporter().javadocMalformedSeeReference(start, end);
					return null;
				}
				return createFieldReference(receiver);
			} finally {
				this.scanner.tokenizeWhiteSpace = tokenWhiteSpace;
			}
		}
		int end = getTokenEndPosition() - 1;
		end = start > end ? start : end;
		if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidReference(start, end);
		// Reset position: we want to rescan last token
		this.index = this.tokenPreviousPosition;
		this.scanner.currentPosition = this.tokenPreviousPosition;
		this.currentTokenType = -1;
		return null;
	}

	/*
	 * Parse @param tag declaration
	 */
	protected boolean parseParam() throws InvalidInputException {

		// Store current state
		int start = this.tagSourceStart;
		int end = this.tagSourceEnd;
		boolean tokenWhiteSpace = this.scanner.tokenizeWhiteSpace;
		this.scanner.tokenizeWhiteSpace = true;

		try {
			// Verify that there are whitespaces after tag
			boolean isCompletionParser = (this.kind & COMPLETION_PARSER) != 0;
			if (this.scanner.currentCharacter != ' ' && !ScannerHelper.isWhitespace(this.scanner.currentCharacter)) {
				if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidTag(start, this.scanner.getCurrentTokenEndPosition());
				if (!isCompletionParser) {
					this.scanner.currentPosition = start;
					this.index = start;
				}
				this.currentTokenType = -1;
				return false;
			}

			// Get first non whitespace token
			this.identifierPtr = -1;
			this.identifierLengthPtr = -1;
			boolean hasMultiLines = this.scanner.currentPosition > (this.lineEnd+1);
			boolean isTypeParam = false;
			boolean valid = true, empty = true;
			boolean mayBeGeneric = this.sourceLevel >= ClassFileConstants.JDK1_5;
			int token = -1;
			nextToken: while (true) {
				this.currentTokenType = -1;
				try {
					token = readToken();
				} catch (InvalidInputException e) {
					valid = false;
				}
				switch (token) {
					case TerminalTokens.TokenNameUNDERSCORE:
					case TerminalTokens.TokenNameIdentifier :
						if (valid) {
							// store param name id
							pushIdentifier(true, false);
							start = this.scanner.getCurrentTokenStartPosition();
							end = hasMultiLines ? this.lineEnd: this.scanner.getCurrentTokenEndPosition();
							break nextToken;
						}
						// $FALL-THROUGH$ - fall through next case to report error
					case TerminalTokens.TokenNameLESS:
						if (valid && mayBeGeneric) {
							// store '<' in identifiers stack as we need to add it to tag element (bug 79809)
							pushIdentifier(true, true);
							start = this.scanner.getCurrentTokenStartPosition();
							end = hasMultiLines ? this.lineEnd: this.scanner.getCurrentTokenEndPosition();
							isTypeParam = true;
							break nextToken;
						}
						// $FALL-THROUGH$ - fall through next case to report error
					default:
						if (token == TerminalTokens.TokenNameLEFT_SHIFT) isTypeParam = true;
						if (valid && !hasMultiLines) start = this.scanner.getCurrentTokenStartPosition();
						valid = false;
						if (!hasMultiLines) {
							empty = false;
							end = hasMultiLines ? this.lineEnd: this.scanner.getCurrentTokenEndPosition();
							break;
						}
						end = this.lineEnd;
						// $FALL-THROUGH$ - when several lines, fall through next case to report problem immediately
					case TerminalTokens.TokenNameWHITESPACE:
						if (this.scanner.currentPosition > (this.lineEnd+1)) hasMultiLines = true;
						if (valid) break;
						// $FALL-THROUGH$ - if not valid fall through next case to report error
					case TerminalTokens.TokenNameEOF:
						if (this.reportProblems)
							if (empty)
								this.sourceParser.problemReporter().javadocMissingParamName(start, end, this.sourceParser.modifiers);
							else if (mayBeGeneric && isTypeParam)
								this.sourceParser.problemReporter().javadocInvalidParamTypeParameter(start, end);
							else
								this.sourceParser.problemReporter().javadocInvalidParamTagName(start, end);
						if (!isCompletionParser) {
							this.scanner.currentPosition = start;
							this.index = start;
						}
						this.currentTokenType = -1;
						return false;
				}
			}

			// Scan more tokens for type parameter declaration
			if (isTypeParam && mayBeGeneric) {
				// Get type parameter name
				nextToken: while (true) {
					this.currentTokenType = -1;
					try {
						token = readToken();
					} catch (InvalidInputException e) {
						valid = false;
					}
					switch (token) {
						case TerminalTokens.TokenNameWHITESPACE:
							if (valid && this.scanner.currentPosition <= (this.lineEnd+1)) {
								break;
							}
							// $FALL-THROUGH$ - if not valid fall through next case to report error
						case TerminalTokens.TokenNameEOF:
							if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidParamTypeParameter(start, end);
							if (!isCompletionParser) {
								this.scanner.currentPosition = start;
								this.index = start;
							}
							this.currentTokenType = -1;
							return false;
						case TerminalTokens.TokenNameUNDERSCORE:
						case TerminalTokens.TokenNameIdentifier :
							end = hasMultiLines ? this.lineEnd: this.scanner.getCurrentTokenEndPosition();
							if (valid) {
								// store param name id
								pushIdentifier(false, false);
								break nextToken;
							}
							break;
						default:
							end = hasMultiLines ? this.lineEnd: this.scanner.getCurrentTokenEndPosition();
							valid = false;
							break;
					}
				}

				// Get last character of type parameter declaration
				boolean spaces = false;
				nextToken: while (true) {
					this.currentTokenType = -1;
					try {
						token = readToken();
					} catch (InvalidInputException e) {
						valid = false;
					}
					switch (token) {
						case TerminalTokens.TokenNameWHITESPACE:
							if (this.scanner.currentPosition > (this.lineEnd+1)) {
								// do not accept type parameter declaration on several lines
								hasMultiLines = true;
								valid = false;
							}
							spaces = true;
							if (valid) break;
							// $FALL-THROUGH$ - if not valid fall through next case to report error
						case TerminalTokens.TokenNameEOF:
							if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidParamTypeParameter(start, end);
							if (!isCompletionParser) {
								this.scanner.currentPosition = start;
								this.index = start;
							}
							this.currentTokenType = -1;
							return false;
						case TerminalTokens.TokenNameGREATER:
							end = hasMultiLines ? this.lineEnd: this.scanner.getCurrentTokenEndPosition();
							if (valid) {
								// store '>' in identifiers stack as we need to add it to tag element (bug 79809)
								pushIdentifier(false, true);
								break nextToken;
							}
							break;
						default:
							if (!spaces) end = hasMultiLines ? this.lineEnd: this.scanner.getCurrentTokenEndPosition();
							valid = false;
							break;
					}
				}
			}

			// Verify that tag name is well followed by white spaces
			if (valid) {
				this.currentTokenType = -1;
				int restart = this.scanner.currentPosition;
				try {
					token = readTokenAndConsume();
				} catch (InvalidInputException e) {
					valid = false;
				}
				if (token == TerminalTokens.TokenNameWHITESPACE) {
					this.scanner.resetTo(restart, this.javadocEnd);
					this.index = restart;
					return pushParamName(isTypeParam);
				}
			}
			// Report problem
			this.currentTokenType = -1;
			if (isCompletionParser) return false;
			if (this.reportProblems) {
				// we only need end if we report problems
				end = hasMultiLines ? this.lineEnd: this.scanner.getCurrentTokenEndPosition();
				try {
					while ((token=readToken()) != TerminalTokens.TokenNameWHITESPACE && token != TerminalTokens.TokenNameEOF) {
						this.currentTokenType = -1;
						end = hasMultiLines ? this.lineEnd: this.scanner.getCurrentTokenEndPosition();
					}
				} catch (InvalidInputException e) {
					end = this.lineEnd;
				}
				if (mayBeGeneric && isTypeParam)
					this.sourceParser.problemReporter().javadocInvalidParamTypeParameter(start, end);
				else
					this.sourceParser.problemReporter().javadocInvalidParamTagName(start, end);
			}
			this.scanner.currentPosition = start;
			this.index = start;
			this.currentTokenType = -1;
			return false;
		} finally {
			// we have to make sure that this is reset to the previous value even if an exception occurs
			this.scanner.tokenizeWhiteSpace = tokenWhiteSpace;
		}
	}

	private boolean isTokenModule(int token, int moduleRefTokenCount) {
		return ((token == TerminalTokens.TokenNameDIVIDE)
				&& (moduleRefTokenCount > 0));
	}

	protected Object parseQualifiedName(boolean reset) throws InvalidInputException {
		return parseQualifiedName(reset, false);
	}

	/*
	 * Parse a qualified name and built a type reference if the syntax is valid.
	 */
	protected Object parseQualifiedName(boolean reset, boolean allowModule) throws InvalidInputException {

		// Reset identifier stack if requested
		if (reset) {
			this.identifierPtr = -1;
			this.identifierLengthPtr = -1;
		}

		// Scan tokens
		int primitiveToken = -1;
		int parserKind = this.kind & PARSER_KIND;
		int prevToken = TerminalTokens.TokenNameNotAToken;
		int curToken = TerminalTokens.TokenNameNotAToken;
		int moduleRefTokenCount = 0;
		boolean lookForModule = false;
		boolean parsingJava15Plus = this.scanner != null ? this.scanner.sourceLevel >= ClassFileConstants.JDK15 : false;
		boolean stop = false;
		nextToken : for (int iToken = 0; ; iToken++) {
			if (iToken == 0) {
				lookForModule = false;
				prevToken = TerminalTokens.TokenNameNotAToken;
			} else {
				prevToken = curToken;
			}
			if (stop) {
				if (parserKind != COMPLETION_PARSER) {
					break;
				}
			}
			int token = readTokenSafely();
			curToken= token;
			switch (token) {
				case TerminalTokens.TokenNameUNDERSCORE:
				case TerminalTokens.TokenNameIdentifier :
					if (((iToken & 1) != 0)) { // identifiers must be odd tokens
						break nextToken;
					}
					pushIdentifier(iToken == 0, false);
					consumeToken();
					if (allowModule && parsingJava15Plus && getChar() == '/') {
						lookForModule = true;
					}
					break;

				case TerminalTokens.TokenNameRestrictedIdentifierYield:
					throw Scanner.invalidInput(); // unexpected.

				case TerminalTokens.TokenNameDOT :
					if ((iToken & 1) == 0) { // dots must be even tokens
						throw Scanner.invalidInput();
					}
					consumeToken();
					break;

				case TerminalTokens.TokenNameabstract:
				case TerminalTokens.TokenNameassert:
				case TerminalTokens.TokenNameboolean:
				case TerminalTokens.TokenNamebreak:
				case TerminalTokens.TokenNamebyte:
				case TerminalTokens.TokenNamecase:
				case TerminalTokens.TokenNamecatch:
				case TerminalTokens.TokenNamechar:
				case TerminalTokens.TokenNameclass:
				case TerminalTokens.TokenNamecontinue:
				case TerminalTokens.TokenNamedefault:
				case TerminalTokens.TokenNamedo:
				case TerminalTokens.TokenNamedouble:
				case TerminalTokens.TokenNameelse:
				case TerminalTokens.TokenNameextends:
				case TerminalTokens.TokenNamefalse:
				case TerminalTokens.TokenNamefinal:
				case TerminalTokens.TokenNamefinally:
				case TerminalTokens.TokenNamefloat:
				case TerminalTokens.TokenNamefor:
				case TerminalTokens.TokenNameif:
				case TerminalTokens.TokenNameimplements:
				case TerminalTokens.TokenNameimport:
				case TerminalTokens.TokenNameinstanceof:
				case TerminalTokens.TokenNameint:
				case TerminalTokens.TokenNameinterface:
				case TerminalTokens.TokenNamelong:
				case TerminalTokens.TokenNamenative:
				case TerminalTokens.TokenNamenew:
				case TerminalTokens.TokenNamenon_sealed:
				case TerminalTokens.TokenNamenull:
				case TerminalTokens.TokenNamepackage:
				case TerminalTokens.TokenNameRestrictedIdentifierpermits:
				case TerminalTokens.TokenNameprivate:
				case TerminalTokens.TokenNameprotected:
				case TerminalTokens.TokenNamepublic:
				case TerminalTokens.TokenNameRestrictedIdentifiersealed:
				case TerminalTokens.TokenNameshort:
				case TerminalTokens.TokenNamestatic:
				case TerminalTokens.TokenNamestrictfp:
				case TerminalTokens.TokenNamesuper:
				case TerminalTokens.TokenNameswitch:
				case TerminalTokens.TokenNamesynchronized:
				case TerminalTokens.TokenNamethis:
				case TerminalTokens.TokenNamethrow:
				case TerminalTokens.TokenNametransient:
				case TerminalTokens.TokenNametrue:
				case TerminalTokens.TokenNametry:
				case TerminalTokens.TokenNamevoid:
				case TerminalTokens.TokenNamevolatile:
				case TerminalTokens.TokenNamewhile:
					if (iToken == 0) {
						pushIdentifier(true, true);
						primitiveToken = token;
						consumeToken();
						break nextToken;
					}
					// Fall through default case to verify that we do not leave on a dot
					//$FALL-THROUGH$
				case TerminalTokens.TokenNameDIVIDE:
					if (parsingJava15Plus && lookForModule) {
						if (((iToken & 1) == 0) || (moduleRefTokenCount > 0)) { // '/' must be even token
							throw Scanner.invalidInput();
						}
						moduleRefTokenCount = (iToken+1) / 2;
						consumeToken();
						lookForModule = false;
						if (!considerNextChar()) {
							stop = true;
						}
						break;
					} // else fall through
					// Note: Add other cases before this case.
					//$FALL-THROUGH$
				default :
					if (iToken == 0) {
						if (this.identifierPtr>=0) {
							this.lastIdentifierEndPosition = (int) this.identifierPositionStack[this.identifierPtr];
						}
						return null;
					}
					if ((iToken & 1) == 0 && !isTokenModule(prevToken, moduleRefTokenCount)) { // cannot leave on a dot
						switch (parserKind) {
							case COMPLETION_PARSER:
								if (this.identifierPtr>=0) {
									this.lastIdentifierEndPosition = (int) this.identifierPositionStack[this.identifierPtr];
								}
								if (moduleRefTokenCount > 0) {
									return syntaxRecoverModuleQualifiedName(primitiveToken, moduleRefTokenCount);
								}
								return syntaxRecoverQualifiedName(primitiveToken);
							case DOM_PARSER:
								if (this.currentTokenType != -1) {
									// Reset position: we want to rescan last token
									this.index = this.tokenPreviousPosition;
									this.scanner.currentPosition = this.tokenPreviousPosition;
									this.currentTokenType = -1;
								}
								// $FALL-THROUGH$ - fall through default case to raise exception
							default:
								throw Scanner.invalidInput();
						}
					}
					break nextToken;
			}
		}
		// Reset position: we want to rescan last token
		if (parserKind != COMPLETION_PARSER && this.currentTokenType != -1) {
			this.index = this.tokenPreviousPosition;
			this.scanner.currentPosition = this.tokenPreviousPosition;
			this.currentTokenType = -1;
		}
		if (this.identifierPtr>=0) {
			this.lastIdentifierEndPosition = (int) this.identifierPositionStack[this.identifierPtr];
		}
		if (moduleRefTokenCount > 0) {
			return createModuleTypeReference(primitiveToken, moduleRefTokenCount);
		}
		return createTypeReference(primitiveToken, (allowModule && parsingJava15Plus));
	}

	protected boolean parseReference() throws InvalidInputException {
		return parseReference(false);
	}

	/*
	 * Parse a reference in @see tag
	 */
	protected boolean parseReference(boolean allowModule) throws InvalidInputException {
		int currentPosition = this.scanner.currentPosition;
		boolean tokenWhiteSpace = this.scanner.tokenizeWhiteSpace;
		this.scanner.tokenizeWhiteSpace = false;
		try {
			Object typeRef = null;
			Object reference = null;
			int previousPosition = -1;
			int typeRefStartPosition = -1;

			// Get reference tokens
			nextToken : while (this.index < this.scanner.eofPosition) {
				previousPosition = this.index;
				int token = readTokenSafely();
				this.scanner.tokenizeWhiteSpace = true;
				switch (token) {
					case TerminalTokens.TokenNameStringLiteral : // @see "string"
						// If typeRef != null we may raise a warning here to let user know there's an unused reference...
						// Currently as javadoc 1.4.2 ignore it, we do the same (see bug 69302)
						if (typeRef != null) break nextToken;
						consumeToken();
						int start = this.scanner.getCurrentTokenStartPosition();
						if (this.tagValue == TAG_VALUE_VALUE) {
							// String reference are not allowed for @value tag
							if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidValueReference(start, getTokenEndPosition(), this.sourceParser.modifiers);
							return false;
						}

						// verify end line
						if (verifyEndLine(previousPosition)) {
							return createFakeReference(start);
						}
						if (this.reportProblems) this.sourceParser.problemReporter().javadocUnexpectedText(this.scanner.currentPosition, this.lineEnd);
						return false;
					case TerminalTokens.TokenNameLESS : // @see <a href="URL#Value">label</a>
						// If typeRef != null we may raise a warning here to let user know there's an unused reference...
						// Currently as javadoc 1.4.2 ignore it, we do the same (see bug 69302)
						if (typeRef != null) break nextToken;
						consumeToken();
						start = this.scanner.getCurrentTokenStartPosition();
						if (parseHref()) {
							consumeToken();
							if (this.tagValue == TAG_VALUE_VALUE) {
								// String reference are not allowed for @value tag
								if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidValueReference(start, getIndexPosition(), this.sourceParser.modifiers);
								return false;
							}
							// verify end line
							if (verifyEndLine(previousPosition)) {
								return createFakeReference(start);
							}
							if (this.reportProblems) this.sourceParser.problemReporter().javadocUnexpectedText(this.scanner.currentPosition, this.lineEnd);
						}
						else if (this.tagValue == TAG_VALUE_VALUE) {
							if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidValueReference(start, getIndexPosition(), this.sourceParser.modifiers);
						}
						return false;
					case TerminalTokens.TokenNameERROR :
						consumeToken();
						if (this.scanner.currentCharacter == '#') { // @see ...#member
							reference = parseMember(typeRef);
							if (reference != null) {
								return pushSeeRef(reference);
							}
							return false;
						}
						char[] currentError = this.scanner.getCurrentIdentifierSource();
						if (currentError.length>0 && currentError[0] == '"') {
							if (this.reportProblems) {
								boolean isUrlRef = false;
								if (this.tagValue == TAG_SEE_VALUE) {
									int length=currentError.length, i=1 /* first char is " */;
									while (i<length && ScannerHelper.isLetter(currentError[i])) {
										i++;
									}
									if (i<(length-2) && currentError[i] == ':' && currentError[i+1] == '/' && currentError[i+2] == '/') {
										isUrlRef = true;
									}
								}
								if (isUrlRef) {
									// https://bugs.eclipse.org/bugs/show_bug.cgi?id=207765
									// handle invalid URL references in javadoc with dedicated message
									this.sourceParser.problemReporter().javadocInvalidSeeUrlReference(this.scanner.getCurrentTokenStartPosition(), getTokenEndPosition());
								} else {
									this.sourceParser.problemReporter().javadocInvalidReference(this.scanner.getCurrentTokenStartPosition(), getTokenEndPosition());
								}
							}
							return false;
						}
						break nextToken;
					case TerminalTokens.TokenNameUNDERSCORE:
					case TerminalTokens.TokenNameIdentifier :
						if (typeRef == null) {
							typeRefStartPosition = this.scanner.getCurrentTokenStartPosition();
							typeRef = parseQualifiedName(true, allowModule);
							if (this.abort) return false; // May be aborted by specialized parser
							break;
						}
						break nextToken;
					default :
						break nextToken;
				}
			}

			// Verify that we got a reference
			if (reference == null) reference = typeRef;
			if (reference == null) {
				this.index = this.tokenPreviousPosition;
				this.scanner.currentPosition = this.tokenPreviousPosition;
				this.currentTokenType = -1;
				if (this.tagValue == TAG_VALUE_VALUE) {
					if ((this.kind & DOM_PARSER) != 0) createTag();
					return true;
				}
				if (this.reportProblems) {
					this.sourceParser.problemReporter().javadocMissingReference(this.tagSourceStart, this.tagSourceEnd, this.sourceParser.modifiers);
				}
				return false;
			}

			// Reset position at the end of type reference
			if (this.lastIdentifierEndPosition > this.javadocStart) {
				this.index = this.lastIdentifierEndPosition+1;
				this.scanner.currentPosition = this.index;
			}
			this.currentTokenType = -1;

			// In case of @value, we have an invalid reference (only static field refs are valid for this tag)
			if (this.tagValue == TAG_VALUE_VALUE) {
				if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidReference(typeRefStartPosition, this.lineEnd);
				return false;
			}

			int currentIndex = this.index; // store current index
			char ch = readChar();
			switch (ch) {
				// Verify that line end does not start with an open parenthese (which could be a constructor reference wrongly written...)
				// See bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=47215
				case '(' :
					if (this.reportProblems) this.sourceParser.problemReporter().javadocMissingHashCharacter(typeRefStartPosition, this.lineEnd, String.valueOf(this.source, typeRefStartPosition, this.lineEnd-typeRefStartPosition+1));
					return false;
				// Search for the :// URL pattern
				// See bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=168849
				case ':' :
					ch = readChar();
					if (ch == '/' && ch == readChar()) {
						if (this.reportProblems) {
							this.sourceParser.problemReporter().javadocInvalidSeeUrlReference(typeRefStartPosition, this.lineEnd);
							return false;
						}
					}
			}
			// revert to last stored index
			this.index = currentIndex;

			// Verify that we get white space after reference
			if (!verifySpaceOrEndComment()) {
				this.index = this.tokenPreviousPosition;
				this.scanner.currentPosition = this.tokenPreviousPosition;
				this.currentTokenType = -1;
				int end = this.starPosition == -1 ? this.lineEnd : this.starPosition;
				if (this.source[end]=='\n') end--;
				if (this.reportProblems) this.sourceParser.problemReporter().javadocMalformedSeeReference(typeRefStartPosition, end);
				return false;
			}

			// Everything is OK, store reference
			return pushSeeRef(reference);
		}
		catch (InvalidInputException ex) {
			if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidReference(currentPosition, getTokenEndPosition());
		}
		finally {
			// we have to make sure that this is reset to the previous value even if an exception occurs
			this.scanner.tokenizeWhiteSpace = tokenWhiteSpace;
		}
		// Reset position to avoid missing tokens when new line was encountered
		this.index = this.tokenPreviousPosition;
		this.scanner.currentPosition = this.tokenPreviousPosition;
		this.currentTokenType = -1;
		return false;
	}

	protected boolean parseSnippet() throws InvalidInputException {
		boolean tokenWhiteSpace = this.scanner.tokenizeWhiteSpace;
		boolean tokenizeComments = this.scanner.tokenizeComments;
		this.scanner.tokenizeWhiteSpace = true;
		this.scanner.tokenizeComments = true;
		int previousPosition = -1;
		int lastRBracePosition = -1;
		int openBraces = 1;
		boolean parsingJava18Plus = this.scanner != null ? this.scanner.sourceLevel >= ClassFileConstants.JDK18 : false;
		boolean valid = true;
		if (!parsingJava18Plus) {
			throw Scanner.invalidInput();
		}
		Object snippetTag = null;
		this.nonRegionTagCount = 0;
		this.inlineTagCount = 0;
		try {
			snippetTag = createSnippetTag();
			Map<String, String> snippetAttributes  = new HashMap();
			if (!parseTillColon(snippetAttributes)) {
				int token = readTokenSafely();
				boolean eitherNameorClass = token == TerminalTokens.TokenNameIdentifier || token == TerminalTokens.TokenNameclass || token == TerminalTokens.TokenNameUNDERSCORE;
				if (!eitherNameorClass ) {
					this.setSnippetError(snippetTag, "Missing colon"); //$NON-NLS-1$
					this.setSnippetIsValid(snippetTag, false);
					if(this.reportProblems)
						this.sourceParser.problemReporter().javadocInvalidSnippetMissingColon(this.index, this.lineEnd);
					valid = false;
				} else {
					final String FILE = "file"; //$NON-NLS-1$
					final String CLASS = "class"; //$NON-NLS-1$
					consumeToken();
					valid = false;
					String snippetType = this.scanner.getCurrentTokenString();
					switch (snippetType) {
						case FILE:
							consumeToken();
							int start = this.scanner.getCurrentTokenStartPosition();
							token = readTokenSafely();
							if (token==TerminalTokens.TokenNameEQUAL) {
								consumeToken();
								token = readTokenSafely();
								String regionName = null;
								if (token==TerminalTokens.TokenNameERROR||token==TerminalTokens.TokenNameStringLiteral){
									String fileName = this.scanner.getCurrentTokenString();
									int lastIndex = fileName.length() - 1;
									if ((fileName.charAt(0) =='"' && fileName.charAt(lastIndex)=='"')
											||(fileName.charAt(0) =='\'' && fileName.charAt(lastIndex)=='\'')) {
										fileName = fileName.substring(1, lastIndex); // strip out quotes
										Path filePath = getFilePathFromFileName(fileName);
										try {
											valid = filePath==null ? false : readFileWithRegions(start, regionName, filePath, snippetTag);
										} catch (IOException e) {
											valid = false;
											this.setSnippetError(snippetTag, "Error in reading file"); //$NON-NLS-1$
										}
									}
								}
							}
							if (snippetTag != null) {
								this.setSnippetIsValid(snippetTag, valid);
							}
							break;
						case CLASS:
							consumeToken();
							start = this.scanner.getCurrentTokenStartPosition();
							token = readTokenSafely();
							if (token==TerminalTokens.TokenNameEQUAL) {
								consumeToken();
								token = readTokenSafely();
								String regionName = null;
								if (token==TerminalTokens.TokenNameERROR||token==TerminalTokens.TokenNameStringLiteral){
									String className = this.scanner.getCurrentTokenString();
									int lastIndex = className.length() - 1;
									if ((className.charAt(0) =='"' && className.charAt(lastIndex)=='"')
											||(className.charAt(0) =='\'' && className.charAt(lastIndex)=='\'')) {
										className = className.substring(1, lastIndex); // strip out quotes
										if(className.contains(".")) { //$NON-NLS-1$
											className = className.replace('.', '/');
										}
										String fileName = className+".java";//$NON-NLS-1$
										Path filePath = getFilePathFromFileName(fileName);

										try {
											valid = filePath==null ? false : readFileWithRegions(start, regionName, filePath, snippetTag);
										} catch (IOException e) {
											valid = false;
											this.setSnippetError(snippetTag, "Error in reading class"); //$NON-NLS-1$
										}
									}
								}
							}
							if (snippetTag != null) {
								this.setSnippetIsValid(snippetTag, valid);
							}
							break;
						default:
							valid = false;
					}
				}
			} else {
				if (this.index < this.scanner.eofPosition) {
					int token = readTokenSafely();
					if (token == TerminalTokens.TokenNameWHITESPACE) {
						if (containsNewLine(this.scanner.getCurrentTokenString())) {
							consumeToken();
						} else {
							valid = false;
							if(this.reportProblems) {
								//after colon new line required
								this.sourceParser.problemReporter().javadocInvalidSnippetContentNewLine(this.index, this.lineEnd);
							}
							this.setSnippetIsValid(snippetTag, false);
							this.setSnippetError(snippetTag, "Snippet content should be in a new line"); //$NON-NLS-1$

						}
					}
				} else {
					//when will this happen?? never?
					valid = false;
				}
			}
			if(hasID(snippetAttributes)) {
				this.setSnippetID(snippetTag, getID(snippetAttributes));
			}
			int textEndPosition = this.index;
			this.textStart = this.index;
			int token;
			while (this.index < this.scanner.eofPosition) {
				this.index = this.scanner.currentPosition;
				if (openBraces == 0) {
					break;
				}
				previousPosition = this.index;
				token = readTokenSafely();
				if (token == TerminalTokens.TokenNameEOF) {
					break;
				}
				switch (token) {
					case TerminalTokens.TokenNameLBRACE:
						openBraces++;
						textEndPosition = this.index;
						break;
					case TerminalTokens.TokenNameRBRACE:
						openBraces--;
						textEndPosition = this.index;
						lastRBracePosition = this.scanner.currentPosition;
						if (openBraces == 0) {
							if (this.lineStarted) {
								if (this.textStart == -1) {
									this.textStart = previousPosition;
								}
								if (this.textStart != -1 && this.textStart < this.index) {
									String textToBeAdded= new String( this.source, this.textStart, this.index-this.textStart);
									int iindex = textToBeAdded.indexOf('*');
									if (iindex > -1 && textToBeAdded.substring(0, iindex+1).trim().equals("*")) { //$NON-NLS-1$
										textToBeAdded = textToBeAdded.substring(iindex+1);
									}
									if (!textToBeAdded.isBlank()) {
										pushSnippetText(this.source, this.textStart, this.index-1, false, snippetTag);
										this.nonRegionTagCount = 0;
										this.inlineTagCount = 0;
									}
								}
							}
						}
						break;
					case TerminalTokens.TokenNameWHITESPACE:
						if (containsNewLine(this.scanner.getCurrentTokenString())) {
							if (this.lineStarted) {
								if (this.textStart != -1 && this.textStart < textEndPosition) {
									if (isProperties(snippetAttributes)) { //single quotes
										String str = new String(this.source, this.textStart,
												textEndPosition - this.textStart);
										if (str.length() > 0 && (str.charAt(0) == '*' ||  str.charAt(0) == '#' ))  {
											if(str.charAt(0) == '*' )
												str = str.substring(1);
											str = str.stripLeading().stripTrailing();
											if (str.length() > 0 && str.charAt(0) == '#'
													&& str.charAt(str.length() - 1) == ':') {
												str = SINGLE_LINE_COMMENT + str.substring(1, str.length() - 1);
												Object innerTag = parseSnippetInlineTags(str, snippetTag, this.scanner);
												if (innerTag != null) {
													addSnippetInnerTag(innerTag, snippetTag);
													this.snippetInlineTagStarted = true;
													this.lineStarted = false;
													this.textStart = -1;
													break;
												}
											}
										}
									}
									pushSnippetText(this.source, this.textStart, textEndPosition, true, snippetTag);
									this.nonRegionTagCount = 0;
									this.inlineTagCount = 0;
								}
							}
							this.lineStarted = false;
							// Fix bug 51650
							this.textStart = -1;
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE:
						String tokenString = this.scanner.getCurrentTokenString();
						boolean handleNow = handleCommentLineForCurrentLine(tokenString);
						boolean lvalid = false;
						int indexOfLastComment = -1;
						int noSingleLineComm = getNumberOfSingleLineCommentInSnippetTag(tokenString.substring(2));
						if (noSingleLineComm > 0)
							indexOfLastComment = indexOfLastSingleComment(tokenString.substring(2),noSingleLineComm);
						if (!handleNow) {
							this.nonRegionTagCount = 0;
							this.inlineTagCount = 0;
						}
						Object innerTag = parseSnippetInlineTags(indexOfLastComment == -1 ? tokenString : tokenString.substring(indexOfLastComment+2), snippetTag, this.scanner);
						if (innerTag != null) {
							lvalid = true;
						}
						if( lvalid && handleNow && innerTag != snippetTag) {
							if ( innerTag != snippetTag )
								addSnippetInnerTag(innerTag, snippetTag);
							this.snippetInlineTagStarted = true;
						}
						textEndPosition = this.index;
						int textPos = previousPosition;
						if (!lvalid) {
							textPos = textEndPosition;
						}
						if (this.lineStarted) {
							if (this.textStart == -1) {
								this.textStart = previousPosition;
							}
							if (this.textStart != -1 && this.textStart < this.index) {
								pushSnippetText(this.source, this.textStart,(innerTag!=null &&  indexOfLastComment >=0) ? textPos+indexOfLastComment+2:textPos, lvalid, snippetTag);
								if (handleNow) {
									this.nonRegionTagCount = 0;
									this.inlineTagCount = 0;
								}
							}
						}
						if (lvalid && !handleNow) {
							if ( innerTag != snippetTag )
								addSnippetInnerTag(innerTag, snippetTag);
							this.snippetInlineTagStarted = true;
						}
						//valid = valid & lvalid;
						break;
					default:
						if (!this.lineStarted || this.textStart == -1) {
							this.textStart = previousPosition;
						}
						this.lineStarted = true;
						textEndPosition = this.index;
						break;
				}
				consumeToken();
			}
		}
		finally {
			if(!areRegionsClosed()) {
				if(this.reportProblems) {
					this.sourceParser.problemReporter().javadocInvalidSnippetRegionNotClosed(this.index, this.lineEnd);
				}
				this.setSnippetError(snippetTag, "Region not closed"); //$NON-NLS-1$
				this.setSnippetIsValid(snippetTag, false);
			}
			// we have to make sure that this is reset to the previous value even if an exception occurs
			this.scanner.tokenizeWhiteSpace = tokenWhiteSpace;
			this.scanner.tokenizeComments = tokenizeComments;
		}
		boolean retVal = false;
		if (!valid) {
			retVal =  false;
		} else if (openBraces == 0) {
			this.scanner.currentPosition = lastRBracePosition-1;
			this.index = lastRBracePosition-1;
			retVal = true;
		}
		if (retVal == false && openBraces == 0) {
			this.scanner.currentPosition = lastRBracePosition - 1;
			this.index = lastRBracePosition - 1;
		}
		if (snippetTag != null) {
			this.setSnippetIsValid(snippetTag, retVal);
		}
		return retVal;
	}

	private Path getFilePathFromFileName(String fileName) {
		if(this.projectPath == null)
			return null;
		ArrayList<String> sourceClassPaths = (ArrayList<String>) this.srcClasspath;
		Path filePath = null;
		for (String iPath : sourceClassPaths) {
			filePath = Path.of(this.projectPath, iPath, fileName);
			if(filePath.toFile().exists())
				break;
		}
		return filePath;
	}

	private boolean readFileWithRegions(int start, String regionName, Path filePath, Object snippetTag) throws IOException {
		boolean valid = false;
		int token;
		int lastIndex;
		String contents = Files.readString(filePath);
		int end = this.scanner.getCurrentTokenEndPosition();
		consumeToken();
		boolean foundRegionDef = false;
		final String REGION = "region"; //$NON-NLS-1$
		while (this.index<this.scanner.eofPosition) {
			token = readTokenSafely();
			if (token == TerminalTokens.TokenNameRBRACE) {
				end = this.index;
				valid = true;
				break;
			} else if (token == TerminalTokens.TokenNameIdentifier || token == TerminalTokens.TokenNameUNDERSCORE) {
				consumeToken();
				if (this.scanner.getCurrentTokenString().equals(REGION)) {
					foundRegionDef = true;
					break;
				}
			} else {
				consumeToken();
			}
		}

		if (foundRegionDef) {
			token = readTokenSafely();
			if (token!= TerminalTokens.TokenNameEQUAL) {
				valid = false;
			}
			consumeToken();
			token = readTokenSafely();
			if (token==TerminalTokens.TokenNameERROR
					|| token==TerminalTokens.TokenNameStringLiteral
					|| token==TerminalTokens.TokenNameIdentifier
					|| token==TerminalTokens.TokenNameUNDERSCORE){
				regionName = this.scanner.getCurrentTokenString();
				consumeToken();
				lastIndex = regionName.length() - 1;
				if ((regionName.charAt(0) =='"' && regionName.charAt(lastIndex)=='"')
						||(regionName.charAt(0) =='\'' && regionName.charAt(lastIndex)=='\'')) {
					regionName = regionName.substring(1, lastIndex); // strip out quotes
					end = this.scanner.getCurrentTokenEndPosition();
				}
				while (this.index<this.scanner.eofPosition) {
					token = readTokenSafely();
					if (token == TerminalTokens.TokenNameRBRACE) {
						end = this.index;
						valid = true;
						break;
					} else {
						consumeToken();
					}
				}
			}
		}

		if (valid) {
			String snippetText = extractExternalSnippet(contents, regionName);
			//pushExternalSnippetText(snippetText, start, end);
			parseExternalSnippet(snippetText, snippetTag);
			this.index = end;
			this.scanner.currentPosition = end;
		}
		return valid;
	}

	private void parseExternalSnippet(String content,  Object snippetTag) {
		Scanner snippetScanner = new Scanner(true, true, false, this.scanner.sourceLevel, this.scanner.complianceLevel,
				null, null, false, false);
		snippetScanner.setSource(content.toCharArray());
		int indexPos = 0;
		int textStartPosition = indexPos;
		int textEndPosition = indexPos;
		int previousPosition = indexPos;
		boolean resetTextStartPos = true;
		boolean newLineStarted = false;
		try {
			while (true) {
				int tokenType;
				previousPosition = indexPos;
				tokenType = snippetScanner.getNextToken();

				if (tokenType == TokenNameEOF) {
					if (!resetTextStartPos) {
						pushExternalSnippetText(snippetScanner.source, textStartPosition, textEndPosition, false, snippetTag);
					}
					break;
				}

				indexPos = snippetScanner.currentPosition;
				textEndPosition = indexPos;
				if (resetTextStartPos) {
					textStartPosition = snippetScanner.getCurrentTokenStartPosition();
					newLineStarted = true;
					resetTextStartPos = false;
				}
				switch (tokenType) {
					case TerminalTokens.TokenNameWHITESPACE:
						if (containsNewLine(snippetScanner.getCurrentTokenString())) {
							pushExternalSnippetText(snippetScanner.source, textStartPosition, textEndPosition, false, snippetTag);
							resetTextStartPos = true;
							newLineStarted = false;
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE:
						String tokenString = snippetScanner.getCurrentTokenString();
						boolean handleNow = handleCommentLineForCurrentLine(tokenString);
						boolean lvalid = false;
						int indexOfLastComment = -1;
						int noSingleLineComm = getNumberOfSingleLineCommentInSnippetTag(tokenString.substring(2));
						if (noSingleLineComm > 0)
							indexOfLastComment = indexOfLastSingleComment(tokenString.substring(2),noSingleLineComm);
						if (!handleNow) {
							this.nonRegionTagCount = 0;
							this.inlineTagCount = 0;
						}
						Object innerTag = parseSnippetInlineTags(indexOfLastComment == -1 ? tokenString : tokenString.substring(indexOfLastComment+2), snippetTag, snippetScanner);
						if (innerTag != null) {
							lvalid = true;
						}
						if( lvalid && handleNow && innerTag != snippetTag) {
							if ( innerTag != snippetTag )
								addSnippetInnerTag(innerTag, snippetTag);
							this.snippetInlineTagStarted = true;
						}
						textEndPosition = this.index;
						int textPos = previousPosition;
						if (!lvalid) {
							textPos = textEndPosition;
						}
						if (newLineStarted) {
							if (textStartPosition == -1) {
								textStartPosition = previousPosition;
							}
							if (textStartPosition != -1 && textStartPosition < indexPos) {
								pushExternalSnippetText(snippetScanner.source, textStartPosition,(innerTag!=null &&  indexOfLastComment >=0) ? textPos+indexOfLastComment+2:textPos, true, snippetTag);
								resetTextStartPos = true;
								newLineStarted = false;
								if (handleNow) {
									this.nonRegionTagCount = 0;
									this.inlineTagCount = 0;
								}
							}
						}
						if (lvalid && !handleNow) {
							if ( innerTag != snippetTag )
								addSnippetInnerTag(innerTag, snippetTag);
							this.snippetInlineTagStarted = true;
						}
						//valid = valid & lvalid;
						break;
					default:
						textEndPosition = indexPos;
						break;
				}
			}
		} catch (InvalidInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	private String extractExternalSnippet(String contents, String region) {
		String snippetString = ""; //$NON-NLS-1$
		final String START  = "start"; //$NON-NLS-1$
		final String END    = "end"; //$NON-NLS-1$
		final String REGION = "region"; //$NON-NLS-1$
		final String HIGHLIGHT = "highlight"; //$NON-NLS-1$
		final String REPLACE = "replace"; //$NON-NLS-1$
		final String LINK = "link"; //$NON-NLS-1$
		boolean insideRegion = false;
		boolean regionStarted = false;
		boolean containsRegionStartSnippetTags = false;
		Scanner snippetScanner = new Scanner(true, true, false, this.scanner.sourceLevel, this.scanner.complianceLevel,
				null, null, false, false);
		snippetScanner.setSource(contents.toCharArray());
		Deque<String> stack = new ArrayDeque<>();
		if (region == null) {
			return contents;
		}
		int count = 0;
		while (true) {
			int tokenType = 0;
			try {
				tokenType = snippetScanner.getNextToken();
				if (!insideRegion && regionStarted) {
					break;
				}
				if (tokenType == TokenNameEOF)
					break;
				if (tokenType == TerminalTokens.TokenNameCOMMENT_LINE) {
					String commentLine = snippetScanner.getCurrentTokenString();
					int noSingleLineComm = getNumberOfSingleLineCommentInSnippetTag(commentLine.substring(2));
					int indexOfLastComment = 0;
					String commentStr = commentLine;
					if (noSingleLineComm > 0) {
						indexOfLastComment = indexOfLastSingleComment(commentLine.substring(2),noSingleLineComm);
						commentStr = commentLine.substring(indexOfLastComment+2);
					}
					Scanner commentScanner = new JavadocScanner(false, false, false/* nls */, this.scanner.sourceLevel, this.scanner.complianceLevel,
							null/* taskTags */, null/* taskPriorities */, false/* taskCaseSensitive */, false, true, true);

					if (commentStr.startsWith("//")) { //$NON-NLS-1$
						commentStr = commentStr.substring(2);
					}
					commentScanner.setSource(commentStr.toCharArray());
					boolean atTokenStarted = false;
					boolean insideValid = false;
					boolean isRegion = false;
					boolean getRegionValue = false;
					String attribute = null;
					while (true) {
						int cType = commentScanner.getNextToken();
						if (cType == TokenNameEOF) {
							break;
						}
						switch (cType) {
							case TerminalTokens.TokenNameAT :
								atTokenStarted = true;
								insideValid = false;
								isRegion = false;
								getRegionValue = false;
								attribute = null;
								break;
							case TerminalTokens.TokenNameUNDERSCORE:
							case TerminalTokens.TokenNameIdentifier :
								if (atTokenStarted) {
									String tokenStr = commentScanner.getCurrentTokenString();
									insideValid = false;
									isRegion = false;
									getRegionValue = false;
									switch (tokenStr) {
										case START:
											insideValid = true;
											attribute = tokenStr;
											break;
										case HIGHLIGHT:
										case REPLACE:
										case LINK:
										case END:
											if (insideRegion) {
												insideValid = true;
												attribute = tokenStr;
											} else {
												insideValid = false;
											}
											break;
										default:
											insideValid = false;
											break;
									}
								} else if (insideValid) {
									String tokenStr = commentScanner.getCurrentTokenString();
									switch (tokenStr) {
										case REGION:
											isRegion = true;
											break;
										default:
											if (getRegionValue) {
												String regionStr = commentScanner.getCurrentTokenString();
												regionStr = stripQuotes(regionStr);
												if (START.equals(attribute) &&  regionStr.equals(region)) {
													insideRegion = true;
													regionStarted = true;
												}
												if (END.equals(attribute)) {
													if (regionStr.equals(region)) {
														insideRegion = false;
													}
													stack.removeFirst();
												}
												if (!END.equals(attribute) && insideRegion) {
													stack.addFirst(attribute);
												}
												attribute = null;
												getRegionValue = false;
											}
											isRegion = false;
											break;
									}
								}
								atTokenStarted = false;
								break;
							case TerminalTokens.TokenNameEQUAL:
								if (isRegion) {
									getRegionValue = true;
								}
								break;
							case TerminalTokens.TokenNameStringLiteral:
							case TerminalTokens.TokenNameSingleQuoteStringLiteral:
								if (getRegionValue) {
									String regionStr = commentScanner.getCurrentTokenString();
									regionStr = stripQuotes(regionStr);
									if (START.equals(attribute) &&  regionStr.equals(region)) {
										insideRegion = true;
										regionStarted = true;
									}
									if (END.equals(attribute)) {
										if (regionStr.equals(region)) {
											insideRegion = false;
										}
										stack.removeFirst();
									}
									if (!END.equals(attribute) && insideRegion) {
										stack.addFirst(attribute);
									}
									attribute = null;
									getRegionValue = false;
								}
								break;
							default :
								atTokenStarted = false;
								isRegion = false;
								break;

						}
					}
					if (END.equals(attribute) && insideRegion) {
						stack.removeFirst();
						if (stack.size() == 0) {
							insideRegion = false;
						}
					}
					if (insideRegion) {
						if (commentStr.stripLeading().startsWith("@start") && count++ == 0) { //$NON-NLS-1$
							containsRegionStartSnippetTags = true;
							snippetString = snippetString + commentLine.substring(0, indexOfLastComment) + System.lineSeparator();
						}
					}
				}
				if (insideRegion && !containsRegionStartSnippetTags) {
					snippetString = snippetString + snippetScanner.getCurrentTokenString();
				}
				if (containsRegionStartSnippetTags) {
					containsRegionStartSnippetTags = false;
				}
			} catch (InvalidInputException e) {
				e.printStackTrace();
			}

		}
		return snippetString;
	}

	private boolean isProperties(Map<String, String> snippetAttributes) {
		if (snippetAttributes.size()==0)
			return false;
		for (Map.Entry<String, String> entry : snippetAttributes.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    if(key.equals("lang") && value.equals("properties")) { //$NON-NLS-1$ //$NON-NLS-2$
		    	return true;
		    }
		}
		return false;
	}

	private boolean hasID(Map<String, String> snippetAttributes) {
		if (snippetAttributes.size()==0)
			return false;
		for (String key: snippetAttributes.keySet()) {
			   if(key.equals("id")) { //$NON-NLS-1$
			    	return true;
			    }
		}
		return false;
	}
	private String getID(Map<String, String> snippetAttributes) {
		for (Map.Entry<String, String> entry : snippetAttributes.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    if(key.equals("id") ) { //$NON-NLS-1$
		    	return value;
		    }
		}
		return ""; //$NON-NLS-1$
	}

	private boolean parseTillColon(Map<String, String> snippetAttributes) {
		boolean isValid =  true;
		boolean colonTokenFound = false;
		int token;
		String key = null;
		boolean lookForValue = false;
		while (this.index < this.scanner.eofPosition) {
			token = readTokenSafely();
			switch(token) {
				case TerminalTokens.TokenNameWHITESPACE :
					if (containsNewLine(this.scanner.getCurrentTokenString())) {
						consumeToken();
						if (this.index < this.scanner.eofPosition) {
							token = readTokenSafely();
							if (token == TerminalTokens.TokenNameMULTIPLY) {
								consumeToken();
							} else {
								isValid = false;
							}
						} else {
							isValid = false;
						}
					} else {
						consumeToken();
					}
					break;
				case TerminalTokens.TokenNameCOLON :
					consumeToken();
					colonTokenFound = true;
					break;
				case TerminalTokens.TokenNameclass:
					if(lookForValue == false) {
						isValid = false;
					}
					break;

				case TerminalTokens.TokenNameUNDERSCORE:
				case TerminalTokens.TokenNameStringLiteral:
				case TerminalTokens.TokenNameIdentifier: // name and equal can come for attribute
					String isFile = this.scanner.getCurrentTokenString();
					if(isFile.equals("file") && lookForValue == false) { //$NON-NLS-1$
						isValid = false;
						break;
					}
					consumeToken();
					if (key == null)
						key = this.scanner.getCurrentTokenString();
					if (lookForValue && key != null) {
						String value = this.scanner.getCurrentTokenString();
						snippetAttributes.put(key,
								token == TerminalTokens.TokenNameStringLiteral ? value.substring(1, value.length() - 1)
										: value);
						lookForValue = false;
						key = null;
					}

				 	break;
				case TerminalTokens.TokenNameEQUAL :
					consumeToken();
					lookForValue=true;
					break;
				case TerminalTokens.TokenNameERROR:
					String currentTokenString = this.scanner.getCurrentTokenString();
					if(currentTokenString.length()> 1 && currentTokenString.charAt(0) =='\'' && currentTokenString.charAt(currentTokenString.length()-1) =='\'') {
						if (lookForValue && key != null) {
							String value = this.scanner.getCurrentTokenString();
							snippetAttributes.put(key, value.substring(1, value.length() - 1));
							lookForValue = false;
							key = null;
							break;
						}
					}
					if (this.scanner.currentCharacter == '"') {
						if (!lookForValue)
							isValid = false;
					}
					consumeToken();

					break;

				default :
					isValid = false;
					break;
			}
			if (colonTokenFound || !isValid) {
				break;
			}
		}
		if (colonTokenFound) {
			isValid = true;
		}
		return isValid;
	}


	public int indexOfLastSingleComment(String tokenString, int last) {
		int indexOfLastCom = 0;
		int temp = -1;
		String tempString = tokenString;
		for (int i = 0; i < last; ++i) {
			temp = tempString.indexOf(SINGLE_LINE_COMMENT);
			if (temp == -1) {
				indexOfLastCom = 0;
				break;
			}
			tempString = tempString.substring(++temp);
			indexOfLastCom += temp;
		}
		return --indexOfLastCom;
	}


	private boolean handleCommentLineForCurrentLine(String tokenString) {
		boolean handle = true;
		if (tokenString != null) {
			String processed= tokenString.trim();
			if (processed.endsWith(":")) { //$NON-NLS-1$
				handle = false;
			}
		}
		return handle;
	}


	protected int getNumberOfSingleLineCommentInSnippetTag(String tokenString) {
		if (tokenString != null) {
			String tokenStringStripped = tokenString.stripLeading();
			Scanner slScanner = new JavadocScanner(true, true, false/* nls */, this.scanner.sourceLevel,
					this.scanner.complianceLevel, null/* taskTags */, null/* taskPriorities */,
					false/* taskCaseSensitive */, false, true, true);
			slScanner.setSource(tokenStringStripped.toCharArray());
			while (true) {
				try {
					int tokenType = slScanner.getNextToken();
					if (tokenType == TokenNameEOF)
						break;
					switch (tokenType) {
						case TerminalTokens.TokenNameCOMMENT_LINE:
							return 1 + getNumberOfSingleLineCommentInSnippetTag(tokenStringStripped
									.substring(2 + tokenStringStripped.indexOf(SINGLE_LINE_COMMENT)));
					}
				} catch (InvalidInputException e) {
					// do nothing
				}
			}
		}
		return 0;
	}

	protected Object parseSnippetInlineTags(String tokenString, Object snippetTag, Scanner sScanner) {
		int commentStart = sScanner.getCurrentTokenStartPosition();
		Object inlineTag = null;
		final String REPLACE = "replace"; //$NON-NLS-1$
		final String HIGHLIGHT = "highlight"; //$NON-NLS-1$
		final String SUBSTRING = "substring"; //$NON-NLS-1$
		final String REGEX = "regex"; //$NON-NLS-1$
		final String TYPE = "type"; //$NON-NLS-1$
		final String REPLACEMENT = "replacement"; //$NON-NLS-1$
		final String REGION = "region"; //$NON-NLS-1$
		final String END = "end"; //$NON-NLS-1$
		final String LINK = "link"; //$NON-NLS-1$
		final String TARGET = "target"; //$NON-NLS-1$
		boolean regionClosed = false;
		int initialTagCount = this.nonRegionTagCount;
		List<Object> inlineTags= new ArrayList<>();
		boolean ignoreLink = false;
		if (sScanner != this.scanner) {
			ignoreLink = true;
		}
		try {
			if (tokenString != null
					&& tokenString.length() > 2
					&& tokenString.startsWith(SINGLE_LINE_COMMENT)) {
				String tobeTokenized = tokenString.substring(2);
				Scanner slScanner = new JavadocScanner(false, false, false/* nls */, sScanner.sourceLevel, sScanner.complianceLevel,
						null/* taskTags */, null/* taskPriorities */, false/* taskCaseSensitive */, false, true, true);
				slScanner.setSource(tobeTokenized.toCharArray());
				boolean atTokenStarted= false;
				int atTokenPos = -1;
				boolean firstTagProcessed = false;
				while (true) {
					try {
						int tokenType = slScanner.getNextToken();
						if (tokenType == TokenNameEOF)
							break;
						mainSwitch : switch (tokenType) {
							case TerminalTokens.TokenNameAT :
								atTokenStarted = true;
								atTokenPos = slScanner.getCurrentTokenStartPosition();
								break;
							case TerminalTokens.TokenNameUNDERSCORE:
							case TerminalTokens.TokenNameIdentifier :
								if(atTokenStarted==false) //invalid snippet inline, treat it like text
									return null;
								if (atTokenStarted) {
									int curPos= slScanner.getCurrentTokenStartPosition();
									if (curPos != atTokenPos+1 && !firstTagProcessed) {
										return inlineTag;
									}
									String snippetDecorator = slScanner.getCurrentTokenString();
									int tokenStart= commentStart + slScanner.getCurrentTokenStartPosition()-1;
									int tokenEnd= commentStart + slScanner.getCurrentTokenEndPosition();
									String newTagName= null;
									switch(snippetDecorator) {
										case  HIGHLIGHT :
											tokenStart= commentStart + 1 + slScanner.getCurrentTokenStartPosition();
											tokenEnd= tokenStart + 10;
											newTagName = '@' + HIGHLIGHT;
											Map<String, Object> map = new HashMap<>();
											boolean breakToMainSwitch = false;
											boolean createTag = false;
											String attribute = null;
											String value = null;
											boolean processValue = false;
											boolean createRegion = false;
											String regionName = null;
											while (true) {
												tokenType = slScanner.getNextToken();
												switch (tokenType) {
													case TokenNameEOF:
														createTag = true;
														break;
													case TerminalTokens.TokenNameAT:
														if (!processValue) {
															breakToMainSwitch = true;
															createTag = true;
														}
														processValue= false;
														break;
													case TerminalTokens.TokenNameCOLON:
														tokenType = slScanner.getNextToken();
														if (tokenType == TokenNameEOF) {
															break;
														} else {
															return inlineTag;
														}
													case TerminalTokens.TokenNameUNDERSCORE:
													case TerminalTokens.TokenNameIdentifier:
														if (processValue) {
															value = slScanner.getCurrentTokenString();
															if (REGION.equals(attribute)) {
																regionName = value;
															} else if (map.get(attribute) == null) {
																map.put(attribute, value);
																if ((attribute.equals(SUBSTRING) && (map.get(REGEX) != null))
																		|| (attribute.equals(REGEX) && (map.get(SUBSTRING) != null))) {
																	reportRegexSubstringTogether(snippetTag);
																	return inlineTag;
																}
															}
															processValue= false;
															attribute = null;
														} else {
															attribute = slScanner.getCurrentTokenString();
															switch(attribute) {
																case  SUBSTRING :
																case  REGEX :
																case  TYPE :
																	break;
																case  REGION :
																	createRegion = true;
																	setRegionPosition(slScanner.currentPosition);
																	break;
																default :
																	break;
															}
														}
														break;
													case TerminalTokens.TokenNameEQUAL:
														if (attribute != null) {
															processValue = true;
														}
														break;
													case TerminalTokens.TokenNameStringLiteral:
													case TerminalTokens.TokenNameSingleQuoteStringLiteral:
														if (processValue) {
															value = slScanner.getCurrentTokenString();
															value = stripQuotes(value);
															if (REGION.equals(attribute)) {
																regionName = value;
															} else if (map.get(attribute) == null) {
																map.put(attribute, value);
																if ((attribute.equals(SUBSTRING) && (map.get(REGEX) != null))
																		|| (attribute.equals(REGEX) && (map.get(SUBSTRING) != null))) {
																	reportRegexSubstringTogether(snippetTag);
																	return inlineTag;
																}
															}
															processValue= false;
															attribute = null;
														}
														break;
												}
												if (createTag) {
													break;
												}
												if (breakToMainSwitch)
													break mainSwitch;
											}
											tokenEnd = commentStart + 1 + slScanner.getCurrentTokenEndPosition();
											inlineTag = createSnippetInnerTag(newTagName, tokenStart, tokenEnd);
											addTagProperties(inlineTag, map, ++this.inlineTagCount);
											if (createRegion) {
												List<Object> tags = new ArrayList<>();
												tags.add(inlineTag);
												inlineTag = createSnippetRegion(regionName, tags, snippetTag, false, false);
											} else {
												this.nonRegionTagCount++;
											}
											inlineTags.add(inlineTag);
											if (!firstTagProcessed) {
												firstTagProcessed = true;
											}
											break;
										case REPLACE:
											tokenStart= commentStart + 1 + slScanner.getCurrentTokenStartPosition();
											tokenEnd= tokenStart + 8;
											newTagName = '@' + REPLACE;
											map = new HashMap<>();
											breakToMainSwitch = false;
											createTag = false;
											attribute = null;
											value = null;
											processValue = false;
											boolean hasReplacementStr = false;
											createRegion = false;
											regionName = null;
											while (true) {
												tokenType = slScanner.getNextToken();
												switch (tokenType) {
													case TokenNameEOF:
														createTag = true;
														break;
													case TerminalTokens.TokenNameAT:
														if (!processValue) {
															breakToMainSwitch = true;
															createTag = true;
														}
														processValue= false;
														break;
													case TerminalTokens.TokenNameCOLON:
														tokenType = slScanner.getNextToken();
														if (tokenType == TokenNameEOF) {
															break;
														} else {
															return inlineTag;
														}
													case TerminalTokens.TokenNameUNDERSCORE:
													case TerminalTokens.TokenNameIdentifier:
														if (processValue) {
															value = slScanner.getCurrentTokenString();
															if (REGION.equals(attribute)) {
																regionName = value;
															} else if (map.get(attribute) == null) {
																if (attribute.equals(REPLACEMENT)) {
																	hasReplacementStr = true;
																}
																map.put(attribute, value);
																if ((attribute.equals(SUBSTRING) && (map.get(REGEX) != null))
																		|| (attribute.equals(REGEX) && (map.get(SUBSTRING) != null))) {
																	reportRegexSubstringTogether(snippetTag);
																	return inlineTag;
																}
															}
															processValue= false;
															attribute = null;
														} else {
															attribute = slScanner.getCurrentTokenString();
															switch(attribute) {
																case  SUBSTRING :
																case  REGEX :
																case  REPLACEMENT :
																	break;
																case  REGION :
																	createRegion = true;
																	setRegionPosition(slScanner.currentPosition);
																	break;
																default :
																	break;
															}
														}
														break;
													case TerminalTokens.TokenNameEQUAL:
														if (attribute != null) {
															processValue = true;
														}
														break;
													case TerminalTokens.TokenNameStringLiteral:
													case TerminalTokens.TokenNameSingleQuoteStringLiteral:
														if (processValue) {
															value = slScanner.getCurrentTokenString();
															value = stripQuotes(value);
															if (REGION.equals(attribute)) {
																regionName = value;
															} else if (map.get(attribute) == null) {
																if (attribute.equals(REPLACEMENT)) {
																	hasReplacementStr = true;
																}
																map.put(attribute, value);
																if ((attribute.equals(SUBSTRING) && (map.get(REGEX) != null))
																		|| (attribute.equals(REGEX) && (map.get(SUBSTRING) != null))) {
																	reportRegexSubstringTogether(snippetTag);
																	return inlineTag;
																}
															}
															processValue= false;
															attribute = null;
														}
														break;
												}
												if (createTag) {
													break;
												}
												if (breakToMainSwitch)
													break mainSwitch;
											}
											if (!hasReplacementStr) {
												return inlineTag;
											}
											tokenEnd = commentStart + 1 + slScanner.getCurrentTokenEndPosition();
											inlineTag = createSnippetInnerTag(newTagName, tokenStart, tokenEnd);
											addTagProperties(inlineTag, map, ++this.inlineTagCount);
											if (createRegion) {
												List<Object> tags = new ArrayList<>();
												tags.add(inlineTag);
												inlineTag = createSnippetRegion(regionName, tags, snippetTag, false, false);
											} else {
												this.nonRegionTagCount++;
											}
											inlineTags.add(inlineTag);
											if (!firstTagProcessed) {
												firstTagProcessed = true;
											}
											break;
										case LINK :
											tokenStart= commentStart + 1 + slScanner.getCurrentTokenStartPosition();
											tokenEnd= tokenStart + 4;
											newTagName = '@' + LINK;
											map = new HashMap<>();
											breakToMainSwitch = false;
											createTag = false;
											attribute = null;
											value = null;
											processValue = false;
											boolean hasTarget = false;
											Object type = null;
											createRegion = false;
											regionName = null;
											while (true) {
												tokenType = slScanner.getNextToken();
												switch (tokenType) {
													case TokenNameEOF:
														createTag = true;
														break;
													case TerminalTokens.TokenNameAT:
														if (!processValue) {
															breakToMainSwitch = true;
															createTag = true;
														}
														processValue= false;
														break;
													case TerminalTokens.TokenNameCOLON:
														tokenType = slScanner.getNextToken();
														if (tokenType == TokenNameEOF) {
															break;
														} else {
															return inlineTag;
														}
													case TerminalTokens.TokenNameUNDERSCORE:
													case TerminalTokens.TokenNameIdentifier:
														if (processValue) {
															value = slScanner.getCurrentTokenString();
															if (map.get(attribute) == null) {
																if (REGION.equals(attribute)) {
																	regionName = value;
																} else if (TARGET.equals(attribute)) {
																	String originalTokenString = sScanner.getCurrentTokenString();
																	int offset = originalTokenString.lastIndexOf(tokenString);
																	if(offset == -1 ) {
																		offset = 1- tokenString.length(); // for # converted to // current position at end
																	}
																	if (!ignoreLink) {
																		type = parseLinkReference(slScanner.getCurrentTokenStartPosition() + offset, value, sScanner);
																	}
																	if (type != null) {
																		map.put(attribute, type);
																		hasTarget = true;
																	}
																} else {
																	map.put(attribute, value);
																	if ((attribute.equals(SUBSTRING) && (map.get(REGEX) != null))
																			|| (attribute.equals(REGEX) && (map.get(SUBSTRING) != null))) {
																		reportRegexSubstringTogether(snippetTag);
																		return inlineTag;
																	}
																}
															}
															processValue= false;
															attribute = null;
														} else {
															attribute = slScanner.getCurrentTokenString();
															switch(attribute) {
																case  SUBSTRING :
																case  REGEX :
																case  TYPE :
																case  TARGET :
																	break;
																case  REGION :
																	createRegion = true;
																	setRegionPosition(slScanner.currentPosition);
																	break;
																default :
																	break;
															}
														}
														break;
													case TerminalTokens.TokenNameEQUAL:
														if (attribute != null) {
															processValue = true;
														}
														break;
													case TerminalTokens.TokenNameStringLiteral:
													case TerminalTokens.TokenNameSingleQuoteStringLiteral:
														if (processValue) {
															value = slScanner.getCurrentTokenString();
															if (map.get(attribute) == null) {
																if (REGION.equals(attribute)) {
																	value = stripQuotes(value);
																	regionName = value;
																} else if (TARGET.equals(attribute)) {
																	String originalTokenString = sScanner.getCurrentTokenString();
																	int offset = originalTokenString.lastIndexOf(tokenString);
																	if(offset == -1 ) {
																		offset = 1- tokenString.length(); // for # converted to // current position at end
																	}
																	if (!ignoreLink) {
																		type = parseLinkReference(slScanner.getCurrentTokenStartPosition() + offset, value, sScanner);
																	}
																	if (type != null) {
																		map.put(attribute, type);
																		hasTarget = true;
																	}
																} else {
																	value = stripQuotes(value);
																	map.put(attribute, value);
																	if ((attribute.equals(SUBSTRING) && (map.get(REGEX) != null))
																			|| (attribute.equals(REGEX) && (map.get(SUBSTRING) != null))) {
																		reportRegexSubstringTogether(snippetTag);
																		return inlineTag;
																	}
																}
															}
															processValue= false;
															attribute = null;
														}
														break;
												}
												if (createTag) {
													break;
												}
												if (breakToMainSwitch)
													break mainSwitch;
											}
											tokenEnd = commentStart + 1 + slScanner.getCurrentTokenEndPosition();
											if (hasTarget) {
												inlineTag = createSnippetInnerTag(newTagName, tokenStart, tokenEnd);
												addTagProperties(inlineTag, map, ++this.inlineTagCount);
												if (createRegion) {
													List<Object> tags = new ArrayList<>();
													tags.add(inlineTag);
													inlineTag = createSnippetRegion(regionName, tags, snippetTag, false, false);
												} else {
													this.nonRegionTagCount++;
												}
												inlineTags.add(inlineTag);
												if (!firstTagProcessed) {
													firstTagProcessed = true;
												}
											}
											break;
										case  END :
											boolean closeRegion = false;
											regionName = null;
											processValue = false;
											attribute = null;
											breakToMainSwitch = false;
											while (true) {
												tokenType = slScanner.getNextToken();
												switch (tokenType) {
													case TokenNameEOF:
														closeRegion = true;
														break;
													case TerminalTokens.TokenNameAT:
														if (!processValue) {
															breakToMainSwitch = true;
															closeRegion = true;
														}
														processValue= false;
														break;
													case TerminalTokens.TokenNameCOLON:
														tokenType = slScanner.getNextToken();
														if (tokenType == TokenNameEOF) {
															break;
														} else {
															return inlineTag;
														}
													case TerminalTokens.TokenNameUNDERSCORE:
													case TerminalTokens.TokenNameIdentifier:
														if (processValue && REGION.equals(attribute)) {
															regionName = slScanner.getCurrentTokenString();
															processValue= false;
															attribute = null;
														} else {
															attribute = slScanner.getCurrentTokenString();
															switch(attribute) {
																case  REGION :
																	setRegionPosition(slScanner.currentPosition);
																	break;
																default :
																	break;
															}
														}
														break;
													case TerminalTokens.TokenNameEQUAL:
														if (attribute != null) {
															processValue = true;
														}
														break;
													case TerminalTokens.TokenNameStringLiteral:
													case TerminalTokens.TokenNameSingleQuoteStringLiteral:
														if (processValue && REGION.equals(attribute)) {
															regionName = slScanner.getCurrentTokenString();
															regionName = stripQuotes(regionName);
														}
														processValue= false;
														attribute = null;
														break;
												}
												if (closeRegion) {
													break;
												}
											}
											if (closeRegion) {
												tokenEnd = commentStart + 1 + slScanner.getCurrentTokenEndPosition();
												this.closeJavaDocRegion(regionName, snippetTag, tokenEnd);
												regionClosed = true;
												if (!firstTagProcessed) {
													firstTagProcessed = true;
												}
											}
											if (breakToMainSwitch) {
												break mainSwitch;
											}
											break;
										default :
											return inlineTag;
									}
								}
								break;
							default:
								return inlineTag;//if at token not started then invalid
						}

					} catch (InvalidInputException e) {
						// do nothing
					}
				}
			}
		}
		finally {
			if (inlineTags.size() > 1
					|| (initialTagCount > 0 && inlineTags.size() > 0)) {
				inlineTag = createSnippetRegion(null, inlineTags, snippetTag, true, (initialTagCount > 0) ? true : false);
			} else if (inlineTags.size() == 1) {
				inlineTag = inlineTags.get(0);
			}
		}
		if (regionClosed && inlineTag == null) {
			return snippetTag;
		}
		if (inlineTags.size() == 0 && inlineTag == null && ignoreLink) {
			return snippetTag;
		}
		return inlineTag;
	}

	private String stripQuotes(String str) {
		if (str == null || str.length() <= 2) {
			return str;
		}
		String finalStr= str;
		int lastIndex = finalStr.length() - 1;
		if ((finalStr.charAt(0) =='"' && finalStr.charAt(lastIndex)=='"')
				||(finalStr.charAt(0) =='\'' && finalStr.charAt(lastIndex)=='\'')) {
			finalStr = finalStr.substring(1, finalStr.length()-1);
		}
		return finalStr;
	}
	private Object parseLinkReference(int curPosition, String value, Scanner sScanner) {
		Object typeRef = null;
		Object reference = null;
		int indexx = this.index;
		int oldCurrentPosition = sScanner.currentPosition;
		int oldStartPosition = sScanner.startPosition;
		char c = value.charAt(0);
		int additionalIndex = 2;
		if (c == '"' || c== '\'' ) {
			additionalIndex += 1;
		}
		sScanner.currentPosition = sScanner.getCurrentTokenStartPosition() + curPosition  + additionalIndex;
		int allowedLength = sScanner.getCurrentTokenStartPosition() + curPosition + value.length();
		this.index = sScanner.startPosition;
		int oldToken = this.currentTokenType;
		this.currentTokenType = -1;
		boolean tokenizeWhiteSpaces = this.scanner.tokenizeWhiteSpace;
		this.scanner.tokenizeWhiteSpace = false;
		try {
			while (this.scanner.currentPosition < allowedLength) {
				int token = readTokenSafely();
				this.scanner.tokenizeWhiteSpace = true;
				switch (token) {
					case TerminalTokens.TokenNameERROR :
						consumeToken();
						if (this.scanner.currentCharacter == '#') { // @see ...#member
							reference = parseMember(typeRef, true);
						}
						break;
					case TerminalTokens.TokenNameUNDERSCORE:
					case TerminalTokens.TokenNameIdentifier :
						typeRef = parseQualifiedName(true, true);
						break;
					default :
						return null;
				}

			}
		} catch (InvalidInputException ex) {
			typeRef= null;
		}
		finally {
			sScanner.currentPosition = oldCurrentPosition;
			sScanner.startPosition = oldStartPosition;
			this.index = indexx;
			this.currentTokenType = oldToken;
			sScanner.tokenizeWhiteSpace = tokenizeWhiteSpaces;
		}
		if (reference != null) {
			typeRef = reference;
		}
		return typeRef;
	}

	private boolean containsNewLine(String str) {
		boolean consider = false;
		if(str != null
				&& (str.contains(System.lineSeparator())
						|| str.indexOf('\n') != -1)) {
			consider = true;
		}
		return consider;
	}

	/*
	 * Parse tag declaration
	 */
	protected abstract boolean parseTag(int previousPosition) throws InvalidInputException;

	/*
	 * Parse @throws tag declaration
	 */
	protected boolean parseThrows() {
		int start = this.scanner.currentPosition;
		boolean isCompletionParser = (this.kind & COMPLETION_PARSER) != 0;
		try {
			Object typeRef = parseQualifiedName(true);
			if (this.abort) return false; // May be aborted by specialized parser
			if (typeRef == null) {
				if (this.reportProblems)
					this.sourceParser.problemReporter().javadocMissingThrowsClassName(this.tagSourceStart, this.tagSourceEnd, this.sourceParser.modifiers);
			} else {
				return pushThrowName(typeRef);
			}
		} catch (InvalidInputException ex) {
			if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidThrowsClass(start, getTokenEndPosition());
		}
		if (!isCompletionParser) {
			this.scanner.currentPosition = start;
			this.index = start;
		}
		this.currentTokenType = -1;
		return false;
	}

	/*
	 * Return current character without move index position.
	 */
	protected char peekChar() {
		int idx = this.index;
		char c = this.source[idx++];
		if (c == '\\' && this.source[idx] == 'u') {
			int c1, c2, c3, c4;
			idx++;
			while (this.source[idx] == 'u')
				idx++;
			if (!(((c1 = ScannerHelper.getHexadecimalValue(this.source[idx++])) > 15 || c1 < 0)
					|| ((c2 = ScannerHelper.getHexadecimalValue(this.source[idx++])) > 15 || c2 < 0)
					|| ((c3 = ScannerHelper.getHexadecimalValue(this.source[idx++])) > 15 || c3 < 0)
					|| ((c4 = ScannerHelper.getHexadecimalValue(this.source[idx++])) > 15 || c4 < 0))) {
				c = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			}
		}
		return c;
	}

	/*
	 * push the consumeToken on the identifier stack. Increase the total number of identifier in the stack.
	 */
	protected void pushIdentifier(boolean newLength, boolean isToken) {

		int stackLength = this.identifierStack.length;
		if (++this.identifierPtr >= stackLength) {
			System.arraycopy(
				this.identifierStack, 0,
				this.identifierStack = new char[stackLength + 10][], 0,
				stackLength);
			System.arraycopy(
				this.identifierPositionStack, 0,
				this.identifierPositionStack = new long[stackLength + 10], 0,
				stackLength);
		}
		this.identifierStack[this.identifierPtr] = isToken ? this.scanner.getCurrentTokenSource() : this.scanner.getCurrentIdentifierSource();
		this.identifierPositionStack[this.identifierPtr] = (((long) this.scanner.startPosition) << 32) + (this.scanner.currentPosition - 1);

		if (newLength) {
			stackLength = this.identifierLengthStack.length;
			if (++this.identifierLengthPtr >= stackLength) {
				System.arraycopy(
					this.identifierLengthStack, 0,
					this.identifierLengthStack = new int[stackLength + 10], 0,
					stackLength);
			}
			this.identifierLengthStack[this.identifierLengthPtr] = 1;
		} else {
			this.identifierLengthStack[this.identifierLengthPtr]++;
		}
	}

	/*
	 * Add a new obj on top of the ast stack.
	 * If new length is required, then add also a new length in length stack.
	 */
	protected void pushOnAstStack(Object node, boolean newLength) {

		if (node == null) {
			int stackLength = this.astLengthStack.length;
			if (++this.astLengthPtr >= stackLength) {
				System.arraycopy(
					this.astLengthStack, 0,
					this.astLengthStack = new int[stackLength + AST_STACK_INCREMENT], 0,
					stackLength);
			}
			this.astLengthStack[this.astLengthPtr] = 0;
			return;
		}

		int stackLength = this.astStack.length;
		if (++this.astPtr >= stackLength) {
			System.arraycopy(
				this.astStack, 0,
				this.astStack = new Object[stackLength + AST_STACK_INCREMENT], 0,
				stackLength);
			this.astPtr = stackLength;
		}
		this.astStack[this.astPtr] = node;

		if (newLength) {
			stackLength = this.astLengthStack.length;
			if (++this.astLengthPtr >= stackLength) {
				System.arraycopy(
					this.astLengthStack, 0,
					this.astLengthStack = new int[stackLength + AST_STACK_INCREMENT], 0,
					stackLength);
			}
			this.astLengthStack[this.astLengthPtr] = 1;
		} else {
			this.astLengthStack[this.astLengthPtr]++;
		}
	}

	/*
	 * Push a param name in ast node stack.
	 */
	protected abstract boolean pushParamName(boolean isTypeParam);

	/*
	 * Push a reference statement in ast node stack.
	 */
	protected abstract boolean pushSeeRef(Object statement);

	/*
	 * Push a text element in ast node stack
	 */
	protected void pushText(int start, int end) {
		// do not store text by default
	}

	protected void pushSnippetText(char[] text, int start, int end, boolean addNewLine, Object snippetTag) {
		// do not store text by default
	}

	protected abstract void closeJavaDocRegion(String name, Object snippetTag, int end);
	protected abstract boolean areRegionsClosed();

	protected void pushExternalSnippetText(char[] text, int start, int end, boolean addNewLine, Object snippetTag) {
		// do not store text by default
	}

	protected abstract Object createSnippetTag();

	protected abstract Object createSnippetInnerTag(String tagName, int start, int end);

	protected abstract Object createSnippetRegion(String name, List<Object> tags, Object snippetTag, boolean isDummyRegion, boolean considerPrevTag);

	protected abstract void addTagProperties(Object Tag, Map<String, Object> map, int tagCount);

	protected abstract void addSnippetInnerTag(Object tag, Object snippetTag);

	protected abstract void setSnippetError(Object tag, String value);

	protected abstract void setSnippetIsValid(Object tag, boolean value);

	protected abstract void setSnippetID(Object tag, String value);

	/*
	 * Push a throws type ref in ast node stack.
	 */
	protected abstract boolean pushThrowName(Object typeRef);

	protected abstract void setRegionPosition(int currentPosition);


	private void reportRegexSubstringTogether(Object snippetTag) {
		if(this.reportProblems) {
			this.sourceParser.problemReporter().javadocInvalidSnippetRegexSubstringTogether(this.lineEnd -this.scanner.getCurrentTokenString().length() +2, this.lineEnd);
		}
		this.setSnippetIsValid(snippetTag, false);
		this.setSnippetError(snippetTag, "Regex and substring together"); //$NON-NLS-1$
	}

	/*
	 * Read current character and move index position.
	 * Warning: scanner position is unchanged using this method!
	 */
	protected char readChar() {

		char c = this.source[this.index++];
		if (c == '\\' && this.source[this.index] == 'u') {
			int c1, c2, c3, c4;
			int pos = this.index;
			this.index++;
			while (this.source[this.index] == 'u')
				this.index++;
			if (!(((c1 = ScannerHelper.getHexadecimalValue(this.source[this.index++])) > 15 || c1 < 0)
					|| ((c2 = ScannerHelper.getHexadecimalValue(this.source[this.index++])) > 15 || c2 < 0)
					|| ((c3 = ScannerHelper.getHexadecimalValue(this.source[this.index++])) > 15 || c3 < 0)
					|| ((c4 = ScannerHelper.getHexadecimalValue(this.source[this.index++])) > 15 || c4 < 0))) {
				c = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			} else {
				// TODO (frederic) currently reset to previous position, perhaps signal a syntax error would be more appropriate
				this.index = pos;
			}
		}
		return c;
	}

	/*
	 * get current character.
	 * Warning: scanner position is unchanged using this method!
	 */
	private char getChar() {
		int indexVal = this.index;
		char c = this.source[indexVal++];
		if (c == '\\' && this.source[indexVal] == 'u') {
			int c1, c2, c3, c4;
			int pos = indexVal;
			indexVal++;
			while (this.source[indexVal] == 'u')
				indexVal++;
			if (!(((c1 = ScannerHelper.getHexadecimalValue(this.source[indexVal++])) > 15 || c1 < 0)
					|| ((c2 = ScannerHelper.getHexadecimalValue(this.source[indexVal++])) > 15 || c2 < 0)
					|| ((c3 = ScannerHelper.getHexadecimalValue(this.source[indexVal++])) > 15 || c3 < 0)
					|| ((c4 = ScannerHelper.getHexadecimalValue(this.source[indexVal++])) > 15 || c4 < 0))) {
				c = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			} else {
				// TODO (frederic) currently reset to previous position, perhaps signal a syntax error would be more appropriate
				indexVal = pos;
			}
		}
		return c;
	}

	private boolean considerNextChar() {
		boolean consider = true;
		char ch = getChar();
		if (ch == ' ' || (System.lineSeparator().indexOf(ch) == 0)) {
			consider = false;
		}
		return consider;
	}

	/*
	 * Read token only if previous was consumed
	 */
	protected int readToken() throws InvalidInputException {
		if (this.currentTokenType < 0) {
			this.tokenPreviousPosition = this.scanner.currentPosition;
			this.currentTokenType = this.scanner.getNextToken();
			if (this.scanner.currentPosition > (this.lineEnd+1)) { // be sure to be on next line (lineEnd is still on the same line)
				this.lineStarted = false;
				while (this.currentTokenType == TerminalTokens.TokenNameMULTIPLY) {
					this.currentTokenType = this.scanner.getNextToken();
				}
			}
			this.index = this.scanner.currentPosition;
			this.lineStarted = true; // after having read a token, line is obviously started...
		}
		return this.currentTokenType;
	}

	protected int readTokenAndConsume() throws InvalidInputException {
		int token = readToken();
		consumeToken();
		return token;
	}

	/*
	 * Read token without throwing any InvalidInputException exception.
	 * Returns TerminalTokens.TokenNameERROR instead.
	 */
	protected int readTokenSafely() {
		int token = TerminalTokens.TokenNameERROR;
		try {
			token = readToken();
		}
		catch (InvalidInputException iie) {
			// token is already set to error
		}
		return token;
	}

	protected void recordInheritedPosition(long position) {
		if (this.inheritedPositions == null) {
			this.inheritedPositions = new long[INHERITED_POSITIONS_ARRAY_INCREMENT];
			this.inheritedPositionsPtr = 0;
		} else {
			if (this.inheritedPositionsPtr == this.inheritedPositions.length) {
				System.arraycopy(
						this.inheritedPositions, 0,
						this.inheritedPositions = new long[this.inheritedPositionsPtr + INHERITED_POSITIONS_ARRAY_INCREMENT], 0,
						this.inheritedPositionsPtr);
			}
		}
		this.inheritedPositions[this.inheritedPositionsPtr++] = position;
	}

	/*
	 * Refresh start position and length of an inline tag.
	 */
	protected void refreshInlineTagPosition(int previousPosition) {
		// do nothing by default
	}

	/*
	 * Refresh return statement
	 */
	protected void refreshReturnStatement() {
		// do nothing by default
	}

	/**
	 * @param started the inlineTagStarted to set
	 */
	protected void setInlineTagStarted(boolean started) {
		this.inlineTagStarted = started;
	}

	/*
	 * Entry point for recovery on invalid syntax
	 */
	protected Object syntaxRecoverQualifiedName(int primitiveToken) throws InvalidInputException {
		// do nothing, just an entry point for recovery
		return null;
	}

	/*
	 * Entry point for recovery on invalid syntax
	 */
	protected Object syntaxRecoverModuleQualifiedName(int primitiveToken, int moduleTokenCount) throws InvalidInputException {
		// do nothing, just an entry point for recovery
		return null;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		int startPos = this.scanner.currentPosition<this.index ? this.scanner.currentPosition : this.index;
		int endPos = this.scanner.currentPosition<this.index ? this.index : this.scanner.currentPosition;
		if (startPos == this.source.length)
			return "EOF\n\n" + new String(this.source); //$NON-NLS-1$
		if (endPos > this.source.length)
			return "behind the EOF\n\n" + new String(this.source); //$NON-NLS-1$

		char front[] = new char[startPos];
		System.arraycopy(this.source, 0, front, 0, startPos);

		int middleLength = (endPos - 1) - startPos + 1;
		char middle[];
		if (middleLength > -1) {
			middle = new char[middleLength];
			System.arraycopy(
				this.source,
				startPos,
				middle,
				0,
				middleLength);
		} else {
			middle = CharOperation.NO_CHAR;
		}

		char end[] = new char[this.source.length - (endPos - 1)];
		System.arraycopy(
			this.source,
			(endPos - 1) + 1,
			end,
			0,
			this.source.length - (endPos - 1) - 1);

		buffer.append(front);
		if (this.scanner.currentPosition<this.index) {
			buffer.append("\n===============================\nScanner current position here -->"); //$NON-NLS-1$
		} else {
			buffer.append("\n===============================\nParser index here -->"); //$NON-NLS-1$
		}
		buffer.append(middle);
		if (this.scanner.currentPosition<this.index) {
			buffer.append("<-- Parser index here\n===============================\n"); //$NON-NLS-1$
		} else {
			buffer.append("<-- Scanner current position here\n===============================\n"); //$NON-NLS-1$
		}
		buffer.append(end);

		return buffer.toString();
	}

	/*
	 * Update
	 */
	protected abstract void updateDocComment();

	/*
	 * Update line end
	 */
	protected void updateLineEnd() {
		while (this.index > (this.lineEnd+1)) { // be sure to be on next line (lineEnd is still on the same line)
			if (this.linePtr < this.lastLinePtr) {
				this.lineEnd = this.scanner.getLineEnd(++this.linePtr) - 1;
			} else {
				this.lineEnd = this.javadocEnd;
				return;
			}
		}
	}

	/*
	 * Verify that end of the line only contains space characters or end of comment.
	 * Note that end of comment may be preceding by several contiguous '*' chars.
	 */
	protected boolean verifyEndLine(int textPosition) {
		boolean domParser = (this.kind & DOM_PARSER) != 0;
		// Special case for inline tag
		if (this.inlineTagStarted) {
			// expecting closing brace
			if (peekChar() == '}') {
				if (domParser) {
					createTag();
					pushText(textPosition, this.index);
				}
				return true;
			}
			return false;
		}

		int startPosition = this.index;
		int previousPosition = this.index;
		this.starPosition = -1;
		char ch = readChar();
		nextChar: while (true) {
			switch (ch) {
				case '\r':
				case '\n':
					if (domParser) {
						createTag();
						pushText(textPosition, previousPosition);
					}
					this.index = previousPosition;
					return true;
				case '\u000c' :	/* FORM FEED               */
				case ' ' :			/* SPACE                   */
				case '\t' :			/* HORIZONTAL TABULATION   */
					if (this.starPosition >= 0) break nextChar;
					break;
				case '*':
					this.starPosition = previousPosition;
					break;
				case '/':
					if (this.starPosition >= textPosition) { // valid only if a star was the previous character
						if (domParser) {
							createTag();
							pushText(textPosition, this.starPosition);
						}
						return true;
					}
					break nextChar;
				default :
					// leave loop
					break nextChar;

			}
			previousPosition = this.index;
			ch = readChar();
		}
		this.index = startPosition;
		return false;
	}

	/*
	 * Verify characters after a name matches one of following conditions:
	 * 	1- first character is a white space
	 * 	2- first character is a closing brace *and* we're currently parsing an inline tag
	 * 	3- are the end of comment (several contiguous star ('*') characters may be
	 * 	    found before the last slash ('/') character).
	 */
	protected boolean verifySpaceOrEndComment() {
		this.starPosition = -1;
		int startPosition = this.index;
		// Whitespace or inline tag closing brace
		char ch = peekChar();
		switch (ch) {
			case '}':
				return this.inlineTagStarted;
			default:
				if (ScannerHelper.isWhitespace(ch)) {
					return true;
				}
		}
		// End of comment
		int previousPosition = this.index;
		ch = readChar();
		while (this.index<this.source.length) {
			switch (ch) {
				case '*':
					// valid whatever the number of star before last '/'
					this.starPosition = previousPosition;
					break;
				case '/':
					if (this.starPosition >= startPosition) { // valid only if a star was the previous character
						return true;
					}
					// $FALL-THROUGH$ - fall through to invalid case
				default :
					// invalid whatever other character, even white spaces
					this.index = startPosition;
					return false;

			}
			previousPosition = this.index;
			ch = readChar();
		}
		this.index = startPosition;
		return false;
	}

	protected void setSourceComplianceLevel() {
		if (this.sourceParser != null) {
			this.checkDocComment = this.sourceParser.options.docCommentSupport;
			this.sourceLevel = this.sourceParser.options.sourceLevel;
			this.scanner.sourceLevel = this.sourceLevel;
			this.complianceLevel = this.sourceParser.options.complianceLevel;
		}
	}

	/**
	 * @param projectPath Absolute path in local file system
	 */
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public void setProjectSrcClasspath(List path) {
		this.srcClasspath = path;
	}


}
