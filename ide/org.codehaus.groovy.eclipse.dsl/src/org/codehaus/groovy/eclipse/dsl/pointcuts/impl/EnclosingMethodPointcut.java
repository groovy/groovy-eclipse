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

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Matches when current context is enclosed by a method declaration that satisfies
 * the given name (string) or constraints (pointcut).
 */
public class EnclosingMethodPointcut extends AbstractPointcut {

    public EnclosingMethodPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        MethodNode enclosing = pattern.getCurrentScope().getEnclosingMethodDeclaration();
        if (enclosing != null) {
            Object argument = getFirstArgument();
            if (argument instanceof String) {
                if (argument.equals(enclosing.getName())) {
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
