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
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * the match returns true if the pattern passed in has a field with the
 * supplied characteristics (either a name, or another pointcut such as hasAnnotation).
 * @author andrew
 * @created Feb 11, 2011
 */
public class FindPropertyPointcut extends FilteringPointcut<PropertyNode> {

    public FindPropertyPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, PropertyNode.class);
    }


    /**
     * Converts toMatch to a collection of property nodes.  Might be null or empty list
     * In either of these cases, this is considered a non-match
     * @param toMatch the object to explode
     */
    @Override
    protected Collection<PropertyNode> explodeObject(Object toMatch) {
        if (toMatch instanceof Collection<?>) {
            List<PropertyNode> properties = new ArrayList<PropertyNode>();
            for (Object elt : (Collection<?>) toMatch) {
                if (elt instanceof PropertyNode) {
                    properties.add((PropertyNode) elt);
                } else if (elt instanceof ClassNode) {
                    properties.addAll(((ClassNode) elt).getProperties());
                }
            }
            return properties;
        } else if (toMatch instanceof ClassNode) {
            return ((ClassNode) toMatch).getProperties();
        } else if (toMatch instanceof PropertyNode) {
            return Collections.singleton((PropertyNode) toMatch);
        }
        return null;
    }
    
    
    @Override
    protected PropertyNode filterObject(PropertyNode result, GroovyDSLDContext context, String firstArgAsString) {
        if (firstArgAsString == null || result.getName().equals(firstArgAsString)) {
            return result;
        } else {
            return null;
        }
    }
}
