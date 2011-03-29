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
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;

/**
 * Tests that the enclosing call matches the name passed in as an argument
 * @author andrew
 * @created Feb 10, 2011
 */
public class EnclosingCallNamePointcut extends AbstractPointcut {

    public EnclosingCallNamePointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        CallAndType enclosing = pattern.getCurrentScope().getEnclosingMethodCallExpression();
        if (enclosing == null) {
            return null;
        }
        
        Object firstArgument = getFirstArgument();
        String methodName = enclosing.call.getMethodAsString();
        if (firstArgument instanceof String) {
            if (methodName.equals(firstArgument)) {
                return new BindingSet().addDefaultBinding(methodName);
            } else {
                return null;
            }
        } else {
            pattern.setOuterPointcutBinding(methodName);
            BindingSet matches = matchOnPointcutArgument((IPointcut) firstArgument, pattern);
            if (matches != null) {
                matches.addDefaultBinding(enclosing);
            }
            return matches;
        }
    }

    /**
     * expecting one arg that is either a string or a pointcut
     */
    @Override
    public void verify() throws PointcutVerificationException {
        String oneStringOrOnePointcutArg = oneStringOrOnePointcutArg();
        if (oneStringOrOnePointcutArg != null) {
            throw new PointcutVerificationException(oneStringOrOnePointcutArg, this);
        }
        super.verify();
    }
}