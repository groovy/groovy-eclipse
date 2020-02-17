/*******************************************************************************
 * Copyright (c) 2014, 2019 Mateusz Matela and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] follow up bug for comments - https://bugs.eclipse.org/458208
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.linewrap;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOLON;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMA;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_BLOCK;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_JAVADOC;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_LINE;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameDOT;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameEQUAL;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameIdentifier;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameLBRACE;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameLESS;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameLPAREN;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameOR;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameQUESTION;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameRBRACE;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameRPAREN;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameSEMICOLON;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameStringLiteral;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameenum;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameextends;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameimplements;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamenew;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamesuper;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamethis;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamethrows;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameto;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamewhile;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamewith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExportsDirective;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.OpensDirective;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ProvidesDirective;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions.Alignment;
import org.eclipse.jdt.internal.formatter.Token;
import org.eclipse.jdt.internal.formatter.Token.WrapMode;
import org.eclipse.jdt.internal.formatter.Token.WrapPolicy;
import org.eclipse.jdt.internal.formatter.TokenManager;
import org.eclipse.jdt.internal.formatter.TokenTraverser;
import org.eclipse.jface.text.IRegion;

public class WrapPreparator extends ASTVisitor {

	/**
	 * Helper for common handling of all expressions that should be treated the same as {@link FieldAccess}
	 */
	private static class FieldAccessAdapter {
		final Expression accessExpression;

		public FieldAccessAdapter(Expression expression) {
			this.accessExpression = expression;
		}

		public static boolean isFieldAccess(ASTNode expr) {
			return expr instanceof FieldAccess || expr instanceof QualifiedName || expr instanceof ThisExpression
					|| expr instanceof SuperFieldAccess;
		}

		public Expression getExpression() {
			if (this.accessExpression instanceof FieldAccess)
				return ((FieldAccess) this.accessExpression).getExpression();
			if (this.accessExpression instanceof QualifiedName)
				return ((QualifiedName) this.accessExpression).getQualifier();
			if (this.accessExpression instanceof ThisExpression)
				return ((ThisExpression) this.accessExpression).getQualifier();
			if (this.accessExpression instanceof SuperFieldAccess)
				return ((SuperFieldAccess) this.accessExpression).getQualifier();
			throw new AssertionError();
		}

		public int getIdentifierIndex(TokenManager tm) {
			if (this.accessExpression instanceof FieldAccess)
				return tm.firstIndexIn(((FieldAccess) this.accessExpression).getName(), TokenNameIdentifier);
			if (this.accessExpression instanceof QualifiedName)
				return tm.firstIndexIn(((QualifiedName) this.accessExpression).getName(), TokenNameIdentifier);
			if (this.accessExpression instanceof ThisExpression)
				return tm.lastIndexIn(this.accessExpression, TokenNamethis);
			if (this.accessExpression instanceof SuperFieldAccess)
				return tm.lastIndexIn(this.accessExpression, TokenNamesuper);
			throw new AssertionError();
		}
	}

	private static final Map<Operator, Integer> OPERATOR_PRECEDENCE;
	private static final Map<Operator, ToIntFunction<DefaultCodeFormatterOptions>> OPERATOR_WRAPPING_OPTION;
	private static final Map<Operator, Predicate<DefaultCodeFormatterOptions>> OPERATOR_WRAP_BEFORE_OPTION;
	static {
		HashMap<Operator, Integer> precedence = new HashMap<>();
		HashMap<Operator, ToIntFunction<DefaultCodeFormatterOptions>> wrappingOption = new HashMap<>();
		HashMap<Operator, Predicate<DefaultCodeFormatterOptions>> wrapBeforeOption = new HashMap<>();
		for (Operator op : Arrays.asList(Operator.TIMES, Operator.DIVIDE, Operator.REMAINDER)) {
			precedence.put(op, 1);
			wrappingOption.put(op, o -> o.alignment_for_multiplicative_operator);
			wrapBeforeOption.put(op, o -> o.wrap_before_multiplicative_operator);
		}
		for (Operator op : Arrays.asList(Operator.PLUS, Operator.MINUS)) {
			precedence.put(op, 2);
			wrappingOption.put(op, o -> o.alignment_for_additive_operator);
			wrapBeforeOption.put(op, o -> o.wrap_before_additive_operator);
		}
		for (Operator op : Arrays.asList(Operator.LEFT_SHIFT, Operator.RIGHT_SHIFT_SIGNED,
				Operator.RIGHT_SHIFT_UNSIGNED)) {
			precedence.put(op, 3);
			wrappingOption.put(op, o -> o.alignment_for_shift_operator);
			wrapBeforeOption.put(op, o -> o.wrap_before_shift_operator);
		}
		for (Operator op : Arrays.asList(Operator.LESS, Operator.GREATER, Operator.LESS_EQUALS,
				Operator.GREATER_EQUALS)) {
			precedence.put(op, 4);
			wrappingOption.put(op, o -> o.alignment_for_relational_operator);
			wrapBeforeOption.put(op, o -> o.wrap_before_relational_operator);
		}
		for (Operator op : Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS)) {
			precedence.put(op, 5);
			wrappingOption.put(op, o -> o.alignment_for_relational_operator);
			wrapBeforeOption.put(op, o -> o.wrap_before_relational_operator);
		}

		precedence.put(Operator.AND, 6);
		precedence.put(Operator.XOR, 7);
		precedence.put(Operator.OR, 8);
		for (Operator op : Arrays.asList(Operator.AND, Operator.XOR, Operator.OR)) {
			wrappingOption.put(op, o -> o.alignment_for_bitwise_operator);
			wrapBeforeOption.put(op, o -> o.wrap_before_bitwise_operator);
		}

		precedence.put(Operator.CONDITIONAL_AND, 9);
		precedence.put(Operator.CONDITIONAL_OR, 10);
		for (Operator op : Arrays.asList(Operator.CONDITIONAL_AND, Operator.CONDITIONAL_OR)) {
			wrappingOption.put(op, o -> o.alignment_for_logical_operator);
			wrapBeforeOption.put(op, o -> o.wrap_before_logical_operator);
		}
		// ternary and assignment operators not relevant to infix expressions

		OPERATOR_PRECEDENCE = Collections.unmodifiableMap(precedence);
		OPERATOR_WRAPPING_OPTION = Collections.unmodifiableMap(wrappingOption);
		OPERATOR_WRAP_BEFORE_OPTION = Collections.unmodifiableMap(wrapBeforeOption);
	}

	/** Penalty multiplier for wraps that are preferred */
	private final static float PREFERRED = 7f / 8;

	final TokenManager tm;
	final DefaultCodeFormatterOptions options;
	final int kind;

	final Aligner aligner;

	/*
	 * temporary values used when calling {@link #handleWrap(int)} to avoid ArrayList initialization and long lists of
	 * parameters
	 */
	private List<Integer> wrapIndexes = new ArrayList<>();
	/** Indexes for wraps that shouldn't happen but should be indented if cannot be removed */
	private List<Integer> secondaryWrapIndexes = new ArrayList<>();
	private List<Float> wrapPenalties = new ArrayList<>();
	private int wrapParentIndex = -1;
	private int wrapGroupEnd = -1;

	private int currentDepth = 0;

	public WrapPreparator(TokenManager tokenManager, DefaultCodeFormatterOptions options, int kind) {
		this.tm = tokenManager;
		this.options = options;
		this.kind = kind;

		this.aligner = new Aligner(this.tm, this.options);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		this.currentDepth++;

		assert this.wrapIndexes.isEmpty() && this.secondaryWrapIndexes.isEmpty() && this.wrapPenalties.isEmpty();
		assert this.wrapParentIndex == -1 && this.wrapGroupEnd == -1;

		boolean isMalformed = (node.getFlags() & ASTNode.MALFORMED) != 0;
		if (isMalformed) {
			this.tm.addDisableFormatTokenPair(this.tm.firstTokenIn(node, -1), this.tm.lastTokenIn(node, -1));
		}
		return !isMalformed;
	}

	@Override
	public void postVisit(ASTNode node) {
		this.currentDepth--;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		int lParen = this.tm.firstIndexAfter(node.getTypeName(), TokenNameLPAREN);
		int rParen = this.tm.lastIndexIn(node, TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_annotation);

		handleArguments(node.values(), this.options.alignment_for_arguments_in_annotation);
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		int lParen = this.tm.firstIndexAfter(node.getTypeName(), TokenNameLPAREN);
		int rParen = this.tm.lastIndexIn(node, TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_annotation);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		Type superclassType = node.getSuperclassType();
		if (superclassType != null) {
			this.wrapParentIndex = this.tm.lastIndexIn(node.getName(), -1);
			this.wrapGroupEnd = this.tm.lastIndexIn(superclassType, -1);
			this.wrapIndexes.add(this.tm.firstIndexBefore(superclassType, TokenNameextends));
			this.wrapIndexes.add(this.tm.firstIndexIn(superclassType, -1));
			handleWrap(this.options.alignment_for_superclass_in_type_declaration, PREFERRED);
		}

		List<Type> superInterfaceTypes = node.superInterfaceTypes();
		if (!superInterfaceTypes.isEmpty()) {
			int implementsToken = node.isInterface() ? TokenNameextends : TokenNameimplements;
			this.wrapParentIndex = this.tm.lastIndexIn(node.getName(), -1);
			this.wrapIndexes.add(this.tm.firstIndexBefore(superInterfaceTypes.get(0), implementsToken));
			prepareElementsList(superInterfaceTypes, TokenNameCOMMA, -1);
			handleWrap(this.options.alignment_for_superinterfaces_in_type_declaration, PREFERRED);
		}

		prepareElementsList(node.typeParameters(), TokenNameCOMMA, TokenNameLESS);
		handleWrap(this.options.alignment_for_type_parameters);

		this.aligner.handleAlign(node.bodyDeclarations());

		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		this.aligner.handleAlign(node.bodyDeclarations());
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		this.aligner.handleAlign(node.bodyDeclarations());
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		int lParen = this.tm.firstIndexAfter(node.getName(), TokenNameLPAREN);
		int rParen = node.getBody() == null ? this.tm.lastIndexIn(node, TokenNameRPAREN)
				: this.tm.firstIndexBefore(node.getBody(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_method_declaration);

		List<SingleVariableDeclaration> parameters = node.parameters();
		Type receiverType = node.getReceiverType();
		if (!parameters.isEmpty() || receiverType != null) {
			if (receiverType != null)
				this.wrapIndexes.add(this.tm.firstIndexIn(receiverType, -1));
			int wrappingOption = node.isConstructor() ? this.options.alignment_for_parameters_in_constructor_declaration
					: this.options.alignment_for_parameters_in_method_declaration;
			this.wrapGroupEnd = this.tm.lastIndexIn(
					parameters.isEmpty() ? receiverType : parameters.get(parameters.size() - 1), -1);
			handleArguments(parameters, wrappingOption);
		}

		List<Type> exceptionTypes = node.thrownExceptionTypes();
		if (!exceptionTypes.isEmpty()) {
			int wrappingOption = node.isConstructor()
					? this.options.alignment_for_throws_clause_in_constructor_declaration
					: this.options.alignment_for_throws_clause_in_method_declaration;
			if ((wrappingOption & Alignment.M_INDENT_ON_COLUMN) == 0)
				this.wrapParentIndex = lParen;
			prepareElementsList(exceptionTypes, TokenNameCOMMA, TokenNameRPAREN);
			// instead of the first exception type, wrap the "throws" token
			this.wrapIndexes.set(0, this.tm.firstIndexBefore(exceptionTypes.get(0), TokenNamethrows));
			handleWrap(wrappingOption, 0.5f);
		}

		if (!node.isConstructor()) {
			this.wrapParentIndex = this.tm.findFirstTokenInLine(this.tm.firstIndexIn(node.getName(), -1));
			while (this.tm.get(this.wrapParentIndex).isComment())
				this.wrapParentIndex++;
			List<TypeParameter> typeParameters = node.typeParameters();
			if (!typeParameters.isEmpty())
				this.wrapIndexes.add(this.tm.firstIndexIn(typeParameters.get(0), -1));
			if (node.getReturnType2() != null) {
				int returTypeIndex = this.tm.firstIndexIn(node.getReturnType2(), -1);
				if (returTypeIndex != this.wrapParentIndex)
					this.wrapIndexes.add(returTypeIndex);
			}
			this.wrapIndexes.add(this.tm.firstIndexIn(node.getName(), -1));
			this.wrapGroupEnd = this.tm.lastIndexIn(node.getName(), -1);
			handleWrap(this.options.alignment_for_method_declaration);
		}

		prepareElementsList(node.typeParameters(), TokenNameCOMMA, TokenNameLESS);
		handleWrap(this.options.alignment_for_type_parameters);

		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		List<EnumConstantDeclaration> enumConstants = node.enumConstants();
		int constantsEnd = -1;
		if (!enumConstants.isEmpty()) {
			for (EnumConstantDeclaration constant : enumConstants)
				this.wrapIndexes.add(this.tm.firstIndexIn(constant, -1));
			this.wrapParentIndex = (this.options.alignment_for_enum_constants & Alignment.M_INDENT_ON_COLUMN) > 0
					? this.tm.firstIndexBefore(enumConstants.get(0), TokenNameLBRACE)
					: this.tm.firstIndexIn(node, TokenNameenum);
			this.wrapGroupEnd = constantsEnd = this.tm.lastIndexIn(enumConstants.get(enumConstants.size() - 1), -1);
			handleWrap(this.options.alignment_for_enum_constants, node);
		}

		if (!this.options.join_wrapped_lines) {
			// preserve a line break between the last comma and semicolon
			int commaIndex = -1;
			int i = constantsEnd > 0 ? constantsEnd : this.tm.firstIndexAfter(node.getName(), TokenNameLBRACE);
			while (++i < this.tm.size()) {
				Token t = this.tm.get(i);
				if (t.isComment())
					continue;
				if (t.tokenType == TokenNameCOMMA) {
					commaIndex = i;
					continue;
				}
				if (t.tokenType == TokenNameSEMICOLON && commaIndex >= 0
						&& this.tm.countLineBreaksBetween(this.tm.get(commaIndex), t) == 1) {
					t.setWrapPolicy(new WrapPolicy(WrapMode.WHERE_NECESSARY, commaIndex, 0));
				}
				break;
			}
		}

		List<Type> superInterfaceTypes = node.superInterfaceTypes();
		if (!superInterfaceTypes.isEmpty()) {
			this.wrapParentIndex = this.tm.lastIndexIn(node.getName(), -1);
			this.wrapIndexes.add(this.tm.firstIndexBefore(superInterfaceTypes.get(0), TokenNameimplements));
			prepareElementsList(superInterfaceTypes, TokenNameCOMMA, -1);
			handleWrap(this.options.alignment_for_superinterfaces_in_enum_declaration, PREFERRED);
		}

		this.aligner.handleAlign(node.bodyDeclarations());

		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		int lParen = this.tm.firstIndexAfter(node.getName(), -1);
		while (this.tm.get(lParen).isComment())
			lParen++;
		if (this.tm.get(lParen).tokenType == TokenNameLPAREN) {
			int rParen = node.getAnonymousClassDeclaration() == null ? this.tm.lastIndexIn(node, TokenNameRPAREN)
					: this.tm.firstIndexBefore(node.getAnonymousClassDeclaration(), TokenNameRPAREN);
			handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_enum_constant_declaration);
		}

		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_enum_constant);
		AnonymousClassDeclaration anonymousClass = node.getAnonymousClassDeclaration();
		if (anonymousClass != null) {
			forceContinuousWrapping(anonymousClass, this.tm.firstIndexIn(node.getName(), -1));
		}
		return true;
	}

	@Override
	public boolean visit(Block node) {
		this.aligner.handleAlign(node);
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		int lParen = this.tm.firstIndexAfter(node.getName(), TokenNameLPAREN);
		int rParen = this.tm.lastIndexIn(node, TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_method_invocation);

		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_method_invocation);
		handleTypeArguments(node.typeArguments());

		boolean isInvocationChainRoot = !(node.getParent() instanceof MethodInvocation)
				|| node.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY;
		if (isInvocationChainRoot) {
			Expression expression = node;
			MethodInvocation invocation = node;
			while (expression instanceof MethodInvocation) {
				invocation = (MethodInvocation) expression;
				expression = invocation.getExpression();
				if (expression != null) {
					this.wrapIndexes.add(this.tm.firstIndexBefore(invocation.getName(), TokenNameDOT));
					this.secondaryWrapIndexes.add(this.tm.firstIndexIn(invocation.getName(), TokenNameIdentifier));
				}
			}
			Collections.reverse(this.wrapIndexes);
			this.wrapParentIndex = (expression != null) ? this.tm.lastIndexIn(expression, -1)
					: this.tm.lastIndexIn(invocation, -1);
			this.wrapGroupEnd = this.tm.lastIndexIn(node, -1);
			handleWrap(this.options.alignment_for_selector_in_method_invocation);
		}
		return true;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		int lParen = this.tm.firstIndexAfter(node.getName(), TokenNameLPAREN);
		int rParen = this.tm.lastIndexIn(node, TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_method_invocation);

		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_method_invocation);
		handleTypeArguments(node.typeArguments());
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		int lParen = this.tm.firstIndexAfter(node.getType(), TokenNameLPAREN);
		int rParen = node.getAnonymousClassDeclaration() == null ? this.tm.lastIndexIn(node, TokenNameRPAREN)
				: this.tm.firstIndexBefore(node.getAnonymousClassDeclaration(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_method_invocation);

		AnonymousClassDeclaration anonymousClass = node.getAnonymousClassDeclaration();
		if (anonymousClass != null) {
			forceContinuousWrapping(anonymousClass, this.tm.firstIndexIn(node, TokenNamenew));
		}

		int wrappingOption = node.getExpression() != null
				? this.options.alignment_for_arguments_in_qualified_allocation_expression
				: this.options.alignment_for_arguments_in_allocation_expression;
		handleArguments(node.arguments(), wrappingOption);

		handleTypeArguments(node.typeArguments());
		return true;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		int lParen = node.arguments().isEmpty() ? this.tm.lastIndexIn(node, TokenNameLPAREN)
				: this.tm.firstIndexBefore((ASTNode) node.arguments().get(0), TokenNameLPAREN);
		int rParen = this.tm.lastIndexIn(node, TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_method_invocation);

		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_explicit_constructor_call);
		handleTypeArguments(node.typeArguments());
		return true;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		int lParen = node.arguments().isEmpty() ? this.tm.lastIndexIn(node, TokenNameLPAREN)
				: this.tm.firstIndexBefore((ASTNode) node.arguments().get(0), TokenNameLPAREN);
		int rParen = this.tm.lastIndexIn(node, TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_method_invocation);

		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_explicit_constructor_call);
		handleTypeArguments(node.typeArguments());
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		handleFieldAccess(node);
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		handleFieldAccess(node);
		return true;
	}

	@Override
	public boolean visit(ThisExpression node) {
		handleFieldAccess(node);
		return true;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		handleFieldAccess(node);
		return true;
	}

	private void handleFieldAccess(Expression node) {
		boolean isAccessChainRoot = !FieldAccessAdapter.isFieldAccess(node.getParent());
		if (!isAccessChainRoot)
			return;

		Expression expression = node;
		FieldAccessAdapter access = null;
		while (FieldAccessAdapter.isFieldAccess(expression)) {
			access = new FieldAccessAdapter(expression);
			int nameIndex = access.getIdentifierIndex(this.tm);
			// find a dot preceding the name, may not be there
			for (int i = nameIndex - 1; i > this.tm.firstIndexIn(node, -1); i--) {
				Token t = this.tm.get(i);
				if (t.tokenType == TokenNameDOT) {
					this.wrapIndexes.add(i);
					this.secondaryWrapIndexes.add(nameIndex);
				}
				if (!t.isComment() && t.tokenType != TokenNamesuper)
					break;
			}
			expression = access.getExpression();
		}
		Collections.reverse(this.wrapIndexes);
		this.wrapParentIndex = this.tm.lastIndexIn(expression != null ? expression : access.accessExpression, -1);
		boolean isFollowedByInvocation = node.getParent() instanceof MethodInvocation
				&& node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY;
		this.wrapGroupEnd = isFollowedByInvocation ? this.tm.lastIndexIn(node.getParent(), -1)
				: new FieldAccessAdapter(node).getIdentifierIndex(this.tm);
		// TODO need configuration for this, now only handles line breaks that cannot be removed
		handleWrap(Alignment.M_NO_ALIGNMENT);
	}

	@Override
	public boolean visit(InfixExpression node) {
		Integer operatorPrecedence = OPERATOR_PRECEDENCE.get(node.getOperator());
		if (operatorPrecedence == null)
			return true;
		ASTNode parent = node.getParent();
		if ((parent instanceof InfixExpression) && samePrecedence(node, (InfixExpression) parent))
			return true; // this node has been handled higher in the AST

		int wrappingOption = OPERATOR_WRAPPING_OPTION.get(node.getOperator()).applyAsInt(this.options);
		boolean wrapBeforeOperator = OPERATOR_WRAP_BEFORE_OPTION.get(node.getOperator()).test(this.options);
		if (this.tm.isStringConcatenation(node)) {
			wrappingOption = this.options.alignment_for_string_concatenation;
			wrapBeforeOperator = this.options.wrap_before_string_concatenation;
		}

		findTokensToWrap(node, wrapBeforeOperator, 0);
		this.wrapParentIndex = this.wrapIndexes.remove(0);
		this.wrapGroupEnd = this.tm.lastIndexIn(node, -1);
		if ((wrappingOption & Alignment.M_INDENT_ON_COLUMN) != 0 && this.wrapParentIndex > 0)
			this.wrapParentIndex--;
		for (int i = this.wrapParentIndex; i >= 0; i--) {
			if (!this.tm.get(i).isComment()) {
				this.wrapParentIndex = i;
				break;
			}
		}
		handleWrap(wrappingOption, !wrapBeforeOperator, node);
		return true;
	}

	private void findTokensToWrap(InfixExpression node, boolean wrapBeforeOperator, int depth) {
		Expression left = node.getLeftOperand();
		if (left instanceof InfixExpression && samePrecedence(node, (InfixExpression) left)) {
			findTokensToWrap((InfixExpression) left, wrapBeforeOperator, depth + 1);
		} else if (this.wrapIndexes.isEmpty() // always add first operand, it will be taken as wrap parent
				|| !wrapBeforeOperator) {
			this.wrapIndexes.add(this.tm.firstIndexIn(left, -1));
		}

		Expression right = node.getRightOperand();
		List<Expression> extended = node.extendedOperands();
		for (int i = -1; i < extended.size(); i++) {
			Expression operand = (i == -1) ? right : extended.get(i);
			if (operand instanceof InfixExpression && samePrecedence(node, (InfixExpression) operand)) {
				findTokensToWrap((InfixExpression) operand, wrapBeforeOperator, depth + 1);
			}
			int indexBefore = this.tm.firstIndexBefore(operand, -1);
			while (this.tm.get(indexBefore).isComment())
				indexBefore--;
			assert node.getOperator().toString().equals(this.tm.toString(indexBefore));
			int indexAfter = this.tm.firstIndexIn(operand, -1);
			this.wrapIndexes.add(wrapBeforeOperator ? indexBefore : indexAfter);
			this.secondaryWrapIndexes.add(wrapBeforeOperator ? indexAfter : indexBefore);

			if (!this.options.join_wrapped_lines) {
				// TODO there should be an option for never joining wraps on opposite side of the operator
				if (wrapBeforeOperator) {
					if (this.tm.countLineBreaksBetween(this.tm.get(indexAfter - 1), this.tm.get(indexAfter)) > 0)
						this.wrapIndexes.add(indexAfter);
				} else {
					if (this.tm.countLineBreaksBetween(this.tm.get(indexBefore), this.tm.get(indexBefore - 1)) > 0)
						this.wrapIndexes.add(indexBefore);
				}
			}
		}
	}

	private boolean samePrecedence(InfixExpression expression1, InfixExpression expression2) {
		Integer precedence1 = OPERATOR_PRECEDENCE.get(expression1.getOperator());
		Integer precedence2 = OPERATOR_PRECEDENCE.get(expression2.getOperator());
		if (precedence1 == null || precedence2 == null)
			return false;
		return precedence1.equals(precedence2);
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		boolean chainsMatter = (this.options.alignment_for_conditional_expression_chain
				& Alignment.SPLIT_MASK) != Alignment.M_NO_ALIGNMENT;
		boolean isNextInChain = node.getParent() instanceof ConditionalExpression
				&& node == ((ConditionalExpression) node.getParent()).getElseExpression();
		boolean isFirstInChain = node.getElseExpression() instanceof ConditionalExpression && !isNextInChain;
		boolean wrapBefore = this.options.wrap_before_conditional_operator;
		List<Integer> before = wrapBefore ? this.wrapIndexes : this.secondaryWrapIndexes;
		List<Integer> after = wrapBefore ? this.secondaryWrapIndexes : this.wrapIndexes;
		if (!chainsMatter || (!isFirstInChain && !isNextInChain)) {
			before.add(this.tm.firstIndexAfter(node.getExpression(), TokenNameQUESTION));
			before.add(this.tm.firstIndexAfter(node.getThenExpression(), TokenNameCOLON));
			after.add(this.tm.firstIndexIn(node.getThenExpression(), -1));
			after.add(this.tm.firstIndexIn(node.getElseExpression(), -1));
			this.wrapParentIndex = this.tm.lastIndexIn(node.getExpression(), -1);
			this.wrapGroupEnd = this.tm.lastIndexIn(node, -1);
			handleWrap(this.options.alignment_for_conditional_expression);

		} else if (isFirstInChain) {
			List<ConditionalExpression> chain = new ArrayList<>();
			chain.add(node);
			ConditionalExpression next = node;
			while (next.getElseExpression() instanceof ConditionalExpression) {
				next = (ConditionalExpression) next.getElseExpression();
				chain.add(next);
			}

			for (ConditionalExpression conditional : chain) {
				before.add(this.tm.firstIndexAfter(conditional.getThenExpression(), TokenNameCOLON));
				after.add(this.tm.firstIndexIn(conditional.getElseExpression(), -1));
			}
			this.wrapParentIndex = this.tm.firstIndexIn(node.getExpression(), -1);
			this.wrapGroupEnd = this.tm.lastIndexIn(node, -1);
			handleWrap(this.options.alignment_for_conditional_expression_chain);

			this.currentDepth++;
			for (ConditionalExpression conditional : chain) {
				before.add(this.tm.firstIndexAfter(conditional.getExpression(), TokenNameQUESTION));
				after.add(this.tm.firstIndexIn(conditional.getThenExpression(), -1));
				this.wrapParentIndex = this.tm.firstIndexIn(conditional.getExpression(), -1);
				this.wrapGroupEnd = this.tm.lastIndexIn(conditional.getThenExpression(), -1);
				handleWrap(this.options.alignment_for_conditional_expression);
			}
			this.currentDepth--;
		}
		return true;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		List<Expression> expressions = node.expressions();
		if (!expressions.isEmpty()) {
			prepareElementsList(expressions, TokenNameCOMMA, TokenNameLBRACE);
			handleWrap(this.options.alignment_for_expressions_in_array_initializer, node);
		}
		int openingBraceIndex = this.tm.firstIndexIn(node, TokenNameLBRACE);
		Token openingBrace = this.tm.get(openingBraceIndex);
		if (openingBrace.isNextLineOnWrap() && openingBrace.getWrapPolicy() == null && openingBraceIndex > 0) {
			// add fake wrap policy to make sure the brace indentation is right
			openingBrace.setWrapPolicy(new WrapPolicy(WrapMode.DISABLED, openingBraceIndex - 1, 0));
		}
		if (!this.options.join_wrapped_lines
				&& !this.options.insert_new_line_before_closing_brace_in_array_initializer) {
			// if there is a line break before the closing brace, formatter should treat it as a valid wrap to preserve
			int closingBraceIndex = this.tm.lastIndexIn(node, TokenNameRBRACE);
			Token closingBrace = this.tm.get(closingBraceIndex);
			if (this.tm.countLineBreaksBetween(this.tm.get(closingBraceIndex - 1), closingBrace) == 1) {
				closingBrace.setWrapPolicy(new WrapPolicy(WrapMode.WHERE_NECESSARY, openingBraceIndex,
						closingBraceIndex, 0, this.currentDepth, 1, true, false));
			}
		}
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		int rightSideIndex = this.tm.firstIndexIn(node.getRightHandSide(), -1);
		if (this.tm.get(rightSideIndex).getLineBreaksBefore() > 0)
			return true; // must be an array initializer in new line because of brace_position_for_array_initializer

		int operatorIndex = this.tm.firstIndexBefore(node.getRightHandSide(), -1);
		while (this.tm.get(operatorIndex).isComment())
			operatorIndex--;
		assert node.getOperator().toString().equals(this.tm.toString(operatorIndex));

		this.wrapIndexes.add(this.options.wrap_before_assignment_operator ? operatorIndex : rightSideIndex);
		this.secondaryWrapIndexes.add(this.options.wrap_before_assignment_operator ? rightSideIndex : operatorIndex);
		this.wrapParentIndex = operatorIndex - 1;
		this.wrapGroupEnd = this.tm.lastIndexIn(node.getRightHandSide(), -1);
		handleWrap(this.options.alignment_for_assignment);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (node.getInitializer() == null)
			return true;
		int rightSideIndex = this.tm.firstIndexIn(node.getInitializer(), -1);
		if (this.tm.get(rightSideIndex).getLineBreaksBefore() > 0)
			return true; // must be an array initializer in new line because of brace_position_for_array_initializer
		int equalIndex = this.tm.firstIndexBefore(node.getInitializer(), TokenNameEQUAL);

		this.wrapIndexes.add(this.options.wrap_before_assignment_operator ? equalIndex : rightSideIndex);
		this.secondaryWrapIndexes.add(this.options.wrap_before_assignment_operator ? rightSideIndex : equalIndex);
		this.wrapParentIndex = equalIndex - 1;
		this.wrapGroupEnd = this.tm.lastIndexIn(node.getInitializer(), -1);
		handleWrap(this.options.alignment_for_assignment);
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		int lParen = this.tm.firstIndexIn(node, TokenNameLPAREN);
		int rParen = this.tm.firstIndexAfter(node.getExpression(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_if_while_statement);

		Statement elseStatement = node.getElseStatement();
		boolean keepThenOnSameLine = this.options.keep_then_statement_on_same_line
				|| (this.options.keep_simple_if_on_one_line && elseStatement == null);
		if (keepThenOnSameLine)
			handleSimpleLoop(node.getThenStatement(), this.options.alignment_for_compact_if);

		if (this.options.keep_else_statement_on_same_line && elseStatement != null)
			handleSimpleLoop(elseStatement, this.options.alignment_for_compact_if);
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		int lParen = this.tm.firstIndexIn(node, TokenNameLPAREN);
		int rParen = this.tm.firstIndexBefore(node.getBody(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_for_statement);

		List<Expression> initializers = node.initializers();
		if (!initializers.isEmpty())
			this.wrapIndexes.add(this.tm.firstIndexIn(initializers.get(0), -1));
		if (node.getExpression() != null)
			this.wrapIndexes.add(this.tm.firstIndexIn(node.getExpression(), -1));
		List<Expression> updaters = node.updaters();
		if (!updaters.isEmpty())
			this.wrapIndexes.add(this.tm.firstIndexIn(updaters.get(0), -1));
		if (!this.wrapIndexes.isEmpty()) {
			this.wrapParentIndex = lParen;
			this.wrapGroupEnd = rParen;
			handleWrap(this.options.alignment_for_expressions_in_for_loop_header);
		}
		if (this.options.keep_simple_for_body_on_same_line)
			handleSimpleLoop(node.getBody(), this.options.alignment_for_compact_loop);
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		int lParen = this.tm.firstIndexIn(node, TokenNameLPAREN);
		int rParen = this.tm.firstIndexBefore(node.getBody(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_for_statement);

		if (this.options.keep_simple_for_body_on_same_line)
			handleSimpleLoop(node.getBody(), this.options.alignment_for_compact_loop);
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		int lParen = this.tm.firstIndexIn(node, TokenNameLPAREN);
		int rParen = this.tm.firstIndexAfter(node.getExpression(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_if_while_statement);

		if (this.options.keep_simple_while_body_on_same_line)
			handleSimpleLoop(node.getBody(), this.options.alignment_for_compact_loop);
		return true;
	}

	private void handleSimpleLoop(Statement body, int wrappingOption) {
		if (!(body instanceof Block)) {
			this.wrapIndexes.add(this.tm.firstIndexIn(body, -1));
			this.wrapParentIndex = this.tm.firstIndexBefore(body, TokenNameRPAREN);
			this.wrapGroupEnd = this.tm.lastIndexIn(body, -1);
			handleWrap(wrappingOption, body.getParent());

			body.accept(new ASTVisitor() {
				@Override
				public boolean visit(Block node) {
					forceContinuousWrapping(node, WrapPreparator.this.tm.firstIndexIn(node, -1));
					return false;
				}
			});
		}
	}

	@Override
	public void endVisit(DoStatement node) {
		if (this.options.keep_simple_do_while_body_on_same_line && !(node.getBody() instanceof Block)) {
			int whileIndex = this.tm.firstIndexAfter(node.getBody(), TokenNamewhile);
			this.wrapIndexes.add(whileIndex);
			this.wrapParentIndex = this.tm.lastIndexIn(node.getBody(), -1);
			this.wrapGroupEnd = this.tm.lastIndexIn(node, -1);

			int alignment = this.options.alignment_for_compact_loop;
			for (int i = this.tm.firstIndexIn(node, -1) + 1; i < whileIndex; i++) {
				Token token = this.tm.get(i);
				if (token.getLineBreaksBefore() > 0 || token.getLineBreaksAfter() > 0)
					alignment |= Alignment.M_FORCE;
			}
			handleWrap(alignment, node);
		}
	}

	@Override
	public boolean visit(TryStatement node) {
		if (!node.resources().isEmpty()) {
			int lParen = this.tm.firstIndexIn(node, TokenNameLPAREN);
			int rParen = this.tm.firstIndexBefore(node.getBody(), TokenNameRPAREN);
			handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_try_clause);
		}
		prepareElementsList(node.resources(), TokenNameSEMICOLON, TokenNameLPAREN);
		handleWrap(this.options.alignment_for_resources_in_try);
		return true;
	}

	@Override
	public boolean visit(UnionType node) {
		List<Type> types = node.types();
		if (types.isEmpty())
			return true;
		if (this.options.wrap_before_or_operator_multicatch) {
			for (Type type : types) {
				if (this.wrapIndexes.isEmpty()) {
					this.wrapIndexes.add(this.tm.firstIndexIn(type, -1));
				} else {
					this.wrapIndexes.add(this.tm.firstIndexBefore(type, TokenNameOR));
					this.secondaryWrapIndexes.add(this.tm.firstIndexIn(type, -1));
				}
			}
			this.wrapParentIndex = this.tm.firstIndexBefore(node, -1);
			while (this.tm.get(this.wrapParentIndex).isComment())
				this.wrapParentIndex--;
			this.wrapGroupEnd = this.tm.lastIndexIn(types.get(types.size() - 1), -1);
			handleWrap(this.options.alignment_for_union_type_in_multicatch);
		} else {
			prepareElementsList(types, TokenNameOR, TokenNameLPAREN);
			handleWrap(this.options.alignment_for_union_type_in_multicatch);
		}
		return true;
	}

	@Override
	public boolean visit(LambdaExpression node) {
		int lParen = this.tm.firstIndexIn(node, -1);
		if (this.tm.get(lParen).tokenType == TokenNameLPAREN) {
			int rParen = this.tm.firstIndexBefore(node.getBody(), TokenNameRPAREN);
			handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_lambda_declaration);
		}
		if (node.getBody() instanceof Block) {
			forceContinuousWrapping(node.getBody(), this.tm.firstIndexIn(node, -1));

			List<Statement> statements = ((Block) node.getBody()).statements();
			if (!statements.isEmpty()) {
				int openBraceIndex = this.tm.firstIndexBefore(statements.get(0), TokenNameLBRACE);
				int closeBraceIndex = this.tm.firstIndexAfter(statements.get(statements.size() - 1), TokenNameRBRACE);
				boolean areKeptOnOneLine = statements.stream()
						.allMatch(n -> this.tm.firstTokenIn(n, -1).getLineBreaksBefore() == 0);
				if (areKeptOnOneLine) {
					for (Statement statement : statements)
						this.wrapIndexes.add(this.tm.firstIndexIn(statement, -1));
					this.wrapParentIndex = openBraceIndex;
					this.wrapGroupEnd = closeBraceIndex;
					handleWrap(Alignment.M_ONE_PER_LINE_SPLIT, node);
					this.tm.get(closeBraceIndex).setWrapPolicy(new WrapPolicy(WrapMode.TOP_PRIORITY, openBraceIndex,
							closeBraceIndex, 0, this.currentDepth, 1, false, false));
				}
			}
		}
		if (node.hasParentheses()) {
			List<VariableDeclaration> parameters = node.parameters();
			// the legacy formatter didn't like wrapping lambda parameters, so neither do we
			this.currentDepth++;
			handleArguments(parameters, this.options.alignment_for_parameters_in_method_declaration);
			this.currentDepth--;
		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		handleVariableDeclarations(node.fragments());
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		handleVariableDeclarations(node.fragments());
		return true;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		prepareElementsList(node.typeArguments(), TokenNameCOMMA, TokenNameLESS);
		handleWrap(this.options.alignment_for_parameterized_type_references);
		return true;
	}

	@Override
	public boolean visit(TypeMethodReference node) {
		handleTypeArguments(node.typeArguments());
		return true;
	}

	@Override
	public boolean visit(ExpressionMethodReference node) {
		handleTypeArguments(node.typeArguments());
		return true;
	}

	@Override
	public boolean visit(SuperMethodReference node) {
		handleTypeArguments(node.typeArguments());
		return true;
	}

	@Override
	public boolean visit(CreationReference node) {
		handleTypeArguments(node.typeArguments());
		return true;
	}

	private void handleTypeArguments(List<Type> typeArguments) {
		if (typeArguments.isEmpty())
			return;
		prepareElementsList(typeArguments, TokenNameCOMMA, TokenNameLESS);
		handleWrap(this.options.alignment_for_type_arguments);
	}

	@Override
	public boolean visit(ExportsDirective node) {
		handleModuleStatement(node.modules(), TokenNameto);
		return true;
	}

	@Override
	public boolean visit(OpensDirective node) {
		handleModuleStatement(node.modules(), TokenNameto);
		return true;
	}

	@Override
	public boolean visit(ProvidesDirective node) {
		handleModuleStatement(node.implementations(), TokenNamewith);
		return true;
	}

	private void handleModuleStatement(List<Name> names, int joiningTokenType) {
		if (names.isEmpty())
			return;
		int joiningTokenIndex = this.tm.firstIndexBefore(names.get(0), joiningTokenType);
		this.wrapParentIndex = this.tm.firstIndexBefore(names.get(0), TokenNameIdentifier);
		this.wrapIndexes.add(joiningTokenIndex);
		prepareElementsList(names, TokenNameCOMMA, -1);
		handleWrap(this.options.alignment_for_module_statements, PREFERRED);
	}

	@Override
	public boolean visit(CatchClause node) {
		int lParen = this.tm.firstIndexIn(node, TokenNameLPAREN);
		int rParen = this.tm.firstIndexBefore(node.getBody(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_catch_clause);
		return true;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		int lParen = this.tm.firstIndexIn(node, TokenNameLPAREN);
		int rParen = this.tm.firstIndexAfter(node.getExpression(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_switch_statement);
		return true;
	}

	@Override
	public boolean visit(SwitchExpression node) {
		int lParen = this.tm.firstIndexIn(node, TokenNameLPAREN);
		int rParen = this.tm.firstIndexAfter(node.getExpression(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_switch_statement);
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		int lParen = this.tm.firstIndexBefore(node.getExpression(), TokenNameLPAREN);
		int rParen = this.tm.firstIndexAfter(node.getExpression(), TokenNameRPAREN);
		handleParenthesesPositions(lParen, rParen, this.options.parenthesis_positions_in_if_while_statement);
		return true;
	}

	/**
	 * Makes sure all new lines within given node will have wrap policy so that
	 * wrap executor will fix their indentation if necessary.
	 */
	void forceContinuousWrapping(ASTNode node, int parentIndex) {
		int parentIndent = this.tm.get(parentIndex).getIndent();
		int indentChange = -parentIndent;
		int lineStart = this.tm.findFirstTokenInLine(parentIndex);
		for (int i = parentIndex; i >= lineStart; i--) {
			int align = this.tm.get(i).getAlign();
			if (align > 0) {
				indentChange = -2 * parentIndent + align;
				break;
			}
		}

		Token previous = null;
		int from = this.tm.firstIndexIn(node, -1);
		int to = this.tm.lastIndexIn(node, -1);
		for (int i = from; i <= to; i++) {
			Token token = this.tm.get(i);
			if ((token.getLineBreaksBefore() > 0 || (previous != null && previous.getLineBreaksAfter() > 0))
					&& (token.getWrapPolicy() == null || token.getWrapPolicy().wrapMode == WrapMode.BLOCK_INDENT)) {
				int extraIndent = token.getIndent() + indentChange;
				token.setWrapPolicy(new WrapPolicy(WrapMode.BLOCK_INDENT, parentIndex, extraIndent));
				token.setIndent(parentIndent + extraIndent);
			}
			previous = token;
		}
	}

	private void handleVariableDeclarations(List<VariableDeclarationFragment> fragments) {
		if (fragments.size() > 1) {
			this.wrapParentIndex = this.tm.firstIndexIn(fragments.get(0), -1);
			prepareElementsList(fragments, TokenNameCOMMA, -1);
			this.wrapIndexes.remove(0);
			handleWrap(this.options.alignment_for_multiple_fields);
		}
	}

	private void handleArguments(List<? extends ASTNode> arguments, int wrappingOption) {
		this.wrapPenalties.add(1 / PREFERRED);
		prepareElementsList(arguments, TokenNameCOMMA, TokenNameLPAREN);
		handleWrap(wrappingOption);
	}

	private void prepareElementsList(List<? extends ASTNode> elements, int separatorType, int wrapParentType) {
		for (int i = 0; i < elements.size(); i++) {
			ASTNode element = elements.get(i);
			this.wrapIndexes.add(this.tm.firstIndexIn(element, -1));
			if (i > 0)
				this.secondaryWrapIndexes.add(this.tm.firstIndexBefore(element, separatorType));
		}
		// wrapIndexes may have been filled with additional values even if arguments is empty
		if (!this.wrapIndexes.isEmpty()) {
			Token firstToken = this.tm.get(this.wrapIndexes.get(0));
			if (this.wrapParentIndex < 0)
				this.wrapParentIndex = this.tm.findIndex(firstToken.originalStart - 1, wrapParentType, false);
			if (!elements.isEmpty() && this.wrapGroupEnd < 0)
				this.wrapGroupEnd = this.tm.lastIndexIn(elements.get(elements.size() - 1), -1);
		}
	}

	private void handleWrap(int wrappingOption) {
		handleWrap(wrappingOption, null);
	}

	private void handleWrap(int wrappingOption, float firstPenaltyMultiplier) {
		this.wrapPenalties.add(firstPenaltyMultiplier);
		handleWrap(wrappingOption, null);
	}

	private void handleWrap(int wrappingOption, ASTNode parentNode) {
		handleWrap(wrappingOption, true, parentNode);
	}

	private void handleWrap(int wrappingOption, boolean wrapPreceedingComments, ASTNode parentNode) {
		doHandleWrap(wrappingOption, wrapPreceedingComments, parentNode);
		this.wrapIndexes.clear();
		this.secondaryWrapIndexes.clear();
		this.wrapPenalties.clear();
		this.wrapParentIndex = this.wrapGroupEnd = -1;
	}

	private void doHandleWrap(int wrappingOption, boolean wrapPreceedingComments, ASTNode parentNode) {
		if (this.wrapIndexes.isEmpty())
			return;
		assert this.wrapParentIndex >= 0 && this.wrapParentIndex < this.wrapIndexes.get(0);
		assert this.wrapGroupEnd >= this.wrapIndexes.get(this.wrapIndexes.size() - 1);

		while (this.tm.get(this.wrapParentIndex).isComment() && this.wrapParentIndex > 0)
			this.wrapParentIndex--;

		float penalty = this.wrapPenalties.isEmpty() ? 1 : this.wrapPenalties.get(0);
		WrapPolicy policy = getWrapPolicy(wrappingOption, penalty, true, parentNode);

		WrapPolicy existing = this.tm.get(this.wrapIndexes.get(0)).getWrapPolicy();
		if (existing != null && existing.wrapMode == WrapMode.TOP_PRIORITY) {
			// SEPARATE_LINES_IF_WRAPPED
			assert existing.wrapParentIndex == this.wrapParentIndex;
			this.wrapGroupEnd = existing.groupEndIndex;
			policy = new WrapPolicy(WrapMode.TOP_PRIORITY, policy.wrapParentIndex, this.wrapGroupEnd, policy.extraIndent,
					policy.structureDepth, policy.penaltyMultiplier, true, policy.indentOnColumn);
		}

		setTokenWrapPolicy(0, policy, true);

		for (int i = 1; i < this.wrapIndexes.size(); i++) {
			penalty = this.wrapPenalties.size() > i ? this.wrapPenalties.get(i) : 1;
			if (penalty != policy.penaltyMultiplier || i == 1)
				policy = getWrapPolicy(wrappingOption, penalty, false, parentNode);
			setTokenWrapPolicy(i, policy, wrapPreceedingComments);
		}

		if (!this.secondaryWrapIndexes.isEmpty()) {
			int optionNoAlignment = (wrappingOption & ~Alignment.SPLIT_MASK) | Alignment.M_NO_ALIGNMENT;
			policy = getWrapPolicy(optionNoAlignment, 1, false, parentNode);
			for (int index : this.secondaryWrapIndexes) {
				Token token = this.tm.get(index);
				if (token.getWrapPolicy() == null)
					token.setWrapPolicy(policy);
			}
		}
	}

	private void setTokenWrapPolicy(int wrapIndexesIndex, WrapPolicy policy, boolean wrapPreceedingComments) {
		int index = this.wrapIndexes.get(wrapIndexesIndex);
		if (wrapPreceedingComments) {
			for (int i = index - 1; i >= 0; i--) {
				Token previous = this.tm.get(i);
				if (!previous.isComment())
					break;
				if (previous.getWrapPolicy() == WrapPolicy.FORCE_FIRST_COLUMN)
					break;
				if (previous.getLineBreaksAfter() == 0 && i == index - 1)
					index = i;
				if (previous.getLineBreaksBefore() > 0)
					previous.setWrapPolicy(policy);
			}
			this.wrapIndexes.set(wrapIndexesIndex, index);
		}

		Token token = this.tm.get(index);
		if (token.getWrapPolicy() == WrapPolicy.DISABLE_WRAP)
			return;

		token.setWrapPolicy(policy);
		if (policy.wrapMode == WrapMode.FORCE) {
			token.breakBefore();
		} else if (this.options.join_wrapped_lines && token.tokenType == TokenNameCOMMENT_BLOCK) {
			// allow wrap preparator to decide if this comment should be wrapped
			token.clearLineBreaksBefore();
		}
	}

	private WrapPolicy getWrapPolicy(int wrappingOption, float penaltyMultiplier, boolean isFirst, ASTNode parentNode) {
		assert this.wrapParentIndex >= 0 && this.wrapGroupEnd >= 0;
		int extraIndent = this.options.continuation_indentation;
		boolean indentOnColumn = (wrappingOption & Alignment.M_INDENT_ON_COLUMN) != 0;
		boolean isForceWrap = (wrappingOption & Alignment.M_FORCE) != 0;
		boolean isAlreadyWrapped = false;
		if (indentOnColumn) {
			extraIndent = 0;
		} else if (parentNode instanceof EnumDeclaration) {
			// special behavior for compatibility with legacy formatter
			extraIndent = ((wrappingOption & Alignment.M_INDENT_BY_ONE) != 0) ? 2 : 1;
			if (!this.options.indent_body_declarations_compare_to_enum_declaration_header)
				extraIndent--;
			isAlreadyWrapped = isFirst;
		} else if (parentNode instanceof IfStatement || parentNode instanceof ForStatement
				|| parentNode instanceof EnhancedForStatement || parentNode instanceof WhileStatement) {
			extraIndent = 1;
			this.wrapParentIndex = this.tm.firstIndexIn(parentNode, -1); // only if !indoentOnColumn
		} else if (parentNode instanceof DoStatement) {
			extraIndent = 0;
			this.wrapParentIndex = this.tm.firstIndexIn(parentNode, -1); // only if !indoentOnColumn
		} else if (parentNode instanceof LambdaExpression) {
			extraIndent = 1;
		} else if ((wrappingOption & Alignment.M_INDENT_BY_ONE) != 0) {
			extraIndent = 1;
		} else if (parentNode instanceof ArrayInitializer) {
			extraIndent = this.options.continuation_indentation_for_array_initializer;
			isAlreadyWrapped = isFirst && this.options.insert_new_line_after_opening_brace_in_array_initializer;
		}

		WrapMode wrapMode = WrapMode.WHERE_NECESSARY;
		boolean isTopPriority = false;
		switch (wrappingOption & Alignment.SPLIT_MASK) {
			case Alignment.M_NO_ALIGNMENT:
				wrapMode = WrapMode.DISABLED;
				isForceWrap = false;
				break;
			case Alignment.M_COMPACT_FIRST_BREAK_SPLIT:
				isTopPriority = isFirst;
				isForceWrap &= isFirst;
				break;
			case Alignment.M_ONE_PER_LINE_SPLIT:
				isTopPriority = true;
				break;
			case Alignment.M_NEXT_SHIFTED_SPLIT:
				isTopPriority = true;
				if (!isFirst)
					extraIndent++;
				break;
			case Alignment.M_NEXT_PER_LINE_SPLIT:
				isTopPriority = !isFirst;
				isForceWrap &= !isFirst;
				break;
		}

		if (isForceWrap) {
			wrapMode = WrapMode.FORCE;
		} else if (isAlreadyWrapped) {
			wrapMode = WrapMode.DISABLED; // to avoid triggering top priority wrapping
		} else if (isTopPriority) { 
			wrapMode = WrapMode.TOP_PRIORITY;
		}
		extraIndent *= this.options.indentation_size;
		return new WrapPolicy(wrapMode, this.wrapParentIndex, this.wrapGroupEnd, extraIndent, this.currentDepth,
				penaltyMultiplier, isFirst, indentOnColumn);
	}

	public void finishUp(ASTNode astRoot, List<IRegion> regions) {
		preserveExistingLineBreaks();
		applyBreaksOutsideRegions(regions);
		new WrapExecutor(this.tm, this.options, regions).executeWraps();
		this.aligner.alignComments();
		wrapComments();
		fixEnumConstantIndents(astRoot);
	}

	private void preserveExistingLineBreaks() {
		// normally n empty lines = n+1 line breaks, but not at the file start and end
		Token first = this.tm.get(0);
		int startingBreaks = first.getLineBreaksBefore();
		first.clearLineBreaksBefore();
		first.putLineBreaksBefore(startingBreaks - 1);

		this.tm.traverse(0, new TokenTraverser() {
			boolean join_wrapped_lines = WrapPreparator.this.options.join_wrapped_lines;

			@Override
			protected boolean token(Token token, int index) {
				int lineBreaks = getLineBreaksToPreserve(getPrevious(), token);
				if (lineBreaks > 1 || (!this.join_wrapped_lines && token.isWrappable()) || index == 0)
					token.putLineBreaksBefore(lineBreaks);
				return true;
			}

		});

		Token last = this.tm.get(this.tm.size() - 1);
		last.clearLineBreaksAfter();
		int endingBreaks = getLineBreaksToPreserve(last, null);
		if (endingBreaks > 0) {
			last.putLineBreaksAfter(endingBreaks);
		} else if ((this.kind & (CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.K_MODULE_INFO)) != 0
				&& this.options.insert_new_line_at_end_of_file_if_missing) {
			last.breakAfter();
		}
	}

	int getLineBreaksToPreserve(Token token1, Token token2) {
		if ((token1 != null && !token1.isPreserveLineBreaksAfter())
				|| (token2 != null && !token2.isPreserveLineBreaksBefore())) {
			return 0;
		}
		if (token1 != null) {
			List<Token> structure = token1.getInternalStructure();
			if (structure != null && !structure.isEmpty())
				token1 = structure.get(structure.size() - 1);
		}
		if (token2 != null) {
			List<Token> structure = token2.getInternalStructure();
			if (structure != null && !structure.isEmpty())
				token2 = structure.get(0);
		}
		int lineBreaks = WrapPreparator.this.tm.countLineBreaksBetween(token1, token2);
		int toPreserve = this.options.number_of_empty_lines_to_preserve;
		if (token1 != null && token2 != null)
			toPreserve++; // n empty lines = n+1 line breaks, except for file start and end
		return Math.min(lineBreaks, toPreserve);
	}

	private void applyBreaksOutsideRegions(List<IRegion> regions) {
		String source = this.tm.getSource();
		int previousRegionEnd = 0;
		for (IRegion region : regions) {
			int index = this.tm.findIndex(previousRegionEnd, -1, true);
			Token token = this.tm.get(index);
			if (this.tm.countLineBreaksBetween(source, previousRegionEnd,
					Math.min(token.originalStart, region.getOffset())) > 0)
				token.breakBefore();
			for (index++; index < this.tm.size(); index++) {
				Token next = this.tm.get(index);
				if (next.originalStart > region.getOffset()) {
					if (this.tm.countLineBreaksBetween(source, token.originalEnd, region.getOffset()) > 0)
						next.breakBefore();
					break;
				}
				if (this.tm.countLineBreaksBetween(token, next) > 0)
					next.breakBefore();
				token = next;
			}
			previousRegionEnd = region.getOffset() + region.getLength() - 1;
		}
	}

	private void wrapComments() {
		CommentWrapExecutor commentWrapper = new CommentWrapExecutor(this.tm, this.options);
		boolean isNLSTagInLine = false;
		for (int i = 0; i < this.tm.size(); i++) {
			Token token = this.tm.get(i);
			if (token.getLineBreaksBefore() > 0 || token.getLineBreaksAfter() > 0)
				isNLSTagInLine = false;
			if (token.hasNLSTag()) {
				assert token.tokenType == TokenNameStringLiteral;
				isNLSTagInLine = true;
			}
			List<Token> structure = token.getInternalStructure();
			if (token.isComment() && structure != null && !structure.isEmpty() && !isNLSTagInLine) {
				int startPosition = this.tm.getPositionInLine(i);
				if (token.tokenType == TokenNameCOMMENT_LINE) {
					commentWrapper.wrapLineComment(token, startPosition);
				} else {
					assert token.tokenType == TokenNameCOMMENT_BLOCK || token.tokenType == TokenNameCOMMENT_JAVADOC;
					commentWrapper.wrapMultiLineComment(token, startPosition, false, false);
				}
			}
		}
	}

	private void fixEnumConstantIndents(ASTNode astRoot) {
		if (this.options.use_tabs_only_for_leading_indentations) {
			// enum constants should be indented like other declarations, not like wrapped elements
			astRoot.accept(new ASTVisitor() {

				@Override
				public boolean visit(EnumConstantDeclaration node) {
					WrapPreparator.this.tm.firstTokenIn(node, -1).setWrapPolicy(null);
					return true;
				}
			});
		}
	}

	private void handleParenthesesPositions(int openingParenIndex, int closingParenIndex, String positionsSetting) {
		boolean isEmpty = openingParenIndex + 1 == closingParenIndex;
		switch (positionsSetting) {
			case DefaultCodeFormatterConstants.COMMON_LINES:
				// nothing to do
				break;
			case DefaultCodeFormatterConstants.SEPARATE_LINES_IF_WRAPPED:
				if (isEmpty)
					break;
				this.tm.get(openingParenIndex + 1).setWrapPolicy(new WrapPolicy(WrapMode.TOP_PRIORITY,
						openingParenIndex, closingParenIndex, this.options.indentation_size, 1, 1, true, false));
				this.tm.get(closingParenIndex).setWrapPolicy(new WrapPolicy(WrapMode.TOP_PRIORITY,
						openingParenIndex, closingParenIndex, 0, 1, 1, false, false));
				break;
			case DefaultCodeFormatterConstants.SEPARATE_LINES_IF_NOT_EMPTY:
				if (isEmpty)
					break;
				//$FALL-THROUGH$
			case DefaultCodeFormatterConstants.SEPARATE_LINES:
			case DefaultCodeFormatterConstants.PRESERVE_POSITIONS:
				boolean always = !positionsSetting.equals(DefaultCodeFormatterConstants.PRESERVE_POSITIONS);
				Token afterOpening = this.tm.get(openingParenIndex + 1);
				if (always || this.tm.countLineBreaksBetween(this.tm.get(openingParenIndex), afterOpening) > 0) {
					afterOpening.setWrapPolicy(
							new WrapPolicy(WrapMode.WHERE_NECESSARY, openingParenIndex, this.options.indentation_size));
					afterOpening.breakBefore();
				}
				Token closingParen = this.tm.get(closingParenIndex);
				if (always || this.tm.countLineBreaksBetween(this.tm.get(closingParenIndex - 1), closingParen) > 0) {
					closingParen.setWrapPolicy(new WrapPolicy(WrapMode.WHERE_NECESSARY, openingParenIndex, 0));
					closingParen.breakBefore();
				}
				break;
			default:
				throw new IllegalArgumentException("Unrecognized parentheses positions setting: " + positionsSetting); //$NON-NLS-1$
		}
	}
}
