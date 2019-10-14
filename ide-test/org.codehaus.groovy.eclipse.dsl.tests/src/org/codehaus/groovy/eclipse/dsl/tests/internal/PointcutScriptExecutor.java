/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.tests.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.script.PointcutClosure;
import org.codehaus.groovy.eclipse.dsl.script.PointcutFactory;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Takes a pointcut expression and evaluates it against a module node.
 */
public class PointcutScriptExecutor {

    private static final String DEFAULT_SCRIPT_NAME = "GeneratedPointcutScriptExecutor";

    private final PointcutFactory factory = new PointcutFactory(
        ResourcesPlugin.getWorkspace().getRoot().getProject("Project").getFile(".project"),
        ResourcesPlugin.getWorkspace().getRoot().getProject("Project"));

    public IPointcut createPointcut(String expression) {
        GroovyCodeSource source = new GroovyCodeSource(expression, DEFAULT_SCRIPT_NAME, GroovyShell.DEFAULT_CODE_BASE);
        Script script = new GroovyShell(this.getClass().getClassLoader()).parse(source);
        script.setBinding(new PoincutBinding());
        Object res = script.run();
        return res instanceof IPointcut ? (IPointcut) res : null;
    }

    @SuppressWarnings("rawtypes")
    protected Object tryRegister(Object args) {
        Object[] nameAndClass = extractArgsForRegister(args);
        if (nameAndClass != null) {
            factory.registerLocalPointcut((String) nameAndClass[0], (Closure) nameAndClass[1]);
            return nameAndClass[1];
        } else {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Cannot register custom pointcut for " +
                    (args instanceof Object[] ? Arrays.toString((Object[]) args) : args));
            return null;
        }
    }

    protected Object[] extractArgsForRegister(Object args) {
        if (args instanceof Object[]) {
            Object[] arr = (Object[]) args;
            if (arr.length == 2 && arr[0] instanceof String && arr[1] instanceof Closure) {
                return arr;
            }
        } else if (args instanceof Collection) {
            Collection<?> coll = (Collection<?>) args;
            if (coll.size() == 2) {
                Iterator<?> iter = coll.iterator();
                return extractArgsForRegister(new Object[] {iter.next(), iter.next()});
            }
        } else if (args instanceof Map) {
            return extractArgsForRegister(((Map<?, ?>) args).values());
        }
        return null;
    }

    private final class RegisterClosure extends Closure<Object> {
        private static final long serialVersionUID = 1162731585734041055L;

        RegisterClosure(Object owner) {
            super(owner);
        }

        @Override
        public Object call(Object arguments) {
            return tryRegister(arguments);
        }

        @Override
        public Object call(Object... arguments) {
            return tryRegister(arguments);
        }
    }

    private final class PoincutBinding extends Binding {
        @Override
        public Object invokeMethod(String name, Object args) {
            if ("registerPointcut".equals(name)) {
                return tryRegister(args);
            }

            IPointcut pc = factory.createPointcut(name);
            if (pc != null) {
                configure(pc, args);
                return pc;
            } else {
                return super.invokeMethod(name, args);
            }
        }

        @Override
        public Object getVariable(String name) {
            if ("registerPointcut".equals(name)) {
                return new RegisterClosure(this);
            }

            IPointcut pc = factory.createPointcut(name);
            if (pc != null) {
                return new PointcutClosure(this, pc);
            } else {
                return super.getVariable(name);
            }
        }

        private void configure(IPointcut pointcut, Object arguments) {
            if (arguments instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) arguments).entrySet()) {
                    Object key = entry.getKey();
                    pointcut.addArgument(key == null ? null : key.toString(), entry.getValue());
                }
            } else if (arguments instanceof Collection) {
                for (Object arg : (Collection<?>) arguments) {
                    pointcut.addArgument(arg);
                }
            } else if (arguments instanceof Object[]) {
                for (Object arg : (Object[]) arguments) {
                    pointcut.addArgument(arg);
                }
            } else if (arguments != null) {
                pointcut.addArgument(arguments);
            }
        }
    }
}
