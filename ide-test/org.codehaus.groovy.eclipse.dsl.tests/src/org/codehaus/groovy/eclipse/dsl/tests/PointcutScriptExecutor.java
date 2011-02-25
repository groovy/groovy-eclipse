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
package org.codehaus.groovy.eclipse.dsl.tests;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.script.PointcutClosure;
import org.codehaus.groovy.eclipse.dsl.script.PointcutFactory;

/**
 * takes a pointcut expression and evaluates it against a module node.
 * 
 * @author andrew
 * @created Feb 11, 2011
 */
public class PointcutScriptExecutor {
    
    private final class RegisterClosure extends Closure {
        private static final long serialVersionUID = 1162731585734041055L;

        public RegisterClosure(Object owner) {
            super(owner);
        }
        
        @Override
        public Object call(Object arguments) {
            return tryRegister(arguments);
        }
        
        @Override
        public Object call(Object[] arguments) {
            return tryRegister(arguments);
        }
    }

    
    private final class PoincutBinding extends Binding {
        @Override
        public Object invokeMethod(String name, Object args) {
            if (name.equals("registerPointcut")) {
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
            if (name.equals("registerPointcut")) {
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
        }
    }

    private final static String DEFAULT_SCRIPT_NAME = "GeneratedPointcutScriptExecutor";
    
    private final PointcutFactory factory = new PointcutFactory(DEFAULT_SCRIPT_NAME);
    
    public IPointcut createPointcut(String expression) {
        GroovyCodeSource source = new GroovyCodeSource(expression, DEFAULT_SCRIPT_NAME, GroovyShell.DEFAULT_CODE_BASE);
        Script script = new GroovyShell(this.getClass().getClassLoader()).parse(source);
        script.setBinding(new PoincutBinding());
        Object res = script.run();
        return res instanceof IPointcut ? (IPointcut) res : null;
    }

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
        } else if (args instanceof Collection<?>) {
            Collection<Object> coll = (Collection<Object>) args;
            Object[] arr = new Object[2];
            Iterator<Object> iter = coll.iterator();
            if (iter.hasNext() && (arr[0] = iter.next()) instanceof String && 
                iter.hasNext() && (arr[1] = iter.next()) instanceof Closure &&
                !iter.hasNext()) {
                return arr;
            }
        } else if (args instanceof Map<?, ?>) {
            return extractArgsForRegister(((Map<Object, Object>) args).values());
        }
        return null;
    }
}
