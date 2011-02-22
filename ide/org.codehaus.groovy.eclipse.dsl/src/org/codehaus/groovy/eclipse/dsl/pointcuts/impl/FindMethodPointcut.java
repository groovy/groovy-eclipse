/*
 * Copyright 2003-2010 the original author or authors.
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
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

/**
 * the match returns true if the pattern passed in has a method with the
 * supplied characteristics (either a name, or another pointcut such as hasAnnotation).
 * @author andrew
 * @created Feb 11, 2011
 */
public class FindMethodPointcut extends FilteringPointcut<MethodNode> {

    public FindMethodPointcut(String containerIdentifier) {
        super(containerIdentifier, MethodNode.class);
    }


    /**
     * extracts fields from the outer binding, or from the current type if there is no outer binding
     * the outer binding should be either a {@link Collection} or a {@link ClassNode}
     */
    protected List<MethodNode> filterOuterBindingByType(GroovyDSLDContext pattern) {
        Object outer = pattern.getOuterPointcutBinding();
        if (outer == null) {
            return pattern.getCurrentType().getMethods();
        } else {
            if (outer instanceof Collection<?>) {
                List<MethodNode> fields = new ArrayList<MethodNode>();
                for (Object elt : (Collection<Object>) outer) {
                    if (elt instanceof MethodNode) {
                        fields.add((MethodNode) elt);
                    }
                }
                return fields;
            } else if (outer instanceof ClassNode) {
                return ((ClassNode) outer).getMethods();
            }
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
