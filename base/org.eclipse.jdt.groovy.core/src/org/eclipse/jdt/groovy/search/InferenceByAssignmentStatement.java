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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Types;

/**
 * Records types for variables in the scope based on assignment statements
 * 
 * @author Andrew Eisenberg
 * @created Sep 25, 2009
 * 
 */
public class InferenceByAssignmentStatement implements ITypeLookup {

	public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
		if (node instanceof DeclarationExpression) {
			DeclarationExpression declExpr = (DeclarationExpression) node;
			if (declExpr.isMultipleAssignmentDeclaration()) {
				ArgumentListExpression args = (ArgumentListExpression) declExpr.getLeftExpression();
				for (Expression argExpr : (Iterable<Expression>) args.getExpressions()) {
					// probable won't get anything out of here
					throw new RuntimeException("Not implemented");
				}
			} else {
				if (declExpr.getLeftExpression() instanceof VariableExpression) {
					// should also be looking at variable types here
					VariableExpression var = (VariableExpression) declExpr.getLeftExpression();
					scope.addVariable(var.getName(), declExpr.getRightExpression().getType(),
							var.getAccessedVariable() instanceof AnnotatedNode ? ((AnnotatedNode) var.getAccessedVariable())
									.getDeclaringClass() : null);
				}
			}

		} else if (node instanceof BinaryExpression) {
			BinaryExpression assign = (BinaryExpression) node;
			if (assign.getOperation().getType() == Types.EQUALS && assign.getLeftExpression() instanceof VariableExpression) {
				VariableExpression var = (VariableExpression) assign.getLeftExpression();
				scope.addVariable(var.getName(), assign.getRightExpression().getType(),
						var.getAccessedVariable() instanceof AnnotatedNode ? ((AnnotatedNode) var.getAccessedVariable())
								.getDeclaringClass() : null);
			}
		}
		return null;
	}

	public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
		Expression init = node.getInitialExpression();
		if (!isObjectType(init)) {
			scope.addVariable(node.getName(), init.getType(), node.getDeclaringClass());
		}
		// if the field has an explicit type, then we don't need to store it,
		// will be stored in the ast itself.
		return null;
	}

	public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
		return null;
	}

	public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
		return null;
	}

	public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
		return null;
	}

	public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
		return null;
	}

	public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
		scope.addVariable(node);
		return null;
	}

	/**
	 * @param init
	 * @return true if init is of type java.lang.Object, or there is no init
	 */
	private boolean isObjectType(Expression init) {
		return init == null || ClassHelper.OBJECT_TYPE.equals(init.getType());
	}

}
