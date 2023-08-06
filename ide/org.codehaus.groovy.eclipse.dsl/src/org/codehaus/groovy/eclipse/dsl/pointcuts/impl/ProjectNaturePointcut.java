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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests that the {@link IProject} that contains the current pattern has a nature of the specified type.
 * This pointcut should be optimized so that it runs before any others in an 'and' or 'or' clause.  Also, its result should be cached in the
 * pattern providing a fail/succeed fast strategy.
 */
public class ProjectNaturePointcut extends AbstractPointcut {

    public ProjectNaturePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        for (String nature : pattern.projectNatures) {
            Object firstArgument = getFirstArgument();
            if (nature.equals(firstArgument) || nature.equals(SHORTCUTS.get(firstArgument))) {
                return Collections.singleton(nature);
            }
        }
        return null;
    }

    @Override
    public boolean fastMatch(GroovyDSLDContext pattern) {
        return matches(pattern, null) != null;
    }

    @Override
    public void verify() throws PointcutVerificationException {
        String maybeStatus = allArgsAreStrings();
        if (maybeStatus != null) {
            throw new PointcutVerificationException(maybeStatus, this);
        }
        maybeStatus = hasOneArg();
        if (maybeStatus != null) {
            throw new PointcutVerificationException(maybeStatus, this);
        }
        super.verify();
    }

    /**
     * In order to remove Eclipse-specific identifiers in scripts,
     * common project natures can use a shortcut instead of the full nature string
     */
    private static final Map<String, String> SHORTCUTS = new HashMap<>();
    static {
        SHORTCUTS.put("java", JavaCore.NATURE_ID);
        SHORTCUTS.put("groovy", GroovyNature.GROOVY_NATURE);
        SHORTCUTS.put("grails", "com.springsource.sts.grails.core.nature");
        SHORTCUTS.put("gradle", "com.springsource.sts.gradle.core.nature");
    }
}
