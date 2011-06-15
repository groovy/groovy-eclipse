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
import java.util.HashSet;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Takes two or more elements and 
 * @author andrew
 * @created Feb 10, 2011
 */
public class OrPointcut extends AbstractPointcut {

    public OrPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        Object[] args = getArgumentValues();
        Collection<Object> result = new HashSet<Object>();
        for (Object arg : args) {
            Collection<?> intermediate = matchOnPointcutArgumentReturnInner((IPointcut) arg, pattern, ensureCollection(toMatch));
            if (intermediate != null) {
                result.addAll(intermediate);
            }
        }
        return result.size() > 0 ? result : null;
    }


    /**
     * Flatten all contained 'or' pointcuts into the top level (but only if they are unnamed) 
     */
    public IPointcut normalize() {
        IPointcut newPointcut = super.normalize();
        
        if (newPointcut instanceof OrPointcut) {
            OrPointcut newOr = (OrPointcut) newPointcut;
            OrPointcut newNewOr = new OrPointcut(getContainerIdentifier(), "or");
            // flatten the ands
            for (int i = 0; i < newOr.getArgumentValues().length; i++) {
                String name = newOr.getArgumentNames()[i];
                Object argument = newOr.getArgumentValues()[i];
                if (argument instanceof OrPointcut && name == null) {
                    OrPointcut other = (OrPointcut) argument;
                    Object[] argumentValues = other.getArgumentValues();
                    String[] argumentNames = other.getArgumentNames();
                    int argCount = argumentNames.length;
                    for (int j = 0; j < argCount; j++) {
                        newNewOr.addArgument(argumentNames[j], argumentValues[j]);
                    } 
                } else {
                    newNewOr.addArgument(name, argument);
                }
            }
            return newNewOr;
        } else {
            return newPointcut;
        }
    }

    @Override
    public void verify() throws PointcutVerificationException {
        // don't call super
        String allArgsArePointcuts = allArgsArePointcuts();
        if (allArgsArePointcuts != null) {
            throw new PointcutVerificationException(allArgsArePointcuts, this);
        }
    }
    
//    @Override
//    protected IPointcut or(IPointcut other) {
//        if (other instanceof OrPointcut) {
//            Object[] argumentValues = other.getArgumentValues();
//            String[] argumentNames = other.getArgumentNames();
//            int argCount = argumentNames.length;
//            for (int i = 0; i < argCount; i++) {
//                this.addArgument(argumentNames[i], argumentValues[i]);
//            }
//        } else {
//            addArgument(other);
//        }
//        return this;
//    }
}
