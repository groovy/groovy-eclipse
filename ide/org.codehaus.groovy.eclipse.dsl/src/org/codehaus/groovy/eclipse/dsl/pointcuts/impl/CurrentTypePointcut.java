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
 * Tests that the type being analyzed matches.  The match can
 * either be a string match (ie - the type name),
 * or it can pass the current type to a containing pointcut
 * @author andrew
 * @created Feb 10, 2011
 */
public class CurrentTypePointcut extends AbstractPointcut {

    public CurrentTypePointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        Object firstArgument = getFirstArgument();
        if (firstArgument instanceof String) {
            if (pattern.matchesType((String) firstArgument)) {
                return new BindingSet().addDefaultBinding(pattern.getCurrentType());
            } else {
                return null;
            }
        } else if (firstArgument instanceof Class<?>) {
            if (pattern.matchesType(((Class<?>) firstArgument).getName())) {
                return new BindingSet().addDefaultBinding(pattern.getCurrentType());
            } else {
                return null;
            }
        } else if (firstArgument != null) {
            // we know this is a pointcut argument
            pattern.setOuterPointcutBinding(pattern.getCurrentType());
            BindingSet matches = matchOnPointcutArgument((IPointcut) firstArgument, pattern);
            if (matches != null) {
                matches.addDefaultBinding(pattern.getCurrentType());
            }
            return matches;
        } else {
            // always match if there is no argument
            return new BindingSet(pattern.getCurrentType());
        }
    }

    /**
     * expecting one arg that is either a string or a pointcut or a class, or no arguments
     */
    @Override
    public String verify() {
        String oneStringOrOnePointcutArg = oneStringOrOnePointcutOrOneClassArg();
        if (oneStringOrOnePointcutArg == null || getArgumentValues().length == 0) {
            return super.verify();
        }
        return oneStringOrOnePointcutArg;
    }
}