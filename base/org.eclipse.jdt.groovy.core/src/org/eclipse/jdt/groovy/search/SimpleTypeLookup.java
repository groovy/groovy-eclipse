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

import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.EXACT;
import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.INFERRED;

import java.util.List;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 *          Looks at the type associated with the ASTNode for the type
 */
public class SimpleTypeLookup implements ITypeLookup {

	public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {

		ClassNode declaringType = objectExpressionType != null ? objectExpressionType : findDeclaringType(node, scope);

		ClassNode type = findType(node, objectExpressionType, scope);

		// always inferred for now
		return new TypeLookupResult(type, declaringType, INFERRED);
	}

	public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
		return new TypeLookupResult(baseType(node.getType()), baseType(node.getDeclaringClass()), EXACT);
	}

	public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
		return new TypeLookupResult(baseType(node.getReturnType()), baseType(node.getDeclaringClass()), EXACT);
	}

	public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
		return new TypeLookupResult(baseType(node.getClassNode()), baseType(node.getClassNode()), EXACT);
	}

	public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
		return new TypeLookupResult(baseType(node.getType()), baseType(node.getType()), EXACT);
	}

	/**
	 * always return the passed in node
	 */
	public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
		return new TypeLookupResult(baseType(node), baseType(node), EXACT);
	}

	public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
		return new TypeLookupResult(baseType(node.getType()), scope.getEnclosingTypeDeclaration(), EXACT);
	}

	/**
	 * @param node
	 * @param scope
	 * @return
	 */
	private ClassNode findDeclaringType(Expression node, VariableScope scope) {
		if (node instanceof ClassExpression || node instanceof ConstructorCallExpression) {
			return node.getType();

		} else if (node instanceof FieldExpression) {
			return baseType(baseType(((FieldExpression) node).getField().getDeclaringClass()));

		} else if (node instanceof MethodCallExpression) {
			return baseType(((MethodCallExpression) node).getObjectExpression().getType());

		} else if (node instanceof StaticMethodCallExpression) {
			return baseType(((StaticMethodCallExpression) node).getOwnerType());

		} else if (node instanceof VariableExpression) {
			Variable var = ((VariableExpression) node).getAccessedVariable();
			if (var instanceof DynamicVariable) {
				// this dynamic variable might be a field
				return findField((VariableExpression) node, scope.getEnclosingTypeDeclaration());
			} else if (var instanceof FieldNode) {
				return ((FieldNode) var).getDeclaringClass();
			}

		} else if (node instanceof DeclarationExpression) {
			// the type declaration of the DeclarationExpression is considered to be the
			// declaring type. This ensures that type declarations are considered
			// to be type references.
			return ((DeclarationExpression) node).getLeftExpression().getType();
		}
		return new ClassNode(Object.class);
	}

	/**
	 * @param node
	 * @param scope
	 * @return
	 */
	private ClassNode findType(Expression node, ClassNode objectExpressionType, VariableScope scope) {
		// check first to see if we have this type inferred
		if (node instanceof Variable) {
			VariableInfo info = scope.lookupName(((Variable) node).getName());
			if (info != null) {
				return baseType(info.type);
			}
		}

		if (objectExpressionType != null) {
			// lookup the type in the object's expression type
			if (node instanceof ConstantExpression) {
				ConstantExpression constExpr = (ConstantExpression) node;
				String name = constExpr.getText();
				PropertyNode property = objectExpressionType.getProperty(name);
				if (property != null) {
					return property.getType();
				}
				// do not distinguish between method variants
				List<MethodNode> methods = objectExpressionType.getMethods(name);
				if (methods.size() > 0) {
					return methods.get(0).getReturnType();
				}

				FieldNode field = objectExpressionType.getField(name);
				if (field != null) {
					return field.getType();
				}
			}
		}

		// don't know
		return baseType(node.getType());
	}

	/**
	 * @param name
	 * @param enclosingDeclarationNode
	 * @return
	 */
	private ClassNode findField(VariableExpression var, ClassNode declaringClass) {
		if (declaringClass == null) {
			return baseType(var.getType());
		}
		String name = var.getName();
		FieldNode field = declaringClass.getField(name);
		if (field != null) {
			return field.getDeclaringClass();
		} else {
			return baseType(var.getType());
		}
	}

	private ClassNode baseType(ClassNode node) {
		return node == null ? null : (node.getComponentType() == null ? node : node.getComponentType());
	}
}
