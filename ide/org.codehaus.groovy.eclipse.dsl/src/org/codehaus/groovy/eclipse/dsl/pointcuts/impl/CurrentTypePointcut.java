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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Tests that the type being analyzed matches.  The match can
 * either be a string match (ie - the type name),
 * or it can pass the current type to a containing pointcut.
 *
 * Only looks at the current type, not the current type's hierarchy
 */
public class CurrentTypePointcut extends AbstractPointcut {

    public CurrentTypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    /**
     * {@code toMatch} is always ignored and the current type is used instead.
     */
    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        ClassNode currentType = pattern.getCurrentType();
        if (currentType != null) {
            Object firstArgument = getFirstArgument();
            if (firstArgument instanceof String) {
                if (currentType.getName().equals(firstArgument)) {
                    return Collections.singleton(currentType);
                }
            } else if (firstArgument instanceof Class) {
                if (currentType.getName().equals(((Class<?>) firstArgument).getName())) {
                    return Collections.singleton(currentType);
                }
            } else if (firstArgument != null) {
                // we know this is a pointcut argument
                return matchOnPointcutArgument((IPointcut) firstArgument, pattern, Collections.singleton(currentType));
            } else {
                // always match if there is no argument
                return Collections.singleton(currentType);
            }
        }
        return null;
    }

    @Override
    public void verify() throws PointcutVerificationException {
        String oneClassOrStringOrPointcutArg = oneStringOrOnePointcutOrOneClassArg();
        String oneOrNone = hasOneOrNoArgs();

        if (oneClassOrStringOrPointcutArg == null || oneOrNone == null) {
            super.verify();
            return;
        }
        throw new PointcutVerificationException("This pointcut expects either 0 arguments or 1 argument of type Pointcut or String or Class", this);
    }
}
