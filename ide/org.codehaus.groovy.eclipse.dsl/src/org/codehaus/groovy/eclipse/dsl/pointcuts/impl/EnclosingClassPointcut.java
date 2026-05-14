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

import static org.codehaus.groovy.transform.trait.Traits.isTrait;

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
 * Tests that the type being analyzed matches. The match can either be a string
 * match (i.e. the type name), or it can pass the current type to a pointcut.
 */
public class EnclosingClassPointcut extends AbstractPointcut {

    public EnclosingClassPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    /**
     * Expects no args or one arg that is either a string or a pointcut or a class.
     */
    @Override
    public void verify() throws PointcutVerificationException {
        String hasOneOrNoArgs = hasOneOrNoArgs();
        if (hasOneOrNoArgs != null) {
            throw new PointcutVerificationException(hasOneOrNoArgs, this);
        }
        super.verify();
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        ClassNode enclosingType = pattern.getCurrentScope().getEnclosingTypeDeclaration();
        if (enclosingType == null ||
                GroovyUtils.isScript(enclosingType) ||
                enclosingType.isAnnotationDefinition() ||
                (enclosingType.isInterface() && !isTrait(enclosingType))) {
            return null;
        }

        String enclosingTypeName = enclosingType.getName();
        Object firstArgument = getFirstArgument();

        if (firstArgument instanceof String) {
            if (enclosingTypeName.equals(firstArgument)) {
                return Collections.singleton(enclosingType);
            }
        } else if (firstArgument instanceof Class) {
            if (enclosingTypeName.equals(((Class<?>) firstArgument).getName())) {
                return Collections.singleton(enclosingType);
            }
        } else if (firstArgument instanceof IPointcut) {
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern, Collections.singleton(enclosingType));
        } else {
            System.err.println("First argument to enclosingClass pointcut was not a Class, String, or IPointcut: " + firstArgument);
        }
        return null;
    }
}
