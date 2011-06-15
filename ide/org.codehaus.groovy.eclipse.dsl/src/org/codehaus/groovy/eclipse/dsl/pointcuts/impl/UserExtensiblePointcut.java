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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * 
 * @author andrew
 * @created Feb 11, 2011
 */
public class UserExtensiblePointcut extends AbstractPointcut {
    @SuppressWarnings("rawtypes")
    private Closure closure;

    public UserExtensiblePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @SuppressWarnings("rawtypes")
    public UserExtensiblePointcut(IStorage containerIdentifier, String pointcutName, Closure closure) {
        super(containerIdentifier, pointcutName);
        setClosure(closure);
    }

    public void setClosure(@SuppressWarnings("rawtypes") Closure closure) {
        this.closure = closure;
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    }
    
    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        if (closure == null) {
            return null;
        }
        try {
            // named arguments are available in pointcut body
            // non-named arguments are not
            // named arguments assigned to pointcuts are also added to the binding
            Map<String, Object> args = namedArgumentsAsMap();
            Map<String,Object> newMap = new HashMap<String, Object>(args.size(), 1.0f);
            for (Entry<String, Object> entry : args.entrySet()) {
                String key = entry.getKey();
                if (entry.getValue() instanceof IPointcut) { 
                    Collection<?> matches = matchOnPointcutArgument((IPointcut) entry.getValue(), pattern, ensureCollection(toMatch));
                    if (matches != null && matches.size() > 0) {
                        newMap.put(key, pattern.getCurrentBinding().getBinding(key));
                    } else {
                        newMap.put(key, null);
                    }
                } else {
                    newMap.put(key, entry.getValue());
                }
            }
            // also ensure that the thing to match is available
//            newMap.put("it2", toMatch);
            Object result = null;
            synchronized(closure) {
                closure.setDelegate(newMap);
                result = closure.call(toMatch);
                closure.setDelegate(null);
            }
            return ensureCollection(result);
        } catch (Exception e) {
            GroovyLogManager.manager.logException(TraceCategory.DSL, e);
            return null;
        }
    }
    
    @Override
    public void verify() throws PointcutVerificationException {
        
    }
}
