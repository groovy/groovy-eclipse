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

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Tests that the type being analyzed matches.  The match can
 * either be a string match (ie - the type name),
 * or it can pass the current type to a containing pointcut
 * @author andrew
 * @created Feb 10, 2011
 */
public class EnclosingFieldPointcut extends AbstractPointcut {

    public EnclosingFieldPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        FieldNode enclosing = pattern.getCurrentScope().getEnclosingFieldDeclaration();
        if (enclosing == null) {
            return null;
        }
        
        Object firstArgument = getFirstArgument();
        Collection<FieldNode> enclosingCollection = Collections.singleton(enclosing);
        if (firstArgument instanceof String) {
            if (enclosing.getName().equals(firstArgument)) {
                return enclosingCollection;
            } else {
                return null;
            }
        } else {
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern, enclosingCollection);
        }
    }

    /**
     * expecting one arg that is either a string or a pointcut or a class
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