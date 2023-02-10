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
package org.codehaus.groovy.eclipse.dsl.pointcuts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

/**
 * A set of elements bound under the current evaluated pointcut.
 */
public class BindingSet {

    /** the table of named bindings built up through contained pointcuts */
    private final Map<String, Collection<Object>> namedBindings = new HashMap<>();

    public BindingSet() {
    }

    /**
     * Augments the existing named binding with the collection value. Creates
     * the binding if it doesn't exist yet.
     */
    public BindingSet addToBinding(String name, Collection<?> values) {
        Collection<Object> binding = namedBindings.get(name);
        if (binding == null) {
            binding = new ArrayList<>();
            namedBindings.put(name, binding);
        }
        binding.addAll(values);
        return this;
    }

    public Map<String, Collection<Object>> getBindings() {
        return Collections.unmodifiableMap(namedBindings);
    }

    public Collection<Object> getBinding(String name) {
        Collection<Object> values = namedBindings.get(name);
        return values == null ? null : Collections.unmodifiableCollection(values);
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
