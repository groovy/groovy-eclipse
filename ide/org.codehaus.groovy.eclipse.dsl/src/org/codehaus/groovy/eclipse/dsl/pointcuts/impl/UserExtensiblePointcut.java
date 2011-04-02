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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    @SuppressWarnings("rawtypes")
    private Closure closure;

    public UserExtensiblePointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    @SuppressWarnings("rawtypes")
    public UserExtensiblePointcut(String containerIdentifier, Closure closure) {
        super(containerIdentifier);
        setClosure(closure);
    }

    public void setClosure(@SuppressWarnings("rawtypes") Closure closure) {
        this.closure = closure;
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    }
    
    @Override
    public final BindingSet matches(GroovyDSLDContext pattern) {
        if (closure == null) {
            return null;
        }
        try {
            // iterate through all named pointcut arguments and replace the values
            // with the default binding
            Map<String,Object> args = namedArgumentsAsMap();
            Map<String,Object> newMap = new HashMap<String, Object>(args.size(), 1.0f);
            for (Entry<String, Object> entry : args.entrySet()) {
                if (entry.getValue() instanceof IPointcut) {
                    BindingSet set = matchOnPointcutArgument((IPointcut) entry.getValue(), pattern);
                    if (set != null) {
                        newMap.put(entry.getKey(), set.getDefaultBinding());
                    } else {
                        newMap.put(entry.getKey(), null);
                    }
                }
            }
            closure.setDelegate(pattern);
            Object result = closure.call(newMap);
            if (result == null) {
                return null;
            }
            
            BindingSet resultSet;
            if (result instanceof BindingSet) {
                resultSet = (BindingSet) result;
            } else {
                resultSet = new BindingSet(result);
            }
            
            // now add all of the values from the map args
            resultSet.combineBindings(newMap);
            
            return resultSet;
            
        } catch (Exception e) {
            GroovyLogManager.manager.logException(TraceCategory.DSL, e);
            return null;
        }
    }
}
