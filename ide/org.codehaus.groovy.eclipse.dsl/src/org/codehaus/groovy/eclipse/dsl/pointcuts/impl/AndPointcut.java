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
public class AndPointcut extends AbstractPointcut {

    public AndPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        Object[] args = getArgumentValues();
        BindingSet bindings = null;
        Object outerBinding = pattern.getOuterPointcutBinding();
        for (Object arg : args) {
            // must re-set this value each time since each segment of the
            // and pointcut will change this value 
            pattern.setOuterPointcutBinding(outerBinding);
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
            } else {
                // failure
                return null;
            }
        }
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
    public void verify() throws PointcutVerificationException {
        String allArgsArePointcuts = allArgsArePointcuts();
        if (allArgsArePointcuts == null) {
            super.verify();
        } else {
            throw new PointcutVerificationException(allArgsArePointcuts, this);
        }
    }
    
    @Override
    protected IPointcut and(IPointcut other) {
        if (other instanceof AndPointcut) {
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
