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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * the match returns true if the pattern passed in has a method with the
 * supplied characteristics (either a name, or another pointcut such as hasAnnotation).
 * @author andrew
 * @created Feb 11, 2011
 */
public class FindMethodPointcut extends FilteringPointcut<MethodNode> {

    public FindMethodPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, MethodNode.class);
    }


    /**
     * Converts toMatch to a collection of method nodes.  Might be null or empty list
     * In either of these cases, this is considered a non-match
     * @param toMatch the object to explode
     */
    @Override
    protected Collection<MethodNode> explodeObject(Object toMatch) {
        if (toMatch instanceof Collection<?>) {
            List<MethodNode> methods = new ArrayList<MethodNode>();
            for (Object elt : (Collection<?>) toMatch) {
                if (elt instanceof MethodNode) {
                    methods.add((MethodNode) elt);
                } else if (elt instanceof ClassNode) {
                    methods.addAll(((ClassNode) elt).getMethods());
                }
            }
            return methods;
        } else if (toMatch instanceof ClassNode) {
            return ((ClassNode) toMatch).getMethods();
        } else if (toMatch instanceof FieldNode) {
            return Collections.singleton((MethodNode) toMatch);
        }
        return null;
    }
    
    
    @Override
    protected MethodNode filterObject(MethodNode result, GroovyDSLDContext context, String firstArgAsString) {
        if (firstArgAsString == null || result.getName().equals(firstArgAsString)) {
            return result;
        } else {
            return null;
        }
    }
}
