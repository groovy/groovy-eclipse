/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests that the {@link IProject} that contains the current pattern has a nature of the specified type.
 * This pointcut should be optimized so that it runs before any others in an 'and' or 'or' clause.  Also, its result should be cached in the 
 * pattern providing a fail/succeed fast strategy.
 * 
 * @author andrew
 * @created Feb 10, 2011
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
     * @param shortcut
     * @return
     */
    private static final Map<String, String> SHORTCUTS = new HashMap<String, String>();
    static {
        SHORTCUTS.put("groovy", GroovyNature.GROOVY_NATURE);
        SHORTCUTS.put("java", JavaCore.NATURE_ID);
        SHORTCUTS.put("grails", "com.springsource.sts.grails.core.nature");
        SHORTCUTS.put("gradle", "com.springsource.sts.gradle.core.nature");
    }
}
