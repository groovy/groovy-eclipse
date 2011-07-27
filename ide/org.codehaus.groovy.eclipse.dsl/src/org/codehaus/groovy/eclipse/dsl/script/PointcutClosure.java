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
package org.codehaus.groovy.eclipse.dsl.script;

import groovy.lang.Closure;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;

/**
 * Used only for testing now.
 * @author andrew
 * @created Feb 11, 2011
 */
// in Groovy 1.8, Closure uses a type parameter, but in 1.7 it does not
public class PointcutClosure extends Closure {
    private static final long serialVersionUID = -1L;
    
    private final IPointcut pointcut;
    
    public PointcutClosure(Object owner, IPointcut pointcut) {
        super(owner);
        this.pointcut = pointcut;
    }

    public PointcutClosure(Object owner, Object thisObject, IPointcut pointcut) {
        super(owner, thisObject);
        this.pointcut = pointcut;
    }
    
    @Override
    public Object call(Object arguments) {
        if (arguments instanceof Map<?, ?>) {
            for (Entry<Object, Object> entry : ((Map<Object, Object>) arguments).entrySet()) {
                Object key = entry.getKey();
                pointcut.addArgument(key == null ? null : key.toString(), entry.getValue());
            }
        } else if (arguments instanceof Collection<?>) {
            for (Object arg : (Collection<Object>) arguments) {
                pointcut.addArgument(arg);
            }
        } else if (arguments instanceof Object[]) {
            for (Object arg : (Object[]) arguments) {
                pointcut.addArgument(arg);
            }
        } else if (arguments != null) {
            pointcut.addArgument(arguments);
        }
        return pointcut;
    }
    
    
    @Override
    public Object call(Object[] args) {
        if (args == null) {
            return null;
        }
        if (args.length == 1 && args[0] instanceof Map) {
            return call(args[0]);
        }
        for (Object arg : args) {
            pointcut.addArgument(arg);
        }
        return pointcut;
    }
    
    @Override
    public Object call() {
        return pointcut;
    }
}
