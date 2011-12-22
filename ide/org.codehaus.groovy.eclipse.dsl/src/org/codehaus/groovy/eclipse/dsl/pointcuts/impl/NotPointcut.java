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
import java.util.Set;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Negates the pointcut that this one encloses
 * @author andrew
 * @created Feb 10, 2011
 */
public class NotPointcut extends AbstractPointcut {

    private static final Set<Object> EMPTY_MATCH = Collections.singleton(new Object());

    public NotPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    public boolean fastMatch(GroovyDSLDContext pattern) {
        return matches(pattern, EMPTY_MATCH) != null;
    }

    
    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        Collection<?> collection;
        if (toMatch instanceof Collection) {
            collection = (Collection<?>) toMatch;
        } else {
            collection = Collections.singleton(toMatch);
        }
        Collection<?> result = matchOnPointcutArgument((IPointcut) getFirstArgument(), pattern, collection);
        if (result != null) {
            return null;
        } else {
            return EMPTY_MATCH;
        }
    }
    
    @Override
    public void verify() throws PointcutVerificationException {
        super.verify();
        Object arg = getFirstArgument();
        if (arg instanceof IPointcut) {
            ((IPointcut) arg).verify();
        } else {
            throw new PointcutVerificationException("A pointcut is required as the single argument to the 'not' pointcut", this);
        }
    }
}
