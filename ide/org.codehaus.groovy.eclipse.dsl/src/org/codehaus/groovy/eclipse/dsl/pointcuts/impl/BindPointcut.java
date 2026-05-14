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

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * The bind() pointcut takes a named argument where the argument is another pointcut.
 */
public class BindPointcut extends AbstractPointcut {

    public BindPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        // convert toMatch to a list
        return matchOnPointcutArgumentReturnInner((IPointcut) getFirstArgument(), pattern, ensureCollection(toMatch));
    }

    @Override
    public void verify() throws PointcutVerificationException {
        super.verify();
        Object arg = getFirstArgument();
        if (arg instanceof IPointcut) {
            String name = getFirstArgumentName();
            if (name == null) {
                throw new PointcutVerificationException("bind requires a named argument", this);
            }
            ((IPointcut) arg).verify();
        } else {
            throw new PointcutVerificationException("A pointcut is required as the single argument to bind", this);
        }
    }
}
