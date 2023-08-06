/*
 * Copyright 2009-2023 the original author or authors.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import groovy.lang.Closure;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

public class UserExtensiblePointcut extends AbstractPointcut {

    private Closure<?> closure;

    public UserExtensiblePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    public UserExtensiblePointcut(IStorage containerIdentifier, String pointcutName, Closure<?> closure) {
        super(containerIdentifier, pointcutName);
        setClosure(closure);
    }

    public void setClosure(Closure<?> closure) {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        this.closure = closure;
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext context, Object toMatch) {
        if (closure != null) {
            try {
                Object result = null;
                synchronized (closure) {
                    closure.setDelegate(getClosureDelegate(context, toMatch));
                    try {
                        result = closure.call(toMatch);
                    } finally {
                        closure.setDelegate(null);
                    }
                }
                return ensureCollection(result);
            } catch (Exception e) {
                GroovyLogManager.manager.logException(TraceCategory.DSL, e);
            }
        }
        return null;
    }

    @Override
    public void verify() throws PointcutVerificationException {
    }

    //--------------------------------------------------------------------------

    /**
     * Creates a map consisting of the pointcut's named arguments and the bound
     * names of pointcut arguments. Also included is a references to the current
     * type. These contributions are in the meta DSLD at the bottom, like this:
     * <pre>
     * contribute(dsldFile & enclosingCallName('registerPointcut') & inClosure() & isThisType()) {
     *   property name: 'currentType', type: ClassNode
     * }</pre>
     */
    private Object getClosureDelegate(GroovyDSLDContext context, Object toMatch) {
        Map<String, Object> map = new HashMap<>();
        map.put("currentType", context.getCurrentType());
        for (Map.Entry<String, Object> entry : namedArgumentsAsMap().entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (val instanceof IPointcut) {
                Collection<?> matches = matchOnPointcutArgument((IPointcut) val, context, ensureCollection(toMatch));
                if (matches != null && !matches.isEmpty()) {
                    map.put(key, context.getCurrentBinding().getBinding(key));
                } else {
                    map.put(key, null);
                }
            } else {
                map.put(key, val);
            }
        }
        return map;
    }
}
