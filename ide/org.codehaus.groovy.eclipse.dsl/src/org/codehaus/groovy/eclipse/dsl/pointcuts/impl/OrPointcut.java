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
 * Takes two or more elements and 
 * @author andrew
 * @created Feb 10, 2011
 */
public class OrPointcut extends AbstractPointcut {

    public OrPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    public BindingSet matches(GroovyDSLDContext pattern) {
        Object[] args = getArgumentValues();
        BindingSet bindings = null;
        for (Object arg : args) {
            BindingSet other = ((IPointcut) arg).matches(pattern);
            if (other != null) {
                if (bindings == null) {
                    bindings = other;
                } else {
                    bindings.combineBindings(other);
                }
                
                // now check for binding
                String name = getNameForArgument(arg);
                if (name != null) {
                    bindings.addBinding(name, other.getDefaultBinding());
                }
            }
        }
        // will be null if no clauses match
        return bindings;
    }

    public IPointcut normalize() {
        Object[] args = getArgumentValues();
        for (Object arg : args) {
            ((IPointcut) arg).normalize();
        }
        return super.normalize();
    }

    @Override
    public void verify() throws PointcutVerificationException{
        String allArgsArePointcuts = allArgsArePointcuts();
        if (allArgsArePointcuts == null) {
            super.verify();
        } else {
            throw new PointcutVerificationException(allArgsArePointcuts, this);
        }
    }
    
    @Override
    protected IPointcut or(IPointcut other) {
        if (other instanceof OrPointcut) {
            Object[] argumentValues = other.getArgumentValues();
            String[] argumentNames = other.getArgumentNames();
            int argCount = argumentNames.length;
            for (int i = 0; i < argCount; i++) {
                this.addArgument(argumentNames[i], argumentValues[i]);
            }
        } else {
            addArgument(other);
        }
        return this;
    }
}
