/*
 * Copyright 2009-2020 the original author or authors.
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
import org.eclipse.jdt.core.Flags;

/**
 * Records variable types in the scope based on declarations and assignments.
 */
public class AssignmentStorer {

    /**
     * Stores the result of an assignment expression in the given scope.
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
     * @param scope scope to store result(s)
     * @param rhsType inferred type of the right-hand side (right expression of {@code exp})
     */
    public void storeAssignment(final BinaryExpression exp, final VariableScope scope, final ClassNode rhsType) {
        assert exp.getOperation().isA(Types.ASSIGNMENT_OPERATOR);

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

    /**
     * Stores static import fields and methods in the given scope.
     */
    public void storeImport(final ImportNode node, final VariableScope scope) {
        ClassNode type = node.getType();
        if (node.isStar() && type != null) {
            List<FieldNode> fields = type.getFields();
            for (FieldNode field : fields) {
                if (isStaticNotSynthetic(field)) {
                    scope.addVariable(field.getName(), field.getType(), type);
                }
            }

            List<MethodNode> methods = type.getMethods();
            for (MethodNode method : methods) {
                if (isStaticNotSynthetic(method)) {
                    scope.addVariable(method.getName(), method.getReturnType(), type);
                }
            }
        } else if (node.isStatic() && type != null) {
            String name = node.getFieldName();
            if (name != null) {
                String alias = node.getAlias();
                if (alias == null) alias = name;

                FieldNode field = type.getField(name);
                if (isStaticNotSynthetic(field)) {
                    scope.addVariable(alias, field.getType(), type);
                }

                List<MethodNode> methods = type.getDeclaredMethods(name);
                for (MethodNode method : methods) {
                    if (isStaticNotSynthetic(method)) {
                        scope.addVariable(alias, method.getReturnType(), type);
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    private static void handleMultiAssignment(final TupleExpression lhs, final Expression rhs, final VariableScope scope, final ClassNode rhsListType) {
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

    private static void handleSingleAssignment(final Expression lhs, final VariableScope scope, final ClassNode rhsType) {
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

        } else if (lhs instanceof ConstantExpression || lhs instanceof BinaryExpression) {
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

    private static ClassNode findComponentType(final ClassNode type) {
        return (type == null ? VariableScope.OBJECT_CLASS_NODE : VariableScope.extractElementType(type));
    }

    /**
     * Finds the declaring type of the accessed variable. Will be {@code null} if this is a local variable.
     */
    private static ClassNode findDeclaringType(final VariableExpression var) {
        if (var.getAccessedVariable() instanceof AnnotatedNode) {
            return ((AnnotatedNode) var.getAccessedVariable()).getDeclaringClass();
        }
        return null;
    }

    private static ClassNode findVariableType(final VariableExpression var, final ClassNode rhsType) {
        ClassNode varType = var.getOriginType();
        if (varType == null) {
            varType = var.getType();
        }
        if (varType != null && !VariableScope.isVoidOrObject(varType)) {
            return varType;
        }
        if (!VariableScope.isVoidOrObject(rhsType)) {
            return rhsType;
        }
        return VariableScope.OBJECT_CLASS_NODE;
    }

    private static boolean isStaticNotSynthetic(final FieldNode node) {
        return node != null && (node.getModifiers() & (Flags.AccStatic | Flags.AccSynthetic)) == Flags.AccStatic;
    }

    private static boolean isStaticNotSynthetic(final MethodNode node) {
        return node != null && (node.getModifiers() & (Flags.AccBridge | Flags.AccStatic | Flags.AccSynthetic)) == Flags.AccStatic;
    }
}
