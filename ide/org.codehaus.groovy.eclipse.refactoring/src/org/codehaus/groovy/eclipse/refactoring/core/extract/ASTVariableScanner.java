/*
 * Copyright (C) 2007, 2010 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * Adapted for Groovy-Eclipse 2.0 by Andrew Eisenberg
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
package org.codehaus.groovy.eclipse.refactoring.core.extract;

import java.util.LinkedHashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTVisitorDecorator;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

/**
 * @author Michael Klenk mklenk@hsr.ch
 *
 *         AST Visitor, which counts all variables
 *         in the given node split in: - declaratedVariables Variables
 *         declarated in this Block - declaratedInblockVariables Variables
 *         declarated in an inner Block - usedVariables Variables which are read
 *         in this block - returnVariables Variables which are assigned to a new
 *         value in this Block
 */
public class ASTVariableScanner {

    private Set<Variable> declaredVariables = new LinkedHashSet<Variable>();

    private Set<Variable> declaredInblockVariables = new LinkedHashSet<Variable>();

    private Set<Variable> usedVariables = new LinkedHashSet<Variable>();

    private Set<Variable> returnVariables = new LinkedHashSet<Variable>();

    private Set<Variable> innerLoopAssignedVariables = new LinkedHashSet<Variable>();

	private boolean selectionIsInLoopOrClosure = false;

	/**
	 * Constructor to initialize the Scanner
	 */
	public ASTVariableScanner(boolean blockIsInLoop) {
		selectionIsInLoopOrClosure = blockIsInLoop;
	}

	/**
	 * @return all new declared variables in the visited node
	 */
    public Set<Variable> getDeclaratedVariables() {
        return declaredVariables;
	}

	/**
	 * @return all read variables in the visted node
	 */
    public Set<Variable> getUsedVariables() {
        return usedVariables;
	}

	/**
	 * @return all variables which are assigned to a new Value a =, a++, a--
	 */
    public Set<Variable> getAssignedVariables() {
        return returnVariables;
	}

	/**
	 * @return all variables which are assigned in an innerLoop
	 */
    public Set<Variable> getInnerLoopAssignedVariables() {
        return innerLoopAssignedVariables;
	}

	public void visitNode(ASTNode node) {
        if (selectionIsInLoopOrClosure) {
			node.visit(new RepeatableBlockVisit(this));
        } else {
			node.visit(new DefaultVisit(this));
        }
	}

	private class DefaultVisit extends ASTVisitorDecorator<ASTVariableScanner> {
        public DefaultVisit(ASTVariableScanner container) {
			super(container);
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
			super.visitVariableExpression(expression);
            Variable var = expression.getAccessedVariable();
            if (expression.getEnd() < 1) {
                return;
            } else if (var != null && !(var instanceof FieldNode)) {
                if (!declaredVariables.contains(var)) {
                    usedVariables.add(var);
				}
			}
		}

		@Override
		public void visitDeclarationExpression(DeclarationExpression expression) {
			Variable var = expression.getVariableExpression()
					.getAccessedVariable();
            if (var != null) {
                declaredVariables.add(var);
            }
			super.visitDeclarationExpression(expression);
		}

		@Override
		public void visitForLoop(ForStatement forLoop) {
            if (forLoop.getVariable() != null) {
                declaredInblockVariables.add(forLoop.getVariable());
            }
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
            if (expression.getParameters().length > 0) {
                for (Parameter p : expression.getParameters()) {
                    declaredInblockVariables.add(p);
                }
            }
			expression.getCode().visit(new ClosureVisit(container));
		}

		@Override
		public void visitBinaryExpression(BinaryExpression expression) {
            if (expression.getLeftExpression() instanceof VariableExpression) {
                expression.getLeftExpression().visit(new DefaultAssignementVisit(container, expression.getOperation()));
            } else {
                expression.getLeftExpression().visit(this);
            }
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

        protected boolean isUsed(Variable var) {
            return declaredVariables.contains(var) || declaredInblockVariables.contains(var);
		}
	}

	private class DefaultAssignementVisit extends DefaultVisit {

		protected Token operator;

		public DefaultAssignementVisit(ASTVariableScanner container, Token token) {
			super(container);
			this.operator = token;
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
			super.visitVariableExpression(expression);
            if (expression.getEnd() < 1) {
                return;
            }
			Variable var = expression.getAccessedVariable();
            if (/* isUsed(var) && */operator.getType() != Types.LEFT_SQUARE_BRACKET) {
                returnVariables.add(var);
			}
		}
	}

	private class InnerBlockVisit extends DefaultVisit {
		public InnerBlockVisit(ASTVariableScanner container) {
			super(container);
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
            if (expression.getEnd() < 1) {
                return;
            }
			Variable var = expression.getAccessedVariable();
            if (var != null && !(var instanceof FieldNode)) {
                if (!declaredInblockVariables.contains(var) && !declaredVariables.contains(var)) {
                    usedVariables.add(var);
				}
			}
		}
	}

	private class RepeatableBlockVisit extends InnerBlockVisit {

		public RepeatableBlockVisit(ASTVariableScanner container) {
			super(container);
		}

		protected void checkAssertedVariables(VariableExpression expression, int operatorTokenType) {
			if(operatorTokenType == Types.LEFT_SQUARE_BRACKET)
				return;
			Variable var = expression.getAccessedVariable();
            if (var != null && isUsed(var)) {
                returnVariables.add(var);
			}
            if (usedVariables.contains(var)) {
                innerLoopAssignedVariables.add(var);
			}
		}

		@Override
		public void visitBinaryExpression(BinaryExpression expression) {
			expression.getLeftExpression().visit(new AssignmentInRepeatableBlockVisit(container, expression.getOperation()));
			expression.getRightExpression().visit(new RepeatableBlockVisit(container));
		}

		@Override
		public void visitPostfixExpression(PostfixExpression expression) {
			expression.getExpression().visit(
					new AssignmentInRepeatableBlockVisit(container, expression.getOperation()));
		}

		@Override
		public void visitPrefixExpression(PrefixExpression expression) {
			expression.getExpression().visit(
					new AssignmentInRepeatableBlockVisit(container, expression.getOperation()));
		}

		@Override
		public void visitIfElse(IfStatement ifElse) {
			ifElse.getIfBlock().visit(this);
			ifElse.getElseBlock().visit(this);
		}

	}

	private class AssignmentInRepeatableBlockVisit extends
			RepeatableBlockVisit {
		protected Token operator;

		public AssignmentInRepeatableBlockVisit(ASTVariableScanner container, Token operator) {
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

		public ClosureVisit(ASTVariableScanner container) {
			super(container);
		}

		@Override
		public void visitVariableExpression(VariableExpression expression) {
			Variable var = expression.getAccessedVariable();
            // implicit closure variable
            if (var instanceof Parameter && ((Parameter) var).getEnd() <= 0 && var.getName().equals("it")) {
                declaredInblockVariables.add(var);
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

		public AssignementInClosureVisit(ASTVariableScanner container, Token token) {
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
