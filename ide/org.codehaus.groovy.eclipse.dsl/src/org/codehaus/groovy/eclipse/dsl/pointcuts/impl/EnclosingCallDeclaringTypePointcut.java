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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;

/**
 * Tests that the declaring type of the enclosing call matches the name or type passed in as an argument.
 */
public class EnclosingCallDeclaringTypePointcut extends AbstractPointcut {

    public EnclosingCallDeclaringTypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    /**
     * Outer pointcut bindings not allowed here.
     * return on match is always a singleton
     * Just like {@link CurrentTypePointcut}
     */
    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        List<CallAndType> enclosing = pattern.getCurrentScope().getAllEnclosingMethodCallExpressions();
        if (enclosing == null) {
            return null;
        }
        Object firstArgument = getFirstArgument();
        if (firstArgument == null) {
            List<CallAndType> allEnclosingMethodCallExpressions = pattern.getCurrentScope().getAllEnclosingMethodCallExpressions();
            if (allEnclosingMethodCallExpressions != null && allEnclosingMethodCallExpressions.size() > 0) {
                List<ClassNode> enclosingCallTypes = new ArrayList<ClassNode>(allEnclosingMethodCallExpressions.size());
                for (CallAndType callAndType : allEnclosingMethodCallExpressions) {
                    enclosingCallTypes.add(callAndType.getPerceivedDeclaringType());
                }
                return enclosingCallTypes;
            }
            return null;
        }
        if (firstArgument instanceof Class) {
            firstArgument = ((Class<?>) firstArgument).getName();
        }
        if (firstArgument instanceof String) {
            ClassNode matchingType = matchesInCalls(enclosing, (String) firstArgument, pattern);
            if (matchingType != null) {
                return Collections.singleton(matchingType);
            } else {
                return null;
            }
        } else {
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern, asTypeList(enclosing));
        }
    }

    private List<ClassNode> asTypeList(List<CallAndType> enclosing) {
        List<ClassNode> types = new ArrayList<ClassNode>(enclosing.size());
        for (CallAndType callAndType : enclosing) {
            types.add(callAndType.getPerceivedDeclaringType());
        }
        return types;
    }

    private ClassNode matchesInCalls(List<CallAndType> enclosing, String typeName, GroovyDSLDContext pattern) {
        for (CallAndType callAndType : enclosing) {
            ClassNode declaringType =
                callAndType.getPerceivedDeclaringType();
            if (declaringType.getName().equals(typeName)) {
                return declaringType;
            }
        }
        return null;
    }

    /**
     * expecting one arg that is either a string or a pointcut or one class
     */
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
