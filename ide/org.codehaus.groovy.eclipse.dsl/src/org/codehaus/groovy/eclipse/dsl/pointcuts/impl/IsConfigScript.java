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
import java.util.Objects;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class IsConfigScript extends AbstractPointcut {

    public IsConfigScript(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public boolean fastMatch(GroovyDSLDContext context) {
        return (matches(context, null) != null);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext context, Object toMatch) {
        String configScript = context.getCurrentOptions().get(CompilerOptions.OPTIONG_GroovyCompilerConfigScript);
        if (Objects.equals(context.fullPathName, configScript)) {
            return (toMatch instanceof Collection ? (Collection<?>) toMatch
                : (toMatch != null ? Collections.singleton(toMatch) : Collections.emptySet()));
        }
        return null;
    }

    @Override
    public void verify() throws PointcutVerificationException {
        String result = hasNoArgs();
        if (result != null) {
            throw new PointcutVerificationException(result, this);
        }
    }
}
