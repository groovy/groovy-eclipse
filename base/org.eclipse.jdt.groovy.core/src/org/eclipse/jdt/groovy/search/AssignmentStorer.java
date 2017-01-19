/*
 * Copyright 2009-2017 the original author or authors.
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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Types;

/**
 * Records types for variables in the scope based on assignment statements.
 *
 * There are several possibilities here:
 * <ul>
 * <li>If {@link DeclarationExpression}, and the declared type isn't Object, use that type</li>
 * <li>If {@link DeclarationExpression}, and the declared type is Object, and there is an object
 *     expression, use the type of the object expression; add to VariableScope, don't replace</li>
 * <li>If {@link BinaryExpression}, use the type of the objectExpression; replace in VariableScope, don't add</li>
 * </ul>
 *
 * @author Andrew Eisenberg
 */
public class AssignmentStorer {

    /**
     * Store the result of the current assignment statement in the given scope.
     *
     * @param assign assignment statement to look at
     * @param scope scope to store result in
     * @param rhsType type of the right hand side
     */
    public void storeAssignment(BinaryExpression assign, VariableScope scope, ClassNode rhsType) {
        if (assign instanceof DeclarationExpression) {
            DeclarationExpression decl = (DeclarationExpression) assign;
            if (decl.isMultipleAssignmentDeclaration()) {
                TupleExpression tuple = (TupleExpression) decl.getLeftExpression();
                handleMultiAssignment(scope, rhsType, decl, tuple);
            } else {
                VariableExpression var = decl.getVariableExpression();
                scope.addVariable(var.getName(), findVariableType(var, rhsType), null);
            }
        } else if (isInterestingOperation(assign)) {
            Expression lhs = assign.getLeftExpression();
            if (lhs instanceof TupleExpression) {
                TupleExpression tuple = (TupleExpression) lhs;
                handleMultiAssignment(scope, rhsType, assign, tuple);
            } else {
                handleSingleAssignment(lhs, scope, rhsType);
            }
        }
    }

    public void storeField(FieldNode node, VariableScope scope) {
        Expression init = node.getInitialExpression();
        if (!isObjectType(init)) {
            scope.addVariable(node.getName(), init.getType(), node.getDeclaringClass());
        }
    }

    public void storeImport(ImportNode node, VariableScope scope) {
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

        } else {
            String fieldName = node.getFieldName();
            if (node.isStatic() && type != null && fieldName != null) {
                String alias;
                if (node.getAlias() != null) {
                    alias = node.getAlias();
                } else {
                    alias = fieldName;
                }
                FieldNode field = type.getField(fieldName);
                if (field != null) {
                    scope.addVariable(alias, field.getType(), type);
                }
                List<MethodNode> methods = type.getDeclaredMethods(fieldName);
                if (methods != null) {
                    for (MethodNode method : methods) {
                        scope.addVariable(alias, method.getReturnType(), type);
                    }
                }
            }
        }
    }

    public void storeParameterType(Parameter node, VariableScope scope) {
        scope.addVariable(node);
    }

    private void handleMultiAssignment(VariableScope scope, ClassNode objectExpressionType, BinaryExpression binaryExpr, TupleExpression tuple) {
        // the type to use if rhs is not a List literal expression
        ClassNode maybeType = findComponentType(objectExpressionType);
        // try to associate the individual tuple expression elements with something on the rhs
        ListExpression rhs = binaryExpr.getRightExpression() instanceof ListExpression ? (ListExpression) binaryExpr.getRightExpression() : null;
        List<Expression> lhsExprs = (tuple == null ? Collections.EMPTY_LIST : tuple.getExpressions());
        List<Expression> rhsExprs = (rhs == null ? Collections.EMPTY_LIST : rhs.getExpressions());

        for (int i = 0, lhsSize = lhsExprs.size(), rhsSize = rhsExprs.size(); i < lhsSize; i += 1) {
            Expression lhsExpr = lhsExprs.get(i);
            ClassNode rhsType = i < rhsSize ? rhsExprs.get(i).getType() : maybeType;

            if (lhsExpr instanceof VariableExpression) {
                VariableExpression var = (VariableExpression) lhsExpr;
                scope.addVariable(var.getName(), findVariableType(var, rhsType), null);
            }
        }
    }

    protected void handleSingleAssignment(Expression lhs, VariableScope scope, ClassNode rhsType) {
        if (lhs instanceof PropertyExpression) {
            lhs = ((PropertyExpression) lhs).getProperty();
            handleSingleAssignment(lhs, scope, rhsType);

        } else if (lhs instanceof VariableExpression) {
            VariableExpression var = (VariableExpression) lhs;
            // two situations here: inside of scripts, we can set variables that are not explicitly
            // declared, so in this case, we updateOrAdd, but in a regular type, if the
            // variable is not already there, we only update (and underline otherwise)
            if (scope.inScriptRunMethod()) {
                scope.updateOrAddVariable(var.getName(), findVariableType(var, rhsType), findDeclaringType(var));
            } else {
                scope.updateVariable(var.getName(), findVariableType(var, rhsType), findDeclaringType(var));
            }

        } else if (lhs instanceof ConstantExpression) {
            // not a variable, but save ref to help find accessor
            scope.getWormhole().put("lhs", lhs);

        }/* else {
            System.err.println("AssignmentStorer.storeAssignment: LHS is " + lhs.getClass().getSimpleName());
        }*/
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
     * @return {@code true} if init is of type java.lang.Object, or there is no init
     */
    private boolean isObjectType(Expression init) {
        return init == null || VariableScope.OBJECT_CLASS_NODE.equals(init.getType());
    }

    private ClassNode findComponentType(ClassNode objectExpressionType) {
        if (objectExpressionType == null) {
            return VariableScope.OBJECT_CLASS_NODE;
        } else {
            return VariableScope.extractElementType(objectExpressionType);
        }
    }

    /**
     * Finds the declaring type of the accessed variable.
     * Will be {@code null} if this is a local variable.
     */
    private ClassNode findDeclaringType(VariableExpression var) {
        if (var.getAccessedVariable() instanceof AnnotatedNode) {
            return ((AnnotatedNode) var.getAccessedVariable()).getDeclaringClass();
        }
        return null;
    }

    private ClassNode findVariableType(VariableExpression var, ClassNode rhsType) {
        ClassNode varType = var.getOriginType();
        if (varType == null) {
            varType = var.getType();
        }
        if (varType != null && !VariableScope.isVoidOrObject(varType)) {
            return varType;
        }
        if (rhsType != null && !VariableScope.isVoidOrObject(rhsType)) {
            return rhsType;
        }
        return VariableScope.OBJECT_CLASS_NODE;
    }
}
