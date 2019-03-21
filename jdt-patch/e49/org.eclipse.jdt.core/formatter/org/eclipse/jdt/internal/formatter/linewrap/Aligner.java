/*******************************************************************************
 * Copyright (c) 2014, 2018 Mateusz Matela and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *     Lars Vogel <Lars.Vogel@vogella.com> - Contributions for
 *     						Bug 473178
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.linewrap;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_BLOCK;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_LINE;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameEQUAL;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameIdentifier;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.Token;
import org.eclipse.jdt.internal.formatter.TokenManager;
import org.eclipse.jdt.internal.formatter.TokenTraverser;

/** Implementation of the "Align items on columns" feature */
public class Aligner {
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
				this.counter = Aligner.this.tm.getPositionInLine(index);
			if (token.getAlign() > 0)
				this.counter = token.getAlign();
			this.counter += Aligner.this.tm.getLength(token, this.counter);
			if (isSpaceAfter() && getLineBreaksAfter() == 0)
				this.counter++;
			this.maxPosition = Math.max(this.maxPosition, this.counter);
			return true;
		}

		public int findMaxPosition(int fromIndex, int toIndex) {
			this.counter = Aligner.this.tm.getPositionInLine(fromIndex);
			this.stoppingIndex = toIndex;
			this.maxPosition = 0;
			Aligner.this.tm.traverse(fromIndex, this);
			return this.maxPosition;
		}
	}

	@FunctionalInterface
	private interface AlignIndexFinder<N extends ASTNode> {
		Optional<Integer> findIndex(N node);
	}

	private final List<List<? extends ASTNode>> alignGroups = new ArrayList<>();

	private final DefaultCodeFormatterOptions options;

	final TokenManager tm;

	public Aligner(TokenManager tokenManager, DefaultCodeFormatterOptions options) {
		this.tm = tokenManager;
		this.options = options;
	}

	public void handleAlign(List<BodyDeclaration> bodyDeclarations) {
		if (!this.options.align_type_members_on_columns)
			return;
		List<List<FieldDeclaration>> fieldGroups = toAlignGroups(bodyDeclarations,
				n -> optionalCast(n, FieldDeclaration.class));
		this.alignGroups.addAll(fieldGroups);

		AlignIndexFinder<FieldDeclaration> nameFinder = fd -> findName(
				(VariableDeclarationFragment) fd.fragments().get(0));
		fieldGroups.forEach(fg -> alignNodes(fg, nameFinder));

		AlignIndexFinder<FieldDeclaration> assignFinder = fd -> findAssign(
				(VariableDeclarationFragment) fd.fragments().get(0));
		fieldGroups.forEach(fg -> alignNodes(fg, assignFinder));
	}

	public void handleAlign(Block block) {
		List<Statement> statements = block.statements();
		if (this.options.align_variable_declarations_on_columns)
			alignDeclarations(statements);
		if (this.options.align_assignment_statements_on_columns)
			alignAssignmentStatements(statements);
	}

	private void alignDeclarations(List<Statement> statements) {
		List<List<VariableDeclarationStatement>> variableGroups = toAlignGroups(statements,
				n -> optionalCast(n, VariableDeclarationStatement.class));
		this.alignGroups.addAll(variableGroups);

		AlignIndexFinder<VariableDeclarationStatement> nameFinder = vd -> findName(
				(VariableDeclarationFragment) vd.fragments().get(0));
		variableGroups.forEach(vg -> alignNodes(vg, nameFinder));

		AlignIndexFinder<VariableDeclarationStatement> assignFinder = vd -> findAssign(
				(VariableDeclarationFragment) vd.fragments().get(0));
		variableGroups.forEach(vg -> alignNodes(vg, assignFinder));
	}

	private void alignAssignmentStatements(List<Statement> statements) {
		List<List<ExpressionStatement>> assignmentGroups = toAlignGroups(statements,
				n -> optionalCast(n, ExpressionStatement.class)
						.filter(es -> es.getExpression() instanceof Assignment));
		this.alignGroups.addAll(assignmentGroups);

		AlignIndexFinder<ExpressionStatement> assignFinder = es -> {
			Assignment a = (Assignment) es.getExpression();
			int operatorIndex = this.tm.firstIndexBefore(a.getRightHandSide(), -1);
			while (this.tm.get(operatorIndex).isComment())
				operatorIndex--;
			return Optional.of(operatorIndex);
		};
		assignmentGroups.forEach(ag -> alignNodes(ag, assignFinder));

		if (this.options.align_with_spaces || this.options.tab_char != DefaultCodeFormatterOptions.TAB) {
			// align assign operators on their right side (e.g. +=, >>=)
			for (List<ExpressionStatement> group : assignmentGroups) {
				List<Token> assignTokens = group.stream()
						.map(assignFinder::findIndex)
						.filter(Optional::isPresent)
						.map(o -> this.tm.get(o.get()))
						.collect(toList());
				int maxWidth = assignTokens.stream().mapToInt(Token::countChars).max().orElse(0);
				for (Token token : assignTokens)
					token.setAlign(token.getAlign() + maxWidth - token.countChars());
			}
		}
	}

	private <N extends ASTNode> Optional<N> optionalCast(ASTNode node, Class<N> c) {
		return Optional.of(node).filter(c::isInstance).map(c::cast);
	}

	private Optional<Integer> findName(VariableDeclarationFragment fragment) {
		int nameIndex = this.tm.firstIndexIn(fragment.getName(), TokenNameIdentifier);
		return Optional.of(nameIndex);
	}

	private Optional<Integer> findAssign(VariableDeclarationFragment fragment) {
		return Optional.ofNullable(fragment.getInitializer())
				.map(i -> this.tm.firstIndexBefore(i, TokenNameEQUAL));
	}

	private <N extends ASTNode> List<List<N>> toAlignGroups(List<? extends ASTNode> nodes,
			Function<ASTNode, Optional<N>> nodeConverter) {
		List<List<N>> result = new ArrayList<>();
		List<N> alignGroup = new ArrayList<>();
		N previous = null;
		for (ASTNode node : nodes) {
			Optional<N> converted = nodeConverter.apply(node);
			if (converted.isPresent()) {
				if (isNewGroup(node, previous)) {
					result.add(alignGroup);
					alignGroup = new ArrayList<>();
				}
				alignGroup.add(converted.get());
			}
			previous = converted.orElse(null);
		}
		result.add(alignGroup);
		result.removeIf(l -> l.size() < 2);
		return result;
	}

	private boolean isNewGroup(ASTNode node, ASTNode previousNode) {
		if (previousNode == null)
			return true;
		int lineBreaks = 0;
		int from = this.tm.lastIndexIn(previousNode, -1);
		int to = this.tm.firstIndexIn(node, -1);
		Token previousToken = this.tm.get(from);
		for (int i = from + 1; i <= to; i++) {
			Token token = this.tm.get(i);
			lineBreaks += Math.min(this.tm.countLineBreaksBetween(previousToken, token),
					this.options.number_of_empty_lines_to_preserve + 1);
			previousToken = token;
		}
		return lineBreaks > this.options.align_fields_grouping_blank_lines;
	}

	private <N extends ASTNode> void alignNodes(List<N> alignGroup, AlignIndexFinder<N> tokenFinder) {
		int[] tokenIndexes = alignGroup.stream()
				.map(tokenFinder::findIndex)
				.filter(Optional::isPresent)
				.mapToInt(Optional::get).toArray();
		OptionalInt maxPosition = IntStream.of(tokenIndexes).map(this.tm::getPositionInLine).max();
		if (maxPosition.isPresent()) {
			int align = normalizedAlign(maxPosition.getAsInt());
			for (int tokenIndex : tokenIndexes)
				this.tm.get(tokenIndex).setAlign(align);
		}
	}

	public void alignComments() {
		boolean alignLineComments = !this.options.comment_preserve_white_space_between_code_and_line_comments;
		PositionCounter positionCounter = new PositionCounter();
		// align comments after field declarations
		for (List<? extends ASTNode> alignGroup : this.alignGroups) {
			int maxCommentAlign = 0;
			for (ASTNode node : alignGroup) {
				int firstIndexInLine = findFirstTokenInLine(node);
				int lastIndex = this.tm.lastIndexIn(node, -1) + 1;
				maxCommentAlign = Math.max(maxCommentAlign,
						positionCounter.findMaxPosition(firstIndexInLine, lastIndex));
			}
			maxCommentAlign = normalizedAlign(maxCommentAlign);

			for (ASTNode node : alignGroup) {
				int firstIndexInLine = findFirstTokenInLine(node);
				int lastIndex = this.tm.lastIndexIn(node, -1);
				lastIndex = Math.min(lastIndex, this.tm.size() - 2);
				for (int i = firstIndexInLine; i <= lastIndex; i++) {
					Token token = this.tm.get(i);
					Token next = this.tm.get(i + 1);
					boolean lineBreak = token.getLineBreaksAfter() > 0 || next.getLineBreaksBefore() > 0;
					if (lineBreak) {
						if (token.tokenType == TokenNameCOMMENT_BLOCK) {
							token.setAlign(maxCommentAlign);
						} else if (alignLineComments) {
							this.tm.addNLSAlignIndex(i, maxCommentAlign);
						}
					} else if (next.tokenType == TokenNameCOMMENT_LINE && alignLineComments
							|| (next.tokenType == TokenNameCOMMENT_BLOCK && i == lastIndex)) {
						next.setAlign(maxCommentAlign);
					}
				}
			}
		}
	}

	private int findFirstTokenInLine(ASTNode node) {
		if (node instanceof FieldDeclaration) {
			int typeIndex = this.tm.firstIndexIn(((FieldDeclaration) node).getType(), -1);
			return this.tm.findFirstTokenInLine(typeIndex);
		}
		if (node instanceof VariableDeclarationStatement) {
			int typeIndex = this.tm.firstIndexIn(((VariableDeclarationStatement) node).getType(), -1);
			return this.tm.findFirstTokenInLine(typeIndex);
		}
		if (node instanceof ExpressionStatement) {
			return this.tm.firstIndexIn(node, -1);
		}
		throw new IllegalArgumentException(node.getClass().getName());
	}

	private int normalizedAlign(int desiredAlign) {
		if (this.options.align_with_spaces)
			return desiredAlign;
		return this.tm.toIndent(desiredAlign, false);
	}
}
