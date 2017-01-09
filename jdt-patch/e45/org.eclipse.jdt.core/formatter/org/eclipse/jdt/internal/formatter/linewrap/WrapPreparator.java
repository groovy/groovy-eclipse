/*******************************************************************************
 * Copyright (c) 2014, 2016 Mateusz Matela and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameLBRACE;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameLPAREN;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameOR;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameQUESTION;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameRBRACE;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameRPAREN;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameSEMICOLON;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameStringLiteral;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameextends;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameimplements;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameIdentifier;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamenew;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamesuper;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamethis;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamethrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.formatter.CodeFormatter;
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

	private final static Map<Operator, Integer> OPERATOR_PRECEDENCE;
	static {
		HashMap<Operator, Integer> precedence = new HashMap<Operator, Integer>();
		precedence.put(Operator.TIMES, 1);
		precedence.put(Operator.DIVIDE, 1);
		precedence.put(Operator.REMAINDER, 1);
		precedence.put(Operator.PLUS, 2);
		precedence.put(Operator.MINUS, 2);
		// shift and comparison operators left out intentionally for compatibility with
		// the legacy formatter, which did not wrap these operators
		precedence.put(Operator.AND, 6);
		precedence.put(Operator.XOR, 7);
		precedence.put(Operator.OR, 8);
		precedence.put(Operator.CONDITIONAL_AND, 9);
		precedence.put(Operator.CONDITIONAL_OR, 10);
		// ternary and assignment operators not relevant to infix expressions
		OPERATOR_PRECEDENCE = Collections.unmodifiableMap(precedence);
	}

	/** Penalty multiplier for wraps that are preferred */
	private final static float PREFERRED = 7f / 8;

	final TokenManager tm;
	final DefaultCodeFormatterOptions options;
	final int kind;

	final FieldAligner fieldAligner;

	int importsStart = -1, importsEnd = -1;

	/*
	 * temporary values used when calling {@link #handleWrap(int)} to avoid ArrayList initialization and long lists of
	 * parameters
	 */
	private List<Integer> wrapIndexes = new ArrayList<Integer>();
	/** Indexes for wraps that shouldn't happen but should be indented if cannot be removed */
	private List<Integer> secondaryWrapIndexes = new ArrayList<Integer>();
	private List<Float> wrapPenalties = new ArrayList<Float>();
	private int wrapParentIndex = -1;
	private int wrapGroupEnd = -1;

	private int currentDepth = 0;

	public WrapPreparator(TokenManager tokenManager, DefaultCodeFormatterOptions options, int kind) {
		this.tm = tokenManager;
		this.options = options;
		this.kind = kind;

		this.fieldAligner = new FieldAligner(this.tm);
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
	public boolean visit(CompilationUnit node) {
		List<ImportDeclaration> imports = node.imports();
		if (!imports.isEmpty()) {
			this.importsStart = this.tm.firstIndexIn(imports.get(0), -1);
			this.importsEnd = this.tm.lastIndexIn(imports.get(imports.size() - 1), -1);
		}
		return true;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		handleArguments(node.values(), this.options.alignment_for_arguments_in_annotation);
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

		if (this.options.align_type_members_on_columns)
			this.fieldAligner.prepareAlign(node.bodyDeclarations());

		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		if (this.options.align_type_members_on_columns)
			this.fieldAligner.prepareAlign(node.bodyDeclarations());
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (this.options.align_type_members_on_columns)
			this.fieldAligner.prepareAlign(node.bodyDeclarations());
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
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
				this.wrapParentIndex = this.tm.firstIndexAfter(node.getName(), TokenNameLPAREN);
			prepareElementsList(exceptionTypes, TokenNameCOMMA, TokenNameRPAREN);
			// instead of the first exception type, wrap the "throws" token
			this.wrapIndexes.set(0, this.tm.firstIndexBefore(exceptionTypes.get(0), TokenNamethrows));
			handleWrap(wrappingOption, 0.5f);
		}

		if (!node.isConstructor()) {
			this.wrapParentIndex = this.tm.findFirstTokenInLine(this.tm.firstIndexIn(node.getName(), -1));
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
					? this.tm.firstIndexBefore(enumConstants.get(0), TokenNameLBRACE) : this.tm.firstIndexIn(node, -1);
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

		if (this.options.align_type_members_on_columns)
			this.fieldAligner.prepareAlign(node.bodyDeclarations());

		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_enum_constant);
		AnonymousClassDeclaration anonymousClass = node.getAnonymousClassDeclaration();
		if (anonymousClass != null) {
			forceContinuousWrapping(anonymousClass, this.tm.firstIndexIn(node.getName(), -1));
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_method_invocation);

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
			this.wrapGroupEnd = this.tm.firstIndexIn(node.getName(), -1);
			handleWrap(this.options.alignment_for_selector_in_method_invocation);
		}
		return true;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_method_invocation);
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		AnonymousClassDeclaration anonymousClass = node.getAnonymousClassDeclaration();
		if (anonymousClass != null) {
			forceContinuousWrapping(anonymousClass, this.tm.firstIndexIn(node, TokenNamenew));
		}

		int wrappingOption = node.getExpression() != null
				? this.options.alignment_for_arguments_in_qualified_allocation_expression
				: this.options.alignment_for_arguments_in_allocation_expression;
		handleArguments(node.arguments(), wrappingOption);
		return true;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_explicit_constructor_call);
		return true;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		handleArguments(node.arguments(), this.options.alignment_for_arguments_in_explicit_constructor_call);
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

		findTokensToWrap(node, 0);
		this.wrapParentIndex = this.wrapIndexes.remove(0);
		this.wrapGroupEnd = this.tm.lastIndexIn(node, -1);
		if ((this.options.alignment_for_binary_expression & Alignment.M_INDENT_ON_COLUMN) != 0
				&& this.wrapParentIndex > 0)
			this.wrapParentIndex--;
		for (int i = this.wrapParentIndex; i >= 0; i--) {
			if (!this.tm.get(i).isComment()) {
				this.wrapParentIndex = i;
				break;
			}
		}
		handleWrap(this.options.alignment_for_binary_expression, node);
		return true;
	}

	private void findTokensToWrap(InfixExpression node, int depth) {
		Expression left = node.getLeftOperand();
		if (left instanceof InfixExpression && samePrecedence(node, (InfixExpression) left)) {
			findTokensToWrap((InfixExpression) left, depth + 1);
		} else if (this.wrapIndexes.isEmpty() // always add first operand, it will be taken as wrap parent
				|| !this.options.wrap_before_binary_operator) {
			this.wrapIndexes.add(this.tm.firstIndexIn(left, -1));
		}

		Expression right = node.getRightOperand();
		List<Expression> extended = node.extendedOperands();
		for (int i = -1; i < extended.size(); i++) {
			Expression operand = (i == -1) ? right : extended.get(i);
			if (operand instanceof InfixExpression && samePrecedence(node, (InfixExpression) operand)) {
				findTokensToWrap((InfixExpression) operand, depth + 1);
			}
			int indexBefore = this.tm.firstIndexBefore(operand, -1);
			while (this.tm.get(indexBefore).isComment())
				indexBefore--;
			assert node.getOperator().toString().equals(this.tm.toString(indexBefore));
			int indexAfter = this.tm.firstIndexIn(operand, -1);
			this.wrapIndexes.add(this.options.wrap_before_binary_operator ? indexBefore : indexAfter);
			this.secondaryWrapIndexes.add(this.options.wrap_before_binary_operator ? indexAfter : indexBefore);

			if (!this.options.join_wrapped_lines) {
				// TODO there should be an option for never joining wraps on opposite side of the operator
				if (this.options.wrap_before_binary_operator) {
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
		this.wrapIndexes.add(this.tm.firstIndexAfter(node.getExpression(), TokenNameQUESTION));
		this.wrapIndexes.add(this.tm.firstIndexAfter(node.getThenExpression(), TokenNameCOLON));
		this.secondaryWrapIndexes.add(this.tm.firstIndexIn(node.getThenExpression(), -1));
		this.secondaryWrapIndexes.add(this.tm.firstIndexIn(node.getElseExpression(), -1));
		this.wrapParentIndex = this.tm.lastIndexIn(node.getExpression(), -1);
		this.wrapGroupEnd = this.tm.lastIndexIn(node, -1);
		handleWrap(this.options.alignment_for_conditional_expression);
		return true;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		List<Expression> expressions = node.expressions();
		if (!expressions.isEmpty()) {
			prepareElementsList(expressions, TokenNameCOMMA, TokenNameLBRACE);
			handleWrap(this.options.alignment_for_expressions_in_array_initializer, node);
		}
		if (!this.options.join_wrapped_lines
				&& !this.options.insert_new_line_before_closing_brace_in_array_initializer) {
			// if there is a line break before the closing brace, formatter should treat it as a valid wrap to preserve
			int closingBraceIndex = this.tm.lastIndexIn(node, TokenNameRBRACE);
			Token closingBrace = this.tm.get(closingBraceIndex);
			if (this.tm.countLineBreaksBetween(this.tm.get(closingBraceIndex - 1), closingBrace) == 1) {
				int openingBraceIndex = this.tm.firstIndexIn(node, TokenNameLBRACE);
				closingBrace.setWrapPolicy(new WrapPolicy(WrapMode.WHERE_NECESSARY, openingBraceIndex,
						closingBraceIndex, 0, this.currentDepth, 1, true, false));
			}
		}
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		int wrapIndex = this.tm.firstIndexIn(node.getRightHandSide(), -1);
		if (this.tm.get(wrapIndex).getLineBreaksBefore() > 0)
			return true;

		int operatorIndex = this.tm.firstIndexBefore(node.getRightHandSide(), -1);
		while (this.tm.get(operatorIndex).isComment())
			operatorIndex--;
		assert node.getOperator().toString().equals(this.tm.toString(operatorIndex));

		this.wrapIndexes.add(wrapIndex);
		this.secondaryWrapIndexes.add(operatorIndex);
		this.wrapParentIndex = operatorIndex - 1;
		this.wrapGroupEnd = this.tm.lastIndexIn(node.getRightHandSide(), -1);
		handleWrap(this.options.alignment_for_assignment);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (node.getInitializer() == null)
			return true;
		int wrapIndex = this.tm.firstIndexIn(node.getInitializer(), -1);
		if (this.tm.get(wrapIndex).getLineBreaksBefore() > 0)
			return true;
		int equalIndex = this.tm.firstIndexBefore(node.getInitializer(), TokenNameEQUAL);

		this.wrapIndexes.add(wrapIndex);
		this.secondaryWrapIndexes.add(equalIndex);
		this.wrapParentIndex = equalIndex - 1;
		this.wrapGroupEnd = this.tm.lastIndexIn(node.getInitializer(), -1);
		handleWrap(this.options.alignment_for_assignment);
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		if (!(node.getThenStatement() instanceof Block)) {
			int thenIndex = this.tm.firstIndexIn(node.getThenStatement(), -1);
			if (this.tm.get(thenIndex).getLineBreaksBefore() == 0) {
				this.wrapIndexes.add(thenIndex);
				this.wrapParentIndex = this.tm.firstIndexAfter(node.getExpression(), TokenNameRPAREN);
				this.wrapGroupEnd = this.tm.lastIndexIn(node.getThenStatement(), -1);
				handleWrap(this.options.alignment_for_compact_if, node);
			}
		}
		Statement elseStatement = node.getElseStatement();
		if (elseStatement != null && !(elseStatement instanceof Block) && !(elseStatement instanceof IfStatement)) {
			int elseIndex = this.tm.firstIndexIn(elseStatement, -1);
			if (this.tm.get(elseIndex).getLineBreaksBefore() == 0) {
				this.wrapIndexes.add(elseIndex);
				this.wrapParentIndex = this.tm.firstIndexAfter(node.getExpression(), TokenNameRPAREN);
				this.wrapGroupEnd = this.tm.lastIndexIn(elseStatement, -1);
				handleWrap(this.options.alignment_for_compact_if, node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(TryStatement node) {
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
		if (node.getBody() instanceof Block) {
			forceContinuousWrapping(node.getBody(), this.tm.firstIndexIn(node, -1));
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

	/**
	 * Makes sure all new lines within given node will have wrap policy so that
	 * wrap executor will fix their indentation if necessary.
	 */
	private void forceContinuousWrapping(ASTNode node, int parentIndex) {
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
					&& (token.getWrapPolicy() == null || token.getWrapPolicy().wrapMode == WrapMode.FORCED)) {
				int extraIndent = token.getIndent() + indentChange;
				token.setWrapPolicy(new WrapPolicy(WrapMode.FORCED, parentIndex, extraIndent));
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
		doHandleWrap(wrappingOption, parentNode);
		this.wrapIndexes.clear();
		this.secondaryWrapIndexes.clear();
		this.wrapPenalties.clear();
		this.wrapParentIndex = this.wrapGroupEnd = -1;
	}

	private void doHandleWrap(int wrappingOption, ASTNode parentNode) {
		if (this.wrapIndexes.isEmpty())
			return;
		assert this.wrapParentIndex >= 0 && this.wrapParentIndex < this.wrapIndexes.get(0);
		assert this.wrapGroupEnd >= this.wrapIndexes.get(this.wrapIndexes.size() - 1);
		float penalty = this.wrapPenalties.isEmpty() ? 1 : this.wrapPenalties.get(0);
		WrapPolicy policy = getWrapPolicy(wrappingOption, penalty, true, parentNode);
		if (policy == null)
			return;

		setTokenWrapPolicy(this.wrapIndexes.get(0), policy, true);

		boolean wrapPreceedingComments = !(parentNode instanceof InfixExpression)
				|| !this.options.wrap_before_binary_operator;
		for (int i = 1; i < this.wrapIndexes.size(); i++) {
			penalty = this.wrapPenalties.size() > i ? this.wrapPenalties.get(i) : 1;
			if (penalty != policy.penaltyMultiplier || i == 1)
				policy = getWrapPolicy(wrappingOption, penalty, false, parentNode);
			setTokenWrapPolicy(this.wrapIndexes.get(i), policy, wrapPreceedingComments);
		}

		boolean forceWrap = (wrappingOption & Alignment.M_FORCE) != 0;
		if (forceWrap) {
			boolean satisfied = false;
			for (int index : this.wrapIndexes) {
				Token token = this.tm.get(index);
				if (token.getWrapPolicy().wrapMode == WrapMode.TOP_PRIORITY) {
					token.breakBefore();
					satisfied = true;
				}
			}
			if (!satisfied) {
				boolean canWrapFirst = (wrappingOption
						& Alignment.M_NEXT_PER_LINE_SPLIT) != Alignment.M_NEXT_PER_LINE_SPLIT;
				if (canWrapFirst)
					this.tm.get(this.wrapIndexes.get(0)).breakBefore();
			}
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

	private void setTokenWrapPolicy(int index, WrapPolicy policy, boolean wrapPreceedingComments) {
		if (wrapPreceedingComments) {
			for (int i = index - 1; i >= 0; i--) {
				Token previous = this.tm.get(i);
				if (!previous.isComment())
					break;
				if (previous.getLineBreaksAfter() == 0 && i == index - 1)
					index = i;
				if (previous.getLineBreaksBefore() > 0)
					previous.setWrapPolicy(policy);
			}
		}

		Token token = this.tm.get(index);
		token.setWrapPolicy(policy);

		if (this.options.join_wrapped_lines && token.tokenType == TokenNameCOMMENT_BLOCK) {
			// allow wrap preparator to decide if this comment should be wrapped
			token.clearLineBreaksBefore();
		}
	}

	private WrapPolicy getWrapPolicy(int wrappingOption, float penaltyMultiplier, boolean isFirst, ASTNode parentNode) {
		assert this.wrapParentIndex >= 0 && this.wrapGroupEnd >= 0;
		int extraIndent = this.options.continuation_indentation;
		boolean indentOnColumn = (wrappingOption & Alignment.M_INDENT_ON_COLUMN) != 0;
		boolean isAlreadyWrapped = false;
		if (indentOnColumn) {
			extraIndent = 0;
		} else if (parentNode instanceof EnumDeclaration) {
			// special behavior for compatibility with legacy formatter
			extraIndent = ((wrappingOption & Alignment.M_INDENT_BY_ONE) != 0) ? 2 : 1;
			isAlreadyWrapped = isFirst;
		} else if (parentNode instanceof IfStatement) {
			extraIndent = 1;
			this.wrapParentIndex = this.tm.firstIndexIn(parentNode, -1); // only if !indoentOnColumn
		} else if ((wrappingOption & Alignment.M_INDENT_BY_ONE) != 0) {
			extraIndent = 1;
		} else if (parentNode instanceof ArrayInitializer) {
			extraIndent = this.options.continuation_indentation_for_array_initializer;
		}

		WrapMode wrapMode = WrapMode.WHERE_NECESSARY;
		boolean isTopPriority = false;
		switch (wrappingOption & Alignment.SPLIT_MASK) {
			case Alignment.M_NO_ALIGNMENT:
				wrapMode = WrapMode.DISABLED;
				break;
			case Alignment.M_COMPACT_FIRST_BREAK_SPLIT:
				isTopPriority = isFirst;
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
				break;
		}

		if (isAlreadyWrapped)
			isTopPriority = false; // to avoid triggering top priority wrapping
		if (isTopPriority)
			wrapMode = WrapMode.TOP_PRIORITY;
		extraIndent *= this.options.indentation_size;
		return new WrapPolicy(wrapMode, this.wrapParentIndex, this.wrapGroupEnd, extraIndent, this.currentDepth,
				penaltyMultiplier, isFirst, indentOnColumn);
	}

	public void finishUp(ASTNode astRoot, IRegion[] regions) {
		preserveExistingLineBreaks();
		if (regions != null)
			applyBreaksOutsideRegions(regions);
		new WrapExecutor(this.tm, this.options).executeWraps();
		this.fieldAligner.alignComments();
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
			DefaultCodeFormatterOptions options2 = WrapPreparator.this.options;

			@Override
			protected boolean token(Token token, int index) {
				boolean isBetweenImports = index > WrapPreparator.this.importsStart
						&& index < WrapPreparator.this.importsEnd;
				int lineBreaks = getLineBreaksToPreserve(getPrevious(), token, isBetweenImports);
				if (lineBreaks <= getLineBreaksBefore())
					return true;

				if (lineBreaks == 1) {
					if ((!this.options2.join_wrapped_lines && token.isWrappable()) || index == 0)
						token.breakBefore();
				} else if (lineBreaks > 1) {
					token.putLineBreaksBefore(lineBreaks);
				}
				return true;
			}

		});

		Token last = this.tm.get(this.tm.size() - 1);
		last.clearLineBreaksAfter();
		int endingBreaks = getLineBreaksToPreserve(last, null, false);
		if (endingBreaks > 0) {
			last.putLineBreaksAfter(endingBreaks);
		} else if ((this.kind & CodeFormatter.K_COMPILATION_UNIT) != 0
				&& this.options.insert_new_line_at_end_of_file_if_missing) {
			last.breakAfter();
		}
	}

	int getLineBreaksToPreserve(Token token1, Token token2, boolean isBetweenImports) {
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
		if (isBetweenImports)
			return lineBreaks > 1 ? (this.options.blank_lines_between_import_groups + 1) : 0;

		int toPreserve = this.options.number_of_empty_lines_to_preserve;
		if (token1 != null && token2 != null)
			toPreserve++; // n empty lines = n+1 line breaks, except for file start and end
		if (token1 != null && token1.tokenType == Token.TokenNameEMPTY_LINE)
			toPreserve--;
		return Math.min(lineBreaks, toPreserve);
	}

	private void applyBreaksOutsideRegions(IRegion[] regions) {
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
			if (structure != null && !structure.isEmpty() && !isNLSTagInLine) {
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
					WrapPreparator.this.tm.firstTokenIn(node, TokenNameIdentifier).setWrapPolicy(null);
					return true;
				}
			});
		}
	}
}
