/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
