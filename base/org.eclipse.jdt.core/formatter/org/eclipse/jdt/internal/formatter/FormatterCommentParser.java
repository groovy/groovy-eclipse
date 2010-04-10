/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.JavadocParser;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.formatter.comment.IJavaDocTagConstants;

/**
 * Internal parser used for formatting javadoc comments.
 */
public class FormatterCommentParser extends JavadocParser implements IJavaDocTagConstants {
	char[][] htmlTags;
	int htmlTagsPtr = -1;
	int inlineHtmlTagsPtr = -1;
	private boolean invalidTagName;
	public boolean parseHtmlTags;

public FormatterCommentParser(long sourceLevel) {
	super(null);
	this.kind = FORMATTER_COMMENT_PARSER | TEXT_PARSE;
	this.reportProblems = false;
	this.checkDocComment = true;
	this.sourceLevel = sourceLevel;
}

public boolean parse(int start, int end) {

	// Init
	this.javadocStart = start;
	this.javadocEnd = end;
	this.firstTagPosition = this.javadocStart;
	// Need to flush html tags stack in case of unclosed ones in previous javadoc comments
	// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=239941
	this.htmlTagsPtr = -1;

	// parse comment
	boolean valid = commentParse();

	return valid && this.docComment != null;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createArgumentReference(char[], int, boolean, java.lang.Object, long[], long)
 */
protected Object createArgumentReference(char[] name, int dim, boolean isVarargs, Object ref, long[] dimPositions, long argNamePos) throws InvalidInputException {
	FormatJavadocReference typeRef = (FormatJavadocReference) ref;
	if (dim > 0) {
		typeRef.sourceEnd = (int) dimPositions[dim-1];
	}
	if (argNamePos >= 0) typeRef.sourceEnd = (int) argNamePos;
	return ref;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#createFakeReference(int)
 */
protected boolean createFakeReference(int start) {
	// synch scanner and parser positions
	this.scanner.currentPosition = this.index;
	// create reference in order to be able to format it
	int lineStart = this.scanner.getLineNumber(start);
	FormatJavadocReference reference = new FormatJavadocReference(start, this.index-1, lineStart);
	return pushSeeRef(reference);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createFieldReference(java.lang.Object)
 */
protected Object createFieldReference(Object receiver) throws InvalidInputException {
	int start = receiver == null ? this.memberStart : ((FormatJavadocReference)receiver).sourceStart;
	int lineStart = this.scanner.getLineNumber(start);
	return new FormatJavadocReference(start, (int) this.identifierPositionStack[0], lineStart);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createMethodReference(java.lang.Object, java.util.List)
 */
protected Object createMethodReference(Object receiver, List arguments) throws InvalidInputException {
	int start = receiver == null ? this.memberStart : ((FormatJavadocReference) receiver).sourceStart;
	int lineStart = this.scanner.getLineNumber(start);
	return new FormatJavadocReference(start, this.scanner.getCurrentTokenEndPosition(), lineStart);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createTag()
 */
protected void createTag() {
	int lineStart = this.scanner.getLineNumber(this.tagSourceStart);
	if (this.inlineTagStarted) {
		FormatJavadocBlock block = new FormatJavadocBlock(this.inlineTagStart, this.tagSourceEnd, lineStart, this.tagValue);
		FormatJavadocBlock previousBlock = null;
		if (this.astPtr == -1) {
			previousBlock = new FormatJavadocBlock(this.inlineTagStart, this.tagSourceEnd, lineStart, NO_TAG_VALUE);
			pushOnAstStack(previousBlock, true);
		} else {
			previousBlock = (FormatJavadocBlock) this.astStack[this.astPtr];
		}
		previousBlock.addBlock(block, this.htmlTagsPtr == -1 ? 0 : this.htmlTagsPtr);
	} else {
		FormatJavadocBlock block = new FormatJavadocBlock(this.tagSourceStart, this.tagSourceEnd, lineStart, this.tagValue);
		pushOnAstStack(block, true);
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createTypeReference(int)
 */
protected Object createTypeReference(int primitiveToken) {
	int size = this.identifierLengthStack[this.identifierLengthPtr];
	if (size == 0) return null;
	int start = (int) (this.identifierPositionStack[this.identifierPtr] >>> 32);
	int lineStart = this.scanner.getLineNumber(start);
	if (size == 1) {
		return new FormatJavadocReference(this.identifierPositionStack[this.identifierPtr], lineStart);
	}
	long[] positions = new long[size];
	System.arraycopy(this.identifierPositionStack, this.identifierPtr - size + 1, positions, 0, size);
	return new FormatJavadocReference((int) (positions[0] >>> 32), (int) positions[positions.length-1], lineStart);
}

/*
 * Return the html tag index in the various arrays of IJavaDocTagConstants.
 * The returned int is set as follow:
 * 	- the array index is set on bits 0 to 7
 * 	- the tag category is set on bit 8 to 15 (0xFF00 if no array includes the tag)
 */
private int getHtmlTagIndex(char[] htmlTag) {
	int length = htmlTag == null ? 0 : htmlTag.length;
	int tagId = 0;
	if (length > 0) {
		for (int i=0, max=JAVADOC_SINGLE_BREAK_TAG.length; i<max; i++) {
			char[] tag = JAVADOC_SINGLE_BREAK_TAG[i];
			if (length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return (tagId | JAVADOC_SINGLE_BREAK_TAG_ID) + i;
			}
		}
		for (int i=0, max=JAVADOC_CODE_TAGS.length; i<max; i++) {
			char[] tag = JAVADOC_CODE_TAGS[i];
			if (length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return (tagId | JAVADOC_CODE_TAGS_ID) + i;
			}
		}
		for (int i=0, max=JAVADOC_BREAK_TAGS.length; i<max; i++) {
			char[] tag = JAVADOC_BREAK_TAGS[i];
			if (length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return (tagId | JAVADOC_BREAK_TAGS_ID) + i;
			}
		}
		for (int i=0, max=JAVADOC_IMMUTABLE_TAGS.length; i<max; i++) {
			char[] tag = JAVADOC_IMMUTABLE_TAGS[i];
			if (length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return (tagId | JAVADOC_IMMUTABLE_TAGS_ID) + i;
			}
		}
		for (int i=0, max=JAVADOC_SEPARATOR_TAGS.length; i<max; i++) {
			char[] tag = JAVADOC_SEPARATOR_TAGS[i];
			if (length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return (tagId | JAVADOC_SEPARATOR_TAGS_ID) + i;
			}
		}
	}
	return JAVADOC_TAGS_ID_MASK;
}

/*
 * Parse an HTML tag expected to be either opening (e.g. <tag_name> ) or
 * closing (e.g. </tag_name>).
 */
protected boolean parseHtmlTag(int previousPosition, int endTextPosition) throws InvalidInputException {
	if (!this.parseHtmlTags) return false;
    boolean closing = false;
    boolean valid = false;
    boolean incremented = false;
    int start = this.scanner.currentPosition;
    int currentPosition = start;
    int htmlPtr = this.htmlTagsPtr;
    char firstChar = peekChar();
    boolean hasWhitespaces = firstChar == ' ' || ScannerHelper.isWhitespace(firstChar);
	try {
	    int token = readTokenAndConsume();
	    char[] htmlTag;
	    int htmlIndex;
	    switch (token) {
	    	case TerminalTokens.TokenNameIdentifier:
	    		// HTML tag opening
				htmlTag = this.scanner.getCurrentIdentifierSource();
				htmlIndex = getHtmlTagIndex(htmlTag);
				if (htmlIndex == JAVADOC_TAGS_ID_MASK) return false;
				if (htmlPtr >= 0) {
		    		int lastHtmlTagIndex = getHtmlTagIndex(this.htmlTags[htmlPtr]);
					if ((lastHtmlTagIndex & JAVADOC_TAGS_ID_MASK) == JAVADOC_IMMUTABLE_TAGS_ID) {
						// Do not accept tags inside immutable tags except the <pre> tag
						if ((htmlIndex & JAVADOC_TAGS_ID_MASK) == JAVADOC_CODE_TAGS_ID) {
							FormatJavadocBlock previousBlock = (FormatJavadocBlock) this.astStack[this.astPtr];
							FormatJavadocNode parentNode = previousBlock;
							FormatJavadocNode lastNode = parentNode;
							while (lastNode.getLastNode() != null) {
								parentNode = lastNode;
								lastNode = lastNode.getLastNode();
							}
							if (lastNode.isText()) {
								FormatJavadocText text = (FormatJavadocText) lastNode;
								if (text.separatorsPtr == -1) {
									break;
								}
							}
						}
		    			return false;
					}
	    		}
				if ((htmlIndex & JAVADOC_TAGS_ID_MASK) > JAVADOC_SINGLE_TAGS_ID) {
		    		if (this.htmlTagsPtr == -1 || !CharOperation.equals(this.htmlTags[this.htmlTagsPtr], htmlTag, false)) {
						if (++this.htmlTagsPtr == 0) { // lazy initialization
							this.htmlTags = new char[AST_STACK_INCREMENT][];
						} else { // resize if needed
							if (this.htmlTagsPtr == this.htmlTags.length) {
								System.arraycopy(this.htmlTags, 0, (this.htmlTags = new char[this.htmlTags.length + AST_STACK_INCREMENT][]), 0, this.htmlTagsPtr);
							}
						}
						this.htmlTags[this.htmlTagsPtr] = htmlTag;
						incremented = true;
		    		}
				}
				// Accept xhtml syntax
				currentPosition = this.scanner.currentPosition;
				if (readToken() == TerminalTokens.TokenNameDIVIDE) {
					consumeToken();
				}
		    	break;
	    	case TerminalTokens.TokenNameDIVIDE:
	    		// HTML tag closing
	    		if (this.htmlTagsPtr == -1) return false;
	    		htmlTag = this.htmlTags[this.htmlTagsPtr];
	    		if ((token = readTokenAndConsume()) != TerminalTokens.TokenNameIdentifier) {
	    			// not a closing html tag
	    			return false;
	    		}
				char[] identifier = this.scanner.getCurrentIdentifierSource();
				htmlIndex = getHtmlTagIndex(identifier);
				if (htmlIndex == JAVADOC_TAGS_ID_MASK) return false;
				int ptr = this.htmlTagsPtr;
	    		while (!CharOperation.equals(htmlTag, identifier, false)) {
	    			if (this.htmlTagsPtr <= 0) {
	    				// consider the closing tag as invalid
	    				this.htmlTagsPtr = ptr;
	    				return false;
	    			}
	    			this.htmlTagsPtr--;
		    		htmlTag = this.htmlTags[this.htmlTagsPtr];
	    		}
				// set closing flag
				htmlIndex |= JAVADOC_CLOSED_TAG;
				closing = true;
				currentPosition = this.scanner.currentPosition;
	    		break;
	    	default:
    			return false;
	    }
	    
	    // Looking for tag closing
	    switch (readTokenAndConsume()) {
	    	case TerminalTokens.TokenNameLESS:
	    	case TerminalTokens.TokenNameLESS_EQUAL:
	    		// consider that the closing '>' is missing
	    		return false;
	    	case TerminalTokens.TokenNameGREATER:
	    		// simple tag without attributes
	    		break;
	    	case TerminalTokens.TokenNameGREATER_EQUAL:
	    	case TerminalTokens.TokenNameRIGHT_SHIFT:
	    	case TerminalTokens.TokenNameRIGHT_SHIFT_EQUAL:
	    		// simple tag without attributes, but the closing '>' is followed by an '=' or '>'
	    		break;
	    	default:
	    		this.index = currentPosition;
	    		loop: while (true) {
//	    			currentPosition = this.index;
				    switch (readChar()) {
				    	case '<':
				    		if (hasWhitespaces) {
				    			// not 100% sure this is a tag definition => give up
				    			return false;
				    		}
				    		// opening tag => consider the current one as closed
				    		this.index = currentPosition;
				    		this.scanner.startPosition = currentPosition;
				    		this.scanner.currentPosition = currentPosition;
				    		this.scanner.currentCharacter = '<';
				    		break loop;
				    	case '>':
				    		// simple tag without attributes
				    		this.scanner.startPosition = this.index;
				    		this.scanner.currentPosition = this.index;
				    		this.scanner.currentCharacter = peekChar();
				    		break loop;
			    		default:
			    			break;
				    }
				    if (this.index >= this.javadocTextEnd) {
		    			// the end of the comment is reached => consider current tag as closed
			    		this.index = currentPosition;
			    		this.scanner.startPosition = currentPosition;
			    		this.scanner.currentPosition = currentPosition;
			    		break;
				    }
	    		}
		}

	    // Push texts
		if (this.lineStarted && this.textStart != -1 && this.textStart < endTextPosition) {
			pushText(this.textStart, endTextPosition, -1, htmlPtr);
		}
		pushText(previousPosition, this.index, htmlIndex, this.htmlTagsPtr);
		this.textStart = -1;
		valid = true;
	}
	finally {
		if (valid) {
			if (closing) {
				this.htmlTagsPtr--;
			}
		} else if (!this.abort) {
	    	if (incremented) {
	    		this.htmlTagsPtr--;
	    		if (this.htmlTagsPtr == -1) this.htmlTags = null;
	    	}
	    	this.scanner.resetTo(start, this.scanner.eofPosition-1);
	    	this.index = start;
		}
	}
    return valid;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#parseIdentifierTag(boolean)
 */
protected boolean parseIdentifierTag(boolean report) {
	if (super.parseIdentifierTag(report)) {
		createTag();
		this.index = this.tagSourceEnd+1;
		this.scanner.resetTo(this.index, this.javadocEnd);
		return true;
	}
	this.tagValue = TAG_OTHERS_VALUE; // tag is invalid, do not keep the parsed tag value
	return false;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#parseParam()
 */
protected boolean parseParam() throws InvalidInputException {
	boolean valid = super.parseParam();
	if (!valid) {
		this.scanner.resetTo(this.tagSourceEnd+1, this.javadocEnd);
		this.index = this.tagSourceEnd+1;
		char ch = peekChar();
		// Try to push an identifier in the stack, otherwise restart from the end tag position
		if (ch == ' ' || ScannerHelper.isWhitespace(ch)) {
			int token = this.scanner.getNextToken();
			if (token == TerminalTokens.TokenNameIdentifier) {
				ch = peekChar();
				if (ch == ' ' || ScannerHelper.isWhitespace(ch)) {
					pushIdentifier(true, false);
					pushParamName(false);
					this.index = this.scanner.currentPosition;
					valid = true;
				}
			}
			this.scanner.resetTo(this.tagSourceEnd+1, this.javadocEnd);
		}
		this.tagValue = TAG_OTHERS_VALUE; // tag is invalid, do not keep the parsed tag value
	}
	return valid;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#parseReference()
 */
protected boolean parseReference() throws InvalidInputException {
	boolean valid = super.parseReference();
	if (!valid) {
		this.scanner.resetTo(this.tagSourceEnd+1, this.javadocEnd);
		this.index = this.tagSourceEnd+1;
		this.tagValue = TAG_OTHERS_VALUE; // tag is invalid, do not keep the parsed tag value
	}
	return valid;
}

/*
 * Parse @return tag declaration
 */
protected boolean parseReturn() {
	createTag();
	return true;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#parseTag(int)
 */
protected boolean parseTag(int previousPosition) throws InvalidInputException {

	// Do not parse javadoc tag inside <pre>...</pre> tags
	if (this.htmlTagsPtr >= 0) {
		int ptr = this.htmlTagsPtr;
   		while (ptr >= 0) {
			if (getHtmlTagIndex(this.htmlTags[ptr--]) == JAVADOC_CODE_TAGS_ID) {
				if (this.textStart == -1) this.textStart = this.inlineTagStarted ? this.inlineTagStart : previousPosition;
				this.inlineTagStarted = false;
				return true;
			}
		}
	}

	// Read tag name
	int ptr = this.astPtr;
	this.tagSourceStart = previousPosition;
	this.scanner.startPosition = this.index;
	this.scanner.currentCharacter = readChar();
	switch (this.scanner.currentCharacter) {
		case ' ':
		case '*':
		case '}':
			// tag name is empty
			this.tagSourceEnd = previousPosition;
			if (this.textStart == -1) this.textStart = previousPosition;
			return true;
		default:
			if (ScannerHelper.isWhitespace(this.scanner.currentCharacter)) {
				// tag name is empty
				this.tagSourceEnd = previousPosition;
				if (this.textStart == -1) this.textStart = previousPosition;
				return true;
			}
			break;
	}
	int currentPosition = this.index;
	char currentChar = this.scanner.currentCharacter;
	while (currentChar != ' ' && currentChar != '*' && currentChar != '}' && !ScannerHelper.isWhitespace(currentChar)) {
		currentPosition = this.index;
		currentChar = readChar();
	}
	this.tagSourceEnd = currentPosition - 1;
	this.scanner.currentCharacter = currentChar;
	this.scanner.currentPosition = currentPosition;
	char[] tagName = this.scanner.getCurrentIdentifierSource();
	int length = tagName.length;
	this.index = this.tagSourceEnd+1;

	// Decide which parse to perform depending on tag name
	this.tagValue = TAG_OTHERS_VALUE;
	boolean valid = false;
	switch (tagName[0]) {
		case 'a':
			if (length == TAG_AUTHOR_LENGTH && CharOperation.equals(TAG_AUTHOR, tagName)) {
				this.tagValue = TAG_AUTHOR_VALUE;
			}
			break;
		case 'c':
			if (length == TAG_CATEGORY_LENGTH && CharOperation.equals(TAG_CATEGORY, tagName)) {
				this.tagValue = TAG_CATEGORY_VALUE;
				valid = parseIdentifierTag(false); // TODO (frederic) reconsider parameter value when @category will be significant in spec
			} else if (length == TAG_CODE_LENGTH && this.inlineTagStarted && CharOperation.equals(TAG_CODE, tagName)) {
				this.tagValue = TAG_CODE_VALUE;
			}
			break;
		case 'd':
			if (length == TAG_DEPRECATED_LENGTH && CharOperation.equals(TAG_DEPRECATED, tagName)) {
				this.deprecated = true;
				valid = true;
				this.tagValue = TAG_DEPRECATED_VALUE;
			} else if (length == TAG_DOC_ROOT_LENGTH && CharOperation.equals(TAG_DOC_ROOT, tagName)) {
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=227730
				// identify @docRoot tag as a base tag that does not expect any argument
				valid = true;
				this.tagValue = TAG_DOC_ROOT_VALUE;
			}
			break;
		case 'e':
			if (length == TAG_EXCEPTION_LENGTH && CharOperation.equals(TAG_EXCEPTION, tagName)) {
				this.tagValue = TAG_EXCEPTION_VALUE;
				valid = parseThrows();
			}
			break;
		case 'i':
			if (length == TAG_INHERITDOC_LENGTH && CharOperation.equals(TAG_INHERITDOC, tagName)) {
				if (this.reportProblems) {
					recordInheritedPosition((((long) this.tagSourceStart) << 32) + this.tagSourceEnd);
				}
				valid = true;
				this.tagValue = TAG_INHERITDOC_VALUE;
			}
			break;
		case 'l':
			if (length == TAG_LINK_LENGTH && CharOperation.equals(TAG_LINK, tagName)) {
				this.tagValue = TAG_LINK_VALUE;
				if (this.inlineTagStarted || (this.kind & COMPLETION_PARSER) != 0) {
					valid = parseReference();
				} else {
					// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53290
					// Cannot have @link outside inline comment
					valid = false;
					if (this.reportProblems) {
						this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
					}
				}
			} else if (length == TAG_LINKPLAIN_LENGTH && CharOperation.equals(TAG_LINKPLAIN, tagName)) {
				this.tagValue = TAG_LINKPLAIN_VALUE;
				if (this.inlineTagStarted) {
					valid = parseReference();
				} else {
					valid = false;
					if (this.reportProblems) {
						this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
					}
				}
			} else if (length == TAG_LITERAL_LENGTH && this.inlineTagStarted && CharOperation.equals(TAG_LITERAL, tagName)) {
				this.tagValue = TAG_LITERAL_VALUE;
			}
			break;
		case 'p':
			if (length == TAG_PARAM_LENGTH && CharOperation.equals(TAG_PARAM, tagName)) {
				this.tagValue = TAG_PARAM_VALUE;
				valid = parseParam();
			}
			break;
		case 's':
			if (length == TAG_SEE_LENGTH && CharOperation.equals(TAG_SEE, tagName)) {
				if (this.inlineTagStarted) {
					// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53290
					// Cannot have @see inside inline comment
					valid = false;
					if (this.reportProblems) {
						this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
					}
				} else {
					this.tagValue = TAG_SEE_VALUE;
					valid = parseReference();
				}
			} else if (length == TAG_SERIAL_LENGTH && CharOperation.equals(TAG_SERIAL, tagName)) {
				this.tagValue = TAG_SERIAL_VALUE;
			} else if (length == TAG_SERIAL_DATA_LENGTH && CharOperation.equals(TAG_SERIAL_DATA, tagName)) {
				this.tagValue = TAG_SERIAL_DATA_VALUE;
			} else if (length == TAG_SERIAL_FIELD_LENGTH && CharOperation.equals(TAG_SERIAL_FIELD, tagName)) {
				this.tagValue = TAG_SERIAL_FIELD_VALUE;
			} else if (length == TAG_SINCE_LENGTH && CharOperation.equals(TAG_SINCE, tagName)) {
				this.tagValue = TAG_SINCE_VALUE;
			}
			break;
		case 'v':
			if (length == TAG_VALUE_LENGTH && CharOperation.equals(TAG_VALUE, tagName)) {
				this.tagValue = TAG_VALUE_VALUE;
				if (this.sourceLevel >= ClassFileConstants.JDK1_5) {
					if (this.inlineTagStarted) {
						valid = parseReference();
					} else {
						valid = false;
						if (this.reportProblems) this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
					}
				}
			} else if (length == TAG_VERSION_LENGTH && CharOperation.equals(TAG_VERSION, tagName)) {
				this.tagValue = TAG_VERSION_VALUE;
			} else {
				createTag();
			}
			break;
		case 'r':
			if (length == TAG_RETURN_LENGTH && CharOperation.equals(TAG_RETURN, tagName)) {
				this.tagValue = TAG_RETURN_VALUE;
				valid = parseReturn();
			}
			break;
		case 't':
			if (length == TAG_THROWS_LENGTH && CharOperation.equals(TAG_THROWS, tagName)) {
				this.tagValue = TAG_THROWS_VALUE;
				valid = parseThrows();
			}
			break;
		default:
			createTag();
			break;
	}
	consumeToken();
	this.textStart = -1;

	// the javadoc parser may not create tag for some valid tags: force tag creation for such tag.
	if (valid) {
		switch (this.tagValue) {
			case TAG_INHERITDOC_VALUE:
			case TAG_DEPRECATED_VALUE:
				createTag();
				break;
		}
	} else if (this.invalidTagName) {
		this.textStart = previousPosition;
	} else if (this.astPtr == ptr) {
		createTag();
	}
	return true;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#parseThrows()
 */
protected boolean parseThrows() {
	boolean valid = super.parseThrows();
	if (!valid) {
		// If invalid, restart from the end tag position
		this.scanner.resetTo(this.tagSourceEnd+1, this.javadocEnd);
		this.index = this.tagSourceEnd+1;
		this.tagValue = TAG_OTHERS_VALUE; // tag is invalid, do not keep the parsed tag value
	}
	return valid;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#pushParamName(boolean)
 */
protected boolean pushParamName(boolean isTypeParam) {
	int lineTagStart = this.scanner.getLineNumber(this.tagSourceStart);
	FormatJavadocBlock block = new FormatJavadocBlock(this.tagSourceStart, this.tagSourceEnd, lineTagStart, TAG_PARAM_VALUE);
	int start = (int) (this.identifierPositionStack[0] >>> 32);
	int lineStart = this.scanner.getLineNumber(start);
	FormatJavadocReference reference;
	reference = new FormatJavadocReference(start, (int) this.identifierPositionStack[isTypeParam ? 2 : 0], lineStart);
	block.reference = reference;
	block.sourceEnd = reference.sourceEnd;
	pushOnAstStack(block, true);
	return true;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#pushSeeRef(java.lang.Object)
 */
protected boolean pushSeeRef(Object statement) {
	FormatJavadocReference reference = (FormatJavadocReference) statement;
	int lineTagStart = this.scanner.getLineNumber(this.tagSourceStart);
	FormatJavadocBlock block = new FormatJavadocBlock(this.tagSourceStart, this.tagSourceEnd, lineTagStart, this.tagValue);
	block.reference = reference;
	block.sourceEnd = reference.sourceEnd;
	if (this.inlineTagStarted) {
		block.sourceStart = this.inlineTagStart;
		FormatJavadocBlock previousBlock = null;
		if (this.astPtr == -1) {
			int lineStart = this.scanner.getLineNumber(this.inlineTagStart);
			previousBlock = new FormatJavadocBlock(this.inlineTagStart, this.tagSourceEnd, lineStart, NO_TAG_VALUE);
			previousBlock.sourceEnd = reference.sourceEnd;
			pushOnAstStack(previousBlock, true);
		} else {
			previousBlock = (FormatJavadocBlock) this.astStack[this.astPtr];
		}
		previousBlock.addBlock(block, this.htmlTagsPtr == -1 ? 0 : this.htmlTagsPtr);
		block.flags |= FormatJavadocBlock.INLINED;
	} else {
		pushOnAstStack(block, true);
	}

	return true;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushText(int, int)
 */
protected void pushText(int start, int end) {
	pushText(start, end, -1, this.htmlTagsPtr == -1 ? 0 : this.htmlTagsPtr);
}

private void pushText(int start, int end, int htmlIndex, int htmlDepth) {

	// Search previous tag on which to add the text element
	FormatJavadocBlock previousBlock = null;
	int previousStart = start;
	int lineStart = this.scanner.getLineNumber(start);
	if (this.astPtr == -1) {
		previousBlock = new FormatJavadocBlock(start, start, lineStart, NO_TAG_VALUE);
		pushOnAstStack(previousBlock, true);
	} else {
		previousBlock = (FormatJavadocBlock) this.astStack[this.astPtr];
		previousStart = previousBlock.sourceStart;
	}

	// If we're in a inline tag, then retrieve previous tag in its fragments
	if (this.inlineTagStarted) {
		if (previousBlock.nodes == null) {
			// no existing fragment => just add the element
		} else {
			// If last fragment is a tag, then use it as previous tag
			FormatJavadocNode lastNode = previousBlock.nodes[previousBlock.nodesPtr];
			while (lastNode != null && lastNode.isText()) {
				lastNode = lastNode.getLastNode();
			}
			if (lastNode != null) {
				previousBlock = (FormatJavadocBlock) lastNode;
				previousStart = previousBlock.sourceStart;
			}
		}
	}

	// Add the text
	FormatJavadocText text = new FormatJavadocText(start, end-1, lineStart, htmlIndex, htmlDepth==-1 ? 0 : htmlDepth);
	previousBlock.addText(text);
	previousBlock.sourceStart = previousStart;
	if (lineStart == previousBlock.lineStart) {
		previousBlock.flags |= FormatJavadocBlock.TEXT_ON_TAG_LINE;
	}
	this.textStart = -1;
}

/*
 * (non-Javadoc)
 *
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushThrowName(java.lang.Object)
 */
protected boolean pushThrowName(Object typeRef) {
	int lineStart = this.scanner.getLineNumber(this.tagSourceStart);
	FormatJavadocBlock block = new FormatJavadocBlock(this.tagSourceStart, this.tagSourceEnd, lineStart, this.tagValue);
	block.reference = (FormatJavadocReference) typeRef;
	block.sourceEnd = block.reference.sourceEnd;
	pushOnAstStack(block, true);
	return true;
}

/*
 * (non-Javadoc)
 * Will update the inline tag position (end position) once tag was fully parsed.
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#refreshInlineTagPosition(int)
 */
protected void refreshInlineTagPosition(int previousPosition) {
	if (this.astPtr != -1) {
		FormatJavadocNode previousBlock = (FormatJavadocNode) this.astStack[this.astPtr];
		if (this.inlineTagStarted) {
			FormatJavadocNode lastNode = previousBlock;
			while (lastNode != null) {
				lastNode.sourceEnd = previousPosition;
				lastNode = lastNode.getLastNode();
			}
		}
	}
}

/*
 * Store the html tags level when entering an inline tag in case a wrong sequence
 * of opening/closing tags is defined inside it. Then, when leaving the inline tag
 * the level is reset to the entering value and avoid to wrongly attach subsequent
 * html tags to node inside the inline tag last node...
 */
protected void setInlineTagStarted(boolean started) {
	super.setInlineTagStarted(started);
	if (started) {
		this.inlineHtmlTagsPtr = this.htmlTagsPtr;
	} else {
		if (this.htmlTagsPtr > this.inlineHtmlTagsPtr) {
			this.htmlTagsPtr = this.inlineHtmlTagsPtr;
		}
	}
}

public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("FormatterCommentParser\n"); //$NON-NLS-1$
	buffer.append(super.toString());
	return buffer.toString();
}

public String toDebugString() {
	if (this.docComment == null) {
		return "No javadoc!";	//$NON-NLS-1$
	}
	return ((FormatJavadoc)this.docComment).toDebugString(this.source);
}

/*
 * Add stored tag elements to associated comment.
 * Clean all blocks (i.e. resize arrays to avoid null slots)
 * Set extra information on block about line relative positions.
 */
protected void updateDocComment() {
	int length = this.astPtr + 1;
	FormatJavadoc formatJavadoc = new FormatJavadoc(this.javadocStart, this.javadocEnd, length);
	if (length > 0) {
		formatJavadoc.blocks = new FormatJavadocBlock[length];
		for (int i=0; i<length; i++) {
			FormatJavadocBlock block = (FormatJavadocBlock) this.astStack[i];
			block.clean();
			block.update(this.scanner);
			formatJavadoc.blocks[i] = block;
			if (i== 0) {
				block.flags |= FormatJavadocBlock.FIRST;
			}
		}
	}
	formatJavadoc.textStart = this.javadocTextStart;
	formatJavadoc.textEnd = this.javadocTextEnd;
	formatJavadoc.lineStart = this.scanner.getLineNumber(this.javadocTextStart);
	formatJavadoc.lineEnd = this.scanner.getLineNumber(this.javadocTextEnd);
	FormatJavadocBlock firstBlock = formatJavadoc.getFirstBlock();
	if (firstBlock != null) {
		firstBlock.setHeaderLine(formatJavadoc.lineStart);
	}
	this.docComment = formatJavadoc;
	if (DefaultCodeFormatter.DEBUG) {
		System.out.println(toDebugString());
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#verifyEndLine(int)
 */
protected boolean verifyEndLine(int textPosition) {
	// do not verify anything at end of line while formatting
	return true;
}

protected boolean verifySpaceOrEndComment() {
	// Don't care if there's no spaces after a reference...
	return true;
}
}
