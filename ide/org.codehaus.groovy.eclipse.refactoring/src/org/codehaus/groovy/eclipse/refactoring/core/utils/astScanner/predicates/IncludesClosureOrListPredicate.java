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

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTVisitorDecorator;

public class IncludesClosureOrListPredicate extends ASTVisitorDecorator<Boolean> {

    private final int line;

    public IncludesClosureOrListPredicate(Boolean container, int line) {
        super(container);
        this.line = line;
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (expression.getLineNumber() == line) {
            container = true;
        }
    }

    @Override
    public void visitListExpression(ListExpression expression) {
        if (expression.getLineNumber() == line) {
            container = true;
        }
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
        if (expression.getLineNumber() == line) {
            container = true;
        }
    }
}
