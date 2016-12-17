/*******************************************************************************
 * Copyright (c) 2014, 2015 Mateusz Matela and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] follow up bug for comments - https://bugs.eclipse.org/458208
 *     Mateusz Matela <mateusz.matela@gmail.com> - NPE in WrapExecutor during Java text formatting  - https://bugs.eclipse.org/465669
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.linewrap;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_BLOCK;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_JAVADOC;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_LINE;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.Token;
import org.eclipse.jdt.internal.formatter.TokenManager;
import org.eclipse.jdt.internal.formatter.TokenTraverser;
import org.eclipse.jdt.internal.formatter.Token.WrapMode;
import org.eclipse.jdt.internal.formatter.Token.WrapPolicy;

public class WrapExecutor {

	private static class WrapInfo {
		public int wrapTokenIndex;
		public int indent;

		public WrapInfo(int wrapIndex, int indent) {
			this.wrapTokenIndex = wrapIndex;
			this.indent = indent;
		}

		public WrapInfo() {
			// empty constructor
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.indent;
			result = prime * result + this.wrapTokenIndex;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WrapInfo other = (WrapInfo) obj;
			if (this.indent != other.indent)
				return false;
			if (this.wrapTokenIndex != other.wrapTokenIndex)
				return false;
			return true;
		}

	}

	private static class WrapResult {

		public static final WrapResult NO_WRAP_NEEDED = new WrapResult(0, 0, null);

		/** Penalty for used wraps */
		public final double penalty;
		/** Penalty for exceeding line limit and extra lines in comments */
		public final int extraPenalty;
		/**
		 * Contains information about the next wrap in the result or <code>null</code> if this is the last wrap.
		 * Can be used as a key in {@link WrapExecutor#wrapSearchResults} to retrieve the next wraps.
		 */
		public final WrapInfo nextWrap;

		WrapResult(double penalty, int extraPenalty, WrapInfo nextWrap) {
			this.penalty = penalty;
			this.extraPenalty = extraPenalty;
			this.nextWrap = nextWrap;
		}
	}

	private class LineAnalyzer extends TokenTraverser {

		private final CommentWrapExecutor commentWrapper;
		private int lineIndent;
		int firstPotentialWrap;
		int activeTopPriorityWrap;
		int minStructureDepth;
		int extraLines;
		boolean lineExceeded;
		boolean isNextLineWrapped;
		final List<Integer> extraLinesPerComment = new ArrayList<Integer>();
		final List<Integer> topPriorityGroupStarts = new ArrayList<Integer>();
		private int currentTopPriorityGroupEnd;
		private boolean isNLSTagInLine;

		public LineAnalyzer(TokenManager tokenManager, DefaultCodeFormatterOptions options) {
			this.commentWrapper = new CommentWrapExecutor(tokenManager, options);
		}

		/**
		 * @return index of the last token in line
		 */
		public int analyzeLine(int startIndex, int indent) {
			Token startToken = WrapExecutor.this.tm.get(startIndex);
			assert startToken.getLineBreaksBefore() > 0;
			this.counter = WrapExecutor.this.tm.toIndent(indent, startToken.isWrappable());
			this.lineIndent = indent;
			this.firstPotentialWrap = -1;
			this.activeTopPriorityWrap = -1;
			this.minStructureDepth = Integer.MAX_VALUE;
			this.extraLines = 0;
			this.isNextLineWrapped = false;
			this.extraLinesPerComment.clear();
			this.topPriorityGroupStarts.clear();
			this.currentTopPriorityGroupEnd = -1;
			this.isNLSTagInLine = false;
			int lastIndex = WrapExecutor.this.tm.traverse(startIndex, this);
			return lastIndex + (this.isNextLineWrapped ? 1 : 0);
		}

		@Override
		protected boolean token(Token token, int index) {
			if (token.hasNLSTag())
				this.isNLSTagInLine = true;

			if (token.isWrappable()) {
				WrapPolicy wrapPolicy = token.getWrapPolicy();
				if (wrapPolicy.wrapMode == WrapMode.TOP_PRIORITY && getLineBreaksBefore() == 0
						&& index > this.currentTopPriorityGroupEnd) {
					if (isActiveTopPriorityWrap(index, wrapPolicy)) {
						this.activeTopPriorityWrap = index;
					} else {
						this.topPriorityGroupStarts.add(index);
						this.currentTopPriorityGroupEnd = wrapPolicy.groupEndIndex;
					}
					if (this.firstPotentialWrap < 0)
						this.firstPotentialWrap = index;
				} else if (this.firstPotentialWrap < 0 && getWrapIndent(token) < this.counter) {
					this.firstPotentialWrap = index;
				}
				this.minStructureDepth = Math.min(this.minStructureDepth, wrapPolicy.structureDepth);
			}

			if (token.getAlign() > 0) {
				this.counter = token.getAlign();
			} else if (isSpaceBefore() && getLineBreaksBefore() == 0 && index > 0
					&& token.tokenType != TokenNameCOMMENT_LINE) {
				this.counter++;
			}

			if (!token.isComment()) {
				this.counter += WrapExecutor.this.tm.getLength(token, this.counter);
			} else if (token.tokenType != TokenNameCOMMENT_LINE) {
				this.counter = this.commentWrapper.wrapMultiLineComment(token, this.counter, true, this.isNLSTagInLine);
				this.extraLines += this.commentWrapper.getLinesCount() - 1;
				this.extraLinesPerComment.add(this.commentWrapper.getLinesCount() - 1);
			}

			this.lineExceeded = this.counter > WrapExecutor.this.options.page_width;
			if (this.lineExceeded && this.firstPotentialWrap >= 0) {
				return false;
			}
			token.setIndent(this.lineIndent);

			if (getNext() != null && getNext().isWrappable() && getLineBreaksAfter() > 0) {
				this.isNextLineWrapped = true;
				if (this.firstPotentialWrap < 0)
					this.firstPotentialWrap = index + 1;
				return false; 
			}

			boolean isLineEnd = getLineBreaksAfter() > 0 || getNext() == null
					|| (getNext().isNextLineOnWrap() && WrapExecutor.this.tm
							.get(WrapExecutor.this.tm.findFirstTokenInLine(index)).isWrappable());
			return !isLineEnd;
		}

		private boolean isActiveTopPriorityWrap(int index, WrapPolicy wrapPolicy) {
			if (this.activeTopPriorityWrap >= 0)
				return false;

			for (int i = index - 1; i > wrapPolicy.wrapParentIndex; i--) {
				Token token = WrapExecutor.this.tm.get(i);
				if (token.isWrappable() && token.getWrapPolicy().wrapParentIndex == wrapPolicy.wrapParentIndex
					&& (token.getLineBreaksBefore() > 0 || WrapExecutor.this.tm.get(i - 1).getLineBreaksAfter() > 0)) {
						return true;
				}
			}
			return false;
		}

		public int getLastPosition() {
			return this.counter;
		}
	}

	private class WrapsApplier extends TokenTraverser {

		private ArrayDeque<Token> stack = new ArrayDeque<>();
		private int initialIndent;
		private int currentIndent;
		private WrapInfo nextWrap;

		public WrapsApplier() {
			// nothing to do
		}

		@Override
		protected boolean token(Token token, int index) {
			if (index == 0 || getLineBreaksBefore() > 0) {
				newLine(token, index);
			} else if ((this.nextWrap != null && index == this.nextWrap.wrapTokenIndex)
					|| checkForceWrap(token, index, this.currentIndent)
					|| (token.isNextLineOnWrap() && WrapExecutor.this.tm
							.get(WrapExecutor.this.tm.findFirstTokenInLine(index)).isWrappable())) {
				token.breakBefore();
				newLine(token, index);
			} else {
				token.setIndent(this.currentIndent);
			}
			return true;
		}

		private void newLine(Token token, int index) {
			while (!this.stack.isEmpty() && index > this.stack.peek().getWrapPolicy().groupEndIndex)
				this.stack.pop();
			if (token.getWrapPolicy() != null) {
				token.setIndent(getWrapIndent(token));
				handleOnColumnIndent(index, token.getWrapPolicy());
				this.stack.push(token);
			} else if (this.stack.isEmpty()) {
				this.initialIndent = token.getIndent();
				WrapExecutor.this.wrapSearchResults.clear();
			}

			this.currentIndent = this.stack.isEmpty() ? this.initialIndent : this.stack.peek().getIndent();
			token.setIndent(this.currentIndent);
			this.nextWrap = findWrapsCached(index, this.currentIndent).nextWrap;
		}
	}

	private class NLSTagHandler extends TokenTraverser {
		private final ArrayList<Token> nlsTags = new ArrayList<Token>();

		public NLSTagHandler() {
			// nothing to do
		}

		@Override
		protected boolean token(final Token token, final int index) {
			if (token.hasNLSTag())
				this.nlsTags.add(token.getNLSTag());

			if (getLineBreaksAfter() > 0 || getNext() == null) {
				// make sure there's a line comment with all necessary NLS tags
				Token lineComment = token;
				if (token.tokenType != TokenNameCOMMENT_LINE) {
					if (this.nlsTags.isEmpty())
						return true;
					lineComment = new Token(token.originalEnd + 1, token.originalEnd + 1, TokenNameCOMMENT_LINE);
					lineComment.breakAfter();
					lineComment.spaceBefore();
					lineComment.setAlign(WrapExecutor.this.tm.getNLSAlign(index));
					lineComment.setInternalStructure(new ArrayList<Token>());
					WrapExecutor.this.tm.insert(index + 1, lineComment);
					structureChanged();
					return true; // will fill the line comment structure in next step
				}

				List<Token> structure = lineComment.getInternalStructure();
				if (structure == null) {
					if (this.nlsTags.isEmpty())
						return true;
					structure = new ArrayList<Token>();
					structure.add(lineComment);
					lineComment.setInternalStructure(structure);
				}

				boolean isPrefixMissing = false;
				for (int i = 0; i < structure.size(); i++) {
					Token fragment = structure.get(i);
					// remove NLS tags that are not associated with this line
					// (these have been added on wrapped lines earlier)
					if (fragment.hasNLSTag()) {
						if (!this.nlsTags.remove(fragment)) {
							if (i == 0)
								isPrefixMissing = true;
							structure.remove(i--);
						} else {
							isPrefixMissing = false;
						}
					} else if (isPrefixMissing) {
						// remove trailing whitespace
						int pos = fragment.originalStart;
						while (pos <= fragment.originalEnd
								&& ScannerHelper.isWhitespace(WrapExecutor.this.tm.charAt(pos)))
							pos++;
						if (pos > fragment.originalEnd) {
							structure.remove(i--);
							continue;
						}
						if (pos > fragment.originalStart) {
							fragment = new Token(pos, fragment.originalEnd, TokenNameCOMMENT_LINE);
							structure.set(i, fragment);
						}

						String fragmentString = WrapExecutor.this.tm.toString(fragment);
						if (!fragmentString.startsWith("//")) { //$NON-NLS-1$
							// forge a prefix
							Token prefix = new Token(lineComment.originalStart, lineComment.originalStart + 1,
									TokenNameCOMMENT_LINE);
							prefix.spaceBefore();
							structure.add(i, prefix);
						}
						isPrefixMissing = false;
					}
				}
				// add all remaining tags in this line
				// (these are currently in a future line comment but will be removed)
				structure.addAll(this.nlsTags);

				if (structure.isEmpty()) { // all the tags have been moved to other lines
					WrapExecutor.this.tm.remove(index);
					structureChanged();
				}

				this.nlsTags.clear();
			}
			return true;
		}
	}

	private final static int[] EMPTY_ARRAY = {};

	final HashMap<WrapInfo, WrapResult> wrapSearchResults = new HashMap<WrapInfo, WrapResult>();
	private final ArrayDeque<WrapInfo> wrapSearchStack = new ArrayDeque<>();

	private final LineAnalyzer lineAnalyzer;

	final TokenManager tm;
	final DefaultCodeFormatterOptions options;

	private final WrapInfo wrapInfoTemp = new WrapInfo();

	public WrapExecutor(TokenManager tokenManager, DefaultCodeFormatterOptions options) {
		this.tm = tokenManager;
		this.options = options;
		this.lineAnalyzer = new LineAnalyzer(tokenManager, options);
	}

	public void executeWraps() {
		this.tm.traverse(0, new WrapsApplier());
		this.tm.traverse(0, new NLSTagHandler());
	}

	WrapResult findWrapsCached(final int startTokenIndex, final int indent) {
		this.wrapInfoTemp.wrapTokenIndex = startTokenIndex;
		this.wrapInfoTemp.indent = indent;
		WrapResult wrapResult = this.wrapSearchResults.get(this.wrapInfoTemp);

		// pre-existing result may be based on different wrapping of earlier tokens and therefore be wrong
		WrapResult wr = wrapResult;
		boolean cacheMissAllowed = true;
		int lookupLimit = 50;
		while (wr != null && wr.nextWrap != null && lookupLimit --> 0) {
			WrapInfo wi = wr.nextWrap;
			Token token = this.tm.get(wi.wrapTokenIndex);
			if (token.getWrapPolicy().wrapParentIndex < startTokenIndex && getWrapIndent(token) != wi.indent) {
				wrapResult = null;
				cacheMissAllowed = false;
				break;
			}
			wr = this.wrapSearchResults.get(wi);
		}

		if (wrapResult != null)
			return wrapResult;

		this.wrapSearchStack.push(new WrapInfo(startTokenIndex, indent));
		if (this.wrapSearchStack.size() > 1 && cacheMissAllowed)
			return null; // cache miss, need to find wraps later in main stack processing

		ArrayList<WrapInfo> reverseStackTemp = new ArrayList<>();
		// run main stack processing
		while (true) {
			final WrapInfo item = this.wrapSearchStack.peek();
			Token token = this.tm.get(item.wrapTokenIndex);
			token.setWrapped(true);
			wrapResult = findWraps(item.wrapTokenIndex, item.indent);

			assert (wrapResult == null) == (this.wrapSearchStack.peek() != item);
			if (wrapResult != null) {
				token.setWrapped(false);
				this.wrapSearchStack.pop();
				this.wrapSearchResults.put(item, wrapResult);
				assert wrapResult.nextWrap == null || this.wrapSearchResults.get(wrapResult.nextWrap) != null; 
				if (item.wrapTokenIndex == startTokenIndex && item.indent == indent)
					break;
			} else {
				// reverse order of new items
				while (this.wrapSearchStack.peek() != item)
					reverseStackTemp.add(this.wrapSearchStack.pop());
				for (WrapInfo item2 : reverseStackTemp)
					this.wrapSearchStack.push(item2);
				reverseStackTemp.clear();
			}
		}
		assert wrapResult != null;
		return wrapResult;
	}

	/**
	 * The main algorithm that looks for optimal places to wrap.
	 * Calls itself recursively to get results for wrapped sub-lines.  
	 */
	private WrapResult findWraps(int wrapTokenIndex, int indent) {
		final int lastIndex = this.lineAnalyzer.analyzeLine(wrapTokenIndex, indent);
		final boolean nextLineWrapped = this.lineAnalyzer.isNextLineWrapped;
		final boolean wrapRequired = this.lineAnalyzer.lineExceeded || nextLineWrapped;
		int lineOverflow = Math.max(0, this.lineAnalyzer.getLastPosition() - this.options.page_width);
		int extraLines = this.lineAnalyzer.extraLines;
		final int firstPotentialWrap = this.lineAnalyzer.firstPotentialWrap;
		final int activeTopPriorityWrap = this.lineAnalyzer.activeTopPriorityWrap;

		final int[] extraLinesPerComment = toArray(this.lineAnalyzer.extraLinesPerComment);
		int commentIndex = extraLinesPerComment.length;

		final int[] topPriorityGroupStarts = toArray(this.lineAnalyzer.topPriorityGroupStarts);
		int topPriorityIndex = topPriorityGroupStarts.length - 1;
		int nearestGroupEnd = topPriorityIndex == -1 ? 0
				: this.tm.get(topPriorityGroupStarts[topPriorityIndex]).getWrapPolicy().groupEndIndex;

		double bestTotalPenalty = getWrapPenalty(wrapTokenIndex, indent, lastIndex + 1, -1, WrapResult.NO_WRAP_NEEDED);
		int bestExtraPenalty = lineOverflow + extraLines;
		int bestNextWrap = -1;
		int bestIndent = 0;
		boolean cacheMiss = false;

		if (!wrapRequired && activeTopPriorityWrap < 0
				&& (!this.options.join_wrapped_lines || !this.options.wrap_outer_expressions_when_nested)) {
			return new WrapResult(bestTotalPenalty, bestExtraPenalty, null);
		}

		// optimization: if there's a possible wrap at depth lower than line start, ignore the rest
		int depthLimit = Integer.MAX_VALUE;
		Token token = this.tm.get(wrapTokenIndex);
		if (token.isWrappable() && this.options.wrap_outer_expressions_when_nested && activeTopPriorityWrap < 0) {
			int currentDepth = token.getWrapPolicy().structureDepth;
			if (this.lineAnalyzer.minStructureDepth < currentDepth)
				depthLimit = currentDepth;
		}
		// optimization: turns out there's no point checking multiple wraps with the same policy 
		ArrayList<WrapPolicy> policiesTried = new ArrayList<>();

		for (int i = lastIndex; firstPotentialWrap >= 0 && i >= firstPotentialWrap; i--) {
			token = this.tm.get(i);
			if (commentIndex > 0
					&& (token.tokenType == TokenNameCOMMENT_BLOCK || token.tokenType == TokenNameCOMMENT_JAVADOC)) {
				extraLines -= extraLinesPerComment[--commentIndex];
				if (extraLinesPerComment[commentIndex] > 0)
					policiesTried.clear();
			}
			if (topPriorityIndex >= 0 && i <= nearestGroupEnd) {
				if (i > topPriorityGroupStarts[topPriorityIndex])
					continue;
				assert i == topPriorityGroupStarts[topPriorityIndex];
				topPriorityIndex--;
				nearestGroupEnd = topPriorityIndex == -1 ? 0
						: this.tm.get(topPriorityGroupStarts[topPriorityIndex]).getWrapPolicy().groupEndIndex;
			}

			WrapPolicy wrapPolicy = token.getWrapPolicy();
			if (!token.isWrappable()
					|| (activeTopPriorityWrap >= 0 && i != activeTopPriorityWrap)
					|| policiesTried.contains(wrapPolicy)
					|| wrapPolicy.structureDepth >= depthLimit)
				continue;
			policiesTried.add(wrapPolicy);

			int nextWrapIndent = getWrapIndent(token);
			WrapResult nextWrapResult = findWrapsCached(i, nextWrapIndent);
			cacheMiss |= nextWrapResult == null;
			if (cacheMiss)
				continue;

			double totalPenalty = getWrapPenalty(wrapTokenIndex, indent, i, nextWrapIndent, nextWrapResult);
			int totalExtraPenalty = nextWrapResult.extraPenalty + extraLines;
			if (lineOverflow > 0) {
				int position = this.tm.getPositionInLine(i - 1);
				position += this.tm.getLength(this.tm.get(i - 1), position);
				lineOverflow = position - this.options.page_width;
				totalExtraPenalty += Math.max(0, lineOverflow);
			}
			boolean isBetter = totalExtraPenalty < bestExtraPenalty
					|| i == activeTopPriorityWrap
					|| (bestNextWrap < 0 && wrapRequired);
			if (!isBetter && totalExtraPenalty == bestExtraPenalty)
				isBetter = totalPenalty < bestTotalPenalty || bestTotalPenalty == Double.MAX_VALUE;
			if (isBetter) {
				bestTotalPenalty = totalPenalty;
				bestExtraPenalty = totalExtraPenalty;
				bestNextWrap = i;
				bestIndent = nextWrapIndent;

				if (!this.options.wrap_outer_expressions_when_nested || i == activeTopPriorityWrap || nextLineWrapped)
					break;
			}
		}
		if (cacheMiss)
			return null;

		return new WrapResult(bestTotalPenalty, bestExtraPenalty,
				bestNextWrap == -1 ? null : new WrapInfo(bestNextWrap, bestIndent));
	}

	private double getWrapPenalty(int lineStartIndex, int lineIndent, int wrapIndex, int wrapIndent,
			WrapResult wrapResult) {
		WrapPolicy wrapPolicy = null;
		Token wrapToken = null;
		if (wrapIndex < this.tm.size()) {
			wrapToken = this.tm.get(wrapIndex);
			wrapPolicy = wrapToken.getWrapPolicy();
			if (wrapIndent < 0)
				wrapIndent = getWrapIndent(this.tm.get(wrapIndex));
		}

		double penalty = wrapToken != null && wrapToken.isWrappable() ? getPenalty(wrapPolicy) : 0;

		// First parameter in method invocation has higher penalty to make wrapping more similar to the old formatter.
		// This can lead to an undesired effect like this (should wrap aaaaaa and bbbbbb, not .bar):
		// foo.foo
		// 		.bar(aaaaaa,
		// 				bbbbbbb);
		if (wrapIndent > lineIndent)
			penalty *= 1 + 3.0 / 16;

		// Avoid ugly formations like this (bar2 should be wrapped):
		// foooooo(bar1(aaaaaa,
		// 		bbb), bar2(aaa,
		// 				bbbbbb)
		// Assuming lineStartIndex is at bbb, look for unwrapped bar2 and if found,
		// add more penalty than if it was wrapped.
		Token lineStartToken = this.tm.get(lineStartIndex);
		WrapPolicy lineStartWrapPolicy = lineStartToken.getWrapPolicy();
		if (wrapToken != null && wrapToken.isWrappable() && lineStartToken.isWrappable()) {
			for (int i = lineStartIndex + 1; i < wrapIndex; i++) {
				WrapPolicy intermediatePolicy = this.tm.get(i).getWrapPolicy();
				if (intermediatePolicy != null
						&& intermediatePolicy.structureDepth < lineStartWrapPolicy.structureDepth
						&& intermediatePolicy.structureDepth < wrapPolicy.structureDepth) {
					penalty += getPenalty(intermediatePolicy) * 1.25;
				}
			}
		}

		// In the previous example, bar1 should be wrapped too, to emphasize that bar1 and bar2 are the same level.
		// Assuming wrapIndex is at bar1, check if there is a higher depth wrap (bbb) followed by
		// a wrap of the same parent (bar2). If so, then bar1 must be wrapped (so give it negative penalty).
		// Update: Actually, every token that is followed by a higher level depth wrap should be also wrapped,
		// as long as this next wrap is not the last in line and the token is not the first in its wrap group.
		WrapInfo nextWrap = wrapResult.nextWrap;
		boolean checkDepth = wrapToken != null && wrapToken.isWrappable()
				&& (lineStartWrapPolicy == null || wrapPolicy.structureDepth >= lineStartWrapPolicy.structureDepth);
		double penaltyDiff = 0;
		while (checkDepth && nextWrap != null) {
			WrapPolicy nextPolicy = this.tm.get(nextWrap.wrapTokenIndex).getWrapPolicy();
			if (nextPolicy.wrapParentIndex == wrapPolicy.wrapParentIndex
					|| (penaltyDiff != 0 && !wrapPolicy.isFirstInGroup)) {
				penalty -= penaltyDiff * (1 + 1.0 / 64);
				break;
			}
			if (nextPolicy.structureDepth <= wrapPolicy.structureDepth)
				break;
			penaltyDiff = Math.max(penaltyDiff, getPenalty(nextPolicy));
			nextWrap = this.wrapSearchResults.get(nextWrap).nextWrap;
		}

		return penalty + wrapResult.penalty;
	}

	private double getPenalty(WrapPolicy policy) {
		return Math.exp(policy.structureDepth) * policy.penaltyMultiplier;
	}

	boolean checkForceWrap(Token token, int index, int currentIndent) {
		// A token that will have smaller indent when wrapped than the current line indent,
		// should be wrapped because it's a low depth token following some complex wraps of higher depth.
		// This rule could not be implemented in getWrapPenalty() because a token's wrap indent may depend
		// on wraps in previous lines, which are not determined yet when the token's penalty is calculated.
		if (!token.isWrappable() || !this.options.wrap_outer_expressions_when_nested
				|| getWrapIndent(token) >= currentIndent)
			return false;
		WrapPolicy lineStartPolicy = this.tm.get(this.tm.findFirstTokenInLine(index, false, true)).getWrapPolicy();
		return lineStartPolicy != null && lineStartPolicy.wrapMode != WrapMode.BLOCK_INDENT;
	}

	private int[] toArray(List<Integer> list) {
		if (list.isEmpty())
			return EMPTY_ARRAY;
		int[] result = new int[list.size()];
		int i = 0;
		for (int item : list) {
			result[i++] = item;
		}
		return result;
	}

	void handleOnColumnIndent(int tokenIndex, WrapPolicy wrapPolicy) {
		if (wrapPolicy != null && wrapPolicy.indentOnColumn && !wrapPolicy.isFirstInGroup
				&& this.options.tab_char == DefaultCodeFormatterOptions.TAB
				&& !this.options.use_tabs_only_for_leading_indentations) {
			// special case: first wrap in a group should be aligned on column even if it's not wrapped
			for (int i = tokenIndex - 1; i >= 0; i--) {
				Token token = this.tm.get(i);
				WrapPolicy wrapPolicy2 = token.getWrapPolicy();
				if (wrapPolicy2 != null && wrapPolicy2.isFirstInGroup
						&& wrapPolicy2.wrapParentIndex == wrapPolicy.wrapParentIndex) {
					token.setAlign(getWrapIndent(token));
					break;
				}
			}
		}
	}

	int getWrapIndent(Token token) {
		WrapPolicy policy = token.getWrapPolicy();
		if (policy == null)
			return token.getIndent();

		if (this.options.never_indent_line_comments_on_first_column && token.tokenType == TokenNameCOMMENT_LINE
				&& token.getIndent() == 0)
			return 0;
		if (this.options.never_indent_block_comments_on_first_column && token.tokenType == TokenNameCOMMENT_BLOCK
				&& token.getIndent() == 0)
			return 0;

		Token wrapParent = this.tm.get(policy.wrapParentIndex);
		int wrapIndent = wrapParent.getIndent();
		if (policy.indentOnColumn) {
			wrapIndent = this.tm.getPositionInLine(policy.wrapParentIndex);
			wrapIndent += this.tm.getLength(wrapParent, wrapIndent);
			if (wrapParent.isSpaceAfter() || this.tm.get(policy.wrapParentIndex + 1).isSpaceBefore())
				wrapIndent++;
		}
		wrapIndent += policy.extraIndent;
		return this.tm.toIndent(wrapIndent, true);
	}
}
