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
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

/**
 * Tests that the type being analyzed matches.  The match can
 * either be a string match (ie - the type name),
 * or it can pass the current type to a containing pointcut
 */
public class EnclosingScriptPointcut extends AbstractPointcut {

    public EnclosingScriptPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        ClassNode enclosing = pattern.getCurrentScope().getEnclosingTypeDeclaration();
        if (enclosing == null || !GroovyUtils.isScript(enclosing)) {
            return null;
        }

        Collection<ClassNode> enclosingCollection = Collections.singleton(enclosing);

        Object firstArgument = getFirstArgument();
        if (firstArgument instanceof String) {
            if (enclosing.getName().equals(firstArgument)) {
                return enclosingCollection;
            } else {
                return null;
            }
        } else if (firstArgument instanceof Class) {
            if (enclosing.getName().equals(((Class<?>) firstArgument).getName())) {
                return enclosingCollection;
            } else {
                return null;
            }
        } else if (firstArgument == null) {
            return enclosingCollection;
        } else {
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern, enclosingCollection);
        }
    }

    /**
     * expecting no args or one arg that is either a string or a pointcut or a class
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
