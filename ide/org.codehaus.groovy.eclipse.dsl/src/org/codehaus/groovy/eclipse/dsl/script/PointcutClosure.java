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
package org.codehaus.groovy.eclipse.dsl.script;

import java.util.Collection;
import java.util.Map;

import groovy.lang.Closure;

import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;

/**
 * Used only for testing now.
 */
public class PointcutClosure extends Closure<Object> {
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
    @SuppressWarnings("unchecked")
    public Object call(Object arguments) {
        if (arguments instanceof Map) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) arguments).entrySet()) {
                Object key = entry.getKey();
                pointcut.addArgument(key == null ? null : key.toString(), entry.getValue());
            }
        } else if (arguments instanceof Collection) {
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
    public Object call(Object... args) {
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
