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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;

/**
 * Tests that the declaring type of the enclosing call matches the name or type passed in as an argument
 * @author andrew
 * @created Feb 10, 2011
 */
public class EnclosingCallDeclaringTypePointcut extends AbstractPointcut {

    public EnclosingCallDeclaringTypePointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        List<CallAndType> enclosing = pattern.getCurrentScope().getAllEnclosingMethodCallExpressions();
        if (enclosing == null) {
            return null;
        }
        
        Object firstArgument = getFirstArgument();
        if (firstArgument instanceof Class<?>) {
            firstArgument = ((Class<?>) firstArgument).getName();
        }
        if (firstArgument instanceof String) {
            ClassNode matchingType = matchesInCalls(enclosing, (String) firstArgument, pattern);
            if (matchingType != null) {
                return new BindingSet().addDefaultBinding(matchingType);
            } else {
                return null;
            }
        } else {
            pattern.setOuterPointcutBinding(asTypeList(enclosing));
            return matchOnPointcutArgument((IPointcut) firstArgument, pattern);
        }
    }

    private List<ClassNode> asTypeList(List<CallAndType> enclosing) {
        List<ClassNode> types = new ArrayList<ClassNode>(enclosing.size());
        for (CallAndType callAndType : enclosing) {
            types.add(callAndType.declaringType);
        }
        return types;
    }

    private ClassNode matchesInCalls(List<CallAndType> enclosing,
            String typeName, GroovyDSLDContext pattern) {
        for (CallAndType callAndType : enclosing) {
            if (pattern.matchesType(typeName, callAndType.declaringType)) {
                return callAndType.declaringType;
            }
        }
        return null;
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