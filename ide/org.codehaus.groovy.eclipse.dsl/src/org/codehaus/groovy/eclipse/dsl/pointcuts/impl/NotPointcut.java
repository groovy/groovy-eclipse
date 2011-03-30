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

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;

/**
 * Negates the pointcut that this one encloses
 * @author andrew
 * @created Feb 10, 2011
 */
public class NotPointcut extends AbstractPointcut {

    public NotPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        BindingSet matches = matchOnPointcutArgument((IPointcut) getFirstArgument(), pattern);
        if (matches == null) {
            String bindName = getFirstArgumentName();
            matches = new BindingSet(new Object());
            if (bindName != null) {
                // do we really need to bind to a name here?
                matches.addBinding(bindName, matches.getDefaultBinding());
            }
            return matches; 
        } else {
            return null;
        }
    }

    public IPointcut normalize() {
//        ((IPointcut) getFirstArgument()).normalize();
        return super.normalize();
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
