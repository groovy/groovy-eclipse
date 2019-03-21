/*
 * Copyright 2009-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * the match returns true if the pattern passed in has a method with the
 * supplied characteristics (either a name, or another pointcut such as hasAnnotation).
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
        if (toMatch instanceof Collection) {
            Collection<MethodNode> methods = new ArrayList<>();
            for (Object obj : (Collection<?>) toMatch) {
                if (obj instanceof MethodNode) {
                    methods.add((MethodNode) obj);
                } else if (obj instanceof ClassNode) {
                    methods.addAll(((ClassNode) obj).getMethods());
                }
            }
            return methods;
        } else if (toMatch instanceof MethodNode) {
            return Collections.singleton((MethodNode) toMatch);
        } else if (toMatch instanceof ClassNode) {
            return new ArrayList<>(((ClassNode) toMatch).getMethods());
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
