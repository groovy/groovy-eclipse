/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 * Tests that the type being analyzed matches.  The match can
 * either be a string match (ie - the type name),
 * or it can pass the current type to a containing pointcut
 * @author andrew
 * @created Feb 10, 2011
 */
public class EnclosingMethodPointcut extends AbstractPointcut {

    public EnclosingMethodPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        MethodNode enclosing = pattern.getCurrentScope().getEnclosingMethodDeclaration();
        if (enclosing == null) {
            return null;
        }
        
        Object firstArgument = getFirstArgument();
        Collection<MethodNode> enclosingCollection = Collections.singleton(enclosing);
        if (firstArgument instanceof String) {
            if (enclosing.getName().equals(firstArgument)) {
                return enclosingCollection;
            } else {
                return null;
            }
        } else {
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern, enclosingCollection);
        }
    }

    /**
     * expecting one arg that is either a string or a pointcut or a class
     */
    @Override
    public void verify() throws PointcutVerificationException {
        String oneStringOrOnePointcutArg = oneStringOrOnePointcutArg();
        if (oneStringOrOnePointcutArg != null) {
            throw new PointcutVerificationException(oneStringOrOnePointcutArg, this);
        }
        super.verify();
    }
}