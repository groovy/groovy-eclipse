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

import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.FragmentVisitor;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.MethodCallFragment;

/**
 * Checks to see if an expression is definitely not static.
 *
 * @author Andrew Eisenberg
 * @created May 11, 2010
 */
public class StaticFragmentChecker extends FragmentVisitor {


    private boolean maybeStatic = true;

    private boolean previousWasClassExpression = false;

    private StaticExpressionChecker expressionChecker;

    public boolean mayNotBeStatic(IASTFragment fragment) {
        expressionChecker = new StaticExpressionChecker();
        fragment.accept(this);
        return maybeStatic;
    }

    @Override
    public boolean previsit(IASTFragment fragment) {
        if (fragment.kind() == ASTFragmentKind.METHOD_CALL && !previousWasClassExpression) {
            maybeStatic = false;
            return false;
        }
        Expression node = fragment.getAssociatedExpression();
        if (node != null) {
            maybeStatic = expressionChecker.doVisit(node);
        }
        previousWasClassExpression = node instanceof ClassExpression;
        return maybeStatic;
    }

    @Override
    public boolean visit(MethodCallFragment fragment) {
        Expression node = fragment.getArguments();
        if (node != null) {
            maybeStatic = expressionChecker.doVisit(node);
        }
        return maybeStatic;
    }
}
