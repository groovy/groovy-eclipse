/*
 * Copyright 2009-2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;

/**
 * Tests that the enclosing call matches certain charactereistics, such as names 
 * arguments, and values
 * @author andrew
 * @created Feb 10, 2011
 */
public class EnclosingCallPointcut extends AbstractPointcut {

    public EnclosingCallPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        List<CallAndType> enclosing = pattern.getCurrentScope().getAllEnclosingMethodCallExpressions();
        if (enclosing == null || enclosing.isEmpty()) {
            return null;
        }

        Object firstArgument = getFirstArgument();
        if (firstArgument instanceof String) {
            return matchesInCalls(enclosing, (String) firstArgument, pattern);
        } else if (firstArgument == null) {
            return asCallList(enclosing);
        } else {
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern, asCallList(enclosing));
        }
    }
    
    private List<MethodCallExpression> asCallList(List<CallAndType> enclosing) {
        List<MethodCallExpression> types = new ArrayList<MethodCallExpression>(enclosing.size());
        for (CallAndType callAndType : enclosing) {
            types.add(callAndType.call);
        }
        return types;
    }

    private List<MethodCallExpression> matchesInCalls(List<CallAndType> enclosing,
            String callName, GroovyDSLDContext pattern) {
        List<MethodCallExpression> calls = null;
        for (CallAndType callAndType : enclosing) {
            if (callName == null || callName.equals(callAndType.call.getMethodAsString())) {
                if (calls == null) {
                    calls = new ArrayList<MethodCallExpression>(1);
                }
                calls.add(callAndType.call);
            }
        }
        return calls;
    }


    /**
     * expecting one arg that is either a string or a pointcut
     */
    @Override
    public void verify() throws PointcutVerificationException {
        String hasOneOrNoArgs = hasOneOrNoArgs();
        if (hasOneOrNoArgs != null) {
            throw new PointcutVerificationException(hasOneOrNoArgs, this);
        }
        super.verify();
    }
}