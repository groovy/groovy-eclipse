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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;

/**
 * Tests that the return type of the enclosing call matches the name or type passed in as an argument
 * @author andrew
 * @created Feb 10, 2011
 */
public class EnclosingCallReturnTypePointcut extends AbstractPointcut {

    public EnclosingCallReturnTypePointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        CallAndType enclosing = pattern.getCurrentScope().getEnclosingMethodCallExpression();
        if (enclosing == null) {
            return null;
        }
        
        Object firstArgument = getFirstArgument();
        ClassNode declaringType = enclosing.declaringType;
        if (firstArgument instanceof String) {
            if (declaringType.getName().equals(firstArgument)) {
                return new BindingSet().addDefaultBinding(declaringType);
            } else {
                return null;
            }
        } else if (firstArgument instanceof Class<?>) {
            if (pattern.matchesType(((Class<?>) firstArgument).getName(), declaringType)) {
                return new BindingSet().addDefaultBinding(enclosing);
            } else {
                return null;
            }
        } else {
            pattern.setOuterPointcutBinding(declaringType);
            BindingSet matches = matchOnPointcutArgument((IPointcut) firstArgument, pattern);
            if (matches != null) {
                matches.addDefaultBinding(enclosing);
            }
            return matches;
        }
    }

    /**
     * expecting one arg that is either a string or a pointcut or one class
     */
    @Override
    public void verify() throws PointcutVerificationException {
        String oneStringOrOnePointcutArg = oneStringOrOnePointcutOrOneClassArg();
        if (oneStringOrOnePointcutArg != null) {
            throw new PointcutVerificationException(oneStringOrOnePointcutArg, this);
        }
        super.verify();
    }
}