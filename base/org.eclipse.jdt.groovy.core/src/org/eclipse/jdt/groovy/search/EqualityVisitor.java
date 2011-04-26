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
package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Tests to see if {@link #nodeToLookFor} exists as a sub node in the {@link ASTNode} that is passed in to {@link #doVisit(ASTNode)}
 * 
 * @author Andrew Eisenberg
 * @created Apr 26, 2011
 */
public class EqualityVisitor extends ClassCodeVisitorSupport {
	private final ASTNode nodeToLookFor;

	private boolean nodeFound = false;

	public EqualityVisitor(ASTNode nodeToLookFor) {
		this.nodeToLookFor = nodeToLookFor;
	}

	public boolean doVisit(ASTNode toVisit) {
		toVisit.visit(this);
		return nodeFound;
	}

	@Override
	protected SourceUnit getSourceUnit() {
		return null;
	}

	@Override
	public void visitFieldExpression(FieldExpression expression) {
		if (nodeToLookFor == expression) {
			nodeFound = true;
		} else {
			super.visitFieldExpression(expression);
		}
	}

	@Override
	public void visitVariableExpression(VariableExpression expression) {
		if (nodeToLookFor == expression) {
			nodeFound = true;
		} else {
			super.visitVariableExpression(expression);
		}
	}

	@Override
	public void visitConstantExpression(ConstantExpression expression) {
		if (nodeToLookFor == expression) {
			nodeFound = true;
		} else {
			super.visitConstantExpression(expression);
		}
	}

	public static boolean checkForAssignment(ASTNode node, BinaryExpression binaryExpr) {
		return binaryExpr != null && new EqualityVisitor(node).doVisit(binaryExpr.getLeftExpression());
	}
}