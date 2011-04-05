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

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;

/**
 * Tests that there is an enclosing closure
 * @author andrew
 * @created Feb 10, 2011
 */
public class EnclosingClosurePointcut extends AbstractPointcut {

    public EnclosingClosurePointcut(String containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        ClosureExpression enclosing = pattern.getCurrentScope().getEnclosingClosure();
        if (enclosing == null) {
            return null;
        }
        return new BindingSet(enclosing);
    }

    /**
     * expecting one arg that is either a string or a pointcut or one class
     */
    @Override
    public void verify() throws PointcutVerificationException {
        String args = hasNoArgs();
        if (args != null) {
            throw new PointcutVerificationException(args, this);
        }
        super.verify();
    }
}