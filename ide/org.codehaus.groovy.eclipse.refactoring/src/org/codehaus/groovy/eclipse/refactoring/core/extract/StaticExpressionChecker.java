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
package org.codehaus.groovy.eclipse.refactoring.core.extract;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;

/**
 * Checks to see if an expression is definitely not static.
 *
 * @author Andrew Eisenberg
 * @created May 11, 2010
 */
public class StaticExpressionChecker extends CodeVisitorSupport {


    boolean maybeStatic = true;


    public boolean doVisit(Expression e) {
        try {
            e.visit(this);
        } catch (VisitCompleteException vce) {
        }

        return maybeStatic;
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
        if (! expression.getField().isStatic()) {
            maybeStatic = false;
            throw new VisitCompleteException();
        }
        super.visitFieldExpression(expression);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (!(call.getObjectExpression() instanceof ClassExpression)) {
            maybeStatic = false;
            throw new VisitCompleteException();
        }
        super.visitMethodCallExpression(call);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        Variable accessedVar = expression.getAccessedVariable();
        boolean notStatic = false;
        if (accessedVar instanceof Parameter || accessedVar instanceof VariableExpression) {
            notStatic = true;
        } else if (accessedVar instanceof FieldNode) {
            notStatic = ! ((FieldNode) accessedVar).isStatic();
        } else if (accessedVar instanceof PropertyNode) {
            notStatic = ! ((PropertyNode) accessedVar).isStatic();
        }

        if (notStatic) {
            maybeStatic = false;
            throw new VisitCompleteException();
        }

        super.visitVariableExpression(expression);
    }

}
