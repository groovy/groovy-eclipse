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

import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * Records types for variables in the scope based on assignment statements.
 * 
 * There are several possibilities here:
 * <ul>
 * <li>
 * If {@link DeclarationExpression}, and the declared type is not Object, use that type</li>
 * <li>
 * If {@link DeclarationExpression}, and the declared type is Object, and there is an objectExpression, use the type of the object
 * expression. Add to the variable scope, don't replace</li>
 * <li>
 * If {@link BinaryExpression} (assignment), then use the type of the objectExpression. Replace in the VariableScope, don't add</li>
 * </ul>
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
				TupleExpression tuple = (TupleExpression) declExpr.getLeftExpression();

				handleMultiAssignment(scope, objectExpressionType, declExpr, tuple);
				return null;
			} else {

				// use the declared type if not void and not object expression
				VariableExpression variableExpression = declExpr.getVariableExpression();
				ClassNode varType;
				if (variableExpression.getOriginType() != null) {
					varType = variableExpression.getOriginType();
				} else {
					varType = variableExpression.getType();
				}
				ClassNode typeToStore;
				if (!VariableScope.isVoidOrObject(varType) && !varType.equals(VariableScope.OBJECT_CLASS_NODE)) {
					typeToStore = varType;
				} else if (objectExpressionType != null) {
					typeToStore = objectExpressionType;
				} else {
					typeToStore = VariableScope.OBJECT_CLASS_NODE;
				}
				// store the variable. declaring type is always null since the
				// variable is being declared right here.
				scope.addVariable(variableExpression.getName(), typeToStore, null);
			}
		} else if (node instanceof BinaryExpression) {
			BinaryExpression assign = (BinaryExpression) node;
			// only look at assignments and can't do much if objectExpressionType is null
			// and only re-set if we have a real type for the object expression
			if (isInterestingOperation(assign) && !VariableScope.isVoidOrObject(objectExpressionType)) {

				if (assign.getLeftExpression() instanceof VariableExpression) {
					VariableExpression var = (VariableExpression) assign.getLeftExpression();
					ClassNode declaringType = findDeclaringType(var);

					scope.updateOrAddVariable(var.getName(), objectExpressionType, declaringType);
					return new TypeLookupResult(objectExpressionType, declaringType, assign.getLeftExpression(),
							TypeConfidence.INFERRED, scope);
				} else if (assign.getLeftExpression() instanceof TupleExpression) {
					TupleExpression tuple = (TupleExpression) assign.getLeftExpression();
					handleMultiAssignment(scope, objectExpressionType, assign, tuple);
					return null;
				} else {
					// FIXADE this is probably a property node, eg- 'foo.bar = somevalue' just do Object type
				}
			}

		}
		return null;
	}

	/**
	 * @param scope
	 * @param objectExpressionType
	 * @param binaryExpr
	 * @param tuple
	 */
	private void handleMultiAssignment(VariableScope scope, ClassNode objectExpressionType, BinaryExpression binaryExpr,
			TupleExpression tuple) {
		// the type to use if rhs is not a List literal expression
		ClassNode maybeType = findComponentType(objectExpressionType);
		// try to associate the individual tuple expression elements
		// with something on the rhs
		ListExpression rhs = binaryExpr.getRightExpression() instanceof ListExpression ? (ListExpression) binaryExpr
				.getRightExpression() : null;
		List<Expression> lhsExprs = (List<Expression>) (tuple == null ? Collections.emptyList() : tuple.getExpressions());
		List<Expression> rhsExprs = (List<Expression>) (rhs == null ? Collections.emptyList() : rhs.getExpressions());
		for (int i = 0, lhsSize = lhsExprs.size(), rhsSize = rhsExprs.size(); i < lhsSize; i++) {
			Expression lhsExpr = lhsExprs.get(i);
			Expression rhsExpr = i < rhsSize ? rhsExprs.get(i) : null;
			if (lhsExpr instanceof VariableExpression) {
				scope.addVariable(((Variable) lhsExpr).getName(), rhsExpr == null ? maybeType : rhsExpr.getType(), null);
			}
		}
	}

	/**
	 * @param objectExpressionType
	 * @return
	 */
	private ClassNode findComponentType(ClassNode objectExpressionType) {
		if (objectExpressionType == null) {
			return VariableScope.OBJECT_CLASS_NODE;
		} else {
			return VariableScope.extractElementType(objectExpressionType);
		}
	}

	/**
	 * This method is a placeholder for supporting more than just assignments.
	 */
	private boolean isInterestingOperation(BinaryExpression assign) {
		switch (assign.getOperation().getType()) {
			case Types.EQUALS:
				// should we handle other cases too?
				// case Types.PLUS_EQUAL:
				// case Types.MINUS_EQUAL:
				// case Types.LEFT_SHIFT:
				// case Types.BITWISE_AND_EQUAL:
				// case Types.BITWISE_OR_EQUAL:
				// case Types.BITWISE_XOR_EQUAL:
				// case Types.DIVIDE_EQUAL:
				// case Types.LOGICAL_AND_EQUAL:
				// case Types.LOGICAL_OR_EQUAL:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Finds the declaring type of the accessed variable. Will be null if this is a local variable
	 * 
	 * @param var
	 * @return
	 */
	private ClassNode findDeclaringType(VariableExpression var) {
		return var.getAccessedVariable() instanceof AnnotatedNode ? ((AnnotatedNode) var.getAccessedVariable()).getDeclaringClass()
				: null;
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
		// if this is a static import, then add to the top level scope
		ClassNode type = node.getType();
		if (node.isStar() && type != null) {
			// importing all static fields in the class
			List<FieldNode> fields = type.getFields();
			for (FieldNode field : fields) {
				if (field.isStatic()) {
					scope.addVariable(field.getName(), field.getType(), type);
				}
			}

			List<MethodNode> methods = node.getType().getMethods();
			for (MethodNode method : methods) {
				if (method.isStatic()) {
					scope.addVariable(method.getName(), method.getReturnType(), type);
				}
			}

		} else if (node.isStatic() && type != null && node.getFieldName() != null) {
			FieldNode field = type.getField(node.getFieldName());
			if (field != null) {
				scope.addVariable(field.getName(), field.getType(), type);
			}
			List<MethodNode> methods = type.getDeclaredMethods(node.getFieldName());
			if (methods != null) {
				for (MethodNode method : methods) {
					scope.addVariable(method.getName(), method.getReturnType(), type);
				}
			}
		}
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

	public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
		// do nothing
	}

}
