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
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;

public class AssignmentAndCall implements IASTNodePredicate {

    @Override
    public ASTNode evaluate(ASTNode input) {
        if (input instanceof ClosureExpression) {
            return input;
        } else if (isAssignment(input) || isMethodCall(input)) {
            return input;
        }
        return null;
    }

    public static boolean isAssignment(ASTNode node) {
        if (node instanceof DeclarationExpression) {
            return true;
        } else if (node instanceof BinaryExpression) {
            return true;
        } else if (node instanceof PostfixExpression) {
            return true;
        } else if (node instanceof PrefixExpression) {
            return true;
        }
        return false;
    }

    public static boolean isMethodCall(ASTNode node) {
        if (node instanceof StaticMethodCallExpression) {
            return true;
        }
        if (node instanceof MethodCallExpression) {
            return true;
        }
        return false;
    }
}
