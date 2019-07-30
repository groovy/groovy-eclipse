/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
 * Records variable types in the scope based on declarations and assignments.
 */
public class AssignmentStorer {

    /**
     * Stores the result of an current assignment expression in the given scope.
     * <p>
     * There are several possibilities here:
     * <ul>
     * <li>If {@link DeclarationExpression} and the declared type isn't Object
     *     (aka {@code def}), use that type.</li>
     * <li>If {@link DeclarationExpression} and the declared type is Object
     *     (aka {@code def}) and there is an initialization expression, use the
     *     type of the init expression; add to VariableScope, don't replace</li>
     * <li>If {@link BinaryExpression} and the operation is an assignment and
     *     the declared type is Object (aka {@code def}), use the type on the
     *     right-hand side; update in VariableScope, don't add</li>
     * </ul>
     *
     * @param exp assignment expression
     * @param scope scope to store result in
     * @param rhsType inferred type of the right-hand side (right expression of {@code exp})
     */
    public void storeAssignment(BinaryExpression exp, VariableScope scope, ClassNode rhsType) {
        assert Types.ofType(exp.getOperation().getType(), Types.ASSIGNMENT_OPERATOR);

        if (exp instanceof DeclarationExpression) {
            DeclarationExpression decl = (DeclarationExpression) exp;
            if (decl.isMultipleAssignmentDeclaration()) {
                TupleExpression vars = decl.getTupleExpression();
                handleMultiAssignment(vars, decl.getRightExpression(), scope, rhsType);
            } else {
                VariableExpression var = decl.getVariableExpression();
                handleSingleAssignment(var, scope, rhsType);
            }
        } else {
            Expression lhs = exp.getLeftExpression();
            if (lhs instanceof TupleExpression) {
                TupleExpression tuple = (TupleExpression) lhs;
                handleMultiAssignment(tuple, exp.getRightExpression(), scope, rhsType);
            } else {
                handleSingleAssignment(lhs, scope, rhsType);
            }
        }
    }

    public void storeField(FieldNode node, VariableScope scope) {
        Expression init = node.getInitialExpression();
        if (init != null && !VariableScope.OBJECT_CLASS_NODE.equals(init.getType())) {
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

    //--------------------------------------------------------------------------

    private static void handleMultiAssignment(TupleExpression lhs, Expression rhs, VariableScope scope, ClassNode rhsListType) {
        List<Expression> lhsExprs = lhs.getExpressions();
        List<Expression> rhsExprs = rhs instanceof ListExpression ? ((ListExpression) rhs).getExpressions() : Collections.emptyList();

        // try to associate each tuple expression element with something on the right-hand side
        for (int i = 0, lhsSize = lhsExprs.size(), rhsSize = rhsExprs.size(); i < lhsSize; i += 1) {
            Expression lhsExpr = lhsExprs.get(i);
            ClassNode  rhsType = (i < rhsSize ? rhsExprs.get(i).getType() : findComponentType(rhsListType));

            if (lhsExpr instanceof VariableExpression) {
                VariableExpression var = (VariableExpression) lhsExpr;
                scope.addVariable(var.getName(), findVariableType(var, rhsType), null);
            }
        }
    }

    private static void handleSingleAssignment(Expression lhs, VariableScope scope, ClassNode rhsType) {
        if (lhs instanceof VariableExpression) {
            VariableExpression var = (VariableExpression) lhs;
            if (var.getAccessedVariable() == var) {
                scope.addVariable(var.getName(), findVariableType(var, rhsType), null);

            } else if (scope.inScriptRunMethod() || scope.getEnclosingClosure() != null ||
                    scope.getEnclosingTypeDeclaration().equals(findDeclaringType(var))) {
                // undeclared variables are allowed in scripts or unqualified names may resolve to something in a closure or the declaring type
                scope.updateOrAddVariable(var.getName(), findVariableType(var, rhsType), findDeclaringType(var));
            } else {
                // undeclared variables are not allowed; do not add, just update
                scope.updateVariable(var.getName(), findVariableType(var, rhsType), findDeclaringType(var));
            }
            scope.getWormhole().put("lhs", lhs);

        } else if (lhs instanceof ConstantExpression) {
            // in "a.b.c = x", lhs is "c" and rhsType is "typeof(x)"

            // save ref to help find an accessor
            scope.getWormhole().put("lhs", lhs);
            lhs.putNodeMetaData("rhsType", rhsType);

        } else if (lhs instanceof PropertyExpression) {
            PropertyExpression exp = (PropertyExpression) lhs;
            handleSingleAssignment(exp.getProperty(), scope, rhsType);
        }/* else {
            System.err.println("AssignmentStorer.storeAssignment: LHS is " + lhs.getClass().getSimpleName());
        }*/
    }

    private static ClassNode findComponentType(ClassNode type) {
        return (type == null ? VariableScope.OBJECT_CLASS_NODE : VariableScope.extractElementType(type));
    }

    /**
     * Finds the declaring type of the accessed variable. Will be {@code null} if this is a local variable.
     */
    private static ClassNode findDeclaringType(VariableExpression var) {
        if (var.getAccessedVariable() instanceof AnnotatedNode) {
            return ((AnnotatedNode) var.getAccessedVariable()).getDeclaringClass();
        }
        return null;
    }

    private static ClassNode findVariableType(VariableExpression var, ClassNode rhsType) {
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
