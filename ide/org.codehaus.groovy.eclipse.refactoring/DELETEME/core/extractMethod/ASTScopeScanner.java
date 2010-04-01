/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.extractMethod;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTVisitorDecorator;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

/**
 * @author Michael Klenk mklenk@hsr.ch AST Visitor, which counts all variables
 *         in the given node split in: - declaratedVariables Variables
 *         declarated in this Block - declaratedInblockVariables Variables
 *         declarated in an inner Block - usedVariables Variables which are read
 *         in this block - returnVariables Variables which are assigned to a new
 *         value in this Block
 */
public class ASTScopeScanner {

	private static final int declaratedVariables = 0;
	private static final int declaratedInblockVariables = 1;
	private static final int usedVariables = 2;
	private static final int returnVariables = 3;
	private static final int innerLoopAssignedVariables = 4;

	private List<LinkedHashSet<Variable>> variables;

	private boolean selectionIsInLoopOrClosure = false;

	/**
	 * Constructor to initialize the Scanner
	 */
	public ASTScopeScanner(boolean blockIsInLoop) {
		selectionIsInLoopOrClosure = blockIsInLoop;
		variables = new ArrayList<LinkedHashSet<Variable>>();
		for (int i = 0; i < 5; i++) {
			variables.add(new LinkedHashSet<Variable>());
		}
	}

	/**
	 * @return all new declared variables in the visited node
	 */
	public List<Variable> getDeclaratedVariables() {
		return getVariables(declaratedVariables);
	}

	/**
	 * @return all read variables in the visted node
	 */
	public List<Variable> getUsedVariables() {
		return getVariables(usedVariables);
	}

	/**
	 * @return all variables which are assigned to a new Value a =, a++, a--
	 */
	public List<Variable> getReturnVariables() {
		return getVariables(returnVariables);
	}

	/**
	 * @return all variables which are assigned in an innerLoop
	 */
	public List<Variable> getInnerLoopAssignedVariables() {
		return getVariables(innerLoopAssignedVariables);
	}

	public void visitNode(ASTNode node) {
		if (selectionIsInLoopOrClosure)
			node.visit(new RepeatableBlockVisit(this));
		else
			node.visit(new DefaultVisit(this));
	}

	private List<Variable> getVariables(int pos) {
		// Remove null Variables which are found in the AST
		variables.get(pos).remove(null);
		List<Variable> vec = new ArrayList<Variable>();
		for (Variable v : variables.get(pos)) {
			vec.add(v);
		}
		return vec;
	}

	private class DefaultVisit extends ASTVisitorDecorator<ASTScopeScanner> {
		public DefaultVisit(ASTScopeScanner container) {
			super(container);
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
			super.visitVariableExpression(expression);
			Variable var = expression.getAccessedVariable();
			if (!(var instanceof FieldNode)) {
				if (!variables.get(declaratedVariables).contains(var)) {
					variables.get(usedVariables).add(var);
				}
			}
		}

		@Override
		public void visitDeclarationExpression(DeclarationExpression expression) {
			Variable var = expression.getVariableExpression()
					.getAccessedVariable();
			variables.get(declaratedVariables).add(var);
			super.visitDeclarationExpression(expression);
		}

		@Override
		public void visitForLoop(ForStatement forLoop) {
			variables.get(declaratedInblockVariables)
					.add(forLoop.getVariable());
			forLoop.getCollectionExpression().visit(
					new RepeatableBlockVisit(container));
			forLoop.getLoopBlock().visit(new RepeatableBlockVisit(container));
		}

		@Override
		public void visitWhileLoop(WhileStatement loop) {
			loop.getLoopBlock().visit(new RepeatableBlockVisit(container));
		}

		@Override
		public void visitDoWhileLoop(DoWhileStatement loop) {
			loop.getLoopBlock().visit(new RepeatableBlockVisit(container));
		}

		@Override
		public void visitIfElse(IfStatement ifElse) {
			ifElse.getIfBlock().visit(new InnerBlockVisit(container));
			ifElse.getElseBlock().visit(new InnerBlockVisit(container));
		}

		@Override
		public void visitClosureExpression(ClosureExpression expression) {
			for (Parameter p : expression.getParameters()) {
				variables.get(declaratedInblockVariables).add(p);
			}
			expression.getCode().visit(new ClosureVisit(container));
		}

		@Override
		public void visitBinaryExpression(BinaryExpression expression) {
				expression.getLeftExpression().visit(new DefaultAssignementVisit(container, expression.getOperation()));
				expression.getRightExpression().visit(this);
		}

		@Override
		public void visitPostfixExpression(PostfixExpression expression) {
			expression.getExpression().visit(
					new DefaultAssignementVisit(container, expression.getOperation()));
		}

		@Override
		public void visitPrefixExpression(PrefixExpression expression) {
			expression.getExpression().visit(
					new DefaultAssignementVisit(container, expression.getOperation()));
		}
		protected boolean isUsedOrDeclarated(Variable var) {
			return variables.get(usedVariables).contains(var)
					|| variables.get(declaratedVariables).contains(var)
					|| variables.get(declaratedInblockVariables).contains(var);
		}
	}

	private class DefaultAssignementVisit extends DefaultVisit {
		
		protected Token operator;

		public DefaultAssignementVisit(ASTScopeScanner container, Token token) {
			super(container);
			this.operator = token;
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
			super.visitVariableExpression(expression);
			Variable var = expression.getAccessedVariable();
			if (isUsedOrDeclarated(var) && operator.getType() != Types.LEFT_SQUARE_BRACKET) {
				variables.get(returnVariables).add(var);
			}
		}
	}

	private class InnerBlockVisit extends DefaultVisit {
		public InnerBlockVisit(ASTScopeScanner container) {
			super(container);
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
			Variable var = expression.getAccessedVariable();
			if (!(var instanceof FieldNode)) {
				if (!variables.get(declaratedInblockVariables).contains(var)
						&& !variables.get(declaratedVariables).contains(var)) {
					variables.get(usedVariables).add(var);
				}
			}
		}
	}

	private class RepeatableBlockVisit extends InnerBlockVisit {

		public RepeatableBlockVisit(ASTScopeScanner container) {
			super(container);
		}

		protected void checkAssertedVariables(VariableExpression expression, int operatorTokenType) {
			if(operatorTokenType == Types.LEFT_SQUARE_BRACKET)
				return;
			Variable var = expression.getAccessedVariable();
			if (isUsedOrDeclarated(var)) {
				variables.get(returnVariables).add(var);
			}
			if (variables.get(usedVariables).contains(var)) {
				variables.get(innerLoopAssignedVariables).add(var);
			}
		}

		@Override
		public void visitBinaryExpression(BinaryExpression expression) {
			expression.getLeftExpression().visit(new AssignementInRepeteableBlockVisit(container, expression.getOperation()));
			expression.getRightExpression().visit(new RepeatableBlockVisit(container));
		}

		@Override
		public void visitPostfixExpression(PostfixExpression expression) {
			expression.getExpression().visit(
					new AssignementInRepeteableBlockVisit(container, expression.getOperation()));
		}

		@Override
		public void visitPrefixExpression(PrefixExpression expression) {
			expression.getExpression().visit(
					new AssignementInRepeteableBlockVisit(container, expression.getOperation()));
		}

		@Override
		public void visitIfElse(IfStatement ifElse) {
			ifElse.getIfBlock().visit(this);
			ifElse.getElseBlock().visit(this);
		}

	}

	private class AssignementInRepeteableBlockVisit extends
			RepeatableBlockVisit {
		protected Token operator;

		public AssignementInRepeteableBlockVisit(ASTScopeScanner container, Token operator) {
			super(container);
			this.operator = operator;
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
			super.visitVariableExpression(expression);
			checkAssertedVariables(expression,operator.getType());
		}

	}

	private class ClosureVisit extends RepeatableBlockVisit {

		public ClosureVisit(ASTScopeScanner container) {
			super(container);
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
			Variable var = expression.getAccessedVariable();
			if (var instanceof Parameter && var.isClosureSharedVariable()) {
				variables.get(declaratedInblockVariables).add(var);
			}
			super.visitVariableExpression(expression);

		}

		@Override
		public void visitBinaryExpression(BinaryExpression expression) {
			expression.getLeftExpression().visit(new AssignementInClosureVisit(container, expression.getOperation()));
			expression.getRightExpression().visit(this);

		}

		@Override
		public void visitPostfixExpression(PostfixExpression expression) {
			expression.getExpression().visit(
					new AssignementInClosureVisit(container, expression.getOperation()));
		}

		@Override
		public void visitPrefixExpression(PrefixExpression expression) {
			expression.getExpression().visit(
					new AssignementInClosureVisit(container, expression.getOperation()));
		}
	}

	private class AssignementInClosureVisit extends ClosureVisit {
		
		protected Token operator;

		public AssignementInClosureVisit(ASTScopeScanner container, Token token) {
			super(container);
			this.operator = token;
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
			super.visitVariableExpression(expression);
			checkAssertedVariables(expression,operator.getType());
		}
	}
}
