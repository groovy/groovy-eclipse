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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;

/**
 * Tests that the declaring type of an enclosing call matches the name or type passed in as an argument.
 */
public class EnclosingCallDeclaringTypePointcut extends AbstractPointcut {

    public EnclosingCallDeclaringTypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        List<CallAndType> enclosing = pattern.getCurrentScope().getAllEnclosingMethodCallExpressions();
        if (enclosing == null) {
            return null;
        }

        Function<CallAndType, ClassNode> toResult = CallAndType::getPerceivedDeclaringType;

        Object firstArgument = getFirstArgument();
        if (firstArgument == null) {
            return enclosing.stream().map(toResult).collect(Collectors.toList());
        }
        if (firstArgument instanceof Class) {
            firstArgument = ((Class<?>) firstArgument).getName();
        }
        if (firstArgument instanceof String) {
            for (CallAndType callAndType : enclosing) {
                ClassNode declaringType = toResult.apply(callAndType);
                if (firstArgument.equals(declaringType.getName())) {
                    return Collections.singletonList(declaringType);
                }
            }
            return null;
        } else {
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern, enclosing.stream().map(toResult).collect(Collectors.toList()));
        }
    }

    @Override
    public void verify() throws PointcutVerificationException {
        String result = hasNoArgs();
        if (result == null) {
            return;
        }
        result = oneStringOrOnePointcutOrOneClassArg();
        if (result != null) {
            throw new PointcutVerificationException(result, this);
        }
        super.verify();
    }
}
