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

import groovy.lang.Closure;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;

/**
 * 
 * @author andrew
 * @created Feb 11, 2011
 */
public class UserExtensiblePointcut extends AbstractPointcut {
    private Closure closure;

    public UserExtensiblePointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    public UserExtensiblePointcut(String containerIdentifier, Closure closure) {
        super(containerIdentifier);
        this.closure = closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }
    
    @Override
    public final BindingSet matches(GroovyDSLDContext pattern) {
        if (closure == null) {
            return null;
        }
        try {
            Object firstArgument = getFirstArgument();
            if (firstArgument instanceof IPointcut) {
               BindingSet set = matchOnPointcutArgument((IPointcut) firstArgument, pattern);
               if (set != null) {
                   firstArgument = set.getDefaultBinding();
               }
            }
            closure.setDelegate(pattern);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            Object result;
            if (firstArgument == null) {
                result = closure.call();
            } else {
                result = closure.call(firstArgument);
            }
            if (result == null) {
                return null;
            } else if (result instanceof BindingSet) {
                return (BindingSet) result;
            } else {
                return new BindingSet(result);
            }
        } catch (Exception e) {
            GroovyLogManager.manager.logException(TraceCategory.DSL, e);
            return null;
        }
    }
}
