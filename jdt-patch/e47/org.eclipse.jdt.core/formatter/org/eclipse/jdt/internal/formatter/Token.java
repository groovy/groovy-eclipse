/*******************************************************************************
 * Copyright (c) 2014, 2015 Mateusz Matela and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *     Till Brychcy - Java Code Formatter breaks code if single line comments contain unicode escape - https://bugs.eclipse.org/471090
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_BLOCK;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_JAVADOC;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_LINE;

import java.util.List;

import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * Stores a token's type, position and all its properties like surrounding whitespace, wrapping behavior and so on.
 */
public class Token {

	public static enum WrapMode {
		/**
		 * Wrap mode for the "Do not wrap" policy. Tokens still should be indented as if wrapped when a preceding line
		 * break cannot be removed due to a line comment or formatting region restriction.
		 */
		DISABLED,
		/** Wrap mode for the "Wrap where necessary" policies. */
		WHERE_NECESSARY,
		/** Wrap mode for the "Wrap all elements" policies. */
		TOP_PRIORITY,
		/** Wrap mode for tokens that must be wrapped due to "Force wrap" setting. */
		FORCE,
		/**
		 * Wrap mode used for lines in anonymous class and lambda body. Means that tokens that are already in new line
		 * before wrapping, but their indentation should be adjusted in similar way to wrapping.
		 */
		BLOCK_INDENT
	}

	public static class WrapPolicy {

		/** Policy used to mark tokens that should never be wrapped */
		public final static WrapPolicy DISABLE_WRAP = new WrapPolicy(WrapMode.DISABLED, 0, 0);

		/**
		 * Policy used for internal structure of multiline comments to mark tokens that can be wrapped only in lines
		 * that have no other tokens to wrap.
		 */
		public final static WrapPolicy SUBSTITUTE_ONLY = new WrapPolicy(WrapMode.DISABLED, 0, 0);

		public final WrapMode wrapMode;
		public final int wrapParentIndex;
		public final int groupEndIndex;
		public final int extraIndent;
		public final int structureDepth;
		public final float penaltyMultiplier;
		public final boolean isFirstInGroup;
		public final boolean indentOnColumn;

		public WrapPolicy(WrapMode wrapMode, int wrapParentIndex, int groupEndIndex, int extraIndent,
				int structureDepth, float penaltyMultiplier, boolean isFirstInGroup, boolean indentOnColumn) {
			assert wrapMode != null && (wrapParentIndex < groupEndIndex || groupEndIndex == -1);

			this.wrapMode = wrapMode;
			this.wrapParentIndex = wrapParentIndex;
			this.groupEndIndex = groupEndIndex;
			this.extraIndent = extraIndent;
			this.structureDepth = structureDepth;
			this.penaltyMultiplier = penaltyMultiplier;
			this.isFirstInGroup = isFirstInGroup;
			this.indentOnColumn = indentOnColumn;
		}

		public WrapPolicy(WrapMode wrapMode, int wrapParentIndex, int extraIndent) {
			this(wrapMode, wrapParentIndex, -1, extraIndent, 0, 1, false, false);
		}
	}

	/** Position in source of the first character. */
	public final int originalStart;
	/** Position in source of the last character (this position is included in the token). */
	public final int originalEnd;
	/** Type of this token. See {@link TerminalTokens} for constants definition. */
	public final int tokenType;
	private boolean spaceBefore, spaceAfter;
	private int lineBreaksBefore, lineBreaksAfter;
	private boolean wrapped;
	private int indent;
	private int emptyLineIndentAdjustment;
	private int align;
	private boolean toEscape;

	private boolean nextLineOnWrap;
	private WrapPolicy wrapPolicy;

	private Token nlsTagToken;

	private List<Token> internalStructure;

	public Token(int sourceStart, int sourceEnd, int tokenType) {
		assert sourceStart <= sourceEnd;
		this.originalStart = sourceStart;
		this.originalEnd = sourceEnd;
		this.tokenType = tokenType;
	}

	public Token(Token tokenToCopy) {
		this(tokenToCopy, tokenToCopy.originalStart, tokenToCopy.originalEnd, tokenToCopy.tokenType);
	}

	public Token(Token tokenToCopy, int newOriginalStart, int newOriginalEnd, int newTokenType) {
		this.originalStart = newOriginalStart;
		this.originalEnd = newOriginalEnd;
		this.tokenType = newTokenType;
		this.spaceBefore = tokenToCopy.spaceBefore;
		this.spaceAfter = tokenToCopy.spaceAfter;
		this.lineBreaksBefore = tokenToCopy.lineBreaksBefore;
		this.lineBreaksAfter = tokenToCopy.lineBreaksAfter;
		this.indent = tokenToCopy.indent;
		this.nextLineOnWrap = tokenToCopy.nextLineOnWrap;
		this.wrapPolicy = tokenToCopy.wrapPolicy;
		this.nlsTagToken = tokenToCopy.nlsTagToken;
		this.internalStructure = tokenToCopy.internalStructure;
	}

	public static Token fromCurrent(Scanner scanner, int currentToken) {
		int start = scanner.getCurrentTokenStartPosition();
		int end = scanner.getCurrentTokenEndPosition();
		if (currentToken == TokenNameCOMMENT_LINE) {
			// don't include line separator
			while(end >= start) {
				char c = scanner.source[end];
				if (c != '\r' && c != '\n')
					break;
				end--;
			}
		}
		Token token = new Token(start, end, currentToken);
		return token;
	}

	/** Adds space before this token */
	public void spaceBefore() {
		this.spaceBefore = true;
	}

	/** Removes space before this token */
	public void clearSpaceBefore() {
		this.spaceBefore = false;
	}

	public boolean isSpaceBefore() {
		return this.spaceBefore;
	}

	/** Adds space after this token */
	public void spaceAfter() {
		this.spaceAfter = true;
	}

	/** Removes space after this token */
	public void clearSpaceAfter() {
		this.spaceAfter = false;
	}

	public boolean isSpaceAfter() {
		return this.spaceAfter;
	}

	public void breakBefore() {
		putLineBreaksBefore(1);
	}

	public void putLineBreaksBefore(int lineBreaks) {
		this.lineBreaksBefore = Math.max(this.lineBreaksBefore, lineBreaks);
	}

	public int getLineBreaksBefore() {
		return this.wrapped ? 1 : this.lineBreaksBefore;
	}

	/** Can be used to temporarily force preceding line break without losing the original number of line breaks. */
	public void setWrapped(boolean wrapped) {
		this.wrapped = wrapped;
	}

	public void clearLineBreaksBefore() {
		this.lineBreaksBefore = 0;
	}

	public void breakAfter() {
		putLineBreaksAfter(1);
	}

	public void putLineBreaksAfter(int lineBreaks) {
		this.lineBreaksAfter = Math.max(this.lineBreaksAfter, lineBreaks);
	}

	public int getLineBreaksAfter() {
		return this.lineBreaksAfter;
	}

	public void clearLineBreaksAfter() {
		this.lineBreaksAfter = 0;
	}

	/** Increases this token's indentation by one position */
	public void indent() {
		this.indent++;
	}

	/** Decreses this token's indentation by one position */
	public void unindent() {
		this.indent--;
	}

	public void setIndent(int indent) {
		this.indent = indent;
	}

	public int getIndent() {
		return this.indent;
	}

	public void setEmptyLineIndentAdjustment(int adjustment) {
		this.emptyLineIndentAdjustment = adjustment;
	}

	public int getEmptyLineIndentAdjustment() {
		return this.emptyLineIndentAdjustment;
	}

	public void setAlign(int align) {
		this.align = align;
	}

	public int getAlign() {
		return this.align;
	}

	public void setToEscape(boolean shouldEscape) {
		this.toEscape = shouldEscape;
	}

	public boolean isToEscape() {
		return this.toEscape;
	}

	public void setNextLineOnWrap() {
		this.nextLineOnWrap = true;
	}

	public boolean isNextLineOnWrap() {
		return this.nextLineOnWrap;
	}

	public void setWrapPolicy(WrapPolicy wrapPolicy) {
		this.wrapPolicy = wrapPolicy;
	}

	public WrapPolicy getWrapPolicy() {
		return this.wrapPolicy;
	}

	public boolean isWrappable() {
		WrapPolicy wp = this.wrapPolicy;
		return wp != null && wp.wrapMode != WrapMode.DISABLED && wp.wrapMode != WrapMode.BLOCK_INDENT;
	}

	public void setNLSTag(Token nlsTagToken) {
		this.nlsTagToken = nlsTagToken;
	}

	public boolean hasNLSTag() {
		return this.nlsTagToken != null;
	}

	public Token getNLSTag() {
		return this.nlsTagToken;
	}

	public void setInternalStructure(List<Token> internalStructure) {
		this.internalStructure = internalStructure;
	}

	public List<Token> getInternalStructure() {
		return this.internalStructure;
	}

	public boolean isComment() {
		switch (this.tokenType) {
			case TokenNameCOMMENT_BLOCK:
			case TokenNameCOMMENT_JAVADOC:
			case TokenNameCOMMENT_LINE:
				return true;
		}
		return false;
	}

	public String toString(String source) {
		return source.substring(this.originalStart, this.originalEnd + 1);
	}

	public int countChars() {
		return this.originalEnd - this.originalStart + 1;
	}

	/*
	 * Conceptually, Token abstracts away from the source so it doesn't need to know how
	 * the source looks like. However, it's useful to see the actual token contents while debugging.
	 * Uncomment this field, commented code in toString() below and in DefaultCodeFormatter.init(String source)
	 * during debugging sessions to easily recognize tokens.
	 */
//	public static String source;

	public String toString() {
//		if (source != null)  // see comment above
//			return toString(source);
		return "[" + this.originalStart + "-" + this.originalEnd + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
