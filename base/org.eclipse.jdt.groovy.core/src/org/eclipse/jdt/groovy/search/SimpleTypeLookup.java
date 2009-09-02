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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 *          Looks at the type associated with the ASTNode for the type
 */
public class SimpleTypeLookup implements ITypeLookup {

	private LookupType lookupType;

	public TypeLookupResult lookupType(Expression node, ASTNode enclosingDeclarationNode) {
		if (node instanceof DeclarationExpression) {
			node = ((DeclarationExpression) node).getLeftExpression();
		}

		if (lookupType == LookupType.DECLARING_TYPE) {
			// must distinguish between the return type and the declaring type only in certain circumstances
			if (node instanceof MethodCallExpression) {
				return new TypeLookupResult(baseType(((MethodCallExpression) node).getObjectExpression().getType()), EXACT);
			} else if (node instanceof FieldExpression) {
				return new TypeLookupResult(baseType(((FieldExpression) node).getField().getDeclaringClass()), EXACT);
			} else if (node instanceof StaticMethodCallExpression) {
				return new TypeLookupResult(baseType(((StaticMethodCallExpression) node).getOwnerType()), EXACT);
			} else if (node instanceof PropertyExpression) {
				PropertyExpression prop = (PropertyExpression) node;
				if (isThisOrSuperReference(prop)) {
					return findFieldReferenceInHierarchy(prop, enclosingDeclarationNode);
				} else if (prop.getObjectExpression() instanceof VariableExpression) {
					return findVariableType(prop);
				}
				return new TypeLookupResult(baseType(prop.getObjectExpression().getType()), EXACT);
			} else if (node instanceof MethodPointerExpression) {
				return new TypeLookupResult(baseType(((MethodPointerExpression) node).getExpression().getType()), EXACT);
			} else if (node instanceof VariableExpression) {
				if (((VariableExpression) node).getAccessedVariable() instanceof DynamicVariable) {
					// this dynamic variable might be a field
					return findField((VariableExpression) node, enclosingDeclarationNode);
				}
			}
		}
		return new TypeLookupResult(baseType(node.getType()), EXACT);
	}

	/**
	 * @param name
	 * @param enclosingDeclarationNode
	 * @return
	 */
	private TypeLookupResult findField(VariableExpression var, ASTNode enclosingDeclarationNode) {
		ClassNode declaringClass = findDeclaringClass(enclosingDeclarationNode);
		if (declaringClass == null) {
			return new TypeLookupResult(baseType(var.getType()), TypeConfidence.POTENTIAL);
		}
		String name = var.getName();
		FieldNode field = declaringClass.getField(name);
		if (field != null) {
			return new TypeLookupResult(field.getDeclaringClass(), EXACT);
		} else {
			return new TypeLookupResult(baseType(var.getType()), TypeConfidence.POTENTIAL);
		}
	}

	/**
	 * @param prop
	 */
	private TypeLookupResult findVariableType(PropertyExpression prop) {
		Variable var = ((VariableExpression) prop.getObjectExpression()).getAccessedVariable();
		if (var.getType() != null) {
			ClassNode variableType = baseType(var.getType());
			FieldNode field = variableType.getField(prop.getPropertyAsString());
			if (field != null) {
				return new TypeLookupResult(field.getDeclaringClass(), EXACT);
			}
		}
		// can't find the type declared in, so just do the best we can
		return new TypeLookupResult(baseType(prop.getObjectExpression().getType()), TypeConfidence.POTENTIAL);
	}

	/**
	 * @param node
	 * @param enclosingDeclarationNode
	 * @return
	 */
	private TypeLookupResult findFieldReferenceInHierarchy(PropertyExpression node, ASTNode enclosingDeclarationNode) {
		ClassNode declaringClass = findDeclaringClass(enclosingDeclarationNode);
		if (declaringClass == null) {
			return new TypeLookupResult(baseType(node.getObjectExpression().getType()), TypeConfidence.POTENTIAL);
		}

		if (isSuperReference(node)) {
			declaringClass = declaringClass.getSuperClass();
		}

		FieldNode field = declaringClass.getField(node.getPropertyAsString());
		if (field != null) {
			return new TypeLookupResult(field.getDeclaringClass(), TypeConfidence.EXACT);
		} else {
			return new TypeLookupResult(baseType(node.getObjectExpression().getType()), TypeConfidence.POTENTIAL);
		}
	}

	/**
	 * @param enclosingDeclarationNode
	 * @return
	 */
	private ClassNode findDeclaringClass(ASTNode enclosingDeclarationNode) {
		ClassNode declaringClass;
		if (enclosingDeclarationNode instanceof ClassNode) {
			declaringClass = (ClassNode) enclosingDeclarationNode;
		} else if (enclosingDeclarationNode instanceof FieldNode) {
			declaringClass = ((FieldNode) enclosingDeclarationNode).getDeclaringClass();
		} else if (enclosingDeclarationNode instanceof MethodNode) {
			declaringClass = ((MethodNode) enclosingDeclarationNode).getDeclaringClass();
		} else {
			// can't find the type declared in, so just do the best we can
			declaringClass = null;
		}
		return declaringClass;
	}

	/**
	 * @param node
	 * @return
	 */
	private boolean isThisOrSuperReference(PropertyExpression node) {
		String objectExprText = node.getObjectExpression().getText();
		return objectExprText.equals("this") || objectExprText.equals("super");
	}

	private boolean isSuperReference(PropertyExpression node) {
		String objectExprText = node.getObjectExpression().getText();
		return objectExprText.equals("super");
	}

	public TypeLookupResult lookupType(FieldNode node) {
		if (lookupType == LookupType.RETURN_TYPE) {
			return new TypeLookupResult(baseType(node.getType()), EXACT);
		} else {
			return new TypeLookupResult(baseType(node.getDeclaringClass()), EXACT);
		}
	}

	public TypeLookupResult lookupType(MethodNode node) {
		if (lookupType == LookupType.RETURN_TYPE) {
			return new TypeLookupResult(baseType(node.getReturnType()), EXACT);
		} else {
			return new TypeLookupResult(baseType(node.getDeclaringClass()), EXACT);
		}
	}

	public TypeLookupResult lookupType(AnnotationNode node) {
		return new TypeLookupResult(baseType(node.getClassNode()), EXACT);
	}

	public TypeLookupResult lookupType(ImportNode node) {
		return new TypeLookupResult(baseType(node.getType()), EXACT);
	}

	private ClassNode baseType(ClassNode node) {
		return node.getComponentType() == null ? node : node.getComponentType();
	}

	/**
	 * always return the passed in node
	 */
	public TypeLookupResult lookupType(ClassNode node) {
		return new TypeLookupResult(baseType(node), EXACT);
	}

	public void setLookupType(LookupType lookupType) {
		this.lookupType = lookupType;
	}
}
