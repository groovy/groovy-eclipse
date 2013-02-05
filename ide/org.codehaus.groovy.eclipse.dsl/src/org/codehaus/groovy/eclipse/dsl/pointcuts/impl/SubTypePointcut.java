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
 * 
 * @author andrew
 * @created Apr 20, 2011
 */
public class SubTypePointcut extends FilteringPointcut<ClassNode> {
    
    private Map<ClassNode, Set<ClassNode>> cachedHierarchies = new HashMap<ClassNode, Set<ClassNode>>();

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
        if (toMatch instanceof Collection<?>) {
            Set<ClassNode> classes = new LinkedHashSet<ClassNode>();
            for (Object elt : (Collection<?>) toMatch) {
                if (elt instanceof ClassNode) {
                    classes.addAll(getAllSupers((ClassNode) elt));
                }
            }
            return classes;
        } else if (toMatch instanceof ClassNode) {
            return getAllSupers(((ClassNode) toMatch));
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
            cached = new HashSet<ClassNode>();
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