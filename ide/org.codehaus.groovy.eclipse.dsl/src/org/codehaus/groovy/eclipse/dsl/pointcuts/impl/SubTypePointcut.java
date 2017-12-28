/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * Converts the object toMatch into a type and then sees if it is a subtype.
 * If there is an inner pointcut, then all super types are passed on to the inner pointcut.
 * Think of it this way..."Is the current type a subtype of something that has these characteristics"?
 */
public class SubTypePointcut extends FilteringPointcut<ClassNode> {

    private Map<ClassNode, Set<ClassNode>> cachedHierarchies = new HashMap<>();

    public SubTypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, ClassNode.class);
    }

    /**
     * Converts toMatch to a collection of property nodes.  Might be null or empty list
     * In either of these cases, this is considered a non-match
     * @param toMatch the object to explode
     */
    @Override
    protected Collection<ClassNode> explodeObject(Object toMatch) {
        if (toMatch instanceof Collection) {
            Collection<ClassNode> classes = new LinkedHashSet<>();
            for (Object obj : (Collection<?>) toMatch) {
                if (obj instanceof ClassNode) {
                    classes.addAll(getAllSupers((ClassNode) obj));
                }
            }
            return classes;
        } else if (toMatch instanceof ClassNode) {
            return new LinkedHashSet<>(getAllSupers(((ClassNode) toMatch)));
        }
        return null;
    }

    @Override
    protected ClassNode filterObject(ClassNode result, GroovyDSLDContext context, String firstArgAsString) {
        if (firstArgAsString == null || result.getName().equals(firstArgAsString)) {
            return result;
        } else {
            return null;
        }
    }

    private Set<ClassNode> getAllSupers(ClassNode type) {
        Set<ClassNode> cached = cachedHierarchies.get(type);
        if (cached == null) {
            cached = new HashSet<>();
            internalGetAllSupers(type, cached);
            cachedHierarchies.put(type, cached);
        }
        return cached;
    }

    private void internalGetAllSupers(ClassNode type, Set<ClassNode> set) {
        if (type == null) {
            return;
        }
        set.add(type);
        internalGetAllSupers(type.getSuperClass(), set);
        for (ClassNode inter : type.getAllInterfaces()) {
            if (! inter.getName().equals(type.getName())) {
                internalGetAllSupers(inter, set);
            }
        }
    }
}
