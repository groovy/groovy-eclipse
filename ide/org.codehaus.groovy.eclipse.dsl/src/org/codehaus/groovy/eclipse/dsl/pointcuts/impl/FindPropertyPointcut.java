/*
 * Copyright 2009-2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
        if (toMatch instanceof Collection) {
            Collection<PropertyNode> properties = new ArrayList<PropertyNode>();
            for (Object obj : (Collection<?>) toMatch) {
                if (obj instanceof PropertyNode) {
                    properties.add((PropertyNode) obj);
                } else if (obj instanceof ClassNode) {
                    properties.addAll(((ClassNode) obj).getProperties());
                }
            }
            return properties;
        } else if (toMatch instanceof PropertyNode) {
            return Collections.singleton((PropertyNode) toMatch);
        } else if (toMatch instanceof ClassNode) {
            return new ArrayList<PropertyNode>(((ClassNode) toMatch).getProperties());
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
