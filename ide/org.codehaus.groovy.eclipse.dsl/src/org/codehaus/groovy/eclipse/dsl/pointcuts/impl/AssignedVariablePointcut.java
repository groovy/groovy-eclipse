/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import java.util.Collection;
import java.util.Collections;

import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Matches when current context is enclosed by a variable assignment that
 * satisfies the given name (string) or constraints (pointcut).
 */
public class AssignedVariablePointcut extends AbstractPointcut {

    public AssignedVariablePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        BinaryExpression enclosing = (BinaryExpression) pattern.getCurrentScope().getWormhole().get("enclosingAssignment");
        if (enclosing != null && enclosing.getLeftExpression() instanceof Variable) {
            Object argument = getFirstArgument();
            if (argument instanceof String) {
                if (argument.equals(((Variable) enclosing.getLeftExpression()).getName())) {
                    return Collections.singleton(enclosing);
                }
            } else {
                return matchOnPointcutArgument((IPointcut) argument, pattern, Collections.singleton(enclosing));
            }
        }
        return null;
    }

    @Override
    public void verify() throws PointcutVerificationException {
        String failure = oneStringOrOnePointcutArg();
        if (failure != null) {
            throw new PointcutVerificationException(failure, this);
        }
        super.verify();
    }
}
