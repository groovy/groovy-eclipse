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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Tests that the type being analyzed matches.  The match can
 * either be a string match (ie - the type name),
 * or it can pass the current type to a containing pointcut.
 * 
 * Only looks at the current type, not the current type's hierarchy
 * @author andrew
 * @created Feb 10, 2011
 */
public class CurrentTypePointcut extends AbstractPointcut {

    public CurrentTypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    /**
     * toMatch is always ignored and the current type is used instead
     */
    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        // toMatch is ignored 
        
        Object firstArgument = getFirstArgument();
        ClassNode currentType = pattern.getCurrentType();
        if (firstArgument instanceof String) {
            if (currentType.getName().equals(firstArgument)) {
                return Collections.singleton(currentType);
            } else {
                return null;
            }
        } else if (firstArgument instanceof Class<?>) {
            if (currentType.getName().equals(((Class<?>) firstArgument).getName())) {
                return Collections.singleton(currentType);
            } else {
                return null;
            }
        } else if (firstArgument != null) {
            // we know this is a pointcut argument
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern, Collections.singleton(currentType));
        } else {
            // always match if there is no argument
            return Collections.singleton(currentType);
        }
    }

    /**
     * expecting one arg that is either a string or a pointcut or a class, or no arguments
     */
    @Override
    public void verify() throws PointcutVerificationException {
        String oneStringOrOnePointcutArg = oneStringOrOnePointcutOrOneClassArg();
        String argNumber = hasOneOrNoArgs();
        
        if (oneStringOrOnePointcutArg == null || argNumber == null) {
            super.verify();
            return;
        }
        throw new PointcutVerificationException("This pointcut expects either no arguments or 1 String or 1 pointcut argument", this);
    }
}