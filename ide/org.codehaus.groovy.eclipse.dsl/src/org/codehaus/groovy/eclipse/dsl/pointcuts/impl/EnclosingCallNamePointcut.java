/*
 * Copyright 2009-2023 the original author or authors.
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;

/**
 * Tests that the enclosing call matches the name passed in as an argument.
 */
public class EnclosingCallNamePointcut extends AbstractPointcut {

    public EnclosingCallNamePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        List<CallAndType> enclosing = pattern.getCurrentScope().getAllEnclosingMethodCallExpressions();
        if (enclosing == null || enclosing.isEmpty()) {
            return null;
        }

        Function<CallAndType, String> toResult = (cat) -> cat.call.getMethodAsString();

        Object firstArgument = getFirstArgument();
        if (firstArgument == null) {
            return enclosing.stream().map(toResult).collect(Collectors.toList());
        } else if (firstArgument instanceof String) {
            List<Object> results = null;
            for (CallAndType callAndType : enclosing) {
                if (firstArgument.equals(callAndType.call.getMethodAsString())) {
                    if (results == null) {
                        results = new ArrayList<>(4);
                    }
                    results.add(toResult.apply(callAndType));
                }
            }
            return results;
        } else {
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern, enclosing.stream().map(toResult).collect(Collectors.toList()));
        }
    }

    @Override
    public void verify() throws PointcutVerificationException {
        String hasOneOrNoArgs = hasOneOrNoArgs();
        if (hasOneOrNoArgs != null) {
            throw new PointcutVerificationException(hasOneOrNoArgs, this);
        }
        super.verify();
    }
}
