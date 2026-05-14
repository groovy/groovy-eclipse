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
import java.util.Set;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Negates the pointcut that this one encloses.
 */
public class NotPointcut extends AbstractPointcut {

    private static final Set<Object> EMPTY_MATCH = Collections.singleton(new Object());

    public NotPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public boolean fastMatch(GroovyDSLDContext pattern) {
        return matches(pattern, EMPTY_MATCH) != null;
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        Collection<?> collection;
        if (toMatch instanceof Collection) {
            collection = (Collection<?>) toMatch;
        } else {
            collection = Collections.singleton(toMatch);
        }
        Collection<?> result = matchOnPointcutArgument((IPointcut) getFirstArgument(), pattern, collection);
        if (result != null) {
            return null;
        } else {
            return EMPTY_MATCH;
        }
    }

    @Override
    public void verify() throws PointcutVerificationException {
        super.verify();
        Object arg = getFirstArgument();
        if (arg instanceof IPointcut) {
            ((IPointcut) arg).verify();
        } else {
            throw new PointcutVerificationException(
                "A pointcut is required as the single argument to the 'not' pointcut", this);
        }
    }
}
