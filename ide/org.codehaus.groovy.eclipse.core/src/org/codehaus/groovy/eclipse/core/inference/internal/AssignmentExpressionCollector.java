 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.inference.internal;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.types.internal.InferringEvaluationContext;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.groovy.syntax.Types;

/**
 * Collects assignment expressions under various conditions. The conditions are set 'builder style', see all methods
 * returning AssignmentExpressionCollector.
 * 
 * @author empovazan
 */
class AssignmentExpressionCollector {
	private final ISourceCodeContext sourceCodeContext;
	
	private ASTNode currentScope;
	
	private final List< Expression > expressions = newList();

	private boolean findClosestToContext;

	private boolean reverseResults;

	private String localName;

	private String fieldName;

	AssignmentExpressionCollector(InferringEvaluationContext evalContext) {
		sourceCodeContext = evalContext.getSourceCodeContext();
	}

	AssignmentExpressionCollector inCurrentScope() {
		currentScope = findCurrentScope();
		return this;
	}

	AssignmentExpressionCollector closestToContext() {
		findClosestToContext = true;
		return this;
	}

	AssignmentExpressionCollector reverseResults() {
		reverseResults = true;
		return this;
	}
	
	AssignmentExpressionCollector localName(String name) {
		localName = name;
		return this;
	}
	
	AssignmentExpressionCollector fieldName(String name) {
		fieldName = name;
		return this;
	}
	
	AssignmentExpressionCollector variableName() {
		return this;
	}
		
	Expression[] getExpressions() {
		if (currentScope instanceof MethodNode) {
			collectInMethod((MethodNode) currentScope);
		} else if (currentScope instanceof ClosureExpression) {
			collectInClosure((ClosureExpression) currentScope);
		} else {
			new CollectInClassOperation().visitClass((ClassNode) sourceCodeContext.getASTPath()[1]);
		}

		if (reverseResults) {
			Collections.reverse(expressions);
		}

		return expressions.toArray(new Expression[expressions.size()]);
	}

	private void collectInMethod(MethodNode node) {
		try {
			if (findClosestToContext) {
				new CollectClosestToContextOperation().visitMethod(node);
			} else {
				new CollectInCurrentMethodOperation().visitMethod(node);
			}
		} catch (RuntimeException e) {
			// Finished collecting.
		}
	}

	private void collectInClosure(ClosureExpression expression) {
		try {
			if (findClosestToContext) {
				new CollectClosestToContextOperation().visitClosureExpression(expression);
			} else {
				new CollectInCurrentMethodOperation().visitClosureExpression(expression);
			}
		} catch (RuntimeException e) {
			// Finished collecting.
		}
	}

	private ASTNode findCurrentScope() {
		ASTNode[] path = sourceCodeContext.getASTPath();

		// First see if this is a method.
		for (int i = path.length - 1; i >= 0; --i) {
			if (path[i] instanceof MethodNode) {
				return path[i];
			}
		}

		// Then we should be in a closure declared in a class.
		for (int i = path.length - 1; i >= 0; --i) {
			if (path[i] instanceof ClosureExpression) {
				return path[i];
			}
		}

		// We must be in the class.
		for (int i = path.length - 1; i >= 0; --i) {
			if (path[i] instanceof ClassNode) {
				return path[i];
			}
		}

		throw new IllegalStateException(
				"Expecting to be in a ClosureExpression, MethodNode or ClassNode, but neither of them are present in current source code context ast path.");
	}
	
	abstract class CollectionOperation extends ClassCodeVisitorSupport {
		class Stack extends ArrayList< VariableScope > {
            private static final long serialVersionUID = -208451556200160829L;

            void push(VariableScope scope) {
				add(scope);
			}
			
			VariableScope pop() {
				return remove(size() - 1);
			}
			
			VariableScope top() {
				return get(size() - 1);
			}
		}
		
		Stack scopeStack = new Stack();
		
		@Override
        public void visitBlockStatement(BlockStatement block) {
			scopeStack.push(block.getVariableScope());
			try {
				super.visitBlockStatement(block);
			} finally {
				scopeStack.pop();
			}
		}
		
		@Override
        public void visitClosureExpression(ClosureExpression expression) {
			scopeStack.push(expression.getVariableScope());
			try {
				super.visitClosureExpression(expression);
			} finally {
				scopeStack.pop();
			}
		}
		
		boolean isLocalInScope(String name) {
			VariableScope scope = scopeStack.top();
			return scope.getReferencedLocalVariable(name) != null || scope.getDeclaredVariable(name) != null;
		}
		
		boolean isFieldInScope(String name) {
			VariableScope scope = scopeStack.top();
			return scope.getReferencedClassVariable(name) != null;
		}
		
		@Override
        protected SourceUnit getSourceUnit() {
			return null;
		}
	}
	
	/**
	 * Collect all assignments to fields anywhere within the class.
	 * Use: new CollectInClassOperation().visitClass(classNode)
	 */
	class CollectInClassOperation extends CollectionOperation {
		@Override
        public void visitBinaryExpression(BinaryExpression expr) {
			super.visitBinaryExpression(expr);
			if (expr.getOperation().getType() == Types.EQUALS
					&& expr.getLeftExpression().getClass() == VariableExpression.class
					&& !expr.getRightExpression().getText().equals("null")) {
				String exprName = ((VariableExpression)expr.getLeftExpression()).getName();
				if (fieldName == null || fieldName.equals(exprName) && isFieldInScope(fieldName)) {
					if (fieldName == null || isFieldInScope(fieldName)) {
						expressions.add(expr.getRightExpression());
					}
				}
			}
		}

		@Override
        public void visitField(FieldNode fieldNode) {
			Expression initialValueExpression = fieldNode.getInitialValueExpression();
			if (initialValueExpression != null && (fieldName == null || fieldName.equals(fieldNode.getName()))) {
				expressions.add(initialValueExpression);
			}
		}
	}

	/**
	 * Use: new CollectInCurrentMethodOperation().visitMethod(methodNode) 
	 */
	class CollectInCurrentMethodOperation extends CollectionOperation {
		@Override
        public void visitBinaryExpression(BinaryExpression expr) {
			super.visitBinaryExpression(expr);
			if (expr.getOperation().getType() == Types.EQUALS
					&& expr.getLeftExpression().getClass() == VariableExpression.class
					&& !expr.getRightExpression().getText().equals("null")) {
				String exprName = ((VariableExpression)expr.getLeftExpression()).getName();
				if (localName == null && fieldName == null
						|| localName != null && localName.equals(exprName) && isLocalInScope(localName)
						|| fieldName != null && fieldName.equals(exprName) && isFieldInScope(fieldName)) {
					expressions.add(expr.getRightExpression());
				}
			} else {
				throw new VisitCompleteException("Done collecting");
			}
		}
	}
	
	/**
	 * Use: new CollectClosestToLocationOperation().visitMethod(methodNode)
	 */
	class CollectClosestToContextOperation extends CollectionOperation {
		@Override
        public void visitBinaryExpression(BinaryExpression expr) {
			Expression left = expr.getLeftExpression();
			Expression right = expr.getRightExpression();
			if (left.getStart() < sourceCodeContext.getOffset()) {
				if (expr.getOperation().getType() == Types.EQUALS 
						&& left.getClass() == VariableExpression.class
						&& !right.getText().equals("null")) {
					super.visitBinaryExpression(expr);
					String exprName = ((VariableExpression) expr.getLeftExpression()).getName();
					if (localName == null && fieldName == null || localName != null && localName.equals(exprName)
							&& isLocalInScope(localName) || fieldName != null && fieldName.equals(exprName)
							&& isFieldInScope(fieldName)) {
						expressions.add(expr.getRightExpression());
					}
				}
			} else {
				throw new VisitCompleteException("Done collecting");
			}
		}		
	}
}
