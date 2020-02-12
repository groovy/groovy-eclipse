/*
 * Copyright 2009-2020 the original author or authors.
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * Converts the object toMatch into a type and then sees if it is a subtype.
 * If there is an inner pointcut, then all super types are passed on to the inner pointcut.
 * Think of it this way..."Is the current type a subtype of something that has these characteristics"?
 */
public class SubTypePointcut extends FilteringPointcut<ClassNode> {

    public SubTypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, ClassNode.class);
    }

    @Override
    protected Collection<ClassNode> explodeObject(Object object) {
        if (object instanceof Collection) {
            Collection<ClassNode> classes = new LinkedHashSet<>();
            for (Object item : (Collection<?>) object) {
                if (item instanceof ClassNode) {
                    classes.addAll(getSuperTypes((ClassNode) item));
                }
            }
            return classes;
        } else if (object instanceof ClassNode) {
            return new LinkedHashSet<>(getSuperTypes(((ClassNode) object)));
        }
        return null;
    }

    @Override
    protected ClassNode filterObject(ClassNode result, GroovyDSLDContext context, String firstArgAsString) {
        if (firstArgAsString == null || firstArgAsString.equals(result.getName())) {
            return result;
        }
        return null;
    }

    private Set<ClassNode> getSuperTypes(ClassNode type) {
        return cachedHierarchies.computeIfAbsent(type, t -> {
            Set<ClassNode> superTypes = new LinkedHashSet<>();
            VariableScope.createTypeHierarchy(type, superTypes, false);
            return superTypes;
        });
    }

    private Map<ClassNode, Set<ClassNode>> cachedHierarchies = new WeakHashMap<>();
}
