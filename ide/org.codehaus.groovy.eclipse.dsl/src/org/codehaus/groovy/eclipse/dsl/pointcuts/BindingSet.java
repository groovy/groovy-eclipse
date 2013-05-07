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
package org.codehaus.groovy.eclipse.dsl.pointcuts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

/**
 * A set of elements bound under the current evaluated pointcut
 * BindingSet 
 * @author andrew
 * @created Feb 10, 2011
 */
public class BindingSet {

    // the table of named bindings built up through contained pointcuts
    private final Map<String, Collection<Object>> namedBindings = new HashMap<String, Collection<Object>>();
    
    
    public BindingSet() {
    }

    /**
     * Augments the existing named binding with the collection value.
     * 
     * Creates the binding if it doesn't exist yet
     * @param name
     * @param value should not be null
     * @return
     */
    public BindingSet addToBinding(String name, Collection<?> value) {
        Collection<Object> binding = namedBindings.get(name);
        if (binding == null) {
            binding = new HashSet<Object>();
            namedBindings.put(name, binding);
        }
        binding.addAll(value);
        return this;
    }

    public Map<String, Collection<Object>> getBindings() {
        return Collections.unmodifiableMap(namedBindings);
    }
    
    public Collection<Object> getBinding(String name) {
        return namedBindings.get(name);
    }
    
    public int size() {
        return namedBindings.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BindingSet [\n");
        for (Entry<String, Collection<Object>> entry : namedBindings.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" : ");
            sb.append(printCollection(entry.getValue()));
        }
        sb.append(']');
        return sb.toString();
    }

    public static String printCollection(Collection<? extends Object> value) {
        StringBuilder sb = new StringBuilder();
        for (Object object : value) {
            sb.append(" [ ");
            sb.append(printValue(object));
            sb.append(" ] ");
        }
        return sb.toString();
    }

    public static String printValue(Object value) {
        if (value instanceof ClassNode) {
            return ((ClassNode) value).getName();
        } else if (value instanceof FieldNode) {
            return ((FieldNode) value).getDeclaringClass().getName() + "." + ((FieldNode) value).getName();
        } else if (value instanceof MethodNode) {
            return ((MethodNode) value).getDeclaringClass().getName() + "." + ((MethodNode) value).getName();
        } else if (value instanceof PropertyNode) {
            return ((PropertyNode) value).getDeclaringClass().getName() + "." + ((PropertyNode) value).getName();
        } else if (value instanceof MethodCallExpression) {
            return ((MethodCallExpression) value).getMethodAsString();
        } else if (value instanceof ASTNode) {
            return ((ASTNode) value).getText();
        } else if (value != null) {
            value.toString();
        }
        return null;
    }
}
