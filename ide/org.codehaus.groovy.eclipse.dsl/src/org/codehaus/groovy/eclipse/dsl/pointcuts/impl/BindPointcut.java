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

/**
 * The bind() pointcut takes a named argument where the argument is another pointcut.
 * @author andrew
 * @created Feb 10, 2011
 */
public class BindPointcut extends AbstractPointcut {
    
    
    public BindPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        return matchOnPointcutArgument((IPointcut) getFirstArgument(), pattern);
    }

    public IPointcut normalize() {
        ((IPointcut) getFirstArgument()).normalize();
        return super.normalize();
    }
    
    @Override
    public String verify() {
        String status = super.verify();
        if (status != null) {
            return status;
        } else {
            Object arg = getFirstArgument();
            if (arg instanceof IPointcut) {
                String name = getFirstArgumentName();
                if (name == null) {
                    return "bind requires a named argument";
                }                
                return ((IPointcut) arg).verify();
            } else {
                return "A pointcut is required as the single argument to bind";
            }
        }
    }

}
