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
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Matches when the current type is the same as the lexically enclosing type.
 * Generally, this matches for references to 'this' or when a new expression is
 * being started.  However, this pointuct may not match on 'this' when inside a
 * closure.
 *
 * @see IsThisTypePointcut
 */
public class CurrentTypeIsEnclosingTypePointcut extends AbstractPointcut {

    public CurrentTypeIsEnclosingTypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext context, Object dontCare) {
        ClassNode currentType = context.getCurrentType();
        ClassNode enclosingType = context.getCurrentScope().getEnclosingTypeDeclaration();
        if (currentType != null && enclosingType != null && currentType.redirect() == enclosingType.redirect()) {
            return Collections.singleton(currentType);
        }
        return null;
    }

    @Override
    public void verify() throws PointcutVerificationException {
        String noArgs = hasNoArgs();
        if (noArgs != null) {
            throw new PointcutVerificationException(noArgs, this);
        }
        super.verify();
    }
}
