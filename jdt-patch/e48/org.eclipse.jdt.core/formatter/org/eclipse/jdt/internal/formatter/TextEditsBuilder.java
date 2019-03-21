/*******************************************************************************
 * Copyright (c) 2014, 2018 Mateusz Matela and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_LINE;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_BLOCK;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_JAVADOC;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameNotAToken;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameStringLiteral;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameWHITESPACE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.formatter.Token.WrapMode;
import org.eclipse.jdt.internal.formatter.Token.WrapPolicy;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Creates the formatter's result TextEdit by scanning through the tokens and comparing them with the original source.
 */
public class TextEditsBuilder extends TokenTraverser {

	private final String source;
	private TokenManager tm;
	private final DefaultCodeFormatterOptions options;
	private final StringBuilder buffer;

	private final List<Token> stringLiteralsInLine = new ArrayList<Token>();
	private final List<TextEdit> edits = new ArrayList<TextEdit>();

	private final List<IRegion> regions;
	private int currentRegion = 0;

	private TextEditsBuilder childBuilder;
	private final TextEditsBuilder parent;
	private int alignChar;
	private int sourceLimit;
	private int parentTokenIndex;

	public TextEditsBuilder(String source, List<IRegion> regions, TokenManager tokenManager,
			DefaultCodeFormatterOptions options) {
		this.source = source;
		this.tm = tokenManager;
		this.options = options;
		this.regions = adaptRegions(regions);

		this.alignChar = this.options.tab_char;
		this.sourceLimit = source.length();
		this.parent = null;

		this.buffer = new StringBuilder();
	}

	private TextEditsBuilder(TextEditsBuilder parent) {
		this.buffer = parent.buffer;
		this.parent = parent;
		this.source = parent.source;
		this.options = parent.options;
		this.regions = parent.regions;
		this.alignChar = DefaultCodeFormatterOptions.SPACE;
	}

	private List<IRegion> adaptRegions(List<IRegion> givenRegions) {
		// make sure regions don't begin or end inside multiline comments
		ArrayList<IRegion> result = new ArrayList<IRegion>();
		IRegion previous = null;
		for (IRegion region : givenRegions) {
			int start = region.getOffset();
			int end = start + region.getLength() - 1;
			int sourceStart = this.tm.get(0).originalStart;

			if (start > sourceStart) {
				Token token = this.tm.get(this.tm.findIndex(start, -1, false));
				if ((token.tokenType == TokenNameCOMMENT_BLOCK || token.tokenType == TokenNameCOMMENT_JAVADOC)
						&& start <= token.originalEnd) {
					start = token.originalStart;
				}
			}

			if (end > start && end > sourceStart) {
				Token token = this.tm.get(this.tm.findIndex(end, -1, false));
				if ((token.tokenType == TokenNameCOMMENT_BLOCK || token.tokenType == TokenNameCOMMENT_JAVADOC)
						&& end < token.originalEnd) {
					end = token.originalEnd;
				}
			}

			if (previous != null && previous.getOffset() + previous.getLength() >= start) {
				result.remove(result.size() - 1);
				start = previous.getOffset();
			}
			if (end + 1 == this.source.length())
				end++;
			IRegion adapted = new Region(start, end - start + 1);
			result.add(adapted);
			previous = adapted;
		}
		return result;
	}

	@Override
	protected boolean token(Token token, int index) {

		bufferWhitespaceBefore(token, index);

		List<Token> structure = token.getInternalStructure();
		if (token.tokenType == TokenNameCOMMENT_LINE) {
			handleSingleLineComment(token, index);
		} else if (structure != null && !structure.isEmpty()) {
			handleMultiLineComment(token, index);
		} else {
			flushBuffer(token.originalStart);
			if (token.isToEscape()) {
				this.buffer.append(this.tm.toString(token));
				flushBuffer(token.originalEnd + 1);
			} else {
				this.counter = token.originalEnd + 1;
			}
		}

		if (token.tokenType == TokenNameStringLiteral)
			this.stringLiteralsInLine.add(token);

		if (getNext() == null) {
			for (int i = 0; i < token.getLineBreaksAfter(); i++)
				bufferLineSeparator(null, i + 1 == token.getLineBreaksAfter());
			char lastChar = this.source.charAt(this.sourceLimit - 1);
			if (token.getLineBreaksAfter() == 0 && (lastChar == '\r' || lastChar == '\n'))
				bufferLineSeparator(null, false);
			flushBuffer(this.sourceLimit);
		}
		return true;
	}

	private void bufferWhitespaceBefore(Token token, int index) {
		if (getLineBreaksBefore() > 0) {
			this.stringLiteralsInLine.clear();
			if (getLineBreaksBefore() > 1) {
				Token indentToken = null;
				if (this.options.indent_empty_lines && token.tokenType != TokenNameNotAToken) {
					if (index == 0) {
						indentToken = token;
					} else {
						boolean isBlockIndent = token.getWrapPolicy() != null
								&& token.getWrapPolicy().wrapMode == WrapMode.BLOCK_INDENT;
						Token previous = this.tm.get(this.tm.findFirstTokenInLine(index - 1, true, !isBlockIndent));
						indentToken = (token.getIndent() > previous.getIndent()) ? token : previous;
					}
				}
				for (int i = 1; i < getLineBreaksBefore(); i++) {
					bufferLineSeparator(token, true);
					if (indentToken != null)
						bufferIndent(indentToken, index);
				}
			}
			bufferLineSeparator(token, false);
			bufferAlign(token, index);
			bufferIndent(token, index);
		} else if (index == 0 && this.parent == null) {
			bufferIndent(token, index);
		} else {
			if (!bufferAlign(token, index) && isSpaceBefore())
				this.buffer.append(' ');
		}
	}

	private void bufferLineSeparator(Token token, boolean emptyLine) {
		if (this.parent == null) {
			this.buffer.append(this.options.line_separator);
			return;
		}

		this.parent.counter = this.counter;
		this.parent.bufferLineSeparator(null, false);
		this.parent.bufferIndent(this.parent.tm.get(this.parentTokenIndex), this.parentTokenIndex);
		this.counter = this.parent.counter;

		if (token != null && token.tokenType == TokenNameNotAToken)
			return; // this is an unformatted block comment, don't force asterisk

		if (getNext() == null && !emptyLine)
			return; // this is the last token of block comment, asterisk is included

		boolean asteriskFound = false;
		int searchLimit = token != null ? token.originalStart : this.sourceLimit;
		for (int i = this.counter; i < searchLimit; i++) {
			char c = this.source.charAt(i);
			if (c == '*') {
				this.buffer.append(' ');
				flushBuffer(i);
				while (i + 1 < this.sourceLimit && this.source.charAt(i + 1) == '*')
					i++;
				this.counter = i + 1;
				c = this.source.charAt(i + 1);
				if ((c != '\r' && c != '\n') || !emptyLine)
					this.buffer.append(' ');
				asteriskFound = true;
				break;
			}
			if (!ScannerHelper.isWhitespace(c))
				break;
		}
		if (!asteriskFound)
			this.buffer.append(" * "); //$NON-NLS-1$
	}

	private void bufferIndent(Token token, int index) {
		int indent = token.getIndent();
		if (getCurrent() != null && getCurrent() != token)
			indent += getCurrent().getEmptyLineIndentAdjustment();
		int spaces = 0;
		if (this.options.use_tabs_only_for_leading_indentations
				&& this.options.tab_char != DefaultCodeFormatterOptions.SPACE) {
			WrapPolicy wrapPolicy = token.getWrapPolicy();
			boolean isWrappedBlockComment = this.childBuilder != null && this.childBuilder.parentTokenIndex == index;
			if (isWrappedBlockComment) {
				Token lineStart = this.tm.get(this.tm.findFirstTokenInLine(index));
				spaces = token.getIndent() - lineStart.getIndent();
				token = lineStart;
				wrapPolicy = token.getWrapPolicy();
			}
			while (wrapPolicy != null) {
				Token parentLineStart = this.tm.get(this.tm.findFirstTokenInLine(wrapPolicy.wrapParentIndex));
				if (wrapPolicy.wrapMode != WrapMode.BLOCK_INDENT)
					spaces += token.getIndent() - parentLineStart.getIndent();
				token = parentLineStart;
				wrapPolicy = token.getWrapPolicy();
			}
		}
		appendIndentationString(this.buffer, this.options.tab_char, this.options.tab_size, indent - spaces, spaces);
	}

	public static void appendIndentationString(StringBuilder target, int tabChar, int tabSize, int indent,
			int additionalSpaces) {
		int spacesCount = additionalSpaces;
		int tabsCount = 0;
		switch (tabChar) {
			case DefaultCodeFormatterOptions.SPACE:
				spacesCount += indent;
				break;
			case DefaultCodeFormatterOptions.TAB:
				if (tabSize > 0) {
					tabsCount += indent / tabSize;
					if (indent % tabSize > 0)
						tabsCount++;
				}
				break;
			case DefaultCodeFormatterOptions.MIXED:
				if (tabSize > 0) {
					tabsCount += indent / tabSize;
					spacesCount += indent % tabSize;
				} else {
					spacesCount += indent;
				}
				break;
			default:
				throw new IllegalStateException("Unrecognized tab char: " + tabChar); //$NON-NLS-1$
		}

		char[] indentChars = new char[tabsCount + spacesCount];
		Arrays.fill(indentChars, 0, tabsCount, '\t');
		Arrays.fill(indentChars, tabsCount, indentChars.length, ' ');
		target.append(indentChars);
	}

	private boolean bufferAlign(Token token, int index) {
		int align = token.getAlign();
		int alignmentChar = this.alignChar;
		if (align == 0 && getLineBreaksBefore() == 0 && this.parent != null) {
			align = token.getIndent();
			token.setAlign(align);
			alignmentChar = DefaultCodeFormatterOptions.SPACE;
		}
		if (align == 0)
			return false;

		int currentPositionInLine = 0;
		if (getLineBreaksBefore() > 0) {
			if (this.parent == null)
				currentPositionInLine = this.tm.toIndent(token.getIndent(), token.getWrapPolicy() != null);
		} else {
			currentPositionInLine = this.tm.getPositionInLine(index - 1);
			currentPositionInLine += this.tm.getLength(this.tm.get(index - 1), currentPositionInLine);
		}
		if (currentPositionInLine >= align)
			return false;

		final int tabSize = this.options.tab_size;
		switch (alignmentChar) {
			case DefaultCodeFormatterOptions.SPACE:
				while (currentPositionInLine++ < align) {
					this.buffer.append(' ');
				}
				break;
			case DefaultCodeFormatterOptions.TAB:
				while (currentPositionInLine < align && tabSize > 0) {
					this.buffer.append('\t');
					currentPositionInLine += tabSize - currentPositionInLine % tabSize;
				}
				break;
			case DefaultCodeFormatterOptions.MIXED:
				while (tabSize > 0 && currentPositionInLine + tabSize - currentPositionInLine % tabSize <= align) {
					this.buffer.append('\t');
					currentPositionInLine += tabSize - currentPositionInLine % tabSize;
				}
				while (currentPositionInLine++ < align) {
					this.buffer.append(' ');
				}
				break;
			default:
				throw new IllegalStateException("Unrecognized align char: " + alignmentChar); //$NON-NLS-1$
		}
		return true;
	}

	private void flushBuffer(int currentPosition) {
		String buffered = this.buffer.toString();
		boolean sourceMatch = this.source.startsWith(buffered, this.counter)
				&& this.counter + buffered.length() == currentPosition;
		while (!sourceMatch && this.currentRegion < this.regions.size()) {
			IRegion region = this.regions.get(this.currentRegion);
			if (currentPosition < region.getOffset())
				break;
			int regionEnd = region.getOffset() + region.getLength();
			if (this.counter >= regionEnd) {
				this.currentRegion++;
				continue;
			}
			if (this.currentRegion == this.regions.size() - 1
					|| this.regions.get(this.currentRegion + 1).getOffset() > currentPosition) {
				this.edits.add(getReplaceEdit(this.counter, currentPosition, buffered, region));
				break;
			}

			// this edit will span more than one region, split it
			IRegion nextRegion = this.regions.get(this.currentRegion + 1);
			int bestSplit = 0;
			int bestSplitScore = Integer.MAX_VALUE;
			for (int i = 0; i < buffered.length(); i++) {
				ReplaceEdit edit1 = getReplaceEdit(this.counter, regionEnd, buffered.substring(0, i), region);
				ReplaceEdit edit2 = getReplaceEdit(regionEnd, currentPosition, buffered.substring(i), nextRegion);
				int score = edit1.getLength() + edit1.getText().length() + edit2.getLength() + edit2.getText().length();
				if (score < bestSplitScore) {
					bestSplit = i;
					bestSplitScore = score;
				}
			}
			this.edits.add(getReplaceEdit(this.counter, regionEnd, buffered.substring(0, bestSplit), region));
			buffered = buffered.substring(bestSplit);
			this.counter = regionEnd;
		}
		this.buffer.setLength(0);
		this.counter = currentPosition;
	}

	private ReplaceEdit getReplaceEdit(int editStart, int editEnd, String text, IRegion region) {
		int regionEnd = region.getOffset() + region.getLength();
		if (editStart < region.getOffset() && regionEnd < editEnd) {
			int breaksInReplacement = this.tm.countLineBreaksBetween(text, 0, text.length());
			int breaksBeforeRegion = this.tm.countLineBreaksBetween(this.source, editStart, region.getOffset());
			int breaksAfterRegion = this.tm.countLineBreaksBetween(this.source, regionEnd, editEnd);
			if (breaksBeforeRegion + breaksAfterRegion > breaksInReplacement) {
				text = ""; //$NON-NLS-1$
				editStart = region.getOffset();
				editEnd = regionEnd;
			}
		}
		if (region.getOffset() > editStart && isOnlyWhitespace(text)) {
			int breaksInReplacement = this.tm.countLineBreaksBetween(text, 0, text.length());
			int breaksOutsideRegion = this.tm.countLineBreaksBetween(this.source, editStart, region.getOffset());
			int breaksToPreserve = breaksInReplacement - breaksOutsideRegion;
			text = adaptReplaceText(text, breaksToPreserve, false, region.getOffset() - 1);
			editStart = region.getOffset();
		}
		if (regionEnd < editEnd && isOnlyWhitespace(text)) {
			int breaksInReplacement = this.tm.countLineBreaksBetween(text, 0, text.length());
			int breaksOutsideRegion = this.tm.countLineBreaksBetween(this.source, regionEnd, editEnd);
			int breaksToPreserve = breaksInReplacement - breaksOutsideRegion;
			text = adaptReplaceText(text, breaksToPreserve, true, regionEnd);
			editEnd = regionEnd;
		}
		return new ReplaceEdit(editStart, editEnd - editStart, text);
	}

	private boolean isOnlyWhitespace(String text) {
		for (int i = 0; i < text.length(); i++)
			if (!ScannerHelper.isWhitespace(text.charAt(i)))
				return false;
		return true;
	}

	private String adaptReplaceText(String text, int breaksToPreserve, boolean isRegionEnd, int regionEdge) {
		int i = isRegionEnd ? 0 : text.length() - 1;
		int direction = isRegionEnd ? 1 : -1;
		int preservedBreaks = 0;
		for (; i >= 0 && i < text.length(); i += direction) {
			assert ScannerHelper.isWhitespace(text.charAt(i));
			char c1 = text.charAt(i);
			if (c1 == '\r' || c1 == '\n') {
				if (preservedBreaks >= breaksToPreserve)
					break;
				preservedBreaks++;
				int i2 = i + direction;
				if (i2 >= 0 && i2 < text.length()) {
					char c2 = text.charAt(i2);
					if ((c2 == '\r' || c2 == '\n') && c2 != c1)
						i = i2;
				}
			}
		}
		text = isRegionEnd ? text.substring(0, i) : text.substring(i + 1);

		// cut out text if the source outside region is a matching whitespace
		int textPos = isRegionEnd ? text.length() - 1 : 0;
		int sourcePos = regionEdge;
		theLoop: while (textPos >= 0 && textPos < text.length() && sourcePos >= 0 && sourcePos < this.source.length()) {
			char c1 = text.charAt(textPos);
			char c2 = this.source.charAt(sourcePos);
			if (c1 == c2 && (c1 == ' ' || c1 == '\t')) {
				textPos -= direction;
				sourcePos += direction;
			} else if (c1 == '\t' && c2 == ' ') {
				for (i = 0; i < this.options.tab_size; i++) {
					sourcePos += direction;
					if (i < this.options.tab_size - 1 && (sourcePos < 0 || sourcePos >= this.source.length()
							|| this.source.charAt(sourcePos) != ' '))
						continue theLoop;
				}
				textPos -= direction;
			} else if (c2 == '\t' && c1 == ' ') {
				for (i = 0; i < this.options.tab_size; i++) {
					textPos -= direction;
					if (i < this.options.tab_size - 1
							&& (textPos < 0 || textPos >= text.length() || text.charAt(textPos) != ' '))
						continue theLoop;
				}
				sourcePos += direction;
			} else {
				break;
			}
		}
		if (isRegionEnd) {
			text = text.substring(0, textPos + 1);
		} else {
			text = text.substring(textPos);
		}

		return text;
	}

	private void handleSingleLineComment(Token lineComment, int index) {
		List<Token> structure = lineComment.getInternalStructure();
		if (structure == null) {
			flushBuffer(lineComment.originalStart);
			this.counter = lineComment.originalEnd + 1;
			return;
		}
		if (structure.get(0).tokenType == TokenNameWHITESPACE) {
			flushBuffer(structure.get(0).originalStart);
		} else {
			flushBuffer(lineComment.originalStart);
		}

		for (int i = 0; i < structure.size(); i++) {
			Token fragment = structure.get(i);

			if (fragment.getLineBreaksBefore() > 0) {
				bufferLineSeparator(fragment, false);
				bufferIndent(fragment, index);
			} else if (fragment.isSpaceBefore() && i > 0) {
				this.buffer.append(' ');
			}

			if (fragment.hasNLSTag()) {
				int tagNumber = this.stringLiteralsInLine.indexOf(fragment.getNLSTag());
				assert tagNumber >= 0;
				this.buffer.append("//$NON-NLS-").append(tagNumber + 1).append("$"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (fragment.originalStart < this.counter) {
				// Comment line prefix may be a copy of earlier code
				this.buffer.append(this.tm.toString(fragment));
			} else {
				flushBuffer(fragment.originalStart);
				this.counter = fragment.originalEnd + 1;
			}
		}

		if (lineComment.originalEnd > lineComment.originalStart) // otherwise it's a forged comment
			flushBuffer(lineComment.originalEnd + 1);
	}

	private void handleMultiLineComment(Token comment, int index) {
		flushBuffer(comment.originalStart);
		if (this.childBuilder == null) {
			this.childBuilder = new TextEditsBuilder(this);
		}
		this.childBuilder.traverseInternalStructure(comment, index);
		this.edits.addAll(this.childBuilder.edits);
		this.childBuilder.edits.clear();
		this.counter = this.childBuilder.sourceLimit;
	}

	private void traverseInternalStructure(Token token, int index) {
		List<Token> structure = token.getInternalStructure();
		this.tm = new TokenManager(structure, this.parent.tm);
		this.counter = token.originalStart;
		this.sourceLimit = token.originalEnd + 1;

		this.parentTokenIndex = index;

		traverse(structure, 0);
	}

	public void processComment(Token commentToken) {
		assert commentToken.isComment();
		if (commentToken.tokenType == TokenNameCOMMENT_LINE) {
			handleSingleLineComment(commentToken, this.tm.indexOf(commentToken));
		} else {
			handleMultiLineComment(commentToken, this.tm.indexOf(commentToken));
		}
	}

	public List<TextEdit> getEdits() {
		return this.edits;
	}

	public void setAlignChar(int alignChar) {
		this.alignChar = alignChar;
	}
}