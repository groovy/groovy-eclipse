/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * Internal parser used for decoding doc comments.
 *
 * @since 3.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class DocCommentParser extends AbstractCommentParser {

	private Javadoc docComment;
	private AST ast;

	DocCommentParser(AST ast, Scanner scanner, boolean check) {
		super(null);
		this.ast = ast;
		this.scanner = scanner;
		switch(this.ast.apiLevel()) {
			case AST.JLS2_INTERNAL :
				this.sourceLevel = ClassFileConstants.JDK1_3;
				break;
			case AST.JLS3_INTERNAL:
				this.sourceLevel = ClassFileConstants.JDK1_5;
				break;
			default:
				// AST.JLS4 for now
				this.sourceLevel = ClassFileConstants.JDK1_7;
		}
		this.checkDocComment = check;
		this.kind = DOM_PARSER | TEXT_PARSE;
	}

	/* (non-Javadoc)
	 * Returns true if tag @deprecated is present in annotation.
	 *
	 * If annotation checking is enabled, will also construct an Annotation node, which will be stored into Parser.annotation
	 * slot for being consumed later on.
	 */
	public Javadoc parse(int[] positions) {
		return parse(positions[0], positions[1]-positions[0]);
	}
	public Javadoc parse(int start, int length) {

		// Init
		this.source = this.scanner.source;
		this.lineEnds = this.scanner.lineEnds;
		this.docComment = new Javadoc(this.ast);

		// Parse
		if (this.checkDocComment) {
			this.javadocStart = start;
			this.javadocEnd = start+length-1;
			this.firstTagPosition = this.javadocStart;
			commentParse();
		}
		this.docComment.setSourceRange(start, length);
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			setComment(start, length);  // backward compatibility
		}
		return this.docComment;
	}

	/**
	 * Sets the comment starting at the given position and with the given length.
	 * <p>
	 * Note the only purpose of this method is to hide deprecated warnings.
	 * @deprecated mark deprecated to hide deprecated usage
	 */
	private void setComment(int start, int length) {
		this.docComment.setComment(new String(this.source, start, length));
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("javadoc: ").append(this.docComment).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(super.toString());
		return buffer.toString();
	}

	@Override
	protected Object createArgumentReference(char[] name, int dim, boolean isVarargs, Object typeRef, long[] dimPositions, long argNamePos) throws InvalidInputException {
		try {
			MethodRefParameter argument = this.ast.newMethodRefParameter();
			ASTNode node = (ASTNode) typeRef;
			int argStart = node.getStartPosition();
			int argEnd = node.getStartPosition()+node.getLength()-1;
			if (dim > 0) argEnd = (int) dimPositions[dim-1];
			if (argNamePos >= 0) argEnd = (int) argNamePos;
			if (name.length != 0) {
				final SimpleName argName = new SimpleName(this.ast);
				argName.internalSetIdentifier(new String(name));
				argument.setName(argName);
				int argNameStart = (int) (argNamePos >>> 32);
				argName.setSourceRange(argNameStart, argEnd-argNameStart+1);
			}
			Type argType = null;
			if (node.getNodeType() == ASTNode.PRIMITIVE_TYPE) {
				argType = (PrimitiveType) node;
			} else {
				Name argTypeName = (Name) node;
				argType = this.ast.newSimpleType(argTypeName);
				argType.setSourceRange(argStart, node.getLength());
			}
			if (dim > 0 && !isVarargs) {
				if (this.ast.apiLevel <= AST.JLS4_INTERNAL) {
					for (int i=0; i<dim; i++) {
						argType = this.ast.newArrayType(argType);
						argType.setSourceRange(argStart, ((int) dimPositions[i])-argStart+1);
					}
				} else {
					ArrayType argArrayType = this.ast.newArrayType(argType, 0);
					argType = argArrayType;
					argType.setSourceRange(argStart, ((int) dimPositions[dim-1])-argStart+1);
					for (int i=0; i<dim; i++) {
						Dimension dimension = this.ast.newDimension();
						int dimStart = (int) (dimPositions[i] >>> 32);
						int dimEnd = (int) dimPositions[i];
						dimension.setSourceRange(dimStart, dimEnd-dimStart+1);
						argArrayType.dimensions().add(dimension);
					}
				}
			}
			argument.setType(argType);
			if (this.ast.apiLevel > AST.JLS8_INTERNAL) {
				argument.setVarargs(isVarargs);
			}
			argument.setSourceRange(argStart, argEnd - argStart + 1);
			return argument;
		}
		catch (ClassCastException ex) {
				throw new InvalidInputException();
		}
	}

	@Override
	protected Object createFieldReference(Object receiver) throws InvalidInputException {
		try {
			MemberRef fieldRef = this.ast.newMemberRef();
			SimpleName fieldName = new SimpleName(this.ast);
			fieldName.internalSetIdentifier(new String(this.identifierStack[0]));
			fieldRef.setName(fieldName);
			int start = (int) (this.identifierPositionStack[0] >>> 32);
			int end = (int) this.identifierPositionStack[0];
			fieldName.setSourceRange(start, end - start + 1);
			if (receiver == null) {
				start = this.memberStart;
				fieldRef.setSourceRange(start, end - start + 1);
			} else {
				Name typeRef = (Name) receiver;
				fieldRef.setQualifier(typeRef);
				start = typeRef.getStartPosition();
				end = fieldName.getStartPosition()+fieldName.getLength()-1;
				fieldRef.setSourceRange(start, end-start+1);
			}
			return fieldRef;
		}
		catch (ClassCastException ex) {
				throw new InvalidInputException();
		}
	}

	@Override
	protected Object createMethodReference(Object receiver, List arguments) throws InvalidInputException {
		try {
			// Create method ref
			MethodRef methodRef = this.ast.newMethodRef();
			SimpleName methodName = new SimpleName(this.ast);
			int length = this.identifierLengthStack[0] - 1; // may be > 0 for member class constructor reference
			methodName.internalSetIdentifier(new String(this.identifierStack[length]));
			methodRef.setName(methodName);
			int start = (int) (this.identifierPositionStack[length] >>> 32);
			int end = (int) this.identifierPositionStack[length];
			methodName.setSourceRange(start, end - start + 1);
			// Set qualifier
			if (receiver == null) {
				start = this.memberStart;
				methodRef.setSourceRange(start, end - start + 1);
			} else {
				Name typeRef = (Name) receiver;
				methodRef.setQualifier(typeRef);
				start = typeRef.getStartPosition();
			}
			// Add arguments
			if (arguments != null) {
				Iterator parameters = arguments.listIterator();
				while (parameters.hasNext()) {
					MethodRefParameter param = (MethodRefParameter) parameters.next();
					methodRef.parameters().add(param);
				}
			}
			methodRef.setSourceRange(start, this.scanner.getCurrentTokenEndPosition()-start+1);
			return methodRef;
		}
		catch (ClassCastException ex) {
				throw new InvalidInputException();
		}
	}

	@Override
	protected void createTag() {
		TagElement tagElement = this.ast.newTagElement();
		int position = this.scanner.currentPosition;
		this.scanner.resetTo(this.tagSourceStart, this.tagSourceEnd);
		StringBuffer tagName = new StringBuffer();
		int start = this.tagSourceStart;
		this.scanner.getNextChar();
		while (this.scanner.currentPosition <= (this.tagSourceEnd+1)) {
			tagName.append(this.scanner.currentCharacter);
			this.scanner.getNextChar();
		}
		tagElement.setTagName(tagName.toString());
		if (this.inlineTagStarted) {
			start = this.inlineTagStart;
			TagElement previousTag = null;
			if (this.astPtr == -1) {
				previousTag = this.ast.newTagElement();
				previousTag.setSourceRange(start, this.tagSourceEnd-start+1);
				pushOnAstStack(previousTag, true);
			} else {
				previousTag = (TagElement) this.astStack[this.astPtr];
			}
			int previousStart = previousTag.getStartPosition();
			previousTag.fragments().add(tagElement);
			previousTag.setSourceRange(previousStart, this.tagSourceEnd-previousStart+1);
		} else {
			pushOnAstStack(tagElement, true);
		}
		tagElement.setSourceRange(start, this.tagSourceEnd-start+1);
		this.scanner.resetTo(position, this.javadocEnd);
	}

	@Override
	protected Object createTypeReference(int primitiveToken) {
		int size = this.identifierLengthStack[this.identifierLengthPtr];
		String[] identifiers = new String[size];
		int pos = this.identifierPtr - size + 1;
		for (int i = 0; i < size; i++) {
			identifiers[i] = new String(this.identifierStack[pos+i]);
		}
		ASTNode typeRef = null;
		if (primitiveToken == -1) {
			typeRef = this.ast.internalNewName(identifiers);
		} else {
			switch (primitiveToken) {
				case TerminalTokens.TokenNamevoid :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.VOID);
					break;
				case TerminalTokens.TokenNameboolean :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.BOOLEAN);
					break;
				case TerminalTokens.TokenNamebyte :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.BYTE);
					break;
				case TerminalTokens.TokenNamechar :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.CHAR);
					break;
				case TerminalTokens.TokenNamedouble :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.DOUBLE);
					break;
				case TerminalTokens.TokenNamefloat :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.FLOAT);
					break;
				case TerminalTokens.TokenNameint :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.INT);
					break;
				case TerminalTokens.TokenNamelong :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.LONG);
					break;
				case TerminalTokens.TokenNameshort :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.SHORT);
					break;
				default:
					// should not happen
					return null;
			}
		}
		// Update ref for whole name
		int start = (int) (this.identifierPositionStack[pos] >>> 32);
//		int end = (int) this.identifierPositionStack[this.identifierPtr];
//		typeRef.setSourceRange(start, end-start+1);
		// Update references of each simple name
		if (size > 1) {
			Name name = (Name)typeRef;
			int nameIndex = size;
			for (int i=this.identifierPtr; i>pos; i--, nameIndex--) {
				int s = (int) (this.identifierPositionStack[i] >>> 32);
				int e = (int) this.identifierPositionStack[i];
				name.index = nameIndex;
				SimpleName simpleName = ((QualifiedName)name).getName();
				simpleName.index = nameIndex;
				simpleName.setSourceRange(s, e-s+1);
				name.setSourceRange(start, e-start+1);
				name =  ((QualifiedName)name).getQualifier();
			}
			int end = (int) this.identifierPositionStack[pos];
			name.setSourceRange(start, end-start+1);
			name.index = nameIndex;
		} else {
			int end = (int) this.identifierPositionStack[pos];
			typeRef.setSourceRange(start, end-start+1);
		}
		return typeRef;
	}

	@Override
	protected boolean parseIdentifierTag(boolean report) {
		if (super.parseIdentifierTag(report)) {
			createTag();
			this.index = this.tagSourceEnd+1;
			this.scanner.resetTo(this.index, this.javadocEnd);
			return true;
		}
		return false;
	}

	/*
	 * Parse @return tag declaration
	 */
	protected boolean parseReturn() {
		createTag();
		return true;
	}

	@Override
	protected boolean parseTag(int previousPosition) throws InvalidInputException {

		// Read tag name
		int currentPosition = this.index;
		int token = readTokenAndConsume();
		char[] tagName = CharOperation.NO_CHAR;
		if (currentPosition == this.scanner.startPosition) {
			this.tagSourceStart = this.scanner.getCurrentTokenStartPosition();
			this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
			tagName = this.scanner.getCurrentIdentifierSource();
		} else {
			this.tagSourceEnd = currentPosition-1;
		}

		// Try to get tag name other than java identifier
		// (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51660)
		if (this.scanner.currentCharacter != ' ' && !ScannerHelper.isWhitespace(this.scanner.currentCharacter)) {
			tagNameToken: while (token != TerminalTokens.TokenNameEOF && this.index < this.scanner.eofPosition) {
				int length = tagName.length;
				// !, ", #, %, &, ', -, :, <, >, * chars and spaces are not allowed in tag names
				switch (this.scanner.currentCharacter) {
					case '}':
					case '*': // break for '*' as this is perhaps the end of comment (bug 65288)
					case '!':
					case '#':
					case '%':
					case '&':
					case '\'':
					case '"':
					case ':':
					case '<':
					case '>':
						break tagNameToken;
					case '-': // allowed in tag names as this character is often used in doclets (bug 68087)
						System.arraycopy(tagName, 0, tagName = new char[length+1], 0, length);
						tagName[length] = this.scanner.currentCharacter;
						break;
					default:
						if (this.scanner.currentCharacter == ' ' || ScannerHelper.isWhitespace(this.scanner.currentCharacter)) {
							break tagNameToken;
						}
						token = readTokenAndConsume();
						char[] ident = this.scanner.getCurrentIdentifierSource();
						System.arraycopy(tagName, 0, tagName = new char[length+ident.length], 0, length);
						System.arraycopy(ident, 0, tagName, length, ident.length);
						break;
				}
				this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
				this.scanner.getNextChar();
				this.index = this.scanner.currentPosition;
			}
		}
		int length = tagName.length;
		this.index = this.tagSourceEnd+1;
		this.scanner.currentPosition = this.tagSourceEnd+1;
		this.tagSourceStart = previousPosition;

		// tage name may be empty (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=125903)
		if (tagName.length == 0) {
			return false;
		}

		// Decide which parse to perform depending on tag name
		this.tagValue = NO_TAG_VALUE;
		boolean valid = true;
		switch (token) {
			case TerminalTokens.TokenNameIdentifier :
				switch (tagName[0]) {
					case 'c':
						if (length == TAG_CATEGORY_LENGTH && CharOperation.equals(TAG_CATEGORY, tagName)) {
							this.tagValue = TAG_CATEGORY_VALUE;
							valid = parseIdentifierTag(false); // TODO (frederic) reconsider parameter value when @category will be significant in spec
						} else if (length == TAG_CODE_LENGTH && CharOperation.equals(TAG_CODE, tagName)) {
							this.tagValue = TAG_CODE_VALUE;
							createTag();
						} else {
							this.tagValue = TAG_OTHERS_VALUE;
							createTag();
						}
						break;
					case 'd':
						if (length == TAG_DEPRECATED_LENGTH && CharOperation.equals(TAG_DEPRECATED, tagName)) {
							this.deprecated = true;
							this.tagValue = TAG_DEPRECATED_VALUE;
						} else {
							this.tagValue = TAG_OTHERS_VALUE;
						}
						createTag();
					break;
					case 'i':
						if (length == TAG_INHERITDOC_LENGTH && CharOperation.equals(TAG_INHERITDOC, tagName)) {
							if (this.reportProblems) {
								recordInheritedPosition((((long) this.tagSourceStart) << 32) + this.tagSourceEnd);
							}
							this.tagValue = TAG_INHERITDOC_VALUE;
						} else {
							this.tagValue = TAG_OTHERS_VALUE;
						}
						createTag();
					break;
					case 'p':
						if (length == TAG_PARAM_LENGTH && CharOperation.equals(TAG_PARAM, tagName)) {
							this.tagValue = TAG_PARAM_VALUE;
							valid = parseParam();
						} else {
							this.tagValue = TAG_OTHERS_VALUE;
							createTag();
						}
					break;
					case 'e':
						if (length == TAG_EXCEPTION_LENGTH && CharOperation.equals(TAG_EXCEPTION, tagName)) {
							this.tagValue = TAG_EXCEPTION_VALUE;
							valid = parseThrows();
						} else {
							this.tagValue = TAG_OTHERS_VALUE;
							createTag();
						}
					break;
					case 's':
						if (length == TAG_SEE_LENGTH && CharOperation.equals(TAG_SEE, tagName)) {
							this.tagValue = TAG_SEE_VALUE;
							if (this.inlineTagStarted) {
								// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53290
								// Cannot have @see inside inline comment
								valid = false;
							} else {
								valid = parseReference();
							}
						} else {
							this.tagValue = TAG_OTHERS_VALUE;
							createTag();
						}
					break;
					case 'l':
						if (length == TAG_LINK_LENGTH && CharOperation.equals(TAG_LINK, tagName)) {
							this.tagValue = TAG_LINK_VALUE;
						} else if (length == TAG_LINKPLAIN_LENGTH && CharOperation.equals(TAG_LINKPLAIN, tagName)) {
							this.tagValue = TAG_LINKPLAIN_VALUE;
						} else if (length == TAG_LITERAL_LENGTH && CharOperation.equals(TAG_LITERAL, tagName)) {
							this.tagValue = TAG_LITERAL_VALUE;
						}
						
						if (this.tagValue != NO_TAG_VALUE && this.tagValue != TAG_LITERAL_VALUE)  {
							if (this.inlineTagStarted) {
								valid = parseReference();
							} else {
								// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53290
								// Cannot have @link outside inline comment
								valid = false;
							}
						} else {
							if (this.tagValue == NO_TAG_VALUE) this.tagValue = TAG_OTHERS_VALUE;
							createTag();
						}
					break;
					case 'v':
						if (this.sourceLevel >= ClassFileConstants.JDK1_5 && length == TAG_VALUE_LENGTH && CharOperation.equals(TAG_VALUE, tagName)) {
							this.tagValue = TAG_VALUE_VALUE;
							if (this.inlineTagStarted) {
								valid = parseReference();
							} else {
								valid = false;
							}
						} else {
							this.tagValue = TAG_OTHERS_VALUE;
							createTag();
						}
					break;
					default:
						this.tagValue = TAG_OTHERS_VALUE;
						createTag();
				}
				break;
			case TerminalTokens.TokenNamereturn :
				this.tagValue = TAG_RETURN_VALUE;
				valid = parseReturn();
				break;
			case TerminalTokens.TokenNamethrows :
				this.tagValue = TAG_THROWS_VALUE;
				valid = parseThrows();
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
			case TerminalTokens.TokenNamenull:
			case TerminalTokens.TokenNamepackage:
			case TerminalTokens.TokenNameprivate:
			case TerminalTokens.TokenNameprotected:
			case TerminalTokens.TokenNamepublic:
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
			case TerminalTokens.TokenNameenum :
			case TerminalTokens.TokenNameconst :
			case TerminalTokens.TokenNamegoto :
				this.tagValue = TAG_OTHERS_VALUE;
				createTag();
				break;
		}
		this.textStart = this.index;
		return valid;
	}

	@Override
	protected boolean pushParamName(boolean isTypeParam) {
		int idIndex = isTypeParam ? 1 : 0;
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(this.identifierStack[idIndex]));
		int nameStart = (int) (this.identifierPositionStack[idIndex] >>> 32);
		int nameEnd = (int) (this.identifierPositionStack[idIndex] & 0x00000000FFFFFFFFL);
		name.setSourceRange(nameStart, nameEnd-nameStart+1);
		TagElement paramTag = this.ast.newTagElement();
		paramTag.setTagName(TagElement.TAG_PARAM);
		if (isTypeParam) { // specific storage for @param <E> (see bug 79809)
			// '<' was stored in identifiers stack
			TextElement text = this.ast.newTextElement();
			text.setText(new String(this.identifierStack[0]));
			int txtStart = (int) (this.identifierPositionStack[0] >>> 32);
			int txtEnd = (int) (this.identifierPositionStack[0] & 0x00000000FFFFFFFFL);
			text.setSourceRange(txtStart, txtEnd-txtStart+1);
			paramTag.fragments().add(text);
			// add simple name
			paramTag.fragments().add(name);
			// '>' was stored in identifiers stack
			text = this.ast.newTextElement();
			text.setText(new String(this.identifierStack[2]));
			txtStart = (int) (this.identifierPositionStack[2] >>> 32);
			txtEnd = (int) (this.identifierPositionStack[2] & 0x00000000FFFFFFFFL);
			text.setSourceRange(txtStart, txtEnd-txtStart+1);
			paramTag.fragments().add(text);
			// set param tag source range
			paramTag.setSourceRange(this.tagSourceStart, txtEnd-this.tagSourceStart+1);
		} else {
			paramTag.setSourceRange(this.tagSourceStart, nameEnd-this.tagSourceStart+1);
			paramTag.fragments().add(name);
		}
		pushOnAstStack(paramTag, true);
		return true;
	}

	@Override
	protected boolean pushSeeRef(Object statement) {
		TagElement seeTag = this.ast.newTagElement();
		ASTNode node = (ASTNode) statement;
		seeTag.fragments().add(node);
		int end = node.getStartPosition()+node.getLength()-1;
		if (this.inlineTagStarted) {
			seeTag.setSourceRange(this.inlineTagStart, end-this.inlineTagStart+1);
			switch (this.tagValue) {
				case TAG_LINK_VALUE:
					seeTag.setTagName(TagElement.TAG_LINK);
				break;
				case TAG_LINKPLAIN_VALUE:
					seeTag.setTagName(TagElement.TAG_LINKPLAIN);
				break;
				case TAG_VALUE_VALUE:
					seeTag.setTagName(TagElement.TAG_VALUE);
				break;
			}
			TagElement previousTag = null;
			int previousStart = this.inlineTagStart;
			if (this.astPtr == -1) {
				previousTag = this.ast.newTagElement();
				pushOnAstStack(previousTag, true);
			} else {
				previousTag = (TagElement) this.astStack[this.astPtr];
				previousStart = previousTag.getStartPosition();
			}
			previousTag.fragments().add(seeTag);
			previousTag.setSourceRange(previousStart, end-previousStart+1);
		} else {
			seeTag.setTagName(TagElement.TAG_SEE);
			seeTag.setSourceRange(this.tagSourceStart, end-this.tagSourceStart+1);
			pushOnAstStack(seeTag, true);
		}
		return true;
	}

	@Override
	protected void pushText(int start, int end) {

		// Create text element
		TextElement text = this.ast.newTextElement();
		text.setText(new String( this.source, start, end-start));
		text.setSourceRange(start, end-start);

		// Search previous tag on which to add the text element
		TagElement previousTag = null;
		int previousStart = start;
		if (this.astPtr == -1) {
			previousTag = this.ast.newTagElement();
			previousTag.setSourceRange(start, end-start);
			pushOnAstStack(previousTag, true);
		} else {
			previousTag = (TagElement) this.astStack[this.astPtr];
			previousStart = previousTag.getStartPosition();
		}

		// If we're in a inline tag, then retrieve previous tag in its fragments
		List fragments = previousTag.fragments();
		if (this.inlineTagStarted) {
			int size = fragments.size();
			if (size == 0) {
				// no existing fragment => just add the element
				TagElement inlineTag = this.ast.newTagElement();
				fragments.add(inlineTag);
				previousTag = inlineTag;
			} else {
				// If last fragment is a tag, then use it as previous tag
				ASTNode lastFragment = (ASTNode) fragments.get(size-1);
				if (lastFragment.getNodeType() == ASTNode.TAG_ELEMENT) {
					previousTag = (TagElement) lastFragment;
					previousStart = previousTag.getStartPosition();
				}
			}
		}

		// Add the text
		previousTag.fragments().add(text);
		previousTag.setSourceRange(previousStart, end-previousStart);
		this.textStart = -1;
	}

	@Override
	protected boolean pushThrowName(Object typeRef) {
		TagElement throwsTag = this.ast.newTagElement();
		switch (this.tagValue) {
			case TAG_THROWS_VALUE:
				throwsTag.setTagName(TagElement.TAG_THROWS);
			break;
			case TAG_EXCEPTION_VALUE:
				throwsTag.setTagName(TagElement.TAG_EXCEPTION);
			break;
		}
		throwsTag.setSourceRange(this.tagSourceStart, this.scanner.getCurrentTokenEndPosition()-this.tagSourceStart+1);
		throwsTag.fragments().add(typeRef);
		pushOnAstStack(throwsTag, true);
		return true;
	}

	@Override
	protected void refreshInlineTagPosition(int previousPosition) {
		if (this.astPtr != -1) {
			TagElement previousTag = (TagElement) this.astStack[this.astPtr];
			if (this.inlineTagStarted) {
				int previousStart = previousTag.getStartPosition();
				previousTag.setSourceRange(previousStart, previousPosition-previousStart+1);
				if (previousTag.fragments().size() > 0) {
					ASTNode inlineTag = (ASTNode) previousTag.fragments().get(previousTag.fragments().size()-1);
					if (inlineTag.getNodeType() == ASTNode.TAG_ELEMENT) {
						int inlineStart = inlineTag.getStartPosition();
						inlineTag.setSourceRange(inlineStart, previousPosition-inlineStart+1);
					}
				}
			}
		}
	}

	/*
	 * Add stored tag elements to associated comment.
	 */
	@Override
	protected void updateDocComment() {
		for (int idx = 0; idx <= this.astPtr; idx++) {
			this.docComment.tags().add(this.astStack[idx]);
		}
	}
}
