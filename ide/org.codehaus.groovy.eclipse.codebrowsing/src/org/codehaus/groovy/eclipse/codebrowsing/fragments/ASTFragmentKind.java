/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.fragments;

import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;

public enum ASTFragmentKind {
    PROPERTY, SAFE_PROPERTY, SPREAD_SAFE_PROPERTY, METHOD_POINTER, METHOD_CALL, BINARY, SIMPLE_EXPRESSION, EMPTY, ENCLOSING;

    static ASTFragmentKind toPropertyKind(Expression expr) {
        if (expr instanceof PropertyExpression) {
            PropertyExpression prop = (PropertyExpression) expr;
            if (prop.isSafe()) {
                return SAFE_PROPERTY;
            } else if (prop.isSpreadSafe()) {
                return SPREAD_SAFE_PROPERTY;
            } else {
                return PROPERTY;
            }
        } else if (expr instanceof MethodPointerExpression) {
            return METHOD_POINTER;
        } else if (expr instanceof MethodCallExpression) {
            return METHOD_CALL;
        } else if (expr instanceof BinaryExpression) {
            return BINARY;
        } else {
            return SIMPLE_EXPRESSION;
        }

    }

    /**
     * @param fragment
     * @return
     */
    public static boolean isExpressionKind(IASTFragment fragment) {
        if (fragment == null)
            return false;

        ASTFragmentKind kind = fragment.kind();
        switch (kind) {
            case PROPERTY:
            case SAFE_PROPERTY:
            case SPREAD_SAFE_PROPERTY:
            case METHOD_POINTER:
            case METHOD_CALL:
            case BINARY:
            case SIMPLE_EXPRESSION:
                return true;
            default:
                return false;
        }
    }
}