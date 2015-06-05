/*******************************************************************************
 * Copyright (c) 2014, 2015 Mateusz Matela and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.linewrap;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_BLOCK;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_LINE;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameEQUAL;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameIdentifier;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.Token;
import org.eclipse.jdt.internal.formatter.TokenManager;
import org.eclipse.jdt.internal.formatter.TokenTraverser;

/** Implementation of the "Align fields on columns" feature */
public class FieldAligner {
	private class PositionCounter extends TokenTraverser {
		int stoppingIndex;
		int maxPosition;

		public PositionCounter() {
			// nothing to do
		}

		@Override
		protected boolean token(Token token, int index) {
			if (index == this.stoppingIndex)
				return false;
			if (getLineBreaksBefore() > 0)
				this.counter = FieldAligner.this.tm.getPositionInLine(index);
			if (token.getAlign() > 0)
				this.counter = token.getAlign();
			this.counter += FieldAligner.this.tm.getLength(token, this.counter);
			if (isSpaceAfter() && getLineBreaksAfter() == 0)
				this.counter++;
			this.maxPosition = Math.max(this.maxPosition, this.counter);
			return true;
		}

		public int findMaxPosition(int fromIndex, int toIndex) {
			this.counter = FieldAligner.this.tm.getPositionInLine(fromIndex);
			this.stoppingIndex = toIndex;
			this.maxPosition = 0;
			FieldAligner.this.tm.traverse(fromIndex, this);
			return this.maxPosition;
		}
	}

	private final List<List<FieldDeclaration>> fieldAlignGroups = new ArrayList<List<FieldDeclaration>>();

	final TokenManager tm;

	private final DefaultCodeFormatterOptions options;

	public FieldAligner(TokenManager tokenManager, DefaultCodeFormatterOptions options) {
		this.tm = tokenManager;
		this.options = options;
	}

	public void prepareAlign(TypeDeclaration node) {
		List<FieldDeclaration> bodyDeclarations = node.bodyDeclarations();
		ArrayList<FieldDeclaration> alignGroup = new ArrayList<FieldDeclaration>();
		BodyDeclaration previous = null;
		for (BodyDeclaration declaration : bodyDeclarations) {
			if (!alignGroup.isEmpty()) {
				if ((declaration instanceof FieldDeclaration) && !areSeparated(previous, declaration)) {
					alignGroup.add((FieldDeclaration) declaration);
				} else {
					alignFields(alignGroup);
					alignGroup = new ArrayList<FieldDeclaration>();
				}
			}
			if (alignGroup.isEmpty()) {
				if (declaration instanceof FieldDeclaration)
					alignGroup.add((FieldDeclaration) declaration);
			}
			previous = declaration;
		}
		alignFields(alignGroup);
	}

	private boolean areSeparated(BodyDeclaration declaration1, BodyDeclaration declaration2) {
		// check if there are more empty lines between fields than normal
		if (this.options.number_of_empty_lines_to_preserve <= this.options.blank_lines_before_field)
			return false;
		int maxLineBreaks = 0;
		int from = this.tm.lastIndexIn(declaration1, -1);
		int to = this.tm.firstIndexIn(declaration2, -1);

		Token previous = this.tm.get(from);
		for (int i = from + 1; i <= to; i++) {
			Token token = this.tm.get(i);
			maxLineBreaks = Math.max(maxLineBreaks, this.tm.countLineBreaksBetween(previous, token));
			previous = token;
		}
		return maxLineBreaks - 1 > this.options.blank_lines_before_field;
	}

	private void alignFields(ArrayList<FieldDeclaration> alignGroup) {
		if (alignGroup.size() < 2)
			return;
		this.fieldAlignGroups.add(alignGroup);

		int maxNameAlign = 0;
		for (FieldDeclaration declaration : alignGroup) {
			List<VariableDeclarationFragment> fragments = declaration.fragments();
			SimpleName fieldName = fragments.get(0).getName();
			int nameIndex = this.tm.firstIndexIn(fieldName, TokenNameIdentifier);
			int positionInLine = this.tm.getPositionInLine(nameIndex);
			maxNameAlign = Math.max(maxNameAlign, positionInLine);
		}
		maxNameAlign = this.tm.toIndent(maxNameAlign, true);

		int maxAssignAlign = 0;
		for (FieldDeclaration declaration : alignGroup) {
			List<VariableDeclarationFragment> fragments = declaration.fragments();
			VariableDeclarationFragment fragment = fragments.get(0);
			int nameIndex = this.tm.firstIndexIn(fragment.getName(), TokenNameIdentifier);
			Token nameToken = this.tm.get(nameIndex);

			nameToken.setAlign(maxNameAlign);

			if (fragment.getInitializer() != null) {
				int equalIndex = this.tm.firstIndexAfter(fragment.getName(), TokenNameEQUAL);
				int positionInLine = this.tm.getPositionInLine(equalIndex);
				maxAssignAlign = Math.max(maxAssignAlign, positionInLine);
			}
		}
		maxAssignAlign = this.tm.toIndent(maxAssignAlign, true);

		for (FieldDeclaration declaration : alignGroup) {
			List<VariableDeclarationFragment> fragments = declaration.fragments();
			VariableDeclarationFragment fragment = fragments.get(0);
			if (fragment.getInitializer() != null) {
				int assingIndex = this.tm.firstIndexAfter(fragment.getName(), TokenNameEQUAL);
				Token assignToken = this.tm.get(assingIndex);
				assignToken.setAlign(maxAssignAlign);

				int baseIndent = this.tm.getPositionInLine(assingIndex + 1) - assignToken.getIndent();
				int lastIndex = this.tm.lastIndexIn(declaration, -1);
				for (int i = assingIndex + 1; i <= lastIndex; i++) {
					Token token = this.tm.get(i);
					token.setIndent(baseIndent + token.getIndent());
				}
			}
		}
	}

	public void alignComments() {
		if (this.fieldAlignGroups.isEmpty())
			return;
		PositionCounter positionCounter = new PositionCounter();
		// align comments after field declarations
		for (List<FieldDeclaration> alignGroup : this.fieldAlignGroups) {
			int maxCommentAlign = 0;
			for (FieldDeclaration declaration : alignGroup) {
				int typeIndex = this.tm.firstIndexIn(declaration.getType(), -1);
				int firstIndexInLine = this.tm.findFirstTokenInLine(typeIndex);
				int lastIndex = this.tm.lastIndexIn(declaration, -1) + 1;
				maxCommentAlign = Math.max(maxCommentAlign,
						positionCounter.findMaxPosition(firstIndexInLine, lastIndex));
			}
			maxCommentAlign = this.tm.toIndent(maxCommentAlign, true);

			for (FieldDeclaration declaration : alignGroup) {
				int typeIndex = this.tm.firstIndexIn(declaration.getType(), -1);
				int firstIndexInLine = this.tm.findFirstTokenInLine(typeIndex);
				int lastIndex = this.tm.lastIndexIn(declaration, -1);
				lastIndex = Math.min(lastIndex, this.tm.size() - 2);
				for (int i = firstIndexInLine; i <= lastIndex; i++) {
					Token token = this.tm.get(i);
					Token next = this.tm.get(i + 1);
					boolean lineBreak = token.getLineBreaksAfter() > 0 || next.getLineBreaksBefore() > 0;
					if (lineBreak) {
						if (token.tokenType == TokenNameCOMMENT_BLOCK) {
							token.setAlign(maxCommentAlign);
						} else {
							this.tm.addNLSAlignIndex(i, maxCommentAlign);
						}
					} else if (next.tokenType == TokenNameCOMMENT_LINE
							|| (next.tokenType == TokenNameCOMMENT_BLOCK && i == lastIndex)) {
						next.setAlign(maxCommentAlign);
					}
				}
			}
		}
	}
}
