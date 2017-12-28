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
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import java.util.Collection;

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * the match returns true if the pattern passed in is an identifier expression
 * (either a {@link ConstantExpression} or a {@link VariableExpression} whose text
 * that matches the contained argument
 */
public class CurrentIdentifierPointcut extends FilteringPointcut<Expression> {

    public CurrentIdentifierPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, Expression.class);
    }


    /**
     * Ignore toMatch and use the current node instead
     */
    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        return super.matches(pattern, pattern.getCurrentScope().getCurrentNode());
    }

    @Override
    protected Expression filterObject(Expression result, GroovyDSLDContext context, String firstArgAsString) {
        if (result instanceof VariableExpression || result instanceof ConstantExpression) {
            if (firstArgAsString == null || result.getText().equals(firstArgAsString)) {
                return result;
            }
        }
        return null;
    }
}
